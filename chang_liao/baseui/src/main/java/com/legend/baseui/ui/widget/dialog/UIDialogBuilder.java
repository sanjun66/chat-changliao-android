package com.legend.baseui.ui.widget.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.com.legend.ui.R;
import com.legend.baseui.ui.widget.UITextView;
import com.legend.baseui.ui.widget.UIWrapContentScrollView;
import com.legend.baseui.ui.layout.UILinearLayout;
import com.legend.baseui.ui.util.DensityUtil;
import com.legend.baseui.ui.util.UIResHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class UIDialogBuilder<T extends UIDialogBuilder<?>> {

    private UIDialog mDialog;
    private String mTitle;
    private List<UIDialogAction> mActions;
    private Context mBaseContext;
    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;
    private float mWidthScale = 0.65f;
    private float mHeightScale = 0.75f;

    public UIDialogBuilder(Context context) {
        this.mBaseContext = context;
        mActions = new ArrayList<>();
    }

    public Context getBaseContext() {
        return mBaseContext;
    }

    /**
     * 设置消息框顶部标题
     * @param mTitle title content
     * @return {@link #UIDialogBuilder}
     */
    @SuppressWarnings("unchecked")
    public T setTitle(String mTitle) {
        if (!TextUtils.isEmpty(mTitle)) {
            this.mTitle = String.format(mTitle + "%s",
                    getBaseContext().getResources().getString(R.string.ui_tool_fix_ellipsize));
        }
        return (T) this;
    }

    /**
     * 设置消息框顶部标题
     * @param res title string resource
     * @return {@link #UIDialogBuilder}
     */
    public T setTitle(int res) {
        return setTitle(getBaseContext().getResources().getString(res));
    }

    /**
     * 设置是否可取消
     * @param cancelable cancelable
     * @return {@link #UIDialogBuilder}
     */
    @SuppressWarnings("unchecked")
    public T setCancelable(boolean cancelable) {
        mCancelable = cancelable;
        return (T) this;
    }

    /**
     * 设置是否可触摸外部取消
     * @param canceledOnTouchOutside canceledOnTouchOutside
     * @return {@link #UIDialogBuilder}
     */
    @SuppressWarnings("unchecked")
    public T setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.mCanceledOnTouchOutside = canceledOnTouchOutside;
        return (T) this;
    }

    /**
     * 添加 Dialog Action
     * @param action action
     * @return @return {@link #UIDialogBuilder}
     */
    @SuppressWarnings("unchecked")
    public T addAction(UIDialogAction action) {
        if (action != null) {
            mActions.add(action);
        }

        return (T) this;
    }

    /**
     * 添加普通操作按钮
     *
     * @param str        文案
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(CharSequence str, UIDialogAction.ActionListener listener) {
        addAction(0, str, listener);
        return (T) this;
    }

    /**
     * 添加普通操作按钮
     *
     * @param strResId   文案
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    private T addAction(int strResId, UIDialogAction.ActionListener listener) {
        addAction(0, strResId, listener);
        return (T) this;
    }

    /**
     * 添加普通操作按钮
     *
     * @param str        文案
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(CharSequence str, @UIDialogAction.Prop int prop, UIDialogAction.ActionListener listener) {
        addAction(0, str, prop, listener);
        return (T) this;
    }

    /**
     * 添加普通操作按钮
     *
     * @param iconResId  图标
     * @param str        文案
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(int iconResId, CharSequence str, UIDialogAction.ActionListener listener) {
        addAction(iconResId, str, UIDialogAction.ACTION_PROP_NEUTRAL, listener);
        return (T) this;
    }

    /**
     * 添加普通操作按钮
     *
     * @param iconResId  图标
     * @param res        文案
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(int iconResId, int res, UIDialogAction.ActionListener listener) {
        addAction(iconResId, mBaseContext.getResources().getString(res), listener);
        return (T) this;
    }

    /**
     * 添加操作按钮
     *
     * @param iconResId  图标
     * @param str        文案
     * @param prop       属性
     * @param listener   点击回调事件
     */
    @SuppressWarnings("unchecked")
    public T addAction(int iconResId, CharSequence str, @UIDialogAction.Prop int prop, UIDialogAction.ActionListener listener) {
        UIDialogAction action = new UIDialogAction(str)
                .setProp(prop)
                .setIconResId(iconResId)
                .setActionListener(listener);
        mActions.add(action);
        return (T) this;
    }

    /**
     * 判断对话框是否显示标题
     *
     * @return 是否有title
     */
    protected boolean hasTitle() {
        return !TextUtils.isEmpty(mTitle);
    }

    public UIDialog create() {
        return create(R.style.UI_Dialog);
    }

    /**
     * 创建对话框
     *
     * @return 对话框
     */
    public UIDialog create(@StyleRes int style) {
        // create dialog
        mDialog = new UIDialog(getBaseContext(), style);
        Context dialogContext = mDialog.getContext();

        // dialog root view
        UIDialogView dialogView = new UIDialogView(dialogContext);
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.setBackgroundColor(Color.WHITE);
        dialogView.setRadius(20);

        // create title
        View titleLayout = onCreateTitleLayout(mDialog);

        // create content
        View contentLayout = wrapWithScroll(onCreateContentLayout(mDialog));

        // create operator
        View operatorLayout = onCreateOperatorLayout(mDialog);


        // check and set id
        checkAndSetId(titleLayout, R.id.ui_dialog_title_layout_id);
        checkAndSetId(operatorLayout, R.id.ui_dialog_operator_layout_id);
        checkAndSetId(contentLayout, R.id.ui_dialog_content_layout_id);

        // inner layout
        if (titleLayout != null) {
            LinearLayout.LayoutParams lp = onCreateTitleLayoutParams();
            dialogView.addView(titleLayout, lp);
        }

        if (contentLayout != null) {
            ViewGroup.LayoutParams lp = onCreateContentLayoutParams();
            dialogView.addView(contentLayout, lp);
        }

        if (operatorLayout != null) {
            LinearLayout.LayoutParams lp = onCreateOperatorLayoutLayoutParams();
            dialogView.addView(operatorLayout, lp);
        }

        int dialogWidth = (int) (DensityUtil.getScreenWidth(dialogContext) * mWidthScale);

        mDialog.setContentView(dialogView,
                new ViewGroup.LayoutParams(
                        dialogWidth,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

        mDialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        mDialog.setCancelable(mCancelable);
        return mDialog;
    }

    /**
     * 检查view是否设置id如果没有则设置对应id
     * @param view view
     * @param id 设置的id
     */
    private void checkAndSetId(View view, int id) {
        if (view != null && view.getId() == View.NO_ID) {
            view.setId(id);
        }
    }

    @NonNull
    protected LinearLayout.LayoutParams onCreateTitleLayoutParams() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    protected View onCreateTitleLayout(UIDialog mDialog) {
        if (hasTitle()) {
            UITextView textView = new UITextView(mDialog.getContext());
            textView.setId(R.id.ui_dialog_title_layout_id);
            textView.setText(mTitle);
            UIResHelper.assignTextViewWithAttr(textView, R.attr.ui_dialog_title_style);
            return textView;
        }

        return null;
    }

    @NonNull
    protected LinearLayout.LayoutParams onCreateOperatorLayoutLayoutParams() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 创建操作区域布局
     * @param mDialog dialog
     * @return 操作按钮区域布局
     */
    protected View onCreateOperatorLayout(UIDialog mDialog) {
        final Context context = mDialog.getContext();
        int size = mActions.size();
        if (size > 0) {
            TypedArray a = context.obtainStyledAttributes(null,
                    R.styleable.UIDialogActionContainerCustomDef,
                    R.attr.ui_dialog_action_container_style, 0);
            int count = a.getIndexCount();
            int justifyContent = 1;
            int spaceCustomIndex = 0;
            int actionHeight = -1;
            int actionSpace = 0;
            int actionThickness = 0;
            int actionContainerTopThickness = 0;
            int dividerColor = 0;
            for (int i = 0; i < count; i++) {
                int attr = a.getIndex(i);

                if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_container_justify_content) {
                    // <enum name="start" value="0" />
                    // <enum name="end" value="1" />
                    // <enum name="stretch" value="2" />
                    // <enum name="custom" value="3" />
                    justifyContent = a.getInteger(attr, justifyContent);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_container_custom_space_index) {
                    spaceCustomIndex = a.getInteger(attr, 0);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_space) {
                    actionSpace = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_height) {
                    actionHeight = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_divider_thickness) {
                    actionThickness = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_action_container_top_divider_thickness) {
                    actionContainerTopThickness = a.getDimensionPixelSize(attr, 0);
                } else if (attr == R.styleable.UIDialogActionContainerCustomDef_ui_dialog_divider_color) {
                    dividerColor = a.getColor(attr, ContextCompat.getColor(context, R.color.ui_dialog_action_divider_color));
                }
            }
            a.recycle();

            // 非stretch模式下 空白撑满填充区域位置
            int spaceInsertPos = -1;
            if (justifyContent == 0) {
                // start 模式 space 在最后位置撑满
                spaceInsertPos = size;
            } else if (justifyContent == 1) {
                // end 模式 space 在最开始位置撑满
                spaceInsertPos = 0;
            } else if (justifyContent == 3) {
                // custom 模式 space 在自定义索引位置撑满
                spaceInsertPos = spaceCustomIndex;
            }

            final UILinearLayout actionWrapperLayout = new UILinearLayout(mDialog.getContext());
            actionWrapperLayout.setId(R.id.ui_dialog_content_layout_id);
            actionWrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
            actionWrapperLayout.onlyShowTopDivider(0, 0, actionContainerTopThickness, dividerColor);

            for (int i = 0; i < size; i++) {
                if (spaceInsertPos == i) {
                    actionWrapperLayout.addView(createActionContainerSpace(context));
                }

                LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, actionHeight);
                if (spaceInsertPos >= 0) {
                    if (i >= spaceInsertPos) {
                        actionLp.leftMargin = actionSpace;
                    } else {
                        actionLp.rightMargin = actionSpace;
                    }
                }
                if (justifyContent == 2) {
                    actionLp.width = 0;
                    actionLp.weight = 1;
                }

                UIDialogAction action = mActions.get(i);
                Button actionView = action.buildActionView(mDialog, i);

                // add action divider
                // action分割线线条粗细>0 && 不是第一个位置 && 不是space插入位置 (综合下来就是不是第一个位置的action view)
                if (actionThickness > 0 && i > 0 && spaceInsertPos != i) {
                    // 展示action左侧的分割线
                    // actionView.onlyShowLeftDivider();

                    // 添加分割线
                    View lineView = new View(context);
                    lineView.setBackgroundColor(dividerColor);
                    LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(actionThickness, LinearLayout.LayoutParams.MATCH_PARENT);
                    actionWrapperLayout.addView(lineView, lineLp);

                }

                actionWrapperLayout.addView(actionView, actionLp);
            }

            return actionWrapperLayout;
        }

        return null;
    }

    private View createActionContainerSpace(Context context) {
        Space space = new Space(context);
        LinearLayout.LayoutParams spaceLp = new LinearLayout.LayoutParams(0, 0);
        spaceLp.weight = 1;
        space.setLayoutParams(spaceLp);
        return space;
    }

    @NonNull
    protected ViewGroup.LayoutParams onCreateContentLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 创建内容区域 layout
     * @param mDialog dialog
     * @return 内容区域布局
     */
    protected abstract View onCreateContentLayout(UIDialog mDialog);


    /**
     * scrollView 包裹 contentView（限制内容高度不超过最大高度）
     * @param view contentView
     * @return scrollView
     */
    private ScrollView wrapWithScroll(@NonNull View view){
        int dialogHeight = (int) (DensityUtil.getScreenWidth(view.getContext()) * mHeightScale);
        UIWrapContentScrollView scrollView = new UIWrapContentScrollView(view.getContext());
        scrollView.setMaxHeight(dialogHeight);
        scrollView.addView(view);
        scrollView.setVerticalScrollBarEnabled(false);
        return scrollView;
    }
}
