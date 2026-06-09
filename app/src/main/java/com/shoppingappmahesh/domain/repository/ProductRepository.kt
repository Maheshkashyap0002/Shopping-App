package com.shoppingappmahesh.domain.repository

import com.shoppingappmahesh.domain.model.Category
import com.shoppingappmahesh.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getCategories(): Flow<List<Category>>
    fun getProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun getProductDetails(productId: String): Flow<Product?>
    suspend fun addProduct(product: Product): Result<Unit>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun uploadImage(uri: android.net.Uri): Result<String>
}