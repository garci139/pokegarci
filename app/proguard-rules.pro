# Stack traces legibles en crashes de release
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(...);
}

# Glide (reglas adicionales; el AAR ya incluye las básicas)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }

# Hilt / Dagger (refuerzo; el AAR de Hilt aporta reglas propias)
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# Application y componentes Android declarados en el manifest
-keep class com.garci.pokegarci.PokeGarciApplication { *; }
