package uk.co.hushchip.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import uk.co.hushchip.app.ui.theme.HushChipTheme
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.viewmodels.SharedViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current as Activity
            //Lock screen orientation to portrait
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            HushChipTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(color = Color(0xFF09090B))
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF111115),
                                            Color(0xFF09090B)
                                        ),
                                        center = Offset(size.width / 2f, size.height * 0.35f),
                                        radius = size.width * 0.9f
                                    )
                                )
                            }
                    ) {
                        Navigation(
                            context = context,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

