package cn.lockyluo.androidcv.utils

import android.content.Context
import com.orhanobut.logger.Logger

/**
 * @author LockyLuo
 * @description: context扩展方法
 * @date 2019/2/24
 */

fun Context.logI(message: Any = "") {
    Logger.t(this::class.java.simpleName).i(message.toString())
}

fun Context.logD(message: Any? = "") {
    Logger.t(this::class.java.simpleName).d(message)
}

fun Context.logE(message: Any = "") {
    Logger.t(this::class.java.simpleName).e(message.toString())
}

fun Context.logWtf(message: Any = "") {
    Logger.t(this::class.java.simpleName).wtf(message.toString())
}