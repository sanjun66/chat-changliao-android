package com.legend.common.db.entity;

import android.text.TextUtils;

import com.legend.common.ApplicationConst;
import com.legend.common.TypeConst;

import java.util.Objects;

// 与DBEntity.ChatMessageEntity 辉映
public class ChatMessageModel<T> {
    public String id ;
    public String session_id = "";
    public String from_uid ;
    public String to_uid;
    public int talk_type;
    public int is_read;
    public int is_revoke;
    public int quote_id;
    public int message_type;
    public int message_local_type;  // 当地为显示更进一步区分的type
    public String warn_users = "";
    public String message = "";
    public Long timestamp;
    public T extra;
    public boolean is_secret;
    public String pwd = "";


    public String uuid = "";
    public int sendStatus = TypeConst.msg_send_status_sending;
    public int revStatus = TypeConst.msg_rev_status_accept;

    public ChatMessageModel() {}
    public ChatMessageModel(String id, String session_id, String fromUid, String toUid, int talkType, int isRead, int isRevoke, int quoteId, int messageType, String warnUsers, String message, long timestamp, String uuid, T extra
            , int sendStatus, int receiveStatus, boolean is_secret, String pwd) {
        this.id = id;
        this.session_id = session_id;
        this.from_uid = fromUid;
        this.to_uid = toUid;
        this.talk_type = talkType;
        this.is_read = isRead;
        this.is_revoke = isRevoke;
        this.quote_id = quoteId;
        this.message_type = messageType;
        this.warn_users = warnUsers;
        this.message = message;
        this.timestamp = timestamp;
        this.extra = extra;
        this.uuid = TextUtils.isEmpty(uuid) ? "" : uuid;
        this.sendStatus = sendStatus;
        this.revStatus = receiveStatus;
        this.is_secret = is_secret;
        this.pwd = pwd;
    }

    public boolean isSender() {
        return Objects.equals(from_uid, ApplicationConst.INSTANCE.getUserId());
    }

    public boolean isFromAndroid() {
        return uuid.startsWith(TypeConst.dev_android);
    }

    public boolean isGroup() {
        return talk_type == TypeConst.talk_type_group_chat;
    }

    public T process() {
        return null;
    }
}
