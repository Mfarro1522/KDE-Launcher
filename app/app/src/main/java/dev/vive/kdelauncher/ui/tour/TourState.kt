package dev.vive.kdelauncher.ui.tour

import dev.vive.kdelauncher.R

enum class TourStep(val target: TourTarget, val titleRes: Int, val descriptionRes: Int) {
    WELCOME(TourTarget.None, R.string.tour_welcome_title, R.string.tour_welcome_desc),
    DEFAULT_LAUNCHER_BANNER(TourTarget.Banner, R.string.tour_banner_title, R.string.tour_banner_desc),
    PROFILE_HEADER(TourTarget.ProfileHeader, R.string.tour_profile_title, R.string.tour_profile_desc),
    SETTINGS_GEAR(TourTarget.SettingsButton, R.string.tour_settings_title, R.string.tour_settings_desc),
    SEARCH_BAR(TourTarget.SearchBar, R.string.tour_search_title, R.string.tour_search_desc),
    CATEGORY_SIDEBAR(TourTarget.CategorySidebar, R.string.tour_sidebar_title, R.string.tour_sidebar_desc),
    APP_GRID(TourTarget.AppGrid, R.string.tour_grid_title, R.string.tour_grid_desc),
    LABS(TourTarget.Labs, R.string.tour_labs_title, R.string.tour_labs_desc),
    FINISH(TourTarget.None, R.string.tour_finish_title, R.string.tour_finish_desc);
}

sealed class TourTarget {
    object None : TourTarget()
    object Banner : TourTarget()
    object ProfileHeader : TourTarget()
    object SettingsButton : TourTarget()
    object SearchBar : TourTarget()
    object CategorySidebar : TourTarget()
    object AppGrid : TourTarget()
    object Labs : TourTarget()
}

data class TourState(
    val isActive: Boolean = false,
    val currentStepIndex: Int = 0,
    val steps: List<TourStep> = TourStep.entries
) {
    fun currentStep(): TourStep? = steps.getOrNull(currentStepIndex)
}
