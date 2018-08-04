package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.utils.ArithUtil;

/**
 * Created by zhanglei on 2016/11/16.
 */
public class MoreMenuDialog implements IControler,View.OnTouchListener{

    private Context context;

    private TPPopupWindow mWindow;

    private View contentView;

    private Button zoomInBtn;
    private Button zoomOutBtn;
    private TextView scaleTv;

    private RelativeLayout rotateLeftLy;
    private ImageView rotateLeftIv;
    private TextView rotateLeftTv;

    private RelativeLayout rotateRightLy;
    private ImageView rotateRightIv;
    private TextView rotateRightTv;

    private RelativeLayout heightSelfLy;
    private ImageView heightSelfIv;
    private TextView heightSelfTv;

    private RelativeLayout widthSelfLy;
    private ImageView widthSelfIv;
    private TextView widthSelfTv;

    private RelativeLayout oneToOneLy;
    private ImageView oneToOneIv;
    private TextView oneToOneTv;


    private int textNormalColor;
    private int textSelectColor;

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        context = null;
        mWindow = null;
        contentView = null;
        zoomInBtn = null;
        zoomOutBtn = null;
        scaleTv = null;
        rotateLeftLy = null;
        rotateLeftIv = null;
        rotateLeftTv = null;
        rotateRightLy = null;
        rotateRightIv = null;
        rotateRightTv = null;
        heightSelfLy= null;
        heightSelfIv = null;
        heightSelfTv = null;
        widthSelfLy= null;
        widthSelfIv = null;
        widthSelfTv = null;
        oneToOneLy = null;
        oneToOneIv = null;
        oneToOneTv = null;
    }

    public MoreMenuDialog(Context context){
        this.context = context;
        initView();
        initPpw();
    }

    private void initView(){

        textNormalColor = context.getResources().getColor(R.color.moremenu_text_normal);
        textSelectColor = context.getResources().getColor(R.color.moremenu_text_select);

        LayoutInflater inflater =  LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dialog_moremenu,null);

        zoomInBtn = (Button) contentView.findViewById(R.id.zoomInBtn);
        zoomOutBtn = (Button) contentView.findViewById(R.id.zoomOutBtn);
        scaleTv = (TextView) contentView.findViewById(R.id.scaleTextView);

        rotateLeftLy = (RelativeLayout) contentView.findViewById(R.id.rotateLeftLy);
        rotateLeftIv = (ImageView) contentView.findViewById(R.id.rotateLeftIv);
        rotateLeftTv = (TextView) contentView.findViewById(R.id.rotateLeftTv);

        rotateRightLy = (RelativeLayout) contentView.findViewById(R.id.rotateRightLy);
        rotateRightIv = (ImageView) contentView.findViewById(R.id.rotateRightIv);
        rotateRightTv = (TextView) contentView.findViewById(R.id.rotateRightTv);

        heightSelfLy = (RelativeLayout) contentView.findViewById(R.id.heightSelfLy);
        heightSelfIv = (ImageView) contentView.findViewById(R.id.heightSelfIv);
        heightSelfTv = (TextView) contentView.findViewById(R.id.heightSelfTv);

        widthSelfLy = (RelativeLayout) contentView.findViewById(R.id.widthSelfLy);
        widthSelfIv = (ImageView) contentView.findViewById(R.id.widthSelfIv);
        widthSelfTv = (TextView) contentView.findViewById(R.id.widthSelfTv);

        oneToOneLy = (RelativeLayout) contentView.findViewById(R.id.oneToOneLy);
        oneToOneIv = (ImageView) contentView.findViewById(R.id.oneToOneIv);
        oneToOneTv = (TextView) contentView.findViewById(R.id.oneToOneTv);


        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float curScale = ((BaseActivity)context).onDZoomInBtnEvent();
                float scale = ArithUtil.mul(curScale,100f);
                scaleTv.setText((int)scale+"%");
            }
        });

        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float curScale = ((BaseActivity)context).onDZoomOutBtnEvent();
                float scale = ArithUtil.mul(curScale,100f);
               // int scale = (int)(curScale*100f);
                scaleTv.setText((int)scale+"%");
            }
        });

        rotateLeftLy.setOnTouchListener(this);
        rotateRightLy.setOnTouchListener(this);
        heightSelfLy.setOnTouchListener(this);
        widthSelfLy.setOnTouchListener(this);
        oneToOneLy.setOnTouchListener(this);
    }

    private void initPpw(){
        mWindow = new TPPopupWindow(context);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //mWindow.setFocusable(true);
        mWindow.setContentView(contentView);
        mWindow.setOnDismissListener((BaseActivity)context);
    }

    private void btnDown(int viewId){
        switch(viewId){
            case R.id.rotateLeftLy:
                rotateLeftIv.setBackgroundResource(R.mipmap.rotateleft_select_icon);
                rotateLeftTv.setTextColor(textSelectColor);
                break;
            case R.id.rotateRightLy:
                rotateRightIv.setBackgroundResource(R.mipmap.rotateright_select_icon);
                rotateRightTv.setTextColor(textSelectColor);
                break;
            case R.id.heightSelfLy:
                heightSelfIv.setBackgroundResource(R.mipmap.heightself_select_icon);
                heightSelfTv.setTextColor(textSelectColor);
                break;
            case R.id.widthSelfLy:
                widthSelfIv.setBackgroundResource(R.mipmap.widthself_select_icon);
                widthSelfTv.setTextColor(textSelectColor);
                break;
            case R.id.oneToOneLy:
                oneToOneIv.setBackgroundResource(R.mipmap.onetoone_select_icon);
                oneToOneTv.setTextColor(textSelectColor);
                break;
        }
    }

    private void btnUp(int viewId,boolean isClick){
        switch(viewId){
            case R.id.rotateLeftLy:
                rotateLeftIv.setBackgroundResource(R.mipmap.rotateleft_normal_icon);
                rotateLeftTv.setTextColor(textNormalColor);
                if(isClick)
                    ((BaseActivity)context).onDRotateLeftBtnEvent();
                break;
            case R.id.rotateRightLy:
                rotateRightIv.setBackgroundResource(R.mipmap.rotateright_normal_icon);
                rotateRightTv.setTextColor(textNormalColor);
                if(isClick)
                    ((BaseActivity)context).onDRotateRightBtnEvent();
                break;
            case R.id.heightSelfLy:
                heightSelfIv.setBackgroundResource(R.mipmap.heightself_normal_icon);
                heightSelfTv.setTextColor(textNormalColor);
                if(isClick)
                    ((BaseActivity)context).onDHeightSelfBtnEvent();
                break;
            case R.id.widthSelfLy:
                widthSelfIv.setBackgroundResource(R.mipmap.widthself_normal_icon);
                widthSelfTv.setTextColor(textNormalColor);
                if(isClick)
                    ((BaseActivity)context).onDWidthSelfBtnEvent();
                break;
            case R.id.oneToOneLy:
                oneToOneIv.setBackgroundResource(R.mipmap.onetoone_normal_icon);
                oneToOneTv.setTextColor(textNormalColor);
                scaleTv.setText("100%");
                if(isClick)
                    ((BaseActivity)context).onDOneToOneBtnEvent();
                break;
        }
    }

    public void setCurScale(float scale){
        int curScale = (int)(scale*100f);
        scaleTv.setText(curScale+"%");
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {
    }

    public void show(View anchr){
        int width = anchr.getWidth();
        int height = anchr.getHeight();

        float bottomBarHeight = context.getResources().getDimension(R.dimen.touchdata_bottombar_height);
        float windowWidth = context.getResources().getDimension(R.dimen.moremenu_panel_width);
        float windowHeight = context.getResources().getDimension(R.dimen.moremenu_panel_height);

        int x = (int)((windowWidth - width)/2f)*-1;
        int y = (int)(windowHeight + bottomBarHeight)*-1;
        mWindow.showAsDropDown(anchr,x,y+5);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        int id = view.getId();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                btnDown(id);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                long eventTime = motionEvent.getEventTime() - motionEvent.getDownTime();
                boolean isClick = eventTime>500?false:true;
                btnUp(id,isClick);
                break;
        }
        return true;
    }
}
