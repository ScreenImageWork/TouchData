package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButtonFromLinearLayout;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.view.TimeView;


public class OneBtnDialog implements IControler ,TimeView.onTimeOutListener {
	
	private Context context;
	
	private TPPopupWindow mWindow;
	
	private View contentView;
	
	private TextView msgView;
	
	private TPDialogButtonFromLinearLayout sureBtn;

	private TimeView mTimeView;

	private TextView timeTv1,timeTv2;

	public OneBtnDialog(Context context){
		
		this.context = context;
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		contentView = inflater.inflate(R.layout.dialog_onebtn, null);
		msgView = (TextView)contentView.findViewById(R.id.msgTv);
		mTimeView = (TimeView) contentView.findViewById(R.id.timeingView);
		sureBtn = (TPDialogButtonFromLinearLayout)contentView.findViewById(R.id.dialogSureBtn);
		TextView tv = (TextView) contentView.findViewById(R.id.dialogSureBtnTv);
		timeTv1 = (TextView) contentView.findViewById(R.id.timeTv1);
		timeTv2 = (TextView) contentView.findViewById(R.id.timeTv2);

		sureBtn.bindTextView(tv);
		sureBtn.setOnTPNewClickListener(new TPDialogButtonFromLinearLayout.OnTPNewClickListener() {
			@Override
			public void onPreClick() {
				timeTv1.setTextColor(Color.parseColor("#ffffff"));
				timeTv2.setTextColor(Color.parseColor("#ffffff"));
				mTimeView.setTextColor(Color.parseColor("#ffffff"));
			}

			@Override
			public void onClick() {
				timeTv1.setTextColor(Color.parseColor("#0088e7"));
				timeTv2.setTextColor(Color.parseColor("#0088e7"));
				mTimeView.setTextColor(Color.parseColor("#0088e7"));
			}
		});

		mTimeView.setOnTimeOutListener(this);
		mTimeView.setStartTime(10);
		mTimeView.setEndTime(0);
		mTimeView.setTimeMode(TimeView.MODE_COUNT_DOWN);

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
		mWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mTimeView.cancelTimer();
			}
		});
	}
	
	public void setMsg(String msg){
		if(msgView==null){
			return;
		}
		msgView.setText(msg);
	}
	
	public void setBtnListener(OnClickListener listener){
		sureBtn.setOnTPClickListener(listener);
	}

	@Override
	public boolean isShow() {
		if(mWindow==null){
			return false;
		}
		return mWindow.isShowing();
	}

	@Override
	public void show() {
		mWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
		mTimeView.reStartTimer();
	}

	@Override
	public void dismiss() {
		mWindow.dismiss();
	}

	@Override
	public void destory() {
		if(mWindow!=null&&mWindow.isShowing()){
			mWindow.dismiss();
		}
		context = null;
		mWindow = null;
		contentView = null;
		msgView = null;
		sureBtn = null;
	}

	@Override
	public void onTimeOut() {
		dismiss();
	}

	@Override
	public void onTimeingStart(int time) {
		mTimeView.setText(time+"s");
	}

	@Override
	public void onTimeingCancel() {
	}

	@Override
	public void onTimeing(int time) {
		mTimeView.setText(time+"s");
	}
}
