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

package uk.co.hushchip.app.services

import android.util.Log
import uk.co.hushchip.app.BuildConfig
import uk.co.hushchip.app.data.LogItem
import java.util.logging.Level

object HushLog {
    var logList = mutableListOf<LogItem>()
    var isDebugModeActivated = false

    private val isEnabled get() = BuildConfig.DEBUG && isDebugModeActivated

    fun addLog(level: Level, tag: String = "", msg: String) {
        val log = LogItem(level= level, tag= tag, msg= msg)
        logList.add(log)
    }
    fun emptyList() {
        logList.clear()
    }

    fun e(tag: String, msg: String) {
        if (isEnabled) {
            Log.e(tag, msg)
            this.addLog(level= Level.SEVERE, tag= tag, msg= msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (isEnabled) {
            Log.w(tag, msg)
            this.addLog(level= Level.WARNING, tag= tag, msg= msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (isEnabled) {
            Log.i(tag, msg)
            this.addLog(level = Level.INFO, tag = tag, msg = msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (isEnabled) {
            Log.d(tag, msg)
            this.addLog(level = Level.CONFIG, tag = tag, msg = msg)
        }
    }
}