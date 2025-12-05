package com.example.miniyam

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.miniyam.Presentation.Navigation.BottomBar
import com.example.miniyam.Presentation.Navigation.MiniPlayer
import com.example.miniyam.Presentation.Navigation.NavigationHost
import com.example.miniyam.Presentation.PlayerViewModel
import com.example.miniyam.Presentation.screens.ExpandedPlayerWithSlideAnimation
import com.example.miniyam.Presentation.screens.RegAuthScreen
import com.example.miniyam.Presentation.viewmodels.HomeViewModel
import com.example.miniyam.Presentation.viewmodels.LikesViewModel
import com.example.miniyam.Presentation.viewmodels.SearchViewModel
import com.example.miniyam.ui.theme.MiniYaMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.RenderEffect
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val playerVM: PlayerViewModel = hiltViewModel()
            val searchVM: SearchViewModel = hiltViewModel()
            val homeVM:HomeViewModel = hiltViewModel()
            val likesVM:LikesViewModel = hiltViewModel()
            MiniYaMTheme(
                dynamicColor = false
            ) {
                AppRoot(playerVM,searchVM,homeVM,likesVM)
            }
        }
    }
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppRoot(
    playerVM: PlayerViewModel,
    searchVM: SearchViewModel,
    homeVM: HomeViewModel,
    likesVM: LikesViewModel
) {
    val context = LocalContext.current
    val sharedPref = remember {
        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val storagePermission = rememberPermissionState(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    var showHome by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val token = sharedPref.getString("token", "") ?: ""
        //sharedPref.edit().putString("token","").apply()
        showHome = token.isNotEmpty()
    }

    LaunchedEffect(Unit) {
        if (!audioPermission.status.isGranted) {
            audioPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(audioPermission.status) {
        if (audioPermission.status.isGranted && !storagePermission.status.isGranted) {
            storagePermission.launchPermissionRequest()
        }
    }

    val allPermissionsGranted = audioPermission.status.isGranted &&
            storagePermission.status.isGranted

    when {
        showHome && allPermissionsGranted -> {
            MainScreen(playerVM, searchVM, homeVM, likesVM, showHome)
        }
        allPermissionsGranted -> {
            RegAuthScreen(onAuthSuccess = { 
                val token = sharedPref.getString("token", "") ?: ""
                showHome = token.isNotEmpty()
            })
        }
        else -> {
            PermissionDeniedScreen {
                val permissionsToRequest = listOf(
                    audioPermission to !audioPermission.status.isGranted,
                    storagePermission to !storagePermission.status.isGranted
                ).filter { it.second }.map { it.first }

                permissionsToRequest.forEach { permission ->
                    permission.launchPermissionRequest()
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Доступ к аудиофайлам необходим для работы приложения.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Предоставить разрешение")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun BlurredBottomBar(
    modifier: Modifier = Modifier,
    radius: Float = 35f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { context ->
                FrameLayout(context).apply {
                    setWillNotDraw(false)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
            },
            update = { view ->
                val blur = RenderEffect.createBlurEffect(
                    radius,
                    radius,
                    android.graphics.Shader.TileMode.CLAMP
                )
                view.setRenderEffect(blur)
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.25f)),
            content = content
        )
    }
}



@Composable
fun MainScreen(
    playerVM: PlayerViewModel,
    searchVM: SearchViewModel,
    homeVM: HomeViewModel,
    likesVM: LikesViewModel,
    showHome: Boolean
) {
    val navController = rememberNavController()
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(showHome) {
        if (showHome) {
            homeVM.loadTracks()
            likesVM.loadTracks()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(Color.Transparent),
            bottomBar = {
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = slideInVertically { height -> height },
                    exit = slideOutVertically { height -> height },
                ) {
                    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape),
                        shape = shape,
                        color = Color.Transparent,
                        tonalElevation = 8.dp,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MiniPlayer(playerVM, onExpand = { isExpanded = true })
                            BottomBar(navController)
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavigationHost(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
                viewModel = playerVM,
                searchVM = searchVM,
                homeVM = homeVM,
                likesVM = likesVM
            )
        }

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures {} }
            ) {
                ExpandedPlayerWithSlideAnimation(
                    onCollapse = { isExpanded = false },
                    viewModel = playerVM
                )
            }
        }
    }
}





