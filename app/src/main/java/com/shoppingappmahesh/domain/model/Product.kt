package com.shoppingappmahesh.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val stock: Int = 0,
    val categoryId: String = "",
    val userId: String = "", // UID of the admin/seller
    val adminPhone: String = "", // Phone number of the admin/seller
    val images: List<String> = emptyList()
)