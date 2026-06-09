package com.shoppingappmahesh.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Otp : Screen("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp/$phoneNumber"
    }
    data object ProfileSetup : Screen("profile_setup")
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Chat : Screen("chat")
    data object ProductDetails : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
    data object Address : Screen("address")
    data object BookingSuccess : Screen("booking_success/{orderId}/{paymentId}/{amount}") {
        fun createRoute(orderId: String, paymentId: String, amount: Double) = 
            "booking_success/$orderId/$paymentId/$amount"
    }
    data object OrderHistory : Screen("order_history")
    data object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    data object AdminDashboard : Screen("admin_dashboard")
    data object AdminAddProduct : Screen("admin_add_product?productId={productId}") {
        fun createRoute(productId: String? = null) = if (productId != null) "admin_add_product?productId=$productId" else "admin_add_product"
    }
}