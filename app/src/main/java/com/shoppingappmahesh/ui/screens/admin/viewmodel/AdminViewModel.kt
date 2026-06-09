package com.shoppingappmahesh.ui.screens.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val currentUserId = authRepository.getCurrentUserId() ?: ""

    // Only show products owned by the current user
    val myProducts = productRepository.getProducts().map { list ->
        list.filter { it.userId == currentUserId }
    }

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            // Ensure the product is tagged with the current user's ID
            val productWithUser = product.copy(userId = currentUserId)
            val result = productRepository.addProduct(productWithUser)
            if (result.isSuccess) {
                _uiState.value = AdminUiState.Success("Product added successfully")
            } else {
                _uiState.value = AdminUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun addProductWithImage(product: Product, imageUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            
            // 1. Upload Image
            val uploadResult = productRepository.uploadImage(imageUri)
            if (uploadResult.isSuccess) {
                val imageUrl = uploadResult.getOrNull() ?: ""
                // 2. Add product with image URL and User ID
                val finalProduct = product.copy(
                    userId = currentUserId,
                    images = listOf(imageUrl)
                )
                val result = productRepository.addProduct(finalProduct)
                if (result.isSuccess) {
                    _uiState.value = AdminUiState.Success("Product & Image uploaded successfully!")
                } else {
                    _uiState.value = AdminUiState.Error(result.exceptionOrNull()?.message ?: "Database error")
                }
            } else {
                _uiState.value = AdminUiState.Error(uploadResult.exceptionOrNull()?.message ?: "Image upload failed")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }
}

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}