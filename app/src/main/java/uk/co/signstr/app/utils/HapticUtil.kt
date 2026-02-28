package uk.co.signstr.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtil {
    private fun vibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun confirm(context: Context) {
        vibrator(context).vibrate(VibrationEffect.createOneShot(50, 128))
    }

    fun reject(context: Context) {
        vibrator(context).vibrate(VibrationEffect.createOneShot(200, 255))
    }

    fun tap(context: Context) {
        vibrator(context).vibrate(VibrationEffect.createOneShot(20, 80))
    }
}
