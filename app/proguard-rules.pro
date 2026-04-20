# 1. Essential Attributes for Retrofit & Serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, *Annotation*

# 2. Kotlin Coroutines & Metadata
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.Metadata { *; }

# 3. Retrofit 2
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# 4. Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
    @kotlinx.serialization.SerialName *;
}

# 5. Protect Our App Structure (Data & Domain)
# This prevents R8 from renaming or stripping our network and database models
-keep class veiga.sl.departures.data.api.** { *; }
-keep class veiga.sl.departures.data.db.** { *; }
-keep class veiga.sl.departures.domain.model.** { *; }
-keep class veiga.sl.departures.data.local.** { *; }

# 6. OkHttp 3
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
