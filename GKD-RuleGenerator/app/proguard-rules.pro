# Copyright 2026, GKD-RuleGenerator contributors
# SPDX-License-Identifier: Apache-2.0

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.gkd.rulegenerator.**$$serializer { *; }
-keepclassmembers class com.gkd.rulegenerator.** {
    *** Companion;
}
-keepclasseswithmembers class com.gkd.rulegenerator.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Ktor - suppress SLF4J missing class warnings
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# Ktor client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# kotlinx.coroutines
-dontwarn kotlinx.coroutines.**
