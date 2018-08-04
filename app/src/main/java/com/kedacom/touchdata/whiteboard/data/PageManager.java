package com.kedacom.touchdata.whiteboard.data;

import java.util.ArrayList;
import java.util.List;

import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.DateUtils;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils.ISavePageCallBack;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

public class PageManager extends BasePageAdapter implements IPageManager,ISavePageCallBack{

	private ArrayList<IPage> pageList = new ArrayList<IPage>();
	
	private int curSelectPageIndex;
	
	private int curSaveMaxCount = 0;
	
	private int curSaveCount = 0;
	
	private ISavePageListener mSaveListener;
	
	@Override
	public int getPageCount() {
		return pageList.size();
	}

	public int getSubPageCount(){
		int count = 0;
		for(int i = 0;i<pageList.size();i++){
			count += pageList.get(i).getSubPageCount();
		}
		return count;
	}

	@Override
	public IPage getPage(int position) {
		int count = getPageCount();
		if(position>=0&&position<count){
			return pageList.get(position);
		}
		return null;
	}

	@Override
	public void addPage(IPage page) {
		pageList.add(page);
		page.setName(""+pageList.size());
		curSelectPageIndex = pageList.size() - 1;
		notifyPageChanged();
	}

	@Override
	public void addPageNotNotify(IPage page) {
		pageList.add(page);
		page.setName(""+pageList.size());
	}

	@Override
	public IPage getPageFromId(long pageId) {
		TPLog.printError(" Mt getPageFromId pageId = "+pageId);
		int pageCount = pageList.size();
		for(int i = 0;i<pageCount;i++){
			TPLog.printError(" Mt getPageFromId pageId-for = "+pageList.get(i).getId());
			if(pageList.get(i).getId() == pageId){
				return pageList.get(i);
			}
		}
		return null;
	}

	public IPage getPageFromRemotePageId(String pageId) {
		if(pageId==null||pageId.trim().isEmpty()){
			return null;
		}
		int pageCount = pageList.size();
		for(int i = 0;i<pageCount;i++){
			TPLog.printError(" Mt pageId = "+pageId+",,,,,pageList.get(i).getRemotePageId() = "+pageList.get(i).getRemotePageId());
			if(pageId.equals(pageList.get(i).getRemotePageId())){
				return pageList.get(i);
			}
		}
		return null;
	}


	@Override
	public void setPageList(List<Page> list) {
		if(list==null||list.isEmpty()){
			return;
		}
		pageList.clear();
		int count = list.size();
		for(int i=0;i<count;i++){
			pageList.add(list.get(i));
		}
	}

	@Override
	public boolean removePage(IPage page) {
		int index = pageList.indexOf(page);
		return removePage(index);
	}

	/**
	 * 删除 白板，这个方法是基础方法，所有的删除都会到这里
	 * @param index
     */
	@Override
	public boolean removePage(int index) {
		if(index<0)return false;
		if(getPageCount() == 0) {
			return false;
		}
		if(index>=getPageCount()){
			return false;
		}
		
		if(curSelectPageIndex==index){
			if(index==pageList.size()-1){
				curSelectPageIndex--;
			}
		}

		if(curSelectPageIndex>index){
			curSelectPageIndex--;
		}
		
		pageList.remove(index).destory();

		if(pageList.isEmpty()){
			WhiteBoardUtils.resetCurPageNum();
			addPage(WhiteBoardUtils.createDefWbPage());
		}
		resetPageName();
		notifyPageChanged();

		if(mSaveListener!=null)
			mSaveListener.onPageCountChanged(getPageCount());

		return true;
	}
	
	@Override
	public boolean removePage(long pageId) {
		TPLog.printError("tabId-----------removePage--------->"+pageId);
		int count = pageList.size();
		int index = -1;
		for(int i = 0;i<count;i++){
			if(pageId == pageList.get(i).getId()){
				index = i;
				break;
			}
		}
		TPLog.printError("tabId-----------removePage--------->index="+index);
		return removePage(index);
	}

	public boolean removePage(String remotePageId) {
		TPLog.printError("tabId-----------removePage--------->"+remotePageId);
		if(remotePageId==null||remotePageId.trim().isEmpty()){
			return false;
		}
		int count = pageList.size();
		int index = -1;
		for(int i = 0;i<count;i++){
			if(remotePageId.equals(pageList.get(i).getRemotePageId())){
				index = i;
				break;
			}
		}
		TPLog.printError("tabId-----------removePage--------->index="+index);
		return removePage(index);
	}


	public boolean removePageNotNotify(String remotePageId) {
		TPLog.printError("tabId-----------removePage--------->"+remotePageId);
		if(remotePageId==null||remotePageId.trim().isEmpty()){
			return false;
		}
		int count = pageList.size();
		int index = -1;
		for(int i = 0;i<count;i++){
			if(remotePageId.equals(pageList.get(i).getRemotePageId())){
				index = i;
				break;
			}
		}
		TPLog.printError("tabId-----------removePage--------->index="+index);
		if(index<0)return false;
		if(getPageCount() == 0) {
			return false;
		}
		if(index>=getPageCount()){
			return false;
		}

		if(curSelectPageIndex==index){
			if(index==pageList.size()-1){
				curSelectPageIndex--;
			}
		}

		if(curSelectPageIndex>index){
			curSelectPageIndex--;
		}

		pageList.remove(index).destory();

		if(pageList.isEmpty()){
			WhiteBoardUtils.resetCurPageNum();
			addPage(WhiteBoardUtils.createDefWbPage());
		}
		resetPageName();

		if(mSaveListener!=null)
			mSaveListener.onPageCountChanged(getPageCount());

		return true;
	}

	@Override
	public void removePage(long pageId, long displayPageId) {
		int count = pageList.size();
		int index = 0;
		for(int i = 0;i<count;i++){
			if(pageId == pageList.get(i).getId()){
				index = i;
				break;
			}
		}
		pageList.remove(index);
		resetPageName();
		selectPage(displayPageId);
		if(mSaveListener!=null)
			mSaveListener.onPageCountChanged(getPageCount());

	}

	@Override
	public void selectPage(int index) {
		curSelectPageIndex = index;
		notifyPageChanged();
	}

	@Override
	public void selectPage(long tabId) {
		TPLog.printKeyStatus("select Page Id:"+tabId);
		int count = pageList.size();
		int index = -1;
		
		for(int i = 0;i<count;i++){
			if(tabId == pageList.get(i).getId()){
				index = i;
				break;
			}
		}
		TPLog.printKeyStatus("select Page index:"+index);
		if(index == -1){
			return;
		}
		
		selectPage(index);
	}

	public void selectPage(String remotePageId) {
		TPLog.printKeyStatus("select Page Id:"+remotePageId);
		if(remotePageId == null||remotePageId.trim().isEmpty()){
			return;
		}
		int count = pageList.size();
		int index = -1;

		for(int i = 0;i<count;i++){
			if(remotePageId.equals(pageList.get(i).getRemotePageId()) ){
				index = i;
				break;
			}
		}
		TPLog.printKeyStatus("select Page index:"+index);
		if(index == -1){
			return;
		}

		selectPage(index);
	}
	
	@Override
	public int getSelectPageIndex() {
		return curSelectPageIndex;
	}

	@Override
	public IPage getSelectPage() {
		if(curSelectPageIndex>=getPageCount()){
			curSelectPageIndex = getPageCount() - 1;
		}
		if(getPageCount()==0)return null;
		return pageList.get(curSelectPageIndex);
	}
	
	@Override
	public ArrayList<IPage> getPageList() {
		return pageList;
	}

	@Override
	public void selectCurPageSubPage(int index) {
		getSelectPage().selectSubPage(index);
		notifyPageChanged();
	}

	public boolean hasPage(long pageId){
		ArrayList<IPage> list = getPageList();
		for(int i = 0;i<list.size();i++){
			if(list.get(i).getId() == pageId){
				return true;
			}
		}
		return false;
	}

	public boolean hasPage(String remotePageId){
		TPLog.printError("Mt remotePageId - > " + remotePageId);
		if(remotePageId == null || remotePageId.trim().isEmpty()){
			return false;
		}
		ArrayList<IPage> list = getPageList();
		for(int i = 0;i<list.size();i++){
			TPLog.printError("Mt local remotePageId - > " + list.get(i).getRemotePageId());
			if(remotePageId.equals(list.get(i).getRemotePageId())){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasPrePage() {
		return curSelectPageIndex == 0?false:true;
	}

	@Override
	public boolean hasNextPage() {
		return curSelectPageIndex == (getPageCount()-1)?false:true;
	}

	@Override
	public IPage prePage() {
		if(curSelectPageIndex==0){
			return null;
		}
		curSelectPageIndex--;

		selectPage(curSelectPageIndex);
		return getSelectPage();
	}

	@Override
	public IPage nextPage() {
		if(curSelectPageIndex==(getPageCount()-1)){
			return null;
		}

		curSelectPageIndex++;

		selectPage(curSelectPageIndex);
		return getSelectPage();
	}

	@Override
	public void selectCurPageSubPage(long imageId) {
		getSelectPage().selectSubPage(imageId);
		notifyPageChanged();
	}

	@Override
	public boolean nextSubPage() {
		Image curImage = getCurSelectSubPage().getImage();
		boolean boo = getSelectPage().nextSubPage();
		notifyPageChanged();
		if(curImage!=null){
			curImage.recycledBitmap();
		}
		return boo;
	}

	@Override
	public boolean previousSubPage() {
		Image curImage = getCurSelectSubPage().getImage();
		boolean boo = getSelectPage().PreviousSubPage();
		notifyPageChanged();
		if(curImage!=null){
			curImage.recycledBitmap();
		}
		return boo;
	}

	@Override
	public boolean hasNextSubPage() {
		return getSelectPage().hasNextSubpage();
	}

	@Override
	public boolean hasPreSubPage() {
		return getSelectPage().hasPreSubPage();
	}

	@Override
	public int getCurPageSelectSubPageIndex() {
		return getSelectPage().getCurSubPageIndex();
	}

	@Override
	public ISubPage getSubPage(int pageIndex, int subPageIndex) {
		return getPage(pageIndex).getSubPage(subPageIndex);
	}

	@Override
	public ISubPage getCurPageSubPage(int subPageIndex) {
		return getSelectPage().getSubPage(subPageIndex);
	}

	@Override
	public ISubPage getCurSelectSubPage() {
		return getSelectPage().getCurSubPage();
	}

	private void resetPageName(){
		int count = pageList.size();
		for(int i = 0;i<count;i++){
			pageList.get(i).setName(""+(i+1));
		}
	}

	@Override
	public void savePage(IPage page, String saveDir, String fileName) {
		curSaveCount = 0;
		curSaveMaxCount = page.getSubPageCount();
		page.saveToImage(saveDir,fileName,this);
	}

	@Override
	public void saveSelectSubPage(IPage page, String saveDir, String fileName) {
		curSaveCount = 0;
		curSaveMaxCount = 1;
		page.saveCurSubPageToImage(saveDir,fileName,this);
	}


	@Override
	public void saveCurSubPage(String saveDir,String fileName) {
		curSaveCount = 0;
		curSaveMaxCount = 1;
		if(mSaveListener!=null){
			mSaveListener.savePageStart(curSaveMaxCount);
		}
		getSelectPage().saveCurSubPageToImage(saveDir,fileName, this);
	}
	
	@Override
	public void saveCurPage(String saveDir,String fileName){
		curSaveCount = 0;
		curSaveMaxCount = 0;
		IPage curPage = getSelectPage();
		curSaveMaxCount = curPage.getSubPageCount();
        curPage.saveToImage(saveDir,fileName, this);
        
	}

    /**
	 * 保存所有白板
	 * @param saveDir
     */
	@Override
	public boolean saveAllPage(String saveDir) {
		TPLog.printError("saveAllPage begin save to "+saveDir);
		curSaveCount = 0;

		curSaveMaxCount = 0;

		int count = pageList.size();

		for(int i = 0; i<count ;i++){
			if(pageList.get(i).hasGraphs()) {
				curSaveMaxCount += pageList.get(i).getSubPageCount();
			}
		}

		TPLog.printError("curSaveMaxSubPageCount  ="+curSaveMaxCount);

		if(curSaveMaxCount==0){
			TPLog.printError("curSaveMaxSubPageCount = 0,return false");
			return false;
		}

		if(mSaveListener!=null){
			TPLog.printError("callback savePageStart...");
			mSaveListener.savePageStart(curSaveMaxCount);
		}

		for(int i = 0; i<count;i++){
			   if(pageList.get(i).hasGraphs()) {//只保存有图元的白板
				   String fileName = pageList.get(i).getName() + "_" + DateUtils.getCurTime("HHmmss");
				   pageList.get(i).saveToImage(saveDir, fileName, this);
			   }
		}
		TPLog.printError("saveAllPage end");
		return true;
	}

	/**
	 * 保存所有白板
	 * @param saveDir
	 */
	public boolean savePageAndClear(String saveDir,ArrayList<IPage> pageList) {
		TPLog.printError("auto saveAllPage begin save to "+saveDir);
		curSaveCount = 0;

		curSaveMaxCount = 0;

		int count = pageList.size();

		for(int i = 0; i<count ;i++){
			if(pageList.get(i).hasGraphs()) {
				curSaveMaxCount += pageList.get(i).getSubPageCount();
			}
		}

		TPLog.printError("auto curSaveMaxSubPageCount  ="+curSaveMaxCount);

		if(curSaveMaxCount==0){
			TPLog.printError("auto curSaveMaxSubPageCount = 0,return false");
			return false;
		}

		for(int i = 0; i<count;i++){
			if(pageList.get(i).hasGraphs()) {//只保存有图元的白板
				String fileName = pageList.get(i).getName() + "_" + DateUtils.getCurTime("HHmmss");
				pageList.get(i).saveToImage(saveDir, fileName, null);
			}
		}
		pageList.clear();
		TPLog.printError("auto saveAllPage end");
		return true;
	}

	@Override
	public void stopSave() {
		SavePageUtils.getInstence().stopSave();
	}

	public boolean saveAllPageToCache(String saveDir) {
		TPLog.printError("saveAllPageToCache begin save to "+saveDir);
		curSaveCount = 0;

		curSaveMaxCount = 0;

		int count = pageList.size();

		for(int i = 0; i<count ;i++){
			if(pageList.get(i).hasGraphs()) {
				curSaveMaxCount += pageList.get(i).getSubPageCount();
			}
		}

		TPLog.printError("curSaveMaxSubPageCount  ="+curSaveMaxCount);

		if(curSaveMaxCount==0){
			TPLog.printError("curSaveMaxSubPageCount = 0,return false");
			return false;
		}

		if(mSaveListener!=null){
			TPLog.printError("callback savePageStart...");
			mSaveListener.savePageStart(curSaveMaxCount);
		}

		for(int i = 0; i<count;i++){
			if(pageList.get(i).hasGraphs()) {//只保存有图元的白板
//				String fileName = pageList.get(i).getName() + "_" + DateUtils.getCurTime("HHmmss");
				String fileName = DateUtils.getCurTime("yyyyMMdd") + "-白板"+pageList.get(i).getName() ;
				pageList.get(i).saveImageToCache(saveDir, fileName, this);
			}
		}
		TPLog.printError("saveAllPageToCache end");
		return true;
	}


	@Override
	public boolean isNeedSave() {
		List<IPage> pageList = getPageList();
		int count = pageList.size();
		for(int i = 0;i<count;i++){
			if(pageList.get(i).isNeedSave()){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isGraphesEmpty() {
		List<IPage> pageList = getPageList();
		int count = pageList.size();
		for(int i = 0;i<count;i++){
			if(!pageList.get(i).isEmpty()){
				return false;
			}
		}
		return true;
	}

	@Override
	public void onSaveSuccess(String savePath) {
		curSaveCount++;
		if(mSaveListener!=null){
			mSaveListener.saveProgress(curSaveCount);
		}
		
		if(curSaveCount>=curSaveMaxCount){//保存图片完成
			int lastIndex = savePath.lastIndexOf("/");
			String path = savePath.substring(0, lastIndex+1);
			mSaveListener.savePageSuccess(path);
		}
	}

	@Override
	public void onSaveFailed() {
		curSaveCount++;
		if(mSaveListener!=null){
			mSaveListener.savePageFailed();
		}
	}


	public void setISavePageListener(ISavePageListener listener){
		mSaveListener = listener;
	}
	
	public interface ISavePageListener{
		void savePageStart(int saveCount);
		void saveProgress(int proress);
		void savePageSuccess(String savePath);
		void savePageFailed();

		//这个函数本不应该在这里，但是又不想重新写一个回调，因此就添加在这里了
		void onPageCountChanged(int count);
	}

	public void clearAll(){
		if(pageList==null){
			return;
		}

		int count = pageList.size();

		for(int i = 0;i<count;i++){
			IPage page = pageList.get(i);
			page.destory();
			page = null;
		}

		pageList.clear();
	}


	@Override
	public void onDestroy() {
		TPLog.printError("PageManager -> onDestroy");
		if(pageList==null)return;
		int count = pageList.size();
		for(int i = 0;i<count;i++){
			pageList.get(i).destory();
		}
		pageList.clear();
		pageList = null;
		mSaveListener = null;
	}
}
