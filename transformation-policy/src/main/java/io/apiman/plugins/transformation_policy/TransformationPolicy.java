package io.apiman.plugins.transformation_policy;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.transformation_policy.beans.DataFormat;
import io.apiman.plugins.transformation_policy.beans.TransformationConfigBean;
import io.apiman.plugins.transformation_policy.transformer.DataTransformer;
import io.apiman.plugins.transformation_policy.transformer.DataTransformerFactory;

/**
 * Policy that transforms the data from one format to another.
 *
 * @author Alexandre Kieling <alex.kieling@gmail.com>
 */
public class TransformationPolicy extends AbstractMappedPolicy<TransformationConfigBean> implements IDataPolicy {

    private static final String CLIENT_FORMAT = "clientFormat"; //$NON-NLS-1$
    private static final String SERVER_FORMAT = "serverFormat"; //$NON-NLS-1$
    private static final String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
    private static final String CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$

    @Override
    protected Class<TransformationConfigBean> getConfigurationClass() {
        return TransformationConfigBean.class;
    }

    @Override
    protected void doApply(ServiceRequest request, IPolicyContext context, TransformationConfigBean config,
            IPolicyChain<ServiceRequest> chain) {
        DataFormat clientFormat = config.getClientFormat();
        DataFormat serverFormat = config.getServerFormat();

        if (isValidTransformation(clientFormat, serverFormat)) {
            context.setAttribute(CLIENT_FORMAT, clientFormat);
            context.setAttribute(SERVER_FORMAT, serverFormat);

            request.getHeaders().put(CONTENT_TYPE, serverFormat.getContentType());
            request.getHeaders().remove(CONTENT_LENGTH);
        }

        super.doApply(request, context, config, chain);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getRequestDataHandler(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ServiceRequest> getRequestDataHandler(final ServiceRequest request,
            IPolicyContext context, Object policyConfiguration) {
        final DataFormat clientFormat = (DataFormat) context.getAttribute(CLIENT_FORMAT, null);
        final DataFormat serverFormat = (DataFormat) context.getAttribute(SERVER_FORMAT, null);

        if (isValidTransformation(clientFormat, serverFormat)) {
            final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
            final int contentLength = request.getHeaders().containsKey(CONTENT_LENGTH)
                    ? Integer.parseInt(request.getHeaders().get(CONTENT_LENGTH))
                    : 0;

            return new AbstractStream<ServiceRequest>() {

                private IApimanBuffer readBuffer = bufferFactory.createBuffer(contentLength);

                @Override
                public ServiceRequest getHead() {
                    return request;
                }

                @Override
                protected void handleHead(ServiceRequest head) {
                }

                @Override
                public void write(IApimanBuffer chunk) {
                    readBuffer.append(chunk.getBytes());
                }

                @Override
                public void end() {
                    if (readBuffer.length() > 0) {
                        DataTransformer dataTransformer = DataTransformerFactory.getDataTransformer(clientFormat, serverFormat);
                        IApimanBuffer writeBuffer = bufferFactory.createBuffer(readBuffer.length());

                        String data = dataTransformer.transform(new String(readBuffer.getBytes()));
                        writeBuffer.append(data);

                        super.write(writeBuffer);
                    }
                    super.end();
                }
            };
        }

        return null;
    }

    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context, TransformationConfigBean config,
            IPolicyChain<ServiceResponse> chain) {
        final DataFormat clientFormat = (DataFormat) context.getAttribute(CLIENT_FORMAT, null);
        final DataFormat serverFormat = (DataFormat) context.getAttribute(SERVER_FORMAT, null);

        if (isValidTransformation(clientFormat, serverFormat)) {
            response.getHeaders().put(CONTENT_TYPE, clientFormat.getContentType());
            response.getHeaders().remove(CONTENT_LENGTH);
        }

        super.doApply(response, context, config, chain);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getResponseDataHandler(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object)
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(final ServiceResponse response,
            IPolicyContext context, Object policyConfiguration) {
        final DataFormat clientFormat = (DataFormat) context.getAttribute(CLIENT_FORMAT, null);
        final DataFormat serverFormat = (DataFormat) context.getAttribute(SERVER_FORMAT, null);

        if (isValidTransformation(clientFormat, serverFormat)) {
            final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
            final int contentLength = response.getHeaders().containsKey(CONTENT_LENGTH)
                    ? Integer.parseInt(response.getHeaders().get(CONTENT_LENGTH))
                    : 0;

            return new AbstractStream<ServiceResponse>() {

                private IApimanBuffer readBuffer = bufferFactory.createBuffer(contentLength);

                @Override
                public ServiceResponse getHead() {
                    return response;
                }

                @Override
                protected void handleHead(ServiceResponse head) {
                }

                @Override
                public void write(IApimanBuffer chunk) {
                    byte[] bytes = chunk.getBytes();
                    readBuffer.append(bytes);
                }

                @Override
                public void end() {
                    if (readBuffer.length() > 0) {
                        DataTransformer dataTransformer = DataTransformerFactory.getDataTransformer(serverFormat, clientFormat);
                        IApimanBuffer writeBuffer = bufferFactory.createBuffer(readBuffer.length());

                        String data = dataTransformer.transform(new String(readBuffer.getBytes()));
                        writeBuffer.append(data);

                        super.write(writeBuffer);
                    }
                    super.end();
                }
            };
        }

        return null;
    }

    private boolean isValidTransformation(DataFormat clientFormat, DataFormat serverFormat) {
        return clientFormat != null && serverFormat != null && !clientFormat.equals(serverFormat);
    }

}
