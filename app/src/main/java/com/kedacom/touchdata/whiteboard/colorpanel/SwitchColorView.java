package com.kedacom.touchdata.whiteboard.colorpanel;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.colorpanel.entity.ColorDisplayEntity;
import com.kedacom.touchdata.whiteboard.colorpanel.entity.ColorPanelEntity;
import com.kedacom.touchdata.whiteboard.colorpanel.entity.HSVProgressEntity;
import com.kedacom.touchdata.whiteboard.colorpanel.entity.ProgressEntity;
import com.kedacom.touchdata.whiteboard.colorpanel.entity.TextEntity;
import com.kedacom.touchdata.whiteboard.colorpanel.listener.OnColorChangedListener;
import com.kedacom.touchdata.whiteboard.colorpanel.utils.ColorUtils;


public class SwitchColorView extends View {

	private static final int BACKGROUNDCOLOR = Color.BLACK;
	
	private static final int TOUCH_LOCK_SWITCH_COLOR = 0;
	
	private static final int TOUCH_LOCK_CHANGED_ALPHA = 1;
	
	private static final int TOUCH_LOCK_NORMAL = 2;

	private int padding = 10;
	
	private int width = 0;
	private int height = 0;
	
	private int switchAreaWidth = 0;
	
	private ColorPanelEntity mColorPanel;
	
	//private ProgressEntity mProgressBar;
	private HSVProgressEntity mHSVProgressEntity;
	
	private ColorDisplayEntity mColorDisplay;
	
	private TextEntity rText;
	
	private TextEntity gText;
	
	private TextEntity bText;
	
	private double density;
	
	private int color = Color.WHITE;
	private int alpha = 255;
	
	private int touchLock = TOUCH_LOCK_NORMAL;
	
	
	public SwitchColorView(Context context){
		super(context);
	}
	
	public SwitchColorView(Context context, AttributeSet attrs){
		super(context, attrs);
	}

	public SwitchColorView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		init();
	}

	private void init(){
		
		width = getWidth();
		height = getHeight();
		
		density = getResources().getDisplayMetrics().density;
		
		
		if(width==0||height == 0){
			width = 480;
			height = 300;
		}
		
		int tempWdith = (int)((float)height *1.6f) -3;
		if(tempWdith!=width){
			width = tempWdith;
			LayoutParams params = getLayoutParams();
			params.width = width;
			setLayoutParams(params);
		}
		
		
		int padding = height/23;
		switchAreaWidth = height - padding;
		
		int positionX = padding/2;
		int positionY = padding/2;

		int spacing = (int)(switchAreaWidth/13);

		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.color);
		int switchCircleRadius = (int)(7 * density);
		mColorPanel = new ColorPanelEntity();
		mColorPanel.setHeight(switchAreaWidth);
		mColorPanel.setWidth(switchAreaWidth);
		mColorPanel.setPanelBackGroundId(bitmap);
		mColorPanel.setPositionX(positionX);
		mColorPanel.setPositionY(positionY);
		mColorPanel.setSwitchCirclePositionX(10);
		mColorPanel.setSwitchCirclePositionY(10);
		mColorPanel.setSwitchCircleRadius(switchCircleRadius);


//		int positionX = (int)(10*density) + switchAreaWidth + (int)(20*density);

		 positionX = positionX  + switchAreaWidth + spacing*2;
//		int progressBarWidth = (int)(17 * density);
		int progressBarWidth = (int)(switchAreaWidth/17.6f);
		int progressBoundSize = (int)(2 * density);
		int dragBarBoundSize = (int)(2 * density);
//		int dragBarHeight = (int)(20 * density);
		int dragBarHeight = (int)(switchAreaWidth/15f);
//		int dragBarWidth = (int)(35 * density);
		int dragBarWidth = (int)(1.75f*dragBarHeight);
		
//		Shader mShader = new LinearGradient(positionX,positionY,positionX,switchAreaWidth+positionY,new int[] {Color.WHITE,Color.BLACK},null,Shader.TileMode.REPEAT);
//		mProgressBar = new ProgressEntity();
//		mProgressBar.setPositionX(positionX);
//		mProgressBar.setPositionY(positionY);
//		mProgressBar.setWidth(progressBarWidth);
//		mProgressBar.setHeight((switchAreaWidth -  progressBoundSize));
//		mProgressBar.setOrientation(ProgressEntity.VERTICAL);
//		mProgressBar.setProgressBoundColor(Color.WHITE);
//		mProgressBar.setProgressBoundSize(progressBoundSize);
//		mProgressBar.setProgressFillInColor(mShader);
//		mProgressBar.setDragBarBoundColor(Color.WHITE);
//		mProgressBar.setDragBarBoundSize(dragBarBoundSize);
//		mProgressBar.setDragBarFillInColor(Color.BLACK);
//		mProgressBar.setDragBarHeight(dragBarHeight);
//		mProgressBar.setDragBarWidth(dragBarWidth);
//		mProgressBar.setMaxProgress(255);
//		mProgressBar.setProgress(alpha);
//		mProgressBar.initDragLump(getContext());

		mHSVProgressEntity = new HSVProgressEntity(getContext(),positionX,positionY,progressBarWidth,(switchAreaWidth -  progressBoundSize));
		mHSVProgressEntity.setColor(Color.BLACK);
		mHSVProgressEntity.dragToPosition(positionX+progressBarWidth/2,positionY+(switchAreaWidth -  progressBoundSize)/2);

//		positionX = positionX +(int)(20*density) +(int)(30*density);
		positionX = positionX + spacing + dragBarWidth;
		int colorDisplaySize = (int)(switchAreaWidth / 3);
		mColorDisplay = new ColorDisplayEntity();
		mColorDisplay.setWidth(colorDisplaySize);
		mColorDisplay.setHeight(colorDisplaySize);
		mColorDisplay.setPositionX(positionX);
		mColorDisplay.setPositionY(positionY);
		mColorDisplay.setColor(color);
		
		
//		int textSize = (int)(25 *density);
		int textSize = (int)(switchAreaWidth/12f);
		
//		int positionY = 100 + 40;
		int textSpeed = (switchAreaWidth - colorDisplaySize - textSize * 3)/2;
		positionY = positionY + textSpeed + colorDisplaySize;
		rText = new TextEntity();
		rText.setPositionX(positionX);
		rText.setPositionY(positionY);
		rText.setText("R:255");
		rText.setTextSize(textSize);
		rText.setTextColor(Color.WHITE);
		
		positionY = positionY + textSpeed;
		gText = new TextEntity();
		gText = new TextEntity();
		gText.setPositionX(positionX);
		gText.setPositionY(positionY);
		gText.setText("G:255");
		gText.setTextSize(textSize);
		gText.setTextColor(Color.WHITE);
		
		positionY = positionY + textSpeed;
		bText = new TextEntity();
		bText = new TextEntity();
		bText.setPositionX(positionX);
		bText.setPositionY(positionY);
		bText.setText("B:255");
		bText.setTextSize(textSize);
		bText.setTextColor(Color.WHITE);
		
		invalidate();
	}
	
	
	public void selectColor(int color){
		this.color = color;
		HashMap<String , Integer> rgb = ColorUtils.getRgb(color);
		updateColorText(rgb);
		mColorDisplay.setColor(color);
		mHSVProgressEntity.setColor(color);
		invalidate();
	}
	
	public void selectAlpha(int alpha){
		this.alpha = alpha;
	}

	@SuppressLint("WrongCall") @Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(BACKGROUNDCOLOR);
		mColorPanel.onDraw(canvas);
		mColorDisplay.onDraw(canvas);
		rText.onDraw(canvas);
		gText.onDraw(canvas);
		bText.onDraw(canvas);
		mHSVProgressEntity.onDraw(canvas);
	}
	
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			touchLock = TOUCH_LOCK_NORMAL;
		}
		
		float x = event.getX();
		float y = event.getY();
		
		if(mColorPanel.contains((int)x, (int)y)&&(touchLock == TOUCH_LOCK_NORMAL||touchLock == TOUCH_LOCK_SWITCH_COLOR)){
			
		color = mColorPanel.getColor((int)x, (int)y);
		HashMap<String,Integer> rgb = ColorUtils.getRgb(color);
		color = ColorUtils.argb(alpha, rgb.get(ColorUtils.KEY_RED), rgb.get(ColorUtils.KEY_GREEN), rgb.get(ColorUtils.KEY_BLUE));
		mColorDisplay.setColor(color);
		mHSVProgressEntity.setColor(color);
		updateColorText(rgb);
		if(listener!=null){
			listener.onColorChanged(color);
		}
		
		touchLock = TOUCH_LOCK_SWITCH_COLOR;
		
		}if(mHSVProgressEntity.isTouch(x,y) && (touchLock == TOUCH_LOCK_NORMAL || touchLock == TOUCH_LOCK_CHANGED_ALPHA)) {
			//if (mHSVProgressEntity.contains((int) x, (int) y)) {
				//mProgressBar.setProgress((int)x, (int)y);
				color = mHSVProgressEntity.getColor(x, y);
//			    alpha = mProgressBar.getProgress();
				mHSVProgressEntity.dragToPosition((int) x, (int) y);
				HashMap<String, Integer> rgb = ColorUtils.getRgb(color);
				//color = ColorUtils.argb(alpha, rgb.get(ColorUtils.KEY_RED), rgb.get(ColorUtils.KEY_GREEN), rgb.get(ColorUtils.KEY_BLUE));
				mColorDisplay.setColor(color);
				updateColorText(rgb);
				if (listener != null) {
					listener.onColorChanged(color);
				}

				touchLock = TOUCH_LOCK_CHANGED_ALPHA;
			//}
		}
		invalidate();

		return true;
	}
	
	private void updateColorText(HashMap<String,Integer> rgb){
		if(rText==null||bText==null||gText==null) return;
		rText.setText("R:"+rgb.get(ColorUtils.KEY_RED));
		bText.setText("B:"+rgb.get(ColorUtils.KEY_BLUE));
		gText.setText("G:"+rgb.get(ColorUtils.KEY_GREEN));
	}
	
	
	private OnColorChangedListener listener;
	public void setOnColorChangedListener(OnColorChangedListener listener){
		this.listener = listener;
	}
}
