package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/11/29.
 */
public class MenuDialog implements IControler,View.OnTouchListener{

    private final int selectTextColor ;

    private final int normalTextColor;

    private Context mContext;

    private View contentView;

    private LinearLayout openFileBtn;
    private ImageView openFileBtnIv;
    private TextView openFileBtnTv;

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


    public MenuDialog(Context context){

        mContext = context;

        selectTextColor = context.getResources().getColor(R.color.menu_text_select);
        normalTextColor = context.getResources().getColor(R.color.menu_text_normal);
        initView();
        initWindow();
    }

   private void initView(){
       LayoutInflater inflater = LayoutInflater.from(mContext);
       contentView = inflater.inflate(R.layout.dialog_menu,null);

       openFileBtn = (LinearLayout) contentView.findViewById(R.id.openBtn);
       openFileBtnIv = (ImageView) contentView.findViewById(R.id.openBtnIv);
       openFileBtnTv = (TextView) contentView.findViewById(R.id.openBtnTv);

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

       openFileBtn.setOnTouchListener(this);
       saveBtn.setOnTouchListener(this);
       sendMailBtn.setOnTouchListener(this);
       scanQRBtn.setOnTouchListener(this);
       changeBgBtn.setOnTouchListener(this);
       exitBtn.setOnTouchListener(this);
   }

    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //mWindow.setFocusable(true);
        //mWindow.setOutsideTouchable(true);
        mWindow.setContentView(contentView);
        mWindow.setOnDismissListener((BaseActivity)mContext);
    }

    private void clickOpenFileBtn(boolean isClick){
        if(isClick) {
            openFileBtnIv.setBackgroundResource(R.mipmap.openfile_select_icon);
            openFileBtnTv.setTextColor(selectTextColor);
        }else{
            openFileBtnIv.setBackgroundResource(R.mipmap.openfile_normal_icon);
            openFileBtnTv.setTextColor(normalTextColor);
        }
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
            changeBgBtnIv.setBackgroundResource(R.mipmap.changebg_select_icon);
            changeBgBtnTv.setTextColor(selectTextColor);
        }else{
            changeBgBtnIv.setBackgroundResource(R.mipmap.changebg_normal_icon);
            changeBgBtnTv.setTextColor(normalTextColor);
        }
    }

    private void clickExitBtn(boolean isClick){
        if(isClick) {
            exitBtnIv.setBackgroundResource(R.mipmap.exit_select_icon);
            exitBtnTv.setTextColor(selectTextColor);
        }else{
            exitBtnIv.setBackgroundResource(R.mipmap.exit_normal_icon);
            exitBtnTv.setTextColor(normalTextColor);
        }
    }

    private void resetBtn(){
        clickChangeBgBtn(false);
        clickExitBtn(false);
        clickOpenFileBtn(false);
        clickSaveBtn(false);
        clickScanQRBtn(false);
        clickSendMailBtn(false);
    }


    private void clickDown(View view){
        int id = view.getId();
        switch(id){
            case R.id.openBtn:
                clickOpenFileBtn(true);
                break;
            case R.id.saveBtn:
                clickSaveBtn(true);
                break;
            case R.id.sendMailBtn:
                clickSendMailBtn(true);
                break;
            case R.id.scanQRBtn:
                clickScanQRBtn(true);
                break;
            case R.id.changeBgBtn:
                clickChangeBgBtn(true);
                break;
            case R.id.exitBtn:
                clickExitBtn(true);
                break;
        }
    }

    private void clickUp(View view){

        int id = view.getId();

        switch(id){
            case R.id.openBtn:
                clickOpenFileBtn(false);
                ((BaseActivity)mContext).onDMenuOpenFileBtnEvent();
                break;
            case R.id.saveBtn:
                clickSaveBtn(false);
                ((BaseActivity)mContext).onDMenuSaveBtnEvent();
                break;
            case R.id.sendMailBtn:
                clickSendMailBtn(false);
                ((BaseActivity)mContext).onDMenuSendMailBtnEvent();
                break;
            case R.id.scanQRBtn:
                clickScanQRBtn(false);
                ((BaseActivity)mContext).onDMenuScanQRBtnEvent();
                break;
            case R.id.changeBgBtn:
                clickChangeBgBtn(false);
                ((BaseActivity)mContext).onDMenuChangeBgBtnEvent();
                break;
            case R.id.exitBtn:
                clickExitBtn(false);
                ((BaseActivity)mContext).onDMenuExitBtnEvent();
                break;
        }
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {
    }

    private float offsetX = Integer.MIN_VALUE;
    private float offsetY = Integer.MIN_VALUE;

    public void show(View anchor){

        if(offsetX == Integer.MIN_VALUE||offsetY == Integer.MIN_VALUE) {

                float anchorWidth = anchor.getWidth();

                float bottomIvHeight = mContext.getResources().getDimension(R.dimen.dialog_bottom_icon_height);

            float bottomIvWidth = mContext.getResources().getDimension(R.dimen.dialog_bottom_icon_width);

            int anchorPoints[] = new int[2];
            anchor.getLocationInWindow(anchorPoints);

            float bottomIvMarginRight = mContext.getResources().getDimension(R.dimen.menu_bottom_iv__marginright) + bottomIvWidth/2;
            float args = WhiteBoardUtils.whiteBoardWidth - anchorPoints[0] - anchorWidth/2;

            float c = args - bottomIvMarginRight;
           // offsetY = 0;
            offsetX = c;
            offsetY = WhiteBoardUtils.bottomBarHeight ;
        }

            //mWindow.showAsDropDown(anchor,(int)offsetX,(int)offsetY);
        mWindow.showAtLocation(contentView,Gravity.BOTTOM|Gravity.RIGHT,(int)offsetX,(int)offsetY);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if(action == MotionEvent.ACTION_DOWN){
            clickDown(view);
        }else if(action == MotionEvent.ACTION_UP){
            clickUp(view);
            dismiss();
        }else if(action == MotionEvent.ACTION_CANCEL){
            resetBtn();
            dismiss();
        }
        return true;
    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing())
            mWindow.dismiss();
        mContext = null;
        contentView = null;
        openFileBtn = null;
        openFileBtnIv = null;
        openFileBtnTv = null;
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

}
