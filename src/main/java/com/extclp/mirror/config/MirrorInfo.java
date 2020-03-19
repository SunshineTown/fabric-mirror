package com.extclp.mirror.config;

import java.util.UUID;

public class MirrorInfo{

    public final String name;

    public final UUID creator;

    public final String fileName;

    public final long createTime;

    private String description;

    public final boolean compress;

    private boolean lock;

    public MirrorInfo(String name, UUID creator, String fileName, long createDate, String description, boolean isCompress){
        this.name = name;
        this.creator = creator;
        this.fileName = fileName;
        this.createTime = createDate;
        this.description = description;
        this.compress = isCompress;
    }

    public String getDescription() {
        return description;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }
}