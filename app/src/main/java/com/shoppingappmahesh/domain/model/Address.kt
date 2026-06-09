package com.shoppingappmahesh.domain.model

data class Address(
    val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val altPhone: String = "",
    val houseNo: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val country: String = "India",
    val landmark: String = "",
    val addressType: String = "Home", // Home, Work, Other
    val isDefault: Boolean = false
)