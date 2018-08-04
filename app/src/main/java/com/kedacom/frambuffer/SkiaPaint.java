/**
 * 调用frambuffer和skia绘图接口
 * 需要开启权限chmod 777 /dev/graphics/fb0
 * @author dengyingying
 *
 */
package com.kedacom.frambuffer;
import android.graphics.Bitmap;


public class SkiaPaint
{
    static {
        System.loadLibrary("frmskia");
    }

    public static  void start(){};

    public static native void init(float fPathEffect);//如不调用，默认PathEffect为20

    public static native void initpen(float width, int color);//可不调用，draw时设置

    public static native int clear();

    public static native int setbg(Bitmap bitmap);//设置擦除时的背景
    public static native int seteraser(Bitmap bitmap);//设置橡皮擦图像

    public static native boolean seterasemode(boolean bIsErase) ;//暂时不用，保留

    public static native boolean draw(float x, float y, float width, int color, int state) ;//state为事件(up\down\move)

    public static native boolean erase(float x, float y, int state) ;

    /**
     * @param bsavelog true时设置log写入到/storage/sdcard/frambuffer.log中
     * @param blogcat true 开启画线时的坐标logcat打印，默认关闭
     * @param strPath 保存文件的位置加名称，如果是空，则默认在/storage/sdcard/frambuffer.log
     */
    public static native void showlog(boolean bsavelog, boolean blogcat, String strPath) ;
}