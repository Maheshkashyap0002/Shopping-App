package com.shoppingappmahesh.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.domain.model.Order
import com.shoppingappmahesh.ui.screens.admin.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    navController: NavHostController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val orders by viewModel.allOrders.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No orders found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    AdminOrderCard(order) {
                        selectedOrder = order
                        showDialog = true
                    }
                }
            }
        }
    }

    if (showDialog && selectedOrder != null) {
        OrderStatusDialog(
            order = selectedOrder!!,
            onDismiss = { showDialog = false },
            onUpdate = { status, estimate ->
                viewModel.updateOrderStatus(selectedOrder!!.orderId, status, estimate)
                showDialog = false
            }
        )
    }
}

@Composable
fun AdminOrderCard(order: Order, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order: #${order.orderId.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                Text(
                    order.status, 
                    color = when(order.status) {
                        "PAID" -> Color(0xFF4CAF50)
                        "SHIPPED" -> Color.Blue
                        "DELIVERED" -> Color(0xFF2E7D32)
                        else -> Color.Red
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Customer: ${order.address?.fullName ?: "Unknown"}", fontSize = 14.sp)
            Text("Amount: ₹${order.amount}", fontWeight = FontWeight.SemiBold, color = Color.Blue)
            
            if (order.deliveryEstimate.isNotEmpty()) {
                Text("Estimate: ${order.deliveryEstimate}", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Update Status")
            }
        }
    }
}

@Composable
fun OrderStatusDialog(
    order: Order,
    onDismiss: () -> Unit,
    onUpdate: (String, String) -> Unit
) {
    var status by remember { mutableStateOf(order.status) }
    var estimate by remember { mutableStateOf(order.deliveryEstimate) }
    val statusOptions = listOf("PAID", "PROCESSING", "SHIPPED", "OUT FOR DELIVERY", "DELIVERED", "CANCELLED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Order Status") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select Status:")
                statusOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = status == option, onClick = { status = option })
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                OutlinedTextField(
                    value = estimate,
                    onValueChange = { estimate = it },
                    label = { Text("Delivery Estimate (e.g. Arriving Today at 11 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdate(status, estimate) }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}