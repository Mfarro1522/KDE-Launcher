package dev.vive.kdelauncher.data.model

data class CategorySuggestion(
    val packageName: String,
    val appName: String,
    val currentCategory: AppCategory,
    val suggestedCategory: AppCategory,
    val reason: String,
)
