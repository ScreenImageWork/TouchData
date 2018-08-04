package com.kedacom.touchdata.whiteboard.op;

import android.os.Parcel;
import android.os.Parcelable;

public class RotateOperation implements IOperation {

	private int curAngle = 0;
	
	private int oldAngle = 0;
	
	@Override
	public int getType() {
		return IOperation.OPT_ROTATE;
	}
	
	public void setAngle(int curAngle,int oldAngle){
		this.curAngle = curAngle;
		this.oldAngle = oldAngle;
	}

	public int getCurAngle() {
		return curAngle;
	}

	public void setCurAngle(int curAngle) {
		this.curAngle = curAngle;
	}

	public int getOldAngle() {
		return oldAngle;
	}

	public void setOldAngle(int oldAngle) {
		this.oldAngle = oldAngle;
	}

	public RotateOperation(){
		
	}

}
