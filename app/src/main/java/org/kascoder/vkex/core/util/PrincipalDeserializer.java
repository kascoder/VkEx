package org.kascoder.vkex.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.kascoder.vkex.core.model.Principal;

import java.io.IOException;

public class PrincipalDeserializer extends StdDeserializer<Principal> {
    private final String encryptionKey;

    public PrincipalDeserializer(String encryptionKey) {
        this(null, encryptionKey);
    }

    protected PrincipalDeserializer(Class<Principal> t, String encryptionKey) {
        super(t);
        this.encryptionKey = encryptionKey;
    }

    @Override
    public Principal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        int id = node.get("id").intValue();
        String encryptedAccessToken = node.get("accessToken").asText();
        var accessToken = CryptUtils.decrypt(encryptedAccessToken, encryptionKey);

        return new Principal(id, accessToken);
    }
}
