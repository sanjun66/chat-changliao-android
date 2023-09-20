package com.legend.imkit.activity

import android.os.Build
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.utils.FileUtils
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ImgLoader
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.imkit.R
import com.legend.common.bean.FileMsgContent
import com.legend.imkit.databinding.ActivityFilePreviewBinding
import com.legend.common.utils.FileTypeUtils

@Route(path = RouterPath.path_file_preview_activity)
class FilePreviewActivity: BaseActivity<ActivityFilePreviewBinding>() {
    private val TAG = "FilePreviewActivity"
    companion object {
        val NOT_DOWNLOAD = 0
        val DOWNLOADED = 1
        val DOWNLOADING = 2
        val DELETED = 3
        val DOWNLOAD_ERROR = 4
        val DOWNLOAD_CANCEL = 5
        val DOWNLOAD_SUCCESS = 6
        val DOWNLOAD_PAUSE = 7
        val TXT_FILE = ".txt"
        val APK_FILE = ".apk"
        val FILE = "file://"
    }
    
    private var mFileContent: FileMsgContent? = null
    private var mSessionId: String? = null
    
    override fun getLayoutId() = R.layout.activity_file_preview

    override fun initView() {
        mSessionId = intent.getStringExtra(KeyConst.key_session_uid)
        mFileContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(KeyConst.key_file_content, FileMsgContent::class.java)
        } else {
            intent.getParcelableExtra<FileMsgContent>(KeyConst.key_file_content)
        }
        
        setTitleBarTitleText(getString(com.legend.commonres.R.string.rc_ac_file_download_preview))
        mDataBinding?.apply {
            ImgLoader.display(mContext, FileTypeUtils.fileTypeImageId(mContext, mFileContent?.original_name), imgFileTypeImage)
            tvFileName.text = mFileContent?.original_name
            tvFileSize.text = FileUtils.convertFileSize(mFileContent?.size?:0).toString()
            btnDownloadButton.setOnClickListener {
                // todo
            }
        }
    }

    override fun initData() {
        val localPath = mFileContent?.getLocalPath(mSessionId?:"", FileUtils.FILE_TYPE_FILE)
        if (FileUtils.hasFile(localPath)) {
        
        } else {
        
        }
    }

    private fun isOpenInsideApp(filePath: String?): Boolean {
        return filePath != null && filePath.endsWith(TXT_FILE)
    }
    
    
    
    
    
    
    
    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}