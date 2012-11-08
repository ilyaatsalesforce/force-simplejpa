/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * An {@link ObjectMapper} module which consists of a matching {@link JsonSerializer} and {@link JsonDeserializer} for
 * representing a {@link Map} as JSON nested inside of a string.
 * <p/>
 * This is used to transfer the {@link Map} through the Force.com REST interface for storage in a String field.
 * <p/>
 * During serialization, the normal json serialization of a map is created but then the serializer writes it as a JSON
 * string instead of as a JSON object.
 * <p/>
 * The normal map serialization looks like this:
 * <pre>
 * {"key2":"value2","key1":"value1"}
 * </pre>
 * This string-based map serialization looks like this:
 * <pre>
 * "{\\\"key2\\\":\\\"value2\",\\\"key1\\\":\\\"value1\\\"}"
 * </pre>
 *
 * @author davidbuccola
 */
@SuppressWarnings("rawtypes")
final class MapAsStringModule extends SimpleModule {
    private static final ObjectMapper plainMapper = new ObjectMapper();

    MapAsStringModule() {
        super(MapAsStringModule.class.getSimpleName(), new Version(1, 0, 0, null));

        addDeserializer(Map.class, new MapAsStringDeserializer<Map>(Map.class));
        addDeserializer(HashMap.class, new MapAsStringDeserializer<HashMap>(HashMap.class));
        addDeserializer(LinkedHashMap.class, new MapAsStringDeserializer<LinkedHashMap>(LinkedHashMap.class));
        addSerializer(Map.class, new MapAsStringSerializer());
    }

    private static class MapAsStringDeserializer<T extends Map> extends JsonDeserializer<T> {
        private Class<T> mapClass;

        MapAsStringDeserializer(Class<T> mapClass) {
            this.mapClass = mapClass;
        }

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                return plainMapper.readValue(jp.getText(), mapClass);
            } else if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
                return null;
            } else {
                throw new JsonMappingException("Token type for map-as-string deserialization was not a String");
            }
        }
    }

    private static class MapAsStringSerializer extends JsonSerializer<Map> {
        @Override
        public void serialize(Map value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            if (value != null) {
                String plainSerialization = plainMapper.writeValueAsString(value);
                jgen.writeString(plainSerialization);
            } else
                jgen.writeNull();
        }
    }
}



