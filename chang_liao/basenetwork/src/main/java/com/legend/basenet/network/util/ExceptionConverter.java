package com.legend.basenet.network.util;

import android.text.TextUtils;

import com.legend.basenet.network.exception.ApiException;
import com.legend.basenet.network.exception.ErrorType;
import com.legend.basenet.network.message.MessageCenter;
import com.legend.basenet.network.ErrorCodeInterceptorManager;
import com.legend.basenet.network.NetworkManager;
import com.google.gson.JsonParseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;

import javax.net.ssl.SSLHandshakeException;

import retrofit2.HttpException;

/**
 * Exception 转换类（将程序异常转换为用户友好异常）
 */
public class ExceptionConverter {

    public static final int CODE_TOKEN_EXPIRED = -200;

    public static ApiException convert(Throwable e) {

        if (e instanceof ApiException) {
            String message;
            // token失效 服务端全局异常错误等
            int code = ((ApiException) e).getCode();
            if (code == CODE_TOKEN_EXPIRED) {
                MessageCenter.sendTokenExpired(code);
                message = "";
            } else {
                ApiException exp = ErrorCodeInterceptorManager.getInstance().process(code);
                if (null != exp)
                    return exp;
                message = e.getMessage();
            }
            return new ApiException(code, message);
        }

        // 非业务错误
        ErrorType errorType = null;
        if (e instanceof HttpException) {
            // http error 404 503 etc.
            int code = ((HttpException) e).code();
            if (code == 404) {
                errorType = ErrorType.NETWORK_ERROR_404;
            } else if (code == 503) {
                errorType = ErrorType.NETWORK_ERROR_503;
            } else {
                errorType = ErrorType.NETWORK_ERROR_OTHER;
            }
        } else if (e instanceof SocketTimeoutException) {
            errorType = ErrorType.NETWORK_TIMEOUT_ERROR;
        } else if (e instanceof JsonParseException) {
            // json parse error
            errorType = ErrorType.JSON_PARSE_ERROR;
        } else if (e instanceof ConnectException) {
            // connect error
            errorType = ErrorType.NETWORK_CONNECT_ERROR;
        } else if (e instanceof UnknownHostException) {
            // Unknown Host
            errorType = ErrorType.NETWORK_UNKNOWN_HOST_ERROR;
        } else if (e instanceof SSLHandshakeException) {
            // SSLHandshakeException
            errorType = ErrorType.NETWORK_CERT_ERROR;
        } else if ( e instanceof CancellationException) {
            errorType = ErrorType.KT_CANCLE_EXCEPTION;
        } else {
            errorType = ErrorType.UNKNOWN_ERROR;
        }
        String message = "";
        if (NetworkManager.getInstance().isDebuggable()) {
            message += "\n [ code: " + errorType.code + ", cause: " + e.getMessage() + " ]";
        }

        if (!TextUtils.isEmpty(errorType.message)) {
            message = errorType.message;
        } else {
            message = errorType.message + message;
        }
        return new ApiException(errorType.code, message);
    }
}
