package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.ResUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2016/11/29.
 */
public class SwitchBackGroundDialog implements IControler{

    private final int selectBkColor = Color.parseColor("#00aff2");

    private final int normalTextColor = Color.parseColor("#b1b1b1");

    private  int bottomBarHeight;

    private final float selectTextSize ;

    private final float normalTextSize;

    private int color[] = WhiteBoardUtils.BACKGROUNDCOLOR;

    private String colorNames[];

    private Context mContext;

    private LayoutInflater mInflater;

    private View contentView;

    private ListView colorListView;

    private TPPopupWindow mWindow;

    private int selectIndex;

    private float curThumbnailWidth;

    private float curThumbnailHeight;



    @TargetApi(Build.VERSION_CODES.M)
    public SwitchBackGroundDialog(Context context){
        colorNames = context.getResources().getStringArray(R.array.bgName);
        selectTextSize = context.getResources().getDimension(R.dimen.switch_bkc_list_item_selecttextsize);
        normalTextSize = context.getResources().getDimension(R.dimen.switch_bkc_list_item_normaltextsize);

        bottomBarHeight =(int) WhiteBoardUtils.bottomBarHeight;

        int height = (int)WhiteBoardUtils.whiteBoardHeight;

        mContext = context;
        mInflater = LayoutInflater.from(context);
        contentView = mInflater.inflate(R.layout.dialog_switchbackgroundcolor,null);
        colorListView = (ListView) contentView.findViewById(R.id.bkColorList);

        colorListView.setAdapter(new ColorAdapter());

        colorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectIndex = i;
                ((ColorAdapter)adapterView.getAdapter()).notifyDataSetChanged();
                ((BaseActivity)mContext).onDSwitchBackGroundColor(color[i]);
            }
        });

        curThumbnailWidth = WhiteBoardUtils.whiteBoardWidth/6f;
        curThumbnailHeight = WhiteBoardUtils.whiteBoardHeight/6f;

        float margineLeft = ResUtils.resToPx(R.dimen.switch_bkc_list_marginleft);
        float margineRight = ResUtils.resToPx(R.dimen.switch_bkc_list_marginright);

       int windowWidth = (int)(curThumbnailWidth + margineLeft + margineRight+4);

        mWindow = new TPPopupWindow(context);
        mWindow.setWidth(windowWidth);
        mWindow.setHeight(height);
        mWindow.setContentView(contentView);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE|WindowManager.LayoutParams.TYPE_TOAST);
        mWindow.setAnimationStyle(0);
        mWindow.setOutsideTouchable(true);
        mWindow.setOnDismissListener((BaseActivity)context);
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

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    private void preAnim(){
        contentView.setVisibility(View.GONE);
    }

    private void startAnim(){
        hand.sendEmptyMessageDelayed(100,10);
    }

    @Override
    public void show() {
        preAnim();
        mWindow.showAtLocation(contentView, Gravity.LEFT|Gravity.BOTTOM,0,0);
        hand.sendEmptyMessageDelayed(100,10);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }


    public int getCurBackgroundColor(){
        return color[selectIndex];
    }


    class ColorAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return color.length;
        }

        @Override
        public Object getItem(int i) {
            return color[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view==null){
                view = mInflater.inflate(R.layout.item_switchbackgroundcolorlist,null);
            }

            LinearLayout selectView = (LinearLayout) view.findViewById(R.id.selectView);
            ImageView colorView = (ImageView) view.findViewById(R.id.colorView);
            TextView colorNameView = (TextView) view.findViewById(R.id.colorNameView);

            colorView.getLayoutParams().width = (int)curThumbnailWidth;
            colorView.getLayoutParams().height = (int)curThumbnailHeight;

           // WhiteBoardUtils.setWbBackground(colorView,color[i]);
            if( i == 0){
                colorView.setImageResource(color[i]);
            }else{
                colorView.setImageBitmap(null);
                colorView.setBackgroundColor(color[i]);
            }

            colorNameView.setText(colorNames[i]);

            if(selectIndex == i){
                selectView.setBackgroundColor(selectBkColor);
                colorNameView.setTextSize(selectTextSize);
                colorNameView.setTextColor(selectBkColor);
            }else{
                selectView.setBackgroundColor(Color.TRANSPARENT);
                colorNameView.setTextSize(normalTextSize);
                colorNameView.setTextColor(normalTextColor);
            }
            return view;
        }
    }


    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        mContext = null;
        contentView = null;
        mWindow = null;
        color = null;
        colorNames = null;
        mInflater = null;
        colorListView = null;
    }

    boolean lockAnim = false;
    public void startEnterAnim(){
        Animation enterAnim =  AnimationUtils.loadAnimation(mContext,R.anim.page_thumbnail_dialog_enter);
        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lockAnim = false;
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

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                hand.sendEmptyMessage(102);
                lockAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentView.startAnimation(exitAnim);
    }


    Handler hand = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 100:
                    startEnterAnim();
                    break;
                case 102:
                    dismiss();
                    break;
            }
        }
    };
}
