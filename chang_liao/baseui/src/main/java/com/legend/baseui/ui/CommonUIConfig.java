package com.legend.baseui.ui;

import androidx.annotation.LayoutRes;

public class CommonUIConfig {
    private static CommonUIConfig config;

    private LoadingConfig loadingConfig;
    private CommonDialogConfig commonDialogConfig;

    private CommonUIConfig() {

    }

    public static CommonUIConfig getInstance() {
        synchronized (CommonUIConfig.class) {
            if (null == config)
                config = new CommonUIConfig();
        }
        return config;
    }

    public void setLoadingConfig(LoadingConfig config) {
        if (null != this.loadingConfig)
            throw new IllegalArgumentException("global config only set once");

        this.loadingConfig = config;
    }


    public LoadingConfig getLoadingConfig() {
        return loadingConfig;
    }

    public LoadingConfig buildLoadingConfig() {
        return new LoadingConfig();
    }

    public CommonDialogConfig buildCommonDialogConfig() {
        return new CommonDialogConfig();
    }

    public void setCommonDialogConfig(CommonDialogConfig commonDialogConfig) {
        if (null != this.commonDialogConfig)
            throw new IllegalArgumentException("global config only set once");
        this.commonDialogConfig = commonDialogConfig;
    }

    public CommonDialogConfig getCommonDialogConfig() {
        return commonDialogConfig;
    }

    public static class LoadingConfig {
        @LayoutRes
        public Integer errPageLayoutId;

        @LayoutRes
        public Integer emptyPageLayoutId;
    }

    public class CommonDialogConfig {
        public String cancelButtonTextColor;

        public String confirmButtonTextColor;

        public String linkButtonTextColor;
    }
}
