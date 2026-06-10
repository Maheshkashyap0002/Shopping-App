package com.shoppingappmahesh.ui.screens.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _productId = MutableStateFlow<String?>(null)

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAddedToCart = MutableStateFlow(false)
    val isAddedToCart: StateFlow<Boolean> = _isAddedToCart.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val product: StateFlow<Product?> = _productId
        .filterNotNull()
        .flatMapLatest { id ->
            productRepository.getProductDetails(id)
                .onStart { _isLoading.value = true }
                .onEach { _isLoading.value = false }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getProductDetails(productId: String) {
        _productId.value = productId
    }

    fun addToCart(product: Product) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val cartItem = CartItem(
                userId = userId,
                productId = product.id,
                quantity = 1,
                productName = product.name,
                productPrice = product.discountPrice,
                productImage = product.images.firstOrNull() ?: "",
                adminPhone = product.adminPhone
            )
            cartRepository.addToCart(cartItem).onSuccess {
                _isAddedToCart.value = true
            }
        }
    }
}