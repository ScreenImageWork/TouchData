package com.kedacom.touchdata.net.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by zhanglei on 2016/1/25.
 */
public class NetUtil {

    public static String IP = "127.0.0.1"; //服务端IP

    public static int PORT = 5000;  //服务端端口

    //建立tcp连接后发送连接事件
    public static final char EVEN_CONNECT_REQ = 61011;
    //请求加入会议
    public static final char EVEN_JOIN_CONFERENCE_REQ = 61015;

//	public static final char EVEN_JOIN_CONFERENCE_REQ = 61001;
    //请求创建会议
	public static final char EVEN_CREATE_CONFERENCE_REQ = 61002;
	//响应加入会议
	public static final char EVEN_JOIN_CONFERENCE_RSP = 61003;
    //响应创建会议
    public static final char EVEN_CREATE_CONFERENCE_RSP = 61004;
    //退出会议
    public static final char EVEN_QUIT_CONFERENCE_REQ = 61005;
    //请求服务器同步信息
    public static final char EVEN_SYNCHRONOUS_REQ = 61006;
    //服务响应同步数据消息
    public static final char EVEN_SYNCHRONOUS_RSP = 61007;

    //服务端响应客户端连接消息
    public static final char EVEN_CONNECT_RSP = 61014;
    //会议列表更新
    public static final char EVEN_CONFERENCE_LISG_RSP = 61013;
    //当前服务器连接人数通知
	public static final char EVEN_CONNECT_NUM_NTF = 61022;
    //广播邮箱服务器
    public static final char ev_User_Mail_Nty=61211;

    //服务端接收流量
    public static final char  EV_SV_CL_BUF_SIZE_REQ	= 61050; //向服务器请求当前接收流量大小
    public static final char  EV_SV_CL_BUF_SIZE_RSP = 61051; //服务器返回当前接收流量大小
	public static final char  EV_SV_CL_FILEEND = 61052;//向服务器发送 文件发送完毕消息

	public static final char EV_CL_SV_UPDATE_CONF_NUM = 61020; //当前与会人员列表通知

//    public static final int PACKET_MAX_SIZE = 4000; //数据包最大内容字节数
    public static final int PACKET_MAX_SIZE = 5000; //数据包最大内容字节数

    public static int currentServerRecFlow = PACKET_MAX_SIZE*4;

    public static int currentRqsImageId = 0;

    public static boolean isLogin = false;

    public static boolean isJoinMeeting = false; //是否加入本地会议

    public static String curMeetingName; //当前会议名称
    public static String curMeetingPwd; //当前会议密码
    
    public static ArrayList<Long> SevExistFiles = new ArrayList<Long>();
    
    public static long curUserId = 0; //当前用户在会议中的Id

	public static boolean isRemoteConf = false;

	public static boolean hasVideoConf = false;

	public static int curServerConnectNum = 0;

	public static int curJoinLocalConfMemberNum = 0;


	public static boolean hasMeeting(){
		if("iMixConf".equals(curMeetingName)){
			return true;
		}
		return false;
	}

    /**
	 * 验证邮箱
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email){
		boolean flag = false;
		try{
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		}catch(Exception e){
			flag = false;
		}
		return flag;
	}
	/**
	 * ip校验
	 * @param ipAddress
	 * @return
	 */
	public static  boolean isIpv4(String ipAddress) {

		String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
				+"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+"(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();

	}

	/**
	 * 对数组进行压缩
	 * @param buffer 需要被压缩的数组
	 * @return 压缩后的数组
	 */
	public static byte[] CompressBuffer(byte buffer[]){
		if(buffer==null){
			return null;
		}

		byte compressBuffer[] = new byte[buffer.length];

		Deflater compresser  = new Deflater();
		compresser.setInput(buffer);
		compresser.finish();
		int len= compresser.deflate(compressBuffer);

		byte result[] = new byte[len];

		for(int i=0;i<len;i++){
			result[i] = compressBuffer[i];
		}

		return result;
	}

	/**
	 * 解压字节数组
	 * @param buffer 压缩后的字节数组
	 * @return 解压后的字节数组
	 */
	public static byte[] UnCompressBuffer(byte buffer[]){
		if(buffer==null){
			return null;
		}
		byte result[] = null;
		try {
			byte outPut[] = new byte[buffer.length * 20];//压缩率未知，这里固定写成压缩率为75%

			Inflater decompresser = new Inflater();
			decompresser.setInput(buffer, 0, buffer.length);

			int len = decompresser.inflate(outPut);
			decompresser.end();

			result = new byte[len];

			for(int i=0;i<len;i++){
				result[i] = outPut[i];
			}

			outPut = null;
		}catch(Exception e){
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 拷贝scrBuffer 元素到desBuffer
	 * @param desBuffer
	 * @param scrBuffer
	 * @return
	 */
	public static int memcpy(byte desBuffer[],byte scrBuffer[]){

		if(scrBuffer==null){
			return 0;
		}

		if(desBuffer==null){
			desBuffer  = scrBuffer;
			return 0;
		}

		return memcpy(desBuffer,scrBuffer,0,0,scrBuffer.length);
	}



	public static int memcpy(byte desBuffer[],byte scrBuffer[],int desoffset){

		if(scrBuffer==null){
			return 0;
		}

		if(desBuffer==null){
			desBuffer  = scrBuffer;
			return 0;
		}

		return memcpy(desBuffer,scrBuffer,desoffset,0,scrBuffer.length);
	}


	/**
	 * scrBuffer 数组内的元素拷贝到desBuffer内  从desBuffer 的offset位开始
	 * @param desBuffer 目标数组
	 * @param scrBuffer 源数组
	 */
	public static int memcpy(byte desBuffer[],byte scrBuffer[],int dstOffset,int scrOffset,int cpyLength){

		if(dstOffset>=desBuffer.length||scrOffset>=scrBuffer.length||cpyLength>scrBuffer.length){
			return 0;
		}

		int endCpyIndex = cpyLength+scrOffset;

		if(endCpyIndex>scrBuffer.length){
			return 0;
		}

		for(int i = scrOffset;i<endCpyIndex;i++){
			desBuffer[dstOffset++] = scrBuffer[i];
		}
		scrBuffer = null;
		return cpyLength;
	}
	
	public static long getSysTimeMillis(){
		return SystemClock.uptimeMillis();
	}
	
	//获取图形ID目前也不知道是什么东西，暂时就定义为这样子
	public static int getGraphId(){
		return (int)getSysTimeMillis();
	}

	/**
	 * 打印数据数据， 一般调试时打印
	 * @param array
	 */
	public static void displayArray(byte array[]){
		//array = null;
		Log.e("msg","--------------------------------------------------------------∨");
		if(array==null)return;
		String msg = "";
		int length = array.length>100?array.length:array.length;

		for(int i = 0;i<length;i++){
			msg = msg + array[i] + ",";
			if(i!=0&&(i%15==0)){
				Log.e("msg",msg);
				msg = "";
			}
		}
		Log.e("msg",msg);
		Log.e("msg", "-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+-+-+∧");
	}
    /**
     * 解析服务发送过来的会议列表
     * @param msg 会议列表字符串
     * @return 会议列表数组
    public static void analysisConfList(String msg){
        if(msg==null)return;

        Utils.confList.clear();

        //去掉所有的空字符
        msg = msg.replaceAll(String.valueOf(((char)0)),"");

        //通过换行符号分割出会议名称  每个会议名称前面会带有0
        String baseConfList[] = msg.split("\\n");

        for(String conf : baseConfList){
            if(conf!=null&&conf.length()>0){
                MeetingEntity me = new MeetingEntity();
                String ispublic = conf.substring(0,1);
                if("0".equals(ispublic)){
                    me.setIsPublic(true);
                }else{
                    me.setIsPublic(false);
                }
                me.setName(conf.substring(1,conf.length()));
                Utils.confList.add(me);
            }
        }
    };
    */
}
