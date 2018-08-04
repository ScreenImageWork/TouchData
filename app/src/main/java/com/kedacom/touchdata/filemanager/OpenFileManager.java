package com.kedacom.touchdata.filemanager;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.vudroid.core.DecodeServiceBase;
import org.vudroid.core.codec.CodecPage;
import org.vudroid.pdfdroid.codec.PdfContext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.kedacom.tplog.TPLog;

import cn.wps.moffice.service.OfficeService;
import cn.wps.moffice.service.base.print.PrintOutItem;
import cn.wps.moffice.service.doc.Document;
import cn.wps.moffice.service.doc.Page;
import cn.wps.moffice.service.doc.PictureFormat;
import cn.wps.moffice.service.presentation.Presentation;
import cn.wps.moffice.service.presentation.Slide;
import cn.wps.moffice.service.spreadsheet.Workbook;
import cn.wps.moffice.service.spreadsheet.Workbooks;
import cn.wps.moffice.service.spreadsheet.Worksheet;

public class OpenFileManager extends FileType{

	// WPS AIDL服务
    public static final String OFFICE_SERVICE_ACTION = "cn.wps.moffice.service.OfficeService";

    public static final String selfPackageName =  "com.kedacom.touchdata";

	// 文件转图片，图片保存路径
	public static String docTempFileDir = FileUtils.OPENFILE_DOC_DIR;
	public static String excelTempFileDir = FileUtils.OPENFILE_EXCEL_DIR;
	public static String pptTempFileDir = FileUtils.OPENFILE_PPT_DIR;
	public static String pdfTempFileDir = FileUtils.OPENFILE_PDF_DIR;
	public static String txttempFileDir = FileUtils.OPENFILE_TXT_DIR;



	private static float scale = 1.5f;//生成图片的缩放比例


	public interface OnFileToImageListener {
		void onPageCount(int count, String checkCode);

		void onProgress(int progress, String checkCode);

		void onFialed(String fialedMsg,String path,String checkCode);

		void onComplete(List<String> files, String fileName, String checkCode);
	}

	/**
	 * 文件转图片 文件不会显示
	 * 
	 * @param context
	 *            上下文对象
	 * @param checkCode 校验码，主要是为了区分那个页面打开的文件 在回掉函数中会返回
	 * @param filePath
	 *            需要转换的文件路径
	 * @param listener
	 *            文件转换监听器，不需要监听的话传入null
	 */
	public static void fileToImage(Context context,String checkCode, String filePath,
			OnFileToImageListener listener) {
		openOrChangeFile(context,checkCode ,filePath, false, listener);
	}

	/**
	 * 打开文件 显示文件
	 * 
	 * @param context
	 *            上下文对象
	 * @param filePath
	 *            文件路径
	 */
	public static void openFile(Context context, String checkCode,String filePath) {
		openOrChangeFile(context, checkCode,filePath, true, null);
	}

	/**
	 * 打开或者转换文件
	 * 
	 * @param context
	 *            上下文对象
	 * @param filePath
	 *            文件路径
	 * @param isDisplay
	 *            是否显示文件，true 直接打开显示文件，false不显示文件直接将文件转换为图片
	 * @param listener
	 *            文件转换为图片监听器，不需要监听可以设置为null
	 */
	public static void openOrChangeFile(Context context,String checkCode, String filePath,
			boolean isDisplay, OnFileToImageListener listener) {


		int type = checkFileType(filePath);

		TPLog.printKeyStatus("打开文件，文件类型："+type+",isDisplay="+isDisplay);

		if(isDisplay){
			if(type!=FILE_TYPE_TXT&&type!=FILE_TYPE_DOC&&type!=FILE_TYPE_EXCEL&&type!=FILE_TYPE_PPT&&type!=FILE_TYPE_PDF){
				if(listener!=null){
					listener.onFialed("暂不支持该文件打开",filePath,checkCode);
				}
				return;
			}
		}


		switch (type) {
		case -1:
			if (listener != null) {
				listener.onFialed("文件类型不存在",filePath,checkCode);
			}
			break;
		case FILE_TYPE_TXT:
			doBindService(context,checkCode, filePath, FILE_TYPE_TXT, isDisplay, listener);
			break;
		case FILE_TYPE_IMAGE:
			if(!isDisplay){
				if(listener!=null){
					listener.onPageCount(1,checkCode);
					List<String> list = new ArrayList<String>();
					list.add(filePath);
					listener.onComplete(list,getFileName(filePath),checkCode);
				}
			}
			break;
		case FILE_TYPE_HTML:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		case FILE_TYPE_APK:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		case FILE_TYPE_VIDEO:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		case FILE_TYPE_VOICE:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		case FILE_TYPE_COMPRESS:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		case FILE_TYPE_DOC:
			doBindService(context,checkCode, filePath, FILE_TYPE_DOC, isDisplay, listener);
			break;
		case FILE_TYPE_EXCEL:
			doBindService(context,checkCode, filePath, FILE_TYPE_EXCEL, isDisplay,
					listener);
			break;
		case FILE_TYPE_PPT:
			doBindService(context,checkCode, filePath, FILE_TYPE_PPT, isDisplay, listener);
			break;
		case FILE_TYPE_PDF:
			pdfOpenOrToImage(context,checkCode, filePath, isDisplay, listener);
			break;
		case FILE_TYPE_OTHER:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		default:
			if(listener!=null){
				listener.onFialed("暂不支持该文件打开",filePath,checkCode);
			}
			break;
		}

	}

	// 检测文件类型
	public static int checkFileType(String filePath) {

		File file = new File(filePath);
		if (!file.exists())
			return -1;

		String name = file.getName();

		int type = FILE_TYPE_OTHER;

		if (name.endsWith(".txt")) {
			type = FILE_TYPE_TXT;
		} else if (name.endsWith(".jpg") || name.endsWith(".png")
				|| name.endsWith(".bmp") || name.endsWith(".jpeg")
				|| name.endsWith(".gif")) {
			type = FILE_TYPE_IMAGE;
		} else if (name.endsWith(".pdf")) {
			type = FILE_TYPE_PDF;
		} else if (name.endsWith(".doc") || name.endsWith(".docx")
				|| name.endsWith(".docm") || name.endsWith(".dotx")
				|| name.endsWith(".dotm")) {
			type = FILE_TYPE_DOC;
		} else if (name.endsWith(".xls") || name.endsWith(".xlsx")
				|| name.endsWith(".xlsm") || name.endsWith(".xltx")
				|| name.endsWith(".xltm") || name.endsWith(".xlsb")
				|| name.endsWith(".xlam")) {
			type = FILE_TYPE_EXCEL;
		} else if (name.endsWith(".ppt") || name.endsWith(".pptx")
				|| name.endsWith(".pptm") || name.endsWith(".ppsx")
				|| name.endsWith(".potx") || name.endsWith(".potm")
				|| name.endsWith(".ppam")) {
			type = FILE_TYPE_PPT;
		} else if (name.endsWith(".mp3") || name.endsWith(".wav")
				|| name.endsWith(".wma") || name.endsWith(".ogg")
				|| name.endsWith(".ape") || name.endsWith(".acc")) {
			type = FILE_TYPE_VOICE;
		} else if (name.endsWith(".mp4") || name.endsWith(".mpg")
				|| name.endsWith(".mpeg") || name.endsWith(".mpe")
				|| name.endsWith(".avi") || name.endsWith(".rmvb")
				|| name.endsWith(".rm") || name.endsWith(".asf")
				|| name.endsWith(".wmv") || name.endsWith(".mov")
				|| name.endsWith(".3gp") || name.endsWith(".flv")) {
			type = FILE_TYPE_VIDEO;
		} else if (name.endsWith(".rar") || name.endsWith(".zip")
				|| name.endsWith(".7z")) {
			type = FILE_TYPE_COMPRESS;
		} else if (name.endsWith(".apk")) {
			type = FILE_TYPE_APK;
		} else if (name.endsWith(".html") || name.endsWith(".htm")) {
			type = FILE_TYPE_HTML;
		} else {
			type = FILE_TYPE_OTHER;
		}

		return type;
	}

	private static OfficeService mService;
	private static ServiceConnection mConnection;

	// 绑定WPS AIDL服务 进行文件打开或者文件转图片
	public static void doBindService(final Context context,final String checkCode, final String path,
			final int type, final boolean isDisplay,
			final OnFileToImageListener listener) {
		TPLog.printRepeat("doBindService---------------->");
		// 获取连接实例
		mConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = OfficeService.Stub.asInterface(service);
				TPLog.printRepeat("WPS 连接成功");
				new OpenOfficeFileThread(context, checkCode,path, type, isDisplay,
						listener).start();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
				TPLog.printRepeat("WPS 断开链接");
			}
		};

		final Intent intentOfficeService = new Intent(OFFICE_SERVICE_ACTION);
		intentOfficeService.putExtra("DisplayView", isDisplay);// 不显示界面
		intentOfficeService.setPackage("com.kingsoft.moffice_pro");
		boolean boo = context.bindService(intentOfficeService, mConnection,
				Context.BIND_AUTO_CREATE);
		TPLog.printRepeat("绑定WPS服务------->"+boo);
	}

	// 解除 WPS AIDL服务
	public static void doUnbindService(Context context) {
		context.unbindService(mConnection);
	}

	// word文件打开显示或者转成图片
	private static void wordOpenOrToImage(String filePath,String checkCode, boolean isDisplay,
			Intent intent, OnFileToImageListener listener) {

		try {

			Document mDoc = mService.openWordDocument(filePath, "", intent);

			if (isDisplay) {
				return;
			}

			int count = mDoc.getPageCount();

			File file = new File(filePath);
			String name = file.getName();
			int pointIndex = name.indexOf(".");
			if (pointIndex != -1) {
				name = name.substring(0, pointIndex);
			}
			file = null;

			if (listener != null) {
				listener.onPageCount(count,checkCode);
			}

			List<String> list = new ArrayList<String>();

			String docFilePath = docTempFileDir + name;

			File docFileDir = new File(docFilePath);

			if(!docFileDir.exists()||!docFileDir.isDirectory()){
				docFileDir.mkdirs();
				docFileDir.mkdir();
			}

			for (int i = 0; i < count; i++) {
				Page page = mDoc.getPage(i);

				float width = page.getWidth()*scale;
				float height = page.getHeight()*scale;
				TPLog.printKeyStatus("文档原始大小，width:"+page.getWidth()+",height:"+page.getHeight()+",缩放后大小，width:"+width,",height:"+height);
				String picName = docFileDir + File.separator+ i + ".jpeg";
				boolean boo = page.saveToImage(picName, PictureFormat.JPEG,
						100, width, height, 64, PrintOutItem.wdPrintContent);

				TPLog.printKeyStatus(picName);

				list.add(picName);

				if (listener != null) {
					listener.onProgress(i,checkCode);
				}
			}

			if (listener != null) {
				listener.onComplete(list,name,checkCode);
			}

			mDoc.close();

		} catch (Exception e) {
			TPLog.printError("打开word文件时，出现异常：");
			TPLog.printError(e);
			if (listener != null) {
				listener.onFialed(e.toString(),filePath,checkCode);
			}
		}
	}

	// excel打开显示或者转换成图片
	private static void excelOpenOrToImage(String filePath,String checkCode, boolean isDisplay,
			Intent intent, OnFileToImageListener listener) {
		try {

			Workbooks mWorkbooks = mService.getWorkbooks();

			Workbook mExcel = mWorkbooks.openBookEx(filePath, "", intent);

			if (isDisplay) {
				return;
			}

			File file = new File(filePath);
			String name = file.getName();
			int pointIndex = name.indexOf(".");
			if (pointIndex != -1) {
				name = name.substring(0, pointIndex);
			}
			file = null;

			int sheetCount = mExcel.getSheetCount();

			if (listener != null) {
				listener.onPageCount(sheetCount,checkCode);
			}

			String fileDirStr = excelTempFileDir + name;
			File fileDir = new File(fileDirStr);
			if (!fileDir.exists()) {
				fileDir.mkdir();
			}

			List<String> list = new ArrayList<String>();

			for (int i = 0; i < sheetCount; i++) {
				Worksheet mWorksheet = mExcel.getWorksheet(i);
				String picPath = fileDirStr + File.separator + i;
				File picFile = new File(picPath);

				if (!picFile.exists()) {
					picFile.mkdir();
				}

				boolean boo = mWorksheet.saveToImage(picPath,
						PictureFormat.JPEG, 50, 1f);
				File flies[] = picFile.listFiles();
				if(flies!=null) {
					for (File f:flies) {
						list.add(f.getAbsolutePath());
					}
				}

				if (listener != null)
					listener.onProgress(i,checkCode);
			}

			if (listener != null) {
				listener.onComplete(list,name,checkCode);
			}

			mExcel.close();

		} catch (Exception e) {
			TPLog.printError("打开Excel时出现异常：");
			TPLog.printError(e);
			if (listener != null) {
				listener.onFialed(e.toString(),filePath,checkCode);
			}
		}
	}

	// ppt打开显示或者ppt转图片
	private static void pptOpenOrToImage(String filePath,String checkCode, boolean isDisplay,
			Intent intent, OnFileToImageListener listener) {

		try {
			Presentation mPPT = mService.openPresentation(filePath, "", intent);

			if (isDisplay) {
				return;
			}

			int count = mPPT.getPageCount();

			File file = new File(filePath);
			String name = file.getName();
			String tempName = name;
			int pointIndex = name.indexOf(".");
			if (pointIndex != -1) {
				tempName = name.substring(0, pointIndex);
			}
			file = null;

			if (listener != null) {
				listener.onPageCount(count,checkCode);
			}

			List<String> list = new ArrayList<String>();

			for (int i = 0; i < count; i++) {
				Slide mSlide = mPPT.getSlide(i);
				String fileName = mSlide.saveToImage(pptTempFileDir + tempName,
						PictureFormat.JPEG, 100, 1);
				list.add(fileName);
				if (listener != null) {
					listener.onProgress(i,checkCode);
				}
			}

			if (listener != null) {
				listener.onComplete(list,name,checkCode);
			}

			mPPT.close();
		} catch (Exception e) {
			TPLog.printError("打开ppt时出现异常：");
			TPLog.printError(e);
			if (listener != null) {
				listener.onFialed(e.toString(),filePath,checkCode);
			}
		}

	}

	// txt文件打开或者转图片
	public static void txtOpenOrToImage(final Context context, final String checkCode,final String path,
			final boolean isDisplay,final OnFileToImageListener listener) {
		
		if (isDisplay) {// 显示文件 就不用转成图片了

			return;
		}
		
		//生成后的图片的宽度和高度
        final int bitmapWidth = 540;
        final int bitmapHeight = 720;
		new Thread(){
			public void run() {
				try {

					List<String> list = new ArrayList<String>();
					String code = codeString(path);
					String name = getFileName(path);
					
					Paint paint = new Paint();
					paint.setColor(Color.BLACK);
					paint.setAntiAlias(true);
					paint.setTextSize(15);
					Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
					Canvas mCanvas = new Canvas(bitmap);
					 mCanvas.drawColor(Color.WHITE);
					float fontSize = paint.measureText("整");
					
					FontMetrics fm = paint.getFontMetrics();   
				    int fontHeight = (int) Math.ceil(fm.descent - fm.top) + 2;  
					
					int count = (int)(bitmapWidth/fontSize) -1;
					int lineMaxNum = (int)((float)(bitmapHeight)/(fontHeight+10));
					if(listener!=null){
						listener.onPageCount(lineMaxNum,checkCode);
					}
					
					
					long filesize  = new File(path).length();
					int pageCount = (int)((filesize/2)/(count*lineMaxNum))+1;
					
					File file = new File(path);
					
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
					
					int len = -1;
					
					char buffer[] = new char[count];
					
					int fileIndex = 0;
					int lineNum = 0;
					
					int textPadding = (int)fontSize+10;
					
					String fileName = txttempFileDir + name ;
					File f = new File(fileName);
					if(!f.exists()||!f.isDirectory()){
						f.mkdir();
					}
					fileName+= File.separator;
					while((len = in.read(buffer))!=-1){
						String content = new String(buffer,0,len);
						
						content = EncodingUtils.getString(content.getBytes("utf-8"),"utf-8");						
						
						mCanvas.drawText(content, fontSize, textPadding, paint);
						textPadding = (int)(textPadding+fontHeight)+10;
						lineNum++;
						if(lineNum>=lineMaxNum){
							lineNum = 0;
							textPadding = 10;
							textPadding = (int)fontHeight+10;
							saveMyBitmap(fileName+fileIndex+".jpeg", bitmap);
							list.add(fileName+fileIndex+".jpeg");
							 bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
							 mCanvas = new Canvas(bitmap);
							 mCanvas.drawColor(Color.WHITE);
							 fileIndex++;
							 if(listener!=null){
									listener.onProgress(fileIndex,checkCode);
								}
						}
					}
					
					in.close();
					
					saveMyBitmap(fileName+fileIndex+".jpeg", bitmap);
					list.add(fileName+fileIndex+".jpeg");
					
					 if(listener!=null){
						listener.onComplete(list,name+".txt",checkCode);
						}
					
				} catch (Exception e) {
					TPLog.printError("打开text文件时出现异常：");
					TPLog.printError(e);
					if (listener != null) {
						listener.onFialed(e.toString(),path,checkCode);
					}
				}
			};
		}.start();
		

	}

	// PDF打开或者转图片
	public static void pdfOpenOrToImage(final Context context,final String checkCode, final String path,
			boolean isDisplay, final OnFileToImageListener listener) {

			if (isDisplay) {// 显示文件 就不用转成图片了

				return;
			}

			new Thread(){
				@Override
				public void run() {
			try {
			DecodeServiceBase decodeService = new DecodeServiceBase(
					new PdfContext());
			decodeService.setContentResolver(context.getContentResolver());
			decodeService.open(Uri.fromFile(new File(path)));

			int count = decodeService.getPageCount();

			File file = new File(path);
			String name = file.getName();
			int pointIndex = name.indexOf(".");
			if (pointIndex != -1) {
				name = name.substring(0, pointIndex);
			}
			file = null;

			File file2 = new File(pdfTempFileDir + name);
			if (!file2.exists()) {
				file2.mkdir();
			}

			if (listener != null) {
				listener.onPageCount(count,checkCode);
			}

			List<String> list = new ArrayList<String>();

			for (int i = 0; i < count; i++) {
				CodecPage mCodecPage = decodeService.getPage(i);
				int width = mCodecPage.getWidth();
				int height = mCodecPage.getHeight();

				RectF rect = new RectF(0, 0, 1, 1);

				Bitmap bitmap = mCodecPage.renderBitmap(width, height, rect);

				if (bitmap == null) {
					return;
				}

				String fileName = pdfTempFileDir + name + File.separator + i
						+ ".jpeg";

				saveMyBitmap(fileName, bitmap);

				list.add(fileName);

				if (listener != null) {
					listener.onProgress(i,checkCode);
				}

			}

			if (listener != null) {
				listener.onComplete(list,name+".pdf",checkCode);
			}

		} catch (Exception e) {
				TPLog.printError("打开text文件时出现异常：");
				TPLog.printError(e);
				if (listener != null) {
					listener.onFialed(e.toString(),path,checkCode);
				}
		}
			}
		}.start();


	}
	
	
	/** 
	 * 判断文件的编码格式 
	 * @param fileName :file 
	 * @return 文件编码格式 
	 * @throws Exception 
	 */  
	public static String codeString(String fileName) throws Exception{  
	    BufferedInputStream bin = new BufferedInputStream(  
	    new FileInputStream(fileName));  
	    int p = (bin.read() << 8) + bin.read();  
	    String code = null;  
	      
	    switch (p) {  
	        case 0xefbb:  
	            code = "UTF-8";  
	            break;  
	        case 0xfffe:  
	            code = "Unicode";
	            break;  
	        case 0xfeff:  
	            code = "UTF-16BE";  
	            break;  
	        default:  
	            code = "GB2312";  
	    }  
	    return code;  
	}  
	
	
	//获取文字的高度
	public static int getFontHeight(float fontSize)   
	{   
	     Paint paint = new Paint();   
	     paint.setTextSize(fontSize);   
	     FontMetrics fm = paint.getFontMetrics();   
	    return (int) Math.ceil(fm.descent - fm.top) + 2;   
	}   



	
	//读取文件内容
	public static String getFileContent(String filePath) throws Exception{
		
		String content = "";
		
		File file = new File(filePath);
		
		InputStream in = new FileInputStream(file);
		
		int len = -1;
		
		byte buffer[] = new byte[1024];
		
		while((len = in.read(buffer))!=-1){
			content = content + new String(buffer,0,len);
		}
		
//		byte contentBytes[] = content.getBytes("GB2312");
//		content = new String(contentBytes);
		
		in.close();
		
		return content;
		
	}

	// 获取文件名称 不带后缀
	public static String getFileName(String filepath) {
		String name = "";
		File file = new File(filepath);
		name = file.getName();
		int pointIndex = name.indexOf(".");
		if (pointIndex != -1) {
			name = name.substring(0, pointIndex);
		}

		return name;
	}

	// 将bitmap转成图片
	public static void saveMyBitmap(String bitName, Bitmap mBitmap) {
		File f = new File(bitName);
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//mBitmap.recycle();
	}

	static class OpenOfficeFileThread extends Thread {

		private String path;

		private Context mContext;

		private int type;

		private boolean isDisplay;

		private OnFileToImageListener listener;
		
		private String checkCode ;

		public OpenOfficeFileThread(Context context,String checkCode, String filePath, int type,
				boolean isDisplay, OnFileToImageListener listener) {
			path = filePath;
			mContext = context;
			this.type = type;
			this.isDisplay = isDisplay;
			this.listener = listener;
			this.checkCode = checkCode;
		}

		@Override
		public void run() {
			try {

				Intent intent = new Intent();
				Bundle data = new Bundle();
				data.putString("OpenMode", "Normal");
				data.putString("ThirdPackage", selfPackageName);
				data.putBoolean("SendCloseBroad", true);
				data.putBoolean("FairCopy", false);
				data.putString("UserName", "");
				intent.putExtras(data);

				switch (type) {
				case FILE_TYPE_DOC:
					wordOpenOrToImage(path,checkCode, isDisplay, intent, listener);
					break;
				case FILE_TYPE_EXCEL:
					excelOpenOrToImage(path,checkCode, isDisplay, intent, listener);
					break;
				case FILE_TYPE_PPT:
					pptOpenOrToImage(path,checkCode, isDisplay, intent, listener);
					break;
				case FILE_TYPE_TXT:
					txtOpenOrToImage(mContext,checkCode,path, isDisplay,listener);
					 break;
				}

				doUnbindService(mContext);

			} catch (Exception e) {
				Log.e("error", "文件转换出错啦----》" + e.getMessage());
			}
		}
	}
}
