package dev.vive.kdelauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.vive.kdelauncher.AppContainer
import dev.vive.kdelauncher.SetDefaultLauncherActivity
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.IconPackManager
import dev.vive.kdelauncher.domain.repository.NotificationTracker
import dev.vive.kdelauncher.domain.repository.ProfileManager
import dev.vive.kdelauncher.domain.repository.SettingsManager
import dev.vive.kdelauncher.domain.repository.WorkProfileManager
import dev.vive.kdelauncher.domain.usecase.GetSystemStatusUseCase
import dev.vive.kdelauncher.domain.usecase.LaunchAppUseCase
import dev.vive.kdelauncher.domain.usecase.LoadAppsUseCase
import dev.vive.kdelauncher.domain.usecase.LoadIconPacksUseCase
import dev.vive.kdelauncher.domain.usecase.SetCategoryOverrideUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleFavoriteUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleWorkAppUseCase
import dev.vive.kdelauncher.service.PackageChangeReceiver
import dev.vive.kdelauncher.ui.components.CategoryConfig
import dev.vive.kdelauncher.ui.components.IconSize
import dev.vive.kdelauncher.ui.components.parseIconSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Complete UI state for the launcher screen.
 */
data class LauncherUiState(
    val allApps: List<AppModel> = emptyList(),
    val filteredApps: List<AppModel> = emptyList(),
    val searchQuery: String = "",
    val activeCategory: AppCategory = AppCategory.FAVORITES,
    val currentProfile: Profile = Profile.Personal,
    val isDarkTheme: Boolean = true,
    val showAppLabels: Boolean = true,
    val showSettings: Boolean = false,
    val isLoading: Boolean = true,
    val appCounts: Map<AppCategory, Int> = emptyMap(),
    val categoryConfigs: List<CategoryConfig> = emptyList(),
    val visibleCategories: List<AppCategory> = AppCategory.entries,
    val iconSize: IconSize = IconSize.MEDIUM,
    val showIconBackground: Boolean = true,
    val gridColumns: Int = 3,
    val installedIconPacks: List<dev.vive.kdelauncher.data.IconPackInfo> = emptyList(),
    val selectedIconPack: String? = null,
    val isLoadingIconPacks: Boolean = false,
    val isDefaultLauncher: Boolean = true,
    val hasRealWorkProfile: Boolean = false,
    val isWorkProfileLocked: Boolean = false,
    val isNotificationAccessGranted: Boolean = false,
)

class LauncherViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val profileManager: ProfileManager,
    private val settingsManager: SettingsManager,
    private val iconPackManager: IconPackManager,
    private val workProfileManager: WorkProfileManager,
    private val notificationTracker: NotificationTracker,
    private val loadAppsUseCase: LoadAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleWorkAppUseCase: ToggleWorkAppUseCase,
    private val loadIconPacksUseCase: LoadIconPacksUseCase,
    private val getSystemStatusUseCase: GetSystemStatusUseCase,
    private val setCategoryOverrideUseCase: SetCategoryOverrideUseCase,
) : AndroidViewModel(application) {

    // ── UI-controlled state ──────────────────────────────────────────────────
    private val _allApps = MutableStateFlow<List<AppModel>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow(AppCategory.FAVORITES)
    private val _showSettings = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _isLoadingIconPacks = MutableStateFlow(false)
    private val _installedIconPacks = MutableStateFlow<List<dev.vive.kdelauncher.data.IconPackInfo>>(emptyList())
    private val _homeResetCounter = MutableStateFlow(0)

    // ── System status ────────────────────────────────────────────────────────
    private val _isDefaultLauncher = MutableStateFlow(true)
    private val _hasRealWorkProfile = MutableStateFlow(false)
    private val _isWorkProfileLocked = MutableStateFlow(false)
    private val _isNotificationAccessGranted = MutableStateFlow(false)

    // ── Package change receiver ──────────────────────────────────────────────
    private val packageChangeReceiver = PackageChangeReceiver(
        onPackageChanged = { refreshApps() }
    )

    // ── Derived flows from reactive repositories ─────────────────────────────
    private val categoryConfigsFlow = combine(
        settingsManager.hiddenCategories,
        settingsManager.categoryDisplayNames,
        settingsManager.categoryIconNames
    ) { hidden, displayNames, iconNames ->
        AppCategory.entries.map { cat ->
            CategoryConfig(
                category = cat,
                displayName = displayNames[cat] ?: cat.displayName,
                iconName = iconNames[cat] ?: "Star",
                isHidden = cat.name in hidden
            )
        }
    }

    private val visibleCategoriesFlow = settingsManager.hiddenCategories.map { hidden ->
        AppCategory.entries.filter { it.name !in hidden }
    }

    // ── Sub-combines (grouped by domain) ─────────────────────────────────────

    private data class AppInput(
        val allApps: List<AppModel>,
        val searchQuery: String,
        val activeCategory: AppCategory
    )

    private val appInput = combine(_allApps, _searchQuery, _activeCategory) { apps, q, cat ->
        AppInput(apps, q, cat)
    }

    private data class SettingsInputPart1(
        val darkTheme: Boolean,
        val showAppLabels: Boolean,
        val showSettings: Boolean,
        val isLoading: Boolean,
        val iconSize: IconSize
    )

    private val settingsInputPart1 = combine(
        settingsManager.darkTheme,
        settingsManager.showAppLabels,
        _showSettings,
        _isLoading,
        settingsManager.iconSize.map { parseIconSize(it) }
    ) { dark, labels, showSettings, loading, size ->
        SettingsInputPart1(dark, labels, showSettings, loading, size)
    }

    private data class SettingsInputPart2(
        val showIconBackground: Boolean,
        val gridColumns: Int,
        val selectedIconPack: String?,
        val isLoadingIconPacks: Boolean,
        val installedIconPacks: List<dev.vive.kdelauncher.data.IconPackInfo>
    )

    private val settingsInputPart2 = combine(
        settingsManager.showIconBackground,
        settingsManager.gridColumns,
        settingsManager.selectedIconPack,
        _isLoadingIconPacks,
        _installedIconPacks
    ) { bg, cols, pack, loadingPacks, packs ->
        SettingsInputPart2(bg, cols, pack, loadingPacks, packs)
    }

    private data class ProfileInput(
        val currentProfile: Profile,
        val favorites: Set<String>,
        val workApps: Set<String>
    )

    private val profileInput = combine(
        profileManager.activeProfile,
        profileManager.favorites,
        profileManager.workApps
    ) { profile, fav, work ->
        ProfileInput(profile, fav, work)
    }

    private data class CategoryInput(
        val categoryConfigs: List<CategoryConfig>,
        val visibleCategories: List<AppCategory>,
        val categoryOverrides: Map<String, AppCategory>
    )

    private val categoryInput = combine(
        categoryConfigsFlow,
        visibleCategoriesFlow,
        settingsManager.categoryOverrides
    ) { configs, visible, overrides ->
        CategoryInput(configs, visible, overrides)
    }

    private data class SystemInput(
        val isDefaultLauncher: Boolean,
        val hasRealWorkProfile: Boolean,
        val isWorkProfileLocked: Boolean,
        val isNotificationAccessGranted: Boolean
    )

    private val systemInput = combine(
        _isDefaultLauncher,
        _hasRealWorkProfile,
        _isWorkProfileLocked,
        _isNotificationAccessGranted
    ) { def, work, locked, notif ->
        SystemInput(def, work, locked, notif)
    }

    // ── Main UI state ────────────────────────────────────────────────────────

    private data class UiInput(
        val app: AppInput,
        val settings1: SettingsInputPart1,
        val settings2: SettingsInputPart2,
        val profile: ProfileInput,
        val category: CategoryInput,
        val system: SystemInput,
        val notificationMap: Map<String, Int>
    )

    private val uiInput = combine(
        combine(appInput, settingsInputPart1, settingsInputPart2) { app, s1, s2 ->
            Triple(app, s1, s2)
        },
        combine(profileInput, categoryInput, systemInput) { profile, cat, sys ->
            Triple(profile, cat, sys)
        },
        notificationTracker.notificationCounts
    ) { appSettings, profileCatSys, notificationMap ->
        UiInput(
            app = appSettings.first,
            settings1 = appSettings.second,
            settings2 = appSettings.third,
            profile = profileCatSys.first,
            category = profileCatSys.second,
            system = profileCatSys.third,
            notificationMap = notificationMap
        )
    }

    val uiState: StateFlow<LauncherUiState> = uiInput.map { input ->
        val hasWork = input.system.hasRealWorkProfile
        val favorites = input.profile.favorites
        val workApps = input.profile.workApps
        val categoryOverrides = input.category.categoryOverrides

        val appsWithMeta = input.app.allApps.map { appModel ->
            val isWorkApp = if (hasWork) appModel.userHandle != null
            else workApps.contains(appModel.packageName)
            val overrideKey = categoryOverrideKey(appModel, isWorkApp)
            val overrideCategory = categoryOverrides[overrideKey]
            appModel.copy(
                isFavorite = favorites.contains(appModel.packageName),
                profileTag = if (isWorkApp) ProfileType.WORK else ProfileType.PERSONAL,
                category = overrideCategory ?: appModel.category,
                notificationCount = input.notificationMap[appModel.packageName] ?: 0
            )
        }

        val profileFiltered = when (input.profile.currentProfile.type) {
            ProfileType.WORK -> appsWithMeta.filter { it.profileTag == ProfileType.WORK }
            ProfileType.PERSONAL -> appsWithMeta.filter { it.profileTag == ProfileType.PERSONAL }
        }

        val filtered = if (input.app.searchQuery.isNotBlank()) {
            appsWithMeta.filter {
                it.label.contains(input.app.searchQuery, ignoreCase = true) ||
                    it.packageName.contains(input.app.searchQuery, ignoreCase = true)
            }
        } else when (input.app.activeCategory) {
            AppCategory.FAVORITES -> profileFiltered.filter { it.isFavorite }
            AppCategory.ALL -> profileFiltered
            else -> profileFiltered.filter { it.category == input.app.activeCategory }
        }

        val counts = AppCategory.entries.associateWith { cat ->
            when (cat) {
                AppCategory.FAVORITES -> profileFiltered.count { it.isFavorite }
                AppCategory.ALL -> profileFiltered.size
                else -> profileFiltered.count { it.category == cat }
            }
        }

        LauncherUiState(
            allApps = appsWithMeta,
            filteredApps = filtered,
            searchQuery = input.app.searchQuery,
            activeCategory = input.app.activeCategory,
            currentProfile = input.profile.currentProfile,
            isDarkTheme = input.settings1.darkTheme,
            showAppLabels = input.settings1.showAppLabels,
            showSettings = input.settings1.showSettings,
            isLoading = input.settings1.isLoading,
            appCounts = counts,
            categoryConfigs = input.category.categoryConfigs,
            visibleCategories = input.category.visibleCategories,
            iconSize = input.settings1.iconSize,
            showIconBackground = input.settings2.showIconBackground,
            gridColumns = input.settings2.gridColumns,
            installedIconPacks = input.settings2.installedIconPacks,
            selectedIconPack = input.settings2.selectedIconPack,
            isLoadingIconPacks = input.settings2.isLoadingIconPacks,
            isDefaultLauncher = input.system.isDefaultLauncher,
            hasRealWorkProfile = input.system.hasRealWorkProfile,
            isWorkProfileLocked = input.system.isWorkProfileLocked,
            isNotificationAccessGranted = input.system.isNotificationAccessGranted,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    val homeResetCounter: StateFlow<Int> = _homeResetCounter

    init {
        try {
            refreshApps()
            refreshIconPacks()
            packageChangeReceiver.register(getApplication())
            refreshSystemStatus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Refresh methods ──────────────────────────────────────────────────────

    fun refreshApps() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val selectedPack = settingsManager.selectedIconPack.first()
                val (metadataApps, fullApps) = loadAppsUseCase(selectedPack)
                _allApps.value = metadataApps
                _isLoading.value = false
                _allApps.value = fullApps
            } catch (e: Exception) {
                e.printStackTrace()
                _allApps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshIconPacks() {
        viewModelScope.launch {
            try {
                _isLoadingIconPacks.value = true
                _installedIconPacks.value = loadIconPacksUseCase()
            } catch (e: Exception) {
                e.printStackTrace()
                _installedIconPacks.value = emptyList()
            } finally {
                _isLoadingIconPacks.value = false
            }
        }
    }

    fun refreshSystemStatus() {
        val status = getSystemStatusUseCase()
        _isDefaultLauncher.value = status.isDefaultLauncher
        _hasRealWorkProfile.value = status.hasRealWorkProfile
        _isWorkProfileLocked.value = status.isWorkProfileLocked
        _isNotificationAccessGranted.value = status.isNotificationAccessGranted
    }

    // ── User actions ─────────────────────────────────────────────────────────

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setActiveCategory(category: AppCategory) {
        _activeCategory.value = category
        _searchQuery.value = ""
    }

    fun resetToHome() {
        _searchQuery.value = ""
        _activeCategory.value = AppCategory.FAVORITES
        _showSettings.value = false
        _homeResetCounter.value = _homeResetCounter.value + 1
    }

    fun handleBackPress(): Boolean {
        return if (_showSettings.value) {
            _showSettings.value = false
            true
        } else {
            false
        }
    }

    fun toggleProfile() {
        viewModelScope.launch {
            val newProfile = if (profileManager.activeProfile.value.type == ProfileType.PERSONAL)
                Profile.Work else Profile.Personal
            profileManager.setActiveProfile(newProfile)
        }
    }

    fun toggleSettings() { _showSettings.value = !_showSettings.value }

    fun toggleTheme() {
        viewModelScope.launch {
            settingsManager.setDarkTheme(!settingsManager.darkTheme.first())
        }
    }

    fun setShowAppLabels(show: Boolean) {
        viewModelScope.launch { settingsManager.setShowAppLabels(show) }
    }

    fun setIconSize(size: IconSize) {
        viewModelScope.launch { settingsManager.setIconSize(size.name.lowercase()) }
    }

    fun setShowIconBackground(show: Boolean) {
        viewModelScope.launch { settingsManager.setShowIconBackground(show) }
    }

    fun setGridColumns(columns: Int) {
        viewModelScope.launch { settingsManager.setGridColumns(columns) }
    }

    fun setCategoryDisplayName(category: AppCategory, name: String) {
        viewModelScope.launch { settingsManager.setCategoryDisplayName(category, name) }
    }

    fun setCategoryIconName(category: AppCategory, iconName: String) {
        viewModelScope.launch { settingsManager.setCategoryIconName(category, iconName) }
    }

    fun toggleCategoryHidden(category: AppCategory) {
        viewModelScope.launch {
            val hidden = settingsManager.hiddenCategories.first().contains(category.name)
            settingsManager.setCategoryHidden(category, !hidden)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsManager.resetAll()
            appRepository.clearIconPackCache()
            refreshApps()
        }
    }

    fun setIconPack(packageName: String?) {
        viewModelScope.launch {
            settingsManager.setSelectedIconPack(packageName)
            appRepository.clearIconPackCache()
            refreshApps()
        }
    }

    fun setCategoryOverride(app: AppModel, category: AppCategory) {
        viewModelScope.launch {
            val isWorkApp = app.userHandle != null || app.profileTag == ProfileType.WORK
            val key = categoryOverrideKey(app, isWorkApp)
            setCategoryOverrideUseCase(key, category)
        }
    }

    fun clearCategoryOverride(app: AppModel) {
        viewModelScope.launch {
            val isWorkApp = app.userHandle != null || app.profileTag == ProfileType.WORK
            val key = categoryOverrideKey(app, isWorkApp)
            setCategoryOverrideUseCase.clear(key)
        }
    }

    fun launchApp(app: AppModel) {
        launchAppUseCase(app)
    }

    fun toggleFavorite(app: AppModel) {
        viewModelScope.launch {
            toggleFavoriteUseCase(app.packageName)
        }
    }

    fun toggleWorkApp(app: AppModel) {
        viewModelScope.launch {
            toggleWorkAppUseCase(app.packageName)
        }
    }

    fun openSetDefaultLauncherScreen() {
        try {
            val app = getApplication<Application>()
            val intent = android.content.Intent(app, SetDefaultLauncherActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            app.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openNotificationSettings() {
        try {
            val intent = android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInfo(app: AppModel) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${app.packageName}")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(app: AppModel) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                data = android.net.Uri.parse("package:${app.packageName}")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshStatus() {
        refreshSystemStatus()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun categoryOverrideKey(app: AppModel, isWorkApp: Boolean): String {
        val scope = if (isWorkApp) "work" else "personal"
        return "$scope:${app.packageName}"
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    class Factory(
        private val container: AppContainer,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(
                application = application,
                appRepository = container.appRepository,
                profileManager = container.profileManager,
                settingsManager = container.settingsManager,
                iconPackManager = container.iconPackManager,
                workProfileManager = container.workProfileManager,
                notificationTracker = container.notificationTracker,
                loadAppsUseCase = container.loadAppsUseCase,
                launchAppUseCase = container.launchAppUseCase,
                toggleFavoriteUseCase = container.toggleFavoriteUseCase,
                toggleWorkAppUseCase = container.toggleWorkAppUseCase,
                loadIconPacksUseCase = container.loadIconPacksUseCase,
                getSystemStatusUseCase = container.getSystemStatusUseCase,
                setCategoryOverrideUseCase = container.setCategoryOverrideUseCase
            ) as T
        }
    }

    override fun onCleared() {
        super.onCleared()
        packageChangeReceiver.unregister(getApplication())
    }
}
