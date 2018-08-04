package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

public class Image implements Parcelable{

	private int id;
	
    private final int type = 10;
	
	private String filePath = "";
	
	private float width;
	
	private float height;
	
	private float x;
	
	private float y;
	
    private String fileName;

    private long fileSize;

    private int dwCurBlock;//当前已下载字节数

    private int dwBlockSize = 32000;//一次下载的字节数 貌似是写死的
    
    private int subpageIndex = 0;
    
    private boolean isExistOnServer = false;//服务器上是否有该图片 0存在 1不存在

	private Paint mPaint;

	private Bitmap curBitmap;

    public Image(){
    	id = (int)WhiteBoardUtils.getId();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getDwCurBlock() {
		return dwCurBlock;
	}

	public void setDwCurBlock(int dwCurBlock) {
		this.dwCurBlock = dwCurBlock;
	}

	public int getDwBlockSize() {
		return dwBlockSize;
	}

	public void setDwBlockSize(int dwBlockSize) {
		this.dwBlockSize = dwBlockSize;
	}

	public int getSubpageIndex() {
		return subpageIndex;
	}

	public void setSubpageIndex(int subpageIndex) {
		this.subpageIndex = subpageIndex;
	}

	public boolean isExistOnServer() {
		return isExistOnServer;
	}

	public void setExistOnServer(boolean isExistOnServer) {
		this.isExistOnServer = isExistOnServer;
	}

	public int getType() {
		return type;
	}
	
	
    public boolean isDlSuccess() {
        if(fileSize==0){
            return false;
        }
        return fileSize == dwCurBlock;
    }

	public void draw(Canvas canvas){
		if(dwCurBlock==0||dwCurBlock!=fileSize)return;
		if(curBitmap==null||curBitmap.isRecycled()) {
			curBitmap = BitmapManager.getInstence().loadBitmap(getFilePath());
		}
		if(curBitmap==null)return;
		canvas.drawBitmap(curBitmap,x,y,mPaint);
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public Image(Parcel arg0){
		id = arg0.readInt();
		filePath = arg0.readString();
		width = arg0.readFloat();
		height = arg0.readFloat();
		x = arg0.readFloat();
		y = arg0.readFloat();
		fileName = arg0.readString();
		fileSize = arg0.readLong();
		dwCurBlock = arg0.readInt();
		dwBlockSize = arg0.readInt();
		subpageIndex = arg0.readInt();
		
		boolean boo[] = new boolean[1];
		arg0.readBooleanArray(boo);
		isExistOnServer = boo[0]; 
	}
	
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeInt(id);
		arg0.writeString(filePath);
		arg0.writeFloat(width);
		arg0.writeFloat(height);
		arg0.writeFloat(x);
		arg0.writeFloat(y);
		arg0.writeString(fileName);
		arg0.writeLong(fileSize);
		arg0.writeInt(dwCurBlock);
		arg0.writeInt(dwBlockSize);
		arg0.writeInt(subpageIndex);
		arg0.writeBooleanArray(new boolean[]{isExistOnServer});
	}

	
	public static final Creator<Image> CREATOR = new Creator<Image>()
			{

				@Override
				public Image createFromParcel(Parcel arg0) {
					
					return new Image(arg0);
				}

				@Override
				public Image[] newArray(int arg0) {
					
					return new Image[arg0];
				}
		   
			 };

	public void recycledBitmap(){
		if(curBitmap!=null&&!curBitmap.isRecycled()){
			curBitmap.recycle();
			curBitmap = null;
		}
		BitmapManager.getInstence().removeBitmap(getFilePath());
	}

	public void destroy(){
		mPaint = null;
	}
}
