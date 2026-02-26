package uk.co.hushchip.app

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import uk.co.hushchip.app.data.ImportMode
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.data.HushChipPreferences
import uk.co.hushchip.app.services.HushLog
import uk.co.hushchip.app.ui.components.NfcScanOverlay
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.utils.HapticUtil
import uk.co.hushchip.app.ui.views.addsecret.AddSecretView
import uk.co.hushchip.app.ui.views.backup.BackupView
import uk.co.hushchip.app.ui.views.home.CardIllustration
import uk.co.hushchip.app.ui.views.cardinfo.CardInformation
import uk.co.hushchip.app.ui.views.factoryreset.FactoryResetView
import uk.co.hushchip.app.ui.views.home.HomeView
import uk.co.hushchip.app.ui.views.cardinfo.EditCardLabelView
import uk.co.hushchip.app.ui.views.import.ImportSecretView
import uk.co.hushchip.app.ui.views.menu.MenuView
import uk.co.hushchip.app.ui.views.mysecret.ShowSecretView
import uk.co.hushchip.app.ui.views.pincode.PinEntryView
import uk.co.hushchip.app.ui.views.settings.SettingsView
import uk.co.hushchip.app.ui.views.showcardlogs.ShowCardLogsView
import uk.co.hushchip.app.ui.views.showlogs.ShowLogsView
import uk.co.hushchip.app.ui.views.about.AboutView
import uk.co.hushchip.app.ui.views.splash.SplashView
import uk.co.hushchip.app.ui.views.welcome.WelcomeView
import uk.co.hushchip.app.viewmodels.SharedViewModel

private const val TAG = "Navigation"

@Composable
fun Navigation(
    context: Context,
    viewModel: SharedViewModel,
) {
    val navController = rememberNavController()
    val settings = context.getSharedPreferences("seedkeeper", Context.MODE_PRIVATE)
    val debugMode = remember {
        mutableStateOf(settings.getBoolean(HushChipPreferences.DEBUG_MODE.name, false))
    }
    HushLog.isDebugModeActivated = debugMode.value
    val startDestination =
        if (settings.getBoolean(HushChipPreferences.FIRST_TIME_LAUNCH.name, true)) {
            settings.edit().putBoolean(HushChipPreferences.FIRST_TIME_LAUNCH.name, false).apply()
            FirstWelcomeView
        } else {
            HomeView
        }

    val view = LocalView.current

    // FIRST TIME SETUP
    LaunchedEffect(viewModel.resultCodeLive) {
        if (viewModel.resultCodeLive == NfcResultCode.REQUIRE_SETUP) {
            HushLog.d(TAG, "Navigation: Card needs to be setup!")
            navController.navigate(
                PinEntryView(
                    pinCodeAction = PinCodeAction.SETUP_PIN_CODE.name,
                    isBackupCard = false,
                )
            )
        } else if (viewModel.resultCodeLive == NfcResultCode.REQUIRE_SETUP_FOR_BACKUP) {
            HushLog.d(TAG, "Navigation: Card needs to be setup!")
            navController.navigate(
                PinEntryView(
                    pinCodeAction = PinCodeAction.SETUP_PIN_CODE.name,
                    isBackupCard = true,
                )
            )
        }
    }

    // Haptic feedback on NFC results
    LaunchedEffect(viewModel.resultCodeLive) {
        when (viewModel.resultCodeLive) {
            // Card detected / PIN accepted / success states
            NfcResultCode.CARD_SCANNED_SUCCESSFULLY,
            NfcResultCode.BACKUP_CARD_SCANNED_SUCCESSFULLY,
            NfcResultCode.CARD_SETUP_SUCCESSFUL,
            NfcResultCode.CARD_SETUP_FOR_BACKUP_SUCCESSFUL,
            NfcResultCode.PIN_CHANGED,
            NfcResultCode.SECRET_IMPORTED_SUCCESSFULLY,
            NfcResultCode.SECRET_EXPORTED_SUCCESSFULLY,
            NfcResultCode.CARD_LABEL_CHANGED_SUCCESSFULLY,
            NfcResultCode.CARD_LOGS_FETCHED_SUCCESSFULLY,
            NfcResultCode.SECRETS_EXPORTED_SUCCESSFULLY_FROM_MASTER,
            NfcResultCode.CARD_SUCCESSFULLY_BACKED_UP -> {
                HapticUtil.confirm(view)
            }
            // Wrong PIN
            NfcResultCode.WRONG_PIN,
            NfcResultCode.CARD_BLOCKED -> {
                HapticUtil.reject(view)
            }
            // Card lost / communication error
            NfcResultCode.NFC_ERROR,
            NfcResultCode.CARD_ERROR,
            NfcResultCode.CARD_MISMATCH -> {
                HapticUtil.reject(view)
            }
            // Secret deleted
            NfcResultCode.SECRET_DELETED -> {
                HapticUtil.heavy(view)
            }
            // Factory reset
            NfcResultCode.CARD_RESET -> {
                HapticUtil.heavy(view)
            }
            else -> {}
        }
    }

    // Haptic feedback when card is first detected
    LaunchedEffect(viewModel.isCardConnected) {
        if (viewModel.isCardConnected) {
            HapticUtil.confirm(view)
        }
    }

    // Main content + NFC overlay layered on top
    Box {
    NavHost(
        navController = navController,
        startDestination = SplashView
    ) {
        composable<SplashView> {
            SplashView()
            LaunchedEffect(Unit) {
                delay(1500)
                navController.navigate(startDestination) {
                    popUpTo(0)
                }
            }
        }
        // Screen 1 — "Your secrets. On a chip."
        composable<FirstWelcomeView> {
            WelcomeView(
                screenIndex = 0,
                heading = stringResource(R.string.onboarding1Heading),
                body = stringResource(R.string.onboarding1Body),
                buttonText = stringResource(R.string.onboardingNext),
                topContent = {
                    CardIllustration(cardWidth = 220, cardHeight = 140)
                },
                onNext = {
                    navController.navigate(SecondWelcomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {}
            )
        }
        // Screen 2 — "Tap. Store. Done."
        composable<SecondWelcomeView> {
            WelcomeView(
                screenIndex = 1,
                heading = stringResource(R.string.onboarding2Heading),
                body = stringResource(R.string.onboarding2Body),
                buttonText = stringResource(R.string.onboardingNext),
                topContent = {
                    // Card + NFC waves + Phone illustration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Tilted card
                        Box(
                            modifier = Modifier.graphicsLayer { rotationZ = -10f }
                        ) {
                            CardIllustration(cardWidth = 160, cardHeight = 100)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // NFC waves
                        Image(
                            painter = painterResource(R.drawable.contactless_24px),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(HushColors.textFaint)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Phone outline
                        Canvas(modifier = Modifier.size(width = 50.dp, height = 90.dp)) {
                            drawRoundRect(
                                color = HushColors.border,
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(width = 2f)
                            )
                            // Notch/camera dot
                            drawCircle(
                                color = HushColors.border,
                                radius = 3f,
                                center = Offset(size.width / 2f, 12f)
                            )
                        }
                    }
                },
                onNext = {
                    navController.navigate(ThirdWelcomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.navigate(FirstWelcomeView) {
                        popUpTo(0)
                    }
                }
            )
        }
        // Screen 3 — "Your PIN is everything."
        composable<ThirdWelcomeView> {
            WelcomeView(
                screenIndex = 2,
                heading = stringResource(R.string.onboarding3Heading),
                body = stringResource(R.string.onboarding3Body),
                bodyColor = HushColors.textMuted,
                buttonText = stringResource(R.string.onboardingUnderstand),
                warningText = stringResource(R.string.onboarding3Warning),
                topContent = {
                    Image(
                        painter = painterResource(R.drawable.ic_lock_outline),
                        contentDescription = "Lock",
                        modifier = Modifier.size(64.dp),
                        colorFilter = ColorFilter.tint(HushColors.textFaint)
                    )
                },
                onNext = {
                    navController.navigate(HomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.navigate(SecondWelcomeView) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable<HomeView> {
            HomeView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<MenuView> {
            MenuView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<SettingsView> {
            SettingsView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<FactoryResetView> {
            FactoryResetView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<CardInformation> {
            CardInformation(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<EditCardLabelView> {
            EditCardLabelView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<PinEntryView> {
            val args = it.toRoute<PinEntryView>()
            PinEntryView(
                context = context,
                navController = navController,
                viewModel = viewModel,
                pinCodeAction = PinCodeAction.valueOf(args.pinCodeAction),
                isBackupCard = args.isBackupCard,
            )
        }
        composable<BackupView> {
            BackupView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<AddSecretView> {
            AddSecretView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<MySecretView> {
            ShowSecretView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<ImportSecretView> {
            val args = it.toRoute<ImportSecretView>()
            ImportSecretView(
                context = context,
                navController = navController,
                viewModel = viewModel,
                settings = settings,
                importMode = ImportMode.valueOf(args.importMode),
            )
        }
        composable<ShowCardLogs> {
            ShowCardLogsView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<ShowLogsView> {
            ShowLogsView(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
        }
        composable<AboutView> {
            AboutView(
                context = context,
                navController = navController,
            )
        }
    }

    // NFC Scan Overlay — layered over all screens
    NfcScanOverlay(
        isVisible = viewModel.showNfcOverlay,
        status = viewModel.nfcScanStatus,
        onCancel = { viewModel.dismissNfcOverlay() },
        onSuccessDismiss = {
            viewModel.showNfcOverlay = false
        },
        progress = when {
            viewModel.backupExportProgress > 0f -> viewModel.backupExportProgress
            viewModel.backupImportProgress > 0f -> viewModel.backupImportProgress
            else -> null
        }
    )
    } // end Box
}

@Serializable
object SplashView
@Serializable
object FirstWelcomeView
@Serializable
object SecondWelcomeView
@Serializable
object ThirdWelcomeView
@Serializable
object HomeView
@Serializable
object MenuView
@Serializable
object SettingsView
@Serializable
object CardInformation
@Serializable
object EditCardLabelView
@Serializable
object BackupView
@Serializable
object AddSecretView
@Serializable
object ShowLogsView
@Serializable
object ShowCardLogs
@Serializable
object FactoryResetView
@Serializable
object AboutView
@Serializable
object MySecretView

@Serializable
data class ImportSecretView(
    val importMode: String,
)

@Serializable
data class PinEntryView (
    val pinCodeAction: String,
    val isBackupCard: Boolean = false,
)
