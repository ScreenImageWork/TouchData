package com.kedacom.touchdata.whiteboard.utils;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.kedacom.touchdata.R;

/**
 * Created by zhanglei on 2017/8/18.
 */
public class ResUtils {

    public static Resources rm;

    public static void init(Context context){
        rm = context.getResources();
    }

    public static String resToString(int strResId){
        return rm.getString(strResId);
    }

    public static int resToColor(int clrResId){
        return rm.getColor(clrResId);
    }

    public static float resToPx(int resId){
        return rm.getDimension(resId);
    }

    public static Drawable resToDrawable(int resId){
        return rm.getDrawable(resId);
    }

    public static Bitmap resToBitmap(int resId){
        return  BitmapFactory.decodeResource(rm,resId);
    }

    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}
