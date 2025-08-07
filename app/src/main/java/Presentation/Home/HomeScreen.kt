package Presentation.Home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

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
fun HomeScreen(viewModel: PlayerViewModel = viewModel()){
    val tracks by viewModel.tracks
    val isLoading by remember {viewModel::isLoading}
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart){
        Text(
            text = "Моя музыка",
            modifier = Modifier.padding(top = 25.dp, start = 16.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        if (isLoading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        else {
            LazyColumn (modifier = Modifier.padding(top=60.dp)){
                items(tracks.size) { index ->
                    val track = tracks[index]
                    val painter = rememberAsyncImagePainter(model = track.imageUri)
                    val isCurrent=track.id==viewModel.currentTrack?.id
                    Box(modifier =
                         if(isCurrent) Modifier.background(Color(0xFFEEEDED))
                         else Modifier.background(Color.Transparent) ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.play(track) }) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(13.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop

                                )
                                if (viewModel.currentTrack?.id == track.id && viewModel.isTrackPlaying)
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
            }
        }

    }
}