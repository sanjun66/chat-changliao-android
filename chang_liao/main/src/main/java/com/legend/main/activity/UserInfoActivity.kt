package com.legend.main.activity

import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.db.entity.DBEntity
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.upload.UploadCallback
import com.legend.common.upload.UploadUtil
import com.legend.common.utils.SaveDataUtils
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.ActivityUserInfoBinding
import com.luck.picture.lib.basic.PictureSelector
import java.io.File

@Route(path = RouterPath.path_user_info_activity)
class UserInfoActivity: BaseActivity<ActivityUserInfoBinding>() {
    private val avatarRequestCode = 200
    private val changeNicknameRequestCode = 201
    private var isModify = false

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    override fun getLayoutId() = R.layout.activity_user_info

    override fun initView() {
        setTitleBarTitleText(getString(R.string.setting_edit))
        mDataBinding?.apply {
            rivAvatar.setOnClickListener {
                // 预览
            }
            sivAvatar.setOnClickListener {
                isModify = true
                PictureSelectorUtil.openGalleryWithCrop(this@UserInfoActivity, avatarRequestCode)
            }
            sivNickName.setOnClickListener {
                isModify = true
                Router.toChangeUserInfoActivity(this@UserInfoActivity, changeNicknameRequestCode, TypeConst.type_modify_user_nick_name, "")
            }
            sivSex.setOnClickListener {
                isModify = true
                changeSex()
            }

        }
    }

    override fun initData() {
        userViewModel.getUserInfo()

        userViewModel.userInfoRes.observe(this) {
            SaveDataUtils.saveUserInfo(it)
            mDataBinding?.apply {
                ApplicationConst.setUserNickName(it.getNickName())
                ApplicationConst.setUserAvatar(it.avatar)
                UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("s" + it.id, it.avatar, it.getNickName(), it.quickblox_id, it.is_disturb))
                if (!TextUtils.isEmpty(it.avatar)) ImgLoader.display(mContext, it.avatar, rivAvatar)
                sivNickName.setTips(it.getNickName())
//                if (!TextUtils.isEmpty(it.phone)) {
//                    sivAccountReal.setContent(getString(R.string.user_phone))
//                    sivAccountReal.setTips(it.phone)
//                    sivAccount.setTips(it.phone)
//                } else {
//                    sivAccountReal.setContent(getString(R.string.user_email))
//                    sivAccountReal.setTips(it.email)
//                    sivAccount.setTips(it.email)

//                }
                sivSex.setTips(
                    when(it.sex) {
                        TypeConst.type_sex_male -> getString(R.string.user_male)
                        TypeConst.type_sex_female -> getString(R.string.user_female)
                        else -> ""
                    }
                )
            }
//            MMKVUtils.putString(KeyConst.key_user_info, GlobalGsonUtils.toJson(it))
        }

        userViewModel.avatarRes.observe(this) {
            if (!TextUtils.isEmpty(it.avatar)) {
                ImgLoader.display(mContext, it.avatar, mDataBinding?.rivAvatar)
            }
        }
        
        userViewModel.changeUserRes.observe(this) {
            ToastUtils.show(it.message)
            if (it.isSuccess) {
                userViewModel.getUserInfo()
            }

        }

    }
    
    private fun changeSex() {
        DialogUitl.showStringArrayDialog(mContext, arrayOf(
            R.string.user_male, R.string.user_female)) { text, tag ->
//            mDataBinding?.sivSex?.setTips(text)
            userViewModel.changeUserInfo(null, if (text == getString(R.string.user_female)) TypeConst.type_sex_female.toString() else TypeConst.type_sex_male.toString())
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                avatarRequestCode -> {
                    val selectPics = PictureSelector.obtainSelectorList(data)
                    if (selectPics.isNotEmpty()) {
                        val media = selectPics[0]
                        val sendPath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath

                        UploadUtil.startUpload {
                            it.upload(File(sendPath), object : UploadCallback {
                                override fun onProgress(progress: Int, url: String?, ossState: String) {
                                    if (progress >= 100 && !TextUtils.isEmpty(url)) {
                                        userViewModel.changeAvatar(1, url!!, ossState)
                                    }
                                }

                            })
                        }
                    }
                }
                changeNicknameRequestCode -> {
                    userViewModel.getUserInfo()
                }
            }
        }
    }

    override fun onDestroy() {
        if (isModify) LiveEventBus.get<Boolean>(EventKey.key_refresh_user_info).post(true)
        super.onDestroy()
    }
}