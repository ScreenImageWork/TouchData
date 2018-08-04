package com.kedacom.touchdata.whiteboard.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public final class LogUtil {
	
	/**
	 * 只打印当前需要打印的Log
	 */
	private static int showLogLevels[] = {Log.DEBUG,Log.VERBOSE,Log.INFO,Log.ERROR,Log.WARN};
	
	/**
	 * 只保存当前需要保存的日志
	 */
	private static int saveLogLevels[] = {Log.DEBUG,Log.VERBOSE,Log.INFO,Log.ERROR,Log.WARN};
	
	private static String logSaveDir = "";
	
	private static SimpleDateFormat dateFormat; 
	
	private static SimpleDateFormat nameFormat;
	
	private LogUtil(){
		
	}
	
	/**
	 * 设置Log日志保存的位置
	 * @param dir  日志保存位置
	 */
	public static void setLogSaveDir(String dir){
		logSaveDir = dir;
	}
	
	/**
	 * 设置那些级别的Log需要打印 默认打印所有 传入null都不打印
	 * @param levels int数组，取值 Log.DEBUG Log.ERROR Log.INFO Log.VERBOSE Log.WARN等
	 *                           Log.VERBOSE< Log.DEBUG < Log.INFO < Log.WARN < Log.ERROR 
	 */
	public static void setShowLogLevel(int levels[]){
		showLogLevels = levels;
	}
	
	/**
	 * 设置需要显示日志的最小级别 大于该级别的日志都将会被显示
	 * @param level 取值 Log.DEBUG Log.ERROR Log.INFO Log.VERBOSE Log.WARN
	 *                  Log.VERBOSE< Log.DEBUG < Log.INFO < Log.WARN < Log.ERROR 
	 */
	public static void setShowLogLevel(int level){
		int count = Log.ERROR - level;
		if(count<0){
			setShowLogLevel(new int[0]);
			return;
		}
		count = count + 1;
		
		if(count>5){
			count = 5;
			level = 2;
		}
		
		int levels[] = new int[count]; 
		
		for(int i = level;i<=Log.ERROR;i++){
			levels[i - level] = i;
		}
		
		setShowLogLevel(levels);
	}
	
	/**
	 *设置需要保存的日志级别  默认保存所有 传入空都不保存
	 * @param levels int数组，取值 Log.DEBUG Log.ERROR Log.INFO Log.VERBOSE Log.WARN等
	 */
	public static void setSaveLevel(int levels[]){
		saveLogLevels = levels;
	}
	
	/**
	 * 设置需要显示日志的最小级别 大于该级别的日志都将会被显示
	 * @param level 取值 Log.DEBUG Log.ERROR Log.INFO Log.VERBOSE  Log.WARN
	 */
	public static void setSaveLevel(int level){
		int count = Log.ERROR - level;
		if(count<0){
			setSaveLevel(new int[0]);
			return;
		}
		count = count + 1;
		
		if(count>5){
			count = 5;
			level = 2;
		}
		
		int levels[] = new int[count]; 
		
		for(int i = level;i<=Log.ERROR;i++){
			levels[i - level] = i;
		}
		
		setSaveLevel(levels);
	}
	
	public static void i(String tag,String msg){
		println(tag,Log.INFO,msg);
	}
	
	public static void i(String tag,String msg,Throwable tr){
		println(tag,Log.INFO,msg,tr);
	}
	
	public static void d(String tag,String msg){
		println(tag,Log.DEBUG,msg);
	}
	
	public static void d(String tag,String msg,Throwable tr){
		println(tag,Log.DEBUG,msg,tr);
	}
	
	public static void e(String tag,String msg){
		println(tag,Log.ERROR,msg);
	}
	
	public static void e(String tag,String msg,Throwable tr){
		println(tag,Log.ERROR,msg,tr);
	}
	
	public static void w(String tag,String msg){
		println(tag,Log.WARN,msg);
	}
	
	public static void w(String tag,String msg,Throwable tr){
		println(tag,Log.WARN,msg,tr);
	}
	
	public static void v(String tag,String msg){
		println(tag,Log.VERBOSE,msg);
	}
	
	public static void v(String tag,String msg,Throwable tr){
		println(tag,Log.VERBOSE,msg,tr);
	}
	
	/**
	 * 打印日志
	 * @param tag 日志标签
	 * @param priority 日志级别 取值 Log.DEBUG Log.ERROR Log.INFO Log.VERBOSE Log.WARN
	 * @param msg 日志内容
	 */
	public synchronized static void println(String tag,int priority, String msg){
		
		String pStr = "";
		boolean isShow = false;
		if(showLogLevels!=null&&isContainsParams(showLogLevels,priority)){
			isShow = true;
		}
		
		switch(priority){
		case Log.DEBUG:
			if(isShow)
			Log.d(tag,msg);
			pStr = "D";
			break;
		case Log.ERROR:
			if(isShow)
			Log.e(tag,msg);
			pStr = "E";
			break;
		case Log.INFO:
			if(isShow)
			Log.i(tag,msg);
			pStr = "I";
			break;
		case Log.WARN:
			if(isShow)
			Log.w(tag,msg);
			pStr = "W";
			break;
		case Log.VERBOSE:
			if(isShow)
			Log.v(tag,msg);
			pStr = "V";
			break;
		}
		
		if(saveLogLevels!=null&&isContainsParams(saveLogLevels,priority)){
		saveLog(tag,pStr,msg);
		}
	}
	
	public static void println(String tag,int priority, String msg,Throwable tr){
		msg = msg + "\n"+getStackTraceString(tr);
		println(tag, priority, msg);
	}
	
	 /**
     * 读取异常的堆栈信息
     * @param tr 需要读取的异常
     */
	public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

	
	private static void checkDir(String logDir){
		if(logSaveDir==null||logSaveDir.trim().isEmpty()){
			resetLogDir();
		}
		File file = new File(logSaveDir);
		if(file.exists()){
			return;
		}
		
		boolean boo = file.mkdirs();
		boolean boo2 = file.mkdir();
		
		file = null;
		
		if(!boo&&!boo2){
			resetLogDir();
			checkDir(logSaveDir);
		}
	}
	
	private static void resetLogDir(){
		logSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		logSaveDir = logSaveDir + File.separator + "KDLOG"; 
	}
	
	@SuppressLint("SimpleDateFormat") 
	private static String getCurTime(){
		if(dateFormat==null){
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		String timeStr = dateFormat.format(new Date());
		return timeStr;
	}
	
	@SuppressLint("SimpleDateFormat") 
	private static String getLogFileName(){
		if(nameFormat==null){
		nameFormat = new SimpleDateFormat("yyyyMMdd");
		}
		String fileName = nameFormat.format(new Date());
		fileName = fileName + ".log";
	    return fileName;
	}
	
	private static boolean isContainsParams(int levels[],int params ){
		int count = levels.length;
		for(int i = 0;i<count;i++){
			if(levels[i]==params){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 保存日志到本地
	 * 日志格式：level----time----tag----msg
	 * @param tag 日志标签
	 * @param pStr 日志类型
	 * @param msg 日志内容
	 * 
	 */
	private static void saveLog(String tag,String pStr,String msg){
		try{
		checkDir(logSaveDir);
		File file = new File(logSaveDir + File.separator + getLogFileName());
		msg = pStr + "-----" + getCurTime() + "-----" + tag + "----" + msg + "\n";
		FileOutputStream out = new FileOutputStream(file,true);
		out.write(msg.getBytes());
		out.flush();
		out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
