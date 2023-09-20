package com.legend.baseui.ui.util.rom;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2018/07/09
 *    desc   :
 * </pre>
 */
public abstract class Checker implements IChecker {

    protected abstract String getManufacturer();

    protected abstract String getRomKey();

    @Override
    public boolean checkManufacturer(String manufacturer) {
        return manufacturer.equalsIgnoreCase(getManufacturer());
    }


    @Override
    public boolean checkRom() {
        RomProperties romProperties = getRomProperties();
        try {
            String property = romProperties.getProperty(getRomKey());
            if (TextUtils.isEmpty(property)) {
                property = RomProperties.getSystemProperty(getRomKey());
            }
            if (!TextUtils.isEmpty(property)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public RomProperties getRomProperties() {
        RomProperties properties = new RomProperties();
        if (Build.VERSION.SDK_INT < 28) {
            FileInputStream is = null;
            try {
                // 获取 build.prop 配置
                Properties buildProperties = new Properties();
                is = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
                buildProperties.load(is);
                properties.setBuildProp(buildProperties);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return properties;
    }

    /*@Override
    public boolean checkApplication(Context context) {
        PackageManager manager = context.getPackageManager();
        for (String pkg : getAppList()) {
            try {
                manager.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean checkApplication(Set<String> installedPackages) {
        int count = 0;
        String[] list = getAppList();
        int aim = (list.length + 1) / 2;
        for (String pkg : list) {
            if (installedPackages.contains(pkg)) {
                count++;
                if (count >= aim)
                    return true;
            }
        }
        return false;
    }*/
}
