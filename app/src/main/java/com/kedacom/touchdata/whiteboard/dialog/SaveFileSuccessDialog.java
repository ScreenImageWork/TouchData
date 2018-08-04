package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kedacom.storagelibrary.unity.StroageManager;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;


public class SaveFileSuccessDialog implements IControler {
	
	private Context context;
	
	private TPPopupWindow mWindow;
	
	private View contentView;

	private TextView titleTv;
	
	private TextView savePathView;
	
	private TPDialogButton openFileDirBtn;
	
	private TPDialogButton cancelBtn;

	private TPDialogButton exitBtn;
	
	private String curFileDir;

	public SaveFileSuccessDialog(Context context){
		
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		contentView = inflater.inflate(R.layout.dialog_savefilesuccess, null);
		
		savePathView = (TextView)contentView.findViewById(R.id.filePath);
		titleTv = (TextView)contentView.findViewById(R.id.dialogTitle);
		openFileDirBtn = (TPDialogButton)contentView.findViewById(R.id.dialogOpenFileDir);

		exitBtn = (TPDialogButton) contentView.findViewById(R.id.exitBtn);

		cancelBtn = (TPDialogButton)contentView.findViewById(R.id.dialogCancelBtn);

		contentView.findViewById(R.id.xbtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)SaveFileSuccessDialog.this.context).onDSureBtnEvent(BaseActivity.DialogType.SAVESUCCESS);
				dismiss();
			}
		});

		savePathView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)SaveFileSuccessDialog.this.context).onDToFileManager(curFileDir);
			}
		});

		openFileDirBtn.setOnTPClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)SaveFileSuccessDialog.this.context).onDToFileManager(curFileDir);
			}
		});

		cancelBtn.setOnTPClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)SaveFileSuccessDialog.this.context).onDSureBtnEvent(BaseActivity.DialogType.SAVESUCCESS);
				dismiss();
			}
		});

		exitBtn.setOnTPClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)SaveFileSuccessDialog.this.context).finish();
			}
		});
		
		mWindow = new TPPopupWindow(context);
		mWindow.setContentView(contentView);
		mWindow.setFocusable(true);
		mWindow.setOnDismissListener((BaseActivity)context);
		
	}
	
	public void setSaveFilePath(String filePath,String aliasPath){
		curFileDir = filePath;
		savePathView.setText(aliasPath);
	}
	
	public String getSaveFilePath(){
		return curFileDir;
	}

	public void setCancelBtnText(String text){
		cancelBtn.setText(text);
	}

	public void setTitleText(String titleText){
		titleTv.setText(titleText);
	}

	private  void resetBtn(){
		openFileDirBtn.setBackgroundResource(R.drawable.dialog_btn_style);
		cancelBtn.setBackgroundResource(R.drawable.dialog_btn_style2);
		cancelBtn.setText("继续使用");
	}

	@Override
	public void show() {
		resetBtn();
		if(mWindow!=null){
			mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
		}
	}

	@Override
	public void dismiss() {
		if(mWindow!=null){
			mWindow.dismiss();
		}
	}

	@Override
	public boolean isShow() {
		return mWindow.isShowing();
	}

	@Override
	public void destory() {
		if(mWindow!=null&&mWindow.isShowing()){
			mWindow.dismiss();
		}
		context = null;
		mWindow = null;
		contentView = null;
		savePathView = null;
		openFileDirBtn = null;
		cancelBtn = null;
		curFileDir = null;
	}



}
