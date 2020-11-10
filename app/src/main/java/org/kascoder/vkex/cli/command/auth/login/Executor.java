package org.kascoder.vkex.cli.command.auth.login;

import org.kascoder.vkex.core.model.ApplicationConfiguration;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Executor {
    private final VkClientMiddleware vkClientMiddleware;
    private final ConfigurationService configurationService;

    public Executor(VkClientMiddleware vkClientMiddleware, ConfigurationService configurationService) {
        this.vkClientMiddleware = vkClientMiddleware;
        this.configurationService = configurationService;
    }

    public void execute(String username, String password) {
        var principal = vkClientMiddleware.authenticate(username, password);
        var applicationConfiguration = configurationService.loadConfiguration()
                .orElseGet(ApplicationConfiguration::new);
        applicationConfiguration.setPrincipal(principal);
        configurationService.saveConfiguration(applicationConfiguration);

        LOGGER.warn("Logged in successfully");
    }
}
