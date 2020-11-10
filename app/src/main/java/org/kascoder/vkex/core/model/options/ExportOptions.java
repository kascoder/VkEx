package org.kascoder.vkex.core.model.options;

import org.kascoder.vkex.core.model.Conversation;
import org.kascoder.vkex.core.model.ExportType;
import org.kascoder.vkex.core.model.source.MessageSource;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;

@Value
public class ExportOptions {
    @NonNull
    Conversation conversation;
    @NonNull
    Set<ExportType> exportTypes;
    @NonNull
    MessageSource messageSource;
    BigDecimal totalSizeLimit;
}
