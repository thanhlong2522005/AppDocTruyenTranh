
package com.example.appdoctruyentranh

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appdoctruyentranh.PrimaryColor
import com.example.appdoctruyentranh.model.UiState

// ==================== SHIMMER ITEM ====================
@Composable
fun ShimmerStoryItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Spacer(
            modifier = Modifier
                .size(130.dp, 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth(0.8f).shimmerEffect())
            Spacer(modifier = Modifier.height(16.dp).fillMaxWidth(0.9f).shimmerEffect())
            Spacer(modifier = Modifier.height(16.dp).fillMaxWidth(0.6f).shimmerEffect())
        }
    }
}

// ==================== EMPTY STATE ====================
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        buttonText?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onClick?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text(buttonText)
            }
        }
    }
}

// ==================== SMART LIST STATE ====================
@Composable
fun <T> SmartListState(
    uiState: UiState<T>,
    onRetry: () -> Unit,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.Inbox,
    emptyTitle: String = "Chưa có dữ liệu",
    emptyMessage: String = "Hãy thử thêm một vài mục nhé!",
    buttonText: String? = null,
    content: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SmartListState Transition"
        ) { state ->
            when (state) {
                is UiState.Loading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(8) { ShimmerStoryItem() }
                    }
                }
                is UiState.Error -> {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Lỗi kết nối",
                        message = state.message,
                        buttonText = "Thử lại",
                        onClick = onRetry
                    )
                }
                is UiState.Empty -> {
                    EmptyState(
                        icon = emptyIcon,
                        title = emptyTitle,
                        message = emptyMessage,
                        buttonText = buttonText,
                        onClick = onRetry
                    )
                }
                is UiState.Success -> {
                    content(state.data)
                }
            }
        }
    }
}

// ==================== SHIMMER EFFECT ====================
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(translateAnim, translateAnim)
    )

    background(brush)
}