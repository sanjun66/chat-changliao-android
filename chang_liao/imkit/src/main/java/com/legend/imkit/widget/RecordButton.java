package com.legend.imkit.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatButton;

import com.legend.baseui.ui.widget.toast.ToastUtils;
import com.legend.imkit.R;
import com.legend.imkit.manager.AudioPlayManager;
import com.legend.imkit.manager.AudioRecordManager;
import com.legend.imkit.util.RongOperationPermissionUtils;

public class RecordButton extends AppCompatButton {

    private static final String TAG = "RecordButton";


    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private OnFinishedRecordListener finishedListener;

    private String mSessionId;
    public void setSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
        finishedListener = listener;
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        mOffsetLimit = 70 * getContext().getResources().getDisplayMetrics().density;
    }

    private AnimationDrawable anim;
    private float mLastTouchY;
    private boolean mUpDirection;
    private float mOffsetLimit;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (AudioPlayManager.getInstance().isPlaying()) {
                    AudioPlayManager.getInstance().stopPlay();
                }
                // 判断正在视频通话和语音通话中不能进行语音消息发送
                if (RongOperationPermissionUtils.isOnRequestHardwareResource()) {
                    ToastUtils.show(getResources().getString(R.string.rc_voip_occupying));
                    return true;
                }
                AudioRecordManager.getInstance().startRecord(getRootView(), mSessionId, finishedListener);
                mLastTouchY = event.getY();
                mUpDirection = false;
                setText(getResources().getString(R.string.rc_voice_release_to_send));
//                initDialogAndStartRecord();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastTouchY - event.getY() > mOffsetLimit && !mUpDirection) {
                    AudioRecordManager.getInstance().willCancelRecord();
                    mUpDirection = true;
                    // 按住说话
                    setText(R.string.rc_voice_press_to_input);
                } else if (event.getY() - mLastTouchY > -mOffsetLimit && mUpDirection) {
                    AudioRecordManager.getInstance().continueRecord();
                    mUpDirection = false;
                    setText(getResources().getString(R.string.rc_voice_release_to_send));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                AudioRecordManager.getInstance().stopRecord();
                setText(R.string.rc_voice_press_to_input);
                break;
        }

        return true;
    }

    public interface OnFinishedRecordListener {
        public void onFinishedRecord(String audioPath, int time);
    }


}
