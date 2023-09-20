package com.legend.baseui.ui.widget.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import androidx.annotation.IntDef;
import com.com.legend.ui.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UIDialogAction {

    @IntDef({ACTION_PROP_NEGATIVE, ACTION_PROP_NEUTRAL, ACTION_PROP_POSITIVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Prop {
    }

    //用于标记positive/negative/neutral
    public static final int ACTION_PROP_POSITIVE = 0;
    public static final int ACTION_PROP_NEUTRAL = 1;
    public static final int ACTION_PROP_NEGATIVE = 2;

    private CharSequence mStr;
    private int mIconResId;
    private int mProp = ACTION_PROP_NEUTRAL;
    private Button mButton;
    private ActionListener mActionListener;

    public UIDialogAction(CharSequence str) {
        this.mStr = str;
    }

    public UIDialogAction(String str, ActionListener mActionListener) {
        this.mStr = str;
        this.mActionListener = mActionListener;
    }

    public UIDialogAction(int strRes, ActionListener mActionListener) {
        this.mStr = mStr;
        this.mActionListener = mActionListener;
    }

    public UIDialogAction setStr(String str) {
        this.mStr = str;
        return this;
    }

    public UIDialogAction setIconResId(int iconResId) {
        this.mIconResId = iconResId;
        return this;
    }

    public UIDialogAction setProp(int prop) {
        this.mProp = prop;
        return this;
    }

    public UIDialogAction setActionListener(ActionListener actionListener) {
        this.mActionListener = actionListener;
        return this;
    }



    public interface ActionListener {
        void onClick(UIDialog dialog, int index);
    }

    public Button buildActionView(final UIDialog dialog, final int index) {
        mButton = generateActionButton(dialog.getContext(), mStr, mIconResId);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionListener != null && mButton.isEnabled()) {
                    mActionListener.onClick(dialog, index);
                }
            }
        });
        return mButton;
    }

    private Button generateActionButton(Context context, CharSequence text, int iconResId) {
        Button button = new Button(context);
        button.setText(String.valueOf(mStr));
        button.setBackground(null);
        button.setMinHeight(0);
        button.setMinimumHeight(0);

        TypedArray a = context.obtainStyledAttributes(
                null, R.styleable.UIDialogActionStyleDef, R.attr.ui_dialog_action_style, 0);
        int count = a.getIndexCount();
        int paddingHor = 0, iconSpace = 0;
        ColorStateList negativeTextColor = null, positiveTextColor = null;
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.UIDialogActionStyleDef_android_gravity) {
                button.setGravity(a.getInt(attr, -1));
            } else if (attr == R.styleable.UIDialogActionStyleDef_android_textColor) {
                button.setTextColor(a.getColorStateList(attr));
            } else if (attr == R.styleable.UIDialogActionStyleDef_android_textSize) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(attr, 0));
            } else if (attr == R.styleable.UIDialogActionStyleDef_ui_dialog_action_button_padding_horizontal) {
                paddingHor = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.UIDialogActionStyleDef_android_background) {
                button.setBackground(a.getDrawable(attr));
            } else if (attr == R.styleable.UIDialogActionStyleDef_android_minWidth) {
                int miniWidth = a.getDimensionPixelSize(attr, 0);
                button.setMinWidth(miniWidth);
                button.setMinimumWidth(miniWidth);
            } else if (attr == R.styleable.UIDialogActionStyleDef_ui_dialog_positive_action_text_color) {
                positiveTextColor = a.getColorStateList(attr);
            } else if (attr == R.styleable.UIDialogActionStyleDef_ui_dialog_negative_action_text_color) {
                negativeTextColor = a.getColorStateList(attr);
            } else if (attr == R.styleable.UIDialogActionStyleDef_ui_dialog_action_icon_space) {
                iconSpace = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.UITextCommonStyleDef_android_textStyle) {
                int styleIndex = a.getInt(attr, -1);
                button.setTypeface(null, styleIndex);
            }
        }
        a.recycle();

        button.setPadding(paddingHor, 0, paddingHor, 0);
        if (iconResId <= 0) {
            button.setText(text);
        } else {
            // TODO: 2020/7/1 有 icon 的 button
            // add span with icon
        }

        if (mProp == ACTION_PROP_NEGATIVE) {
            button.setTextColor(negativeTextColor);
        } else if (mProp == ACTION_PROP_POSITIVE) {
            button.setTextColor(positiveTextColor);
        }

        button.setClickable(true);

        return button;
    }
}
