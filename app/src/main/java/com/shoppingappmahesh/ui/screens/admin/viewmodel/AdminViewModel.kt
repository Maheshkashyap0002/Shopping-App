package com.shoppingappmahesh.ui.screens.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: String = authRepository.getCurrentUserId() ?: ""

    // Use stateIn to make myProducts lifecycle-aware and shared efficiently
    val myProducts: StateFlow<List<Product>> = productRepository.getProducts()
        .map { list -> list.filter { it.userId == userId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            val productWithUser = product.copy(userId = userId)
            val result = productRepository.addProduct(productWithUser)
            _uiState.value = result.fold(
                onSuccess = { AdminUiState.Success("Product added successfully") },
                onFailure = { AdminUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun addProductWithImage(product: Product, imageUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            
            val uploadResult = productRepository.uploadImage(imageUri)
            uploadResult.fold(
                onSuccess = { imageUrl ->
                    val finalProduct = product.copy(
                        userId = userId,
                        images = listOf(imageUrl)
                    )
                    val result = productRepository.addProduct(finalProduct)
                    _uiState.value = result.fold(
                        onSuccess = { AdminUiState.Success("Product & Image uploaded successfully!") },
                        onFailure = { AdminUiState.Error(it.message ?: "Database error") }
                    )
                },
                onFailure = {
                    _uiState.value = AdminUiState.Error(it.message ?: "Image upload failed")
                }
            )
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