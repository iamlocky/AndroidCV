package cn.lockyluo.androidcv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import cn.lockyluo.androidcv.ApplicationCv


/**
 * @author LuoTingWei
 * 功能 toast工具类
 * @date 2018/12/12
 */
object ToastUtil {
    private var toast: Toast? = null
    private const val TAG = "ToastUtil"
    private var during = Toast.LENGTH_SHORT
    fun show(message: CharSequence) {
        show(ApplicationCv.getInstance(), message)
    }

    fun setDuring(type: Int) {
        during = type
    }

    @SuppressLint("ShowToast")
    fun show(context: Context, message: CharSequence) {
        context.logD("ToastUtil show $message")

        if (toast == null) {
            toast =
                Toast.makeText(context, message, during)
        } else {
            toast?.setText(message)
            toast?.duration = during
        }
        toast?.show()
    }

    /**
     * 功能： 返回一个Snackbar
     *
     * @author LuoTingWei
     * @date 2018/12/14
     */
    fun snackBarMake(
        parent: View,
        text: CharSequence,
        btnText: CharSequence?,
        onClickListener: View.OnClickListener?
    ): Snackbar {
        return if (btnText != null && onClickListener != null) {
            Snackbar.make(
                parent,
                text,
                Snackbar.LENGTH_LONG
            ).setAction(btnText, onClickListener)
        } else {
            Snackbar.make(
                parent,
                text,
                Snackbar.LENGTH_SHORT
            )
        }
    }
}
