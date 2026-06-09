package com.shoppingappmahesh.ui.screens.checkout

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.shoppingappmahesh.ui.components.SwipeToPayButton
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.cart.viewmodel.CartViewModel
import com.shoppingappmahesh.ui.screens.checkout.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavHostController,
    cartViewModel: CartViewModel = hiltViewModel(),
    checkoutViewModel: CheckoutViewModel = hiltViewModel()
) {
    val totalAmount by cartViewModel.totalAmount.collectAsState()
    val orderId by checkoutViewModel.orderPlaced.collectAsState()
    val isLoading by checkoutViewModel.isLoading.collectAsState()
    val selectedAddress by checkoutViewModel.selectedAddress.collectAsState()
    val currentUser by checkoutViewModel.currentUser.collectAsState()
    
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

    var finalAmount by remember { mutableStateOf(0.0) }

    LaunchedEffect(orderId) {
        orderId?.let { id ->
            navController.navigate(Screen.BookingSuccess.createRoute(id, lastPaymentId, finalAmount)) {
                popUpTo(Screen.Cart.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        SwipeToPayButton(
                            amount = totalAmount,
                            enabled = selectedAddress != null,
                            onSwipeComplete = {
                                if (selectedAddress != null) {
                                    finalAmount = totalAmount
                                    val intent = Intent(context, RazorpayActivity::class.java).apply {
                                        putExtra("amount", totalAmount)
                                        putExtra("phone", selectedAddress?.phone ?: currentUser?.phone ?: "")
                                        putExtra("email", currentUser?.email ?: "")
                                    }
                                    razorpayLauncher.launch(intent)
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Address Section
            SectionHeader("Shipping Address")
            
            if (selectedAddress != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { navController.navigate(Screen.AddressList.createRoute(isFromCheckout = true)) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(selectedAddress?.addressType) {
                                "Home" -> Icons.Default.Home
                                "Work" -> Icons.Default.Work
                                else -> Icons.Default.LocationOn
                            },
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedAddress?.fullName ?: "", fontWeight = FontWeight.Bold)
                            Text("${selectedAddress?.houseNo}, ${selectedAddress?.street}", fontSize = 14.sp, color = Color.Gray)
                            Text("${selectedAddress?.city}, ${selectedAddress?.state} - ${selectedAddress?.pincode}", fontSize = 14.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            } else {
                Button(
                    onClick = { navController.navigate(Screen.AddressList.createRoute(isFromCheckout = true)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select Delivery Address")
                }
            }

            // Order Summary
            SectionHeader("Order Summary")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = Color.Gray)
                        Text("₹$totalAmount", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shipping", color = Color.Gray)
                        Text("FREE", color = Color(0xFF2D6A4F), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("₹$totalAmount", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
