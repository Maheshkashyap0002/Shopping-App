package com.shoppingappmahesh.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.shoppingappmahesh.ui.components.BannerAdView

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import com.shoppingappmahesh.util.PdfGenerator
import androidx.compose.ui.platform.LocalContext

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId.takeLast(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Receipt",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDownloadClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download Receipt",
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.status,
                    color = if (order.status == "PAID") Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                Text(
                    text = sdf.format(Date(order.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.products.size} items",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${order.amount}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Green.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}