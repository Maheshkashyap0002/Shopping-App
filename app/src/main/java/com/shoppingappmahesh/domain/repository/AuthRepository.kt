package com.shoppingappmahesh.domain.repository

import com.shoppingappmahesh.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    suspend fun signOut()
    fun getUserDetails(uid: String): Flow<User?>
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun isProfileComplete(uid: String): Boolean
}