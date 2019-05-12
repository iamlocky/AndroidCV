package cn.lockyluo.androidcv.helper

import android.content.Context
import android.graphics.Bitmap
import android.media.FaceDetector
import android.support.annotation.RawRes
import cn.lockyluo.androidcv.utils.DetectionResult
import cn.lockyluo.androidcv.utils.FileUtils
import cn.lockyluo.androidcv.utils.GsonUtils
import cn.lockyluo.androidcv.utils.logD
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect

/**
 * LockyLuo
 * 检测帮助类基类
 *
 * 2019/3/23
 */
open class BaseDetectionHelper(val context: Context, private val openCVHelper: OpenCVHelper) {
    private var lastTime = System.currentTimeMillis()
    private var time: Long = 0
    protected val stringBuilderTime = StringBuilder()

    init {
        if (!openCVHelper.isCVLoaded) {
            openCVHelper.init()
        }
    }

    /**
     * 计时
     */
    protected fun countTime(tag: String? = null) {
        time = System.currentTimeMillis()
        if (tag.isNullOrEmpty()) {
            stringBuilderTime.clear()
            stringBuilderTime.append("TimeMillis\n")
        } else {
            stringBuilderTime.append("$tag: ${time - lastTime}\n")
        }
        lastTime = time
    }

    /**
     * 通用的检测方法
     */
    @JvmOverloads
    fun detect(
        cascadeClassifier: CascadeClassifier,
        gray: Mat,
        rgba: Mat,
        detectRotation: Int = 0,
        scaleFactor: Double = 1.1,
        minNeighbors: Int = 3,
        flags: Int = Objdetect.CASCADE_DO_CANNY_PRUNING,
        minSize: Size = Size(),
        maxSize: Size = Size(),
        isEqualizeHist: Boolean = true,
        isShowMark: Boolean = true,
        markColor: Scalar = Scalar(0.0, 255.0, 127.0, 127.0),//BGR
        thickness: Int = 2
    ): DetectionResult {
        countTime()

        if (isEqualizeHist) {//直方图均衡化
            Imgproc.equalizeHist(gray, gray)
        }

        countTime("equalizeHist")

        val rotateCode = when (detectRotation) {//旋转灰度图
            90 -> Core.ROTATE_90_CLOCKWISE
            180 -> Core.ROTATE_180
            270, -90 -> Core.ROTATE_90_COUNTERCLOCKWISE
            else -> -1
        }
        if (rotateCode != -1) {
            Core.rotate(gray, gray, rotateCode)
            countTime("rotate gray")
        }

        val matOfRect = MatOfRect()

        cascadeClassifier.detectMultiScale(gray, matOfRect, scaleFactor, minNeighbors, flags, minSize, maxSize)
        countTime("detect")

        val resultRects = matOfRect.toArray()
        if (isShowMark) {
            resultRects.forEach {
                Imgproc.rectangle(rgba, it.tl(), it.br(), markColor, thickness)
            }
            countTime("rectangle")
        }

        return DetectionResult(gray, rgba, resultRects, stringBuilderTime.toString())
    }


    val classNames = arrayOf(
        "background",
        "aeroplane",
        "bicycle",
        "bird",
        "boat",
        "bottle",
        "bus",
        "car",
        "cat",
        "chair",
        "cow",
        "diningtable",
        "dog",
        "horse",
        "motorbike",
        "person",
        "pottedplant",
        "sheep",
        "sofa",
        "train",
        "tvmonitor"
    )

    /**
     * 获取深度神经网络
     */
    fun getDnnFromCaffe(@RawRes prototxtId: Int, @RawRes caffemodelId: Int): Net {
        val dnnDir = context.getDir("dnn", Context.MODE_PRIVATE)
        val configurationFile =
            FileUtils.copyFilesFromRaw(context, prototxtId, dnnDir.path, "deployproto.prototxt").path
        val modelFile =
            FileUtils.copyFilesFromRaw(context,caffemodelId, dnnDir.path, "deploymodel.caffemodel").path
        return Dnn.readNetFromCaffe(configurationFile, modelFile)
    }

    /**
     * android提供的api，用于对比测试
     */
    fun androidFaceDetector(bitmap: Bitmap,maxFaces:Int=10):List<FaceDetector.Face>{
        countTime()
        val faceDetector=FaceDetector(bitmap.width,bitmap.height,maxFaces)
        var faceList = arrayOfNulls<FaceDetector.Face>(10)
        val nums=faceDetector.findFaces(bitmap,faceList)
        countTime("found $nums")
        val results= mutableListOf<FaceDetector.Face>()
        faceList.forEach { if (it!=null) results.add(it) }
        context.logD("$stringBuilderTime\n"+GsonUtils.gson.toJson(results))
        return results
    }

}