package com.kedacom.tdc.utils;

import android.util.Log;

public class EncryptionUtils {
	
	/**
	 * 对字符串进行加密
	 * @param data 需要加密的数据
	 * @return String 加密后的数据
	 */
    public static String encode(String data){
			
			if(data==null)return null;
			
			char charArray[] =  data.toCharArray();
			
			int count = charArray.length;
			
			System.out.println(count);
			
			int half = count/2;
			
			char eCharArray[] = new char[count];
			
			for(int i = 0;i<count;i++){
				int a = ((int)charArray[i])<<1;
			    if(i<=half){
			    	eCharArray[half-i] = (char)a;
			    }else{
			    	eCharArray[count-(i-half)] = (char)a;
			    }
			}
			
			String str = new String(eCharArray);
			
			Log.e("error", "加密："+data+"----->"+str);
			
			return str;
		}
	
    /**
     * 对字符串进行解密
     * @param data 需要解密的数据
     * @return  String 解密后的数据
     */
	public static String decode(String data){
			
			if(data==null)return null;
			
			char charArray[] =  data.toCharArray();
			
			int count = charArray.length;
			
			System.out.println(count);
			
			int half = count/2;
			
			char dCharArray[] = new char[count];
			
			for(int i = 0;i<count;i++){
				int a = ((int)charArray[i])>>1;
			    if(i<=half){
			    	dCharArray[half-i] = (char)a;
			    }else{
			    	dCharArray[count-(i-half)] = (char)a;
			    }
			}
			
			String str = new String(dCharArray);
			Log.e("error", "解密："+data+"----->"+str);
			return str;
		}
}
		