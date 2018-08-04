package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.graphics.Canvas;

public abstract class Entity {
	
	public abstract void onDraw(Canvas canvas);

	
	public abstract boolean contains(int x,int y);
}
