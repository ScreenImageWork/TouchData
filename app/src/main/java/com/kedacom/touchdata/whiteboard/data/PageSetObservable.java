package com.kedacom.touchdata.whiteboard.data;

public class PageSetObservable extends Observable<PageSetObserver> {
	
	public void notifyPageChanged() {
	        synchronized(mObservers) {
	            for (int i = mObservers.size() - 1; i >= 0; i--) {
	                mObservers.get(i).onPageChanged();
	            }
	        }
	    }
	 
	  public void notifySubpageChanged() {
	        synchronized (mObservers) {
	            for (int i = mObservers.size() - 1; i >= 0; i--) {
	                mObservers.get(i).onSubPageChanged();
	            }
	        }
	    }
}
