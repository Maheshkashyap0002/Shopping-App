package com.shoppingappmahesh.ui.screens.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _orderPlaced = MutableStateFlow<String?>(null) // orderId
    val orderPlaced = _orderPlaced.asStateFlow()

    fun placeOrder(amount: Double, paymentId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val cartItems = cartRepository.getCartItems(userId).first()
                if (cartItems.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val order = Order(
                    userId = userId,
                    products = cartItems,
                    amount = amount,
                    paymentId = paymentId,
                    status = "PAID",
                    createdAt = System.currentTimeMillis()
                )

                orderRepository.placeOrder(order).onSuccess { orderId ->
                    cartRepository.clearCart(userId)
                    _orderPlaced.value = orderId
                    _isLoading.value = false
                }.onFailure {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}