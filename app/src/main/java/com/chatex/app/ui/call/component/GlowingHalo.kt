package com.chatex.app.ui.call.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * تأثير هالة متوهجة مع حركة نابضة
 * @param color لون الهالة
 * @param size حجم الهالة
 * @param pulseRange نطاق النبض (0f - 1f)
 * @param pulseSpeed سرعة النبض (عدد المرات في الثانية)
 * @param pulseCount عدد طبقات الهالة
 * @param content المحتوى الذي سيكون داخل الهالة
 */
@Composable
fun GlowingHalo(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.5f),
    size: Dp = 300.dp,
    pulseRange: ClosedFloatingPointRange<Float> = 0.3f..1f,
    pulseSpeed: Float = 1f,
    pulseCount: Int = 3,
    content: @Composable () -> Unit
) {
    // إنشاء حركة نابضة باستخدام Animatable
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = pulseRange.start,
        targetValue = pulseRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 / pulseSpeed).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // رسم طبقات الهالة النابضة
        for (i in 0 until pulseCount) {
            val layerPulse = remember(pulse, i) {
                // تأخير كل طبقة قليلاً للحصول على تأثير متتالي
                (pulse * (1f - (i * 0.2f))).coerceIn(0f, 1f)
            }
            
            val layerAlpha = remember(pulse, i) {
                // تقليل الشفافية تدريجياً للطبقات الخارجية
                (1f - (i * 0.3f)).coerceIn(0.1f, 1f)
            }
            
            val layerSize = size * (1f + (i * 0.2f) * layerPulse)
            
            Canvas(
                modifier = Modifier
                    .size(layerSize)
                    .drawWithContent {}
            ) {
                // رسم دائرة متدرجة اللون
                val haloColor = color.copy(alpha = color.alpha * layerAlpha)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            haloColor.copy(alpha = 0f),
                            haloColor,
                            haloColor.copy(alpha = 0f)
                        ),
                        radius = size.toPx() / 2
                    ),
                    radius = size.toPx() / 2,
                    blendMode = BlendMode.Screen
                )
                
                // إضافة حدود ناعمة
                drawCircle(
                    color = color.copy(alpha = 0.2f * layerAlpha),
                    radius = size.toPx() / 2,
                    style = Stroke(width = 4.dp.toPx() * layerPulse)
                )
            }
        }
        
        // عرض المحتوى في منتصف الهالة
        content()
    }
}
