package com.kedacom.touchdata.whiteboard.dialog.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.kedacom.touchdata.R;

/**
 * Created by zhanglei on 2017/7/24.
 */
@SuppressLint("AppCompatCustomView")
public class TPDialogButton extends Button implements View.OnClickListener,ITPButton{

    private  int NORMAL_TEXT_COLOR;
    private  int SELECT_TEXT_COLOR;
    private int NORMAL_BK_COLOR;
    private int SELECT_BK_COLOR;

    private OnClickListener curListener;

    public TPDialogButton(Context context) {
        super(context);
        init();
    }

    public TPDialogButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TPDialogButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOnClickListener(this);
        SELECT_TEXT_COLOR= getResources().getColor(R.color.dialog_btn_text_select_color);
        NORMAL_TEXT_COLOR = getResources().getColor(R.color.dialog_btn_text_normal_color);
        NORMAL_BK_COLOR = getResources().getColor(R.color.dialog_btn_normalColor);
        SELECT_BK_COLOR = getResources().getColor(R.color.dialog_btn_pressedColor);
    }

    public void setClickColor(int selectTextColor,int noramlTextColor,int normalBkColor,int selectBkColor){
        SELECT_TEXT_COLOR = selectTextColor;
        NORMAL_TEXT_COLOR = noramlTextColor;
        NORMAL_BK_COLOR = normalBkColor;
        SELECT_BK_COLOR = selectBkColor;
    }

    @Override
    public void onClick(View v) {
        TPDialogBtnHelper.getInstance().executeBtnClick(this);
    }

    public void preClick(){
        setTextColor(SELECT_TEXT_COLOR);
        setBackgroundColor(SELECT_BK_COLOR);
//        setBackgroundResource(R.color.dialog_btn_pressedColor);
    }

    public void click(){
//        setBackgroundResource(R.color.dialog_btn_normalColor);
        setBackgroundColor(NORMAL_BK_COLOR);
        setTextColor(NORMAL_TEXT_COLOR);
        if(curListener!=null)
         curListener.onClick(this);
    }

    public void setOnTPClickListener(OnClickListener l) {
        curListener = l;
    }
}
