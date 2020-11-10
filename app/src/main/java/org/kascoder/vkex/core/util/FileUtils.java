package org.kascoder.vkex.core.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;

@UtilityClass
public class FileUtils {
    public File getOrCreate(String filePath) {
        File file = new File(filePath);

        try {
            Files.createDirectories(file.getParentFile().toPath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (Files.notExists(file.toPath())) {
            try {
                Files.createFile(file.toPath());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return file;
    }
}
