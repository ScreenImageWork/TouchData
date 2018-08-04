package com.kedacom.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.tplog.TPLog;

public class CrashExceptionHandler implements UncaughtExceptionHandler {

	// CrashExceptionHandler 实例
	private static CrashExceptionHandler INSTANCE = new CrashExceptionHandler();

	// 程序的 Context 对象
	private Context mContext;
	
	// 系统默认的 UncaughtException 处理类
	private UncaughtExceptionHandler mDefaultHandler;
	
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	//用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

	private String logPath;

	/** 保证只有一个 CrashHandler 实例 */
	private CrashExceptionHandler() {
		
	}
	
	/** 获取 CrashHandler 实例 ,单例模式 */
	public static CrashExceptionHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 *
	 * @param context
	 *
	 */
	public void init(Context context) {
		mContext = context;
		this.logPath = logPath;
		// 获取系统默认的 UncaughtException 处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该 CrashHandler 为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 初始化
	 *
	 * @param context
	 * 
	 */
	public void init(Context context,String logPath) {
		mContext = context;
		this.logPath = logPath;
		// 获取系统默认的 UncaughtException 处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该 CrashHandler 为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	
	
	/**
	 * 当 UncaughtException 发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			TPLog.printError((Exception)ex);
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	/**
	 * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成
	 * 
	 * @param ex
	 * @return true：如果处理了该异常信息；否则返回 false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 使用 Toast 来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉，数据协作程序出现异常，即将退出。", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();

		//交给LogUtil进行处理
		//Log.e("error", "应用崩溃异常",ex);
		saveCrashInfo2File(ex);
		//android.os.Process.killProcess(android.os.Process.myPid());
		return true;
	}


	/**
	 * 保存错误信息到文件中
	 *
	 * @param ex
	 * @return  返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			//TPLog.setLocalSaveLogParams(FileUtils.LOG_DIR,10,TPLog.LOG_LVL_ERROR);
			TPLog.printError(sb.toString());
//			long timestamp = System.currentTimeMillis();
//			String time = formatter.format(new Date());
//			//String fileName = "error" + time + "-" + timestamp + ".log";
//			String fileName = "error" + time +  ".log";
//			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//				String path = logPath;
//
//				if(path.endsWith("/")){
//					path = path.substring(0,path.length()-1);
//				}
//
//				File dir = new File(path);
//				if (!dir.exists()) {
//					dir.mkdirs();
//					dir.mkdir();
//				}
//
//				path = path + File.separator;
//
//				FileOutputStream fos = new FileOutputStream(path + fileName);
//				fos.write(sb.toString().getBytes());
//				fos.close();
//			}
			return "";
		} catch (Exception e) {
			Log.e("error", "an error occured while writing file...", e);
		}
		return null;
	}

}
