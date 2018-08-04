package com.kedacom.utils;

import java.math.BigDecimal;

/**
 * Created by zhanglei on 2017/5/22.
 *
 * java小数计算不准确，必须借助这个工具类去进行计算才可以
 */
public class ArithUtil {
    private static final int DEF_DIV_SCALE=10;

    private ArithUtil(){}
    //相加
    public static float add(float d1,float d2){
        BigDecimal b1=new BigDecimal(Float.toString(d1));
        BigDecimal b2=new BigDecimal(Float.toString(d2));
        return b1.add(b2).floatValue();

    }
    //相减
    public static float sub(float d1,float d2){
        BigDecimal b1=new BigDecimal(Float.toString(d1));
        BigDecimal b2=new BigDecimal(Float.toString(d2));
        return b1.subtract(b2).floatValue();
    }
    //相乘
    public static float mul(float d1,float d2){
        BigDecimal b1=new BigDecimal(Float.toString(d1));
        BigDecimal b2=new BigDecimal(Float.toString(d2));
        return b1.multiply(b2).floatValue();

    }
    //相除
    public static float div(float d1,float d2){

        return div(d1,d2,DEF_DIV_SCALE);

    }

    public static float div(float d1,float d2,int scale){
        if(scale<0){
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1=new BigDecimal(Float.toString(d1));
        BigDecimal b2=new BigDecimal(Float.toString(d2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).floatValue();

    }

}