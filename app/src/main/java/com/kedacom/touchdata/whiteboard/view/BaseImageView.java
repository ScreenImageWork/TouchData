package com.kedacom.touchdata.whiteboard.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager.OnLoadCallBack;
import com.kedacom.tplog.TPLog;

public class BaseImageView extends View implements IImageView,OnLoadCallBack{
	
	private BitmapManager mBitmapManager;
	
	private Image curImage;
	
	private float scale = 1.0f;
	
	private int angle = 0;
	
	private float offsetX = 0;

	private float offsetY =0;

	private Bitmap curBitmap;

	private Paint paint;
	
	public BaseImageView(Context context){
		super(context);
		init();
	}

	public BaseImageView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public BaseImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		mBitmapManager = BitmapManager.getInstence();
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
	}
	
	@Override
	public void drawImage(Image mImage) {
		curImage = mImage;
		invalidate();
		if(curImage==null)return;
		mBitmapManager.loadBitmap(mImage.getFilePath(), this);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		//clearCanvas(canvas);
		displayImage(curBitmap , canvas);
	}

	@Override
	public void rotate(int angle) {
		this.angle = angle;
		invalidate();
		//drawImage(curImage);
	}

	@Override
	public void translate(float ox, float oy) {
		offsetX = ox;
		offsetY = oy;
		//drawImage(curImage);
		invalidate();
	}

	@Override
	public void scale(float scale) {
		this.scale = scale;
		//drawImage(curImage);
		invalidate();
	}

	@Override
	public Image getImage() {
		return curImage;
	}

	private void displayImage(Bitmap bitmap,Canvas canvas){
		if(bitmap==null||curImage == null)return;
		
		long l1 = System.currentTimeMillis();
		
		//计算当前屏幕中心点
		float px = (WhiteBoardUtils.whiteBoardWidth )/2f - offsetX ;
		float py = (WhiteBoardUtils.whiteBoardHeight )/2f - offsetY ;
		
		canvas.drawColor(Color.TRANSPARENT);
		canvas.save();
		canvas.translate(offsetX, offsetY);
		canvas.rotate(angle, px, py);
		canvas.scale(scale, scale, px, py);
		
		canvas.drawBitmap(bitmap, curImage.getX(), curImage.getY(), paint);
		
		canvas.restore();
		

		long l2 = System.currentTimeMillis();
		
		long l3 = l2 - l1;
		
	}

	@Override
	public void onLoadSuccess(Bitmap bitmap) {
		curBitmap = bitmap;
		TPLog.printKeyStatus("加载图片成功--->显示");
		hand.sendEmptyMessage(100);
	}

	@Override
	public void onLoadFailed() {//后续进行处理
		TPLog.printKeyStatus("00.加载图片失败");
	}

	@Override
	public Bitmap saveToBitmap() {//需要保存时再实现
		return null;
	}
	
	@SuppressLint("HandlerLeak") 
	Handler hand = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == 100){
				invalidate();
			}
		};
	};
}
