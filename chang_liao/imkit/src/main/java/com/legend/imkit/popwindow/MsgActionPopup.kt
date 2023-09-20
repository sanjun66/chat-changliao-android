package com.legend.imkit.popwindow

import android.content.Context
import android.view.View
import com.legend.base.utils.StringUtils
import com.legend.common.TypeConst
import com.legend.common.bean.UiMessage
import com.legend.imkit.R
import com.legend.imkit.popwindow.action.PopMenuAction
import com.legend.imkit.util.ExecutorHelper

class MsgActionPopup(private val context: Context) {
    val mPopActions = mutableListOf<PopMenuAction>()
    var mOnPopActionClickListener: OnPopActionClickListener? = null


    fun showItemPopMenu(index: Int, msg:UiMessage, view: View) {
        if (msg.message.message_type == TypeConst.chat_msg_type_group_op
            || msg.message.is_revoke == TypeConst.type_yes) {
            return
        }
        initPopActions(msg)
        if (mPopActions.isEmpty()) return
        val popupList = PopupList(context)
        val itemList = arrayListOf<String>()
        for (action in mPopActions) {
            itemList.add(action.actionName)
        }
        popupList.show(view, itemList, object : PopupList.PopupListListener {
            override fun showPopupList(
                adapterView: View?,
                contextView: View?,
                contextPosition: Int
            ): Boolean {
                return true
            }

            override fun onPopupListClick(contextView: View?, contextPosition: Int, position: Int) {
                val action = mPopActions[position]
                action.actionClickListener?.let { it.onActionClick(index, msg) }
            }
        })

        (ExecutorHelper.getInstance().mainThread() as ExecutorHelper.MainThreadExecutor).executeDelayed({
                   popupList.hidePopupListWindow()
        }, 10 * 1000) // 10s后无操作自动消失

    }


    private fun initPopActions(msg: UiMessage) {
        val actions = arrayListOf<PopMenuAction>()
        if (msg.message.message_type == TypeConst.chat_msg_type_text) {
            val action = PopMenuAction()
            action.actionName = StringUtils.getString(R.string.copy_action)
            action.setActionClickListener { position, data -> mOnPopActionClickListener?.onCopyClicked(position, data as UiMessage) }
            actions.add(action)
        }

        if (msg.message.isSender && msg.message.sendStatus == TypeConst.msg_send_status_sent/*&& System.currentTimeMillis() - msg.message.timestamp <= 2 * 60 * 1000*/) {
            val action = PopMenuAction()
            action.actionName = StringUtils.getString(R.string.recall_action)
            action.setActionClickListener { position, data -> mOnPopActionClickListener?.onRevokeMessageClick(position, data as UiMessage) }
            actions.add(action)
        }

        val delAction = PopMenuAction()
        delAction.actionName = StringUtils.getString(R.string.delete_action)
        delAction.setActionClickListener { position, data -> mOnPopActionClickListener?.onDeleteMessageClick(position, data as UiMessage) }
        actions.add(delAction)

        if (msg.message.sendStatus == TypeConst.msg_send_status_sent && !msg.message.is_secret
            && !(msg.message.message_type == TypeConst.chat_msg_type_file && msg.message.message_local_type == TypeConst.chat_msg_type_file_voice)
        ) {
            val action = PopMenuAction()
            action.actionName = StringUtils.getString(R.string.forward_action)
            action.setActionClickListener { position, data -> mOnPopActionClickListener?.onForwardClicked(position, data as UiMessage) }
            actions.add(action)
        }

        if (msg.message.isSender && (msg.message.sendStatus == TypeConst.msg_send_status_failed || msg.message.sendStatus == TypeConst.msg_send_status_uploading)) {
            val action = PopMenuAction()
            action.actionName = StringUtils.getString(R.string.resend_action)
            action.setActionClickListener { position, data -> mOnPopActionClickListener?.onSendMessageClicked(msg, true) }
            actions.add(action)
        }

        val multiSelectAction = PopMenuAction()
        multiSelectAction.actionName = StringUtils.getString(R.string.multiple_select_action)
        multiSelectAction.setActionClickListener { position, data ->  mOnPopActionClickListener?.onMultiSelectClick(position, data as UiMessage)}
        actions.add(multiSelectAction)

        mPopActions.clear()
        mPopActions.addAll(actions)
    }
}

interface OnPopActionClickListener {
    fun onCopyClicked(position: Int, msg: UiMessage)
    fun onForwardClicked(position: Int, msg: UiMessage)
    fun onMultiSelectClick(position: Int, msg: UiMessage)
    fun onSendMessageClicked(msg: UiMessage, retry: Boolean)
    fun onDeleteMessageClick(position: Int, msg: UiMessage)
    fun onRevokeMessageClick(position: Int, msg: UiMessage)
}