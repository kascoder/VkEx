package org.kascoder.vkex;

import org.kascoder.vkex.cli.CliApplication;
import org.kascoder.vkex.desktop.DesktopApplication;

import java.awt.*;

public class VkExLauncher {
    public static void main(String[] args) {
        var executionType = System.getProperty("execution.type");
        if ("gui".equals(executionType)) {
            if (Desktop.isDesktopSupported()) {
                DesktopApplication.main(args);
            } else {
                System.out.println("Desktop isn't supported");
            }
        } else if ("console".equals(executionType)) {
            CliApplication.main(args);
        }
    }
}
