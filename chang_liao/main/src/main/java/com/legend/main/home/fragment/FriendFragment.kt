package com.legend.main.home.fragment

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.BaseFragment
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.widget.susindexbar.suspension.SuspensionDecoration
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.ApplicationConst
import com.legend.common.EventKey
import com.legend.common.Router
import com.legend.common.TypeConst
import com.legend.common.bean.UserBean
import com.legend.common.utils.ChatUtil
import com.legend.imkit.popwindow.FriendListPopup
import com.legend.main.R
import com.legend.main.databinding.FragmentFriendBinding
import com.legend.main.friends.ApplyListActivity
import com.legend.main.home.adapter.ConcatPersonAdapter
import com.legend.main.network.viewmodel.FriendViewModel
import razerdp.util.animation.AlphaConfig
import razerdp.util.animation.AnimationHelper

class FriendFragment: BaseFragment<FragmentFriendBinding>() {
    private var concatAdapter: ConcatPersonAdapter? = null
    private var suspensionDecoration: SuspensionDecoration? = null
    private val concatPersonData = mutableListOf<UserBean.ConcatFriend>()
    private var tvNewFriendDot: TextView? = null

    private val viewModel:FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_friend

    override fun initView(view: View?) {
        mDataBinding?.apply {
            val layoutManager = LinearLayoutManager(mContext)
            recyclerView.layoutManager = layoutManager
            concatAdapter = ConcatPersonAdapter()
            suspensionDecoration = SuspensionDecoration(mContext, concatPersonData)
            suspensionDecoration?.setTitleMarginLeft(DisplayUtils.dp2px(mContext, 25f))
            recyclerView.addItemDecoration(suspensionDecoration!!)
            // 如果add两个，那么按照先后顺序，依次渲染
            recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
            recyclerView.adapter = concatAdapter
            initHeader()
            concatAdapter?.setOnItemClickListener { adapter, view, position ->
                val item: UserBean.ConcatFriend = adapter.getItem(position) as UserBean.ConcatFriend
                ChatUtil.toUserActivity(item.friend_id == ApplicationConst.getUserId(), item.friend_id, "")
//                val item: UserBean.ConcatFriend = adapter.getItem(position) as UserBean.ConcatFriend
//                Router.toChatActivity("s"+item.friend_id, item.remark)
            }
//            concatAdapter?.addChildClickViewIds(R.id.riv_avatar)
//            concatAdapter?.setOnItemChildClickListener { adapter, view, position ->
//                val item: UserBean.ConcatFriend = adapter.getItem(position) as UserBean.ConcatFriend
//                ChatUtil.toUserActivity(item.friend_id == ApplicationConst.USER_ID, item.friend_id, "")
//            }
            concatAdapter?.setOnItemLongClickListener { adapter, view, position ->
                val friendListPop = FriendListPopup(requireContext())
                friendListPop.setAutoMirrorEnable(true)
                friendListPop.showAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.IN).toShow()
                friendListPop.dismissAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.OUT).toDismiss()
                friendListPop.popupGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                friendListPop.listener = object : FriendListPopup.OnPopItemClickListener {
                    override fun onItemClicked(type: Int) {
                        if (type == FriendListPopup.TYPE_DELETE_FRIEND) {
                            viewModel.deleteFriend((adapter.getItem(position) as UserBean.ConcatFriend).friend_id, position)
                        }
                    }
                }
                friendListPop.showPopupWindow(view)
                true
            }

            indexBar.setmPressedShowTextView(tvSideBarHint) // 设置HintTextView
                .setNeedRealIndex(true) // 设置需要真实的索引
                .setmLayoutManager(layoutManager)   // 设置recyclerview的layooutManager
        }
    }

    private fun initHeader() {
        val headerView = LayoutInflater.from(context).inflate(R.layout.header_fragment_friend, null)
        concatAdapter?.addHeaderView(headerView)
        tvNewFriendDot = headerView.findViewById(R.id.new_friend_red_dot)
        headerView.findViewById<LinearLayout>(R.id.llt_group).setOnClickListener {
            Router.toGroupListActivity(requireActivity(), TypeConst.group_list_type_show, 100)
        }
        headerView.findViewById<LinearLayout>(R.id.llt_new_friend).setOnClickListener {
//            setFriendDotShow(false)
            Router.toApplyListActivity()
        }
        suspensionDecoration?.headerViewCount = 1
    }

    override fun initData() {
        initEvent()

        viewModel.friendListRes.observe(this) {
            concatPersonData.clear()
            for (data in it.friend_list[0].group_list) {
                concatPersonData.add(data)
            }
            concatAdapter?.data = concatPersonData
            // 使用新的数据集合，改变原有数据集合内容。 注意：不会替换原有的内存引用，只是替换内容,导致没排序
//        concatAdapter?.setList(concatPersonData)
            concatAdapter?.notifyDataSetChanged()
            // 设置数据，会进行排序
            mDataBinding?.indexBar?.setmSourceDatas(concatPersonData)?.invalidate()
            suspensionDecoration?.setmDatas(concatPersonData)
        }

        viewModel.applyList.observe(this) {
            var num = 0
            for (item in it.apply_list) {
                if (item.state == 1 && item.flag == "taker") {
                    num++
                    break
                }
            }
            setFriendDotShow(num)
        }

        viewModel.friendDeleteRes.observe(this) {
            if (it >= 0) {
//                val deleteItem = concatAdapter?.getItem(it) as UserBean.ConcatFriend
                concatAdapter?.removeAt(it)
//                ChatUtil.deleteFriend(requireContext(), deleteItem.friend_id.toString())
            } else {
                ToastUtils.show(getString(R.string.delete_fail))
            }
        }
    }

    private fun setFriendDotShow(num: Int) {
        if (num > 0) {
            tvNewFriendDot?.visibility = View.VISIBLE
            tvNewFriendDot?.text = num.toString()
        } else {
            tvNewFriendDot?.visibility = View.GONE
        }
        LiveEventBus.get<Int>(EventKey.key_friend_show_dot).post(num)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateFriends() {
        viewModel.getFriends()
        viewModel.getFriendApplyList()
    }

    private fun initEvent() {
        LiveEventBus.get<Boolean>(EventKey.key_add_friend).observe(this, object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                if (value)  updateFriends()
            }
        })

        // 有好友申请
        LiveEventBus.get<Boolean>(EventKey.key_have_friend_apply).observe(this) {
            if (ActivityManager.getInstance().topActivityClass != ApplyListActivity::class.java) {
                viewModel.getFriendApplyList()
            }
        }

        // 从申请列表页面回来
        LiveEventBus.get<Boolean>(EventKey.key_apply_friend_activity_destroy).observe(this) {
            if (it) viewModel.getFriendApplyList()
        }
    }

    override fun onResume() {
        super.onResume()
        //  每次切换到都会运行到onResume
        updateFriends()
    }

}