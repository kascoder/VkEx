package org.kascoder.vkex.core.service.export;

import org.kascoder.vkex.core.model.ExportResultObject;
import org.kascoder.vkex.core.util.ExportContext;
import org.kascoder.vkex.core.util.JobProgressNotifier;
import lombok.NonNull;

public interface ConversationExportService {
    ExportResultObject export(@NonNull ExportContext exportContext, @NonNull JobProgressNotifier exportProgressNotifier);

    default ExportResultObject export(ExportContext exportContext) {
        return export(exportContext, JobProgressNotifier.ignore());
    }
}
