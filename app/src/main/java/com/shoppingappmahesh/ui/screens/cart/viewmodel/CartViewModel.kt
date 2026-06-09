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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val userId: String? = authRepository.getCurrentUserId()

    val cartItems: StateFlow<List<CartItem>> = if (userId != null) {
        cartRepository.getCartItems(userId)
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    val totalAmount: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.productPrice * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun updateQuantity(productId: String, quantity: Int) {
        val uid = userId ?: return
        if (quantity < 1) return
        viewModelScope.launch {
            cartRepository.updateQuantity(uid, productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            cartRepository.removeFromCart(uid, productId)
        }
    }
}