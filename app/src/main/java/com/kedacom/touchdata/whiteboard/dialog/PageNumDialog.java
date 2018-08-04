package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2016/12/9.
 */
public class PageNumDialog implements IControler{

    private final int normalTextColor;

    private final int selectTextColor;

    private Context mContext;

    private  LayoutInflater inflater;

    private View contentView;

    private ListView numLv;

    private TPPopupWindow mWindow;

    private int curSelectPageNum;

    private PageNumAdapter mPageNumAdapter;



    public PageNumDialog(Context context){
        mContext = context;
        normalTextColor = context.getResources().getColor(R.color.pagenum_normal_textcolor);
        selectTextColor = context.getResources().getColor(R.color.pagenum_select_textcolor);

        inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.dilaog_pagenum,null);
        numLv = (ListView) contentView.findViewById(R.id.pageNumList);
        mPageNumAdapter = new PageNumAdapter();
        numLv.setAdapter(mPageNumAdapter);

        numLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                curSelectPageNum = i;
                ((BaseActivity)mContext).onDSelectSubPageNum(curSelectPageNum+1);
                mPageNumAdapter.notifyDataSetChanged();
            }
        });

        mWindow = new TPPopupWindow(context);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
       // mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setContentView(contentView);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setOnDismissListener((BaseActivity)context);
    }

    public int getCurSelectPageNum() {
        return curSelectPageNum;
    }

    public void setCurSelectPageNum(int curSelectPageNum) {
        this.curSelectPageNum = curSelectPageNum;
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {

    }

    public void show(int maxNum,View anchor){
        mPageNumAdapter.setData(maxNum);
        resetWindowHeight(maxNum);
        float pageNumLvWidth = mContext.getResources().getDimension(R.dimen.pagenum_list_width);
        int windowHeight = mWindow.getHeight();
        float bottomBarHeight = mContext.getResources().getDimension(R.dimen.touchdata_bottombar_height);
       // int offsetY = (int)bottomBarHeight;
//        Rect rect = new Rect();
//        anchor.getHitRect(rect);

        int offsetX = (anchor.getWidth() - (int)pageNumLvWidth)/2;

        mWindow.showAsDropDown(anchor,offsetX,0);

    }

    private void resetWindowHeight(int maxNum){
        int temMaxNum = maxNum;
        if(maxNum>6){
            temMaxNum = 6;
        }

        float itemHeight = mContext.getResources().getDimension(R.dimen.pagenum_list_item_height);
        float divHeight =  mContext.getResources().getDimension(R.dimen.pagenum_list_dividerheight);
        float lvPadTop =  mContext.getResources().getDimension(R.dimen.pagenum_list_item_paddingtop);
        float lvPadBottom =  mContext.getResources().getDimension(R.dimen.pagenum_list_item_paddingbottom);

        float itemTotalHeight = itemHeight * temMaxNum;
        float itemTotalDivHeight = (temMaxNum)*divHeight;

        float windowHeight = itemTotalHeight + itemTotalDivHeight + lvPadTop + lvPadBottom;
        mWindow.setHeight((int)windowHeight);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    class PageNumAdapter extends BaseAdapter{

        int numArray[];

        public void setData(int max){
            numArray = new int[max];
            for(int i = 1;i<=max; i++){
                numArray[i-1] = i;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(numArray==null)return 0;
            return numArray.length;
        }

        @Override
        public Object getItem(int i) {
            return numArray[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view==null){
                view = inflater.inflate(R.layout.item_paenum,null);
            }

            ImageView selectIv = (ImageView) view.findViewById(R.id.selectIv);
            TextView numTv = (TextView) view.findViewById(R.id.pageNumTv);

            numTv.setText(numArray[i]+"");

            if(curSelectPageNum == i){
                selectIv.setVisibility(View.VISIBLE);
                numTv.setTextColor(selectTextColor);
            }else{
                selectIv.setVisibility(View.GONE);
                numTv.setTextColor(normalTextColor);
            }

            return view;
        }
    }

    @Override
    public void destory() {
        if(mWindow!=null&&mWindow.isShowing()){
            mWindow.dismiss();
        }
        mContext = null;
        inflater = null;
        contentView = null;
        numLv = null;
        mWindow = null;
        mPageNumAdapter = null;
    }

}
