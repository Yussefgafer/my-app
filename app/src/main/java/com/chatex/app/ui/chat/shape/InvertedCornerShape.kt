package com.chatex.app.ui.chat.shape

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * شكل مخصص لإنشاء زاوية منحنية للداخل (Inverted Corner)
 * @param cornerRadius نصف قطر الانحناء
 * @param cornerSize حجم المنطقة المنحنية
 */
class InvertedCornerShape(
    private val cornerRadius: Dp = 16.dp,
    private val cornerSize: Dp = 32.dp
) : androidx.compose.ui.graphics.Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val cornerSizePx = with(density) { cornerSize.toPx() }

        // إنشاء مسار للشكل الأساسي
        val basePath = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
        }

        // إنشاء مسار للزاوية المنحنية للداخل
        val cornerPath = Path().apply {
            // الانتقال إلى بداية المنحنى
            moveTo(0f, cornerSizePx)
            // رسم القوس للداخل
            arcTo(
                rect = Rect(
                    -cornerRadiusPx,
                    -cornerRadiusPx,
                    cornerRadiusPx * 2,
                    cornerRadiusPx * 2
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // إغلاق المسار
            lineTo(0f, 0f)
            close()
        }

        // دمج المسارين معاً
        val resultPath = Path.combine(
            operation = PathOperation.Difference,
            path1 = basePath,
            path2 = cornerPath
        )

        return Outline.Generic(resultPath)
    }
}
