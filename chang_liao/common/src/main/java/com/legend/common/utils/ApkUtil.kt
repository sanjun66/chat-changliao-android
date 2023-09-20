package com.legend.common.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object ApkUtil {

    fun getVersionCode(context: Context, pkgName: String): Int {
        return try {
            context.packageManager.getPackageInfo(pkgName, 0).versionCode
        } catch (e: java.lang.Exception) {
            0
        }
    }

    fun installAPK(context: Context, file: File?) {
        if (file == null || !file.exists()) return
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        context.startActivity(intent)
    }
}