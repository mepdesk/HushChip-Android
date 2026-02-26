package uk.co.hushchip.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import uk.co.hushchip.app.ui.components.home.NfcDialog
import uk.co.hushchip.app.ui.theme.SatoGray
import uk.co.hushchip.app.ui.views.addsecret.AddSecretView
import uk.co.hushchip.app.ui.views.backup.BackupView
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
import uk.co.hushchip.app.ui.views.splash.SplashView
import uk.co.hushchip.app.ui.views.welcome.WelcomeView
import uk.co.hushchip.app.utils.webviewActivityIntent
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

    // NFC DIALOG
    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
    if (showNfcDialog.value) {
        NfcDialog(
            openDialogCustom = showNfcDialog,
            resultCodeLive = viewModel.resultCodeLive,
            isConnected = viewModel.isCardConnected
        )
    }

    // FIRST TIME SETUP
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
        composable<FirstWelcomeView> {
            WelcomeView(
                title = R.string.welcome,
                text = R.string.welcomeInfo,
                colors = listOf(Color.White, SatoGray, SatoGray),
                onNext = {
                    navController.navigate(SecondWelcomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {},
                onClick = {}
            )
        }
        composable<SecondWelcomeView> {
            WelcomeView(
                title = R.string.seedphraseManager,
                text = R.string.seedphraseManagerInfo,
                colors = listOf(SatoGray, Color.White, SatoGray),
                onNext = {
                    navController.navigate(ThirdWelcomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.navigate(FirstWelcomeView) {
                        popUpTo(0)
                    }
                },
                onClick = {}
            )
        }
        composable<ThirdWelcomeView> {
            val linkUrl = stringResource(id = R.string.moreInfoUrl)
            WelcomeView(
                title = R.string.usingNfc,
                text = R.string.usingNfcInfo,
                isFullWidth = true,
                link = linkUrl,
                onNext = {
                    navController.navigate(HomeView) {
                        popUpTo(0)
                    }
                },
                onBack = {
                    navController.navigate(SecondWelcomeView) {
                        popUpTo(0)
                    }
                },
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(linkUrl)
                    )
                    val packageManager = context.packageManager
                    val chooserIntent = Intent.createChooser(intent, "Open with")

                    if (chooserIntent.resolveActivity(packageManager) != null) {
                        context.startActivity(chooserIntent)
                    } else {
                        webviewActivityIntent(
                            url = linkUrl,
                            context = context
                        )
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
    }
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
