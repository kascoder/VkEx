package org.kascoder.vkex.desktop.controller.event;

import lombok.NonNull;
import lombok.Value;
import org.kascoder.vkex.core.model.options.SearchOptions;

@Value
public class SearchRequestedEvent {
    @NonNull SearchOptions searchOptions;
}
