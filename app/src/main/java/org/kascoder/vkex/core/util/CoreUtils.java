package org.kascoder.vkex.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreUtils {
    private static final Pattern expressionPattern = Pattern.compile("\\$\\{(\\w+\\.\\w+)}");

    public static String resolveSystemProperties(String value) {
        var result = value;
        var matcher = expressionPattern.matcher(result);
        while (matcher.find()) {
            var expr = matcher.group();
            var prop = matcher.group(1);
            result = result.replace(expr, System.getProperty(prop));
        }

        return result;
    }

    public static void sleep(long timeoutMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeoutMs);
        } catch (Exception ignored) {

        }
    }
}
