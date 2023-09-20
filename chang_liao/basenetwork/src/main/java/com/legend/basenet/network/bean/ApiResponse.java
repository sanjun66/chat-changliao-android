package com.legend.basenet.network.bean;

public class ApiResponse<T> {
    public static final int CODE_LOGIN_FIRST = 401;
    public static final int CODE_TOKEN_EXPIRED = 402;
    public static final int CODE_LOGIN_OTHER_DEVICE = 403;
    public T data;
    public T getData() {
        return data;
    }
    public String message;
    public int code;
    public String uuid;

    public boolean isSuccess() {
        return code == 200;
    }

    public boolean isTokenExpired(){
        return code == CODE_TOKEN_EXPIRED;
    }

    public boolean isLoginOtherDevice() {
        return code == CODE_LOGIN_OTHER_DEVICE || code == CODE_LOGIN_FIRST;
    }
}
