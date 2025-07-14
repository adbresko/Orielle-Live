# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep your domain models
-keep class com.orielle.domain.model.** { *; }

# Keep data classes (Room entities, etc.)
-keepclassmembers class * {
    @androidx.room.* *;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep generic signatures
-keepattributes Signature

# Keep exceptions
-keep public class * extends java.lang.Exception

# Keep annotations
-keepattributes *Annotation*

# Keep inner classes
-keepattributes InnerClasses

# Keep synthetic methods
-keepattributes SyntheticAccess

# Keep bridge methods
-keepattributes Bridge

# Keep deprecated methods
-keepattributes Deprecated

# Keep method parameters
-keepattributes MethodParameters

# Keep constant fields
-keepclassmembers class * {
    public static final *;
}

# Keep public methods in public classes
-keep public class * {
    public <methods>;
}

# Keep public fields in public classes
-keep public class * {
    public <fields>;
}

# Keep public constructors in public classes
-keep public class * {
    public <init>(...);
}