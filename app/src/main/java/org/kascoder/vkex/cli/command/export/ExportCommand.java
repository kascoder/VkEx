package org.kascoder.vkex.cli.command.export;

import com.google.inject.Inject;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.options.StorageOptions;
import org.kascoder.vkex.core.model.util.StorageType;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.export.ConversationExportService;
import org.kascoder.vkex.core.service.io.ContentService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Command(name = "export")
public class ExportCommand implements Runnable {
    private final Executor executor;
    private final ApplicationContext applicationContext;

    private static final String EXPORT_PATH_REFERENCE = "${export.path}";

    @Inject
    public ExportCommand(@NonNull ContentService contentService,
                         @NonNull VkClientMiddleware vkClientMiddleware,
                         @NonNull ApplicationContext applicationContext,
                         @NonNull ConversationExportService conversationExportService) {
        this.executor = new Executor(contentService, applicationContext, vkClientMiddleware, conversationExportService);
        this.applicationContext = applicationContext;
    }

    @Option(names = "--conversations", split = ",")
    private Set<Integer> conversations;

    @Option(
            names = "--types",
            split = ",",
            description = "Valid values: ${COMPLETION-CANDIDATES}"
    )
    private Set<ExportType> exportTypes;

    @Option(names = "--export-size-limit", description = "Unit of measure - Mb. Default value: ${DEFAULT-VALUE}", defaultValue = "5000")
    private Double exportSizeLimit;

    @Option(names = "--export-path")
    private Path exportPath;

    @Option(names = "--export-history-path")
    private String exportHistoryPath;

    @Override
    public void run() {
        if (!applicationContext.hasPrincipal()) {
            throw new RuntimeException("You are not logged in");
        }

        BigDecimal limit = null;
        if (exportSizeLimit != null) {
            limit = new BigDecimal(exportSizeLimit);
            limit = limit.multiply(new BigDecimal(1024));
            limit = limit.multiply(new BigDecimal(1024));
        }

        try {
            prepareContext();
            executor.execute(exportHistoryPath, conversations, limit);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void prepareContext() {
        var preferences = applicationContext.getUserPreferences();
        if (exportPath != null) {
            preferences.setStorageOptions(new StorageOptions(StorageType.LOCAL, exportPath.toString()));
        }
        if (exportTypes != null && !exportTypes.isEmpty()) {
            preferences.setExportTypes(exportTypes);
        }

        if (conversations == null) {
            conversations = Collections.emptySet();
        }

        @NonNull var storageOptions = preferences.getStorageOptions();
        var path = storageOptions.getPath();
        if (isNotEmptyHistoryPath() && exportHistoryPath.contains(EXPORT_PATH_REFERENCE)) {
            exportHistoryPath = exportHistoryPath.replace(EXPORT_PATH_REFERENCE, path);
        }
        var exportPath = Path.of(path);
        if (Files.exists(exportPath)) {
            if (!Files.isDirectory(exportPath)) {
                throw new RuntimeException("Export path should direct to the directory");
            }
        } else {
            try {
                Files.createDirectories(exportPath);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private boolean isNotEmptyHistoryPath() {
        return exportHistoryPath != null && !exportHistoryPath.isEmpty();
    }
}
