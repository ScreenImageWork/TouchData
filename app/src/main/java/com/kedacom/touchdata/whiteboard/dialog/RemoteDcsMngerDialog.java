package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.tplog.TPLog;

import java.lang.ref.SoftReference;

/**
 * Created by zhanglei on 2018/6/8.
 */

public class RemoteDcsMngerDialog implements IControler ,PopupWindow.OnDismissListener{

    private final static int BTN_NORMAL_BK_COLOR = Color.parseColor("#1e1e1e");
    private final static int BTN_SELECT_BK_COLOR = Color.parseColor("#00aff2");
    private final static int BTN_NORMAL_TEXT_COLOR = Color.parseColor("#b1b1b1");
    private final static int BTN_SELECT_TEXT_COLOR = Color.parseColor("#ffffff");

    private final static int REDIO_SELECT_TEXT_COLOR = Color.parseColor("#00aff2");
    private final static int REDIO_NORMAL_TEXT_COLOR = Color.parseColor("#b1b1b1");

    private SoftReference<Context>  mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private LinearLayout interface1;
    private LinearLayout interface2;
    private LinearLayout dcsMngTalkRedio;
    private ImageView dcsMngTalkRedioIcon;
    private TextView dcsMngTalkRedioTv;
    private LinearLayout dcsMngSpeakerRedio;
    private ImageView dcsMngSpeakerRedioIcon;
    private TextView dcsMngSpeakerRedioTv;

    private TPDialogButton rlsDcsMngerBtn;
    private TPDialogButton rlsDcsMngerSureBtn;
    private TPDialogButton rlsDcsMngerCancelBtn;

    public RemoteDcsMngerDialog( Context context){
        mContext = new SoftReference<Context>(context);
        initView();
        initWindow();
    }

    private void  initView(){
        if(mContext==null||mContext.get()==null)
            return;
        LayoutInflater inflater = LayoutInflater.from(mContext.get());
        contentView = inflater.inflate(R.layout.dialog_remote_dcsmnger, null);

        interface1 = (LinearLayout) contentView.findViewById(R.id.interface1);
        interface2 = (LinearLayout) contentView.findViewById(R.id.interface2);

        dcsMngTalkRedio = (LinearLayout) contentView.findViewById(R.id.dcsMngTalkRedio);
        dcsMngTalkRedioIcon = (ImageView) contentView.findViewById(R.id.dcsMngTalkRedioIcon);
        dcsMngTalkRedioTv = (TextView) contentView.findViewById(R.id.dcsMngTalkRedioTv);

        dcsMngSpeakerRedio = (LinearLayout) contentView.findViewById(R.id.dcsMngSpeakerRedio);
        dcsMngSpeakerRedioIcon = (ImageView) contentView.findViewById(R.id.dcsMngSpeakerRedioIcon);
        dcsMngSpeakerRedioTv = (TextView) contentView.findViewById(R.id.dcsMngSpeakerRedioTv);

        rlsDcsMngerBtn = (TPDialogButton) contentView.findViewById(R.id.rlsDcsMngerBtn);
        rlsDcsMngerSureBtn = (TPDialogButton) contentView.findViewById(R.id.rlsDcsMngerSureBtn);
        rlsDcsMngerCancelBtn = (TPDialogButton) contentView.findViewById(R.id.rlsDcsMngerCancelBtn);

        rlsDcsMngerBtn.setClickColor(BTN_SELECT_TEXT_COLOR,BTN_NORMAL_TEXT_COLOR,BTN_NORMAL_BK_COLOR,BTN_SELECT_BK_COLOR);
        rlsDcsMngerSureBtn.setClickColor(BTN_SELECT_TEXT_COLOR,BTN_NORMAL_TEXT_COLOR,BTN_NORMAL_BK_COLOR,BTN_SELECT_BK_COLOR);
        rlsDcsMngerCancelBtn.setClickColor(BTN_SELECT_TEXT_COLOR,BTN_NORMAL_TEXT_COLOR,BTN_NORMAL_BK_COLOR,BTN_SELECT_BK_COLOR);

        dcsMngTalkRedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRedio(v);
                ((BaseActivity)mContext.get()).onDDataConfModeChange(MtNetUtils.MODE_DATA_CONF_TALK);
            }
        });

        dcsMngSpeakerRedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRedio(v);
                ((BaseActivity)mContext.get()).onDDataConfModeChange(MtNetUtils.MODE_DATA_CONF_SPEAKER);
            }
        });

        rlsDcsMngerBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterface2();
            }
        });

        rlsDcsMngerSureBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContext!=null&&mContext.get()!=null){
                    ((BaseActivity)mContext.get()).onDRlsDcsMnger();
                }
                dismiss();
            }
        });

        rlsDcsMngerCancelBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        if(mContext==null||mContext.get()==null)
            return;
        mWindow = new TPPopupWindow(mContext.get());
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setFocusable(false);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
        mWindow.setOnDismissListener(this);
        mWindow.setOutsideTouchable(true);
    }

    private void initBtnState(){
        if(MtNetUtils.curDataConfMode == MtNetUtils.MODE_DATA_CONF_TALK){
            selectRedio(dcsMngTalkRedio);
        }else{
            selectRedio(dcsMngSpeakerRedio);
        }
        showInterface1();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener disLis){
        if(mWindow!=null){
            mWindow.setOnDismissListener(disLis);
        }
    }


    private void selectRedio(View view){
        dcsMngTalkRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_false);
        dcsMngSpeakerRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_false);
        dcsMngTalkRedioTv.setTextColor(REDIO_NORMAL_TEXT_COLOR);
        dcsMngSpeakerRedioTv.setTextColor(REDIO_NORMAL_TEXT_COLOR);

        if(view.getId() == R.id.dcsMngTalkRedio){
            dcsMngTalkRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_true);
            dcsMngTalkRedioTv.setTextColor(REDIO_SELECT_TEXT_COLOR);
        }else{
            dcsMngSpeakerRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_true);
            dcsMngSpeakerRedioTv.setTextColor(REDIO_SELECT_TEXT_COLOR);
        }
    }

    private void showInterface2(){
        interface2.setVisibility(View.VISIBLE);
    }

    private void showInterface1(){
        interface2.setVisibility(View.GONE);
    }


    @Override
    public boolean isShow() {
        if(mWindow==null){
            return false;
        }
        return mWindow.isShowing();
    }


    public void show(View anchor){
        initBtnState();
        int width = anchor.getWidth();
        int height = anchor.getHeight();

        int location[] = new int[2];
        anchor.getLocationInWindow(location);

        float bottomBarHeight = mContext.get().getResources().getDimension(R.dimen.touchdata_bottombar_height);
        float windowWidth = 370;//bottomIvWidth + bottomIvMarginRight + 80*1.5f +

        int x = (int)(location[0] + width/2f - windowWidth/2f);
        int y = (int)bottomBarHeight;

        mWindow.showAtLocation(contentView, Gravity.BOTTOM|Gravity.LEFT,x,y);
    }

    @Override
    public void show() {
        initBtnState();

    }

    @Override
    public void dismiss() {
        if(mWindow!=null)
            mWindow.dismiss();
    }

    @Override
    public void destory() {
        dismiss();
        mWindow = null;
    }

    @Override
    public void onDismiss() {

    }
}
