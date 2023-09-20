package com.legend.imkit.mention;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.legend.common.Router;
import com.legend.common.TypeConst;
import com.legend.common.bean.UserBean;
import com.legend.common.db.entity.DBEntity;
import com.legend.imkit.util.RTLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import io.rong.common.RLog;

public class RongMentionManager {
    private static String TAG = "RongMentionManager";
    private Stack<MentionInstance> stack = new Stack<>();

    private static class SingletonHolder {
        static RongMentionManager sInstance = new RongMentionManager();
    }

    private RongMentionManager() {
        // default implementation ignored
    }

    public static RongMentionManager getInstance() {
        return SingletonHolder.sInstance;
    }

    public void createInstance( EditText editText) {
        RLog.i(TAG, "createInstance");
        for (int i = 0; i < stack.size(); i++) {
            MentionInstance item = stack.get(i);
            if (item.inputEditText.equals(editText)) {
                return;
            }
        }
        MentionInstance mentionInstance = new MentionInstance();
        mentionInstance.inputEditText = editText;
        mentionInstance.mentionBlocks = new ArrayList<>();
        stack.add(mentionInstance);
    }

    public void destroyInstance(EditText editText) {
        RLog.i(TAG, "destroyInstance");
        for (int i = 0; i < stack.size(); i++) {
            MentionInstance item = stack.get(i);
            if (item.inputEditText.equals(editText)) {
                stack.remove(i);
                return;
            }
        }
    }

    public void mentionMember(DBEntity.UserSimpleInfo userInfo) {
        if (stack.isEmpty()) {
            RLog.e(TAG, "Illegal argument stack is Empty");
            return;
        }

        if (userInfo == null || TextUtils.isEmpty(userInfo.getUid())) {
            RLog.e(TAG, "Invalid userInfo");
            return;
        }

        addMentionedMember(userInfo.getNick_name(), userInfo.getUid(), 0);
    }

    public void mentionMember(UserBean.ConcatSimple userInfo, int fromIndex) {
        if (userInfo == null || TextUtils.isEmpty(userInfo.getId())) {
            RLog.e(TAG, "Invalid userInfo");
            return;
        }
        addMentionedMember(userInfo.getFinalNickName(), userInfo.getId(),fromIndex);
    }

    public void mentionMembers(List<UserBean.ConcatSimple> users) {
        for (int i = 0; i < users.size(); i++) {
            mentionMember(users.get(i), i == 0 ? 1 : 0);
        }
    }

    /**
     * @param from 0 代表来自会话界面，1 来着群成员选择界面。
     */
    private void addMentionedMember(String name, String uId, int from) {
        if (!stack.isEmpty()) {
            MentionInstance mentionInstance = stack.peek();
            EditText editText = mentionInstance.inputEditText;
            if (editText != null) {
                String mentionContent;
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                        == View.LAYOUT_DIRECTION_RTL) {
                    // RTL布局处理
                    // @字符和人名需要一起落到ET上。方便起见落之前先删除@字符再添加；
                    if (from == 1) deleteLastChar(editText);
                    // @字符前需添加"\u200e"(LRM)或"\u200f"(RLM)表明方向，否则RLT下多种文字混排有问题
                    String str = "@" + name + " ";
                    mentionContent = RTLUtils.adapterAitInRTL(str);
                } else {
                    mentionContent = from == 0 ? "@" + name + " " : name + " ";
                }
                int len = mentionContent.length();
                int cursorPos = editText.getSelectionStart();

                MentionBlock brokenBlock = getBrokenMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
                if (brokenBlock != null) {
                    mentionInstance.mentionBlocks.remove(brokenBlock);
                }

                MentionBlock mentionBlock = new MentionBlock();
                mentionBlock.userId = uId;
                mentionBlock.offset = false;
                mentionBlock.name = name;
                if (from == 1) {
                    mentionBlock.start = cursorPos - 1;
                } else {
                    mentionBlock.start = cursorPos;
                }
                mentionBlock.end = cursorPos + len;
                mentionInstance.mentionBlocks.add(mentionBlock);

                editText.getEditableText().insert(cursorPos, mentionContent);
                editText.setSelection(cursorPos + len);
                mentionBlock.offset = true;
            }
        }
    }

    private MentionBlock getBrokenMentionedBlock(int cursorPos, List<MentionBlock> blocks) {
        MentionBlock brokenBlock = null;
        for (MentionBlock block : blocks) {
            if (block.offset && cursorPos < block.end && cursorPos > block.start) {
                brokenBlock = block;
                break;
            }
        }
        return brokenBlock;
    }

    private void offsetMentionedBlocks(int cursorPos, int offset, List<MentionBlock> blocks) {
        for (MentionBlock block : blocks) {
            if (cursorPos <= block.start && block.offset) {
                block.start += offset;
                block.end += offset;
            }
            block.offset = true;
        }
    }

    private MentionBlock getDeleteMentionedBlock(int cursorPos, List<MentionBlock> blocks) {
        MentionBlock deleteBlock = null;
        for (MentionBlock block : blocks) {
            if (cursorPos == block.end) {
                deleteBlock = block;
                break;
            }
        }
        return deleteBlock;
    }

    /**
     * 当输入框文本变化时，回调此方法。
     *
     * @param cursorPos 输入文本时，光标位置初始位置
     * @param offset 文本的变化量：增加时为正数，减少是为负数
     * @param text 文本内容
     */
    public void onTextChanged(Activity activity, int cursorPos, int offset, String text, EditText editText, String groupId, ArrayList<UserBean.GroupMember> members, boolean isGroupOwner) {
        RLog.d(TAG, "onTextEdit " + cursorPos + ", " + text);

        if (stack == null || stack.isEmpty()) {
            RLog.w(TAG, "onTextEdit ignore.");
            return;
        }
        MentionInstance mentionInstance = null;
        for (int i = 0; i < stack.size(); i++) {
            MentionInstance item = stack.get(i);
            if (item.inputEditText.equals(editText)) {
                mentionInstance = item;
                break;
            }
        }
        if (mentionInstance == null) {
            RLog.w(TAG, "onTextEdit ignore conversation.");
            return;
        }
        // 判断单个字符是否是@
        if (offset == 1) {
            if (!TextUtils.isEmpty(text)) {
                boolean showMention = false;
                String str;
                if (cursorPos == 0) {
                    str = text.substring(0, 1);
                    showMention = str.equals("@");
                } else {
                    String preChar = text.substring(cursorPos - 1, cursorPos);
                    str = text.substring(cursorPos, cursorPos + 1);
                    if (str.equals("@")
                            && !preChar.matches("^[a-zA-Z]*")
                            && !preChar.matches("^\\d+$")) {
                        showMention = true;
                    }
                }
                if (showMention) {
                    Router.INSTANCE.toSelectMemberActivity(activity, 0, groupId, TypeConst.state_group_mention, members, 0, isGroupOwner);
                }
            }
        }

        // 判断输入光标位置是否破坏已有的“@块”。
        MentionBlock brokenBlock =
                getBrokenMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
        if (brokenBlock != null) {
            mentionInstance.mentionBlocks.remove(brokenBlock);
        }
        // 改变所有有效“@块”位置。
        offsetMentionedBlocks(cursorPos, offset, mentionInstance.mentionBlocks);
    }

    public String getMentionIds(EditText editText, boolean isClear) {
        if (stack.isEmpty()) return "";
        MentionInstance curInstance = null;
        String strUserIds = "";
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < stack.size(); i++) {
            MentionInstance item = stack.get(i);
            if (item.inputEditText.equals(editText)) {
                curInstance = item;
                break;
            }
        }
        if (curInstance == null) {
            RLog.w(TAG, "not found editText");
            return "";
        }
        for (MentionBlock block : curInstance.mentionBlocks) {
            if (!userIds.contains(block.userId)) {
                userIds.add(block.userId);
                strUserIds = block.userId + ",";
            }
        }
        if (strUserIds.endsWith(",")) strUserIds = strUserIds.substring(0, strUserIds.length() - 1);
        if (!userIds.isEmpty() && isClear) {
            curInstance.mentionBlocks.clear();
        }

        return strUserIds;

    }

    public void onDeleteClick(EditText editText, int cursorPos) {
        RLog.d(TAG, "onTextEdit " + cursorPos);

        if (!stack.isEmpty() && cursorPos > 0) {
            MentionInstance mentionInstance = null;
            for (int i = 0; i < stack.size(); i++) {
                MentionInstance item = stack.get(i);
                if (item.inputEditText.equals(editText)) {
                    mentionInstance = item;
                    break;
                }
            }
            if (mentionInstance == null) {
                RLog.w(TAG, "not found editText");
                return;
            }
            MentionBlock deleteBlock =
                    getDeleteMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
            if (deleteBlock != null) {
                mentionInstance.mentionBlocks.remove(deleteBlock);
                String delText = deleteBlock.name;
                int start = cursorPos - delText.length() - 1;
                editText.getEditableText().delete(start, cursorPos);
                editText.setSelection(start);
            }
        }
    }

    private void deleteLastChar(EditText editText) {
        int index = editText.getSelectionStart();
        Editable editable = editText.getText();
        if (editable != null && editable.length() > 0) {
            editable.delete(index - 1, index);
        }
    }

}
