package com.shoppingappmahesh.ui.screens.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.shoppingappmahesh.ui.navigation.Screen
import com.shoppingappmahesh.ui.screens.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController: NavHostController,
    phoneNumber: String,
    viewModel: AuthViewModel
) {
    var otpValue by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val activity = LocalActivity.current
    
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isProfileComplete by viewModel.isProfileComplete.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val timer by viewModel.resendTimer.collectAsState()

    LaunchedEffect(isLoggedIn, isProfileComplete) {
        if (isLoggedIn) {
            if (isProfileComplete) {
                navController.navigate(Screen.Home.route) {
                    popUpTo("auth_flow") { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                navController.navigate(Screen.ProfileSetup.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We have sent the code verification to",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "+91 $phoneNumber",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Premium OTP Input
            OtpInputField(
                otpValue = otpValue,
                onOtpValueChange = {
                    if (it.length <= 6) {
                        otpValue = it
                        if (it.length == 6) {
                            keyboardController?.hide()
                            viewModel.verifyOtp(it)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (timer > 0) {
                Text(
                    text = "Resend code in ${timer}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Resend Code",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                    modifier = Modifier.clickable { 
                        activity?.let { viewModel.resendOtp("+91$phoneNumber", it) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { viewModel.verifyOtp(otpValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            //    enabled = otpValue.length == 6 && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            error?.let {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpValueChange: (String) -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        BasicTextField(
            value = otpValue,
            onValueChange = onOtpValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { index ->
                        val char = when {
                            index >= otpValue.length -> ""
                            else -> otpValue[index].toString()
                        }
                        val isFocused = otpValue.length == index
                        
                        Box(
                            modifier = Modifier
                                .size(width = 48.dp, height = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFocused) Color.Blue.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Color.Blue
                                            else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        )
    }
}