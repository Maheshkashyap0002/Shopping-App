package com.shoppingappmahesh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.shoppingappmahesh.ui.screens.auth.LoginScreen
import com.shoppingappmahesh.ui.screens.auth.OtpScreen
import com.shoppingappmahesh.ui.screens.auth.ProfileSetupScreen
import com.shoppingappmahesh.ui.screens.auth.viewmodel.AuthViewModel
import com.shoppingappmahesh.ui.screens.home.HomeScreen
import com.shoppingappmahesh.ui.screens.home.SplashScreen
import com.shoppingappmahesh.ui.screens.product.ProductDetailsScreen
import com.shoppingappmahesh.ui.screens.cart.CartScreen
import com.shoppingappmahesh.ui.screens.profile.ProfileScreen
import com.shoppingappmahesh.ui.screens.checkout.AddressScreen
import com.shoppingappmahesh.ui.screens.checkout.BookingSuccessScreen
import com.shoppingappmahesh.ui.screens.order.OrderHistoryScreen
import com.shoppingappmahesh.ui.screens.order.OrderDetailsScreen
import com.shoppingappmahesh.ui.screens.admin.AdminDashboard
import com.shoppingappmahesh.ui.screens.admin.AdminAddProductScreen
import com.shoppingappmahesh.ui.screens.search.SearchScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        
        navigation(startDestination = Screen.Login.route, route = "auth_flow") {
            composable(Screen.Login.route) { entry ->
                val parentEntry = remember(entry) {
                    try { navController.getBackStackEntry("auth_flow") } catch (e: Exception) { entry }
                }
                val authViewModel: AuthViewModel = hiltViewModel(parentEntry)
                LoginScreen(navController, authViewModel)
            }
            composable(Screen.Otp.route) { entry ->
                val phoneNumber = entry.arguments?.getString("phoneNumber") ?: ""
                val parentEntry = remember(entry) {
                    try { navController.getBackStackEntry("auth_flow") } catch (e: Exception) { entry }
                }
                val authViewModel: AuthViewModel = hiltViewModel(parentEntry)
                OtpScreen(navController, phoneNumber, authViewModel)
            }
            composable(Screen.ProfileSetup.route) { entry ->
                val parentEntry = remember(entry) {
                    try { navController.getBackStackEntry("auth_flow") } catch (e: Exception) { entry }
                }
                val authViewModel: AuthViewModel = hiltViewModel(parentEntry)
                ProfileSetupScreen(navController, authViewModel)
            }
        }

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(Screen.ProductDetails.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(navController, productId)
        }
        composable(Screen.Cart.route) {
            CartScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Address.route) {
            AddressScreen(navController)
        }
        composable(Screen.BookingSuccess.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            BookingSuccessScreen(navController, orderId, paymentId, amount)
        }
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(navController)
        }
        composable(Screen.OrderDetails.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailsScreen(navController, orderId)
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(navController)
        }
        composable(Screen.AdminAddProduct.route) {
            AdminAddProductScreen(navController)
        }
    }
}