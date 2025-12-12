package com.example.miniyam.Graphics

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

fun getDiffuse(colorD:Int) = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
    strokeWidth = 30f
    color = colorD
    maskFilter = BlurMaskFilter(70f, BlurMaskFilter.Blur.NORMAL)
}


@Composable
fun GlowLineLeft(diffuseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        drawRect(
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            color = Color(0xFF12A619)
        )

        val composePath = Path().apply {
            moveTo(0f, h * 0.7f)
            quadraticBezierTo(
                w * 0.1f, h * 0.9f,
                w * 0.25f, h * 0.6f
            )
        }

        val androidPath = android.graphics.Path().apply {
            addPath(composePath.asAndroidPath())
        }

        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE

            strokeWidth = 3f

            shader = android.graphics.LinearGradient(
                0f, h * 0.5f,
                w *0.3f, h ,
                intArrayOf(
                    Color.White.copy(alpha = 0.9f).toArgb(),
                    Color.White.copy(alpha = 0.9f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )


            maskFilter = BlurMaskFilter(
                30f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        val linePaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f

            shader = android.graphics.LinearGradient(
                0f, h * 0.7f,
                w * 0.25f, h * 0.6f,
                intArrayOf(
                    diffuseColor.copy(0.5f).toArgb(),
                    diffuseColor.copy(alpha = 0.1f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )


            maskFilter = BlurMaskFilter(
                5f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }

        val diffuseGlowPaint= getDiffuse(diffuseColor.toArgb())

        drawContext.canvas.nativeCanvas.apply {
            save()
            drawPath(androidPath, diffuseGlowPaint)
            drawPath(androidPath, glowPaint)
            drawPath(androidPath, linePaint)
            restore()
        }
    }
}
@Composable
fun GlowLineCenter(diffuseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        val composePath = Path().apply {
            moveTo(w*0.3f, h * 0.99f)
            quadraticBezierTo(
                w * 0.45f, h * 0.7f,
                w * 0.6f, h
            )
        }

        val androidPath = android.graphics.Path().apply {
            addPath(composePath.asAndroidPath())
        }

        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f

            shader = android.graphics.LinearGradient(
                w*0.7f, h*0.99f,
                w *0.4f, h*0.7f ,
                intArrayOf(
                    Color.White.copy(alpha = 0.7f).toArgb(),
                    Color.White.copy(alpha = 0.7f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.7f,1f),
                android.graphics.Shader.TileMode.CLAMP
            )

            maskFilter = BlurMaskFilter(
                15f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        val linePaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f

            shader = android.graphics.LinearGradient(
                w*0.7f, h*0.99f,
                w *0.2f, h*0.7f ,
                intArrayOf(
                    diffuseColor.copy(0.5f).toArgb(),
                    diffuseColor.copy(alpha = 0.1f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.6f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )


            maskFilter = android.graphics.BlurMaskFilter(
                5f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }

        val diffuseGlowPaint= getDiffuse(diffuseColor.toArgb())

        drawContext.canvas.nativeCanvas.apply {
            save()
            drawPath(androidPath, diffuseGlowPaint)
            drawPath(androidPath, glowPaint)
            drawPath(androidPath, linePaint)
            restore()
        }
    }
}
@Composable
fun GlowLineRight(diffuseColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        val w = size.width
        val h = size.height

        val composePath = Path().apply {
            moveTo(w*1f, h * 0.55f)
            quadraticBezierTo(
                w * 0.85f, h * 0.2f,
                w * 0.75f, 0.53f*h
            )
        }

        val androidPath = android.graphics.Path().apply {
            addPath(composePath.asAndroidPath())
        }

        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2f

            shader = android.graphics.LinearGradient(
                w*1f, h*0.55f,
                w *0.75f, 0.53f*h,
                intArrayOf(
                    Color.White.copy(alpha = 0.7f).toArgb(),
                    Color.White.copy(alpha = 0.1f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.7f,1f),
                android.graphics.Shader.TileMode.CLAMP
            )

            maskFilter = BlurMaskFilter(
                15f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        val linePaint = Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f

            shader = android.graphics.LinearGradient(
                w*1f, h*0.55f,
                w *0.75f, 0.53f*h,
                intArrayOf(
                    diffuseColor.copy(0.5f).toArgb(),
                    diffuseColor.copy(alpha = 0.1f).toArgb(),
                    Color.Transparent.toArgb()
                ),
                floatArrayOf(0f, 0.99f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )


            maskFilter = android.graphics.BlurMaskFilter(
                5f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }

        val diffuseGlowPaint= getDiffuse(diffuseColor.toArgb())

        drawContext.canvas.nativeCanvas.apply {
            save()
            drawPath(androidPath, diffuseGlowPaint)
            drawPath(androidPath, glowPaint)
            drawPath(androidPath, linePaint)
            restore()
        }

    }
}
