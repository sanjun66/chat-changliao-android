package com.legend.base.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.legend.base.Applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by niuzj on 2019/8/6
 * Copyright 2019 BSD. All rights reserved.
 * description:
 */
public class IMEIUtil {
    public static String getImei() {
        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) Applications.getCurrent().getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            String imei = getNewDeviceId(mTelephonyMgr);
            if (TextUtils.isEmpty(imei))
                imei = "bsd" + MD5(getMacAddress() + getAndroidIdWithSerial());
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 没有授权情况下获取其它信息
     * @return
     */
    public static String getImeiNoPersmisson() {
        try {
            //获取IMEI号
            String imei = "bsd" + MD5(getAndroidIdWithSerial());
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getNewDeviceId(TelephonyManager telephonyManager) {
        String deviceId = "";
        try {
            deviceId = telephonyManager.getDeviceId();
        } catch (Exception e) {

        }

        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                deviceId = telephonyManager.getImei();
            } catch (Exception e) {

            }
            if (!TextUtils.isEmpty(deviceId)) {
                return deviceId;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                deviceId = telephonyManager.getMeid();
            } catch (Exception e) {

            }
            if (!TextUtils.isEmpty(deviceId)) {
                return deviceId;
            }
        }

        return deviceId;

    }

    private static String getMacAddress() {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(Applications.getCurrent());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    public static String getAndroidIdWithSerial() {
        String androidId = Settings.System.getString(Applications.getCurrent().getContentResolver(), Settings.Secure.ANDROID_ID);
        String serial = getSerial();
        StringBuffer stringBuffer = new StringBuffer();
        if (!TextUtils.isEmpty(androidId)) {
            stringBuffer.append(androidId);
        }
        if (!TextUtils.isEmpty(serial)) {
            stringBuffer.append(serial);
        }
        return stringBuffer.toString();
    }

    private static String getSerial() {
        String serial = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                serial = Build.getSerial();
            } catch (Exception e) {

            }
        } else {
            serial = Build.SERIAL;
        }
        if (TextUtils.equals(serial, Build.UNKNOWN)) {
            serial = "";
        }
        return serial;
    }

    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";

        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    private static String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {

        }
        if (!TextUtils.isEmpty(result)) {
            if (result.length() < 24) {
                return result;
            } else {
                return result.substring(8, 24);
            }
        } else {
            return result;
        }

    }
}
