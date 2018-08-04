package com.kedacom.osp;

/**
 * Created by zhanglei on 2016/4/9.
 */
public class OspUtils {
    //目标Appid
    public static final char DST_APPID = 501;
    //源Appid
    public static final char SRC_APPID = 103;
    //目标实例id
    public static final char DST_INSID = 0xfffc;
    //当前的目标实例INSId  由服务端分配
    public static char DST_CURRENT_INSID = DST_INSID;
    //源实例id
    public static final char SRC_INSID = 1;
    //目标节点 本地生成
    public static int DST_NODE = 1;
    //源节点 服务端生成
    public static int SRC_NODE = 0;
    //消息类型
    public static final char MSG_TYPE = 0;


    //断链检测事件
    public static final char EVEN_CHECK_CONNECT_REQ = 265;
    //断链检测响应事件
    public static final char EVEN_CHECK_CONNECT_RSP = 272;
    //断链检测AppId 2字节
    public static final char CHECK_CONNECT_APPID = 122;
    //断链检测InsID 2字节
    public static final char CHECK_CONNECT_INSID = 1;
    //4字节的断链检测Insid 消息传递时使用
    public static final long CHECK_CONNECT_INSID_4BYTE = MAKEIID(CHECK_CONNECT_APPID,CHECK_CONNECT_INSID);



    private OspUtils(){

    }


    /**
     * 将2字节的appid和2字节的Insid组合成一个新的4字节的insId
     * @param appId
     * @param insId
     * @return 4字节的insId
     */
    public static long	MAKEIID( char appId, char insId)
    {
        int a = (appId << 16)+insId;
        return a;
    }

    //从4字节的InsId内获取2字节的InSid
    public static char getInsId(long insId4Byte){
        char insId = 1;
        insId = (char)(insId4Byte&0xFFFF);
        return insId;
    }

    /**
     * int 数据转换为长度为4的字节数组  高位在前低位在后
     * @param data 需要转换的int值
     * @return buf 长度为4的字节数组
     */
    public static byte[] uintToByte (long data){
        byte[] buf=new byte[4];
        buf[0] = (byte)((data&0xFF000000L)>>24);
        buf[1] = (byte)((data&0x00FF0000L)>>16);
        buf[2] = (byte)((data&0x0000FF00L)>>8);
        buf[3] = (byte)((data&0x000000FFL));
        return buf;
    }

    /**
     * short 数据转化为长度为2的字节数组  高位在前低位在后
     * @param data 需要转化的short值
     * @return buf 长度为2的字节数组
     */
    public static byte[] uShortToByte (char data){
        byte[] buf=new byte[2];
        buf[0] = (byte)((data&0xFF00)>>8);
        buf[1] = (byte)(data&0x00FF);

        return buf;
    }

    /**
     * 浮点转换为字节
     *
     * @param f
     * @return
     */
    public static byte[] uFloatToByte(float f) {
        int intBits = Float.floatToIntBits(f);
        return uintToByte(intBits);
    }

    /**
     * 从字节数组中取出四个字节转化为无符号的int值 高位在前低位在后
     * @param buf 需要转换的字节数组
     * @param index 转换时的起始索引位置
     * @return reslut 要获取的int值
     */
    public static long getUintFromBuf (byte[] buf,int index){

        long reslut = (0x000000FF & ((int)buf[index]))<<24
                | (0x000000FF & ((int)buf[index+1]))<<16
                | (0x000000FF & ((int)buf[index+2]))<<8
                | (0x000000FF & ((int)buf[index+3]));
        return reslut;
    }


    /**
     * 从字节数组中取出两个字节转换为无符号的short值 高位在前低位在后
     * @param buf 需要转换的字节数组
     * @param index 转换时的起始索引位置
     * @return reslut 要获取的short值
     */
    public static char getUshortFromBuf (byte[] buf,int index){

        char reslut = (char)((0x000000FF & ((int)buf[index]))<<8
                | (0x000000FF & ((int)buf[index+1])));
        return reslut;
    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param index 开始位置
     * @return
     */
    public static float getUFloatFromBuf(byte[] b, int index) {
        int temp = (int)getUintFromBuf(b,index);
        return Float.intBitsToFloat(temp);
    }


    /**
     * 字符串校验 判断是否为NULL或者""
     * @param str
     * @return true是NULL或者""
     */
    public static boolean isNullOrEmpty(String str){
        if(str==null||str.equals("")){
            return true;
        }
        return false;
    }
}
