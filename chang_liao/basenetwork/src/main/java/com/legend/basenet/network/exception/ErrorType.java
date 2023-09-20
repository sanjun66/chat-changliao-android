package com.legend.basenet.network.exception;

public enum ErrorType {

//    NETWORK_ERROR_OTHER(-2, "HTTP Exception"),
    NETWORK_ERROR_OTHER(-2, "网络异常"),
//    NETWORK_ERROR_404(404, "404 not found"),
    NETWORK_ERROR_404(404, "网络异常"),
//    NETWORK_ERROR_503(503, "503 Service Unavailable"),
    NETWORK_ERROR_503(503, "网络异常"),
//    NETWORK_TIMEOUT_ERROR(10001, "网络连接超时"),
    NETWORK_TIMEOUT_ERROR(10001, "网络异常"),
//    NETWORK_CONNECT_ERROR(10002, "网络连接失败"),
    NETWORK_CONNECT_ERROR(10002, "网络异常"),
    NETWORK_UNKNOWN_HOST_ERROR(10003, "网络异常"),
//    NETWORK_CERT_ERROR(10004, "签名校验异常"),
    NETWORK_CERT_ERROR(10004, "网络异常"),
    JSON_PARSE_ERROR(20000, "数据解析失败"),
//    ILLEGAL_ARGUMENTEXCEPTION(30000, "接口请求移除"),
    ILLEGAL_ARGUMENTEXCEPTION(30000, ""),
    KT_CANCLE_EXCEPTION(40000, ""),
    UNKNOWN_ERROR(-1, "未知错误");

    public void setMessage(String message) {
        this.message = message;
    }

    public int code;

    public String message;

    ErrorType(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
