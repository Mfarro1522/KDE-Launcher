package dev.vive.kdelauncher.domain.usecase

import android.content.Intent
import android.graphics.Bitmap
import android.os.UserHandle
import dev.vive.kdelauncher.data.WorkProfileApp
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.WorkProfileManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class LoadAppsUseCaseTest : FunSpec({

    test("merges personal and work profile apps sorted by label") {
        val appRepository = FakeAppRepository(
            metadataApps = listOf(
                AppModel("com.personal.mail", ".Main", "Mail", category = AppCategory.SOCIAL),
                AppModel("com.personal.notes", ".Main", "Notes", category = AppCategory.UTILITIES)
            )
        )
        val workHandle = mockk<UserHandle>(relaxed = true)
        val workProfileManager = FakeWorkProfileManager(
            hasRealWorkProfile = true,
            workApps = listOf(
                WorkProfileApp(
                    packageName = "com.work.docs",
                    activityName = ".Main",
                    label = "Docs",
                    userHandle = workHandle,
                    icon = null,
                    androidCategory = android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY
                )
            )
        )

        val (metadataApps, fullApps) = LoadAppsUseCase(appRepository, workProfileManager)(null)

        metadataApps.map { it.label } shouldContainExactly listOf("Docs", "Mail", "Notes")
        fullApps.map { it.label } shouldContainExactly listOf("Docs", "Mail", "Notes")
        metadataApps.first().userHandle shouldBe workHandle
    }

    test("does not query work profile apps when there is no real work profile") {
        val appRepository = FakeAppRepository(
            metadataApps = listOf(
                AppModel("com.personal.mail", ".Main", "Mail", category = AppCategory.SOCIAL)
            )
        )
        val workProfileManager = FakeWorkProfileManager(hasRealWorkProfile = false)

        val (metadataApps, fullApps) = LoadAppsUseCase(appRepository, workProfileManager)(null)

        metadataApps.map { it.packageName } shouldContainExactly listOf("com.personal.mail")
        fullApps.map { it.packageName } shouldContainExactly listOf("com.personal.mail")
        workProfileManager.getWorkProfileAppsCalls shouldBe 0
    }

    test("loads icons for personal and work apps into the full app list") {
        val personalBitmap = mockk<Bitmap>(relaxed = true)
        val workBitmap = mockk<Bitmap>(relaxed = true)
        val workHandle = mockk<UserHandle>(relaxed = true)

        val appRepository = FakeAppRepository(
            metadataApps = listOf(
                AppModel("com.personal.mail", ".Main", "Mail", category = AppCategory.SOCIAL)
            ),
            iconsByPackage = mapOf("com.personal.mail" to personalBitmap)
        )
        val workProfileManager = FakeWorkProfileManager(
            hasRealWorkProfile = true,
            workApps = listOf(
                WorkProfileApp(
                    packageName = "com.work.docs",
                    activityName = ".Main",
                    label = "Docs",
                    userHandle = workHandle,
                    icon = null,
                    androidCategory = android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY
                )
            ),
            workIconsByPackage = mapOf("com.work.docs" to workBitmap)
        )

        val (_, fullApps) = LoadAppsUseCase(appRepository, workProfileManager)("pack.example")

        fullApps.first { it.packageName == "com.personal.mail" }.icon?.bitmap shouldBe personalBitmap
        fullApps.first { it.packageName == "com.work.docs" }.icon?.bitmap shouldBe workBitmap
        appRepository.requestedSelectedIconPacks shouldContainExactly listOf("pack.example")
    }
})

private class FakeAppRepository(
    private val metadataApps: List<AppModel>,
    private val iconsByPackage: Map<String, Bitmap> = emptyMap()
) : AppRepository {

    val requestedSelectedIconPacks = mutableListOf<String?>()

    override suspend fun getInstalledApps(selectedIconPack: String?): List<AppModel> = metadataApps

    override suspend fun getInstalledAppsMetadata(): List<AppModel> = metadataApps

    override fun getLaunchIntent(packageName: String): Intent? = null

    override suspend fun getAppIcon(
        packageName: String,
        activityName: String,
        selectedIconPack: String?
    ): Bitmap? {
        requestedSelectedIconPacks += selectedIconPack
        return iconsByPackage[packageName]
    }

    override fun clearIconPackCache() = Unit
}

private class FakeWorkProfileManager(
    private val hasRealWorkProfile: Boolean,
    private val workApps: List<WorkProfileApp> = emptyList(),
    private val workIconsByPackage: Map<String, Bitmap> = emptyMap()
) : WorkProfileManager {

    var getWorkProfileAppsCalls = 0
        private set

    override fun hasRealWorkProfile(): Boolean = hasRealWorkProfile

    override fun getWorkProfileHandle(): UserHandle? = workApps.firstOrNull()?.userHandle

    override fun getWorkProfileApps(loadIcons: Boolean): List<WorkProfileApp> {
        getWorkProfileAppsCalls += 1
        return workApps
    }

    override fun launchWorkApp(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Boolean = false

    override fun loadWorkAppIcon(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Bitmap? = workIconsByPackage[packageName]

    override fun isWorkProfileLocked(): Boolean = false
}
