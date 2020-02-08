package com.extclp.mirror.config;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class MirrorInfo{

    private String name;

    private UUID owner;

    private String fileName;

    private long createDate;

    private String description;

    @SerializedName(value = "isCompress", alternate = "isZip")
    private boolean isCompress;

    @SerializedName(value = "lock", alternate = "final_")
    private boolean lock;

    public MirrorInfo(String name, UUID owner, String fileName, long createDate, String description, boolean isCompress){
        this.name = name;
        this.owner = owner;
        this.fileName = fileName;
        this.createDate = createDate;
        this.description = description;
        this.isCompress = isCompress;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public long getCreateDate() {
        return createDate;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }

    public boolean isCompress() {
        return isCompress;
    }
}