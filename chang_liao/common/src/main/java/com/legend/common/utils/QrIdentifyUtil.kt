package com.legend.common.utils

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.google.gson.reflect.TypeToken
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.StringUtils
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.Router
import com.legend.common.TypeConst
import com.legend.common.activity.QrScanActivity
import com.legend.common.bean.UserBean
import com.legend.commonres.R
import com.yxing.ScanCodeConfig
import com.yxing.bean.ScanRect
import com.yxing.def.ScanMode
import com.yxing.def.ScanStyle

object QrIdentifyUtil {

    fun qrScan(activity: Activity) {
        ScanCodeConfig.create(activity)
            // 设置扫码页样式 ScanStyle.NONE：无  ScanStyle.QQ ：仿QQ样式   ScanStyle.WECHAT ：仿微信样式    ScanStyle.CUSTOMIZE ： 自定义样式
            .setStyle(ScanStyle.CUSTOMIZE)
            .setScanRect( ScanRect(50, 200, 300, 450), false)
            .setScanSize(600, 0, 0)
            // 扫码成功设置播放音效
            .setPlayAudio(false)
            //设置 二维码提示按钮的宽度 单位：px
            .setQrCodeHintDrawableWidth(120)
            //设置 二维码提示按钮的高度 单位：px
            .setQrCodeHintDrawableHeight(120)
            //设置 二维码提示Drawable 是否开启缩放动画效果
            .setStartCodeHintAnimation(true)
            //设置 二维码选择页 背景透明度
            .setQrCodeHintAlpha(0.5f)
            .setShowFrame(true)
            .setFrameRadius(2)
            .setFrameWith(4)
            .setFrameLength(15)
            .setFrameColor(com.legend.commonres.R.color.primary_color)
            .setShowShadow(true)
            .setScanMode(ScanMode.REVERSE)
            .setScanDuration(3000)
            .setScanBitmapId(com.example.yxing.R.drawable.scan_wechatline)
            //设置边框外部阴影颜色
            .setShadeColor(com.legend.commonres.R.color.black_tran30)
//            .setShadeColor(com.legend.commonres.R.color.primary_color)
//            .setShowShadow(true)
            .buidler()
            .start(QrScanActivity::class.java)
    }

    fun qrIdentify(qrCodeRes: String?, isGroupScan: Boolean): UserBean.QRBeanRes? {
        Log.i("byy", "识别二维码 qrCodeRes = $qrCodeRes")
        if (TextUtils.isEmpty(qrCodeRes)) {
            ToastUtils.show(StringUtils.getString(R.string.identify_qr_code_error))
            return null
        }

        val qrBean = GlobalGsonUtils.fromJson<UserBean.QRBean>(qrCodeRes, object : TypeToken<UserBean.QRBean>(){}.type)
        if (qrBean == null || qrBean.appName != StringUtils.getString(com.legend.commonres.R.string.app_name)) {
            ToastUtils.show(StringUtils.getString(R.string.identify_qr_code_error))
            return null
        }

        if (qrBean.type == TypeConst.qr_type_single) {
            val realCode = qrBean.uid
            Router.toUserActivity(realCode, "")
            return UserBean.QRBeanRes(TypeConst.qr_type_single, realCode, null, null)
        } else {
            if (isGroupScan) Router.toQrGroupIdentifyActivity(qrBean.uid, qrBean.groupId, qrBean.groupName)
            return UserBean.QRBeanRes(TypeConst.qr_type_group, qrBean.uid, qrBean.groupId, qrBean.groupName)
        }
    }
}