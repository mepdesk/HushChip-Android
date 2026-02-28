package uk.co.signstr.app.ui.views.connect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.signstrClickEffect
import java.util.concurrent.Executors

/**
 * Parsed nostrconnect:// URI
 */
data class NostrConnectInfo(
    val pubkey: String,
    val relays: List<String>,
    val secret: String,
    val name: String
)

fun parseNostrConnectUri(uri: String): NostrConnectInfo? {
    if (!uri.startsWith("nostrconnect://")) return null
    return try {
        val parsed = Uri.parse(uri)
        val pubkey = parsed.host ?: return null
        if (pubkey.length != 64) return null

        val relays = parsed.getQueryParameters("relay")
        val secret = parsed.getQueryParameter("secret") ?: ""
        val name = parsed.getQueryParameter("name") ?: ""

        NostrConnectInfo(pubkey, relays, secret, name)
    } catch (_: Exception) {
        null
    }
}

enum class ScannerState {
    SCANNING, MANUAL_ENTRY, REVIEW
}

@Composable
fun ScannerView(
    context: Context,
    onConnect: (NostrConnectInfo) -> Unit,
    onBack: () -> Unit
) {
    var state by remember { mutableStateOf(ScannerState.SCANNING) }
    var scannedInfo by remember { mutableStateOf<NostrConnectInfo?>(null) }
    var pasteText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2190",
                style = TextStyle(fontSize = 20.sp, color = SignstrColors.textMuted),
                modifier = Modifier.signstrClickEffect(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ADD CONNECTION",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    color = SignstrColors.textFaint
                )
            )
        }

        when (state) {
            ScannerState.SCANNING -> {
                if (hasCameraPermission) {
                    // Camera preview with guide frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CameraPreview(
                            context = context,
                            onQrScanned = { rawValue ->
                                val info = parseNostrConnectUri(rawValue)
                                if (info != null) {
                                    scannedInfo = info
                                    state = ScannerState.REVIEW
                                }
                            }
                        )

                        // Guide frame overlay
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Semi-transparent overlay with cutout effect
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .border(2.dp, SignstrColors.textMuted.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            )
                        }

                        // Instruction text at bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan a nostrconnect:// QR code",
                                style = TextStyle(
                                    fontFamily = outfitFamily,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 12.sp,
                                    color = SignstrColors.textBright
                                )
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Camera permission required\nto scan QR codes",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                color = SignstrColors.textFaint,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Toggle to manual entry
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                ) {
                    GhostButton(
                        text = "Paste URI Instead",
                        onClick = { state = ScannerState.MANUAL_ENTRY }
                    )
                }
            }

            ScannerState.MANUAL_ENTRY -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PASTE URI",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            color = SignstrColors.textGhost
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it; errorText = "" },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = SignstrColors.textBright,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(SignstrColors.textMuted),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        decorationBox = { inner ->
                            Box {
                                if (pasteText.isEmpty()) {
                                    Text(
                                        text = "nostrconnect://pubkey?relay=...",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Light,
                                            fontSize = 12.sp,
                                            color = SignstrColors.textGhost
                                        )
                                    )
                                }
                                inner()
                            }
                        }
                    )

                    if (errorText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorText,
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Light,
                                fontSize = 11.sp,
                                color = SignstrColors.danger
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GhostButton(
                            text = "Scan QR",
                            onClick = { state = ScannerState.SCANNING },
                            modifier = Modifier.weight(1f)
                        )
                        GhostButton(
                            text = "Connect",
                            onClick = {
                                val info = parseNostrConnectUri(pasteText.trim())
                                if (info != null) {
                                    scannedInfo = info
                                    state = ScannerState.REVIEW
                                } else {
                                    errorText = "Invalid nostrconnect:// URI"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            ScannerState.REVIEW -> {
                val info = scannedInfo ?: return
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "REVIEW CONNECTION",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                            color = SignstrColors.textFaint
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
                            .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            if (info.name.isNotEmpty()) {
                                ReviewRow("APP NAME", info.name)
                            }
                            ReviewRow("CLIENT PUBKEY", info.pubkey.take(24) + "...")
                            if (info.relays.isNotEmpty()) {
                                ReviewRow("RELAYS", info.relays.joinToString("\n"))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GhostButton(
                            text = "Reject",
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            isDanger = true
                        )
                        GhostButton(
                            text = "Approve",
                            onClick = { onConnect(info) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = SignstrColors.textGhost
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = SignstrColors.textBody,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
private fun CameraPreview(
    context: Context,
    onQrScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var scannedAlready by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            startCamera(ctx, lifecycleOwner, previewView) { rawValue ->
                if (!scannedAlready && rawValue.startsWith("nostrconnect://")) {
                    scannedAlready = true
                    onQrScanned(rawValue)
                }
            }
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onQrDetected: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val scanner = BarcodeScanning.getClient()
            val executor = Executors.newSingleThreadExecutor()

            analyzer.setAnalyzer(executor) { imageProxy ->
                processImageProxy(scanner, imageProxy, onQrDetected)
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
        } catch (_: Exception) {
            // Camera init failed
        }
    }, ContextCompat.getMainExecutor(context))
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { onQrDetected(it) }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
