package com.legend.base.app;

import android.app.Activity;
import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AppManager {

    private static final String TAG = "AppManager";

    private static volatile AppManager instance = null;

    private List<Activity> mActivityList;
    private Activity mCurrentActivity;

    public static AppManager getInstance() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    public List<Activity> getActivityList() {
        if (mActivityList == null) {
            mActivityList = new LinkedList<>();
        }
        return mActivityList;
    }

    /**
     * 添加 Activity 到集合
     * @param activity 添加的Activity
     */
    public void addActivity(Activity activity) {
        synchronized (AppManager.class) {
            List<Activity> activities = getActivityList();
            if (!activities.contains(activity)) {
                activities.add(activity);
            }
        }
    }

    /**
     * 删除集合里的指定的 Activity 实例
     * @param activity 要删除的Activity
     */
    public void removeActivity(Activity activity) {
        if (mActivityList == null) {
            Log.d(TAG, "mActivityList == null when removeActivity(Activity)");
            return;
        }
        synchronized (AppManager.class) {
            mActivityList.remove(activity);
        }
    }

    /**
     * 获取在前台的可见的Activity，如果应该后退回至后台或者息屏状态是currentActivity为null
     *
     * eg.
     * 使用场景比较适合[只需要在可见状态的Activity上]执行的操作
     * 如当后台 Service 执行某个任务时, 需要让前台Activity做出某种响应操作或其他操作,
     * 如弹出Dialog这时在 Service 中就可以使用 getCurrentActivity(),
     * 如果返回为 null 说明没有前台 Activity (用户返回桌面或者打开了其他 App 会出现此状况), 则不做任何操作,
     * 如果不为 null, 则弹出 Dialog
     *
     * @return Activity
     */
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        this.mCurrentActivity = activity;
    }

    /**
     * 获取栈顶 Activity
     *
     * 获取最近启动的一个 Activity, 此方法不保证获取到的 Activity 正处于前台可见状态
     * 即使 App 进入后台或在这个 Activity 中打开一个之前已经存在的 Activity, 这时调用此方法
     * 还是会返回这个最近启动的 Activity, 因此基本不会出现 null 的情况
     * 比较适合大部分的使用场景, 如 startActivity
     * @return 栈顶Activity
     */
    public Activity getTopActivity() {
        if (mActivityList != null && mActivityList.size() > 0) {
            return mActivityList.get(mActivityList.size() - 1);
        }
        return null;
    }

    /**
     * 关闭所有 Activity
     */
    public void killAll() {
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();
                iterator.remove();
                next.finish();
            }
        }
    }

    /**
     * 退出App
     */
    public void exitApp() {
        killAll();
        System.exit(0);
    }

    /**
     * 关闭所有 Activity，排除指定的 Activity
     *
     * @param excludeActivityClasses activity class
     */
    public void killAllExclude(Class<?>... excludeActivityClasses) {
        List<Class<?>> excludeList = Arrays.asList(excludeActivityClasses);
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();

                if (excludeList.contains(next.getClass())) {
                    continue;
                }

                iterator.remove();
                next.finish();
            }
        }
    }

    /**
     * 关闭所有 Activity，排除指定的 Activity
     *
     * @param excludeActivityName Activity 的完整全路径
     */
    public void killAllExclude(String... excludeActivityName) {
        List<String> excludeList = Arrays.asList(excludeActivityName);
        synchronized (AppManager.class) {
            Iterator<Activity> iterator = getActivityList().iterator();
            while (iterator.hasNext()) {
                Activity next = iterator.next();

                if (excludeList.contains(next.getClass().getName())) {
                    continue;
                }

                iterator.remove();
                next.finish();
            }
        }
    }

    public interface AppListener {
        void onForegroundToBackground();

        void onBackgroundToForeground();

        void onAppRestore();
    }

    private AppListener appListener;

    public void setAppListener(AppListener appListener) {
        this.appListener = appListener;
    }

    public AppListener getAppListener() {
        return appListener;
    }
}
