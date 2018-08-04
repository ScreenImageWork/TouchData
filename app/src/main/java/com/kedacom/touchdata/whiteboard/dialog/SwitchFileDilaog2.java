package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.filemanager.FileType;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.filemanager.entity.FileEntity;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

import java.io.File;


public class SwitchFileDilaog2 implements IControler {

    private int textNormalColor = Color.parseColor("#b1b1b1");
    private int textSelectColor = Color.parseColor("#00aff2");

    private Context context;

    private TPPopupWindow mWindow;

    private View contentView;

    private ListView deviceLV;

    private ListView fileLV;

    private Button cancelBtn;

    private Button openBtn;

    private TextView curPathTV;

    private OnClickListener openCallBack;

    private DevicesListAdapter mDevicesListAdapter;

    private FileListAdapter mFileListAdapter;

    public SwitchFileDilaog2(Context context){
        this.context = context;
        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_switchfile, null);

        curPathTV = (TextView)contentView.findViewById(R.id.pathTv);
        deviceLV = (ListView)contentView.findViewById(R.id.deviceList);
        fileLV = (ListView)contentView.findViewById(R.id.fileList);
        cancelBtn = (Button)contentView.findViewById(R.id.dialogCancelBtn);
        openBtn = (Button)contentView.findViewById(R.id.dialogSureBtn);

        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
       // mWindow.setFocusable(true);
        mWindow.setOnDismissListener((BaseActivity)context);

        mDevicesListAdapter = new DevicesListAdapter(context);
        mFileListAdapter = new FileListAdapter(context);
        deviceLV.setAdapter(mDevicesListAdapter);
        fileLV.setAdapter(mFileListAdapter);

        deviceLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if(arg2 == mDevicesListAdapter.getSelectPosition()){
                    return ;
                }
                mDevicesListAdapter.select(arg2);
                String devName = (String)mDevicesListAdapter.getItem(arg2);
                String devPath = FileUtils.getRealPath(devName);
                FileEntity fe = new FileEntity();
                fe.setAbsolutePath(devPath);
                FileUtils.loadChildFile(fe);
                mFileListAdapter.setData(fe);
            }
        });

        fileLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if(mFileListAdapter.getSelectPosition() == arg2 ){
                    FileEntity fe = (FileEntity)mFileListAdapter.getItem(arg2);
                    if(fe.getType() == FileType.FIlE_TYPE_DIR){
                        mFileListAdapter.into(arg2);
                    }else{
                        if(openCallBack!=null)
                            openCallBack.onClick(openBtn);
                    }
                }else{
                    mFileListAdapter.select(arg2);
                }
            }
        });


        openBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int selectPosition = mFileListAdapter.getSelectPosition();
                FileEntity fe = (FileEntity)mFileListAdapter.getItem(selectPosition);

                if(fe == null)return;

                if(fe.getType() == FileType.FIlE_TYPE_DIR){
                    mFileListAdapter.into(selectPosition);
                }else{
                    if(openCallBack!=null){
                        openCallBack.onClick(arg0);
                    }
                }
            }
        });
    }

    public void setCancelBtnListener(OnClickListener listener){
        cancelBtn.setOnClickListener(listener);
    }

    public void setOpenBtnListener(OnClickListener listener){
        openCallBack = listener;
    }

    public String getSelectFile(){
        FileEntity fe = (FileEntity)mFileListAdapter.getItem(mFileListAdapter.getSelectPosition());
        return fe.getAbsolutePath();
    }


    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        String devNames[] = new String[FileUtils.aliasList.size()];
        FileUtils.aliasList.toArray(devNames);
        mDevicesListAdapter.setData(devNames);

        FileEntity fe = new FileEntity();
        fe.setAbsolutePath(FileUtils.getRealPath(devNames[0]));
        FileUtils.loadChildFile(fe);

        mFileListAdapter.setData(fe);
        mWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }




    class FileListAdapter extends BaseAdapter{


        private FileEntity fe;

        private int selectIndex = -1;

        private LayoutInflater inflater;

        public FileListAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }

        public FileListAdapter(Context context,FileEntity fe){
            inflater = LayoutInflater.from(context);
            this.fe = fe;
        }

        public void select(int args){
            selectIndex = args;
            notifyDataSetChanged();
        }

        public int getSelectPosition(){
            return selectIndex;
        }

        public void into(int index){
            selectIndex = -1;
            FileEntity child = fe.getChilds()[index];
            if(child.getType() == FileType.FIlE_TYPE_DIR){
                fe = child;
                FileUtils.loadChildFile(fe);
            }else{
                selectIndex = index;
            }


            notifyDataSetChanged();
        }

        public void goBack(){
            selectIndex = -1;
            FileEntity parent = fe.getParent();
            if(parent==null){
                return;
            }
            fe = parent;
            notifyDataSetChanged();
        }


        public void setData(FileEntity fileEntity){
            this.fe = fileEntity;
            notifyDataSetChanged();
        }

        public boolean getGoBackEnable(){
            FileEntity parent = fe.getParent();
            if(parent==null){
                return false;
            }
            return true;
        }

        @Override
        public int getCount() {
            if(fe==null)return 0;
            if(fe.getChilds()!=null)
                return fe.getChilds().length;

            return 0;
        }

        @Override
        public Object getItem(int arg0) {
            return fe.getChilds()[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if(arg1 == null){
                arg1 = inflater.inflate(R.layout.item_switchfile_filelist, null);
            }

            TextView nameTv = (TextView)arg1.findViewById(R.id.filename);
            TextView updateTimeTv = (TextView)arg1.findViewById(R.id.fileUpdateTime);
            TextView sizeTv = (TextView)arg1.findViewById(R.id.fileSize);
            ImageView iconIv = (ImageView)arg1.findViewById(R.id.fileicon);

            if(selectIndex == arg0){
                nameTv.setTextColor(textSelectColor);
                updateTimeTv.setTextColor(textSelectColor);
                sizeTv.setTextColor(textSelectColor);
            }else{
                nameTv.setTextColor(textNormalColor);
                updateTimeTv.setTextColor(textNormalColor);
                sizeTv.setTextColor(textNormalColor);
            }

            FileEntity item = fe.getChilds()[arg0];

            nameTv.setText(item.getName());
            updateTimeTv.setText(item.getDisplayUpdateTime());
            sizeTv.setText(item.getDisplaySize());
            iconIv.setBackgroundResource(item.getIcon());

            return arg1;
        }
    }

    class DevicesListAdapter extends BaseAdapter{

        private LayoutInflater inflater;

        private String devices[];

        private int select = 0;

        public DevicesListAdapter(Context context){
            inflater = LayoutInflater.from(context);
        }

        public DevicesListAdapter(Context context,String devices[]){
            this.devices = devices;
        }

        public void select(int position){
            select = position;
        }

        public int getSelectPosition(){
            return select;
        }

        public void setData(String data[]){
            devices = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(devices==null)return 0;
            return devices.length;
        }

        @Override
        public Object getItem(int arg0) {
            return devices[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if(arg1 == null){
                arg1 = inflater.inflate(R.layout.item_switchfile_devicelist, null);
            }

            TextView deviceNameTv = (TextView)arg1.findViewById(R.id.deviceName);

            if(select == arg0){
                deviceNameTv.setTextColor(textSelectColor);
            }else{
                deviceNameTv.setTextColor(textNormalColor);
            }

            deviceNameTv.setText(devices[arg0]);

            return arg1;
        }

    }


    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        context = null;
        contentView = null;
        mWindow = null;
        deviceLV = null;
        fileLV = null;
        cancelBtn = null;
        openBtn = null;
        curPathTV = null;
        openCallBack = null;
        mDevicesListAdapter = null;
        mFileListAdapter = null;
    }

}
