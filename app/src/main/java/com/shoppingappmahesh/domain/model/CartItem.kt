package com.shoppingappmahesh.domain.model

data class CartItem(
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    // Including product details for convenience in UI
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: String = "",
    val adminPhone: String = "" // Tracks the phone number of the admin who added this product
)