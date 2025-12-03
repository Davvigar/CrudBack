package org.david.crm.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil { // utilidad para convertir objetos a JSON y viceversa
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
        // Registrar módulo de Hibernate para manejar proxies lazy
        Hibernate6Module hibernateModule = new Hibernate6Module();
        hibernateModule.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        hibernateModule.configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        objectMapper.registerModule(hibernateModule);
        // Configurar para manejar proxies lazy de Hibernate
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // Ignorar propiedades de Hibernate proxy
        objectMapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
    
    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }
}
