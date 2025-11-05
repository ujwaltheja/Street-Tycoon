# Add project specific ProGuard rules here.
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep GameSimulation JNI class
-keep class com.streettycoon.game.native.GameSimulation {
    native <methods>;
}

# Keep data classes for JSON serialization
-keep class com.streettycoon.game.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
