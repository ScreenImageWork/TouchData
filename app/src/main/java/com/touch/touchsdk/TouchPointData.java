package com.touch.touchsdk;

/**
 * Created by user on 2017-11-07.
 */
public class TouchPointData {
    /** contact id **/
    public int c;
    /** touch status 0:none,1:down,2:move,3:up**/
    public int ts;
    /** touch original state **/
    public int s;
    /** coordinate X **/
    public int x;
    /** coordinate Y **/
    public int y;
    /** width **/
    public int w;
    /** height **/
    public int h;

    /** pen id range 1~4**/
    public int e;
    /** pressure  range 1~10**/
    public int p;
    /** original pressure range 0~1023**/
    public int op;
    /** eraser 0:no; 1:yes **/
    public int r;
}
