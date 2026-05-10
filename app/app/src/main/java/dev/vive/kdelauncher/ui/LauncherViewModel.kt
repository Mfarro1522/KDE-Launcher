package dev.vive.kdelauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.vive.kdelauncher.AppContainer
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.Profile
import dev.vive.kdelauncher.data.model.ProfileType
import dev.vive.kdelauncher.domain.repository.AppRepository
import dev.vive.kdelauncher.domain.repository.ProfileManager
import dev.vive.kdelauncher.domain.repository.SettingsManager
import dev.vive.kdelauncher.domain.repository.WorkProfileManager
import dev.vive.kdelauncher.domain.usecase.GetSystemStatusUseCase
import dev.vive.kdelauncher.domain.usecase.LaunchAppUseCase
import dev.vive.kdelauncher.domain.usecase.LoadAppsUseCase
import dev.vive.kdelauncher.domain.usecase.LoadIconPacksUseCase
import dev.vive.kdelauncher.domain.usecase.OpenAppInfoUseCase
import dev.vive.kdelauncher.domain.usecase.OpenSetDefaultLauncherUseCase
import dev.vive.kdelauncher.domain.usecase.SetCategoryOverrideUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleFavoriteUseCase
import dev.vive.kdelauncher.domain.usecase.ToggleWorkAppUseCase
import dev.vive.kdelauncher.domain.usecase.UninstallAppUseCase
import dev.vive.kdelauncher.domain.usecase.CheckProductTourStatusUseCase
import dev.vive.kdelauncher.domain.usecase.DismissProductTourUseCase
import dev.vive.kdelauncher.ui.tour.TourState
import dev.vive.kdelauncher.ui.tour.TourStep
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

sealed class OrganizationSuggestionState {
    object Idle : OrganizationSuggestionState()
    object Loading : OrganizationSuggestionState()
    data class Preview(val result: dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase.SuggestionResult) : OrganizationSuggestionState()
    object Applied : OrganizationSuggestionState()
}

/**
 * Complete UI state for the launcher screen.
 */
data class LauncherUiState(
    val allApps: List<AppModel> = emptyList(),
    val filteredApps: List<AppModel> = emptyList(),
    val searchQuery: String = "",
    val activeCategory: String = AppCategory.FAVORITES,
    val currentProfile: Profile = Profile.Personal,
    val isDarkTheme: Boolean = true,
    val colorTheme: dev.vive.kdelauncher.data.model.ColorTheme = dev.vive.kdelauncher.data.model.ColorTheme.SYSTEM,
    val showAppLabels: Boolean = true,
    val showSettings: Boolean = false,
    val isLoading: Boolean = true,
    val appCounts: Map<String, Int> = emptyMap(),
    val categoryConfigs: List<CategoryConfig> = emptyList(),
    val visibleCategories: List<String> = AppCategory.FIXED,
    val iconSize: IconSize = IconSize.MEDIUM,
    val showIconBackground: Boolean = true,
    val gridColumns: Int = 3,
    val installedIconPacks: List<dev.vive.kdelauncher.data.IconPackInfo> = emptyList(),
    val selectedIconPack: String? = null,
    val isLoadingIconPacks: Boolean = false,
    val isDefaultLauncher: Boolean = true,
    val hasRealWorkProfile: Boolean = false,
    val isWorkProfileLocked: Boolean = false,
    val organizationSuggestionState: OrganizationSuggestionState = OrganizationSuggestionState.Idle,
    val pendingInstallSuggestions: List<dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase.Suggestion> = emptyList(),
    val tourState: TourState = TourState()
)

class LauncherViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val profileManager: ProfileManager,
    private val settingsManager: SettingsManager,
    private val workProfileManager: WorkProfileManager,
    private val loadAppsUseCase: LoadAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleWorkAppUseCase: ToggleWorkAppUseCase,
    private val loadIconPacksUseCase: LoadIconPacksUseCase,
    private val getSystemStatusUseCase: GetSystemStatusUseCase,
    private val setCategoryOverrideUseCase: SetCategoryOverrideUseCase,
    private val openSetDefaultLauncherUseCase: OpenSetDefaultLauncherUseCase,
    private val openAppInfoUseCase: OpenAppInfoUseCase,
    private val uninstallAppUseCase: UninstallAppUseCase,
    private val suggestAppOrganizationUseCase: dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase,
    private val categoryCache: dev.vive.kdelauncher.data.repository.CategoryCache,
    private val checkProductTourStatusUseCase: CheckProductTourStatusUseCase,
    private val dismissProductTourUseCase: DismissProductTourUseCase,
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

    private val _tourState = MutableStateFlow(TourState())

    // ── Auto-organize suggestions (offline) ──────────────────────────────────
    private val _organizationSuggestionState = MutableStateFlow<OrganizationSuggestionState>(OrganizationSuggestionState.Idle)

    // ── Pending install suggestions (proactive categorization) ───────────────
    private val _pendingInstallSuggestions = MutableStateFlow<List<dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase.Suggestion>>(emptyList())

    // ── System status ────────────────────────────────────────────────────────
    private val _isDefaultLauncher = MutableStateFlow(true)
    private val _hasRealWorkProfile = MutableStateFlow(false)
    private val _isWorkProfileLocked = MutableStateFlow(false)

    // ── Package change receiver ──────────────────────────────────────────────
    private val packageChangeReceiver = PackageChangeReceiver(
        onPackageChanged = { refreshApps() }
    )

    // ── Derived flows from reactive repositories ─────────────────────────────
    private data class CategoryBaseInput(
        val apps: List<AppModel>,
        val hidden: Set<String>,
        val displayNames: Map<String, String>,
        val iconNames: Map<String, String>,
        val order: List<String>
    )

    private val categoryBaseFlow = combine(
        _allApps,
        settingsManager.hiddenCategories,
        settingsManager.categoryDisplayNames,
        settingsManager.categoryIconNames,
        settingsManager.categoryOrder
    ) { apps, hidden, displayNames, iconNames, order ->
        CategoryBaseInput(apps, hidden, displayNames, iconNames, order)
    }

    private val categoryConfigsFlow = categoryBaseFlow.combine(
        settingsManager.customCategories
    ) { base, custom ->
        val presentCategories = base.apps.map { it.category }.toSortedSet()
        val allCategories = (presentCategories + AppCategory.FIXED + custom).toSortedSet()
        val sortedCategories = allCategories.sortedBy { cat ->
            val idx = base.order.indexOf(cat)
            if (idx >= 0) idx else Int.MAX_VALUE
        }
        sortedCategories.map { cat ->
            CategoryConfig(
                category = cat,
                displayName = base.displayNames[cat] ?: AppCategory.displayName(cat),
                iconName = base.iconNames[cat] ?: AppCategory.defaultIcon(cat),
                isHidden = cat in base.hidden
            )
        }
    }

    private val visibleCategoriesFlow = categoryConfigsFlow.map { configs ->
        configs.filter { !it.isHidden }.map { it.category }
    }

    private val appInput = combine(_allApps, _searchQuery, _activeCategory) { apps, q, cat ->
        LauncherAppInput(apps, q, cat)
    }

    private val settingsThemeInput = combine(
        settingsManager.darkTheme,
        settingsManager.colorTheme
    ) { dark, themeStr ->
        val theme = runCatching { dev.vive.kdelauncher.data.model.ColorTheme.valueOf(themeStr.uppercase()) }
            .getOrDefault(dev.vive.kdelauncher.data.model.ColorTheme.SYSTEM)
        Pair(dark, theme)
    }

    private val settingsDisplayInput = combine(
        settingsThemeInput,
        settingsManager.showAppLabels,
        _showSettings,
        _isLoading,
        settingsManager.iconSize.map { parseIconSize(it) }
    ) { themeInput, labels, showSettings, loading, size ->
        LauncherSettingsDisplayInput(themeInput.first, themeInput.second, labels, showSettings, loading, size)
    }

    private val settingsIconInput = combine(
        settingsManager.showIconBackground,
        settingsManager.gridColumns,
        settingsManager.selectedIconPack,
        _isLoadingIconPacks,
        _installedIconPacks
    ) { bg, cols, pack, loadingPacks, packs ->
        LauncherSettingsIconInput(bg, cols, pack, loadingPacks, packs)
    }

    private val profileInput = combine(
        profileManager.activeProfile,
        profileManager.personalFavorites,
        profileManager.workFavorites,
        profileManager.workApps
    ) { profile, personalFavorites, workFavorites, workApps ->
        LauncherProfileInput(profile, personalFavorites, workFavorites, workApps)
    }

    private val categoryInput = combine(
        categoryConfigsFlow,
        visibleCategoriesFlow,
        settingsManager.categoryOverrides
    ) { configs, visible, overrides ->
        LauncherCategoryInput(configs, visible, overrides)
    }

    private val systemInput = combine(
        _isDefaultLauncher,
        _hasRealWorkProfile,
        _isWorkProfileLocked
    ) { def, work, locked ->
        LauncherSystemInput(def, work, locked)
    }

    // Separate metadata computation (expensive) from filtering (cheap)
    private val appsWithMetaFlow = combine(
        _allApps,
        profileInput,
        categoryInput,
        systemInput
    ) { apps, profile, category, system ->
        LauncherUiStateMapper.mapAppsWithMeta(
            apps = apps,
            profile = profile,
            categoryOverrides = category.categoryOverrides,
            hasRealWorkProfile = system.hasRealWorkProfile,
            workApps = profile.workApps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val appContentState = combine(
        appsWithMetaFlow,
        _searchQuery,
        _activeCategory,
        profileInput
    ) { appsWithMeta, query, activeCategory, profile ->
        LauncherUiStateMapper.mapAppContentFiltered(
            appsWithMeta = appsWithMeta,
            searchQuery = query,
            activeCategory = activeCategory,
            currentProfile = profile.currentProfile
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherAppContentState(
            allApps = emptyList(),
            filteredApps = emptyList(),
            appCounts = emptyMap()
        )
    )

    private val uiInput = combine(
        combine(appInput, settingsDisplayInput, settingsIconInput) { app, display, icon ->
            Triple(app, display, icon)
        },
        combine(profileInput, categoryInput, systemInput) { profile, cat, sys ->
            Triple(profile, cat, sys)
        }
    ) { appSettings, profileCatSys ->
        LauncherUiProjectionInput(
            app = appSettings.first,
            settingsDisplay = appSettings.second,
            settingsIcon = appSettings.third,
            profile = profileCatSys.first,
            category = profileCatSys.second,
            system = profileCatSys.third
        )
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        uiInput,
        appContentState,
        _organizationSuggestionState,
        _pendingInstallSuggestions,
        _tourState
    ) { input, appContent, orgState, pending, tour ->
        LauncherUiStateMapper.map(input, appContent, orgState, pending, tour)
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

            viewModelScope.launch {
                _isLoading.first { !it }
                val isCompleted = checkProductTourStatusUseCase().first()
                if (!isCompleted) {
                    startProductTour()
                }
            }
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
                val (_, fullApps) = loadAppsUseCase(selectedPack)
                val respectedCategories = AppCategory.FIXED.toSet() + AppCategory.AI_EXCLUDED
                _allApps.value = fullApps.map { app ->
                    if (app.category in respectedCategories) app
                    else app.copy(category = AppCategory.ALL)
                }
                categoryCache.purge(_allApps.value.map { it.packageName to it.versionCode })

                // Detect newly installed uncategorized apps and suggest categories
                val suggestionResult = suggestAppOrganizationUseCase(_allApps.value)
                _pendingInstallSuggestions.value = suggestionResult.suggestions
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
    }

    // ── User actions ─────────────────────────────────────────────────────────

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun setActiveCategory(category: String) {
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

    fun setColorTheme(theme: dev.vive.kdelauncher.data.model.ColorTheme) {
        viewModelScope.launch {
            settingsManager.setColorTheme(theme.name)
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

    fun setCategoryDisplayName(category: String, name: String) {
        viewModelScope.launch { settingsManager.setCategoryDisplayName(category, name) }
    }

    fun setCategoryIconName(category: String, iconName: String) {
        viewModelScope.launch { settingsManager.setCategoryIconName(category, iconName) }
    }

    fun toggleCategoryHidden(category: String) {
        viewModelScope.launch {
            val hidden = settingsManager.hiddenCategories.first().contains(category)
            settingsManager.setCategoryHidden(category, !hidden)
        }
    }

    fun setCategoryOrder(order: List<String>) {
        viewModelScope.launch { settingsManager.setCategoryOrder(order) }
    }

    fun deleteCategory(category: String, appCount: Int) {
        viewModelScope.launch {
            _allApps.value = _allApps.value.map { app ->
                if (app.category == category) app.copy(category = AppCategory.ALL) else app
            }
            settingsManager.setCategoryHidden(category, true)
            settingsManager.removeCustomCategory(category)
            if (_activeCategory.value == category) {
                _activeCategory.value = AppCategory.ALL
            }
        }
    }

    fun addCustomCategory(name: String) {
        viewModelScope.launch {
            val id = "custom_${System.currentTimeMillis()}"
            settingsManager.addCustomCategory(id)
            settingsManager.setCategoryDisplayName(id, name)
            settingsManager.setCategoryIconName(id, "Folder")
            val currentOrder = settingsManager.categoryOrder.first()
            settingsManager.setCategoryOrder(currentOrder + id)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            _allApps.value = emptyList()
            _activeCategory.value = AppCategory.FAVORITES
            _searchQuery.value = ""
            _showSettings.value = false
            settingsManager.resetAll()
            categoryCache.clearAll()
            AppCategory.FIXED.forEach { cat ->
                settingsManager.setCategoryHidden(cat, false)
            }
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

    fun setCategoryOverride(app: AppModel, category: String) {
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
            val profile = if (app.userHandle != null || app.profileTag == ProfileType.WORK) {
                Profile.Work
            } else {
                Profile.Personal
            }
            toggleFavoriteUseCase(profile, app.packageName)
        }
    }

    fun toggleWorkApp(app: AppModel) {
        viewModelScope.launch {
            toggleWorkAppUseCase(app.packageName)
        }
    }

    fun openSetDefaultLauncherScreen() {
        try {
            openSetDefaultLauncherUseCase()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun openAppInfo(app: AppModel) {
        try {
            openAppInfoUseCase(app)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(app: AppModel) {
        try {
            uninstallAppUseCase(app)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshStatus() {
        refreshSystemStatus()
    }

    // ── Auto-organize (offline heuristics) ───────────────────────────────────

    fun suggestOrganization() {
        viewModelScope.launch {
            _organizationSuggestionState.value = OrganizationSuggestionState.Loading
            val result = suggestAppOrganizationUseCase(_allApps.value)
            _organizationSuggestionState.value = OrganizationSuggestionState.Preview(result)
        }
    }

    fun applyOrganizationSuggestions(suggestions: List<dev.vive.kdelauncher.domain.usecase.SuggestAppOrganizationUseCase.Suggestion>) {
        viewModelScope.launch {
            suggestions.forEach { suggestion ->
                val app = _allApps.value.find { it.packageName == suggestion.packageName }
                if (app != null) {
                    val isWorkApp = app.userHandle != null || app.profileTag == ProfileType.WORK
                    val key = categoryOverrideKey(app, isWorkApp)
                    setCategoryOverrideUseCase(key, suggestion.proposedCategory)
                }
            }
            _pendingInstallSuggestions.value = emptyList()
            _organizationSuggestionState.value = OrganizationSuggestionState.Applied
        }
    }

    fun cancelOrganization() {
        _organizationSuggestionState.value = OrganizationSuggestionState.Idle
    }

    fun clearPendingInstallSuggestions() {
        _pendingInstallSuggestions.value = emptyList()
    }

    // ── Product Tour ─────────────────────────────────────────────────────────

    fun startProductTour() {
        _tourState.value = TourState(
            isActive = true,
            currentStepIndex = 0,
            steps = buildTourSteps()
        )
    }

    private fun buildTourSteps(): List<TourStep> {
        val baseSteps = TourStep.entries.filter {
            it != TourStep.LABS && it != TourStep.DEFAULT_LAUNCHER_BANNER
        }.toMutableList()

        if (!_isDefaultLauncher.value) {
            baseSteps.add(1, TourStep.DEFAULT_LAUNCHER_BANNER)
        }

        return baseSteps
    }

    fun nextTourStep() {
        val current = _tourState.value
        if (current.currentStepIndex < current.steps.lastIndex) {
            _tourState.value = current.copy(currentStepIndex = current.currentStepIndex + 1)
        } else {
            dismissProductTour()
        }
    }

    fun previousTourStep() {
        val current = _tourState.value
        if (current.currentStepIndex > 0) {
            _tourState.value = current.copy(currentStepIndex = current.currentStepIndex - 1)
        }
    }

    fun skipProductTour() {
        dismissProductTour()
    }

    fun dismissProductTour() {
        viewModelScope.launch {
            _tourState.value = _tourState.value.copy(isActive = false)
            dismissProductTourUseCase()
        }
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
                workProfileManager = container.workProfileManager,
                loadAppsUseCase = container.loadAppsUseCase,
                launchAppUseCase = container.launchAppUseCase,
                toggleFavoriteUseCase = container.toggleFavoriteUseCase,
                toggleWorkAppUseCase = container.toggleWorkAppUseCase,
                loadIconPacksUseCase = container.loadIconPacksUseCase,
                getSystemStatusUseCase = container.getSystemStatusUseCase,
                setCategoryOverrideUseCase = container.setCategoryOverrideUseCase,
                openSetDefaultLauncherUseCase = container.openSetDefaultLauncherUseCase,
                openAppInfoUseCase = container.openAppInfoUseCase,
                uninstallAppUseCase = container.uninstallAppUseCase,
                suggestAppOrganizationUseCase = container.suggestAppOrganizationUseCase,
                categoryCache = container.categoryCache,
                checkProductTourStatusUseCase = container.checkProductTourStatusUseCase,
                dismissProductTourUseCase = container.dismissProductTourUseCase,
            ) as T
        }
    }

    override fun onCleared() {
        super.onCleared()
        packageChangeReceiver.unregister(getApplication())
    }
}
