package org.kascoder.vkex.core.model;

import org.kascoder.vkex.core.model.options.StorageOptions;
import org.kascoder.vkex.core.model.util.StorageType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.File;
import java.util.Set;

@Getter
@Setter
public class UserPreferences {
    public static final UserPreferences DEFAULT = createDefault();

    @NonNull
    private StorageOptions storageOptions;
    @NonNull
    private Set<ExportType> exportTypes;

    @Override
    public String toString() {
        return "UserPreferences {\n" +
                "\tstorageOptions: " + storageOptions + "\n" +
                "\texportTypes: " + exportTypes + "\n" +
                '}';
    }

    private static UserPreferences createDefault() {
        var preferences = new UserPreferences();
        var userDownloadsFolder = System.getProperty("user.home") + File.separator + "Downloads";
        preferences.setStorageOptions(new StorageOptions(StorageType.LOCAL, userDownloadsFolder));
        preferences.setExportTypes(Set.of(ExportType.values()));
        return preferences;
    }
}
