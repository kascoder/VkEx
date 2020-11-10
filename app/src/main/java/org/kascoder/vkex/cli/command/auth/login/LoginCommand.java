package org.kascoder.vkex.cli.command.auth.login;

import com.google.inject.Inject;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.service.VkClientMiddleware;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(name = "login", description = "Allows to log in Vkontakte social network")
public class LoginCommand implements Runnable {
    private final Executor executor;
    private final ApplicationContext applicationContext;

    @Inject
    public LoginCommand(@NonNull VkClientMiddleware vkClientMiddleware,
                        @NonNull ConfigurationService configurationService,
                        @NonNull ApplicationContext applicationContext) {
        this.executor = new Executor(vkClientMiddleware, configurationService);
        this.applicationContext = applicationContext;
    }

    @Option(names = { "-u", "--username" }, required = true, description = "Username")
    private String username;

    @Option(names = { "-p", "--password" }, arity = "0..1", required = true, description = "Password", interactive = true)
    private String password;

    @Override
    public void run() {
        Validate.notBlank(username, "Username cannot be empty");
        Validate.notBlank(password, "Password cannot be empty");

        if (applicationContext.hasPrincipal()) {
            LOGGER.warn("Already logged in");
            return;
        }

        executor.execute(username, password);
    }
}
