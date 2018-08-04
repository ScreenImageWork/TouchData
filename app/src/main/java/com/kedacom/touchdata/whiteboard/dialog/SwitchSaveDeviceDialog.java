package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kedacom.storagelibrary.model.StorageItem;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

import java.util.List;
import java.util.zip.DeflaterInputStream;

/**
 * 选择保存位置弹出框
 */
public class SwitchSaveDeviceDialog implements IControler {

	private final int TEXT_SELECT_COLOR;

	private final int TEXT_NORMAL_COLOR;

	private final int BTN_TEXT_SELECT_COLOR;

	private final int BTN_TEXT_NORMAL_COLOR;
	
	private Context context;
	
	private TPPopupWindow mWindow;
	
	private View contentView;
	
	private GridView mGridView;
	
	private TPDialogButton surBtn;
	
	private TPDialogButton cancelBtn;

	private TextView localDevicesNameTv;
	private LinearLayout localDevicesBtn;

	private ImageView localDevicesRedioBtn;
	
	private String selectDevPath;

	private SaveDeviceAdapter mSaveDeviceAdapter;

	public SwitchSaveDeviceDialog(final Context context){
		this.context = context;

		TEXT_SELECT_COLOR = context.getResources().getColor(R.color.switch_storage_device_text_normal_color);
		TEXT_NORMAL_COLOR = context.getResources().getColor(R.color.switch_storage_device_text_select_color);

		BTN_TEXT_SELECT_COLOR = context.getResources().getColor(R.color.dialog_btn_text_select_color);
		BTN_TEXT_NORMAL_COLOR = context.getResources().getColor(R.color.dialog_btn_text_normal_color);

		LayoutInflater inflater = LayoutInflater.from(context);

		contentView = inflater.inflate(R.layout.dialog_switchsavedevice, null);

		contentView.findViewById(R.id.xbtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)context).onDCancelBtnEvent(BaseActivity.DialogType.SWITCHDEVICE);
				dismiss();
			}
		});


		mGridView = (GridView)contentView.findViewById(R.id.switchDeviceGridView);
		
		surBtn = (TPDialogButton)contentView.findViewById(R.id.dialogSureBtn);
		
		cancelBtn = (TPDialogButton)contentView.findViewById(R.id.dialogCancelBtn);

		localDevicesBtn = (LinearLayout) contentView.findViewById(R.id.localDevicesBtn);

		localDevicesNameTv = (TextView) contentView.findViewById(R.id.deviceName);

		localDevicesRedioBtn = (ImageView) contentView.findViewById(R.id.localDevicesRedioBtn);

		contentView.findViewById(R.id.xbtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		mWindow = new TPPopupWindow(context);
		mWindow.setContentView(contentView);
		mWindow.setFocusable(true);
//		mWindow.setFocusable(true);
		mWindow.setOnDismissListener((BaseActivity)context);


		localDevicesBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				localDevicesRedioBtn.setBackgroundResource(R.drawable.redio_true);
				localDevicesNameTv.setTextColor(TEXT_SELECT_COLOR);
				selectDevPath = FileUtils.SAVE_WRITEBOARD_DIR;
				mSaveDeviceAdapter.select(-1);
			}
		});
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				localDevicesRedioBtn.setBackgroundResource(R.drawable.redio_false);
				localDevicesNameTv.setTextColor(TEXT_NORMAL_COLOR);

				mSaveDeviceAdapter.select(arg2);
				selectDevPath = ((StorageItem)((SaveDeviceAdapter)arg0.getAdapter()).getItem(arg2)).path;
			}
		});
		
	}

	private void initBtnStyle(){
		localDevicesRedioBtn.setBackgroundResource(R.drawable.redio_true);
		localDevicesNameTv.setTextColor(TEXT_SELECT_COLOR);
		selectDevPath = FileUtils.SAVE_WRITEBOARD_DIR;
		if(mSaveDeviceAdapter!=null) {
			mSaveDeviceAdapter.select(-1);
		}
		surBtn.setBackgroundResource(R.color.dialog_btn_normalColor);
		surBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
		cancelBtn.setBackgroundResource(R.color.dialog_btn_normalColor);
		cancelBtn.setTextColor(BTN_TEXT_NORMAL_COLOR);
	}
	
	public String getSelectDevPath(){
		return selectDevPath;
	}
	
	public void setSureBtnListener(OnClickListener listener){
		surBtn.setOnTPClickListener(listener);
	}
	
	public void setCancelBtnListener(OnClickListener listener){
		cancelBtn.setOnTPClickListener(listener);
	}

	@Override
	public boolean isShow() {
		return mWindow.isShowing();
	}
	
	public void show(List<StorageItem> devices){
		initBtnStyle();
		if(mSaveDeviceAdapter==null) {
			mSaveDeviceAdapter = new SaveDeviceAdapter(context, devices);
			mGridView.setAdapter(mSaveDeviceAdapter);
		}else{
			mSaveDeviceAdapter.setData(devices);
		}
		show();
	}

	@Override
	public void show() {
		mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
	}

	@Override
	public void dismiss() {
		mWindow.dismiss();
	}

	class SaveDeviceAdapter extends BaseAdapter{
	    
	    private List<StorageItem> devices;
	    
	    private LayoutInflater inflater;
	    
	    private int select = -1;
		
		public SaveDeviceAdapter(Context context,List<StorageItem> devices){
			this.devices = devices;
			inflater = LayoutInflater.from(context);
		}

		public void setData(List<StorageItem> devices){
			this.devices = devices;
			notifyDataSetChanged();
		}
		
		public void select(int position){
			select = position;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int arg0) {
			return devices.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			
			
			if(arg1==null){
				arg1 = inflater.inflate(R.layout.item_switchsavedevice, null);
			}
			
			ImageView redioBtn = (ImageView)arg1.findViewById(R.id.redioBtn);
			TextView deviceName = (TextView)arg1.findViewById(R.id.deviceName);
			
			if(select == arg0){
				redioBtn.setBackgroundResource(R.drawable.redio_true);
				deviceName.setTextColor(TEXT_SELECT_COLOR);
			}else{
				redioBtn.setBackgroundResource(R.drawable.redio_false);
				deviceName.setTextColor(TEXT_NORMAL_COLOR);
			}
			
			redioBtn.invalidate();
			
			deviceName.setText(devices.get(arg0).name);
			
			return arg1;
		}
		
	}
	
//	public class SaveDevice{
//
//		public String deviceName;
//		public String devicePath;
//
//		public SaveDevice(String name,String path){
//			deviceName = name;
//			devicePath = path;
//		}
//	}

	@Override
	public void destory() {
		if(mWindow!=null&&mWindow.isShowing()){
			mWindow.dismiss();
		}
		context = null;
		contentView = null;
		mWindow = null;
		mGridView = null;
		surBtn = null;
		cancelBtn = null;
		selectDevPath = null;
	}


}
