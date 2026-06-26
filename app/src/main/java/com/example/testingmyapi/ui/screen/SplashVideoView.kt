package com.example.testingmyapi.ui.screen

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testingmyapi.R

@Composable
fun SplashVideoView(
    onVideoComplete: () -> Unit
) {
    // ============================================================
    // 1. DEKLARASI VARIABEL
    // ============================================================
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var error by remember { mutableStateOf<String?>(null) }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var isVideoCompleted by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var isCompleteCalled by remember { mutableStateOf(false) }

    // ============================================================
    // 2. ANIMASI FADE IN
    // ============================================================
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "fade_in"
    )

    // ============================================================
    // 3. CEK LIFECYCLE (Hanya pause/resume, TANPA panggil onComplete)
    // ============================================================
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    videoViewRef?.let {
                        currentPosition = it.currentPosition
                        it.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isVideoCompleted) {
                        videoViewRef?.let {
                            it.seekTo(currentPosition)
                            it.start()
                        }
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ============================================================
    // 4. LAYOUT UTAMA
    // ============================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer(alpha = alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ============================================================
        // 4a. VIDEO DI TENGAH
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this

                        try {
                            val videoUri = Uri.parse("android.resource://${context.packageName}/raw/splash_video")
                            setVideoURI(videoUri)
                            setMediaController(null)
                            start()

                            setOnCompletionListener {
                                if (!isCompleteCalled) {
                                    isCompleteCalled = true
                                    isVideoCompleted = true
                                    onVideoComplete()
                                }
                            }

                            setOnErrorListener { _, _, _ ->
                                if (!isCompleteCalled) {
                                    isCompleteCalled = true
                                    isVideoCompleted = true
                                    onVideoComplete()
                                }
                                true
                            }
                        } catch (e: Exception) {
                            if (!isCompleteCalled) {
                                isCompleteCalled = true
                                isVideoCompleted = true
                                onVideoComplete()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }

        // ============================================================
        // 4b. KONTEN DI BAWAH
        // ============================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Icon",
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Metal Cardbot Character",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Explore Your Favorite Characters",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}