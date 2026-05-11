package dev.vive.kdelauncher

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.vive.kdelauncher.ui.LauncherViewModel
import dev.vive.kdelauncher.ui.screens.LauncherScreen
import dev.vive.kdelauncher.ui.theme.KDELauncherTheme

class MainActivity : ComponentActivity() {

    private var launcherViewModel: LauncherViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lock to portrait programmatically rather than via manifest to avoid
        // interfering with system gesture detection (long-press Home → assistant).
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        val container = (application as TAPOLauncherApp).container

        // Use OnBackPressedDispatcher (modern API) instead of the deprecated
        // onBackPressed. This properly integrates with the system gesture
        // navigation and assistant trigger (long-press Home).
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val handled = launcherViewModel?.handleBackPress() == true
                    if (!handled) {
                        // Let the system handle it — Android will never "exit"
                        // a HOME-category activity on back press, so this
                        // simply returns control to the OS without swallowing
                        // the event.
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        )

        setContent {
            val viewModel: LauncherViewModel = viewModel(
                factory = LauncherViewModel.Factory(container, application)
            )
            launcherViewModel = viewModel
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            KDELauncherTheme(
                profile = uiState.currentProfile,
                isDarkTheme = uiState.isDarkTheme,
                colorTheme = uiState.colorTheme
            ) {
                LauncherScreen(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel?.refreshStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            launcherViewModel?.resetToHome()
        }
    }
}
