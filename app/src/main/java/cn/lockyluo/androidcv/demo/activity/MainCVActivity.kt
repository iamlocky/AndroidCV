package cn.lockyluo.androidcv.demo.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import cn.lockyluo.androidcv.R
import cn.lockyluo.androidcv.activity.BaseActivity
import cn.lockyluo.androidcv.exception.CVNotLoadedException
import cn.lockyluo.androidcv.helper.FaceDetectionHelper
import cn.lockyluo.androidcv.helper.OpenCVHelper
import cn.lockyluo.androidcv.utils.DetectionResult
import cn.lockyluo.androidcv.utils.GsonUtils
import cn.lockyluo.androidcv.utils.logD
import cn.lockyluo.photopicker.PhotoPickUtils
import kotlinx.android.synthetic.main.activity_main_cv.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.dnn.Net
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.objdetect.Objdetect
import java.lang.RuntimeException
import java.util.*


@Suppress("DEPRECATION")
@SuppressLint("SetTextI18n")
class MainCVActivity : BaseActivity() {
    override val layoutId: Int
        get() = R.layout.activity_main_cv

    private var cameraType = CameraBridgeViewBase.CAMERA_ID_FRONT//相机类型前置/后置
    private val btnFlashSrcRes = arrayOf(R.drawable.ic_flash_price_hotel, R.drawable.ic_flash_price_hotel_on)//闪光灯按钮
    private var flashStatus: Boolean = false
    private var faceDetectionHelper: FaceDetectionHelper? = null
    private lateinit var openCVHelper: OpenCVHelper
    private var enablePreview = true
    private var imgList = ArrayList<String>()
    private var results = ArrayList<DetectionResult>()
    private var spCameraSizeAdapter: ArrayAdapter<String>? = null//分辨率下拉栏Adapter
    private var currentImgIndex: Int = 0
    private val gson = GsonUtils.prettyGson

    override fun initData() {
        openCVHelper = OpenCVHelper(context) {
            if (it) {
                if (faceDetectionHelper == null) {
                    faceDetectionHelper = openCVHelper.getFaceDetectionHelper(FaceDetectionHelper.TYPE_LBP)

                }
                flResultView.visibility = if (enablePreview) {//如果选择了图片检测，则暂停摄像头
                    setShowRightButton(true)
                    cameraCv.enableView()
                    View.GONE
                } else {
                    setShowRightButton(false)
                    View.VISIBLE
                }
            } else {
                openCVHelper.onResume()
            }
        }
    }

    override fun initView() {

        cameraCv.onCameraFrame { gray, rgba ->

            if (spCameraSizeAdapter == null) {
                initCameraSizeSpinnerAdapter()
            }

            if (!cbEnableFace.isChecked) {
                return@onCameraFrame rgba
            }

            when (spClassifierType.selectedItemPosition) {
                2 -> {
                    if (net == null) {//获取人脸深度神经网络
                        net = faceDetectionHelper?.getDnnFromCaffe(R.raw.deployproto, R.raw.res10_ssd_iter)
                    }
                    net?.let {
                        faceDetectionHelper?.detectFaceDnn(it, rgba, 90)?.rgba
                    }
                }

                else -> {
                    val rotation=when(cameraCv.getScreenRotation()){
                        90->0
                        180->90
                        270->180
                        else->90
                    }
                    val result = faceDetectionHelper?.detectFace(
                        rgba, gray, rotation, cbEnableEye.isChecked,
                        1 + (seekBarScale.progress.toDouble() / 100),
                        seekBarMinNeighbors.progress, Objdetect.CASCADE_DO_CANNY_PRUNING,
                        Size(), Size(),
                        cbEnableEqualize.isChecked,
                        true
                    )
                    result?.rgba
                }
            }.also {
                postToUI {
                    //显示灰度图
                    showMat2ImgView(gray, ivGray)
                }
            }
        }

        btnSelectImage.setOnClickListener { selectPicture() }
        btnSwitchFlash.setOnClickListener { switchFlashLight() }
        ivBtnPre.setOnClickListener { if (currentImgIndex > 0) setResultView(currentImgIndex - 1) }
        ivBtnNext.setOnClickListener { if (currentImgIndex < imgList.size - 1) setResultView(currentImgIndex + 1) }
        ivGray.setOnClickListener { selectPicture() }
        setShowRightButton(true)
        titleBarView.ivButtonRight.apply {
            setImageResource(R.drawable.ic_switch)
            setOnClickListener {
                switchCamera()
                cameraCv.enableView()
            }
        }
        ivRgba.setOnClickListener {
            showAlertDialog(
                "第${currentImgIndex + 1}张",
                "facesResult:\n${gson.toJson(results[currentImgIndex].results)}"
            )
        }
        ivBtnClose.setOnClickListener {
            enablePreview = true
            openCVHelper.onResume()
        }

        spClassifierType.setSelection(1)
        spClassifierType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {//haar
                    faceDetectionHelper?.switchFaceClassifier(FaceDetectionHelper.TYPE_HAAR)
                } else {//lbp
                    faceDetectionHelper?.switchFaceClassifier(FaceDetectionHelper.TYPE_LBP)
                }
            }
        }

        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    seekBar?.progress = 1
                } else {
                    tvScale.text = "scale ${progress.toDouble() / 100}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
        seekBarMinNeighbors.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvMinNeighbors.text = "minNeighbors $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

    }


    /**
     * 切换相机
     */
    private fun switchCamera() {
        cameraCv.disableView().also {
            cameraType =
                if (cameraType == CameraBridgeViewBase.CAMERA_ID_FRONT) CameraBridgeViewBase.CAMERA_ID_BACK else CameraBridgeViewBase.CAMERA_ID_FRONT
            cameraCv.javaCameraView.cameraIndex = cameraType
        }
        cameraCv.enableView()
    }


    /**
     * 开关闪光灯
     */
    private fun switchFlashLight() {
        flashStatus = if (cameraType == CameraBridgeViewBase.CAMERA_ID_BACK) {
            cameraCv.switchFlashLight(!flashStatus)
        } else false

        //btn闪光灯图标
        btnSwitchFlash.setImageResource(btnFlashSrcRes[if (flashStatus) 1 else 0])
    }

    override fun onPause() {
        cameraCv.disableView()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        openCVHelper.onResume()
    }

    override fun onDestroy() {
        cameraCv.disableView()
        super.onDestroy()
    }

    var net: Net? = null

    private fun initCameraSizeSpinnerAdapter() {
        postToUI {
            //初始化分辨率下拉栏
            val cameraSizes = mutableListOf("默认")
            cameraCv.javaCameraView.sizes.forEach {
                cameraSizes.add(it.toString())
            }
            spCameraSizeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, cameraSizes)
            spCameraSize.adapter = spCameraSizeAdapter
            spCameraSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    cameraCv.javaCameraView.switchCameraSize(position - 1)
                }
            }
        }
    }

    private fun showMat2ImgView(mat: Mat, view: ImageView) {
        if (mat.width() > 0) {//判断图像不为空
            val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565)
            Utils.matToBitmap(mat, bitmap)
            view.setImageBitmap(bitmap)
        } else {
            view.setImageDrawable(ColorDrawable(Color.GRAY))
        }
    }

    /**
     * 选择图片
     */
    private fun selectPicture() {
        PhotoPickUtils.startPick(this, false, 50, imgList)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgList.clear()
        results.clear()
        logD("onActivityResult $resultCode $requestCode")
        if (resultCode == RESULT_OK) {//文件选择结果
            PhotoPickUtils.onActivityResult(requestCode, RESULT_OK, data, object : PhotoPickUtils.PickHandler {
                override fun onPickCancel() {
                    logD("onPickCancel")
                }

                override fun onPreviewBack(photos: ArrayList<String>?) {
                    logD("onPreviewBack")
                }

                override fun onPickSuccess(photos: ArrayList<String>?) {
                    showProgressDialog()
                    imgList.clear()
                    logD("onPickSuccess" + GsonUtils.gson.toJson(photos))
                    photos?.let {
                        imgList.addAll(it)
                    }
                    enablePreview = if (!imgList.isNullOrEmpty()) {
                        detectAll(handleImgFile2Mat(imgList))
                        false
                    } else true
                }

                override fun onPickFail(error: String?) {
                    logD("onPickFail")
                }

            })

        } else {
            enablePreview = true
        }
    }

    private fun handleImgFile2Mat(list: ArrayList<String>): ArrayList<Mat> {
        val mats = ArrayList<Mat>()
        try {
            list.forEach {
                val mat = Imgcodecs.imread(it, Imgcodecs.IMREAD_COLOR)
                val matBGR = mutableListOf<Mat>()
                Core.split(mat, matBGR)
                Core.merge(listOf(matBGR[2], matBGR[1], matBGR[0]), mat)
                mats.add(mat)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastShow(e.message)
        }
        return mats
    }

    private fun detectAll(mats: ArrayList<Mat>) {

        results.clear()
        Thread {
            for (i in 0 until mats.size) {
                runOnUiThread { progressDialog?.setMessage("正在检测第${i + 1}张") }
                //--
//                val bitmap = Bitmap.createBitmap(mats[i].width(), mats[i].height(), Bitmap.Config.RGB_565)
//                Utils.matToBitmap(mats[i], bitmap)
//                faceDetectionHelper?.androidFaceDetector(bitmap)
                //--
                if (spClassifierType.selectedItemPosition == 2) {
                    net?.let {
                        faceDetectionHelper?.detectFaceDnn(net!!, mats[i])?.also {
                            results.add(it)//添加检测结果到ArrayList
                        }
                    }
                } else {
                    faceDetectionHelper?.detectFace(
                        mats[i], openCVHelper.matRgb2Gray(mats[i]),
                        0, cbEnableEye.isChecked, 1 + (seekBarScale.progress.toDouble() / 100),
                        seekBarMinNeighbors.progress,isEqualizeHist = cbEnableEqualize.isChecked
                    )?.also {
                        results.add(it)//添加检测结果到ArrayList
                    }
                }

            }
            runOnUiThread {
                dismissDialog()
                setResultView(0)
            }
        }.start()
    }

    /**
     * 显示本地图片检测结果
     */
    private fun setResultView(index: Int) {
        if (index >= 0 && index < imgList.size && index < results.size) {
            currentImgIndex = index
            val result = results[index]
            showMat2ImgView(result.gray, ivGray)
            showMat2ImgView(result.rgba, ivRgba)
            tvMessage.text = "${index + 1}/${imgList.size}\n${result.message}"
        } else {
            currentImgIndex = 0
            logD("index越界 index$index imgList${imgList.size} results${results.size}")
        }
    }

    override fun onBackPressed() {
        if (flResultView.visibility == View.VISIBLE) {
            ivBtnClose.performClick()
        } else
            super.onBackPressed()
    }

}
