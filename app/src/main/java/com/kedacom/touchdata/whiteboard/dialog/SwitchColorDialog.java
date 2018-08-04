package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.colorpanel.SwitchColorView;
import com.kedacom.touchdata.whiteboard.colorpanel.listener.OnColorChangedListener;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.SquareLayout;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/11/14.
 */
public class SwitchColorDialog implements IControler,OnColorChangedListener ,PopupWindow.OnDismissListener{

    private final int paintSizes[];

    private Context context;

    LayoutInflater inflater;

    private TPPopupWindow mWindow;

    private View contentView;

    private View customColorBgView;
    private SwitchColorView mSwitchColorView;

    private ImageView penSize3Px;

    private ImageView penSize6Px;

    private ImageView penSize9Px;

    private GridView mColorGridView;

    private int selectColorIndex = 0;

    private int selectPenSizeIndex = 0;

    //自定义颜色选择图标
    private int customColorRes = R.mipmap.custom_color_icon;

    //颜色选择选择框颜色
    private int selectBgColor = Color.parseColor("#00aff2");
    //颜色选择颜色默认背景色
    private int normalBgColor = Color.BLACK;

    private boolean isShowing = false;


    public SwitchColorDialog(Context context){

        paintSizes = WhiteBoardUtils.PAINT_SIZE;

        this.context = context;
        inflater =  LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_color_panel,null);

        mSwitchColorView = (SwitchColorView) contentView.findViewById(R.id.customColorView);
        mSwitchColorView.setOnColorChangedListener(this);
        customColorBgView = contentView.findViewById(R.id.customColorBgView);

        penSize3Px = (ImageView) contentView.findViewById(R.id.penSize3Px);
        penSize6Px = (ImageView) contentView.findViewById(R.id.penSize6Px);
        penSize9Px = (ImageView) contentView.findViewById(R.id.penSize9Px);

        mColorGridView = (GridView) contentView.findViewById(R.id.colorGridView);
        mColorGridView.setAdapter(new ColorAdapder());
        mColorGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectColorIndex = i;
                ColorAdapder adapter = (ColorAdapder)adapterView.getAdapter();
                if(i<adapterView.getAdapter().getCount()-1) {
                   // WhiteBoardUtils.curColor = WhiteBoardUtils.colors[i];
                    ((BaseActivity)SwitchColorDialog.this.context).onDPenColorBtnEvent(WhiteBoardUtils.colors[i]);
                   dismissCustomColorView();
                }else{
                    //弹出调色板
                    displayCustomColorView();
                }
                adapter.notifyDataSetChanged();
            }
        });

        penSize3Px.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPenSize(0);
                ((BaseActivity)SwitchColorDialog.this.context).onDPenSizeBtnEvent(paintSizes[0]);
            }
        });

        penSize6Px.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPenSize(1);
                ((BaseActivity)SwitchColorDialog.this.context).onDPenSizeBtnEvent(paintSizes[1]);
            }
        });

        penSize9Px.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPenSize(2);
                ((BaseActivity)SwitchColorDialog.this.context).onDPenSizeBtnEvent(paintSizes[2]);
            }
        });

//        customColorBgView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int v = mSwitchColorView.getVisibility();
//                if(v != View.VISIBLE){
//                    mWindow.dismiss();
//                }
//            }
//        });

        mWindow = new TPPopupWindow(context);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setOutsideTouchable(true);
        //mWindow.setFocusable(true);
        mWindow.setContentView(contentView);
        mWindow.setOnDismissListener(this);

        selectPenSize(selectPenSizeIndex);
        //dismissCustomColorView();
    }

    private void displayCustomColorView(){
        mSwitchColorView.setVisibility(View.VISIBLE);
        mSwitchColorView.selectColor(WhiteBoardUtils.curColor);
        customColorBgView.setBackgroundColor(Color.BLACK);
    }

    private void dismissCustomColorView(){
        customColorBgView.setBackgroundColor(Color.TRANSPARENT);
        mSwitchColorView.setVisibility(View.GONE);
    }

    private void dismissCustomColorView2(){
        customColorBgView.setBackgroundColor(Color.TRANSPARENT);
        mSwitchColorView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean isShow() {
        return isShowing;
    }

    private void selectPenSize(int selectIndex){
        selectPenSizeIndex = selectIndex;
        penSize3Px.setBackgroundResource(R.mipmap.pen_3px_normal_icon);
        penSize6Px.setBackgroundResource(R.mipmap.pen_6px_normal_icon);
        penSize9Px.setBackgroundResource(R.mipmap.pen_9px_normal_icon);

        switch(selectIndex){
            case 0:
                penSize3Px.setBackgroundResource(R.mipmap.pen_3px_select_icon);
                break;
            case 1:
                penSize6Px.setBackgroundResource(R.mipmap.pen_6px_select_icon);
                break;
            case 2:
                penSize9Px.setBackgroundResource(R.mipmap.pen_9px_select_icon);
                break;
        }
    }

    private void checkSelectColor(){
        int colorCount = WhiteBoardUtils.colors.length;
        int localSelectColorIndex = -1;
        for(int i = 0;i<colorCount;i++){
            if(WhiteBoardUtils.curColor == WhiteBoardUtils.colors[i]){
//                selectColorIndex = i;
                localSelectColorIndex = i;
                break;
            }
        }

        if(localSelectColorIndex==-1){
            localSelectColorIndex = colorCount;
        }

        selectColorIndex = localSelectColorIndex;

        mColorGridView.setSelection(selectColorIndex);
    }

    @Override
    @Deprecated
    public void show() {
        isShowing = true;
        dismissCustomColorView2();
        mWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
    }

    public void show(View referView) {
        isShowing = true;
        checkSelectColor();
        dismissCustomColorView2();
        int width = referView.getWidth();

        int location[] = new int[2];
        referView.getLocationInWindow(location);

        float bottomBarHeight = context.getResources().getDimension(R.dimen.touchdata_bottombar_height);
        float windowWidth = context.getResources().getDimension(R.dimen.color_panel_width);

        float offsetY = bottomBarHeight;

        float offsetX = location[0] + width/2f -  windowWidth/2f;

        mWindow.showAtLocation(contentView,Gravity.BOTTOM|Gravity.LEFT,(int)offsetX,(int)offsetY);

        handler.sendEmptyMessageDelayed(1000,200);
    }

    @Override
    public void dismiss() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
    }

    @Override
    public void onColorChanged(int color) {
        ((BaseActivity)SwitchColorDialog.this.context).onDPenColorBtnEvent(color);
    }

    @Override
    public void onDismiss() {
        if(customColorBgView!=null){
            customColorBgView.setOnTouchListener(null);
        }
        ((BaseActivity)context).onDismiss();
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isShowing = false;
            }
        }.start();
    }

    class ColorAdapder extends BaseAdapter{

        private int colors[] = WhiteBoardUtils.colors;

        @Override
        public int getCount() {
            return colors.length+1;
        }

        @Override
        public Object getItem(int i) {
            if(i<getCount()-1){
                return colors[i];
            }else{
                return customColorRes;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = inflater.inflate(R.layout.item_color_gridview,null);
            }

            View selectView = view.findViewById(R.id.itemSelectView);
            ImageView colorIv = (ImageView) view.findViewById(R.id.colorlump);


            if(selectColorIndex == i){
                selectView.setBackgroundColor(selectBgColor);
            }else{
                selectView.setBackgroundColor(normalBgColor);
            }

            if(i<getCount()-1) {
                colorIv.setBackgroundColor(colors[i]);
            }else{
                colorIv.setBackgroundResource(customColorRes);
            }
            return view;
        }
    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        context = null;
        contentView = null;
        mWindow = null;
        inflater = null;
        customColorBgView = null;
        mSwitchColorView = null;
        penSize3Px = null;
        penSize6Px = null;
        penSize9Px = null;
        mColorGridView = null;
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1000){
                dismissCustomColorView();
            }
        }
    };
}
