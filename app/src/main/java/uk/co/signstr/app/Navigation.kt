package uk.co.signstr.app

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import uk.co.signstr.app.data.SignstrPreferences
import uk.co.signstr.app.ui.components.shared.*
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.ui.views.connect.ConnectView
import uk.co.signstr.app.ui.views.events.EventsView
import uk.co.signstr.app.ui.views.identity.IdentityView
import uk.co.signstr.app.ui.views.settings.SettingsView
import uk.co.signstr.app.ui.views.splash.SplashView
import uk.co.signstr.app.ui.views.onboarding.OnboardingView
import uk.co.signstr.app.ui.views.keysetup.KeySetupView
import uk.co.signstr.app.ui.views.keysetup.BackupKeyView
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel
import kotlinx.coroutines.delay

enum class SignstrTab(val label: String) {
    CONNECT("CONNECT"),
    EVENTS("EVENTS"),
    IDENTITY("IDENTITY"),
    SETTINGS("SETTINGS")
}

enum class AppScreen {
    SPLASH, ONBOARDING, KEY_SETUP, BACKUP_KEY, MAIN
}

@Composable
fun SignstrNavigation(
    context: Context,
    viewModel: SignstrViewModel
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentTab by remember { mutableStateOf(SignstrTab.CONNECT) }
    var pendingNsecForBackup by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.initialize()
        delay(2000)
        currentScreen = when {
            !SignstrPreferences.isOnboardingComplete(context) -> AppScreen.ONBOARDING
            !SignstrPreferences.isKeySetupComplete(context) || viewModel.identities.isEmpty() -> AppScreen.KEY_SETUP
            else -> AppScreen.MAIN
        }
    }

    val showApproval = viewModel.showApprovalDialog.value
    val pendingRequest = viewModel.pendingRequest.value

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.SPLASH -> SplashView()

            AppScreen.ONBOARDING -> OnboardingView(
                onComplete = {
                    SignstrPreferences.setOnboardingComplete(context, true)
                    currentScreen = AppScreen.KEY_SETUP
                }
            )

            AppScreen.KEY_SETUP -> KeySetupView(
                onIdentityCreated = { identity ->
                    val nsec = viewModel.getNsec(identity.id)
                    pendingNsecForBackup = nsec
                    currentScreen = AppScreen.BACKUP_KEY
                },
                viewModel = viewModel,
                context = context
            )

            AppScreen.BACKUP_KEY -> BackupKeyView(
                nsec = pendingNsecForBackup ?: "",
                context = context,
                onComplete = {
                    SignstrPreferences.setKeySetupComplete(context, true)
                    SignstrPreferences.setFirstLaunchDone(context)
                    pendingNsecForBackup = null
                    currentScreen = AppScreen.MAIN
                }
            )

            AppScreen.MAIN -> {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(SignstrColors.bg)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "SIGNSTR",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                letterSpacing = 5.sp,
                                color = SignstrColors.textMuted
                            )
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            SignstrTab.CONNECT -> ConnectView(context, viewModel)
                            SignstrTab.EVENTS -> EventsView(viewModel)
                            SignstrTab.IDENTITY -> IdentityView(
                                context = context,
                                viewModel = viewModel,
                                onCreateIdentity = {
                                    currentScreen = AppScreen.KEY_SETUP
                                }
                            )
                            SignstrTab.SETTINGS -> SettingsView(
                                context = context,
                                viewModel = viewModel,
                                onDeleteAllData = {
                                    viewModel.deleteAllData()
                                    currentScreen = AppScreen.KEY_SETUP
                                },
                                onResetApp = {
                                    viewModel.resetApp()
                                    currentScreen = AppScreen.ONBOARDING
                                }
                            )
                        }
                    }

                    TabBar(currentTab = currentTab, onTabSelected = { currentTab = it })
                }
            }
        }

        // Approval dialog with biometric gate
        if (showApproval && pendingRequest != null) {
            ApprovalDialog(
                request = pendingRequest,
                onApprove = {
                    val activity = context as? FragmentActivity
                    val biometricsEnabled = SignstrPreferences.isBiometricsEnabled(context)
                    if (activity != null && biometricsEnabled) {
                        val canAuth = BiometricManager.from(context)
                            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                            val executor = ContextCompat.getMainExecutor(context)
                            val prompt = BiometricPrompt(activity, executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        viewModel.approveRequest()
                                    }
                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { }
                                    override fun onAuthenticationFailed() { }
                                })
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Sign Nostr Event")
                                .setSubtitle("Authenticate to approve signing")
                                .setNegativeButtonText("Cancel")
                                .build()
                            prompt.authenticate(promptInfo)
                        } else {
                            viewModel.approveRequest()
                        }
                    } else {
                        viewModel.approveRequest()
                    }
                },
                onReject = { viewModel.rejectRequest() }
            )
        }
    }
}

@Composable
private fun TabBar(currentTab: SignstrTab, onTabSelected: (SignstrTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bg)
            .navigationBarsPadding()
            .padding(top = 1.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (tab in SignstrTab.entries) {
            val isActive = tab == currentTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .signstrClickEffect(onClick = { onTabSelected(tab) })
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = if (isActive) SignstrColors.textMuted else SignstrColors.textGhost
                    )
                )
            }
        }
    }
}
