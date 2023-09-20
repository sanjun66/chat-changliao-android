package com.legend.baseui.ui.adapter.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BaseRecyclerViewAdapter<H extends RecyclerView.ViewHolder, D> extends RecyclerView.Adapter {
    protected List<D> dataList;
    protected Context context;
    protected SparseArrayCompat<ViewBinder> binders;
    protected LayoutInflater mInflater;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    /**
     * 如果不设置OnItemClickListener将一直初始值或由调用层设置的值
     */
    private int mSelectIndex;

    public BaseRecyclerViewAdapter(Context context, ViewBinder... binders) {
        this(context, null, binders);
    }

    public BaseRecyclerViewAdapter(Context context, List<D> dataList, ViewBinder... binders) {
        init(binders);
        this.context = context;
        this.dataList = dataList;
        mInflater = LayoutInflater.from(context);
    }

    public void setViewBinder(ViewBinder binder) {
        if (null == binder)
            return;
        binders.append(binders.size(), binder);
    }

    public void setData(List<D> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void addData(D data) {
        if (null == data)
            return;
        if (null == dataList)
            dataList = new ArrayList<>();
        dataList.add(data);
        notifyDataSetChanged();
    }

    public void addData(List<D> dataList) {
        if (null == dataList || dataList.size() <= 0)
            return;
        if (null == this.dataList)
            this.dataList = new ArrayList<>();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public List<D> getDataList() {
        return dataList;
    }

    public void setSelectIndex(int selectIndex) {
        this.mSelectIndex = selectIndex;
    }

    public int getSelectIndex() {
        return mSelectIndex;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    @NonNull
    @Override
    public final H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return (H) binders.get(viewType).onCreateViewHolder(parent, mInflater, context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int i = 0, l = binders.size();
        D itemData = dataList.get(position);
        ViewBinder binder;
        for (; i < l; i++) {
            binder = binders.valueAt(i);
            if (binder.isMyType(itemData, position)) {
                binder.bind(holder, itemData, position, context, mSelectIndex);
                if (null != onItemClickListener) {
                    if (binder.supportOnItemClick()) {
                        holder.itemView.setOnClickListener(view -> {
                            mSelectIndex = position;
                            onItemClickListener.onItemClick(itemData, view, position);
                        });
                    } else {
                        holder.itemView.setOnClickListener(null);
                    }
                }

                if (null != onItemLongClickListener) {
                    if (binder.supportOnItemLongClick()) {
                        holder.itemView.setOnLongClickListener(view -> onItemLongClickListener.onItemLongClick(itemData, view, position));
                    } else {
                        holder.itemView.setOnClickListener(null);
                    }
                }
                return;
            }
        }
        throw new IllegalArgumentException(String.format("onBindViewHolder -> not match view processor, position : %s, data : %s", position, itemData.toString()));
    }

    @Override
    public final int getItemViewType(int position) {
        int i = 0, l = binders.size();
        D itemData = dataList.get(position);
        for (; i < l; i++) {
            if (binders.valueAt(i).isMyType(itemData, position))
                return binders.keyAt(i);
        }
        throw new IllegalArgumentException(String.format("getItemViewType -> not match view processor, position : %s, data : %s", position, itemData.toString()));
    }

    @Override
    public int getItemCount() {
        return null == dataList ? 0 : dataList.size();
    }

    private void init(ViewBinder... binders) {
        if (null == binders || binders.length <= 0)
            throw new IllegalArgumentException("binders can't is empty");

        this.binders = new SparseArrayCompat();
        int i = 0, l = binders.length;
        for ( ; i < l; i++) {
            this.binders.put(i, binders[i]);
        }
    }

    public interface OnItemClickListener<D> {
        void onItemClick(D data, View view, int position);
    }


    public interface OnItemLongClickListener<D> {
        boolean onItemLongClick(D data, View view, int position);
    }
}
