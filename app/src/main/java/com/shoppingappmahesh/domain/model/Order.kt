package com.shoppingappmahesh.domain.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val products: List<CartItem> = emptyList(),
    val amount: Double = 0.0,
    val paymentId: String = "",
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val address: Address? = null,
    val paymentMethod: String = "Online",
    val customerEmail: String = "",
    val deliveryEstimate: String = "",
    val adminPhone: String = "" // Phone number of the admin/seller responsible for this order
)