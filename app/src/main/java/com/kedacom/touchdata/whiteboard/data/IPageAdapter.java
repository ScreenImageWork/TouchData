package com.kedacom.touchdata.whiteboard.data;

import com.kedacom.touchdata.whiteboard.page.IPage;

public interface IPageAdapter {
	 void registerDataSetObserver(PageSetObserver observer);
	 void unregisterDataSetObserver(PageSetObserver observer);
	 int getPageCount();
	 IPage getPage(int position);
}
