package com.extclp.mirror.config;

import java.io.File;
import java.text.SimpleDateFormat;

public class MirrorConfig{

    public int shutdownWaitTime = 15;

    public String mirrorNameColor = "§e";

    public String lockMirrorNameColor = "§4";

    public int createMirrorLimit = 5;

    public boolean compressBackupFile = true;

    public SimpleDateFormat mirrorDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public File backupsFolder = new File("mirror/backups");

    public File tempWorldFolder = new File("mirror/temp");

    public File oldWorldFolder = new File("mirror/old");

    public File mirrorsDataFile = new File("mirror/mirrors.json");

}