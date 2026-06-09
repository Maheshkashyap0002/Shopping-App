package com.shoppingappmahesh.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.shoppingappmahesh.domain.model.Category
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.home.viewmodel.HomeViewModel
import com.shoppingappmahesh.ui.screens.cart.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = {
            HomeHeader(
                cartItemCount = cartItems.size,
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onCartClick = { navController.navigate(Screen.Cart.route) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Chat.route) },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 110.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat with ShopGPT")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Blue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    // Promo Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PromoCard(
                            title = "Explore All\nProduct",
                            color = Color(0xFFDED9FF),
                            modifier = Modifier.weight(1f)
                        )
                        PromoCard(
                            title = "Top Selling\nProduct",
                            color = Color(0xFFFFF3D9),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category List in normal boxes
                    CategoryBoxSection(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelect = { viewModel.selectCategory(it) }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Popular Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("See all", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }

                items(products) { product ->
                    LuxProductCard(
                        product = product,
                        onClick = { navController.navigate(Screen.ProductDetails.createRoute(product.id)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HomeHeader(cartItemCount: Int, onSearchClick: () -> Unit, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shopping App",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onCartClick) {
                BadgedBox(badge = { 
                    if (cartItemCount > 0) {
                        Badge(containerColor = Color.Red) { 
                            Text(cartItemCount.toString(), color = Color.White) 
                        } 
                    }
                }) {
                    Icon(Icons.Outlined.LocalMall, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun PromoCard(title: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp)
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CategoryBoxSection(
    categories: List<Category>,
    selectedCategoryId: String,
    onCategorySelect: (String) -> Unit
) {
    val cats = listOf(Category("all", "All", "")) + categories
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cats) { cat ->
            val isSelected = selectedCategoryId == cat.id
            Surface(
                onClick = { onCategorySelect(cat.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.height(45.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        cat.name,
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LuxProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Top overlay tags
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(color = Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(20.dp)) {
                    Text("33 Items", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {}, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            
            // Bottom info bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    IconButton(onClick = onClick, modifier = Modifier.size(36.dp).background(Color(0xFFE8E8E8), CircleShape)) {
                        Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}