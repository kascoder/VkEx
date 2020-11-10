package org.kascoder.vkex.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.kascoder.vkex.core.model.Principal;

import java.io.IOException;

public class PrincipalSerializer extends StdSerializer<Principal> {
    private final String encryptionKey;

    public PrincipalSerializer(String encryptionKey) {
        this(null, encryptionKey);
    }

    protected PrincipalSerializer(Class<Principal> t, String encryptionKey) {
        super(t);
        this.encryptionKey = encryptionKey;
    }

    @Override
    public void serialize(Principal principal, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeNumberField("id", principal.getId());
        var encryptedAccessToken = CryptUtils.encrypt(principal.getAccessToken(), encryptionKey);
        generator.writeStringField("accessToken", encryptedAccessToken);
        generator.writeEndObject();
    }
}
