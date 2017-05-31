package org.wso2.carbon.server.util;

public class JarInfo {


    private String name;
    private String path;
    private String md5SumValue;

    JarInfo(String name, String path) {
        this(name, path, null);
    }
    JarInfo(String name, String path, String md5SumValue) {
        this.name = name;
        this.path = path;
        this.md5SumValue = md5SumValue;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getMd5SumValue() {
        return md5SumValue;
    }

    public void setMd5SumValue(String md5SumValue) {
        this.md5SumValue = md5SumValue;
    }
}
