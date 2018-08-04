package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class TextEntity extends Entity {
	
	private String text;
	
	private int positionX;
	
	private int positionY;
	
	private int textSize = 15;
	
	private int textColor = Color.BLACK;
	
	private Paint paint;
	
	
	public TextEntity(){
		init();
	}
	
	private void init(){
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setStyle(Paint.Style.STROKE);
	}
	

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
		paint.setTextSize(textSize);
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
		paint.setColor(textColor);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(canvas==null||text==null||text.equals(""))return;
		canvas.drawText(text, positionX, positionY, paint);
	}

	@Override
	public boolean contains(int x, int y) {
		return false;
	}

}
