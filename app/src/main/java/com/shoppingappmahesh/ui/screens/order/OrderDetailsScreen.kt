package com.shoppingappmahesh.ui.screens.order

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.ui.screens.order.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Download
import com.shoppingappmahesh.util.PdfGenerator
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    navController: NavHostController,
    orderId: String,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val order by viewModel.getOrderById(orderId).collectAsState(initial = null)
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    order?.let { ord ->
                        IconButton(onClick = { PdfGenerator.generateInvoice(context, ord) }) {
                            Icon(Icons.Default.Download, contentDescription = "Download Invoice")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        order?.let { ord ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    StatusCard(ord)
                }
                
                item {
                    Text("Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                items(ord.products) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.productImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.Bold)
                            Text("${item.quantity} x ₹${item.productPrice}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹${item.quantity * item.productPrice}", fontWeight = FontWeight.Bold)
                    }
                }
                
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("₹${ord.amount}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Blue)
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun StatusCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Order Status", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(order.status, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (order.status == "PAID") Color(0xFF4CAF50) else Color.Red)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Order ID", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(order.orderId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment ID", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(order.paymentId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            Text("Placed On", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(sdf.format(Date(order.createdAt)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(24.dp))

            val context = LocalContext.current
            OutlinedButton(
                onClick = { PdfGenerator.generateInvoice(context, order) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Blue)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Blue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download PDF Receipt", color = Color.Blue)
            }
        }
    }
}