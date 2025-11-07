# ================================
# STREET TYCOON PROGUARD RULES
# ================================

# ==================== GENERAL ====================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep all R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ==================== NATIVE (JNI) ====================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep GameSimulation JNI class - CRITICAL for C++ integration
-keep class com.streettycoon.game.native.GameSimulation {
    native <methods>;
    <init>(...);
    public <methods>;
}

# ==================== DATA MODELS ====================
# Keep data classes for JSON serialization
-keep class com.streettycoon.game.model.** { *; }
-keep class com.streettycoon.data.** { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ==================== ROOM DATABASE ====================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep Room generated code
-keep class com.streettycoon.data.GameDatabase_Impl { *; }

# ==================== GSON ====================
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson uses generic type information stored in a class file when working with fields.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# ==================== KOTLINX SERIALIZATION ====================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.streettycoon.**$$serializer { *; }
-keepclassmembers class com.streettycoon.** {
    *** Companion;
}
-keepclasseswithmembers class com.streettycoon.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== KOTLIN COROUTINES ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ==================== JETPACK COMPOSE ====================
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

-keepclassmembers class androidx.compose.** {
    <init>(...);
}

# ==================== GOOGLE PLAY SERVICES ====================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Play Games Services
-keep class com.google.android.gms.games.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Play Billing
-keep class com.android.billingclient.** { *; }

# ==================== FIREBASE ====================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Firebase Analytics
-keep class com.google.android.gms.measurement.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase Performance
-keep class com.google.firebase.perf.** { *; }

# ==================== MEDIA3 (ExoPlayer) ====================
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ExoPlayer specific
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# ==================== GAME SYSTEMS ====================
# Keep particle system
-keep class com.streettycoon.game.ParticleSystem { *; }
-keep class com.streettycoon.game.ParticleSystem$** { *; }

# Keep performance profiler
-keep class com.streettycoon.game.PerformanceProfiler { *; }
-keep class com.streettycoon.game.PerformanceProfiler$** { *; }

# Keep haptic feedback manager
-keep class com.streettycoon.game.HapticFeedbackManager { *; }
-keep class com.streettycoon.game.HapticFeedbackManager$** { *; }

# Keep object pooling
-keep class com.streettycoon.game.ObjectPool { *; }
-keep class com.streettycoon.game.ObjectPool$** { *; }
-keep class com.streettycoon.game.GameObjectPools { *; }

# Keep services
-keep class com.streettycoon.services.PlayGamesManager { *; }
-keep class com.streettycoon.services.PlayGamesManager$** { *; }
-keep class com.streettycoon.services.AnalyticsManager { *; }
-keep class com.streettycoon.services.AnalyticsManager$** { *; }

# Keep audio managers
-keep class com.streettycoon.audio.** { *; }

# ==================== VIEW MODELS ====================
-keep class com.streettycoon.viewmodel.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# ==================== REFLECTION ====================
# Keep classes used via reflection
-keep class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== OPTIMIZATION ====================
# Optimization settings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Remove logging (optional - comment out for debugging)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ==================== WARNINGS ====================
# Suppress warnings
-dontwarn kotlin.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
