package org.kascoder.vkex.desktop.configuration;

import com.google.common.eventbus.EventBus;
import org.kascoder.vkex.core.configuration.ApplicationModule;
import javafx.fxml.FXMLLoader;

public class GuiApplicationModule extends ApplicationModule {
    @Override
    protected void configure() {
        super.configure();
        bind(FXMLLoader.class).toProvider(FXMLLoaderProvider.class);
        bind(EventBus.class).toInstance(new EventBus());
    }
}
