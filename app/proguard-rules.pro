-optimizationpasses 30
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-mergeinterfacesaggressively
-optimizations !code/simplification/arithmetic
-dontusemixedcaseclassnames
-allowaccessmodification
-useuniqueclassmembernames
-keeppackagenames doNotKeepAThing

-repackageclasses 'com'

-keep public class com.android.installreferrer.** { *; }
-keep class com.appsflyer.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.okhttp.internal.**

-keepattributes *Annotation*
-keepattributes Signature

-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.google.** { *; }
-keep class org.apache.** { *; }
-keep class com.android.** { *; }
-keep class junit.** { *; }
-keep class * implements android.os.Parcelable {*;}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep public class com.android.installreferrer.** { *; }