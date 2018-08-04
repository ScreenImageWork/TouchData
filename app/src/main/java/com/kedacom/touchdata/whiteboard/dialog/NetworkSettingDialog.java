package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2017/1/9.
 */
public class NetworkSettingDialog implements IControler{

    private Context mContext;

    private TPPopupWindow mWindow;

    private View contentView;

    private TextView mMsgTv;

    private TPDialogButton sureBtn;

    private TPDialogButton cancelBtn;

    public NetworkSettingDialog(Context context){
        this.mContext = context;
        LayoutInflater inflater =  LayoutInflater.from(context);

        contentView = inflater.inflate(R.layout.dialog_twobtn,null);
        mMsgTv = (TextView) contentView.findViewById(R.id.msgTv);
        sureBtn = (TPDialogButton) contentView.findViewById(R.id.dialogSureBtn);
        cancelBtn = (TPDialogButton) contentView.findViewById(R.id.dialogCancelBtn);
        TextView titleView = (TextView) contentView.findViewById(R.id.dialogTitle);
        titleView.setText("网络异常");

        String networkUnusable = context.getResources().getString(R.string.networkUnusable);
        mMsgTv.setText(networkUnusable);
        String setting = context.getResources().getString(R.string.setting);
        sureBtn.setText(setting);

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        sureBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)mContext).onDToSettingNetwork();
                dismiss();
            }
        });

        cancelBtn.setOnTPClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mWindow.dismiss();
            }
        });

        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity)context);
    }


    public void setCancelBtnClickListener(View.OnClickListener listener){
        //cancelBtn.setOnClickListener(listener);
    }

    public void initViewState(){
        sureBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
        cancelBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        initViewState();
        mWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
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
        mContext = null;
        contentView = null;
        mWindow = null;
        mMsgTv = null;
        sureBtn = null;
        cancelBtn = null;
    }
}
