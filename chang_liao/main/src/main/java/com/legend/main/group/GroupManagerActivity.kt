package com.legend.main.group

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.common.utils.ChatUtil
import com.legend.main.R
import com.legend.main.databinding.ActivityGroupManagerBinding
import com.legend.main.group.adapter.GroupMemberAdapter
import com.legend.main.network.viewmodel.GroupViewModel

@Route(path = RouterPath.path_group_manager_activity)
class GroupManagerActivity: BaseActivity<ActivityGroupManagerBinding>() {
    private var groupMemberAdapter: GroupMemberAdapter? = null
    private val groupMemberList = mutableListOf<UserBean.GroupMember>()
    private val groupManagerList = mutableListOf<UserBean.GroupMember>()    // 管理员列表
    private var groupId: String = ""
    private var maxManagerNum = 1
    private var myRole = TypeConst.group_member_normal
    private val addItem = UserBean.GroupMember(R.drawable.profile_ic_grid_member_add.toString(), "", TypeConst.state_group_invite, 0, "0", "0")
    private val deleteIem = UserBean.GroupMember(R.drawable.profile_ic_grid_member_delete.toString(), "", TypeConst.state_group_kit_out, 0, "0", "0")
    private var mySelfItem: UserBean.GroupMember? = null


    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_group_manager

    override fun initView() {
        mDataBinding?.apply {
            val layoutManager = GridLayoutManager(mContext, 5)
            recyclerView.layoutManager = layoutManager
            groupMemberAdapter = GroupMemberAdapter()
            groupMemberAdapter?.data = groupManagerList
            recyclerView.adapter = groupMemberAdapter

            groupMemberAdapter?.setOnItemClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as UserBean.GroupMember
                when (item.uid) {
                    TypeConst.state_group_invite -> {
                        val members = arrayListOf<UserBean.GroupMember>()
                        members.addAll(groupMemberList)
                        members.removeAll(groupManagerList.toSet())

                        val maxNum = maxManagerNum - if (groupManagerList.contains(addItem)) groupManagerList.size - 2 else groupManagerList.size
                        if (maxNum <= 0) {
                            ToastUtils.show(getString(R.string.group_manager_max_tip))
                            return@setOnItemClickListener
                        }
                        Router.toSelectMemberActivity(this@GroupManagerActivity, 0, groupId, TypeConst.state_group_add_manager, members, maxNum)
                    }
                    TypeConst.state_group_kit_out -> {
                        val members = arrayListOf<UserBean.GroupMember>()
                        members.addAll(groupManagerList)
                        members.remove(mySelfItem)
                        members.remove(addItem)
                        members.remove(deleteIem)
                        if (members.size <= 0) {
                            ToastUtils.show(getString(R.string.group_manager_min_tip))
                            return@setOnItemClickListener
                        }
                        Router.toSelectMemberActivity(this@GroupManagerActivity, 0, groupId, TypeConst.state_group_remove_manager, members)
                    }
                    else -> {
//                        ChatUtil.toUserActivity(item.uid.toString() == ApplicationConst.USER_ID, item.uid.toString(), groupId)
                    }
                }
            }

        }

        setTitleBarTitleText(getString(R.string.group_manager))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        groupId = intent.getStringExtra(KeyConst.key_group_id)?: ""

        groupViewModel.getGroupInfo(groupId)
        groupViewModel.groupInfoRes.observe(this) {
            if (it.isSuccess) {
                val groupInfoRes = it.groupInfo!!
                maxManagerNum = groupInfoRes.group_info!!.max_manager
                mDataBinding?.tvTips?.text = groupInfoRes.group_info!!.manager_explain

                if (groupInfoRes.group_member != null && groupInfoRes.group_member!!.isNotEmpty()) {
                    groupMemberList.clear()
                    groupManagerList.clear()
                    for (member in groupInfoRes.group_member!!) {
                        if (member.uid.toString() == ApplicationConst.getUserId()) {
                            mySelfItem = member
                            myRole = member.role?:TypeConst.group_member_normal
                        }

                        if (member.role == TypeConst.group_member_manager) {
                            groupManagerList.add(member)
                        }
                    }

                    groupMemberList.addAll(groupInfoRes.group_member!!)
                    if (myRole == TypeConst.group_member_master) {
                        groupManagerList.add(addItem)
                        groupManagerList.add(deleteIem)
                    }
                    groupMemberAdapter?.notifyDataSetChanged()
                }

            } else {
                ToastUtils.show(it.errorMsg?:"")
            }
        }

        LiveEventBus.get<Boolean>(EventKey.key_group_manager_change).observe(this) {
            groupViewModel.getGroupInfo(groupId)
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}