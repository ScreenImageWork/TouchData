package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kedacom.kdv.mt.mtapi.ConfCtrl;
import com.kedacom.kdv.mt.mtapi.LocContactCtrl;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.mtnet.MtConnectManager;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.RemoteDcsOnOrOffBtn;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.NavigationBarHelp;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhanglei on 2017/6/27.
 */
public class ToolsBarDialog implements IControler,PopupWindow.OnDismissListener{

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    public Button menuBtn;

    public Button insertImgBtn;

    public Button selectImgBtn;

    public Button paintBtn;

    public ImageView paintColorIv;

    public Button eraseBtn;

    public Button undoBtn;

    public Button redoBtn;

    public Button selfAdaptionBtn;

    public Button addPageBtn;

    public Button prePageBtn;

    public Button pageNumBtn;

    public Button nextPageBtn;

    private  RemoteDcsHintDialog mRemoteDcsHintDialog;

    private RemoteDcsMngerDialog mRemoteDcsMngerDialog;

    private RemoteDcsQuitSwichDialog mRemoteDcsQuitSwichDialog;

    private RemoteDcsOnOrOffBtn joinConfBtn;//temp

    private TextView confMemberNumTv;

    private RelativeLayout confMemberNumLayout;

    private Button reqOperBtn;

    private LinearLayout reqOperHintLayout;

    private TextView reqOperHintTv;

    private Button mngerBtn;

    private int animCount = 0;

    private Handler hand = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                animCount++;
                switch(animCount){
                    case 0:
                        reqOperHintTv.setText("等待管理员审批");
                        break;
                    case 1:
                        reqOperHintTv.setText("等待管理员审批.");
                        break;
                    case 2:
                        reqOperHintTv.setText("等待管理员审批..");
                        break;
                    case 3:
                        reqOperHintTv.setText("等待管理员审批...");
                        animCount = -1;
                        break;
                }
                this.sendEmptyMessageDelayed(100,1000);
            }
        }
    };

    public ToolsBarDialog( Context context){
        mContext = context;

        initView();
        initWindow();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);

        contentView = inflater.inflate(R.layout.dialog_toolsbar,null);

        menuBtn = (Button) contentView.findViewById(R.id.menuBtn);
        insertImgBtn = (Button) contentView.findViewById(R.id.insertImgBtn);
        selectImgBtn = (Button) contentView.findViewById(R.id.selectImgBtn);
        paintBtn = (Button) contentView.findViewById(R.id.paintBtn);
        eraseBtn = (Button) contentView.findViewById(R.id.eraseBtn);
        undoBtn = (Button) contentView.findViewById(R.id.undoBtn);
        redoBtn = (Button) contentView.findViewById(R.id.redoBtn);
        selfAdaptionBtn = (Button) contentView.findViewById(R.id.selfAdaptionBtn);
        addPageBtn = (Button) contentView.findViewById(R.id.addBtn);
        prePageBtn = (Button) contentView.findViewById(R.id.prePageBtn);
        pageNumBtn = (Button) contentView.findViewById(R.id.pageNumBtn);
        nextPageBtn = (Button) contentView.findViewById(R.id.nextPageBtn);
        paintColorIv = (ImageView)contentView.findViewById(R.id.paintColorIv);

        reqOperBtn = (Button) contentView.findViewById(R.id.reqOperBtn);
        reqOperHintLayout = (LinearLayout) contentView.findViewById(R.id.reqOperHintLy);
        reqOperHintTv = (TextView) contentView.findViewById(R.id.reqOperHintTv);

        mngerBtn = (Button) contentView.findViewById(R.id.mngerBtn);

        confMemberNumTv = (TextView) contentView.findViewById(R.id.confMemberNumTv);

        confMemberNumLayout = (RelativeLayout) contentView.findViewById(R.id.confMemberNumLayout);

        joinConfBtn = (RemoteDcsOnOrOffBtn)contentView.findViewById(R.id.joinConf);
        joinConfBtn.setOnSelectListener(new RemoteDcsOnOrOffBtn.OnSelectListener() {
            @Override
            public void onSelect(boolean select) {
                TPLog.printError("select--->"+select);
                if(select){
                    //加入会议第一步
                    MtNetUtils.confJoining = true;
                    MtConnectManager.getInstance().getMtNetSender().reqMtConfState();
                    mRemoteDcsHintDialog.dismiss();
                }else{
                    if(!MtNetUtils.isConfManager) {
                        MtConnectManager.getInstance().getMtNetSender().quitWBConf(MtNetUtils.achConfE164, 1);
                        if (NetUtil.hasVideoConf) {
                            mRemoteDcsHintDialog.show(joinConfBtn);
                        }
                    }else{
                        joinConfBtn.setSelect(true);
                        //弹出退会或者节会对话框
                        if(mRemoteDcsQuitSwichDialog!=null){
                            mRemoteDcsQuitSwichDialog.show(joinConfBtn);
                        }
                    }
                }
            }
        });


        mngerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mngerBtn.setBackgroundResource(R.mipmap.req_oper_down);
                if(mRemoteDcsMngerDialog!=null){
                    mRemoteDcsMngerDialog.show(mngerBtn);
                }
            }
        });

        contentView.findViewById(R.id.testBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MtConnectManager.getInstance().getMtNetSender().reqApplyOper(MtNetUtils.achConfE164);
//                MtConnectManager.getInstance().getMtNetSender().reqDCSGetUserList(MtNetUtils.achConfE164);
                TPLog.printError("Mt   Test begin time :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
                ConfCtrl.ConfGetConfInfoCmd();
            }
        });

    }

    private void startAnimTimer(){
        animCount = 0;
        hand.sendEmptyMessage(100);
    }

    private void cancelAnimTimer(){
        hand.removeMessages(100);
        //做检查
        if(hand.hasMessages(100)){
            cancelAnimTimer();
        }
    }

    //开始申请权限，等待管理员审批
    public void operReqStart(){
        reqOperHintTv.setText("等待管理员审批");
        reqOperHintTv.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        //启动动画定时器
        startAnimTimer();
    }

    public void operReqEnd(boolean success){
        //移除定时器
        cancelAnimTimer();
        reqOperHintTv.setGravity(Gravity.CENTER);
        if(success){
            reqOperHintTv.setText("申请成功！");
        }else{
            reqOperHintTv.setText("申请加入协作");
        }
    }

    public void setOper(boolean isOper){
        if(isOper){
            operTure();
        }else{
            operFalse();
        }
    }

    public void operFalse(){
        insertImgBtn.setVisibility(View.GONE);
        selectImgBtn.setVisibility(View.GONE);
        paintBtn.setVisibility(View.GONE);
        eraseBtn.setVisibility(View.GONE);
        undoBtn.setVisibility(View.GONE);
        redoBtn.setVisibility(View.GONE);
        selfAdaptionBtn.setVisibility(View.GONE);
        paintColorIv.setVisibility(View.GONE);
        addPageBtn.setVisibility(View.GONE);
        prePageBtn.setEnabled(false);
        pageNumBtn.setEnabled(false);
        nextPageBtn.setEnabled(false);
        pageNumBtn.setTextColor(Color.parseColor("#4c4c4c"));

        reqOperBtn.setVisibility(View.VISIBLE);
        reqOperHintLayout.setVisibility(View.VISIBLE);
    }

    public void operTure(){
        insertImgBtn.setVisibility(View.VISIBLE);
        selectImgBtn.setVisibility(View.VISIBLE);
        paintBtn.setVisibility(View.VISIBLE);
        eraseBtn.setVisibility(View.VISIBLE);
        undoBtn.setVisibility(View.VISIBLE);
        redoBtn.setVisibility(View.VISIBLE);
        selfAdaptionBtn.setVisibility(View.VISIBLE);
        paintColorIv.setVisibility(View.VISIBLE);
        addPageBtn.setVisibility(View.VISIBLE);
        prePageBtn.setEnabled(true);
        pageNumBtn.setEnabled(true);
        nextPageBtn.setEnabled(true);
        reqOperBtn.setVisibility(View.GONE);
        reqOperHintLayout.setVisibility(View.GONE);
        pageNumBtn.setTextColor(Color.parseColor("#ffffff"));
    }

    public void setDcsConfManager(boolean mng){
//        TPLog.printError(new Exception());
        if(MtNetUtils.curEmConfMode == MtNetUtils.emConfModeAuto_Api){
            mngerBtn.setVisibility(View.GONE);
            return;
        }
        if(mng){
            mngerBtn.setVisibility(View.VISIBLE);
        }else {
            mngerBtn.setVisibility(View.GONE);
            mRemoteDcsMngerDialog.dismiss();

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(0);
        mWindow.setFocusable(false);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
        mWindow.setOnDismissListener(this);

        mRemoteDcsHintDialog = new RemoteDcsHintDialog(mContext);
        mRemoteDcsMngerDialog = new RemoteDcsMngerDialog(mContext);
        mRemoteDcsMngerDialog.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mngerBtn.setBackgroundResource(R.mipmap.req_oper_normal);
            }
        });

        mRemoteDcsQuitSwichDialog = new RemoteDcsQuitSwichDialog(mContext);
    }

    public void setBtnClick(View.OnClickListener clickListener){
        menuBtn.setOnClickListener(clickListener);
        insertImgBtn.setOnClickListener(clickListener);
        selectImgBtn.setOnClickListener(clickListener);
        paintBtn.setOnClickListener(clickListener);
        eraseBtn.setOnClickListener(clickListener);
        undoBtn.setOnClickListener(clickListener);
        redoBtn.setOnClickListener(clickListener);
        selfAdaptionBtn.setOnClickListener(clickListener);
        addPageBtn.setOnClickListener(clickListener);
        prePageBtn.setOnClickListener(clickListener);
        pageNumBtn.setOnClickListener(clickListener);
        nextPageBtn.setOnClickListener(clickListener);
        reqOperBtn.setOnClickListener(clickListener);
    }

    public void setRemoteDataConfBtnState(boolean isJoinConf){
        TPLog.printError("setRemoteDataConfBtnState-------》isJoinConf = "+isJoinConf);
        joinConfBtn.setSelect(isJoinConf);
        if(isJoinConf){
            mRemoteDcsHintDialog.dismiss();
        }else if(joinConfBtn.getVisibility() == View.VISIBLE){
            mRemoteDcsHintDialog.show(joinConfBtn);
        }
    }

    public void setRemoteDataConfBtnEnable(boolean enable){
        TPLog.printError("setRemoteDataConfBtnEnable-------》 enable= "+enable);
        if(enable){
            if(joinConfBtn.getVisibility() != View.VISIBLE) {
                joinConfBtn.setVisibility(View.VISIBLE);
//                mRemoteDcsHintDialog.show(joinConfBtn);
            }

            if(!joinConfBtn.isSelect()){
                mRemoteDcsHintDialog.show(joinConfBtn);
            }

        }else{
            if(joinConfBtn.getVisibility() != View.GONE) {
                joinConfBtn.setSelect(false);
                joinConfBtn.setVisibility(View.GONE);
                mRemoteDcsHintDialog.dismiss();
            }
        }
        joinConfBtn.invalidate();
    }

    //SP2以下两个功能去掉
    public void setConfMemberNum(int num){
//        if(confMemberNumTv!=null)
//          confMemberNumTv.setText(num+"");
    }

    public void setConfMemBerViewDisplay(boolean display){
//        if(confMemberNumLayout == null){
//            return;
//        }
//        if(display){
//            confMemberNumLayout.setVisibility(View.VISIBLE);
//        }else{
//            confMemberNumLayout.setVisibility(View.GONE);
//        }
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        mWindow = null;
        mRemoteDcsHintDialog.destory();
    }

    @Override
    public void onDismiss() {
        TPLog.printKeyStatus("onDismiss。。。");
        ((BaseActivity)mContext).onDismiss();
        if(mRemoteDcsHintDialog!=null){
            mRemoteDcsHintDialog.dismiss();
        }

        if(mRemoteDcsMngerDialog!=null){
            mRemoteDcsMngerDialog.dismiss();
        }
        if(mRemoteDcsQuitSwichDialog!=null){
            mRemoteDcsQuitSwichDialog.dismiss();
        }
//        if(joinConfBtn!=null)
//         joinConfBtn.setVisibility(View.GONE);
    }
}
