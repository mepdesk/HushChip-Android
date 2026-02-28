package uk.co.signstr.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ClipboardUtil {
    fun copyWithAutoClear(context: Context, label: String, text: String, clearDelayMs: Long = 30_000L) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied — clears in 30s", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            val current = clipboard.primaryClip
            if (current != null && current.itemCount > 0 && current.getItemAt(0).text?.toString() == text) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }, clearDelayMs)
    }
}
