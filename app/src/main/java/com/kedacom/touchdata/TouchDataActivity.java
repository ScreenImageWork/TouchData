package com.kedacom.touchdata;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kedacom.httpserver.utils.Network;
import com.kedacom.receiver.NetworkBroadcastReceiver;
import com.kedacom.service.BootService;
import com.kedacom.storagelibrary.model.MailboxInfo;
import com.kedacom.storagelibrary.model.StorageItem;
import com.kedacom.storagelibrary.model.WifiInfo;
import com.kedacom.storagelibrary.unity.StroageManager;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.filemanager.OpenFileManager;
import com.kedacom.touchdata.mail.MailUtil;
import com.kedacom.touchdata.net.ConnectManager;
import com.kedacom.touchdata.net.SendHelper;
import com.kedacom.touchdata.net.SynFileManager;
import com.kedacom.touchdata.net.callback.NetCallback;
import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.net.entity.CoordinateChangedMsg;
import com.kedacom.touchdata.net.entity.DeleteGraphMsg;
import com.kedacom.touchdata.net.entity.GraphCoordinateChangedMsg;
import com.kedacom.touchdata.net.utils.NetError;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.helper.IHelperHolder;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.IWhiteBoardStateChanagedListener;
import com.kedacom.touchdata.whiteboard.view.WhiteBoardView;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.ArithUtil;
import com.kedacom.utils.NetworkUtil;
import com.kedacom.utils.StorageMangerJarUtils;
import com.kedacom.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2016/9/22.
 */
public class TouchDataActivity extends BaseActivity implements IWhiteBoardStateChanagedListener, PageManager.ISavePageListener,MailUtil.OnSendMailListener,NetCallback,NetworkBroadcastReceiver.OnNetworkChangeListener {

    private PageManager mPageManager;

    private StroageManager mStroageManager;//U盘管理类

    private WhiteBoardView mWb;
    private IHelperHolder mHelperHolder;

    private  RelativeLayout bottomLayout;

    private TextView wbNameTv;
    private LinearLayout closeWbBtn;
    private Button dragBtn;

    private LinearLayout penBtn;
    private ImageView subPenBtn;
    private ImageView subPenIv;
    private View penColorTag;

    private LinearLayout eraseBtn;
    private ImageView subEraseBtn;
    private ImageView subEraseIv;

    private Button undoBtn;
    private Button redoBtn;
    private Button moreBtn;

    private Button createWbBtn;
    private Button browseWbListBtn;
    private Button menuBtn;

    private RelativeLayout pageNumLayout;
    private Button preSubPageBtn;
    private Button nextSubPageBtn;
    private LinearLayout selectSubPageBtn;
    private TextView curSubPageNumTv;
    private TextView maxSupPageNumTv;
    private TextView subPageNumBiasLine;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image7.jpg";

    private SendHelper mSendHelper;

    private ConnectManager mConnectManager;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchdata);
        initView();
        initBtnClick();
        initWbPage(savedInstanceState);
    }

    private void init(){
        //检测服务是否启动，没有启动 执行启动
        Intent intent = new Intent(this, BootService.class);
        if(!Utils.isServiceRunning(this,"com.kedacom.service.BootService")){
            TPLog.printKeyStatus("BootService 未启动");
            startService(intent);
        }else{
            TPLog.printKeyStatus("BootService 已启动");
        }
        //初始化白板工具类
        WhiteBoardUtils.init(this);
        MailUtil.setOnSendMailListener(this);
        //创建页面管理器
        mPageManager = new PageManager();
        SynFileManager.getInstance().setPageManager(mPageManager);

        ConnectManager.getInstance().setCallback(this);
//        ConnectManager.getInstance().start(NetUtil.IP,NetUtil.PORT);

        mSendHelper = SendHelper.getInstance();

        //初始化网络监听
        NetworkBroadcastReceiver.registerOnNetworkChangeListener(this);

        //初始化U盘管理类
        mStroageManager = new StroageManager(this);
    }


    private void initView(){

        bottomLayout = (RelativeLayout) findViewById(R.id.bottom);

        wbNameTv = (TextView) findViewById(R.id.wbNameTv);
        closeWbBtn = (LinearLayout) findViewById(R.id.closeWbBtn);
        dragBtn = (Button) findViewById(R.id.dragBtn);

        penBtn = (LinearLayout) findViewById(R.id.penBtn);
        subPenBtn = (ImageView) findViewById(R.id.subPenBtn);
        subPenIv = (ImageView) findViewById(R.id.subPenIv);
        penColorTag = findViewById(R.id.colorTag);

        eraseBtn = (LinearLayout) findViewById(R.id.eraseBtn);
        subEraseBtn = (ImageView) findViewById(R.id.subEraseBtn);
        subEraseIv = (ImageView) findViewById(R.id.subEraseIv);

        undoBtn = (Button) findViewById(R.id.undoBtn);
        redoBtn = (Button) findViewById(R.id.redoBtn);
        moreBtn = (Button) findViewById(R.id.moreBtn);

        createWbBtn = (Button)findViewById(R.id.createWbBtn);
        browseWbListBtn = (Button)findViewById(R.id.browseWbListBtn);
        menuBtn = (Button)findViewById(R.id.menuBtn);

        preSubPageBtn = (Button) findViewById(R.id.preSubPageBtn);
        nextSubPageBtn = (Button) findViewById(R.id.nextSubPageBtn);
        selectSubPageBtn = (LinearLayout) findViewById(R.id.selectPageNumBtn);
        curSubPageNumTv = (TextView) findViewById(R.id.curPageNumTv);
        maxSupPageNumTv = (TextView) findViewById(R.id.maxPageNumTv);
        subPageNumBiasLine = (TextView) findViewById(R.id.pageNumBiasLine);
        pageNumLayout= (RelativeLayout)findViewById(R.id.pageNumLayout);

        penBtn.setClickable(true);
        eraseBtn.setClickable(true);


        mWb = (WhiteBoardView) findViewById(R.id.mWhiteBoardView);
        mWb.setIWhiteBoardStateChanagedListener(this);
    }

    private void initWbPage(Bundle savedInstanceState){
        if(savedInstanceState==null) {
            Page page = WhiteBoardUtils.createDefWbPage();
            page.addSubPage(new SubPage());
            mPageManager.addPage(page);
            page.setBackGroundColor(getCurBackgroundColor());
        }else{
            restore(savedInstanceState);
        }

        mWb.setPageManager(mPageManager);
        mHelperHolder = mWb.getHelperHolder();
        setPageManager(mPageManager);
        mPageManager.setISavePageListener(this);

        mWb.setBackgroundColor(getCurBackgroundColor());
    }

    private void initBtnClick(){
        //废弃
        closeWbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = mPageManager.getPageCount();
                if(count>=1) {
                    int curPosintion = mPageManager.getSelectPageIndex();
                    mPageManager.removePage(curPosintion);
                }else{
                    finish();
            }
            }
        });

        dragBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSelectBtn(view.getId());
                //开启拖动模式
                mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_DRAG);
            }
        });

        subPenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasDialogShowing()){
                    dismissDialog();
                    return;
                }
                resetSelectBtn(penBtn.getId());
                if(mHelperHolder.getCurOpType() == WhiteBoardUtils.GRAPH_PEN) {
//                    subPenIv.setBackgroundResource(R.mipmap.triangle_select_icon);
//                    //弹出画笔选择框
//                    showSwitchColorDialog(subPenIv);
                }else {
                    subPenIv.setBackgroundResource(R.mipmap.triangle_normal_icon);
                    mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_PEN);
                }
            }
        });

        subPenIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasDialogShowing()){
                    dismissDialog();
                    return;
                }
                mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_PEN);
                resetSelectBtn(penBtn.getId());
                subPenIv.setBackgroundResource(R.mipmap.triangle_select_icon);
                //弹出画笔选择框
                showSwitchColorDialog(subPenIv);
            }
        });

        subEraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasDialogShowing()){
                    dismissDialog();
                    return;
                }
                resetSelectBtn(eraseBtn.getId());
                if(mHelperHolder.getCurOpType() == WhiteBoardUtils.GRAPH_ERASE||mHelperHolder.getCurOpType() == WhiteBoardUtils.GRAPH_ERASE_AREA) {
                    //弹出擦除模式选择对话框
//                    showSwitchEraseDialog(subEraseIv);
//                    subEraseIv.setBackgroundResource(R.mipmap.triangle_select_icon);
                }else{
                    subEraseIv.setBackgroundResource(R.mipmap.triangle_normal_icon);
                    mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE);
                }

            }
        });

        subEraseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasDialogShowing()){
                    dismissDialog();
                    return;
                }
                mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE);
                resetSelectBtn(eraseBtn.getId());
                subEraseIv.setBackgroundResource(R.mipmap.triangle_select_icon);
                //弹出擦除模式选择对话框
                showSwitchEraseDialog(subEraseIv);
            }
        });

        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelperHolder.undo();
                //发送撤销消息
                IPage page = mPageManager.getSelectPage();
                long pageId = page.getId();
                int subPageIndex = page.getCurSubPageIndex() - 1;
                mSendHelper.sendUndoMsg(pageId, subPageIndex);
            }
        });

        redoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelperHolder.redo();
                //发送恢复消息
                IPage page = mPageManager.getSelectPage();
                long pageId = page.getId();
                int subPageIndex = page.getCurSubPageIndex() - 1;
                mSendHelper.sendRedoMsg(pageId, subPageIndex);
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreBtn.setBackgroundResource(R.mipmap.more_select_icon);
                showMoreMenuDialog(moreBtn);
            }
        });

        createWbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int normalBg = WhiteBoardUtils.curBackground;
                mWb.setBackgroundColor(normalBg);
                Page page = WhiteBoardUtils.createDefWbPage();
                page.setBackGroundColor(normalBg);
                String name = page.getName();
                setWbName(name);
                mPageManager.addPage(page);

                int count = mPageManager.getPageCount();
                if (count >= 20) {
                    //toastMsg("白板数量已达上限！");
                    createWbBtn.setBackgroundResource(R.mipmap.create_wb_unable_icon);
                    createWbBtn.setClickable(false);
                    return;
                }

                //发送创建白板消息
                mSendHelper.sendCreateNewPageMsg(page);
            }
        });

        browseWbListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                browseWbListBtn.setBackgroundResource(R.mipmap.browse_wb_select_icon);
                showPageThumbnailDialog();
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuBtn.setBackgroundResource(R.mipmap.menu_select_icon);
                menuBtn.setEnabled(false);
                showMenuDialog(view);
            }
        });

        preSubPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPageManager.previousSubPage();
                //发送翻页消息
                IPage page =  mPageManager.getSelectPage();
                long pageId = page.getId();
                int subPageIndex = page.getCurSubPageIndex();
                Image image = page.getCurSubPage().getImage();
                mSendHelper.sendChangePageMsg(pageId,subPageIndex,image);
                mSendHelper.requestImage(image,pageId,subPageIndex,((Page)page).getOwnerIndex());
            }
        });

        nextSubPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPageManager.nextSubPage();
                //发送翻页消息
                IPage page = mPageManager.getSelectPage();
                long pageId = page.getId();
                int subPageIndex = page.getCurSubPageIndex();
                Image image = page.getCurSubPage().getImage();
                mSendHelper.sendChangePageMsg(pageId, subPageIndex, image);
                mSendHelper.requestImage(image, pageId, subPageIndex, ((Page) page).getOwnerIndex());
            }
        });
        selectSubPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelectSubPageBtn(true);
                //弹出页码选择框
                showPageNumDialog(mPageManager.getSelectPage().getSubPageCount(),selectSubPageBtn);
            }
        });
    }

    private void changeSubPageEnable(boolean isEnable){
        selectSubPageBtn.setEnabled(isEnable);
        nextSubPageBtn.setEnabled(isEnable);
        preSubPageBtn.setEnabled(isEnable);
    }

    private void isSelectSubPageBtn(boolean isSelect){
        int color = getResources().getColor(R.color.pagenum_normal_textcolor);
        if(isSelect) {
            color = getResources().getColor(R.color.pagenum_select_textcolor);
        }
        curSubPageNumTv.setTextColor(color);
        subPageNumBiasLine.setTextColor(color);
        maxSupPageNumTv.setTextColor(color);
    }

    private void resetSelectBtn(int btnId){
        dragBtn.setBackgroundResource(R.mipmap.drag_normal_icon);
        subPenBtn.setBackgroundResource(R.mipmap.pen_normal_icon);
        subEraseBtn.setBackgroundResource(R.mipmap.erase_normal_icon);
        switch(btnId){
            case R.id.dragBtn:
                dragBtn.setBackgroundResource(R.mipmap.drag_select_icon);
                break;
            case R.id.penBtn:
                subPenBtn.setBackgroundResource(R.mipmap.pen_select_icon);
                break;
            case R.id.eraseBtn:
                subEraseBtn.setBackgroundResource(R.mipmap.erase_select_icon);
                break;
        }
    }

    //重置所有的弹出框弹出标识
    private void resetTriangleIv(){
        subPenIv.setBackgroundResource(R.mipmap.triangle_normal_icon);
        subEraseIv.setBackgroundResource(R.mipmap.triangle_normal_icon);
        moreBtn.setBackgroundResource(R.mipmap.more_normal_icon);
        browseWbListBtn.setBackgroundResource(R.mipmap.browse_wb_normal_icon);
        menuBtn.setBackgroundResource(R.mipmap.menu_normal_icon);
        isSelectSubPageBtn(false);

        menuBtn.setEnabled(true);
    }

    @Override
    public void onDCancelBtnEvent(DialogType dtype) {
        resetTriangleIv();
        if(needSaveProgressDialog&&curSavePage!=null&&dtype != DialogType.LOADFILE) {
            curSaveProgress += curSavePage.getSubPageCount();
            setCurProgress(curSaveProgress);
        }else if(needSaveProgressDialog&&dtype == DialogType.LOADFILE){ //取消保存
            //停止保存
            saveQueue.clear();
            if(curSavePage!=null){
                ((Page)curSavePage).stopSave();
            }
            curSavePage = null;
            if(isExit){  //如果是退出保存的话，取消保存就直接退出应用
                finish();
            }
        }else  if(dtype == DialogType.LOADFILE){ //取消打开文件
            int index = mPageManager.getSelectPageIndex();
            mPageManager.removePage(index);
        }
        if(dtype == DialogType.FILEEXIST){
            if(isCloseSave){
                isCloseSave = false;
                mPageManager.removePage(curSavePage);
            }
            if(isExit){
                if(curSaveAllType == SAVEALL_ONEBYONE){
                    if(!saveQueue.isEmpty()){
                        curSavePage = saveQueue.remove(0);
                        TouchDataActivity.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
                        FileUtils.checkDirExists(savePath);
                        saveDirName = curSavePage.getName();
                        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
                        isSaveAll = true;
                        String msg = "\"" + curSavePage.getName() + "\""+" 是否保存?";
                        showIsSaveFileDilaog(msg);
                    }else{
                        finish();
                    }
                }else if(curSaveAllType == SAVEALL_NOT_PROMPT){
                    if(!saveQueue.isEmpty()){
                        curSavePage = saveQueue.remove(0);
                        TouchDataActivity.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
                        FileUtils.checkDirExists(savePath);
                        saveDirName = curSavePage.getName();
                        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
                        isSaveAll = true;
                        onDSaveBtnEvent(saveDirName,true);
                    }else{
                        finish();
                    }
                }
            }
        }
    }

    @Override
    public void onDSureBtnEvent(DialogType dtype) {
     if(!saveQueue.isEmpty()){
            curSavePage = saveQueue.remove(0);
            TouchDataActivity.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
            FileUtils.checkDirExists(savePath);
            saveDirName = curSavePage.getName();
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            isSaveAll = true;
            if(curSaveAllType == SAVEALL_ONEBYONE){
                String msg = "\"" + curSavePage.getName() + "\""+" 是否保存?";
                showIsSaveFileDilaog(msg);
            }
        }else if(isExit){
                finish();
        }else if(isUnConnectServer&&dtype == DialogType.SURE){
            showNetworkStateDialog(false,true);
        }
    }

    @Override
    public void onDOpenDirBtnEvent(String filePath) {

    }

    @Override
    public void onDResetFileNameBtnEvent() {
       // String aliasPath = FileUtils.getAliasPath(savePath);
        //TPLog.printError("aliasPath-->"+aliasPath);
        showSaveDialog();
    }

    @Override
    public void onDReplaceFileBtnEvent() {
        String localFilePath = savePath + File.separator + saveDirName;
        FileUtils.deleteFile(localFilePath);//删掉之前的所有文件
        onDSaveBtnEvent(saveDirName,isSaveAll);
    }

    private void initSaveAll(){
        isExit = true;
        setSaveDialogSaveAllBtnIsDisplay(false);
        saveQueue.clear();
        List<IPage> list = mPageManager.getPageList();
        int count = list.size();
        int subPageCount = 0; //统计所有子页个数
        for(int i = 0;i<count;i++){
            saveQueue.add(list.get(i));
            subPageCount += list.get(i).getSubPageCount();
        }

        if(saveQueue.isEmpty())return;

        curSavePage = saveQueue.remove(0);
        savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
        FileUtils.checkDirExists(savePath);
        saveDirName = curSavePage.getName();
        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
        isSaveAll = true;

        savePageStart(subPageCount);
    }

    @Override
    public void onDExitSaveBtnEvent() {//保存全部按钮
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }
        initSaveAll();
        curSaveAllType = SAVEALL_NOT_PROMPT;
        onDSaveBtnEvent(saveDirName, isSaveAll);
    }

    @Override
    public void onDExitNotSaveBtnEvent() {

        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }

        if(needSaveProgressDialog){//有保存进度对话框，关闭先
            dismissDialog(DialogType.LOADFILE);
        }
        initSaveAll();
        curSaveAllType = SAVEALL_ONEBYONE;
        String msg = "\""+ curSavePage.getName() +"\"" + " 是否保存？";
        showIsSaveFileDilaog(msg);
    }

    @Override
    public void onDExitCancelBtnEvent() {
        finish();
    }

    @Override
    public void onDPenSizeBtnEvent(int size) {
        mHelperHolder.setPaintStrokeWidth(size);
    }

    @Override
    public void onDPenColorBtnEvent(int color) {
        mHelperHolder.setPaintColor(color);
        penColorTag.setBackgroundColor(color);
    }

    @Override
    public void onDEraseBtnEvent() {
        mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE);
    }

    @Override
    public void onDAreaEraseBtnEvent() {
        mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE_AREA);
    }

    @Override
    public void onDClearScreenBtnEvent() {
        mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_PEN);
        resetSelectBtn(penBtn.getId());
        mHelperHolder.clearScreen();
        //发送清屏数据
        mSendHelper.sendClearScreenMsg();
}

    @Override
    public float onDZoomOutBtnEvent() {
        float curScale = mPageManager.getSelectPage().getCurScale();
        //curScale = curScale + 0.1f; //java浮点运算就是坑
        curScale = ArithUtil.add(curScale, 0.1f);
        if(curScale>3.0f){
            curScale = 3.0f;
        }
         mHelperHolder.scale(curScale);
//        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendScaleMsg(pageId, curScale * 100f);
        return curScale;
    }

    @Override
    public float onDZoomInBtnEvent() {
        float curScale = mPageManager.getSelectPage().getCurScale();
        if(curScale>3.0f){
            curScale = 3.0f;
        }
        //curScale = curScale - 0.1f;
        curScale = ArithUtil.sub(curScale,0.1f);
        if(curScale<0.5f){
            curScale = 0.5f;
        }
        mHelperHolder.scale(curScale);
//        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendScaleMsg(pageId, curScale * 100f);
        return curScale;
    }

    @Override
    public void onDRotateLeftBtnEvent() {
        int curAngle = mPageManager.getSelectPage().getCurAngle();
        curAngle = curAngle - 90;
        mHelperHolder.rotate(curAngle, true);
        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        //mSendHelper.sendRotateMsg((int)pageId,curAngle,true);
        mSendHelper.sendLeftOrRightRotateMsg(true, pageId);
    }

    @Override
    public void onDRotateRightBtnEvent() {
        int curAngle = mPageManager.getSelectPage().getCurAngle();
        curAngle = curAngle + 90;
        mHelperHolder.rotate(curAngle, true);
        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendLeftOrRightRotateMsg(false, pageId);
    }

    @Override
    public void onDHeightSelfBtnEvent() {//需要进行图片居中计算
        mHelperHolder.heightSelf();
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendScrollMsg(page.getOffsetX(),page.getOffsetY(),page.getId(),mPageManager.getCurPageSelectSubPageIndex(),true);
        mSendHelper.sendScaleMsg(page.getId(),page.getCurScale());
    }

    @Override
    public void onDWidthSelfBtnEvent() {
        mHelperHolder.widthSelf();
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendScrollMsg(page.getOffsetX(),page.getOffsetY(),page.getId(),mPageManager.getCurPageSelectSubPageIndex(),true);
        mSendHelper.sendScaleMsg(page.getId(), page.getCurScale() * 100f);
    }

    @Override
    public void onDOneToOneBtnEvent() {
        mHelperHolder.scale(1.0f);
        //发送消息
        long id = mPageManager.getSelectPage().getId();
        mSendHelper.sendScaleMsg(id, 1.0f * 100f);
    }

    @Override
    public void onDCloseWbBtnEvent() {
        onDCloseWbBtnEvent(mPageManager.getSelectPage());
    }

    @Override
    public void onDCloseWbBtnEvent(IPage page) {
        boolean isNeedSave = page.isNeedSave();
        TPLog.printKeyStatus("关闭白板，是否需要保存:" + isNeedSave);
        if(!isNeedSave) {
//            if(mPageManager.getPageCount()==1){
//                dismissDialog();
//            }
            mPageManager.removePage(page);
            //发送关闭白板消息
            mSendHelper.sendCloseWb(page.getId(),mPageManager.getSelectPage().getId());
        }else{
            dismissDialog();
            isCloseSave = true;
            curSavePage = page;
            savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            saveDirName = page.getName();
            String msg = "\""+page.getName() + "\" 是否保存?";
            showCloseSelectSaveWbDiloag(msg);
        }
    }

    @Override
    public void onDIsCloseWbSureBtnEvent(IPage page) {

    }

    @Override
    public void onDCloseAllWbBtnEvent() {

    }

    @Override
    public void onDSwitchDeviceSaveBtnEvent(String path) {
        if(!new File(path).exists()){
            showIsSureDialog("保存操作失败，外接存储设备已拔出！");
            return;
        }
        if(!path.endsWith("/")){
            path = path + File.separator;
        }
        TPLog.printKeyStatus("select storage device path:"+path);
        setSaveDialogSaveAllBtnIsDisplay(true);
        curSavePage = mPageManager.getSelectPage();
        saveDirName = curSavePage.getName();
        if(!path.contains(FileUtils.SDPATH)) {
            savePath = path + FileUtils.USBFlashDriveSaveDir + FileUtils.getCurWhiteBoardSaveDir();
        }else{
            savePath = path + FileUtils.getCurWhiteBoardSaveDir();
        }
        FileUtils.checkDirExists(savePath);
        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
        showSaveDialog();
    }

    @Override
    public void onDSwitchBackGroundColor(int color) {
        mHelperHolder.setBackgroundColor(color);
        mWb.setBackgroundColor(color);
        mPageManager.getSelectPage().setBackGroundColor(color);
    }

    @Override
    public void onDSelectSubPageNum(int pageNum) {
        mPageManager.selectCurPageSubPage(pageNum);
        //发送消息
        IPage page = mPageManager.getSelectPage();
        long pageId = page.getId();
        int subPageIndex = page.getCurSubPageIndex();
        mSendHelper.sendChangePageMsg(pageId, subPageIndex, page.getCurSubPage().getImage());
    }

    @Override
    public void onDSelectPage(IPage page) {
        mPageManager.selectPage(page.getId());
        int subPageIndex = page.getCurSubPageIndex();
        mSendHelper.sendChangePageMsg(page.getId(), subPageIndex, page.getCurSubPage().getImage());
    }

    @Override
    public void onDMenuOpenFileBtnEvent() {  //打开文件选择器
//        String devices[] = {"本机文件"};
//        FileEntity fe = FileUtils.findFiles(FileUtils.SDPATH);
//        showSwitchFileDialog(devices,fe);


       //showSwitchFileDialog();

        int count = mPageManager.getPageCount();

        if(count>=20){
            toastMsg("当前白板页数量创建已达到上限，文件打开失败！");
        }else{
            startSwitchFileActivity();
        }
    }

    //onDSwitchDeviceSaveBtnEvent
    @Override
    public void onDMenuSaveBtnEvent() {
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }
        //检查是否有U盘存在
        //true
        List<StorageItem>  mStorageItems = mStroageManager.getStorageItems();
        if(mStorageItems!=null&&!mStorageItems.isEmpty()) {
            showSwitchSaveDeviceDialog(mStorageItems);
        }else {
            //false
            //U盘不存在直接跳转到保存界面
            setSaveDialogSaveAllBtnIsDisplay(true);
            curSavePage = mPageManager.getSelectPage();
            saveDirName = curSavePage.getName();
            savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
            FileUtils.checkDirExists(savePath);
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            showSaveDialog();
        }
    }

    @Override
    public void onDMenuSendMailBtnEvent() {
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }

        if(!NetworkUtil.getNetworkState(this)){
            showNetworkSettingDialog();
        }else {
            //sendMail = true;
            showSendMailDialog();
        }
    }

    @Override
    public void onDMenuScanQRBtnEvent() {
        if(NetworkUtil.getNetworkState(this)) {
            TPLog.printKeyStatus("show qr code dialog and save current all page");
            qRDownLoad = true;
            showQRCodeDialog();
            FileUtils.deleteFile(FileUtils.RUNNING_CACHE);
            mPageManager.saveAllPage(FileUtils.RUNNING_CACHE);
        }else{
            TPLog.printKeyStatus(" qr code dialog display failed ,network unusable, network setting dialog showing");
            showNetworkSettingDialog();
        }
    }

    @Override
    public void onDMenuChangeBgBtnEvent() {
        showSwitchBackGroundDialog();
    }

    @Override
    public void onDMenuExitBtnEvent() {
        int count = mPageManager.getPageCount();

        boolean needSave = false;
        for(int i = 0;i<count;i++){
            IPage page = mPageManager.getPage(i);
            if(page.isNeedSave()){
                needSave = true;
                break;
            }
        }

        showQuitIsSaveFileDialog();
    }

    //保存文件按钮触发事件  需要在此方法检查文件是否存在，进行后续处理
    @Override
    public void onDSaveBtnEvent(String dirName,boolean issa) {
        isSaveAll = issa;
        saveDirName = dirName;
        final String curSavePath = savePath;
        String filePath = curSavePath + File.separator + saveDirName;

        TPLog.printKeyStatus("文件保存路径："+filePath);

        boolean isExists = FileUtils.checkFileExists(filePath);
        if(isExists){//文件已经存在弹出文件存在对话框
            if(needSaveProgressDialog){//有保存进度对话框，关闭先
                dismissDialog(DialogType.LOADFILE);
            }
            String hintText = "\""+saveDirName+"\"文件已经存在，是否替换？";
            showWhiteBoardNameDialog();
            showFileExistDialog(hintText);
        }else{
            if(isSaveAll) {
                //如果保存所有的类型不是逐个保存或者全部保存那么就是手动保存
                if(curSaveAllType != SAVEALL_ONEBYONE&&curSaveAllType != SAVEALL_NOT_PROMPT){
                    savePageStart(curSavePage.getSubPageCount());
                }
                mPageManager.savePage(curSavePage,curSavePath,saveDirName);
            }else{
                mPageManager.saveSelectSubPage(curSavePage,curSavePath,saveDirName);
            }
        }
    }

    @Override
    public void onDSaveDilaogCancelBtnEvent() {
        if(!saveQueue.isEmpty()){
            curSavePage = saveQueue.remove(0);
            saveDirName =  curSavePage.getName();
            savePath = FileUtils.SAVE_WRITEBOARD_DIR+FileUtils.getCurWhiteBoardSaveDir();
            FileUtils.checkDirExists(savePath);
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            if(curSaveAllType == SAVEALL_ONEBYONE) {
                String msg = "\""+curSavePage.getName() + "\""+" 是否保存？";
                showIsSaveFileDilaog(msg);
            }else{
                onDSaveBtnEvent(saveDirName,true);
            }
        }else{
            if(isCloseSave){
                mPageManager.removePage(curSavePage);
                isCloseSave = false;
            }
        }
    }

    @Override
    public void onDSaveAllPageBtnEvent(String path, String dirName) {

    }

    @Override
    public void onDIsSaveDialogSurBtnEvent() {
        dismissDialog(DialogType.LOADFILE);
        showSaveDialog();
    }

    @Override
    public void onDIsSaveDialogCancelBtnEvent() {
        if(curSaveAllType == SAVEALL_ONEBYONE&&!saveQueue.isEmpty()){
            curSaveProgress += curSavePage.getSubPageCount();
            IPage page  = saveQueue.remove(0);
            curSavePage = page;
            savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            saveDirName = page.getName();
            String msg = "\""+page.getName() + "\" 是否保存?";
            dismissDialog(DialogType.LOADFILE);
            showIsSaveFileDilaog(msg);
        }else if(!isExit&&isCloseSave){
            mPageManager.removePage(curSavePage);
            //发送关闭白板消息
            mSendHelper.sendCloseWb(curSavePage.getId(),mPageManager.getSelectPage().getId());
            isCloseSave = false;
        }else{
            finish();
        }
    }

    @Override
    public void onDCloseSelectSaveWbDiloagCancelBtnEvent() {
        if(isCloseSave){
            isCloseSave = false;
        }
    }

    //发送邮件按钮事件
    @Override
    public void onDSendMailBtnEvent(String title, String[] mails) {
        dismissDialog();
        showIsSureDialog("邮件发送中......");
        //showLoadDialog("正在发送邮件。。。");

        sendMail = true;
        mailTitle  = title;
        recMails = mails;
        mailImageCacheDir = FileUtils.RUNNING_CACHE;
        FileUtils.deleteFile(mailImageCacheDir);
        mPageManager.saveAllPage(mailImageCacheDir);
    }

    @Override
    public void onDSwitchFile(String filePath) {
        TPLog.printKeyStatus("onDSwitchFile  选择文件--->"+filePath);
        File file = new File(filePath);
        Page page = WhiteBoardUtils.createDefWbPage();
        String name = file.getName();
        int index = name.indexOf(".");
        if(index>0) {
            name = name.substring(0, index);
        }
        page.setName(name);
        SubPage subPage = new SubPage();
        page.addSubPage(subPage);
        page.setAnoymous(false);
        mPageManager.addPage(page);  //创建一个新的白板
        mSendHelper.sendCreateNewPageMsg(page);  //发送新建白板信息
        //showLoadDialog("文件打开中...");
        //showLoadFileDilaog(0,0,true);
        //打开文档
        OpenFileManager.openOrChangeFile(TouchDataActivity.this,page.getId()+"",filePath,false,new OpenFileListener());
    }

    @Override
    public void onDToMailConfigSetting() {
        StorageMangerJarUtils.toEmailSetting(this);
        //toastMsg("去邮箱设置。。。。。。。。。。。。。");
    }

    @Override
    public void onDilaogDismiss() {
        resetTriangleIv();
    }

    @Override
    public void onDRejectApplyChairman() {

    }

    @Override
    public void onDAgreeApplyChairman(ApplyChairNtf acn) {

    }


    @Override
    public void onScaleChanged(final float curScale) {
        int tempScale = (int)(curScale*100f);
        toastMsg(tempScale + "%");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurScale(curScale);
            }
        });
        mHelperHolder.scale(curScale);
        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendScaleMsg(pageId, tempScale);
    }

    @Override
    public void onScaleChangedFromGesture(float scaleFactor, float focusX, float focusY) {
        final float curScale = mPageManager.getSelectPage().getCurScale();
        final int tempScale = (int)(curScale*100f);
        toastMsg(tempScale+"%");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurScale(curScale);
            }
        });
        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendGestureZoomMsg(pageId, scaleFactor, (int) focusX, (int) focusY);
    }

    @Override
    public void onUpdateScaleUI(final float curScale) {
        mHelperHolder.updateGestureCurScale(curScale);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurScale(curScale);
            }
        });
    }

    @Override
    public void onRotateChanged(int angle, boolean isFinish) {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendRotateMsg((int) page.getId(), angle, isFinish);
    }

    @Override
    public void onUndo() {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendUndoMsg(page.getId(), page.getCurSubPageIndex() - 1);
    }

    @Override
    public void onRedo() {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendRedoMsg(page.getId(), page.getCurSubPageIndex() - 1);
    }

    @Override
    public void onUndoEnable(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    undoBtn.setBackgroundResource(R.drawable.undo_btn_style);
                } else {
                    undoBtn.setBackgroundResource(R.mipmap.undo_unable_icon);
                }
                undoBtn.setEnabled(enable);
            }
        });
    }

    @Override
    public void onRedoEnable(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enable) {
                    redoBtn.setBackgroundResource(R.drawable.redo_btn_style);
                } else {
                    redoBtn.setBackgroundResource(R.mipmap.redo_unable_icon);
                }
                redoBtn.setEnabled(enable);
            }
        });
    }

    @Override
    public void onGraphUpdate(Graph graph) {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendGraphMsg(graph, (int) page.getId(), page.getCurSubPageIndex() - 1);
    }

    @Override
    public void onTranslateChanged(float ox, float oy, boolean isFinish) {
        IPage page = mPageManager.getSelectPage();
        TPLog.printError("onTranslateChanged---------------->ox=" + ox + ",oy=" + oy);
        mSendHelper.sendScrollMsg(ox, oy, (int) page.getId(), page.getCurSubPageIndex() - 1, isFinish);
    }

    @Override
    public void onCoordinateChanged() {

    }

    @Override
    public void onPageChanged(final int pageIndex,final int curSubPageIndex,final  int subPageNum, final boolean nextSubPageEnable, final boolean lastSubPageEnable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

        if(subPageNum>1){
            pageNumLayout.setVisibility(View.VISIBLE);
        }else{
            pageNumLayout.setVisibility(View.GONE);
        }

        setSelectPageIndex(curSubPageIndex-1);

        IPage page = mPageManager.getPage(pageIndex);
        String name =  page.getName();
        mWb.setBackgroundColor(page.getBackGroundColor());
        setWbName(name);

        curSubPageNumTv.setText(curSubPageIndex+"");
        maxSupPageNumTv.setText(subPageNum+"");

        if(subPageNum<2) {
            changeSubPageEnable(false);
        }else{
            TPLog.printError("lastSubPageEnable--->"+lastSubPageEnable+",nextSubPageEnable--->"+nextSubPageEnable);
            if(!lastSubPageEnable){
                selectSubPageBtn.setEnabled(true);
                nextSubPageBtn.setEnabled(true);
                preSubPageBtn.setEnabled(false);
            }else if(!nextSubPageEnable){
                preSubPageBtn.setEnabled(true);
                selectSubPageBtn.setEnabled(true);
                nextSubPageBtn.setEnabled(false);
            }else{
                changeSubPageEnable(true);
            }
        }
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IPage page = mPageManager.getPage(pageIndex);
                setCurScale(page.getCurScale());
            }
        });
    }

    @Override
    public void cancelSelecImageTimer() {

    }

    @Override
    public void onDelSelectImg(int imgId) {

    }

    @Override
    public void onPaintDrawDown() {

    }

    @Override
    public void onServerConnected() {
       // toastMsg("onServerConnected");
    }

    @Override
    public void onLoginServerSuccess() {  //登录成功后反馈，一般为断线后登录
        //toastMsg("onLoginServerSuccess");
//        if(WhiteBoardUtils.isIntoMeeting&&WhiteBoardUtils.isAPPShowing){

        if(isUnConnectServer) {
            fullHorizontalScreenToastMsg("数据会议服务器连接成功！");
        }
        isUnConnectServer = false;
        if(WhiteBoardUtils.isAPPShowing){
            ConnectManager.getInstance().getSender().joinOrCreateMeeting("","",true);
        }
        dismissDialog(DialogType.NETWORKSTATE);
        dismissDialog(DialogType.SURE);
    }

    @Override
    public void onRecMeetingName(String meetingName) {

    }

    @Override
    public void onServerDisconnected() {
       TPLog.printKeyStatus("datameeting server disconnected");
        if(!isUnConnectServer) {
            isUnConnectServer = true;
            if(!NetworkUtil.getNetworkState(this)) {
                showNetworkStateDialog(true,false);
                showNetworkSettingDialog();
            }else{
                showNetworkStateDialog(false,false);
                showIsSureDialog("服务器异常,无法进行数据协作,正在重新连接......");
            }
            }
    }

    @Override
    public void onServerConnectException(Exception e) {
        //Toast.makeText(this,"onServerConnectException",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecCreateMeeting(long resultCode) {

    }

    @Override
    public void onRecJoinMeeting(long resultCode) {
        TPLog.printError("receive join meeting result:" + resultCode);
        if(resultCode== NetError.ESUCCESS) {
            NetUtil.isJoinMeeting = true;
        }
    }

    @Override
    public void onRecServerCurBufferSzie(long size) {
        Image image = mPageManager.getSelectPage().getCurSubPage().getImage();
        SynFileManager.getInstance().sendImage(image);
    }

    @Override
    public void onRecServerReqSyn(long synReqId) {
        mSendHelper.sendSynData(mPageManager, synReqId);
    }

    @Override
    public void onRecSynFailed() {

    }

    @Override
    public void onRecSynData(long curPageId,List<Page> pageList) {
        mPageManager.getPageList().clear();
        mPageManager.setPageList(pageList);
        mPageManager.selectPage(curPageId);
        Image image = mPageManager.getSelectPage().getCurSubPage().getImage();
        if(image!=null){
            SynFileManager.getInstance().requestFileInfo(image);
        }
    }

    @Override
    public void onRecGraphData(Graph graph) {
        if(graph==null)return;
        mHelperHolder.requestDrawGraph(graph);
    }

    @Override
    public void onRecImageData(SubPage subpage) {
        mPageManager.getSelectPage().addSubPage(subpage);
     }

    @Override
    public void onRecZoomData(float zoom) {
        TPLog.printKeyStatus("receive zoom："+zoom);
        mHelperHolder.scale(zoom / 100f);
        setCurScale(zoom / 100f);
    }

    @Override
    public void onRecGestureZoomData(float scaleFactor, int focusX, int focusY) {
        TPLog.printError("onRecGestureZoomData------->" + scaleFactor);
        float zoom = mPageManager.getSelectPage().getCurScale();
        zoom = zoom*scaleFactor;
        mHelperHolder.postScale(scaleFactor, focusX, focusY);
        setCurScale(zoom);
    }

    @Override
    public void onRecScrollData(android.graphics.Point scrollData) {
        TPLog.printKeyStatus("receive scroll：x:"+scrollData.x+",y:"+scrollData.y);

        Page page = (Page)mPageManager.getSelectPage();
        float ox = scrollData.x - page.getOffsetX();
        float oy = scrollData.y - page.getOffsetY();

        //为了兼容PC，因此接收到的偏移量，这边需要进行特殊处理
        mHelperHolder.postTranslate(ox, oy);
    }

    @Override
    public void onRecClearScreen() {
        mHelperHolder.clearScreen();
    }

    @Override
    public void onRecCreateWbData(Page page) {
        mPageManager.addPage(page);
    }

    @Override
    public void onRecDelWbData(long delWbId,long nextWbId) {
        mPageManager.removePage(delWbId);
        mPageManager.selectPage(nextWbId);
    }

    @Override
    public void onRecDelAllWbData(long newTabId) {

    }

    @Override
    public void onRecChangePage(long wbId, long subPageIndex) {
        subPageIndex = subPageIndex +1;
        TPLog.printKeyStatus("onRecChangePage---->wbId="+wbId+",subPageIndex="+subPageIndex);
        IPage page = mPageManager.getSelectPage();
        if(page.getId()!=wbId) {
            mPageManager.selectPage(wbId);
        }else {
            mPageManager.selectCurPageSubPage((int)(subPageIndex));
        }

        Image image = mPageManager.getCurSelectSubPage().getImage();
        if(image!=null&&!image.isDlSuccess()){
            mSendHelper.requestImage(image,mPageManager.getSelectPage().getId(),mPageManager.getCurPageSelectSubPageIndex()-1,((Page)mPageManager.getSelectPage()).getOwnerIndex());
        }
        mPageManager.notifyPageChanged();
    }

    @Override
    public void onRecServerReqChangePage(long wbId, long subPageIndex) { //服务器请求翻页，其实是请求图片
        IPage page = mPageManager.getSelectPage();
        Image image = mPageManager.getSelectPage().getCurSubPage().getImage();
        mSendHelper.requestServerRecFlow();
       // mSendHelper.requestImage(image,page.getId(),page.getCurSubPageIndex()-1,((Page)page).getOwnerIndex());
    }

    @Override
    public void onRecImageDownloaded(long imageId) {
        TPLog.printKeyStatus("图片下载完成 Id："+imageId);
        mPageManager.notifyPageChanged();
    }

    @Override
    public void onRecRedoData(long wbId, long subPageIndex) {
        mHelperHolder.redo();
    }

    @Override
    public void onRecUndoData(long wbId, long subPageIndex) {
        mHelperHolder.undo();
    }

    @Override
    public void onRecLeftRotate() {
        int angle = mPageManager.getSelectPage().getCurAngle();
        angle = angle - 90;
        mHelperHolder.rotate(angle,true);
    }

    @Override
    public void onRecRightRotate() {
        int angle = mPageManager.getSelectPage().getCurAngle();
        angle = angle + 90;
        mHelperHolder.rotate(angle,true);
    }

    @Override
    public void onRecRotate(long angle, long isFinish) {
        boolean boo = false;
        if(isFinish==0){
            boo = true;
        }else{
            boo = false;
        }
        mHelperHolder.rotate((int)angle,boo);
    }

    @Override
    public void onRecInsertImg(ImageGraph img) {

    }

    @Override
    public void onRecCoordinateChanged(CoordinateChangedMsg msg) {

    }

    @Override
    public void onRecSelectGrpahCoordinateChanged(ArrayList<GraphCoordinateChangedMsg> list) {

    }

    @Override
    public void onRecDeleteGraph(DeleteGraphMsg msg) {

    }

    @Override
    public void onRecConfMemberListUpdate(int num) {

    }

    @Override
    public void onRecServerConnectNumNtf(int num) {

    }

    @Override
    public void onDToSettingNetwork() {
//        toastMsg("打开网络设置!");
        try {
            StorageMangerJarUtils.toNetWorkSetting(this);
        }catch(Exception e){
            TPLog.printError("PackageUtils", "not found activity " + StorageMangerJarUtils.PACKAGE_SYSTEM_SETTINGS + "/" + StorageMangerJarUtils.APP_SYSTEM_SETTINGS);
            //toastMsg("IMIX设置程序未安装，该功能无法使用！");
        }
    }

    @Override
    public void onDRlsDcsMnger() {

    }

    @Override
    public void onDDataConfModeChange(int mode) {

    }

    @Override
    public void onDToFileManager(String path) {

    }

    /**********************网络状态监听*****************************/

    @Override
    public void onNetworkUsable() {
        TPLog.printKeyStatus("current network usable");
        if(!WhiteBoardUtils.isAPPShowing){
            return;
        }
        ConnectManager.getInstance().startReconnect();
        //隐藏网络连接对话框
        dismissDialog(DialogType.SETTINGNETWORK);
        showNetworkStateDialog(false,true);
    }

    @Override
    public void onNetworkUnusable() {
        TPLog.printKeyStatus("current network unusable");
        if(!WhiteBoardUtils.isAPPShowing){
            TPLog.printKeyStatus("white board did not showing");
            return;
        }
        ConnectManager.getInstance().stopReconnect();
        //打开网络异常对话框
        showNetworkSettingDialog();
        showNetworkStateDialog(true, false);
    }
    /**********************保存白板************************/

    @Override
    public void savePageStart(int saveCount) {
        TPLog.printKeyStatus("need save page count: "+saveCount);
        if(saveCount>2){
            if(!sendMail&&!qRDownLoad){//发送邮件和二维码下载保存白板时不需要进行进度显示
                //showLoadFileDilaog(saveCount,0,false);
                needSaveProgressDialog = true;
                curSaveProgress = 0;
            }
        }
    }

    @Override
    public void saveProgress(int progress) {
        TPLog.printKeyStatus("current save page progress:"+progress);
        if(sendMail||qRDownLoad){
            return;
        }
        if(needSaveProgressDialog) {
            curSaveProgress ++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loadFileDiloagShowAgain();
                    setCurProgress(curSaveProgress);
                }
            });
        }
    }

    @Override
    public void savePageSuccess(final String savePath) {
        TPLog.printKeyStatus("save all page success！");
        if(!sendMail&&!qRDownLoad) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (saveQueue.isEmpty() || curSaveAllType == SAVEALL_ONEBYONE) {
                        String locPath = FileUtils.getAliasPath(savePath,mStroageManager);
                        String strs[] = locPath.split("/");
                        String path = "";
                        for (int i = 0; i < strs.length; i++) {
                            if (i == (strs.length - 1)) break;
                            path = path + strs[i] + "/";
                        }
                        String msg = "已保存至：" + path;
                        TPLog.printKeyStatus("page save path:"+path);
                        dismissDialog(DialogType.LOADFILE);
                        dismissDialog();
                        showIsSureDialog(msg);

                        if(isCloseSave){
                            mPageManager.removePage(curSavePage);
                            //发送关闭白板消息
                            mSendHelper.sendCloseWb(curSavePage.getId(),mPageManager.getSelectPage().getId());
                            isCloseSave = false;
                        }
                    } else {
                        curSavePage = saveQueue.remove(0);
                        TouchDataActivity.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
                        FileUtils.checkDirExists(savePath);
                        saveDirName = curSavePage.getName();
                        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
                        isSaveAll = true;
                        if (curSaveAllType == SAVEALL_NOT_PROMPT) {
                            onDSaveBtnEvent(saveDirName, isSaveAll);
                        }
                    }
                }
            });
        }else if(sendMail){ //发送邮件
            TPLog.printKeyStatus("send mail start...");
            List<String> filePaths = new ArrayList<String>();
            FileUtils.findAllChildFile(mailImageCacheDir,filePaths);
            TPLog.printKeyStatus("add "+filePaths.size()+" files to mail");
            String images[] = new String[filePaths.size()];
            filePaths.toArray(images);
            MailUtil.sendImageMail(recMails,mailTitle,images);
            sendMail = false;
        }else if(qRDownLoad){
//            List<String> filePaths = new ArrayList<String>();
//            FileUtils.findAllChildFile(FileUtils.RUNNING_CACHE,filePaths);
            String title = FileUtils.getCurWhiteBoardSaveDir();
            String subtitle = mPageManager.getPageCount()+"个白板,共"+mPageManager.getSubPageCount()+"页";
            setQRCodeCacheFileDir(title,subtitle,FileUtils.RUNNING_CACHE);
            qRDownLoad = false;
        }
    }

    @Override
    public void savePageFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TPLog.printKeyStatus("save page failed");
                String msg = "文件保存失败！";
                dismissDialog(DialogType.LOADFILE);
                dismissDialog();
                showIsSureDialog(msg);
            }
        });
    }

    @Override
    public void onPageCountChanged(int count) {
        createWbBtn.setBackgroundResource(R.drawable.create_wb_btn_style);
        createWbBtn.setClickable(true);
    }

    /************************邮件发送******************************/

    @Override
    public void onSendMailSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissLoadDialog();//现在可以不要，不过留着也不会报错 ，留着先
                String msg = "邮件发送成功！";
                TPLog.printKeyStatus("send mail success!");
                showIsSureDialog(msg);
            }
        });
    }

    @Override
    public void onSendMailFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissLoadDialog();
                String msg = "邮件发送失败！";
                showIsSureDialog(msg);
            }
        });
    }

    @Override
    public void onSendMailUnknownHost() {//这个暂时不处理
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                dismissLoadDialog();
//                String msg = "邮件发送失败！";
//                showIsSureDialog(msg);
//            }
//        });
    }

    @Override
    public void onConnectMailServerFailed() {

    }

    @Override
    public void onSendMailAuthenticationFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissLoadDialog();
                dismissDialog();
                showToMailConfigSettingDialog();
            }
        });
    }

    /************************打开文件*****************************/

    class OpenFileListener implements OpenFileManager.OnFileToImageListener{
        private int totalCount;
        @Override
        public void onPageCount(final int count, final String checkCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    totalCount = count;
                    TPLog.printKeyStatus("开始打开文件，文件总页数："+count+",checkCode:"+checkCode);
                    String pageId = mPageManager.getSelectPage().getId()+"";
                    if(!pageId.equals(checkCode))
                        return;
                    setProgressMax(count);
                }
            });
        }

        @Override
        public void onProgress(final int progress, final String checkCode) {
            TPLog.printKeyStatus("打开文件当前进度："+progress+"/"+totalCount);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String pageId = mPageManager.getSelectPage().getId() + "";
                    if (!pageId.equals(checkCode))
                        return;
                    setCurProgress(progress);
                }});
        }

        @Override
        public void onFialed(String errorMsg,String path,final String checkCode) {
            TPLog.printKeyStatus("文件打开失败！");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String pageId = mPageManager.getSelectPage().getId() + "";
                    if (!pageId.equals(checkCode))
                        return;
//                    if (!hasDialogShowing()) {
//                        showLoadFileDilaog(1, 0,true);
//                    }
                    showLoadFileFailedText();
                }});
        }

        @Override
        public void onComplete(List<String> files, String fileName, String checkCode) {
            TPLog.printKeyStatus("打开文件成功，fileName="+fileName);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                }
            });

            String pageId = mPageManager.getSelectPage().getId()+"";
            if(!pageId.equals(checkCode))
                return;
            IPage page = mPageManager.getSelectPage();
            int count = files.size();

            //1.发送文档打开完成命令
            mSendHelper.sendOpenOfficeComplete(page.getId());

            //2.发送文档总页数
            mSendHelper.sendSubpageCountMsg(mPageManager.getSelectPage().getId(), count);

           for(int i = 0;i<files.size();i++){

               Image image = WhiteBoardUtils.createImage(files.get(i),true);
               image.setSubpageIndex(i);
               SubPage subPage = new SubPage(image);
               subPage.setIndex(i);
               page.addSubPage(subPage);

               //3.发送文件列表
               SynFileManager.getInstance().sendImageInfo(image);

              //4.如果是第一张图的话，就发送文件信息和文件内容到服务器
               if (i == 0) {
                   SynFileManager.getInstance().sendImage(image);
               }
           }
            mPageManager.notifyPageChanged();

            //新需求，打开文件完毕后，修改画笔颜色为红色
           // if(mHelperHolder.getPaintColor() == WhiteBoardUtils.colors[0]) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDPenColorBtnEvent(WhiteBoardUtils.colors[1]);
                    }
                });
            //}

            //新需求打开文件默认宽度自适应  去掉了
           // onDWidthSelfBtnEvent();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        TPLog.printError("TouchDataActivity---------------->onResume");
        ConnectManager.getInstance().setCallback(this);
        WhiteBoardUtils.isAPPShowing = true;
        ConnectManager.getInstance().getSender().joinOrCreateMeeting("", "", true); // 每次App启动都会执行一次加入会议操作
        intent = new Intent();
        intent.setAction("com.kedacom.touchdata");
        intent.putExtra("isShow", true);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        TPLog.printError("TouchDataActivtiy    ------------onStop--------------->");
        //ConnectManager.getInstance().setCallback(null);
        dismissWhiteBoardNameDialog();
        WhiteBoardUtils.isAPPShowing = false;
        NetUtil.isJoinMeeting = false;
        mSendHelper.quitMeeting();
       //finish();
        intent.putExtra("isShow",false);
        sendBroadcast(intent);
    }


    @Override
    protected void onDestroy() {
        TPLog.printError("TouchDataActivtiy -> onDestroy");
        ConnectManager.getInstance().setCallback(null);
        super.onDestroy();
        NetworkBroadcastReceiver.unRegisterOnNetworkChangeListener(this);
        //mHelperHolder.onDestory();
        //WhiteBoardUtils.resetCurPageNum();
       // android.os.Process.killProcess( android.os.Process.myPid());
        mWb.destroy();
        if(mHelperHolder!=null)
        mHelperHolder.onDestory();
        if(mPageManager!=null)
        mPageManager.onDestroy();
        WhiteBoardUtils.reset();
    }


    private void restore(Bundle saveState) {
        if (saveState == null) {
            return;
        }
        if (!saveState.containsKey("PAGE")) {
            return;
        }

        //还原Page页
        Parcelable parcelabe[] = saveState.getParcelableArray("PAGE");

        if (parcelabe == null) {
            return;
        }

        List<Page> pages = new ArrayList<Page>();

        for (int i = 0; i < parcelabe.length; i++) {
            pages.add((Page) parcelabe[i]);
        }

        int curPageIndex = saveState.getInt("SelectPageIndex");

        mPageManager.setPageList(pages);
        mPageManager.selectPage(curPageIndex);

        Page page = (Page) mPageManager.getSelectPage();

        //还原基础数据
        mHelperHolder.translate(page.getOffsetX(), page.getOffsetY());
        mHelperHolder.scale(page.getCurScale());
        mHelperHolder.rotate(page.getCurAngle(), false);

        //还原画板面板数据
        int color = saveState.getInt("Color");
        mHelperHolder.setPaintColor(color);

        float width = saveState.getFloat("StrokeWidth");
        TPLog.printError("2StrokeWidth------------->"+width);
        mHelperHolder.setPaintStrokeWidth(width);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /*
        TPLog.printKeyStatus("onSaveInstanceState---->start save data");
        List<IPage> list = mPageManager.getPageList();
        if(list==null)return;
        int count = list.size();

        Page[] pages = new Page[count];

        for (int i = 0; i < count; i++) {
            IPage page = list.get(i);
            pages[i] = (Page) page;
        }
        outState.putParcelableArray("PAGE", pages);
        outState.putInt("SelectPageIndex", mPageManager.getSelectPageIndex());
        outState.putInt("Color", mHelperHolder.getPaintColor());
        outState.putFloat("StrokeWidth", mHelperHolder.getPaintStrokeWidth());
        outState.setClassLoader(getClass().getClassLoader());

        TPLog.printKeyStatus("onSaveInstanceState---->暂存数据完成！");

        */

        super.onSaveInstanceState(outState);
    }

    private static final String ACTION_SWTICH_FILE_ACTIVITY = "com.kedacom.fileexplorer.FileRetriever";
    private  void startSwitchFileActivity(){
        try {
            Intent intent = new Intent(ACTION_SWTICH_FILE_ACTIVITY);
            startActivityForResult(intent, 1);
        }catch(Exception e){
            toastMsg("文件管理未安装，该功能无法使用！");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String returnedData = data.getStringExtra("file_retriever_path");
                    if (returnedData!=null&&!returnedData.trim().isEmpty()) {
                        onDSwitchFile(returnedData);
                    }
                }
                break;
            default:
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 19){
            Intent intent1 = new Intent();
            intent1.setAction("android.intent.action.MAIN");
            intent1.addCategory("android.intent.category.HOME");
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("keycode" + keyCode);
            int pageCount = mPageManager.getPageCount();
            int selectPageIndex = mPageManager.getSelectPageIndex();
        if (keyCode == 21){   //向上翻页键
            System.out.println("pageCount"+pageCount);
            if (selectPageIndex < pageCount-1 ) {
                prePage(selectPageIndex);
            }else if (selectPageIndex == pageCount-1){
                selectPageIndex = -1;
                prePage(selectPageIndex);
            }
        }else if (keyCode == 22){ //向下翻页键
            if (selectPageIndex > 0 ) {
                nextPage(selectPageIndex);
            }else if (selectPageIndex == 0){
                selectPageIndex = pageCount + 1;
                nextPage(selectPageIndex);
            }
        }else if (keyCode == 20){
            if(!hasDialogShowing()){
            mHelperHolder.setOpType(WhiteBoardUtils.GRAPH_PEN);
            resetSelectBtn(penBtn.getId());
            subPenIv.setBackgroundResource(R.mipmap.triangle_select_icon);
            //弹出画笔选择框
            showSwitchColorDialog(subPenIv);

            }
        }
        return true;
    }

    private void nextPage(int selectPageIndex) {
        selectPageIndex = selectPageIndex - 1;
        mPageManager.selectPage(selectPageIndex);
        Page page = (Page) mPageManager.getSelectPage();
        long id = page.getId();
        System.out.println("subPageIndex" + selectPageIndex);
        mSendHelper.sendChangePageMsg(id, selectPageIndex, page.getCurSubPage().getImage());
    }

    private void prePage(int selectPageIndex) {
        selectPageIndex = selectPageIndex + 1;
        mPageManager.selectPage(selectPageIndex);
        Page page = (Page) mPageManager.getSelectPage();
        long id = page.getId();
        System.out.println("subPageIndex" + selectPageIndex);
        mSendHelper.sendChangePageMsg(id, selectPageIndex, page.getCurSubPage().getImage());
    }
}