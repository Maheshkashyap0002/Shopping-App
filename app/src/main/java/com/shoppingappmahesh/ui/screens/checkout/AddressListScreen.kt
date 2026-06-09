package com.shoppingappmahesh.ui.screens.checkout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.domain.model.Address
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.checkout.viewmodel.AddressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    navController: NavHostController,
    viewModel: AddressViewModel = hiltViewModel(),
    isFromCheckout: Boolean = false
) {
    val addresses by viewModel.addresses.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Addresses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("add_edit_address") },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Address")
            }
        },
        containerColor = Color(0xFFF7F7F7)
    ) { paddingValues ->
        if (addresses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOff, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = Color.LightGray
                    )
                    Text("No addresses found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(addresses) { address ->
                    AddressCard(
                        address = address,
                        onEdit = { navController.navigate("add_edit_address?addressId=${address.id}") },
                        onDelete = { viewModel.deleteAddress(address.id) },
                        onSetDefault = { viewModel.setDefaultAddress(address.id) },
                        onClick = {
                            if (isFromCheckout) {
                                viewModel.setDefaultAddress(address.id)
                                navController.navigate(Screen.Checkout.route)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (address.isDefault) Color.Blue.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when(address.addressType) {
                        "Home" -> Icons.Default.Home
                        "Work" -> Icons.Default.Work
                        else -> Icons.Default.LocationOn
                    },
                    contentDescription = null,
                    tint = if (address.isDefault) Color.Blue else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = address.addressType,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (address.isDefault) Color.Blue else Color.Black
                )
                if (address.isDefault) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Color.Blue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "DEFAULT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.7f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(address.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${address.houseNo}, ${address.street}", color = Color.DarkGray, fontSize = 14.sp)
            Text("${address.city}, ${address.state} - ${address.pincode}", color = Color.DarkGray, fontSize = 14.sp)
            Text("Phone: ${address.phone}", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            
            if (!address.isDefault) {
                TextButton(
                    onClick = onSetDefault,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Set as default", fontSize = 12.sp, color = Color.Blue)
                }
            }
        }
    }
}
