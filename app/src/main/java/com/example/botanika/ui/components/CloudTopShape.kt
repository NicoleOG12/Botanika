package com.example.botanika.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

internal class CloudTopShape(private val bumpHeight: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val bump = with(density) { bumpHeight.toPx() }
        val control = bump / 0.75f
        val base = bump
        val joints = floatArrayOf(0f, 0.30f, 0.73f, 1f)
        val joinHeights = floatArrayOf(0.45f, 1f, 0.8f, 0.55f)

        val path = Path().apply {
            moveTo(0f, base * joinHeights[0])
            for (i in 1 until joints.size) {
                val x0 = size.width * joints[i - 1]
                val y0 = base * joinHeights[i - 1]
                val x1 = size.width * joints[i]
                val y1 = base * joinHeights[i]
                cubicTo(x0, y0 - control, x1, y1 - control, x1, y1)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}
