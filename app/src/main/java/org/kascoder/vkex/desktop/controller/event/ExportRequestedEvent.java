package org.kascoder.vkex.desktop.controller.event;

import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.source.MessageSource;
import org.kascoder.vkex.core.util.JobProgressNotifier;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
public class ExportRequestedEvent {
    boolean wrapPath;
    @NonNull String downloadPath;
    @NonNull MessageSource messageSource;
    @NonNull Set<ExportType> exportTypes;
    @NonNull JobProgressNotifier progressNotifier;
}
