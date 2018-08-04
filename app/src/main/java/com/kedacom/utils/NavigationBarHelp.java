package com.kedacom.utils;

/**
 * Created by zhanglei on 2017/7/26.
 */
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class NavigationBarHelp {

    /**
     * 是否有虚拟键
     *
     * @return
     */
    public static boolean hasNavigationBar(Context context) {
        boolean hasMenuKey = true, hasBackKey = true;
        boolean ret = false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
                hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            }

            if ((!hasMenuKey) && (!hasBackKey)) {
                ret = true;
            }
        } catch (Exception e) {
            ret = false;
        }

        return ret;
    }

    /**
     * 隐藏虚拟键
     */
    public static void hideNavigation(Activity context) {

        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)) {

            context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    /**
     * 隐藏虚拟键
     */
    public static void hideNavigation(View view) {

        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)) {

//            | View.SYSTEM_UI_FLAG_FULLSCREEN

            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    /**
     * 显示虚拟键
     */
    public static void showNavigation(View view) {

        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)) {

            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public static int getNavigationHeight(Context conetxt){
        Resources res = conetxt.getResources();
        int navigationHeight = res.getIdentifier("navigation_bar_height", "dimen", "android");
        navigationHeight = res.getDimensionPixelSize(navigationHeight);
        return navigationHeight;
    }

    public static int getStatusHeight(Context conetxt){
        Resources res = conetxt.getResources();
        int statusHeight = res.getIdentifier("status_bar_height", "dimen", "android");
        statusHeight = res.getDimensionPixelSize(statusHeight);
        return statusHeight;
    }
}