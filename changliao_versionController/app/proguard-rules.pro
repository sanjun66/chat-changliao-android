-optimizationpasses 8
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontnote com.android.vending.licensing.ILicensingService
-dontwarn android.support.v4.**
-dontpreverify
-verbose
-ignorewarnings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-assumenosideeffects class android.util.Log{public static *** d(...);public static *** i(...);}

-keepattributes *Annotation*
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public interface com.android.vending.licensing.ILicensingService
##-keep public interface com.google.vending.licensing.ILicensingService
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-dontwarn org.apache.**
-keep class org.apache.**{*;}
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep class com.basestonedata.instalment.net.model.** {*;}
-keep class com.basestonedata.instalment.net.data.model.** {*;}
-keep class com.basestonedata.instalment.anlysis.** {*;}
-keep class com.basestonedata.anlysis.** {*;}
-keep class android.support.design.widget.TabLayout** {*;}


-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable


-keep class com.tencent.mm.sdk.** {
   *;
}

#butterknife 混淆
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keep class com.squareup.wire.**{*;}

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class com.basestonedata.instalment.BuildConfig {
    public static <fields>;
}

-keepclassmembers class com.basestonedata.instalment.BuildConfig {
    *;
}

-keep class **.R$* {
 *;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##保护所有实体中的字段名称##
-keepclassmembers class * implements java.io.Serializable {
    <fields>;
    <methods>;
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}
#Gson的处理
#-keepattributes Signature
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.examples.android.model.** { *; }

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ----------

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#sharesdk
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class com.alipay.share.sdk.**{*;}

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#glide 美洽需要
-keep class com.bumptech.glide.Glide { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-dontwarn com.bumptech.glide.**



#volley
-dontwarn com.android.volley.jar.**
-keep class com.android.volley.**{*;}

-keep class org.apache.**{ *; }
-keep class com.baidu.**{ *; }
-keep class com.umeng.**{*;}

-keep class com.mob.tools.**{*;}

#芝麻信用
-keep class com.alipayzhima.**{*;}
-keep class com.android.moblie.zmxy.antgroup.creditsdk.**{*;}
-keep class com.antgroup.zmxy.mobile.android.container.**{*;}
-keep class org.json.alipayzhima.**{*;}

-keepattributes Signature,*Annotation*
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep class com.tencent.android.tpush.**  {* ;}
-keep class com.tencent.mid.**  {* ;}

#支付宝支付

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}


#HotFix

#基线包使用，生成mapping.txt
-printmapping mapping.txt
#生成的mapping.txt在app/buidl/outputs/mapping/release路径下，移动到/app路径下
#修复后的项目使用，保证混淆结果一致
#-applymapping mapping.txt
#hotfix
-keep class com.taobao.sophix.**{*;}
-keep class com.ta.utdid2.device.**{*;}
#防止inline
-dontoptimize

#视屏上传
-keep class FileCloud.**{*;}
-keep class com.tencent.upload.**{*;}
-keep class com.qq.**{*;}
-keep class com.qq.**{*;}
-keep class org.jsoup.**{*;}

#腾讯云图片上传混淆
#--------------------------------
-printmapping  upload.map

-keepattributes Signature,InnerClasses


-keep class com.tencent.upload.network.base.ConnectionImpl
-keep class com.tencent.upload.network.base.ConnectionImpl {
    *;
}

-keep class com.tencent.upload.UploadManager { *; }
-keep class com.tencent.upload.UploadManager$* { *; }

-keep class com.tencent.upload.Const {
    *;
}
-keep class com.tencent.upload.Const$* { *; }

-keep class com.tencent.upload.task.** { *;}
-keep class com.tencent.upload.impl.** { * ; }
-keep class com.tencent.upload.utils.** { * ; }

-keepclasseswithmembers class com.tencent.upload.task.** { *; }
-keepclasseswithmembernames class com.tencent.upload.task.** { *; }

-keep class com.tencent.upload.task.ITask$* { *; }
-keep class com.tencent.upload.task.impl.FileDeleteTask$* { *; }
-keep class com.tencent.upload.task.impl.FileStatTask$* { *; }
-keep class com.tencent.upload.task.impl.FileCopyTask$* { *; }

-keep class com.tencent.upload.common.Global { *; }
-keep class com.tencent.upload.log.trace.TracerConfig { *; }

-keep class * extends com.qq.taf.jce.JceStruct { *; }

-keep public class * extends android.app.Service -keep public class * extends android.content.BroadcastReceiver -keep class com.tencent.android.tpush.** {* ;}
-keep class com.tencent.mid.** {* ;}

# JS相关的混淆
-keep  class com.basestonedata.instalment.ui.auth.CrawlerJdActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.ui.auth.CrawlerTaobaoActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.ui.auth.CrawlerZhifuBaoActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.ui.auth.LearningAuthorizeActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.activity.LearningFindPassWorldActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.activity.LearningFindUserNameActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.activity.LearningRegeditActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.ui.auth.CreditPeopleBankActivity$OnJsHtml {*;}
-keep  class com.basestonedata.instalment.ui.base.BaseWebViewFragment$JSImgUrlInject {*;}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*

#灵动分析统计混淆
-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.tenddata.** { public protected *;}
-keepclassmembers class com.tendcloud.tenddata.**{
public void *(***);
}
-keep class com.talkingdata.sdk.TalkingDataSDK {public *;}
-keep class com.apptalkingdata.** {*;}
-keep public class com.basestonedata.instalment.sonic.TalkingDataHTML { *;}
-keep public class com.basestonedata.analysis.third.TalkingDataHTML { *;}
-keepclassmembers class ** {
    @com.tendcloud.tenddata.OttoProduce public *;
    @com.tendcloud.tenddata.OttoSubscribe public *;
}

# 阿里百川混淆
# -----------

-keepattributes EnclosingMethod
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class org.json.** {*;}
-keep class com.ali.auth.**  {*;}

-keepattributes Signature
-ignorewarnings
-keep class javax.ws.rs.** { *; }
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.**
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class org.json.** {*;}
-keep class com.ali.auth.**  {*;}
-dontwarn com.ali.auth.**
-keep class com.taobao.securityjni.** {*;}
-keep class com.taobao.wireless.security.** {*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}
-keep interface mtopsdk.mtop.global.init.IMtopInitTask {*;}
-keep class * implements mtopsdk.mtop.global.init.IMtopInitTask {*;}
-keep class com.basestonedata.instalment.tunion.** {*;}



#讯飞语音混淆
-keep class com.iflytek.**{*;}


# 定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

# 搜索
-keep   class com.amap.api.services.**{*;}

# 2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

#京东开普勒 kepler
-keep class com.kepler.**{*;}
-dontwarn com.kepler.**
-keep class com.jingdong.jdma.**{*;}
-dontwarn com.jingdong.jdma.**
-keep class com.jingdong.crash.**{*;}
-dontwarn com.jingdong.crash.**


#活体检测
#-dontwarn com.sensetime.**
#-keep class com.sensetime.** { *; }
#-libraryjars ../common/libs/motion-liveness.jar
-dontwarn com.linkface.**
-keep class com.linkface.** { *; }



 #信鸽
# -keep public class * extends android.app.Service
# -keep public class * extends android.content.BroadcastReceiver
# -keep class com.tencent.android.tpush.**  {* ;}
# -keep class com.tencent.mid.**  {* ;}
 -keep public class * extends android.app.Service
 -keep public class * extends android.content.BroadcastReceiver
 -keep class com.tencent.android.tpush.** {* ;}
 -keep class com.tencent.mid.** {* ;}
 -keep class com.qq.taf.jce.** {*;}
 #华为
 #-ignorewarning
 -keepattributes *Annotation*
 -keepattributes Exceptions
 -keepattributes InnerClasses
 -keepattributes Signature
 -keepattributes SourceFile,LineNumberTable
 -keep class com.hianalytics.android.**{*;}
 -keep class com.huawei.updatesdk.**{*;}
 -keep class com.huawei.hms.**{*;}
 -keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; }
-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {<init>(...); }
-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }
 #mi
 -dontwarn com.xiaomi.push.**
 -keepclasseswithmembernames class com.xiaomi.**{*;}
 -keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver
 #meizu
-dontwarn com.meizu.cloud.pushsdk.**
-keep class com.meizu.cloud.pushsdk.**{*;}
 #shareSdk
 -keep class cn.sharesdk.**{*;}
 -keep class com.sina.**{*;}
 -keep class **.R$* {*;}
 -keep class **.R{*;}
 -keep class com.mob.**{*;}
 -keep class m.framework.**{*;}
 -keep class com.bytedance.**{*;}
 -dontwarn com.sina.**
 -dontwarn com.mob.**
 -dontwarn cn.sharesdk.**
 -dontwarn **.R$*
 -keep class com.basestonedata.tools.**{*;}


 #talkingData
 -dontwarn com.tendcloud.tenddata.**
 -keep class com.tendcloud.** {*;}
 -keep public class com.tendcloud.tenddata.** { public protected *;}
 -keepclassmembers class com.tendcloud.tenddata.**{
 public void *(***);
 }
 -keep class com.talkingdata.sdk.TalkingDataSDK {public *;}
 -keep class com.apptalkingdata.** {*;}
 -keep class dice.** {*; }
 -dontwarn dice.**

 #arouter
 -keep public class com.alibaba.android.arouter.routes.**{*;}
 -keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

#ijkPlayer
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

#############################################################################################
#京东开普勒

-keep class com.basestonedata.radical.data.** {*;}
-keep class com.basestonedata.radical.analytics.** {*;}
-keep class com.basestonedata.framework.aspect.** {*;}


# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-keep class org.aspectj.** { *; }
-keep class com.iflytek.**{*;}
#eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}

#Bugly 混淆
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
# tinker混淆规则
-dontwarn com.tencent.tinker.**
-keep class com.tencent.tinker.** { *; }
-keep class android.support.**{*;}

-keep class com.basestonedata.framework.jsbridge.Message
-keep class com.basestonedata.instalment.sonic.callbackbean.**{*;}
-keep class com.basestonedata.instalment.ui.cookie.**{*;}
#狼人杀混淆
#-------------------------------------------定制化区域----------------------------------------------
#---------------------------------1.实体类---------------------------------

#-keep class com.langrenapp.langren.bean
#-keep class com.langrenapp.langren.base
#
##-------------------------------------------------------------------------
#
##---------------------------------2.第三方包-------------------------------
#
##环信'com.hyphenate:hyphenate-sdk:3.3.0'
##-keep class com.easemob.** {*;}
##-keep class org.jivesoftware.** {*;}
##-keep class org.apache.** {*;}
#-keep class com.hyphenate.** {*;}
#-dontwarn  com.hyphenate.**
#-keep class com.superrtc.** {*;}
#
##agora
#-keep class io.agora.** {*;}
#
##rxjava 和 rxAndroid
#-dontwarn sun.misc.**
#-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
#   long producerIndex;
#   long consumerIndex;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode producerNode;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode consumerNode;
#}
##-dontnote rx.internal.util.PlatformDependent
#
##retrofit2
#-dontnote retrofit2.Platform
#-dontwarn retrofit2.Platform$Java8
#-keepattributes Signature
#-keepattributes Exceptions
#
#
##netty
#-keepattributes Signature,InnerClasses
#-keepclasseswithmembers class io.netty.** {
#    *;
#}
#-dontwarn io.netty.**
#-dontwarn sun.**
#
##glide
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
#  **[] $VALUES;
#  public *;
#}
#
## Gson
#-keep class com.google.**{*;}
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
#
###---------------Begin: proguard configuration for Gson  ----------
#-keepattributes Signature
#-keep class sun.misc.Unsafe { *; }
##-keep class com.google.gson.stream.** { *; }
## Application classes that will be serialized/deserialized over Gson
#-keep class com.langrenapp.langren.bean.** { *; }  ##这里需要改成解析到哪个  javabean
#
#
#
#
##greendao3.2.0,此是针对3.2.0，如果是之前的，可能需要更换下包名
#### greenDAO 3
#-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
#public static java.lang.String TABLENAME;
#}
#-keep class **$Properties
#-dontwarn org.greenrobot.greendao.database.**
#-dontwarn rx.**
#
#
#-dontwarn org.codehaus.**
#-dontwarn java.nio.**
#-dontwarn java.lang.invoke.**
#
#
##友盟
#-keepclassmembers class * {
#   public <init> (org.json.JSONObject);
#}
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
## filedownloader
#-dontwarn okhttp3.*
#-dontwarn okio.**
##bugly
#-dontwarn com.tencent.bugly.**
#-keep public class com.tencent.bugly.**{*;}
##友盟
#-dontusemixedcaseclassnames
#-dontshrink
#-dontoptimize
#-dontwarn com.google.android.maps.**
#-dontwarn android.webkit.WebView
#-dontwarn com.umeng.**
#-dontwarn com.tencent.weibo.sdk.**
#-dontwarn com.facebook.**
#-keep public class javax.**
#-keep public class android.webkit.**
#-dontwarn android.support.v4.**
#-keep enum com.facebook.**
#-keepattributes Exceptions,InnerClasses,Signature
#-keepattributes *Annotation*
#-keepattributes SourceFile,LineNumberTable
#
#-keep public interface com.facebook.**
#-keep public interface com.tencent.**
#-keep public interface com.umeng.socialize.**
#-keep public interface com.umeng.socialize.sensor.**
#-keep public interface com.umeng.scrshot.**
#-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
#-keep public class com.umeng.socialize.* {*;}
#
#
#-keep class com.facebook.**
#-keep class com.facebook.** { *; }
#-keep class com.umeng.scrshot.**
#-keep public class com.tencent.** {*;}
#-keep class com.umeng.socialize.sensor.**
#-keep class com.umeng.socialize.handler.**
#-keep class com.umeng.socialize.handler.*
#-keep class com.umeng.weixin.handler.**
#-keep class com.umeng.weixin.handler.*
#-keep class com.umeng.qq.handler.**
#-keep class com.umeng.qq.handler.*
#-keep class UMMoreHandler{*;}
#-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
#-keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
#-keep class im.yixin.sdk.api.YXMessage {*;}
#-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
#-keep class com.tencent.mm.sdk.** {
# *;
#}
#-keep class com.tencent.mm.opensdk.** {
#*;
#}
#-dontwarn twitter4j.**
#-keep class twitter4j.** { *; }
#
#-keep class com.tencent.** {*;}
#-dontwarn com.tencent.**
#-keep public class com.umeng.com.umeng.soexample.R$*{
#public static final int *;
#}
#-keep public class com.linkedin.android.mobilesdk.R$*{
#public static final int *;
#    }
#-keepclassmembers enum * {
#public static **[] values();
#public static ** valueOf(java.lang.String);
#}
#
#-keep class com.tencent.open.TDialog$*
#-keep class com.tencent.open.TDialog$* {*;}
#-keep class com.tencent.open.PKDialog
#-keep class com.tencent.open.PKDialog {*;}
#-keep class com.tencent.open.PKDialog$*
#-keep class com.tencent.open.PKDialog$* {*;}
#
#-keep class com.sina.** {*;}
#-dontwarn com.sina.**
#-keep class  com.alipay.share.sdk.** {
#   *;
#}
#-keepnames class * implements android.os.Parcelable {
#public static final ** CREATOR;
#}
#
#-keep class com.linkedin.** { *; }
#-keepattributes Signature
#
##-------------------------------------------------------------------------
#
##---------------------------------3.与js互相调用的类------------------------
#
#
#
##-------------------------------------------------------------------------
#
##---------------------------------4.反射相关的类和方法-----------------------
#
#
#
##----------------------------------------------------------------------------
##---------------------------------------------------------------------------------------------------
#
##-------------------------------------------基本不用动区域--------------------------------------------
##---------------------------------基本指令区----------------------------------
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers
#-dontpreverify
#-verbose
#-printmapping proguardMapping.txt
#-optimizations !code/simplification/cast,!field/*,!class/merging/*
#-keepattributes *Annotation*,InnerClasses
#-keepattributes Signature
#-keepattributes SourceFile,LineNumberTable
##----------------------------------------------------------------------------
#
##---------------------------------默认保留区---------------------------------
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService
#-keep class android.support.** {*;}
#
#-keepclasseswithmembernames class * {
#    native <methods>;
#}
#-keepclassmembers class * extends android.app.Activity{
#    public void *(android.view.View);
#}
#-keep public class * extends android.view.View{
#    *** get*();
#    void set*(***);
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}
#
#-keep class **.R$* {
# *;
#}
##处理断言错误
#-keepattributes EnclosingMethod
#
#-keep class com.basestonedata.framework.jsbridge.Message
#-keep class com.basestonedata.instalment.sonic.callbackbean.**{*;}
##----------------------------------------------------------------------------
#
##---------------------------------webview------------------------------------
#
##----------------------------------------------------------------------------
##---------------------------------------------------------------------------------------------------
#
#######################################################中原消费金融混淆#########################################################################################

-keep class com.hnzycfc.zycfcsdk.** { *; }

-keep class com.bqs.risk.df.android.contact.SdkContactTools {public *;}

-dontwarn okio.**

#Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontwarn retrofit2.**

-keep class retrofit2.** { *; }

#Retain declared checked exceptions for use by a Proxy instance.

-keepattributes Exceptions #OkHttp

-dontwarn com.squareup.okhttp3.**

-keep class com.squareup.okhttp3.** { *;}

#Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

#Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}


-keep class com.basestonedata.zycfc.** { *; }
-keep class com.basestonedata.bean.** { *; }
-keep class com.intsig.** { *; }
-keep class com.basestonedata.instalment.ui.creditcard.adapter.**{ *; }
-keep class com.basestonedata.instalment.ui.creditcard.model.**{ *; }
-keep class com.basestonedata.instalment.ui.creditcard.bean.**{ *; }
-keepclassmembers class * extends android.webkit.WebChromeClient{
       public void openFileChooser(...);
}


-keep class com.theartofdev.edmodo.cropper.** { *; }
-keep class com.taobao.wireless.**{*;}
-keep class android.support.v7.widget.** { *; }
-keep class com.zhihu.matisse.** { *; }




##########////////////////////////墨盒数据电商SDK 混淆/////////////////////

-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
#-overloadaggressively

# --------------------------------------------------------------------------
# Addidional for x5.sdk classes for apps

-keep class com.tencent.smtt.export.external.**{
    *;
}

-keep class com.tencent.tbs.video.interfaces.IUserStateChangedListener {
	*;
}

-keep class com.tencent.smtt.sdk.CacheManager {
	public *;
}

-keep class com.tencent.smtt.sdk.CookieManager {
	public *;
}

-keep class com.tencent.smtt.sdk.WebHistoryItem {
	public *;
}

-keep class com.tencent.smtt.sdk.WebViewDatabase {
	public *;
}

-keep class com.tencent.smtt.sdk.WebBackForwardList {
	public *;
}

-keep public class com.tencent.smtt.sdk.WebView {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$HitTestResult {
	public static final <fields>;
	public java.lang.String getExtra();
	public int getType();
}

-keep public class com.tencent.smtt.sdk.WebView$WebViewTransport {
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$PictureListener {
	public <fields>;
	public <methods>;
}


-keepattributes InnerClasses

-keep public enum com.tencent.smtt.sdk.WebSettings$** {
    *;
}

-keep public enum com.tencent.smtt.sdk.QbSdk$** {
    *;
}

-keep public class com.tencent.smtt.sdk.WebSettings {
    public *;
}


-keepattributes Signature
-keep public class com.tencent.smtt.sdk.ValueCallback {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebViewClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient$FileChooserParams {
	public <fields>;
	public <methods>;
}

-keep class com.tencent.smtt.sdk.SystemWebChromeClient{
	public *;
}
# 1. extension interfaces should be apparent
-keep public class com.tencent.smtt.export.external.extension.interfaces.* {
	public protected *;
}

# 2. interfaces should be apparent
-keep public class com.tencent.smtt.export.external.interfaces.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebViewCallbackClient {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebStorage$QuotaUpdater {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebIconDatabase {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebStorage {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk$PreInitCallback {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.Tbs* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.LogFileUtils {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLog {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLogClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

# Added for game demos
-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.Apn {
	public <fields>;
	public <methods>;
}
-keep class com.tencent.smtt.** {
	*;
}
# end


-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
	public <fields>;
	public <methods>;
}

-keep class MTT.ThirdAppInfoNew {
	*;
}

-keep class com.tencent.mtt.MttTraceEvent {
	*;
}

# Game related
-keep public class com.tencent.smtt.gamesdk.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBooter {
        public <fields>;
        public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
	public protected *;
}

-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
	public *;
}

-keepclasseswithmembers class * {
    ... *JNI*(...);
}

-keepclasseswithmembernames class * {
	... *JRI*(...);
}

-keep class **JNI* {*;}

-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**
-keep class cn.fraudmetrix.octopus.aspirit.bean.** { *; }

# 美妆相机 makeup
-keep class com.sensetime.stmobile.* { *;}
-keep class com.sensetime.stmobile.model.* { *;}
-keep class com.basestonedata.makeup.bean.** { *;}

#TONGDUN(指纹收集)
-dontwarn android.os.**
-dontwarn com.android.internal.**
    -keep class cn.tongdun.android.**{*;}
    -keep class cn.tongdun.bugly.**{*;}

#网易云信
-dontwarn org.apache.http.**
-dontwarn com.netease.**
-keep class com.netease.** {*;}
#如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}
-keep class com.basestonedata.instalment.ui.social.yunxin** { *;}
-keep class net.sqlcipher.** {*;}
-dontwarn org.json.**


-keep class com.basestonedata.pricecomponent.bean.**{*;}
-keep class com.basestonedata.pricecomponent.utils.**{*;}
-keep class android.support.design.**{*;}
-keep class com.basestonedata.instalment.util.GoodsGuideRouter {*;}

#七鱼
-dontwarn com.qiyukf.**
-keep class com.qiyukf.** {*;}

-keep class com.basestonedata.appearance.bean.**{*;}
-keep class com.basestonedata.appearance.view.comment.bean.**{*;}
-keep class com.basestonedata.appearance.net.response.**{*;}
#海淘json
-keep class com.basestonedata.instalment.util.haitao.**{*;}

#新浪闪付
-keep class com.weibopay.quickpay.entity.**{*;}
-dontwarn com.cfca.mobile.**
-keep class com.cfca.mobile.** {*;}

#搜索
-keep class com.basestonedata.searchmodule.results.achieve.shop.net.bean.**{*;}
-keep class com.basestonedata.searchmodule.results.achieve.shop.net.response.**{*;}
-keep class com.basestonedata.searchmodule.results.achieve.goods.net.bean.**{*;}
-keep class com.basestonedata.searchmodule.results.achieve.goods.net.response.**{*;}
-keep class com.basestonedata.searchmodule.results.achieve.heikafanli.net.bean.**{*;}
-keep class com.basestonedata.searchmodule.results.achieve.heikafanli.net.reponse.**{*;}
-keep class com.basestonedata.searchmodule.search.heikafanli.bean.**{*;}

-keep class com.sensorsdata.analytics.android.** { *; }
# 移动安全联盟
-keep class com.bun.miitmdid.core.** {*;}
-keep class com.bun.supplier.** {*;}
#微信SDK 混淆
-keep class com.tencent.mm.opensdk.** {*;}
-keep class com.tencent.wxop.** {*;}
-keep class com.tencent.mm.sdk.** {*;}
# 闪验sdk 混淆
-ignorewarnings
-dontwarn com.baidu.**
-dontwarn com.tencent.**
-dontwarn com.cmic.sso.sdk.**
-keep class com.cmic.sso.sdk.**{*;}
-dontwarn com.sdk.**
-keep class com.sdk.** { *;}
-keep class cn.com.chinatelecom.account.api.**{*;}

#个推
-dontwarn com.igexin.**
-keep class com.igexin.** { *; }

#AndroidX混淆开始
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
#AndroidX混淆结束

#XPopup混淆开始
-dontwarn com.lxj.xpopup.widget.**
-keep class com.lxj.xpopup.widget.**{*;}
#XPopup混淆结束