package dev.vive.kdelauncher.data

import android.graphics.Bitmap
import android.os.UserHandle
import dev.vive.kdelauncher.data.platform.WorkProfilePlatformGateway
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class WorkProfileManagerImplTest : FunSpec({

    test("caches the work profile handle after first lookup") {
        val handle = mockk<UserHandle>(relaxed = true)
        val gateway = FakeWorkProfilePlatformGateway(handle = handle)
        val manager = WorkProfileManagerImpl(gateway)

        manager.getWorkProfileHandle() shouldBe handle
        manager.getWorkProfileHandle() shouldBe handle

        gateway.handleLookups shouldBe 1
    }

    test("reports locked work profile when quiet mode is enabled") {
        val handle = mockk<UserHandle>(relaxed = true)
        val gateway = FakeWorkProfilePlatformGateway(handle = handle, quietModeEnabled = true)
        val manager = WorkProfileManagerImpl(gateway)

        manager.isWorkProfileLocked() shouldBe true
    }

    test("returns empty list when launcher apps access throws SecurityException") {
        val handle = mockk<UserHandle>(relaxed = true)
        val gateway = FakeWorkProfilePlatformGateway(
            handle = handle,
            getAppsError = SecurityException("denied")
        )
        val manager = WorkProfileManagerImpl(gateway)

        manager.getWorkProfileApps(loadIcons = false) shouldBe emptyList()
    }

    test("returns empty list when launcher apps returns no activities") {
        val handle = mockk<UserHandle>(relaxed = true)
        val gateway = FakeWorkProfilePlatformGateway(handle = handle, apps = emptyList())
        val manager = WorkProfileManagerImpl(gateway)

        manager.getWorkProfileApps(loadIcons = true) shouldBe emptyList()
    }

    test("returns work apps from gateway when profile exists") {
        val handle = mockk<UserHandle>(relaxed = true)
        val app = WorkProfileApp(
            packageName = "com.work.docs",
            activityName = ".Main",
            label = "Docs",
            userHandle = handle,
            icon = null,
            androidCategory = 0
        )
        val gateway = FakeWorkProfilePlatformGateway(handle = handle, apps = listOf(app))
        val manager = WorkProfileManagerImpl(gateway)

        manager.getWorkProfileApps(loadIcons = false).map { it.packageName } shouldContainExactly
            listOf("com.work.docs")
    }
})

private class FakeWorkProfilePlatformGateway(
    private val handle: UserHandle? = null,
    private val apps: List<WorkProfileApp> = emptyList(),
    private val quietModeEnabled: Boolean = false,
    private val getAppsError: Exception? = null
) : WorkProfilePlatformGateway {

    var handleLookups = 0
        private set

    override fun getWorkProfileHandle(): UserHandle? {
        handleLookups += 1
        return handle
    }

    override fun getWorkProfileApps(userHandle: UserHandle, loadIcons: Boolean): List<WorkProfileApp> {
        getAppsError?.let { throw it }
        return apps
    }

    override fun launchWorkApp(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Boolean = true

    override fun loadWorkAppIcon(
        packageName: String,
        activityName: String,
        userHandle: UserHandle
    ): Bitmap? = null

    override fun isQuietModeEnabled(userHandle: UserHandle): Boolean = quietModeEnabled
}
