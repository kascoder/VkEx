package org.kascoder.vkex.cli.util;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.kascoder.vkex.core.service.RepositoryService;
import picocli.CommandLine;

@Slf4j
public class StartupExecutionStrategy implements CommandLine.IExecutionStrategy {
    private final RepositoryService repositoryService;

    @Inject
    public StartupExecutionStrategy(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public int execute(CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException, CommandLine.ParameterException {
        init();
        return new CommandLine.RunLast().execute(parseResult);
    }

    private void init() {
        try {
            repositoryService.checkForUpdates()
                    .ifPresent(update -> LOGGER.warn("There's a new version of VkEx. Use 'vkex update' to update."));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
