package com.extclp.mirror.utils;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class PathUtils {

    public static void createParentDirectory(Path path) throws IOException {
        Path parent = path.getParent();
        if(parent != null && !Files.exists(parent)){
            Files.createDirectories(parent);
        }
    }

    private static FileSystem createZipFileSystem(Path path) throws URISyntaxException, IOException {
        return FileSystems.newFileSystem(
                new URI("jar:" + path.toUri().toString()), ImmutableMap.of("create", "true"));
    }

    public static void zip(Path from, Path to) throws IOException, URISyntaxException {
        createParentDirectory(to);
        try (FileSystem fs = createZipFileSystem(to)){
            zipCopy(from, fs.getPath("/"));
        }
    }
    public static void unzip(Path from, Path to) throws IOException, URISyntaxException {
        Files.createDirectories(to);
        try (FileSystem fs = createZipFileSystem(from)){
            zipCopy(fs.getPath("/"), to);
        }
    }
    private static void zipCopy(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>(){@Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if(!Files.isSameFile(from, dir)){
                Files.createDirectory(from.resolve(from.relativize(dir).toString()));
            }
            return FileVisitResult.CONTINUE;
        }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copy(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectory(to.resolve(from.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
