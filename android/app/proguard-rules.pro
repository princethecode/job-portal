# Add project specific ProGuard rules here.

# General Android compatibility
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontoptimize
-dontpreverify
-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# Keep Android Patterns for validation (used for email and phone)
-keep class android.util.Patterns { *; }

# For native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views for XML inflation
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep Activity subclasses and their methods
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

# Retrofit - Fixed for ProGuard obfuscation issues
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep Retrofit interfaces and their methods
-keep interface retrofit2.Call
-keep interface retrofit2.http.* { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep API service interfaces completely - CRITICAL for preventing crashes
-keep interface com.emps.abroadjobs.network.ApiService { *; }
-keep interface com.emps.abroadjobs.network.RecruiterApiService { *; }

# Keep all Retrofit method signatures and generic types
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Preserve generic signatures for Retrofit Call types
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep specific model classes that are used as raw Call types
-keep class com.emps.abroadjobs.models.User { *; }
-keep class com.emps.abroadjobs.models.LoginResponse { *; }

# Additional Retrofit rules
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Keep generic type information for API responses
-keepattributes *Annotation*,Signature,Exception

# OkHttp - Enhanced rules for network security
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep OkHttp interceptors
-keep class com.emps.abroadjobs.network.ResponseInterceptor { *; }

# Gson - Enhanced rules for model preservation and TypeToken safety
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep all model classes and their fields
-keep class com.emps.abroadjobs.models.** { *; }
-keep class com.emps.abroadjobs.data.model.** { *; }
-keep class com.emps.abroadjobs.data.api.** { *; }
-keep class com.emps.abroadjobs.network.ApiResponse { *; }
-keep class com.emps.abroadjobs.network.SafeJsonParser { *; }

# Keep Gson TypeAdapters
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep generic type information for Gson
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent TypeToken issues by keeping reflection-based classes
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
  <init>();
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Services and Google Sign-In
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# Google Auth
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-keep class com.google.auth.** { *; }
-dontwarn com.google.auth.**

# Keep Google Services generated resources
-keep class **.R$string { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Additional Google Sign-In specific rules
-keep class com.google.android.gms.auth.api.signin.internal.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }
-keep class com.google.android.gms.common.api.internal.** { *; }

# PDF Viewer
-keep class com.shockwave.**

# AndroidX and Support Library
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Material Design Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Multidex
-keep class androidx.multidex.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
} 