package com.kedacom.touchdata.whiteboard.data;

import java.util.ArrayList;
import java.util.List;

import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils;

public interface IPageManager {

	void addPage(IPage page);

	void addPageNotNotify(IPage page);

	IPage getPageFromId(long pageId);
	
	void setPageList(List<Page> list);
	
	boolean removePage(IPage page);

	boolean removePage(int index);

	boolean removePage(long pageId);
	
	void removePage(long pageId, long diplayPageId);
	
	void selectPage(int index);
	
	void selectCurPageSubPage(long imageId);
	
	void selectPage(long tabId);
	
	int getSelectPageIndex();
	
	IPage getSelectPage();
	
	ArrayList<IPage> getPageList();
	
	void selectCurPageSubPage(int index);

	boolean hasPrePage();

	boolean hasNextPage();

	IPage prePage();

    IPage nextPage();
	
	boolean nextSubPage();
	
	boolean previousSubPage();
	
	boolean hasNextSubPage();

	boolean hasPreSubPage();
	
	int getCurPageSelectSubPageIndex();
	
	ISubPage getSubPage(int pageIndex, int subPageIndex);
	
	ISubPage getCurPageSubPage(int subPageIndex);
	
	ISubPage getCurSelectSubPage();

	void savePage(IPage page,String saveDir,String fileName);

	void saveSelectSubPage(IPage page,String saveDir,String fileName);

	void saveCurSubPage(String saveDir,String fileName);
	
	void saveCurPage(String saveDir,String fileName);
	
	boolean saveAllPage(String saveDir);

	//中断保存操作
	void stopSave();
	
	boolean isNeedSave();

	boolean isGraphesEmpty();//是否存在图元

	void onDestroy();
	
}
