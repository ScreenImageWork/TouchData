package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanglei on 2016/11/28.
 * 暂未使用，被PageThumbnailDialog2替代
 */
public class PageThumbnailDialog implements IControler,PopupWindow.OnDismissListener{

    final int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory() / 1024);

    private final long ANIMATION_DURATION = 300;

    private Map<Long,Bitmap> mCache = new HashMap<Long,Bitmap>(MAXMEMONRY/8);

    private Context mContext;

    private LayoutInflater inflater;

    private View contentView;

    private ListView thumbnailListView =null;

    private TPPopupWindow mWindow;

    private PageManager mPageManager;

    private int offsetY;

    private int selectColor = Color.parseColor("#00aff2");

    private int animIndex = -1;

    private  PageAdapter adapter;

    public PageThumbnailDialog(Context context){

        this.mContext = context;

        inflater =  LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.view_page_list,null);
        thumbnailListView = (ListView)contentView.findViewById(R.id.pageList);

        thumbnailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if(mPageManager==null)return;
                Page page = (Page)arg0.getAdapter().getItem(arg2);
                ((BaseActivity)mContext).onDSelectPage(page);
                ((PageAdapter)arg0.getAdapter()).notifyDataSetChanged();
            }
        });

        float width = ViewGroup.LayoutParams.WRAP_CONTENT;
        float height = WhiteBoardUtils.whiteBoardHeight;

        offsetY = (int)context.getResources().getDimension(R.dimen.touchdata_bottombar_height);

        mWindow = new TPPopupWindow(context);
        mWindow.setOutsideTouchable(true);
        //mWindow.setFocusable(true);
        mWindow.setWidth((int)width);
        mWindow.setHeight((int)height);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.ListViewAnim);
        mWindow.setOnDismissListener(this);
    }

    public void setPageManager(PageManager pm){
        mPageManager = pm;
    }

    @Override
    public boolean isShow() {
        return false;
    }

    @Override
    public void show() {
        if(mPageManager==null)return;
        List<IPage> list = mPageManager.getPageList();
        if(adapter==null) {
            adapter = new PageAdapter(list);
        }else{
            adapter.setData(list);
        }
        thumbnailListView.setAdapter(adapter);
        int index = mPageManager.getSelectPageIndex();
        thumbnailListView.setSelection(index);
//        thumbnailListView.setLayoutAnimation(listAnim);
//        thumbnailListView.startLayoutAnimation();
        mWindow.showAtLocation(contentView, Gravity.RIGHT|Gravity.BOTTOM,0,offsetY);
    }

    @Override
    public void dismiss() {
        if(mWindow==null)return;
        mWindow.dismiss();
        recycleData();
    }

    private void recycleData(){
        for(Long key:mCache.keySet()){
            Bitmap bitmap = mCache.get(key);
            if(bitmap!=null&&!bitmap.isRecycled()){
                bitmap.recycle();
            }
            bitmap = null;
        }
        mCache.clear();
    }

    private void displayThumbnail(Page page ,ImageView iv){
        Bitmap bitmap = mCache.get(page.getId());
        if(bitmap!=null){
            iv.setImageBitmap(bitmap);
            return;
        }
        bitmap = page.getPageThumbnail();
        iv.setImageBitmap(bitmap);
        mCache.put(page.getId(), bitmap);
    }

    @Override
    public void onDismiss() {
        ((BaseActivity)mContext).onDismiss();
        recycleData();
    }

    class PageAdapter extends BaseAdapter {

        private List<IPage> mPageList;

        private List<ViewHolder> viewHolders = new ArrayList<ViewHolder>();

        public PageAdapter(List<IPage> pageList){
            mPageList = pageList;
        }

        public void setData(List<IPage> list){
            mPageList = list;
        }

        public void removeViewHolder(int index){
            viewHolders.remove(index);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mPageList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            ViewHolder vh =null;

            if(viewHolders.size()>arg0){
                vh = viewHolders.get(arg0);
                arg1 = vh.view;
            }

            if(arg1==null||vh==null||vh.needInflate){
                arg1 = inflater.inflate(R.layout.item_pagelist, arg2,false);
                vh = new ViewHolder();
                vh.view = arg1;
                vh.nameTv = (TextView) arg1.findViewById(R.id.pageName);
                vh.delBtn = (ImageView) arg1.findViewById(R.id.delPageBtn);
                vh.pageThumIv = (ImageView) arg1.findViewById(R.id.pageThum);
                viewHolders.add(vh);
            }

                TextView nameTv =  vh.nameTv;
                ImageView delBut = vh.delBtn;
                ImageView pageThumIv = vh.pageThumIv;

                Page page = (Page) getItem(arg0);
                String name = page.getName();
                nameTv.setText(name);

                delBut.setOnClickListener(new DelPageOnClickListener(vh, page,arg0));

                displayThumbnail(page, pageThumIv);

                if (mPageManager.getSelectPage().getId() == page.getId()) {
                    arg1.setBackgroundColor(selectColor);
                } else {
                    arg1.setBackgroundColor(Color.TRANSPARENT);
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

        private ViewHolder vh;

        private int index = 0;

        public DelPageOnClickListener(ViewHolder vh,Page page,int index) {
            this.mPage = page;
            this.mAdapter = adapter;
            this.vh = vh;
            this.index = index;
        }

        @Override
        public void onClick(View arg0) {
//            if(adapter.getCount() > 1) {
//                vh.view.setVisibility(View.GONE);
//                thumbnailListView.removeViewAt(index);
//            }
            ((BaseActivity) mContext).onDCloseWbBtnEvent(mPage);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void destory() {
        if(!mCache.isEmpty()){
            for(Long key : mCache.keySet()){
                Bitmap bitmap = mCache.get(key);
                if(bitmap!=null&&bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }

        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }

        mContext = null;
        inflater = null;
        contentView = null;
        thumbnailListView = null;
        mWindow  = null;
        mPageManager = null;
    }


}
