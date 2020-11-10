package org.kascoder.vkex.core.model.options;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kascoder.vkex.core.model.util.StorageType;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class StorageOptions {
    @NonNull
    private final StorageType type;
    private final String path;

    @JsonCreator
    public StorageOptions(@JsonProperty("type") @NonNull StorageType type, @JsonProperty("path") String path) {
        this.type = type;
        this.path = path;
    }

    @Override
    public String toString() {
        return "{\n" +
                "\t\ttype: '" + type + "'\n" +
                "\t\tpath: '" + path + "'\n" +
                "\t}";
    }
}
