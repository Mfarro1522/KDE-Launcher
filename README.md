# TAPO-Launcher

![Demo](pics/AppEnUso.gif)

Launcher para Android hecho con **Kotlin + Jetpack Compose**. Busca ser ligero, minimalista y práctico para uso diario, con soporte para categorías, perfiles de trabajo, icon packs y ajustes visuales.

<p align="center">
  <img src="pics/ScreenApp1_whiteTheme.png" width="200" alt="Pantalla principal - Tema claro" />
  <img src="pics/ScreenApp2.png" width="200" alt="Categorías y sidebar" />
  <img src="pics/ScreenApp3.png" width="200" alt="Panel de ajustes" />
</p>

## Características

- **Categorías configurables** — Favoritos, Desarrollo, Gráficos, Internet, Juegos, Multimedia, Sistema y Utilidades.
- **Categorías inteligentes** — Smart grouping automático para Wallets (Binance, Yape, PayPal), Compras (Rappi, Amazon), Finanzas (merge inteligente), y Dev (Termux, GitHub).
- **Perfiles Personal / Trabajo** — Detecta un Work Profile real y permite lanzar apps del perfil laboral.
- **Apps ocultas** — Oculta apps permanentemente o temporalmente (5 min, hasta reinicio, indefinido) con persistencia en DataStore. Gestión completa desde el panel de ajustes.
- **Menú contextual** — Long-press en cualquier app abre un menú flotante posicionado cerca del ícono con acciones: Favorito, Mover, Información, Desinstalar.
- **Búsqueda instantánea** — Filtra apps por nombre o paquete.
- **Ajustes de interfaz** — Tema claro/oscuro, tamaño de íconos, columnas, fondo de íconos y etiquetas.
- **Icon packs** — Detecta packs instalados y resuelve íconos desde su `appfilter.xml`.
- **Personalización de categorías** — Cambia nombre, ícono y visibilidad de cada categoría.
- **Carga optimizada** — Single emission en startup con caché de proceso (`AppListCache`) + caché persistente en disco (`PersistentAppCache`) para recuperación instantánea tras process death.
- **Warm start instantáneo** — Caché de lista de apps a nivel de proceso (`AppListCache`) y caché en disco (`PersistentAppCache`) para recuperación fluida tras estar en background o ser matado por el sistema.
- **Notificaciones** — Incluye un `NotificationListenerService` para mostrar estado relacionado con notificaciones.

## Arquitectura

```text
app/
├── app/src/main/java/dev/vive/kdelauncher/
│   ├── KDELauncherApp.kt         ← `Application` y punto de entrada.
│   ├── MainActivity.kt           ← Punto de entrada de la UI.
│   ├── SetDefaultLauncherActivity.kt ← Pantalla puente para fijar el launcher predeterminado.
│   ├── data/
│   │   ├── model/                ← Modelos de apps (`@Immutable`), perfiles y categorías.
│   │   ├── platform/             ← Gateways para interactuar con Android OS (PackageManager).
│   │   ├── provider/             ← Clientes HTTP/API para proveedores de IA (Labs).
│   │   ├── repository/           ← Implementaciones de repositorios y caches (DataStore, PersistentAppCache).
│   │   ├── IconPackManagerImpl.kt← Resolución de icon packs.
│   │   ├── ProfileManagerImpl.kt ← Favoritos y apps de trabajo.
│   │   ├── SettingsManagerImpl.kt← Configuración persistida.
│   │   └── WorkProfileManagerImpl.kt ← Apps del perfil laboral.
│   ├── di/
│   │   └── AppContainer.kt       ← Contenedor e inyección manual de dependencias.
│   │                               Incluye `AppListCache` (memoria) y `PersistentAppCache` (disco)
│   │                               para warm start instantáneo y recuperación tras process death.
│   ├── domain/
│   │   ├── repository/           ← Interfaces (contratos) de repositorios y Managers.
│   │   └── usecase/              ← Lógica principal y reglas de negocio (SRP).
│   ├── service/
│   │   └── PackageChangeReceiver.kt ← Receptor de broadcast de cambios en paquetes instalados.
│   └── ui/
│       ├── LauncherViewModel.kt  ← ViewModel con múltiples StateFlows independientes
│       │                             (uiState, appGridState, tourState) para aislar
│       │                             recomposiciones.
│       ├── LauncherUiStateMapper.kt ← Mapeador de proyección y filtrado de estado.
│       │                              El filtrado corre en `Dispatchers.Default`.
│       ├── screens/              ← Pantalla principal del launcher (`LauncherScreen`).
│       ├── components/           ← Grid (`LazyVerticalGrid` con keys + contentType),
│       │                             sidebar, buscador, header y panel de ajustes.
│       ├── theme/                ← Colores, tipografía y temas dinámicos (Dev Themes).
│       └── tour/                 ← Product Tour con modifier condicional
│                                     (solo activo cuando `tourState.isActive`).
```

## Tech stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | **Kotlin** 100% |
| UI | **Jetpack Compose** + Material3 |
| Arquitectura | **MVVM** + **use cases** + contenedor manual de dependencias |
| Gradle | **Kotlin DSL** + Version Catalog (`libs.versions.toml`) |
| Mínimo SDK | **API 26** (Android 8.0) |
| Target SDK | **API 35** (Android 15) |
| Compilación | Java 17, Compose BOM |

## Build

```bash
cd app

# Debug APK
./gradlew assembleDebug

# Producción firmada (minificado + shrinkResources)
./gradlew assembleRelease

# Usando el script helper
./build-release.sh
```

El APK generado estará en `app/app/build/outputs/apk/debug/` o `app/app/build/outputs/apk/release/`. El script `build-release.sh` copia el release final a `releases/`.

### Firma de release

Los builds `release` necesitan una keystore real para generar un APK instalable fuera de Android Studio.

Variables de entorno requeridas para `assembleRelease`:

```bash
export TAPO_RELEASE_KEYSTORE_PATH="/ruta/a/tu/release-keystore.jks"
export TAPO_RELEASE_STORE_PASSWORD="tu_store_password"
export TAPO_RELEASE_KEY_ALIAS="tu_alias"
export TAPO_RELEASE_KEY_PASSWORD="tu_key_password"
```

Secrets requeridos en GitHub Actions para publicar releases instalables:

- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_STORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`

Para `ANDROID_RELEASE_KEYSTORE_BASE64`, sube el contenido base64 del archivo `.jks`:

```bash
base64 -w 0 release-keystore.jks
```

## Licencia

[Apache License 2.0](LICENSE)

## Privacidad y Permisos

Consulta el documento de [Permisos y Privacidad](PERMISSIONS.md) para conocer la justificación del uso del permiso `QUERY_ALL_PACKAGES` y otros permisos requeridos.
