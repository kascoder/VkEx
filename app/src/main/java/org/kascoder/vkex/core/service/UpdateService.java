package org.kascoder.vkex.core.service;

import lombok.NonNull;
import org.kascoder.vkex.core.model.Update;
import org.kascoder.vkex.core.util.UpdateBackwardIncompatibleException;

import java.util.function.Consumer;

public interface UpdateService {
    void initiateUpdate(@NonNull Update update, boolean launch, Consumer<Exception> errorHandler) throws UpdateBackwardIncompatibleException;
}
