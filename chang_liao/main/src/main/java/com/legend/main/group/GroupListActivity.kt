package com.legend.main.group

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.main.R
import com.legend.main.databinding.ActivityGroupListBinding
import com.legend.main.group.adapter.GroupListAdapter
import com.legend.main.network.viewmodel.GroupViewModel

@Route(path = RouterPath.path_group_list_activity)
class GroupListActivity: BaseActivity<ActivityGroupListBinding>() {

    private var type: Int = TypeConst.group_list_type_show
    private var adapter: GroupListAdapter? = null
    private val groupList = mutableListOf<UserBean.Group>()

    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_group_list

    override fun initView() {
        mDataBinding?.apply {
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            adapter = GroupListAdapter()
            recyclerView.adapter = adapter
            adapter?.setOnItemClickListener { adapter, view, position ->
                if (type == TypeConst.group_list_type_pick) {
                    val item = adapter.getItem(position) as UserBean.Group
                    val intent = Intent()
                    intent.putExtra(KeyConst.key_pick_result_ids, item.id )
                    intent.putExtra(KeyConst.key_pick_result_name, item.name)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    val item = adapter.getItem(position) as UserBean.Group
                    Router.toChatActivity("g" + item.id, item.name)
                }
            }
        }

        type = intent.getIntExtra(KeyConst.key_group_list_type, TypeConst.group_list_type_show)
        if (type == TypeConst.group_list_type_pick) {
            setTitleBarTitleText(getString(R.string.pick_group))
        } else {
            setTitleBarTitleText(getString(R.string.group))
        }
    }

    override fun initData() {
        initEvent()
        groupViewModel.getGroupList()

        groupViewModel.groupListRes.observe(this) {
            groupList.clear()
            groupList.addAll(it.group_list)
            adapter?.setList(groupList)
        }
    }

    private fun initEvent() {
        LiveEventBus.get<UserBean.GroupInfo>(EventKey.key_modify_group_info).observe(this) {
            val position = findGroupById(it.id.toString())
            if (position >= 0) {
                val group = groupList[position]
                group.name = it.name?:""
                group.avatar = it.avatar?:""
                adapter?.notifyItemChanged(position)
            }
        }
    }

    private fun findGroupById(id: String): Int {
        for ((index, item) in groupList.withIndex()) {
            if (item.id == id) {
                return index
            }
        }

        return -1
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }


}