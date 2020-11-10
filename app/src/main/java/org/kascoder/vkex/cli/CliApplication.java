package org.kascoder.vkex.cli;

import org.kascoder.vkex.cli.command.auth.AuthCommand;
import org.kascoder.vkex.cli.command.export.ExportCommand;
import org.kascoder.vkex.cli.command.preferences.PreferencesCommand;
import org.kascoder.vkex.cli.command.update.UpdateCommand;
import org.kascoder.vkex.cli.util.DependencyFactory;
import org.kascoder.vkex.cli.util.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@Command(
        subcommands = {
                AuthCommand.class,
                PreferencesCommand.class,
                ExportCommand.class,
                UpdateCommand.class
        }
)
public class CliApplication {
    public static void main(String[] args) {
        var dependencyFactory = new DependencyFactory();
        CommandLine.IExecutionStrategy executionStrategy;
        try {
            executionStrategy = dependencyFactory.create(CommandLine.IExecutionStrategy.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        new CommandLine(new CliApplication(), dependencyFactory)
                .setExecutionExceptionHandler(new ExceptionHandler())
                .setExecutionStrategy(executionStrategy)
                .execute(args);
    }
}
