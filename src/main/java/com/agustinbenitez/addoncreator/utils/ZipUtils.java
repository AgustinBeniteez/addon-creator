package com.agustinbenitez.addoncreator.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Create a relative path for the zip entry
                    Path targetFile = sourceDir.relativize(file);
                    
                    // Add zip entry
                    zos.putNextEntry(new ZipEntry(targetFile.toString().replace("\\", "/")));
                    
                    // Copy file content
                    Files.copy(file, zos);
                    
                    // Close entry
                    zos.closeEntry();
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
