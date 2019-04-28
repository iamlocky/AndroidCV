package cn.lockyluo.androidcv.activity

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.lockyluo.androidcv.utils.*
import cn.lockyluo.androidcv.view.TitleBar
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.titlebar_view.*
import org.opencv.R
import java.io.File
import java.lang.ref.WeakReference

/**
 * @author LockyLuo
 * @description: activity基类，封装了常用方法
 * @date 2019/1/1
 */
abstract class BaseActivity : AppCompatActivity(), IPostToUI {
    protected abstract val layoutId: Int
    @Throws(Exception::class) protected abstract fun initData()
    @Throws(Exception::class) protected abstract fun initView()
    private var mainHandler: Handler? = null
    protected lateinit var context: Context
    protected lateinit var titleBarView: TitleBar
    private var alertDialog: AlertDialog? = null
    protected var progressDialog: ProgressDialog? = null
    private var snackbarRef: WeakReference<Snackbar>? = null
    private var metrics: DisplayMetrics = DisplayMetrics()//屏幕分辨率

    companion object {
        @JvmField
        val sdPath: String = Environment.getExternalStorageDirectory().absolutePath + File.separator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        mainHandler = Handler(Looper.getMainLooper())
        requestPermission { permission ->
            if (!permission.granted) {
                toastShow("权限${permission.name}被拒绝")
                val intent = Intent()
                with(intent) {
                    //权限被拒绝时
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                    data = Uri.fromParts("package", packageName, null)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (layoutId > 0) {
            setContentView(layoutId)
            val windowManager = (applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            windowManager.defaultDisplay.also {
                //获取屏幕分辨率
                it.getRealMetrics(metrics)
            }
            titleBarView = titleBar
            titleBarView.ivButtonLeft.setOnClickListener {
                finish()
            }

            initData()
            initView()
        } else {
            logD("找不到id")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun getRxPermission(): RxPermissions {
        return RxPermissions(this)
    }

    /**
     * 检查权限
     */
    fun checkPermissionGranted(permission: String): Boolean {
        return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
    }

    /**
     * 请求权限
     */
    fun requestPermission(
        vararg permissions: String = Constant.Permission.permissions,
        callBack: (Permission) -> Unit
    ) {
        getRxPermission()
            .requestEach(*permissions)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY))
            .subscribe {
                callBack.invoke(it)
            }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        logI("onConfigurationChanged")
    }

    override fun onResume() {
        super.onResume()
        logI("onResume")
    }

    override fun onPause() {
        super.onPause()
        logI("onPause")
    }

    override fun onStop() {
        dismissDialog()
        dismissSnackbar()

        super.onStop()
        logI("onStop")
    }

    override fun onDestroy() {
        progressDialog = null
        alertDialog = null
        snackbarRef = null
        mainHandler?.removeCallbacksAndMessages(null)
        logI("onDestroy")
        super.onDestroy()
    }

    /**
     * 功能： 显示返回按钮
     *
     * @author LuoTingWei
     * @date 2018/12/12
     */
    protected fun setShowBackButton(isShowBackButton: Boolean) {
        titleBarView.setShowBackButton(isShowBackButton)
    }

    protected fun setShowRightButton(isShowRightButton: Boolean) {
        titleBarView.setShowRightButton(isShowRightButton)
    }

    /**
     *
     * 功能  主线程更新ui，destroy后清空message
     *
     * @author LuoTingWei
     * @date 2019/1/12
     */
    override fun postToUI(runnable: Runnable?) {
        if (mainHandler != null && runnable != null) {
            mainHandler?.post(runnable)
        } else {
            Logger.e("null, cancel post Runnable")
        }
    }

    fun toastShow(any: Any? = "") {
        postToUI {
            if (any != null) {
                ToastUtil.show(any.toString())
            } else {
                Logger.e("toastShow null!")
            }
        }
    }

    protected fun showProgressDialog(): ProgressDialog {
        if (progressDialog != null && progressDialog!!.isShowing) {

        } else {
            progressDialog = ProgressDialog(this)
            progressDialog!!.show()
        }
        return progressDialog!!
    }

    protected fun dismissDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        alertDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    private fun dismissSnackbar() {
        snackbarRef?.get()?.let {
            if (it.isShown) {
                it.dismiss()
            }
        }
    }

    /**
     *
     * 功能 可设置Message的进度对话框
     *
     * @author LuoTingWei
     */
    protected inner class ProgressDialog internal constructor(context: Context) :
        Dialog(context, android.R.style.Theme_Dialog) {
        private val tvMessage: TextView?

        init {
            val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog_layout, null)
            setContentView(view)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            tvMessage = view.findViewById<View>(R.id.tv_progress_dialog_message) as TextView
            setCanceledOnTouchOutside(false)
        }

        fun setMessage(message: CharSequence) {
            tvMessage?.text = message
        }
    }

    /**
     * 功能： 显示对话框
     *
     * @author LuoTingWei
     * @date 2018/12/12
     */
    protected fun showAlertDialog(title: String, message: String) {
        showAlertDialog(title, message, DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }, null)
    }

    @Suppress("DEPRECATION")
    protected fun showAlertDialog(
        title: String,
        message: String,
        onPositiveButtonListener: DialogInterface.OnClickListener?,
        onNegativeButtonListener: DialogInterface.OnClickListener?
    ) {
        dismissDialog()
        val builder = AlertDialog.Builder(this, 0)
        if (onPositiveButtonListener != null) {
            builder.setPositiveButton(android.R.string.ok, onPositiveButtonListener)
        }
        if (onNegativeButtonListener != null) {
            builder.setNegativeButton(android.R.string.cancel, onNegativeButtonListener)
        }
        alertDialog = builder.create()
        alertDialog?.apply {
            setTitle(title)
            setMessage(message)
            window?.apply {
                val layoutParams = attributes.apply {
                    width = metrics.widthPixels * 2 / 3
                    height = metrics.heightPixels * 2 / 3
                }
                attributes = layoutParams
            }
            show()
        }
    }

    /**
     *
     * 功能  透明状态栏
     *
     * @author LuoTingWei
     * @date 2019/1/21
     */
    private fun switchTransStatusBar(@ColorInt color: Int = Color.TRANSPARENT) {
        StatusBarUtils.colorNormalByColor(this, color)
    }

}