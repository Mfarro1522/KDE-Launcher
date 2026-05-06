# Permisos de la Aplicación y Políticas de Privacidad

Este documento detalla los permisos requeridos por **TAPO-Launcher** y su justificación funcional, particularmente para cumplir con las políticas de Google Play Store.

## Permisos Core

### `QUERY_ALL_PACKAGES`

- **Uso:** Permite a la aplicación obtener una lista completa de todas las aplicaciones instaladas en el dispositivo.
- **Justificación de Play Policy:** **TAPO-Launcher es un Launcher (Aplicación de inicio).** La funcionalidad principal e irremplazable de un launcher es mostrar, organizar y lanzar cualquier aplicación instalada en el dispositivo del usuario. Sin este permiso, el launcher no podría descubrir las aplicaciones de usuario y sistema para mostrarlas en el "App Drawer" (Cajón de aplicaciones) o en la pantalla de inicio, lo que rompería completamente la funcionalidad principal de la app.
- **Excepción permitida:** La política de "Permisos amplios de visibilidad de paquetes (QUERY_ALL_PACKAGES)" de Google Play explícitamente permite el uso de este permiso para aplicaciones cuyo propósito principal sea actuar como un **Launcher** (Device Automation / Launcher).

### `REQUEST_DELETE_PACKAGES`

- **Uso:** Permite al usuario iniciar el proceso de desinstalación de una aplicación desde el launcher.
- **Justificación:** Es una característica estándar y esperada de cualquier launcher permitir a los usuarios gestionar sus aplicaciones, incluyendo la desinstalación directa desde el menú contextual del ícono de la app.

### `VIBRATE`

- **Uso:** Proporciona retroalimentación háptica.
- **Justificación:** Mejora la experiencia del usuario (UX) al proporcionar respuesta física al realizar acciones prolongadas, como un "long press" (mantener presionado) sobre el ícono de una aplicación para abrir su menú contextual.

## Permisos de Componentes y Sistema

### `NotificationListenerService` (Deshabilitado / Opcional)

Si se habilita el acceso a notificaciones, el launcher puede mostrar "Notification Dots" (puntos de notificación) en los íconos de las aplicaciones que tienen notificaciones activas.
- **Privacidad:** El launcher no lee el contenido de los mensajes, únicamente cuenta la cantidad de notificaciones activas por paquete (`packageName`) para actualizar el estado visual en el grid de aplicaciones.

## Resumen de Privacidad

**TAPO-Launcher no recopila, almacena ni transmite información personal o datos de uso a servidores externos.** Toda la información sobre las aplicaciones instaladas, el perfil de trabajo y el estado de configuración se procesa **exclusivamente de forma local** en el dispositivo del usuario.
