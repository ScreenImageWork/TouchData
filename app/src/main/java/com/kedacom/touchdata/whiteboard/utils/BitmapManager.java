package com.kedacom.touchdata.whiteboard.utils;

import java.io.File;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.LruCache;

import com.kedacom.touchdata.whiteboard.msg.MsgQueue;
import com.kedacom.tplog.TPLog;

public class BitmapManager {

	public static final Bitmap CLIP_BTN_ICON[] = new Bitmap[2];
	public static final Bitmap ROTATE_BTN_ICON[] = new Bitmap[2];
	public static final Bitmap DELETE_BTN_ICON[] = new Bitmap[2];
	public static final Bitmap SURE_BTN_ICON[] = new Bitmap[2];
	public static final Bitmap CANCEL_BTN_ICON[] = new Bitmap[2];

	public static final Bitmap REMOTE_DCCONF_ON_OFF_BTN_ICON[] = new Bitmap[5];

	public static Bitmap IMG_TOOLS_TOP_ICON;

	final int MAXMEMONRY = (int) (Runtime.getRuntime() .maxMemory() / 8);
	
	private static BitmapManager instence;
	
	private LruCache<String,Bitmap> mCache ;
	 
	private MsgQueue<Task> taskQueue = new MsgQueue<Task>();
	
	private boolean isLoading = false;
	
	private LoadThread mLoadThread;
	
	private BitmapManager(){
		mCache = new LruCache<String,Bitmap>(MAXMEMONRY){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes()*value.getHeight();
			}
		};
	}
	
	public synchronized static BitmapManager getInstence(){
		if(instence==null){
			instence = new BitmapManager();
		}
		return instence;
	}

	public void init(Context context){
		try {
			AssetManager assetMng = context.getResources().getAssets();
			CLIP_BTN_ICON[0] = BitmapFactory.decodeStream(assetMng.open("new_clip_normal_icon.png"));
			CLIP_BTN_ICON[1] = BitmapFactory.decodeStream(assetMng.open("new_clip_select_icon.png"));

			ROTATE_BTN_ICON[0] = BitmapFactory.decodeStream(assetMng.open("new_rotate_normal_icon.png"));
			ROTATE_BTN_ICON[1] = BitmapFactory.decodeStream(assetMng.open("new_rotate_select_icon.png"));

			DELETE_BTN_ICON[0] = BitmapFactory.decodeStream(assetMng.open("new_delete_normal_icon.png"));
			DELETE_BTN_ICON[1] = BitmapFactory.decodeStream(assetMng.open("new_delete_select_icon.png"));

			SURE_BTN_ICON[0] = BitmapFactory.decodeStream(assetMng.open("new_sure_normal_icon.png"));
			SURE_BTN_ICON[1] = BitmapFactory.decodeStream(assetMng.open("new_sure_select_icon.png"));

			CANCEL_BTN_ICON[0] = BitmapFactory.decodeStream(assetMng.open("new_cancel_normal_icon.png"));
			CANCEL_BTN_ICON[1] = BitmapFactory.decodeStream(assetMng.open("new_cancel_select_icon.png"));

			IMG_TOOLS_TOP_ICON = BitmapFactory.decodeStream(assetMng.open("new_img_toolsbar_top_icon.png"));

			REMOTE_DCCONF_ON_OFF_BTN_ICON[0] = zoomImg(BitmapFactory.decodeStream(assetMng.open("start_remote_dcs_normal.png")),35,35);
			REMOTE_DCCONF_ON_OFF_BTN_ICON[1] = zoomImg(BitmapFactory.decodeStream(assetMng.open("start_remote_dcs_down.png")),35,35);
			REMOTE_DCCONF_ON_OFF_BTN_ICON[2] =  zoomImg(BitmapFactory.decodeStream(assetMng.open("stop_remote_dcs_normal.png")),35,35);
			REMOTE_DCCONF_ON_OFF_BTN_ICON[3] =  zoomImg(BitmapFactory.decodeStream(assetMng.open("stop_remote_dcs_down.png")),35,35);
			REMOTE_DCCONF_ON_OFF_BTN_ICON[4] =  zoomImg(BitmapFactory.decodeStream(assetMng.open("start_remote_dcs_disable.png")),35,35);
		}catch(Exception e){
			TPLog.printError("加载图片按钮控件素材时出现异常：");
			TPLog.printError(e);
		}
	}
	
	public void loadBitmap(String path,OnLoadCallBack callBack){

		TPLog.printKeyStatus("请求加载图片，图片地址："+ path);

		Bitmap bitmap = null;
		
		bitmap = mCache.get(path);
		
		if(bitmap!=null){
			if(callBack!=null){
				callBack.onLoadSuccess(bitmap);
			}
			return;
		}

		taskQueue.addMsg(new Task(path, callBack));
		
		if(mLoadThread==null||!mLoadThread.isAlive()){
			mLoadThread = new LoadThread();
			mLoadThread.start();
		}
	}
	
	public static Point getImageSize(String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
		BitmapFactory.decodeFile(path, options);
		
		Point size = new Point();
		size.x = options.outWidth;
		size.y = options.outHeight;

		int maxWidth = 1920;
		int maxHeight = 1080;

		int width_tmp=options.outWidth, height_tmp=options.outHeight;

//		float width_scale = 1f;
//		if(width_tmp>maxWidth){
//			width_scale = (float)maxWidth / (float)width_tmp;
//		}
//		float height_scale = 1f;
//		if(height_tmp > maxHeight ){
//			height_scale = (float)maxHeight / (float)height_tmp;
//		}
//
//		float scale = 1f;
//		if(width_scale>height_scale){
//			scale = height_scale;
//		}else{
//			scale = width_scale;
//		}
//
//		if(width_tmp>maxWidth||height_tmp>maxHeight) {
//			options.inSampleSize = 2;
//		}

		BitmapFactory.decodeFile(path, options);

		size.x = options.outWidth;
		size.y = options.outHeight;

		options = null;
		return size;
		
	}
	
  public  Bitmap loadBitmap(String path){
	  if(path==null)return null;
	  Bitmap bitmap = mCache.get(path);
	  if(bitmap!=null&&!bitmap.isRecycled()){
		  return bitmap;
	  }
	  bitmap = getBitmap(path);
	  if(bitmap==null){
		  return null;
	  }
	  mCache.put(path, bitmap);
	  return bitmap;
  }
	
  private  Bitmap getBitmap(String path){
	  File file = new File(path);
	  if(!file.exists()){
		  TPLog.printKeyStatus("图片文件不存在："+ path);
		  return null;
	  }
//
//	  BitmapFactory.Options options = new BitmapFactory.Options();
//	  options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
//	  Bitmap bitmap = null;
//	  BitmapFactory.decodeFile(path, options);
//
//	  int maxWidth = WhiteBoardUtils.IMG_MAX_WIDTH;
//	  int maxHeight = WhiteBoardUtils.IMG_MAX_HEIGHT;
//
//	  int width_tmp=options.outWidth, height_tmp=options.outHeight;
//
//	  float width_scale = 1f;
//	  if(width_tmp>maxWidth){
//		  width_scale = (float)maxWidth / (float)width_tmp;
//	  }
//	  float height_scale = 1f;
//	  if(height_tmp > maxHeight ){
//		  height_scale = (float)maxHeight / (float)height_tmp;
//	  }
//
//	  float scale = 1f;
//	  if(width_scale>height_scale){
//		  scale = height_scale;
//	  }else{
//		  scale = width_scale;
//	  }
//
//	//  TPLog.printError("scale----------------------->"+scale);
//
//	  options.outWidth =  (int)(options.outWidth*scale);
//	  options.outHeight =  (int)(options.outHeight*scale);
//	  if(width_tmp>maxWidth||height_tmp>maxHeight) {
//		  int w_be =  width_tmp/maxWidth;
//		  int h_be = height_tmp/maxHeight;
//		  int be = 1;
//		  if(w_be>h_be){
//			  be = w_be;
//		  }else{
//			  be = h_be;
//		  }
//		  if(be<=0){
//			  be = 1;
//		  }
//		  options.inSampleSize = be;
//	  }
//	  options.inPreferredConfig = Bitmap.Config.RGB_565;
//	  options.inPurgeable = true;
//	  options.inInputShareable=true;
//
//	  options.inJustDecodeBounds = false;

//	  bitmap = BitmapFactory.decodeFile(path, options);

	  //暂时先不优化
	 Bitmap bitmap =  BitmapFactory.decodeFile(path);
	 return bitmap;
  }




	
  public interface OnLoadCallBack{
	  void onLoadSuccess(Bitmap bitmap);
	  void onLoadFailed();
  }
	
	
  class Task{
	  public String path;
	  public OnLoadCallBack callBack;
	  
	  public Task(String path, OnLoadCallBack callBack){
		  this.path = path;
		  this.callBack = callBack;
	  }
  }

	// 等比缩放图片
	public static Bitmap zoomImg(Bitmap bm, int newWidth ,int newHeight){
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		return newbm;
	}

	// 等比缩放图片
	public static Bitmap zoomImg(Bitmap bm, Matrix matrix){
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		return newbm;
	}
	
  
  class LoadThread extends Thread{
	  @Override
	public void run() {
		  isLoading = true;
		  while(isLoading){
			  try {
				  
				Task mTask = taskQueue.nextMsg();
				String filePath = mTask.path;

				Bitmap bitmap = getBitmap(filePath);
				  if(filePath!=null&&bitmap!=null){
					  mCache.put(filePath, bitmap);
				  }

				if(mTask.callBack==null){
					return;
				}
				
				if(bitmap!=null){
					TPLog.printKeyStatus("加载图片成功！.00.");
					mTask.callBack.onLoadSuccess(bitmap);
				}else{
					mTask.callBack.onLoadFailed();
					TPLog.printKeyStatus("加载图片失败！.00.");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				TPLog.printError("加载图片时出现异常："+e);
			}
			  
		  }
	}
  }

	public void clearCache(){
		TPLog.printError("BitmapManager","清空缓存数据。。。");
		if (mCache != null) {
			if (mCache.size() > 0) {
				TPLog.printError("BitmapManager",
						"mCache.size() " + mCache.size());
				mCache.evictAll();
				TPLog.printError("BitmapManager", "mCache.size()" + mCache.size());
			}
		}
	}

	public void removeBitmap(String path){
		mCache.remove(path);
	}

}
