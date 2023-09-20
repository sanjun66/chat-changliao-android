package com.legend.common.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.util.statusbar.StatusBarUtil
import com.legend.baseui.ui.widget.titlebar.MTitleBar
import com.legend.common.utils.QrIdentifyUtil
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.commonres.R
import com.legend.main.network.viewmodel.GroupViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.yxing.ScanCodeActivity
import com.yxing.ScanCodeConfig

class QrScanActivity: ScanCodeActivity() {
    private val REQUEST_CODE_IMAGE  = 103
    private var titleBar: MTitleBar? = null

    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qrscan
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideActionStatusBar()
        initTitleBar()
    }

    private fun initTitleBar() {
        titleBar = findViewById(R.id.title_bar)
        titleBar?.let {
            it.setLeftIcon(com.com.legend.ui.R.drawable.ui_double_back)
            it.setTitle(getString(R.string.seal_main_title_scan))
            it.setRightText(getString(R.string.extra_photo_album))
            it.setRightTitleColor("#27DF99")
            it.setListener(object : MTitleBar.OnTitleListener {
                override fun titleBarLeftClick() {
                    finish()
                }

                override fun titleBarRightClick() {
                    PictureSelectorUtil.openGalleryPicSingle(this@QrScanActivity, "s0", REQUEST_CODE_IMAGE)
                }

                override fun titleBarTitleClick() {
                }

            })
    
            val statusBarHeight = StatusBarUtil.getStatusBarHeight(this)
            val params:RelativeLayout.LayoutParams = it.layoutParams as RelativeLayout.LayoutParams
            params.topMargin = statusBarHeight
            it.layoutParams = params
        }
    }

    private fun hideActionStatusBar() {
        // actionbar隐藏
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        // 设置状态栏颜色
        window.statusBarColor = resources.getColor(com.legend.commonres.R.color.comm_page_bg)
        // 设置状态栏字体颜色
        val ui = window.decorView.systemUiVisibility
        val ui1 = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = ui1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_CODE_IMAGE -> {
                    val selectPics = PictureSelector.obtainSelectorList(data)
                    for (media in selectPics) {
                        var filePath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath
                        if (!FileUtils.hasFile(filePath) && !TextUtils.isEmpty(media.path)) filePath = FileUtils.getFileAbsolutePath(this, Uri.parse(media.path))
                        val code = ScanCodeConfig.scanningImage(this,  FileUtils.getUriFromPath(this, filePath))
                        QrIdentifyUtil.qrIdentify(code, true)
                    }
                }
            }
        }

    }
}