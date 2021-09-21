package io.apiman.manager.api.providers;

import io.apiman.common.util.JsonUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Use our own configuration of ObjectMapper as the default one can't serialise Java 8 datastructures
 * (e.g. OffsetDateTime).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
@Consumes({"application/json", "application/*+json", "text/json"})
@Produces({"application/json", "application/*+json", "text/json"})
public class JacksonObjectMapperProvider extends JacksonJsonProvider {

    public JacksonObjectMapperProvider() {}

    @Override
    public ObjectMapper locateMapper(Class<?> type, MediaType mediaType) {
        return JsonUtil.getObjectMapper();
    }
}

