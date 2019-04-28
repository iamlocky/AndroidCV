package cn.lockyluo.androidcv.helper

import android.content.Context
import android.support.annotation.RawRes
import cn.lockyluo.androidcv.utils.DetectionResult
import cn.lockyluo.androidcv.utils.FileUtils
import cn.lockyluo.androidcv.utils.logD
import org.opencv.R
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect


/**
 * LockyLuo
 *
 * 人脸检测Helper
 *
 * @param faceClassifierType 分类器类型
 *
 * 2019/4/1
 */
class FaceDetectionHelper(
    context: Context,
    private val openCVHelper: OpenCVHelper,
    private var faceClassifierType: Int
) : BaseDetectionHelper(context, openCVHelper) {

    companion object {
        //人脸检测分类器类型
        const val TYPE_HAAR = 0
        const val TYPE_LBP = 1
    }


    private val faceRectColor = Scalar(0.0, 255.0, 0.0, 255.0)//BGR 绿色
    private val eyeRectColor = Scalar(0.0, 255.0, 255.0, 255.0)//BGR 浅蓝色
    private var faceCascadeClassifier: CascadeClassifier? = null
    private var eyeCascadeClassifier: CascadeClassifier? = null


    var relativeMinFaceScale = 0.20f //脸大小相对于屏幕高度的最小比例
    var absoluteMinFaceSize = 0 //脸的最小值
    var isShowMark: Boolean = true //是否用框标记检测结果

    init {
        switchFaceClassifier(faceClassifierType)
        eyeCascadeClassifier = openCVHelper.getEyeHaarCascadeClassifier()
    }

    /**
     * 切换人脸检测分类器
     */
    fun switchFaceClassifier(faceClassifierType: Int) {
        this.faceClassifierType = faceClassifierType
        faceCascadeClassifier = if (faceClassifierType == TYPE_HAAR) {
            openCVHelper.getFaceHaarCascadeClassifier()
        } else {
            openCVHelper.getFaceLBPCascadeClassifier()
        }
    }


    /**
     * 人脸检测方法
     * 设横屏角度为标准0，顺时针角度加，则竖屏为90，反向横屏为180，反向竖屏为270
     * 为提高检测效果，将会对灰度图进行旋转，并将检测结果坐标进行换算，在原rgb图上画框
     *
     * @param inputFrame [CameraBridgeViewBase]帧
     */
    @JvmOverloads
    fun detectFace(
        inputFrame: CameraBridgeViewBase.CvCameraViewFrame?,
        detectRotation: Int = 0,
        enableDetectEye: Boolean = false,
        scaleFactor: Double = 1.1,
        minNeighbors: Int = 3,
        flags: Int = Objdetect.CASCADE_DO_CANNY_PRUNING,
        minSize: Size = Size(),
        maxSize: Size = Size(),
        isEqualizeHist: Boolean = true,
        cameraBridgeType: Int = CameraBridgeViewBase.CAMERA_ID_BACK
    ): DetectionResult {
        var result = DetectionResult()
        inputFrame?.let {
            result =
                detectFace(
                    it.rgba(),
                    it.gray(),
                    detectRotation,
                    enableDetectEye,
                    scaleFactor,
                    minNeighbors,
                    flags,
                    minSize,
                    maxSize,
                    isEqualizeHist,
                    true,
                    cameraBridgeType
                )
        }
        return result
    }

    /**
     * 人脸检测方法
     * @param rgba Mat
     * @param gray Mat
     * @param detectRotation 检测时的灰度图旋转方向，不影响输出的rgb图
     */
    @JvmOverloads
    fun detectFace(
        rgba: Mat,
        gray: Mat,
        detectRotation: Int = 0,
        enableDetectEye: Boolean = true,
        scaleFactor: Double = 1.1,
        minNeighbors: Int = 3,
        flags: Int = Objdetect.CASCADE_DO_CANNY_PRUNING,
        minSize: Size = Size(),
        maxSize: Size = Size(),
        isEqualizeHist: Boolean = true,
        isCamera: Boolean = false,
        cameraBridgeType: Int = CameraBridgeViewBase.CAMERA_ID_BACK
    ): DetectionResult {
        if (minSize.empty() && isCamera) {
            //使用摄像头时，根据图像大小计算脸的min阈值
            val height = Math.max(gray.rows(), gray.cols())
            context.logD("gray:height ${gray.rows()} width ${gray.cols()}")
            if (Math.round(height * relativeMinFaceScale) > 0) {
                absoluteMinFaceSize = Math.round(height * relativeMinFaceScale)
                minSize.height = absoluteMinFaceSize.toDouble()
                minSize.width = absoluteMinFaceSize.toDouble()
            }
        }
        countTime()

        if (isEqualizeHist) {//直方图均衡化
            Imgproc.equalizeHist(gray, gray)
        }

        countTime("equalizeHist")

//        //使前置的图像也是正的
//        if (cameraBridgeType == CameraBridgeViewBase.CAMERA_ID_FRONT) {
//            Core.flip(rgba, rgba, 1)
//            Core.flip(gray, gray, 1)
//            countTime("flip CAMERA_ID_FRONT")
//        }

        val faces = MatOfRect()

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

        //调用分类器的检测方法
        faceCascadeClassifier?.detectMultiScale(gray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize)

        countTime("detect face")

        //框出检测结果
        val facesArray = faces.toArray()
        if (facesArray.isNotEmpty()) {
            for (i in 0 until facesArray.size) {
                if (isCamera && i > 0) {
                    continue
                }
                facesArray[i].let { face ->
                    val faceSquareSize = Math.max(face.width, face.height)//初略确定脸的大小
                    val faceStartPoint =
                        if (isCamera) Point(face.tl().y, rgba.height() - face.br().x) else face.tl()//换算旋转后的起始点坐标
                    val faceEndPoint = if (isCamera) Point(
                        faceStartPoint.x + face.height,
                        faceStartPoint.y + face.width
                    ) else face.br()//换算旋转后的结束点坐标

                    if (isShowMark) {
                        Imgproc.rectangle(rgba, faceStartPoint, faceEndPoint, faceRectColor, 2)
                        countTime("rectangle face($i)")
                    }

                    if (enableDetectEye) {//检测到脸才开始检测眼睛
                        detectEyes(
                            rgba,
                            gray,
                            0,
//                            flags = flags,
                            minSize = Size(faceSquareSize / 8.0, faceSquareSize / 8.0),//设置最小检测阈值
                            maxSize = Size(faceSquareSize.toDouble() / 2, faceSquareSize.toDouble() / 2),//设置最大检测阈值
                            isCamera = isCamera
                        )
                    }
                }
            }

        }
        context.logD(stringBuilderTime.toString())

        return DetectionResult(gray, rgba, facesArray, stringBuilderTime.toString())
    }

    /**
     * 眼睛检测方法
     */
    @JvmOverloads
    fun detectEyes(
        rgba: Mat,
        gray: Mat,
        detectRotation: Int = 0,
        scaleFactor: Double = 1.1,
        minNeighbors: Int = 8,
        flags: Int = Objdetect.CASCADE_DO_CANNY_PRUNING,
        minSize: Size = Size(1.0, 1.0),
        maxSize: Size = Size(),
        isCamera: Boolean
    ): DetectionResult {
        val eyes = MatOfRect()

        eyeCascadeClassifier?.detectMultiScale(
            gray,
            eyes,
            scaleFactor,
            minNeighbors,
            flags,
            minSize,
            maxSize
        )

        countTime("detect eyes")

        val rotateCode = when (detectRotation) {
            90 -> Core.ROTATE_90_CLOCKWISE
            180 -> Core.ROTATE_180
            270, -90 -> Core.ROTATE_90_COUNTERCLOCKWISE
            else -> -1
        }
        if (rotateCode != -1) {
            Core.rotate(gray, gray, rotateCode)
        }

        val eyesArray = eyes.toArray()
        eyesArray.sortWith(Comparator { o1, o2 -> o1.y - o2.y })//按y坐标排序
        //标记眼睛框
        if (eyesArray.isNotEmpty() && isShowMark) {
            for (i in 0 until eyesArray.size) {
//                if (i > 1) {//跳过多余的结果
//                    continue
//                }
                val eyeStartPoint =
                    if (isCamera) Point(
                        eyesArray[i].tl().y,
                        rgba.height() - eyesArray[i].br().x
                    ) else eyesArray[i].tl()//如果是摄像头，换算旋转后的坐标
                val eyeEndPoint =
                    if (isCamera) Point(
                        eyeStartPoint.x + eyesArray[i].height,
                        eyeStartPoint.y + eyesArray[i].width
                    ) else eyesArray[i].br()

                Imgproc.rectangle(rgba, eyeStartPoint, eyeEndPoint, eyeRectColor, 1)
            }
            countTime("rectangle eyes")
        }

        return DetectionResult(gray, rgba, eyesArray, stringBuilderTime.toString())
    }



    /**
     * 深度神经网络检测样例
     */
    @JvmOverloads
    open fun detectFaceDnn(net: Net, rgb: Mat, detectRotation: Int = 0,threshold:Double=0.8): DetectionResult {
        countTime()
        val IN_WIDTH = 300//模型大小
        val IN_HEIGHT = 300
        val WH_RATIO = IN_WIDTH.toFloat() / IN_HEIGHT
        val IN_SCALE_FACTOR = 1.0
        val MEAN_VAL = 127.5
        val THRESHOLD = threshold
        // Get a new rgb
        val rotateCode = when (detectRotation) {
            90 -> Core.ROTATE_90_CLOCKWISE
            180 -> Core.ROTATE_180
            270, -90 -> Core.ROTATE_90_COUNTERCLOCKWISE
            else -> -1
        }
        if (rotateCode != -1) {
            Core.rotate(rgb, rgb, rotateCode)
            countTime("rotate rgb")
        }
        Imgproc.cvtColor(rgb, rgb, Imgproc.COLOR_RGBA2RGB)
        countTime("cvtColor rgb")
        // Forward image through network.
        val blob = Dnn.blobFromImage(
            rgb, IN_SCALE_FACTOR,
            Size(IN_WIDTH.toDouble(), IN_HEIGHT.toDouble()),
            Scalar(104.0, 177.0, 123.0)
        )
        countTime("blobFromImage")

        net.setInput(blob)
        countTime("net.setInput")

        var detections = net.forward()
        countTime("net.forward")

        var cols = rgb.width()
        var rows = rgb.height()

        val cropSize = if (cols.toFloat() / rows > WH_RATIO) {
            Size((rows * WH_RATIO).toDouble(), rows.toDouble())
        } else {
            Size(cols.toDouble(), (cols / WH_RATIO).toDouble())
        }
        val y1 = (rows - cropSize.height).toInt() / 2
        val y2 = (y1 + cropSize.height).toInt()
        val x1 = (cols - cropSize.width).toInt() / 2
        val x2 = (x1 + cropSize.width).toInt()
        val subFrame = rgb.submat(y1, y2, x1, x2)
        cols = subFrame.cols()
        rows = subFrame.rows()

        detections = detections.reshape(1, detections.total().toInt() / 7)
        val rects=mutableListOf<Rect>()
        for (i in 0 until detections.rows()) {
            val confidence = detections.get(i, 2)[0]
            if (confidence > THRESHOLD) {
                val classId = detections.get(i, 1)[0].toInt()//类型id
                val left = (detections.get(i, 3)[0] * cols)
                val top = (detections.get(i, 4)[0] * rows)
                val right = (detections.get(i, 5)[0] * cols)
                val bottom = (detections.get(i, 6)[0] * rows)
                // Draw rectangle around detected object.
                rects.add(Rect(left.toInt(),top.toInt(),(right-left).toInt(),(bottom-top).toInt()))//添加结果

                Imgproc.rectangle(
                    subFrame, Point(left, top),
                    Point(right, bottom),
                    Scalar(0.0, 255.0, 0.0), 2
                )

//                val label = classNames[classId] + ": " + String.format("%.3f",confidence)
                val label = "face: " + String.format("%.3f", confidence)
                val baseLine = IntArray(1)
                val labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine)
                // Draw background for label.
                Imgproc.rectangle(
                    rgb, Point(left, top - labelSize.height),
                    Point(left + labelSize.width, top + baseLine[0]),
                    Scalar(225.0, 225.0, 225.0, 100.0), Imgproc.FILLED
                )
                // Write class name and confidence.
                Imgproc.putText(
                    rgb, label, Point(left, top),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, Scalar(0.0, 0.0, 0.0)
                )

            }
        }
        countTime("rectangle")
        if (rotateCode != -1) {
            Core.rotate(rgb, rgb, 2 - rotateCode)
            countTime("rotate back rgb")
        }

        return DetectionResult(rgba = rgb,results = rects.toTypedArray(),message = stringBuilderTime.toString().also { context.logD(it) })
    }
}
