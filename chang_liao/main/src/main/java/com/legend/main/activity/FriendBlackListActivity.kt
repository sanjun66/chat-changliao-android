package com.legend.main.activity

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.RouterPath
import com.legend.common.bean.UserBean
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.main.R
import com.legend.main.adapter.FriendBlackAdapter
import com.legend.main.databinding.ActivitySimpleRvlistBinding
import com.legend.main.dialog.OneTextPopup

@Route(path = RouterPath.path_friend_black_list_activity)
class FriendBlackListActivity: BaseActivity<ActivitySimpleRvlistBinding>() {
    private var adapter: FriendBlackAdapter? = null

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_simple_rvlist

    override fun initView() {
        mDataBinding?.apply {
            recyclerView.layoutManager = LinearLayoutManager(mContext)
            adapter = FriendBlackAdapter()
            recyclerView.adapter = adapter

            adapter?.setOnItemLongClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as UserBean.FriendBlack
                val onTextPop = OneTextPopup(mContext, getString(R.string.remove_black))
                onTextPop.listener = object: OneTextPopup.OnTextClickListener {
                    override fun onTextClicked() {
                        userViewModel.removeFriendBlack(item.friend_id, item)
                    }
                }
                onTextPop.showPopupWindow(view)
                true
            }
        }
    }

    override fun initData() {
        setTitleBarTitleText(getString(R.string.setting_black_list))
        userViewModel.getBlackFriends()

        userViewModel.blackListRes.observe(this) {
            adapter?.setList(it.friend_black)
        }
        userViewModel.removeBlackRes.observe(this) {
            if (it.isSuccess) {
                ToastUtils.show(it.message?:"")
                if (it.blackFriend != null) adapter?.remove(it.blackFriend!!)
            }
        }
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }
}