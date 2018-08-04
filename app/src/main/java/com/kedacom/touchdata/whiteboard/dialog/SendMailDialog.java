package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.SoftKeyboardMonitor;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPEditText;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.InputTools;
import com.kedacom.utils.NetworkUtil;
import com.kedacom.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhanglei on 2016/12/7.
 */
public class SendMailDialog implements IControler,PopupWindow.OnDismissListener,SoftKeyboardMonitor.OnSoftKeyboardListener{

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private TPEditText titleEdit;

    private TPEditText contentEdit;

    private TPDialogButton surBtn;

    private TPDialogButton cancelBtn;

    private SoftKeyboardMonitor mSoftKeyboardMonitor;

    private LinearLayout hintLayout;

    private TextView hintTv;

    private  String mailTitle;

    private String mails[];

    public SendMailDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }

        private void initView(){

            LayoutInflater inflater = LayoutInflater.from(mContext);

            contentView = inflater.inflate(R.layout.page_sendmail,null);

            titleEdit = (TPEditText)contentView.findViewById(R.id.titleEdit);
            contentEdit = (TPEditText)contentView.findViewById(R.id.contentEdit);

            surBtn = (TPDialogButton) contentView.findViewById(R.id.sureBtn);
            cancelBtn = (TPDialogButton) contentView.findViewById(R.id.cancelBtn);

            mSoftKeyboardMonitor = (SoftKeyboardMonitor) contentView.findViewById(R.id.softKeyboardMonitor);
            mSoftKeyboardMonitor.setOnSoftKeyboardListener(this);

            hintLayout = (LinearLayout) contentView.findViewById(R.id.hintLayout);
            hintTv = (TextView) contentView.findViewById(R.id.hintTv);

            contentView.findViewById(R.id.xbtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            contentEdit.setOnTPEditTextFocusChangeListener(new TPEditText.OnTPEditTextFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b){
                        String text = contentEdit.getText().toString();
                        if(text==null||text.isEmpty()){
                            showHint("请添加收件人地址！");
                        }
                    }
                }
            });


            contentEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = contentEdit.getText().toString();
                    if(text==null||text.isEmpty()){
                        showHint("请添加收件人地址！");
                    }else{
                        String mailStr = contentEdit.getText().toString();
                        mailStr = mailStr.replaceAll("\n",";");

                        String mails[] = null;
                        if(mailStr.contains(";")) {
                            mails = mailStr.split(";");
                        }

                        if(mails==null){
                            mails = new String[1];
                            mails[0] = mailStr;
                        }
                        //4、收件人格式是否正确
                        for(String mail:mails){
                            boolean valid = Utils.checkEmail(mail);
                            if(!valid){
//                            ((BaseActivity)mContext).toastMsg("邮箱 "+mail+" 格式不正确，请确认后重新提交！");
                                showHint("存在格式错误的收件人地址！");
                                return;
                            }
                        }
                        setHintEnable(false);
                    }
                }
            });


            surBtn.setOnTPClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //1、检查当前网络状态
                    boolean netWorkState = NetworkUtil.getNetworkState(mContext);
                    if(!netWorkState){
                        ((BaseActivity)mContext).toastMsg("邮件发送失败，当前网络不可用!");
                        return;
                    }

                     mailTitle = titleEdit.getText().toString();
                     String mailStr = contentEdit.getText().toString();

                     //2、检查标题是否存在
                    if(mailTitle==null||mailTitle.trim().isEmpty()){
                        //Toast.makeText(mContext,"邮件标题不能为空！",Toast.LENGTH_SHORT).show();
                        ((BaseActivity)mContext).toastMsg("邮件标题为空，请添加标题！");
                        return;
                    }

                    //3、收件人是否为空
                    if(mailStr==null||mailStr.trim().isEmpty()){
//                        Toast.makeText(mContext,"收件人不能为空！",Toast.LENGTH_SHORT).show();
                        ((BaseActivity)mContext).toastMsg("收件人为空，请添加收件人！");
                        return;
                    }


                    mails = null;

                   //之前是以；分割的，这里是进行中文和英文转换的，不过现在换成了\n 为了不给其他代码造成影响因此在这里进行了修改(最小修改量)
                    mailStr = mailStr.replaceAll("\n",";");

                    if(mailStr.contains(";")) {
                        mails = mailStr.split(";");
                    }

                    if(mails==null){
                        mails = new String[1];
                        mails[0] = mailStr;
                    }

                    //4、收件人格式是否正确
                    for(String mail:mails){
                        boolean valid = Utils.checkEmail(mail);
                        if(!valid){
//                            ((BaseActivity)mContext).toastMsg("邮箱 "+mail+" 格式不正确，请确认后重新提交！");
                            ((BaseActivity)mContext).toastMsg("请输入正确的收件人地址");
                            return;
                        }
                    }

                    titleEdit.clearFocus();
                    contentEdit.clearFocus();
                    InputTools.hideKeyboard(titleEdit);
                    InputTools.hideKeyboard(contentEdit);
                    handler.sendEmptyMessageDelayed(101,500);

                }
            });

            cancelBtn.setOnTPClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputTools.hideKeyboard(titleEdit);
                    titleEdit.clearFocus();
                    handler.sendEmptyMessageDelayed(100,500);
                }
            });


            contentEdit.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(event.getAction() == KeyEvent.ACTION_DOWN){
                        if(keyCode == KeyEvent.KEYCODE_ENTER){
                            contentEdit.append("\n");
                            return true;
                        }
                    }
                    return false;
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

    private String getCurMeetingName(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH时mm分");

        String dateStr = sdf.format(new Date());

        String meetingName = dateStr + "白板会议纪要";

        sdf = null;

        return meetingName;
    }

    public void showHint(String text){
        if(hintLayout.getVisibility() == View.GONE){
            setHintEnable(true);
        }
        hintTv.setText(text);
    }

    public void setHintEnable(boolean enable){
        if(enable){
            hintLayout.setVisibility(View.VISIBLE);
        }else{
            hintLayout.setVisibility(View.GONE);
        }
    }


    private void reset(){
        contentEdit.setText("");
        setHintEnable(false);
    }

    @Override
    public boolean isShow() {
        if(mWindow==null)
            return false;
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        reset();
        String title = getCurMeetingName();
       // String title = ((BaseActivity)mContext).getCurMeetingName();
        titleEdit.setText(title);
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    @Override
    public void dismiss() {
        titleEdit.clearFocus();
        contentEdit.clearFocus();
        InputTools.hideKeyboard(titleEdit);
        InputTools.hideKeyboard(contentEdit);
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
        titleEdit = null;
        contentEdit = null;
        surBtn = null;
        cancelBtn = null;
    }

    @Override
    public void onDismiss() {
        ((BaseActivity)mContext).onDismiss();
        TPLog.printKeyStatus("onDismiss......");
    }

    @Override
    public void onSoftKeyboardShowing() {
        TPLog.printKeyStatus("-->onSoftKeyboardShowing");
        ((BaseActivity)mContext).dismissToolsBar();
    }

    @Override
    public void onSoftKeyboardHide() {
        TPLog.printKeyStatus("-->onSoftKeyboardHide");
        ((BaseActivity)mContext).showToolsBar();
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                dismiss();
                ((BaseActivity)mContext).showToolsBar();
            }else if(msg.what == 101){
                dismiss();
                ((BaseActivity)mContext).onDSendMailBtnEvent(mailTitle,mails);
            }
        }
    };
}
