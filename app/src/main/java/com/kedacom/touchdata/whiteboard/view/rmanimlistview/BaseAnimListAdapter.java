package com.kedacom.touchdata.whiteboard.view.rmanimlistview;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseAnimListAdapter implements IAnimListAdapter {
	
	private List<IAnimListObserver> list = new ArrayList<IAnimListObserver>();
	
	@Override
	public void registerDataSetObserver(IAnimListObserver observer) {
		list.add(observer);
	}
	
	@Override
	public void unregisterDataSetObserver(IAnimListObserver observer) {
		list.remove(observer);
	}
	
	public void notifyAnimListChanged(){
		int obsCount = list.size();
		for(int i = 0;i<obsCount;i++){
			list.get(i).onAnimListDataChanged();
		}
	}
	
	public boolean isEmpty(){
		boolean empty = getCount() == 0?true:false;
		return empty;
	}
}
