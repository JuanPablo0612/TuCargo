# Koin
-keep class org.koin.** { *; }

# kotlinx-serialization
-keepattributes Annotation, InnerClasses, Signature, Exceptions, SourceFile, LineNumberTable
-keep class kotlinx.serialization.json.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# GitLive Firebase
-keep class dev.gitlive.firebase.** { *; }

# App @Serializable classes (Firestore DTOs, navigation routes, BuildKonfig):
# keep the generated serializers so release builds can encode/decode them.
-keep,includedescriptorclasses class com.juanpablo0612.tucargo.**$$serializer { *; }
-keepclassmembers class com.juanpablo0612.tucargo.** {
    *** Companion;
}
-keepclasseswithmembers class com.juanpablo0612.tucargo.** {
    kotlinx.serialization.KSerializer serializer(...);
}
