# Plan de Refactorización — Checklist

> Este archivo rastrea el progreso de la refactorización técnica de TAPO-Launcher.
> **No borrar hasta completar todas las fases.**

---

## Fase 1 — Fixes Inmediatos (1-2 días)

- [x] Arreglar CI/CD para build release en lugar de debug (`assembleRelease`)
- [x] Crear `proguard-rules.pro` con reglas básicas de keep para Compose
- [x] Eliminar dependencia de Navigation Compose si no se usa
- [x] Normalizar nombres del proyecto en todos los archivos (un solo branding)
- [x] Arreglar o eliminar `build-release.sh`

---

## Fase 2 — Refactorización Estructural (1-2 semanas)

- [x] Extraer UseCases del ViewModel (`LoadApps`, `LaunchApp`, `ToggleFavorite`, etc.)
- [x] Crear interfaces para Repositorios y Managers (contratos de capa de datos)
- [x] Migrar `SettingsManager` de SharedPreferences a DataStore
- [x] Introducir inyección de dependencias (DI manual vía AppContainer)
- [x] Simplificar el `combine()` del ViewModel: usar un solo `data class` de estado interno
- [x] Mover lógica de BroadcastReceiver fuera del ViewModel
- [x] Convertir `NotificationTracker` de `object` singleton a repositorio inyectado

---

## Fase 3 — Calidad y Testing (2+ semanas)

- [ ] Agregar tests unitarios para `AppCategorizer`
- [ ] Agregar tests unitarios para `IconPackManager`
- [ ] Agregar tests unitarios para `SettingsManager` / DataStore
- [ ] Agregar tests de UI básicos con Compose Test
- [ ] Refactorizar `AppRepository` para eliminar duplicación (`getInstalledApps` vs `getInstalledAppsMetadata`)
- [ ] Mejorar categorización con sistema basado en scoring o allowlist exacta
- [ ] Agregar tests para `WorkProfileManager` (mock de `UserManager` / `LauncherApps`)
- [ ] Verificar que ProGuard no rompe la app en release (smoke test manual)

---

## Notas

- **Fase actual:** `Fase 2 — Refactorización Estructural (completada)`
- **Última actualización:** `2026-05-05`
- **Bloqueos actuales:** `Ninguno`
