@file:Suppress("DEPRECATION")

package cn.lockyluo.androidcv.utils

import android.annotation.TargetApi
import android.content.Context
import android.hardware.Camera
import android.hardware.camera2.CameraManager
import android.os.Build
import org.opencv.android.CameraBridgeViewBase

/**
 * 切换闪光灯 for camera, Android5.1以下
 * @param cameraFlashMode 对应值为：
 * [android.hardware.Camera.Parameters.FLASH_MODE_TORCH]
 * [android.hardware.Camera.Parameters.FLASH_MODE_OFF]
 * @return flashMode
 */

fun CameraBridgeViewBase.switchFlashLight(camera: Camera, cameraFlashMode: Boolean): Boolean {
    val param = camera.parameters
    with(param) {
        flashMode =
            if (cameraFlashMode) {
                Camera.Parameters.FLASH_MODE_TORCH
            } else {
                Camera.Parameters.FLASH_MODE_OFF
            }
        camera.parameters = this
    }
    return cameraFlashMode
}

/**
 * 切换闪光灯 for camera2, Android6.0
 *
 * @param cameraFlashMode
 */
@TargetApi(Build.VERSION_CODES.M)
fun  CameraBridgeViewBase.switchFlashLight(cameraFlashMode: Boolean, cameraId: String? = ""): Boolean {
    val cameraManager = this.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    with(cameraManager) {
        if (cameraId.isNullOrEmpty()) {
            setTorchMode(cameraIdList[0], cameraFlashMode)
        } else {
            setTorchMode(cameraId, cameraFlashMode)
        }
        return cameraFlashMode
    }
}