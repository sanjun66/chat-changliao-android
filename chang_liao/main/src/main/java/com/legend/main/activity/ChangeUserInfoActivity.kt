package com.legend.main.activity

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.main.R
import com.legend.main.databinding.ActivityChangeUserInfoBinding
import com.legend.main.network.viewmodel.GroupViewModel

/**
 *
 * @Date: 2023/7/10 21:56
 */
@Route(path = RouterPath.path_change_user_info_activity)
class ChangeUserInfoActivity: BaseActivity<ActivityChangeUserInfoBinding>() {
    private var type: Int? = -1
    private var mId: String = ""

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    
    override fun getLayoutId() = R.layout.activity_change_user_info
    
    override fun initView() {
//        initTitleBar()
        mDataBinding?.apply {
            tvSave.setOnClickListener {
                saveClicked()
            }
            tvContent.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrEmpty() || s.isBlank()) {
                        tvTips.text = "0/20"
                    } else {
                        tvTips.text = s.length.toString() + "/20"
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    override fun initData() {
        type = intent.getIntExtra(TypeConst.change_info_type, TypeConst.type_modify_user_nick_name)
        mId = intent.getStringExtra(KeyConst.key_user_id)?:""
        when (type) {
            TypeConst.type_modify_user_nick_name -> {
                setTitleBarTitleText(getString(R.string.modify_nick_name))
                mDataBinding?.tvContent?.hint = getString(R.string.modify_nick_name_hint)
            }
            TypeConst.type_modify_group_name -> {
                setTitleBarTitleText(getString(R.string.modify_group_name))
                mDataBinding?.tvContent?.hint = getString(R.string.modify_group_name_hint)
            }
            TypeConst.type_modify_note_name -> {
                setTitleBarTitleText(getString(R.string.modify_note_name))
                mDataBinding?.tvContent?.hint = getString(R.string.set_note_name_hint)
            }
        }

        userViewModel.changeUserRes.observe(this) {
            if (it.isSuccess) {
                ToastUtils.show(getString(com.legend.commonres.R.string.success))
                setResult(RESULT_OK)
                finish()
            }
        }

        groupViewModel.modifyGroupInfoRes.observe(this) { it ->
            if (it.isSuccess) {
                ToastUtils.show(getString(com.legend.commonres.R.string.success))
                setResult(RESULT_OK)
                finish()
            }
        }

        userViewModel.changeNoteNameRes.observe(this) {
            if (it.isSuccess) {
                ToastUtils.show(getString(com.legend.commonres.R.string.success))
                setResult(RESULT_OK)
                finish()
            }
        }

    }
    
    private fun initTitleBar() {
        setTitleBarRightText(getString(R.string.confirm))
        setTitleBarRightTextColor("#27DF99")
    }
    
    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    private fun saveClicked() {
        if (TextUtils.isEmpty(mDataBinding?.tvContent?.text) && type != TypeConst.type_modify_note_name) {
            when (type) {
                TypeConst.type_modify_user_nick_name -> ToastUtils.show(getString(R.string.nick_name_cannot_empty))
                TypeConst.type_modify_group_name -> ToastUtils.show(getString(R.string.group_name_cannot_empty))
//                TypeConst.type_modify_note_name -> ToastUtils.show(getString(R.string.note_name_cannot_empty))
            }
            return
        }
        when(type) {
            TypeConst.type_modify_user_nick_name -> userViewModel.changeUserInfo(mDataBinding?.tvContent?.text!!.toString(), null)
            TypeConst.type_modify_group_name -> groupViewModel.modifyGroupInfo(mId, mDataBinding?.tvContent?.text!!.toString(), null, null, null)
            TypeConst.type_modify_note_name -> userViewModel.changeNoteName(mId, mDataBinding?.tvContent?.text!!.toString())
        }
    }
    override fun onTitleBarRightClick() {
        // nothing
    }
}