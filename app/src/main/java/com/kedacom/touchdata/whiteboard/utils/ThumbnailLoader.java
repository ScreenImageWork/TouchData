package com.kedacom.touchdata.whiteboard.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.LruCache;
import android.widget.ImageView;

import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.page.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhanglei on 2017/7/27.
 * 由于白板预览，在白板较多的情况下，会有延时较高的情况
 * 因此这里编译一个异步加载类，专门用于白板预览图片加载
 */
public class ThumbnailLoader {

    private ExecutorService executorService;//线程池，用于消息处理

    private  static ThumbnailLoader mLoader;

    private Map<String,Bitmap> mCache = new HashMap<String,Bitmap>();

    private BaseActivity activity;

    private ThumbnailLoader(){
        executorService =  Executors.newSingleThreadExecutor();
    }

    public void init(BaseActivity activity){
        this.activity = activity;
    }

    public synchronized static ThumbnailLoader getLoader(){
        if(mLoader==null){
            mLoader = new ThumbnailLoader();
        }
        return mLoader;
    }

    public void load(Page page , ImageView iv){
        if(page==null||iv==null){
            return;
        }

        if(mCache == null){
            mCache = new HashMap<String,Bitmap>();
        }

        if(executorService==null){
            executorService =  Executors.newSingleThreadExecutor();
        }
        executorService.execute(new Task(page,iv));
    }

    public void clearTask(){
        if(executorService!=null)
            executorService.shutdown();
        executorService = null;
        executorService = Executors.newSingleThreadExecutor();
    }

    public void reset(){
        if(mCache==null){
            return;
        }
        Set<String> keys = mCache.keySet();
        for(String key:keys){
            Bitmap bitmap = mCache.get(key);
            if(bitmap!=null&&!bitmap.isRecycled()){
                bitmap.recycle();
                bitmap = null;
            }
        }
        mCache.clear();
    }

    public void destory(){
        if(executorService!=null&&!executorService.isShutdown()) {
            executorService.shutdown();
            executorService = null;
        }
        reset();
        mCache = null;
    }


    class Task implements Runnable{
        private Page page;
        private ImageView iv;
        private Bitmap curBitmap;
        public Task(Page page,ImageView iv){
            this.page = page;
            this.iv = iv;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
                curBitmap = mCache.get(page.getId()+"");

                if(curBitmap == null || curBitmap.isRecycled()) {
                    if(page!=null){
                        curBitmap = page.getPageThumbnail();
                    }
                    if(curBitmap==null){
                        return;
                    }
                    mCache.put(page.getId()+"", curBitmap);
                }

                if(activity!=null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setBackground(new BitmapDrawable(curBitmap));
                        }
                    });
                }
        }
    }

}

