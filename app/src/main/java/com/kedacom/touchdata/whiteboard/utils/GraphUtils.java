package com.kedacom.touchdata.whiteboard.utils;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;

import java.util.ArrayList;
import java.util.List;


public class GraphUtils {

	
	/**
	 * 计算两点间的距离
	* @param x
	* @param y
	* @param x1
	* @param y1
	* @return 
	*/
	public static double computeSegmentLength(float x,float y,float x1,float y1){
		double length = 0;

		   length = Math.sqrt(Math.abs((x - x1) * (x - x1)) + Math.abs((y - y1) * (y-y1)));

		return length;
	}
	
	
	/**
	 * 坐标点围绕某一中心点进行旋转
	 * 
	 * @param point
	 *            要旋转的坐标点
	 * @param centerPoint
	 *            中心点
	 * @param angle
	 *            旋转角度
	 * @return 旋转后的坐标点
	 */
	public static Point computeRotatePoint(Point point, Point centerPoint, int angle) {

		double sx = point.x;
		double sy = point.y;

		double cx = centerPoint.x;
		double cy = centerPoint.y;

		double r = Math.abs(Math.sqrt((Math.abs((sx - cx) * (sx - cx)) + Math
				.abs((sy - cy) * (sy - cy)))));

		if (r == 0) {
			return point;
		}
		double ra = (double) angle * Math.PI / 180d;
		float x = new Float((sx - cx) * Math.cos(ra) + (sy - cy) * Math.sin(ra)
				+ cx);
		float y = new Float(-(sx - cx) * Math.sin(ra) + (sy - cy)
				* Math.cos(ra) + cy);

		Point p = new Point();
		p.set((int) x, (int) y);

		return p;
	}


	public static Rect computeGraphBounds(ArrayList<Graph> list,Matrix pageMatrix){
		if(list==null){
			return new Rect();
		}

		int graphCount = list.size();

		if(graphCount == 0){
			return new Rect();
		}

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;

		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for(int i = 0;i<graphCount;i++){
			Graph graph = list.get(i);
			Rect rect = graph.getBounds();
			if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
				Matrix invert = new Matrix();
				pageMatrix.invert(invert);
				RectF dst = new RectF();
				RectF src = new RectF(rect.left,rect.top,rect.right,rect.bottom);
				invert.mapRect(dst,src);
				rect.set((int)dst.left,(int)dst.top,(int)dst.right,(int)dst.bottom);
			}

			if(minX > rect.left){
				minX =  rect.left;
			}

			if(minY > rect.top){
				minY = rect.top;
			}

			if(maxX < rect.right){
				maxX = rect.right;
			}

			if(maxY < rect.bottom){
				maxY = rect.bottom;
			}
		}

		Rect bounds  = new Rect(minX,minY,maxX,maxY);
		return bounds;
	}


	public static RectF combineRect(RectF rf[]){
		RectF dstRf = new RectF();
		float minX = Integer.MAX_VALUE;
		float minY = Integer.MAX_VALUE;
		float maxX = Integer.MIN_VALUE;
		float maxY = Integer.MIN_VALUE;

		for(int i = 0;i<rf.length;i++){
			if(minX>rf[i].left){
				minX =rf[i].left;
			}

			if(minY>rf[i].top){
				minY = rf[i].top;
			}

			if(maxX<rf[i].right){
				maxX = rf[i].right;
			}

			if(maxY<rf[i].bottom){
				maxY = rf[i].bottom;
			}
		}

		dstRf.set(minX,minY,maxX,maxY);
		return dstRf;
	}

	/**
	 * 获取有效的点，移除掉距离过于远的坐标点
	 * @param event
	 * @param invalidList  当前已知的无效点，可以为null
	 * @param pointValidLen //点和点之间的有效距离，超过这个距离时则判定为无效点
	 * @return
	 */
	public static boolean checkPointerDownIsValid(MotionEvent event,List<Integer> invalidList,float pointValidLen){

		int actionIndex = event.getActionIndex();

		float x = event.getX(actionIndex);
		float y = event.getY(actionIndex);

		for(int i = 0;i<event.getPointerCount();i++){
			if(actionIndex==i){
				continue;
			}

			if(invalidList!=null&&!invalidList.isEmpty()){//屏蔽已知的无效点
				if(invalidList.contains(i)){
					continue;
				}
			}

			double length = computeSegmentLength(x,y,event.getX(i),event.getY(i));

			if(pointValidLen>=length){//有效点
				return true;
			}
		}

		return false;
	}

}
