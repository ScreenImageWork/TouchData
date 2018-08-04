package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2018/5/24.
 */

public class SwitchQuitOrOverConfDialog implements IControler {

    private Context mContext;

    private View contentView;

    private TextView mMsgTv;

    private TPDialogButton sureBtn;

    private TPDialogButton cancelBtn;

    private TPPopupWindow mWindow;


    public SwitchQuitOrOverConfDialog(Context context) {
        mContext = context;

        LayoutInflater inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_twobtn, null);
        mMsgTv = (TextView) contentView.findViewById(R.id.msgTv);
        sureBtn = (TPDialogButton) contentView.findViewById(R.id.dialogSureBtn);
        cancelBtn = (TPDialogButton) contentView.findViewById(R.id.dialogCancelBtn);

        sureBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cancelBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity) context);
    }

    public void setMsg(String msg) {
        mMsgTv.setText(msg);
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    public void show(String msg) {
        sureBtn.setBackgroundResource(R.drawable.dialog_btn_style2);
        sureBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
        cancelBtn.setBackgroundResource(R.drawable.dialog_btn_style2);
        cancelBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
        setMsg(msg);
        show();
    }

    @Override
    @Deprecated
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        if (mWindow != null && mWindow.isShowing()) {
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
