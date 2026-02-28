package uk.co.signstr.app.viewmodels

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
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

    var nip46Service: NIP46Service? = null
        private set

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "SignstrViewModel"
    }

    fun initialize() {
        val ctx = getApplication<Application>()
        loadData(ctx)
        startService(ctx)
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
                    eventLog.add(entry)
                }
            }
        )
        nip46Service?.start()
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
        }.start()
    }

    fun rejectRequest() {
        val request = pendingRequest.value ?: return
        showApprovalDialog.value = false
        pendingRequest.value = null
        Thread {
            nip46Service?.rejectRequest(request)
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

    override fun onCleared() {
        super.onCleared()
        nip46Service?.stop()
    }
}
