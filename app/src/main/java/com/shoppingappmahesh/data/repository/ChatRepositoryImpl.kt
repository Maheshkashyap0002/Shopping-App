package com.shoppingappmahesh.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.shoppingappmahesh.domain.model.ChatMessage
import com.shoppingappmahesh.domain.model.Participant
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.ChatRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import com.shoppingappmahesh.di.GeminiApiKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    @param:GeminiApiKey private val apiKey: String
) : ChatRepository {

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    override fun getChatHistory(): Flow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = apiKey,
            systemInstruction = content {
                text(
                    """
                You are ShopGPT, a highly capable AI shopping assistant for 'Fashion Bajar'. 
                Your goal is to provide a personalized and helpful shopping experience.
                
                CORE RULES:
                1. Always use the real data provided in the context (Catalog, Cart, Orders).
                2. Be extremely polite, professional, and helpful.
                3. If a user asks for a product, search through the entire catalog provided.
                4. Highlight savings (Discount Prices) to encourage purchases.
                5. Use Emojis to make the conversation engaging.
                6. When recommending, mention both Original Price and Discount Price if available.
            """.trimIndent()
                )
            }
        )
    }

    private val fallbackModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey,
            systemInstruction = content {
                text(
                    """
                You are ShopGPT, a highly capable AI shopping assistant for 'Fashion Bajar'. 
                Your goal is to provide a personalized and helpful shopping experience.
                
                CORE RULES:
                1. Always use the real data provided in the context (Catalog, Cart, Orders).
                2. Be extremely polite, professional, and helpful.
                3. If a user asks for a product, search through the entire catalog provided.
                4. Highlight savings (Discount Prices) to encourage purchases.
                5. Use Emojis to make the conversation engaging.
                6. When recommending, mention both Original Price and Discount Price if available.
            """.trimIndent()
                )
            }
        )
    }

    override suspend fun sendMessage(userMessage: String): Result<String> {
        _chatHistory.update { it + ChatMessage(userMessage, Participant.USER) }
        
        return try {
            val combinedContext = prepareContext(userMessage)
            
            // Try with primary model (3.5)
            val response = try {
                generativeModel.generateContent(content { text(combinedContext) })
            } catch (e: Exception) {
                if (e.message?.contains("503") == true || e.message?.contains("Unavailable") == true) {
                    Log.w("ShopGPT", "Primary model 3.5 unavailable, switching to fallback 2.5")
                    fallbackModel.generateContent(content { text(combinedContext) })
                } else {
                    throw e
                }
            }
            
            val responseText = response.text ?: "I apologize, but I am unable to process your request at the moment. Please try again."
            
            _chatHistory.update { it + ChatMessage(responseText, Participant.MODEL) }
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e("ShopGPT", "Chat Error", e)
            val friendlyError = when {
                e.message?.contains("503") == true || e.message?.contains("Unavailable") == true -> 
                    "Both AI models are currently busy. Please wait a moment and try again. 🙏"
                else -> "Something went wrong. Please check your internet or try again later."
            }
            _chatHistory.update { it + ChatMessage(friendlyError, Participant.ERROR) }
            Result.failure(e)
        }
    }

    private suspend fun prepareContext(userMessage: String): String {
        // 1. User Profile
        val userId = authRepository.getCurrentUserId() ?: ""
        val user = authRepository.getUserDetails(userId).first()
        val userInfo = if (user != null) {
            "User Profile: Name: ${user.name}, Email: ${user.email}, Phone: ${user.phone}"
        } else {
            "Guest User"
        }

        // 2. Complete Catalog Access with Discount Prices
        val allProducts = productRepository.getProducts().first()
        val productContext = allProducts.joinToString("\n") { 
            "Product: ${it.name} | Original Price: ₹${it.price} | Discount Price: ₹${it.discountPrice} | Stock: ${it.stock} | Category: ${it.categoryId} | Description: ${it.description}" 
        }

        // 3. Current Cart State
        val cartItems = cartRepository.getCartItems(userId).first()
        val cartContext = if (cartItems.isEmpty()) "Cart is empty." else {
            cartItems.joinToString("\n") { "- ${it.productName} (Qty: ${it.quantity}) @ ₹${it.productPrice}" }
        }

        // 4. Order History for Support
        val orders = orderRepository.getOrders(userId).first()
        val orderContext = if (orders.isEmpty()) "No previous orders found." else {
            orders.joinToString("\n") { 
                "OrderID: ${it.orderId} | Status: ${it.status} | Total: ₹${it.amount}" 
            }
        }

        // 5. Support Info
        val supportInfo = "Official Support: maheshkashyap0002@gmail.com | WhatsApp: +91 7724858379"
        
        return """
            $userInfo
            $supportInfo
            
            USER'S CART:
            $cartContext
            
            USER'S ORDERS:
            $orderContext
            
            FULL PRODUCT CATALOG:
            $productContext
            
            User Input: $userMessage
        """.trimIndent()
    }

    override suspend fun clearChat() {
        _chatHistory.value = emptyList()
    }
}