package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.page.IPage;


public class ThreeBtnDialog implements IControler {

	private final int BTN_TEXT_SELECT_COLOR;

	private final int BTN_TEXT_NORMAL_COLOR;
	
	private Context context;
	
	private TPPopupWindow mWindow;

	private View contentView;

	private TextView titleView;
	
	private TextView msgView;
	
	private TPDialogButton btn1;
	
	private TPDialogButton btn2;
	
	private TPDialogButton btn3;

	private String curFileName;

	private IPage curSavePage;

	private OnClickListener mBtn2Listener;

	private OnClickListener mBtn3Listener;


	public ThreeBtnDialog(Context context){
		
		this.context = context;

		BTN_TEXT_SELECT_COLOR = context.getResources().getColor(R.color.dialog_btn_text_select_color);
		BTN_TEXT_NORMAL_COLOR = context.getResources().getColor(R.color.dialog_btn_text_normal_color);
		
		LayoutInflater inflater =  LayoutInflater.from(context);

		contentView = inflater.inflate(R.layout.dialog_threebtn, null);
		titleView = (TextView) contentView.findViewById(R.id.dialogTitle);
		msgView = (TextView)contentView.findViewById(R.id.msgTv);
		btn1 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn1);
		btn2 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn2);
		btn3 = (TPDialogButton)contentView.findViewById(R.id.dialogBtn3);

		contentView.findViewById(R.id.xbtn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseActivity)ThreeBtnDialog.this.context).clickListener.onClick(btn3);
				dismiss();
			}
		});

//		btn2.setOnTPClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
////				btn1.setBackgroundResource(R.drawable.dialog_btn_style2);
//				btn2.setTextColor(BTN_TEXT_SELECT_COLOR);
////				btn1.invalidate();
////				btn2.setBackgroundResource(R.drawable.dialog_btn_style);
////				btn2.setTextColor(BTN_TEXT_SELECT_COLOR);
//				if(mBtn2Listener!=null){
//					mBtn2Listener.onClick(btn2);
//				}
//				//handler.sendEmptyMessageDelayed(100	,200);
//			}
//		});
//
//		btn3.setOnTPClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
////				btn1.setBackgroundResource(R.drawable.dialog_btn_style2);
//				btn3.setTextColor(BTN_TEXT_SELECT_COLOR);
////				btn3.setBackgroundResource(R.drawable.dialog_btn_style);
////				btn3.setTextColor(BTN_TEXT_SELECT_COLOR);
//
//				if(mBtn3Listener!=null){
//					mBtn3Listener.onClick(btn3);
//				}
//				//handler.sendEmptyMessageDelayed(101,200);
//			}
//		});
		
		mWindow = new TPPopupWindow(context);
		mWindow.setContentView(contentView);
		mWindow.setFocusable(true);
		mWindow.setOnDismissListener((BaseActivity)context);
	}

	public void setTitle(String title){
		titleView.setText(title);
	}
	
	public void setMsg(String msg){
		msgView.setText(msg);
	}
	
	
	public void setBtn1Listener(OnClickListener listener){
		btn1.setOnTPClickListener(listener);
	}
	
	public void setBtn2Listener(OnClickListener listener){
		btn2.setOnTPClickListener(listener);
	}
	
	public void setBtn3Listener(OnClickListener listener){
		btn3.setOnTPClickListener(listener);
	}
	
	
	public void setBtnText(String text1,String text2,String text3){
		btn1.setText(text1);
		btn2.setText(text2);
		btn3.setText(text3);
	}


	public void setCurFileName(String curFileName){
		this.curFileName = curFileName;
	}

	public void setCurSavePage(IPage curSavePage) {
		this.curSavePage = curSavePage;
	}

	public IPage getCurSavePage() {
		return curSavePage;
	}

	public String getCurFileName(){
		return curFileName;
	}
	
	@Override
	public boolean isShow() {
		return mWindow.isShowing();
	}


	private void init(){
		btn1.setBackgroundResource(R.drawable.dialog_btn_style2);
		btn2.setBackgroundResource(R.drawable.dialog_btn_style2);
		btn3.setBackgroundResource(R.drawable.dialog_btn_style2);

		btn1.setTextColor(BTN_TEXT_NORMAL_COLOR);
		btn2.setTextColor(BTN_TEXT_NORMAL_COLOR);
		btn3.setTextColor(BTN_TEXT_NORMAL_COLOR);
	}

	@Override
	public void show() {
		init();
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
	public void destory() {
		if(mWindow!=null&&mWindow.isShowing()){
			mWindow.dismiss();
		}
		context = null;
		contentView = null;
		mWindow = null;
		msgView = null;
		btn1 = null;
		btn2 = null;
		btn3 = null;
		curFileName = null;
		curSavePage = null;
	}


	Handler handler  = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 100){
				if(mBtn2Listener!=null){
					mBtn2Listener.onClick(btn2);
				}
			}else if(msg.what == 101){
				if(mBtn3Listener!=null){
					mBtn3Listener.onClick(btn3);
				}
			}
		}
	};

}
