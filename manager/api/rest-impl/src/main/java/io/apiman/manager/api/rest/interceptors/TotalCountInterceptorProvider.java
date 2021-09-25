package io.apiman.manager.api.rest.interceptors;

import io.apiman.manager.api.beans.search.ITotalSize;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Provider
@ServerInterceptor
public class TotalCountInterceptorProvider implements WriterInterceptor {

    public TotalCountInterceptorProvider() {}

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (context.getEntity() instanceof ITotalSize) {
            ITotalSize sizedResults = (ITotalSize) context.getEntity();
            context.getHeaders().putIfAbsent("X-Total-Count", List.of(sizedResults.getTotalSize()));
            context.getHeaders().putIfAbsent("Total-Count", List.of(sizedResults.getTotalSize()));
        }
        context.proceed();
    }
}
