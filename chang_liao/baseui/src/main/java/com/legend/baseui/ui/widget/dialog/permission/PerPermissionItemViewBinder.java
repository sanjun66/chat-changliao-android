package com.legend.baseui.ui.widget.dialog.permission;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.legend.baseui.ui.adapter.recyclerview.ViewBinder;
import com.legend.baseui.ui.util.DensityUtil;

public class PerPermissionItemViewBinder extends ViewBinder<PerPermissionItemViewBinder.VH, PerRequestPermissionConstant.PerRequestItem, PerRequestPermissionConstant.PerRequestItem> {

    @Override
    public boolean isMyType(@NonNull PerRequestPermissionConstant.PerRequestItem data, int position) {
        return true;
    }

    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater, @NonNull Context context) {
        PerRequestPermissionItemView itemView = new PerRequestPermissionItemView(context);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = DensityUtil.dip2px(context, 21);
        itemView.setLayoutParams(layoutParams);
        return new VH(itemView);
    }

    @Override
    public void bind(@NonNull VH holder, @NonNull PerRequestPermissionConstant.PerRequestItem data, int position, @NonNull Context context, int selectIndex) {
        holder.permissionItemView.bindData(data);
    }

    public class VH extends RecyclerView.ViewHolder {
        private PerRequestPermissionItemView permissionItemView;
        public VH(@NonNull PerRequestPermissionItemView itemView) {
            super(itemView);
            permissionItemView = itemView;
        }
    }
}
