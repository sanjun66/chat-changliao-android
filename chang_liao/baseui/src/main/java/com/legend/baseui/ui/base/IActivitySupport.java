package com.legend.baseui.ui.base;

import androidx.fragment.app.Fragment;

public interface IActivitySupport {

    /**
     * 添加Fragment到容器
     * @param container .
     * @param fragment  .
     */
    void replaceFragment(int container, Fragment fragment);

    /**
     * 添加Fragment到容器
     * @param container     .
     * @param fragmentPath  Fragment ARouter Path
     */
    void replaceFragment(int container, String fragmentPath);

    /**
     * 添加Fragment到容器并设置转场动画
     * @param container .
     * @param fragment  .
     */
    void replaceFragment(int container, Fragment fragment, int inAnim, int outAnim);

}
