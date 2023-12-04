# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

##########################################
############### Retrofit #################
##########################################
-keep public class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn rx.**
-dontwarn retrofit.**
-keep public class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

##########################################
######## Android Support Library #########
##########################################
-keep public class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep public class androidx.** { *; }
-keep interface androidx.** { *; }

##########################################
################# Native #################
##########################################
-keepclasseswithmembers  class * {
    native <methods>;
}

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.safenet.service.data.network.dto.** { *; }