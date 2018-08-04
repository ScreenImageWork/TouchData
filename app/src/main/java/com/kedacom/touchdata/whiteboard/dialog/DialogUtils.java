package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.kedacom.touchdata.R;


public class DialogUtils {
	
	
	public static PopupWindow showOpenFileDialog(Context context){
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		View view = inflater.inflate(R.layout.dialog_openfile, null);
		
		PopupWindow pw = new PopupWindow(context);
	
		
		return pw;
		
	}

}
