package com.legend.basenet.network.coroutine

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.legend.base.Applications
import com.legend.basenet.network.bean.ApiResponse
import com.legend.basenet.network.exception.ApiException
import com.legend.basenet.network.message.MessageCenter
import com.legend.basenet.network.util.ExceptionConverter
import kotlinx.coroutines.*

typealias Block<T> = suspend (CoroutineScope) -> T
typealias Error = suspend (Exception) -> Unit
typealias Cancel = suspend (Exception) -> Unit

/**
 * 单个请求任务
 */
fun <T> ViewModel.request(
    block: suspend () -> ApiResponse<T>,
    success: (T) -> Unit,
    error: (ApiException) -> Unit = {}
): Job {
    return viewModelScope.launch {
        runCatching {
            //请求体
            withContext(Dispatchers.IO) {
                block()
            }
        }.onSuccess {
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it, { t -> success(t) }, error)
            }.onFailure { e ->
                error(ExceptionConverter.convert(e))
            }
        }.onFailure {
            error(ExceptionConverter.convert(it))
        }
    }
}

/**
 * fragment 异步请求
 */
fun Fragment.request(
    block: Block<Unit>,
    error: (ApiException) -> Unit = {}
): Job {
    return lifecycleScope.launch {
        try {
            withContext(Dispatchers.IO) {
                block.invoke(this)
            }
        } catch (e: Exception) {
            error(ExceptionConverter.convert(e))
        }
    }
}

/**
 * actvity 异步请求
 */
fun FragmentActivity.request(
    block: Block<Unit>,
    error: (ApiException) -> Unit = {}
): Job {
    return lifecycleScope.launch {
        try {
            withContext(Dispatchers.IO) {
                block.invoke(this)
            }
        } catch (e: Exception) {
            error(ExceptionConverter.convert(e))
        }
    }
}


suspend fun <T> LifecycleOwner.request(block: Block<Unit>,
                                       error: (ApiException) -> Unit = {}
): Job {
    return lifecycleScope.launch {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            error(ExceptionConverter.convert(e))
        }
    }
}
/**
 * launch 多个网络接口请求的请求处理
 */
fun ViewModel.launch(
    block: Block<Unit>,
    error: (ApiException) -> Unit = {}
): Job {
    return viewModelScope.launch {
        try {
            block.invoke(this)
        } catch (e: Exception) {
            error(ExceptionConverter.convert(e))
        }
    }
}

/**
 * 单个请求接口的block块
 */
fun <T> ViewModel.async(block: suspend () -> ApiResponse<T>): Deferred<ApiResponse<T>> {
    return viewModelScope.async {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    }
}


/**
 * 统一处理await报错
 */
suspend fun <T> ViewModel.awaitPlus(block: Deferred<ApiResponse<T>>, showErrorToast: Boolean = false): T? {
    var data: T? = null
    try {
        val response = block.await()
        if (response.isSuccess) {
            data = response.data
        } else if (response.isTokenExpired) {
            MessageCenter.sendTokenExpired(response.code)
        }
//        else if (response.isLoginOtherDevice) {
//
//        }
        response?.let {
            if (showErrorToast && !it.isSuccess && !it.message.isNullOrEmpty()) {
                Toast.makeText(Applications.getCurrent(), response.message, Toast.LENGTH_SHORT).show()
            }
        }

    } catch (e: java.lang.Exception) {
        error(ExceptionConverter.convert(e))
    }
    return data
}


/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
suspend fun <T> executeResponse(
    response: ApiResponse<T>,
    success: suspend CoroutineScope.(T) -> Unit,
    error: (ApiException) -> Unit
) {
    coroutineScope {
        when {
            response.isSuccess -> {
                success(response.data)
            }
            response.isTokenExpired || response.isLoginOtherDevice -> {
                MessageCenter.sendTokenExpired(response.code)
                error(
                    ApiException(
                        response.code,
                        ""
                    )
                )
            }
            else -> {
                error(
                    ExceptionConverter.convert(
                        ApiException(
                    response.code,
                    response.message
                )
                    ))
            }
        }
    }
}

/**
 * 请求结果过滤，判断请求服务器请求结果是否成功，不成功则会抛出异常
 */
fun <T> handleResponse(
    response: ApiResponse<T>
): T {
    if (response.isSuccess) {
        return response.data
    } else {
        error(
            ExceptionConverter.convert(
                ApiException(
                    response.code,
                    response.message
                )
            )
        )
    }
}


/**
 * 不依赖viewModel的请求
 */
fun <T> mainRequest(
    block: suspend () -> ApiResponse<T>,
    success: (T) -> Unit,
    error: (ApiException) -> Unit = {}
): Job {
    return MainScope().launch {
        runCatching {
            //请求体
            withContext(Dispatchers.IO) {
                block()
            }
        }.onSuccess {
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                executeResponse(it, { t -> success(t) }, error)
            }.onFailure { e ->
                error(ExceptionConverter.convert(e))
            }
        }.onFailure {
            error(ExceptionConverter.convert(it))
        }
    }
}
