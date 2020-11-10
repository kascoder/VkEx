package org.kascoder.vkex.updater;

import lombok.Value;

@Value
public class Config {
    boolean debug;
    boolean launch;
    String appDownloadUrl;
}
