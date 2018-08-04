package com.kedacom.touchdata.whiteboard.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.data.PageSetObserver;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.helper.DrawBrushPenHelper;
import com.kedacom.touchdata.whiteboard.helper.HelperHolder;
import com.kedacom.touchdata.whiteboard.helper.IHelperListener;
import com.kedacom.touchdata.whiteboard.msg.MsgQueue;
import com.kedacom.touchdata.whiteboard.msg.entity.AreaEraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.BrushPenPaintMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.DirtyRectMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.EraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.FrameMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintAndSaveGraphState;
import com.kedacom.touchdata.whiteboard.msg.entity.RefreshScreenState;
import com.kedacom.touchdata.whiteboard.msg.entity.RotateMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleAndTranslateMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.TranslateMsgState;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.op.RotateOperation;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.IUndoAndRedoListener;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.FloatViewManager;
import com.kedacom.utils.VersionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseWhiteBoardView extends SurfaceView implements Callback, IHelperListener, IUndoAndRedoListener, View.OnTouchListener {

    protected SurfaceHolder mHolder;

    protected Canvas backCanvas;

    protected Bitmap backPaintPaper;

    private DrawThread mDrawThread;

    private DealWithDrawDataThread mDealWithDrawDataThread;   //处理绘图数据

    private Rect workingRect = new Rect(0, 0, (int) WhiteBoardUtils.whiteBoardWidth, (int) WhiteBoardUtils.whiteBoardHeight);

    private AdapterDataSetObserver mDataSetObserver;

    private PageManager pageManager;

    private MsgQueue<MsgEntity> mMsgQueue = new MsgQueue<MsgEntity>();

    private MsgQueue<MsgEntity> mDealDrawMsgQueue = new MsgQueue<MsgEntity>();

    private IPage curPage;

    private IWhiteBoardStateChanagedListener mStateListener;

    private HelperHolder mHelpHolder;

    //private BaseImageView mBaseImageView;//这里保存BaseImageView对象主要是为了实现文档图片与图元的同步缩放，平移，旋转

    private Activity curActivity;   //当前活动

    private FloatViewManager mErasePanel;

    private ImageView erseTempImgView;

    private boolean lock = false;

    public BaseWhiteBoardView(Context context) {
        super(context);
        init(context);
    }

    public BaseWhiteBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseWhiteBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setActivity(Activity activity) {
        curActivity = activity;
    }

    public void setImageView(ImageView imageView) {
        erseTempImgView = imageView;
    }

    private void init(Context context) {
        setOnTouchListener(this);
        //优化绘图速度，能去的缓存都去掉，能降低的性能都降低
        setDrawingCacheEnabled(false);//设置绘图不缓存
        setBackgroundDrawable(null);//设置背景为null
        setWillNotCacheDrawing(false);//设置不缓存绘图
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_LOW);//设置绘图时的半透明质量，这里设置的时最低级的

        mHolder = getHolder();
        mHolder.addCallback(this);
        setZOrderOnTop(true);  //设置surface置顶
        mHolder.setFormat(PixelFormat.TRANSPARENT);

        backPaintPaper = Bitmap.createBitmap((int) WhiteBoardUtils.whiteBoardWidth, (int) WhiteBoardUtils.whiteBoardHeight, Bitmap.Config.ARGB_4444);
        backCanvas = new Canvas(backPaintPaper);
        backCanvas.drawColor(Color.TRANSPARENT);
        mHelpHolder = new HelperHolder(context, this);

        //mMsgQueue.addMsg(workingRect);

        View erasePanel = LayoutInflater.from(getContext()).inflate(R.layout.view_erasepanel, null);
        erasePanel.setDrawingCacheEnabled(false);
        erasePanel.setWillNotCacheDrawing(false);
        erasePanel.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        erasePanel.setBackgroundDrawable(null);

        mErasePanel = new FloatViewManager(getContext(), erasePanel);
        //mErasePanel.setFloatView(R.layout.view_erasepanel);
        mErasePanel.setFloatGravity(Gravity.LEFT | Gravity.TOP);

        //setDrawingCacheEnabled(false);
//		//开启硬件加速
        //this.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    }

    public void lock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }


    private void startHardware() {
//		if(curActivity!=null){
//			curActivity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					setLayerType(View.LAYER_TYPE_HARDWARE, null);
//					TPLog.printKeyStatus("启动硬件加速");
//				}
//			});
//		}
    }

    private void stopHardware() {
//		if(curActivity!=null){
//			curActivity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//					TPLog.printKeyStatus("关闭硬件加速");
//				}
//			});
//		}
    }

    private boolean hasHardware() {
        boolean boo = isHardwareAccelerated();
        return boo;
    }
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		if(curPage==null)return false;
//		if(curPage.isLock()){
//			return true;
//		}
//		return mHelpHolder.onTouchEvent(event);
//	}

    boolean drawEnable = true;

    @Override
    public void setDrawEnable(boolean enable) {
        drawEnable = enable;
    }

    @Override
    public Canvas getCavans() {
        return backCanvas;
    }

    @Override
    public IPage getCurPage() {
        return curPage;
    }

    @Override
    public void saveGraphEntity(Graph ge, boolean isSyn) {
        ge.changeCoordinate(curPage.getMatrix(), curPage.getCurScale());
        ge.setTabId(curPage.getId());
        ge.setRemotePageId(curPage.getRemotePageId());
        TPLog.printKeyStatus("saveGraphEntity save graphs.....");
        curPage.getCurSubPage().addGraph(ge);
        TPLog.printKeyStatus("saveGraphEntity save graphs....." + curPage.getCurSubPage().getGraphList().size());
//		curPage.drawImage(backCanvas);
        if (mStateListener != null && isSyn) {
            mStateListener.onGraphUpdate(ge);
        }
    }

    @Override
    public ArrayList<Graph> getCurGraphList() {
        ISubPage subPage = curPage.getCurSubPage();
        if (subPage == null) {
            return null;
        }
        return subPage.getGraphList();
    }

    @Override
    public ArrayList<Graph> getCurImageGraphList() {
        return curPage.getCurSubPage().getImageGraphList();
    }

    @Override
    public void requestRepaint() {
        updateUI(new MsgEntity(new DirtyRectMsgState(workingRect)));
    }

    @Override
    public void refreshScreen() {
        if (mDealDrawMsgQueue != null) {
            mDealDrawMsgQueue.addMsg(new MsgEntity(new RefreshScreenState()));
        }
    }

    @Override
    public void updateUI(MsgEntity msgEntity) {
        if (mMsgQueue != null) {
            mMsgQueue.addMsg(msgEntity);
        }
    }

    @Override
    public void requestPaint(MsgEntity paintMsgEntity) {
        TPLog.printRepeat("接收到画笔绘制消息，msgType = " + paintMsgEntity.getCurState().getType());
        if (mDealDrawMsgQueue != null) {
            TPLog.printRepeat("mDealDrawMsgQueue!=null ,添加消息到，mDealDrawMsgQueue");
            Log.e("wdt", "requestPaint: 添加消息到" + paintMsgEntity.getCurState().getType());
            mDealDrawMsgQueue.addMsg(paintMsgEntity);
        }
    }

    @Override
    @Deprecated
    public void requestDrawGraph(Graph graph) {
        if (backPaintPaper == null) return;
        Canvas canvas = mHolder.lockCanvas();
        clearCanvas(canvas);
        canvas.drawBitmap(backPaintPaper, 0, 0, null);
        graph.draw(canvas);
        mHolder.unlockCanvasAndPost(canvas);
    }

    public void requestDrawGraphAndSave(Graph graph) {
        //本来是打算全部放在处理线程内进行操作的，但是这项会引发更多的问题，因此就不放进去了
        //这里添加了一个白板锁定符号，如果当前白板有用户在触摸屏上进行操作，那么同步过来的图元必须等到
        //用户操作完毕之后才能显示出来
        TPLog.printKeyStatus("接收到同步图元~~");
        if (mDealDrawMsgQueue != null) {
            mDealDrawMsgQueue.addMsg(new MsgEntity(new PaintAndSaveGraphState(graph)));
        }
//		curPage.getCurSubPage().addGraph(graph);
//		if(lockScreen.get() == false) {
//			clearCanvas(backCanvas);
//			backCanvas.save();
//			backCanvas.setMatrix(curPage.getMatrix());
//			curPage.draw(backCanvas);
//			curPage.drawImage(backCanvas);
//			backCanvas.restore();
//			requestRepaint();
//		}
    }

    @Override
    public void onScale(float scale, boolean isSyn) {
        TPLog.printRepeat("接收到缩放消息 ,scale= " + scale + ",isSynchronization = " + isSyn);
        if (curPage == null) return;
        ScaleMsgState scaleMsgState = new ScaleMsgState(scale, true, isSyn);
        requestPaint(new MsgEntity(scaleMsgState));
    }

    @Override
    public void onRotate(int angle, boolean isComplete, boolean isSyn) {
        TPLog.printRepeat("接收到旋转消息,  angle= " + angle + ",isComplete = " + isComplete + ",isSynchronization = " + isSyn);
        RotateMsgState rotateMsgState = new RotateMsgState(angle, isComplete, isSyn);
        requestPaint(new MsgEntity(rotateMsgState));
    }

    @Override
    public void onTranslate(float ox, float oy, boolean isSyn, boolean isfinish) {
        TPLog.printRepeat("接收到平移消息,  offsetX= " + ox + ",offsetY = " + oy + ",isComplete = " + isfinish + ",isSynchronization = " + isSyn);
        if (curPage == null) return;

        TranslateMsgState state = new TranslateMsgState(ox, oy, isfinish, isSyn);
        requestPaint(new MsgEntity(state));
    }

    @Override
    public void onTransform(float scale, int angle, float offsetX, float offsetY) {
        if (curPage == null) return;
        curPage.scale(scale);
        curPage.rotate(angle, false);
        curPage.translate(offsetX, offsetY);
        reDrawAll();
        requestRepaint();
    }

    @Override
    public void onPostTransform(float scale, float spx, float spy, int angle, float offsetX, float offsetY) {
        if (curPage == null) return;
        curPage.postTranslate(offsetX, offsetY);
        curPage.postRotate(angle, false);
        curPage.postScale(scale, spx, spy);
        reDrawAll();
        requestRepaint();
    }

    @Override
    public void onUndoEnable(boolean enable) {
        if (mStateListener != null) {
            mStateListener.onUndoEnable(enable);
        }
    }

    @Override
    public void onRedoEnable(boolean enable) {
        if (mStateListener != null) {
            mStateListener.onRedoEnable(enable);
        }
    }

    /**
     * unuse
     **/
    @Override
    @Deprecated
    public void onPageChanged(int pageIndex, boolean isSyn) {

    }

    /**
     * unuse
     **/
    @Override
    @Deprecated
    public void onSubPageChanged(int index, boolean isSyn) {

    }

    /**
     * 执行撤销操作
     *
     * @param isSend 是否同步给其他客户端
     * @return IOperation 当前撤销的操作
     */
    public IOperation undo(boolean isSend) {
        TPLog.printError("undo begin...");
        IOperation op = curPage.undo();
        if (op != null && op.getType() == IOperation.OPT_ROTATE) {
            RotateOperation rop = (RotateOperation) op;
            int curAngle = rop.getCurAngle();
            //mBaseImageView.rotate(curAngle);
        }
        TPLog.printError("undo reDrawAll...");
        reDrawAll();
        TPLog.printError("undo requestRepaint...");
        requestRepaint();
        if (mStateListener != null && isSend) {
            mStateListener.onUndo();
        }

        TPLog.printError("undo end...");
        return op;
    }

    /**
     * 执行还原操作
     *
     * @param isSend 是否同步给其他客户端
     * @return IOperation 当前还原的操作
     */
    public IOperation redo(boolean isSend) {
        IOperation op = curPage.redo();
        if (op != null && op.getType() == IOperation.OPT_ROTATE) {
            RotateOperation rop = (RotateOperation) op;
            int curAngle = rop.getOldAngle();
            //mBaseImageView.rotate(curAngle);
        }
        reDrawAll();
        requestRepaint();
        if (mStateListener != null && isSend) {
            mStateListener.onRedo();
        }
        return op;
    }

    /**
     * 清屏
     *
     * @param isSyn 是否进行同步
     */
    @Override
    public void clearScreen(boolean isSyn) {
        curPage.clearAll();
        reDrawAll();
        requestRepaint();
    }

    /**
     * 获得当前的旋转角度
     *
     * @return int 当前的旋转角度
     */
    @Override
    public int getCurAngle() {
        if (curPage != null) {
            return curPage.getCurAngle();
        }
        return 0;
    }

    /**
     * 获得当前缩放级别
     *
     * @return int 当前的缩放级别
     */
    @Override
    public float getCurScale() {
        if (curPage != null) {
            return curPage.getCurScale();
        }
        return 1;
    }

    @Override
    public int getOffsetX() {
        return (int) curPage.getOffsetX();
    }

    @Override
    public int getOffsetY() {
        return (int) curPage.getOffsetY();
    }

    /**
     * 显示擦板 擦除时弹出 进行初始化操作
     *
     * @param width  板擦的宽度
     * @param height 板擦的高度
     * @param x      板擦显示x坐标
     * @param y      板擦显示的y坐标
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void displayErasePanel(int width, int height, int x, int y) {
//		Toast.makeText(getContext(),"displayErasePanel",Toast.LENGTH_SHORT).show();
        TPLog.printKeyStatus("显示擦板 ,width = " + width + ",height = " + height + ",px = " + x + ",py = " + y);
        mErasePanel.setFloatViewWidth(width);
        mErasePanel.setFloatViewHeight(height);
        mErasePanel.setPosition(x, y);
        mErasePanel.show();
    }

    /**
     * 擦板移动
     *
     * @param x 板擦移动的x坐标
     * @param y 板擦移动的y坐标
     */
    long lastErasePanelMoveTime;

    @Override
    public void erasePanelMoveTo(int x, int y) {
        long curErasePanelMoveTime = System.currentTimeMillis();
        long c = curErasePanelMoveTime - lastErasePanelMoveTime;
        lastErasePanelMoveTime = curErasePanelMoveTime;
        TPLog.printKeyStatus("移动擦板到 (" + x + "," + y + "),millis:" + c);
        mErasePanel.setPosition(x, y);
    }

    /**
     * 隐藏擦板
     */
    @Override
    public void dismissErasePanelWindow() {
        TPLog.printKeyStatus("隐藏擦板");
        if (mErasePanel != null)
            mErasePanel.hide();
    }

    @Override
    public boolean erasePanelIsShowing() {
        return mErasePanel.isShowing();
    }

    @Override
    public void widthSelf() {
        if (curPage != null) {
            curPage.imgWidthToScreenWidth();
            updateScaleUI(curPage.getCurScale());
            reDrawAll();
            requestRepaint();
        }
    }

    @Override
    public void heightSelf() {
        if (curPage != null) {
            curPage.imgHeightToScreenHeight();
            updateScaleUI(curPage.getCurScale());
            reDrawAll();
            requestRepaint();
        }
    }

    @Override
    public void selfAdaption() {
        if (curPage != null) {
            curPage.selfAdaption();
            updateScaleUI(curPage.getCurScale());
            reDrawAll();
            requestRepaint();
        }
    }

    @Override
    public void oneToOne() {
        if (curPage != null) {
            curPage.oneToOne();
            updateScaleUI(curPage.getCurScale());
            reDrawAll();
            requestRepaint();
        }
    }

    @Override
    public void updateScaleUI(float scale) {
        if (mStateListener != null)
            mStateListener.onUpdateScaleUI(scale);
    }

    @Override
    public void cancelSelectImageTimer() {
        if (mStateListener != null)
            mStateListener.cancelSelecImageTimer();
    }

    @Override
    public void onDelSelectImg(int imageId) {
        if (mStateListener != null)
            mStateListener.onDelSelectImg(imageId);
    }

    @Override
    public void onDestory() {
        TPLog.printKeyStatus("BaseWhiteBoardView Destory");
    }

    @Override
    public void onPaintDrawDown() {
        if (mStateListener != null)
            mStateListener.onPaintDrawDown();
    }

    @Override
    public void compatibilityMtAreaErase(AreaErase areaErase) {
        String remotePageId = areaErase.getRemotePageId();
        IPage page = pageManager.getPageFromRemotePageId(remotePageId);
        if (page == null) {
            return;
        }
        page.compatibilityMtAreaErase(areaErase);

        reDrawAll();
        requestRepaint();
    }

    /**
     * 获取当前白板控件，控制代理
     *
     * @return HelperHolder  白板控制代理
     */
    public HelperHolder getHelperHolder() {
        return mHelpHolder;
    }

    /**
     * 重新绘制所有图形
     */
    private void reDrawAll() {
        clearCanvas(backCanvas);
        curPage.draw(backCanvas);
        curPage.drawImage(backCanvas);
    }

    //清除Canvas上所有的图形
    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
    }

    //切换页面后进行初始化
    private void callBackPageChanged() {
        if (mStateListener == null) return;
        int angle = curPage.getCurAngle();
        int pageIndex = pageManager.getSelectPageIndex();
        int subPageIndex = pageManager.getCurPageSelectSubPageIndex();
        int subPageNum = curPage.getSubPageCount();
        float curScale = curPage.getCurScale();
        float ox = curPage.getOffsetX();
        float oy = curPage.getOffsetY();
        boolean undoEnable = curPage.undoEnable();
        boolean redoEnable = curPage.redoEnable();
        boolean locNextEnable = curPage.hasNextSubpage();
        boolean locLastEnable = curPage.hasPreSubPage();

        mStateListener.onUndoEnable(undoEnable);
        mStateListener.onRedoEnable(redoEnable);
        mStateListener.onPageChanged(pageIndex, subPageIndex, subPageNum, locNextEnable, locLastEnable);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        TPLog.printKeyStatus("BaseWhiteBoardView 初始化....");
        mDrawThread = new DrawThread();
        mDrawThread.startDrawThread();

        mDealWithDrawDataThread = new DealWithDrawDataThread();
        mDealWithDrawDataThread.startThread();

        if (curPage != null) {
            clearCanvas(backCanvas);
            curPage.draw(backCanvas);
            curPage.drawImage(backCanvas);
        }
        requestRepaint();
        TPLog.printKeyStatus("BaseWhiteBoardView 初始化完成");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        //destroy();
        if (mDrawThread != null) {
            mDrawThread.stopDrawThead();
            mDrawThread = null;
        }

        if (mDealWithDrawDataThread != null)
            mDealWithDrawDataThread.stopThread();
        mDealWithDrawDataThread = null;
        dismissErasePanelWindow();
    }

    public void setIWhiteBoardStateChanagedListener(IWhiteBoardStateChanagedListener listener) {
        mStateListener = listener;
    }

    public void setPageManager(PageManager pageManager) {
        if (pageManager != null && mDataSetObserver != null) {
            pageManager.unregisterDataSetObserver(mDataSetObserver);
        }

        this.pageManager = pageManager;

        if (this.pageManager != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            this.pageManager.registerDataSetObserver(mDataSetObserver);
        }

        int curPageCount = this.pageManager.getPageCount();

        if (curPageCount > 0) {
            curPage = this.pageManager.getPage(0);
            reDrawAll();
            curPage.setUndoAndRedoListener(this);
            callBackPageChanged();
            requestRepaint();
        }
    }

    private AtomicBoolean lockScreen = new AtomicBoolean(false);

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (curPage == null) return false;
        if (lock) {
            return true;
        }
        int action = event.getActionMasked();

        mHelpHolder.onTouchEvent(event);

        if (action == MotionEvent.ACTION_DOWN) {
            lockScreen.set(true);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            lockScreen.set(false);
        }

        return true;
    }

    class AdapterDataSetObserver extends PageSetObserver {
        @Override
        public synchronized void onPageChanged() {
            super.onPageChanged();
            TPLog.printKeyStatus("页面数据已经改变，重新加载显示");
            if (pageManager == null || mHelpHolder == null) return;
            curPage = pageManager.getSelectPage();
            if (curPage == null) {
                return;
            }
            mHelpHolder.init(curPage.getCurScale(), curPage.getCurAngle(), curPage.getOffsetX(), curPage.getOffsetY());
            if (curPage != null) {
                try {
                    reDrawAll();
                    callBackPageChanged();
                    curPage.setUndoAndRedoListener(BaseWhiteBoardView.this);
                    requestRepaint();
                } catch (Exception e) {
                    TPLog.printError("刷新页面出现异常：");
                    TPLog.printError(e);
                }
            }
        }

        @Override
        public synchronized void onSubPageChanged() {
            super.onSubPageChanged();
            if (curPage != null) {
                reDrawAll();
                callBackPageChanged();
                curPage.setUndoAndRedoListener(BaseWhiteBoardView.this);
                requestRepaint();
            }
        }
    }


    class DrawThread extends Thread {

        private boolean isRunning = false;

        @Override
        public void run() {
            TPLog.printKeyStatus("DrawThread 启动");
            while (isRunning) {
                try {
                    List<MsgEntity> localList = mMsgQueue.nextAllMsg();
                    if (!drawEnable) {
                        TPLog.printRepeat("DrawThread 接收到消息,drawEnable = " + drawEnable);
                        continue;
                    }
                    TPLog.printRepeat("DrawThread 接收到消息,msgCount = " + localList.size());

                    if (localList.isEmpty()) {
                        return;
                    }

                    switch (localList.get(0).getCurState().getType()) {
                        case TYPE_DIRTYRECT:
                            updateGraph(localList);
                            Log.e("lw", "run: zzz");
                            break;
                        case TYPE_FRAME:
                            Log.e("lw", "run: zzz22");
                            updateFrame(localList);
                            break;
                    }

                } catch (Exception localException) {
//					TPLog.printError("绘制图形出现异常:");
//					TPLog.printError(localException);
                }
            }

            TPLog.printKeyStatus("DrawThread 执行结束--死亡");
        }

        private void updateGraph(List<MsgEntity> localList) throws InterruptedException {
            TPLog.printRepeat("开始更新图形");
            long startTime = System.currentTimeMillis();
            //拿出当前所有的脏区
            int count = localList.size();
            TPLog.printRepeat("需要更新的脏矩形个数：" + count);
            List<Rect> rectList = new ArrayList<Rect>();
            for (int i = 0; i < count; i++) {
                if (localList.get(i).getCurState() instanceof DirtyRectMsgState)
                    rectList.add(((DirtyRectMsgState) localList.get(i).getCurState()).getDirtyRect());
            }
            localList.clear();
            localList = null;
            //进行脏区合并
            Rect localRect = collectDirtyAreas(rectList);

            if (localRect == null)
                return;
            //重绘脏区
            redrawDirtyArea(localRect);
            long endTime = System.currentTimeMillis();
            TPLog.printRepeat("updateGraph:更新图形结束，耗时：" + (endTime - startTime) + "ms");
        }

//		private void updateFrame(List<MsgEntity> localList){
//			MsgEntity lastMsg = localList.get(localList.size() - 1);
//			for(int i = 0 ;i<localList.size()-1;i++){
//				FrameMsgState frameMsg = (FrameMsgState)localList.get(i).getCurState();
//				frameMsg.destory();
//			}
//			synchronized (backPaintPaper) {
//				FrameMsgState frame = ((FrameMsgState) lastMsg.getCurState());
//				Bitmap bitmap = frame.getFrame();
//				Bitmap backBitmap = backPaintPaper;
//				backPaintPaper = bitmap;
//				Canvas localCanvas = mHolder.lockCanvas();
//				clearCanvas(localCanvas);
//				localCanvas.drawBitmap(backPaintPaper, 0, 0, null);
//				//curPage.drawImage(localCanvas);
//				mHolder.unlockCanvasAndPost(localCanvas);
//				backBitmap.recycle();
//				backBitmap = null;
//				backCanvas.setBitmap(backPaintPaper);
//			}
//		}

        private void updateFrame(List<MsgEntity> localList) {
            for (MsgEntity msg : localList) {
                msg = null;
            }
            localList.clear();
            localList = null;
            synchronized (backPaintPaper) {
                Canvas localCanvas = mHolder.lockCanvas(workingRect);
                clearCanvas(localCanvas);
                localCanvas.drawBitmap(backPaintPaper, workingRect, workingRect,
                        null);
                mHolder.unlockCanvasAndPost(localCanvas);
            }
        }

        private Rect collectDirtyAreas(List<Rect> paramList) {
            Rect localRect1 = new Rect();
            Iterator<Rect> localIterator = paramList.iterator();

            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;

            while (localIterator.hasNext()) {
                Rect localRect2;
                localRect2 = localIterator.next();
                localRect2.sort();
                if (localRect2 != null) {
                    if (left > localRect2.left) {
                        left = localRect2.left;
                    }
                    if (top > localRect2.top) {
                        top = localRect2.top;
                    }
                    if (right < localRect2.right) {
                        right = localRect2.right;
                    }
                    if (bottom < localRect2.bottom) {
                        bottom = localRect2.bottom;
                    }
                }
            }
            localRect1.set(left, top, right, bottom);

            TPLog.printKeyStatus("脏区合成：" + paramList.toString() + "--->" + localRect1);

            paramList.clear();
            paramList = null;
            return localRect1;
        }

        private void redrawDirtyArea(Rect paramRect)
                throws InterruptedException {
            long startTime = System.currentTimeMillis();
            TPLog.printRepeat("redrawDirtyArea->开始更新脏区");
            //synchronized (backPaintPaper) {
            Canvas localCanvas = mHolder.lockCanvas(paramRect);
            long endTime = System.currentTimeMillis();
            TPLog.printRepeat("redrawDirtyArea->获取脏区Canvas耗时:" + (endTime - startTime) + "ms");
            if (localCanvas == null) {
                TPLog.printRepeat("redrawDirtyArea->脏区Canvas获取失败，请求全屏更新");
                requestRepaint();
            } else {
//					if (paramRect.width() == WhiteBoardUtils.screenWidth && paramRect.height() == WhiteBoardUtils.screenHeight) {
//						clearCanvas(localCanvas);
//					}
                clearCanvas(localCanvas);
                synchronized (backPaintPaper) {
                    localCanvas.drawBitmap(backPaintPaper, paramRect, paramRect,
                            null);
                }
                mHolder.unlockCanvasAndPost(localCanvas);
                paramRect = null;
                endTime = System.currentTimeMillis();
                TPLog.printRepeat("redrawDirtyArea->更新脏区完成，耗时:" + (endTime - startTime) + "ms");

                //	}
            }
        }

        public void startDrawThread() {
            isRunning = true;
            this.start();
        }

        public void stopDrawThead() {
            isRunning = false;
            this.interrupt();
        }
    }

    class DealWithDrawDataThread extends Thread {

        private boolean isRunning = false;

        @Override
        public void run() {
            TPLog.printKeyStatus("DealWithDrawDataThread start...");
            while (isRunning) {
                try {
                    MsgEntity msg = mDealDrawMsgQueue.nextMsg();

                    switch (msg.getCurState().getType()) {
                        case TYPE_PAINT:
                            dealPaintMsg(msg);
                            break;
                        case TYPE_SCALE:
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> list = mDealDrawMsgQueue.nextAllMsg();
                                msg = list.get(list.size() - 1);
                            }
                            dealScaleMsg(msg);
                            break;
                        case TYPE_ROTATE:
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> list = mDealDrawMsgQueue.nextAllMsg();
                                msg = list.get(list.size() - 1);
                            }
                            dealRotateMsg(msg);
                            break;
                        case TYPE_TRANSLATE:
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> list = mDealDrawMsgQueue.nextAllMsg();
                                msg = list.get(list.size() - 1);
                            }
                            dealTranslateMsg(msg);
                            break;
                        case TYPE_ERASE:
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> list = mDealDrawMsgQueue.nextAllMsg();
                                msg = list.get(list.size() - 1);
                            }
                            dealEraseMsg(msg);
                            break;
                        case TYPE_AREA_ERASE:
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> list = mDealDrawMsgQueue.nextAllMsg();
                                msg = list.get(list.size() - 1);
                            }
                            dealAreaEraseMsg(msg);
                            break;
                        case TYPE_SCALE_TRANSLATE:
                            List<MsgEntity> list = new ArrayList<MsgEntity>();
                            list.add(msg);
                            if (mDealDrawMsgQueue.hasNext()) {
                                List<MsgEntity> temp = mDealDrawMsgQueue.nextAllMsg();
                                for (int i = 0; i < temp.size(); i++) {
                                    if (temp.get(i).getCurState().getType() == MsgState.MsgType.TYPE_SCALE_TRANSLATE) {
                                        list.add(temp.get(i));
                                    } else {
                                        mDealDrawMsgQueue.addMsg(temp.get(i));
                                    }
                                }
                            }
                            dealScaleTranslate(list);
                            break;
                        case TYPE_SELECT_GRAPH:
                            list = null;
                            if (mDealDrawMsgQueue.hasNext()) {
                                list = mDealDrawMsgQueue.nextAllMsg();
                            }
                            if (list != null) {
                                dealSelectGraphMsg(list.get(list.size() - 1));
                            } else {
                                dealSelectGraphMsg(msg);
                            }
                            break;
                        case TYPE_REFRESH:
                            list = null;
                            if (mDealDrawMsgQueue.hasNext()) {
                                list = mDealDrawMsgQueue.nextAllMsg();
                            }
                            list = null;
                            dealRefreshScreenMsg();
                            break;
                        case TYPE_SAVE_PAINT:
                            dealSaveAndPaintMsg(msg);
                            break;
                        case TYPE_BRUSH_PEN:
                            dealBrushPenPaintMsg(msg);
                            break;
                    }
                } catch (Exception e) {
                    TPLog.printError("处理绘图数据时出现异常:");
                    TPLog.printError(e);
                    e.printStackTrace();
                }
            }

            TPLog.printKeyStatus("DealWithDrawDataThread 执行结束--死亡");
        }

        private void dealBrushPenPaintMsg(MsgEntity msg) {
            TPLog.printKeyStatus("dealBrashPenPaintMsg   begin...");
            BrushPenPaintMsgState state = (BrushPenPaintMsgState) msg.getCurState();
            DrawBrushPenHelper.BrushPenSegment segment = state.getSegment();

            if (state.isComplete()) {
                TPLog.printKeyStatus("dealBrashPenPaintMsg  complete...");
                saveGraphEntity(state.getGraph(), state.isSyn());
                clearCanvas(backCanvas);
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
                msg.setCurState(new FrameMsgState());
                updateUI(msg);
            } else {
                TPLog.printKeyStatus("dealBrashPenPaintMsg not complete...");
                backCanvas.save();
                Paint paint = state.getGraph().getPaint();
                segment.draw(backCanvas, paint);
                backCanvas.restore();
                msg.setCurState(new DirtyRectMsgState(segment.getBounds()));
                updateUI(msg);
//				msg.setCurState(new FrameMsgState());
//				updateUI(msg);
            }

        }


        private void dealSaveAndPaintMsg(MsgEntity msg) {
            TPLog.printKeyStatus("处理同步到的图元~~");

            Graph graph = ((PaintAndSaveGraphState) msg.getCurState()).getGraph();
            IPage page = null;
            TPLog.printError("Mt NetUtil.isRemoteConf = " + NetUtil.isRemoteConf + ",RemotePageId=" + graph.getRemotePageId());
            if (NetUtil.isRemoteConf) {
                page = pageManager.getPageFromRemotePageId(graph.getRemotePageId());
            } else {
//				page = pageManager.getPageFromId(graph.getTabId());
                page = pageManager.getSelectPage();
            }

            if (page != null) {
                TPLog.printError("Mt page != null,1graphs count:" + page.getCurSubPage().getGraphList().size());
                page.getCurSubPage().addGraph(graph);
                TPLog.printError("Mt page != null,2graphs count:" + page.getCurSubPage().getGraphList().size());
            } else {
                TPLog.printError("Mt page == null");
            }

            TPLog.printError("Mt graph.getRemotePageId() = " + graph.getRemotePageId() + ",curPage.getRemotePageId()=" + curPage.getRemotePageId());
            if (!NetUtil.isRemoteConf) {
//				if (graph.getTabId() != curPage.getId()) {
//					TPLog.printError("Mt 不是当前显示白板。。。");
//					return;
//				}
            } else {
                if (!graph.getRemotePageId().equals(curPage.getRemotePageId())) {
                    TPLog.printError("Mt 不是当前显示白板。。。");
                    return;
                }
            }


            TPLog.printError("处理同步到的图元~~drawEnable =" + drawEnable);
            if (!drawEnable) {//终端白板同步太变态，没办法，只能在这里枷锁，返回，否则，同步白板时闪烁非常厉害
                return;
            }
//			curPage.getCurSubPage().addGraph(graph);
            TPLog.printError("处理同步到的图元~~lockScreen.get()=" + lockScreen.get());
            if (lockScreen.get() == false) {
                clearCanvas(backCanvas);
                backCanvas.save();
                backCanvas.setMatrix(curPage.getMatrix());
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
                backCanvas.restore();
                requestRepaint();
            }
        }


        private void dealRefreshScreenMsg() {
            long startMillis = System.currentTimeMillis();
            synchronized (backPaintPaper) {
                reDrawAll();
            }
            long endMillis = System.currentTimeMillis();
            TPLog.printRepeat("刷新屏幕耗时：" + (endMillis - startMillis));
            requestRepaint();
        }

        private void dealSelectGraphMsg(MsgEntity msg) {
            if (msg == null) {
                return;
            }
            long startMillis = System.currentTimeMillis();
            synchronized (backPaintPaper) {
//				SelectGraphMsgState sgms = (SelectGraphMsgState)msg.getCurState();
//				if(!sgms.isComplete()) {
//					SelectGraph mSelectGraph = sgms.getmSelectGraph();
//					mSelectGraph.draw(backCanvas);
//				}else {
                clearCanvas(backCanvas);
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
//				}
            }
            msg.setCurState(new FrameMsgState());
            updateUI(msg);
            long endMillis = System.currentTimeMillis();

            TPLog.printRepeat("选择图形数据处理总耗时:" + (endMillis - startMillis));
        }

        //缩放和拖动合起来的一个消息  2017.05.24添加
        private void dealScaleTranslate(List<MsgEntity> list) {

//			if(eraseTempBitmap!=null&&!eraseTempBitmap.isRecycled()){
//				endErase();
//			}

            long startMillis = System.currentTimeMillis();

            if (!hasHardware()) {
                startHardware();
            }

            boolean sComplete = false;
            boolean tComplete = false;

            boolean synScale = false;
            boolean synTranslate = false;

            for (MsgEntity msgEntity : list) {
                ScaleAndTranslateMsgState satms = (ScaleAndTranslateMsgState) msgEntity.getCurState();
                if (satms == null) {
                    return;
                }

                ScaleMsgState sms = satms.getmScaleMsgState();
                TranslateMsgState tms = satms.getmTranslateMsgState();

                if (sms != null) {
                    final float scale = sms.getScale();
                    final float focusX = sms.getCurFocusX();
                    final float focusY = sms.getCurFocusY();
                    final boolean isSyn = sms.isSyn();
                    synScale = isSyn;
                    sComplete = sms.isComplete();

                    curPage.postScale(scale, focusX, focusY);

                    if (curActivity != null) {
                        curActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mStateListener != null && isSyn)
                                    mStateListener.onScaleChangedFromGesture(scale, focusX, focusY);
                            }
                        });
                    }
                }

                if (tms != null) {
                    final float ox = tms.getOffsetX();
                    final float oy = tms.getOffsetY();
                    tComplete = tms.isComplete();
                    final boolean isSyn = tms.isSyn();
                    synTranslate = isSyn;
                    final boolean isComplete = tms.isComplete();
                    if (curPage == null) return;

                    curPage.postTranslate(ox, oy);

                    if (curActivity != null) {
                        curActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mBaseImageView.translate(ox, oy);
                                if (mStateListener != null && isSyn) {
                                    mStateListener.onTranslateChanged(curPage.getOffsetX(), curPage.getOffsetY(), isComplete);
                                }
                            }
                        });
                    }
                }
            }

            if (synTranslate || synScale) {
                if (curActivity != null) {
                    curActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mStateListener != null) {
                                mStateListener.onCoordinateChanged();
                            }
                        }
                    });
                }
            }

            long endtMillis = System.currentTimeMillis();
            TPLog.printRepeat("平移数据处理总耗时:" + (endtMillis - startMillis));

            synchronized (backPaintPaper) {
                clearCanvas(backCanvas);
                if (!sComplete || !tComplete) {
                    backCanvas.drawColor(Color.parseColor("#05000000"));
                } else {
                }
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
            }

            MsgEntity msgEntity = new MsgEntity();
            msgEntity.setCurState(new FrameMsgState());
            updateUI(msgEntity);

            if (sComplete && tComplete) {
                stopHardware();
                System.gc();
            }

            endtMillis = System.currentTimeMillis();
            TPLog.printRepeat("缩放平移数据处理总耗时:" + (endtMillis - startMillis));
        }

        private void dealPaintMsg(MsgEntity msgEntity) {

            long startDealTime = System.currentTimeMillis();

//			TPLog.printRepeat("TimeTest","dealPaintMsg-->开始处理铅笔绘图数据,startDealTime="+startDealTime);
            PaintMsgState pme = (PaintMsgState) msgEntity.getCurState();
            if (pme.isComplete()) {
                Log.e("wdt", "dealPaintMsg: zz00");
                pme.getGraph().setStrokeWidth(WhiteBoardUtils.curStrokeWidth);
                TPLog.printKeyStatus("还原StrokeWidth:" + WhiteBoardUtils.curStrokeWidth);
//                if (pme.isLastUp()) {
                saveGraphEntity(pme.getGraph(), true);
//                }
                synchronized (backPaintPaper) {
                    if (!VersionUtils.isImix()) {
                        clearCanvas(backCanvas);
                        curPage.draw(backCanvas);
                        curPage.drawImage(backCanvas);
                    } else {//添加了优化书写速度库后存在需要这里这么做进行规避，图元多的话安卓显示慢的问题
                        backCanvas.save();
                        backCanvas.setMatrix(curPage.getMatrix());
                        pme.getGraph().draw(backCanvas);
                        backCanvas.restore();

                    }
                }

                msgEntity.setCurState(new FrameMsgState());
                if (pme.isLastUp()) {
                    updateUI(msgEntity);
                }

            } else {
                Log.e("wdt", "dealPaintMsg: 绘制缓冲");
                Path path = pme.getPath();
                Paint paint = pme.getPaint();

                //计算重绘区域
                RectF rf = new RectF();
                path.computeBounds(rf, true);
                float w = paint.getStrokeWidth() + 3;
                rf.set(rf.left - w, rf.top - w, rf.right + w, rf.bottom + w);
                Rect rect = new Rect();
                rf.roundOut(rect);
                synchronized (backPaintPaper) {
                    if (!pme.isComplete()) {
                        backCanvas.save();
                        backCanvas.clipRect(rect);
                        //将内容绘制到缓冲区
                        backCanvas.drawPath(path, paint);
                        backCanvas.restore();
                    }
                }
                path.reset();
                path = null;
                msgEntity.setCurState(new DirtyRectMsgState(rect));
                updateUI(msgEntity);
            }

            long endDealTime = System.currentTimeMillis();
            TPLog.printRepeat("dealPaintMsg-->处理铅笔绘图数据结束,耗时：" + (endDealTime - startDealTime) + "ms");
        }

        /**
         * 擦除
         *
         * @param msgEntity
         */
        private void dealEraseMsg(MsgEntity msgEntity) {
            if (msgEntity.getCurState().getType() != MsgState.MsgType.TYPE_ERASE) {
                return;
            }

            if (eraseTempBitmap == null || eraseTempBitmap.isRecycled()) {//擦除开始
                preErase();
            }

            EraseMsgState eraseMsgEntity = (EraseMsgState) msgEntity.getCurState();
            synchronized (backPaintPaper) {
                if (!eraseMsgEntity.isComplete()) {
                    backCanvas.save();
                    eraseMsgEntity.getmGraph().draw(backCanvas);
                    backCanvas.restore();
                    msgEntity.setCurState(new FrameMsgState());
                    updateUI(msgEntity);
                } else {
                    backCanvas.save();
                    eraseMsgEntity.getmGraph().draw(backCanvas);
                    curPage.drawImage(backCanvas);
                    backCanvas.restore();
                    saveGraphEntity(eraseMsgEntity.getmGraph(), true);
                    //这里暂时只能这样子否则多线程处理出现不同步现象，出现图片闪动
                    Canvas canvas = mHolder.lockCanvas();
                    clearCanvas(canvas);
                    canvas.drawBitmap(backPaintPaper, 0, 0, null);
                    mHolder.unlockCanvasAndPost(canvas);
                    endErase();
                }
            }
        }

        /**
         * 区域擦除数据
         *
         * @param msgEntity
         */
        private void dealAreaEraseMsg(MsgEntity msgEntity) {

            if (msgEntity.getCurState().getType() != MsgState.MsgType.TYPE_AREA_ERASE) {
                return;
            }

            if (eraseTempBitmap == null || eraseTempBitmap.isRecycled()) {//擦除开始
                preErase();
            }

            AreaEraseMsgState eraseMsgEntity = (AreaEraseMsgState) msgEntity.getCurState();

            synchronized (backPaintPaper) {
                if (eraseMsgEntity.isComplete()) {
                    eraseMsgEntity.getmGraph().draw(backCanvas);
                    curPage.drawImage(backCanvas);
//				reDrawImg((AreaErase) eraseMsgEntity.getmGraph(),backCanvas);
                    saveGraphEntity(eraseMsgEntity.getmGraph(), true);
//					msgEntity.setCurState(new FrameMsgState());
//					updateUI(msgEntity);
                    //这里暂时只能这样子否则多线程处理出现不同步现象，出现图片闪动
                    Canvas canvas = mHolder.lockCanvas();
                    clearCanvas(canvas);
                    canvas.drawBitmap(backPaintPaper, 0, 0, null);
                    mHolder.unlockCanvasAndPost(canvas);
                    endErase();
                } else {
                    Canvas canvas = mHolder.lockCanvas(null);
                    clearCanvas(canvas);
                    canvas.drawBitmap(backPaintPaper, 0, 0, null);
                    eraseMsgEntity.getmGraph().draw(canvas);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        /**
         * 拖动数据处理
         *
         * @param msgEntity
         */
        private void dealTranslateMsg(MsgEntity msgEntity) {
            if (!hasHardware()) {
                startHardware();
            }
            TranslateMsgState translateMsgEntity = (TranslateMsgState) msgEntity.getCurState();

            final float ox = translateMsgEntity.getOffsetX();
            final float oy = translateMsgEntity.getOffsetY();

            final boolean isSyn = translateMsgEntity.isSyn();

            final boolean isComplete = translateMsgEntity.isComplete();

            if (curPage == null) return;
            boolean boo = curPage.translate(ox, oy);
            if (!boo) {
                return;
            }

//			curPage.translate(ox, oy);

            synchronized (backPaintPaper) {
                clearCanvas(backCanvas);
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
            }

            msgEntity.setCurState(new FrameMsgState());

            if (curActivity != null) {
                curActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mBaseImageView.translate(ox, oy);
                        if (mStateListener != null && isSyn) {
                            mStateListener.onTranslateChanged(ox, oy, isComplete);
                        }
                    }
                });
            }

            updateUI(msgEntity);

            if (isComplete) {
                stopHardware();
                System.gc();
            }

        }

        private void dealScaleMsg(MsgEntity msg) {
            if (!hasHardware()) {
                startHardware();
            }
            if (curPage == null) return;
            ScaleMsgState scaleMsgEntity = (ScaleMsgState) msg.getCurState();

            final float scale = scaleMsgEntity.getScale();
            final boolean isSyn = scaleMsgEntity.isSyn();

            final boolean isComplete = scaleMsgEntity.isComplete();

            if (curPage.getCurScale() == scale) {
                msg = null;
                return;
            }

//			curPage.scale(scale);
            curPage.scale(scale);

            synchronized (backPaintPaper) {
                clearCanvas(backCanvas);
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
            }

            msg.setCurState(new FrameMsgState());
            updateUI(msg);

            if (curActivity != null) {
                curActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mStateListener != null && isSyn)
                            mStateListener.onScaleChanged(scale);
                    }
                });
            }

            if (isComplete) {
                stopHardware();
                System.gc();
            }

        }


        private void dealRotateMsg(MsgEntity msg) {
            if (!hasHardware()) {
                startHardware();
            }
            RotateMsgState rotateMsgEntity = (RotateMsgState) msg.getCurState();
            if (curPage == null) return;

            final int angle = rotateMsgEntity.getAngle();
            final boolean isComplete = rotateMsgEntity.isComplete();
            final boolean isSyn = rotateMsgEntity.isSyn();

            curPage.rotate(angle, isComplete);
            synchronized (backPaintPaper) {
                clearCanvas(backCanvas);
                curPage.draw(backCanvas);
                curPage.drawImage(backCanvas);
            }
            msg.setCurState(new FrameMsgState());
            updateUI(msg);

            if (curActivity != null) {
                curActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mStateListener != null && isSyn)
                            mStateListener.onRotateChanged(angle, isComplete);
                    }
                });
            }

            if (isComplete) {
                mStateListener.onUndoEnable(curPage.undoEnable());
                stopHardware();
                System.gc();
            }

        }


        private synchronized void reDrawImg(Erase graph) {
            if (graph == null || graph.getGraphType() != WhiteBoardUtils.GRAPH_ERASE) {
                return;
            }

            Path path = graph.getCurAbsolutePath(curPage.getMatrix(), curPage.getCurScale());
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            Region region = new Region();
            region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));

            ArrayList<Graph> list = curPage.getCurSubPage().getImageGraphList();
            for (int i = 0; i < list.size(); i++) {
                ImageGraph ig = (ImageGraph) list.get(i);
                Rect rect = ig.getClipRectBounds();
                if (!region.quickReject(rect)) {
                    ig.draw(backCanvas);
                }
            }
        }

        private void reDrawImg(AreaErase graph, Canvas canvas) {
            Rect rect = graph.getCurAbsolutePath(curPage.getMatrix(), curPage.getCurScale());
            rect.sort();
            ArrayList<Graph> list = curPage.getCurSubPage().getImageGraphList();
            for (int i = 0; i < list.size(); i++) {
                ImageGraph ig = (ImageGraph) list.get(i);
                if (rect.intersect(ig.getClipRectBounds())) {
                    ig.draw(canvas);
                }
            }
        }


        private Bitmap createFrame() {
            Bitmap bitmap = Bitmap.createBitmap((int) WhiteBoardUtils.whiteBoardWidth, (int) WhiteBoardUtils.whiteBoardHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            curPage.draw(canvas);
            curPage.drawImage(canvas);
            canvas = null;
            return bitmap;
        }

        public void startThread() {
            isRunning = true;
            this.start();
        }

        public void stopThread() {
            isRunning = false;
            this.interrupt();
        }
    }


    public void destroy() {
        try {
            backCanvas = null;
            if (backPaintPaper != null)
                backPaintPaper.recycle();
            backPaintPaper = null;

//			if (mMsgQueue != null)
//				mMsgQueue.notifyAll();
//			if (mDealDrawMsgQueue != null)
//				mDealDrawMsgQueue.notifyAll();
            workingRect = null;
            mDataSetObserver = null;
//			if (pageManager != null)
//				pageManager.onDestroy();
//			pageManager = null;
            mMsgQueue = null;
            mDealDrawMsgQueue = null;
            curPage = null;
            if (mErasePanel != null) {
                dismissErasePanelWindow();
                mErasePanel.release();
                mErasePanel = null;
            }
        } catch (Exception e) {
            TPLog.printError("销毁WhiteBoardView 出现异常：");
            TPLog.printError(e);
        }
    }

    private Bitmap eraseTempBitmap;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void preErase() {
        TPLog.printError("----->preErase");
//		clearCanvas(backCanvas);
//		WhiteBoardUtils.drawWbBackground(backCanvas,WhiteBoardUtils.curBackground);
//		curPage.drawImage(backCanvas);
//      curPage.draw(backCanvas);
//
//		Canvas surfaceCanvas = getHolder().lockCanvas();
//		surfaceCanvas.drawBitmap(backPaintPaper,0,0,null);
//		getHolder().unlockCanvasAndPost(surfaceCanvas);


        eraseTempBitmap = Bitmap.createBitmap(workingRect.width(), workingRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(eraseTempBitmap);
        curPage.drawImage(canvas);

        curActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (erseTempImgView != null) {
                    erseTempImgView.setBackground(new BitmapDrawable(eraseTempBitmap));
                }
            }
        });

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clearCanvas(backCanvas);
        curPage.draw(backCanvas);

        //2018.01.03添加，修复在擦除时出现图片闪烁现象
        Canvas sCanvas = mHolder.lockCanvas(null);
        clearCanvas(sCanvas);
        sCanvas.drawBitmap(backPaintPaper, 0, 0, null);
        mHolder.unlockCanvasAndPost(sCanvas);
    }

    private void endErase() {
        TPLog.printError("----->endErase");

        curActivity.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {

                if (erseTempImgView != null) {
                    erseTempImgView.setBackground(null);
                    erseTempImgView.setBackgroundColor(Color.TRANSPARENT);
                }


                if (eraseTempBitmap != null && !eraseTempBitmap.isRecycled()) {
                    eraseTempBitmap.recycle();
                    eraseTempBitmap = null;
                }


            }
        });
    }

}
