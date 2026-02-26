package uk.co.hushchip.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun webviewActivityIntent(
    url: String,
    context: Context
) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    )
}
