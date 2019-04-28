package cn.lockyluo.androidcv.view

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * @author LuoTingWei
 * 功能 支持高效点击监听的RecyclerView
 * @date 2019/1/21
 */
class RecyclerItemView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs), RecyclerView.OnItemTouchListener {

    private lateinit var mGestureDetector: GestureDetectorCompat
    var onItemClickListenerAction: ((RecyclerView.ViewHolder) -> Unit)? = null
    var onItemLongClickListenerAction: ((RecyclerView.ViewHolder) -> Unit)? = null

    init {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        mGestureDetector = GestureDetectorCompat(context, ItemTouchHelperGestureListener())
        addOnItemTouchListener(this)
    }


    override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(event)
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        mGestureDetector.onTouchEvent(event)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private inner class ItemTouchHelperGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val child = findChildViewUnder(e.x, e.y)
            if (child != null) {
                // 回调
                onItemClickListenerAction?.invoke(getChildViewHolder(child))
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val child = findChildViewUnder(e.x, e.y)
            if (child != null) {
                // 回调
                onItemLongClickListenerAction?.invoke(getChildViewHolder(child))
            }
        }
    }


    fun setOnItemClickListener(action: (RecyclerView.ViewHolder) -> Unit) {
        onItemClickListenerAction = action
    }

    fun setOnItemLongClickListener(action: (RecyclerView.ViewHolder) -> Unit) {
        if (!isLongClickable) {
            isLongClickable = true
        }
        onItemLongClickListenerAction = action
    }

}
