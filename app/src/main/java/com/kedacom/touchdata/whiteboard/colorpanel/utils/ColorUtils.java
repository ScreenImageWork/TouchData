package com.kedacom.touchdata.whiteboard.colorpanel.utils;

import java.util.HashMap;

import android.graphics.Color;

public class ColorUtils extends Color{
	
	public static final String KEY_RED = "red";
	
	public static final String KEY_GREEN = "green";
	
	public static final String KEY_BLUE = "blue";
	
	public static HashMap<String , Integer> getRgb(int color){
		int r = red(color);
		int g = green(color);
		int b = blue(color);
		
		HashMap<String, Integer> rgb = new HashMap<String, Integer>();
		rgb.put(KEY_RED, r);
		rgb.put(KEY_GREEN, g);
		rgb.put(KEY_BLUE, b);
		
		return rgb;
	}
	
}
