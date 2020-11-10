package org.kascoder.vkex.core.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.kascoder.vkex.core.model.Update;
import org.kascoder.vkex.core.util.UpdateBackwardIncompatibleException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class DefaultUpdateService implements UpdateService {
    @Override
    public void initiateUpdate(@NonNull Update update, boolean launch, @NonNull Consumer<Exception> errorHandler) throws UpdateBackwardIncompatibleException {
        if (!update.isBackwardCompatible()) {
            throw new UpdateBackwardIncompatibleException();
        }

        try {
            File appJarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            LOGGER.info("App jar file path: {}", appJarFile.getAbsolutePath());
            LOGGER.info("App updater url: {}", update.getUpdaterDownloadUrl());
            URL appUpdaterJarDownloadUrl;
            try {
                appUpdaterJarDownloadUrl = new URL(update.getUpdaterDownloadUrl());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage());
            }
            String appUpdaterPath = appJarFile.getParent() + File.separator + "vkex-updater.jar";
            File appUpdaterTargetJarFile = new File(appUpdaterPath + ".download");

            LOGGER.info("App updater path: {}", appUpdaterPath);
            LOGGER.info("appUpdaterTargetJarFile {}", appUpdaterTargetJarFile.getPath());

            if (appUpdaterTargetJarFile.exists() && !appUpdaterTargetJarFile.delete()) {
                throw new RuntimeException("Cannot delete " + appUpdaterTargetJarFile.getAbsolutePath());
            }

            FileUtils.copyURLToFile(appUpdaterJarDownloadUrl, appUpdaterTargetJarFile);

            File appUpdaterJarFile = new File(appUpdaterPath);
            if (appUpdaterJarFile.exists()) {
                if (!appUpdaterJarFile.delete()) {
                    throw new RuntimeException("Cannot delete obsolete vkex updater jar file");
                }
            }

            if (!appUpdaterTargetJarFile.renameTo(new File(appUpdaterPath))) {
                throw new RuntimeException("Cannot rename fresh vkex updater jar file");
            }

            try {
                var javaPath = String.join(File.separator, appJarFile.getParent(), "jre", "bin", "java");
                var args = List.of(javaPath, "-jar", appUpdaterPath, update.getAppDownloadUrl(), Boolean.toString(launch), Boolean.toString(false));
                LOGGER.debug("Run command {} ...", String.join(" ", args));
                new ProcessBuilder(args).start();
                System.exit(0);
            } catch (IOException e) {
                LOGGER.error("Update error: ", e);
                errorHandler.accept(e);
            }
        } catch (Exception e) {
            LOGGER.error("Download error: ", e);
            errorHandler.accept(e);
        }
    }
}
