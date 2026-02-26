// Copyright (c) 2026 Gridmark Technologies Ltd (HushChip)
// https://github.com/hushchip/HushChip-Android
//
// Based on Seedkeeper-Android by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-Android
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

package uk.co.hushchip.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ClipboardUtil {
    private var clearJob: Job? = null

    fun copyWithAutoClear(
        context: Context,
        text: String,
        label: String = "HushChip",
        clearDelayMs: Long = 30_000L,
        scope: CoroutineScope
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        // On Android 12 and below, show a custom toast (Android 13+ shows its own)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied \u2014 clipboard clears in 30s", Toast.LENGTH_SHORT).show()
        }

        // Cancel any existing clear timer
        clearJob?.cancel()

        // Schedule clear after 30 seconds
        clearJob = scope.launch {
            delay(clearDelayMs)
            val emptyClip = ClipData.newPlainText("", "")
            clipboard.setPrimaryClip(emptyClip)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Clipboard cleared", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
