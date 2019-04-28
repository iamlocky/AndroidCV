package cn.lockyluo.androidcv.utils

import org.opencv.core.Mat
import org.opencv.core.Rect

/**
 * LockyLuo
 *
 * 检测结果
 *
 * 2019/3/31
 */
class DetectionResult() {
    var gray = Mat()
    var rgba = Mat()
    var results = arrayOf<Rect>()
    var message: String = ""

    constructor(gray: Mat= Mat(), rgba: Mat= Mat(), results: Array<Rect>, message: String = "") : this() {
        this.gray = gray
        this.rgba = rgba
        this.results = results
        this.message = message
    }

}
