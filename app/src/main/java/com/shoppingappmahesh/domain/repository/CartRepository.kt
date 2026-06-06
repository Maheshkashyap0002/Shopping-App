package com.shoppingappmahesh.domain.repository

import com.shoppingappmahesh.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(userId: String): Flow<List<CartItem>>
    suspend fun addToCart(cartItem: CartItem): Result<Unit>
    suspend fun removeFromCart(userId: String, productId: String): Result<Unit>
    suspend fun updateQuantity(userId: String, productId: String, quantity: Int): Result<Unit>
    suspend fun clearCart(userId: String): Result<Unit>
}