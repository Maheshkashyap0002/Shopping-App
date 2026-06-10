package com.shoppingappmahesh.ui.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppingappmahesh.domain.model.Product
import com.shoppingappmahesh.ui.screens.admin.viewmodel.AdminUiState
import com.shoppingappmahesh.ui.screens.admin.viewmodel.AdminViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminAddProductScreen(
    navController: NavHostController,
    productId: String? = null,
    viewModel: AdminViewModel = hiltViewModel()
) {
    // Filter out the literal template string from NavGraph
    val actualProductId = if (productId == "{productId}") null else productId

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var manualProductId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("10") }
    
    val predefinedCategories = listOf("Clothes", "Mobiles", "Shoes", "Watches", "Laptops", "Groceries", "Home", "Beauty")
    var categoryId by remember { mutableStateOf("Clothes") }
    var manualCategory by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val products by viewModel.myProducts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Load existing product if editing
    LaunchedEffect(actualProductId, products) {
        if (actualProductId != null && products.isNotEmpty()) {
            products.find { it.id == actualProductId }?.let { prod ->
                name = prod.name
                manualProductId = prod.id
                description = prod.description
                price = prod.price.toString()
                discountPrice = prod.discountPrice.toString()
                stock = prod.stock.toString()
                
                if (predefinedCategories.any { it.equals(prod.categoryId, ignoreCase = true) }) {
                    categoryId = prod.categoryId
                    isOtherSelected = false
                } else {
                    categoryId = "Other"
                    manualCategory = prod.categoryId
                    isOtherSelected = true
                }
                
                existingImageUrl = prod.images.firstOrNull()
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AdminUiState.Success -> {
                Toast.makeText(context, (uiState as AdminUiState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.navigateUp()
            }
            is AdminUiState.Error -> {
                Toast.makeText(context, (uiState as AdminUiState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (actualProductId == null) "Add Product" else "Update Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Picker Section
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (existingImageUrl != null) {
                    AsyncImage(
                        model = existingImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(40.dp))
                        Text("Pick Photo", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            OutlinedTextField(
                value = manualProductId,
                onValueChange = { manualProductId = it },
                label = { Text("Product ID (Required for tracking)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. MOBILE_001, LAPTOP_DELL_XPS") },
                enabled = actualProductId == null, // Only allow setting ID for new products
                isError = manualProductId.isEmpty() && actualProductId == null,
                supportingText = { 
                    if (manualProductId.isEmpty() && actualProductId == null) {
                        Text("Unique Product ID is required to prevent merging")
                    }
                }
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Original Price") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = discountPrice, onValueChange = { discountPrice = it }, label = { Text("Discount Price (Optional)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock Quantity") }, modifier = Modifier.fillMaxWidth())
            
            Text("Category:", modifier = Modifier.align(Alignment.Start))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                predefinedCategories.forEach { cat ->
                    FilterChip(
                        selected = categoryId == cat && !isOtherSelected,
                        onClick = { 
                            categoryId = cat
                            isOtherSelected = false
                        },
                        label = { Text(cat) }
                    )
                }
                FilterChip(
                    selected = isOtherSelected,
                    onClick = { 
                        isOtherSelected = true
                        categoryId = "Other"
                    },
                    label = { Text("Other") }
                )
            }

            if (isOtherSelected) {
                OutlinedTextField(
                    value = manualCategory,
                    onValueChange = { manualCategory = it },
                    label = { Text("Enter Custom Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    if (selectedImageUri == null && existingImageUrl == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (actualProductId == null && manualProductId.isBlank()) {
                        Toast.makeText(context, "Please enter a unique Product ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val finalCategory = if (isOtherSelected) manualCategory else categoryId
                    if (finalCategory.isBlank()) {
                        Toast.makeText(context, "Please select or enter a category", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isUploading = true
                    scope.launch {
                        val product = Product(
                            id = actualProductId ?: manualProductId.trim(),
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            discountPrice = discountPrice.toDoubleOrNull() ?: 0.0,
                            stock = stock.toIntOrNull() ?: 0,
                            categoryId = finalCategory,
                            images = if (existingImageUrl != null && selectedImageUri == null) listOf(existingImageUrl!!) else emptyList()
                        )
                        
                        if (selectedImageUri != null) {
                            viewModel.addProductWithImage(product, selectedImageUri!!)
                        } else {
                            viewModel.addProduct(product)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotEmpty() && price.isNotEmpty() && !isUploading && uiState !is AdminUiState.Loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading || uiState is AdminUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (actualProductId == null) "Upload & Save Product" else "Update Product")
                }
            }
        }
    }
}
