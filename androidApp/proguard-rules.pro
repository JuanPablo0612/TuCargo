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
