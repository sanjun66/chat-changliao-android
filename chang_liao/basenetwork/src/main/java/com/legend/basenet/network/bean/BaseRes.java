package com.legend.basenet.network.bean;

public class BaseRes {
    public String message;
    public boolean isSuccess;

    public BaseRes(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
