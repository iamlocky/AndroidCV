package cn.lockyluo.androidcv.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.FrameLayout
import org.opencv.R
import java.lang.reflect.InvocationTargetException


/**
 * Created by LockyLuo on 2018/1/23.
 */

object StatusBarUtils {

    fun colorNormal(activity: Activity, @ColorRes color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4.2~4.4W
            try {
                val release = Build.VERSION.RELEASE.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                val last = Integer.parseInt(release[release.size - 1])
                if (last >= 2) {
                    setStatusBarColorOldSDK(activity, color)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.+~
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            val colorInt = ContextCompat.getColor(activity, color)
            window.statusBarColor = colorInt
            window.navigationBarColor = colorInt
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.+~
            val window = activity.window
            window.decorView.systemUiVisibility = View.VISIBLE
        }
    }

    fun setNavigationBarColor(window: Window, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = color
        }
    }

    fun colorNormalByColor(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4.2~4.4W
            try {
                val release = Build.VERSION.RELEASE.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val last = Integer.parseInt(release[release.size - 1])
                if (last >= 2) {
                    setStatusBarColorOldSDKByColor(activity, color)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.+~
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
            window.navigationBarColor = color
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.+~
            val window = activity.window
            window.decorView.systemUiVisibility = View.VISIBLE
        }
    }


    fun colorLight(activity: Activity, @ColorRes color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4.2~4.4W
            try {
                val release = Build.VERSION.RELEASE.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val last = Integer.parseInt(release[release.size - 1])
                if (last >= 2) {
                    setStatusBarColorOldSDK(activity, R.color.colorPrimary)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //5.+
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = activity.resources.getColor(R.color.colorPrimary)
            window.navigationBarColor = ActivityCompat.getColor(activity, R.color.colorPrimary)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.+~
            //浅色背景时设置深色字体,仅安卓6.0以上支持
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ActivityCompat.getColor(activity, color)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.navigationBarColor = ActivityCompat.getColor(activity, R.color.colorPrimary)
        }
    }

    fun colorLightByColor(activity: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4.2~4.4W
            try {
                val release = Build.VERSION.RELEASE.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val last = Integer.parseInt(release[release.size - 1])
                if (last >= 2) {
                    setStatusBarColorOldSDK(activity, R.color.colorPrimary)
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //5.+
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = activity.resources.getColor(R.color.colorPrimary)
            window.navigationBarColor = ActivityCompat.getColor(activity, R.color.colorPrimary)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.+~
            //浅色背景时设置深色字体,仅安卓6.0以上支持
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.navigationBarColor = ActivityCompat.getColor(activity, R.color.colorPrimary)
        }
    }

    fun setStatusBarColorOldSDK(activity: Activity, @ColorRes colorId: Int) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val decorViewGroup = activity.window.decorView as ViewGroup
        val statusBarView = View(activity)
        val statusBarHeight = getStatusBarHeight(activity)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight)
        params.gravity = Gravity.TOP
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(activity.resources.getColor(colorId))
        decorViewGroup.addView(statusBarView)

        val mContentView = activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            mChildView.fitsSystemWindows = true
        }
    }

    fun setStatusBarColorOldSDKByColor(activity: Activity, @ColorInt color: Int) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val decorViewGroup = activity.window.decorView as ViewGroup
        val statusBarView = View(activity)
        val statusBarHeight = getStatusBarHeight(activity)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight)
        params.gravity = Gravity.TOP
        statusBarView.layoutParams = params
        statusBarView.setBackgroundColor(color)
        decorViewGroup.addView(statusBarView)

        val mContentView = activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            mChildView.fitsSystemWindows = true
        }
    }

    private fun getStatusBarHeight(context: Context): Int {
        var statusBarHeight = 0
        val res = context.resources
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    fun showMenuButton(window: Window) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            showNavigationIceCreamSandwich(window)
        } else {
            showNavigationLollipopMR1(window)
        }
    }

    /**
     * 显示虚拟导航栏菜单按钮.
     * Android 4.0 - Android 5.0
     * API 14 - 21
     *
     * @param window [Window]
     */
    private fun showNavigationIceCreamSandwich(window: Window) {
        try {
            val flags = WindowManager.LayoutParams::class.java.getField("FLAG_NEEDS_MENU_KEY").getInt(null)
            window.addFlags(flags)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

    }

    /**
     * 显示虚拟导航栏菜单按钮.
     * Android 5.1.1 - Android 7.0
     * API 22 - 25
     *
     * @param window [Window]
     */
    private fun showNavigationLollipopMR1(window: Window) {
        try {
            val setNeedsMenuKey =
                Window::class.java.getDeclaredMethod("setNeedsMenuKey", Int::class.javaPrimitiveType!!)
            setNeedsMenuKey.isAccessible = true
            val value = WindowManager.LayoutParams::class.java.getField("NEEDS_MENU_SET_TRUE").getInt(null)
            setNeedsMenuKey.invoke(window, value)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }
}
