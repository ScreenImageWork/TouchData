package com.kedacom.touchdata.whiteboard.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhanglei on 2017/4/5.
 */
public class DateUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat();

    public static String getCurTime(String pattern){
        dateFormat.applyPattern(pattern);
        return dateFormat.format(new Date());
    }


}
