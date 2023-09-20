package com.legend.baseui.ui.adapter.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public abstract class  ViewBinder<H extends RecyclerView.ViewHolder, D, B> {
    protected final String TAG = getClass().getSimpleName();

    private List<D> dataList;

    public abstract boolean isMyType(@NonNull B data, int position);

    public abstract H onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LayoutInflater inflater, @NonNull Context context);

    public abstract void bind(@NonNull H holder, @NonNull D data, int position, @NonNull Context context, int selectIndex);

    public boolean supportOnItemClick() {
        return true;
    }

    public boolean supportOnItemLongClick() {
        return true;
    }

    public void setDataList(List<D> dataList) {
        this.dataList = dataList;
    }

    public List<D> getDataList() {
        return dataList;
    }

    public int getDataLength() {
        return null == dataList ? 0 : dataList.size();
    }
}
