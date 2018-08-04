package com.kedacom.touchdata.filemanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhanglei on 2016/1/29.
 */
public class FileManager {

    private static FileManager mFileManager;

    private List<File> currentDirFiles = new ArrayList<File>();

    private FileGridViewAdapter mFileGridViewAdapter;

    private PopupWindow fileManagerWindow;

    private LayoutInflater inflater;

    private File crrentOpenDir ;

    private TextView pathTv;

    private TextView fileNameTv;

    private OnChooseFileListener mListener;

    private FileManager(){
        mFileGridViewAdapter = new FileGridViewAdapter();
    }

    public static FileManager getInstance(){
        if(mFileManager==null){
            mFileManager = new FileManager();
        }
        return mFileManager;
    }

    /**
     * 显示文件选择
     * @param context
     */
    public void showSwitchFileWindow(final Context context){
        inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ppw_filemanager, null);

        pathTv = (TextView)view.findViewById(R.id.pathTv);
        fileNameTv = (TextView)view.findViewById(R.id.fileNameTv);
        final Button backBtn = (Button)view.findViewById(R.id.backBtn);
        Button sureBtn = (Button)view.findViewById(R.id.sureBtn);
        Button cancelBtn = (Button)view.findViewById(R.id.cancelBtn);

        GridView fileGridView = (GridView)view.findViewById(R.id.fileGridView);

        openDir(FileUtils.SDPATH);

        fileGridView.setAdapter(mFileGridViewAdapter);

        backBtn.setEnabled(false);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileUtils.SDPATH.equals(crrentOpenDir.getAbsolutePath())) {
                    backBtn.setEnabled(false);
                    return;
                }
               String parent =  crrentOpenDir.getParent();
                if(parent.endsWith(FileUtils.SDPATH)){
                    backBtn.setEnabled(false);
                }
                openDir(parent);
            }
        });

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileManagerWindow.dismiss();
                File file = mFileGridViewAdapter.getSelectItem();
                if(file!=null&&mListener!=null) {
                    mListener.onChooseFile(file);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileManagerWindow.dismiss();
            }
        });

        fileGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File) mFileGridViewAdapter.getItem(position);
                if (file.isDirectory()) {
                    backBtn.setEnabled(true);
                    openDir(file.getAbsolutePath());
                } else {
                    mFileGridViewAdapter.setSelect(position);
                    fileNameTv.setText(file.getName());
                }
            }
        });

        fileManagerWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,  ViewGroup.LayoutParams.WRAP_CONTENT, true);
        fileManagerWindow.setFocusable(true);
        fileManagerWindow.setOutsideTouchable(false);
        fileManagerWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fileManagerWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


    private void openDir(String dir){

        File file = new File(dir);

        List<File> dirList = new ArrayList<File>();
        List<File> fileList = new ArrayList<File>();

        if(!file.exists()||!file.isDirectory()){
            return;
        }

        mFileGridViewAdapter.setSelect(-1);

        File files[] = file.listFiles();

        currentDirFiles.clear();

        if(files!=null){
            for(File f:files){
                if(!f.getName().startsWith(".")) {
                    if(f.isDirectory()){
                        dirList.add(f);
                    }else{
                        fileList.add(f);
                    }
                }
            }
        }

        sortList(dirList);
        sortList(fileList);

        currentDirFiles.clear();

        for(File f : dirList){
            currentDirFiles.add(f);
        }

        for(File f : fileList){
            currentDirFiles.add(f);
        }

        if(mFileGridViewAdapter!=null) {
            //更新适配器刷新界�?
            mFileGridViewAdapter.notifyDataSetChanged();
        }

        dirList.clear();
        fileList.clear();
        dirList = null;
        fileList = null;

        fileNameTv.setText("");
        pathTv.setText(dir);
        crrentOpenDir = file;

    }




    class FileGridViewAdapter extends BaseAdapter{

        private  int selectPos = -1;

        @Override
        public int getCount() {
            return currentDirFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return currentDirFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSelect(int position){
            if(selectPos!=position) {
                selectPos = position;
            }else{
                selectPos = -1;
            }
            notifyDataSetChanged();
        }

        public File getSelectItem(){
            if(selectPos!=-1){
                return currentDirFiles.get(selectPos);
            }

            return null;
        }

        //根据文件名称选择响应的图
        public void chooseIcon(ImageView iv , File file){

            int resId = 0;

            if(file.isDirectory()){//文件
                resId = R.drawable.folder_icon;
            }else{
                String name = file.getName();

                name = name.toLowerCase();

                if(name.endsWith(".txt")){
                    resId = R.drawable.txt_file_icon;
                }else if(name.endsWith(".jpg")||name.endsWith(".png")||name.endsWith(".bmp")||name.endsWith(".jpeg")||name.endsWith(".gif")){
                    resId = R.drawable.image_file_icon;
                }else if(name.endsWith(".pdf")){
                    resId = R.drawable.pdf_file_icon;
                }else if(name.endsWith(".doc")||name.endsWith(".docx")||name.endsWith(".docm")||name.endsWith(".dotx")||name.endsWith(".dotm")){
                    resId = R.drawable.doc_file_icon;
                }else if(name.endsWith(".xls")||name.endsWith(".xlsx")||name.endsWith(".xlsm")||name.endsWith(".xltx")||name.endsWith(".xltm")||name.endsWith(".xlsb")||name.endsWith(".xlam")){
                    resId = R.drawable.xls_file_icon;
                }else if(name.endsWith(".ppt")||name.endsWith(".pptx")||name.endsWith(".pptm")||name.endsWith(".ppsx")||name.endsWith(".potx")||name.endsWith(".potm")||name.endsWith(".ppam")){
                    //resId = R.drawable.
                    resId = R.drawable.unknown_file_icon;//暂时没有ppt图标暂时代替
                }else if(name.endsWith(".mp3")||name.endsWith(".wav")||name.endsWith(".wma")||name.endsWith(".ogg")||name.endsWith(".ape")||name.endsWith(".acc")){
                    resId = R.drawable.audio_file_icon;
                }else if(name.endsWith(".mp4")||name.endsWith(".mpg")||name.endsWith(".mpeg")||name.endsWith(".mpe")||name.endsWith(".avi")||name.endsWith(".rmvb")||name.endsWith(".rm")||name.endsWith(".asf")||name.endsWith(".wmv")||name.endsWith(".mov")||name.endsWith(".3gp")||name.endsWith(".flv")){
                    resId = R.drawable.video_file_icon;
                }else if(name.endsWith(".rar")||name.endsWith(".zip")||name.endsWith(".7z")){
                    resId = R.drawable.compressed_file_icon;
                }else if(name.endsWith(".apk")){
                    resId = R.drawable.apk_file_icon;
                }else if(name.endsWith(".html")||name.endsWith(".htm")){
                    resId = R.drawable.html_file_icon;
                }else{
                    resId = R.drawable.unknown_file_icon;
                }
            }
            iv.setBackgroundResource(resId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            File item = currentDirFiles.get(position);

            if(convertView==null){
                convertView = inflater.inflate(R.layout.item_filegridview,null);
            }

            ImageView fileIcon = (ImageView)convertView.findViewById(R.id.fileIcon);

            ImageView selectIcon = (ImageView)convertView.findViewById(R.id.selectIcon);

            TextView fileNameTv = (TextView)convertView.findViewById(R.id.fileNameTv);

            chooseIcon(fileIcon,item);

            fileNameTv.setText(item.getName());

            if(selectPos == position){//显示选中图标
                selectIcon.setVisibility(View.VISIBLE);
            }else{
                selectIcon.setVisibility(View.GONE);
            }

            return convertView;
        }

    }


    //对List进行字母排序
    private void sortList(List<File> strList) {

        Collections.sort(strList, new Comparator<File>() {
            public int compare(File o1, File o2) {
                char[] a1 = o1.getName().toCharArray();
                char[] a2 = o2.getName().toCharArray();

                for (int i = 0; i < a1.length && i < a2.length; i++) {
                    int c1 = getGBCode(a1[i]);
                    int c2 = getGBCode(a2[i]);

                    System.out.println(new StringBuffer().append(c1).toString());
                    System.out.println(new StringBuffer().append(c2).toString());

                    if (c1 == c2)
                        continue;

                    return c1 - c2;
                }

                if (a1.length == a2.length) {
                    return 0;
                }

                return a1.length - a2.length;
            }
        });

    }

    private  int getGBCode(char c){
        byte[] bytes= null;
        try {
            bytes = new StringBuffer().append(c).toString().getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(bytes.length == 1){
            return bytes[0];
        }
        int a = bytes[0]-0xA0+256;
        int b = bytes[1]-0xA0+256;

        return a*100+b;
    }


    public void setOnChooseFileListener(OnChooseFileListener ocfl){
        mListener = ocfl;
    }

    public interface  OnChooseFileListener{
        void onChooseFile(File file);
    }

}
