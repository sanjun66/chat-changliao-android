package com.legend.main.network.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.legend.base.utils.StringUtils
import com.legend.basenet.network.NetworkManager
import com.legend.basenet.network.bean.BaseRes
import com.legend.basenet.network.coroutine.request
import com.legend.common.bean.UserBean
import com.legend.common.network.services.UserService
import com.legend.common.network.services.GroupService
import com.legend.commonres.R

class GroupViewModel: ViewModel() {
    val createGroupRes = MutableLiveData<BaseRes>()
    val dissolveGroupRes = MutableLiveData<BaseRes>()
    val exitGroupRes = MutableLiveData<BaseRes>()
    val inviteGroupRes = MutableLiveData<BaseRes>()
    val kickOutGroupRes = MutableLiveData<BaseRes>()
    val groupInfoRes = MutableLiveData<GroupInfoRes>()
    val modifyGroupInfoRes = MutableLiveData<BaseRes>()
    val groupListRes = MutableLiveData<UserBean.GroupList>()
    val groupMemberMuteRes = MutableLiveData<MemberMuteRes>()
    val groupDisturbRes = MutableLiveData<GroupDisturbRes>()
    val groupManagerRes = MutableLiveData<BaseRes>()
    val scanQrRes = MutableLiveData<BaseRes>()

    private val service by lazy {
        NetworkManager.getInstance().getService(GroupService::class.java)
    }
    private val userService by lazy {
        NetworkManager.getInstance().getService(UserService::class.java)
    }

    fun createGroup( memberId: String) {
        request({service.createGroup(memberId)}, {
            createGroupRes.value = BaseRes(true, "")
        }, {
            createGroupRes.value = BaseRes(false, it.message)
        })
    }

    fun dissolveGroup(groupId: String) {
        request({service.dissolveGroup(groupId)}, {
            dissolveGroupRes.value = BaseRes(true, "")
        }, {
            dissolveGroupRes.value = BaseRes(false, it.message)
        })
    }

    fun exitGroup(groupId: String) {
        request({service.exitGroup(groupId)}, {
            exitGroupRes.value = BaseRes(true, "")
        }, {
            exitGroupRes.value = BaseRes(false, it.message)
        })
    }

    fun getGroupInfo(groupId: String) {
        request({userService.getGroupInfo(groupId)}, {
            groupInfoRes.value = GroupInfoRes(true, 200, it, null)
        }, {
            groupInfoRes.value = GroupInfoRes(false, it.code, null, it.message)
        })
    }

    fun modifyGroupInfo(groupId: String, groupName: String? = null, subAvatar: String? = null, avatar: String? = null, isMute: Boolean? = null, isAudio: Boolean? = null, driver: String? = null) {
        val params = HashMap<String, String>()
        params["id"] = groupId
        groupName?.let { params["name"] = it }
        subAvatar?.let { params["avatar"] = it }
        isMute?.let { params["is_mute"] = if (it) "1" else "0" }
        isAudio?.let { params["is_audio"] = if (it) "1" else "0"}
        driver?.let { params["driver"] = it}
        request({service.modifyGroupInfo(params)}, {
            modifyGroupInfoRes.value = BaseRes(true, "")
        }, {
            modifyGroupInfoRes.value = BaseRes(false, it.message)
        })
    }

    fun setGroupDisturb(groupId: String, isDisturb: Boolean) {
        val params = HashMap<String, String>()
        params["group_id"] = groupId
        params["is_disturb"] = if (isDisturb) "1" else "0"
        request({service.setGroupDisturb(params)}, {
            groupDisturbRes.value = GroupDisturbRes(true, StringUtils.getString(R.string.success), !isDisturb)
        }, {
            groupDisturbRes.value = GroupDisturbRes(false, it.message, !isDisturb)
        })
    }

    fun inviteToGroup(groupId: String, memberId: String) {
        request({service.inviteToGroup(groupId, memberId)}, {
            inviteGroupRes.value = BaseRes(true, "")
        }, {
            inviteGroupRes.value = BaseRes(false, "")
        })
    }
    
    fun kickOutGroup(groupId: String, memberId: String) {
        request({service.kickOutGroup(groupId, memberId)}, {
            kickOutGroupRes.value = BaseRes(true, StringUtils.getString(R.string.success))
        }, {
            kickOutGroupRes.value = BaseRes(false, it.message)
        })
    }

    fun getGroupList() {
        request({service.getGroupList()}, {
            groupListRes.value = it
        })
    }

    /**
     * 群聊个人禁言
     */
    fun groupMemberMute(groupId: String, id: String, isMute: Boolean) {
        val params = HashMap<String, String>()
        params["group_id"] = groupId
        params["id"] = id
        params["is_mute"] = if (isMute) "1" else "0"
        request({service.groupMemberMute(params)}, {
            groupMemberMuteRes.value = MemberMuteRes(true, StringUtils.getString(R.string.success), if (isMute) 1 else 0)
        }, {
            groupMemberMuteRes.value = MemberMuteRes(false, it.message?:"", if (isMute) 1 else 0)
        })
    }


    fun opGroupManager(groupId: String, toRole: String, users: String) {
        val params = HashMap<String, String>()
        params["group_id"] = groupId
        params["is_manager"] = toRole
        params["user_str"] = users
        request({service.opGroupManager(params)}, {
            groupManagerRes.value = BaseRes(true, StringUtils.getString(R.string.success))
        }, {
            groupManagerRes.value = BaseRes(false, it.message?:"")
        })
    }

    fun scanJoinGroup(groupId: String, inviteId: String) {
        val params = HashMap<String, String>()
        params["id"] = groupId
        params["invite_id"] = inviteId
        request({service.scanJoinGroup(params)}, {
            scanQrRes.value = BaseRes(true, StringUtils.getString(R.string.success))
        }, {
            scanQrRes.value = BaseRes(false, it.message?:"")
        })
    }


}

data class MemberMuteRes(val isSuccess: Boolean, val message: String , val is_mute: Int)
data class GroupInfoRes(val isSuccess: Boolean, val code: Int, val groupInfo: UserBean.GroupAllInfo?, val errorMsg: String?)
data class GroupDisturbRes(val isSuccess:Boolean, val message: String?, val originDisturb: Boolean)