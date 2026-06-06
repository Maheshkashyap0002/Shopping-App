package com.shoppingappmahesh.ui.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Category
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        _isLoading.value = true
        repository.getCategories().onEach {
            _categories.value = it
        }.launchIn(viewModelScope)

        repository.getProducts().onEach {
            _allProducts.value = it
            filterProducts(_selectedCategoryId.value)
            _isLoading.value = false
        }.launchIn(viewModelScope)
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
        filterProducts(categoryId)
    }

    private fun filterProducts(categoryId: String) {
        if (categoryId == "all") {
            _products.value = _allProducts.value
        } else {
            _products.value = _allProducts.value.filter { it.categoryId == categoryId }
        }
    }
}