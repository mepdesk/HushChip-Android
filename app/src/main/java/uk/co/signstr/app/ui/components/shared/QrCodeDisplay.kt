package uk.co.signstr.app.ui.components.shared

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.g0dkar.qrcode.QRCode
import java.io.ByteArrayOutputStream

@Composable
fun QrCodeDisplay(
    data: String,
    modifier: Modifier = Modifier,
    size: Int = 220
) {
    val bitmap = remember(data) {
        if (data.isEmpty()) null
        else {
            try {
                val qr = QRCode(data)
                val rendered = qr.render(cellSize = 10, margin = 20)
                val baos = ByteArrayOutputStream()
                rendered.writeImage(baos)
                val bytes = baos.toByteArray()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
        }
    }

    if (bitmap != null) {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
