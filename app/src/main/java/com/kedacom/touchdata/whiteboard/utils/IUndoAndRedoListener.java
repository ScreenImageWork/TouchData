package com.kedacom.touchdata.whiteboard.utils;

public interface IUndoAndRedoListener {

	void onUndoEnable(boolean enable);
	
	void onRedoEnable(boolean enable);
	
}
