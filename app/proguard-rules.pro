-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.xgmusic.app.plugin.** { *; }
-keep class com.xgmusic.app.data.model.** { *; }

-dontwarn org.jsoup.**
-keep class kotlinx.serialization.** { *; }
