package com.kedacom.touchdata.whiteboard.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;

import com.kedacom.touchdata.whiteboard.bg.BgFactory;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.msg.MsgQueue;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.tplog.TPLog;

public class SavePageUtils {

	private final float BITMAP_BOUNDS = 50;//生成图片的白边为50px

	private final float BITMAP_MAX_WIDTH = 1920;

	private final float BITMAP_MAX_HEIGHT = 1080;

	private final float BITMAP_MIN_WIDTH = 1920;

	private final float BITMAP_MIN_HEIGHT = 1080;

	private final float BITMAP_MAX_ACREAGE = BITMAP_MAX_WIDTH * BITMAP_MAX_HEIGHT;

	private static SavePageUtils mSavePageUtils;

	private final String FILE_PATH_NORMAL = "";//默认文件路径  末尾不带斜杠 /

	private MsgQueue<Task> taskQueue = new MsgQueue<Task>();

	private SaveThread mSaveThread;

	private boolean isRunning = false;

	private boolean interrupt = true;

	private BitmapManager mBitmapManager = BitmapManager.getInstence();

	private SavePageUtils(){

	}

	public synchronized static SavePageUtils getInstence(){
		if(mSavePageUtils==null){
			mSavePageUtils = new SavePageUtils();
		}
		return mSavePageUtils;
	}

	public void stopSave(){
		interrupt = true;
		if(taskQueue!=null&&taskQueue.isEmpty()){
			TPLog.printKeyStatus("中断失败，保存任务列表已经为空！");
			return;
		}
		taskQueue.clear();
		TPLog.printKeyStatus("中断保存成功，保存任务列表已经置为空！");
	}

	public void saveSubPage(SubPage page,String saveDir,String fileName,ISavePageCallBack callback){
		TPLog.printError("saveSubPage begin");
		interrupt = false;
		if(saveDir==null){
			saveDir = FILE_PATH_NORMAL;
		}else{
			if(saveDir.endsWith("/")){
				saveDir = saveDir.substring(0, saveDir.length()-1);
			}
		}
		Task task = new Task(page, callback, saveDir,fileName);
		taskQueue.addMsg(task);

		TPLog.printError("saveDir = "+saveDir);
		TPLog.printError("fileName = "+fileName);

		if(mSaveThread==null||!mSaveThread.isAlive()){
			mSaveThread = new SaveThread();
			mSaveThread.start();
		}

		TPLog.printError("saveSubPage end");
	}


	private Bounds computeSubPageBounds(SubPage subPage){
		PointF bounds = new PointF();
		ArrayList<Graph> graphList = subPage.getGraphList();
		ArrayList<Graph> imgList = subPage.getImageGraphList();

		ArrayList<Graph> list = new ArrayList<Graph>();

		int graphCount = graphList.size();
		int imgCount = imgList.size();
		for(int i = 0;i<graphCount;i++){
			list.add(graphList.get(i));
		}

		for(int i = 0;i<imgCount;i++){
			list.add(imgList.get(i));
		}

		Rect rect = GraphUtils.computeGraphBounds(list,subPage.getMatrix());

		float minX =rect.left;
		float maxX = rect.right;

		float minY = rect.top;
		float maxY = rect.bottom;

//		int count = list.size();
//
//		for(int i = 0;i<count;i++){
//			Graph graph = list.get(i);
//			Rect rect = graph.getBounds();
//			if(minX>rect.left){
//				minX = rect.left;
//			}
//
//			if(maxX<rect.right){
//				maxX = rect.right;
//			}
//
//			if(minY>rect.top){
//				minY = rect.top;
//			}
//
//			if(maxY < rect.bottom){
//				maxY = rect.bottom;
//			}
//		}
//
//		Image image = subPage.getImage();
//		if(image!=null){
//			float lx = image.getX();
//			float ly = image.getY();
//
//			float rx = image.getWidth() + lx;
//			float ry = image.getHeight() + ly;
//
//			Rect rect = new Rect((int)lx, (int)ly, (int)rx, (int)ry);
//			rect.sort();
//
//			if(minX>rect.left){
//				minX = rect.left;
//			}
//
//			if(maxX<rect.right){
//				maxX = rect.right;
//			}
//
//			if(minY>rect.top){
//				minY = rect.top;
//			}
//
//			if(maxY < rect.bottom){
//				maxY = rect.bottom;
//			}
//
//		}

//         float width = maxX - minX + subPage.getOffsetX();
//         float height = maxY - minY + subPage.getOffsetY();

		float width = maxX - minX;
		float height = maxY - minY;



//		if(width<WhiteBoardUtils.whiteBoardWidth&&height<WhiteBoardUtils.whiteBoardHeight){
//			width = WhiteBoardUtils.whiteBoardWidth;
//			height = WhiteBoardUtils.whiteBoardHeight;
//		}

		if(width<=0||height<=0){
			width = WhiteBoardUtils.whiteBoardWidth;
			height = WhiteBoardUtils.whiteBoardHeight;
		}

//		if(width<=BITMAP_MIN_WIDTH&&height<=BITMAP_MIN_HEIGHT){
//				width = (int)BITMAP_MIN_WIDTH;
//				height = (int)BITMAP_MIN_HEIGHT;
//		}

		bounds.set(width, height);

		//Bounds bound = new Bounds((int)(width), (int)(height), minX, minY);

		Bounds bound = new Bounds((int)(width+BITMAP_BOUNDS), (int)(height+BITMAP_BOUNDS), minX-BITMAP_BOUNDS/2f, minY-BITMAP_BOUNDS/2f);


		TPLog.printKeyStatus("计算图元边界大小：width="+bound.width+",height="+bound.height);

		return bound;
	}


	public interface ISavePageCallBack{
		void onSaveSuccess(String path);
		void onSaveFailed();
	}

	public Point changeCoordinate(Point point ,Point cPoint ,int angle, float scale, float offsetX,
								  float offsetY){

		int x1 = point.x;
		int y1 = point.y;

		//平移
		x1 = (int)(x1) + (int)(offsetX)*-1;
		y1 = (int)(y1) + (int)(offsetY)*-1;

		//缩放
		float cx = x1 - cPoint.x;
		float cy = y1 - cPoint.y;

		x1 = (int)(cPoint.x + (cx/scale));
		y1 = (int)(cPoint.y + (cy/scale));

		//旋转
		Point p = GraphUtils.computeRotatePoint(new Point(x1, y1),cPoint, angle);

		//Point point1 = new Point(x1, y1);
		return p;
	}

	class Task {
		public SubPage subPage;
		public ISavePageCallBack callBack;
		public String saveDir;
		public String fileName;

		public Task(SubPage sp,ISavePageCallBack cb,String dir,String fn){
			subPage = sp;
			callBack = cb;
			saveDir = dir;
			fileName = fn;
		}
	}


	/**
	 * 白板保存线程
	 * 保存流程，首先在不进行旋转的情况下将文档图片和图元保存到相应的Bitmap内
	 * 最终在合成文档图片和图元图片时进行旋转
	 * @author zhanglei
	 *
	 */
	class SaveThread extends Thread{

		private SimpleDateFormat sdf;

		@SuppressLint("SimpleDateFormat")
		public SaveThread(){
			sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		}

		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void run() {
			isRunning = true;

			while(isRunning){
				try{
					Task mTask = taskQueue.nextMsg();

					TPLog.printError("receive save task,save begin...");

					SubPage mSubPage = mTask.subPage;

					float ox = mSubPage.getOffsetX();
					float oy = mSubPage.getOffsetY();

					if(mSubPage!=null){
						Bounds bounds = computeSubPageBounds(mSubPage);

						Matrix matrix = new Matrix();
						matrix.postTranslate(bounds.x*-1,bounds.y*-1);
						matrix.postScale(1.0f,1.0f);


						int width = (int)bounds.width;
						int height = (int)bounds.height;


						int scaleWidth = 0;
						int scaleHeight = 0;

						float curScale = 1.0f;
						scaleWidth = (int)((float)width*curScale);// + (int)BITMAP_BOUNDS*2;
						scaleHeight = (int)((float)height*curScale);// + (int)BITMAP_BOUNDS*2;

						TPLog.printError("save img width="+scaleWidth+",height="+scaleHeight);

						//1.将图元转化成bitmap
						Bitmap graphBitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(graphBitmap);

						if(mSubPage.getBackgroundColor() == WhiteBoardUtils.BACKGROUNDCOLOR[0]){
							BgFactory.drawGriddingBg(canvas,scaleWidth,scaleHeight,WhiteBoardUtils.normalBgColor);
						}else{
							canvas.drawColor(mSubPage.getBackgroundColor());
						}

						//WhiteBoardUtils.drawWbBackground(canvas,mSubPage.getBackgroundColor());
						TPLog.printError("save background layer...");
						int layerCount = canvas.saveLayer(0,0,(int)scaleWidth,(int)scaleHeight,null);
						mSubPage.save(canvas,matrix);

						//2.将文档图片保存到Bitmap
						Image image = mSubPage.getImage();
						if(image!=null){
							int saveCount = canvas.save();
							canvas.setMatrix(matrix);
							image.draw(canvas);
							canvas.restoreToCount(saveCount);
						}
						canvas.restoreToCount(layerCount);

						Bitmap bitmap = null;
						int angle = mSubPage.getAngle();



						if(angle!=0) {//rotating img

							TPLog.printError(angle+" degrees rotating img...");

							angle = angle % 360;

							angle = Math.abs(angle);

							if (angle == 90 || angle == 270) {
								int temp = scaleWidth;
								scaleWidth = scaleHeight;
								scaleHeight = temp;
							}



							TPLog.printError("width="+scaleWidth,",height="+scaleHeight);

							//4.进行图片合成，叠加合成
							bitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);

							float rotatePx = scaleWidth / 2f;
							float rotatePy = scaleHeight / 2f;

							Canvas cv = new Canvas(bitmap);
							cv.save();
							cv.translate(rotatePx,rotatePy);
							cv.rotate(mSubPage.getAngle());
							cv.drawBitmap(graphBitmap,width/2f*-1 , height/2f*-1, null);
							cv.restore();
						}else{
							bitmap = graphBitmap;
						}
						//5.将合成的Bitmap保存到sdcard
						String fileName = mTask.fileName;
						String filePath = mTask.saveDir + File.separator + fileName;

						TPLog.printError("save img absolute path:"+filePath);

						checkDir(mTask.saveDir);

						boolean boo = saveBitmap(filePath,bitmap);

						TPLog.printError("save img result "+boo);

						//6.释放所有的Bitmap
						if(bitmap!=null&&!bitmap.isRecycled()){
							TPLog.printError("recycle bitmap...");
							bitmap.recycle();
							bitmap = null;
						}

						if(graphBitmap!=null&&!graphBitmap.isRecycled()){
							TPLog.printError("recycle graphBitmap...");
							graphBitmap.recycle();
							graphBitmap = null;
						}

						if(mTask.callBack!=null&&!interrupt){
							TPLog.printError("callback begin...");
							if(boo){
								TPLog.printError("callback onSaveSuccess...");
								mTask.callBack.onSaveSuccess(filePath);
							}else{
								TPLog.printError("callback onSaveFailed...");
								mTask.callBack.onSaveFailed();
								taskQueue.clear();
							}
							TPLog.printError("callback end...");
						}

						TPLog.printError("save img end...");
						Thread.sleep(100);

					}
				}catch(Exception e){
					TPLog.printError("保存白板时出现异常：");
					TPLog.printError(e);
				}
			}
		}

		private void checkDir(String path){
			File file = new File(path);
			if(!file.exists()){
				file.mkdirs();
				file.mkdir();
			}
		}

		private  boolean saveBitmap(String path,Bitmap mBitmap){
			TPLog.printKeyStatus("保存文件路径："+path);
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
			FileOutputStream fOut = null;
			try {
				fOut = new FileOutputStream(path);
				mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
				fOut.flush();
				fOut.close();
				fOut = null;
			} catch (Exception e) {
				TPLog.printError("保存文件时出现异常:");
				TPLog.printError(e);
				return false;
			}
			return true;
		}

		private String getFileName(){
			String name = sdf.format(new Date());
			name = name + ".jpg";
			return name;
		}
	}

	public class Bounds{
		public int width;
		public int height;
		public float x;
		public float y;

		public Bounds(int width,int height,float x,float y){
			this.width = width;
			this.height = height;
			this.x = x;
			this.y = y;
		}
	}

}
