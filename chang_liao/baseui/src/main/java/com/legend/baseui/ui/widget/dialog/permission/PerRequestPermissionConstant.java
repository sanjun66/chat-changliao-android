package com.legend.baseui.ui.widget.dialog.permission;

import android.Manifest;

import com.com.legend.ui.R;

import java.util.HashMap;

public class PerRequestPermissionConstant {
    public static HashMap<String, PerRequestItem> perRequestItemHashMap = new HashMap<String, PerRequestItem>() {
        {
            put(Manifest.permission.ACCESS_FINE_LOCATION, new PerRequestItem(R.drawable.ui_permission_img_location, "位置权限", "帮助您快速填写收货地址，获取周边库存信息，推送各类优惠活动，验证反欺诈机制时获取。"));
            put(Manifest.permission.ACCESS_COARSE_LOCATION, new PerRequestItem(R.drawable.ui_permission_img_location, "位置权限", "帮助您快速填写收货地址，获取周边库存信息，推送各类优惠活动，验证反欺诈机制时获取。"));
            put(Manifest.permission.READ_CONTACTS, new PerRequestItem(R.drawable.ui_permission_img_location, "通讯录", "为您提供手机充值服务、社交推荐服务、填写紧急联系人时，方便您快速选择联系人。"));
            put(Manifest.permission.CAMERA, new PerRequestItem(R.drawable.ui_permission_img_camera, "相机", "为您提供视频拍摄、扫一扫、头像设置、人脸图像识别服务时获取。"));
            put(Manifest.permission.READ_EXTERNAL_STORAGE, new PerRequestItem(R.drawable.ui_permission_img_location, "存储权限", "保存商品图片、上传或修改用户头像。"));
            put(Manifest.permission.WRITE_EXTERNAL_STORAGE, new PerRequestItem(R.drawable.ui_permission_img_location, "存储权限", "保存商品图片、上传或修改用户头像。"));
            put(Manifest.permission.READ_PHONE_STATE, new PerRequestItem(R.drawable.ui_permission_img_location, "设备信息", "保障您安全正常的使用app，使用设备标识码进行统计、账户安全风控和服务推送等。"));
            put(Manifest.permission.RECORD_AUDIO, new PerRequestItem(R.drawable.ui_permission_img_voice, "麦克风", "为您提供语音搜索、语音客服、视频拍摄服务时获取。"));
        }
    };

    public static class PerRequestItem {
        public int resIcon;
        public String permissionName;
        public String description;

        public PerRequestItem(int resIcon, String permissionName, String description) {
            this.resIcon = resIcon;
            this.permissionName = permissionName;
            this.description = description;
        }
    }
}
