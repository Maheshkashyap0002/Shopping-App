package com.shoppingappmahesh.domain.repository

import com.shoppingappmahesh.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(userId: String): Flow<List<Order>>
    fun getAllOrders(): Flow<List<Order>>
    suspend fun placeOrder(order: Order): Result<String> // Returns orderId
    suspend fun updateOrderStatus(orderId: String, status: String, deliveryEstimate: String = ""): Result<Unit>
}