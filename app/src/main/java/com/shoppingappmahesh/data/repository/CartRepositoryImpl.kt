package com.shoppingappmahesh.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.repository.CartRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : CartRepository {
    override fun getCartItems(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = database.getReference("cart").child(userId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun addToCart(cartItem: CartItem): Result<Unit> = try {
        database.getReference("cart")
            .child(cartItem.userId)
            .child(cartItem.productId)
            .setValue(cartItem)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeFromCart(userId: String, productId: String): Result<Unit> = try {
        database.getReference("cart")
            .child(userId)
            .child(productId)
            .removeValue()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateQuantity(userId: String, productId: String, quantity: Int): Result<Unit> = try {
        database.getReference("cart")
            .child(userId)
            .child(productId)
            .child("quantity")
            .setValue(quantity)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun clearCart(userId: String): Result<Unit> = try {
        database.getReference("cart").child(userId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}