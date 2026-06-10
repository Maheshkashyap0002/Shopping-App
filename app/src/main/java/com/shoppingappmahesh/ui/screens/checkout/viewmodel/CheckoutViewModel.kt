package com.shoppingappmahesh.ui.screens.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Address
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.domain.model.User
import com.shoppingappmahesh.domain.repository.AddressRepository
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _orderPlaced = MutableStateFlow<String?>(null) // orderId
    val orderPlaced = _orderPlaced.asStateFlow()

    private val userId: String? = authRepository.getCurrentUserId()

    val currentUser: StateFlow<User?> = if (userId != null) {
        authRepository.getUserDetails(userId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    val selectedAddress: StateFlow<Address?> = if (userId != null) {
        addressRepository.getAddresses(userId).map { list ->
            list.find { it.isDefault } ?: list.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    fun placeOrder(amount: Double, paymentId: String) {
        val uid = userId ?: return
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val cartItems = cartRepository.getCartItems(uid).first()
                if (cartItems.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val order = Order(
                    userId = uid,
                    products = cartItems,
                    amount = amount,
                    paymentId = paymentId,
                    status = "PAID",
                    createdAt = System.currentTimeMillis(),
                    address = selectedAddress.value,
                    adminPhone = cartItems.firstOrNull()?.adminPhone ?: ""
                )

                orderRepository.placeOrder(order).onSuccess { orderId ->
                    cartRepository.clearCart(uid)
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