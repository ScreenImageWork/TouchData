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
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2017/5/4.
 */
public class ToMailConfigSettingDialog implements IControler {

    private final int BTN_TEXT_SELECT_COLOR;

    private final int BTN_TEXT_NORMAL_COLOR;

    private Context mContext;

    private View contentView;

    private TextView mMsgTv;

    private Button sureBtn;

    private Button cancelBtn;

    private TPPopupWindow mWindow;

    private View.OnClickListener mCancelBtnListener;


    public ToMailConfigSettingDialog(Context context){
        mContext = context;

        BTN_TEXT_SELECT_COLOR = context.getResources().getColor(R.color.dialog_btn_text_select_color);
        BTN_TEXT_NORMAL_COLOR = context.getResources().getColor(R.color.dialog_btn_text_normal_color);

        LayoutInflater inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_twobtn,null);
        mMsgTv = (TextView) contentView.findViewById(R.id.msgTv);
        sureBtn = (Button) contentView.findViewById(R.id.dialogSureBtn);
        cancelBtn = (Button) contentView.findViewById(R.id.dialogCancelBtn);
        TextView titleView = (TextView) contentView.findViewById(R.id.dialogTitle);
        titleView.setText("邮件配置异常");

        sureBtn.setText("去设置");

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBtn.setTextColor(BTN_TEXT_SELECT_COLOR);
                ((BaseActivity)mContext).onDToMailConfigSetting();
                dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                sureBtn.setBackgroundResource(R.color.dialog_btn_normalColor);
//                sureBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
//                cancelBtn.setBackgroundResource(R.drawable.dialog_btn_style);
                cancelBtn.setTextColor(BTN_TEXT_SELECT_COLOR);

                dismiss();
            }
        });


        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity)context);

        mMsgTv.setText("发送失败，请配置正确的邮箱账户和密码！");
    }

    private void initViewState(){
        sureBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
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

    @Override
    @Deprecated
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


//    Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            if(msg.what==100){
//                dismiss();
//            }
//        }
//    };


}