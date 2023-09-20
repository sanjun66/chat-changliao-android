package com.legend.base.utils.permission.listener;

public interface PermissionListener {
    /**
     * 预申请权限, 先调用BSDPermission.request(), 设置listener并重写preRequest, 如果需要申请权限, 会回调该方法, 业务层可以给出提示或做其他处理. 返回值为true时将中断权限申请流程, false将继续申请权限
     * 当需要再次申请的时候调用BSDPermission.requestNow, 该方法不会回调preRequest
     * @param permissions
     * @return
     */
    boolean preRequest(String[] permissions);

    /**
     * 权限申请成功
     * @param permissions
     */
    void granted(String[] permissions);

    /**
     * 权限申请失败
     * @param permissions 仅有被拒绝的权限
     */
    void denied(String[] permissions);
}
