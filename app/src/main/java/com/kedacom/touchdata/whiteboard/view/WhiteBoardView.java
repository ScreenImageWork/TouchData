package com.kedacom.touchdata.whiteboard.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.helper.HelperHolder;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class WhiteBoardView extends FrameLayout {
	
	private ImageView erseTempImgView;
	private BaseWhiteBoardView mBaseWhiteBoardView;
	
	public WhiteBoardView(Context context) {
		super(context);
		init(context);
	}
	
	public WhiteBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public WhiteBoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		erseTempImgView = new ImageView(context);
		LayoutParams params2 = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

		mBaseWhiteBoardView = new BaseWhiteBoardView(context);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		//mBaseImageView.setLayoutParams(params);
		mBaseWhiteBoardView.setLayoutParams(params);

		erseTempImgView.setLayoutParams(params2);
		erseTempImgView.setBackgroundColor(Color.TRANSPARENT);

		addView(erseTempImgView);
		addView(mBaseWhiteBoardView);
		
		//mBaseWhiteBoardView.setImageView(mBaseImageView);
		
		mBaseWhiteBoardView.setActivity((Activity)context);
		mBaseWhiteBoardView.setImageView(erseTempImgView);
		
		//setBackgroundColor(Color.parseColor("#dddddd"));
		WhiteBoardUtils.setWbBackground(this,WhiteBoardUtils.BACKGROUNDCOLOR[0]);
	}
	

	public void setPageManager(PageManager manager){
		if(mBaseWhiteBoardView!=null){
			mBaseWhiteBoardView.setPageManager(manager);
		}
	}
	
	public void setIWhiteBoardStateChanagedListener(IWhiteBoardStateChanagedListener listener){
		if(mBaseWhiteBoardView!=null){
			mBaseWhiteBoardView.setIWhiteBoardStateChanagedListener(listener);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBackgroundImage(String imagePath) {
		try {
			Drawable drawable = BitmapDrawable.createFromPath(imagePath);
			setBackground(drawable);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public HelperHolder getHelperHolder(){
		return mBaseWhiteBoardView.getHelperHolder();
	}

	public void destroy(){
		mBaseWhiteBoardView.destroy();
	}
}
