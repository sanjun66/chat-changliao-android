package com.legend.main.activity

import android.app.Dialog
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.ActivityQrGroupIdentifyBinding
import com.legend.main.network.viewmodel.GroupViewModel

@Route(path = RouterPath.path_qrcode_group_identify)
class QrGroupIdentifyActivity: BaseActivity<ActivityQrGroupIdentifyBinding>() {
    private var uid: String = ""
    private var groupId: String = ""
    private var groupName: String = ""

    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_qr_group_identify

    override fun initView() {
        uid = intent.getStringExtra(KeyConst.key_user_id)?:""
        groupId = intent.getStringExtra(KeyConst.key_group_id)?:""
        groupName = intent.getStringExtra(KeyConst.key_group_name)?:""

        DialogUitl.showSimpleDialog(this, String.format(getString(com.legend.commonres.R.string.add_group_tips), groupName), object : DialogUitl.SimpleCallback2 {
            override fun onConfirmClick(dialog: Dialog?, content: String?) {
                groupViewModel.scanJoinGroup(groupId, uid)
            }

            override fun onCancelClick() {
                finish()
            }

        })

        groupViewModel.scanQrRes.observe(this) {
            if (!it.isSuccess) {
                ToastUtils.show(it.message)
                finish()
            }
        }
    }


    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.backgroundColor = resources.getColor(com.com.legend.ui.R.color.transparent)
    }
}