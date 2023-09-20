package com.legend.common.bean;

import android.text.SpannableStringBuilder;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.legend.common.TypeConst;
import com.legend.common.db.entity.ChatMessageModel;
import com.legend.common.db.entity.DBEntity;
import com.legend.common.utils.UserSimpleDataHelper;

public class UiMessage extends UiBaseBean implements MultiItemEntity, Comparable<UiMessage> {
    private ChatMessageModel message;
    private DBEntity.UserSimpleInfo userInfo;
    private int progress;
    private String destructTime;
    private boolean isPlaying;
    private boolean isEdit;
    private boolean isSelected;
    private String title;
    /** TextMessage 和 ReferenceMessage 的 content 字段 */
    private SpannableStringBuilder contentSpannable;
    /** ReferenceMessage 的 referMsg 为 TextMessage 时 的 content 字段 */
    private SpannableStringBuilder referenceContentSpannable;
    /** 翻译之后的文本 */
    private String translatedContent;

    public UiMessage(ChatMessageModel message) {
        setMessage(message);
        if (message.talk_type == TypeConst.talk_type_group_chat) {
            // 群聊天获取的头像都是消息接收
            userInfo = UserSimpleDataHelper.INSTANCE.getUserInfoForJava("s" + message.from_uid);
        } else {
            userInfo = UserSimpleDataHelper.INSTANCE.getUserInfoForJava("s" + message.from_uid);
        }
        change();
    }

    public ChatMessageModel getMessage() {
        return message;
    }

    public void setMessage(ChatMessageModel message) {
        this.message = message;
        // 可以设置发送状态
    }



    public DBEntity.UserSimpleInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(DBEntity.UserSimpleInfo userInfo) {
        this.userInfo = userInfo;
        change();
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        change();
    }

    public String getDestructTime() {
        return destructTime;
    }

    public void setDestructTime(String destructTime) {
        this.destructTime = destructTime;
        change();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        change();
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
        change();
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        change();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SpannableStringBuilder getContentSpannable() {
        return contentSpannable;
    }

    public void setContentSpannable(SpannableStringBuilder contentSpannable) {
        this.contentSpannable = contentSpannable;
    }

    public SpannableStringBuilder getReferenceContentSpannable() {
        return referenceContentSpannable;
    }

    public void setReferenceContentSpannable(SpannableStringBuilder referenceContentSpannable) {
        this.referenceContentSpannable = referenceContentSpannable;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    @Override
    public int getItemType() {
        return message == null? TypeConst.chat_msg_type_text : message.message_type;
    }

    @Override
    public int compareTo(UiMessage o) {
        if (message == null || o.message == null) return 0;
        int i = (int) (o.message.timestamp - this.message.timestamp);
//        if (i == 0) {
//            return (int) (o.message.id - this.message.id);
//        }
        return i;
    }
}
