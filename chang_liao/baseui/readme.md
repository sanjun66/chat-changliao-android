# Global UI Library
`全局UI库`（需要与UI界定好哪些UI是全局样式，将全局样式UI放入该库中）
通用式组件只关注自身组件样式的实现，该library中添加的UI组件必须是**通用的，可扩展的，与业务隔离的**

__命名规范__
所有组件类名以 `UI` 开头如： `UILoadingView` 
所有layout、drawable、values等资源名以 `ui_` 开头

## Base Page
- BaseActivity
    使用说明:
        1. 继承BaseActivity, 实现getLayoutId(), initView()两个抽象方法
        2. 需要修改页面显示配置(默认隐藏系统标题栏, 沉浸式状态栏), 需要重写initPageConfig(ActivityPageConfig pageConfig)方法给pageConfig设置相关属性
             2.1 页面配置开启下拉刷新和下滑加载默认数据时, 需要重写onRefresh()和onLoadMore()两个方法
             2.2 页面配置开启显示自定义标题栏时, 在initView之后可设置左右两边按钮icon,标题以及背景颜色
        3. 首次加载数据时可调用showLoading()方法显示loading页, 数据加载成功后可以调用showContent()显示内容页或showEmpty显示空白提示页, 数据加载失败时可以调用showError()显示失败页                 
           * 如果需要自定义样式, 调用show之前, 调用对应的set方法, 如loadingPage.setLoading()
        4. 继承至BaseActivity的类可选择性指定泛型<P extends BasePresenter>, BaseActivity会自动构造这个Presenter, 如果需要自定义可重写initPresenter()
            * 如果页面中有数据获取相关逻辑, 可重写getService,返回数据处理服务类, 可以在Presenter中以service.调用相关方法
      
- BaseFragment 
   1. 继承BaseFragment, 实现getLayoutId(), initView()两个抽象方法
        2. 需要修改页面显示配置, 需要重写initPageConfig(FragmentPageConfig pageConfig)方法给pageConfig设置相关属性
             2.1 页面配置开启下拉刷新和下滑加载默认数据时, 需要重写onRefresh()和onLoadMore()两个方法
             2.2 页面配置开启显示自定义标题栏时, 在initView之后可设置左右两边按钮icon,标题以及背景颜色, titleBar.setLeftIcon()
        3. 首次加载数据时可调用showLoading()方法显示loading页, 数据加载成功后可以调用showContent()显示内容页或showEmpty显示空白提示页, 数据加载失败时可以调用showError()显示失败页                 
           * 如果需要自定义样式, 调用show之前, 调用对应的set方法, 如loadingPage.setLoading()
        4. 继承至BaseFragment的类可选择性指定泛型<P extends BasePresenter>, FragmentActivity会自动构造这个Presenter, 如果需要自定义可重写initPresenter()
            * 如果页面中有数据获取相关逻辑, 可重写getService,返回数据处理服务类, 可以在Presenter中以service.调用相关方法
- BaseListActivity 
- BaseListFragment 
- etc.

## Widget
#### pull refresh、load more
- SmartRefreshLayout
    使用说明:   https://github.com/scwang90/SmartRefreshLayout

#### Dialog
- message dialog
- tip dialog
- LoadingDialog
        使用说明 : new LoadingDialog(Context context).show();
                    canBackPressed(), 设置是否可以返回键关闭, 默认为true
- bottom sheet dialog

#### Toast
- LegoToastUtils
    使用说明: LegoToastUtils.show

#### Font

#### Common Icons

#### Title Bar
- BSDTitleBar
    使用说明: xml布局中放到顶端
        setLeftIcon, 设置左边icon
        setRightIcon, 设置右边icon
        setTitle, 设置标题
        setBackground, 设置背景颜色

#### ProgressBar

#### Empty View (Loading Layout)
- LoadingPage
    使用说明: 根布局使用LoadingPage
                showContent() 显示内容页
                showLoading() 显示加载页
                showError()   显示错误页
                showEmpty()   显示空白提示页
                setLoading()  设置自定义加载页
                setError()    设置自定义错误页
                setEmpty()    设置自定义空白提示页
            
     

#### Safe Keyboard

#### BaseAdapter
- BaseRecyclerViewAdapter
    使用说明: Recycler.setAdapter(new  BaseRecyclerViewAdapter(Context context, ViewBinder... binders))
        ViewBinder, 每个条目的绑定器
            必须实现, isMyType(), 确定当前条目是否由这个绑定器处理,
                     onCreateViewHolder(), 创建当前条目ViewHolder
                     bind() , 条目绑定数据
            可续实现:
                     supportOnItemClick(), 条目是否支持单机事件, 默认为true
                     supportOnItemLongClick(), 条目是否支持长按事件, 默认为true
                     
               
            setOnItemClickListener(), 设置条目点击事件
            setOnItemLongClickListener(), 设置条目长按事件
            setData(), 设置数据
            addData(), 新增数据(用于分页加载)
                     
