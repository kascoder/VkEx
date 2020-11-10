package org.kascoder.vkex.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class VkCredentials {
    private final int clientId;
    private final String clientSecret;

    public VkCredentials(@JsonProperty("clientId") int clientId,
                         @JsonProperty("clientSecret") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
