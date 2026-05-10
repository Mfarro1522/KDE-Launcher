package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppCategorizer
import dev.vive.kdelauncher.data.model.AppModel

/**
 * Offline suggestion engine that uses local heuristics to propose
 * categories for uncategorized apps. No network, no API keys, no latency.
 */
class SuggestAppOrganizationUseCase {

    data class Suggestion(
        val packageName: String,
        val label: String,
        val proposedCategory: String,
        val iconName: String,
        val source: String = "heuristics"
    )

    data class SuggestionResult(
        val totalCandidates: Int,
        val suggestions: List<Suggestion>
    )

    operator fun invoke(apps: List<AppModel>): SuggestionResult {
        val candidates = apps.filter {
            it.profileTag == dev.vive.kdelauncher.data.model.ProfileType.PERSONAL &&
                it.category == AppCategory.ALL
        }

        val suggestions = candidates.mapNotNull { app ->
            val proposed = AppCategorizer.categorize(
                app.packageName,
                -1,
                app.isSystemApp
            )
            if (proposed == AppCategory.ALL || proposed in AppCategory.AI_EXCLUDED) {
                null
            } else {
                val icon = LocalHeuristics.classify(app.packageName)?.second
                    ?: AppCategory.defaultIcon(proposed)
                Suggestion(
                    packageName = app.packageName,
                    label = app.label,
                    proposedCategory = proposed,
                    iconName = icon
                )
            }
        }

        return SuggestionResult(
            totalCandidates = candidates.size,
            suggestions = suggestions
        )
    }
}