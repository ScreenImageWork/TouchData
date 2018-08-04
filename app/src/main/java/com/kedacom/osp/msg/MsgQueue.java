package com.kedacom.osp.msg;

import java.util.ArrayList;
import java.util.LinkedList;

public class MsgQueue<T> {

	private LinkedList<T> msgQueue = new LinkedList<T>();

	public synchronized void addMsg(T msg) {
			msgQueue.add(msg);
			notifyAll();
	}

	public boolean hasNext() {
		return !msgQueue.isEmpty();
	}

	public synchronized T nextMsg() throws InterruptedException {
			if (msgQueue.isEmpty()) {
				wait();
			}
			T msg = msgQueue.pop();
			return msg;
	}

	public synchronized ArrayList<T> nextAllMsg() throws InterruptedException {
			if (msgQueue.isEmpty()) {
				wait();
			}
			
			int msgCount = msgQueue.size();
			ArrayList<T> localList = new ArrayList<T>();

			for (int i = 0; i < msgCount; i++) {
				T msg = msgQueue.poll();
				localList.add(msg);
			}
		    //msgQueue.clear();
			return localList;
	}

	public synchronized boolean isEmpty(){
		return msgQueue.isEmpty();
	}

	public synchronized int getSize() {
		return msgQueue.size();
	}

	public synchronized void clear() {
		msgQueue.clear();
	}

}
