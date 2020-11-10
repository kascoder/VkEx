package org.kascoder.vkex.core.model;

import lombok.Value;

@Value
public class Update {
    String appDownloadUrl;
    String updaterDownloadUrl;
    boolean backwardCompatible;
}
