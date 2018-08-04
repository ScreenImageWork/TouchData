package com.kedacom.touchdata.net.mtnet.utils;

/**
 * Created by zhanglei on 2018/3/15.
 */

public class MTEntity {

    private String name;

    private String e164;

    private  int emMttype;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public int getEmMttype() {
        return emMttype;
    }

    public void setEmMttype(int emMttype) {
        this.emMttype = emMttype;
    }
}
