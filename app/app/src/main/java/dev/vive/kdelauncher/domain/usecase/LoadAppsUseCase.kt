package dev.vive.kdelauncher.domain.usecase

import android.app.Application
import android.content.Intent
import dev.vive.kdelauncher.data.model.AppIconBitmap
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.repository.IconDiskCache
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.WorkProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class LoadAppsUseCase(
    private val appRepository: AppRepository,
    private val workProfileManager: WorkProfileManager,
    private val iconDiskCache: IconDiskCache
) {

    /**
     * Limits concurrent PackageManager icon decodings to prevent HWUI saturation.
     * Only acquired for cache MISSES — disk reads are I/O-bound and don't pressure HWUI,
     * so they run fully in parallel without the semaphore.
     * 4 permits ≈ 4 × ~8 MB per decode = ~32 MB peak HWUI pressure.
     */
    private val iconSemaphore = Semaphore(4)

    /**
     * Two-phase loading:
     * 1. Returns metadata-only apps instantly
     * 2. Returns fully-loaded apps with icons (from disk cache or PackageManager)
     *
     * ## Icon loading strategy (fast-path first)
     * 1. Check [IconDiskCache] — O(1) file read, ~5 ms, no HWUI pressure → run in parallel
     * 2. On miss: query PackageManager via [Semaphore] → store result in disk cache
     *
     * ## Pre-warming strategy
     * ImageBitmap lazy properties are pre-warmed in batches with yield() calls between
     * chunks. This prevents monopolizing the CPU and allows Compose frames to render
     * between batches, eliminating the startup scroll jank caused by the old sequential
     * approach that blocked the thread for the full forEach.
     */
    suspend operator fun invoke(selectedIconPack: String?): Pair<List<AppModel>, List<AppModel>> {
        return withContext(Dispatchers.IO) {
            val personalMeta = appRepository.getInstalledAppsMetadata()
            val workMeta = if (workProfileManager.hasRealWorkProfile()) {
                workProfileManager.getWorkProfileApps(loadIcons = false).map { app ->
                    AppModel(
                        packageName = app.packageName,
                        activityName = app.activityName,
                        label = app.label,
                        icon = app.icon,
                        category = dev.vive.kdelauncher.data.model.AppCategorizer.categorize(
                            app.packageName, app.androidCategory, isSystemApp = false
                        ),
                        userHandle = app.userHandle,
                        versionCode = app.versionCode,
                        isSystemApp = false
                    )
                }
            } else emptyList()

            val mergedApps = mergeApps(personalMeta, workMeta)
            val metadataApps = mergedApps

            val iconsByKey = withContext(Dispatchers.IO) {
                val personalDeferreds = personalMeta.map { app ->
                    async {
                        val key = iconKey(app)
                        val bitmap = loadIconWithCache(
                            packageName = app.packageName,
                            activityName = app.activityName,
                            versionCode = app.versionCode,
                            selectedIconPack = selectedIconPack,
                            fromPackageManager = {
                                appRepository.getAppIcon(
                                    packageName = app.packageName,
                                    activityName = app.activityName,
                                    selectedIconPack = selectedIconPack
                                )
                            }
                        )
                        key to bitmap
                    }
                }
                val workDeferreds = if (workMeta.isNotEmpty()) {
                    workMeta.map { app ->
                        async {
                            val key = iconKey(app)
                            // Work profile icons cannot be cached by versionCode alone
                            // (same package, different profile). Load via semaphore.
                            val bitmap = if (app.userHandle != null) {
                                iconSemaphore.withPermit {
                                    workProfileManager.loadWorkAppIcon(
                                        packageName = app.packageName,
                                        activityName = app.activityName,
                                        userHandle = app.userHandle
                                    )
                                }
                            } else null
                            key to bitmap
                        }
                    }
                } else emptyList()

                (personalDeferreds + workDeferreds).awaitAll().toMap()
            }

            val fullApps = metadataApps.map { app ->
                val key = iconKey(app)
                val bitmap = iconsByKey[key]
                if (bitmap != null) app.copy(icon = AppIconBitmap(bitmap)) else app
            }

            // Pre-warm ImageBitmap lazy properties on the IO thread.
            // Process in chunks of 20 with yield() between batches to avoid
            // monopolizing the CPU. This lets Compose render frames between
            // batches so scroll is fluid even if pre-warming is still running.
            val chunkSize = 20
            for (chunk in fullApps.chunked(chunkSize)) {
                chunk.forEach { it.icon?.imageBitmap }
                yield() // release the thread for a frame
            }

            // Evict icons for uninstalled packages (best-effort, non-blocking).
            val knownPackages = personalMeta.map { it.packageName }.toSet()
            iconDiskCache.evictUnknown(knownPackages)

            metadataApps to fullApps
        }
    }

    /**
     * Loads an icon using a two-level cache strategy:
     * 1. [IconDiskCache] (fast, parallel-safe, survives process death)
     * 2. PackageManager via [iconSemaphore] (slow, HWUI-throttled)
     */
    private suspend fun loadIconWithCache(
        packageName: String,
        activityName: String?,
        versionCode: Long,
        selectedIconPack: String?,
        fromPackageManager: suspend () -> android.graphics.Bitmap?
    ): android.graphics.Bitmap? {
        // Icon packs change all icons for a package — the disk cache key must include
        // the pack so switching packs doesn't show stale icons.
        val cacheKey = if (selectedIconPack.isNullOrEmpty()) versionCode
                       else "${selectedIconPack.hashCode()}_$versionCode".hashCode().toLong()

        // Fast path: disk cache hit — no semaphore, no HWUI pressure
        iconDiskCache.get(packageName, cacheKey)?.let { return it }

        // Slow path: decode from PackageManager (throttled to avoid HWUI saturation)
        return iconSemaphore.withPermit {
            // Double-check: another coroutine may have written the cache while we waited
            iconDiskCache.get(packageName, cacheKey)?.let { return@withPermit it }

            fromPackageManager()?.also { bitmap ->
                iconDiskCache.put(packageName, cacheKey, bitmap)
            }
        }
    }

    private fun mergeApps(
        personalApps: List<AppModel>,
        workApps: List<AppModel>
    ): List<AppModel> {
        // distinctBy eliminates any package that appears twice in the same profile scope
        // (can happen on MIUI when an app has multiple launcher activities or when the
        // APK overlay system creates phantom entries). Key = packageName + userHandle
        // so genuine work-profile duplicates (different UserHandle) are kept.
        return (personalApps + workApps)
            .distinctBy { "${it.packageName}|${it.userHandle?.hashCode() ?: 0}" }
            .sortedBy { it.label.lowercase() }
    }



    private fun iconKey(app: AppModel): String {
        val handleId = app.userHandle?.hashCode() ?: 0
        return "${app.packageName}|${app.activityName}|$handleId"
    }
}
