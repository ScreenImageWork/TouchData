package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.ProgressBar;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.tplog.TPLog;


public class ProgressDialog implements IControler {
	
	private Context context;
	
	private TPPopupWindow mWindow;
	
	private View contentView;
	
	private ProgressBar mProgressBar;
	
	private View progressLayout;
	
	private TextView progressTv;
	
	private TextView warnTv;

	private TextView stateText;
	
	private TPDialogButton cancelBtn;
	
	private int max;


	public ProgressDialog(Context context){
		
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		contentView = inflater.inflate(R.layout.dialog_openfile, null);
		mProgressBar = (ProgressBar)contentView.findViewById(R.id.mprogressBar);
		progressLayout = contentView.findViewById(R.id.progressLayout);
		progressTv = (TextView)contentView.findViewById(R.id.progressText);
		warnTv = (TextView)contentView.findViewById(R.id.warnTv);
		cancelBtn = (TPDialogButton)contentView.findViewById(R.id.dialogCancelBtn);

		stateText = (TextView) contentView.findViewById(R.id.stateText);

		contentView.findViewById(R.id.xbtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		mWindow = new TPPopupWindow(context);
		mWindow.setContentView(contentView);
		mWindow.setFocusable(true);
		mWindow.setOnDismissListener((BaseActivity)context);
		if(cancelBtn==null)return;
		cancelBtn.setOnTPClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dismiss();
			}
		});
	}
	
	private void reset(){
		mProgressBar.setProgress(0);
		warnTv.setVisibility(View.GONE);
		progressLayout.setVisibility(View.VISIBLE);
	}
	
	public void setProgressMax(int max){
		this.max = max;
		mProgressBar.setMax(max);
		mProgressBar.invalidate();
	}

	public void setHintText(String text){
		if(stateText==null){
			return;
		}
		stateText.setText(text);
		stateText.invalidate();
	}
	
	public void setProgress(int progress){
		mProgressBar.setProgress(progress);
		int temp = Math.round(((float)progress/(float)max)*100);
		progressTv.setText("已完成:"+temp+"%");
	}
	
	public void setCancelBtnListener(OnClickListener listener){
		cancelBtn.setOnClickListener(listener);
	}


	public void showLoadFileFailedText(String failedText){
		warnTv.setText(failedText);
		warnTv.setVisibility(View.VISIBLE);
		progressLayout.setVisibility(View.GONE);
	}
	
	public void showLoadFileFailedText(){
		String defaultText = context.getResources().getString(R.string.openFileFailedDefaultText);
		showLoadFileFailedText(defaultText);
	}

	@Override
	public void show() {
		stateText.setText("正在保存文件......");
		reset();
		mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0,0);
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
		if(mWindow!=null&&mWindow.isShowing())
			mWindow.dismiss();
		context = null;
		mWindow = null;
		contentView = null;
		mProgressBar = null;
		progressLayout = null;
		progressTv = null;
		warnTv = null;
		cancelBtn = null;
	}


}
