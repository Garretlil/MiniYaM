package com.example.miniyam

import Presentation.Home.PlayerViewModel
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.math.sin

fun calculateRealAverageColor(bitmap: Bitmap?): List<Color> {
    val width = bitmap?.width
    val height = bitmap?.height
    var redSum = 0L
    var greenSum = 0L
    var blueSum = 0L
    var pixelCount = 0L
    for (y in 0 until height!! step 5) {
        for (x in 0 until width!! step 5) {
            val pixel = bitmap.getPixel(x, y)
            redSum += android.graphics.Color.red(pixel)
            greenSum += android.graphics.Color.green(pixel)
            blueSum += android.graphics.Color.blue(pixel)
            pixelCount++
        }
    }
    val factor1=0.85f ;val factor2=0.72f ;val factor3=0f
    val red = (redSum / pixelCount).toInt()
    val green = (greenSum / pixelCount).toInt()
    val blue = (blueSum / pixelCount).toInt()
    val barRed=red + ((255 - red) * factor2).toInt()
    val barGreen=green + ((255 - green) * factor2).toInt()
    val barBlue=blue + ((255 - blue) * factor2).toInt()
    val newR = red + ((255 - red) * factor1).toInt()
    val newG = green + ((255 - green) * factor1).toInt()
    val newB = blue + ((255 - blue) * factor1).toInt()
    val newBackR = red + ((255 - red) * factor3).toInt()
    val newBackG = green + ((255 - green) * factor3).toInt()
    val newBackB = blue + ((255 - blue) * factor3).toInt()
    return listOf(
        Color(newR,newG,newB),
        Color(barRed,barGreen,barBlue),
        Color(newBackR,newBackG,newBackB)
    )
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun MiniPlayer(viewModel: PlayerViewModel,onExpand: () -> Unit) {
    val context = LocalContext.current
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel::isTrackPlaying
    val currentPosition by viewModel::currentPositionMs
    val imageUrl = BASEURL + currentTrack?.imageUrl

    var trackAverageColor by remember { mutableStateOf(Color(0xFFE3E1E1)) }
    var barAverageColor by remember { mutableStateOf(Color(0xFFE3E1E1)) }
    var backAvColor by remember { mutableStateOf(Color(0xFFB0B0B0)) }

    LaunchedEffect(imageUrl) {
        imageUrl.let { url ->
            try {
                val imageLoader = ImageLoader.Builder(context)
                    .build()
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap()
                    val colors = calculateRealAverageColor(bitmap)
                    trackAverageColor = colors[0]
                    barAverageColor = colors[1]
                    backAvColor = colors[2]
                }
            } catch (e: Exception) {
                Log.e("ImageLoad", "Error loading image: ${e.message}")
                trackAverageColor = Color(0xFFD2D2D2)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xB3F6F6F6))
            .clickable { onExpand() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(top = 10.dp, start = 12.dp, end = 12.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(trackAverageColor)
        ) {
            if (currentTrack!=null)
            Box(
                modifier = Modifier
                    .height(70.dp).width(((currentPosition.toDouble()/ currentTrack!!.duration)*390).dp)
                    .padding(end = 0.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .background(barAverageColor)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 3.dp),
            ) {
                if (currentTrack != null) {
                    Box(contentAlignment = Alignment.Center) {
                        SubcomposeAsyncImage(
                            model = BASEURL + currentTrack!!.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(55.dp)
                                .padding(5.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            loading = {
                                CircularProgressIndicator()
                            },
                            error = {
                                Text("Ошибка загрузки")
                            }
                        )

                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack!!.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = currentTrack!!.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF6E6E6E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val heights by viewModel.heights.collectAsState()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val reorderedIndices = listOf(3, 1, 2,4)
                        for (i in reorderedIndices) {
                            val animatedHeight by animateDpAsState(
                                targetValue = heights[i].coerceAtLeast(4.dp),
                                animationSpec = tween(100, easing = LinearEasing),
                                label = "",
                            )
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(animatedHeight)
                                    .background(lerp(backAvColor, Color.White,0.4f), RoundedCornerShape(3.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(
                        Icons.Default.HeartBroken,
                        contentDescription = null,
                        tint = Color(0xFFDC3535),
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    if (isPlaying) {
                        Icon(
                            Icons.Default.Pause,
                            modifier = Modifier.clickable {
                                viewModel.pause()
                            },
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            modifier = Modifier.clickable {
                                viewModel.resume()
                            },
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(17.dp))

                } else {
                    Spacer(modifier = Modifier.width(30.dp))
                    PerfectWaveDots()
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "Пока ничего не воспроизводится",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
fun PerfectWaveDots(
    dotSize: Dp = 8.dp,
    color: Color = Color.Gray,
    duration: Int = 1000
) {
    val progress = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WaveDot(dotSize, color, progress.value, 0f)

        WaveDot(dotSize, color, progress.value, 0.33f)

        WaveDot(dotSize, color, progress.value, 0.66f)
    }
}

@Composable
private fun WaveDot(
    size: Dp,
    color: Color,
    baseProgress: Float,
    phase: Float
) {
    val combinedProgress = (baseProgress + phase) % 1f
    val height = sin(combinedProgress * Math.PI ).toFloat() * size.value
    Box(
        modifier = Modifier
            .size(size)
            .offset(y = -height.dp)
            .background(color, CircleShape)
    )
}
