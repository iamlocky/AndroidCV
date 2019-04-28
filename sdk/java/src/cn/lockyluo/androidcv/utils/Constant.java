package cn.lockyluo.androidcv.utils;

/**
 * LockyLuo
 *  常量
 * 2019/1/22
 */
public interface Constant {
    interface Permission{
        String camera = android.Manifest.permission.CAMERA;
        String internet = android.Manifest.permission.INTERNET;
        String writeExternal = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String[] permissions = new String[]{writeExternal, internet, camera};
    }
}
