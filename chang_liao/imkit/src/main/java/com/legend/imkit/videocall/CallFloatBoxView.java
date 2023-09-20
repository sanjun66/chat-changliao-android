package com.legend.imkit.videocall;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.legend.base.utils.StringUtils;
import com.legend.baseui.ui.util.DisplayUtils;
import com.legend.baseui.ui.util.UiUtils;
import com.legend.imkit.R;
import com.legend.imkit.videocall.activity.CallActivity;
import com.legend.imkit.videocall.service.CallService;
import com.legend.imkit.videocall.util.FLoatViewUtils;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.Map;
import java.util.Timer;

/** Created by weiqinxiao on 16/3/17. */
public class CallFloatBoxView {
    private static Context mContext;
    private static Timer timer;
    private static View mView;
    private static Boolean isShown = false;
    private static WindowManager wm;
    private static Bundle mBundle;
    private static final String TAG = "CallFloatBoxView";
    private static TextView showFBCallTime = null;
    private static FrameLayout remoteVideoContainer = null;
    private static boolean isIncomingCall = false;
    private static boolean isSessionConnected = false;
    private static boolean mIsVideoCall = false;
    private static CallService.CallTimerListener timeCallBack = new CallService.CallTimerListener() {
        @Override
        public void onCallTimeUpdate(@NonNull String time) {
            if (showFBCallTime != null) {
                showFBCallTime.post(()-> {
                    if (showFBCallTime != null) {
                        if (!mIsVideoCall) {
                            showFBCallTime.setVisibility(View.VISIBLE);
                            showFBCallTime.setText(time);
                        } else {
                            showFBCallTime.setVisibility(View.GONE);
                        }
                    }
                });

            }
        }
    };

    public static CallService.CallTimerListener getTimeCallBack() {
        return timeCallBack;
    }

    public static void showFB(Context context, Bundle bundle, Boolean isVideoCall, Map<Integer, QBRTCVideoTrack> videoTrackMap, Boolean connected, boolean isIncoming) {
        if (isShown) {
            return;
        }
        mIsVideoCall = isVideoCall;
        isIncomingCall = isIncoming;
        setExcludeFromRecents(context, true);

//        if (!isSessionConnected) {
//            CallFloatBoxView.showFloatBoxToCall(context, bundle, isVideoCall);
//        } else {
//            CallFloatBoxView.showFloatBox(context, bundle, isVideoCall, videoTrackMap);
//        }

        if (!isVideoCall) {
            CallFloatBoxView.showFloatBoxToCall(context, bundle, isVideoCall);
        } else {
            CallFloatBoxView.showFloatBox(context, bundle, isVideoCall, videoTrackMap);
        }
    }

    public static void showFloatBox(Context context, Bundle bundle, Boolean isVideoCall, Map<Integer, QBRTCVideoTrack> videoTrackMap) {
        mContext = context;
        isShown = true;

        mBundle = bundle;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = createLayoutParams(context);

        if (isVideoCall) {
            QBRTCSurfaceView videoView = null;
            if (!videoTrackMap.isEmpty()) {
                QBRTCVideoTrack videoTrack = FLoatViewUtils.getUserVideos(videoTrackMap);
                if (videoTrack != null) {
                    videoView = new QBRTCSurfaceView(mContext);
                    videoTrack.removeRenderer(videoTrack.getRenderer());
                    videoView.setMirror(true);
                    videoTrack.addRenderer(videoView);
                }

            }
            if (videoView != null) {
                ViewGroup parent = (ViewGroup) videoView.getParent();
                if (parent != null) {
                    parent.removeView(videoView);
                }
                params.width = DisplayUtils.dp2px(context, 80f);
                params.height = DisplayUtils.dp2px(context, 120f);
                remoteVideoContainer = new FrameLayout(mContext);
                remoteVideoContainer.addView(
                        videoView,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                remoteVideoContainer.setOnTouchListener(createTouchListener());
                wm.addView(remoteVideoContainer, params);
                if (!isSessionConnected) {
                    showFBCallTime = new TextView(remoteVideoContainer.getContext());
                    FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    tvParams.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
                    tvParams.bottomMargin = DisplayUtils.dp2px(mContext, 5);
                    remoteVideoContainer.addView(showFBCallTime);
                    showFBCallTime.setTextSize(10);
                    showFBCallTime.setTextColor(remoteVideoContainer.getContext().getResources().getColor(com.com.legend.ui.R.color.white));
                    showFBCallTime.setLayoutParams(tvParams);
                    showFBCallTime.setText(StringUtils.getString(R.string.call_wait_for_accept));
                }
            }
        }
        if (remoteVideoContainer == null) {
            mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
            mView.setOnTouchListener(createTouchListener());
            wm.addView(mView, params);
            showFBCallTime = (TextView) mView.findViewById(R.id.rc_time);
            ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
            if (!isVideoCall) {
                mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
            } else {
                showFBCallTime.setVisibility(View.GONE);
                mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
            }
        }
    }

    private static WindowManager.LayoutParams createLayoutParams(Context context) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < 24) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags =
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        return params;
    }

    private static View.OnTouchListener createTouchListener() {
        return new View.OnTouchListener() {
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                WindowManager.LayoutParams params =
                        (WindowManager.LayoutParams) v.getLayoutParams();
                if (params == null) {
                    return true;
                }
                if (tag == 0) {
                    oldOffsetX = params.x;
                    oldOffsetY = params.y;
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    // 减小偏移量,防止过度抖动
                    params.x += (int) (x - lastX) / 3;
                    params.y += (int) (y - lastY) / 3;
                    tag = 1;
                    wm.updateViewLayout(v, params);
                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = params.x;
                    int newOffsetY = params.y;
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20
                            && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                        if (!UiUtils.isFastDoubleClick()) {
                            onClickToResume();
                        }
                    } else {
                        tag = 0;
                    }
                }
                return true;
            }
        };
    }

    public static void showFloatBoxToCall(Context context, Bundle bundle,Boolean isVideoCall) {
        if (isShown) {
            return;
        }
        mContext = context;
        isShown = true;

        mBundle = bundle;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = createLayoutParams(context);

        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
        mView.setOnTouchListener(
                new View.OnTouchListener() {
                    float lastX, lastY;
                    int oldOffsetX, oldOffsetY;
                    int tag = 0;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int action = event.getAction();
                        float x = event.getX();
                        float y = event.getY();
                        if (tag == 0) {
                            oldOffsetX = params.x;
                            oldOffsetY = params.y;
                        }
                        if (action == MotionEvent.ACTION_DOWN) {
                            lastX = x;
                            lastY = y;
                        } else if (action == MotionEvent.ACTION_MOVE) {
                            // 减小偏移量,防止过度抖动
                            params.x += (int) (x - lastX) / 3;
                            params.y += (int) (y - lastY) / 3;
                            tag = 1;
                            if (mView != null) wm.updateViewLayout(mView, params);
                        } else if (action == MotionEvent.ACTION_UP) {
                            int newOffsetX = params.x;
                            int newOffsetY = params.y;
                            if (Math.abs(oldOffsetX - newOffsetX) <= 20
                                    && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                                if (!UiUtils.isFastDoubleClick()) {
                                    onClickToResume();
                                }
                            } else {
                                tag = 0;
                            }
                        }
                        return true;
                    }
                });
        wm.addView(mView, params);
        showFBCallTime = (TextView) mView.findViewById(R.id.rc_time);
        showFBCallTime.setVisibility(View.GONE);
        ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
        if (!isVideoCall) {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
        } else {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
        }
    }

    public static void hideFloatBox() {
        setExcludeFromRecents(mContext, false);
        if (isShown) {
            if (mView != null) {
                wm.removeView(mView);
            }
            mView = null;
            if (remoteVideoContainer != null) {
                wm.removeView(remoteVideoContainer);
            }
            remoteVideoContainer = null;
            if (null != timer) {
                timer.cancel();
                timer = null;
            }
            isShown = false;
            mView = null;
            mBundle = null;
            showFBCallTime = null;
        }
    }


    public static void onClickToResume() {
        // 当快速双击悬浮窗时，第一次点击之后会把mBundle置为空，第二次点击的时候出现NPE

        CallActivity.Companion.start(mContext, isIncomingCall);
    }

    /**
     * 设置app是否现在在最近列表中，
     *
     * @param appContext
     * @param excluded
     */
    private static void setExcludeFromRecents(Context appContext, boolean excluded) {
        if (appContext == null) return;
        ActivityManager manager =
                (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (ActivityManager.AppTask task : manager.getAppTasks()) {
                task.setExcludeFromRecents(excluded);
            }
        }
    }

    public static boolean isCallFloatBoxShown() {
        return isShown;
    }

    public static boolean isVideoCall() {
        return mIsVideoCall;
    }

    public static boolean isIncomingCall() {
        return isIncomingCall;
    }

    public static void updateSessionState(boolean connected) {
        isSessionConnected = connected;
    }
}
