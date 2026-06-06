package com.shoppingappmahesh.ui.screens.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.domain.repository.AuthRepository
import com.shoppingappmahesh.domain.repository.CartRepository
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isAddedToCart = MutableStateFlow(false)
    val isAddedToCart = _isAddedToCart.asStateFlow()

    fun getProductDetails(productId: String) {
        _isLoading.value = true
        productRepository.getProductDetails(productId).onEach {
            _product.value = it
            _isLoading.value = false
        }.launchIn(viewModelScope)
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
                productImage = product.images.firstOrNull() ?: ""
            )
            cartRepository.addToCart(cartItem).onSuccess {
                _isAddedToCart.value = true
            }
        }
    }
}