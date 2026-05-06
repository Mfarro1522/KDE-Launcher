package dev.vive.kdelauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.collectAsState
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
        enableEdgeToEdge()

        val container = (application as TAPOLauncherApp).container

        setContent {
            val viewModel: LauncherViewModel = viewModel(
                factory = LauncherViewModel.Factory(container, application)
            )
            launcherViewModel = viewModel
            val uiState by viewModel.uiState.collectAsState()

            KDELauncherTheme(
                profile = uiState.currentProfile,
                isDarkTheme = uiState.isDarkTheme
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val handled = launcherViewModel?.handleBackPress() == true
        if (!handled) {
            // Do nothing — launcher should not exit on back press
        }
    }
}
