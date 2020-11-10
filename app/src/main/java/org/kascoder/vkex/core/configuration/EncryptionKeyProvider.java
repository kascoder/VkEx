package org.kascoder.vkex.core.configuration;

import com.google.inject.Provider;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import oshi.SystemInfo;

@Slf4j
@NoArgsConstructor
public class EncryptionKeyProvider implements Provider<String> {
    @Override
    public String get() {
        SystemInfo systemInfo = new SystemInfo();
        var hardware = systemInfo.getHardware();
        var computerSystem = hardware.getComputerSystem();
        var key = computerSystem.getSerialNumber();
        if (StringUtils.isBlank(key)) {
            var message = "Encryption key is empty";
            LOGGER.error(message);
            throw new RuntimeException(message);
        }

        return key;
    }
}
