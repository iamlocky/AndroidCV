package cn.lockyluo.androidcv.helper

import android.content.Context
import android.support.annotation.CallSuper
import android.support.annotation.RawRes
import cn.lockyluo.androidcv.exception.CVNotLoadedException
import cn.lockyluo.androidcv.utils.FileUtils
import cn.lockyluo.androidcv.utils.ToastUtil
import cn.lockyluo.androidcv.utils.logD
import org.bytedeco.javacpp.opencv_dnn
import org.bytedeco.javacpp.opencv_face
import org.opencv.R
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File

/**
 * opencv常用方法封装类
 *
 * 其他常用模块
 * [org.opencv.imgproc.Imgproc], [org.opencv.imgcodecs.Imgcodecs], [org.opencv.objdetect.Objdetect]
 *
 * LockyLuo
 *
 * 2019/2/29
 */
class OpenCVHelper(private val context: Context, onCvLoadCallback: (isSucceed:Boolean) -> Unit) {
    private val cvLoadCallback = CvLoadCallback(context, onCvLoadCallback)//每次onResume时会回调

    @Volatile var isCVLoaded = false
    private set

    /**
     * 如果openCV没加载则抛出异常
     */
    @Throws(CVNotLoadedException::class)
    private fun checkCVState() {
        if (!isCVLoaded) {
            throw CVNotLoadedException()
        }
    }


    @Synchronized fun init(){
        if (!isCVLoaded) {
            isCVLoaded = OpenCVLoader.initDebug()
        }
    }

    /**
     * 获取人脸检测类,默认使用TYPE_LBP
     * @param faceClassifierType
     * [FaceDetectionHelper.TYPE_HAAR],[FaceDetectionHelper.TYPE_LBP]
     */
    fun getFaceDetectionHelper(faceClassifierType: Int = FaceDetectionHelper.TYPE_LBP): FaceDetectionHelper {
        checkCVState()
        return FaceDetectionHelper(context, this, faceClassifierType)
    }

    fun matRgb2Gray(rgba: Mat): Mat {
        checkCVState()
        val gray = Mat()
        Imgproc.cvtColor(rgba, gray,Imgproc.COLOR_RGB2GRAY)
        return gray
    }



    /**
     * 获取分类器
     * @param rawId 分类器资源id
     * 完整分类器文件位于/sdk/etc
     */
    fun getCascadeClassifier(@RawRes rawId: Int): CascadeClassifier {
        checkCVState()
        val cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE)
        val modelName = "${Math.abs(rawId)}.xml"
        val cascadeFile = FileUtils.copyFilesFromRaw(context, rawId, cascadeDir.path, modelName)
        return getCascadeClassifier(cascadeFile)
    }

    /**
     * 获取分类器
     * @param cascadeFile 分类器xml文件目录
     */
    fun getCascadeClassifier(cascadeFile: File): CascadeClassifier {
        checkCVState()
        var cascadeClassifier: CascadeClassifier
        cascadeFile.also { file ->
            cascadeClassifier = CascadeClassifier(file.absolutePath)
            cascadeClassifier.let {
                if (it.empty()) {
                    context.logD("cascade classifier 加载失败")
                } else {
                    context.logD("加载 cascade classifier from " + file.absolutePath)
                }
            }
        }
        return cascadeClassifier
    }


    /**
     * ---人脸检测
     *
     * 人脸LBP分类器
     */
    fun getFaceLBPCascadeClassifier(): CascadeClassifier {
        return getCascadeClassifier(R.raw.lbpcascade_frontalface_improved)
    }

    /**
     * 人脸Haar分类器
     */
    fun getFaceHaarCascadeClassifier(): CascadeClassifier {
        return getCascadeClassifier(R.raw.haarcascade_frontalface_alt2)
    }

    /**
     * 眼睛Haar分类器
     */
    fun getEyeHaarCascadeClassifier(): CascadeClassifier {
        return getCascadeClassifier(R.raw.haarcascade_eye)
    }

    /**
     * ---人脸识别模块，基于openCV2
     */
    fun getFisherFaceRecognizer(): opencv_face.FisherFaceRecognizer {
        return opencv_face.FisherFaceRecognizer.create()
    }

    fun getEigenFaceRecognizer(): opencv_face.EigenFaceRecognizer {
        return opencv_face.EigenFaceRecognizer.create()
    }

    fun getLBPHFaceRecognizer(): opencv_face.LBPHFaceRecognizer {
        return opencv_face.LBPHFaceRecognizer.create()
    }


    /**
     * 在activity的onResume中必须调用
     */
    @CallSuper
    fun onResume() {
        init()
        if (!isCVLoaded) {
            ToastUtil.show("Internal OpenCV library not found!")
            cvLoadCallback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED)
        } else {
            context.logD("OpenCV loaded successfully")
            cvLoadCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    /**
     * 封装BaseLoaderCallback的Kotlin DSL回调
     */
    class CvLoadCallback(context: Context, private val onCvLoadCallback: (Boolean) -> Unit) :
        BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            super.onManagerConnected(status)
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    onCvLoadCallback.invoke(true)
                }
                else -> {
                    onCvLoadCallback.invoke(false)
                    super.onManagerConnected(status)
                }
            }
        }
    }

}