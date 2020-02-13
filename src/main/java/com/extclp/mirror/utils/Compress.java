package com.extclp.mirror.utils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Compress {

    public static void zip(File from, File to) throws IOException {
        to.getParentFile().mkdirs();
        try (ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(to))) {
            Deque<File> files = Lists.newLinkedList();
            URI url = from.toURI();
            files.add(from);
            File f;
            while ((f = files.poll()) != null) {
                 String fileName = url.relativize(f.toURI()).toString();
                if (f.isDirectory()) {
                    fileName = fileName.endsWith("/") ? fileName : fileName + "/";
                    if(f.listFiles().length == 0){
                        ZipEntry zipEntry = new ZipEntry(fileName);
                        stream.putNextEntry(zipEntry);
                    } else {
                        Collections.addAll(files, f.listFiles());
                    }
                } else {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipEntry.setLastModifiedTime(FileTime.fromMillis(f.lastModified()));
                    stream.putNextEntry(zipEntry);
                    Files.copy(f, stream);
                }
                stream.closeEntry();
            }
        }
    }

    public static void unzip(File from, File to) throws IOException {
        try (ZipInputStream stream = new ZipInputStream(new FileInputStream(from))){
            ZipEntry zipEntry;
            while ((zipEntry = stream.getNextEntry()) != null){
                File file = new File(to, zipEntry.getName());
                if(zipEntry.getName().endsWith("/")){
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream outputStream = new FileOutputStream(file)){
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = stream.read(bytes)) != -1){
                            outputStream.write(bytes, 0, length);
                        }
                    }
                    file.setLastModified(zipEntry.getLastModifiedTime().toMillis());
                }
            }
        }
    }

    public static void copy(File from, File to) throws IOException {
        Deque<File> files = Lists.newLinkedList();

        URI uri = from.toURI().normalize();
        files.add(from);
        File f;
        while ((f = files.poll()) != null){
            if(f.isFile()){
                File newFile = new File(to, uri.relativize(f.toURI()).getPath());
                newFile.getParentFile().mkdirs();
                Files.copy(f, newFile);
                newFile.setLastModified(f.lastModified());
            } else {
                File[] childs = f.listFiles();
                if(childs == null ||childs.length == 0){
                    new File(to, uri.relativize(f.toURI()).getPath()).mkdirs();
                } else {
                    files.addAll(Arrays.asList(childs));
                }
            }
        }
    }

}
