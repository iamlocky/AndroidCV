package cn.lockyluo.androidcv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View

import com.orhanobut.logger.Logger
import java.io.*

/**
 * @author LuoTingWei
 * 功能 处理Bitmap
 * @date 2018/12/24
 */
object BitmapUtils {

    /**
     * 对单独某个View进行截图
     *
     * @param view
     * @return
     */
    @JvmOverloads
    fun loadBitmapFromView(view: View?, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
        if (view == null) {
            Logger.e("loadBitmapFromView null!")
            return null
        }
        val screenshot = Bitmap.createBitmap(view.width, view.height, config)
        val c = Canvas(screenshot)
        c.translate((-view.scrollX).toFloat(), (-view.scrollY).toFloat())
        view.draw(c)
        return screenshot
    }

    /**
     * 文件名
     *
     * @return
     */
    private fun generateFileName(): String {
        return "img_" + CalendarUtil.dateTimeMillisBottomLine
    }

    /**
     * 保存bitmap到本地
     *
     * @param mBitmap
     * @return
     */
    fun saveBitmap(path: String, mBitmap: Bitmap?): String? {
        if (mBitmap == null) {
            return null
        }
        var savePath = path
        val filePic: File
        if (!savePath.endsWith("/")) {
            savePath += "/"
        }
        try {
            filePic = File(savePath + generateFileName() + ".jpg")
            if (!filePic.exists()) {
                filePic.parentFile.mkdirs()
                filePic.createNewFile()
            }
            val fos = FileOutputStream(filePic)
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return filePic.absolutePath
    }

    /**
     * 压缩bitmap
     * @param image
     * @param size  kb
     * @return
     */
    fun compressImage(image: Bitmap, size: Int): Bitmap? {
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (outputStream.toByteArray().size / 1024 > size) {  //循环判断如果压缩后图片是否大于?kb,大于继续压缩
            Logger.d("compressImage $options")
            outputStream.reset()//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, outputStream)//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10//每次都减少10
        }
        val isBm = ByteArrayInputStream(outputStream.toByteArray())//把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null)
    }
}

