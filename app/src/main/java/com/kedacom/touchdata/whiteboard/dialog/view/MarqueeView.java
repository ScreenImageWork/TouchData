package com.kedacom.touchdata.whiteboard.dialog.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zhanglei on 2018/1/31.
 */

@SuppressLint("AppCompatCustomView")
public class MarqueeView extends TextView {


    public MarqueeView(Context context) {
        super(context);
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean isFocused() {
        return true;
    }
}
