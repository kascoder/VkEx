package org.kascoder.vkex.cli.command.update;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.kascoder.vkex.core.model.Update;
import org.kascoder.vkex.core.service.RepositoryService;
import org.kascoder.vkex.core.service.UpdateService;
import org.kascoder.vkex.core.util.UpdateBackwardIncompatibleException;
import picocli.CommandLine.Command;

@Slf4j
@Command(name = "update")
public class UpdateCommand implements Runnable {
    private final UpdateService updateService;
    private final RepositoryService repositoryService;

    @Inject
    public UpdateCommand(UpdateService updateService, RepositoryService repositoryService) {
        this.updateService = updateService;
        this.repositoryService = repositoryService;
    }

    @Override
    public void run() {
        repositoryService.checkForUpdates()
                .ifPresentOrElse(this::update, () -> LOGGER.warn("Application is up to date"));
    }

    private void update(Update update) {
        try {
            updateService.initiateUpdate(update, false, e -> {});
        } catch (UpdateBackwardIncompatibleException e) {
            LOGGER.warn("The new version of the app isn't compatible with the current. Please, uninstall the app and install it again using the latest installer.");
            LOGGER.warn("Latest installer can be found here: https://vk.com/public195065105"); // TODO fix
        }
    }
}
