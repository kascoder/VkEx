package org.kascoder.vkex.updater;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.List;

public class VkExUpdater {
    public static void main(String[] args) {
        try {
            var config = parseArgs(args);
            if (config.isDebug()) {
                File logFile = new File("vkex-updater.log");
                PrintStream logOut = new PrintStream(
                        new BufferedOutputStream(new FileOutputStream(logFile)), true);
                System.setOut(logOut);
                System.setErr(logOut);
            }

            update(config);
        } catch (Exception e) {
            System.out.println("[ERROR]: " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void update(Config config) throws Exception {
        System.out.println("[INFO] launch: " + config.isLaunch());
        System.out.println("[INFO] appDownloadUrl: " + config.getAppDownloadUrl());

        File appUpdaterJarFile = new File(VkExUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        URL appJarDownloadUrl = new URL(config.getAppDownloadUrl());
        String appPath = appUpdaterJarFile.getParent() + File.separator + "vkex.jar";
        File appTargetJarFile = new File(appPath + ".download");

        System.out.println("[INFO] appPath: " + appPath);
        System.out.println("[INFO] appTargetJarFilePath: " + appTargetJarFile.getPath());
        System.out.println("[INFO] appUpdaterJarFilePath: " + appUpdaterJarFile.getPath());

        if (appTargetJarFile.exists() && !appTargetJarFile.delete()) {
            throw new RuntimeException("Cannot delete " + appTargetJarFile.getAbsolutePath());
        }

        final UpdateFrame updateFrame = new UpdateFrame(appJarDownloadUrl, appTargetJarFile, (success, e) -> {
            if (success) {
                File appJarFile = new File(appPath);
                if (appJarFile.exists()) {
                    if (!appJarFile.delete()) {
                        throw new RuntimeException("Cannot delete obsolete vkex jar file");
                    }
                }

                if (!appTargetJarFile.renameTo(new File(appPath))) {
                    throw new RuntimeException("Cannot rename fresh vkex jar file");
                }

                if (config.isLaunch()) {
                    try {
                        var args = List.of(appUpdaterJarFile.getParent() + File.separator + "vkex");
                        System.out.printf("Run command %s ...", String.join(" ", args));
                        new ProcessBuilder(args).start();
                        System.exit(0);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1.getMessage());
                    }
                }
            } else {
                throw new RuntimeException(e.getMessage());
            }
        });

        updateFrame.setVisible(true);
        updateFrame.doDownload();
    }

    private static Config parseArgs(String[] args) throws Exception {
        if (args.length < 3) {
            throw new Exception("Usage: java -jar vkex-updater.jar app_download_url launch debug");
        }

        var appDownloadUrl = args[0];
        var launch = Boolean.parseBoolean(args[1]);
        var debug = Boolean.parseBoolean(args[2]);

        return new Config(debug, launch, appDownloadUrl);
    }
}
