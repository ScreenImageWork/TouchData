package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2017/1/9.
 */
public class NetworkStateDialog implements IControler{

    private final float marginTop ;

    private final float marginRight;

    private Context mContext;

    private View contentView;

    private ImageView mServerConnectStateIv;

    private ImageView mNetworkUnusableIv;

    private TPPopupWindow mWindow;

    private boolean isServerConnecting = false;


    @TargetApi(Build.VERSION_CODES.M)
    public NetworkStateDialog(Context context){
        mContext = context;

        marginTop = context.getResources().getDimension(R.dimen.network_state_dialog_margintop);
        marginRight = context.getResources().getDimension(R.dimen.network_state_dialog_marginright);

        LayoutInflater inflater = LayoutInflater.from(context);

        contentView = inflater.inflate(R.layout.dialog_networkstate,null);
        mServerConnectStateIv = (ImageView) contentView.findViewById(R.id.serverConnectStateIv);
        mNetworkUnusableIv = (ImageView) contentView.findViewById(R.id.networkUnusableIv);

        mWindow = new TPPopupWindow(context);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(0);
        mWindow.setOnDismissListener((BaseActivity)context);
    }

    public boolean isNetworkUnusable(){
        return mNetworkUnusableIv.getVisibility() == View.VISIBLE?true:false;
    }

    public boolean isServerConnecting(){
        return isServerConnecting;
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }


    public void show(boolean networkUsable, boolean serverConnecting){

        isServerConnecting = serverConnecting;

        if(networkUsable){
            mNetworkUnusableIv.setVisibility(View.GONE);
        }else{
            mNetworkUnusableIv.setVisibility(View.VISIBLE);
        }
//  屏蔽掉本地功能 20180530
//        if(serverConnecting){
//            mServerConnectStateIv.setBackgroundResource(R.drawable.server_connecting);
//            AnimationDrawable anim = (AnimationDrawable) mServerConnectStateIv.getBackground();
//            anim.start();
//        }else{
//            mServerConnectStateIv.setBackgroundResource(R.mipmap.server_close_icon);
//        }

        show();
    }

    @Override
    @Deprecated
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.TOP|Gravity.RIGHT,(int)marginTop,(int)marginRight);
    }

    @Override
    public void dismiss() {
        if(mWindow!=null)
         mWindow.dismiss();
    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        mContext = null;
        contentView = null;
        mWindow = null;
    }
}
