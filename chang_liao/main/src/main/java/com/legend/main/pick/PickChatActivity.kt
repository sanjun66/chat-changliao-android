package com.legend.main.pick

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.KeyConst
import com.legend.common.Router
import com.legend.common.RouterPath
import com.legend.common.TypeConst
import com.legend.common.db.DbManager
import com.legend.common.db.entity.DBEntity
import com.legend.common.network.viewmodel.MsgViewModel
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.ActivityPickChatBinding
import kotlinx.coroutines.launch

@Route(path = RouterPath.path_pick_chat_activity)
class PickChatActivity: BaseActivity<ActivityPickChatBinding>() {
    private val REQUEST_CODE_GROUP = 100
    private val REQUEST_CODE_CONTACT = 101
    private var latestChatAdapter: LatestChatAdapter? = null
    private var msgIds = ""
    private var forwardType = TypeConst.forward_type_one_by_one

    val msgViewModel: MsgViewModel by lazy {
        ViewModelProvider(this)[MsgViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_pick_chat

    override fun initView() {
        mDataBinding?.apply {
            tvConcatPerson.setOnClickListener {
                Router.toSelectMemberActivity(this@PickChatActivity, REQUEST_CODE_CONTACT, state = TypeConst.state_pick_one_contact)
            }
            tvGroup.setOnClickListener {
                Router.toGroupListActivity(this@PickChatActivity, TypeConst.group_list_type_pick, REQUEST_CODE_GROUP)
            }

            latestChatAdapter = LatestChatAdapter()
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            recyclerView.adapter = latestChatAdapter
            latestChatAdapter?.setOnItemClickListener { adapter, view, position ->
                val item: DBEntity.ChatListEntity = adapter.getItem(position) as DBEntity.ChatListEntity
                DialogUitl.showSimpleDialog(mContext, String.format(getString(R.string.is_forward_message), item.nick_name)) { dialog, content ->
                    val toUid = item.session_id.substring(1, item.session_id.length)
                    if (item.session_id.startsWith("s")) {
                        msgViewModel.msgForward(msgIds, toUid, TypeConst.talk_type_single_chat, forwardType)
                    } else {
                        msgViewModel.msgForward(msgIds, toUid, TypeConst.talk_type_group_chat, forwardType)
                    }
                }
            }
        }
        setTitleBarTitleText(getString(com.legend.commonres.R.string.pick_one_chat))
    }

    override fun initData() {
        msgIds = intent.getStringExtra(KeyConst.key_message_ids)?:""
        forwardType = intent.getIntExtra(KeyConst.key_forward_type, TypeConst.forward_type_one_by_one)

        lifecycleScope.launch {
            val latestChatList = DbManager.getSoChatDB().chatListDao().getAllChatList()
            if (!latestChatList.isNullOrEmpty()) {
                latestChatAdapter?.setList(latestChatList)
            }
        }

        msgViewModel.msgForwardRes.observe(this) {
            ToastUtils.show(it.message)
            if (it.isSuccess) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }


    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode)  {
                REQUEST_CODE_GROUP -> {
                    data?.let {
                        val groupId = data.getStringExtra(KeyConst.key_pick_result_ids)
                        val name = data.getStringExtra(KeyConst.key_pick_result_name)
                        groupId?.let {
                            DialogUitl.showSimpleDialog(mContext, String.format(getString(R.string.is_forward_message), name)) { dialog, content ->
                                msgViewModel.msgForward(msgIds, groupId?:"", TypeConst.talk_type_group_chat, forwardType)
                            }
                        }
                    }
                }

                REQUEST_CODE_CONTACT -> {
                    data?.let {
                        val concatId = data.getStringExtra(KeyConst.key_pick_result_ids)
                        val name = data.getStringExtra(KeyConst.key_pick_result_name)
                        DialogUitl.showSimpleDialog(mContext, String.format(getString(R.string.is_forward_message), name)) { dialog, content ->
                            msgViewModel.msgForward(msgIds, concatId?:"", TypeConst.talk_type_single_chat, forwardType)
                        }
                    }
                }
            }
        }
    }
}