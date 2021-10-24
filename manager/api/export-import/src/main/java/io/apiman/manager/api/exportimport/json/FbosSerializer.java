package io.apiman.manager.api.exportimport.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.io.FileBackedOutputStream;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class FbosSerializer extends StdSerializer<FileBackedOutputStream> {

    protected FbosSerializer(Class<FileBackedOutputStream> t) {
        super(t);
    }

    @Override
    public void serialize(FileBackedOutputStream fbos, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeBinary(fbos.asByteSource().openStream(), Math.toIntExact(fbos.asByteSource().size()));
    }
}
