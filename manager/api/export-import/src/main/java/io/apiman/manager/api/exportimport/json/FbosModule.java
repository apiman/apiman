package io.apiman.manager.api.exportimport.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.io.FileBackedOutputStream;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class FbosModule extends SimpleModule {
    public FbosModule() {
        super();
        super.addSerializer(new FbosSerializer(FileBackedOutputStream.class));
        super.addDeserializer(FileBackedOutputStream.class, new FbosDeserializer(FileBackedOutputStream.class));
    }
}
