# 网络库
## 使用
1. 添加依赖
```groovy
    def dependenciesConfig = rootProject.dependenciesConfig
    implementation dependenciesConfig.retrofit
    implementation dependenciesConfig.rxjava3
    implementation "com.basestonedata.lib:network:1.0.0"
```

2. 初始化
```java
NetworkManager
        .getInstance()
        .setBaseUrl("https://dev.xiaoxiangyoupin.com/v2/")
        .setDebuggable(true)
        .init();
```

3. 代码调用
```java
NetworkManager
        .getInstance()
        .getService(UserService.class)
        .login("xxx", "xx", "xx")
        .compose(BaseTransformer.applyTransform())
        .subscribe(new BaseObserver<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse body) {
                // do something
            }

            @Override
            public void onFailed(@NonNull Throwable e) {
                // do something
            }
        });
```

4. 动态新增header参数
```java
InterceptorManager.getInstance().addOrUpdateHeader("token", "xxx");
```

## TODO

1. X509 trust manager
2. CA certification
3. multi baseUrl support

