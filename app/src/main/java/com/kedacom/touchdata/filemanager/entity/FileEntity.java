package com.kedacom.touchdata.filemanager.entity;

import com.kedacom.touchdata.filemanager.FileUtils;

/**
 * Created by zhanglei on 2016/10/10.
 */
public class FileEntity {

    private int icon;

    private String name;

    private String absolutePath;

    private long size;

    private String displaySize;

    private int type;

    private String makeTime;

    private long updateTime;

    private FileEntity parent;

    private FileEntity childs[];

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMakeTime() {
        return makeTime;
    }

    public void setMakeTime(String makeTime) {
        this.makeTime = makeTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public FileEntity getParent() {
        return parent;
    }

    public void setParent(FileEntity parent) {
        this.parent = parent;
    }

    public FileEntity[] getChilds() {
        return childs;
    }

    public void setChilds(FileEntity[] childs) {
        this.childs = childs;
    }

    public String getDisplaySize() {
        return  FileUtils.formatFileSize(size);
    }

    public String getDisplayUpdateTime(){
        return FileUtils.formatDate(updateTime);
    }
}
