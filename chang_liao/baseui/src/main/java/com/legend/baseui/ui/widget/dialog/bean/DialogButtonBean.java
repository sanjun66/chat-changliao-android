package com.legend.baseui.ui.widget.dialog.bean;

import java.io.Serializable;

public class DialogButtonBean implements Serializable {
    public static final String ACTION_CONFIRM = "CONFIRM";
    public static final String ACTION_CANCEL = "CANCEL";
    public static final String ACTION_LINK = "LINK";

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
