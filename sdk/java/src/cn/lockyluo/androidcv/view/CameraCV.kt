package cn.lockyluo.androidcv.view

import android.content.Context
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import cn.lockyluo.androidcv.utils.logE
import cn.lockyluo.androidcv.utils.switchFlashLight
import kotlinx.android.synthetic.main.cameracv_view.view.*
import org.opencv.R
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCameraView
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat

/**
 * @author LockyLuo
 * @description: 封装好的CV相机
 * @date 2019/3/14
 */
@Suppress("DEPRECATION")
class CameraCV : FrameLayout {
    private var rgba: Mat? = null
    private var gray: Mat? = null
    private var onStartAction: ((Int, Int) -> Unit)? = null
    private var onStopAction: (() -> Unit)? = null
    private var onFrameAction: ((gray: Mat, rgba: Mat) -> Mat?)? = null
    var javaCameraView: JavaCameraView
    lateinit var camera: Camera
    var tvFps: TextView //fps TextView
    private val mainHandler = Handler(Looper.getMainLooper())


    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.cameracv_view, this)
        javaCameraView = cvJavaCameraView
        tvFps = cvTvFps

        if (attrs != null) {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.CameraCV)

            if (styledAttrs.getBoolean(R.styleable.CameraCV_show_fps_cv, true)) {
                javaCameraView.isShowFps = true
            }
            javaCameraView.mCameraIndex =
                styledAttrs.getInt(R.styleable.CameraCV_camera_id_cv, CameraBridgeViewBase.CAMERA_ID_FRONT)
            styledAttrs.recycle()
        }

        initListener()
    }

    fun switchFlashLight(cameraFlashMode: Boolean): Boolean {
        var status = false
        if (javaCameraView.mCamera != null) {
            status = javaCameraView.switchFlashLight(javaCameraView.mCamera, cameraFlashMode)
        } else {
            context.logE("mCamera is null")
        }

        return status
    }

    private fun initListener() {

        javaCameraView.setCvCameraViewListener(object : CameraBridgeViewBase.CvCameraViewListener2 {
            override fun onCameraViewStarted(width: Int, height: Int) {
                camera=javaCameraView.mCamera
                setOnClickListener {
                    autoFocus()
                }
                gray = Mat()
                rgba = Mat(width, height, CvType.CV_8UC3)

                onStartAction?.invoke(width, height)
                mainHandler.post {
                    if (javaCameraView.isShowFps) {
                        tvFps.visibility = View.VISIBLE
                    } else {
                        tvFps.visibility = View.GONE
                    }
                }
            }

            override fun onCameraViewStopped() {
                gray?.release()
                rgba?.release()
                onStopAction?.invoke()
            }

            override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
                if (javaCameraView.isShowFps) {
                    mainHandler.post { tvFps.text = javaCameraView.fpsString }
                }

                inputFrame?.let {
                    gray = it.gray()
                    rgba = it.rgba()

                    if (gray != null && rgba != null) {
                        //使前置的图像也是正的
                        if (javaCameraView.cameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT) {
                            Core.flip(rgba, rgba, 1)
                            Core.flip(gray, gray, 1)
                        }
                        return onFrameAction?.invoke(gray!!, rgba!!)
                    }
                }
                return rgba
            }
        })
    }

    /**
     * 自动对焦
     */
    @Synchronized fun autoFocus(){
        val parameters=camera.parameters
        if (javaCameraView.focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)
            ||javaCameraView.focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            iv_focused.visibility= View.VISIBLE
            parameters.focusMode=Camera.Parameters.FOCUS_MODE_AUTO
            camera.parameters=parameters
            camera.autoFocus { success, _ ->
                iv_focused.visibility= View.GONE
                parameters.focusMode=Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                camera.parameters=parameters
            }
        }
    }

    fun onCameraViewStarted(action: (width: Int, height: Int) -> Unit) {
        onStartAction = action
    }

    fun onCameraViewStopped(action: () -> Unit) {
        onStopAction = action
    }

    fun onCameraFrame(action: (gray: Mat, rgba: Mat) -> Mat?) {
        onFrameAction = action
    }

    fun enableView() {
        javaCameraView.enableView()
    }

    fun disableView() {
        javaCameraView.disableView()
    }

}