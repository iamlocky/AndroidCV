package cn.lockyluo.androidcv.exception

/**
 * LockyLuo
 * cv未加载完成时抛出的异常
 *
 * 2019/4/1
 */
class CVNotLoadedException(message:String="openCV is not loaded") : Exception(message)