package dev.vive.kdelauncher

import android.app.Application

class TAPOLauncherApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
