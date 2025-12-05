package com.example.miniyam.Presentation.screens
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.miniyam.BASEURL
import com.example.miniyam.Presentation.Navigation.calculateRealAverageColor
import com.example.miniyam.Presentation.PlayerViewModel
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@SuppressLint("ConfigurationScreenWidthHeight", "RememberReturnType", "UseOfNonLambdaOffsetOverload")
@Composable
fun ExpandedPlayerWithSlideAnimation(onCollapse: () -> Unit,viewModel: PlayerViewModel) {
    val screenHeightD = LocalConfiguration.current.screenHeightDp + 50
    val screenHeight = screenHeightD.dp
    val screenWidthD=LocalConfiguration.current.screenWidthDp+1
    val screenWidth = screenWidthD.dp
    val heightAnim = remember { Animatable(80.dp, Dp.VectorConverter) }
    val offsetYAnim = remember { Animatable(screenHeight, Dp.VectorConverter) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        launch {
            heightAnim.animateTo(
                targetValue = screenHeight,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
        launch {
            offsetYAnim.animateTo(
                targetValue = 0.dp,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .offset(y = offsetYAnim.value)
            .width(screenWidth)
            .height(heightAnim.value)
    ) {
        val context = LocalContext.current
        val currentTrack by viewModel.currentTrack.collectAsState()
        val isPlaying = viewModel.isTrackPlaying
        val currentDuration = viewModel.currentPosSec
        val imageUrl =  currentTrack?.imageUrl
        var trackAverageColor by remember { mutableStateOf(Color(0xFFD2D2D2)) }
        var barAverageColor by remember { mutableStateOf(Color(0xFFB0B0B0)) }
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
            modifier = Modifier.fillMaxSize().background(backAvColor)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 10.dp).size(40.dp).clickable {
                            scope.launch {
                                offsetYAnim.animateTo(
                                    targetValue = screenHeight,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                                onCollapse()
                            }
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(105.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Сейчас играет",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Моя волна", color = Color.White, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.width(60.dp))
                    Icon(
                        Icons.Default.Airplay,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 10.dp).size(25.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(
                        Icons.Default.FormatListNumbered,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 10.dp).size(25.dp),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.height(50.dp))
                if (currentTrack != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model =  currentTrack!!.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(350.dp)
                                .shadow(
                                    elevation = 25.dp,
                                    shape = RoundedCornerShape(10.dp),
                                    spotColor = Color.White.copy(alpha = 0.5f)
                                )
                                .clip(RoundedCornerShape(10.dp)),
                            loading = {
                                CircularProgressIndicator()
                            },
                            error = {
                                Text("Ошибка загрузки")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(25.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (currentTrack != null) {
                        SubcomposeAsyncImage(
                            model =  currentTrack!!.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.2f)),
                            loading = {
                                CircularProgressIndicator()
                            },
                            error = {
                                Text("Ошибка загрузки")
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentTrack!!.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentTrack!!.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.IosShare,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding().size(25.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Icon(
                            Icons.Default.MoreHoriz,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 10.dp).size(25.dp),
                            contentDescription = null
                        )
                    }
                }

                var progress by remember { mutableFloatStateOf(0f) }
                var isDragging by remember { mutableStateOf(false) }
                val interactionSource = remember { MutableInteractionSource() }

                LaunchedEffect(currentDuration, currentTrack?.duration) {
                    if (!isDragging && currentTrack != null && currentTrack!!.duration > 0) {
                        progress = (currentDuration.toFloat() / currentTrack!!.duration).coerceIn(0f, 1f)
                    }
                }
                var circleSize by remember{ mutableStateOf(false) }
                Canvas(
                    modifier = Modifier
                        .padding(horizontal = 25.dp, vertical = 30.dp)
                        .fillMaxWidth()
                        .height(5.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                progress = (offset.x / size.width).coerceIn(0f, 1f)
                                viewModel.seekTo((((progress * currentTrack?.duration!!))).toLong())
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true;circleSize=true },
                                onDragEnd = {
                                    isDragging = false
                                    circleSize=false
                                    viewModel.seekTo((((progress * currentTrack?.duration!!))).toInt().toLong())
                                }
                            ) { change, _ ->
                                progress = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        }
                        .indication(interactionSource, LocalIndication.current)
                ) {
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height / 2),
                        end = Offset(progress*size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (!circleSize) 3.dp.toPx() else 7.dp.toPx(),
                        center = Offset(if (!circleSize) progress*size.width+15 else progress*size.width+23, size.height / 2)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(progress*size.width+30, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-22).dp)
                        .padding(horizontal = 25.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentDuration),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatTime(currentTrack!!.duration.minus(currentDuration)),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(25.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.Default.MoodBad,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 10.dp).size(30.dp),
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(35.dp))
                    Icon(
                        Icons.Default.SkipPrevious,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 10.dp).size(40.dp).clickable {
                            viewModel.playPrev()
                        },
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(20.dp))
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.matchParentSize()
                        ) {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2
                            )
                        }
                        if (isPlaying) {
                            Icon(
                                Icons.Default.Pause,
                                tint = backAvColor,
                                modifier = Modifier
                                    .clickable { viewModel.pause() }
                                    .size(40.dp),
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                tint = backAvColor,
                                modifier = Modifier
                                    .clickable { viewModel.resume() }
                                    .size(40.dp),
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.SkipNext,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 10.dp).size(40.dp).clickable {
                            viewModel.playNext()
                        },
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(35.dp))
                    Icon(
                        imageVector = Icons.Default.HeartBroken,
                        modifier = Modifier.padding(start = 10.dp).size(30.dp).clickable { viewModel.likeTrack(currentTrack!!) },
                        contentDescription = null,
                        tint = if (currentTrack!!.liked) Color(0xFFDC3535) else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
