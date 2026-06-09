package com.shoppingappmahesh.ui.screens.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppingappmahesh.domain.model.CartItem
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.cart.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavHostController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.border(1.dp, Color(0xFFE8E8E8), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }, modifier = Modifier.border(1.dp, Color(0xFFE8E8E8), CircleShape)) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 110.dp), // Floating above the bottom nav bar
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, Color(0xFFF1F1F1))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        SummaryRow("Sub Total", "₹$totalAmount")
                        SummaryRow("Shipping", "Free")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F1F1))
                        SummaryRow("Total Amount", "₹$totalAmount", isTotal = true)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { navController.navigate(Screen.AddressList.createRoute(isFromCheckout = true)) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Blue)
            }
        } else if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${cartItems.size} Items", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 220.dp), // Extra bottom padding for floating card
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRowLux(
                            item = item,
                            onUpdateQuantity = { viewModel.updateQuantity(item.productId, it) },
                            onRemove = { viewModel.removeFromCart(item.productId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRowLux(item: CartItem, onUpdateQuantity: (Int) -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.productImage,
            contentDescription = null,
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFF7F7F7)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("₹${item.productPrice.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                
                // Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF7F7F7))
                        .padding(4.dp)
                ) {
                    IconButton(onClick = { if(item.quantity > 1) onUpdateQuantity(item.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onUpdateQuantity(item.quantity + 1) }, modifier = Modifier.size(28.dp).background(Color.White, CircleShape)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if(isTotal) Color.Black else Color.Gray, fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Medium, fontSize = if(isTotal) 18.sp else 14.sp)
        Text(value, fontWeight = if(isTotal) FontWeight.ExtraBold else FontWeight.Bold, fontSize = if(isTotal) 18.sp else 14.sp)
    }
}