package org.david.crm.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil { // utilidad para convertir objetos a JSON y viceversa
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
    
    public static String toJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }
}

