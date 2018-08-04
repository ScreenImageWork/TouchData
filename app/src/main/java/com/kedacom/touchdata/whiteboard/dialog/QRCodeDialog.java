package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.httpserver.HttpServerManager;
import com.kedacom.tdc.utils.TdcUtils;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.StorageMangerJarUtils;

/**
 * Created by zhanglei on 2016/12/7.
 */
public class QRCodeDialog implements IControler,PopupWindow.OnDismissListener {

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private ImageView qrIv;

    private ImageView qrLoadingIv;

    private TextView wifiNameTv;   //数据待定

    private TextView wifiPsdTv;   //数据待定

    private LinearLayout qrPanel;

    TdcUtils qrUtils;

    private  Bitmap curQRBitmap;

    public QRCodeDialog(Context context){

        mContext = context;

        qrUtils = new TdcUtils();

        LayoutInflater inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_qrcode,null);

        qrIv = (ImageView) contentView.findViewById(R.id.qrIv);
        qrLoadingIv = (ImageView) contentView.findViewById(R.id.qrLoadingIv);
        wifiNameTv = (TextView) contentView.findViewById(R.id.wifiNameTv);
        wifiPsdTv = (TextView) contentView.findViewById(R.id.wifiPsdTv);

        qrPanel = (LinearLayout) contentView.findViewById(R.id.qrPanel);

        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setOnDismissListener(this);


        contentView.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){

                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    Rect rect = new Rect();
                    qrPanel.getHitRect(rect);

                    if(!rect.contains((int)x,(int)y)){
                        dismiss();
                    }

                }
                return true;
            }
        });
    }


    public void setQRCacheFileDir(String title,String subtitle,String cacheDir){
        TPLog.printError("qr code  share dir :"+cacheDir);
        HttpServerManager.setShareParams(cacheDir,title,subtitle);
        createQrCodeImage();
        TPLog.printError("start httpserver...");
        HttpServerManager.startHttpServer(mContext);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createQrCodeImage(){
        TPLog.printError("create qrcode img begin...");
        //获取链接
        String uri = HttpServerManager.getWbShareUri(mContext);
        //生成二维码图片
        TPLog.printKeyStatus("create qr code image,download address :"+uri);
        curQRBitmap = qrUtils.createTdc(uri,400,400);
        //获取wifi名称和密码  暂时还未提供

        //设置数据到控件
        Drawable drawable =new BitmapDrawable(curQRBitmap);
        qrIv.setBackground(drawable);
        qrIv.setVisibility(View.VISIBLE);
        qrLoadingIv.setVisibility(View.GONE);
        TPLog.printError("create qrcode img end...");
    }
    /**
     * 初始化数据
     */
    private void qrLoading(){
        qrLoadingIv.setVisibility(View.VISIBLE);
        qrIv.setVisibility(View.INVISIBLE);
        AnimationDrawable anim = (AnimationDrawable)qrLoadingIv.getBackground();
        anim.start();
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    public void showWifiInfo(){
        wifiNameTv.setText(StorageMangerJarUtils.PARAMS_WIFI_SSID);
        wifiPsdTv.setText(StorageMangerJarUtils.PARAMS_WIFI_PASSWORD);
    }

    @Override
    public void show() {
        qrLoading();
        showWifiInfo();
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }


    @Override
    public void onDismiss() {
        HttpServerManager.stopHttpServere(mContext);
        ((BaseActivity)mContext).onDismiss();
        if(curQRBitmap!=null&&!curQRBitmap.isRecycled()){
            curQRBitmap.recycle();
            curQRBitmap = null;
        }
    }

    @Override
    public void destory(){
        if(curQRBitmap!=null&&!curQRBitmap.isRecycled()){
            curQRBitmap.recycle();
            curQRBitmap = null;
        }

        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }

        mContext = null;
        contentView = null;
        mWindow = null;
        qrIv = null;
        wifiNameTv = null;
        wifiPsdTv = null;
        qrPanel = null;
        qrUtils = null;
    }
}
