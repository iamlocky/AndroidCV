package cn.lockyluo.androidcv.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * @author LuoTingWei
 * 功能 gson工具
 * @date 2018/12/26
 */
object GsonUtils {
    val gson = GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create()!!
    val prettyGson: Gson
        get() = GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().setPrettyPrinting().create()
}
