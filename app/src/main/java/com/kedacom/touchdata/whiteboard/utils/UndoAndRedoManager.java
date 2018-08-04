package com.kedacom.touchdata.whiteboard.utils;

import java.util.ArrayList;
import java.util.List;

import com.kedacom.touchdata.whiteboard.op.IOperation;

public class UndoAndRedoManager {
	
	private final int MAX_OPT = 5;
	
	ArrayList<IOperation> undoArray = new ArrayList<IOperation>();
	
	ArrayList<IOperation> redoArray = new ArrayList<IOperation>();
	
	private IUndoAndRedoListener muarListener;
	
	public UndoAndRedoManager(){
		
	}
	
	public int addToUndo(IOperation opt){
		undoArray.add(0,opt);
		int count = undoArray.size();
		if(count>MAX_OPT){
			undoArray.remove(count -1);
		}
		redoArray.clear();
		callBack();
		return count;
	}
	
	public  int addToRedo(IOperation opt){
		redoArray.add(0,opt);
		int count = redoArray.size();
		if(count>MAX_OPT){
			redoArray.remove(count -1);
		}
		return count;
	}
	
	public IOperation undo(){
		if(undoArray.size()<=0)return null;
		IOperation opt = undoArray.remove(0);
		addToRedo(opt);
		callBack();
		return opt;
	}
	
	public IOperation redo(){
		if(redoArray.size()<=0)return null;
		IOperation opt = redoArray.remove(0);
		undoArray.add(0,opt);
		callBack();
		return opt;
	}
	
   public boolean undoEnable(){
	   return undoArray.size() == 0?false:true;
   }
	
   public boolean redoEnable(){
	   return redoArray.size() == 0?false:true;
   }
   
   
   private void callBack(){
	   callBackUndo();
	   callBackRedo();
   }
   
   private void callBackUndo(){
	   if(muarListener!=null){
		   muarListener.onUndoEnable(undoEnable());
	   }
   }
   
   private void callBackRedo(){
	   if(muarListener!=null){
		   muarListener.onRedoEnable(redoEnable());
	   }
   }
   
   
   public void setUndoAndRedoListener(IUndoAndRedoListener listener){
	   muarListener = listener;
   }
   
   
   public List<IOperation> getUndoList(){
	   return undoArray;
   }
   
   public List<IOperation> getRedoList(){
	   return redoArray;
   }
   
   public void clearUnoList(){
	   undoArray.clear();
   }
   
   public void clearRedoList(){
	   redoArray.clear();
   }

}
