# Signstr ProGuard rules

# Keep NIP-46 service and crypto
-keep class uk.co.signstr.app.services.** { *; }
-keep class uk.co.signstr.app.crypto.** { *; }
-keep class uk.co.signstr.app.nip46.** { *; }
-keep class uk.co.signstr.app.data.** { *; }

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn okhttp3.internal.platform.**

# SLF4J
-dontwarn org.slf4j.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class uk.co.signstr.app.data.**$$serializer { *; }
-keepclassmembers class uk.co.signstr.app.data.** { *** Companion; }
-keepclasseswithmembers class uk.co.signstr.app.data.** { kotlinx.serialization.KSerializer serializer(...); }

# ML Kit barcode scanning
-dontwarn com.google.mlkit.**
