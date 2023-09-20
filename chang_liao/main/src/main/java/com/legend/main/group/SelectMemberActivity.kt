package com.legend.main.group

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ActivityManager
import com.legend.baseui.ui.util.DisplayUtils
import com.legend.baseui.ui.widget.susindexbar.suspension.SuspensionDecoration
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.common.widget.DialogUitl
import com.legend.imkit.popwindow.FriendListPopup
import com.legend.main.R
import com.legend.main.databinding.ActivityCreateGroupBinding
import com.legend.main.home.adapter.ConcatSimpleAdapter
import com.legend.main.network.viewmodel.FriendViewModel
import com.legend.main.network.viewmodel.GroupViewModel
import razerdp.util.animation.AlphaConfig
import razerdp.util.animation.AnimationHelper

@Route(path = RouterPath.path_select_member_activity)
class SelectMemberActivity: BaseActivity<ActivityCreateGroupBinding>() {
    private var concatAdapter: ConcatSimpleAdapter? = null
    private var suspensionDecoration: SuspensionDecoration? = null
    private val concatPersonData = mutableListOf<UserBean.ConcatSimple>()
    private val selectedData = mutableListOf<UserBean.ConcatSimple>()   // 被选中的数据
    private var imgSelectAllPeople: ImageView? = null
    private var allPeopleItem: UserBean.ConcatSimple? = null

    private var groupId: String? = ""
    private var opState: Int = 0
    private var friends: ArrayList<UserBean.GroupMember>? = null
    private val groupFriends: ArrayList<UserBean.ConcatSimple> = arrayListOf()
    private var maxSelectNum: Int = 0
    private var isGroupOwner: Boolean = false

    private var preSelectPosition: Int = -1

    private val friendViewModel: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_create_group

    override fun initView() {
        groupId = intent.getStringExtra(KeyConst.key_group_id)
        opState = intent.getIntExtra(KeyConst.key_group_inout_state, 0)
        friends = intent.getParcelableArrayListExtra(KeyConst.key_group_friend)
        maxSelectNum = intent.getIntExtra(KeyConst.key_select_max_num, 0)
        isGroupOwner = intent.getBooleanExtra(KeyConst.key_is_group_owner, false)

        mDataBinding?.apply {
            setConfigText(0)
            val layoutManager = LinearLayoutManager(mContext)
            recyclerView.layoutManager = layoutManager
            concatAdapter = ConcatSimpleAdapter(opState != TypeConst.state_pick_one_contact)
            suspensionDecoration = SuspensionDecoration(mContext, concatPersonData)
            suspensionDecoration?.setTitleMarginLeft(DisplayUtils.dp2px(mContext, 25f))
            recyclerView.addItemDecoration(suspensionDecoration!!)
            // 如果add两个，那么按照先后顺序，依次渲染
            recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
            recyclerView.adapter = concatAdapter
            concatAdapter?.setOnItemClickListener { adapter, view, position ->
                val headCount = suspensionDecoration?.headerViewCount?:0
                val item: UserBean.ConcatSimple = adapter.getItem(position) as UserBean.ConcatSimple
                if (opState == TypeConst.state_pick_one_contact) {
                    val intent = Intent()
                    intent.putExtra(KeyConst.key_pick_result_ids, item.id)
                    intent.putExtra(KeyConst.key_pick_result_name, item.nick_name)
                    setResult(RESULT_OK, intent)
                    finish()
                } else if (opState == TypeConst.state_single_chat) {
                    if (item.isSelected) {
                        item.isSelected = false
                        preSelectPosition = -1
                        selectedData.remove(item)
                    } else {
                        item.isSelected = true

                        if (preSelectPosition >= 0) {
                            selectedData[0].isSelected = false
                            adapter.notifyItemChanged(preSelectPosition)
                            selectedData.removeAt(0)
                        }
                        preSelectPosition = position
                        selectedData.add(item)
                    }
                    adapter.notifyItemChanged(position)
                    setConfigText(selectedData.size)
                } else {
                    if (item.isSelected) {
                        item.isSelected = false
                        selectedData.remove(item)

                        adapter.notifyItemChanged(position + headCount)
                        setConfigText(selectedData.size)
                    } else {
                        if (maxSelectNum > 0 && selectedData.size >= maxSelectNum) {
                            ToastUtils.show(String.format(getString(R.string.max_member_be_select), maxSelectNum))
                        } else {
                            item.isSelected = true
                            selectedData.add(item)

                            adapter.notifyItemChanged(position + headCount)
                            if (opState == TypeConst.state_group_mention && isGroupOwner) {
                                if (imgSelectAllPeople?.isSelected == true) {
                                    imgSelectAllPeople?.isSelected = false
                                    if (selectedData.isNotEmpty()) selectedData.removeAt(0)
                                }
                            }
                            setConfigText(selectedData.size)
                        }
                    }
                }
            }
            concatAdapter?.setOnItemLongClickListener { adapter, view, position ->
                val friendListPop = FriendListPopup(mContext)
                friendListPop.setAutoMirrorEnable(true)
                friendListPop.showAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.IN).toShow()
                friendListPop.dismissAnimation = AnimationHelper.asAnimation().withAlpha(AlphaConfig.OUT).toDismiss()
                friendListPop.popupGravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                friendListPop.listener = object : FriendListPopup.OnPopItemClickListener {
                    override fun onItemClicked(type: Int) {
                        if (type == FriendListPopup.TYPE_DELETE_FRIEND) {
                            DialogUitl.showSimpleDialog(mContext, getString(com.legend.imkit.R.string.delete_friend_tips)
                            ) { dialog, content ->
                                friendViewModel.deleteFriend((adapter.getItem(position) as UserBean.ConcatFriend).friend_id, position)
                            }
                        }
                    }
                }
                friendListPop.showPopupWindow(view)
                true
            }

            indexBar.setmPressedShowTextView(tvSideBarHint) // 设置HintTextView
                .setNeedRealIndex(true) // 设置需要真实的索引
                .setmLayoutManager(layoutManager)   // 设置recyclerview的layooutManager

            tvConfirm.setOnClickListener { confirmClicked() }
        }
    }

    private fun setConfigText(size: Int) {
        mDataBinding?.tvConfirm?.isEnabled = size > 0
        when(opState) {
            TypeConst.state_group_kit_out, TypeConst.state_group_remove_manager -> {
                mDataBinding?.tvConfirm?.text = if (size <= 0) getString(R.string.delete) else getString(R.string.delete) + "($size)"
            }

            TypeConst.state_single_chat -> {
                mDataBinding?.tvConfirm?.text = if (size <= 0)  getString(R.string.complete) else getString(R.string.complete)
            }
            
            else -> {
                mDataBinding?.tvConfirm?.text = if (size <= 0) getString(R.string.complete) else getString(R.string.complete) + "($size)"
            }
        }
    }

    private fun confirmClicked() {
        var ids = ""
//        var groupName = ApplicationConst.USER_NICK_NAME + "、"
        val size = selectedData.size
        when (opState) {
            TypeConst.state_group_kit_out -> {
                for ((index, data) in selectedData.withIndex()) {
                    ids += data.id
                    if (index < size -1) {
                        ids = "$ids,"
                    }
                }
                groupViewModel.kickOutGroup(groupId!!, ids)
            }

            TypeConst.state_group_invite -> {
                for ((index, data) in selectedData.withIndex()) {
                    ids += data.id
                    if (index < size -1) {
                        ids = "$ids,"
                    }
                }
                groupViewModel.inviteToGroup(groupId!!, ids)
            }

            TypeConst.state_group_add_manager -> {
                for ((index, data) in selectedData.withIndex()) {
                    ids += data.id
                    if (index < size -1) {
                        ids = "$ids,"
                    }
                }
                groupViewModel.opGroupManager(groupId!!, TypeConst.group_member_manager, ids)
            }

            TypeConst.state_group_remove_manager -> {
                for ((index, data) in selectedData.withIndex()) {
                    ids += data.id
                    if (index < size -1) {
                        ids = "$ids,"
                    }
                }
                groupViewModel.opGroupManager(groupId!!, TypeConst.group_member_normal, ids)
            }

            TypeConst.state_single_chat -> {
                Router.toChatActivity("s" + selectedData[0].id, selectedData[0].nick_name)
                toHomeMessagePage()
            }
            
            TypeConst.state_invite_group_call -> {
                LiveEventBus.get<List<UserBean.ConcatSimple>>(EventKey.key_group_call_selected).post(selectedData)
                finish()
            }

            TypeConst.state_group_mention -> {
                LiveEventBus.get<List<UserBean.ConcatSimple>>(EventKey.key_mention_selected).post(selectedData)
                finish()
            }

            else -> {
                if (size == 1) {
                    Router.toChatActivity("s" + selectedData[0].id, selectedData[0].nick_name)
                    toHomeMessagePage()
                } else {
                    for ((index, data) in selectedData.withIndex()) {
                        ids += data.id
//                        groupName += data.nick_name
                        if (index < size -1) {
                            ids = "$ids,"
//                            groupName = "$groupName、"
                        }
                    }
                    groupViewModel.createGroup(ids)
                }
            }
        }
    }

    private fun toHomeMessagePage() {
        LiveEventBus.get<Boolean>(EventKey.key_to_home_message_page).post(true)
        finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        if (friends != null && friends!!.isNotEmpty()) {
            for (friend in friends!!) {
                groupFriends.add(UserBean.ConcatSimple(friend.avatar?:"", friend.uid.toString(), friend.notes?:"null", friend.quickblox_id))
            }
        }
    
        when (opState) {
            TypeConst.state_group_kit_out, TypeConst.state_invite_group_call -> {
                setTitleBarTitleText(getString(R.string.group_member))
    
                concatPersonData.clear()
                concatPersonData.addAll(groupFriends)
                refreshData()
            }
            TypeConst.state_group_add_manager, TypeConst.state_group_remove_manager -> {
                setTitleBarTitleText(getString(R.string.group_manager))

                concatPersonData.clear()
                concatPersonData.addAll(groupFriends)
                refreshData()
            }
            TypeConst.state_single_chat -> {
                setTitleBarTitleText(getString(R.string.select_friend))
                friendViewModel.getFriends()
            }
            TypeConst.state_pick_one_contact -> {
                setTitleBarTitleText(getString(R.string.select_concat))
                mDataBinding?.viewBottom?.visibility = View.GONE
                friendViewModel.getFriends()
            }
            TypeConst.state_group_mention -> {
                setTitleBarTitleText(getString(R.string.select_mention_people))
                for (friend in groupFriends) {
                    if (friend.id == ApplicationConst.getUserId()) {
                        groupFriends.remove(friend)
                        break
                    }
                }
                concatPersonData.clear()
                concatPersonData.addAll(groupFriends)
                refreshData()

                if (isGroupOwner) {
                    allPeopleItem = UserBean.ConcatSimple("", "0", getString(R.string.all_people), "-1") // 所有人item
                    val headView = LayoutInflater.from(mContext).inflate(R.layout.header_select_member, null)
                    concatAdapter?.addHeaderView(headView)
                    suspensionDecoration?.headerViewCount = 1
                    imgSelectAllPeople = headView.findViewById(R.id.img_select)
                    headView.setOnClickListener {
                        imgSelectAllPeople?.let { it1 ->
                            if (it1.isSelected) {
                                it1.isSelected = false
                            } else {
                                if (selectedData.isNotEmpty()) {
                                    selectedData.clear()
                                    for (concatPerson in concatPersonData) {
                                        concatPerson.isSelected = false
                                    }
                                    concatAdapter?.notifyDataSetChanged()
                                }

                                it1.isSelected = true
                                selectedData.add(allPeopleItem!!)
                                setConfigText(selectedData.size)
                            }
                        }
                    }
                }
            }
            else -> {
                setTitleBarTitleText(getString(R.string.seal_select_group_member))
                friendViewModel.getFriends()
            }
        }

        groupViewModel.createGroupRes.observe(this) {
            if (it.isSuccess) {
                toHomeMessagePage()
            } else {
                ToastUtils.show(it.message)
            }
        }

        friendViewModel.friendListRes.observe(this) {
            concatPersonData.clear()

            val resGroupData = mutableListOf<UserBean.ConcatSimple>()
            for (res in it.friend_list[0].group_list) {
                if (res.friend_id != ApplicationConst.getUserId()) {
                    resGroupData.add(UserBean.ConcatSimple(res.avatar, res.friend_id, res.getFinalRemark(), res.quickblox_id))
                }
            }

            if (opState == TypeConst.state_group_invite) {
                resGroupData.removeAll(groupFriends)
            }

            concatPersonData.addAll(resGroupData)
            refreshData()
        }

        groupViewModel.inviteGroupRes.observe(this) {
            if (it.isSuccess) {
                LiveEventBus.get<Boolean>(EventKey.key_in_out_group).post(true)
                ActivityManager.getInstance().getActivity(GroupChatInfoActivity::class.java)?.finish()
                finish()
            }
        }
        
        groupViewModel.kickOutGroupRes.observe(this) {
            if (it.isSuccess) {
                LiveEventBus.get<Boolean>(EventKey.key_in_out_group).post(true)
                ActivityManager.getInstance().getActivity(GroupChatInfoActivity::class.java)?.finish()
                finish()
            }
        }

        groupViewModel.groupManagerRes.observe(this) {
            if (it.isSuccess) {
                LiveEventBus.get<Boolean>(EventKey.key_group_manager_change).post(true)
                finish()
            } else {
                ToastUtils.show(it.message)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        concatAdapter?.data = concatPersonData
        // 使用新的数据集合，改变原有数据集合内容。 注意：不会替换原有的内存引用，只是替换内容,导致没排序
//        concatAdapter?.setList(concatPersonData)
        concatAdapter?.notifyDataSetChanged()
        // 设置数据，会进行排序
        mDataBinding?.indexBar?.setmSourceDatas(concatPersonData)?.invalidate()
        suspensionDecoration?.setmDatas(concatPersonData)
    }


    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true

    }
}