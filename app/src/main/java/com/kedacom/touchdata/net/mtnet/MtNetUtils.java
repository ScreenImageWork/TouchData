package com.kedacom.touchdata.net.mtnet;

import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.net.mtnet.msg.DispathMtNetMsg;
import com.kedacom.touchdata.net.mtnet.entity.MtEntity;
import com.kedacom.touchdata.net.mtnet.utils.MethodExecutor;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanglei on 2018/3/19.
 */

public class MtNetUtils {

    public static final int MODE_DATA_CONF_TALK = 1;
    public static final int MODE_DATA_CONF_SPEAKER = 2;

    //白板会议模式
    public static final int emConfModeStop_Api = 0;  //关闭数据协作
    public static final int emConfModeManage_Api  = 1; //主席控制
    public static final int emConfModeAuto_Api  = 2; //自动协作

//    public static String MT_IP = "172.16.161.115";
    public static String MT_IP = "172.16.160.115";     //gongweifeng
//        public static String MT_IP = "172.16.79.71";  //shanghailong
//    public static String MT_IP = "10.83.0.213";       //liuyanshuang
    public final static String MT_USERNAME = "Username";
    public final static String MT_PASSWORD = "2018_Inner_Pwd_|}><NewAccess#@k";

//    public final static String MT_IP = "172.16.161.115";
//    public final static String MT_USERNAME = "admin";
//    public final static String MT_PASSWORD = "1111111q";

    public final static int MT_PORT = 60001;

    public static int sessionId = -1;//当前连接SessionId

    public static StringBuffer strSessionId = new StringBuffer(); //当前连接SessionId

    public static String achTerminalE164 = "";

    public static String achConfE164;//会议E164号

    public static String achConfName;//会议名称

    public static int dwMcuId;//当前的mcuId  如果是0，就说明没有主席

    public static int dwTerId; //当前的主席Id

    public static List<MtEntity> confMemberList = new ArrayList<MtEntity>();//当前与会人员列表

    public static boolean synConfData = false;

    public static boolean confJoining = false;

    public static boolean isOper = false;

    public static boolean isConfManager = false;

    public static int curEmConfMode = emConfModeAuto_Api; //当前会议模式，自由协作/管理员模式

    public static int curDataConfMode = MODE_DATA_CONF_TALK; //当前会议协作会议模式  主讲/讨论

    /**
     * 检查当前终端是否是管理者
     */
    public static boolean checkConfManager(){
        if(MtNetUtils.dwMcuId!=0){
            for(MtEntity mt: MtNetUtils.confMemberList){
                //            TPLog.printError(mt.toString());
                if(mt.getE164() == null){
                    continue;
                }
                if(mt.getE164().equals(MtNetUtils.achTerminalE164)){
                    int dwTerId = mt.getDwTerId();
                    if(dwTerId != 0&& dwTerId == MtNetUtils.dwTerId){
                        MtNetUtils.isConfManager = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static MtEntity getMtEntity(ApplyChairNtf acn){
        if(acn == null){
            return null;
        }
        for(MtEntity mt: MtNetUtils.confMemberList){
            if(acn.getDwTerId() == mt.getDwTerId()){
                return mt;
            }
        }
        return null;
    }

    //将data字节型数据转换为0~255 (0xFF 即BYTE)。
    public static int getUnsignedByte (byte data){
        return data&0x0FF;
    }

    public static int getUnsignedShort(short data){
        return data&0xFFFF;
    }

    public static long getUnsignedInt(int data){
       String value =  Integer.toHexString(data);
        return Long.valueOf(value,16);
    }

    public static String strE164ToStrJsonE164(String strConfE164){
        try {
            JSONObject jsonConfE164 = new JSONObject();
            jsonConfE164.put("basetype", strConfE164);
            return jsonConfE164.toString();
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static JSONObject pageToJSONObject(Page page){
        JSONObject jsonPage = new JSONObject();

        try {

            jsonPage.put("achWbName", page.getName());
            jsonPage.put("emWbMode",page.getPageMode());
            jsonPage.put("dwWbPageNum", 1);
            jsonPage.put("dwWbCreateTime",0);
            jsonPage.put("achTabId",page.getRemotePageId());
            jsonPage.put("dwPageId",page.getSubPageCount());
            jsonPage.put("dwWbSerialNumber",0);
            jsonPage.put("achWbCreatorE164","");//创建者的E164号码
            jsonPage.put("dwWbWidth", (int)WhiteBoardUtils.whiteBoardWidth);
            jsonPage.put("dwWbHeight",(int)WhiteBoardUtils.whiteBoardHeight);
            jsonPage.put("achElementUrl",""); //同步数据下载地址
            jsonPage.put("achDownloadUrl","");//据说已经废弃
            jsonPage.put("achUploadUrl","");  //据说已经废弃
            jsonPage.put("dwWbAnonyId",0);

        }catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("Mt pageToJSONObject -> " +jsonPage.toString());

        return jsonPage;
    }

    public static String getStrJsonOper(String strConfE164,String tabId,long subPageIndex){
        JSONObject jsonOperReq = new JSONObject();
        try {
            jsonOperReq.put("strConfE164", strConfE164);
            jsonOperReq.put("achTabId",tabId+"");
            jsonOperReq.put("dwWbPageId",subPageIndex);
        }catch(Exception e){
            e.printStackTrace();
        }

        return jsonOperReq.toString();
    }

}
