package com.legend.basenet.network;

import com.legend.basenet.network.exception.ApiException;

import io.reactivex.rxjava3.annotations.Nullable;

public class ErrorCodeInterceptorManager {

    private IErrorCodeInterceptor interceptor;
    private static volatile ErrorCodeInterceptorManager instance = null;

    private ErrorCodeInterceptorManager() {
    }

    public static ErrorCodeInterceptorManager getInstance() {
        if (instance == null) {
            synchronized (ErrorCodeInterceptorManager.class) {
                if (instance == null) {
                    instance = new ErrorCodeInterceptorManager();
                }
            }
        }
        return instance;
    }

    public void setInterceptor(IErrorCodeInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public ApiException process(int errCode) {
        if (null == interceptor)
            return null;
        return interceptor.process(errCode);
    }

    public interface IErrorCodeInterceptor {
        @Nullable ApiException process(int errCode);
    }
}
