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

# 保留行号信息用于调试崩溃堆栈
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保护农历库相关类，防止被混淆或删除
-keep class com.nlf.calendar.** { *; }
-keepclassmembers class com.nlf.calendar.** { *; }

# 保护反射调用的方法
-keepclassmembers class * {
    public <methods>;
}

# 保护农历库的所有公共API
-keep public class com.nlf.calendar.Solar {
    public static com.nlf.calendar.Solar fromYmd(int, int, int);
    public com.nlf.calendar.Lunar getLunar();
    public java.lang.String getJieQi();
    public java.util.List getJieQiList();
    public java.util.List getFestivals();
    public java.lang.String getXingZuo();
}

-keep public class com.nlf.calendar.Lunar {
    public java.lang.String getYearInGanZhi();
    public java.lang.String getYearShengXiao();
    public java.lang.String getMonthInChinese();
    public java.lang.String getDayInChinese();
    public java.lang.String getJieQi();
    public java.util.List getFestivals();
}

# 保护所有使用反射的类和方法
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# 保护应用的主要类不被混淆
-keep class com.example.calendar.MainActivity { *; }
-keep class com.example.calendar.util.** { *; }
-keep class com.example.calendar.data.** { *; }
-keep class com.example.calendar.viewmodel.** { *; }

# 保护Compose相关类
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# 保护Room数据库相关
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# 不要混淆枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保护Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保护Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}