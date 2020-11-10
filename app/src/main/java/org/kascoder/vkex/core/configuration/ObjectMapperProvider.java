package org.kascoder.vkex.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kascoder.vkex.core.model.Principal;
import org.kascoder.vkex.core.util.PrincipalDeserializer;
import org.kascoder.vkex.core.util.PrincipalSerializer;
import org.kascoder.vkex.core.util.EncryptionKey;

public class ObjectMapperProvider implements Provider<ObjectMapper> {

    private final String encryptionKey;

    @Inject
    public ObjectMapperProvider(@EncryptionKey String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Override
    public ObjectMapper get() {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleModule module = new SimpleModule();
        module.addSerializer(Principal.class, new PrincipalSerializer(encryptionKey));
        module.addDeserializer(Principal.class, new PrincipalDeserializer(encryptionKey));
        mapper.registerModule(module);

        return mapper;
    }
}
