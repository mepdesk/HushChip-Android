package uk.co.hushchip.app.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import uk.co.hushchip.app.data.NfcResultCode

private const val TAG = "SeedkeeperCardListener"
private const val OPERATION_TIMEOUT_MS = 5000L

object SatochipCardListenerForAction : CardListener {

    @Volatile
    private var lastConnectionTimestamp = 0L

    override fun onConnected(cardChannel: CardChannel?) {

        // Debounce: ignore rapid re-taps if an operation is still in progress
        val now = System.currentTimeMillis()
        if (NFCCardService.isOperationInProgress) {
            val elapsed = now - lastConnectionTimestamp
            if (elapsed < OPERATION_TIMEOUT_MS) {
                HushLog.d(TAG, "onConnected: ignoring — operation in progress (${elapsed}ms elapsed)")
                return
            }
            // Operation has timed out — allow the new connection through
            HushLog.d(TAG, "onConnected: previous operation timed out after ${elapsed}ms, allowing new connection")
        }
        lastConnectionTimestamp = now

        // Reset all cached state from any previous session before starting fresh
        NFCCardService.resetCardState()

        NFCCardService.isConnected.postValue(true)
        HushLog.d(TAG, "onConnected: Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            // start to interact with card — always fresh command set
            NFCCardService.initialize(cmdSet)

            onDisconnected()
            Thread.sleep(100) // delay to let resultCodeLive update (avoid race condition?)
            HushLog.d(TAG, "onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            NFCCardService.disableScanForAction()
        } catch (e: Exception) {
            HushLog.e(TAG, "onConnected: an exception has been thrown during card init.")
            HushLog.e(TAG, Log.getStackTraceString(e))
            // Post error so overlay shows card-lost message
            NFCCardService.resultCodeLive.postValue(NfcResultCode.NFC_ERROR)
            onDisconnected()
        }
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        HushLog.d(TAG, "onDisconnected: Card disconnected!")
    }
}
