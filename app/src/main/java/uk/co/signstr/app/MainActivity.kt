package uk.co.signstr.app

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import uk.co.signstr.app.services.SignstrForegroundService
import uk.co.signstr.app.ui.theme.SignstrTheme
import uk.co.signstr.app.viewmodels.SignstrViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: SignstrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            SignstrTheme {
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
                    SignstrNavigation(
                        context = this@MainActivity,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isAppInForeground.value = true
        startForegroundServiceIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        viewModel.isAppInForeground.value = false
    }

    private fun startForegroundServiceIfNeeded() {
        try {
            if (viewModel.connections.isNotEmpty() || viewModel.identities.isNotEmpty()) {
                val intent = Intent(this, SignstrForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        } catch (_: Exception) {
            // Foreground service may fail if app is in background or restricted
        }
    }
}
