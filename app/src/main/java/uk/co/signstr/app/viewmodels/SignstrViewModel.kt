package uk.co.signstr.app.viewmodels

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import uk.co.signstr.app.MainActivity
import uk.co.signstr.app.crypto.*
import uk.co.signstr.app.data.*
import uk.co.signstr.app.nip46.NIP46Service
import java.util.UUID

class SignstrViewModel(application: Application) : AndroidViewModel(application) {

    val identities = mutableStateListOf<SignstrIdentity>()
    val activeIdentity = mutableStateOf<SignstrIdentity?>(null)
    val connections = mutableStateListOf<SignstrConnection>()
    val eventLog = mutableStateListOf<EventLogEntry>()
    val pendingRequest = mutableStateOf<NIP46Service.NIP46Request?>(null)
    val showApprovalDialog = mutableStateOf(false)
    val bunkerUri = mutableStateOf("")
    val isAppInForeground = mutableStateOf(true)

    var nip46Service: NIP46Service? = null
        private set

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val NOTIFICATION_CHANNEL_REQUESTS = "signstr_requests"
        private const val NOTIFICATION_ID_REQUEST = 100
    }

    fun initialize() {
        val ctx = getApplication<Application>()
        createNotificationChannel()
        loadData(ctx)
        startService(ctx)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_REQUESTS,
                "Signing Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for signing request approvals"
            }
            val manager = getApplication<Application>().getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadData(context: android.content.Context) {
        identities.clear()
        identities.addAll(SignstrPreferences.getIdentities(context))
        connections.clear()
        connections.addAll(SignstrPreferences.getConnections(context))
        eventLog.clear()
        eventLog.addAll(SignstrPreferences.getEventLog(context))

        val activeId = SignstrPreferences.getActiveIdentityId(context)
        activeIdentity.value = identities.find { it.id == activeId } ?: identities.firstOrNull()
    }

    private fun startService(context: android.content.Context) {
        nip46Service?.stop()
        nip46Service = NIP46Service(
            context = context,
            onSigningRequest = { request ->
                mainHandler.post {
                    pendingRequest.value = request
                    showApprovalDialog.value = true
                    if (!isAppInForeground.value) {
                        fireSigningNotification(request)
                    }
                }
            },
            onConnectionRequest = { connection ->
                mainHandler.post {
                    if (connections.none { it.id == connection.id }) {
                        connections.add(connection)
                    }
                }
            },
            onEventLogged = { entry ->
                mainHandler.post {
                    eventLog.add(0, entry)
                }
            }
        )
        nip46Service?.start()
    }

    private fun fireSigningNotification(request: NIP46Service.NIP46Request) {
        val ctx = getApplication<Application>()
        if (!SignstrPreferences.isNotificationsEnabled(ctx)) return

        val appName = request.connection.clientName.ifEmpty { "A client" }
        val kindInfo = request.eventKind?.let { " kind $it" } ?: ""

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_REQUESTS)
            .setContentTitle("Signing Request")
            .setContentText("$appName wants to sign a$kindInfo event")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID_REQUEST, notification)
        } catch (_: SecurityException) { }
    }

    fun createIdentity(name: String): SignstrIdentity {
        val ctx = getApplication<Application>()
        val privkey = Secp256k1.generatePrivateKey()
        val pubkey = Secp256k1.getPublicKey(privkey)
        val pubkeyHex = NIP44.bytesToHex(pubkey)

        val identity = SignstrIdentity(
            id = UUID.randomUUID().toString(),
            name = name,
            pubkeyHex = pubkeyHex,
            createdAt = System.currentTimeMillis() / 1000
        )

        SignstrKeyStore.storePrivateKey(ctx, identity.id, privkey)
        privkey.fill(0)

        identities.add(identity)
        SignstrPreferences.saveIdentities(ctx, identities.toList())

        if (activeIdentity.value == null) {
            activeIdentity.value = identity
            SignstrPreferences.setActiveIdentityId(ctx, identity.id)
        }

        nip46Service?.restart()
        return identity
    }

    fun importIdentity(name: String, nsec: String): SignstrIdentity? {
        val ctx = getApplication<Application>()
        val privkey = Bech32.nsecToBytes(nsec) ?: return null
        val pubkey = Secp256k1.getPublicKey(privkey)
        val pubkeyHex = NIP44.bytesToHex(pubkey)

        if (identities.any { it.pubkeyHex == pubkeyHex }) {
            privkey.fill(0)
            return null
        }

        val identity = SignstrIdentity(
            id = UUID.randomUUID().toString(),
            name = name,
            pubkeyHex = pubkeyHex,
            createdAt = System.currentTimeMillis() / 1000
        )

        SignstrKeyStore.storePrivateKey(ctx, identity.id, privkey)
        privkey.fill(0)

        identities.add(identity)
        SignstrPreferences.saveIdentities(ctx, identities.toList())

        if (activeIdentity.value == null) {
            activeIdentity.value = identity
            SignstrPreferences.setActiveIdentityId(ctx, identity.id)
        }

        nip46Service?.restart()
        return identity
    }

    fun setActiveIdentity(identity: SignstrIdentity) {
        val ctx = getApplication<Application>()
        activeIdentity.value = identity
        SignstrPreferences.setActiveIdentityId(ctx, identity.id)
        updateBunkerUri()
    }

    fun renameIdentity(identity: SignstrIdentity, newName: String) {
        val ctx = getApplication<Application>()
        val idx = identities.indexOfFirst { it.id == identity.id }
        if (idx >= 0) {
            val updated = identity.copy(name = newName)
            identities[idx] = updated
            if (activeIdentity.value?.id == identity.id) activeIdentity.value = updated
            SignstrPreferences.saveIdentities(ctx, identities.toList())
        }
    }

    fun updateIdentity(updated: SignstrIdentity) {
        val ctx = getApplication<Application>()
        val idx = identities.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            identities[idx] = updated
            if (activeIdentity.value?.id == updated.id) activeIdentity.value = updated
            SignstrPreferences.saveIdentities(ctx, identities.toList())
        }
    }

    fun deleteIdentity(identity: SignstrIdentity) {
        val ctx = getApplication<Application>()
        SignstrKeyStore.deletePrivateKey(ctx, identity.id)
        identities.removeAll { it.id == identity.id }
        connections.removeAll { it.identityId == identity.id }
        SignstrPreferences.saveIdentities(ctx, identities.toList())
        SignstrPreferences.saveConnections(ctx, connections.toList())

        if (activeIdentity.value?.id == identity.id) {
            activeIdentity.value = identities.firstOrNull()
            activeIdentity.value?.let {
                SignstrPreferences.setActiveIdentityId(ctx, it.id)
            }
        }
        nip46Service?.restart()
    }

    fun removeConnection(connection: SignstrConnection) {
        val ctx = getApplication<Application>()
        connections.removeAll { it.id == connection.id }
        SignstrPreferences.saveConnections(ctx, connections.toList())
        ApprovalPolicyStore.removePolicy(ctx, connection.clientPubkeyHex)
    }

    fun updateBunkerUri() {
        val identity = activeIdentity.value ?: return
        bunkerUri.value = nip46Service?.generateBunkerUri(identity) ?: ""
    }

    fun approveRequest() {
        val request = pendingRequest.value ?: return
        showApprovalDialog.value = false
        pendingRequest.value = null
        Thread {
            nip46Service?.approveRequest(request)
            nip46Service?.onRequestProcessed()
        }.start()
    }

    fun rejectRequest() {
        val request = pendingRequest.value ?: return
        showApprovalDialog.value = false
        pendingRequest.value = null
        Thread {
            nip46Service?.rejectRequest(request)
            nip46Service?.onRequestProcessed()
        }.start()
    }

    fun getActiveConnections(): List<SignstrConnection> {
        val identity = activeIdentity.value ?: return emptyList()
        return connections.filter { it.identityId == identity.id && it.isActive }
    }

    fun getActiveEventLog(): List<EventLogEntry> {
        val identity = activeIdentity.value ?: return emptyList()
        return eventLog.filter { it.identityId == identity.id }.sortedByDescending { it.timestamp }
    }

    fun getNsec(identityId: String): String? {
        val ctx = getApplication<Application>()
        val privkey = SignstrKeyStore.loadPrivateKey(ctx, identityId) ?: return null
        val nsec = Bech32.bytesToNsec(privkey)
        privkey.fill(0)
        return nsec
    }

    fun deleteAllData() {
        val ctx = getApplication<Application>()
        nip46Service?.stop()
        for (id in identities.map { it.id }) {
            SignstrKeyStore.deletePrivateKey(ctx, id)
        }
        identities.clear()
        connections.clear()
        eventLog.clear()
        activeIdentity.value = null
        pendingRequest.value = null
        showApprovalDialog.value = false
        SignstrPreferences.clearAllData(ctx)
        SignstrPreferences.setKeySetupComplete(ctx, false)
    }

    fun resetApp() {
        val ctx = getApplication<Application>()
        deleteAllData()
        SignstrPreferences.setOnboardingComplete(ctx, false)
    }

    override fun onCleared() {
        super.onCleared()
        nip46Service?.stop()
    }
}
