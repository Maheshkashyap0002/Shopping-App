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

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        val userId = authRepository.getCurrentUserId() ?: return
        _isLoading.value = true
        orderRepository.getOrders(userId).onEach { items ->
            _orders.value = items.sortedByDescending { it.createdAt }
            _isLoading.value = false
        }.launchIn(viewModelScope)
    }

    fun getOrderById(orderId: String): Flow<Order?> {
        return _orders.map { list -> list.find { it.orderId == orderId } }
    }
}