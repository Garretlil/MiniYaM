package com.example.miniyam

import Presentation.Home.PlayerViewModel
import Presentation.Home.PulsingCircle
import Presentation.Home.getDuration
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    val sourceBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    }
    return sourceBitmap?.copy(Bitmap.Config.ARGB_8888, true)
}


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
    val factor1=0.85f
    val factor2=0.72f
    val red = (redSum / pixelCount).toInt()
    val green = (greenSum / pixelCount).toInt()
    val blue = (blueSum / pixelCount).toInt()
    val barRed=red + ((255 - red) * factor2).toInt()
    val barGreen=green + ((255 - green) * factor2).toInt()
    val barBlue=blue + ((255 - blue) * factor2).toInt()
    val newR = red + ((255 - red) * factor1).toInt()
    val newG = green + ((255 - green) * factor1).toInt()
    val newB = blue + ((255 - blue) * factor1).toInt()
    return listOf(
        Color(newR,newG,newB),
        Color(barRed,barGreen,barBlue)
    )
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun MiniPlayer(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val currentTrack = viewModel.currentTrack
    val isPlaying = viewModel.isTrackPlaying
    val currentPosition = viewModel.currentPositionMs
    val ac=Color.LightGray

    var trackAverageColor by remember { mutableStateOf(Color(0xFFD2D2D2)) }
    var barAverageColor by remember { mutableStateOf(Color(0xFFB0B0B0)) }

    LaunchedEffect(currentTrack?.imageUri) {
        currentTrack?.imageUri?.let { imageUriString ->
            try {
                val uri = Uri.parse(imageUriString)
                val bitmap = getBitmapFromUri(context, uri)
                val colors=calculateRealAverageColor(bitmap)
                trackAverageColor = colors[0]
                barAverageColor=colors[1]
            } catch (e: Exception) {
                trackAverageColor = Color(0xFFD2D2D2)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xB3F6F6F6))
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
                    .height(70.dp).width(((currentPosition.toDouble()/viewModel.currentTrack!!.duration)*390).dp)
                    .padding(end = 0.dp)
                    .clip(RoundedCornerShape(0.dp))
                    .background(barAverageColor)
            ) {}
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 3.dp),
            ) {
                if (currentTrack != null) {
                    val model = currentTrack.imageUri
                    val painter = rememberAsyncImagePainter(model = model)

                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(55.dp)
                                .padding(5.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = currentTrack.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF6E6E6E),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        Icons.Default.HeartBroken,
                        contentDescription = null,
                        tint = Color(0xFFDC3535),
                        modifier = Modifier.size(25.dp)
                    )

                    Spacer(modifier = Modifier.width(25.dp))

                    if (isPlaying) {
                        Icon(
                            Icons.Default.Pause,
                            modifier = Modifier.clickable {
                                viewModel.pause(currentTrack)
                            },
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            modifier = Modifier.clickable {
                                viewModel.resume(currentTrack)
                            },
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(17.dp))
                    Icon(
                        Icons.Default.SkipNext,
                        modifier = Modifier.clickable {
                            viewModel.playNext()
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(17.dp))

                } else {
                    Spacer(modifier = Modifier.width(35.dp))
                    Icon(
                        Icons.Default.TagFaces,
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
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
