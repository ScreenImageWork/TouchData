package com.kedacom.touchdata.whiteboard.view.rmanimlistview;

import android.view.View;
import android.view.ViewGroup;

public abstract interface IAnimListAdapter {
	
	int getCount();
	
	Object getItem(int index);
	
	void removeItem(int index);

	int getSelectItemIndex();

	void select(int index);

	boolean isEmpty();
	
	View getView(int index, View contentView, ViewGroup parent);
	
	void registerDataSetObserver(IAnimListObserver observer);
	 
	void unregisterDataSetObserver(IAnimListObserver observer);

}
