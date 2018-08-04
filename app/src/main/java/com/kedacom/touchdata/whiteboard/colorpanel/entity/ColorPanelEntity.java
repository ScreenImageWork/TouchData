package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class ColorPanelEntity extends Entity{
	
	private int positionX;
	
	private int positionY;

	private int width = 300;
	
	private int height = 300;
	
	private int panelBoundsColor = Color.GRAY;
	
	private int switchCirclePositionX;
	
	private int switchCirclePositionY;
	
	private int switchCircleRadius = 7;
	
	private int switchCircleBoundsColor = Color.BLACK;
	
	private Bitmap panelBackGround;
	
	private boolean isChanged = false;
	
	private int selectColor = Integer.MIN_VALUE;
	
	
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



	public int getWidth() {
		return width;
	}



	public void setWidth(int width) {
		this.width = width;
		isChanged = false;
	}



	public int getHeight() {
		return height;
	}



	public void setHeight(int height) {
		this.height = height;
		isChanged = false;
	}



	public int getPanelBoundsColor() {
		return panelBoundsColor;
	}



	public void setPanelBoundsColor(int panelBoundsColor) {
		this.panelBoundsColor = panelBoundsColor;
	}



	public int getSwitchCircleRadius() {
		return switchCircleRadius;
	}



	public void setSwitchCircleRadius(int switchCircleRadius) {
		this.switchCircleRadius = switchCircleRadius;
	}



	public int getSwitchCircleBoundsColor() {
		return switchCircleBoundsColor;
	}



	public void setSwitchCircleBoundsColor(int switchCircleBoundsColor) {
		this.switchCircleBoundsColor = switchCircleBoundsColor;
		isChanged = false;
	}



	public Bitmap getPanelBackGroundId() {
		return panelBackGround;
	}


	public int getSwitchCirclePositionX() {
		return switchCirclePositionX;
	}

	public void setSwitchCirclePositionX(int switchCirclePositionX) {
		this.switchCirclePositionX = switchCirclePositionX;
	}

	public int getSwitchCirclePositionY() {
		return switchCirclePositionY;
	}


	public void setSwitchCirclePositionY(int switchCirclePositionY) {
		this.switchCirclePositionY = switchCirclePositionY;
	}


	public void setPanelBackGroundId(Bitmap panelBackGround) {
		this.panelBackGround = panelBackGround;

	}
	
    public void selectColor(int color){
    	selectColor = color;
    }
    
    public int getColor(int x,int y){
    	
    	x = x - positionX;
    	y = y - positionY;
    	
    	x = x<0?0:x;
    	x = x>=panelBackGround.getWidth()?panelBackGround.getWidth()-1:x;
    	
    	y = y<0?0:y;
    	y = y>=panelBackGround.getHeight()?panelBackGround.getHeight()-1:y;
    	
    	switchCirclePositionX = x + positionX;
    	switchCirclePositionY = y + positionY;
    	
    	
    	return panelBackGround.getPixel(x, y);
    }
    
    @Override
    public boolean contains(int x,int y){
    	Rect rect = new Rect(positionX,positionY,positionX+width,positionX+height);
    	return rect.contains(x, y);
    }
    
    private Paint paint;
    private Paint getPaint(){
    	if(paint!=null)return paint;
    	paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(switchCircleBoundsColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		return paint;
    }

	@SuppressLint("DrawAllocation")
    @Override
	public void onDraw(Canvas canvas) {
		changeBitmapSize();
		checkSelectPosition();
		Rect rect = new Rect(positionX,positionY,positionX+width,positionX+height);
		canvas.drawBitmap(panelBackGround, rect, rect, null);
		Paint paint = getPaint();
		canvas.drawCircle(switchCirclePositionX, switchCirclePositionY, switchCircleRadius, paint);
	}
	
	
	private void changeBitmapSize(){
		if(isChanged){
			return;
		}
		int bitmapWidth = panelBackGround.getWidth();
		int bitmapHeight = panelBackGround.getHeight();
		
		if(bitmapWidth==width||bitmapHeight==height){
			return;
		}
		
		float scale = 1.0f;
		
		scale = (float)height / (float)bitmapHeight;
		
		Matrix matrix = new Matrix(); 
		matrix.setScale(scale, scale);
		Bitmap tempBitmap = Bitmap.createBitmap(panelBackGround, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
		panelBackGround.recycle();
		panelBackGround = null;
		panelBackGround = tempBitmap;
	}
	
	private void checkSelectPosition(){
		if(selectColor == Integer.MIN_VALUE) return;
		selectColor = Integer.MIN_VALUE;
		for(int y = 0;y<height;y++){
			for(int x = 0;x<width;x++){
				int px = panelBackGround.getPixel(x, y);
				if(selectColor == px){
					switchCirclePositionX = x + positionX;
					switchCirclePositionY = y + positionY;
				}
			}
		}
		
	}
	
}
