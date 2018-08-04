package com.kedacom.touchdata.whiteboard.op;

import android.os.Parcelable;

public interface IOperation{

	public final int OPT_GRAPH = 0;  //擦除和区域擦除都可归于图元
	
	public final int OPT_ROTATE = 1;
	
	public final int OPT_CLEAR_SCREEN = 2;

	public final int MT_AREA_ERASE = 3;
	
	int getType();
	
}
