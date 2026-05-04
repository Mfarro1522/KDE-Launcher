package dev.vive.kdelauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import dev.vive.kdelauncher.data.IconPackManager
import dev.vive.kdelauncher.data.model.AppCategorizer
import dev.vive.kdelauncher.data.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository that queries the device's PackageManager for all launchable apps,
 * auto-categorizes them, de-duplicates by package name, and applies icon packs.
 */
class AppRepository(private val context: Context) {

    private val iconPackManager = IconPackManager(context)

    /**
     * Fetch all launchable applications.
     * Optionally overlays icons from [selectedIconPack] (package name, or null for system).
     */
    suspend fun getInstalledApps(
        selectedIconPack: String? = null
    ): List<AppModel> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }

            val seen = mutableSetOf<String>()

            resolveInfos
                .filter { ri ->
                    val pkg = ri.activityInfo.packageName
                    if (pkg == context.packageName) return@filter false
                    seen.add(pkg)
                }
                .map { ri ->
                    val activityInfo = ri.activityInfo
                    val appInfo = activityInfo.applicationInfo
                    val androidCategory = appInfo?.category ?: -1

                    // Try icon pack first, fall back to system icon
                    val bitmap = if (selectedIconPack != null) {
                        iconPackManager.loadIcon(
                            iconPackPackage = selectedIconPack,
                            componentPackage = activityInfo.packageName,
                            activityName = activityInfo.name
                        ) ?: try {
                            ri.loadIcon(pm)?.toBitmap(96, 96)
                        } catch (_: Exception) { null }
                    } else {
                        try {
                            ri.loadIcon(pm)?.toBitmap(96, 96)
                        } catch (_: Exception) { null }
                    }

                    AppModel(
                        packageName = activityInfo.packageName,
                        activityName = activityInfo.name,
                        label = ri.loadLabel(pm).toString(),
                        iconBitmap = bitmap,
                        category = AppCategorizer.categorize(
                            activityInfo.packageName,
                            androidCategory
                        )
                    )
                }
                .sortedBy { it.label.lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    fun clearIconPackCache() {
        iconPackManager.clearCache()
    }
}
