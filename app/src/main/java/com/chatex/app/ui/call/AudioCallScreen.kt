package com.chatex.app.ui.call

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.chatex.app.R
import com.chatex.app.ui.theme.ChateXTheme
import com.chatex.app.ui.theme.Purple40
import com.chatex.app.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioCallScreen(
    onBackClick: () -> Unit,
    viewModel: AudioCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // فرشاة التدرج اللوني للخلفية
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E), // لون داكن جداً
            Color(0xFF16213E), // لون داكن
            Color(0xFF0F3460), // لون متوسط
            Color(0xFF533483)  // لون أرجواني داكن
        )
    )
    
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(padding)
        ) {
            // المحتوى الرئيسي
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // زر الرجوع
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // منطقة معلومات المتصل مع الهالة
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // الهالة النابضة مع صورة المتصل
                    GlowingHalo(
                        color = Purple80.copy(alpha = 0.6f),
                        size = 280.dp,
                        pulseSpeed = 0.8f,
                        pulseRange = 0.3f..0.8f,
                        pulseCount = 3
                    ) {
                        // صورة المتصل
                        val painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(uiState.callerAvatarUrl)
                                .crossfade(true)
                                .build()
                        )
                        
                        Image(
                            painter = painter,
                            contentDescription = "صورة المتصل",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // اسم المتصل
                    Text(
                        text = uiState.callerName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    // مدة المكالمة
                    Text(
                        text = uiState.callDuration,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // شريط التحكم السفلي
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر كتم الصوت
                    CallControlButton(
                        icon = if (uiState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (uiState.isMuted) "إلغاء الكتم" else "كتم",
                        isActive = uiState.isMuted,
                        onClick = { viewModel.toggleMute() }
                    )
                    
                    // زر مكبر الصوت
                    CallControlButton(
                        icon = if (uiState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (uiState.isSpeakerOn) "إيقاف مكبر الصوت" else "تشغيل مكبر الصوت",
                        isActive = uiState.isSpeakerOn,
                        onClick = { viewModel.toggleSpeaker() }
                    )
                    
                    // زر إضافة مكالمة
                    CallControlButton(
                        icon = Icons.Default.AddCall,
                        contentDescription = "إضافة مكالمة",
                        onClick = { viewModel.addCall() },
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // زر إنهاء المكالمة
                    CallControlButton(
                        icon = Icons.Default.CallEnd,
                        contentDescription = "إنهاء المكالمة",
                        onClick = { 
                            viewModel.endCall()
                            onBackClick()
                        },
                        backgroundColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0x40FFFFFF),
    contentColor: Color = Color.White,
    isActive: Boolean = false,
    modifier: Modifier = Modifier.size(56.dp)
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "buttonAlpha"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = if (isActive) Purple40.copy(alpha = 0.5f) 
                       else backgroundColor,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor.copy(alpha = animatedAlpha),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AudioCallScreenPreview() {
    ChateXTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            AudioCallScreen(
                onBackClick = {}
            )
        }
    }
}
