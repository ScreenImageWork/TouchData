package com.kedacom.httpserver.cache;


import java.util.concurrent.ConcurrentHashMap;


public class SdFileCache
{
    public static ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();

    public static void addSdFile(String name, String path)
    {
        if (!isContainFile(name))
            fileMap.put(name, path);
    }

    public static boolean isContainFile(String name)
    {
        return fileMap.containsKey(name);
    }

    public static String getFilePath(String name)
    {
        if (isContainFile(name))
            return fileMap.get(name);
        else
            return null;
    }
}
