package org.kascoder.vkex.cli.util;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.kascoder.vkex.core.configuration.ApplicationModule;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class DependencyFactory implements IFactory {
    private final Injector injector = Guice.createInjector(new CliApplicationModule());

    @Override
    public <K> K create(Class<K> type) throws Exception {
        try {
            return injector.getInstance(type);
        } catch (ConfigurationException ex) {
            LOGGER.error("Error during type creation", ex);
            return CommandLine.defaultFactory().create(type); // fallback if missing
        }
    }

    static class CliApplicationModule extends ApplicationModule {
        @Override
        protected void configure() {
            super.configure();
            bind(Set.class).to(HashSet.class);
            bind(CommandLine.IExecutionStrategy.class).to(StartupExecutionStrategy.class).in(Singleton.class);
        }
    }
}
