package com.shoppingappmahesh.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shoppingappmahesh.domain.model.Address
import com.shoppingappmahesh.domain.repository.AddressRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : AddressRepository {

    override fun getAddresses(userId: String): Flow<List<Address>> = callbackFlow {
        val ref = database.getReference("addresses").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addresses = snapshot.children.mapNotNull { it.getValue(Address::class.java) }
                trySend(addresses)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun addAddress(address: Address): Result<Unit> = try {
        val ref = database.getReference("addresses").child(address.userId).push()
        val finalAddress = address.copy(id = ref.key ?: "")
        
        if (finalAddress.isDefault) {
            clearOtherDefaults(address.userId)
        }
        
        ref.setValue(finalAddress).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAddress(address: Address): Result<Unit> = try {
        if (address.isDefault) {
            clearOtherDefaults(address.userId)
        }
        database.getReference("addresses")
            .child(address.userId)
            .child(address.id)
            .setValue(address)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAddress(userId: String, addressId: String): Result<Unit> = try {
        database.getReference("addresses").child(userId).child(addressId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setDefaultAddress(userId: String, addressId: String): Result<Unit> = try {
        clearOtherDefaults(userId)
        database.getReference("addresses").child(userId).child(addressId).child("isDefault").setValue(true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun clearOtherDefaults(userId: String) {
        val ref = database.getReference("addresses").child(userId)
        val snapshot = ref.get().await()
        snapshot.children.forEach { child ->
            if (child.child("isDefault").getValue(Boolean::class.java) == true) {
                child.ref.child("isDefault").setValue(false).await()
            }
        }
    }
}