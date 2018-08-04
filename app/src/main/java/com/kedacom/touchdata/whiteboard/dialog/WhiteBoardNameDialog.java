package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2016/11/22.
 */
public class WhiteBoardNameDialog implements IControler{

    private Context mContext;

    private View contentView;

    private TextView nameTv;

    private ImageView closeBtn;

    private TPPopupWindow mWindow;

    @TargetApi(Build.VERSION_CODES.M)
    public WhiteBoardNameDialog(Context context){

        this.mContext = context;

        LayoutInflater inflater =  LayoutInflater.from(context);

        contentView = inflater.inflate(R.layout.dialog_wbname,null);
        nameTv = (TextView)contentView.findViewById(R.id.wbNameTv);
        closeBtn = (ImageView)contentView.findViewById(R.id.closeWbBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)mContext).onDCloseWbBtnEvent();
            }
        });

        mWindow = new TPPopupWindow(context);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(0);
        mWindow.setOnDismissListener((BaseActivity)context);
        }

    public void setWbName(String wbName){
        String text = nameTv.getText().toString();
        if(text.equals(wbName)){
            return;
        }
        nameTv.setText(wbName);
        dismiss();
      //  mWindow.showAtLocation(contentView, Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
    }


    @Override
    public boolean isShow() {
        if(mWindow==null)
            return false;
        return mWindow.isShowing();
    }

    @Override
    public void show() {
//        if(mWindow!=null&&!mWindow.isShowing())
//            mWindow.showAtLocation(contentView, Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
        //handler.sendEmptyMessageDelayed(100,300);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                if(mWindow!=null&&!mWindow.isShowing())
                mWindow.showAtLocation(contentView, Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
            }
        }
    };


    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        mContext = null;
        contentView = null;
        mWindow = null;
        nameTv = null;
        closeBtn = null;
    }


}
