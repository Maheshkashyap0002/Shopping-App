package com.shoppingappmahesh.ui.screens.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.order.viewmodel.OrderViewModel
import com.shoppingappmahesh.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*
import com.shoppingappmahesh.ui.components.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavHostController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BannerAdView(modifier = Modifier.padding(bottom = 100.dp))
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderItemCard(
                        order = order,
                        onClick = {
                            navController.navigate(Screen.OrderDetails.createRoute(order.orderId))
                        },
                        onDownloadClick = {
                            PdfGenerator.generateInvoice(context, order) { uri ->
                                uri?.let { PdfGenerator.openPdf(context, it) }
                            }
                        },
                        onShareClick = {
                            PdfGenerator.generateInvoice(context, order) { uri ->
                                uri?.let { PdfGenerator.sharePdf(context, it) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(
    order: Order,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val firstProduct = order.products.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Product Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = firstProduct?.productImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9F9F9)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = firstProduct?.productName ?: "Product Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2
                    )
                    Text(
                        text = "Qty: ${firstProduct?.quantity ?: 1}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${order.amount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F7FF))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (order.status) {
                            "OUT FOR DELIVERY" -> "Your order is on the way."
                            "DELIVERED" -> "Your order is delivered."
                            else -> order.status
                        },
                        color = Color(0xFF1A73E8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (order.status != "DELIVERED") {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A73E8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A73E8))
                    ) {
                        Text("Track Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom bar with Order ID and Share/Download
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order ID: #${order.orderId.takeLast(8).uppercase()}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Placed on ${sdf.format(Date(order.createdAt))}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDownloadClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}