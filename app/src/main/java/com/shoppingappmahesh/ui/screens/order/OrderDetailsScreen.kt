package com.shoppingappmahesh.ui.screens.order

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
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
        containerColor = Color(0xFFF7F7F7),
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        order?.let { ord ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    StatusTimelineCard(ord)
                }
                
                item {
                    Text("Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                items(ord.products) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
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
                            Text(item.productName, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${item.quantity} x ₹${item.productPrice}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹${item.quantity * item.productPrice}", fontWeight = FontWeight.Bold)
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("₹${ord.amount}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Blue)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Order Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OrderInfoRow("Order ID", ord.orderId)
                            OrderInfoRow("Payment ID", ord.paymentId)
                            
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            OrderInfoRow("Placed On", sdf.format(Date(ord.createdAt)))
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Support", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${ord.adminPhone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Call Support", fontSize = 12.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        val url = "https://api.whatsapp.com/send?phone=${ord.adminPhone}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(url)
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WhatsApp", fontSize = 12.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = { PdfGenerator.generateInvoice(context, ord) },
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
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OrderInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatusTimelineCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Order Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            if (order.deliveryEstimate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = order.deliveryEstimate,
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val statuses = listOf("PAID", "PROCESSING", "SHIPPED", "OUT FOR DELIVERY", "DELIVERED")
            val currentIndex = statuses.indexOf(order.status).let { if (it == -1) 0 else it }

            statuses.forEachIndexed { index, status ->
                TimelineItem(
                    title = status,
                    isCompleted = index <= currentIndex,
                    isLast = index == statuses.size - 1,
                    time = if (index == currentIndex && order.deliveryEstimate.isNotEmpty()) order.deliveryEstimate else ""
                )
            }
        }
    }
}

@Composable
fun TimelineItem(title: String, isCompleted: Boolean, isLast: Boolean, time: String = "") {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) Color.Blue else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isCompleted) Color.Blue else Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted) Color.Black else Color.Gray,
                fontSize = 14.sp
            )
            if (time.isNotEmpty()) {
                Text(
                    text = time,
                    color = Color.Blue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}