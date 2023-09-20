package com.legend.basenet.network;

import android.text.TextUtils;

public class HttpHeaderManager {

    public static final String TOKEN = "token";
    public static final String BEARER_TOKEN = "Authorization";
    /**
     * App 端build 版本， 客户端 【打包时候】 版本比如 3.3.1
     */
    public static final String APP_VERSION = "Build-Version";
    public static final String BUILD = "API-Version";
    public static final String APP_CHANNEL = "appChannel";
    public static final String DEVICE_ID = "Device-Id";
    public static final String DEVICE_ID2 = "DeviceId2";
    public static final String OAID = "Oa-Id";
    public static final String LON = "Longitude";
    /**
     * 经纬度,如：42,32
     */
    public static final String LAT = "Latitude";
    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String CITY_CODE = "City-Code";
    public static final String CITY_NAME = "City-Code";
    public static final String DISTRICT = "district";
    public static final String STREET = "street";
    public static final String ADDRESS = "address";
    /**
     *
     */
    public static final String APP_TYPE  = "apptype";
    /**
     * 网络环境类型 wifi，5g，4g，3g
     */
    public static final String NETWORK = "Network-Env";
    /**
     * 网络供应商， 可选, CN_MOBILE
     */
    public static final String NETWORK_PROVIDER = "Network-Provider";
    public static final String AGENT_TYPE = "agenttype";
    /**
     * 手机品牌， 可选： Huawei, xiaomi
     */
    public static final String PHONE_BRAND = "Mobile-Brand";
    /**
     * 手机品牌对应型号Model
     */
    public static final String MOBILE_TYPE = "Mobile-Type";
    /**
     * 手机品牌对应型号Model
     */
    public static final String OS_TYPE = "OS";
    /**
     * 操作系统版本， 可选, 比如 Android, iOS，Window， Mac 的 版本
     */
    public static final String OS_VERSION = "OS-Version";
    /**
     * app 发行渠道， 比如华为应用市场,google play, 应用宝
     */
    public static final String CHANNEL_TYPE = "Pkg-Delivery-Code";
    /**
     * 对应产品， 后续， 一账通， 产品矩阵， 不同产品编号！， 不同产品代号
     */
    public static final String PRODUCT = "Product";

    public static final String USER_AGENT = "User-Agent";

    public static final String ANDROID_ID = "ANDROID-ID";
    /**
     * 媒体渠道， 哪里引导下载渠道包的地方！第一次用户注册使用， 不要和发行渠道混淆！他们可能一样也可能不一样
     */
    public static final String MEDIA_CHANNEL = "Media-Channel";

    /**
     * 同盾科技SDK返回信息
     */
    public static final String BLACK_BOX = "BlackBox";

    public static void setToken(String token) {
        InterceptorManager.getInstance().addOrUpdateHeader(TOKEN, token);
    }

    public static void setBearerToken(String token) {
        InterceptorManager.getInstance().addOrUpdateHeader(BEARER_TOKEN, "Bearer " + token);
    }

    public static void setAppVersion(String appVersion) {
        InterceptorManager.getInstance().addOrUpdateHeader(APP_VERSION, appVersion);
    }
    public static void setUserAgent(String cusAgent) {
        InterceptorManager.getInstance().addOrUpdateHeader(USER_AGENT, cusAgent);
    }
    public static void setAndroidId(String androidId) {
        InterceptorManager.getInstance().addOrUpdateHeader(ANDROID_ID, androidId);
    }
    public static void setMediaChannel(String mediaChannel) {
        InterceptorManager.getInstance().addOrUpdateHeader(MEDIA_CHANNEL, mediaChannel);
    }
    public static void setBuild(String build) {
        InterceptorManager.getInstance().addOrUpdateHeader(BUILD, build);
    }

    public static void setAppChannel(String appChannel) {
        InterceptorManager.getInstance().addOrUpdateHeader(APP_CHANNEL, appChannel);
    }

    public static void setDeviceId(String deviceId) {
        InterceptorManager.getInstance().addOrUpdateHeader(DEVICE_ID, deviceId);
    }

    public static void setDeviceId2(String deviceId2) {
        InterceptorManager.getInstance().addOrUpdateHeader(DEVICE_ID2, deviceId2);
    }

    public static void setOAID(String oaid) {
        InterceptorManager.getInstance().addOrUpdateHeader(OAID, oaid);
    }

    public static void setLon(String lon) {
        InterceptorManager.getInstance().addOrUpdateHeader(LON, lon);
    }

    public static void setLat(String lat) {
        InterceptorManager.getInstance().addOrUpdateHeader(LAT, lat);
    }

    public static void setProvince(String province) {
        InterceptorManager.getInstance().addOrUpdateHeader(PROVINCE, province);
    }

    public static void setCity(String city) {
        InterceptorManager.getInstance().addOrUpdateHeader(CITY, city);
    }
    public static void setCityName(String cityName) {
        InterceptorManager.getInstance().addOrUpdateHeader(CITY_NAME, cityName);
    }

    public static void setCityCode(String cityCode) {
        InterceptorManager.getInstance().addOrUpdateHeader(CITY_CODE, cityCode);
    }

    public static void setDistrict(String district) {
        InterceptorManager.getInstance().addOrUpdateHeader(DISTRICT, district);
    }

    public static void setStreet(String street) {
        InterceptorManager.getInstance().addOrUpdateHeader(STREET, street);
    }

    public static void setAddress(String address) {
        InterceptorManager.getInstance().addOrUpdateHeader(ADDRESS, address);
    }

    public static void setAppType(String appType) {
        InterceptorManager.getInstance().addOrUpdateHeader(APP_TYPE, appType);
    }

    public static void setNetwork(String network) {
        InterceptorManager.getInstance().addOrUpdateHeader(NETWORK, network);
    }

    public static void setAgentType(String agentType) {
        InterceptorManager.getInstance().addOrUpdateHeader(AGENT_TYPE, agentType);
    }

    public static void setPhoneBrand(String phoneBrand) {
        InterceptorManager.getInstance().addOrUpdateHeader(PHONE_BRAND, phoneBrand);
    }
    public static void setPhoneModel(String phoneBrand) {
        InterceptorManager.getInstance().addOrUpdateHeader(MOBILE_TYPE, phoneBrand);
    }

    public static void setOsType(String osType) {
        InterceptorManager.getInstance().addOrUpdateHeader(OS_TYPE, osType);
    }

    public static void setOsVersion(String osVersion) {
        InterceptorManager.getInstance().addOrUpdateHeader(OS_VERSION, osVersion);
    }

    public static void setChannelType(String channelType) {
        InterceptorManager.getInstance().addOrUpdateHeader(CHANNEL_TYPE, channelType);
    }

    public static void setHeader(String key, String value) {
        if (TextUtils.isEmpty(key))
            return;
        InterceptorManager.getInstance().addOrUpdateHeader(key, value);
    }

    public static void removeHeader(String key) {
        if (TextUtils.isEmpty(key))
            return;
        InterceptorManager.getInstance().removeHeader(key);
    }

    public static void setBlackBox(String blackBox){
        if(TextUtils.isEmpty(blackBox)){
            return;
        }
        InterceptorManager.getInstance().addOrUpdateHeader(BLACK_BOX, blackBox);
    }
}
