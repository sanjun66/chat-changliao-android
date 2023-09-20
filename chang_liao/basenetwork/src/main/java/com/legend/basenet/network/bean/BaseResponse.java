package com.legend.basenet.network.bean;

public class BaseResponse<T> {
    public static final int CODE_TOKEN_EXPIRED = -200;
    public T data;

    public String msg;
    public int code;

    public boolean isSuccess() {
        return code == 200;
    }
    public boolean isTokenExpired() {
        return code == CODE_TOKEN_EXPIRED;
    }
}
