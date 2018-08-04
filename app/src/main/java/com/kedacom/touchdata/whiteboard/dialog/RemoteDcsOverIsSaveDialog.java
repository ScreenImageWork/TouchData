package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.text.method.CharacterPickerDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;

/**
 * Created by zhanglei on 2018/4/27.
 */

public class RemoteDcsOverIsSaveDialog  implements IControler {

    private Context context;

    private TPPopupWindow mWindow;

    private View contentView;

    private TextView titleView;

    private TextView msgView;

    private TPDialogButton btn1;

    private TPDialogButton btn2;

    private TPDialogButton btn3;


    public RemoteDcsOverIsSaveDialog(Context context){
        this.context = context;

        LayoutInflater inflater =  LayoutInflater.from(context);

        contentView = inflater.inflate(R.layout.dialog_threebtn, null);
        titleView = (TextView) contentView.findViewById(R.id.dialogTitle);
        msgView = (TextView)contentView.findViewById(R.id.msgTv);
        btn1 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn1);
        btn2 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn2);
        btn3 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn3);

        titleView.setText(R.string.hints);
        msgView.setText(R.string.rmt_dc_dialog_hint);
        btn1.setText(R.string.save);
        btn2.setText(R.string.notSave);
        btn3.setText(R.string.keepUsing);

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)RemoteDcsOverIsSaveDialog.this.context).onDSureBtnEvent(BaseActivity.DialogType.REMOTE_DCS_OVER_SAVE);
                dismiss();
            }
        });

        btn1.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)RemoteDcsOverIsSaveDialog.this.context).onDMenuSaveBtnEvent();
                dismiss();
            }
        });

        btn2.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)RemoteDcsOverIsSaveDialog.this.context).onDSureBtnEvent(BaseActivity.DialogType.REMOTE_DCS_OVER_SAVE);
                dismiss();
            }
        });

        btn3.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)RemoteDcsOverIsSaveDialog.this.context).onDSureBtnEvent(BaseActivity.DialogType.REMOTE_DCS_OVER_SAVE);
                dismiss();
            }
        });

        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity)context);
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        if(isShow()){
            dismiss();
        }
        mWindow = null;
        contentView = null;
    }
}
