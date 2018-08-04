package com.kedacom.touchdata.whiteboard.dialog.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.view.TimeView;

/**
 * Created by zhanglei on 2018/6/1.
 */

public class TPDialogButtonFromLinearLayout extends LinearLayout implements View.OnClickListener,ITPButton{

    private final int NORMAL_TEXT_COLOR;
    private final int SELECT_TEXT_COLOR;

    private OnClickListener curListener;

    private TextView bindTextView;

    private TimeView bindTimeView;

    public TPDialogButtonFromLinearLayout(Context context) {
        super(context);
        SELECT_TEXT_COLOR= getResources().getColor(R.color.dialog_btn_text_select_color);
        NORMAL_TEXT_COLOR = getResources().getColor(R.color.dialog_btn_text_normal_color);
        init();
    }

    public TPDialogButtonFromLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        SELECT_TEXT_COLOR= getResources().getColor(R.color.dialog_btn_text_select_color);
        NORMAL_TEXT_COLOR = getResources().getColor(R.color.dialog_btn_text_normal_color);
        init();
    }

    public TPDialogButtonFromLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        SELECT_TEXT_COLOR= getResources().getColor(R.color.dialog_btn_text_select_color);
        NORMAL_TEXT_COLOR = getResources().getColor(R.color.dialog_btn_text_normal_color);
        init();
    }

    private void init(){
        setOnClickListener(this);
    }

    public void bindTextView(TextView tv){
        bindTextView = tv;
    }

    @Override
    public void onClick(View v) {
        TPDialogBtnHelper.getInstance().executeBtnClick(this);
    }

    public void preClick(){
        setBackgroundResource(R.color.dialog_btn_pressedColor);
        if(bindTextView!=null){
            bindTextView.setTextColor(SELECT_TEXT_COLOR);
        }
        if(newListener!=null){
            newListener.onPreClick();
        }
    }

    public void click(){
        setBackgroundResource(R.color.dialog_btn_normalColor);
        if(bindTextView!=null){
            bindTextView.setTextColor(NORMAL_TEXT_COLOR);
        }
        if(newListener!=null){
            newListener.onClick();
        }
        curListener.onClick(this);
    }

    public void setOnTPClickListener(OnClickListener l) {
        curListener = l;
    }

    private OnTPNewClickListener newListener;
    public void setOnTPNewClickListener(OnTPNewClickListener l) {
        newListener = l;
    }

    public interface OnTPNewClickListener{
        void onPreClick();
        void onClick();
    }
}
