package cn.lockyluo.androidcv.utils

import android.app.Application
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.os.Process
import org.opencv.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author LuoTingWei
 * 功能 保存错误信息
 * @date 2019/1/26
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {
    val path = Environment.getExternalStorageDirectory().path + "/crash/"

    private object Holder {
        val INSTANCE = CrashHandler()
    }

    companion object {
        val instance: CrashHandler by lazy { Holder.INSTANCE }
    }

    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    private var mContext: Application? = null
    // 保存手机信息和异常信息
    private val mMessage = HashMap<String, String>()

    /**
     * 初始化默认异常捕获
     *
     * @param application context
     */
    fun init(application: Application) {
        mContext = application
        // 获取默认异常处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 将此类设为默认异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handleException(e)) {
            // 未经过人为处理,则调用系统默认处理异常,弹出系统强制关闭的对话框
            mDefaultHandler?.uncaughtException(t, e)
        } else {
            //已经人为处理,系统自己退出
            Thread.sleep(1000)
            System.exit(-1)
            Process.killProcess(Process.myPid())
        }
    }

    /**
     * 是否人为捕获异常
     *
     * @param e Throwable
     * @return true:已处理 false:未处理
     */
    private fun handleException(e: Throwable?): Boolean {
        if (e == null) {// 异常是否为空
            return false
        }

        val trueEx = if (e is RuntimeException && e.cause != null) {
            val cause = e.cause!!
            if (cause is InvocationTargetException) {
                cause.targetException ?: cause
            } else cause
        } else e
        Thread {
            // 在主线程中弹出提示
            Looper.prepare()
            ToastUtil.show("捕获到异常,将保存到/crash目录\n${trueEx.message}")
            Looper.loop()
        }.start()
        collectErrorMessages()
        saveErrorMessages(trueEx)
        return true
    }

    /**
     * 1.收集错误信息
     */
    private fun collectErrorMessages() {
        try {
            mMessage["versionName"] = BuildConfig.VERSION_NAME
            mMessage["versionCode"] = BuildConfig.VERSION_CODE.toString()
            // 通过反射拿到错误信息
            val fields = Build::class.java.fields
            if (fields.isNotEmpty()) {
                for (field in fields) {
                    field.isAccessible = true
                    try {
                        mMessage[field.name] = field.get(null).toString()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(-1)
        }
    }

    /**
     * 2.保存错误信息
     *
     * @param e Throwable
     */
    private fun saveErrorMessages(e: Throwable) {
        val sb = StringBuilder()
        for ((key, value) in mMessage) {
            sb.append(key).append("=").append(value).append("\n")
        }
        val writer = StringWriter()
        val pw = PrintWriter(writer)
        pw.use {
            e.printStackTrace(it)
            var cause: Throwable? = e.cause
            // 循环取出Cause
            while (cause != null) {
                cause.printStackTrace(it)
                cause = e.cause
            }
        }

        val result = writer.toString()
        sb.append(result)
        val time = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA).format(Date())
        val fileName = "crash-" + time + "_" + System.currentTimeMillis() + ".log"
        // 有无SD卡
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fos = FileOutputStream(path + fileName)
            fos.use {
                it.write(sb.toString().toByteArray())
            }
        }
    }


}
