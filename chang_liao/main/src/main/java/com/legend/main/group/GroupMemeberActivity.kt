package com.legend.main.group

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.KeyConst
import com.legend.common.RouterPath
import com.legend.common.bean.UserBean
import com.legend.common.db.entity.DBEntity
import com.legend.common.utils.ChatUtil
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.main.R
import com.legend.main.databinding.ActivityRecyclerViewBinding
import com.legend.main.group.adapter.GroupMemberAdapter
import com.legend.main.network.viewmodel.GroupViewModel

@Route(path = RouterPath.path_group_member_activity)
class GroupMemberActivity: BaseActivity<ActivityRecyclerViewBinding>() {
    private var groupMemberAdapter: GroupMemberAdapter? = null
    private var isGroupOwner = false
    private var groupId: String = ""
    private val groupMemberList = mutableListOf<UserBean.GroupMember>()
    private var groupInfo: UserBean.GroupInfo? = null

    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_recycler_view

    override fun initView() {
        mDataBinding?.apply {
            val layoutManager = GridLayoutManager(mContext, 5)
            recyclerView.layoutManager = layoutManager
            groupMemberAdapter = GroupMemberAdapter()
            groupMemberAdapter?.data = groupMemberList
            recyclerView.adapter = groupMemberAdapter

            groupMemberAdapter?.setOnItemClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as UserBean.GroupMember
                ChatUtil.toUserActivity(item.uid.toString() == ApplicationConst.getUserId(), item.uid.toString(), groupId)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        groupId = intent.getStringExtra(KeyConst.key_group_id)?:""
        groupViewModel.getGroupInfo(groupId)
        groupViewModel.groupInfoRes.observe(this) {
            if (it.isSuccess) {
                val groupRes = it.groupInfo!!
                isGroupOwner = groupRes.group_info?.uid == ApplicationConst.getUserId()
                groupInfo = groupRes.group_info
                groupInfo?.let { it1 ->
                    UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("g$groupId", it1.avatar, it1.name, is_disturb = it1.is_disturb))
                }
                if (groupRes.group_member != null && groupRes.group_member!!.isNotEmpty()) {
                    groupMemberList.clear()
                    setTitleBarTitleText(getString(R.string.group_member) + "(${groupRes.group_member?.size})")

                    groupMemberList.addAll(groupRes.group_member!!)
                    groupMemberAdapter?.notifyDataSetChanged()
                }
            } else {
                ToastUtils.show(it.errorMsg?:"")
            }
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}