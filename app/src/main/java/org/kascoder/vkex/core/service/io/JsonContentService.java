package org.kascoder.vkex.core.service.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.kascoder.vkex.core.util.FileUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Optional;

@Slf4j
public class JsonContentService implements ContentService {
    private final ObjectMapper mapper;

    @Inject
    public JsonContentService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void write(@NonNull String path, @NonNull Object obj) {
        try (final FileWriter writer = new FileWriter(FileUtils.getOrCreate(buildFullFilePath(path)), false)) {
            mapper.writeValue(writer, obj);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public <T> Optional<T> read(@NonNull String path, @NonNull Class<T> clazz) {
        try (final FileReader reader = new FileReader(buildFullFilePath(path))) {
            return Optional.ofNullable(mapper.readValue(reader, clazz));
        } catch (FileNotFoundException ignored) {
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public boolean exist(@NonNull String path) {
        return new File(buildFullFilePath(path)).exists();
    }

    @Override
    public String buildFullFilePath(@NonNull String partialFilePath) {
        String p = partialFilePath;
        if (!p.endsWith(getFileExtension())) {
            p = partialFilePath + getFileExtension();
        }

        return p;
    }

    private String getFileExtension() {
        return ".json";
    }
}
