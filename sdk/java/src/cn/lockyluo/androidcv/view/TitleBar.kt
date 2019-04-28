package cn.lockyluo.androidcv.view

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView


import kotlinx.android.synthetic.main.titlebar_layout.view.*
import org.opencv.R

/**
 * @author LuoTingWei
 * 功能 标题栏
 * @date 2019/1/24
 */
class TitleBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    var mainView: View? = null
    lateinit var ivButtonLeft: AppCompatImageView
    lateinit var ivButtonRight: AppCompatImageView
    lateinit var tvTitle: TextView
    private val tagName = "TitleBar"

    init {
        initView()
    }

    fun setTitle(title: CharSequence) {
        tvTitle.text = title
    }

    fun setTitle(@StringRes title: Int) {
        tvTitle.setText(title)
    }

    /**
     * 功能： 显示返回按钮
     *
     * @author LuoTingWei
     * @date 2018/12/12
     */
    fun setShowBackButton(isShowBackButton: Boolean) {
        ivButtonLeft.visibility = if (isShowBackButton) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setShowRightButton(isShowRightButton: Boolean) {
        ivButtonRight.visibility =
                if (isShowRightButton) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
    }

    fun setOnLeftButtonClickListener(onLeftButtonClickListener: View.OnClickListener?) {
        if (onLeftButtonClickListener != null) {
            ivButtonLeft.setOnClickListener(onLeftButtonClickListener)
        }
    }

    fun setOnRightButtonClickListener(onRightButtonClickListener: View.OnClickListener?) {
        if (onRightButtonClickListener != null) {
            ivButtonRight.setOnClickListener(onRightButtonClickListener)
        }
    }

    fun setOnTitleClickListener(onTitleClickListener: View.OnClickListener?) {
        if (onTitleClickListener != null) {
            tvTitle.setOnClickListener(onTitleClickListener)
        }
    }

    private fun initView() {
        tag = tagName
        gravity = Gravity.CENTER
        orientation = LinearLayout.VERTICAL
        mainView = LayoutInflater.from(context).inflate(R.layout.titlebar_layout, this)
        ivButtonLeft = ivTitleBtnBack
        ivButtonRight = ivTitleBtnRight
        tvTitle = tvTitleName
    }


}
