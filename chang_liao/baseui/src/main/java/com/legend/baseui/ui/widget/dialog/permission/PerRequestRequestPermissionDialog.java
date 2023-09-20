package com.legend.baseui.ui.widget.dialog.permission;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.com.legend.ui.R;
import com.legend.baseui.ui.adapter.recyclerview.BaseRecyclerViewAdapter;
import com.legend.baseui.ui.util.DensityUtil;

import java.util.List;

public class PerRequestRequestPermissionDialog extends Dialog implements View.OnClickListener {
    private TextView tvTips;
    private RecyclerView recyclerView;
    private BaseRecyclerViewAdapter adapter;

    private IPerRequestPermissionListener listener;

    public PerRequestRequestPermissionDialog(@NonNull Context context) {
        super(context, R.style.UI_Dialog);
        initView();
        initAttr();
    }

    private void initView() {

        setCanceledOnTouchOutside(false);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.ui_pre_request_permission_dialog, null);

        tvTips = view.findViewById(R.id.tv_tips);
        tvTips.setText(String.format("为了您正常使用%s，需要获取您的以下权限：", getAppName()));
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter = new BaseRecyclerViewAdapter(getContext(), new PerPermissionItemViewBinder()));

        registerEvent(view);

        setContentView(view);
    }

    private void registerEvent(View view) {
        view.findViewById(R.id.tv_cancel).setOnClickListener(this);
        view.findViewById(R.id.tv_confirm).setOnClickListener(this);
    }

    public void preRequest(List<PerRequestPermissionConstant.PerRequestItem> list, IPerRequestPermissionListener listener) {
        adapter.setData(list);
        this.listener = listener;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.tv_cancel) {
            if (null != listener)
                listener.denied();
            dismiss();
        } else if (vId == R.id.tv_confirm) {
            if (null != listener)
                listener.granted();
            dismiss();
        }
    }

    private String getAppName() {
        try {
            PackageManager packageManager = getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return getContext().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "App";
    }

    private void initAttr() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = DensityUtil.dip2px(getContext(), 295);
        window.setAttributes(attributes);
    }

    public interface IPerRequestPermissionListener {
        void granted();

        void denied();
    }
}
