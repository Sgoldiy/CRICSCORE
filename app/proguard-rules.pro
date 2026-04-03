# Standard ProGuard/R8 Rules
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# Gson
-keep class com.google.gson.** { *; }
-keep class com.cricpeak.scorelive.ui.apicallfiles.** { *; } 
-dontwarn com.google.gson.**

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# Google Play Billing (IAP)
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Android UI
-keep class com.cricpeak.scorelive.** { *; }
-dontwarn com.cricpeak.scorelive.**

# iZooto SDK
-keep class com.izooto.** { *; }
-dontwarn com.izooto.**

# iZooto optional dependencies (Glide, Google Identity, Huawei AGConnect)
-dontwarn com.bumptech.glide.**
-dontwarn com.google.android.libraries.identity.googleid.**
-dontwarn com.huawei.agconnect.**
