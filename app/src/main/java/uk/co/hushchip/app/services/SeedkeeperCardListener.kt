package uk.co.hushchip.app.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import uk.co.hushchip.app.data.NfcResultCode

private const val TAG = "SeedkeeperCardListener"

object SatochipCardListenerForAction : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {

        NFCCardService.isConnected.postValue(true)
        HushLog.d(TAG, "onConnected: Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            // start to interact with card
            NFCCardService.initialize(cmdSet)

            // TODO: disconnect?
            onDisconnected()
            Thread.sleep(100) // delay to let resultCodeLive update (avoid race condition?)
            HushLog.d(TAG, "onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            NFCCardService.disableScanForAction()
        } catch (e: Exception) {
            HushLog.e(TAG, "onConnected: an exception has been thrown during card init.")
            HushLog.e(TAG, Log.getStackTraceString(e))
            onDisconnected()
        }
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        HushLog.d(TAG, "onDisconnected: Card disconnected!")
    }
}