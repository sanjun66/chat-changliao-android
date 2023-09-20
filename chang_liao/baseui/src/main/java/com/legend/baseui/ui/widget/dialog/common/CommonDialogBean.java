package com.legend.baseui.ui.widget.dialog.common;

import android.text.TextUtils;

import com.legend.baseui.ui.CommonUIConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommonDialogBean implements Serializable {
    public String picUrl;  //图片地址
    public List<ButtonBean> buttonList;  //按钮列表
    public String title; //弹框标题
    public String subTitle; //弹框子标题
    public String content; //弹窗内容
    public String tips; //内容下方提示
    public boolean showCloseIcon; //是否显示右侧关闭按钮
    public boolean hideBtnLine; //是否隐藏内容与按钮区域的分割线
    public int marginLeft;
    public int marginRight;
    public int marginTop;
    public int marginBottom;
    public ExtInfo extInfo;//额外信息


    public ButtonBean buildButtonBean() {
        return new ButtonBean();
    }

    public void buildCommonButton(String cancel, String confirm) {
        buttonList = new ArrayList<>();
        ButtonBean buttonBean;

        String cancelButtonColor = null;
        String confirmButtonColor = null;
        CommonUIConfig.CommonDialogConfig commonDialogConfig = CommonUIConfig.getInstance().getCommonDialogConfig();
        if (null != commonDialogConfig) {
            cancelButtonColor = commonDialogConfig.cancelButtonTextColor;
            confirmButtonColor = commonDialogConfig.confirmButtonTextColor;
        }

        if (!TextUtils.isEmpty(cancel)) {
            buttonBean = buildButtonBean();
            buttonBean.action = ButtonBean.ACTION_CANCEL;
            buttonBean.title = cancel;
            buttonBean.color = TextUtils.isEmpty(cancelButtonColor) ? "#999999" : cancelButtonColor;
            buttonList.add(buttonBean);
        }

        if (!TextUtils.isEmpty(confirm)) {
            buttonBean = buildButtonBean();
            buttonBean.action = ButtonBean.ACTION_CONFIRM;
            buttonBean.title = confirm;
            if (!TextUtils.isEmpty(confirmButtonColor))
                buttonBean.color = commonDialogConfig.confirmButtonTextColor;
            buttonList.add(buttonBean);
        }
    }


    public static CommonDialogBean buildCommonBean(String title, String content, String btnCancel, String btnConfirm) {
        CommonDialogBean bean = new CommonDialogBean();
        bean.title = title;
        bean.content = content;
        bean.buildCommonButton(btnCancel, btnConfirm);
        return bean;
    }

    public class ExtInfo implements Serializable {
        public String newPrice;
    }

    public class ButtonBean implements Serializable {
        public static final int ACTION_TYPE_CONFIRM = 1;
        public static final int ACTION_TYPE_CANCEL = 2;
        public static final int ACTION_TYPE_GOTO_URL = 3;

        public static final String ACTION_CONFIRM = "CONFIRM";
        public static final String ACTION_CANCEL = "CANCEL";
        public static final String ACTION_LINK = "LINK";
        public static final String ACTION_CODE_1001 = "CODE_1001";//特殊场景，提交订单时价格变动，需要刷新页面价格
        public static final String ACTION_REFRESH= "REFRESH";

        public int actionType;  //操作类型
        public String action;//CONFIRM：确认；CANCEL：取消；LINK：跳转
        public String title;  //按钮文案
        public String gotoUrl; //调整链接
        public String color; //按钮文字颜色
        public Background background; //按钮背景


        public Background buildBackground() {
            return new Background();
        }


        public class Background implements Serializable {
            public String color; //背景颜色
            public int radius; //背景圆角率
            public int strokeWidth; //边框大小
            public String strokeColor; //边框颜色
        }
    }
}
