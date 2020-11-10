package org.kascoder.vkex.core.util;

import lombok.NonNull;
import org.kascoder.vkex.core.model.JobProgress;

public interface JobProgressNotifier {
    void notify(@NonNull JobProgress job);

    static JobProgressNotifier ignore() {
        return job -> {};
    }
}
