# ProGuard rules for TAPO-Launcher
# IMPORTANT: Compose libraries ship their own consumer ProGuard rules.
# Do NOT add broad -keep rules for androidx.compose here — it disables
# R8 optimisations and causes lock-verification warnings at runtime.

# ── Compose SnapshotStateList lock-verification fix ─────────────────────────
# R8 inlines or removes methods that ART needs for lock verification.
# These rules keep the exact signatures Compose runtime expects.
-keepclassmembers class androidx.compose.runtime.snapshots.SnapshotStateList {
    *** conditionalUpdate(...);
    *** mutate(...);
    *** update(...);
}

# ── Application entry points ────────────────────────────────────────────────
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# ── ViewModel & Lifecycle ───────────────────────────────────────────────────
-keep public class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

# ── Kotlin metadata & coroutines ────────────────────────────────────────────
-keepattributes *Annotation*, Signature, Exceptions, InnerClasses, EnclosingMethod
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# ── Enums & Parcelable ──────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Keep app-specific model classes (adjust package if needed) ──────────────
# Add explicit rules here only for classes that R8 removes incorrectly.
