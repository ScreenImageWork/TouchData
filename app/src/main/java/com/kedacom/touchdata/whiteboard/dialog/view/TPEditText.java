package com.kedacom.touchdata.whiteboard.dialog.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.kedacom.touchdata.R;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.InputTools;

/**
 * Created by zhanglei on 2016/12/26.
 */
@SuppressLint("AppCompatCustomView")
public class TPEditText extends EditText implements View.OnFocusChangeListener{

    private  int normal_color;

    private  int select_color;

    public TPEditText(Context context){
        super(context);
        init(context);
    }

    public TPEditText(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public TPEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context conetxt){
        normal_color = conetxt.getResources().getColor(R.color.page_edit_normal_color);
        select_color = conetxt.getResources().getColor(R.color.page_edit_select_color);
        setFocusable(true);
        setSelectAllOnFocus(true);
        setOnFocusChangeListener(this);
    }



    @Override
    public void onFocusChange(View view, boolean b) {
        if(b){
            setTextColor(select_color);
        }else{
            setTextColor(normal_color);
        }

        if(mFocusChangeListener!=null){
            mFocusChangeListener.onFocusChange(view,b);
        }
    }

    private OnTPEditTextFocusChangeListener mFocusChangeListener;
    public void setOnTPEditTextFocusChangeListener(OnTPEditTextFocusChangeListener listener){
        mFocusChangeListener = listener;
    }

    public interface OnTPEditTextFocusChangeListener{
         void onFocusChange(View view,boolean b);
    }

}
