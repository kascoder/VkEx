package org.kascoder.vkex.cli.command.auth;

import lombok.extern.slf4j.Slf4j;
import org.kascoder.vkex.core.ApplicationContext;
import picocli.CommandLine.Command;

import javax.inject.Inject;

@Slf4j
@Command(name = "status")
public class StatusCommand implements Runnable {
    private final ApplicationContext applicationContext;

    @Inject
    public StatusCommand(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        if (applicationContext.hasPrincipal()) {
            LOGGER.warn("Logged in");
        } else {
            LOGGER.warn("You are not logged in. Run vkex auth login to authenticate.");
        }
    }
}
