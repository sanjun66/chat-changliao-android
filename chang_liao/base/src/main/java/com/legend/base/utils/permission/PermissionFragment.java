package com.legend.base.utils.permission;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.legend.base.utils.EmptyUtils;
import com.legend.base.utils.permission.listener.PermissionListener;

import java.util.ArrayList;
import java.util.List;


public class PermissionFragment extends Fragment {
    public static final String TAG = "BSDPermissionFragment";

    private static final int REQUEST_CODE = 10001;

    public static final String REQUEST_PERMISSIONS = "request_permissions";

    private PermissionListener listener;
    private String[] permissions;

    private boolean onResume = false;

    public static PermissionFragment newInstance(String[] permissions) {
        PermissionFragment legoPermissionFragment = new PermissionFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(REQUEST_PERMISSIONS, permissions);
        legoPermissionFragment.setArguments(bundle);
        return legoPermissionFragment;
    }

    public void setListener(PermissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() == null || getActivity() == null) return;
        String[] stringArrayList = getArguments().getStringArray(REQUEST_PERMISSIONS);
        if (stringArrayList != null && stringArrayList.length > 0) {
            if (onResume) {
                return;
            }
            this.permissions = stringArrayList;
            onResume = true;
            requestPermissions(permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (null == listener)
                return;

            if (EmptyUtils.isEmpty(permissions) || EmptyUtils.isEmpty(grantResults) || permissions.length != grantResults.length) {
                listener.denied(this.permissions);
                return;
            }

            int index = 0;
            List<String> deniedList = null;
            for (String permission : permissions) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    if (null == deniedList)
                        deniedList = new ArrayList<>();
                    deniedList.add(permission);
                }
                index++;
            }
            if (EmptyUtils.isEmpty(deniedList)) {
                listener.granted(permissions);
            } else {
                listener.denied(deniedList.toArray(new String[deniedList.size() - 1]));
            }
        }
    }
}
