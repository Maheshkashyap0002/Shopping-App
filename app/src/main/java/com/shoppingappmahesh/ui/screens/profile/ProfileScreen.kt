package com.shoppingappmahesh.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.profile.viewmodel.ProfileViewModel

import com.shoppingappmahesh.ui.components.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember(user) { mutableStateOf(user?.name ?: "") }
    var editEmail by remember(user) { mutableStateOf(user?.email ?: "") }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            isEditMode = false
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        bottomBar = {
            BannerAdView(modifier = Modifier.padding(bottom = 100.dp))
        },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    } else {
                        IconButton(onClick = { isEditMode = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user?.photoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF7F7F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.LightGray
                    )
                }
            } else {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isEditMode) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value =  user?.phone ?: "" ,
                    onValueChange = { },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Picture Edit button
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let { 
                        // In a real app, upload to Firebase Storage first
                        // Simulating direct update for now
                        // viewModel.updatePhoto(it.toString())
                    }
                }

                Button(
                    onClick = { viewModel.updateProfile(editName, editEmail) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text(user?.name ?: "Guest User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(user?.phone ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(40.dp))
                

                ProfileMenuItem(Icons.Default.History, "Order History") {
                    navController.navigate(Screen.OrderHistory.route)
                }
                ProfileMenuItem(Icons.Default.LocationOn, "Addresses") {
                    navController.navigate(Screen.AddressList.route)
                }
                ProfileMenuItem(Icons.Default.AdminPanelSettings, "Admin Panel") {
                    navController.navigate(Screen.AdminDashboard.route)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileMenuItem(Icons.Default.Logout, "Logout", textColor = Color.Red) {
                    viewModel.logout()
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, textColor: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (textColor == Color.Red) Color.Red else Color.Blue)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}