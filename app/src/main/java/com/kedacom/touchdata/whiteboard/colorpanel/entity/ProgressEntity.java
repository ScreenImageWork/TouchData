package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

public class ProgressEntity extends Entity {

	private float otherHeight = WhiteBoardUtils.density*15; //滚动条向上延伸距离
	
	public static final int VERTICAL = 0;
	
	public static final int HORIZONTAL = 1;
	
	private int positionX;
	
	private int positionY;

	private int width;
	
	private int height;
	
	private int dragBarWidth;
	
	private int dragBarHeight;
	
	private int progressBoundSize = 2;
	
	private int progressBoundColor = Color.WHITE;
	
	private int progressFillInColor = Color.BLACK;
	
	private Shader mShader;
	
	private int dragBarBoundSize = 2;
	
	private int dragBarBoundColor = Color.WHITE;
	
	private int dragBarFillInColor = Color.BLACK;
	
	private int orientation = VERTICAL;
	
	private int maxProgress;
	
	private int progress;
	
	private float progressUnit = 1.0f;
	
	private Paint paint;

	private DragLumpEntity mDragLumpEntity;
	
	public ProgressEntity(){
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStrokeWidth(progressBoundSize);
	}
	
	public void initDragLump(Context context){
		mDragLumpEntity = new DragLumpEntity(context,width);
		mDragLumpEntity.setX(positionX);
		mDragLumpEntity.setY(positionY);
	}
	
	
	public int getWidth() {
		return width;
	}





	public void setWidth(int width) {
		this.width = width;
	}





	public int getHeight() {
		return height;
	}





	public void setHeight(int height) {
		this.height = height;
	}





	public int getDragBarWidth() {
		return dragBarWidth;
	}





	public void setDragBarWidth(int dragBarWidth) {
		this.dragBarWidth = dragBarWidth;
	}





	public int getDragBarHeight() {
		return dragBarHeight;
	}





	public void setDragBarHeight(int dragBarHeight) {
		this.dragBarHeight = dragBarHeight;
	}





	public int getProgressBoundColor() {
		return progressBoundColor;
	}





	public void setProgressBoundColor(int progressBoundColor) {
		this.progressBoundColor = progressBoundColor;
	}





	public int getProgressFillInColor() {
		return progressFillInColor;
	}





	public void setProgressFillInColor(int progressFillInColor) {
		this.progressFillInColor = progressFillInColor;
	}


	public void setProgressFillInColor(Shader shader){
		mShader = shader;
	}



	public int getDragBarBoundColor() {
		return dragBarBoundColor;
	}





	public void setDragBarBoundColor(int dragBarBoundColor) {
		this.dragBarBoundColor = dragBarBoundColor;
	}





	public int getDragBarFillInColor() {
		return dragBarFillInColor;
	}





	public void setDragBarFillInColor(int dragBarFillInColor) {
		this.dragBarFillInColor = dragBarFillInColor;
	}





	public int getMaxProgress() {
		return maxProgress;
	}





	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
		
//		progressUnit = (float)(height-dragBarHeight)/(float)maxProgress;
		progressUnit = (float)(height)/(float)maxProgress;
	}





	public int getProgress() {
		return progress;
	}





	public void setProgress(int progress) {
		this.progress = progress;
	}
	
	
	
	public void setProgress(int x,int y) {
		progress = (int)((float)(height - (positionY+height - y))/progressUnit);
		TPLog.printError("setProgress----------->"+progress);
		if(progress>255){
			progress = 255;
		}else if(progress<0){
			progress = 0;
		}
	}




	public int getProgressBoundSize() {
		return progressBoundSize;
	}





	public void setProgressBoundSize(int progressBoundSize) {
		this.progressBoundSize = progressBoundSize;
	}





	public int getDragBarBoundSize() {
		return dragBarBoundSize;
	}





	public void setDragBarBoundSize(int dragBarBoundSize) {
		this.dragBarBoundSize = dragBarBoundSize;
	}



	public int getOrientation() {
		return orientation;
	}


	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
	

	public int getPositionX() {
		return positionX;
	}




	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}




	public int getPositionY() {
		return positionY;
	}




	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	

	@Override
	public boolean contains(int x, int y) {
		 Rect touchRect = new Rect(positionX,positionY,positionX+dragBarWidth,(positionY+height));
		
		return touchRect.contains(x, y);
	}



	@SuppressLint("DrawAllocation") 
	@Override
	public void onDraw(Canvas canvas) {
		
		Rect progressRect = new Rect(positionX,positionY,positionX+width,positionY+height);
		
		int dragLeft = positionX - (dragBarWidth - width)/2;
		int dragTop = positionY + ( (int)(progressUnit * progress));
		TPLog.printError("setProgress----------->dragTop="+dragTop);
		if(mDragLumpEntity!=null)
			mDragLumpEntity.setY(dragTop);
		//Rect dragRect = new Rect(dragLeft,dragTop,dragLeft+dragBarWidth,dragTop+dragBarHeight);
		
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(progressBoundSize);
		if(mShader==null){
		    paint.setColor(progressFillInColor);
		}else{
			paint.setShader(mShader);
		}
		canvas.drawRect(progressRect, paint);
		
		paint.setShader(null);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(progressBoundSize);
		paint.setColor(progressBoundColor);
		canvas.drawRect(progressRect, paint);

		mDragLumpEntity.onDraw(canvas);
		
//		paint.setStyle(Paint.Style.FILL);
//		paint.setStrokeWidth(dragBarBoundSize);
//		paint.setColor(dragBarFillInColor);
//		canvas.drawRect(dragRect, paint);
//
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setStrokeWidth(dragBarBoundSize);
//		paint.setColor(dragBarBoundColor);
//		canvas.drawRect(dragRect, paint);
		
	}

}
