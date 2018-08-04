package com.kedacom.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import java.io.DataOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhanglei on 2016/12/22.
 */
public class Utils {

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceRunning(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /*
    public static void doStartApplicationWithPackageName(Context context,String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }*/

    public static void doStartApplicationWithPackageName(Context context,String uriStr) {
        Uri uri = Uri.parse(uriStr);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    // 判断email格式是否正确
    public static boolean checkEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }


    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     * @param command
     *            命令：String apkRoot="chmod 777 "+getPackageCodePath();
     *            RootCommand(apkRoot);
     * @return 应用程序是/否获取Root权限
     */
    public static boolean rootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.i("ROOT", e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }



    public static void notificationTouchDataDisplayState(Context context,boolean displayState){
        Intent intent = new Intent();
        intent.setAction("com.kedacom.touchdata");
        intent.putExtra("isShow", displayState);
        context.sendBroadcast(intent);
    }


    /**
     * 远程协作
     * 数据1 : boolean ： 是否是远程会议
     * 数据2 : boolean ： 是否是创建者
     */
    public static final String REMOTE_COOPERATION = "com.kedacom.touchdata.REMOTE_COOPERATION";
    public static final String KEY_1 = "KEY_1";
    public static final String KEY_2 = "KEY_2";

    public static void setRecScreenShareEnable(Context context , boolean isCreater,boolean isJoiningDcs){
        Intent intent = new Intent();
        intent.setAction(REMOTE_COOPERATION);
        intent.putExtra(KEY_1,isJoiningDcs);
        intent.putExtra(KEY_2,isCreater);
        context.sendBroadcast(intent);
    }


    

    public static String touchActionToString(int action){
        String msg = "";
        switch(action){
            case MotionEvent.ACTION_DOWN:
                msg = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                msg = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                msg = "ACTION_UP";
                break;
            case MotionEvent.ACTION_CANCEL:
                msg = "ACTION_CANCEL";
                break;
        }
        return msg;
    }

}
