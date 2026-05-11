package dev.vive.kdelauncher.data.repository

import android.content.Context
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.ProfileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persistent on-disk cache for app metadata.
 *
 * Unlike [AppListCache] (which lives only in memory and is lost on process death),
 * this cache writes app metadata to a JSON file in the app's private files directory.
 * Bitmap icons are NOT persisted — they are re-fetched from the icon cache or
 * PackageManager on demand.
 *
 * ## Why this matters for process death
 *
 * Android kills background processes aggressively. When the launcher process is
 * killed and the user returns home, the in-memory [AppListCache] is empty.
 * Without a disk cache, the launcher has to query PackageManager for every app,
 * decode every icon, and re-run heuristics before showing anything — a 10-20s
 * cold-start experience.
 *
 * With this persistent cache:
 * 1. Launcher reads metadata from disk (~50ms for 200 apps).
 * 2. UI renders instantly with app names and categories.
 * 3. Icons appear as they load (or show placeholders).
 * 4. Background refresh updates the list if apps were installed/removed.
 */
class PersistentAppCache(private val context: Context) {

    private val cacheFile: File
        get() = File(context.filesDir, "app_metadata_cache.json")

    private data class CachedSnapshot(
        val apps: List<AppModel>,
        val timestamp: Long
    )

    /**
     * Read the cached app list from disk.
     * Returns null if the cache file does not exist, is corrupt, or is too old.
     */
    suspend fun read(maxAgeMs: Long = DEFAULT_MAX_AGE_MS): List<AppModel>? = withContext(Dispatchers.IO) {
        try {
            val file = cacheFile
            if (!file.exists()) return@withContext null

            val json = JSONObject(file.readText())
            val version = json.optInt("version", 0)
            if (version != CACHE_VERSION) return@withContext null

            val timestamp = json.optLong("timestamp", 0L)
            if (System.currentTimeMillis() - timestamp > maxAgeMs) return@withContext null

            val appsArray = json.getJSONArray("apps")
            val apps = mutableListOf<AppModel>()
            for (i in 0 until appsArray.length()) {
                val appJson = appsArray.getJSONObject(i)
                apps.add(parseAppModel(appJson))
            }
            apps
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Write the app list to disk. Icons (Bitmaps) are stripped before saving.
     */
    suspend fun write(apps: List<AppModel>) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("version", CACHE_VERSION)
                put("timestamp", System.currentTimeMillis())
                put("apps", JSONArray().apply {
                    apps.forEach { app ->
                        put(serializeAppModel(app))
                    }
                })
            }
            cacheFile.writeText(json.toString())
        } catch (_: Exception) {
            // Silently ignore write failures — the cache is best-effort.
        }
    }

    /**
     * Clear the persistent cache. Called when the user explicitly wants a full reset.
     */
    suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            cacheFile.delete()
        } catch (_: Exception) {
            // Ignore
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun serializeAppModel(app: AppModel): JSONObject {
        return JSONObject().apply {
            put("packageName", app.packageName)
            put("activityName", app.activityName)
            put("label", app.label)
            put("category", app.category)
            put("isFavorite", app.isFavorite)
            put("profileTag", app.profileTag.name)
            put("versionCode", app.versionCode)
            put("isSystemApp", app.isSystemApp)
        }
    }

    private fun parseAppModel(json: JSONObject): AppModel {
        return AppModel(
            packageName = json.getString("packageName"),
            activityName = json.getString("activityName"),
            label = json.getString("label"),
            icon = null, // Icons are not persisted; they reload from memory cache or PackageManager
            category = json.optString("category", dev.vive.kdelauncher.data.model.AppCategory.ALL),
            isFavorite = json.optBoolean("isFavorite", false),
            profileTag = runCatching {
                ProfileType.valueOf(json.optString("profileTag", "PERSONAL"))
            }.getOrDefault(ProfileType.PERSONAL),
            userHandle = null, // UserHandle is not serializable and not needed for display
            versionCode = json.optLong("versionCode", 0L),
            isSystemApp = json.optBoolean("isSystemApp", false)
        )
    }

    companion object {
        private const val CACHE_VERSION = 1
        private const val DEFAULT_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }
}
