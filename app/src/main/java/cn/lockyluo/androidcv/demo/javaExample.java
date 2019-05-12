package cn.lockyluo.androidcv.demo;

import android.content.Context;
import cn.lockyluo.androidcv.helper.FaceDetectionHelper;
import cn.lockyluo.androidcv.helper.OpenCVHelper;


/**
 * LockyLuo
 * <p>
 * 2019/4/1
 */
public class javaExample {
    private OpenCVHelper openCVHelper;
    private FaceDetectionHelper faceDetectionHelper;

    void init(Context context) {
        openCVHelper = new OpenCVHelper(context, isSucceed -> {
            //Succeed
            faceDetectionHelper = openCVHelper.getFaceDetectionHelper(FaceDetectionHelper.TYPE_HAAR);
            return null;
        });

//        faceDetectionHelper.detectFace(new Mat(),new Mat());

    }


}
