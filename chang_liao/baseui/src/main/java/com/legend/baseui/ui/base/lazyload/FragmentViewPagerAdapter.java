package com.legend.baseui.ui.base.lazyload;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import java.util.List;

/**
 * 通过BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT参数，结合viewpager 实现可实现懒加载
 * 如果 behavior 的值为 BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ，那么当前选中的 Fragment在 Lifecycle.State#RESUMED 状态 ，其他不可见的 Fragment 会被限制在Lifecycle.State#STARTED 状态。
 * @param <T>
 */
public class FragmentViewPagerAdapter<T extends Fragment> extends FragmentPagerAdapter {
    private List<T> fragments;
    private List<String> titles;

    public FragmentViewPagerAdapter(@NonNull FragmentManager fm, List<T> fragments) {
        this(fm, fragments, null);
    }

    @SuppressLint("WrongConstant")
    public FragmentViewPagerAdapter(@NonNull FragmentManager fm, List<T> fragments, List<String> titles) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
        this.titles = titles;
    }


    @Override
    public int getCount() {
        return null != fragments ? fragments.size() : 0;
    }



    @Override
    public T getItem(int position) {
        return fragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null == titles ? super.getPageTitle(position) : titles.get(position);
    }
}
