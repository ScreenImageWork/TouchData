package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.SoftKeyboardMonitor;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPEditText;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.InputTools;

/**
 * Created by zhanglei on 2016/12/7.
 */
public class SaveDialog implements IControler,PopupWindow.OnDismissListener,SoftKeyboardMonitor.OnSoftKeyboardListener,TPEditText.OnTPEditTextFocusChangeListener{

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private boolean isSaveAll;

    private TPEditText fileNameEdit;

    private TextView filePathTv;

    private TPDialogButton saveBtn;

    private TPDialogButton cancelBtn;

    private LinearLayout saveAllBtn;

    private ImageView saveAllBtnIv;

    private IPage curSavePage;

    private  SoftKeyboardMonitor mSoftKeyboardMonitor;


    public SaveDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }


    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);

        contentView = inflater.inflate(R.layout.page_save,null);

        fileNameEdit = (TPEditText) contentView.findViewById(R.id.fileNameEdit);
        filePathTv = (TextView) contentView.findViewById(R.id.filePathTv);

        saveBtn = (TPDialogButton) contentView.findViewById(R.id.sureBtn);
        cancelBtn = (TPDialogButton) contentView.findViewById(R.id.cancelBtn);

        saveAllBtn = (LinearLayout) contentView.findViewById(R.id.selectSaveAllBtn);
        saveAllBtnIv = (ImageView)contentView.findViewById(R.id.saveAllBtnIv);

        mSoftKeyboardMonitor = (SoftKeyboardMonitor) contentView.findViewById(R.id.softKeyboardMonitor);
        mSoftKeyboardMonitor.setOnSoftKeyboardListener(this);

        contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)mContext).onDSaveDilaogCancelBtnEvent();
                dismiss();
            }
        });

        //fileNameEdit.setOnTPEditTextFocusChangeListener(this);

        saveBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = fileNameEdit.getText().toString();
                String filePath = filePathTv.getText().toString();

                if(fileName==null||fileName.trim().isEmpty()){//文件名不合法
                    Toast.makeText(mContext,"文件名不能为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                //callBack
                ((BaseActivity)mContext).onDSaveBtnEvent(fileName,isSaveAll);
                dismiss();
            }
        });

        cancelBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)mContext).onDSaveDilaogCancelBtnEvent();
                fileNameEdit.clearFocus();
                InputTools.hideKeyboard(fileNameEdit);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                },300);
            }
        });

        saveAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSaveAll = !isSaveAll;
                resetSaveBtnState();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setOnDismissListener(this);
    }

    private void initViewState(){
        cancelBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
        saveBtn.setTextColor(mContext.getResources().getColor(R.color.dialog_btn_text_normal_color));
    }

    private void resetSaveBtnState(){
//        if(isSaveAll){
//            saveAllBtnIv.setBackgroundResource(R.mipmap.saveall_select_icon);
//        }else{
//            saveAllBtnIv.setBackgroundResource(R.mipmap.saveall_normal_icon);
//        }
    }

    public void setSaveAllBtnIsDisplay(boolean isDisplay){
        if(!isDisplay) {
            saveAllBtn.setVisibility(View.GONE);
        }else{
            saveAllBtn.setVisibility(View.VISIBLE);
        }
    }

    public void setSaveBtnText(String text){
//        int btnWidth = (int)mContext.getResources().getDimension(R.dimen.dialog_btnWidth);
//        if(text.length()>2){
//            btnWidth = (int)mContext.getResources().getDimension(R.dimen.dialog_btnWidth2);
//        }
//        saveBtn.getLayoutParams().width = btnWidth;
        saveBtn.setText(text);
    }


    private void reset(){
        isSaveAll = false;
        saveAllBtnIv.setBackgroundResource(R.mipmap.saveall_normal_icon);
        initViewState();
    }

    @Override
    public boolean isShow() {
        if(mWindow==null)
            return false;
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {
        reset();
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    public void show(String defaultFileName,String saveDir){
        reset();
        fileNameEdit.setText(defaultFileName);
        filePathTv.setText(saveDir);
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    public void setCurSavePage(IPage curSavePage) {
        this.curSavePage = curSavePage;
    }

    public boolean isSaveAll() {
        return isSaveAll;
    }

    public void setSaveAll(boolean saveAll) {
        isSaveAll = saveAll;
        resetSaveBtnState();
    }

    @Override
    public void dismiss() {
        fileNameEdit.clearFocus();
        InputTools.hideKeyboard(fileNameEdit);
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
        fileNameEdit = null;
        filePathTv = null;
        saveBtn = null;
        cancelBtn = null;
        saveAllBtn = null;
        saveAllBtnIv = null;
        curSavePage = null;
    }

    @Override
    public void onDismiss() {
        TPLog.printKeyStatus("onDismiss...........");
        ((BaseActivity)mContext).onDismiss();
        ((BaseActivity)mContext).showToolsBar();
    }

    @Override
    public void onSoftKeyboardShowing() {
        ((BaseActivity)mContext).dismissToolsBar();
    }

    @Override
    public void onSoftKeyboardHide() {
        ((BaseActivity)mContext).showToolsBar();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
//        ((BaseActivity)mContext).dismissToolsBar();
    }
}
