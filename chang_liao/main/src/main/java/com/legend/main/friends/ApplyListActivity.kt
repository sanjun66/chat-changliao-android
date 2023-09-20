package com.legend.main.friends

import android.content.Intent
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.main.R
import com.legend.main.databinding.ActivityApplyListBinding
import com.legend.main.friends.adapter.ApplyListAdapter
import com.legend.main.friends.adapter.OnCheckListener
import com.legend.main.network.viewmodel.FriendViewModel

@Route(path = RouterPath.path_apply_list_activity)
class ApplyListActivity: BaseActivity<ActivityApplyListBinding>() {
    private val REQUEST_CODE_REFUSE = 100
    private var adapter: ApplyListAdapter? = null

    private val viewModel: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_apply_list

    override fun initView() {
        setTitleBarTitleText(getString(R.string.new_friend))
        mDataBinding?.apply {
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            adapter = ApplyListAdapter()
            recyclerView.adapter = adapter
            adapter?.listener = object : OnCheckListener {
                override fun onCheckApply(id: Int, state: Int, processMsg: String, position: Int) {
                    if (state == 3) {
                        Router.toInputTransActivity(this@ApplyListActivity, REQUEST_CODE_REFUSE, TypeConst.trans_input_page_type_refuse, id.toString(), itemPosition = position)
                    } else {
                        viewModel.checkApply(id, state, processMsg, position)
                    }
                }
            }
            adapter?.setOnItemClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as UserBean.FriendApply
                Router.toUserActivity(if (item.uid == ApplicationConst.getUserId()) item.friend_id else item.uid, "")
            }
        }
    }

    override fun initData() {
        initEvent()
        viewModel.getFriendApplyList()
        viewModel.applyList.observe(this) {
            adapter?.setList(it.apply_list)
        }

        viewModel.checkApplyRes.observe(this) {
            adapter?.data?.apply {
                for ((i, item) in adapter?.data!!.withIndex()) {
                    if (item.id == it.id) {
                        item.state = it.state
                        adapter?.notifyItemChanged(i)
                    }
                }
                LiveEventBus.get<Boolean>(EventKey.key_add_friend).post(true)
            }
        }
    }

    private fun initEvent() {
        LiveEventBus.get<Boolean>(EventKey.key_have_friend_apply).observe(this) {
            viewModel.getFriendApplyList()
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
                REQUEST_CODE_REFUSE -> {
                    val id = data?.getStringExtra(KeyConst.key_id)?:""
                    val inputReason = data?.getStringExtra(KeyConst.key_input_content)?:""
                    val position = data?.getIntExtra(KeyConst.key_item_position, 0)?:0
                    viewModel.checkApply(if (TextUtils.isEmpty(id)) 0 else id.toInt(), 3, inputReason, position)
                }
            }
        }
    }

    override fun onDestroy() {
        LiveEventBus.get<Boolean>(EventKey.key_apply_friend_activity_destroy).post(true)
        super.onDestroy()
    }
}