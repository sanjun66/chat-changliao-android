package com.legend.baseui.ui.widget.dialog.picker;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.com.legend.ui.R;
import com.legend.baseui.ui.util.DensityUtil;
import com.legend.baseui.ui.widget.dialog.UIBaseDialog;
import java.util.List;

public class SinglePickerDialog extends UIBaseDialog {
    private ItemAdapter adapter;
    private int selTextColor, unSelTextColor, selItemBg, unSelItemBg;

    private SinglePickerDialog(Context context, SinglePickerDialogBuilder builder) {
        super(context, R.style.ui_ActionSheetDialogStyle);
        selTextColor = Color.parseColor("#FA541C");
        unSelTextColor = Color.parseColor("#A6000000");
        selItemBg = Color.parseColor("#0A000000");
        unSelItemBg = Color.parseColor("#FFFFFF");
        initView(builder);
    }

    private void initView(SinglePickerDialogBuilder builder) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.ui_picker_dialog, null);

        int screenHeight = DensityUtil.getScreenHeight(getContext());
        int height = (int) (screenHeight * 0.42);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        view.setLayoutParams(layoutParams);
        setContentView(view);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = DensityUtil.getScreenWidth(getContext());
        lp.height = height;
        lp.dimAmount = 0.5f;
        getWindow().setWindowAnimations(R.style.ui_BottomToTopAnim);


        TextView tvLeft = findViewById(R.id.tv_left_text);
        TextView tvRightLeft = findViewById(R.id.tv_right_text);
        TextView tvTitle = findViewById(R.id.tv_title_text);

        tvLeft.setText(builder.leftText);
        tvLeft.setTextColor(builder.iLeftColor);
        tvLeft.setOnClickListener(v -> {
            if (null != builder.iTitleListener)
                builder.iTitleListener.leftClick(SinglePickerDialog.this, adapter.selIndex);
        });

        tvRightLeft.setText(builder.rightText);
        tvRightLeft.setTextColor(builder.iRightColor);
        tvRightLeft.setOnClickListener(v -> {
            if (null != builder.iTitleListener)
                builder.iTitleListener.rightClick(SinglePickerDialog.this, adapter.selIndex);
        });

        tvTitle.setText(builder.title);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter = new ItemAdapter(getContext()));
        adapter.setData(builder.items, builder.selIndex);
    }

    public static class SinglePickerDialogBuilder {
        private String rightText;
        private String rightColor;
        private String leftText;
        private String leftColor;
        private String title;
        private ITitleListener iTitleListener;
        private List<? extends IPickerItem> items;
        private String itemSelTextColor;
        private String itemUnSelTextColor;
        private int iLeftColor;
        private int iRightColor;
        private int iItemSelTextColor;
        private int iItemUnSelTextColor;
        private int selIndex;

        public SinglePickerDialogBuilder rightText(String text) {
            this.rightText = text;
            return this;
        }

        public SinglePickerDialogBuilder leftText(String text) {
            this.leftText = text;
            return this;
        }

        public SinglePickerDialogBuilder title(String text) {
            this.title = text;
            return this;
        }

        public SinglePickerDialogBuilder leftTitleColor(String color) {
            this.leftColor = color;
            return this;
        }

        public SinglePickerDialogBuilder rightTitleColor(String color) {
            this.rightColor = color;
            return this;
        }

        public SinglePickerDialogBuilder titleListener(ITitleListener listener) {
            this.iTitleListener = listener;
            return this;
        }

        public SinglePickerDialogBuilder items(List<? extends IPickerItem> items) {
            this.items = items;
            return this;
        }

        public SinglePickerDialogBuilder itemSelColor(String color) {
            this.itemSelTextColor = color;
            return this;
        }

        public SinglePickerDialogBuilder selIndex(int selIndex) {
            this.selIndex = selIndex;
            return this;
        }

        public SinglePickerDialog build(Context context) {
            iLeftColor = parseColor(leftColor, "#33000000");
            iRightColor = parseColor(rightColor, "#FA541C");
            iItemSelTextColor = parseColor(itemSelTextColor,"#FA541C");
            iItemUnSelTextColor = parseColor(itemUnSelTextColor, "#A6000000");

            return new SinglePickerDialog(context, this);
        }

        private int parseColor(String color, String defColor) {
            if (TextUtils.isEmpty(color))
                color = defColor;
            try {
                return Color.parseColor(color);
            } catch (IllegalArgumentException exp) {
                return Color.parseColor(defColor);
            }
        }

        public interface ITitleListener {
            void rightClick(SinglePickerDialog dialog, int index);

            void leftClick(SinglePickerDialog dialog, int index);
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
        private List<? extends IPickerItem> items;
        private LayoutInflater inflater;
        private int selIndex = -1;

        private ItemAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setData(List<? extends IPickerItem> items, int selIndex) {
            this.items = items;
            this.selIndex = selIndex;
            notifyDataSetChanged();
        }

        public int  setSelIndex() {
            return selIndex;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder((TextView) inflater.inflate(R.layout.ui_picker_item_view, parent, false));
        }

        @Override
        public int getItemCount() {
            return null == items ? 0 : items.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(items.get(position).getItemText());
            if (position == selIndex) {
                ((TextView) holder.itemView).setTextColor(selTextColor);
                ((TextView) holder.itemView).setBackgroundColor(selItemBg);
            } else {
                ((TextView) holder.itemView).setTextColor(unSelTextColor);
                ((TextView) holder.itemView).setBackgroundColor(unSelItemBg);
            }

            holder.itemView.setOnClickListener(v -> {
                int tempIndex = selIndex;
                selIndex = position;
                notifyItemChanged(tempIndex);
                notifyItemChanged(selIndex);
            });
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull TextView itemView) {
                super(itemView);
            }
        }
    }

    public interface IPickerItem {
        String getItemText();
    }
}
