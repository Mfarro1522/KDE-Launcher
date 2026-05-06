package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.IconPackInfo
import dev.vive.kdelauncher.domain.repository.IconPackManager

class LoadIconPacksUseCase(private val iconPackManager: IconPackManager) {
    suspend operator fun invoke(): List<IconPackInfo> {
        return iconPackManager.getInstalledPacks()
    }
}
