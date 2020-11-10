package org.kascoder.vkex.cli.command.auth;

import com.google.inject.Inject;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

import java.time.LocalDateTime;

@Slf4j
@Command(name = "logout")
public class LogoutCommand implements Runnable {
    private final ApplicationContext applicationContext;
    private final ConfigurationService configurationService;

    @Inject
    public LogoutCommand(ApplicationContext applicationContext, ConfigurationService configurationService) {
        this.applicationContext = applicationContext;
        this.configurationService = configurationService;
    }

    @Override
    public void run() {
        if (!applicationContext.hasPrincipal()) {
            return;
        }

        configurationService.loadConfiguration()
                .ifPresent(applicationConfiguration -> {
                    applicationConfiguration.setPrincipal(null);
                    configurationService.saveConfiguration(applicationConfiguration);
                    LOGGER.warn("Logout at " + LocalDateTime.now().toString());
                });
    }
}
