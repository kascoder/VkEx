package org.kascoder.vkex.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ExportHistory {
    private List<ExportFailure> failures;
    private Collection<ExportHistoryItem> items;

    @JsonIgnore
    public boolean hasFailures() {
        return failures != null && !failures.isEmpty();
    }
}
