package com.kedacom.touchdata.filemanager;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.kedacom.storagelibrary.model.StorageItem;
import com.kedacom.storagelibrary.unity.StroageManager;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.filemanager.entity.FileEntity;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

public class FileUtils {

    public final static String FILE_SYSTEM_ALIAS = "本机文件";

    public static String DEVICE_MODEL_PATH = "NexVision/IMIX-MODEL";

    public static  String SDPATH ;

    public static String LOCAL_DEVICE_PATH;

    public static String NEXVISION_DIR;

    public static String APP_DIR;

    public static String OPENFILE_DIR;

    public static String OPENFILE_DOC_DIR;

    public static String OPENFILE_EXCEL_DIR;

    public static String OPENFILE_PPT_DIR;

    public static String OPENFILE_PDF_DIR;

    public static String OPENFILE_TXT_DIR;

    public static String SAVE_WRITEBOARD_DIR;

    public static String REC_IMAGE_DIR;

    public static String  REGROUP_SEGMENT;  //接收过来的分包数据在这里进行重组 包Id命名

    public static String LOG_DIR;

    //public static String ERROR_LOG_DIR ;
    
    public static String RUNNING_CACHE;

    public static ArrayList<String> aliasList = new ArrayList<String>();

    public static HashMap<String,String> pathMap = new HashMap<String,String>();


    public static String USBFlashDriveSaveDir = "NexVision/whiteboard/";

    static{
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if   (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        if(sdDir!=null) {
            SDPATH = sdDir.toString();
        }

        DEVICE_MODEL_PATH = SDPATH + File.separator + DEVICE_MODEL_PATH;

        if(VersionUtils.isImix()) {//IMIX设备
            LOCAL_DEVICE_PATH = SDPATH + File.separator + "kedacom"; //跟文件管理保持一致
            NEXVISION_DIR = LOCAL_DEVICE_PATH+File.separator+ "NexVision";
        }else{
            LOCAL_DEVICE_PATH = SDPATH;
            NEXVISION_DIR = SDPATH + File.separator + "NexVision";
        }

        APP_DIR = SDPATH + File.separator +"NexVision"+File.separator+ "TouchData";
        OPENFILE_DIR = APP_DIR + File.separator + "openfile";
        OPENFILE_DOC_DIR = OPENFILE_DIR + File.separator + "doc";
        OPENFILE_EXCEL_DIR = OPENFILE_DIR + File.separator + "excel";
        OPENFILE_PPT_DIR = OPENFILE_DIR + File.separator + "ppt";
        OPENFILE_PDF_DIR = OPENFILE_DIR + File.separator + "pdf";
        OPENFILE_TXT_DIR = OPENFILE_DIR + File.separator + "txt";
        SAVE_WRITEBOARD_DIR = NEXVISION_DIR + File.separator + "whiteboard";
        REC_IMAGE_DIR = APP_DIR + File.separator + "recimage";
        REGROUP_SEGMENT = APP_DIR + File.separator + "segment";
//        LOG_DIR = APP_DIR + File.separator + "log";
        if(!VersionUtils.isImix()) {//PAD
            LOG_DIR = SDPATH + File.separator + "NexVision" + File.separator + "log" + File.separator + "TouchData";
        }else{//IMIX
            LOG_DIR = "/log" + File.separator + "TouchData";
        }
//        LOG_DIR = "/log" + File.separator +"NexVision"+File.separator+"log"+File.separator+"TouchData";
        //ERROR_LOG_DIR = APP_DIR + File.separator + "errlog";
        RUNNING_CACHE = APP_DIR + File.separator + "cache";

        Log.e("FileUtils","LOG_DIR  = "+LOG_DIR);

        try {
            String filePaths[] = {
                    SDPATH,NEXVISION_DIR,APP_DIR, OPENFILE_DIR, OPENFILE_DOC_DIR, OPENFILE_EXCEL_DIR,
                    OPENFILE_PPT_DIR, OPENFILE_PDF_DIR, OPENFILE_TXT_DIR,SAVE_WRITEBOARD_DIR,
                    REC_IMAGE_DIR,REGROUP_SEGMENT,LOG_DIR,RUNNING_CACHE,//ERROR_LOG_DIR
            };

            for (String path : filePaths) {
                File file = new File(path);
                TPLog.printKeyStatus("path="+path);
                if (!file.exists() || !file.isDirectory()) {
                    file.mkdirs();
                    file.mkdir();
                }
                file = null;
            }

            filePaths = null;

            APP_DIR += File.separator;
            OPENFILE_DIR += File.separator;
            OPENFILE_DOC_DIR += File.separator;
            OPENFILE_EXCEL_DIR += File.separator;
            OPENFILE_PPT_DIR += File.separator;
            OPENFILE_PDF_DIR += File.separator;
            OPENFILE_TXT_DIR += File.separator;
            SAVE_WRITEBOARD_DIR += File.separator;
            REC_IMAGE_DIR += File.separator;
            REGROUP_SEGMENT += File.separator;
            LOG_DIR += File.separator;
            RUNNING_CACHE += File.separator;
//            ERROR_LOG_DIR += File.separator;

            aliasList.add(FILE_SYSTEM_ALIAS);
            pathMap.put(FILE_SYSTEM_ALIAS,SDPATH);

        }catch(Exception e){
             Log.e("msg",e.getMessage());
        }
    }

    public static void clearRecCacheImg(){
        File file = new File(REC_IMAGE_DIR);
        if(!file.exists()||!file.isDirectory()){
           return;
        }
        File files[] = file.listFiles();
        if(files==null){
            return;
        }

        for(int i = 0;i<files.length;i++){
            TPLog.printKeyStatus("files-->"+i+":"+files[i].getAbsolutePath());
            deleteFile(files[i].getAbsolutePath());
        }
    }

    /**
     * 校验文件夹是否存在  不存在的话就创建
     * @param dirPath
     */
    public static void checkDirExists(String dirPath){
        String path = null;
        if(dirPath.endsWith("/")){
            path = dirPath.substring(0,dirPath.length()-1);
        }else{
            path = dirPath;
        }
        File file = new File(path);
        if(!file.exists()||!file.isDirectory()){
            file.mkdirs();
            file.mkdir();
        }
    }

    public static boolean checkFileExists(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file = null;
            return true;
        }else{
            file = null;
            return false;
        }
    }
    
    /**
     * 删除当前路径下所有的文件和文件夹
     * @param filePath 需要删除文件的路径
     */
    public static void deleteFile(String filePath){
    	File file = new File(filePath);
    	if(!file.exists()){
    		return;
    	}
    	if(file.isDirectory()){
    		File files[] = file.listFiles();
    		for(File f:files){
    			deleteFile(f.getAbsolutePath());
    		}
    		file.delete();
    	}else{
    		file.delete();
    	}
    }

    public static void findAllChildFile(String parentFilePath,List<String> childFileList){
        TPLog.printKeyStatus("查找文件路径："+parentFilePath);
        File file = new File(parentFilePath);
        if(!file.exists()||file.isFile()){
            return ;
        }
        File files[] = file.listFiles();
        if(files==null)return ;
        for(int i = 0;i<files.length;i++){
            if(files[i].isFile()) {
                TPLog.printKeyStatus("获得文件："+files[i].getAbsolutePath());
                childFileList.add(files[i].getAbsolutePath());
            }
            else
                findAllChildFile(files[i].getAbsolutePath(),childFileList);
        }
    }


    
    private static List<String> fileListCache = new ArrayList<String>();
    
    /**
     * 获取当前路径下所有的文件
     * @param filePath 当前路径
     * @return List<String> 所有子文件路径集合
     */
    public static List<String> getAllSubFile(String filePath){
    	fileListCache.clear();
    	getAllSubFileToList(filePath);
    	return fileListCache;
    }
    
    private static void getAllSubFileToList(String filePath){
    	File file = new File(filePath);
    	if(!file.exists()){
    		return ;
    	}
    	if(file.isDirectory()){
    		File files[] = file.listFiles();
    		for(File f:files){
    			getAllSubFileToList(f.getAbsolutePath());
    		}
    	}else{
    		fileListCache.add(filePath);
    	}
    }

    /**
     * 新方法，最新需求获取文件大小和修改日期
     */
   public static FileEntity findFiles(String filePath){
       File file = new File(filePath);
       FileEntity fe = new FileEntity();
       findChilds(fe,file);
       return fe;
   }

    public static void findChilds(FileEntity fe,File file){
        fe.setAbsolutePath(file.getAbsolutePath());
        fe.setName(file.getName());
        fe.setParent(null);
        checkFileTypeAndIcon(file);
        if(file.isDirectory()){
            File files[] = file.listFiles();
            if(files==null)return;
            FileEntity childs[] = new FileEntity[files.length];
            fe.setChilds(childs);
            for(int i = 0;i<files.length;i++){
                FileEntity fileEntity = new FileEntity();
                fileEntity.setParent(fe);
                childs[i] = fileEntity;
                findChilds(fe,file);
                if(fileEntity.getType() == FileType.FIlE_TYPE_DIR){
                    long tempSize = fe.getSize() + fileEntity.getSize();
                    fe.setSize(tempSize);
                }
            }
        }else{
            long length = file.length();
            fe.setSize(length);
            FileEntity parent = fe.getParent();
            if(parent!=null){
                long parentLength = parent.getSize()+length;
                parent.setSize(parentLength);
            }
        }
    }


    public static void loadChildFile(FileEntity parent){
        FileEntity fe[] = null;
        File file = new File(parent.getAbsolutePath());
        if(!file.exists()||file.isFile()){
            return;
        }
        File files[] = file.listFiles();
        if(files==null) {
            return ;
        }

        fe = new FileEntity[files.length];

        int dirCount = 0;
        int fileCount = 0;

        for(int i = 0;i<files.length;i++){
            File locFile = files[i];
            FileEntity f = new FileEntity();
            f.setName(locFile.getName());
            if(locFile.isDirectory()) {
                f.setSize(-1);
                dirCount++;
            }else{
                f.setSize(locFile.length());
                fileCount++;
            }
            f.setAbsolutePath(locFile.getAbsolutePath());
            f.setIcon(getFileIcon(locFile.getAbsolutePath()));
            f.setUpdateTime(locFile.lastModified());
            f.setType(checkFileTypeAndIcon(locFile));
            f.setParent(parent);
            fe[i] = f;
        }

        FileEntity dirFe[] = new FileEntity[dirCount];
        FileEntity fileFe[] = new FileEntity[fileCount];

        int dirIndex = 0;
        int fileIndex = 0;

        for(int i = 0;i<fe.length;i++){
            if(fe[i].getType() == FileType.FIlE_TYPE_DIR){
                dirFe[dirIndex++] = fe[i];
            }else{
                fileFe[fileIndex++] = fe[i];
            }
        }

        dirIndex = 0;
        fileIndex = 0;
        for(int i = 0;i<fe.length;i++){
            if(i<dirCount) {
                fe[i] = dirFe[dirIndex++];
            }else{
                fe[i] = fileFe[fileIndex++];
            }
        }

        dirFe = null;
        fileFe = null;
        parent.setChilds(fe);
    }

    /**
     * 获取子文件
     * @param path 目录地址
     * @param filter 过滤器
     * @return 所有的子文件
     */
    public static String[] getChildFile(String path, FilenameFilter filter){
        File file = new File(path);
        if(!file.exists()||file.isFile()){
            return null;
        }
        if(filter!=null){
            return file.list(filter);
        }else{
            return file.list();
        }
    }


    public static int checkFileTypeAndIcon(File file){

        String name = file.getName();

        int type = FileType.FILE_TYPE_OTHER;

        if(!file.isDirectory()) {
            if (name.endsWith(".txt")) {
                type = FileType.FILE_TYPE_TXT;
            } else if (name.endsWith(".jpg") || name.endsWith(".png")
                    || name.endsWith(".bmp") || name.endsWith(".jpeg")
                    || name.endsWith(".gif")) {
                type = FileType.FILE_TYPE_IMAGE;
            } else if (name.endsWith(".pdf")) {
                type = FileType.FILE_TYPE_PDF;
            } else if (name.endsWith(".doc") || name.endsWith(".docx")
                    || name.endsWith(".docm") || name.endsWith(".dotx")
                    || name.endsWith(".dotm")) {
                type = FileType.FILE_TYPE_DOC;
            } else if (name.endsWith(".xls") || name.endsWith(".xlsx")
                    || name.endsWith(".xlsm") || name.endsWith(".xltx")
                    || name.endsWith(".xltm") || name.endsWith(".xlsb")
                    || name.endsWith(".xlam")) {
                type = FileType.FILE_TYPE_EXCEL;
            } else if (name.endsWith(".ppt") || name.endsWith(".pptx")
                    || name.endsWith(".pptm") || name.endsWith(".ppsx")
                    || name.endsWith(".potx") || name.endsWith(".potm")
                    || name.endsWith(".ppam")) {
                type = FileType.FILE_TYPE_PPT;
            } else if (name.endsWith(".mp3") || name.endsWith(".wav")
                    || name.endsWith(".wma") || name.endsWith(".ogg")
                    || name.endsWith(".ape") || name.endsWith(".acc")) {
                type = FileType.FILE_TYPE_VOICE;
            } else if (name.endsWith(".mp4") || name.endsWith(".mpg")
                    || name.endsWith(".mpeg") || name.endsWith(".mpe")
                    || name.endsWith(".avi") || name.endsWith(".rmvb")
                    || name.endsWith(".rm") || name.endsWith(".asf")
                    || name.endsWith(".wmv") || name.endsWith(".mov")
                    || name.endsWith(".3gp") || name.endsWith(".flv")) {
                type = FileType.FILE_TYPE_VIDEO;
            } else if (name.endsWith(".rar") || name.endsWith(".zip")
                    || name.endsWith(".7z")) {
                type = FileType.FILE_TYPE_COMPRESS;
            } else if (name.endsWith(".apk")) {
                type = FileType.FILE_TYPE_APK;
            } else if (name.endsWith(".html") || name.endsWith(".htm")) {
                type = FileType.FILE_TYPE_HTML;
            } else {
                type = FileType.FILE_TYPE_OTHER;
            }
        }else{
            type = FileType.FIlE_TYPE_DIR;
        }

       return type;
    }


    public static int getFileIcon(String path){
        File file = new File(path);
        String name = file.getName();

        if(!file.isDirectory()) {
            if (name.endsWith(".txt")) {
                return R.mipmap.txt_file_icon;
            } else if (name.endsWith(".jpg") || name.endsWith(".png")
                    || name.endsWith(".bmp") || name.endsWith(".jpeg")
                    || name.endsWith(".gif")) {
                return R.drawable.image_file_icon;
            } else if (name.endsWith(".pdf")) {
                return R.mipmap.pdf_file_icon;
            } else if (name.endsWith(".doc") || name.endsWith(".docx")
                    || name.endsWith(".docm") || name.endsWith(".dotx")
                    || name.endsWith(".dotm")) {
                return R.mipmap.doc_file_icon;
            } else if (name.endsWith(".xls") || name.endsWith(".xlsx")
                    || name.endsWith(".xlsm") || name.endsWith(".xltx")
                    || name.endsWith(".xltm") || name.endsWith(".xlsb")
                    || name.endsWith(".xlam")) {
                return R.mipmap.xls_file_icon;
            } else if (name.endsWith(".ppt") || name.endsWith(".pptx")
                    || name.endsWith(".pptm") || name.endsWith(".ppsx")
                    || name.endsWith(".potx") || name.endsWith(".potm")
                    || name.endsWith(".ppam")) {
                return R.mipmap.ppt_file_icon;
            } else if (name.endsWith(".mp3") || name.endsWith(".wav")
                    || name.endsWith(".wma") || name.endsWith(".ogg")
                    || name.endsWith(".ape") || name.endsWith(".acc")) {
                return R.mipmap.audio_file_icon;
            } else if (name.endsWith(".mp4") || name.endsWith(".mpg")
                    || name.endsWith(".mpeg") || name.endsWith(".mpe")
                    || name.endsWith(".avi") || name.endsWith(".rmvb")
                    || name.endsWith(".rm") || name.endsWith(".asf")
                    || name.endsWith(".wmv") || name.endsWith(".mov")
                    || name.endsWith(".3gp") || name.endsWith(".flv")) {
                return R.drawable.video_file_icon;
            } else if (name.endsWith(".rar") ) {
                return R.mipmap.rar_file_icon;
            } else if(name.endsWith(".zip")){
                return R.mipmap.zip_file_icon;
            } else if (name.endsWith(".apk")) {
                return R.drawable.apk_file_icon;
            } else if (name.endsWith(".html") || name.endsWith(".htm")) {
                return R.drawable.html_file_icon;
            } else {
                return R.mipmap.unknown_file_icon;
            }
        }else{
          return R.mipmap.folder_icon;
        }
    }

    private static SimpleDateFormat sdf;
    public static String formatDate(long date){
        if(sdf==null) {
            sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        }
        return sdf.format(new Date(date));
}

    /**
     * 获取文件大小
     * @param filePath
     * @return
     */
    public static String getFileSize(String filePath){
        File file = new File(filePath);
        if(!file.exists()||file.isDirectory()){
            return "-";
        }
        return formatFileSize(file.length());
    }

    public static String formatFileSize(long size){
        String strSize = null;

        if(size<0){
            return "--";
        }

        if(size>1024) {

            //k
            float k = (float) size / 1024f;

            if (k > 1024) {
                //m
                float m = k / 1024f;

                if (m > 1024) {

                    //g
                    float g = m / 1024f;

                    if (g > 1024) {

                        float t = g / 1024f;
                        t = Math.round(t);
                        strSize = t + "TB";

                    } else {
                        g = Math.round(g);
                        strSize = g + "GB";
                    }

                } else {
                    m = Math.round(m);
                    strSize = m + "MB";
                }

            } else {
                k = Math.round(k);
                strSize = k + "KB";
            }
        }else{
            strSize = size + "B";
        }

        return strSize;
    }


    public static String getCurWhiteBoardSaveDir(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        String saveDir = sdf.format(new Date()) + "白板会议";
        sdf = null;
        return saveDir;
    }

    public void savePath(){

    }

    /**
     * 获取别名路径
     * @param path
     * @return
     */
    public static String getAliasPath(String path, StroageManager mStroageManager){

        if(path.contains(LOCAL_DEVICE_PATH)){
            return path.replace(LOCAL_DEVICE_PATH,FILE_SYSTEM_ALIAS);
        }

        List<StorageItem> list =  mStroageManager.getStorageItems();

        if(list==null){
            return path;
        }

        int count = list.size();

        for(int i = 0;i<count;i++){
            StorageItem sItem = list.get(i);

            if(path.contains(sItem.path)){
                return path.replace(sItem.path,sItem.name);
            }
        }
        return path;
    }

    /**
     * 获取真实路径
     * @param path
     * @return
     */
    public static String getRealPath(String path,StroageManager mStroageManager){
        List<StorageItem> list = mStroageManager.getStorageItems();
        int  index = path.indexOf("/");
        String deName = null;
        if(index!=-1) {
            deName  = path.substring(0, index);
        }else{
            deName = path;
        }
        String devPath = pathMap.get(deName);
        if(devPath == null){
            for(int i = 0;i<list.size();i++){
                if(deName.equals(list.get(i).name)){
                    return list.get(i).path+File.separator + USBFlashDriveSaveDir;
                }
            }
        }
        return path.replace(deName,devPath);
    }


    /**
     * 获取真实路径
     * @param path
     * @return
     */
    public static String getRealPath(String path){
        int  index = path.indexOf("/");
        String deName = null;
        if(index!=-1) {
            deName  = path.substring(0, index);
        }else{
            deName = path;
        }
        String devPath = pathMap.get(deName);
        return path.replace(deName,devPath);
}

    public static boolean checkFileIsImg(String path){

        int lastIndex = path.lastIndexOf(".");

        if(lastIndex<=0){
            return false;
        }

        String temp = path.substring(lastIndex+1,path.length());

        temp = temp.toLowerCase();

        TPLog.printKeyStatus("打开文件后缀名:"+temp);

        String temps[] = {"jpg","png","jpeg","bmp"};

        for(String s:temps){
            if(s.equals(temp)){
                return true;
            }
        }

        return false;
    }

    public static boolean checkImgCanOpen(String path){
       Bitmap bitmap = BitmapManager.getInstence().loadBitmap(path);
       if(bitmap==null){
           return false;
       }
       return true;
    }


    

}
