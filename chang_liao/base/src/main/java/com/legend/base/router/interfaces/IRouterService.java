package com.legend.base.router.interfaces;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * 通用路由服务接口
 * 所有需要通过RouterManager管理的service【必须实现该接口】
 * 可作为【业务逻辑路由】做一些页面页面跳转的前置判断
 * 使用场景：相当于通过路由调用原生方法
 *
 * execute() 方法为RouterService默认执行的方法
 */
public interface IRouterService extends IProvider {
    void execute(Bundle extras);
}
