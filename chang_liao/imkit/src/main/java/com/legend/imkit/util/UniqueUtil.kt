package com.legend.imkit.util

import java.util.UUID

/**
 *
 *
 * @Author: young
 * @Date: 2023/6/28 22:13
 */
object UniqueUtil {
    fun getRandomUUID(): String {
        return UUID.randomUUID().toString()
    }
}