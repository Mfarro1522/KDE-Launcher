package dev.vive.kdelauncher.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver that listens for package install/uninstall/change events
 * and notifies the caller via [onPackageChanged] callback.
 *
 * This class is responsible for its own lifecycle:
 * - [register] must be called to start listening
 * - [unregister] must be called to clean up (typically from [onCleared])
 */
class PackageChangeReceiver(
    private val onPackageChanged: () -> Unit
) : BroadcastReceiver() {

    private var isRegistered = false

    override fun onReceive(context: Context?, intent: Intent?) {
        onPackageChanged()
    }

    fun register(context: Context) {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            context,
            this,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        isRegistered = true
    }

    fun unregister(context: Context) {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(this)
        } catch (_: Exception) {}
        isRegistered = false
    }
}
