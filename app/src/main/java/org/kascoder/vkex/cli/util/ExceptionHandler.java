package org.kascoder.vkex.cli.util;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

@Slf4j
public class ExceptionHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
        LOGGER.error(ex.getMessage());
        LOGGER.info("", ex);
        return 0;
    }
}
