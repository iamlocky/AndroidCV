package cn.lockyluo.androidcv.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author LuoTingWei
 * 功能 时间格式化
 * @date 2018/12/24
 */
object CalendarUtil {
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE)
    private val simpleDateFormatMillis = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINESE)
    private val simpleDateFormatMillisB = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_SSS", Locale.CHINESE)

    val dateTime: String
        get() = simpleDateFormat.format(Date())
    val dateTimeMillis: String
        get() = simpleDateFormatMillis.format(Date())
    val dateTimeMillisBottomLine: String
        get() = simpleDateFormatMillisB.format(Date())
}
