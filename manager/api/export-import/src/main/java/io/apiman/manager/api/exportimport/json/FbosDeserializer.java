package io.apiman.manager.api.exportimport.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.io.FileBackedOutputStream;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class FbosDeserializer extends StdDeserializer<FileBackedOutputStream> {
    protected FbosDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FileBackedOutputStream deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        FileBackedOutputStream fbos = new FileBackedOutputStream(256 * 1024);
        fbos.write(p.getBinaryValue());
        return fbos;
    }
}
