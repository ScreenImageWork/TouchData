package com.kedacom.touchdata.whiteboard.dialog.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.kedacom.touchdata.R;

public class TPPopupWindow extends PopupWindow {

	@TargetApi(Build.VERSION_CODES.M)
	public TPPopupWindow(Context context){
		super(context);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setAnimationStyle(R.style.dialog_anim_style);

		setTouchable(true);
		setFocusable(false);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setOutsideTouchable(false);
//		setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE|WindowManager.LayoutParams.TYPE_TOAST);
		setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
	}

	@TargetApi(Build.VERSION_CODES.M)
	public TPPopupWindow(Context context,int type){
		super(context);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setAnimationStyle(R.style.dialog_anim_style);

		setTouchable(true);
		setFocusable(false);
		setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setOutsideTouchable(false);
		setWindowLayoutType(type);
	}

	
}
