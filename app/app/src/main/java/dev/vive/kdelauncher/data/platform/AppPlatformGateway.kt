package dev.vive.kdelauncher.data.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

data class InstalledLauncherApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val androidCategory: Int
)

interface AppPlatformGateway {
    fun queryLaunchableApps(excludedPackageName: String): List<InstalledLauncherApp>
    fun getLaunchIntent(packageName: String): Intent?
    fun loadAppIcon(packageName: String, activityName: String): Bitmap?
}

class AndroidAppPlatformGateway(
    private val context: Context
) : AppPlatformGateway {

    override fun queryLaunchableApps(excludedPackageName: String): List<InstalledLauncherApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }

        val seen = mutableSetOf<String>()
        return resolveInfos
            .filter { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == excludedPackageName) return@filter false
                seen.add(packageName)
            }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                InstalledLauncherApp(
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    label = resolveInfo.loadLabel(pm).toString(),
                    androidCategory = activityInfo.applicationInfo?.category ?: -1
                )
            }
    }

    override fun getLaunchIntent(packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    override fun loadAppIcon(packageName: String, activityName: String): Bitmap? {
        return try {
            val activityInfo = context.packageManager.getActivityInfo(
                ComponentName(packageName, activityName),
                0
            )
            activityInfo.loadIcon(context.packageManager)?.toBitmap(128, 128)
        } catch (_: Exception) {
            null
        }
    }
}
