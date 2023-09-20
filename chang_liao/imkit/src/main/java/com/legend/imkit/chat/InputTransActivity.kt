package com.legend.imkit.chat

import android.app.Dialog
import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.network.viewmodel.MsgViewModel
import com.legend.common.widget.DialogUitl
import com.legend.imkit.R
import com.legend.imkit.databinding.ActivityTransBinding

@Route(path = RouterPath.path_trans_activity)
class InputTransActivity: BaseActivity<ActivityTransBinding>() {

    private var mId: String? = ""
    private var msgPwd: String? = ""
    private var msgMsg: String? = ""
    private var pageType: Int = TypeConst.trans_input_page_type_secret
    private var itemPosition: Int = 0
    private var curDialog: Dialog? = null

    val msgViewModel: MsgViewModel by lazy {
        ViewModelProvider(this)[MsgViewModel::class.java]
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.backgroundColor = resources.getColor(com.com.legend.ui.R.color.transparent)
    }
    override fun getLayoutId() = R.layout.activity_trans

    override fun initData() {
        super.initData()
        msgViewModel.decryMsgRes.observe(this) {
            curDialog?.dismiss()
            val intent = Intent()
            intent.putExtra(KeyConst.key_id, mId)
            intent.putExtra(KeyConst.key_secret_msg_content, it)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun initView() {
        mId = intent.getStringExtra(KeyConst.key_id)
        msgPwd = intent.getStringExtra(KeyConst.key_secret_pwd)
        msgMsg = intent.getStringExtra(KeyConst.key_secret_msg_content)
        pageType = intent.getIntExtra(KeyConst.key_page_type, TypeConst.trans_input_page_type_secret)
        itemPosition = intent.getIntExtra(KeyConst.key_item_position, 0)

        when(pageType) {
            TypeConst.trans_input_page_type_secret -> showSecretDlg()

            TypeConst.trans_input_page_type_add_friend, TypeConst.trans_input_page_type_refuse -> showOpFriendReasonDlg()
        }

    }

    private fun showOpFriendReasonDlg() {
        val title = if (pageType == TypeConst.trans_input_page_type_add_friend) getString(R.string.add_friend_title) else getString(R.string.refuse_friend_title)
        val hint = if (pageType == TypeConst.trans_input_page_type_add_friend) getString(R.string.add_friend_hint) else getString(R.string.refuse_friend_hint)
        DialogUitl.showSimpleInputDialog(this, title, hint, DialogUitl.INPUT_TYPE_TEXT,100, false, object :
            DialogUitl.SimpleCallback2 {
            override fun onConfirmClick(dialog: Dialog?, content: String?) {
                dialog?.dismiss()
                val intent = Intent()
                intent.putExtra(KeyConst.key_id, mId)
                intent.putExtra(KeyConst.key_input_content, content?:"")
                intent.putExtra(KeyConst.key_item_position, itemPosition)
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun onCancelClick() {
                finish()
            }

        })

    }

    private fun showSecretDlg() {
        DialogUitl.showSimpleInputDialog(this, getString(R.string.secret_pwd), getString(R.string.input_secret_pwd), DialogUitl.INPUT_TYPE_VARIATION_PASSWORD,16, false, object :
            DialogUitl.SimpleCallback2 {
            override fun onConfirmClick(dialog: Dialog?, content: String?) {
                if (TextUtils.isEmpty(content) || content!!.length < 4 || content.length > 16) {
                    ToastUtils.show(getString(R.string.secret_pwd_tip))
                    return
                }

                if (!TextUtils.isEmpty(msgMsg)) {
                    if (content.trim() == msgPwd) {
                        dialog?.dismiss()
                        val intent = Intent()
                        intent.putExtra(KeyConst.key_id, mId)
                        intent.putExtra(KeyConst.key_secret_msg_content, msgMsg)
                        intent.putExtra(KeyConst.key_secret_msg_uuid, mId)
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        ToastUtils.show(getString(R.string.input_correct_secret_pwd))
                    }

                    return
                }

                if (TextUtils.isEmpty(mId)) {
                    dialog?.dismiss()
                    val intent = Intent()
                    intent.putExtra(KeyConst.key_secret_pwd, content)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    curDialog = dialog
                    msgViewModel.msgDecrypt(mId!!, content)
                }
            }

            override fun onCancelClick() {
                finish()
            }

        })
    }
}