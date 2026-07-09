# R8/ProGuard Rules

# Retrofit & Gson (Must keep names to map JSON correctly)
-keep class com.example.tubetogether.api.VideoResponse { *; }
-keep class com.example.tubetogether.api.StreamSourceResponse { *; }
-keep class com.example.tubetogether.api.StaffResponse { *; }
-keep class com.example.tubetogether.api.VideoCategoryResponse { *; }
-keep class com.example.tubetogether.api.TubeApi { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Strip all logging (Log.d, Log.e, Log.i, Log.v, Log.w)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Aggressive Obfuscation
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5

# Security Crypto
-keep class androidx.security.crypto.** { *; }
