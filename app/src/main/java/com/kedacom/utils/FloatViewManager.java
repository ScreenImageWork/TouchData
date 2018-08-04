package com.kedacom.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * 悬浮控件管理
 * 使用时需添加以下权限：
 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 * @author zhanglei
 *
 */

public class FloatViewManager {
	
	public static final int WINDOW_LAYOUTPARAMS_WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;
	
	public static final int WINDOW_LAYOUTPARAMS_MATCH_PARENT = WindowManager.LayoutParams.MATCH_PARENT;
	
    //定义浮动窗口布局
    private View mFloatView;
    
    private WindowManager.LayoutParams wmParams;
    
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;
    
    private Context mContext;
    
    private boolean isShowing = false;
    
    public FloatViewManager(Context context){
    	this(context,null,0,0,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,false);
    }
    
    public FloatViewManager(Context context,View floatView){
    	this(context,floatView,0,0,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,false);
    }
    
    public FloatViewManager(Context context,View floatView,int posx,int posy){
    	this(context,floatView,posx,posy,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,WINDOW_LAYOUTPARAMS_WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,false);
    }
    
    public FloatViewManager(Context context,View floatView,int posx,int posy,int width,int height){
    	this(context,floatView,posx,posy,width,height,Gravity.LEFT|Gravity.TOP,false);
    }
    
    public FloatViewManager(Context context,View floatView,int posx,int posy,int width,int height,int gravity){
    	this(context,floatView,posx,posy,width,height,gravity,false);
    }
    
    /**
     * @param context 
     * @param floatView 需要浮动的控件
     * @param posx 控件浮动的X坐标
     * @param posy 控件浮动的y坐标
     * @param width 浮动控件的宽度
     * @param height 浮动控件的高度
     * @param gravity 浮动控件的相对屏幕的位置
     * @param isShow  是否直接显示
     */
	public FloatViewManager(Context context,View floatView,int posx,int posy,int width,int height,int gravity,boolean isShow){
		mContext = context;
		mFloatView = floatView;
		isShowing = isShow;
		initFloatWindow(posx,posy,width,height,gravity);
		if(isShowing){
			show();
		}
	}
	
	private void initFloatWindow(int posx,int posy,int width ,int height,int gravity) {
		wmParams = new WindowManager.LayoutParams();
		// 获取WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		// 设置window type
		wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		// 设置图片格式，效果为背景透明
		wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = gravity;

		// 以屏幕左上角为原点，设置x、y初始值
		wmParams.x = posx;
		wmParams.y = posy;

		// 设置悬浮窗口长宽数据
		wmParams.width = width;
		wmParams.height = height;
	}
	
	public void show(){
		if(mWindowManager==null||mFloatView==null){
			return;
		}

		if(isShowing){
			return;
		}

		  mWindowManager.addView(mFloatView, wmParams);
		  
		  isShowing = true;
	}
	
	
	public void hide(){
		if(mWindowManager==null||mFloatView==null){
			return;
		}
		
		if(!isShowing){
			return;
		}
		
		isShowing = false;
		
		 mWindowManager.removeView(mFloatView);
	}

	public void setAnimationStyle(int animationStyle){
		if(wmParams!=null){
			wmParams.windowAnimations = animationStyle;
		}
	}
	
	public void setFloatView(View view){
		mFloatView = view;
	}
	
	public void setFloatView(int viewResId){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mFloatView = inflater.inflate(viewResId, null);
	}
	
	public void setPosition(int px,int py){
		if(wmParams==null){
			return;
		}
		wmParams.x = px;
		wmParams.y = py;
		if(isShowing)
		mWindowManager.updateViewLayout(mFloatView, wmParams);
	}

	public void  setFloatViewWidth(int width){
		if(wmParams==null){
			return;
		}
		wmParams.width = width;
	}
	
	public void setFloatViewHeight(int height){
		if(wmParams==null){
			return;
		}
		wmParams.height = height;
	}
	
	public void setFloatGravity(int gravity){
		if(wmParams==null){
			return;
		}
		wmParams.gravity = gravity;
	}

	public boolean isShowing(){
		return isShowing;
	}
	
	public int getPositionX(){
		if(wmParams==null){
			return 0;
		}
		return wmParams.x;
	}
	
	public int getPositionY(){
		if(wmParams==null){
			return 0;
		}
		return wmParams.y;
	}
	
	public int getFloatViewWidth(){
		if(wmParams==null){
			return 0;
		}
		return wmParams.width;
	}
	
	public int getFloatViewHeight(){
		if(wmParams==null){
			return 0;
		}
		return wmParams.height;
	}
	
	public int getGravity(){
		if(wmParams==null){
			return Gravity.LEFT|Gravity.TOP;
		}
		return wmParams.gravity;
	}
	
	public void release(){
		if(mWindowManager!=null&&isShowing) {
			mWindowManager.removeView(mFloatView);
		}
		mWindowManager = null;
		mFloatView = null;
		wmParams = null;
		mContext = null;
	}
}
