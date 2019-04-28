package cn.lockyluo.androidcv

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import cn.lockyluo.androidcv.utils.CrashHandler
import com.didichuxing.doraemonkit.DoraemonKit
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.opencv.BuildConfig

/**
 * 本sdk的Application，如果要使用ToastUtil,Logger等工具，需要设置manifest的Application下name标签为ApplicationCv，
 * 或者在你的Application的onCreate()内调用install传入，否则工具将无法使用
 */

class ApplicationCv : Application() {
    companion object {
        private var instance: Application? = null
        @JvmStatic
        fun getInstance(): Application {
            return instance ?: ApplicationCv()
        }

        fun install(application: Application) {
            instance = application
            CrashHandler.instance.init(application)
            Logger.addLogAdapter(object : AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
//                    .showThreadInfo(true)
//                    .methodCount(2)
//                    .methodOffset(6)
                    .tag("AndroidCVSdk")
                    .build()
            ) {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return BuildConfig.DEBUG
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        install(this)
        DoraemonKit.install(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
