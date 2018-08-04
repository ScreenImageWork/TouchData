package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2017/06/20.
 */
public class NewMenuDialog implements IControler,View.OnClickListener,PopupWindow.OnDismissListener{

    private final int selectTextColor ;

    private final int normalTextColor;

    private Context mContext;

    private View contentView;

    private LinearLayout saveBtn;
    private ImageView saveBtnIv;
    private TextView saveBtnTv;

    private LinearLayout sendMailBtn;
    private ImageView sendMailBtnIv;
    private TextView sendMailBtnTv;

    private LinearLayout scanQRBtn;
    private ImageView scanQRBtnIv;
    private TextView scanQRBtnTv;


    private LinearLayout changeBgBtn;
    private ImageView changeBgBtnIv;
    private TextView changeBgBtnTv;


    private LinearLayout exitBtn;
    private ImageView exitBtnIv;
    private TextView exitBtnTv;

    private TPPopupWindow mWindow;

    private boolean isShowing = false;

    public NewMenuDialog(Context context){

        mContext = context;

        selectTextColor = context.getResources().getColor(R.color.menu_text_select);
        normalTextColor = context.getResources().getColor(R.color.menu_text_normal);
        initView();
        initWindow();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        contentView = inflater.inflate(R.layout.dialog_new_menu,null);
        saveBtn = (LinearLayout) contentView.findViewById(R.id.saveBtn);
        saveBtnIv = (ImageView) contentView.findViewById(R.id.saveBtnIv);
        saveBtnTv = (TextView) contentView.findViewById(R.id.saveBtnTv);

        sendMailBtn = (LinearLayout) contentView.findViewById(R.id.sendMailBtn);
        sendMailBtnIv = (ImageView) contentView.findViewById(R.id.sendMailBtnIv);
        sendMailBtnTv = (TextView) contentView.findViewById(R.id.sendMailBtnTv);

        scanQRBtn = (LinearLayout) contentView.findViewById(R.id.scanQRBtn);
        scanQRBtnIv = (ImageView) contentView.findViewById(R.id.scanQRBtnIv);
        scanQRBtnTv = (TextView) contentView.findViewById(R.id.scanQRBtnTv);

        changeBgBtn = (LinearLayout) contentView.findViewById(R.id.changeBgBtn);
        changeBgBtnIv = (ImageView) contentView.findViewById(R.id.changeBgBtnIv);
        changeBgBtnTv = (TextView) contentView.findViewById(R.id.changeBgBtnTv);

        exitBtn = (LinearLayout) contentView.findViewById(R.id.exitBtn);
        exitBtnIv = (ImageView) contentView.findViewById(R.id.exitBtnIv);
        exitBtnTv = (TextView) contentView.findViewById(R.id.exitBtnTv);

        saveBtnIv.setOnClickListener(this);
        sendMailBtnIv.setOnClickListener(this);
        scanQRBtnIv.setOnClickListener(this);
        changeBgBtnIv.setOnClickListener(this);
        exitBtnIv.setOnClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE|WindowManager.LayoutParams.TYPE_TOAST);
        //mWindow.setOutsideTouchable(true);
        mWindow.setOnDismissListener(this);
    }


    private void clickSaveBtn(boolean isClick){
        if(isClick) {
            saveBtnIv.setBackgroundResource(R.mipmap.save_select_icon);
            saveBtnTv.setTextColor(selectTextColor);
        }else{
            saveBtnIv.setBackgroundResource(R.mipmap.save_normal_icon);
            saveBtnTv.setTextColor(normalTextColor);
        }
    }

    private void clickSendMailBtn(boolean isClick){
        if(isClick) {
            sendMailBtnIv.setBackgroundResource(R.mipmap.mail_select_icon);
            sendMailBtnTv.setTextColor(selectTextColor);
        }else{
            sendMailBtnIv.setBackgroundResource(R.mipmap.mail_normal_icon);
            sendMailBtnTv.setTextColor(normalTextColor);
        }
    }

    private void clickScanQRBtn(boolean isClick){
        if(isClick) {
            scanQRBtnIv.setBackgroundResource(R.mipmap.scanqr_select_icon);
            scanQRBtnTv.setTextColor(selectTextColor);
        }else{
            scanQRBtnIv.setBackgroundResource(R.mipmap.scanqr_normal_icon);
            scanQRBtnTv.setTextColor(normalTextColor);
        }
    }

    private void clickChangeBgBtn(boolean isClick){
        if(isClick) {
            changeBgBtnIv.setBackgroundResource(R.mipmap.new_changebg_select_icon);
            changeBgBtnTv.setTextColor(selectTextColor);
        }else{
            changeBgBtnIv.setBackgroundResource(R.mipmap.new_changebg_normal_icon);
            changeBgBtnTv.setTextColor(normalTextColor);
        }
    }

    private void clickExitBtn(boolean isClick){
        if(isClick) {
            exitBtnIv.setBackgroundResource(R.mipmap.new_exit_select_icon);
            exitBtnTv.setTextColor(selectTextColor);
        }else{
            exitBtnIv.setBackgroundResource(R.mipmap.new_exit_normal_icon);
            exitBtnTv.setTextColor(normalTextColor);
        }
    }

    private void resetBtn(){
        clickChangeBgBtn(false);
        clickExitBtn(false);
        clickSaveBtn(false);
        clickScanQRBtn(false);
        clickSendMailBtn(false);
    }


    private void clickDown(View view){
        int id = view.getId();
        switch(id){
            case R.id.openBtn:
                break;
            case R.id.saveBtnIv:
                clickSaveBtn(true);
                break;
            case R.id.sendMailBtnIv:
                clickSendMailBtn(true);
                break;
            case R.id.scanQRBtnIv:
                clickScanQRBtn(true);
                break;
            case R.id.changeBgBtnIv:
                clickChangeBgBtn(true);
                break;
            case R.id.exitBtnIv:
                clickExitBtn(true);
                break;
        }
    }

    private void clickUp(View view){

        int id = view.getId();

        switch(id){
            case R.id.openBtnIv:
                ((BaseActivity)mContext).onDMenuOpenFileBtnEvent();
                break;
            case R.id.saveBtnIv:
//                clickSaveBtn(false);
                ((BaseActivity)mContext).onDMenuSaveBtnEvent();
                break;
            case R.id.sendMailBtnIv:
//                clickSendMailBtn(false);
                ((BaseActivity)mContext).onDMenuSendMailBtnEvent();
                break;
            case R.id.scanQRBtnIv:
//                clickScanQRBtn(false);
                ((BaseActivity)mContext).onDMenuScanQRBtnEvent();
                break;
            case R.id.changeBgBtnIv:
//                clickChangeBgBtn(false);
                ((BaseActivity)mContext).onDMenuChangeBgBtnEvent();
                break;
            case R.id.exitBtnIv:
//                clickExitBtn(false);
                ((BaseActivity)mContext).onDMenuExitBtnEvent();
                break;
        }
    }

    @Override
    public boolean isShow() {
//        return mWindow.isShowing();
        return isShowing;
    }

    @Override
    @Deprecated
    public void show() {
    }

    private float offsetX = Integer.MIN_VALUE;
    private float offsetY = Integer.MIN_VALUE;

    public void show(View anchor){
        isShowing = true;

        if(offsetX == Integer.MIN_VALUE||offsetY == Integer.MIN_VALUE) {

            float anchorWidth = anchor.getWidth();
            float anchorHeight = anchor.getHeight();
            float bottomIvHeight = mContext.getResources().getDimension(R.dimen.dialog_bottom_icon_height);
            float bottomIvWidth = mContext.getResources().getDimension(R.dimen.dialog_bottom_icon_width);

            float bottomIvMarginRight = mContext.getResources().getDimension(R.dimen.new_menu_bottom_iv_marginright);

            int anchorPoints[] = new int[2];
            anchor.getLocationInWindow(anchorPoints);

            offsetY = WhiteBoardUtils.bottomBarHeight;

            offsetX = (anchorWidth/2f) + anchorPoints[0] - bottomIvMarginRight - bottomIvWidth/2f;

        }

        //mWindow.showAsDropDown(anchor,(int)offsetX,(int)offsetY);
        mWindow.showAtLocation(contentView,Gravity.BOTTOM|Gravity.LEFT,(int)offsetX,(int)offsetY);
    }

    @Override
    public void dismiss() {
        if(mWindow==null){
            return;
        }
        mWindow.dismiss();
//        isShowing = false;
    }

//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        int action = motionEvent.getActionMasked();
//        if(action == MotionEvent.ACTION_DOWN){
//            clickDown(view);
//        }else if(action == MotionEvent.ACTION_UP){
//            clickUp(view);
//            dismiss();
//        }else if(action == MotionEvent.ACTION_CANCEL){
//            resetBtn();
//            dismiss();
//        }
//        return true;
//    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing())
            mWindow.dismiss();
        mContext = null;
        contentView = null;
        saveBtn = null;
        saveBtnIv = null;
        saveBtnTv = null;
        sendMailBtn = null;
        sendMailBtnIv = null;
        sendMailBtnTv = null;
        scanQRBtn = null;
        scanQRBtnIv = null;
        scanQRBtnTv = null;
        changeBgBtn = null;
        changeBgBtnIv = null;
        changeBgBtnTv = null;
        exitBtn = null;
        exitBtnIv = null;
        exitBtnTv = null;
        mWindow = null;
    }

    @Override
    public void onDismiss() {
        ((BaseActivity)mContext).onDismiss();
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isShowing = false;
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        clickUp(v);
        dismiss();
    }
}
