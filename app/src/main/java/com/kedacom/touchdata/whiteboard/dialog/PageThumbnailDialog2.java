package com.kedacom.touchdata.whiteboard.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.ThumbnailLoader;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.DelAllWbBtn;
import com.kedacom.touchdata.whiteboard.view.rmanimlistview.AnimListView;
import com.kedacom.touchdata.whiteboard.view.rmanimlistview.BaseAnimListAdapter;
import com.kedacom.touchdata.whiteboard.view.rmanimlistview.IAnimListAdapter;
import com.kedacom.touchdata.whiteboard.view.rmanimlistview.IAnimListItemClickListener;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanglei on 2017/02/06.
 */
public class PageThumbnailDialog2 implements IControler,PopupWindow.OnDismissListener{

    public static final int  HANDER_WHAT_NOTIFY_ADAPTER = 99;

    public static final int  HANDER_WHAT_START_ENTERANIM = 100;

    public static final int  HANDER_WHAT_WINDOW_DISMISS = 101;

    public static final int HANDER_WHAT_DELALLBTN_PRESS = 102;

    private final static long ADAPTER_REMOVE_TO_NOTIFY_SPEED = 600;

    private Context mContext;

    private LayoutInflater inflater;

    private View contentView;

    private AnimListView thumbnailListView =null;

    private LinearLayout delAllWbBtn = null;
    private ImageView delAllWbBtnIcon = null;
    private TextView delAllWbBtnText = null;

    private RelativeLayout isDelAllLayout;
    private Button delAllSureBtn;
    private Button delAllCancelBtn;

    LayoutTransition mLayoutTransition;

    private LinearLayout delBar;

    private DelAllWbBtn newDelAllBtn;

    private TPPopupWindow mWindow;

    private PageManager mPageManager;

    private int offsetY;

    private int selectColor = Color.parseColor("#00aff2");

    private  PageAdapter adapter;

    private float curThumbnailWidth;

    private float curThumbnailHeight;

    private boolean lockAnim;

    private boolean isShowing = false;

    @TargetApi(Build.VERSION_CODES.M)
    public PageThumbnailDialog2(Context context){

        this.mContext = context;

        ThumbnailLoader.getLoader().init((BaseActivity)context);

         mLayoutTransition = new LayoutTransition();

//        //通过加载XML动画设置文件来创建一个Animation对象；
//        itemAnimation = AnimationUtils.loadAnimation(context, R.anim.list_item_anim);
//        //得到一个LayoutAnimationController对象；
//        listAnim = new LayoutAnimationController(itemAnimation);
//        listAnim.setDelay(0);

        inflater =  LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_pagethumbnail,null);
        thumbnailListView = (AnimListView)contentView.findViewById(R.id.pageList);
        delAllWbBtn = (LinearLayout) contentView.findViewById(R.id.delAllWbBtn);
        delAllWbBtnIcon = (ImageView) contentView.findViewById(R.id.delAllWbBtnIcon);
        delAllWbBtnText = (TextView) contentView.findViewById(R.id.delAllWbBtnText);

        isDelAllLayout = (RelativeLayout) contentView.findViewById(R.id.isDelAllWbLayout);
         delAllSureBtn = (Button) contentView.findViewById(R.id.isDelAllWbLayoutSureBtn);
         delAllCancelBtn = (Button) contentView.findViewById(R.id.isDelAllWbLayoutCancelBtn);

        delBar = (LinearLayout) contentView.findViewById(R.id.delBar);
        delBar.setLayoutTransition(mLayoutTransition);

        newDelAllBtn = (DelAllWbBtn)contentView.findViewById(R.id.delAllBtn);
//        setAnim();

        thumbnailListView.setOnItemClickListener(new IAnimListItemClickListener() {
            @Override
            public void onItemClick(int index, View itemView, IAnimListAdapter madapter) {
                if(mPageManager==null)return;
                if(adapter.getCount()<=index){//异常情况，重新更新Adapter
                    adapter.setData(mPageManager.getPageList());
                    return;
                }
                Page page = (Page)adapter.getItem(index);

                if(page.getId() == mPageManager.getSelectPage().getId()){
                    return;
                }

                ((BaseActivity)mContext).onDSelectPage(page);
//                adapter.notifyAnimListChanged();
                 adapter.select(index);//防止闪烁，才加了这个函数
                //unDisplayPageList();
            }
        });

        delAllWbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delAllWbBtnIcon.setBackgroundResource(R.mipmap.del_down_icon);
                delAllWbBtnText.setTextColor(mContext.getResources().getColor(R.color.edit_select_color));
                hand.sendEmptyMessageDelayed(HANDER_WHAT_DELALLBTN_PRESS,200);
            }
        });

        delAllSureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPageManager.clearAll();
                ((BaseActivity)mContext).onDCloseAllWbBtnEvent();
//                dismiss();
                startExitAnim();
            }
        });

        newDelAllBtn.setOnSelectListener(new DelAllWbBtn.OnSelectListener() {
            @Override
            public void onSelect(boolean select) {
                if(select){
                    newDelAllBtn.setSelect(false);
                    ((BaseActivity)mContext).onDCloseAllWbBtnEvent();
                    startExitAnim();
                }
            }
        });

        delAllCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDelBarDelAllPageMode();
            }
        });

//        float listMarginLeft = mContext.getResources().getDimension(R.dimen.pagelist_marginleft);
//        float listMarginRight = mContext.getResources().getDimension(R.dimen.pagelist_marginright);

        curThumbnailWidth = WhiteBoardUtils.whiteBoardWidth/6f;
        curThumbnailHeight = WhiteBoardUtils.whiteBoardHeight/6f;

        isDelAllLayout.getLayoutParams().width = (int)curThumbnailWidth;

        float width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        float width = WhiteBoardUtils.whiteBoardWidth/4.5f + listMarginLeft + listMarginRight+10;
        float height = WhiteBoardUtils.whiteBoardHeight;

        offsetY = (int)context.getResources().getDimension(R.dimen.touchdata_bottombar_height);

        mWindow = new TPPopupWindow(context);
        //mWindow.setOutsideTouchable(true);
        //mWindow.setFocusable(true);
        mWindow.setWidth((int)width);
        mWindow.setHeight((int)height);
        mWindow.setContentView(contentView);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
        mWindow.setAnimationStyle(0);
        mWindow.setOnDismissListener(this);
        mWindow.setOutsideTouchable(true);
        mWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE){
                    if(lockAnim){
                        return true;
                    }
                    startExitAnim();
                    return true;
                }
                return false;
            }
        });

    }

    public void setPageManager(PageManager pm){
        mPageManager = pm;
    }


    private void setDelAllWbBtnEnable(boolean enable){
        int normalTextColor = mContext.getResources().getColor(R.color.dialog_pagethumbnail_deleteBar_textNormalColor);
        int disableTextColor = mContext.getResources().getColor(R.color.dialog_pagethumbnail_deleteBar_textDisableColor);
        if(enable){
            delAllWbBtnIcon.setBackgroundResource(R.mipmap.del_normal_icon);
            delAllWbBtnText.setTextColor(normalTextColor);
        }else{
            delAllWbBtnIcon.setBackgroundResource(R.mipmap.del_disable_icon);
            delAllWbBtnText.setTextColor(disableTextColor);
        }
        delAllWbBtn.setEnabled(enable);

        newDelAllBtn.setEnable(enable);
    }

    private void showDelBarIsSureDelAllPageMode(){
        delAllWbBtnIcon.setBackgroundResource(R.mipmap.del_normal_icon);
        delAllWbBtnText.setTextColor(mContext.getResources().getColor(R.color.dialog_pagethumbnail_deleteBar_textNormalColor));
        isDelAllLayout.setVisibility(View.VISIBLE);
        delAllWbBtn.setVisibility(View.GONE);
    }

    private void showDelBarDelAllPageMode(){
        delAllWbBtn.setVisibility(View.VISIBLE);
        isDelAllLayout.setVisibility(View.GONE);
    }

    private void setAnim(){
//        /**
//         * view出现时 view自身的动画效果
//         */
//        ObjectAnimator animator1 = ObjectAnimator.ofFloat(null, "translationY", 1F, 0F).
//                setDuration(mLayoutTransition.getDuration(LayoutTransition.APPEARING));
//        mLayoutTransition.setAnimator(LayoutTransition.APPEARING, animator1);
//
//        /**
//         * view 消失时，view自身的动画效果
//         */
//        ObjectAnimator animator2 = ObjectAnimator.ofFloat(null, "translationY", 0F, -1F).
//                setDuration(mLayoutTransition.getDuration(LayoutTransition.DISAPPEARING));
//        mLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, animator2);
//
//        /**
//         * view 动画改变时，布局中的每个子view动画的时间间隔
//         */
//        mLayoutTransition.setStagger(LayoutTransition.CHANGE_APPEARING, 200);
//        mLayoutTransition.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 200);

    }


    public void notifyDataChanged(){
        if(!mWindow.isShowing()){
            return;
        }

        if(adapter==null){
            return;
        }
        TPLog.printError("mPageManager.getPageList().size="+mPageManager.getPageList().size());
        adapter.setData(mPageManager.getPageList());
        adapter.notifyAnimListChanged();
//        if(thumbnailListView!=null){
//            thumbnailListView.onAnimListDataChanged();
//        }
    }

    @Override
    public boolean isShow() {
        return isShowing;
    }

    @Override
    public void show() {
        if(mPageManager==null)return;

//        if(isShow()&&lockAnim){
//            return;
//        }
        if(isShow()){
            return;
        }
        lockAnim = false;

        isShowing  = true;
//        lockAnim = true;
        contentView.setVisibility(View.GONE);

        List<IPage> list = mPageManager.getPageList();
        if(adapter==null) {
            adapter = new PageAdapter(list);
        }else{
            adapter.setData(list);
        }
        thumbnailListView.setAdapter(adapter);
//        thumbnailListView.scrollToCurSelectItem();
//        int index = mPageManager.getSelectPageIndex();
        //thumbnailListView.setSelection(index);
//        thumbnailListView.setLayoutAnimation(listAnim);
//        thumbnailListView.startLayoutAnimation();
//        mWindow.showAtLocation(contentView, Gravity.RIGHT|Gravity.BOTTOM,0,offsetY);
        mWindow.showAtLocation(contentView, Gravity.RIGHT|Gravity.BOTTOM,0,0);

        hand.sendEmptyMessageDelayed(HANDER_WHAT_START_ENTERANIM,10);

        newDelAllBtn.setSelect(false);
    }

    @Deprecated
    public void show(View view) {
//        if(isShow()&&lockAnim){
//            return;
//        }

        if(isShow()){
            return;
        }

        lockAnim = false;

        isShowing = true;

        if(mPageManager==null)return;
//        lockAnim = true;
        contentView.setVisibility(View.GONE);

        List<IPage> list = mPageManager.getPageList();
        if(adapter==null) {
            adapter = new PageAdapter(list);
        }else{
            adapter.setData(list);
        }

        thumbnailListView.setAdapter(adapter);
        mWindow.showAtLocation(contentView, Gravity.RIGHT|Gravity.BOTTOM,0,offsetY);

        hand.sendEmptyMessageDelayed(HANDER_WHAT_START_ENTERANIM,10);

    }

    @Override
    public void dismiss() {
        if(mWindow==null)return;
        mWindow.dismiss();
        recycleData();
    }

    private void recycleData(){
        ThumbnailLoader.getLoader().reset();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void displayThumbnail(Page page , ImageView iv){
//        iv.setBackgroundColor(page.getBackGroundColor());
        WhiteBoardUtils.setWbBackground(iv,page.getBackGroundColor());
        ThumbnailLoader.getLoader().load(page,iv);
    }

    @Override
    public void onDismiss() {
//        lockAnim = false;
        ((BaseActivity)mContext).onDismiss();
        recycleData();
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isShowing = false;
            }
        }.start();
    }

    class PageAdapter extends BaseAnimListAdapter {

        private List<IPage> mPageList;

        private List<ViewHolder> viewHolders = new ArrayList<ViewHolder>();

        private boolean select = false;

        public PageAdapter(List<IPage> pageList){
            mPageList = pageList;
        }

        public void setData(List<IPage> list){
            mPageList = list;
//            notifyAnimListChanged();
        }

        public void removeViewHolder(int index){
            viewHolders.remove(index);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        public int getSelectItemIndex(){
            return mPageManager.getSelectPageIndex();
        }

        @Override
        public void select(int index) {
            select = true;
            notifyAnimListChanged();
        }

        @Override
        public Object getItem(int arg0) {
            return mPageList.get(arg0);
        }

        @Override
        public void removeItem(int index) {
           // mPageList.remove(index);
            if(mPageList.size()<=index){
                return;
            }
            ((BaseActivity) mContext).onDCloseWbBtnEvent(mPageList.get(index));
           hand.sendEmptyMessageDelayed(HANDER_WHAT_NOTIFY_ADAPTER,ADAPTER_REMOVE_TO_NOTIFY_SPEED);//延时刷新ListView,否则会与动画冲突
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            if(arg1==null)
                arg1 = inflater.inflate(R.layout.item_pagelist, arg2,false);

            TextView nameTv =  (TextView) arg1.findViewById(R.id.pageName);
            ImageView delBut = (ImageView) arg1.findViewById(R.id.delPageBtn);
            ImageView pageThumIv = (ImageView) arg1.findViewById(R.id.pageThum);
            View contentView = arg1.findViewById(R.id.contentView);

            View titleLayout = arg1.findViewById(R.id.titleLayout);
            Page page = (Page) getItem(arg0);

            pageThumIv.getLayoutParams().width = (int) curThumbnailWidth;
            pageThumIv.getLayoutParams().height = (int) curThumbnailHeight;
            titleLayout.getLayoutParams().width = (int) curThumbnailWidth;

            if(!select) {  //不用选择的时候重新设置数据

                String name = page.getName();
                nameTv.setText("白板" + name);

                delBut.setOnClickListener(new DelPageOnClickListener(arg1, page, arg0));

                displayThumbnail(page, pageThumIv);
            }

            if (mPageManager.getSelectPage().getId() == page.getId()) {
                contentView.setBackgroundColor(selectColor);
            } else {
                contentView.setBackgroundColor(Color.TRANSPARENT);
            }

            TPLog.printKeyStatus("arg0 = "+arg0+",getCount() = "+getCount()+",select = "+select);
            if(arg0 == getCount()-1&&select){
                select = false;
            }

            return arg1;
        }
    }

    private  class ViewHolder{
        public boolean needInflate;
        public View view;
        public TextView nameTv;
        public ImageView delBtn;
        public ImageView pageThumIv;
    }

    class DelPageOnClickListener implements View.OnClickListener {

        private Page mPage;

        private PageAdapter mAdapter;

        private View view;

        private int index = 0;

        public DelPageOnClickListener(View view,Page page,int index) {
            this.mPage = page;
            this.mAdapter = adapter;
            this.index = index;
            this.view = view;
        }

        @Override
        public void onClick(View arg0) {
            //animIndex = delIndex;
            int count = adapter.getCount();
            TPLog.printError("thumbnailListView count------------------>"+count);
            if(count > 1) {
                thumbnailListView.startItemAnim(view);
            }else{
                ((BaseActivity) mContext).onDCloseWbBtnEvent(mPage);
                startExitAnim();
            }
        }
    }

    @Override
    public void destory() {
//        if(!mCache.isEmpty()){
//            for(Long key : mCache.keySet()){
//                Bitmap bitmap = mCache.get(key);
//                if(bitmap!=null&&bitmap.isRecycled()){
//                    bitmap.recycle();
//                    bitmap = null;
//                }
//            }
//        }

        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }

        mContext = null;
        inflater = null;
        contentView = null;
        thumbnailListView = null;
        mWindow  = null;
        mPageManager = null;

        ThumbnailLoader.getLoader().destory();
    }



    public void startEnterAnim(){
        Animation enterAnim =  AnimationUtils.loadAnimation(mContext,R.anim.page_thumbnail_dialog_enter);
        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                lockAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentView.startAnimation(enterAnim);
        contentView.setVisibility(View.VISIBLE);
    }

    public void startExitAnim(){
        lockAnim = true;
        Animation exitAnim =  AnimationUtils.loadAnimation(mContext,R.anim.page_thumbnail_dialog_exit);
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                lockAnim = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hand.sendEmptyMessage(HANDER_WHAT_WINDOW_DISMISS);
//                lockAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentView.startAnimation(exitAnim);
    }

    private boolean isHaveNeedSavePage(){
        return mPageManager.isNeedSave();
    }

    Handler hand = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case HANDER_WHAT_NOTIFY_ADAPTER:
                    adapter.notifyAnimListChanged();
                    break;
                case HANDER_WHAT_START_ENTERANIM:
                    startEnterAnim();
                    showDelBarDelAllPageMode();
                    if(mPageManager.getPageCount()!=1||isHaveNeedSavePage()){
                        setDelAllWbBtnEnable(true);
                    }else{
                        setDelAllWbBtnEnable(false);
                    }
                    break;
                case HANDER_WHAT_WINDOW_DISMISS:
                    dismiss();
                    break;
                case HANDER_WHAT_DELALLBTN_PRESS:
                    if(isHaveNeedSavePage()){//更换界面是否删除全部
                        showDelBarIsSureDelAllPageMode();
                    }else{//直接删除全部
                        ((BaseActivity)mContext).onDCloseAllWbBtnEvent();
//                    dismiss();
                        startExitAnim();
                    }
                    break;
            }
        }
    };



}
