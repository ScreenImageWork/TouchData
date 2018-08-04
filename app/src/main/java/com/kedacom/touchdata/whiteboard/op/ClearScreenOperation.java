package com.kedacom.touchdata.whiteboard.op;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.graph.Graph;

public class ClearScreenOperation implements IOperation {

	private ArrayList<Graph> list = new ArrayList<Graph>();
	private ArrayList<Graph> imgList = new ArrayList<Graph>();
	
	
	public ClearScreenOperation(){
		
	}

	@Override
	public int getType() {
		return IOperation.OPT_CLEAR_SCREEN;
	}
	
	public void addOldGraphList(ArrayList<Graph> list){
		this.list = list;
	}

	public void addOldImgGraphList(ArrayList<Graph> imgList){
		this.imgList = imgList;
	}
	
	public ArrayList<Graph> getGraphList(){
		return list;
	}

	public  ArrayList<Graph> getImgGraphList(){
		return imgList;
	}
}
