package com.shoppingappmahesh.ui.screens.checkout

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.cart.viewmodel.CartViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.shoppingappmahesh.ui.screens.checkout.viewmodel.CheckoutViewModel

import com.shoppingappmahesh.ui.components.SwipeToPayButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavHostController,
    cartViewModel: CartViewModel = hiltViewModel(),
    checkoutViewModel: CheckoutViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    val totalAmount by cartViewModel.totalAmount.collectAsState()
    val orderId by checkoutViewModel.orderPlaced.collectAsState()
    val isLoading by checkoutViewModel.isLoading.collectAsState()
    
    val context = LocalContext.current
    var lastPaymentId by remember { mutableStateOf("") }

    val razorpayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val paymentId = result.data?.getStringExtra("payment_id") ?: ""
            lastPaymentId = paymentId
            checkoutViewModel.placeOrder(totalAmount, paymentId)
        }
    }

    LaunchedEffect(orderId) {
        orderId?.let { id ->
            navController.navigate(Screen.BookingSuccess.createRoute(id, lastPaymentId, totalAmount)) {
                popUpTo(Screen.Cart.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Delivery Address", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Blue)
                } else {
                    SwipeToPayButton(
                        amount = totalAmount,
                        enabled = name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty(),
                        onSwipeComplete = {
                            val intent = Intent(context, RazorpayActivity::class.java).apply {
                                putExtra("amount", totalAmount)
                                putExtra("phone", phone)
                                putExtra("email", "customer@example.com")
                            }
                            razorpayLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Shipping Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Flat, House no., Building") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pincode, onValueChange = { pincode = it }, label = { Text("Pincode") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            }
        }
    }
}