package com.shoppingappmahesh.ui.screens.order.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val userId: String? = authRepository.getCurrentUserId()

    val orders: StateFlow<List<Order>> = if (userId != null) {
        orderRepository.getOrders(userId)
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
            .map { list -> list.sortedByDescending { it.createdAt } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } else {
        MutableStateFlow(emptyList())
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return orders.map { list -> list.find { it.orderId == orderId } }
    }
}