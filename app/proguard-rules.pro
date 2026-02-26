# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Satochip card communication library — do not obfuscate
-keep class org.satochip.** { *; }
-dontwarn org.satochip.**

# Keep NFC service
-keep class uk.co.hushchip.app.services.** { *; }

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp platform checks
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# SLF4J
-dontwarn org.slf4j.**
