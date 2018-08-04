package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2016/11/16.
 */
public class SwitchEraseDialog implements IControler,PopupWindow.OnDismissListener{

    private final static int SELECT_CANCEL = 0;

    private final static int SELECT_ERASE = 1;

    private final static int SELECT_AREAERASE = 2;

    private Context context;

    private TPPopupWindow mWindow;

    private View contentView;

    private RelativeLayout eraseLy;
    private ImageView eraseIv;
    private TextView eraseTv;

    private RelativeLayout areaEraseLy;
    private ImageView areaEraseIv;
    private TextView areaEraseTv;

    private RelativeLayout clearScreenLy;
    private ImageView clearScreenIv;
    private TextView clearScreenTv;

    private int normalTextColor ;
    private int selectTextColor ;

    private int curSelect = SELECT_CANCEL;

    private boolean isShowing = false;

    public SwitchEraseDialog(Context context){
        this.context = context;
        initView();
        initPpw();
    }

    private void initPpw(){
        mWindow = new TPPopupWindow(context);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setOutsideTouchable(true);
        //mWindow.setFocusable(true);
        mWindow.setContentView(contentView);
        mWindow.setOnDismissListener(this);
    }

    private void initView(){

        normalTextColor = context.getResources().getColor(R.color.switcherase_text_normal);
        selectTextColor = context.getResources().getColor(R.color.switcherase_text_select);

        LayoutInflater inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_switcherase,null);

        eraseLy = (RelativeLayout) contentView.findViewById(R.id.eraseLy);
        eraseIv = (ImageView) contentView.findViewById(R.id.eraseImageView);
        eraseTv = (TextView) contentView.findViewById(R.id.eraseTextView);

        areaEraseLy = (RelativeLayout) contentView.findViewById(R.id.areaEraseLy);
        areaEraseIv = (ImageView) contentView.findViewById(R.id.areaEraseImageView);
        areaEraseTv = (TextView) contentView.findViewById(R.id.areaEraseTextView);

        clearScreenLy = (RelativeLayout) contentView.findViewById(R.id.clearScreenLy);
        clearScreenIv = (ImageView) contentView.findViewById(R.id.clearImageView);
        clearScreenTv = (TextView) contentView.findViewById(R.id.clearScreenTextView);


        eraseLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)context).onDEraseBtnEvent();
                select(SELECT_ERASE);
            }
        });

        areaEraseLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)context).onDAreaEraseBtnEvent();
                select(SELECT_AREAERASE);
            }
        });

        clearScreenLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)context).onDClearScreenBtnEvent();
//                select(SELECT_CANCEL);
                mWindow.dismiss();
            }
        });

//        clearScreenLy.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                int action = motionEvent.getActionMasked();
//
//                switch(action){
//                    case MotionEvent.ACTION_DOWN:
//                        clearScreenBtnDown();
//                        break;
//                    case MotionEvent.ACTION_CANCEL:  //按下后然后移动到控件之外就不做处理
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        clearScreenBtnUp();
//                        long eventTime =motionEvent.getEventTime() -  motionEvent.getDownTime();
//                        if(eventTime<=500){//小于500毫秒视为点击，如果超过的话就视为长按 长按不做任何处理
//                            select(SELECT_CANCEL);
//                            ((BaseActivity)context).onDClearScreenBtnEvent();
//                            mWindow.dismiss();
//                        }
//                        break;
//                }
//                return true;
//            }
//        });
    }

    public void reset(){
        select(SELECT_CANCEL);
    }

    private void select(int type){
        curSelect = type;
        switch(type){
            case SELECT_CANCEL:
                selectErase(false);
                selectAreaErase(false);
                break;
            case SELECT_ERASE:
                selectErase(true);
                selectAreaErase(false);
                break;
            case SELECT_AREAERASE:
                selectErase(false);
                selectAreaErase(true);
                break;
        }
    }

    private void selectErase(boolean select){
        if(select) {
            eraseIv.setBackgroundResource(R.mipmap.erase_select_icon);
            eraseTv.setTextColor(selectTextColor);
        }else{
            eraseIv.setBackgroundResource(R.mipmap.erase_normal_icon);
            eraseTv.setTextColor(normalTextColor);
        }
    }

    private void selectAreaErase(boolean select){
        if(select) {
            areaEraseIv.setBackgroundResource(R.mipmap.areaerase_select_icon);
            areaEraseTv.setTextColor(selectTextColor);
        }else{
            areaEraseIv.setBackgroundResource(R.mipmap.areaerase_normal_icon);
            areaEraseTv.setTextColor(normalTextColor);
        }
    }

    private void clearScreenBtnDown(){
        clearScreenIv.setBackgroundResource(R.mipmap.clearscreen_select_icon);
        clearScreenTv.setTextColor(selectTextColor);
    }

    private void clearScreenBtnUp(){
        clearScreenIv.setBackgroundResource(R.mipmap.clearscreen_normal_icon);
        clearScreenTv.setTextColor(normalTextColor);
    }

    @Override
    public boolean isShow() {
        return isShowing;
    }

    @Override
    @Deprecated
    public void show() {
    }

    public void show(View anchor){
        isShowing = true;
        if(curSelect == SELECT_CANCEL){
            select(SELECT_ERASE);
        }

        switch(curSelect){
            case SELECT_ERASE:
                ((BaseActivity)context).onDEraseBtnEvent();
                break;
            case SELECT_AREAERASE:
                ((BaseActivity)context).onDAreaEraseBtnEvent();
                break;
        }

        int width = anchor.getWidth();
        int height = anchor.getHeight();

        int location[] = new int[2];
        anchor.getLocationInWindow(location);

        float bottomBarHeight = context.getResources().getDimension(R.dimen.touchdata_bottombar_height);
        float windowWidth = context.getResources().getDimension(R.dimen.switcherase_panel_width);

        int x = (int)(location[0] + width/2f - windowWidth/2f);
        int y = (int)bottomBarHeight;

        mWindow.showAtLocation(contentView, Gravity.BOTTOM|Gravity.LEFT,x,y);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }


    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        context = null;
        contentView = null;
        mWindow = null;
        eraseLy = null;
        eraseIv = null;
        eraseTv = null;
        areaEraseLy = null;
        areaEraseIv = null;
        areaEraseTv = null;
        clearScreenLy = null;
        clearScreenIv = null;
        clearScreenTv = null;
    }

    @Override
    public void onDismiss() {
        ((BaseActivity)context).onDismiss();
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
}
