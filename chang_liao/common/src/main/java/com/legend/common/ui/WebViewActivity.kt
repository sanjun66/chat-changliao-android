package com.legend.common.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.utils.NetUtils
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.commonres.BuildConfig
import com.legend.commonres.R
import com.legend.commonres.databinding.ActivityWebviewBinding

@Route(path = RouterPath.path_webview_activity)
class WebViewActivity: BaseActivity<ActivityWebviewBinding>() {
    private val CHOOSE = 100 //Android 5.0以下的
    private val CHOOSE_ANDROID_5 = 200 //Android 5.0以上的
    private var mValueCallback: ValueCallback<Uri>? = null
    private var mValueCallback2: ValueCallback<Array<Uri>>? = null
    private var mUrl = ""

    override fun getLayoutId() = R.layout.activity_webview

    override fun initView() {
        mUrl = intent.extras?.getString(KeyConst.key_url) ?: ""
        initWebView()
        initWebViewClient()
        mDataBinding?.webView?.loadUrl(mUrl)
    }

    private fun initWebView() {
        if (mDataBinding?.webView == null) return
        val settings: WebSettings = mDataBinding?.webView!!.settings
        // 开启和JS交互
        settings.javaScriptEnabled = true
        // 开启 DOM storage API 功能 较大存储空间
        settings.domStorageEnabled = true
        // 允许在WebView中访问内容URL
        settings.allowContentAccess = true
        // 允许应用缓存API
//        settings.setAppCacheEnabled(true)
        if (NetUtils.netIsConnected(mContext)) {
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
        } else {
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        // 屏幕自适应
        settings.useWideViewPort = true
        // 缩放至屏幕的大小
        settings.loadWithOverviewMode = true
        // debug可调试
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        // 设置默认缓存策略,缓存可获取并且没有过期的情况下加载缓存，否则通过网络获取资源
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        // 开启database storage API功能
        settings.databaseEnabled = true
        // 设置定位的数据库路径
        val dir: String = mContext.applicationContext.getDir("database", MODE_PRIVATE).path
        settings.setGeolocationDatabasePath(dir)
        settings.setGeolocationEnabled(true)
        // 支持混合模式
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        // 支持自动加载图片
        settings.loadsImagesAutomatically = true

    }

    private fun initWebViewClient() {
        mDataBinding?.webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // 返回true，过滤url
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setTitleBarTitleText(view?.title)
            }
        }

        mDataBinding?.webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    mDataBinding?.progressbar?.visibility = View.GONE
                } else {
                    mDataBinding?.progressbar?.progress = newProgress
                }
            }

            // 以下是在各个Android版本中 webview调用文件选择器的方法
            // For Android < 3.0
            open fun openFileChooser(valueCallback: ValueCallback<Uri>?) {
                openImageChooserActivity(valueCallback)
            }

            // For Android  >= 3.0
            fun openFileChooser(valueCallback: ValueCallback<Uri>?, acceptType: String?) {
                openImageChooserActivity(valueCallback)
            }

            //For Android  >= 4.1
            fun openFileChooser(
                valueCallback: ValueCallback<Uri>,
                acceptType: String?, capture: String?
            ) {
                openImageChooserActivity(valueCallback)
            }

            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                mValueCallback2 = filePathCallback
                val intent = fileChooserParams.createIntent()
                startActivityForResult(intent, CHOOSE_ANDROID_5)
                return true
            }
        }
    }

    private fun openImageChooserActivity(valueCallback: ValueCallback<Uri>?) {
        mValueCallback = valueCallback
        val intent = Intent()
        if (Build.VERSION.SDK_INT < 19) {
            intent.action = Intent.ACTION_GET_CONTENT
        } else {
            intent.action = Intent.ACTION_PICK
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_flie)), CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            CHOOSE -> processResult(resultCode, intent)
            CHOOSE_ANDROID_5 -> processResultAndroid5(resultCode, intent)
        }
    }

    private fun processResult(resultCode: Int, intent: Intent?) {
        if (mValueCallback == null) {
            return
        }
        if (resultCode == RESULT_OK && intent != null) {
            val result = intent.data
            mValueCallback!!.onReceiveValue(result)
        } else {
            mValueCallback!!.onReceiveValue(null)
        }
        mValueCallback = null
    }

    private fun processResultAndroid5(resultCode: Int, intent: Intent?) {
        if (mValueCallback2 == null) {
            return
        }
        if (resultCode == RESULT_OK && intent != null) {
            mValueCallback2!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    intent
                )
            )
        } else {
            mValueCallback2!!.onReceiveValue(null)
        }
        mValueCallback2 = null
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.fakeStatusBar = true
        pageConfig?.showAppTitleBar = true
        pageConfig?.backgroundColor = Color.WHITE
    }

}