package com.shoppingappmahesh.ui.screens.product

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocalMall
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
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.ui.screens.product.viewmodel.ProductDetailsViewModel
import com.shoppingappmahesh.ui.screens.cart.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavHostController,
    productId: String,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAddedToCart by viewModel.isAddedToCart.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    
    var selectedSize by remember { mutableStateOf("L") }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(productId) {
        viewModel.getProductDetails(productId)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.border(1.dp, Color(0xFFE8E8E8), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }, modifier = Modifier.border(1.dp, Color(0xFFE8E8E8), CircleShape)) {
                        BadgedBox(badge = { 
                            if (cartItems.isNotEmpty()) {
                                Badge(containerColor = Color.Red) { Text(cartItems.size.toString(), color = Color.White) }
                            }
                        }) {
                            Icon(Icons.Outlined.LocalMall, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            product?.let { prod ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    color = Color.Transparent
                ) {
                    Button(
                        onClick = { viewModel.addToCart(prod) },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        enabled = !isAddedToCart
                    ) {
                        Text(if(isAddedToCart) "Already in Cart" else "Add to Cart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Blue)
            }
        } else {
            product?.let { prod ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Main Image with previews
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = prod.images.firstOrNull(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(300.dp)
                                .padding(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF7F7F7))
                                        .border(if(it==0) 2.dp else 0.dp, Color.Black, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(model = prod.images.firstOrNull(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                            }
                        }
                    }
                    
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prod.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            IconButton(onClick = {}, modifier = Modifier.background(Color(0xFFFEECEE), CircleShape)) {
                                Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.Red)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Size", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("S", "M", "L", "XL", "XXL", "3XL").forEach { size ->
                                val isSelected = selectedSize == size
                                Surface(
                                    onClick = { selectedSize = size },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFFDED9FF) else Color(0xFFF7F7F7),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(size, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Color", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(Color.Red, Color.Black, Color.Blue, Color.Green).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(if(selectedColor == color) 2.dp else 0.dp, Color.Gray, CircleShape)
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("₹${prod.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.Red, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                Text("₹${prod.discountPrice.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            }
                            
                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFF7F7F7))
                                    .padding(4.dp)
                            ) {
                                IconButton(onClick = { if(quantity > 1) quantity-- }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                }
                                Text(String.format(Locale.getDefault(), "%02d", quantity), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { quantity++ }, modifier = Modifier.size(36.dp).background(Color.White, CircleShape)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}