package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class ColorDisplayEntity extends Entity {

	private float boundsSize = 2;

	private int boundsColor = Color.parseColor("#333333");

	private int positionX;
	
	private int positionY;
	
	private int width;
	
	private int height;
	
	private int color;
	
	private Paint paint;

	private RectF rect;
	
	public ColorDisplayEntity(){
		boundsSize = WhiteBoardUtils.density * boundsColor;
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(boundsSize);

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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		paint.setColor(color);
	}

	@Override
	public void onDraw(Canvas canvas) {
		 if(canvas==null)return;
		if(rect==null)
		 rect = new RectF(positionX,positionY,positionX+width,positionY+height);
		 paint.setStyle(Paint.Style.FILL);
		 paint.setColor(this.color);
         canvas.drawRect(rect, paint);
		 paint.setStyle(Paint.Style.STROKE);
		 paint.setColor(boundsColor);
		 canvas.drawRect(rect,paint);
	}

	@Override
	public boolean contains(int x, int y) {
		if(rect==null)
			rect = new RectF(positionX,positionY,positionX+width,positionY+height);
		return rect.contains(x, y);
	}

}
