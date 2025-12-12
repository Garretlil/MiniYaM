package com.example.miniyam.Presentation.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.miniyam.BASEURL
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.viewmodels.HomeViewModel
import com.example.miniyam.Presentation.viewmodels.SearchStates
import com.example.miniyam.Presentation.viewmodels.SearchViewModel
import kotlin.collections.get


fun getDuration(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
@Composable
fun PulsingCircle() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Canvas(
        modifier = Modifier
            .size(10.dp)
    ) {
        val radius = size.minDimension / 2 * scale
        drawCircle(
            color = Color(0xFFF8AF6F),
            radius = radius,
            center = center
        )
    }
}

@Composable
fun HomeScreen(playerVM: PlayerViewModel, homeVM: HomeViewModel){
    val queue by homeVM.homeQueue.collectAsState()
    val currentTrack by playerVM.currentTrack.collectAsState()
    val isLoading by remember { homeVM::isLoading }
    val errorMessage by remember { homeVM::errorMessage }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Text(
            text = "Вся музыка",
            modifier = Modifier.padding(top = 60.dp, start = 16.dp).blur(1.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        when (isLoading) {
            SearchStates.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            SearchStates.ERROR -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Произошла ошибка",
                        color = Color.Red,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            homeVM.clearError()
                            homeVM.loadTracks()
                        }
                    ) {
                        Text("Повторить")
                    }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.padding(top = 100.dp)) {
                    items(queue.tracks.size) { index ->
                        val track = queue.tracks[index]
                        val isCurrent = track.id == currentTrack?.id
                        Box(
                            modifier =
                            if (isCurrent) Modifier.background(Color(0xFFEEEDED))
                            else Modifier.background(Color.Transparent)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { homeVM.play(playerVM, track) }) {
                                Box(contentAlignment = Alignment.Center) {
                                    SubcomposeAsyncImage(
                                        model = track.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(13.dp).clip(RoundedCornerShape(8.dp)),
                                        loading = {
                                            CircularProgressIndicator()
                                        },
                                        error = {
                                            Text("Ошибка загрузки")
                                        }
                                    )
                                    if (currentTrack?.id == track.id && playerVM.isTrackPlaying)
                                        PulsingCircle()
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = track.artist,
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFF6E6E6E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowCircleDown, contentDescription = null,
                                    tint = Color(0xFF33961E), modifier = Modifier.size(25.dp)
                                )
                                Spacer(modifier = Modifier.width(15.dp))
                                Text(
                                    text = getDuration(track.duration), fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, color = Color(0xFF6E6E6E)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(165.dp))
                    }
                }
            }
        }
    }
}