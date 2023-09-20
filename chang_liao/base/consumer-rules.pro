# 保留 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 不被混淆
-keepclassmembers class * implements java.io.Serializable {
      <fields>;
      <methods>;
}