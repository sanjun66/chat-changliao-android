package com.legend.main.group

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.common.db.entity.DBEntity
import com.legend.common.upload.UploadCallback
import com.legend.common.upload.UploadUtil
import com.legend.common.utils.ChatUtil
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.common.utils.picture.PictureSelectorUtil
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.ActivityGroupChatInfoBinding
import com.legend.main.group.adapter.GroupMemberAdapter
import com.legend.main.network.viewmodel.GroupViewModel
import com.legend.main.util.GroupOpUtil
import com.luck.picture.lib.basic.PictureSelector
import java.io.File

@Route(path = RouterPath.path_group_chat_info_activity)
class GroupChatInfoActivity: BaseActivity<ActivityGroupChatInfoBinding>() {
    private val avatarRequestCode = 200
    private val changeNicknameRequestCode = 201
    private var groupId: String = ""
    private var groupMemberAdapter: GroupMemberAdapter? = null
    private val maxMemberShow = 20
    private val addItem = UserBean.GroupMember(R.drawable.profile_ic_grid_member_add.toString(), "", TypeConst.state_group_invite, 0, "0", "0")
    private val deleteIem = UserBean.GroupMember(R.drawable.profile_ic_grid_member_delete.toString(), "", TypeConst.state_group_kit_out, 0, "0", "0")
    private val groupMemberList = mutableListOf<UserBean.GroupMember>()
    private val groupManagerList = mutableListOf<UserBean.GroupMember>()    // 管理员列表
    private var groupInfo: UserBean.GroupInfo? = null
    private var isModified = false
    private var myRole = TypeConst.group_member_normal

    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_group_chat_info

    override fun initView() {
        mDataBinding?.apply {
            val layoutManager = GridLayoutManager(mContext, 5)
            recyclerView.layoutManager = layoutManager
            groupMemberAdapter = GroupMemberAdapter()
            groupMemberAdapter?.data = groupMemberList
            recyclerView.adapter = groupMemberAdapter

            groupMemberAdapter?.setOnItemClickListener { adapter, view, position ->
                val item = adapter.getItem(position) as UserBean.GroupMember
                if (item.uid == TypeConst.state_group_invite) {
                    val members = arrayListOf<UserBean.GroupMember>()
                    members.addAll(groupMemberList)
                    members.remove(addItem)
                    members.remove(deleteIem)
                    Router.toSelectMemberActivity(this@GroupChatInfoActivity, 0, groupId, TypeConst.state_group_invite, members)
                } else if (item.uid == TypeConst.state_group_kit_out) {
                    val members = arrayListOf<UserBean.GroupMember>()
                    members.addAll(groupMemberList)
                    if (myRole == TypeConst.group_member_manager) {
                        members.removeAll(groupManagerList.toSet())
                    }
                    members.remove(addItem)
                    members.remove(deleteIem)
                    val mySelf = findFriendById(ApplicationConst.getUserId().toInt())
                    mySelf?.let { members.remove(mySelf) }
                    Router.toSelectMemberActivity(this@GroupChatInfoActivity, 0, groupId, TypeConst.state_group_kit_out, members)
                } else {
                    ChatUtil.toUserActivity(item.uid.toString() == ApplicationConst.getUserId(), item.uid.toString(), groupId)
                }
            }
            tvShowMore.setOnClickListener {
                Router.toGroupMemberActivity(groupId)
            }
            tvDelete.setOnClickListener {
                if (myRole == TypeConst.group_member_master) {
                    DialogUitl.showSimpleDialog(mContext, getString(R.string.is_dissolve_group)) { dialog, content ->
                        groupViewModel.dissolveGroup(groupId)
                    }
                } else {
                    DialogUitl.showSimpleDialog(mContext, getString(R.string.is_exit_group)) { dialog, content ->
                        groupViewModel.exitGroup(groupId)
                    }
                }
            }
            sivGroupName.setOnClickListener {
                if (myRole == TypeConst.group_member_master || myRole == TypeConst.group_member_manager) {
                    Router.toChangeUserInfoActivity(this@GroupChatInfoActivity, changeNicknameRequestCode, TypeConst.type_modify_group_name, groupId)
                } else {
                    ToastUtils.show(getString(R.string.group_host_can_modify))
                }
            }
            rivAvatar.setOnClickListener {
                if (myRole == TypeConst.group_member_master || myRole == TypeConst.group_member_manager) {
                    PictureSelectorUtil.openGalleryWithCrop(this@GroupChatInfoActivity, avatarRequestCode)
                } else {
                    ToastUtils.show(getString(R.string.group_host_can_modify))
                }
            }
            sivGroupAvatar.setOnClickListener {
                if (myRole == TypeConst.group_member_master || myRole == TypeConst.group_member_manager) {
                    PictureSelectorUtil.openGalleryWithCrop(this@GroupChatInfoActivity, avatarRequestCode)
                } else {
                    ToastUtils.show(getString(R.string.group_host_can_modify))
                }
            }
            sivGroupCall.setSwitchCheckListener {buttonView, isChecked ->
                showLoadingDialog()
                groupViewModel.modifyGroupInfo(groupId, isAudio = isChecked)
            }
            sivGroupMute.setSwitchCheckListener { buttonView, isChecked ->
                showLoadingDialog()
                groupViewModel.modifyGroupInfo(groupId, null, null, null, isChecked)
//                groupViewModel.groupMemberMute(groupId, null, isChecked)
            }
            sivGroupDisturb.setSwitchCheckListener { buttonView, isChecked ->
                showLoadingDialog()
                groupViewModel.setGroupDisturb(groupId, isChecked)
            }
            sivGroupManager.setOnClickListener {
                Router.toGroupManagerActivity(groupId)
            }
            fltGroupQrcode.setOnClickListener {
                Router.toQrCodeDisplayActivity(ApplicationConst.getUserId(), groupId)
            }
        }
        setTitleBarTitleText(getString(R.string.group_info))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        groupId = intent.getStringExtra(KeyConst.key_group_id)?:""

        groupViewModel.getGroupInfo(groupId)
        groupViewModel.groupInfoRes.observe(this){
            if (it.isSuccess) {
                val groupInfoRes = it.groupInfo!!
                groupInfo = groupInfoRes.group_info
                if (isModified) {
                    isModified = false
                    groupInfo?.let { info -> UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("g" + info.id, info.avatar, info.name, is_disturb = info.is_disturb)) }
                    LiveEventBus.get<UserBean.GroupInfo>(EventKey.key_modify_group_info).post(groupInfo)
                }
                groupInfo?.let { it1 ->
                    UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("g$groupId", it1.avatar, it1.name, is_disturb = it1.is_disturb))
                    mDataBinding?.sivGroupCall?.setCheckedWithOutEvent(it1.is_audio == 1)
                    mDataBinding?.sivGroupMute?.setCheckedWithOutEvent(it1.is_mute == 1)
                }

                mDataBinding?.apply {
                    // 普通用户通用
                    ImgLoader.display(mContext, groupInfoRes.group_info?.avatar, rivAvatar)
                    sivGroupName.setTips(groupInfoRes.group_info?.name)
                    sivGroupDisturb.setCheckedImmediatelyWithOutEvent(groupInfo?.is_disturb == TypeConst.type_yes)
                }

                if (groupInfoRes.group_member != null && groupInfoRes.group_member!!.isNotEmpty()) {
                    groupManagerList.clear()
                    for (member in groupInfoRes.group_member!!) {
                        if (member.uid.toString() == ApplicationConst.getUserId()) {
                            myRole = member.role?:TypeConst.group_member_normal
                        }
                        if (member.role == TypeConst.group_member_manager || member.role == TypeConst.group_member_master) {
                            groupManagerList.add(member)
                        }
                    }

                    mDataBinding?.apply {
                        if (myRole == TypeConst.group_member_manager || myRole == TypeConst.group_member_master) {
                            sivGroupManager.visibility = View.VISIBLE
                            sivGroupMute.visibility = View.VISIBLE
                            tvMuteTips.visibility = View.VISIBLE
                            tvDelete.text = if (myRole == TypeConst.group_member_master) getString(R.string.dissolve_exit) else getString(R.string.delete_exit)
                        } else {
                            sivGroupManager.visibility = View.GONE
                            sivGroupMute.visibility = View.GONE
                            tvMuteTips.visibility = View.GONE
                            tvDelete.text = getString(R.string.delete_exit)
                        }

                        sivGroupCall.visibility = if (myRole == TypeConst.group_member_master && groupInfo?.audio == 1) View.VISIBLE else View.GONE
                    }

                    groupMemberList.clear()
                    setTitleBarTitleText(getString(R.string.group_info) + "(${groupInfoRes.group_member?.size})")

                    if (myRole == TypeConst.group_member_master || myRole == TypeConst.group_member_manager) {
                        if (groupInfoRes.group_member!!.size > maxMemberShow - 2) {
                            mDataBinding?.tvShowMore?.visibility = View.VISIBLE
                            groupMemberList.addAll(groupInfoRes.group_member!!.subList(0, maxMemberShow - 2))
                        } else {
                            mDataBinding?.tvShowMore?.visibility = View.GONE
                            groupMemberList.addAll(groupInfoRes.group_member!!)
                        }
                        groupMemberList.add(addItem)
                        groupMemberList.add(deleteIem)
                        groupMemberAdapter?.notifyDataSetChanged()
                        mDataBinding?.viewSpace?.visibility = if (groupMemberList.size % 5 == 1 || groupMemberList.size % 5 == 2) View.GONE else View.VISIBLE
                    } else {
                        if (groupInfoRes.group_member!!.size > maxMemberShow - 1) {
                            groupMemberList.addAll(groupInfoRes.group_member!!.subList(0, maxMemberShow - 1))
                            mDataBinding?.tvShowMore?.visibility = View.VISIBLE
                        } else {
                            groupMemberList.addAll(groupInfoRes.group_member!!)
                            mDataBinding?.tvShowMore?.visibility = View.GONE
                        }
                        groupMemberList.add(addItem)
                        groupMemberAdapter?.notifyDataSetChanged()
                        mDataBinding?.viewSpace?.visibility = if (groupMemberList.size % 5 == 1) View.GONE else View.VISIBLE
                    }
                }
            } else {
                ToastUtils.show(it.errorMsg?:"")
            }

        }

        groupViewModel.dissolveGroupRes.observe(this) {
            if (it.isSuccess) {
//                GroupOpUtil.deleteGroup(groupId)
                finish()
            } else {
                ToastUtils.show(it.message)
            }
        }

        groupViewModel.exitGroupRes.observe(this) {
            if (it.isSuccess) {
//                GroupOpUtil.deleteGroup(groupId)
                finish()
            } else {
                ToastUtils.show(it.message)
            }
        }

        groupViewModel.modifyGroupInfoRes.observe(this) { it ->
            hideLoadingDialog()
            if (it.isSuccess) {
                groupViewModel.getGroupInfo(groupId)
                isModified = true
            }
        }

        groupViewModel.groupDisturbRes.observe(this) {
            hideLoadingDialog()
            if (!TextUtils.isEmpty(it.message)) ToastUtils.show(it.message?:"")
            if (it.isSuccess) {
                groupViewModel.getGroupInfo(groupId)
            } else {
                mDataBinding?.sivGroupDisturb?.setCheckedImmediatelyWithOutEvent(it.originDisturb)
            }
        }

    }

    private fun findFriendById(id: Int): UserBean.GroupMember? {
        for (friend in groupMemberList) {
            if (friend.uid == id) {
                return friend
            }
        }

        return null
    }

    private fun findFriendPositionById(id: Int): Int {
        for ((index, friend) in groupMemberList.withIndex()) {
            if (friend.uid == id) {
                return index
            }
        }

        return -1
    }
    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                avatarRequestCode -> {
                    val selectPics = PictureSelector.obtainSelectorList(data)
                    if (selectPics.isNotEmpty()) {
                        val media = selectPics[0]
                        val sendPath = if (!TextUtils.isEmpty(media.compressPath)) media.compressPath else media.realPath
                        UploadUtil.startUpload {
                            it.upload(File(sendPath), object : UploadCallback {
                                override fun onProgress(progress: Int, url: String?, ossState: String) {
                                    if (progress >= 100 && !TextUtils.isEmpty(url)) {
                                        groupViewModel.modifyGroupInfo(groupId,null, url,  driver = ossState)
                                    }
                                }

                            })
                        }
                    }
                }
                changeNicknameRequestCode -> {
                    groupViewModel.getGroupInfo(groupId)
                    isModified = true
                }
            }
        }
    }

}