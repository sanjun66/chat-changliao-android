package com.legend.main.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import com.legend.base.utils.ClipBoardUtils
import com.legend.base.utils.StringUtils
import com.legend.baseui.ui.base.ActivityPageConfig
import com.legend.baseui.ui.base.BaseActivity
import com.legend.baseui.ui.util.ImgLoader
import com.legend.baseui.ui.widget.toast.ToastUtils
import com.legend.common.*
import com.legend.common.bean.UserBean
import com.legend.common.db.entity.DBEntity
import com.legend.common.network.viewmodel.UserViewModel
import com.legend.common.utils.ChatUtil
import com.legend.common.utils.UserSimpleDataHelper
import com.legend.common.widget.DialogUitl
import com.legend.main.R
import com.legend.main.databinding.ActivityUserBinding
import com.legend.main.network.viewmodel.FriendViewModel
import com.legend.main.network.viewmodel.GroupViewModel

@Route(path = RouterPath.path_user_activity)
class UserActivity: BaseActivity<ActivityUserBinding>() {

    private val REQUEST_CODE_ADD_FRIEND = 100
    private val changeNoteNameRequestCode = 201
    private var realAccount: String? = null
    private var mUserInfo:UserBean.UserInfo? = null
    private var mUid: String = ""
    private var mGroupId: String = ""
    private var mIsGroupMaster = false
    private var mIsModified = false
    private var misFromGroup = false

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val friendViewModel: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val groupViewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }

    override fun getLayoutId(): Int = R.layout.activity_user

    override fun initView() {
        mDataBinding?.apply {
            sivNoteName.setOnClickListener {
                mUserInfo?.let { Router.toChangeUserInfoActivity(this@UserActivity, changeNoteNameRequestCode, TypeConst.type_modify_note_name, it.id) }
            }
            tvSendMsg.setOnClickListener { mUserInfo?.let { Router.toChatActivity("s" + it.id , it.getNickName()) } }
            tvOpFriend.setOnClickListener {
                mUserInfo?.let {
                    if (it.is_friend == TypeConst.type_yes) {
                        // 删除
                        DialogUitl.showSimpleDialog(mContext, getString(com.legend.imkit.R.string.delete_friend_tips)
                        ) { dialog, content ->
                            friendViewModel.deleteFriend(it.id, 0)
                        }
                    } else {
                        // 添加好友
                        Router.toInputTransActivity(this@UserActivity, REQUEST_CODE_ADD_FRIEND, TypeConst.trans_input_page_type_add_friend, it.id)
                    }
                }
            }
            sivBlackName.setSwitchCheckListener { buttonView, isChecked ->
                mUserInfo?.let {
                    showLoadingDialog()
                    if (isChecked) {
                        userViewModel.addFriendBlack(it.id)
                    } else {
                        userViewModel.removeFriendBlack(it.id)
                    }
                }
            }
            sivDisturb.setSwitchCheckListener { buttonView, isChecked ->
                showLoadingDialog()
                mUserInfo?.let {
                    userViewModel.setFriendDisturb(it.id, isChecked)
                }
            }
            tvAccount.setOnClickListener {
                if (!TextUtils.isEmpty(mGroupId)) return@setOnClickListener
                DialogUitl.showStringArrayDialog(mContext, arrayOf(R.string.copy_account)) { text, tag ->
                    ClipBoardUtils.clipboardCopyText(mContext, "message", realAccount?:"") }
            }

            sivMute.setSwitchCheckListener { buttonView, isChecked ->
                if (mIsGroupMaster) {
                    groupViewModel.groupMemberMute(mGroupId, mUid, isChecked)
                }
            }
        }
        setTitleBarRightIcon(R.mipmap.ic_title_refresh)
    }

    override fun onTitleBarRightClick() {
        showLoadingDialog()
        userViewModel.getUserInfo(id = mUid)
    }

    override fun initData() {
        mUid = intent.getStringExtra(KeyConst.key_user_id)?:""
        mGroupId = intent.getStringExtra(KeyConst.key_group_id)?:""
        userViewModel.getUserInfo(id = mUid)
        if (!TextUtils.isEmpty(mGroupId)) groupViewModel.getGroupInfo(mGroupId)

        userViewModel.userInfoRes.observe(this) {
            hideLoadingDialog()
            mUserInfo = it
            if (mIsModified) {
                LiveEventBus.get<UserBean.UserInfo>(EventKey.key_modify_other_user_info).post(it)
                mIsModified = false
            }
            mDataBinding?.apply {
                UserSimpleDataHelper.saveUserInfo(DBEntity.UserSimpleInfo("s" + it.id, it.avatar, it.getNickName(), it.quickblox_id, it.is_disturb))
                if (!TextUtils.isEmpty(it.avatar)) ImgLoader.display(mContext, it.avatar, avatar)
                tvName.text = it.getNickName()
                realAccount = if (!TextUtils.isEmpty(it.phone)) it.phone else it.email
                tvAccount.text = String.format(getString(R.string.user_account1), if (!TextUtils.isEmpty(mGroupId)) StringUtils.hideMiddleContent(realAccount) else realAccount)
                tvNickName.text = String.format(getString(R.string.user_nick_name1), it.nick_name)
                ImgLoader.display(mContext, if (it.sex == 2) R.mipmap.ic_sex_female else R.mipmap.ic_sex_male , imgSex)

//                if (!TextUtils.isEmpty(it.note_name)) sivNoteName.setTips(it.note_name)
                sivBlackName.setCheckedImmediatelyWithOutEvent(it.is_black == TypeConst.type_yes)
                sivDisturb.setCheckedImmediatelyWithOutEvent(it.is_disturb == TypeConst.type_yes)

                if(it.is_friend == TypeConst.type_yes) {
                    lineTop.visibility = View.VISIBLE
                    sivNoteName.visibility = View.VISIBLE
                    sivBlackName.visibility = View.VISIBLE
                    sivDisturb.visibility = View.VISIBLE
                    tvSendMsg.visibility = View.VISIBLE
                    tvOpFriend.visibility = View.VISIBLE
                    tvOpFriend.text = getString(R.string.delete_friend)
                    tvOpFriend.setTextColor(resources.getColor(com.com.legend.ui.R.color.ui_red_FE2121))
                } else {
                    lineTop.visibility = View.GONE
                    sivNoteName.visibility = View.GONE
                    sivBlackName.visibility = View.GONE
                    sivDisturb.visibility = View.GONE
                    tvSendMsg.visibility = View.INVISIBLE
                    if (TextUtils.isEmpty(mGroupId) && mUid != ApplicationConst.getUserId()) {
                        tvOpFriend.visibility = View.VISIBLE
                        tvOpFriend.text = getString(R.string.add_friend)
                        tvOpFriend.setTextColor(resources.getColor(com.legend.commonres.R.color.primary_color))
                    } else {
                        tvOpFriend.visibility = View.GONE
                    }
                }
            }
        }

        userViewModel.addBlackRes.observe(this) {
            hideLoadingDialog()
            ToastUtils.show(it.message)
            if (!it.isSuccess) {
                mDataBinding?.sivBlackName?.setCheckedWithOutEvent(false)
            }
        }

        userViewModel.removeBlackRes.observe(this) {
            hideLoadingDialog()
            if (!TextUtils.isEmpty(it.message)) ToastUtils.show(it.message!!)
            if (!it.isSuccess) {
                mDataBinding?.sivBlackName?.setCheckedWithOutEvent(true)
            }
        }
        userViewModel.friendDisturbRes.observe(this) {
            hideLoadingDialog()
            if (!TextUtils.isEmpty(it.message)) ToastUtils.show(it.message?:"")
            if (it.isSuccess) {
                userViewModel.getUserInfo(mUid)
            } else {
                mDataBinding?.sivDisturb?.setCheckedImmediatelyWithOutEvent(it.originalDisturb)
            }
        }

        friendViewModel.friendDeleteRes.observe(this) {
            if (it >= 0) {
                mUserInfo?.let { it1 ->
                    Router.toHomeActivity()
//                    ChatUtil.deleteFriend(mContext, it1.id)
                }
            }
        }

        friendViewModel.applyFriendSuccess.observe(this) {
            if (it.isSuccess) {
                ToastUtils.show(getString(R.string.apply_friend_success))
                finish()
            } else {
                ToastUtils.show(it.message)
            }
        }

        groupViewModel.groupInfoRes.observe(this) {
            if (it.isSuccess) {
                val groupInfo = it.groupInfo!!
                mIsGroupMaster = groupInfo.group_info?.uid == ApplicationConst.getUserId()
                if (mIsGroupMaster) {
                    // 群主才可以给用户禁言
                    mDataBinding?.sivMute?.visibility = if (mUid == ApplicationConst.getUserId()) View.GONE else View.VISIBLE
                    if (groupInfo.group_member != null) {
                        val member = findMemberInGroup(mUid, groupInfo.group_member!!)
                        member?.apply {
                            mDataBinding?.sivMute?.setCheckedWithOutEvent(is_mute == TypeConst.type_yes)
                        }
                    }
                } else {
                    mDataBinding?.sivMute?.visibility = View.GONE
                }
            } else {
                ToastUtils.show(it.errorMsg?:"")
            }

        }

        groupViewModel.groupMemberMuteRes.observe(this) {
            ToastUtils.show(it.message)
            if (!it.isSuccess) {
                mDataBinding?.sivMute?.setCheckedWithOutEvent(it.is_mute == TypeConst.type_yes)
            }
        }
    }

    private fun findMemberInGroup(id: String, groupMember: List<UserBean.GroupMember>): UserBean.GroupMember? {
        for (member in groupMember) {
            if (member.uid.toString() == id) {
                return member
            }
        }

        return null
    }

    override fun initPageConfig(pageConfig: ActivityPageConfig?) {
        super.initPageConfig(pageConfig)
        pageConfig?.showAppTitleBar = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                changeNoteNameRequestCode -> {
                    mIsModified = true
                    userViewModel.getUserInfo(mUid)
                }
                REQUEST_CODE_ADD_FRIEND -> {
                    val id = data?.getStringExtra(KeyConst.key_id)?:""
                    val inputReason = data?.getStringExtra(KeyConst.key_input_content)?:""
                    friendViewModel.applyFriend(id, inputReason)
                }
            }
        }
    }
}