package org.kascoder.vkex.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import org.kascoder.vkex.core.ApplicationContext;
import org.kascoder.vkex.core.service.*;
import org.kascoder.vkex.core.service.export.ConversationExportService;
import org.kascoder.vkex.core.service.export.DefaultConversationExportService;
import org.kascoder.vkex.core.service.io.ContentService;
import org.kascoder.vkex.core.service.io.JsonContentService;
import org.kascoder.vkex.core.service.preferences.UserPreferencesService;
import org.kascoder.vkex.core.service.preferences.UserPreferencesServiceImpl;
import org.kascoder.vkex.core.service.settings.ConfigurationService;
import org.kascoder.vkex.core.service.settings.DefaultConfigurationService;
import org.kascoder.vkex.core.util.ApplicationPath;
import org.kascoder.vkex.core.util.ApplicationVersion;
import org.kascoder.vkex.core.util.EncryptionKey;

import java.util.Properties;

public abstract class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();
        Names.bindProperties(binder(), loadCoreProperties());

        bind(ContentService.class).to(JsonContentService.class).in(Singleton.class);
        bind(UpdateService.class).to(DefaultUpdateService.class).in(Singleton.class);
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
        bind(VkClientMiddleware.class).to(DefaultVkClientMiddleware.class).in(Singleton.class);
        bind(ConfigurationService.class).to(DefaultConfigurationService.class).in(Singleton.class);
        bind(UserPreferencesService.class).to(UserPreferencesServiceImpl.class).in(Singleton.class);
        bind(RepositoryService.class).toProvider(RepositoryServiceProvider.class).in(Singleton.class);
        bind(ApplicationContext.class).toProvider(ApplicationContextProvider.class).in(Singleton.class);
        bind(ConversationExportService.class).to(DefaultConversationExportService.class).in(Singleton.class);

        bind(String.class).annotatedWith(EncryptionKey.class).toProvider(EncryptionKeyProvider.class).in(Singleton.class);
        bind(String.class).annotatedWith(ApplicationPath.class).toProvider(ApplicationPathProvider.class).in(Singleton.class);
        bind(Version.class).annotatedWith(ApplicationVersion.class).toProvider(ApplicationVersionProvider.class).in(Singleton.class);
    }

    @SneakyThrows
    private Properties loadCoreProperties() {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
        return properties;
    }
}
