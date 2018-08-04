package com.kedacom.touchdata.whiteboard.view;

import android.graphics.Bitmap;

import com.kedacom.touchdata.whiteboard.graph.Image;


public interface IImageView {
	
	void drawImage(Image mImage);
	
	void rotate(int angle);
	
    void translate(float ox, float oy);
    
    void scale(float scale);
    
    Image getImage();
    
    Bitmap saveToBitmap();
}
