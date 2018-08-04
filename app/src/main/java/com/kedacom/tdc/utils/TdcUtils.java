package com.kedacom.tdc.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kedacom.tplog.TPLog;

public class TdcUtils {

	private int QR_WIDTH = 200;
	private int QR_HEIGHT = 200;
	
	private String fileDir = Environment.getExternalStorageDirectory().getPath();
	
	/**
	 * 生成不加密的二维码
	 * @param data 需要生成二维码的数据  
	 * @param width  生成二维码的宽度
	 * @param height  生成二维码的高度
	 * @return   Bitmap  生成的二维码
	 */
    public Bitmap createTdc(String data,int width,int height) {
        TPLog.printError("createTdc begin...");
    	 Bitmap bitmap = null;
    	 if(width>0){
    	 QR_WIDTH = width;
    	 }
    	 if(height>0){
    	 QR_HEIGHT = height;
    	 }
        try {
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();

            String text = data;

            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);
            
            TPLog.printError("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            
            
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int maxy = Integer.MIN_VALUE;
            
            //对二维码进行裁剪只获取有效的二维码部分
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        
                        if(minx>x)
                        {
                        	minx = x;
                        }
                        
                        if(miny>y){
                        	miny = y;
                        }
                        
                        if(maxx<x){
                        	maxx = x;
                        }
                        
                        if(maxy<y){
                        	maxy = y;
                        }
                        
                    } 
                }
            }
            
            
            int w = maxx - minx;
            int h = maxy - miny;
            
            String msg = "maxx="+maxx+",minx="+minx+",maxy="+maxy+",miny="+miny;
            TPLog.printError(msg);
            TPLog.printError("二维码有效宽高度--->w:" + w + ",h:"
                    + h);
            
            int[] pixels = new int[w * h+2];
            
            int index = 0;
            
            for (int y = miny; y < maxy; y++) {
                for (int x = minx; x < maxx; x++) {
                    if (bitMatrix.get(x, y)) {
                    	pixels[index++] = 0xff000000;
                    }else{
                    	pixels[index++] = 0xffffffff;
                    }
                }
            }
            
            bitmap = Bitmap.createBitmap(w, h,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            
            TPLog.printError(data+"->二维码，width="+QR_WIDTH+",height="+QR_HEIGHT);

        } catch (WriterException e) {
        	TPLog.printError(data+"->二维码创建失败,失败原因："+e);
            e.printStackTrace();
        }
        return bitmap;
    }
    
    
    public Bitmap createEncryptionTdc(String data,int width,int height){
    	//对数据进行加密
    	data = EncryptionUtils.encode(data);
    	return createTdc(data,width,height);
    }
    
    
    /**
     * 在二维码中间添加Logo图案
     */
    public static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
 
        if (logo == null) {
            return src;
        }
 
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
 
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
 
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
 
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
 
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            TPLog.printError("二维码添加Logo失败,失败原因："+e);
            e.getStackTrace();
        }
 
        return bitmap;
    }
    
    
    private  void saveBitmap(Bitmap bm,String url) {
    try{
    String fileName = getFileName(url);
    OutputStream out = new FileOutputStream(new File(fileName));
    bm.compress(Bitmap.CompressFormat.PNG, 100, out);
    out.close();
    }catch(Exception e){
    }
    }
    
    public Bitmap getBitmap(String url){
    	String fileName = getFileName(url);
    	File file = new File(fileName);
    	
    	if(!file.exists()){
    		return null;
    	}
    	Bitmap bitmap = BitmapFactory.decodeFile(fileName);
    	return bitmap;
    }
    
    public String getFileName(String url){
    url = MD5.stringMD5(url);
    String fileName = "qr_"+url+".png";
    fileName = fileDir + File.separator + fileName;
    return fileName;
    }
    
    
}
