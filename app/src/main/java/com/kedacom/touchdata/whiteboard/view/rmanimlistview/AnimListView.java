package com.kedacom.touchdata.whiteboard.view.rmanimlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.utils.ThumbnailLoader;

/**
 * 动画ListView 目前只支持Item删除动画 具体使用，参考PageThumbnaildialog2
 */

public class AnimListView extends ScrollView implements IAnimListObserver{
	
	private long ANIMATION_DURATION = 100;
	
	private Context mContext;
	
	private LinearLayout mContentView;
	
	private IAnimListAdapter mAdapter;
	
	private IAnimListItemClickListener mItemClickListener;

	public AnimListView(Context context) {
		super(context);
		init(context);
	}
	
	public AnimListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public AnimListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mContext = context;
		mContentView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.view_base_animlist,null);
		mContentView.setGravity(Gravity.BOTTOM);
		this.addView(mContentView);
	}

	public void removeAllChildViews(){
		if(mContentView!=null){
			mContentView.clearAnimation();
			mContentView.removeAllViews();
		}
	}
	
	public void setAdapter(IAnimListAdapter adapter){
		if(mAdapter!=null){
			mAdapter.unregisterDataSetObserver(this);
		}

		removeAllChildViews();

		mAdapter = adapter;
		
		mAdapter.registerDataSetObserver(this);
		onAnimListDataChanged();
		scrollToCurSelectItem();
	}

	@Override
	public void onAnimListDataChanged() {
		if(mAdapter==null){
			return;
		}
		boolean isEmpty = mAdapter.isEmpty();
		
		if(isEmpty){
			return;
		}

		ThumbnailLoader.getLoader().clearTask();
		
		int itemCount = mAdapter.getCount();
		
		int viewCount = mContentView.getChildCount();

		//移除多余的View
		if(itemCount<viewCount){
			int c = viewCount - itemCount;
			mContentView.removeViews(itemCount, c);
		}
		
		for(int i=0;i<itemCount;i++){
			if(i<viewCount){
			    mAdapter.getView(i, mContentView.getChildAt(i), null);
			}else{
				View view = mAdapter.getView(i, null, null);
				mContentView.addView(view);
			}
			
			mContentView.getChildAt(i).setTag(i);
			
			if(mItemClickListener!=null){
			mContentView.getChildAt(i).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Integer index = (Integer)arg0.getTag();
						mItemClickListener.onItemClick(index, arg0, mAdapter);
					}
				});
			}
		}

//		postInvalidate();
		invalidate();
	}


	public void scrollToCurSelectItem(){
		handler.sendEmptyMessage(100);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
 }

	public void startItemAnim(final View view){
		Integer index = (Integer)view.getTag();
		view.setVisibility(View.GONE);
		mContentView.removeView(view);
		mAdapter.removeItem(index);
	}

	public void setOnItemClickListener(IAnimListItemClickListener listener){
		mItemClickListener = listener;
		if(mContentView==null||mAdapter == null){
			return;
		}

		if(mItemClickListener==null){
			return;
		}
		
		int count = mContentView.getChildCount();
		for(int i = 0; i< count; i++){
			mContentView.getChildAt(i).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int index  = (Integer)arg0.getTag();
					mItemClickListener.onItemClick(index, arg0, mAdapter);
				}
			});
		}
	}


	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 100){
				View view = mContentView.getChildAt(mAdapter.getSelectItemIndex());
				float x = view.getX();
				float y = view.getY();
				smoothScrollTo((int)x,(int)y);
			}
		}
	};
}
