package com.shoppingappmahesh.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.domain.repository.OrderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : OrderRepository {
    override fun getOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val ref = database.getReference("orders").orderByChild("userId").equalTo(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val ref = database.getReference("orders")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun placeOrder(order: Order): Result<String> = try {
        val ref = database.getReference("orders").push()
        val newOrder = order.copy(orderId = ref.key ?: "")
        ref.setValue(newOrder).await()
        Result.success(ref.key ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String, deliveryEstimate: String): Result<Unit> = try {
        val updates = mutableMapOf<String, Any>(
            "status" to status
        )
        if (deliveryEstimate.isNotEmpty()) {
            updates["deliveryEstimate"] = deliveryEstimate
        }
        database.getReference("orders").child(orderId).updateChildren(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}