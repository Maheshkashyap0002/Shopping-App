package com.shoppingappmahesh.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shoppingappmahesh.domain.model.ChatMessage
import com.shoppingappmahesh.domain.model.Participant
import com.shoppingappmahesh.ui.screens.chat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    navController: NavHostController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistory.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    var userMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    // Main layout without Scaffold to avoid redundant padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .statusBarsPadding() // Space for status bar at the top
    ) {
        // Custom Header
        Surface(
            modifier = Modifier.fillMaxWidth().shadow(4.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ShopGPT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }

        // Message List takes remaining space
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeCard()
            }
            
            items(chatHistory) { message ->
                ChatMessageItem(message)
            }

            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Input Area - Managed by imePadding for magnetic keyboard stickiness
        ChatInputArea(
            message = userMessage,
            onMessageChange = { userMessage = it },
            onSendClick = {
                if (userMessage.isNotBlank()) {
                    viewModel.sendMessage(userMessage)
                    userMessage = ""
                }
            },
            isEnabled = !isLoading,
            modifier = Modifier
                .navigationBarsPadding() // Space for gesture bar when keyboard is hidden
                .imePadding() // STICKS TO KEYBOARD when keyboard is shown
        )
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Welcome to Fashion Bajar ✨",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "I am your dedicated shopping concierge. Ask me about products, track orders, or manage your cart.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.participant == Participant.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    
    val userGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2B2B2B), Color(0xFF000000))
    )
    
    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(if (isUser) 4.dp else 2.dp, shape),
            color = if (isUser) Color.Transparent else Color.White,
            shape = shape
        ) {
            Box(
                modifier = Modifier
                    .then(if (isUser) Modifier.background(userGradient) else Modifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isUser) Color.White else Color.Black,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .padding(start = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600
                        0f at index * 100
                        1f at index * 100 + 300
                    },
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .padding(horizontal = 1.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = animatedAlpha))
            )
            if (index < 2) Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
fun ChatInputArea(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(28.dp)),
                placeholder = { Text("Ask ShopGPT ...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFAFAFA),
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    disabledContainerColor = Color(0xFFFAFAFA),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                maxLines = 5,
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.width(12.dp))
            FloatingActionButton(
                onClick = onSendClick,
                containerColor = if (isEnabled && message.isNotBlank()) Color.Black else Color.LightGray,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    Icons.Default.Send, 
                    contentDescription = "Send", 
                    modifier = Modifier.size(24.dp),
                    tint = if (isEnabled && message.isNotBlank()) Color(0xFFFFD700) else Color.White
                )
            }
        }
    }
}