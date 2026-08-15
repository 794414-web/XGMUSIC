package com.xgmusic.app.car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xgmusic.app.XGMusicApp
import com.xgmusic.app.ui.theme.XGMUSICTheme

/**
 * Car Activity
 *
 * Dedicated car-optimized UI with large controls,
 * high contrast, and driver-friendly layout.
 */
class CarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XGMUSICTheme(darkTheme = true, dynamicColor = false) {
                CarPlayerScreen()
            }
        }
    }
}

@Composable
fun CarPlayerScreen() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as XGMusicApp
    val playerManager = app.container.playerManager
    val currentMusic by playerManager.currentMusic
    val isPlaying by playerManager.isPlaying
    val progress by playerManager.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large album art placeholder
        Text(
            text = currentMusic?.name?.take(1) ?: "♪",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(32.dp)
        )

        // Song info (large for car visibility)
        Text(
            text = currentMusic?.name ?: "未选择歌曲",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = currentMusic?.artist ?: "",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Progress
        Slider(
            value = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f,
            onValueChange = { /* seeking not recommended while driving */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )

        // Large playback controls for car
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* previous */ },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.padding(16.dp)
                )
            }

            IconButton(
                onClick = { playerManager.togglePlayPause() },
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.padding(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { /* next */ },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
