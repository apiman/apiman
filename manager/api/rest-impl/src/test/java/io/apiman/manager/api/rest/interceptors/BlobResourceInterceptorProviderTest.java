package io.apiman.manager.api.rest.interceptors;

import io.apiman.common.util.Holder;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.rest.interceptors.BlobResourceInterceptorProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class BlobResourceInterceptorProviderTest {

    @Test
    public void simple_object() throws IOException {
        ApiBean ab = new ApiBean();
        ab.setImage("blobaroni/foo-bar-baz_jpeg");

        BlobResourceInterceptorProvider interceptorProvider = new BlobResourceInterceptorProvider();
        interceptorProvider.aroundWriteTo(new DummyWriterContext(ab));

        assertThat(ab.getImage()).isEqualTo("blobs/blobaroni/foo-bar-baz_jpeg");
    }

    @Test
    public void simple_nested_structures_with_collection_traversal() throws IOException {
        ApiBean ab = new ApiBean();
        ab.setImage("blobaroni/foo-bar-baz_jpeg");

        ApiVersionBean avb = new ApiVersionBean();
        avb.setApi(ab);

        SearchResultsBean<ApiVersionBean> searchResults = new SearchResultsBean<ApiVersionBean>()
                                                                  .setTotalSize(1)
                                                                  .setBeans(List.of(avb));

        BlobResourceInterceptorProvider interceptorProvider = new BlobResourceInterceptorProvider();
        interceptorProvider.aroundWriteTo(new DummyWriterContext(searchResults));

        assertThat(avb.getApi().getImage()).isEqualTo("blobs/blobaroni/foo-bar-baz_jpeg");
    }

    @Test
    public void nested_collection_traversal() throws IOException {
        ApiBean ab = new ApiBean();
        ab.setImage("blobaroni/foo-bar-baz_jpeg");

        List<ApiBean> listInner = List.of(ab);
        List<List<ApiBean>> listOuter = List.of(listInner);

        BlobResourceInterceptorProvider interceptorProvider = new BlobResourceInterceptorProvider();
        interceptorProvider.aroundWriteTo(new DummyWriterContext(listOuter));

        assertThat(ab.getImage()).isEqualTo("blobs/blobaroni/foo-bar-baz_jpeg");
    }

    @Test
    public void nested_collection_traversal_with_object_intermediaries()  throws IOException {
        ApiBean ab = new ApiBean();
        ab.setImage("blobaroni/foo-bar-baz_jpeg");

        List<ApiBean> listInner = List.of(ab);

        Holder<List<ApiBean>> intermediary = new Holder<>();
        intermediary.setValue(listInner);

        List<Holder<List<ApiBean>>> listOuter = List.of(intermediary);

        BlobResourceInterceptorProvider interceptorProvider = new BlobResourceInterceptorProvider();
        interceptorProvider.aroundWriteTo(new DummyWriterContext(listOuter));

        assertThat(ab.getImage()).isEqualTo("blobs/blobaroni/foo-bar-baz_jpeg");
    }

    private static class DummyWriterContext implements WriterInterceptorContext {
        private final Object anyObject;

        public DummyWriterContext(Object anyObject) {
            this.anyObject = anyObject;
        }

            @Override
            public void proceed() throws IOException, WebApplicationException {

            }

            @Override
            public Object getEntity() {
                return anyObject;
            }

            @Override
            public void setEntity(Object entity) {

            }

            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public void setOutputStream(OutputStream os) {

            }

            @Override
            public MultivaluedMap<String, Object> getHeaders() {
                return null;
            }

            @Override
            public Object getProperty(String name) {
                return null;
            }

            @Override
            public Collection<String> getPropertyNames() {
                return null;
            }

            @Override
            public void setProperty(String name, Object object) {

            }

            @Override
            public void removeProperty(String name) {

            }

            @Override
            public Annotation[] getAnnotations() {
                return new Annotation[0];
            }

            @Override
            public void setAnnotations(Annotation[] annotations) {

            }

            @Override
            public Class<?> getType() {
                return null;
            }

            @Override
            public void setType(Class<?> type) {

            }

            @Override
            public Type getGenericType() {
                return null;
            }

            @Override
            public void setGenericType(Type genericType) {

            }

            @Override
            public MediaType getMediaType() {
                return null;
            }

            @Override
            public void setMediaType(MediaType mediaType) {

            }
    }
}