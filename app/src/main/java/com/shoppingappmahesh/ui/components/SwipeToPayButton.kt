package com.shoppingappmahesh.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToPayButton(
    amount: Double,
    enabled: Boolean,
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val buttonWidth = 300.dp
    val buttonHeight = 75.dp
    val thumbSize = 56.dp
    val context = LocalContext.current
    val maxOffset = with(density) { (buttonWidth - thumbSize - 8.dp).toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
//            .width(buttonWidth)
            .height(buttonHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(if (enabled) Color.Black else Color.Black)
            .padding(8.dp)
           ,
        contentAlignment = Alignment.CenterStart
    ) {
        // Background Text
        Text(
            text = "Swipe to Pay ₹$amount",
            modifier = Modifier
                .fillMaxWidth()
                .alpha(1f - (offsetX.value / maxOffset)),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        if (enabled) {
                            coroutineScope.launch {
                                val newValue = (offsetX.value + delta).coerceIn(0f, maxOffset)
                                offsetX.snapTo(newValue)
                            }
                        }
                    },
                    onDragStopped = {
                        if (offsetX.value > maxOffset * 0.9f) {
                            coroutineScope.launch {
                                offsetX.animateTo(maxOffset)
                                onSwipeComplete()
                                offsetX.animateTo(0f) // Reset after complete
                            }
                        } else {
                            coroutineScope.launch {
                                offsetX.animateTo(0f)
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (enabled) Color.Black else Color.Gray
            )
        }
    }
}
