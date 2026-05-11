---
type: "query"
date: "2026-05-10T07:39:53.279643+00:00"
question: "Why does LauncherViewModel connect to Set Category Override, Tour Product Dismiss, Uninstall Invoke, Icon Packs Load, Toggle Favorite Invoke, Launch Invoke, Open Info Invoke, Toggle Work Invoke, Open Set Default, Organization View Model, Suggest Organization Suggestion?"
contributor: "graphify"
source_nodes: ["LauncherViewModel", "LauncherUiState", "OrganizationSuggestionState"]
---

# Q: Why does LauncherViewModel connect to Set Category Override, Tour Product Dismiss, Uninstall Invoke, Icon Packs Load, Toggle Favorite Invoke, Launch Invoke, Open Info Invoke, Toggle Work Invoke, Open Set Default, Organization View Model, Suggest Organization Suggestion?

## Answer

LauncherViewModel is the central orchestration hub of the TAPO Launcher. It connects to all these distinct communities because it monolithically handles almost every user-facing operation: (1) App operations via UseCases like LaunchAppUseCase, OpenAppInfoUseCase, UninstallAppUseCase, ToggleFavoriteUseCase, ToggleWorkAppUseCase; (2) Visual customization via LoadIconPacksUseCase and SetCategoryOverrideUseCase; (3) AI organization via SuggestAppOrganizationUseCase and its internal OrganizationSuggestionState (Idle/Loading/Preview/Applied); (4) Product tour flow via DismissProductTourUseCase and CheckProductTourStatusUseCase; and (5) System-level actions like OpenSetDefaultLauncherUseCase. It bridges UI state (LauncherUiState) with domain data through LauncherUiStateMapper and directly calls into ProfileManager, SettingsManager, WorkProfileManager, and CategoryCache.

## Source Nodes

- LauncherViewModel
- LauncherUiState
- OrganizationSuggestionState