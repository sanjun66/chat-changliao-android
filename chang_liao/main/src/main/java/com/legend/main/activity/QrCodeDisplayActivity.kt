package com.legend.main.activity

import android.Manifest
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.base.Applications
import com.legend.base.utils.GlobalGsonUtils
import com.legend.base.utils.permission.Permission
import com.legend.base.utils.permission.PermissionUtils
import com.legend.base.utils.permission.listener.PermissionListener
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.CommonDialogUtils
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.utils.SaveDataUtils
import com.legend.common.utils.SavePhotoUtils
import com.legend.main.R
import com.legend.main.databinding.ActivityQrcodeDisplayBinding
import com.legend.main.network.viewmodel.GroupViewModel
import com.yxing.ScanCodeConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Route(path = RouterPath.path_qrcode_display_activity)
class QrCodeDisplayActivity: BaseActivity<ActivityQrcodeDisplayBinding>() {
    private val STORAGE_PERMSSSION = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var qrBitmap: Bitmap? = null
    private var uid: String? = null
    private var groupId: String? = null

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_qrcode_display

    override fun initView() {
        mDataBinding?.apply {
            tvSavePhone.setOnClickListener {
//                qrBitmap?.let {
//                    saveQrToPhone(it)
//                }

                val bitmap = getScreenShot()
                bitmap?.let {
                    saveQrToPhone(it)
                }
            }

        }
    }
    
    private fun saveQrToPhone(bitmap: Bitmap) {
        Permission.request(STORAGE_PERMSSSION, this, object : PermissionListener {
            override fun preRequest(permissions: Array<out String>?): Boolean {
                return false
            }
        
            override fun granted(permissions: Array<out String>?) {
                CoroutineScope(Dispatchers.IO).launch {
//                    if (FileUtils.saveQrToPhone(this@QrCodeDisplayActivity, bitmap)) {
//                        ToastUtils.show(getString(R.string.save_success))
//                    } else {
//                        ToastUtils.show(getString(R.string.save_fail))
//                    }

                    val destFile = SavePhotoUtils.createSaveFile(Applications.getCurrent(), true, "a_${System.currentTimeMillis()}.jpg", "sss")
                    SavePhotoUtils.saveBitmap2SelfDirectory(this@QrCodeDisplayActivity, bitmap, destFile,
                        object : SavePhotoUtils.SaveBitmapCallBack {
                            override fun saveSuccess(path: String) {
                                ToastUtils.show(getString(R.string.save_success))
                            }

                            override fun fail() {
                                ToastUtils.show(getString(R.string.save_fail))
                            }
                        }, true) }
            }
        
            override fun denied(permissions: Array<out String>?) {
                ToastUtils.show(getString(com.legend.imkit.R.string.permission_denied))
                CommonDialogUtils.showPermissionDeniedTipsDialog(PermissionUtils.getPermissionName(
                    STORAGE_PERMSSSION
                ), this@QrCodeDisplayActivity)
            }
        
        })
    }

    private fun getScreenShot(): Bitmap? {
        val rootView = mDataBinding?.llCardContainer
        rootView?.apply {
            isDrawingCacheEnabled = true
            buildDrawingCache()
            val screenShot = Bitmap.createBitmap(drawingCache)
            isDrawingCacheEnabled = false
            return screenShot
        }
        return null
    }

    override fun initData() {
        uid = intent.getStringExtra(KeyConst.key_user_id)
        groupId = intent.getStringExtra(KeyConst.key_group_id)

        if (TextUtils.isEmpty(groupId)) {
            setTitleBarTitleText(getString(R.string.setting_my_qrcode))
            mDataBinding?.tvBottomTips?.text = getString(R.string.scan_qr_join_friend)
            userViewModel.getUserInfo(uid)
            userViewModel.userInfoRes.observe(this) {
                SaveDataUtils.saveUserInfo(it)
                mDataBinding?.apply {
                    tvName.text = it.getNickName()
                    ImgLoader.display(mContext, it.avatar, rivAvatar)
                    val qrBean = UserBean.QRBean(getString(com.legend.commonres.R.string.app_name), "", "", it.id, TypeConst.qr_type_single)
                    qrBitmap = ScanCodeConfig.createQRCode(GlobalGsonUtils.toJson(qrBean), DisplayUtils.dp2px(mContext, 200f))
                    imgQrCode.setImageBitmap(qrBitmap)
                }
            }
        } else {
            setTitleBarTitleText(getString(R.string.group_qrcode))
            mDataBinding?.tvBottomTips?.text = getString(R.string.scan_qr_join_group)
            groupViewModel.getGroupInfo(groupId!!)
            groupViewModel.groupInfoRes.observe(this) {
                if (it.isSuccess) {
                    val groupInfoRes = it.groupInfo
                    mDataBinding?.apply {
                        tvName.text = groupInfoRes?.group_info?.name
                        ImgLoader.display(mContext, groupInfoRes?.group_info?.avatar, rivAvatar)
                        val qrBean = UserBean.QRBean(getString(com.legend.commonres.R.string.app_name), groupInfoRes?.group_info?.id.toString(), groupInfoRes?.group_info?.name?:"", uid?:"", TypeConst.qr_type_group)
                        qrBitmap = ScanCodeConfig.createQRCode(GlobalGsonUtils.toJson(qrBean))
                        imgQrCode.setImageBitmap(qrBitmap)
                    }
                }
            }
        }

    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}