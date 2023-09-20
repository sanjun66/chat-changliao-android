package com.legend.base.router;

public class UrlUtils {
    /**
     * 去除url指定参数
     */
    public static String removeParam(String url, String ...name){
        for (String s : name) {
            url = url.replaceAll("&?"+s+"=[^&]*","");
        }
        return url;
    }

}
