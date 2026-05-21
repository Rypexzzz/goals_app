# Hilt
-keep class dagger.hilt.android.internal.managers.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }

# Kotlinx serialization (typesafe Navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.aim.app.**$$serializer { *; }
-keepclassmembers class com.aim.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.aim.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
