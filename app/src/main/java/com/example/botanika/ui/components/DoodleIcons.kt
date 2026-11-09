package com.example.botanika.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import com.example.botanika.ui.theme.BotanikaGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class DoodleIconType {
    Home, Search, Garden, Calendar, Profile, Settings, Bell, ArrowRight,
    Drop, Sun, Spray, Fertilizer, Camera, AddPlant,
    ArrowLeft, Plus, Minus, Check, Close, Image, Thermometer, Trash, Clock
}

private val DropFill = Color(0xFF5AA2FF)
private val DropStroke = Color(0xFF2F6FD6)
private val SunFill = Color(0xFFF9D45C)
private val SunStroke = Color(0xFFE5A92A)
private val BagFill = Color(0xFFD9A58F)

private fun pathOf(d: String): Path = PathParser().parsePathString(d).toPath()

private object Glyphs {
    val home = pathOf(
        "M2.8,11.6 C6,8.6 9,6 12.1,3.4 C15,6 18,8.7 21.2,11.5 " +
            "M5.4,9.8 C5.3,13 5.5,16.8 5.4,20.2 C9.5,20.6 14.5,20.4 18.6,20.3 C18.5,16.8 18.7,13 18.5,9.8 " +
            "M10,20.4 C9.9,18.4 10.1,16.2 10,14.6 C11.3,14.4 12.7,14.5 14,14.6 C14.1,16.2 13.9,18.4 14,20.4"
    )
    val homeFill = pathOf("M5.4,9.8 C7.6,8 10,5.9 12.1,4.2 C14.2,5.9 16.6,8 18.6,9.8 L18.6,20.3 L5.4,20.3 Z")

    val searchRing = pathOf(
        "M10.5,3.8 C14.4,3.6 17.4,6.8 17.2,10.6 C17,14.4 13.8,17.3 10,17 " +
            "C6.2,16.7 3.5,13.6 3.8,9.9 C4.1,6.4 6.9,3.9 10.5,3.8 Z"
    )
    val searchHandle = pathOf("M15.6,15.4 C17.5,17.3 19.4,19.2 21,20.8")

    val gardenStem = pathOf("M12.1,21.4 C11.8,17.6 12.2,13.4 12.4,9.6")
    val gardenLeafLeft = pathOf("M12,16 C7.6,16.2 4.4,13.2 4.2,9 C8.4,8.8 11.6,11.2 12,16 Z")
    val gardenLeafRight = pathOf("M12.4,10.6 C12.4,6.2 15.4,3.2 20,2.8 C20.4,7.2 17.2,10.6 12.4,10.6 Z")

    val calendarBody = pathOf(
        "M4.6,6.4 C9,6.1 15,6.2 19.4,6.4 C19.6,10.6 19.5,15.4 19.3,19.2 " +
            "C15,19.5 9,19.4 4.7,19.3 C4.4,15.4 4.5,10.6 4.6,6.4 Z"
    )
    val calendarHeaderFill = pathOf("M4.6,6.4 C9,6.1 15,6.2 19.4,6.4 C19.5,7.6 19.5,9 19.4,10.2 C15,10 9,10.1 4.6,10.2 Z")
    val calendarDetails = pathOf(
        "M8.5,3.4 L8.4,8 M15.6,3.4 L15.5,8 M4.6,10.2 C9,10 15,10.1 19.4,10.2 " +
            "M8.3,13.8 L8.35,13.85 M12,13.8 L12.05,13.85 M15.7,13.8 L15.75,13.85 " +
            "M8.3,16.6 L8.35,16.65 M12,16.6 L12.05,16.65 M15.7,16.6 L15.75,16.65"
    )

    val profileRing = pathOf(
        "M12,2.5 C17.3,2.4 21.6,6.6 21.5,12 C21.4,17.4 17.2,21.6 12,21.5 " +
            "C6.6,21.4 2.4,17.2 2.5,11.9 C2.6,6.7 6.8,2.6 12,2.5 Z"
    )
    val profilePerson = pathOf(
        "M12,6.6 C13.6,6.5 14.7,7.8 14.6,9.4 C14.5,10.9 13.5,12 12,12 C10.5,12 9.4,10.8 9.5,9.3 C9.6,7.8 10.6,6.7 12,6.6 Z " +
            "M6.4,18.6 C7.2,15.9 9.3,14.8 12,14.8 C14.7,14.8 16.8,15.9 17.6,18.6"
    )

    val bellBody = pathOf(
        "M12,4 C8.4,4 6.6,6.9 6.6,10.2 C6.6,14 5.6,15.3 4.2,16.8 " +
            "C7.6,17.4 16.4,17.4 19.8,16.8 C18.4,15.3 17.4,14 17.4,10.2 C17.4,6.9 15.6,4 12,4 Z " +
            "M10,19.6 C10.8,20.9 13.2,20.9 14,19.6 M12,2.6 L12,4"
    )

    val arrow = pathOf(
        "M3.5,12.2 C8,11.9 14,12.1 20,12 M14,6 C16.4,8.4 18.4,10.4 20.6,12 C18.4,13.8 16.4,15.8 14,18.2"
    )

    val drop = pathOf(
        "M12,2.8 C15,7 18.4,10 18.4,14.2 C18.4,17.9 15.6,20.8 12,20.8 " +
            "C8.4,20.8 5.6,17.9 5.6,14.2 C5.6,10 9,7 12,2.8 Z"
    )
    val dropShine = pathOf("M9,14.5 C9.2,16.2 10.2,17.4 11.8,17.8")

    val sprayBody = pathOf(
        "M6,10.6 C8.6,10.4 11.4,10.5 14,10.6 C14.2,13.6 14.1,17.4 14,20.6 " +
            "C11.4,20.8 8.6,20.7 6,20.6 C5.9,17.4 5.8,13.6 6,10.6 Z"
    )
    val sprayLabel = pathOf("M6.2,14 C8.6,13.8 11.4,13.9 13.8,14 L13.8,18 C11.4,18.2 8.6,18.1 6.2,18 Z")
    val sprayHead = pathOf("M8.2,10.6 L8.1,8 C9.6,7.8 11.4,7.9 12.8,8 L12.9,10.6 M12.8,6.4 C13.6,6.2 14.6,6.2 15.6,6.4")
    val sprayMist = pathOf(
        "M17.6,5.4 L17.65,5.45 M19.8,4.2 L19.85,4.25 M19.4,7.2 L19.45,7.25 " +
            "M17.8,8.6 L17.85,8.65 M21,6 L21.05,6.05 M21.2,9.2 L21.25,9.25"
    )

    val bagBody = pathOf(
        "M6.6,5.8 C9,5.4 15,5.4 17.4,5.8 C17.8,10 18.4,15.6 18.6,20 " +
            "C14.4,20.6 9.6,20.6 5.4,20 C5.6,15.6 6.2,10 6.6,5.8 Z"
    )
    val bagTop = pathOf("M6.6,5.8 C7.6,3.8 9.4,4.6 12,4 C14.6,4.6 16.4,3.8 17.4,5.8")
    val bagSprout = pathOf(
        "M12,18 C12,15.8 12,13.6 12,11.4 M12,15.2 C10,15.2 8.9,14 8.7,12.4 C10.6,12.4 11.8,13.3 12,15.2 " +
            "M12,12.8 C12,11.2 13.1,10 15.1,9.8 C15.1,11.5 14,12.6 12,12.8"
    )

    val cameraBody = pathOf(
        "M3.2,8.2 C6,8 7.4,7.8 8.2,7.6 C8.9,6.4 9.4,5.2 10,4.6 C11.4,4.4 12.8,4.5 14,4.6 " +
            "C14.6,5.2 15.1,6.4 15.8,7.6 C16.6,7.8 18,8 20.8,8.2 C21,11.6 20.9,15.4 20.7,19.2 " +
            "C15.6,19.5 8.4,19.5 3.4,19.2 C3.1,15.4 3.2,11.6 3.2,8.2 Z"
    )
    val cameraLens = pathOf(
        "M12,9.4 C14.2,9.3 16,11 15.9,13.2 C15.8,15.3 14.1,17 12,17 " +
            "C9.9,17 8.1,15.3 8.2,13.1 C8.3,11 9.9,9.5 12,9.4 Z"
    )

    val addPlantPot = pathOf(
        "M6.4,16.4 C8.6,16.2 11.4,16.3 13.6,16.4 C13.4,18.2 13,19.6 12.6,21 " +
            "C10.4,21.2 9.6,21.2 7.4,21 C7,19.6 6.6,18.2 6.4,16.4 Z"
    )
    val addPlantStem = pathOf("M10,16.4 C9.9,15.2 10.1,14.2 10,13.2")
    val addPlantPetals = pathOf(
        "M10,9.2 C8.6,6.8 9.4,5 10,4.6 C10.6,5 11.4,6.8 10,9.2 Z " +
            "M10,9.2 C7.4,9.4 5.8,8.4 5.4,7.6 C6,6.8 8,6.8 10,9.2 Z " +
            "M10,9.2 C12.6,9.4 14.2,8.4 14.6,7.6 C14,6.8 12,6.8 10,9.2 Z " +
            "M10,9.2 C8.2,11 6.8,11.4 6,11 C6,10 7.4,9.2 10,9.2 Z " +
            "M10,9.2 C11.8,11 13.2,11.4 14,11 C14,10 12.6,9.2 10,9.2 Z"
    )
    val addPlantPlus = pathOf("M19,2.4 L19.1,8.4 M16,5.4 L22,5.5")

    val arrowLeft = pathOf(
        "M20.5,12 C16,11.9 10,12.1 4,12 M10,6 C7.6,8.4 5.6,10.4 3.4,12 C5.6,13.8 7.6,15.8 10,18.2"
    )
    val plus = pathOf("M12,4.5 L12.1,19.5 M4.5,12 L19.5,12.1")
    val minus = pathOf("M4.5,12 C9,11.9 15,12.1 19.5,12")
    val check = pathOf("M4.6,12.6 C7,14.4 8.6,16.2 10,18.4 C13,13 16.4,8.6 20.2,5.4")
    val close = pathOf("M5.4,5.4 L18.6,18.6 M18.5,5.5 L5.5,18.5")

    val imageFrame = pathOf(
        "M3.6,5.2 C9,4.9 15,5 20.4,5.2 C20.6,9.6 20.5,14.4 20.3,18.8 " +
            "C15,19.1 9,19.1 3.7,18.8 C3.4,14.4 3.5,9.6 3.6,5.2 Z"
    )
    val imageHills = pathOf("M3.8,16.4 C6.4,13 8.4,12.6 10.4,15 C12,13 13.6,12.2 15.4,13.2 L20.2,16.8")
    val imageSun = pathOf(
        "M8,8 C8.9,8 9.6,8.7 9.6,9.5 C9.6,10.3 8.9,11 8,11 C7.2,11 6.4,10.3 6.4,9.5 C6.4,8.7 7.2,8 8,8 Z"
    )

    val thermometer = pathOf(
        "M10,13.6 L10,5.2 C10,4 10.8,3.2 12,3.2 C13.2,3.2 14,4 14,5.2 L14,13.6 " +
            "C15.4,14.4 16.2,15.6 16.2,17.2 C16.2,19.4 14.4,21 12,21 C9.6,21 7.8,19.4 7.8,17.2 " +
            "C7.8,15.6 8.6,14.4 10,13.6 Z M12,8 L12,16.6"
    )

    val trash = pathOf(
        "M4.4,6.6 L19.6,6.6 M9.4,6.6 C9.4,4.8 9.8,4 10.6,3.8 L13.4,3.8 C14.2,4 14.6,4.8 14.6,6.6 " +
            "M6.4,6.6 C6.6,10.6 6.8,16 7,20 C10,20.4 14,20.4 17,20 C17.2,16 17.4,10.6 17.6,6.6 " +
            "M10,10.4 L10.1,16.4 M14,10.4 L13.9,16.4"
    )

    val clockRing = pathOf(
        "M12,3 C16.8,2.9 21,7 21,12 C21,16.9 16.9,21.1 12,21 C7.1,20.9 2.9,16.9 3,12 C3.1,7 7.1,3.1 12,3 Z"
    )
    val clockHands = pathOf("M12,7 L12,12.2 L15.6,14.2")

    val gear: Path = Path().apply {
        val teeth = 8
        val profile = listOf(-0.30 to 7.7, -0.17 to 10.0, 0.17 to 10.0, 0.30 to 7.7)
        for (i in 0 until teeth) {
            val base = i * 2 * PI / teeth
            profile.forEachIndexed { j, (offset, radius) ->
                val x = (12 + radius * cos(base + offset)).toFloat()
                val y = (12 + radius * sin(base + offset)).toFloat()
                if (i == 0 && j == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        close()
        addOval(Rect(Offset(12f - 3.3f, 12f - 3.3f), Offset(12f + 3.3f, 12f + 3.3f)))
    }
}

@Composable
fun DoodleIcon(
    type: DoodleIconType,
    modifier: Modifier = Modifier,
    color: Color = BotanikaGreen,
    fill: Color = Color.Transparent
) {
    Canvas(modifier) {
        val unit = size.minDimension / 24f
        scale(unit, unit, pivot = Offset.Zero) {
            val pen = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (type) {
                DoodleIconType.Home -> {
                    drawPath(Glyphs.homeFill, fill)
                    drawPath(Glyphs.home, color, style = pen)
                }

                DoodleIconType.Search -> {
                    drawPath(Glyphs.searchRing, fill)
                    drawPath(Glyphs.searchRing, color, style = pen)
                    drawPath(Glyphs.searchHandle, color, style = pen)
                }

                DoodleIconType.Garden -> {
                    drawPath(Glyphs.gardenLeafLeft, fill)
                    drawPath(Glyphs.gardenLeafRight, fill)
                    drawPath(Glyphs.gardenStem, color, style = pen)
                    drawPath(Glyphs.gardenLeafLeft, color, style = pen)
                    drawPath(Glyphs.gardenLeafRight, color, style = pen)
                }

                DoodleIconType.Calendar -> {
                    drawPath(Glyphs.calendarBody, fill)
                    drawPath(Glyphs.calendarBody, color, style = pen)
                    drawPath(Glyphs.calendarDetails, color, style = pen)
                }

                DoodleIconType.Profile -> {
                    drawPath(Glyphs.profileRing, fill)
                    drawPath(Glyphs.profileRing, color, style = pen)
                    drawPath(Glyphs.profilePerson, color, style = pen)
                }

                DoodleIconType.Settings -> drawPath(Glyphs.gear, color, style = pen)

                DoodleIconType.Bell -> {
                    drawPath(Glyphs.bellBody, color, style = pen)
                    drawCircle(color, radius = 2.7f, center = Offset(18.6f, 5.2f))
                }

                DoodleIconType.ArrowRight -> drawPath(Glyphs.arrow, color, style = pen)

                DoodleIconType.Drop -> {
                    drawPath(Glyphs.drop, DropFill)
                    drawPath(Glyphs.drop, DropStroke, style = pen)
                    drawPath(Glyphs.dropShine, Color.White, style = pen)
                }

                DoodleIconType.Sun -> drawSun()

                DoodleIconType.Spray -> {
                    drawPath(Glyphs.sprayLabel, SprayLabel)
                    drawPath(Glyphs.sprayBody, color, style = pen)
                    drawPath(Glyphs.sprayHead, color, style = pen)
                    drawPath(Glyphs.sprayMist, color, style = Stroke(2.3f, cap = StrokeCap.Round))
                }

                DoodleIconType.Fertilizer -> {
                    drawPath(Glyphs.bagBody, BagFill)
                    drawPath(Glyphs.bagBody, color, style = pen)
                    drawPath(Glyphs.bagTop, color, style = pen)
                    drawPath(Glyphs.bagSprout, color, style = Stroke(1.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }

                DoodleIconType.Camera -> {
                    drawPath(Glyphs.cameraBody, color, style = pen)
                    drawPath(Glyphs.cameraLens, color, style = pen)
                }

                DoodleIconType.AddPlant -> {
                    drawPath(Glyphs.addPlantPot, color, style = pen)
                    drawPath(Glyphs.addPlantStem, color, style = pen)
                    drawPath(Glyphs.addPlantPetals, color, style = pen)
                    drawPath(Glyphs.addPlantPlus, color, style = pen)
                }

                DoodleIconType.ArrowLeft -> drawPath(Glyphs.arrowLeft, color, style = pen)
                DoodleIconType.Plus -> drawPath(Glyphs.plus, color, style = pen)
                DoodleIconType.Minus -> drawPath(Glyphs.minus, color, style = pen)
                DoodleIconType.Check -> drawPath(Glyphs.check, color, style = pen)
                DoodleIconType.Close -> drawPath(Glyphs.close, color, style = pen)

                DoodleIconType.Image -> {
                    drawPath(Glyphs.imageFrame, color, style = pen)
                    drawPath(Glyphs.imageHills, color, style = pen)
                    drawPath(Glyphs.imageSun, color, style = pen)
                }

                DoodleIconType.Thermometer -> {
                    drawPath(Glyphs.thermometer, color, style = pen)
                }

                DoodleIconType.Trash -> drawPath(Glyphs.trash, color, style = pen)

                DoodleIconType.Clock -> {
                    drawPath(Glyphs.clockRing, color, style = pen)
                    drawPath(Glyphs.clockHands, color, style = pen)
                }
            }
        }
    }
}

private val SprayLabel = Color(0xFFC9D6CB)

private fun DrawScope.drawSun() {
    val center = Offset(12f, 12f)
    val rays = Stroke(width = 1.9f, cap = StrokeCap.Round)
    for (i in 0 until 8) {
        val angle = i * PI / 4
        val inner = 7.4f
        val outer = if (i % 2 == 0) 10.8f else 9.8f
        drawLine(
            color = SunStroke,
            start = Offset(center.x + inner * cos(angle).toFloat(), center.y + inner * sin(angle).toFloat()),
            end = Offset(center.x + outer * cos(angle).toFloat(), center.y + outer * sin(angle).toFloat()),
            strokeWidth = rays.width,
            cap = StrokeCap.Round
        )
    }
    drawCircle(SunFill, radius = 4.6f, center = center)
    drawCircle(SunStroke, radius = 4.6f, center = center, style = Stroke(1.7f))
}
