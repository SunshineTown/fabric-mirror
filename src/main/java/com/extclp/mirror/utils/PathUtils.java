package com.extclp.mirror.utils;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.stream.Collectors;

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
            copy(from, fs.getPath("/"));
        }
    }
    public static void unzip(Path from, Path to) throws IOException, URISyntaxException {
        Files.createDirectories(to);
        try (FileSystem fs = createZipFileSystem(from)){
            copy(fs.getPath("/"), to);
        }
    }

    public static void copy(Path from, Path to) throws IOException {
        for (Path path : Files.list(from).collect(Collectors.toList())) {
            Path targetPath = to.resolve(path.getFileName().toString());
            if(Files.isDirectory(path)){
                Files.createDirectory(targetPath);
                copy(path, targetPath);
            }else {
                Files.copy(path, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }
}
