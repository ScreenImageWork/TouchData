package com.kedacom.touchdata.whiteboard.helper;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.kedacom.tplog.TPLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanglei on 2016/9/27.
 */
public class GestureHelper implements IHelper {

    //方向
    public final int DIRECTION_LEFT = 0;

    public final int DIRECTION_TOP = 1;

    public final int DIRECTION_BOTTOM = 2;

    public final int DIRECTION_RIGHT = 3;

    public final int DIRECTION_LEFT_TOP = 4;

    public final int DIRECTION_RIGHT_TOP = 5;

    public final int DIRECTION_LEFT_BOTTOM = 6;

    public final int DIRECTION_RIGHT_BOTTOM = 7;

    public final int DIRECTION_UNKNOW = -1;

    //手势类型

    public final int GESTURE_PAINT = 9;  //绘制

    public final int GESTURE_MOVE = 10;  //画布拖动

    public final int GESTURE_ERASER = 11;  //擦除

    public final int GESTURE_SCALE = 12;  //缩放

    public final int GESTURE_ROTATE = 13;  //旋转

    public final int GESTURE_UNKUOW = -1;

    //拖动和擦除手势判断面积临界值，手势擦除大于该值 拖动小于该值
    public final int CRITICAL_MAX_MOVE_ERASER_AREA = 8000;
    public final int CRITICAL_MIN_MOVE_ERASER_AREA = 0;   //手势擦除面积下限值

    /**
     * 用面积来校验擦除和拖动非常的不准确，所以这里决定使用触摸点的个数来校验
     * 一般拖动时都是手指接触触摸屏幕进行滑动，而擦除都是手掌或者手背大面积进行接触的
     * 因此这里判断时已5个触摸点为分界线，大于5个触摸点是擦除，而小于5个点则为拖动
     * 注:这里的前提是只有一个方向
     * 该逻辑废弃
    **/
    public final int CRITICAL_MOVE_ERASER_POINTERS = 3;

    public final int CRITICAL_MOVE_ERASER_TIME = 100;

    public final long CRITICAL_MOVE_ERASER_DISTANCE = 10;


    //缩放和旋转临界值，如果大于该值为缩放小于该值为旋转
//    public final int CRITICAL_SCALE = 33;
    public final int CRITICAL_SCALE = 55;

    private int curGesture = GESTURE_UNKUOW;

    private IHelperListener mIHelperListener;

    /**
     * 在进行手势类型判断之前需要先进行触摸数据的采集
     * 目前暂定采集次数为10次  大约耗时160毫秒左右
     */

    private final int COLLECT = 10;

    /**
     * 进行偏移校准参数，如果两次移动X和Y的差值小于该值的话可视为未偏移
     */
    private final int CORRECT =3;

    /**
     * 用来保存所有采集来的点
     * Integer 是触摸点的Id
     */
    private Map<Integer,Pointer> pointers = new HashMap<Integer,Pointer>();

    private int curCollect = 0;   //当前采集数据次数

    private boolean isInit = false; //是否初始化成功

    private ScaleHelper mScaleHelper;

    private RotateHelper mRotateHelper;

    private TranslateHelper mTranslateHelper;

    private EraseHelper mGestureEraseHelper;

    private boolean isLock;

    public GestureHelper(Context context, IHelperListener listener){
        mIHelperListener = listener;
        init(context);
    }

    private void init(Context context){
        mScaleHelper = new ScaleHelper(context,mIHelperListener);
        mRotateHelper = new RotateHelper(mIHelperListener);
        mTranslateHelper = new TranslateHelper(mIHelperListener);
        mGestureEraseHelper = new EraseHelper(mIHelperListener);
    }

    public boolean isLock(){
        return isLock;
    }

    //特殊手势 拖动 1个触摸点也可以
    public void drag(MotionEvent event){
        mTranslateHelper.onTouchEvent(event);
        if(event.getActionMasked() == MotionEvent.ACTION_UP||event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            mTranslateHelper.complete();
        }
    }

    public void setCurGesture(int gesture,MotionEvent event){
        curGesture = gesture;
        isInit = true;
        curCollect = COLLECT +1;
        if(curGesture == GESTURE_ERASER) {
            if (mGestureEraseHelper != null) {
                mGestureEraseHelper.onTouchEvent(event);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        if(action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL){
            curCollect = 0;
            pointers.clear();
            isInit = false;

          switch(curGesture){
              case GESTURE_ROTATE:
                  mRotateHelper.complete();
                  break;
              case GESTURE_ERASER:
                  mGestureEraseHelper.complete();
                  break;
              case GESTURE_SCALE:
                  mScaleHelper.onTouchEvent(event);
                  break;
              case GESTURE_MOVE:
                  mTranslateHelper.complete();
                  break;
          }
            curGesture = GESTURE_UNKUOW;

            isLock = false;

            return true;

        }

        if(action == MotionEvent.ACTION_MOVE) {
            isLock = true;
            if (curCollect <= COLLECT) {//进行数据采集

                int count = event.getPointerCount();

                for (int i = 0; i < count; i++) {
                    int id = event.getPointerId(i);
                    int x = (int) event.getX(i);
                    int y = (int) event.getY(i);
                    Point point = new Point(x, y);
                    if (pointers.containsKey(id)) {
                        pointers.get(id).points.add(point);
                    } else {
                        Pointer p = new Pointer(id, point);
                        pointers.put(id, p);
                    }
                }

                curCollect++;
            }


            if (curCollect >= COLLECT && !isInit) {//数据采集完毕，可以开始计算了
                int pointerCount = pointers.size();

                if (pointerCount == 1) {//始终只有一个触摸点的，可以判断其为图元绘制
                    curGesture = GESTURE_PAINT;
                } else {//大于一个点的话那么就需要进行点移动方向计算了，通过计算的方向结果再进行分类
                    //进行所有手指的移动方向计算
                    for (Integer id : pointers.keySet()) {
                        computeDirection(pointers.get(id));
                    }
                    //进行归类并选择出代表点
                    classify(event);
                }

               // TPLog.printKeyStatus("手势判断完毕，当前识别手势为:"+getGestrueText(curGesture));
                isInit = true;
            }
        }

        if (isInit) {
            switch (curGesture) {
                case GESTURE_PAINT:
                    float x = event.getX();
                    float y = event.getY();
                    // mListener.onPaint(x, y);
                    break;
                case GESTURE_MOVE:
                    mTranslateHelper.onTouchEvent(event);
                    break;
                case GESTURE_ERASER:
//                    onGestureEraser(event);
                    mGestureEraseHelper.onTouchEvent(event);
                    break;
                case GESTURE_SCALE:
                    mScaleHelper.onTouchEvent(event);
                    break;
                case GESTURE_ROTATE:
                    mRotateHelper.onTouchEvent(event);
                    break;
            }
        }

        return true;
    }


    /**
     * 对采集到的点进行方向分类合并
     * @return
     */
    private Map<Integer,List<Pointer>> classify(MotionEvent event){
        //TPLog.printKeyStatus("开始进行触摸方向分类。。。");
        Map<Integer,List<Pointer>> map = new HashMap<Integer, List<Pointer>>();

        for(Integer id:pointers.keySet()){
            Pointer pointer = pointers.get(id);
            if(map.containsKey(pointer.direction)){
                map.get(pointer.direction).add(pointer);
            }else{
                List<Pointer> list = new ArrayList<Pointer>();
                list.add(pointer);
                map.put(pointer.direction, list);
            }
        }

        int count = map.size();

        //TPLog.printKeyStatus("触摸方向分类完毕，方向个数:"+count);

        if(count==1){//如果只有1类的话就直接返回
            //对一个方向的手势操作进行辨别  擦除|移动
            checkOneDirectionGesture(map,event);
            return map;
        }

        //分类大于2，进行第2次分类 大方向，上下左右

        //取出现在已经分好类的方向
        List<Integer> keyList = new ArrayList<Integer>();
        for(Integer key:map.keySet()){
            keyList.add(key);
        }

        //进行方向合并最终方向不会超过2个
        map = electDirection(map);

        int mapSize = map.size();

        //TPLog.printKeyStatus("最终分类个数："+mapSize);

        if(mapSize==1){
            //检查一个方向的具体操作类型
            checkOneDirectionGesture(map,event);

        }else if(mapSize==2){
            //如果是两个方向的话，就需要对两个方向的手指ID进行记录，方便后续取点操作

            List<Integer> idList1 = new ArrayList<Integer>();
            List<Integer> idList2 = new ArrayList<Integer>();

            Set<Integer> keySet = map.keySet();
            Iterator<Integer> it = keySet.iterator();
            int d1 = it.next();
            List<Pointer> pointers1 = map.get(d1);
            for(Pointer p:pointers1){
                idList1.add(p.id);
            }

            int d2 = it.next();
            List<Pointer> pointers2 = map.get(d2);
            for(Pointer p:pointers2){
                idList2.add(p.id);
            }

            //检查两个方向的具体手势类型，旋转|缩放
            checkDoubleDirectionGesture(map,idList1,idList2);
        }

        return map;
    }


    /**
     * 检查两个方向的手势类型
     * @param map
     */
    private void checkDoubleDirectionGesture(Map<Integer, List<Pointer>> map,List<Integer> idList1,List<Integer> idList2){

        int count1 = idList1.size();
        int count2 = idList2.size();

        float sx = 0;
        float sy = 0;

        float ex = 0;
        float ey = 0;

        float sx1 = 0;
        float sy1 = 0;

        float ex1 = 0;
        float ey1 = 0;

        Set<Integer> keySet = map.keySet();

        for(Integer key:keySet){
            List<Pointer> list = map.get(key);
            for(int k = 0;k<list.size();k++){
                Pointer p = list.get(k);
                int id = p.id;
                for(int j = 0;j<count1;j++){//1
                    if(id == idList1.get(j)){
                        sx = sx + p.points.get(0).x;
                        sy = sy + p.points.get(0).y;

                        ex = ex + p.points.get(p.points.size() - 1).x;
                        ey = ey + p.points.get(p.points.size() - 1).y;
                    }
                }

                for(int y = 0; y < count2; y++){
                    if(id == idList2.get(y)){
                        sx1 = sx1 + p.points.get(0).x;
                        sy1 = sy1 + p.points.get(0).y;

                        ex1 = ex1 + p.points.get(p.points.size() - 1).x;
                        ey1 = ey1 + p.points.get(p.points.size() - 1).y;
                    }
                }
            }
        }

        float csx = sx / count1;
        float csy = sy /count1;

        float cex = ex/count1;
        float cey = ey/count1;

        float csx1 = sx1 / count2;
        float csy1 = sy1 /count2;

        float cex1 = ex1 / count2;
        float cey1 = ey1 / count2;

        double d1 = computeSegmentLength(csx, csy, csx1, csy1);
        double d2 = computeSegmentLength(cex, cey, cex1, cey1);

        int cl = (int)Math.abs(d2 - d1);

        if(cl>=CRITICAL_SCALE){
            curGesture = GESTURE_SCALE;

        }else {
            curGesture = GESTURE_ROTATE;
            mRotateHelper.setDoubleHandId(idList1,idList2);
        }

    }

    /**
     * 检查一个方向的手势类型
     * @param map  Map<int direction,List<Pointer>>
     */
    private void checkOneDirectionGesture(Map<Integer, List<Pointer>> map,MotionEvent event){
        Set<Integer> keySet = map.keySet();
        int pointerCount = 0;
        for(int key:keySet){
            List<Pointer> list = map.get(key);
            pointerCount = pointerCount +  list.size();
        }


        Path path = new Path();
        for(int key:keySet){
            List<Pointer> list = map.get(key);
            for(int i = 0 ; i<list.size();i++){
                Pointer p = list.get(i);
                Point point = p.points.get(p.points.size() - 1);
                if(path.isEmpty()){
                    path.moveTo(point.x, point.y);
                }else{
                    path.lineTo(point.x, point.y);
                }
            }
        }

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        float area = bounds.width() * bounds.height();

        float startCx = 0;
        float endCx = 0;

        float startCy = 0;
        float endCy = 0;

        for(int key:keySet){
            List<Pointer> item = map.get(key);
            for(int i = 0;i<item.size();i++){
                List<Point> list = item.get(i).points;
                startCx += list.get(0).x;
                startCy += list.get(0).y;

                endCx += list.get(list.size()-1).x;
                endCy += list.get(list.size()-1).y;
            }
        }

        startCx = startCx / pointerCount;
        startCy = startCy /pointerCount;

        endCx = endCx / pointerCount;
        endCy = endCy / pointerCount;

        double startDistance = Math.abs(Math.sqrt(startCx*startCx-startCy*startCy));
        double endDistance = Math.abs(Math.sqrt(endCx*endCx-endCy*endCy));

        double cDistance = Math.abs(endDistance - startDistance);

        long downTime = event.getDownTime();
        long eventTime = event.getEventTime();

        long cTime = eventTime - downTime;

        if(cTime>CRITICAL_MOVE_ERASER_TIME&&cDistance<CRITICAL_MOVE_ERASER_DISTANCE&&area<CRITICAL_MAX_MOVE_ERASER_AREA){
            curGesture = GESTURE_ERASER;
        }else{
            curGesture = GESTURE_MOVE;
        }
    }

    /**
     * 进行方向合并
     * @param map
     * @return 合并后的数据
     */
    private Map<Integer,List<Pointer>> electDirection(Map<Integer,List<Pointer>> map){
        //TPLog.printKeyStatus("开始进行方向合并。。。");
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        Set<Integer> keSet = map.keySet();

        int pointerCount = 0;
        //投票
        for(int key : keSet){
            pointerCount += map.get(key).size();
            if(key == DIRECTION_LEFT||key == DIRECTION_LEFT_BOTTOM||key == DIRECTION_LEFT_TOP){
                left++;
            }
            if(key == DIRECTION_TOP||key == DIRECTION_LEFT_TOP||key == DIRECTION_RIGHT_TOP){
                top++;
            }
            if(key == DIRECTION_RIGHT_TOP|| key == DIRECTION_RIGHT || key == DIRECTION_RIGHT_BOTTOM){
                right++;
            }
            if(key == DIRECTION_BOTTOM || key == DIRECTION_LEFT_BOTTOM || key == DIRECTION_RIGHT_BOTTOM){
                bottom++;
            }
        }

        //合并
        if(left>0&&top>0&&right>0&&bottom>0){ //4
            if(left>=top&&left>=bottom&&left>=right){
                return doubleDirectionCombine(DIRECTION_LEFT,DIRECTION_RIGHT,map);
            }else if(top>=left&&top>=right&&top>=bottom){
                return doubleDirectionCombine(DIRECTION_TOP,DIRECTION_BOTTOM,map);
            }else if(right>=left&&right>=top&&right>=bottom){
                return doubleDirectionCombine(DIRECTION_RIGHT,DIRECTION_LEFT,map);
            }else if(bottom>=top&&bottom>=right&&bottom>=left){
                return doubleDirectionCombine(DIRECTION_BOTTOM,DIRECTION_TOP,map);
            }
        }else if(left >0&&top>0&&bottom>0&&right==0){//3
            if(left>top&&left>bottom&&pointerCount<=5){
                return oneDirectionCombine(DIRECTION_LEFT,map);
            }else{
                return doubleDirectionCombine(DIRECTION_BOTTOM,DIRECTION_TOP,map);
            }
        }else if(top>0&&left>0&&right>0&&bottom == 0){
            if(top>left&&top>right&&pointerCount<=5){
                return oneDirectionCombine(DIRECTION_TOP,map);
            }else{
                return doubleDirectionCombine(DIRECTION_LEFT,DIRECTION_RIGHT,map);
            }
        }else if(right>0&&top>0&&bottom>0 && left == 0){
            if(right>top&&right>bottom&&pointerCount<=5){
                return oneDirectionCombine(DIRECTION_RIGHT,map);
            }else{
                return doubleDirectionCombine(DIRECTION_BOTTOM,DIRECTION_TOP,map);
            }
        }else if(bottom>0&&right>0&&left>0 && top == 0){
            if(bottom>right&&bottom>left&&pointerCount<=5){
                return oneDirectionCombine(DIRECTION_BOTTOM,map);
            }else{
                return doubleDirectionCombine(DIRECTION_LEFT,DIRECTION_RIGHT,map);
            }
        }else if(left>0&&top>0&&right==0&&bottom==0){  //2
            if(left>top){
                return oneDirectionCombine(DIRECTION_LEFT,map);
            }else{
                return oneDirectionCombine(DIRECTION_TOP,map);
            }
        }else if(top>0&&right>0&&left==0&&bottom==0){
            if(top>right){
                return oneDirectionCombine(DIRECTION_TOP,map);
            }else{
                return oneDirectionCombine(DIRECTION_RIGHT,map);
            }
        }else if(right>0&&bottom>0&&top==0&&left==0){
            if(right>bottom){
                return oneDirectionCombine(DIRECTION_RIGHT,map);
            }else{
                return oneDirectionCombine(DIRECTION_BOTTOM,map);
            }
        }else if(bottom>0&&left>0&&top==0&&right==0){
            if(bottom>left){
                return oneDirectionCombine(DIRECTION_BOTTOM,map);
            }else{
                return oneDirectionCombine(DIRECTION_LEFT,map);
            }
        }else if(left>0&&right>0&&top==0&&bottom==0){
            return doubleDirectionCombine(DIRECTION_LEFT,DIRECTION_RIGHT,map);
        }else if(top>0&&bottom>0&&left == 0&&right==0){
            return doubleDirectionCombine(DIRECTION_TOP,DIRECTION_BOTTOM,map);
        }

        else if(left>0&&right==0&&top==0&&bottom==0){  //1
            return oneDirectionCombine(DIRECTION_LEFT,map);
        }else if(top>0&&right==0&&left==0&&bottom==0){
            return oneDirectionCombine(DIRECTION_TOP,map);
        }else if(right>0&&top==0&&left==0&&bottom==0){
            return oneDirectionCombine(DIRECTION_RIGHT,map);
        }else if(bottom>0&&top==0&&left==0&&right==0){
            return oneDirectionCombine(DIRECTION_BOTTOM,map);
        }

       // TPLog.printKeyStatus("方向合并完成，合并后方向个数:"+map.size());
        return map;

    }

    /**
     * 将所有的方向数据合并成两个方向数据
     * @param direction1 需要合并成的第一个方向
     * @param direction2 需要合并成的第二个方向
     * @param map 多个方向的数据
     * @return Map<Integer,List<Pointer>> 返回包含两个方向的数据（direction1和direction2）
     */
    public Map<Integer,List<Pointer>> doubleDirectionCombine(int direction1, int direction2 , Map<Integer,List<Pointer>> map){

        Map<Integer,List<Pointer>> doubleDMap = new HashMap<Integer, List<Pointer>>();
        doubleDMap.put(direction1, new ArrayList<Pointer>());
        doubleDMap.put(direction2, new ArrayList<Pointer>());

        for(int key:map.keySet()){
            if(checkDirection(direction1,key)){
                doubleDMap.get(direction1).addAll(map.get(key));
            }else{
                doubleDMap.get(direction2).addAll(map.get(key));
            }
        }

        return doubleDMap;

    }

    /**
     * 将所有方向数据合并成一个方向数据
     * @param direction 需要合并成的方向
     * @param map  所有方向数据
     * @return Map<Integer,List<Pointer>> 只包含一个方向的数据
     */
    public Map<Integer,List<Pointer>> oneDirectionCombine(int direction , Map<Integer,List<Pointer>> map){
        Map<Integer,List<Pointer>> doubleDMap = new HashMap<Integer, List<Pointer>>();
        doubleDMap.put(direction, new ArrayList<Pointer>());
        for(int key:map.keySet()){
            doubleDMap.get(direction).addAll(map.get(key));
        }
        return doubleDMap;
    }

    /**
     * 检测两个方向是否可以进行合并
     * @param direction1  方向1
     * @param direction2 方向2
     * @return  boolean  true 可以合并，false 不可以合并
     */
    public boolean checkDirection(int direction1,int direction2){
        if(direction1 == direction2 ){
            return true;
        }
        if(direction1==DIRECTION_LEFT){
            if(direction2 == DIRECTION_LEFT_TOP||direction2 == DIRECTION_LEFT_BOTTOM){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_TOP){
            if(direction2 == DIRECTION_LEFT_TOP||direction2 == DIRECTION_RIGHT_TOP){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_RIGHT){
            if(direction2 == DIRECTION_RIGHT_TOP||direction2 == DIRECTION_RIGHT_BOTTOM){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_BOTTOM){
            if(direction2 == DIRECTION_LEFT_BOTTOM||direction2 == DIRECTION_RIGHT_BOTTOM){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_LEFT_TOP){
            if(direction2 == DIRECTION_LEFT||direction2 == DIRECTION_TOP){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_RIGHT_TOP){
            if(direction2 == DIRECTION_RIGHT||direction2 == DIRECTION_TOP){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_RIGHT_BOTTOM){
            if(direction2 == DIRECTION_RIGHT||direction2 == DIRECTION_BOTTOM){
                return true;
            }else {
                return false;
            }
        }else if(direction1==DIRECTION_LEFT_BOTTOM){
            if(direction2 == DIRECTION_LEFT||direction2 == DIRECTION_BOTTOM){
                return true;
            }else {
                return false;
            }
        }

        return false;
    }


    //进行点的移动方向计算
    private  void computeDirection(Pointer pointer){
        int count = pointer.points.size();
		int x = 0;
		int y = 0;
		for(int i= 0;i<count;i++){
			Point point = pointer.points.get(i);
			x = x + point.x;
			y = y + point.y;
		}

		int cx = x/count;
		int cy = y/count;

        int sx = pointer.points.get(0).x;
        int sy = pointer.points.get(0).y;

        int dx = Math.abs(cx - sx);
        int dy = Math.abs(cy - sy);

        if(cx>=sx&&cy>=sy&&dx>=CORRECT&&dy>=CORRECT){//右下
            pointer.direction = DIRECTION_RIGHT_BOTTOM;
        }else if(cx<=sx&&cy<=sy&&dx>=CORRECT&&dy>=CORRECT){//左上
            pointer.direction = DIRECTION_LEFT_TOP;
        }else if(cx>=sx&&cy<=sy&&dx>=CORRECT&&dy>=CORRECT){//右上
            pointer.direction = DIRECTION_RIGHT_TOP;
        }else if(cx<=sx&&cy>=sy&&dx>=CORRECT&&dy>=CORRECT){//左下
            pointer.direction = DIRECTION_LEFT_BOTTOM;
        }else if(cx<=sx&&dy<=CORRECT&&dx>=CORRECT){//左
            pointer.direction = DIRECTION_LEFT;
        }else if(cx>=sx&&dy<=CORRECT&&dx>=CORRECT){//右
            pointer.direction = DIRECTION_RIGHT;
        }else if(cy<=sy&&dx<=CORRECT&&dy>=CORRECT){//上
            pointer.direction = DIRECTION_TOP;
        }else if(cy>=sy&&dx<=CORRECT&&dy>=CORRECT){//下
            pointer.direction = DIRECTION_BOTTOM;
        }

        /*
		   Log.e("error", "*****************************************************");
		   Log.e("error", "computeDirection--------------------->"+count);
           String msg = "";
           for(int i = 0;i<pointer.points.size();i++){
               msg = msg + "("+pointer.points.get(i).x +","+ pointer.points.get(i).y+")；";
            }
           Log.e("error", "points---------->"+msg);
		   Log.e("error", "direction---------->"+pointer.direction);
		   Log.e("error", "sx--------->"+sx+",sy----------->"+sy);
		   Log.e("error", "cx--------->"+cx+",cy----------->"+cy);
		   Log.e("error", "dx--------->"+dx+",dy----------->"+dy);
		   Log.e("error", "*****************************************************");
		   */
    }

    /**
     * 计算两点间的距离
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @return
     */
    private double computeSegmentLength(float x,float y,float x1,float y1){
        double length = 0;
        length = Math.sqrt(Math.abs((x - x1) * (x - x1)) + Math.abs((y - y1) * (y-y1)));
        return length;
    }


    public void setCurAngle(int angle){
        mRotateHelper.setCurAngle(angle);
    }

    public void setCurScale(float scale){
        mScaleHelper.setCurScale(scale);
    }

    public void setCurTranslate(float ox,float oy){
        mTranslateHelper.setCurTranslate(ox,oy);
    }

    class Pointer{
        public int id;
        public List<Point> points = new ArrayList<Point>();
        public int direction = DIRECTION_UNKNOW;
        public Pointer(int id,Point point){
            this.id = id;
            points.add(point);
        }
    }

    @Override
    public void onDestory() {
        mGestureEraseHelper.onDestory();
        mRotateHelper.onDestory();
        mScaleHelper.onDestory();
        mTranslateHelper.onDestory();

        mGestureEraseHelper = null;
        mRotateHelper = null;
        mScaleHelper = null;
        mTranslateHelper = null;
    }


    private String getGestrueText(int gestrue){
        String text = "";
        switch(gestrue){
            case GESTURE_PAINT:
                text = "GESTURE_PAINT";
                break;
            case GESTURE_ERASER:
                text = "GESTURE_ERASER";
                break;
            case GESTURE_MOVE:
                text = "GESTURE_MOVE";
                break;
            case GESTURE_ROTATE:
                text = "GESTURE_ROTATE";
                break;
            case GESTURE_SCALE:
                text = "GESTURE_SCALE";
                break;
            case GESTURE_UNKUOW:
                text = "GESTURE_UNKUOW";
                break;
        }

        return text;
    }

}
