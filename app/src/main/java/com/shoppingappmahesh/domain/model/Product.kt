package com.shoppingappmahesh.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val stock: Int = 0,
    val categoryId: String = "",
    val userId: String = "", // Added to track owner
    val images: List<String> = emptyList()
)