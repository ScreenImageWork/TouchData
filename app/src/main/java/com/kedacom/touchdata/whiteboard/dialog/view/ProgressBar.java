package com.kedacom.touchdata.whiteboard.dialog.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kedacom.touchdata.R;


public class ProgressBar extends View{

	public static final int HORIZONTAL = 1;

	public static final int VERTICAL = 2;

	public int orientation = HORIZONTAL;

	private int max = 1;

	private int backgroundColor = Color.BLACK;

	private int foregroundColor = Color.BLUE;

	private int width;

	private int height;

	private float units;

	private int curProgress;

	private Rect progress = new Rect();

	private Paint paint;

	public ProgressBar(Context context){
		super(context);
	}

	public ProgressBar(Context context, AttributeSet attrs){
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ProgressBar);
		backgroundColor = a.getColor(R.styleable.ProgressBar_backgroundColor, Color.BLACK);
		foregroundColor = a.getColor(R.styleable.ProgressBar_foregroundColor, Color.BLUE);
		orientation = a.getInt(R.styleable.ProgressBar_orientation, HORIZONTAL);
		a.recycle();
	}


	private void init(){
		width = getWidth();
		height = getHeight();

		if(orientation == 3){
			if(width>height){
				orientation = HORIZONTAL;
			}else{
				orientation = VERTICAL;
			}
		}

		progress.set(0, 0, 0, 0);

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		if(orientation == HORIZONTAL){
			paint.setStrokeWidth(height);
		}else{
			paint.setStrokeWidth(width);
		}
		paint.setColor(foregroundColor);

		setMax(max);
	}

	public void resetUnit(){
		if(orientation == HORIZONTAL){
			units = (float)width/(float)max;
		}else{
			units = (float)height/(float)max;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if(changed){
			init();
		}
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		if(this.max<=0){
			this.max = 1;
		}
		resetUnit();
	}

	public int getProgress() {
		return curProgress;
	}

	public void setProgress(int curProgress) {
		if(curProgress>max){
			return;
		}
		this.curProgress = curProgress;
		int tempWidth = (int)((float)curProgress * units);

		if(orientation == HORIZONTAL){
			progress.set(0, 0, tempWidth, height);
		}else{
			progress.set(0, (height-tempWidth), width, height);
		}

		Log.e("error", "width="+width+",tempWidth="+tempWidth+",curProgress="+curProgress);
		invalidate();
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(int foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public void setOrientation(int orientation){

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(backgroundColor);
		canvas.drawRect(progress, paint);
	}

}
