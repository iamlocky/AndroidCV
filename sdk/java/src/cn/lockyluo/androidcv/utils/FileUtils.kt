package cn.lockyluo.androidcv.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * @author LuoTingWei
 * 功能 FileUtils
 * @date 2018/12/27
 */
object FileUtils {
    private val SEPARATOR = File.separator//路径分隔符

    /**
     * 复制res/raw中的文件到指定目录
     *
     * @param context     上下文
     * @param id          资源ID
     * @param storagePath 目标文件夹的路径
     * @param fileName    文件名
     */
    fun copyFilesFromRaw(context: Context, id: Int, storagePath: String, fileName: String): File {
        val inputStream = context.resources.openRawResource(id)
        val file = File(storagePath)
        if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
            file.mkdirs()
        }
        return readInputStream(
            storagePath + SEPARATOR + fileName,
            inputStream
        )
    }

    fun copyFilesFromRaw(context: Context, id: Int, outputFile: File): File {
        return copyFilesFromRaw(context, id, outputFile.path, outputFile.name)
    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    private fun readInputStream(storagePath: String, inputStream: InputStream): File {
        val file = File(storagePath)
        inputStream.use { stream ->
            if (!file.exists()) {
                // 1.建立通道对象
                val fos = FileOutputStream(file)
                fos.use {
                    // 2.定义存储空间
                    val buffer = ByteArray(stream.available())
                    // 3.开始读文件
                    var length = 0
                    while ({ length = stream.read(buffer);length }() != -1) {// 循环从输入流读取buffer字节
                        // 将Buffer中的数据写到outputStream对象中
                        it.write(buffer, 0, length)
                    }
                    it.flush()// 刷新缓冲区
                }
            }
        }
        return file
    }

    /**
     * 持久化存储字符串
     */
    fun writeToFile(data: String, path: String, fileName: String) {
        val outputFile = File(path, fileName)
        val filePath = File(path)
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        FileOutputStream(outputFile).use {
            it.write(data.toByteArray())
            it.flush()
        }
    }

    /**
     * 调用系统应用打开图片
     *
     * @param context context
     * @param file  file
     */
    fun openFile(context: Context, file: File) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //设置intent的Action属性
        intent.action = Intent.ACTION_VIEW

        var uri: Uri?
        // 支持Android7.0，Android 7.0以后，用了Content Uri 替换了原本的File Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 方式一
            uri = getContentUri(context, file)
        } else {
            uri = Uri.fromFile(file)
        }

        //获取文件file的MIME类型
        val type = getMIMEType(file)
        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type)
        Logger.d("$uri $type")
        //跳转
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "找不到打开此文件的应用！", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * 转换 content:// uri
     *
     * @param imageFile imageFile
     * @return
     */
    fun getContentUri(context: Context, imageFile: File): Uri? {
        val filePath = imageFile.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath), null
        )
        cursor.use {
            return if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getInt(
                    cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID)
                )
                val baseUri = Uri.parse("content://media/external/images/media")
                Uri.withAppendedPath(baseUri, "" + id)
            } else {
                if (imageFile.exists()) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, filePath)
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                } else {
                    null
                }
            }
        }
    }

    /**
     * 根据文件后缀回去MIME类型
     *
     * @param file file
     * @return string
     */
    private fun getMIMEType(file: File): String {
        var type = "file/*"
        val fName = file.name

        //获取后缀名前的分隔符"."在fName中的位置。
        val dotIndex = fName.lastIndexOf(".")
        if (dotIndex < 0) {
            return type
        }

        /* 获取文件的后缀名*/
        val end = fName.substring(dotIndex, fName.length).toLowerCase()
        if (TextUtils.isEmpty(end)) {
            return type
        }

        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (i in MIME_MapTable.indices) {
            if (end == MIME_MapTable[i][0]) {
                type = MIME_MapTable[i][1]
                break
            }
        }
        return type
    }

    private val MIME_MapTable = arrayOf(
        // {后缀名，MIME类型}
        arrayOf(".3gp", "video/3gpp"),
        arrayOf(".apk", "application/vnd.android.package-archive"),
        arrayOf(".asf", "video/x-ms-asf"),
        arrayOf(".avi", "video/x-msvideo"),
        arrayOf(".bin", "application/octet-stream"),
        arrayOf(".bmp", "image/bmp"),
        arrayOf(".c", "text/plain"),
        arrayOf(".class", "application/octet-stream"),
        arrayOf(".conf", "text/plain"),
        arrayOf(".cpp", "text/plain"),
        arrayOf(".doc", "application/msword"),
        arrayOf(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        arrayOf(".xls", "application/vnd.ms-excel"),
        arrayOf(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        arrayOf(".exe", "application/octet-stream"),
        arrayOf(".gif", "image/gif"),
        arrayOf(".gtar", "application/x-gtar"),
        arrayOf(".gz", "application/x-gzip"),
        arrayOf(".h", "text/plain"),
        arrayOf(".htm", "text/html"),
        arrayOf(".html", "text/html"),
        arrayOf(".jar", "application/java-archive"),
        arrayOf(".java", "text/plain"),
        arrayOf(".jpeg", "image/jpeg"),
        arrayOf(".jpg", "image/jpeg"),
        arrayOf(".js", "application/x-javascript"),
        arrayOf(".log", "text/plain"),
        arrayOf(".m3u", "audio/x-mpegurl"),
        arrayOf(".m4a", "audio/mp4a-latm"),
        arrayOf(".m4b", "audio/mp4a-latm"),
        arrayOf(".m4p", "audio/mp4a-latm"),
        arrayOf(".m4u", "video/vnd.mpegurl"),
        arrayOf(".m4v", "video/x-m4v"),
        arrayOf(".mov", "video/quicktime"),
        arrayOf(".mp2", "audio/x-mpeg"),
        arrayOf(".mp3", "audio/x-mpeg"),
        arrayOf(".mp4", "video/mp4"),
        arrayOf(".mpc", "application/vnd.mpohun.certificate"),
        arrayOf(".mpe", "video/mpeg"),
        arrayOf(".mpeg", "video/mpeg"),
        arrayOf(".mpg", "video/mpeg"),
        arrayOf(".mpg4", "video/mp4"),
        arrayOf(".mpga", "audio/mpeg"),
        arrayOf(".msg", "application/vnd.ms-outlook"),
        arrayOf(".ogg", "audio/ogg"),
        arrayOf(".pdf", "application/pdf"),
        arrayOf(".png", "image/png"),
        arrayOf(".pps", "application/vnd.ms-powerpoint"),
        arrayOf(".ppt", "application/vnd.ms-powerpoint"),
        arrayOf(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        arrayOf(".prop", "text/plain"),
        arrayOf(".rc", "text/plain"),
        arrayOf(".rmvb", "audio/x-pn-realaudio"),
        arrayOf(".rtf", "application/rtf"),
        arrayOf(".sh", "text/plain"),
        arrayOf(".tar", "application/x-tar"),
        arrayOf(".tgz", "application/x-compressed"),
        arrayOf(".txt", "text/plain"),
        arrayOf(".wav", "audio/x-wav"),
        arrayOf(".wma", "audio/x-ms-wma"),
        arrayOf(".wmv", "audio/x-ms-wmv"),
        arrayOf(".wps", "application/vnd.ms-works"),
        arrayOf(".xml", "text/plain"),
        arrayOf(".z", "application/x-compress"),
        arrayOf(".zip", "application/x-zip-compressed"),
        arrayOf("", "*/*")
    )

}
