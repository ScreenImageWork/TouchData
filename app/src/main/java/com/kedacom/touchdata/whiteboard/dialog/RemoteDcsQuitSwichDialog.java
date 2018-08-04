package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.mtnet.MtConnectManager;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

import java.lang.ref.SoftReference;

/**
 * Created by zhanglei on 2018/6/11.
 */

public class RemoteDcsQuitSwichDialog implements IControler {
    private final static int REDIO_SELECT_TEXT_COLOR = Color.parseColor("#00aff2");
    private final static int REDIO_NORMAL_TEXT_COLOR = Color.parseColor("#b1b1b1");
    private final static int BTN_NORMAL_BK_COLOR = Color.parseColor("#1e1e1e");
    private final static int BTN_SELECT_BK_COLOR = Color.parseColor("#00aff2");
    private final static int BTN_NORMAL_TEXT_COLOR = Color.parseColor("#b1b1b1");
    private final static int BTN_SELECT_TEXT_COLOR = Color.parseColor("#ffffff");


    private SoftReference<Context> mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private LinearLayout dcsQuitRedio;
    private ImageView  dcsQuitRedioIcon;
    private TextView  dcsQuitRedioTv;
    private LinearLayout dcsOverRedio;
    private ImageView dcsOverRedioIcon;
    private TextView dcsOverRedioTv;

    private TPDialogButton sureBtn;

    private boolean isQuit = true;


    public RemoteDcsQuitSwichDialog(Context context){
        mContext = new SoftReference<Context>(context);
        initView();
        initWindow();
    }

    private void  initView(){
        if(mContext==null||mContext.get()==null)
            return;
        LayoutInflater inflater = LayoutInflater.from(mContext.get());
        contentView = inflater.inflate(R.layout.dialog_remote_dcs_quit_switch, null);

        dcsQuitRedio = (LinearLayout) contentView.findViewById(R.id.dcsQuitRedio);
        dcsQuitRedioIcon = (ImageView) contentView.findViewById(R.id. dcsQuitRedioIcon);
        dcsQuitRedioTv = (TextView) contentView.findViewById(R.id. dcsQuitRedioTv);

        dcsOverRedio = (LinearLayout) contentView.findViewById(R.id.dcsOverRedio);
        dcsOverRedioIcon = (ImageView) contentView.findViewById(R.id.dcsOverRedioIcon);
        dcsOverRedioTv = (TextView) contentView.findViewById(R.id.dcsOverRedioTv);

        sureBtn = (TPDialogButton) contentView.findViewById(R.id.sureBtn);
        sureBtn.setClickColor(BTN_SELECT_TEXT_COLOR,BTN_NORMAL_TEXT_COLOR,BTN_NORMAL_BK_COLOR,BTN_SELECT_BK_COLOR);

        dcsQuitRedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRedio(v);
            }
        });

        dcsOverRedio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRedio(v);
            }
        });

        sureBtn.setOnTPClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isQuit){
                    MtConnectManager.getInstance().getMtNetSender().quitConfReq(MtNetUtils.achConfE164);
                }else{
                    MtConnectManager.getInstance().getMtNetSender().releaseConf(MtNetUtils.achConfE164);
                }
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
        mWindow.setOutsideTouchable(true);
    }

    private void selectRedio(View view){
        dcsQuitRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_false);
        dcsOverRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_false);
        dcsQuitRedioTv.setTextColor(REDIO_NORMAL_TEXT_COLOR);
        dcsOverRedioTv.setTextColor(REDIO_NORMAL_TEXT_COLOR);

        if(view.getId() == R.id.dcsQuitRedio){
            isQuit = true;
            dcsQuitRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_true);
            dcsQuitRedioTv.setTextColor(REDIO_SELECT_TEXT_COLOR);
        }else{
            isQuit = false;
            dcsOverRedioIcon.setBackgroundResource(R.mipmap.dcs_redio_true);
            dcsOverRedioTv.setTextColor(REDIO_SELECT_TEXT_COLOR);
        }
    }


    @Override
    public boolean isShow() {
        if(mWindow==null){
            return false;
        }
        return mWindow.isShowing();
    }

    public void show(View anchor){
        selectRedio(dcsQuitRedio);
        int width = anchor.getWidth();
        int height = anchor.getHeight();

        int location[] = new int[2];
        anchor.getLocationInWindow(location);

        float bottomBarHeight = mContext.get().getResources().getDimension(R.dimen.touchdata_bottombar_height);
        float windowWidth = 360;//bottomIvWidth + bottomIvMarginRight + 80*1.5f +

        int x = (int)(location[0] + width/2f - windowWidth/2f + 45);
        int y = (int)bottomBarHeight;

        mWindow.showAtLocation(contentView, Gravity.BOTTOM|Gravity.LEFT,x,y);
    }


    @Override
    @Deprecated
    public void show() {
        selectRedio(dcsQuitRedio);

    }

    @Override
    public void dismiss() {
        if(mWindow!=null){
            mWindow.dismiss();
        }
    }

    @Override
    public void destory() {
        dismiss();
        mWindow = null;
    }
}
