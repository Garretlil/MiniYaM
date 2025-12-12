package com.example.miniyam.Presentation.screens
import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
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
import com.example.miniyam.Presentation.Navigation.calculateRealAverageColor
import com.example.miniyam.Presentation.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin

@SuppressLint("DefaultLocale")
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ConfigurationScreenWidthHeight", "RememberReturnType", "UseOfNonLambdaOffsetOverload",
    "DefaultLocale"
)
@Composable
fun ExpandedPlayerWithSlideAnimation(onCollapse: () -> Unit,viewModel: PlayerViewModel) {
    val screenHeightD = LocalConfiguration.current.screenHeightDp + 50
    val screenHeight = screenHeightD.dp
    val screenWidthD=LocalConfiguration.current.screenWidthDp+1
    val screenWidth = screenWidthD.dp
    val heightAnim = remember { Animatable(80.dp, Dp.VectorConverter) }
    val offsetYAnim = remember { Animatable(screenHeight, Dp.VectorConverter) }
    val scope = rememberCoroutineScope()
    var showTimer by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    LaunchedEffect(showTimer) {
        if (showTimer) sheetState.expand()
    }
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

        val isTimerOn = viewModel.isTimerOn.collectAsState()
        val remainingTime = viewModel.remainingTime.collectAsState()

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
                .fillMaxSize()
                .background(backAvColor)
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
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(40.dp)
                            .clickable {
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
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(
                        Icons.Default.FormatListNumbered,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp),
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
                            modifier = Modifier
                                .padding()
                                .size(25.dp),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Icon(
                            Icons.Default.MoreHoriz,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(25.dp),
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
                                onDragStart = { isDragging = true;circleSize = true },
                                onDragEnd = {
                                    isDragging = false
                                    circleSize = false
                                    viewModel.seekTo(
                                        (((progress * currentTrack?.duration!!))).toInt().toLong()
                                    )
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
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(30.dp),
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(35.dp))
                    Icon(
                        Icons.Default.SkipPrevious,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(40.dp)
                            .clickable {
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
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(40.dp)
                            .clickable {
                                viewModel.playNext()
                            },
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(35.dp))
                    Icon(
                        imageVector = Icons.Default.HeartBroken,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(30.dp)
                            .clickable { viewModel.likeTrack(currentTrack!!) },
                        contentDescription = null,
                        tint = if (currentTrack!!.liked) Color(0xFFDC3535) else Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.Default.Repeat,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp),
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(50.dp))
                    Icon(
                        Icons.Default.Equalizer,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp)
                            .clickable {
                            },
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(40.dp))
                    Icon(
                        Icons.Default.TextFields,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp)
                            .clickable {
                            },
                        contentDescription = null)
                    Spacer(modifier = Modifier.width(35.dp))
                    Box(
                        modifier = Modifier
                            .height(25.dp)
                            .width(55.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.Timelapse,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(25.dp)
                                .clickable { showTimer = true },
                            contentDescription = null
                        )

                        if (isTimerOn.value) {
                            val seconds = (remainingTime.value / 1000).toInt()
                            val text = if (seconds >= 3600) {
                                val hours = seconds / 3600
                                val minutes = (seconds % 3600) / 60
                                val secs = seconds % 60
                                String.format("%02d:%02d:%02d", hours, minutes, secs)
                            } else {
                                val minutes = seconds / 60
                                val secs = seconds % 60
                                String.format("%02d:%02d", minutes, secs)
                            }

                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 22.dp)
                            )
                        }
                    }


                    Spacer(modifier = Modifier.width(30.dp))
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(25.dp)
                            .clickable { },
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
    val scopeModal = rememberCoroutineScope()

    if (showTimer) {
        ModalBottomSheet(
            onDismissRequest = { showTimer = false },
            sheetState = sheetState,
            windowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sleep Timer",
                    modifier = Modifier,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )

                CircularSleepTimer(
                    playerVM = viewModel,
                    hideTimer = {
                        scopeModal.launch {
                            sheetState.hide()
                            showTimer = false
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CircularSleepTimer(
    modifier: Modifier = Modifier,
    onValueChange: (minutes: Int) -> Unit = {},
    playerVM: PlayerViewModel,
    hideTimer: () ->Unit
) {
    val remainingTime = playerVM.remainingTime.collectAsState()
    val isTimerOn = playerVM.isTimerOn.collectAsState()
    val scope = rememberCoroutineScope()

    var totalAngle by remember { mutableFloatStateOf(180f) }
    var lastFingerAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var countTurns by remember { mutableIntStateOf(0) }
    var radius by remember { mutableFloatStateOf(0f) }
    var lastDragEndTime by remember { mutableLongStateOf(0L) }

    var isFirstStart by remember { mutableStateOf(!isTimerOn.value) }
    
    LaunchedEffect(remainingTime.value, isTimerOn.value, isDragging) {
        if (isTimerOn.value && !isDragging) {
            val timeSinceDragEnd = System.currentTimeMillis() - lastDragEndTime
            if (timeSinceDragEnd > 200) {
                val totalMinutesExact = (remainingTime.value / 1000f / 60f).coerceIn(0f, 179f)
                countTurns = (totalMinutesExact / 60f).toInt()
                val minutesInCurrentHour = totalMinutesExact % 60f
                totalAngle = (minutesInCurrentHour / 60f) * 360f
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { size ->
                    radius = (min(size.width, size.height) / 2.5f)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { touch ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val radKnob = Math.toRadians((totalAngle % 360 - 90.0))
                            val knobX = cx + radius * cos(radKnob).toFloat()
                            val knobY = cy + radius * sin(radKnob).toFloat()
                            val dist = (Offset(touch.x, touch.y) - Offset(knobX, knobY)).getDistance()
                            val touchSlop = 24.dp.toPx()
                            if (dist <= touchSlop) {
                                isDragging = true
                                lastFingerAngle = Math.toDegrees(
                                    atan2(
                                        (touch.y - cy).toDouble(),
                                        (touch.x - cx).toDouble()
                                    )
                                ).toFloat()
                                lastFingerAngle = (lastFingerAngle + 90f + 360f) % 360f

                            }
                        },
                        onDrag = { change, _ ->
                            if (!isDragging) return@detectDragGestures

                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val px = change.position.x
                            val py = change.position.y

                            var fingerAngle = Math.toDegrees(atan2((py - cy).toDouble(), (px - cx).toDouble())).toFloat()
                            fingerAngle = (fingerAngle + 90f + 360f) % 360f

                            var delta = fingerAngle - lastFingerAngle
                            if (delta > 180f) delta -= 360f
                            if (delta < -180f) delta += 360f
                            if (countTurns == 0 && totalAngle + delta < 0f) delta = 0f

                            totalAngle += delta
                            lastFingerAngle = fingerAngle

                            if (totalAngle >= 360f) {
                                totalAngle -= 360f
                                if (countTurns < 2) {
                                    countTurns++
                                } else {
                                    totalAngle = 359f
                                }
                            } else if (totalAngle < 0f && countTurns > 0) {
                                totalAngle += 360f
                                countTurns--
                            }

                            val minutesExact = ((countTurns * 60) + (totalAngle / 360f * 60f)).roundToInt()
                            val limitedMinutes = minutesExact.coerceIn(0, 179)
                            
                            if (limitedMinutes < minutesExact) {
                                countTurns = limitedMinutes / 60
                                val minutesInCurrentHour = limitedMinutes % 60
                                totalAngle = (minutesInCurrentHour / 60f) * 360f
                            }
                            
                            if (countTurns > 2) {
                                countTurns = 2
                                totalAngle = 354f
                            }
                            
                            onValueChange(limitedMinutes)

                            change.consume()
                        },
                        onDragEnd = {
                            if (isDragging) {
                                isDragging = false
                                lastDragEndTime = System.currentTimeMillis()
                                val minutesExact = ((countTurns * 60) + (totalAngle / 360f * 60f)).roundToInt().coerceIn(0, 179)
                                val minutesInCurrentHour = minutesExact % 60
                                totalAngle = (minutesInCurrentHour / 60f) * 360f
                                countTurns = minutesExact / 60
                                
                                if (minutesExact > 0) {
                                    if (isTimerOn.value) {
                                        playerVM.startTimer(durationMillis = 0L)
                                        scope.launch {
                                            delay(50)
                                            playerVM.startTimer(durationMillis = minutesExact * 60 * 1000L)
                                        }
                                    } else {
                                        if (!isFirstStart){
                                            playerVM.startTimer(durationMillis = minutesExact * 60 * 1000L)
                                        }
                                    }
                                } else {
                                    playerVM.startTimer(durationMillis = 0L)
                                }
                                onValueChange(minutesExact)
                            }
                        },

                        onDragCancel = {
                            isDragging = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            var isFirstActive by remember { mutableStateOf(false) }
            var isFirstSecondActivate by remember { mutableStateOf(false) }

            LaunchedEffect(countTurns) {
                when {
                    countTurns == 1 -> {
                        isFirstActive = true
                        isFirstSecondActivate = false
                    }
                    countTurns == 2 -> {
                        isFirstActive = true
                        isFirstSecondActivate = true
                    }
                    countTurns > 2 -> {
                        isFirstActive = false
                        isFirstSecondActivate = false
                    }
                    else -> {
                        isFirstActive = false
                        isFirstSecondActivate = false
                    }
                }
            }

            val animatedRadius1 by animateFloatAsState(
                targetValue = if (isFirstActive) radius * 0.8f else radius,
                animationSpec = tween(400)
            )
            val animatedRadius2 by animateFloatAsState(
                targetValue = if (isFirstSecondActivate) radius * 0.6f else radius * 0.8f,
                animationSpec = tween(400)
            )
            val animatedColor1 by animateColorAsState(
                targetValue = if (isFirstActive) Color(0xFFEECD09).copy(0.5f) else Color(0xFFEECD09),
                animationSpec = tween(400)
            )
            val animatedColor2 by animateColorAsState(
                targetValue = if (isFirstSecondActivate) Color(0xFFEECD09).copy(0.3f) else Color(0xFFEECD09).copy(0.5f),
                animationSpec = tween(400)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    style = Stroke(width = 10f),
                    radius = radius,
                    center = Offset(cx, cy)
                )

                val textRadius = radius + 30.dp.toPx()
                for (i in 0 until 12) {
                    val value = i * 5
                    val label = if (value == 0) "0$value" else value.toString()
                    val rad = Math.toRadians((i * 30 - 90).toDouble())
                    val x = cx + textRadius * cos(rad)
                    val y = cy + textRadius * sin(rad) + 12
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            label,
                            x.toFloat(),
                            y.toFloat(),
                            Paint().apply {
                                textAlign = Paint.Align.CENTER
                                color = android.graphics.Color.WHITE
                                textSize = (17.sp.toPx())
                                isAntiAlias = true
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }
                        )
                    }
                }

                val knobRadiusPx = 18.dp.toPx()
                val knobRad = Math.toRadians((totalAngle - 90.0))
                val knobX = cx + radius * cos(knobRad).toFloat()
                val knobY = cy + radius * sin(knobRad).toFloat()

                val sweep = totalAngle.coerceIn(0f, 360f)
                drawArc(
                    color = Color(0xFFEECD09),
                    style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(cx - radius, cy - radius),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    size = Size(radius * 2, radius * 2)
                )

                if (isFirstActive) {
                    drawCircle(
                        color = animatedColor1,
                        radius = animatedRadius1,
                        style = Stroke(width = 10f)
                    )
                }
                if (isFirstSecondActivate) {
                    drawCircle(
                        color = animatedColor2,
                        radius = animatedRadius2,
                        style = Stroke(width = 10f)
                    )
                }

                drawCircle(
                    color = Color(0xFFEECD09),
                    radius = knobRadiusPx * 0.5f,
                    center = Offset(knobX, knobY)
                )
            }

            Text(
                text = if (isDragging || !isTimerOn.value) {
                    val minutes = ((countTurns * 60) + (totalAngle / 360f * 60f)).roundToInt().coerceIn(0, 179)
                    countTimeFromMinutes(minutes)
                } else {
                    val totalSeconds = (remainingTime.value / 1000).toInt()
                    if (totalSeconds >= 3600) {
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        val minutes = totalSeconds / 60
                        val seconds = totalSeconds % 60
                        String.format("%02d:%02d", minutes, seconds)
                    }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        OrangeRoundedButton(
            text = if (isTimerOn.value) "Stop" else "Start",
            onClick = {
                val minutes = ((countTurns * 60) + (totalAngle / 360f * 60f)).roundToInt().coerceIn(0, 179)
                playerVM.startTimer(
                    durationMillis = minutes * 60 * 1000L
                )
                isFirstStart=false
                if (!isTimerOn.value){
                    hideTimer()
                }
            }
        )
    }
}


@Composable
fun OrangeRoundedButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEECD09)
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(
            horizontal = 24.dp,
        ),
        modifier = Modifier
            .height(52.dp).width(135.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}


private fun countTimeFromMinutes(minutes: Int) :String{
    if (minutes<60){
        return minutes.toString().padStart(2, '0') + ":00"
    } else {
        val hours = minutes/60
        val outMinutes = minutes%60
        var padOutMinutes = outMinutes.toString()
        if (outMinutes<10) {
            padOutMinutes= "0$outMinutes"
        }
        return "${hours}:$padOutMinutes:00"
    }
}
