package com.kedacom.touchdata.net.utils;

/**
 * Created by zhanglei on 2016/3/4.
 */
public class Command {

    public static final byte SC_OWERLEAVE = 0x10;
    public static final byte CC_CLEAR = 0x11;    //清屏
    public static final byte CC_SCROLL = 0x13;    //水平/垂直滚动屏幕
    public static final byte CC_ERASE = 0x14;    //擦除屏幕
    public static final byte CC_ZOOM = 0x16;    //放大、缩小屏幕
    public static final byte CC_ROTATELEFT = 0x17;    //左旋转
    public static final byte CC_ROTATERIGHT = 0x18;    //右旋转
    public static final byte CC_ROTATE = 0x19;   //任意角度旋转
    public static final byte CC_USERDATA = 0x20;    //？？？？
    public static final byte CS_REQUESTPAGETURN = 0x21;        //客户向服务器请求翻页
    public static final byte SC_REQUESTPAGETURN = 0x22;        //服务器向客户请求翻页
    public static final byte CC_IMAGE = 0x23;   //打开文件 用来传递文件打开后的的图片信息
    public static final byte SOCK_FIRST_SAVE_FILE = 0x75;   //第一次入会时上传图片到服务器
    public static final byte CC_LAYER = 0x24;
    public static final byte CC_UNDO = 0x27; //撤销
    public static final byte CC_SYNCHRONOUSEND = 0x28;//同步完成
    public static final byte SC_FILEINVALID = 0x29;    //文件无效
    public static final byte SC_TABINVALID = 0x2b;
    public static final byte CC_TABPAGE = 0x2e;//切换页或子页
    public static final byte CC_ZOOM_GESTURE = 0x2f;   //手势放大、缩小屏幕
    public static final byte CC_INSERT_IMG = 0x30; //2017.07.04新增插入图片接口
    public static final byte CC_COORDINATE_CHANGED = 0x31; //2017.07.04白板坐标系改变消息，替换之前的缩放、平移和旋转
    public static final byte CC_GRAPH_COORDINATE_CHANGED = (byte)0x50; //2017.07.04 选中图元属性改变消息，如：缩放、旋转或者平移
    public static final byte CC_DELETE_GRAPH= (byte)0x51; //2017.07.04 单个图元删除


    public static final byte CS_SYNCHRONOUS = 0x32;//客户端请求同步
    public static final byte CC_REQUESTFILEBODY = 0x33;
    public static final byte CC_REQUESTFILEBODY_ACK = 0x34;
    public static final byte CS_GETFILEINFO = 0x35;    //取文件信息
    public static final byte CC_SYNCHRONOUSEBODY = 0x37;    //同步的内容
    public static final byte CC_PAGEAVAILABLY = 0x38;        //已发送子页
    public static final byte CC_REMOVEPAGE = 0x39;    //删除白板标签页
    public static final byte CC_REMOVEALLPAGE = 0x76;    //删除所有白板标签页
    public static final byte CC_ADDSUBPAGE = 0x40;       //增加子页
    public static final byte CC_DOCPAGECONVERTCOMPLETE = 0x41;   //文档页office转pdf结束

    public static final byte SC_SYNCHRONOUS = 0x42;//服务器要求客户端提供同步数据
    public static final byte SC_SYNCHRONOUSFAILED = 0x43;   //暂时同步失败，请稍候再试
    public static final byte SC_EXIST_FILE_NOTIFY = 0x44;   //服务器告知客户端已存在文件id

    public static final byte CC_DRAWENTITY = 0x60;   //画图元对象
    public static final byte CC_NEWPAGE = 0x62;//新建白板面
    public static final byte CC_REDO = 0x63;  //反撤销
    public static final byte CC_MINHEIGHT = 0x64;    //高度自适应
    public static final byte CC_MINWIDTH = 0x65;//宽度自适应
    public static final byte CC_SYNCHRONOUSEEND = 0x67;    //同步完成
    public static final byte CC_SEGMENTMSG = 0x70;    //分段消息de

    public static final byte SOCK_FILEINFO = 0x72; //服务端发送过来的文件信息
    public static final byte SOCK_FILEBODY = 0x73; //服务端发送过来的文件内容
    public static final byte SOCK_FILEINFOFAILED = 0x74;
    public static final byte SOCK_TERM = 0x0D;

    public static final byte MSG_TERM = 0x0D;
    public static final byte MSG_C2S = 0x09;    //c-s    客户端传给服务器
}
