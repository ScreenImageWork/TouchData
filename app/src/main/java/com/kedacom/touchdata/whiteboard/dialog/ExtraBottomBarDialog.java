package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2017/4/13.
 *
 * 现在所有的悬浮框都会比App界面级别高，因此在执行预览界面动画弹出的时候会重界面最底部弹出
 * 因此这里在预览界面动画弹出的时候再弹出一个显示底部工具栏的悬浮框以达到需要的效果
 */
@Deprecated
public class ExtraBottomBarDialog implements IControler {

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    private ImageView mImageView;

    private Bitmap curBitmap;

    public ExtraBottomBarDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        contentView = inflater.inflate(R.layout.dialog_extrabottombar,null);
        mImageView = (ImageView) contentView.findViewById(R.id.mBottomBarIv);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext,WindowManager.LayoutParams.TYPE_TOAST);
        mWindow.setOutsideTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setContentView(contentView);
        //mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE);
        //mWindow.setAnimationStyle(R.style.ListViewAnim);
        //mWindow.setOnDismissListener(this);
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
    }

    public void show(View view){
        if(view == null){
            return;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        if(bitmap==null){
            return;
        }

        mImageView.setImageBitmap(bitmap);

        if(curBitmap!=null&&!curBitmap.isRecycled()){
            curBitmap.recycle();
            curBitmap = null;
        }

        curBitmap = bitmap;

    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        mWindow = null;
        contentView = null;
        mImageView = null;
        if(curBitmap!=null&&!curBitmap.isRecycled()){
            curBitmap.recycle();
            curBitmap = null;
        }
    }
}
