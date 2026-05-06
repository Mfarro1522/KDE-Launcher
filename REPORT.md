# Informe Técnico: TAPO-Launcher (KDE-Launcher / Vøid Launcher)

> **Fecha:** 2026-05-05
> **Análisis:** Auditoría de código completa — arquitectura, calidad, CI/CD, seguridad y mantenibilidad.
> **Veredicto preliminar:** La app **funciona**, pero está construida sobre una base técnicamente frágil. Necesita refactorización estructural antes de escalar.

---

## 1. Resumen Ejecutivo

TAPO-Launcher es un launcher Android nativo con Kotlin + Jetpack Compose que cumple su propósito funcional: muestra apps, permite categorización, soporta icon packs, work profiles y notificaciones. Sin embargo, el código presenta **concentración extrema de responsabilidades**, **falta de arquitectura limpia**, **deuda técnica significativa en CI/CD** y **cero cobertura de tests**.

No es un "montón de escombros", pero sí un **edificio funcional con cimientos agrietados**. Cada nueva feature que se agregue aumentará exponencialmente el costo de mantenimiento.

---

## 2. Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| **Líneas de Kotlin** | ~1,381 (sin contar recursos XML) |
| **Archivos Kotlin** | 18 |
| **Tests** | 0 (cero unitarios, cero instrumentados) |
| **Módulos** | 1 (`app/app`) |
| **Dependencias externas** | Mínimas (solo AndroidX + Compose + Coroutines) |
| **ProGuard/R8 rules** | Referenciado pero **archivo ausente** |
| **Skills instaladas** | 8 (localmente en `.agents/skills/`) |

---

## 3. Problemas Críticos (Bloqueantes o de Alto Riesgo)

### 3.1 ViewModel God Object — `LauncherViewModel.kt` (692 líneas)

**Severidad: CRÍTICA**

Este archivo es el corazón del problema arquitectónico. Contiene:

- 15+ `MutableStateFlow` privados
- Lógica de negocio (carga de apps, icon packs, work profiles)
- Persistencia (SharedPreferences vía SettingsManager)
- Sistema de notificaciones
- Navegación implícita (lanzar activities)
- Gestión de BroadcastReceivers
- Un `combine()` de 4 niveles de anidamiento con `List<Any>`, `Triple`, y **unchecked casts masivos**

```kotlin
// Líneas 167-174 — Esto es una bomba de mantenimiento:
val uiState: StateFlow<LauncherUiState> = combine(
    combine(_allApps, _searchQuery) { apps, q -> apps to q },
    combine(_activeCategory, _iconPackState, _iconSettingsState) { cat, ipState, iconSettings -> Triple(cat, ipState, iconSettings) },
    combine(_secondaryState, _statusState) { sec, stat -> sec to stat },
    combine(
        combine(_profileData, _categoryState) { pd, cs -> pd to cs },
        combine(_notificationCounts, _isNotificationAccessGranted) { nc, ag -> nc to ag }
    ) { pdCs, notifData -> pdCs to notifData }
) { (allApps, query), (category, ipState, iconSettingsData), (secondary, status), (pdCs, notifData) ->
    // ... 90+ líneas de casts y desempaquetado manual
```

**Problemas derivados:**
- Un cambio en cualquier StateFlow dispara recomputación del estado entero.
- Los casts `@Suppress("UNCHECKED_CAST")` ocultan errores de tipo que solo fallarán en runtime.
- Es imposible unit-testear este ViewModel sin mockear Android completamente.

### 3.2 CI/CD Roto — Releases Buildan Debug

**Severidad: CRÍTICA**

`.github/workflows/release.yml` ejecuta:
```yaml
run: ./gradlew assembleDebug --no-daemon --stacktrace
```

Esto genera un **APK debug para releases de producción**. Los APKs debug:
- No tienen optimización de código (R8/ProGuard desactivado en debug)
- Incluyen información de debug innecesaria
- Tienen peor rendimiento

Además, `build-release.sh` está roto (cd a repo root en lugar de `app/`, llama `./gradlew` desde lugar incorrecto).

### 3.3 ProGuard Rules — Archivo Ausente

`build.gradle.kts` referencia:
```kotlin
proguardFiles(
    getDefaultProguardFile("proguard-android-optimize.txt"),
    "proguard-rules.pro"
)
```

Pero **`proguard-rules.pro` no existe en el repositorio**. Esto significa que R8 solo usa las reglas por defecto, lo cual puede causar:
- Crash en release por reflexión u obfuscación incorrecta
- Falta de reglas de keep para Compose runtime

### 3.4 BroadcastReceiver Potencialmente Inseguro

`LauncherViewModel` registra un `packageReceiver` con `RECEIVER_EXPORTED`:
```kotlin
ContextCompat.registerReceiver(
    getApplication<Application>(),
    packageReceiver,
    filter,
    ContextCompat.RECEIVER_EXPORTED  // ← cualquier app puede enviar estos intents
)
```

Aunque el filtro usa `package` data scheme, un receiver exportado para eventos de sistema incrementa la superficie de ataque.

---

## 4. Deuda Técnica Arquitectónica

### 4.1 Sin Capas de Abstracción

La arquitectura actual mezcla todo en un solo módulo sin separación clara:

| Capa | Estado |
|------|--------|
| **UI (Compose)** | Acoplada directamente al ViewModel |
| **ViewModel** | Contiene lógica de dominio, datos y sistema |
| **Repositorio** | Solo `AppRepository` existe; Settings/Profile/Work son Managers sin interfaz |
| **Datasource** | Inexistente (SharedPreferences accedido directamente) |
| **UseCases** | Inexistentes |

**Recomendación:** Separar en capas con contratos (interfaces). El ViewModel debería delegar en UseCases, que delegan en Repositorios.

### 4.2 Persistencia Acoplada — SharedPreferences Crudo

`SettingsManager` y `ProfileManager` usan `SharedPreferences` directamente con **múltiples `.apply()` por operación**:

```kotlin
// SettingsManager.kt — cada llamada genera un write async al disco
fun setDarkTheme(isDark: Boolean) {
    prefs.edit().putBoolean("dark_theme", isDark).apply()
}
```

Esto genera I/O disperso y no transaccional. Debería migrarse a **DataStore** (Preferences o Proto) que ofrece:
- APIs async/Flow nativas
- Transaccionalidad
- Type safety
- Mejor performance en lecturas concurrentes

### 4.3 No Hay Inyección de Dependencias

Todos los managers se instancian con `new` dentro del ViewModel:
```kotlin
private val appRepository = AppRepository(application)
private val profileManager = ProfileManager(application)
private val settingsManager = SettingsManager(application)
// ...
```

Esto hace el código **imposible de testear** sin framework de inyección. La solución mínima es usar un `Application`-level container manual; lo ideal es Hilt o Koin.

### 4.4 NotificationTracker — Singleton Global con Estado Mutable

```kotlin
// NotificationService.kt
object NotificationTracker {
    private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
}
```

Un `object` (singleton) expone un `MutableStateFlow` que cualquier parte de la app puede mutar. No hay control de lifecycle ni de acceso. Debería ser un repositorio inyectado.

---

## 5. Problemas de Calidad de Código

### 5.1 Duplicación Masiva en `AppRepository`

`getInstalledApps()` y `getInstalledAppsMetadata()` son **casi idénticas** (~50 líneas cada una). La única diferencia es que una carga iconos y la otra no. Esto viola DRY y duplica bugs.

### 5.2 Categorización Frágil

`AppCategorizer` usa substrings genéricos como heurística:

```kotlin
"play" to AppCategory.GAMES,       // "Google Play Store"? "Play Music"?
"player" to AppCategory.MULTIMEDIA, // Conflicto con "play"
"manager" to AppCategory.SYSTEM,    // "Pages Manager" de Facebook
```

Esto produce categorizaciones incorrectas silenciosamente. Necesita un scoring system o allowlist por package name exacto con fallback a heurísticas.

### 5.3 `calculateColumnRange` Hardcodea Ancho de Pantalla

```kotlin
// LauncherSettingsPanel.kt
val screenWidthDp = 360f  // ← Valor mágico fijo
```

Esto ignora tablets, foldables y dispositivos con densidades diferentes. Debería usar `LocalConfiguration.current.screenWidthDp`.

### 5.4 `KDELauncherApp` Vacío

```kotlin
class KDELauncherApp : Application() {
    override fun onCreate() { super.onCreate() }
}
```

Declara una `Application` custom innecesaria. Si no inicializa nada, se puede eliminar y quitar `android:name` del manifest.

### 5.5 Manifest Bloquea Orientación Landscape

```xml
android:screenOrientation="portrait"
```

Esto impide usar el launcher en tablets o en modo landscape. Para un launcher, debería ser `unspecified` o `fullUser`.

### 5.6 IconPackManager — Resource Leak Potencial

```kotlin
// Línea 178: stream.close() nunca se ejecuta si hay excepción ANTES
stream.close()
return result
```

Debería usar `use { }` para auto-cierre. Además, el método `parseXmlFilter` acepta `Any` como parámetro cuando solo soporta `XmlPullParser`.

### 5.7 DropdownMenu con `expanded = true` Hardcodeado

```kotlin
// AppIcon.kt línea 235
DropdownMenu(
    expanded = true,  // Siempre true, controlado solo por composición condicional
    onDismissRequest = { menuExpanded = false },
```

Funciona porque el `if (menuExpanded)` lo envuelve, pero es un patrón confuso que puede causar recomposiciones innecesarias.

---

## 6. Problemas de Nomenclatura y Branding

El proyecto tiene **4 nombres diferentes** dependiendo de dónde mires:

| Ubicación | Nombre Usado |
|-----------|-------------|
| Repo / Carpeta | TAPO-Launcher |
| Gradle (`settings.gradle.kts`) | KDE-Launcher |
| Package ID | `dev.vive.kdelauncher` |
| CI Release | Vøid Launcher |
| Clase Application | `KDELauncherApp` |
| Manifest label | KDE Launcher Notifications |

Esto genera confusión en código, documentación y distribución. **Debería normalizarse a un solo nombre** (recomendado: `TAPO-Launcher`).

---

## 7. Estado del Ecosistema de Build

### 7.1 Dependencias Incluidas pero No Usadas

- `androidx.navigation.compose` está en `libs.versions.toml` y `build.gradle.kts`, pero **no hay navegación** en la app (es single-screen).

### 7.2 Versiones

Las versiones están razonablemente actualizadas:
- AGP 8.7.3 ✅
- Kotlin 2.1.0 ✅
- Compose BOM 2024.12.01 ✅
- Compile SDK 35 ✅

### 7.3 `local.properties` en Git

El archivo `app/local.properties` existe en el repo. Aunque `.gitignore` típicamente lo excluye, esto puede filtrar paths locales del desarrollador.

---

## 8. Matriz de Riesgo

| Problema | Impacto | Probabilidad de Fallo | Esfuerzo de Fix |
|----------|---------|----------------------|-----------------|
| ViewModel God Object | Alto | Media | Alto |
| CI build debug en release | Alto | **100%** (siempre pasa) | Bajo |
| ProGuard rules ausente | Alto | Media | Bajo |
| Sin tests | Alto | N/A (deuda acumulada) | Alto |
| SharedPreferences crudo | Medio | Baja | Medio |
| Categorización frágil | Medio | Alta | Medio |
| Receiver exportado | Medio | Baja | Bajo |
| Sin DI | Medio | N/A | Medio |
| Manifest portrait-only | Medio | Siempre afecta tablets | Bajo |
| `screenWidthDp = 360f` | Bajo | Alta en tablets | Bajo |

---

## 9. Recomendaciones Priorizadas

### Fase 1 — Fixes Inmediatos (1-2 días)
1. **Arreglar CI/CD** para build release en lugar de debug.
2. **Crear `proguard-rules.pro`** con reglas básicas de keep para Compose.
3. **Eliminar dependencia de Navigation Compose** si no se usa.
4. **Normalizar nombres** del proyecto en todos los archivos.
5. **Arreglar `build-release.sh`** o eliminarlo si se prefiere CI puro.

### Fase 2 — Refactorización Estructural (1-2 semanas)
1. **Extraer UseCases** del ViewModel (LoadApps, LaunchApp, ToggleFavorite, etc.).
2. **Crear interfaces** para Repositorios y Managers.
3. **Migrar SettingsManager a DataStore**.
4. **Introducir Hilt** para inyección de dependencias.
5. **Simplificar el combine** del ViewModel: usar un solo `data class` de estado interno en lugar de 4 niveles de combine.

### Fase 3 — Calidad y Testing (2+ semanas)
1. **Agregar tests unitarios** para `AppCategorizer`, `IconPackManager`, `SettingsManager`.
2. **Agregar tests de UI** básicos con Compose Test.
3. **Refactorizar `AppRepository`** para eliminar duplicación.
4. **Mejorar categorización** con un sistema basado en scoring o allowlist.

---

## 10. Veredicto Final

### ¿Está trabajando sobre escombros?

**No es un desastre total, pero sí una base frágil.**

La aplicación **funciona** y tiene funcionalidades sorprendentemente completas para un proyecto de un solo módulo: icon packs, work profiles, notificaciones, categorización, temas, perfiles. El código Compose es visualmente cuidado y la UX tiene detalles pulidos (animaciones, acentos por perfil, badges de notificación).

Sin embargo, la **arquitectura no escala**. El ViewModel de 692 líneas es un antipatrón grave. La falta de tests significa que cada refactor es un riesgo. El CI roto significa que las releases son subóptimas.

**Recomendación:** Invertir 1-2 semanas en refactorización estructural **antes** de agregar cualquier feature nueva. Cada línea de código que se agregue al ViewModel actual empeora exponencialmente la deuda técnica.

---

*Informe generado por análisis automático del codebase. Revisar con el equipo antes de priorizar acciones.*
