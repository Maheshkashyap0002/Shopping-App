package com.shoppingappmahesh.domain.model

data class CartItem(
    val userId: String = "",
    val productId: String = "",
    val quantity: Int = 1,
    // Including product details for convenience in UI, though Firestore model is minimal
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: String = ""
)