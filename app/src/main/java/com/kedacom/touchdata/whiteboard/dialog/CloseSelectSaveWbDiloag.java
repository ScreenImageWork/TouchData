package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
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
 * Created by zhanglei on 2017/5/3.
 */
public class CloseSelectSaveWbDiloag implements IControler{

    private final int BTN_TEXT_SELECT_COLOR;

    private final int BTN_TEXT_NORMAL_COLOR;

    private Context mContext;

    private View contentView;

    private TextView mMsgTv;

    private TPDialogButton saveBtn;

    private TPDialogButton closeBtn;

    private TPDialogButton cancelBtn;

    private TPPopupWindow mWindow;

    private View.OnClickListener mCancelBtnListener;

    public CloseSelectSaveWbDiloag(Context context){
        BTN_TEXT_SELECT_COLOR = context.getResources().getColor(R.color.dialog_btn_text_select_color);
        BTN_TEXT_NORMAL_COLOR = context.getResources().getColor(R.color.dialog_btn_text_normal_color);

        mContext = context;

        initView();
        initWindow();
    }


    private void initView(){
        LayoutInflater inflater =  LayoutInflater.from(mContext);
        contentView = inflater.inflate(R.layout.dialog_closesave, null);
        mMsgTv = (TextView)contentView.findViewById(R.id.msgTv);
        saveBtn = (TPDialogButton)contentView.findViewById(R.id.dialogBtn1);
        closeBtn = (TPDialogButton)contentView.findViewById(R.id.dialogBtn2);
        cancelBtn = (TPDialogButton)contentView.findViewById(R.id.dialogBtn3);

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        saveBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                saveBtn.setTextColor(BTN_TEXT_SELECT_COLOR);
                ((BaseActivity)mContext).onDIsSaveDialogSurBtnEvent();
                dismiss();
            }
        });

        closeBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                closeBtn.setBackgroundResource(R.drawable.dialog_btn_style);
//                closeBtn.setTextColor(BTN_TEXT_SELECT_COLOR);
                ((BaseActivity)mContext).onDIsSaveDialogCancelBtnEvent();
                dismiss();
            }
        });

        cancelBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 //               cancelBtn.setBackgroundResource(R.drawable.dialog_btn_style);
//                saveBtn.setBackgroundResource(R.color.dialog_btn_normalColor);
//                cancelBtn.setTextColor(BTN_TEXT_SELECT_COLOR);
//                saveBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
                ((BaseActivity)mContext).onDCloseSelectSaveWbDiloagCancelBtnEvent();
                dismiss();
            }
        });
    }

    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity)mContext);
    }

    private void initViewState(){
        saveBtn.setBackgroundResource(R.drawable.dialog_btn_style2);
        closeBtn.setBackgroundResource(R.drawable.dialog_btn_style2);
        cancelBtn.setBackgroundResource(R.drawable.dialog_btn_style2);

        saveBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
        closeBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
        cancelBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
    }


    public void setMsg(String msg){
        mMsgTv.setText(msg);
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }


    public void show(String msg){
        setMsg(msg);
        show();
    }

    @Deprecated
    @Override
    public void show() {
        initViewState();
        if(mWindow!=null){
            mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
        }
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
        saveBtn = null;
        closeBtn = null;
        cancelBtn = null;
    }


    Handler handler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                ((BaseActivity)mContext).onDIsSaveDialogCancelBtnEvent();
                dismiss();
            }else if(msg.what == 101){
                ((BaseActivity)mContext).onDCloseSelectSaveWbDiloagCancelBtnEvent();
                dismiss();
            }
        }
    };
}
