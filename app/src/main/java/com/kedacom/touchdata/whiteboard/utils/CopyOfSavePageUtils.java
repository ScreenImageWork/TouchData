package com.kedacom.touchdata.whiteboard.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.msg.MsgQueue;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.tplog.TPLog;

public class CopyOfSavePageUtils {
	
	private final float BITMAP_BOUNDS = 50;//生成图片的白边为50px
	
	private final float BITMAP_MAX_WIDTH = 1920;
	
	private final float BITMAP_MAX_HEIGHT = 1080;
	
	private final float BITMAP_MAX_ACREAGE = BITMAP_MAX_WIDTH * BITMAP_MAX_HEIGHT;
	
	private static CopyOfSavePageUtils mSavePageUtils;
	
	private final String FILE_PATH_NORMAL = "";//默认文件路径  末尾不带斜杠 /
	
	private MsgQueue<Task> taskQueue = new MsgQueue<Task>();
	
	private SaveThread mSaveThread;
	
	private boolean isRunning = false;
	
	private BitmapManager mBitmapManager = BitmapManager.getInstence();
	
	
	private CopyOfSavePageUtils(){
		
	}
	
	public synchronized static CopyOfSavePageUtils getInstence(){
		if(mSavePageUtils==null){
			mSavePageUtils = new CopyOfSavePageUtils();
		}
		return mSavePageUtils;
	}
	
	public void saveSubPage(SubPage page,String saveDir,ISavePageCallBack callback){
		if(saveDir==null){
			saveDir = FILE_PATH_NORMAL;
		}else{
			if(saveDir.endsWith("/")){
				saveDir = saveDir.substring(0, saveDir.length()-1);
			}
		}
		Task task = new Task(page, callback, saveDir);
		taskQueue.addMsg(task);
		
		if(mSaveThread==null||!mSaveThread.isAlive()){
			mSaveThread = new SaveThread();
			mSaveThread.start();
		}
	}
	
	private float minX = 0;
	private float minY = 0;
	
	private PointF computeSubPageBounds(SubPage subPage){
		PointF bounds = new PointF();
		ArrayList<Graph> list = subPage.getGraphList();
		
		float minX = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;
		
		float minY = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		
		int count = list.size();
		
		for(int i = 0;i<count;i++){
			Graph graph = list.get(i);
			Rect rect = graph.getBounds();
			rect.sort();
			if(minX>rect.left){
				minX = rect.left;
			}
			
			if(maxX<rect.right){
				maxX = rect.right;
			}
			
			if(minY>rect.top){
				minY = rect.top;
			}
			
			if(maxY < rect.bottom){
				maxY = rect.bottom;
			}
		}
		
		Image image = subPage.getImage();
		if(image!=null){
			float lx = image.getX();
			float ly = image.getY();
			
			float rx = image.getWidth() + lx;
			float ry = image.getHeight() + ly;
			
			Rect rect = new Rect((int)lx, (int)ly, (int)rx, (int)ry);
			rect.sort();
			
			if(minX>rect.left){
				minX = rect.left;
			}
			
			if(maxX<rect.right){
				maxX = rect.right;
			}
			
			if(minY>rect.top){
				minY = rect.top;
			}
			
			if(maxY < rect.bottom){
				maxY = rect.bottom;
			}
			
		}
		
		this.minX = minX;
		this.minY = minY;
		
		
		float px = (maxX - minX)/2f;
		float py = (maxY - minY)/2f;
		
		/**
		对最终矩形进行旋转
		px =Math.abs((maxX - minX))/2f;
		py =Math.abs((maxY - minY))/2f;
		Region region = new Region((int)minX,(int)minY,(int)maxX,(int)maxY);
		Path path = region.getBoundaryPath();
		Matrix matrix = new Matrix();
		matrix.setRotate(subPage.getAngle(),px,py);
		path.transform(matrix);
		RectF rf  = new RectF();
		path.computeBounds(rf, true);
		Rect rect  = new Rect();
		rf.roundOut(rect);
		
		rect.sort();
		
		float width = rect.width();
		float height = rect.height();
		
		this.minX = rect.left;
		this.minY = rect.top;
		 */
		
		/*对矩形的四个定点进行旋转 ，选额出最大和最小的两个点*/
		int angle = subPage.getAngle();
		Point ltPoint = changeCoordinate(new Point((int)minX, (int)minY),new Point((int)px, (int)py),angle,1.0f,0,0);
		Point rtPoint = changeCoordinate(new Point((int)maxX, (int)minY),new Point((int)px, (int)py),angle,1.0f,0,0);
		Point lbPoint = changeCoordinate(new Point((int)minX, (int)maxY),new Point((int)px, (int)py),angle,1.0f,0,0);
		Point rbPoint = changeCoordinate(new Point((int)maxX, (int)maxY),new Point((int)px, (int)py),angle,1.0f,0,0);
		
		 minX = Integer.MAX_VALUE;
		 maxX = Integer.MIN_VALUE;
		 minY = Integer.MAX_VALUE;
		 maxY = Integer.MIN_VALUE;
		 
		 Point points[] = {ltPoint,rtPoint,lbPoint,rbPoint};
		 for(int i = 0;i<points.length;i++){
			 int x = points[i].x;
			 int y = points[i].y;
			 
			 if(minX>x){
				 minX = x;
			 }
			 
			 if(maxX<x){
				 maxX = x;
			 }
			 
			 if(minY>y){
				 minY = y;
			 }
			 
			 if(maxY<y){
				 maxY = y;
			 }
		 }
		 
		 this.minX = minX;
         this.minY = minY;
         
         float width = maxX - minX;
         float height = maxY - minY;
         
		if(width<WhiteBoardUtils.whiteBoardWidth&&height<WhiteBoardUtils.whiteBoardHeight){
			width = WhiteBoardUtils.whiteBoardWidth;
			height = WhiteBoardUtils.whiteBoardHeight;
		}
		
		bounds.set(width, height);
		
		return bounds;
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
		
		public Task(SubPage sp,ISavePageCallBack cb,String dir){
			subPage = sp;
			callBack = cb;
			saveDir = dir;
		}
	}
	
	class SaveThread extends Thread{
		
		private SimpleDateFormat sdf;
		
		@SuppressLint("SimpleDateFormat") 
		public SaveThread(){
			sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		}
		
		@Override
		public void run() {
			isRunning = true;
			
			while(isRunning){
				try{
				Task mTask = taskQueue.nextMsg();
				SubPage mSubPage = mTask.subPage;
				if(mSubPage!=null){
				PointF bounds = computeSubPageBounds(mSubPage);
				
				TPLog.printError("mSubPage.getAngle="+mSubPage.getAngle());
				
				int width = (int)bounds.x;
				int height = (int)bounds.y;
				
				int scaleWidth = 0;
				int scaleHeight = 0;
				
				
				float centerX = (bounds.x)/2;
				float centerY = (bounds.y)/2;
				
				float offsetX = minX;//minX ;
				float offsetY = minY;//minY ;

				int angle = mSubPage.getAngle();
				
				if(offsetX<=0&&angle<30&&angle>60){
					offsetX = (Math.abs(offsetX));
				}else{
					offsetX = offsetX*-1;
				}
				
				if(offsetY<=0){
					offsetY = (Math.abs(offsetY));
				}else{
					offsetY = offsetY*-1;
				}
				float scale = 1.0f;
				
				if(width>BITMAP_MAX_WIDTH&&height>BITMAP_MAX_HEIGHT){
					float ws = BITMAP_MAX_WIDTH /width;
					float hs = BITMAP_MAX_HEIGHT / height;
					if(ws>hs){
						scale = hs;
					}else{
						scale = ws;
					}
				}else if(width>BITMAP_MAX_WIDTH||height>BITMAP_MAX_HEIGHT){
					float area = width * height;
					if(area > BITMAP_MAX_ACREAGE){
						if(width>BITMAP_MAX_WIDTH){
							scale = BITMAP_MAX_WIDTH / width;
						}else{
							scale = BITMAP_MAX_HEIGHT / height;
						}
					}
				}
				
				scaleWidth = (int)((float)width*scale);// + (int)BITMAP_BOUNDS*2;
				scaleHeight = (int)((float)height*scale);// + (int)BITMAP_BOUNDS*2;
				
				Image image = mSubPage.getImage();
				Bitmap imageBitmap = null;
				if(image!=null){
					Bitmap bitmap = mBitmapManager.loadBitmap(image.getFilePath());
					if(bitmap!=null){
					imageBitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(imageBitmap);
					canvas.rotate(mSubPage.getAngle(), centerX, centerY);
					canvas.translate(offsetX, offsetY);
					canvas.scale(scale, scale);
					canvas.drawBitmap(bitmap, image.getX(), image.getY(), null);
					}
				}
				
				//2.将图元转化成bitmap
				Bitmap graphBitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(graphBitmap);
				canvas.rotate(mSubPage.getAngle(), centerX, centerY);
				canvas.translate(offsetX, offsetY);
				canvas.scale(scale, scale);
				
				ArrayList<Graph> list = mSubPage.getGraphList();
				for(int i = 0;i<list.size();i++){
					Graph graph = list.get(i);
					graph.draw(canvas);
				}
				
				//进行图片合成，叠加
				Bitmap bitmap = Bitmap.createBitmap(scaleWidth, scaleHeight, Bitmap.Config.ARGB_8888);
				Canvas cv = new Canvas(bitmap);
				cv.drawColor(Color.WHITE);//图片背景色为白色
				if(imageBitmap!=null){
				cv.drawBitmap(imageBitmap, 0, 0, null);
				}
				cv.drawBitmap(graphBitmap, 0, 0, null);
				
				String fileName = getFileName();
				String filePath = mTask.saveDir + File.separator + fileName;
				
				checkDir(mTask.saveDir);
				boolean boo = saveBitmap(filePath,bitmap);
				
				if(bitmap!=null&&!bitmap.isRecycled()){
					bitmap.recycle();
					bitmap = null;
				}
				
				if(graphBitmap!=null&&!graphBitmap.isRecycled()){
					graphBitmap.recycle();
					graphBitmap = null;
				}
				
				if(imageBitmap!=null&&!imageBitmap.isRecycled()){
					imageBitmap.recycle();
					imageBitmap = null;
				}

				if(mTask.callBack!=null){
					if(boo){
					mTask.callBack.onSaveSuccess(filePath);
					}else{
					mTask.callBack.onSaveFailed();
					}
				}	
				
				}
				}catch(Exception e){
					TPLog.printError("保存页面时出现异常:");
					TPLog.printError(e);
					e.printStackTrace();
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
			   FileOutputStream fOut = null;
			   try {
			    fOut = new FileOutputStream(path);
			    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			    fOut.flush();
			    fOut.close();
			   } catch (Exception e) {
			    e.printStackTrace();
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

}
