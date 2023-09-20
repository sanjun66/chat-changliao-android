package com.legend.main.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.TypeConst
import com.legend.common.bean.AppVersion
import com.legend.common.utils.ApkUtil
import com.legend.common.utils.ImFileDownloadUtil
import com.legend.imkit.videocall.util.getString
import com.legend.main.R
import java.io.File

class UpdateDialog(context: Context, private val updateInfo: AppVersion) : Dialog(context, com.legend.commonres.R.style.dialog) {
    private var progressBar: ProgressBar? = null
    private var tvConfirm: TextView? = null

    private val apkName = "myApp.apk"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update)

        initView()
    }

    private fun initView() {
        findViewById<TextView>(R.id.content).text = updateInfo.desc
        progressBar = findViewById(R.id.pb_progress)
        val tvCancel = findViewById<TextView>(R.id.tv_cancel)
        val btnLine: View = findViewById(R.id.view_btn_line)
        tvConfirm = findViewById(R.id.tv_confirm)
        if (updateInfo.forced_update == TypeConst.type_yes) {
            tvCancel.visibility = View.GONE
            btnLine.visibility = View.GONE

            setCancelable(false)
            setCanceledOnTouchOutside(false)
        } else {
            tvCancel.visibility = View.VISIBLE
            btnLine.visibility = View.VISIBLE

            setCancelable(true)
            setCanceledOnTouchOutside(false)
        }

        tvCancel.setOnClickListener {
            dismiss()
        }
        tvConfirm?.setOnClickListener {
            toPhoneWeb(if (TextUtils.isEmpty(updateInfo.update_url)) "http://www.baidu.com" else updateInfo.update_url)
            dismiss()
        }
    }

    private fun toPhoneWeb(url: String) {
        val uri: Uri = Uri.parse(url)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun toDownload() {
        progressBar?.visibility = View.VISIBLE
        deleteCurApk()
        downloadApk()
    }

    private fun downloadApk() {
        ImFileDownloadUtil.downloadWithProgress(updateInfo.update_url, FileUtils.getAppFilePath() + apkName, object : ImFileDownloadUtil.DownloadListener {
            override fun onStart() {
            }

            override fun onProgress(progress: Int) {
                progressBar?.progress = progress
            }

            override fun onCompleted() {
                ApkUtil.installAPK(context, File(FileUtils.getAppFilePath() + apkName))
            }

            override fun onFail() {
                progressBar?.visibility = View.GONE
                ToastUtils.show(getString(com.legend.commonres.R.string.update_error))
                deleteCurApk()
                tvConfirm?.text = getString(com.legend.commonres.R.string.update_again)
            }

        })
    }

    private fun deleteCurApk() {
        val filePath = FileUtils.getAppFilePath() + apkName
        if (FileUtils.hasFile(filePath)) File(filePath).delete()
    }

}