package com.kedacom.touchdata.whiteboard.bg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by zhanglei on 2017/8/21.
 */
public class BgFactory {

    private final static int GRID_WIDTH = 40;

    private final static int GRID_HEIGHT = 40;

    public static void drawGriddingBg(Canvas canvas, float bgWidth, float bgHeight, int bgColor){
        canvas.drawColor(bgColor);

        //计算水平横线条数
        int hLineCount = (int)bgHeight/GRID_HEIGHT;
        int vLineCount = (int)bgWidth/GRID_WIDTH;

        float offsetX = (bgWidth - vLineCount*GRID_WIDTH)/2f;
        float offsetY = (bgHeight - hLineCount*GRID_HEIGHT)/2f;

       if(offsetX==0){
           offsetX = GRID_WIDTH/2f;
       }

        if(offsetY == 0){
            offsetY = GRID_HEIGHT/2f;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.parseColor("#08ffffff"));

        for(int i = 0;i<=hLineCount;i++){
            canvas.drawLine(0,offsetY,bgWidth,offsetY,paint);
            offsetY = offsetY + GRID_HEIGHT;
        }

        for(int i = 0;i<=vLineCount;i++){
            canvas.drawLine(offsetX,0,offsetX,bgHeight,paint);
            offsetX = offsetX + GRID_WIDTH;
        }
    }
}
