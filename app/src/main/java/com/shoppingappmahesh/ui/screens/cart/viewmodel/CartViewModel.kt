package com.shoppingappmahesh.ui.screens.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount = _totalAmount.asStateFlow()

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        val userId = authRepository.getCurrentUserId() ?: return
        _isLoading.value = true
        cartRepository.getCartItems(userId).onEach { items ->
            _cartItems.value = items
            _totalAmount.value = items.sumOf { it.productPrice * it.quantity }
            _isLoading.value = false
        }.launchIn(viewModelScope)
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val userId = authRepository.getCurrentUserId() ?: return
        if (quantity < 1) return
        viewModelScope.launch {
            cartRepository.updateQuantity(userId, productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            cartRepository.removeFromCart(userId, productId)
        }
    }
}