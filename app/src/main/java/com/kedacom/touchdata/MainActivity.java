package com.kedacom.touchdata;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kedacom.touchdata.filemanager.FileManager;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.filemanager.OpenFileManager;
import com.kedacom.touchdata.mail.MailUtil;
import com.kedacom.touchdata.net.NetHandler;
import com.kedacom.touchdata.net.SendHelper;
import com.kedacom.touchdata.net.SynFileManager;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.helper.HelperHolder;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.IWhiteBoardStateChanagedListener;
import com.kedacom.touchdata.whiteboard.view.WhiteBoardView;
import com.kedacom.tplog.TPLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/*
    public class MainActivity extends BaseActivity implements IWhiteBoardStateChanagedListener, PageManager.ISavePageListener, View.OnClickListener, FileManager.OnChooseFileListener {

        private final int SAVE_MODE_NORMAL = 0;

        private final int SAVE_MODE_MAIL = 1;

        private final int SAVE_MODE_SAVE = 2;

        private final String MEETING_NAME = "TouchData";

        private final String MEETING_PASSWORD = "";

        private int curSaveMode = SAVE_MODE_NORMAL;

        private WhiteBoardView mWb;

        private PageManager mPageManager;

        private HelperHolder mWbHolder;

        private PageThumbnailManager mPageThumManager;

        //private String path = "/storage/sdcard1/IMG_20160530_120515.jpg";
        private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image1.jpg";
        private String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image2.jpg";
        private String path3 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image3.jpg";
        private String path4 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image4.jpg";

        private String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SAVEKD";

        String paths[] = {path, path2, path3, path4};

        int ids[] = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btn10,
                R.id.btn11, R.id.btn12, R.id.btn13, R.id.btn14, R.id.btn15, R.id.btn16, R.id.btn17, R.id.btn18};
        private Button btns[] = new Button[ids.length];

        private int colors[] = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.WHITE};

        private int curColorIndex = 0;

        private ConnectManager mConnect;

        private SendHelper mSendHelper;

        private String serverIp = "172.16.48.50";
        private int serverPort = 5000;


        private String mailCache = FileUtils.RUNNING_CACHE + "mailCache";

        private TextView logTv;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            TPLog.printError("电子白板启动中。。。。。。");

            //初始化白板工具类
            WhiteBoardUtils.init(this);
            //初始化文件管理类
            FileManager.getInstance().setOnChooseFileListener(this);
            //初始化连接类
            mConnect = ConnectManager.getInstance();
            mConnect.addCallBack(new TouchDataCallBack(mHandler));

            mConnect.connect(serverIp, serverPort);

            mSendHelper = SendHelper.getInstance();
            mSendHelper.init(mConnect);


            //创建页面管理器
            mPageManager = new PageManager();

            SynFileManager.getInstance().init(mPageManager, mHandler);

            mPageThumManager = new PageThumbnailManager(this, mPageManager);

            setContentView(R.layout.activity_main);

            initView();

            mWb = (WhiteBoardView) findViewById(R.id.mWb);
            mWb.setIWhiteBoardStateChanagedListener(this);

            mWbHolder = mWb.getHelperHolder();

            if (savedInstanceState == null) {
                Page page = new Page();
                for (int i = 0; i < paths.length; i++) {
//                    Image im = new Image();
//                    String path = paths[i];
//                    im.setFilePath(path);
                  //  Point size = BitmapManager.getImageSize(path);
//                    im.setWidth(size.x);
//                    im.setHeight(size.y);
                   // File file = new File(path);
                   // im.setFileName(file.getName());
                    SubPage subPage = new SubPage();
                    page.addSubPage(subPage);
                }
                page.setName(WhiteBoardUtils.getPageName());
                mPageManager.addPage(page);

            } else {
                restore(savedInstanceState);
            }

            mWb.setPageManager(mPageManager);
            mPageManager.setISavePageListener(this);

            initView();

            logTv = (TextView) findViewById(R.id.logId );

            findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logTv.setText("");
                }
            });
            // test();

            //mWb.setBackgroundImage(path3);
        }

        private void initView() {

            for (int i = 0; i < ids.length; i++) {
                btns[i] = (Button) findViewById(ids[i]);
                btns[i].setOnClickListener(this);
            }
        }
*/
        /*******************************
         * IWhiteBoardStateChanagedListener
         ********************************************/
/*
        @Override
        public void onScaleChanged(final float curScale) {
            float args = (curScale * 100f);
            toastMsg((int) args + "%");
            //发送缩放信息
            long tabId = mPageManager.getSelectPage().getId();
            mSendHelper.sendScaleMsg(tabId, args);
        }

        @Override
        public void onRotateChanged(final int angle, boolean isFinish) {
            long tabId = mPageManager.getSelectPage().getId();
            mSendHelper.sendRotateMsg((int) tabId, angle, isFinish);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    logTv.append("\n旋转:"+angle);
//                }
//            });
        }

        @Override
        public void onGraphUpdate(Graph graph) {
            TPLog.printError("发送图元信息");
            int pageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
            long tabId = mPageManager.getSelectPage().getId();
            mSendHelper.sendGraphMsg(graph, (int) tabId, pageIndex);
            TPLog.printError("发送图元信息---------------完毕");
        }

        @Override
        public void onTranslateChanged(final float ox, final float oy, boolean isFinish) {
            long tabId = mPageManager.getSelectPage().getId();
            long pageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
            mSendHelper.sendScrollMsg(ox, oy, tabId, pageIndex, isFinish);

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    logTv.append("\n拖动:（"+ox+","+oy+")");
//                }
//            });
        }

        @Override
        public void onUndo() {

        }

        @Override
        public void onRedo() {

        }

        @Override
        public void onUndoEnable(final boolean enable) {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   btns[2].setEnabled(enable);
               }
           });
        }

        @Override
        public void onRedoEnable(final boolean enable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btns[3].setEnabled(enable);
                }
            });
        }

        @Override
        public void onPageChanged(int pageIndex, int curSubPageIndex, int subPageNum, final boolean nextSubPageEnable, final boolean lastSubPageEnable) {
            String msg = "pageIndex=" + pageIndex + ",curSubPageIndex=" + curSubPageIndex + ",subPageNum=" + subPageNum + ",nextSubPageEnable=" + nextSubPageEnable + ",lastSubPageEnable=" + lastSubPageEnable;
            TPLog.printError(msg);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btns[0].setEnabled(lastSubPageEnable);
                    btns[1].setEnabled(nextSubPageEnable);
                }
            });
        }
*/

        /**************************************************
         * IWhiteBoardStateChanagedListener END
         *******************************************************************/
/*
        //对Image
        private void checkImage(Image image) {
            if(image==null)return;
            if (!image.isDlSuccess() && !image.isExistOnServer()) {
                //向服务器请求翻页
                long tabId = mPageManager.getSelectPage().getId();
                long pageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                int imageId = ((SubPage) mPageManager.getSelectPage().getCurSubPage()).getImage().getId();
                int ownerIndex = ((Page) mPageManager.getSelectPage()).getOwnerIndex();
                mSendHelper.sendRequestServerTurnPageMsg(tabId, pageIndex, imageId, ownerIndex);
            } else if (!image.isDlSuccess() && image.isExistOnServer()) {
                //请求文件信息
                SynFileManager.getInstance().requestFileInfo(image);

            } else if (image.isDlSuccess() && !image.isExistOnServer()) {
                //SynFileManager.getInstance().sendImage(image);
            }
        }

        private int curIndex = 0;

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.btn1:
                    mPageManager.previousSubPage();
                    long tabId = mPageManager.getSelectPage().getId();
                    long subPageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                    Image image = ((SubPage) mPageManager.getCurSelectSubPage()).getImage();
                    mSendHelper.sendChangePageMsg(tabId, subPageIndex, image);
                    checkImage(image);
                    break;
                case R.id.btn2:
                    mPageManager.nextSubPage();
                    tabId = mPageManager.getSelectPage().getId();
                    subPageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                    image = ((SubPage) mPageManager.getCurSelectSubPage()).getImage();
                    mSendHelper.sendChangePageMsg(tabId, subPageIndex, image);
                    checkImage(image);
                    break;
                case R.id.btn3:
                    mWbHolder.undo();
                    tabId = mPageManager.getSelectPage().getId();
                    subPageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                    mSendHelper.sendUndoMsg(tabId, subPageIndex);
                    break;
                case R.id.btn4:
                    mWbHolder.redo();
                    tabId = mPageManager.getSelectPage().getId();
                    subPageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                    mSendHelper.sendRedoMsg(tabId, subPageIndex);
                    break;
                case R.id.btn5:
                    Page page = new Page();
                    SubPage subPage = new SubPage();
                    page.addSubPage(subPage);
                    page.setName(WhiteBoardUtils.getPageName());
                    mPageManager.addPage(page);
                    mSendHelper.sendCreateNewPageMsg(page);
                    break;
                case R.id.btn6:
                    curIndex++;
                    if (curIndex >= mPageManager.getPageCount()) {
                        curIndex = 0;
                    }
                    mPageManager.selectPage(curIndex);

                    tabId = mPageManager.getSelectPage().getId();
                    subPageIndex = mPageManager.getCurPageSelectSubPageIndex() - 1;
                    image = ((SubPage) mPageManager.getCurSelectSubPage()).getImage();
                    mSendHelper.sendChangePageMsg(tabId, subPageIndex, image);
                    break;
                case R.id.btn7:
                    if (curIndex >= mPageManager.getPageCount()) {
                        curIndex = mPageManager.getPageCount() - 1;
                    }
                    if (curIndex < 0) {
                        curIndex = 0;
                    }
                    mPageManager.removePage(curIndex);
                    break;
                case R.id.btn8:
                    mWbHolder.setOpType(WhiteBoardUtils.GRAPH_PEN);
                    break;
                case R.id.btn9:
                    mWbHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE);
                    break;
                case R.id.btn10:
                    mWbHolder.setOpType(WhiteBoardUtils.GRAPH_ERASE_AREA);
                    break;
                case R.id.btn11:
                    mWbHolder.clearScreen();
                    mSendHelper.sendClearScreenMsg();
                    break;
                case R.id.btn12:
                    curSaveMode = SAVE_MODE_SAVE;
                   //mPageManager.saveCurSubPage(saveDir);
                    break;
                case R.id.btn13:
                    curColorIndex++;
                    if (curColorIndex >= colors.length) {
                        curColorIndex = 0;
                    }
                    mWbHolder.setPaintColor(colors[curColorIndex]);
                    arg0.setBackgroundColor(colors[curColorIndex]);
                    break;
                case R.id.btn14:
                    FileManager.getInstance().showSwitchFileWindow(this);
                    break;
                case R.id.btn15:
                    curSaveMode = SAVE_MODE_SAVE;
                    //mPageManager.saveCurPage(saveDir);
                    break;
                case R.id.btn16:
                    mPageThumManager.displayPageList();
                    break;
                case R.id.btn17:
                    sendMail();
                    break;
                case R.id.btn18:
                    mSendHelper.createMeeting(MEETING_NAME, MEETING_PASSWORD);
                    break;
            }
        }

        private void sendMail() {
            curSaveMode = SAVE_MODE_MAIL;
            FileUtils.deleteFile(mailCache);
            mPageManager.saveAllPage(mailCache);
        }


        @Override
        public void savePageStart(int saveCount) {
            TPLog.printKeyStatus("需要保存文件的总数-------------->" + saveCount);
        }

        @Override
        public void saveProgress(int proress) {
            TPLog.printKeyStatus("当前保存第-" + proress + "-页");
        }

        @Override
        public void savePageSuccess(String savePath) {
            TPLog.printError("---------------->保存成功");
            if (curSaveMode == SAVE_MODE_SAVE) {

            } else if (curSaveMode == SAVE_MODE_MAIL) {
                List<String> files = FileUtils.getAllSubFile(mailCache);
                if (files == null) return;

                String user[] = {"zhanglei_sxcpx@kedacom.com"};
                int count = files.size();
                String filePaths[] = new String[count];
                for (int i = 0; i < count; i++) {
                    filePaths[i] = files.get(i);
                }
                MailUtil.sendImageMail(user, MailUtil.subject, filePaths);
            }
        }

        @Override
        public void savePageFailed() {
            TPLog.printKeyStatus("有页面保存失败啦，具体是那个页面不清楚");
            curSaveMode = SAVE_MODE_NORMAL;
        }

        @Override
        public void onChooseFile(File file) {

            Page page = new Page();
            page.setOwnerIndex((int) NetUtil.curUserId);
            page.setPageMode(Page.MODE_DOCUMENT);
            page.setName(file.getName());
            SubPage subPage = new SubPage();
            page.addSubPage(subPage);
            page.setAnoymous(false);
            mPageManager.addPage(page);

            //发送新建白板
            mSendHelper.sendCreateNewPageMsg(page);

            showLoadDialog();

            OpenFileManager.openOrChangeFile(this, page.getId() + "", file.getAbsolutePath(), false, new OpenFileManager.OnFileToImageListener() {

                @Override
                public void onProgress(int progress, String checkCode) {
                    TPLog.printError("checkCode---" + checkCode + "------------------->progress->" + progress);
                }

                @Override
                public void onPageCount(int count, String checkCode) {
                    TPLog.printError("checkCode---" + checkCode + "------------------->count->" + count);
                }

                @Override
                public void onFialed() {
                    Log.e("error", "解析文件失败 0.0");
                }

                @Override
                public void onComplete(final List<String> files, final String fileName, String checkCode) {
                    if (files == null) {
                        return;
                    }
                    final IPage page = mPageManager.getSelectPage();
                    if (checkCode.equals(page.getId() + "")) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BitmapManager bmm = BitmapManager.getInstence();
                                int count = files.size();
                                int screenWidth = (int) WhiteBoardUtils.whiteBoardWidth;
                                int screnHeight = (int) WhiteBoardUtils.whiteBoardHeight;

                                //1.发送文档打开完成命令
                                mSendHelper.sendOpenOfficeComplete(mPageManager.getSelectPage().getId());
                                //2.发送文档总页数
                                mSendHelper.sendSubpageCountMsg(mPageManager.getSelectPage().getId(), count);

                                for (int i = 0; i < count; i++) {
                                    Image image = new Image();
                                    image.setFilePath(files.get(i));
                                    Point size = bmm.getImageSize(files.get(i));
                                    image.setWidth(size.x);
                                    image.setHeight(size.y);

                                    File file = new File(files.get(i));
                                    image.setFileSize(file.length());
                                    image.setFileName(file.getName());
                                    image.setId((int) WhiteBoardUtils.getId());
                                    image.setSubpageIndex(i);
                                    image.setDwCurBlock((int) file.length());

                                    int x = (screenWidth - size.x) / 2;
                                    int y = (screnHeight - size.y) / 2;

                                    String log = "screenWidth="+screenWidth+",screnHeight="+screnHeight+",imgWidth="+size.x+",imgHeight="+size.y;
                                    log = log + ",x="+x+",y="+y;
                                    TPLog.printError(log);
                                    image.setX(x);
                                    image.setY(y);

                                    //发送文件列表
                                    SynFileManager.getInstance().sendImageInfo(image);

                                    if (i == 0) {
                                        ISubPage subpage = page.getCurSubPage();
                                        subpage.setImage(image);

                                        //如果是第一张图的话，就发送文件信息和文件内容到服务器
                                        SynFileManager.getInstance().sendImage(image);

                                    } else {
                                        SubPage spage = new SubPage(image);
                                        page.addSubPage(spage);
                                    }


                                }
                                mPageManager.notifyPageChanged();
                                dismissLoadDialog();
                            }
                        });
                    }
                }
            });
        }*/

/*
        private long tabId;

        NetHandler mHandler = new NetHandler() {
            public void handleMessage(android.os.Message msg) {

                switch (msg.what) {
                    case WHAT_OSP_CONNECT://OSP连接成功发送登陆信息
                        mConnect.writeConnectMsg();
                        toastMsg("成功连接数据会议服务器");
                        break;
                    case WHAT_OSP_DISCONNECT://osp连接断开
                        toastMsg("与数据会议服务器连接已经断开");
                        break;
                    case WHAT_EXCEPTION:
                        toastMsg("与数据会议服务器进行通信时出现异常");
                        break;
                    case WHAT_LOGIN://登陆
                        toastMsg("登录数据会议服务器成功！");

                        break;
                    case WHAT_LOGIN_TIMEOUT: //登陆超时
                        toastMsg("登录数据会议服务器超时！");
                        break;
                    case WHAT_RELOGIN: //重新登陆

                        break;
                    case WHAT_CREATE_MEETING://创建会议

                        break;
                    case WHAT_JOIN_MEETING://加入会议
                        mConnect.requestSynchronous();
                        toastMsg("加入会议成功" + msg.obj);
                        break;
                    case WHAT_GRAPH_UPDATE: //接收到新的图元
                        Graph graph = (Graph) msg.obj;
                        if (graph != null) {
                            mWbHolder.requestDrawGraph(graph);
                        }
                        break;
                    case WHAT_SYNCHRONOUSE: //同步数据
                        Map<String, Object> synMap = (Map<String, Object>) msg.obj;
                        long curPageId = (Long) synMap.get("curPageId");
                        List<Page> pageList = (List<Page>) synMap.get("PageList");
                        mPageManager.setPageList(pageList);
                        mPageManager.selectPage(curPageId);

                        Image image = ((SubPage) mPageManager.getSelectPage().getCurSubPage()).getImage();
                        if (image != null && !image.isDlSuccess()) {
                            SynFileManager.getInstance().requestFileInfo(((SubPage) mPageManager.getSelectPage().getCurSubPage()).getImage());
                        }
                        IPage selectPage = mPageManager.getSelectPage();
                        mWbHolder.scale(selectPage.getCurScale());
                        mWbHolder.rotate(selectPage.getCurAngle(), false);
                        mWbHolder.translate(selectPage.getOffsetX(), selectPage.getOffsetY());
                        break;
                    case WHAT_IMAGE_DOWNLOAD: //图片下载完成
                        long dwTimeId = (Long) msg.obj;
                        mPageManager.selectCurPageSubPage(dwTimeId);
                        break;
                    case WHAT_NOTIFY_TABLE:  //这两个合在一起处理
                    case WHAT_CHANGE_PAGE:
                        Map<String, Long> map = (Map<String, Long>) msg.obj;
                        long tadId1 = map.get("tabId");
                        long pageIndex1 = map.get("pageIndex");
                        if (mPageManager.getSelectPage().getId() == tadId1) {
                            mPageManager.selectCurPageSubPage((int) pageIndex1 + 1);
                        } else {
                            mPageManager.selectPage(tadId1);
                        }
                        break;
                    case WHAT_SR_CHANGE_PAGE:
                        this.sendMessage(WHAT_CHANGE_PAGE, msg.obj);
                        mSendHelper.requestServerRecFlow(); //向服务器请求当前流量大小
                        break;
                    case WHAT_SCROLL_CHANGED:
                        Point scrollPoint = (Point) msg.obj;
                        mWbHolder.translate(scrollPoint.x, scrollPoint.y);
                        break;
                    case WHAT_SCALE_CHANGED:
                        float zoom = (Float) msg.obj;
                        mWbHolder.scale(zoom / 100f);
                        break;
                    case WHAT_ROTATE_CHANGED:
                        Map<String, Long> rotateData = (Map<String, Long>) msg.obj;
                        long angle1 = rotateData.get("angle");
                        long isFinish = rotateData.get("isFinish");
                        mWbHolder.rotate((int) angle1, isFinish == 0);
                        break;
                    case WHAT_ROTATE_LEFT:
                        int angle = mPageManager.getSelectPage().getCurAngle();
                        mWbHolder.rotate(angle - 90, true);
                        break;
                    case WHAT_ROTATE_RIGHT:
                        int angle2 = mPageManager.getSelectPage().getCurAngle();
                        mWbHolder.rotate(angle2 + 90, true);
                        break;
                    case WHAT_SC_SYNCHRONOUS:  //服务器请求数据同步
                        long dwRequestId = (Long) msg.obj;
                        mSendHelper.sendSynData(mPageManager, dwRequestId);
                        break;
                    case WHAT_CLEAR_SCREEN:
                        mWbHolder.clearScreen();
                        break;
                    case WHAT_CREATE_TAB:
                        Page page = (Page) msg.obj;
                        mPageManager.addPage(page);
                        break;
                    case WHAT_DEL_TAB:
                        String data = (String) msg.obj;
                        String datas[] = data.split("_");
                        long delTabId = Long.parseLong(datas[0]);
                        long displayTabId = Long.parseLong(datas[1]);
                        mPageManager.removePage(delTabId, displayTabId);
                        break;
//              case WHAT_TABID_TEST:
//            	  long tabId = (Long)msg.obj;
//            	  Page tpage = new Page();
//            	  tpage.setId(tabId);
//            	  tpage.setName(WhiteBoardUtils.getPageName());
//            	  SubPage subPage = new SubPage();
//            	  tpage.addSubPage(subPage);
//            	  mPageManager.addPage(tpage);
//            	  TPLog.printError("当前的tabId--------------------------->"+tabId);
//            	  break;
                    case WHAT_UNDO:
                        //暂时没有进行页面校验，后面有需要再进行
                        Map<String, Long> obj = (Map<String, Long>) msg.obj;
                        tabId = obj.get("tabId");
                        long pageIndex = obj.get("pageIndex");

                        mWbHolder.undo();
                        break;
                    case WHAT_REDO:
                        //暂时没有进行页面校验，后面有需要再进行
                        obj = (Map<String, Long>) msg.obj;
                        tabId = obj.get("tabId");
                        pageIndex = obj.get("pageIndex");
                        mWbHolder.redo();
                        break;
                    case WHAT_ADD_IMAGE:
                        SubPage subpage = (SubPage) msg.obj;
                        mPageManager.getSelectPage().addSubPage(subpage);
                        break;
                    case WHAT_BUF_SIZE:  //服务器返回的当前的流量大小   流量大小暂时没有使用  感觉没有什么意义  主要是为了响应服务请求的翻页操作
                        image = ((SubPage) mPageManager.getCurSelectSubPage()).getImage();
                        SynFileManager.getInstance().sendImage(image);
                        break;
                }
            }

            ;
        };

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
            mWbHolder.translate(page.getOffsetX(), page.getOffsetY());
            mWbHolder.scale(page.getCurScale());
            mWbHolder.rotate(page.getCurAngle(), false);

            //还原画板面板数据
            int color = saveState.getInt("Color");
            mWbHolder.setPaintColor(color);

            float width = saveState.getFloat("StrokeWidth");
            TPLog.printError("2StrokeWidth------------->"+width);
            mWbHolder.setPaintStrokeWidth(width);
        }

        protected void onSaveInstanceState(Bundle outState) {

            List<IPage> list = mPageManager.getPageList();
            int count = list.size();

            Page[] pages = new Page[count];

            for (int i = 0; i < count; i++) {
                IPage page = list.get(i);
                pages[i] = (Page) page;
            }
            outState.putParcelableArray("PAGE", pages);
            outState.putInt("SelectPageIndex", mPageManager.getSelectPageIndex());
            outState.putInt("Color", mWbHolder.getPaintColor());
            outState.putFloat("StrokeWidth", mWbHolder.getPaintStrokeWidth());
            outState.setClassLoader(getClass().getClassLoader());
            TPLog.printError("onSaveInstanceState------->数据保存完成");
        };

        protected void onDestroy() {
            super.onDestroy();
            TPLog.printError("电子白板已经退出。。。。。。。。。。。。。。。。。。。");
        }

        @Override
        public void onDCancelBtnEvent(DialogType dtype) {

        }

        @Override
        public void onDSureBtnEvent(DialogType dtype) {

        }

        @Override
        public void onDOpenDirBtnEvent(String filePath) {

        }

        @Override
        public void onDResetFileNameBtnEvent(String filePath,String name) {

        }

        @Override
        public void onDReplaceFileBtnEvent(String filePath) {

        }

        @Override
        public void onDAllSaveBtnEvent() {

        }

        @Override
        public void onDOneByOneSaveBtnEvent() {

        }

        @Override
        public void onDQuitBtnEvent() {

        }

        @Override
        public void onDPenSizeBtnEvent(int size) {

        }

        @Override
        public void onDPenColorBtnEvent(int color) {

        }

        @Override
        public void onDEraseBtnEvent() {

        }

        @Override
        public void onDAreaEraseBtnEvent() {

        }

        @Override
        public void onDClearScreenBtnEvent() {

        }

        @Override
        public float onDZoomOutBtnEvent() {
            return 0;
        }

        @Override
        public float onDZoomInBtnEvent() {
            return 0;
        }

        @Override
        public void onDRotateLeftBtnEvent() {

        }

        @Override
        public void onDRotateRightBtnEvent() {

        }

        @Override
        public void onDHeightSelfBtnEvent() {

        }

        @Override
        public void onDWidthSelfBtnEvent() {

        }

        @Override
        public void onDOneToOneBtnEvent() {

        }

        @Override
        public void onDCloseWbBtnEvent() {

        }

        @Override
        public void onDCloseWbBtnEvent(Page page) {

        }


        @Override
        public void onDSwitchDeviceSaveBtnEvent(String path) {

        }

        @Override
        public void onDSwitchBackGroundColor(int color) {

        }

        @Override
        public void onDMenuOpenFileBtnEvent() {

        }

        @Override
        public void onDMenuSaveBtnEvent() {

        }

        @Override
        public void onDMenuSendMailBtnEvent() {

        }

        @Override
        public void onDMenuScanQRBtnEvent() {

        }

        @Override
        public void onDMenuChangeBgBtnEvent() {

        }

        @Override
        public void onDMenuExitBtnEvent() {

        }

        @Override
        public void onDSaveBtnEvent(String fileName, String savePath, boolean isSaveAll) {

        }

        @Override
        public void onDSendMailBtnEvent(String mailTitle, String[] mails) {

        }

        @Override
        public void onDSwitchFile(String filePath) {

        }

        @Override
        public void onDilaogDismiss() {

        }

        ;

    }*/