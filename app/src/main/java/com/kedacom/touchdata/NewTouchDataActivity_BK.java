package com.kedacom.touchdata;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;

import com.kedacom.receiver.NetworkBroadcastReceiver;
import com.kedacom.service.BootService;
import com.kedacom.storagelibrary.model.StorageItem;
import com.kedacom.storagelibrary.unity.AppUtil;
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
import com.kedacom.touchdata.whiteboard.dialog.PaintRefreshDialog;
import com.kedacom.touchdata.whiteboard.dialog.ToolsBarDialog;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.helper.IHelperHolder;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.DateUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.DisplayTouchView;
import com.kedacom.touchdata.whiteboard.view.IWhiteBoardStateChanagedListener;
import com.kedacom.touchdata.whiteboard.view.WhiteBoardView;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.ArithUtil;
import com.kedacom.utils.NetworkUtil;
import com.kedacom.utils.StorageMangerJarUtils;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2017/6/19.
 * 
 */
public class NewTouchDataActivity_BK extends BaseActivity implements IWhiteBoardStateChanagedListener, PageManager.ISavePageListener,MailUtil.OnSendMailListener,NetCallback,NetworkBroadcastReceiver.OnNetworkChangeListener {

    private PageManager mPageManager;

    private StroageManager mStroageManager;//U盘管理类

    private WhiteBoardView mWb;

    private DisplayTouchView mDisplayTouchView;

    private IHelperHolder mHelperHolder;

    private SendHelper mSendHelper;

    private int curEraseMode;

    private ToolsBarDialog mToolsBar;

//    private View paintRefreshView;

    private PaintRefreshDialog mPaintRefreshDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TPLog.printError("->onCreate");
        initUtils();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newtouchdata);
        initView();
        initWbPage();
    }

    //初始化各种使用到的工具
    private void initUtils(){
        //初始化白板工具类
        WhiteBoardUtils.init(this);
        //设置邮件发送监听器
        MailUtil.setOnSendMailListener(this);
        //创建白板页面管理器
        mPageManager = new PageManager();
        //初始化文件管理类  后续所有的图片同步都是基于该类进行处理
        SynFileManager.getInstance().setPageManager(mPageManager);
        //初始化数据通信管理类，该类主要负责本地白板内容的发送
        mSendHelper = SendHelper.getInstance();
        //初始化网络监听
        NetworkBroadcastReceiver.registerOnNetworkChangeListener(this);
        //初始化U盘管理类，用于U盘名称和路径的读取
        mStroageManager = new StroageManager(this);
    }

    //界面初始化
    private void initView(){
        //白板控件
        mWb = (WhiteBoardView) findViewById(R.id.mWhiteBoardView);
        //设置白板状态改变监听器
        mWb.setIWhiteBoardStateChanagedListener(this);
        //获取白板代理类，白板的所有功能都由该类代理
        mHelperHolder = mWb.getHelperHolder();
        //触控显示控件   B2版本新增
        mDisplayTouchView = (DisplayTouchView) findViewById(R.id.displayTouchView);
        //所有的触控操作都是由白板代理类分发处理的
        mHelperHolder.setDisplayTouchView(mDisplayTouchView);
        //获取底部工具栏管理类实例
        mToolsBar = getToolsBarDialog();
        //设置底部工具栏监听
        setToolsBarBtnClickListener(bottomBarBtnClickListener);

//        paintRefreshView = findViewById(R.id.paintRefreshView);
        mPaintRefreshDialog = new PaintRefreshDialog(this);
    }

    //初始化白板
    private void initWbPage(){
        //创建默认白板
        Page page = WhiteBoardUtils.createDefWbPage();
        //白板相当于是一个容器，默认里面是没有纸张的，需要填充纸张
        page.addSubPage(new SubPage());
        //保存白板页
        mPageManager.addPage(page);
        //设置白板背景颜色
        page.setBackGroundColor(getCurBackgroundColor());
        //将白板设置管理器设置给白板控件，这里白板管理类就相当于是一个Adapter
        mWb.setPageManager(mPageManager);
        //将白板管理器设置给其他需要用到的界面，目前只有一个白板预览界面
        setPageManager(mPageManager);
        //设置白板保存监听器
        mPageManager.setISavePageListener(this);
        //设置白板背景色，这块是后来添加了网格背景后，便于管理，因此进行了封装，设置背景
        WhiteBoardUtils.setWbBackground(mWb,getCurBackgroundColor());
    }

   //设置白板是否可以进行页面切换
    private void switchPageEnable(boolean isEnable){
        mToolsBar.prePageBtn.setEnabled(isEnable);
        mToolsBar.nextPageBtn.setEnabled(isEnable);
    }

    //重置所有的弹出框弹出标识
    private void resetBtnState(){
        mToolsBar.menuBtn.setBackgroundResource(R.mipmap.new_menu_btn_normal_icon);
        mToolsBar.menuBtn.setEnabled(true);

        mToolsBar.pageNumBtn.setBackgroundResource(R.mipmap.new_page_num_normal_icon);
        mToolsBar.pageNumBtn.setEnabled(true);
    }

    //Btn选择，部分按钮之间选择是互斥的，这个函数只是改变UI显示而已
    private void switchBtn(int id){
        boolean imgIsEmpty = mPageManager.getCurSelectSubPage().getImageGraphList().isEmpty();
        if(!imgIsEmpty){
            mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.new_select_img_normal_icon);
        }else{
            mToolsBar.selectImgBtn.setEnabled(false);
            mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.select_img_enable_icon);
        }

        mToolsBar.paintBtn.setBackgroundResource(R.mipmap.new_paint_normal_icon);
        if(curEraseMode == WhiteBoardUtils.OP_ERASE_AREA){
            mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_area_normal_icon);
        }else{
            mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_normal_icon);
        }

        switch(id){
            case R.id.selectImgBtn:
                mToolsBar.selectImgBtn.setEnabled(true);
                mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.new_select_img_select_icon);
                break;
            case R.id.paintBtn:
                mToolsBar.paintBtn.setBackgroundResource(R.mipmap.new_paint_select_icon);
                break;
            case R.id.eraseBtn:
                if(curEraseMode == WhiteBoardUtils.OP_ERASE_AREA){
                    mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_area_select_icon);
                }else{
                    mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_select_icon);
                }
                break;
        }

    }

    //所有的对话框的取消按钮事件
    @Override
    public void onDCancelBtnEvent(DialogType dtype) {
        //重置所有的弹出框弹出标识
        resetBtnState();
        //保存进度设置，已经废弃，该需求已经变更
        if(needSaveProgressDialog&&curSavePage!=null&&dtype != DialogType.LOADFILE) {
            curSaveProgress += curSavePage.getSubPageCount();
            setCurProgress(curSaveProgress);
        }else//之前是保存所有的时候，进度对话框上的取消按钮，取消保存，已经废弃，需求已变更
        if(needSaveProgressDialog&&dtype == DialogType.LOADFILE){
            //清空保存队列
            mPageManager.stopSave();

            if(isExit){
                isExit = false;
            }
        }else  if(dtype == DialogType.LOADFILE){ //取消打开文件，该需求已经移除，该功能废弃
            int index = mPageManager.getSelectPageIndex();
            mPageManager.removePage(index);
        }else if(dtype == DialogType.SWITCHDEVICE ){
            if(isExit){
                isExit = false;
            }
        }

        if(dtype == DialogType.FILEEXIST){//保存时，文件已经存在
            if(isCloseSave){ //如果是关闭白板保存，出现保存文件已存在时，点击取消，那救直接删关闭需要关闭的白板
                isCloseSave = false;
                mPageManager.removePage(curSavePage);
            }
            if(isExit){//退出保存时，出现文件已存在
                isExit = false;
            }
        }
    }

    //所有对话框的确认按钮事件
    @Override
    public void onDSureBtnEvent(DialogType dtype) {
        if(!saveQueue.isEmpty()){//saveQueue不为空，那么就说明是保存所有功能
            curSavePage = saveQueue.remove(0);
            NewTouchDataActivity_BK.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
            FileUtils.checkDirExists(savePath);
            saveDirName = curSavePage.getName();
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
            isSaveAll = true;
            if(curSaveAllType == SAVEALL_ONEBYONE){//如果是逐个保存
                String msg = "\"" + curSavePage.getName() + "\""+" 是否保存?";
                showIsSaveFileDilaog(msg);
            }
        }else if(isExit){
            finish();
        }else if(isUnConnectServer&&dtype == DialogType.SURE){//如果是网络链接异常，而对话框是确认对话框
            showNetworkStateDialog(true,true);
        }
    }

    @Override
    public void onDOpenDirBtnEvent(String filePath) {

    }

    //保存文件名重复对话框，重命名按钮点击事件
    @Override
    public void onDResetFileNameBtnEvent() {
        showSaveDialog();
    }

    //保存文件名重复对话框，替换文件按钮事件
    @Override
    public void onDReplaceFileBtnEvent() {
//        String localFilePath = savePath + File.separator + saveDirName+".png";
        TPLog.printError("选择替换文件。。。");
        TPLog.printError("删除文件夹："+savePath + File.separator + saveDirName);
        FileUtils.deleteFile(savePath + File.separator + saveDirName);//删掉之前的所有文件
        TPLog.printError("启动保存。。。");
        onDSaveBtnEvent(saveDirName,isSaveAll);
    }

    /*
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
    */
   //退出保存按钮事件
    @Override
    public void onDExitSaveBtnEvent() {
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }

        isExit = true;

        List<StorageItem>  mStorageItems = mStroageManager.getStorageItems();
        if(mStorageItems!=null&&!mStorageItems.isEmpty()) {//新规格2017.07.24，如果有U盘存在，就弹出存储位置选择对话框
            showSwitchSaveDeviceDialog(mStorageItems);
        }else {
            dismissDialog();
//            showSaveProgressDialog();

            savePath = FileUtils.SAVE_WRITEBOARD_DIR;
            saveDirName = WhiteBoardUtils.getNewSaveFileDir(savePath);
            FileUtils.checkDirExists(savePath);
            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
//            savePath = savePath + saveDirName;
//            FileUtils.checkDirExists(savePath);
//            mPageManager.saveAllPage(savePath);
            showSaveDialog();
        }
    }

    //退出提示保存对话框，不保存按钮事件
    @Override
    public void onDExitNotSaveBtnEvent() {
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }

        finish();
//        if(needSaveProgressDialog){//有保存进度对话框，关闭先
//            dismissDialog(DialogType.LOADFILE);
//        }
//        initSaveAll();
//        curSaveAllType = SAVEALL_ONEBYONE;
//        String msg = "\""+ curSavePage.getName() +"\"" + " 是否保存？";
//        showIsSaveFileDilaog(msg);
    }

    //退出提示保存对话框，取消退出按钮事件
    @Override
    public void onDExitCancelBtnEvent() {
        //finish();
        dismissDialog();
    }

    //画笔设置对话框，画笔粗细选择事件
    @Override
    public void onDPenSizeBtnEvent(int size) {
        mHelperHolder.setPaintStrokeWidth(size);
    }

    //画笔设置，画笔颜色选择事件
    @Override
    public void onDPenColorBtnEvent(int color) {
        TPLog.printKeyStatus("设置画笔颜色："+color);
        mHelperHolder.setPaintColor(color);
        mToolsBar.paintColorIv.setBackgroundColor(color);
    }

    //擦除对话框，选择默认擦除
    @Override
    public void onDEraseBtnEvent() {
        curEraseMode = WhiteBoardUtils.OP_ERASE;
        mHelperHolder.setOpType(WhiteBoardUtils.OP_ERASE);
        mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_select_icon);
        dismissDialog();
    }
    //擦除对话框，选择区域擦除
    @Override
    public void onDAreaEraseBtnEvent() {
        curEraseMode = WhiteBoardUtils.OP_ERASE_AREA;
        mHelperHolder.setOpType(WhiteBoardUtils.OP_ERASE_AREA);
        mToolsBar.eraseBtn.setBackgroundResource(R.mipmap.new_erase_area_select_icon);
        dismissDialog();
    }
   //擦除对话框，选择清屏
    @Override
    public void onDClearScreenBtnEvent() {
        mHelperHolder.clearScreen();
        //清屏完了后，选择画笔
        bottomBarBtnClickListener.onClick(mToolsBar.paintBtn);
        //发送清屏数据
        mSendHelper.sendClearScreenMsg();
    }

    //白板操作对话框，放大按钮事件 ，已经废弃
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
    //白板操作对话框，缩小按钮事件 ，已经废弃
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
    //白板操作对话框，左旋转按钮事件 ，已经废弃
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
    //白板操作对话框，右旋转按钮事件 ，已经废弃
    @Override
    public void onDRotateRightBtnEvent() {
        int curAngle = mPageManager.getSelectPage().getCurAngle();
        curAngle = curAngle + 90;
        mHelperHolder.rotate(curAngle, true);
        //发送消息
        long pageId = mPageManager.getSelectPage().getId();
        mSendHelper.sendLeftOrRightRotateMsg(false, pageId);
    }

    //白板操作对话框，高度自适应按钮事件 ，已经废弃
    @Override
    public void onDHeightSelfBtnEvent() {//需要进行图片居中计算
        mHelperHolder.heightSelf();
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendScrollMsg(page.getOffsetX(),page.getOffsetY(),page.getId(),mPageManager.getCurPageSelectSubPageIndex(),true);
        mSendHelper.sendScaleMsg(page.getId(), page.getCurScale());
    }

    //白板操作对话框，宽度自适应按钮事件 ，已经废弃
    @Override
    public void onDWidthSelfBtnEvent() {
        mHelperHolder.widthSelf();
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendScrollMsg(page.getOffsetX(),page.getOffsetY(),page.getId(),mPageManager.getCurPageSelectSubPageIndex(),true);
        mSendHelper.sendScaleMsg(page.getId(), page.getCurScale() * 100f);
    }

    //白板操作对话框，一比一应按钮事件
    @Override
    public void onDOneToOneBtnEvent() {
        mHelperHolder.scale(1.0f);
        //发送消息
        long id = mPageManager.getSelectPage().getId();
        mSendHelper.sendScaleMsg(id, 1.0f * 100f);
    }

    //删除白板
    @Override
    public void onDCloseWbBtnEvent() {
        onDCloseWbBtnEvent(mPageManager.getSelectPage());
    }

    /*
    //删除白板
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
            isCloseSave = false;
        }else{
//            isCloseSave = true;
            dismissDialog();
//            curSavePage = page;
//            savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
//            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
//            saveDirName = page.getName()+"_"+DateUtils.getCurTime("HHmmss");
//            String msg = "\""+saveDirName + "\" 是否保存?";
//            showCloseSelectSaveWbDiloag(msg);
            showIsCloseWbDialog(page);
        }
    }
    */

    //删除白板
    @Override
    public void onDCloseWbBtnEvent(IPage page) {
        boolean isNeedSave = page.isNeedSave();
        TPLog.printKeyStatus("关闭白板，是否需要保存:" + isNeedSave);
        mPageManager.removePage(page);
        //发送关闭白板消息
        mSendHelper.sendCloseWb(page.getId(),mPageManager.getSelectPage().getId());
        isCloseSave = false;
    }

    @Override
    public void onDIsCloseWbSureBtnEvent(IPage page) {
        mPageManager.removePage(page);
        //发送关闭白板消息
        mSendHelper.sendCloseWb(page.getId(),mPageManager.getSelectPage().getId());
        isCloseSave = false;
    }

    @Override
    public void onDCloseAllWbBtnEvent() {
        int count = mPageManager.getPageCount();
        for(int i = 0;i<count;i++){
            mPageManager.removePage(0);
        }
        long newTabId = mPageManager.getSelectPage().getId();
        if(mSendHelper!=null)
            mSendHelper.sendCloseAllWb(newTabId,0);
    }

    //选择存储设备对话框，确认按钮事件
    @Override
    public void onDSwitchDeviceSaveBtnEvent(String path) {
        if(!new File(path).exists()){
            showIsSureDialog("保存操作失败，外接存储设备已拔出！");
            return;
        }
//        if(!path.endsWith("/")){
//            path = path + File.separator;
//        }
        TPLog.printKeyStatus("选择的存储设备路径 path:"+path);
        if (!path.contains(FileUtils.SDPATH)) {
            savePath = path +File.separator+ FileUtils.USBFlashDriveSaveDir;
        } else {
            savePath = path;
        }

        saveDirName = WhiteBoardUtils.getNewSaveFileDir(savePath);
        aliasSavePath = FileUtils.getAliasPath(savePath, mStroageManager);

        FileUtils.checkDirExists(savePath);
        TPLog.printKeyStatus("白板保存路径:"+savePath);

        if(isCloseSave){
//            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
//            saveDirName = curSavePage.getName()+"_"+DateUtils.getCurTime("HHmmss");
            showSaveDialog();
        }else if(isExit){
//            mPageManager.saveAllPage(savePath);
            showSaveDialog();
        } else{
            setSaveDialogSaveAllBtnIsDisplay(true);
            curSavePage = mPageManager.getSelectPage();
//            saveDirName = curSavePage.getName() + "_" + DateUtils.getCurTime("HHmmss");
            showSaveDialog();
        }
    }

    //白板背景选择
    @Override
    public void onDSwitchBackGroundColor(int color) {

        WhiteBoardUtils.setWbBackground(mWb,color);

        mHelperHolder.setBackgroundColor(color);
        mPageManager.getSelectPage().setBackGroundColor(color);
    }

    //白板子页翻页，已经废弃
    @Override
    public void onDSelectSubPageNum(int pageNum) {
        mPageManager.selectCurPageSubPage(pageNum);
        //发送消息
        IPage page = mPageManager.getSelectPage();
        long pageId = page.getId();
        int subPageIndex = page.getCurSubPageIndex();
        mSendHelper.sendChangePageMsg(pageId, subPageIndex, page.getCurSubPage().getImage());
    }

    //白板翻页
    @Override
    public void onDSelectPage(IPage page) {
        mPageManager.selectPage(page.getId());
        int subPageIndex = page.getCurSubPageIndex();
        mSendHelper.sendChangePageMsg(page.getId(), subPageIndex, page.getCurSubPage().getImage());
    }

    //打开文件选择器
    @Override
    public void onDMenuOpenFileBtnEvent() {
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
    //菜单对话框，保存按钮事件
    @Override
    public void onDMenuSaveBtnEvent() {
        if(sendMail){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }

        //2018.02.06新策略，检测是否已经保存过了，
        // 1、如果已经保存，则弹出替换，重命名和取消弹框
        // 2、如果没有保存，则取一个新的名字，进行保存
        //??
           //新的白板
        if(mPageManager.isGraphesEmpty()){
            toastMsg("白板内容为空，不需要保存！");
            return;
        }else //有内容，但白板已经保存过了，还未进行新的操作，使用之前的文件夹名称
            if(!mPageManager.isGraphesEmpty()&&!mPageManager.isNeedSave()){
                TPLog.printError("白板已经保存过了，而且没有进行过操作，显示文件已存在对话框。。。");
//                savePath = FileUtils.SAVE_WRITEBOARD_DIR;
//                saveDirName = WhiteBoardUtils.getPreSaveFileDir(savePath);
                TPLog.printError("上次白板保存路径："+savePath);
                TPLog.printError("上次白板保存文件夹名称："+saveDirName);
                showFileExistDialog("文件 \""+saveDirName+"\" 已存在，是否替换？");
//                saveDirName = WhiteBoardUtils.getPreSaveFileDir();
        }else//有内容，而且白板已经做过修改，取一个新的文件夹名称
            if((!mPageManager.isGraphesEmpty()&&mPageManager.isNeedSave())||(mPageManager.isGraphesEmpty() && mPageManager.isNeedSave())) {
                TPLog.printError("白板已经进行了操作，尚未保存，准备启动正常保存流程。。。");
                savePath = FileUtils.SAVE_WRITEBOARD_DIR;
                saveDirName = WhiteBoardUtils.getNewSaveFileDir(savePath);
                FileUtils.checkDirExists(savePath);
                aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
                TPLog.printError("当前白板保存文件夹名称："+saveDirName);
                //检查是否有U盘存在
                List<StorageItem>  mStorageItems = mStroageManager.getStorageItems();
                if(mStorageItems!=null&&!mStorageItems.isEmpty()) {
                    TPLog.printError("存在外接存储设备，显示存储设备选择对话框。。。");
                    showSwitchSaveDeviceDialog(mStorageItems);
                }else{
                    TPLog.printError("外接存储设备不存在，开始显示保存对话框。。。");
                    showSaveDialog();
                }
        }else {//白板是空的，但是也需要保存，有一种情况可以触发，就是清屏操作，去一个新的文件夹名称...放上去了

        }

//        if(mStorageItems!=null&&!mStorageItems.isEmpty()) {
////            for(int i = 0;i<mStorageItems.size();i++){
////                TPLog.printError("name="+mStorageItems.get(i).name+","+"path="+mStorageItems.get(i).path);
////            }
//            showSwitchSaveDeviceDialog(mStorageItems);
//        }else {
//            //false
//            //U盘不存在直接跳转到保存界面
//            setSaveDialogSaveAllBtnIsDisplay(true);
//            curSavePage = mPageManager.getSelectPage();
//            saveDirName = curSavePage.getName()+"_"+DateUtils.getCurTime("HHmmss");
//            savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
//            FileUtils.checkDirExists(savePath);
//            aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
//            showSaveDialog();
//        }
    }

    //菜单对话框 邮件分享按钮事件
    @Override
    public void onDMenuSendMailBtnEvent() {
        if(sendMail||qRDownLoad){
            toastMsg("设备繁忙，请稍后再试！");
            return;
        }
        dismissDialog();

        List<IPage> list = mPageManager.getPageList();

        boolean sendMailEnable = false;
        for(int i = 0;i<list.size();i++){
            if(list.get(i).hasGraphs()){
                sendMailEnable = true;
                break;
            }
        }

        if(!sendMailEnable){
            showIsSureDialog("白板内容不存在，不需要进行邮件发送！");
            return;
        }

        if(!NetworkUtil.getNetworkState(this)){
            showNetworkSettingDialog();
        }else {
            if(MailUtil.isConfigMail()){
                showSendMailDialog();
            }else{
                showToMailConfigSettingDialog("邮箱服务信息未配置，邮件分享功能暂时无法使用！");
            }
            //sendMail = true;

        }
    }

    //菜单对话框，扫码分享按钮事件
    @Override
    public void onDMenuScanQRBtnEvent() {
        dismissDialog();
        TPLog.printError("onDMenuScanQRBtnEvent begin...");
        if(NetworkUtil.getNetworkState(this)) {
            FileUtils.deleteFile(FileUtils.RUNNING_CACHE);
            qRDownLoad = true;
            boolean boo = mPageManager.saveAllPageToCache(FileUtils.RUNNING_CACHE);
            if(!boo){
                TPLog.printError("whiteboard empty...");
                qRDownLoad = false;
                showIsSureDialog("白板内容不存在，无法进行分享！");
                TPLog.printKeyStatus("白板内容不存在，无法进行分享！");
                return;
            }
            TPLog.printError("display qrcode dialog...");
            showQRCodeDialog();
        }else{
            TPLog.printError(" qr code dialog display failed ,network unusable, network setting dialog showing");
            showNetworkSettingDialog();
        }
        TPLog.printError("onDMenuScanQRBtnEvent end...");
    }

    //菜单对话框 背景选择按钮事件
    @Override
    public void onDMenuChangeBgBtnEvent() {
        showSwitchBackGroundDialog();
    }

    //菜单对话框，退出按钮事件
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

        if(!needSave){
            showIsSureExitDialog();
            return;
        }

        showQuitIsSaveFileDialog();
    }

    //保存文件按钮触发事件  需要在此方法检查文件是否存在，进行后续处理
    @Override
    public void onDSaveBtnEvent(String dirName,boolean issa) {
        isSaveAll = issa;
        saveDirName = dirName;
        /*
        isSaveAll = issa;
        saveDirName = dirName;
        final String curSavePath = savePath;

        String filePath = curSavePath + File.separator + saveDirName+".png";
        //String filePath = curSavePath;

        TPLog.printKeyStatus("文件保存路径："+curSavePath);
        TPLog.printKeyStatus("文件保存路径->filePath："+filePath);

        boolean isExists = FileUtils.checkFileExists(filePath);
        if(isExists){//文件已经存在弹出文件存在对话框
            if(needSaveProgressDialog){//有保存进度对话框，关闭先
                dismissDialog(DialogType.LOADFILE);
            }
            String hintText = "\""+saveDirName+"\"文件已经存在，是否替换？";
            showWhiteBoardNameDialog();
            showFileExistDialog(hintText);
        }else{
//            if(isSaveAll) {
//                //如果保存所有的类型不是逐个保存或者全部保存那么就是手动保存
//                if(curSaveAllType != SAVEALL_ONEBYONE&&curSaveAllType != SAVEALL_NOT_PROMPT){
//                    savePageStart(curSavePage.getSubPageCount());
//                }
//                mPageManager.savePage(curSavePage,curSavePath,saveDirName);
//            }else{
//                mPageManager.saveSelectSubPage(curSavePage,curSavePath,saveDirName);
//            }
            showSaveProgressDialog();
            mPageManager.saveSelectSubPage(curSavePage,curSavePath,saveDirName);
        }
         */
        onDSaveAllPageBtnEvent(savePath,dirName);
    }

    //保存所有白板 20180206 添加
    @Override
    public void onDSaveAllPageBtnEvent(String path, String dirName) {
        File file = new File(path,dirName);
        if(file.exists()){
            TPLog.printError("文件已经存在，显示文件已经存在对话框。。。");
            showFileExistDialog("文件 \""+dirName +"\" 已存在，是否替换？");
            return;
        }
        TPLog.printError("文件不存在，开始保存。。。");
        String savePath = file.getAbsolutePath();
        TPLog.printError("保存所有白板到文件夹："+savePath);
        FileUtils.checkDirExists(savePath);
        mPageManager.saveAllPage(savePath);
    }

    //保存对话框取消按钮事件
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

        if(isExit){
            isExit = false;
        }
    }

    //是否保存对话框，确认按钮事件
    @Override
    public void onDIsSaveDialogSurBtnEvent() {
        dismissDialog(DialogType.LOADFILE);
        List<StorageItem>  mStorageItems = mStroageManager.getStorageItems();
        if(mStorageItems!=null&&!mStorageItems.isEmpty()) {//新规格2017.07.24
            showSwitchSaveDeviceDialog(mStorageItems);
        }else {
            TPLog.printError("开始显示保存对话框。。。。。");
            showSaveDialog();
        }
    }

    //是否保存对话框，取消按钮事件
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
            curSavePage.destory();
            isCloseSave = false;
        }else{
            finish();
        }
    }

    //关闭提示保存对话框，取消按钮事件
    @Override
    public void onDCloseSelectSaveWbDiloagCancelBtnEvent() {
        if(isCloseSave){
            isCloseSave = false;
        }
    }

    //发送邮件对话框，发送邮件按钮事件
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
        mPageManager.saveAllPageToCache(mailImageCacheDir);
    }

    @Override
    //2017.06.20修改，移除文件打开功能，改为图片插入
    public void onDSwitchFile(String filePath) {

        boolean isImg = FileUtils.checkFileIsImg(filePath);

        if(!isImg){
            toastMsg("打开文件失败，文件格式不正确");
            return;
        }

        if(!FileUtils.checkImgCanOpen(filePath)){
            toastMsg("打开文件失败，该文件无法打开！");
            return;
        }

        onDPenColorBtnEvent(WhiteBoardUtils.colors[1]);

        ImageGraph img = new ImageGraph(filePath);
        img.load();

        int width = img.getWidth();
        int height = img.getHeight();

        int x = (int) ((WhiteBoardUtils.whiteBoardWidth - width)/2f);
        int y = (int)((WhiteBoardUtils.screenHeight - height)/2f);

        img.setX(x);
        img.setY(y);

        IPage page = mPageManager.getSelectPage();

        img.setTabId(page.getId());
        img.setPageIndex(page.getCurSubPageIndex() -1);
        Matrix matrix = page.getMatrix();
        img.changeCoordinate(matrix,page.getCurScale());

        page.getCurSubPage().addGraph(img);

        startSelectImageTimer();

        mHelperHolder.setOpType(WhiteBoardUtils.OP_SELECT_IMG);
        mHelperHolder.selectImage(img);
        switchBtn(R.id.selectImgBtn);

        mPageManager.notifyPageChanged();

        //发送同步信息
        mSendHelper.sendInsertImgMsg(img); // 发送图片图元基本信息
        Image image = WhiteBoardUtils.createImage(filePath,true);
        image.setId(img.getId());
        image.setSubpageIndex(0);
        SynFileManager.getInstance().sendImageInfo(image); //发送图片信息
        SynFileManager.getInstance().sendImage(image); //发送图片文件
    }

    //邮件发送失败提示对话框，去邮箱设置
    @Override
    public void onDToMailConfigSetting() {
        StorageMangerJarUtils.toEmailSetting(this);
    }

    //所有的对话框 退出事件
    @Override
    public void onDilaogDismiss() {
        resetBtnState();
    }

    @Override
    public void onDRejectApplyChairman() {

    }

    @Override
    public void onDAgreeApplyChairman(ApplyChairNtf acn) {

    }


    //白板缩放改变事件，已经废弃
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

    //白板缩放改变来自与手势缩放
    @Override
    public void onScaleChangedFromGesture(float scaleFactor, float focusX, float focusY) {
        final float curScale = mPageManager.getSelectPage().getCurScale();
        //因为是float值因此不能精确的计算到300%或者50%因此这里做了一个容差机制，
        // 大于等于297的值当成300，小于等于52的值当成50来显示
        int tempScale = (int)(curScale*100);
        toastMsg(tempScale+"%");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setCurScale(curScale);
            }
        });
        //发送消息  2017.07.05修改不单独发送缩放数据了，统一改为发送Matrix数据
//        long pageId = mPageManager.getSelectPage().getId();
//        mSendHelper.sendGestureZoomMsg(pageId, scaleFactor, (int) focusX, (int) focusY);
    }

    //更新缩放UI
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

    //白板旋转角度改变，已经废弃
    @Override
    public void onRotateChanged(int angle, boolean isFinish) {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendRotateMsg((int) page.getId(), angle, isFinish);
    }

    //撤销按钮事件
    @Override
    public void onUndo() {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendUndoMsg(page.getId(), page.getCurSubPageIndex() - 1);
    }

    //恢复按钮事件
    @Override
    public void onRedo() {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendRedoMsg(page.getId(), page.getCurSubPageIndex() - 1);
    }

    //是否可撤销状态
    @Override
    public void onUndoEnable(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToolsBar.undoBtn.setEnabled(enable);
            }
        });
    }

    //是否可恢复状态
    @Override
    public void onRedoEnable(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToolsBar.redoBtn.setEnabled(enable);
            }
        });
    }
    //图元更新，每绘制完一个图元后，都会调用该函数，主要是为了同步给其他客户端
    @Override
    public void onGraphUpdate(Graph graph) {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendGraphMsg(graph, (int) page.getId(), page.getCurSubPageIndex() - 1);
        handler.sendEmptyMessage(102);
    }

    //白板显示坐标改变  已经废弃
    @Override
    public void onTranslateChanged(float ox, float oy, boolean isFinish) {
//2017.07.05修改，不再单独发送拖动消息了，之后的统一发送Matrix数据
//        IPage page = mPageManager.getSelectPage();
//        TPLog.printError("onTranslateChanged---------------->ox=" + ox + ",oy=" + oy);
//        mSendHelper.sendScrollMsg(ox, oy, (int) page.getId(), page.getCurSubPageIndex() - 1, isFinish);
    }

    //白板坐标系改变，主要是为了同步缩放和平移手势
    @Override
    public void onCoordinateChanged() {
        IPage page = mPageManager.getSelectPage();
        mSendHelper.sendCoordinateChanged(page.getMatrix(), page.getId(), page.getCurSubPageIndex() - 1);
    }

    @Override
    public void onPageCountChanged(int count) {
        if(count<20){
            mToolsBar.addPageBtn.setEnabled(true);
        }else{
            mToolsBar.addPageBtn.setEnabled(false);
        }
    }


    //白板切换，每次切换完白板都会执行到这里，主要是对切换后的白板进行状态初始化
    @Override
    public void onPageChanged(final int pageIndex,final int curSubPageIndex,final  int subPageNum, final boolean nextSubPageEnable, final boolean lastSubPageEnable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                setSelectPageIndex(curSubPageIndex-1);

                IPage page = mPageManager.getPage(pageIndex);
                String name =  page.getName();

                WhiteBoardUtils.setWbBackground(mWb,page.getBackGroundColor());

                setWbName(name);

                int pageCount = mPageManager.getPageCount();

                mToolsBar.pageNumBtn.setText((pageIndex+1)+"/"+pageCount);

                ISubPage mSubPage = page.getCurSubPage();

                if(mSubPage.getImageGraphList().isEmpty()){
                    setSelectImgBtnEnable(false);
                }else{
                    setSelectImgBtnEnable(true);
                }

                if(pageCount<2) {
                    switchPageEnable(false);
                }else{
                    if(!mPageManager.hasPrePage()){
                        mToolsBar.prePageBtn.setEnabled(false);
                    }else{
                        mToolsBar.prePageBtn.setEnabled(true);
                    }

                    if(!mPageManager.hasNextPage()){
                        mToolsBar.nextPageBtn.setEnabled(false);
                    }else{
                        mToolsBar.nextPageBtn.setEnabled(true);
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

    //链接到白板服务器
    @Override
    public void onServerConnected() {
        // toastMsg("onServerConnected");
//        if(closeTimer!=null) {
//            TPLog.printError("数据协作已经重新建立链接，移除定时器！");
//            closeTimer.cancelTimer();
//        }
    }
    //登录到白板服务器
    @Override
    public void onLoginServerSuccess() {  //登录成功后反馈，一般为断线后登录
        //toastMsg("onLoginServerSuccess");
//        if(WhiteBoardUtils.isIntoMeeting&&WhiteBoardUtils.isAPPShowing){

        TPLog.printKeyStatus("->onLoginServerSuccess");

//        if(isUnConnectServer) {
//            toastMsg("数据会议服务器连接成功！");
//        }
        isUnConnectServer = false;
        TPLog.printKeyStatus("->isAPPShowing="+WhiteBoardUtils.isAPPShowing);
        //如果是IMIX白板已经打开并且没有会议，那么就直接创建会议
        if(WhiteBoardUtils.isAPPShowing&&!NetUtil.hasMeeting()&&VersionUtils.isImix()){
            ConnectManager.getInstance().getSender().joinOrCreateMeeting("","",false);
        }else//如果是PAD 白板也已经打开，当前也有会议，那么就直接加入到会议中
        if(WhiteBoardUtils.isAPPShowing&&NetUtil.hasMeeting()&&!VersionUtils.isImix()){
            ConnectManager.getInstance().getSender().joinOrCreateMeeting("","",true);
        }else if(!WhiteBoardUtils.isAPPShowing){
            return;
        }
        dismissDialog(DialogType.NETWORKSTATE);
        dismissDialog(DialogType.SURE);
        dismissNetworkStateDialog();
    }

    //接收到会议，可以理解成有人发起会议
    @Override
    public void onRecMeetingName(String meetingName) {
        TPLog.printError("meetingName="+meetingName);
        TPLog.printError("->NetUtil.isJoinMeeting="+NetUtil.isJoinMeeting+",NetUtil.hasMeeting()="+NetUtil.hasMeeting());
        if(NetUtil.isJoinMeeting&&!NetUtil.hasMeeting()&&!VersionUtils.isImix()){
            NetUtil.isJoinMeeting = false;
            NetUtil.curMeetingName = "";
            TPLog.printError("本地会议已经结束。。。");
            showIsSureDialog("本地会议已经结束！");
        }else if(!NetUtil.isJoinMeeting&&NetUtil.hasMeeting()&&!VersionUtils.isImix()){
            TPLog.printError("收到IMIX发起会议信息，准备加入会议。。。");
            dismissDialog(DialogType.SURE);
            ConnectManager.getInstance().getSender().joinOrCreateMeeting("", "", true);
        }
    }
    //与白板服务器链接断开
    @Override
    public void onServerDisconnected() {
        TPLog.printKeyStatus("数据会议服务器已断开链接");
        NetUtil.isJoinMeeting = false;
        NetUtil.curMeetingName = "";
        if(!isUnConnectServer) {
            isUnConnectServer = true;
            if(!NetworkUtil.getNetworkState(this)) {
                TPLog.printKeyStatus("当前网络不可用，打开网络设置对话框！");
                dismissDialog();
                showNetworkStateDialog(false,false);
                showNetworkSettingDialog();
            }else{
                TPLog.printKeyStatus("当前网络可用，准备重连！");
                dismissDialog();
                showNetworkStateDialog(true,false);
                showIsSureDialog("服务器异常,无法进行数据协作,正在重新连接......");
            }
        }
    }

    //白板服务器连接出现异常
    @Override
    public void onServerConnectException(Exception e) {
        NetUtil.isJoinMeeting = false;
        NetUtil.curMeetingName = "";
    }

    //接收到创建会议返回结果
    @Override
    public void onRecCreateMeeting(long resultCode) {
        TPLog.printError("创建会议返回结果："+resultCode);
        if(resultCode== NetError.ESUCCESS) { //入会需要同步数据
            TPLog.printError("创建会议成功！");
            NetUtil.isJoinMeeting = true;
            TPLog.printError("开始上传图片资源。。。！");
            SynFileManager.getInstance().uploadImg(mPageManager);
        }else if(resultCode == NetError.PROHIBIT_CREAR_CONFERNENCE){
            TPLog.printError("创建会议失败，禁止非IMIX客户端创建会议！");
        }else{
            TPLog.printError("创建会议失败");
        }
    }


    /**
     * 如果是第一个入会者，那么就不需要清理数据和同步数据了
     * 只有非第一个入会者才需要进行数据清理和同步
     * @param resultCode
     */
    @Override
    public void onRecJoinMeeting(long resultCode) {
        TPLog.printError("加入会议返回结果："+resultCode);
        if(resultCode== NetError.ESUCCESS) { //入会需要同步数据
            TPLog.printError("加入会议成功！需要进行会议数据同步");
            NetUtil.isJoinMeeting = true;

            //入会后就清掉所有的数据
            mPageManager.getPageList().clear();
            //再次填充一个Page
            Page page = WhiteBoardUtils.createDefWbPage();
            page.addSubPage(new SubPage());
            mPageManager.addPage(page);
            page.setBackGroundColor(getCurBackgroundColor());
            mPageManager.notifyPageChanged();
            //清除掉之前接收到的图片
            FileUtils.clearRecCacheImg();
            //清空内存中所有已经加载的图片
            BitmapManager.getInstence().clearCache();

            SynFileManager.getInstance().reset();

            //如果加入会议成功就请求服务器进行同步
            TPLog.printError("->开始请求会议数据同步");
            SendHelper.getInstance().requestSynchronous();
            showIsSureDialog("同步会议数据中...");
            startSynMeetingDataTimer();//启动超时定时器
       }else if(resultCode== NetError.ESUCCESS_NOT_NEED_SYN) {//入会不需要同步数据
            NetUtil.isJoinMeeting = true;
            TPLog.printError("加入会议成功！不需要进行会议数据同步\n上传打开的图片到服务器。。。");
            SynFileManager.getInstance().uploadImg(mPageManager);
        }
    }

    //未知参数，同步文件时需要用到
    @Override
    public void onRecServerCurBufferSzie(long size) {
        Image image = mPageManager.getSelectPage().getCurSubPage().getImage();
        SynFileManager.getInstance().sendImage(image);
    }

    //接收到服务器请求同步消息
    @Override
    public void onRecServerReqSyn(long synReqId) {
        TPLog.printError("接收到服务端请求同步会议内容消息...");
        mSendHelper.sendSynData(mPageManager, synReqId);
    }

    //接收到服务器反馈同步失败消息
    @Override
    public void onRecSynFailed() {
        cancelSynMeetingDataTimer();//取消掉同步数据定时器
        TPLog.printError("接收到服务端通知，同步会议数据失败！");
        if( NetUtil.isJoinMeeting)
        showIsSureDialog("同步会议数据失败！");
        //退出会议并清空会议信息
        mSendHelper.quitMeeting();
        NetUtil.isJoinMeeting = false;
        NetUtil.curMeetingName = "";
    }
   //接收到其他客户端发送过来的同步数据
    @Override
    public void onRecSynData(long curPageId,List<Page> pageList) {
        cancelSynMeetingDataTimer();//取消掉同步数据定时器
        dismissDialog();
        TPLog.printError("同步会议数据成功！");

        if(pageList.size()>=20){
            mToolsBar.addPageBtn.setEnabled(false);
        }else{
            mToolsBar.addPageBtn.setEnabled(true);
        }

        mPageManager.setPageList(pageList);
        mPageManager.selectPage(curPageId);

        int imgCount = 0;
        //将所有的图片拿出来，添加到下载队列内，进行下载
        for(int i = 0;i<pageList.size();i++){
            IPage page = pageList.get(i);
            ArrayList<SubPage> subPageList = page.getSubPageList();
            for(int j = 0;j<subPageList.size();j++){
                SubPage subPage = subPageList.get(j);
                ArrayList<Graph> imgGraphs = subPage.getImageGraphList();
                for(Graph g:imgGraphs){
//                    ((ImageGraph)g).reset(subPage.getMatrix());
                    SynFileManager.getInstance().requestDownload(g.getId());//2017.10.18 请求下载取消掉，服务端那边说是会自动推送
                    imgCount++;
                }
            }
        }

        TPLog.printKeyStatus("onRecSynData-->同步图片个数共"+imgCount+"张");

        if(imgCount>0){
            onDPenColorBtnEvent(WhiteBoardUtils.colors[1]);
            setSelectImgBtnEnable(true);
        }else{
            setSelectImgBtnEnable(false);
        }
    }
   //接收到同步图元
    @Override
    public void onRecGraphData(Graph graph) {
        if(graph==null)return;
        mHelperHolder.requestDrawGraph(graph);
    }
    //废弃了
    @Override
    public void onRecImageData(SubPage subpage) {
        mPageManager.getSelectPage().addSubPage(subpage);
    }
    //接收到缩放同步消息
    @Override
    public void onRecZoomData(float zoom) {
        mHelperHolder.scale(zoom / 100f);
        setCurScale(zoom / 100f);
    }
    //接收到手势缩放消息
    @Override
    public void onRecGestureZoomData(float scaleFactor, int focusX, int focusY) {
        float zoom = mPageManager.getSelectPage().getCurScale();
        zoom = zoom*scaleFactor;
        mHelperHolder.postScale(scaleFactor, focusX, focusY);
        setCurScale(zoom);
    }
    //接收到白板平移消息
    @Override
    public void onRecScrollData(Point scrollData) {
        Page page = (Page)mPageManager.getSelectPage();
        float ox = scrollData.x - page.getOffsetX();
        float oy = scrollData.y - page.getOffsetY();

        //为了兼容PC，因此接收到的偏移量，这边需要进行特殊处理
        mHelperHolder.postTranslate(ox, oy);
    }
    //接收到清屏消息
    @Override
    public void onRecClearScreen() {
        mHelperHolder.clearScreen();
    }

    //接收到白板新建消息
    @Override
    public void onRecCreateWbData(Page page) {
        mPageManager.addPage(page);
        checkPageNum();
    }
    //接收到移除白板消息
    @Override
    public void onRecDelWbData(long delWbId,long nextWbId) {
        if(mPageManager.removePage(delWbId)){
            mPageManager.selectPage(nextWbId);
            if(dialogIsShowing(DialogType.PAGETHUMBNAIL)){
                notifyPageThumbnailDataChanged();
            }
        }
    }

    @Override
    public void onRecDelAllWbData(long newTabId) {
        dismissDialog(DialogType.PAGETHUMBNAIL);
        int count = mPageManager.getPageCount();
        for(int i = 0;i<count;i++){
            mPageManager.removePage(0);
        }
        mPageManager.getSelectPage().setId(newTabId);
    }

    //接收到切换白板消息
    @Override
    public void onRecChangePage(long wbId, long subPageIndex) {
        subPageIndex = subPageIndex +1;
        TPLog.printKeyStatus("onRecChangePage->wbId="+wbId+",subPageIndex="+subPageIndex);
        IPage page = mPageManager.getSelectPage();
        if(page.getId()!=wbId) {
            mPageManager.selectPage(wbId);
            if(dialogIsShowing(DialogType.PAGETHUMBNAIL)){
                notifyPageThumbnailDataChanged();
            }
        }else {
            mPageManager.selectCurPageSubPage((int)(subPageIndex));
        }

        Image image = mPageManager.getCurSelectSubPage().getImage();
        if(image!=null&&!image.isDlSuccess()){
            mSendHelper.requestImage(image,mPageManager.getSelectPage().getId(),mPageManager.getCurPageSelectSubPageIndex()-1,((Page)mPageManager.getSelectPage()).getOwnerIndex());
        }
        mPageManager.notifyPageChanged();
    }

    //服务器请求翻页，其实是请求图片
    @Override
    public void onRecServerReqChangePage(long wbId, long subPageIndex) {
        mSendHelper.requestServerRecFlow();
    }

    //图片下载完成后回调
    @Override
    public synchronized void onRecImageDownloaded(long imageId) {
        TPLog.printKeyStatus("图片下载完成 Id：" + imageId);
        ArrayList<IPage> pageList = mPageManager.getPageList();
        for(int i = 0;i<pageList.size();i++){    //tab
            ArrayList<SubPage> subPageList = pageList.get(i).getSubPageList();
            for(int j =0;j<subPageList.size();j++){   //SubPage
                SubPage subPage = subPageList.get(j);
               ArrayList<Graph> list = subPage.getImageGraphList();
                for(int k = 0;k<list.size();k++){   //ImageGraph
                    if(list.get(k).getId() == imageId){
                        TPLog.printKeyStatus("找到了图片实体："+imageId+"，开始加载数据！");
                        if(!((ImageGraph)list.get(k)).load()){
                            return;
                        }
                        if(pageList.get(i).getId() == mPageManager.getSelectPage().getId()){
                            TPLog.printKeyStatus("准备更新界面。。。");
                            mPageManager.notifyPageChanged();
                        }
                        return;
                    }
                }
            }
        }
    }

    //接收到撤销消息
    @Override
    public void onRecRedoData(long wbId, long subPageIndex) {
        mHelperHolder.redo();
    }
   //接收到恢复消息
    @Override
    public void onRecUndoData(long wbId, long subPageIndex) {
        mHelperHolder.undo();
    }
    //接收到左旋转消息
    @Override
    public void onRecLeftRotate() {
        int angle = mPageManager.getSelectPage().getCurAngle();
        angle = angle - 90;
        mHelperHolder.rotate(angle, true);
    }
   //接收到右旋转消息
    @Override
    public void onRecRightRotate() {
        int angle = mPageManager.getSelectPage().getCurAngle();
        angle = angle + 90;
        mHelperHolder.rotate(angle, true);
    }
    //接收到旋转消息
    @Override
    public void onRecRotate(long angle, long isFinish) {
        boolean boo = false;
        if(isFinish==0){
            boo = true;
        }else{
            boo = false;
        }
        mHelperHolder.rotate((int) angle, boo);
    }
    //接收到插入图片消息
    @Override
    public void onRecInsertImg(ImageGraph img) {
        IPage page = mPageManager.getPageFromId(img.getTabId());
        if(page == null){
            return;
        }
        ISubPage subPage = page.getSubPage((int) img.getPageIndex());
        if(subPage==null){
            return;
        }
        subPage.addGraph(img);
        onDPenColorBtnEvent(WhiteBoardUtils.colors[1]);
        //请求下载图片
//        SynFileManager.getInstance().requestDownload(img.getId());
        //设置图片选择按钮可用
        setSelectImgBtnEnable(true);
    }

    //接收到坐标系改变消息，缩放和平移
    @Override
    public void onRecCoordinateChanged(CoordinateChangedMsg msg) {
        long tabId = msg.getTabId();
        long subPageIndex = msg.getSubPageIndex();
        float matrixValues[] = msg.getMatrixValues();

        IPage page = mPageManager.getPageFromId(tabId);

        if(page!=null){
            ISubPage subPage = page.getSubPage((int)subPageIndex);
            if(subPage!=null){
                subPage.setMatrixValues(matrixValues);
                if(tabId == mPageManager.getSelectPage().getId()){
                   mHelperHolder.refreshScreen();
                }
            }
        }

        if(mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_SELECT_IMG) {
            mHelperHolder.resetSelectImage();
        }
    }

    //接收到选择图片坐标系改变消息
    @Override
    public void onRecSelectGrpahCoordinateChanged(ArrayList<GraphCoordinateChangedMsg> list) {
        if(list==null||list.isEmpty()){
            return;
        }
        int count = list.size();
        TPLog.printKeyStatus("onRecSelectGrpahCoordinateChanged--->Graph Count:"+count);
        IPage page = mPageManager.getSelectPage();
        ISubPage subPage = page.getCurSubPage();
        for(int i = 0;i<count;i++){
            GraphCoordinateChangedMsg msg = list.get(i);
            ArrayList<Graph> imgList = subPage.getImageGraphList();
            int imgCount = imgList.size();
            for(int j = 0;j<imgCount;j++){
                if(imgList.get(j).getId() == msg.getId()){
                    TPLog.printKeyStatus("onRecSelectGrpahCoordinateChanged--->msg.getId():"+msg.getId());
                    Graph mGraph = imgList.get(j);
                    ((ImageGraph)mGraph).setMatrixValues(msg.getMatrixValue());
                    //((ImageGraph)imgList.get(j)).reset(subPage.getMatrix());
                    imgList.remove(mGraph);
                    imgList.add(mGraph);//把选择的图元放在最前面显示
                    //((SubPage)subPage).debug();
                    break;
                }
            }
        }
        if(mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_SELECT_IMG) {
            mHelperHolder.resetSelectImage();
        }
        mHelperHolder.refreshScreen();
    }

    //接收到选择图元删除消息
    @Override
    public void onRecDeleteGraph(DeleteGraphMsg msg) {
        if(msg==null){
            return;
        }
        IPage page = mPageManager.getPageFromId(msg.getTabId());
        ISubPage subPage = page.getSubPage(msg.getSubPageIndex());
        ArrayList<Graph> list = subPage.getImageGraphList();
        int count = list.size();
        for(int i = 0;i<count;i++){
            if(list.get(i).getId() == msg.getGraphId()){
                list.remove(i);
                break;
            }
        }
        if(mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_SELECT_IMG) {
            mHelperHolder.resetSelectImage();
        }
        mPageManager.notifyPageChanged();
    }

    @Override
    public void onRecConfMemberListUpdate(int num) {

    }

    @Override
    public void onRecServerConnectNumNtf(int num) {

    }

    //去网络设置
    @Override
    public void onDToSettingNetwork() {
//        toastMsg("打开网络设置!");
        try {
            if(VersionUtils.isImix()) {
                StorageMangerJarUtils.toNetWorkSetting(this);
            }else{
                StorageMangerJarUtils.toPadNetWorkSetting(this);
            }
        }catch(Exception e){
            toastMsg("打开网络设置失败！");
            TPLog.printError("PackageUtils", "not found activity " + StorageMangerJarUtils.PACKAGE_SYSTEM_SETTINGS + "/" + StorageMangerJarUtils.APP_SYSTEM_SETTINGS);
        }
    }

    @Override
    public void onDRlsDcsMnger() {

    }

    @Override
    public void onDDataConfModeChange(int mode) {

    }

    //打开文件选择
    @Override
    public void onDToFileManager(String path) {
        dismissDialog();
        boolean isInstall = StorageMangerJarUtils.isInstallKDFileBroser(this);
        if(isInstall) {
            StorageMangerJarUtils.toFileManager(this, path);
        }else{
//            boolean boo = StorageMangerJarUtils.toSystemFileManager(this, path);
            boolean boo = false;
            if(!boo){
                toastMsg("打开文件管理失败，文件管理程序未安装");
            }
        }
        if(isExit){
            finish();
        }
    }

    /**********************网络状态监听*****************************/

    @Override
    public void onNetworkUsable() {
        TPLog.printKeyStatus("当前网络可用");
        if(StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF) {
            ConnectManager.getInstance().startReconnect();
        }
        if(WhiteBoardUtils.isAPPShowing){
            TPLog.printKeyStatus("数据协作现在正在显示");
            //隐藏网络连接对话框
            dismissDialog(DialogType.SETTINGNETWORK);
            dismissDialog();
            if(StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF)  {
                showNetworkStateDialog(true, true);
            }
        }else{
            TPLog.printKeyStatus("数据协作现在没有显示");
        }
    }

    //实际研发的过程中出现过，网络不可用会存在连续发送多次广播现象，因此这里做一个简单的处理逻辑
    private  long lastNetworkUnusableTime = 0;
    @Override
    public void onNetworkUnusable() {
        long time = System.currentTimeMillis();
        //神奇的IMIX他会发送两次网络不可用广播
        if((time - lastNetworkUnusableTime)<100){
            return;
        }
        NetUtil.isJoinMeeting = false;
        lastNetworkUnusableTime = time;
        TPLog.printKeyStatus("当前网络不可用");
        ConnectManager.getInstance().stopReconnect();
        if(WhiteBoardUtils.isAPPShowing) {
            TPLog.printKeyStatus("数据协作现在正在显示");
            dismissDialog(DialogType.SETTINGNETWORK);
            dismissDialog();
            if(StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF) {//协作功能开启
                //打开网络异常对话框
                showNetworkSettingDialog();
                showNetworkStateDialog(false, false);
            }
        }else{
            TPLog.printKeyStatus("数据协作现在没有显示");
        }
    }
    /**********************保存白板************************/

    @Override
    public void savePageStart(final int saveCount) {
        TPLog.printKeyStatus("开始保存，保存总页数: " + saveCount);
        if(saveCount>=1){
            if(!sendMail&&!qRDownLoad){//发送邮件和二维码下载保存白板时不需要进行进度显示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoadFileDilaog(saveCount,0);
                        needSaveProgressDialog = true;
                        curSaveProgress = 0;
                    }
                });
            }
        }
    }

    @Override
    public void saveProgress(final int progress) {
        TPLog.printKeyStatus("当前保存进度:" + progress);
        if(sendMail||qRDownLoad){
            return;
        }
        if(needSaveProgressDialog) {
            curSaveProgress ++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCurProgress(progress);
                    IPage page = mPageManager.getPage(progress-1);//因为现在每个白板只有一个子页，因此这个用progress可以获取到当前保存的白板
                    if (page!=null) {
                        String fileName = page.getName() + "_" + DateUtils.getCurTime("HHmmss");
                        TPLog.printKeyStatus("当前保存文件：" + fileName);
                        String hintText = "正在保存 \""+fileName+"\" ...";
                        setProgressDialogHintText(hintText);
                    }
                }
            });
        }
    }

    @Override
    public void savePageSuccess(final String savePath) {
        TPLog.printKeyStatus("保存文件成功！savePath:"+savePath);
        if(!sendMail&&!qRDownLoad) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
  //                  if (saveQueue.isEmpty() || curSaveAllType == SAVEALL_ONEBYONE) {
                       dismissDialog();

//                        String locPath = FileUtils.getAliasPath(savePath,mStroageManager);
//
//                        if(locPath.endsWith("/")){
//                            locPath = locPath.substring(0,locPath.length()-1);
//                        }
//
//                        String path = locPath;
//                        String msg = "已保存至：" + locPath;
//                        dismissDialog(DialogType.LOADFILE);
//                        String path1 = FileUtils.getRealPath(path,mStroageManager);

                        //showSaveFileSuccessDialog(path1,path);

                        if(isExit){
                            finish();
//                            setSaveFileSuccessDialogBtnText("关闭");
                        }else{
//                            setSaveFileSuccessDialogBtnText("确定");
//                            toastMsg("保存成功！");
                            if(WhiteBoardUtils.isAPPShowing) {
                                if (aliasSavePath.endsWith("/")) {
                                    aliasSavePath = aliasSavePath.substring(0, aliasSavePath.length() - 1);
                                }
                                showSaveFileSuccessDialog(savePath, aliasSavePath);
                            }
                        }

                        if(isCloseSave){
                            mPageManager.removePage(curSavePage);
                            //发送关闭白板消息
                            mSendHelper.sendCloseWb(curSavePage.getId(),mPageManager.getSelectPage().getId());
                            isCloseSave = false;
                        }
//                    }
//                    else {
//                        curSavePage = saveQueue.remove(0);
//                        NewTouchDataActivity.this.savePath = FileUtils.SAVE_WRITEBOARD_DIR + FileUtils.getCurWhiteBoardSaveDir();
//                        FileUtils.checkDirExists(savePath);
//                        saveDirName = curSavePage.getName();
//                        aliasSavePath = FileUtils.getAliasPath(savePath,mStroageManager);
//                        isSaveAll = true;
//                        if (curSaveAllType == SAVEALL_NOT_PROMPT) {
//                            onDSaveBtnEvent(saveDirName, isSaveAll);
//                        }
//                    }
                }
            });
        }else if(sendMail){ //发送邮件
            TPLog.printKeyStatus("发送邮件开始...");
            List<String> filePaths = new ArrayList<String>();
            FileUtils.findAllChildFile(mailImageCacheDir,filePaths);
            TPLog.printKeyStatus("添加 "+filePaths.size()+" 个图片附件到邮件");
            String images[] = new String[filePaths.size()];
            filePaths.toArray(images);
            if(NetworkUtil.getNetworkState(this)){
                MailUtil.sendImageMail(recMails,mailTitle,images);
            }else{
                MailUtil.callBackSendMailFailed();
            }
            sendMail = false;
        }else if(qRDownLoad){
//            List<String> filePaths = new ArrayList<String>();
//            FileUtils.findAllChildFile(FileUtils.RUNNING_CACHE,filePaths);
            TPLog.printError("qrcode image cache resources to completed...");
            String title = FileUtils.getCurWhiteBoardSaveDir();
            TPLog.printError("html title :"+title);

            int savePageCount = 0;

            List<IPage> pageList = mPageManager.getPageList();
            for(int i = 0;i<pageList.size();i++){
                if(pageList.get(i).isNeedSave()){
                    savePageCount ++;
                }
            }
            String subtitle = savePageCount+"个白板,共"+savePageCount+"页";
            TPLog.printError("html subtitle :"+subtitle);
            setQRCodeCacheFileDir(title,subtitle,FileUtils.RUNNING_CACHE);
            qRDownLoad = false;
        }
//        FileUtil.refreshFileInDB(this,savePath);

        Utils.rootCommand("busybox sync");
    }

    @Override
    public void savePageFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TPLog.printKeyStatus("save page failed");
//                String msg = "文件保存失败！";
//                dismissDialog(DialogType.LOADFILE);
                dismissDialog();
                toastMsg("保存失败！");
//                showIsSureDialog(msg);
            }
        });
    }

    /************************邮件发送******************************/

    @Override
    public void onSendMailSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                dismissLoadDialog();//现在可以不要，不过留着也不会报错 ，留着先
                if(!WhiteBoardUtils.isAPPShowing){
                    return;
                }
                dismissDialog();
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
//                dismissLoadDialog();
                if(!WhiteBoardUtils.isAPPShowing){
                    return;
                }
                dismissDialog();
                String msg = "邮件发送失败，请检查网络或邮件服务设置";
                showIsSureDialog(msg);
            }
        });
    }

    @Override
    public void onSendMailUnknownHost() {//这个暂时不处理
        onSendMailFailed();
//        showToMailConfigSettingDialog("请配置正确的邮箱账户和密码!");
    }

    @Override
    public void onConnectMailServerFailed() {
        onSendMailFailed();
    }

    @Override
    public void onSendMailAuthenticationFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!WhiteBoardUtils.isAPPShowing){
                    return;
                }
                dismissLoadDialog();
                dismissDialog();
                showToMailConfigSettingDialog();
            }
        });
    }

    /************************打开文件  已经废弃*****************************/

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
        TPLog.printError("->onResume");

        WhiteBoardUtils.isAPPShowing = true;

        ConnectManager.getInstance().setCallback(this);

        //检测服务是否启动，没有启动 执行启动
        if(!VersionUtils.is55InchDevice()) {//55寸版本无协作功能
            Intent intent = new Intent(this, BootService.class);
            if (!Utils.isServiceRunning(this, "com.kedacom.service.BootService")) {
                TPLog.printKeyStatus("->BootService 未启动");
                startService(intent);
            } else {
                TPLog.printKeyStatus("->BootService 已启动");
                if (BootService.instance != null) {
                    BootService.instance.initStorageMangerJarUtils();
                }
            }
        }


        String msg = StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF?"当前协作功能打开":"当前协作功能关闭";
        TPLog.printKeyStatus("->"+msg);
        ConnectManager.getInstance().setCallback(this);

        if( ConnectManager.getInstance().getConnecter().isConnect()&&StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF) {
            if(VersionUtils.isImix()&&!NetUtil.hasMeeting()){// 只能Imix创会
                TPLog.printError("当前是IMIX,开始创建会议！");
                ConnectManager.getInstance().getSender().joinOrCreateMeeting("", "", false);
            }else if(NetUtil.hasMeeting()&&!NetUtil.isJoinMeeting&&StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF){//PAD 版本  被动入会 2017.09.26
                TPLog.printError("当前是PAD,开始加入会议！");
                ConnectManager.getInstance().getSender().joinOrCreateMeeting("", "", true);
            }
        }

        if(!StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF){
            dismissNetworkStateDialog();
            dismissDialog();
        }

        Utils.notificationTouchDataDisplayState(this,true);


        boolean imgIsEmpty = mPageManager.getCurSelectSubPage().getImageGraphList().isEmpty();

        if(imgIsEmpty){
            setSelectImgBtnEnable(false);
//            mToolsBar.selectImgBtn.setEnabled(false);
//            mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.select_img_enable_icon);
        }

        //如果刚打开数据协作，他没有进行协作那个就启动定时器
//        if(!ConnectManager.getInstance().getConnecter().isConnect()&&StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF){
//            closeTimer.startTimer();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TPLog.printError("->onPause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        ConnectManager.getInstance().setCallback(null);

        TPLog.printError("->onStop");
        //ConnectManager.getInstance().setCallback(null);
        dismissWhiteBoardNameDialog();
        WhiteBoardUtils.isAPPShowing = false;
        NetUtil.isJoinMeeting = false;

        //发送退会消息
        mSendHelper.quitMeeting();

        //finish();
        Utils.notificationTouchDataDisplayState(this,false);

//        if(closeTimer!=null){
//            closeTimer.cancelTimer();
//        }
    }


    @Override
    protected void onDestroy() {
        TPLog.printError("->onDestroy");
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

        SynFileManager.getInstance().reset();

//        closeTimer = null;
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

    private  boolean startSwitchFileActivity(){
        if(AppUtil.isFileBroser(this)) {
            try {
                Intent intent = new Intent(StorageMangerJarUtils.ACTION_SWTICH_FILE_ACTIVITY);
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                TPLog.printError("打开文件选择器失败:");
                TPLog.printError(e);
                toastMsg("文件管理打开异常，请检查文件管理是否正常安装！");
                return false;
            }
        }else if(AppUtil.isFileShare(this)){
            try {
                Intent intent = new Intent(StorageMangerJarUtils.ACTION_SWTICH_FILE_ACTIVITY2);
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                TPLog.printError("打开文件选择器失败:");
                TPLog.printError(e);
                toastMsg("文件共享打开异常，请检查文件管理是否正常安装！");
                return false;
            }
        }else{
            toastMsg("文件管理未安装，该功能无法使用！");
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //重置控件状态
        mToolsBar.insertImgBtn.setBackgroundResource(R.mipmap.new_insert_img_normal_icon);
        mToolsBar.insertImgBtn.setEnabled(true);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String returnedData = data.getStringExtra("file_retriever_path");
                    if (returnedData!=null&&!returnedData.trim().isEmpty()) {
                        TPLog.printKeyStatus("接收到了文件："+returnedData);
                        onDSwitchFile(returnedData);
                    }
                }
                break;
            default:
        }
    }


    private long last134DownTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TPLog.printKeyStatus("点击按键："+keyCode);
        if (keyCode == 92){   //向上翻页键
//            prePage();
            toastMsg("向前翻页");
            bottomBarBtnClickListener.onClick(mToolsBar.prePageBtn);
        }else if (keyCode == 93){ //向下翻页键
//           nextPage();
            toastMsg("向后翻页");
            bottomBarBtnClickListener.onClick(mToolsBar.nextPageBtn);
        }else if (134 == keyCode){
            last134DownTime = System.currentTimeMillis();
        }else if(140==keyCode){
            long curDownTime = System.currentTimeMillis();
            long cTime = curDownTime - last134DownTime;
            if(cTime<500){
                bottomBarBtnClickListener.onClick(mToolsBar.paintBtn);
            }
        }
        return true;
    }

    private void selfAdaption(){

        mHelperHolder.resetSelectImage();

        if(selfAdaption) {
            mHelperHolder.selfAdaption();
        } else{
            mHelperHolder.oneToOne();
        }
        selfAdaption = !selfAdaption;

        if(!selfAdaption){
            mToolsBar.selfAdaptionBtn.setBackgroundResource(R.mipmap.new_onetoone_normal_icon);
        }else{
            mToolsBar.selfAdaptionBtn.setBackgroundResource(R.mipmap.new_selfadaption_normal_icon);
        }

        Matrix matrix = mPageManager.getSelectPage().getMatrix();
        mSendHelper.sendCoordinateChanged(matrix,mPageManager.getSelectPage().getId(),mPageManager.getSelectPage().getCurSubPageIndex()-1);
        mHelperHolder.refreshScreen();
    }


    boolean selfAdaption = true;

    View.OnClickListener bottomBarBtnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            mWb.getHelperHolder().dismissErasePanelWindow();//点击任意按钮，都要移除掉擦板否则，擦板移除事件会被拦截，无法消失
            switch(v.getId()){
                case R.id.menuBtn:
                    mHelperHolder.resetSelectImage();
                    mPageManager.notifyPageChanged();
                    if(dialogIsShowing(DialogType.MENU)){
                        dismissDialog(DialogType.MENU);
                    }else {
                        mToolsBar.menuBtn.setBackgroundResource(R.mipmap.new_menu_btn_select_icon);
                        mToolsBar.menuBtn.setEnabled(false);
                        showMenuDialog(v);
                    }
                    break;
                case R.id.insertImgBtn:
                    if(!startSwitchFileActivity()){
                        return;
                    }
                    switchFileDialogShowing = true;
                    mToolsBar.insertImgBtn.setBackgroundResource(R.mipmap.new_insert_img_select_icon);
                    mToolsBar.insertImgBtn.setEnabled(false);
                    break;
                case R.id.selectImgBtn:
                    switchBtn(v.getId());
                    mHelperHolder.setOpType(WhiteBoardUtils.OP_SELECT_IMG);
                    break;
                case R.id.paintBtn:
                    dismissDialog();
                    if(dialogIsShowing(DialogType.SWITCHCOLOR)){
                        dismissDialog(DialogType.SWITCHCOLOR);
                    }else {
                        switchBtn(v.getId());
                        if (mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_PAINT) {
                            //弹出画笔选择框
                            showSwitchColorDialog(v);
                        }
                        TPLog.printKeyStatus("bottomBarBtnClickListener --> OP_PAINT");
                        mHelperHolder.setOpType(WhiteBoardUtils.OP_PAINT);
                    }
                    break;
                case R.id.eraseBtn:
                    if(dialogIsShowing(DialogType.SWITCHERASE)){
                        dismissDialog(DialogType.SWITCHERASE);
                    }else {
                        switchBtn(v.getId());
                        if (mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_ERASE || mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_ERASE_AREA) {
                            showSwitchEraseDialog(v);
                        }
                        if (curEraseMode == WhiteBoardUtils.OP_ERASE_AREA) {
                            mHelperHolder.setOpType(WhiteBoardUtils.OP_ERASE_AREA);
                        } else {
                            mHelperHolder.setOpType(WhiteBoardUtils.OP_ERASE);
                        }
                    }
                    break;
                case R.id.undoBtn:
                    mHelperHolder.resetSelectImage();
                    mHelperHolder.undo();
                    //发送撤销消息
                    IPage page = mPageManager.getSelectPage();
                    long pageId = page.getId();
                    int subPageIndex = page.getCurSubPageIndex() - 1;
                    mSendHelper.sendUndoMsg(pageId, subPageIndex);
                    break;
                case R.id.redoBtn:
                    mHelperHolder.resetSelectImage();
                    mHelperHolder.redo();
                    //发送恢复消息
                    IPage page2 = mPageManager.getSelectPage();
                    long pageId1 = page2.getId();
                    int subPageIndex1 = page2.getCurSubPageIndex() - 1;
                    mSendHelper.sendRedoMsg(pageId1, subPageIndex1);
                    break;
                case R.id.selfAdaptionBtn:
                    mHelperHolder.resetSelectImage();
                    selfAdaption();
                    break;
                case R.id.addBtn:
                    mHelperHolder.resetSelectImage();
                    int normalBg = WhiteBoardUtils.curBackground;
                    WhiteBoardUtils.setWbBackground(mWb,normalBg);
                    Page page1 = WhiteBoardUtils.createDefWbPage();
                    page1.setBackGroundColor(normalBg);
                    String name = page1.getName();
                    setWbName(name);
                    mPageManager.addPage(page1);

                    //发送创建白板消息
                    mSendHelper.sendCreateNewPageMsg(page1);

                    checkPageNum();

//                    int count = mPageManager.getPageCount();
//                    if (count >= 20) {
//                        //toastMsg("白板数量已达上限！");
//                        mToolsBar.addPageBtn.setEnabled(false);
//                    }
                    break;
                case R.id.prePageBtn:
                    mHelperHolder.resetSelectImage();
                    prePage();
                    break;
                case R.id.pageNumBtn:
                    mHelperHolder.resetSelectImage();
                    if(!dialogIsShowing(DialogType.PAGETHUMBNAIL)) {
                        mToolsBar.pageNumBtn.setBackgroundResource(R.mipmap.new_page_num_select_icon);
                        mToolsBar.pageNumBtn.setEnabled(false);
                        showPageThumbnailDialog();
                    }else{
                        dismissDialog(DialogType.PAGETHUMBNAIL);
                    }
                    break;
                case R.id.nextPageBtn:
                    mHelperHolder.resetSelectImage();
                    nextPage();
                    break;
            }
            cancelSelecImageTimer();
        }
    };


    private void prePage(){
        IPage page = mPageManager.prePage();

        if(page==null){
            return;
        }

        if(!mPageManager.hasPrePage()){
            mToolsBar.prePageBtn.setEnabled(false);
        }

        if(mPageManager.hasNextPage()){
            mToolsBar.pageNumBtn.setEnabled(true);
        }

        mToolsBar.pageNumBtn.setText((mPageManager.getSelectPageIndex()+1)+"/"+mPageManager.getPageCount());

        //发送同步消息
        mSendHelper.sendChangePageMsg(mPageManager.getSelectPage().getId(),0,null);
    }


    private void nextPage(){
        IPage page = mPageManager.nextPage();
        if(page==null){
            return;
        }
        if(mPageManager.hasPrePage()){
            mToolsBar.prePageBtn.setEnabled(true);
        }
        if(!mPageManager.hasNextPage()){
            mToolsBar.pageNumBtn.setEnabled(true);
        }
        mToolsBar.pageNumBtn.setText((mPageManager.getSelectPageIndex()+1)+"/"+mPageManager.getPageCount());

        //发送同步消息
        mSendHelper.sendChangePageMsg(mPageManager.getSelectPage().getId(),0,null);
    }


    private void startSelectImageTimer(){
        if(handler.hasMessages(100)){
            handler.removeMessages(100);
        }
        if(mHelperHolder.getCurOpType() != WhiteBoardUtils.OP_SELECT_IMG){
            handler.sendEmptyMessageDelayed(100,5*1000);
        }
    }

    public void cancelSelecImageTimer(){
        if(handler.hasMessages(100))
            handler.removeMessages(100);
    }

    @Override
    public void onDelSelectImg(int imgId) {
        boolean isEmpty = mPageManager.getCurSelectSubPage().getImageGraphList().isEmpty();
        TPLog.printKeyStatus("delete img:"+imgId,"current have img :"+isEmpty);
        if(isEmpty){
            bottomBarBtnClickListener.onClick(mToolsBar.paintBtn);
        }
    }

    @Override
    public void onPaintDrawDown() {
        if(mPaintRefreshDialog!=null)
            mPaintRefreshDialog.stopRefresh();
    }

    private void startSynMeetingDataTimer(){
        if(handler.hasMessages(101)){
            handler.removeMessages(101);
        }
        TPLog.printKeyStatus("启动入会同步定时器！60s后超时！");
        handler.sendEmptyMessageDelayed(101,60*1000);
    }

    private void   cancelSynMeetingDataTimer(){
        if(handler.hasMessages(101))
            handler.removeMessages(101);
        TPLog.printKeyStatus("取消入会同步定时器！");
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                if(mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_SELECT_IMG) {
                    mHelperHolder.setOpType(WhiteBoardUtils.OP_PAINT);
                    switchBtn(R.id.paintBtn);
                    mHelperHolder.resetSelectImage();
                    mHelperHolder.refreshScreen();
                    //mPageManager.notifyPageChanged();
                }
            }else if(msg.what == 101){//请求同步数据超时定时器
                TPLog.printError("同步入会数据失败，同步数据超时！");
//                showIsSureDialog("同步入会数据失败！");
                onRecSynFailed();
            }else if(msg.what == 102){
                if(mPaintRefreshDialog!=null)
                    mPaintRefreshDialog.show();
            }
        }
    };


    private void checkPageNum(){
        int count = mPageManager.getPageCount();
        if (count >= 20) {
            //toastMsg("白板数量已达上限！");
            mToolsBar.addPageBtn.setEnabled(false);
        }
    }

    private void setSelectImgBtnEnable(boolean hasImg){

        if(mHelperHolder==null){
            return;
        }

        mToolsBar.selectImgBtn.setEnabled(hasImg);

        if(!hasImg){//不存在图片
            mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.select_img_enable_icon);
            if(mHelperHolder.getCurOpType() == WhiteBoardUtils.OP_SELECT_IMG){
                bottomBarBtnClickListener.onClick(mToolsBar.paintBtn);
            }
        }else{ //有图片
            if(mHelperHolder.getCurOpType() != WhiteBoardUtils.OP_SELECT_IMG){//当前操作模式非图片选择
                mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.new_select_img_normal_icon);
            }else{
                mToolsBar.selectImgBtn.setBackgroundResource(R.mipmap.new_select_img_select_icon);
            }
        }
    }

}