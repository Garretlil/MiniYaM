package com.example.miniyam.Presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.miniyam.BASEURL
import com.example.miniyam.Domain.Track
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.viewmodels.LikesViewModel
import com.example.miniyam.Presentation.viewmodels.SearchStates
import com.example.miniyam.R

@Composable
fun GradientRoundedContainerCanvas(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {

            val w = size.width
            val h = size.height

            drawRect(
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                color = Color(0xFF05A821)
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

            val glowPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f

                shader = android.graphics.LinearGradient(
                    0f, h * 0.7f,
                    w * 0.25f, h * 0.6f,
                    intArrayOf(
                        Color.White.copy(alpha = 0.5f).toArgb(),
                        Color.White.copy(alpha = 0.05f).toArgb(),
                        Color.Transparent.toArgb()
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
                )

                maskFilter = android.graphics.BlurMaskFilter(
                    15f,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }

            val colorLight = Color(0xFFDFE8E0)

            val linePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f

                shader = android.graphics.LinearGradient(
                    0f, h * 0.7f,
                    w * 0.25f, h * 0.6f,
                    intArrayOf(
                        colorLight.copy(alpha = 0.5f).toArgb(),
                        colorLight.copy(alpha = 0.1f).toArgb(),
                        Color.Transparent.toArgb()
                    ),
                    floatArrayOf(0f, 0.7f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
                )

                maskFilter = android.graphics.BlurMaskFilter(
                    5f,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }

            drawContext.canvas.nativeCanvas.apply {
                save()
                drawPath(androidPath, glowPaint)
                drawPath(androidPath, linePaint)
                restore()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLikesAppBar() {
    SmallTopAppBar(
        title = {
            Text(
                text = "My Collection",
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Profile Icon"
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.White
        )
    )
}



@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun LikesScreen(playerVM: PlayerViewModel, likesVM: LikesViewModel) {
    val queue by likesVM.likesQueue.collectAsState()
    val currentTrack by playerVM.currentTrack.collectAsState()
    val isLoading by remember { likesVM::isLoading }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp)
    ) {
        MyLikesAppBar()

        GradientRoundedContainerCanvas(
            modifier = Modifier
                .height(70.dp).padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "My Vibe for",
                        color = Color.White,
                        fontWeight = FontWeight.W600
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Collection",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your music has ",
                    color = Color.Gray,
                    fontWeight = FontWeight.W700
                )
                Text(
                    "color",
                    color = Color(0xFF2DB93D),
                    fontWeight = FontWeight.W700
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (isLoading == SearchStates.LOADING) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.myl),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "My Favorites ❭",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 25.sp,
                        )
                        Text(
                            "${likesVM.likesQueue.value.tracks.size} tracks",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
                LazyColumn(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
                    items(queue.tracks.size) { index ->
                        val track = queue.tracks[index]
                        val isCurrent = track.id == currentTrack?.id
                        if (index<=2)
                        Box(
                            modifier =
                            if (isCurrent) Modifier.background(Color(0xFFEEEDED))
                            else Modifier.background(Color.Transparent)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { likesVM.play(playerVM, track) }) {
                                Box(contentAlignment = Alignment.Center) {
                                    SubcomposeAsyncImage(
                                        model =  track.imageUrl,
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
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

