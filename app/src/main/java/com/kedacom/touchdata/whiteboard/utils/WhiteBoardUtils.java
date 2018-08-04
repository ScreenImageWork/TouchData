package com.kedacom.touchdata.whiteboard.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.kedacom.httpserver.utils.Network;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.tplog.TPLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class WhiteBoardUtils {

	public static final int GRAPH_UNKNOWN = -1;

	public static final int GRAPH_PEN = 1;
	
	public static final int GRAPH_ERASE = 7;
	
	public static final int GRAPH_ERASE_AREA = 8;
	
	public static final int GRAPH_IMAGE = 10;
	
	public static final int GRAPH_CIRCLE = 6;
	
	public static final int GRAPH_LINE = 4;
	
	public static final int GRAPH_NITEPEN = 3;
	
	public static final int GRAPH_CLEAR = 5;

	public static final int GRAPH_DRAG = 9;

	public static final int GRAPH_SELECT = 11;

	public static final int GRAPH_BRUSHPEN = 12;  //带笔锋

	public static final int GRAPH_RECTANGLE = 13;  //带笔锋

	public static final int OP_DRAG = 0;

	public static final int OP_SELECT_IMG = 1;

	public static final int OP_PAINT = 2;

	public static final int OP_ERASE = 3;

	public static final int OP_ERASE_AREA = 4;

	public static final int OP_CLEAR = 5;

	public static final float IMG_MIN_WIDTH = 100;  //图片的最小宽度

	public static final float IMG_MIN_HEIGHT = 100; //图片的最小高度

	public static final int IMG_MAX_WIDTH = 1920;   //图片的最大宽度

	public static final int IMG_MAX_HEIGHT = 1080;  //图片的最大高度

	public static final int CLIP_RECT_MIN_WIDTH = 100;

	public static final int CLIP_RECT_MIN_HEIGHT = 100;

	public static  int BACKGROUNDCOLOR[] = {
			R.mipmap.new_bg_1080,
			Color.parseColor("#202d35"),
//			Color.parseColor("#4c3f2d"),
			Color.parseColor("#252626"),
			Color.parseColor("#1c241e")
			//,Color.parseColor("#959595"),
	                                         };

	public static int normalBgColor ;

	public static final int PAINT_SIZE[] = {3,6,8};

	public static float screenWidth;

	public static float screenHeight;

	public static float whiteBoardWidth;

	public static float whiteBoardHeight;

	public static float bottomBarHeight;

	public static float whiteBoardCenterX;

	public static float whiteBoardCenterY;

	public static float density;

	public static float densityDpi;

	public static float curStrokeWidth = PAINT_SIZE[0];

	public static int curColor = Color.WHITE;

	public static int curOpType = OP_PAINT;

	public  static int curPageIndex = 1;

	public static int curBackground = BACKGROUNDCOLOR[0];

	//调色板固定可选颜色值
	public final static int colors[] = {Color.parseColor("#ffffffff"),Color.parseColor("#ffff0000"),Color.parseColor("#ffbf5500"),Color.parseColor("#fffff000"),
			                               Color.parseColor("#ff00a604"),Color.parseColor("#ff00aeff"),Color.parseColor("#ffc950fb")
	                                       };

//	public static  boolean isIntoMeeting = false;  //当前是否在会议中

	public static boolean isAPPShowing = false; //应用是否在显示

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static void init(Context context){
		TPLog.printError("WhiteBoardUtils init...");
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		Point point = new Point();
		((Activity)context).getWindowManager().getDefaultDisplay().getRealSize(point);
		screenWidth = point.x;
		screenHeight = point.y;
		density = dm.density;
		densityDpi = dm.densityDpi;

		whiteBoardWidth = screenWidth;
		bottomBarHeight = context.getResources().getDimension(R.dimen.touchdata_bottombar_height);
//		whiteBoardHeight = screenHeight - bottomBarHeight;
		whiteBoardHeight = screenHeight;

		whiteBoardCenterX = whiteBoardWidth/2;
		whiteBoardCenterY = whiteBoardHeight/2;

		if(screenHeight>1080){
			BACKGROUNDCOLOR[0] = R.mipmap.new_bg_1200;
		}

		curBackground = BACKGROUNDCOLOR[0];

//		IMG_MAX_WIDTH = (int)(whiteBoardWidth*0.65f);
//		IMG_MAX_HEIGHT = (int)(whiteBoardHeight*0.65f);

		TPLog.printKeyStatus("screenWidth="+screenWidth+";screenHeight="+screenHeight);
		TPLog.printKeyStatus("whiteBoardWidth="+whiteBoardWidth+";whiteBoardHeight="+whiteBoardHeight);

		printScreenParams(context);

		normalBgColor = context.getResources().getColor(R.color.background_normal);
	}

	public static void displayScreenParams(){

	}

	public static int getCurEraseSize(int curStrokeWidth){
		switch (curStrokeWidth){
			case 3:
				return 90;
			case 6:
				return 120;
			case 8:
				return 150;
		}
		return 90;
	}
	
	public static long getId(){
		return SystemClock.uptimeMillis();
	}

	//远程数据协作ID
	public static String getRemoteId(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
	public static String getPageName(){
		String name = ""+curPageIndex;
		curPageIndex++;
		return name;
	}

	public static void setCurPageNum(int index){
		curPageIndex = index;
	}

	public static void resetCurPageNum(){
		curPageIndex = 1;
	}

	/**
	 * 设置白板背景
	 * @param wbView
	 * @param color
	 */
	public static void setWbBackground(View wbView,int color){
		int firstColor = BACKGROUNDCOLOR[0];
		if(color == firstColor){
			wbView.setBackgroundResource(color);
		}else{
			wbView.setBackgroundColor(color);
		}
	}

	public static void drawWbBackground(Canvas canvas, int color){
		if(color == BACKGROUNDCOLOR[0]){
			Bitmap bitmap = ResUtils.resToBitmap(color);
			canvas.drawBitmap(bitmap,0,0,null);
		}else{
			canvas.drawColor(color);
		}
	}

	public static Image createImage(String path,boolean isComputePosition){
		File file = new File(path);
		Image image = new Image();
		image.setFilePath(path);
		image.setFileSize(file.length());
		image.setFileName(file.getName());
		image.setDwCurBlock((int) file.length());
		android.graphics.Point size = BitmapManager.getImageSize(path);
		image.setWidth(size.x);
		image.setHeight(size.y);
		float px = 0;
		float py = 0;

		if(isComputePosition) {
			 px = (WhiteBoardUtils.whiteBoardWidth - image.getWidth()) / 2f;
			 py = (WhiteBoardUtils.whiteBoardHeight - image.getHeight()) / 2f;
		}

		if(py<0)py = 0;
		if(px<0)px = 0;
		image.setX(px);
		image.setY(py);
		return image;
	}

	public static Page createDefWbPage(){
		Page page = new Page();
		page.setName(getPageName());
		SubPage subPage = new SubPage();
		page.addSubPage(subPage);
		page.setAnoymous(true);
		page.setOwnerIndex((int)NetUtil.curUserId);
		page.setBackGroundColor(WhiteBoardUtils.curBackground);
		return page;
	}

	public static Page createWbPage(Image image){
		Page page = new Page();
		page.setName(getPageName());
		SubPage subPage = new SubPage(image);
		page.addSubPage(subPage);
		page.setBackGroundColor(WhiteBoardUtils.curBackground);
		page.setAnoymous(true);
		page.setOwnerIndex((int)NetUtil.curUserId);
		return page;
	}

	public static Page createWbPage(String name){
		Page page = new Page();
		page.setName(name);
		SubPage subPage = new SubPage();
		page.addSubPage(subPage);
		page.setBackGroundColor(WhiteBoardUtils.curBackground);
		page.setAnoymous(false);
		page.setOwnerIndex((int)NetUtil.curUserId);
		return page;
	}

	public static Page createWbPage(String name,Image image){
		Page page = new Page();
		page.setName(name);
		SubPage subPage = new SubPage(image);
		page.addSubPage(subPage);
		page.setBackGroundColor(WhiteBoardUtils.curBackground);
		return page;
	}


	public static String getNewSaveFileDir(String path){
		//保存文件夹默认名称（前缀）
		String curSaveFileDirPre =getTodaySaveDirPrefixName();

		String curSaveFileDir =curSaveFileDirPre+"_0"+1;

		if(path==null||path.isEmpty()){
			return curSaveFileDir;
		}

		File file = new File(path,curSaveFileDir);
		int index = 1;
		while(file.exists()){
			index++;
			String suffix = ""+index;
			if(index<10){
				suffix = "0"+index;
			}
			curSaveFileDir = curSaveFileDirPre+"_"+suffix;
			file = new File(path,curSaveFileDir);
		}

		curSaveFileDir = file.getName();

		return curSaveFileDir;
	}


	public static String getPreSaveFileDir(String path){
       //保存文件夹默认名称（前缀）
		String curSaveFileDirPre = getTodaySaveDirPrefixName();

		String curSaveFileDir = curSaveFileDirPre + "_1";
		if(path==null||path.isEmpty()){
			return curSaveFileDir;
		}

		File file = new File(path,curSaveFileDir);
		int index = 1;
		while(file.exists()){
			index++;
//			TPLog.printError("ZL","index--------"+(index));
			String suffix = ""+index;
			if(index<10){
				suffix = "0"+index;
			}
			curSaveFileDir = curSaveFileDirPre + "_" + suffix;
			file = new File(path,curSaveFileDir);
		}

//		TPLog.printError("ZL","lasFile--------"+(index-1));
		if((index-1)!=0){
			String suffix = ""+(index-1);
			if((index-1)<10){
				suffix = "0"+(index-1);
			}
			curSaveFileDir = curSaveFileDirPre+"_"+suffix;
		}

		return curSaveFileDir;
	}

	//获取今天保存文件夹前缀
	private static String getTodaySaveDirPrefixName(){
		String name = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		name = sdf.format(new Date());
		sdf = null;
		return name ;
	}


	public static void reset(){
		curStrokeWidth = PAINT_SIZE[0];
		curColor = Color.WHITE;
		curOpType = OP_PAINT;
		curPageIndex = 1;
		curBackground = BACKGROUNDCOLOR[0];
	}



	public static void printScreenParams(Context context){
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		float density = dm.density;
		float densityDpi = dm.densityDpi;
		float xdpi = dm.xdpi;
		float ydpi = dm.ydpi;

		Log.e("ScreenParams","*******************ScreenParams*********************");
		Log.e("ScreenParams","screenWidth:"+screenWidth);
		Log.e("ScreenParams","screenHeight:"+screenHeight);
		Log.e("ScreenParams","density:"+density);
		Log.e("ScreenParams","densityDpi:"+densityDpi);
		Log.e("ScreenParams","xdpi:"+xdpi);
		Log.e("ScreenParams","ydpi:"+ydpi);
		Log.e("ScreenParams","*******************ScreenParams*********************");

	}
}
