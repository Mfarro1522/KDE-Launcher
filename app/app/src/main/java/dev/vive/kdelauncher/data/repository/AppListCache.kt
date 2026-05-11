package dev.vive.kdelauncher.data.repository

import dev.vive.kdelauncher.data.model.AppModel

/**
 * In-memory cache for the last loaded app list.
 *
 * Survives Activity and ViewModel recreation as long as the process lives.
 * When Android kills the process the cache is lost, but the manifest fixes
 * (no clearTaskOnLaunch, largeHeap) drastically reduce how often that happens.
 *
 * The ViewModel seeds [_allApps] from this cache on init for an instant warm start.
 */
class AppListCache {

    @Volatile
    var lastApps: List<AppModel> = emptyList()
        private set

    @Volatile
    var lastLoadTimestamp: Long = 0L
        private set

    fun update(apps: List<AppModel>) {
        lastApps = apps
        lastLoadTimestamp = System.currentTimeMillis()
    }

    fun isFresh(maxAgeMs: Long = CACHE_MAX_AGE_MS): Boolean {
        return lastApps.isNotEmpty() &&
            (System.currentTimeMillis() - lastLoadTimestamp) < maxAgeMs
    }

    fun clear() {
        lastApps = emptyList()
        lastLoadTimestamp = 0L
    }

    companion object {
        private const val CACHE_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes
    }
}
