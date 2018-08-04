package com.kedacom.touchdata.whiteboard.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Point implements Parcelable{
	
	
public int x;

public int y;

public Point() {
	
}

public Point(Parcel parcel){
	x = parcel.readInt();
	y = parcel.readInt();
}

public Point(int x,int y){
	this.x = x;
	this.y = y;
}

public void set(int x,int y){
	this.x = x;
	this.y = y;
}

public boolean equals(int x,int y){
	if(this.x == x&&this.y == y){
		return true;
	}
	return false;
}

@Override
public int describeContents() {
	return 0;
}

@Override
public void writeToParcel(Parcel arg0, int arg1) {
	arg0.writeInt(x);
	arg0.writeInt(y);
}

public static final Creator<Point> CREATOR = new Creator<Point>()
{

	@Override
	public Point createFromParcel(Parcel arg0) {
		return new Point(arg0);
	}

	@Override
	public Point[] newArray(int arg0) {
		return new Point[arg0];
	}
	
};
}
