package com.kedacom.touchdata.whiteboard.data;


public abstract class BasePageAdapter implements IPageAdapter {
	
    private final PageSetObservable mDataSetObservable = new PageSetObservable();
	 
	 
	@Override
	public void registerDataSetObserver(PageSetObserver observer) {
		 mDataSetObservable.registerObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(PageSetObserver observer) {
		mDataSetObservable.unregisterObserver(observer);
	}
	
	public void notifyPageChanged() {
        mDataSetObservable.notifyPageChanged();
    }
	
	public void notifySubpageChanged(){
	    mDataSetObservable.notifySubpageChanged();
	}
	 
	public boolean isEmpty() {
	        return getPageCount() == 0;
	}
}
