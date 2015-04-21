//package io.apiman.gateway.engine.policies.connectors;
//
//import io.apiman.gateway.engine.IServiceConnection;
//import io.apiman.gateway.engine.IServiceConnectionResponse;
//import io.apiman.gateway.engine.async.IAsyncHandler;
//import io.apiman.gateway.engine.beans.ServiceResponse;
//import io.apiman.gateway.engine.components.IDataStoreComponent;
//import io.apiman.gateway.engine.impl.CachedResponse;
//import io.apiman.gateway.engine.io.IApimanBuffer;
//import io.apiman.gateway.engine.policies.CachingPolicy;
//import io.apiman.gateway.engine.policy.IPolicyContext;
//
///**
// * This {@link IServiceConnection} implementation aims to simulate a back-end
// * connection but what will do is to retrieve an existing
// * {@link CachedResponse} from the {@link IDataStoreComponent} and generate
// * the {@link IServiceConnectionResponse}
// *
// * @author rubenrm1@gmail.com
// *
// */
//public class CachedResponseConnection implements IServiceConnection, IServiceConnectionResponse {
//
//    private CachedResponse cachedResponse;
//    private IAsyncHandler<IApimanBuffer> bodyHandler;
//    private IAsyncHandler<Void> endHandler;
//    private boolean connected;
//    private ServiceResponse response;
//    private IPolicyContext context;
//
//    public CachedResponseConnection(IPolicyContext context) {
//        this.context = context;
//        connected = true;
//        cachedResponse = context.getAttribute(CachingPolicy.CACHED_RESPONSE, null);
//    }
//
//    /**
//     * @see io.apiman.gateway.engine.io.IReadStream#getHead()
//     */
//    @Override
//    public ServiceResponse getHead() {
//        response = new ServiceResponse();
//        response.setHeaders(cachedResponse.getHeaders());
//        response.setCode(cachedResponse.getCode());
//        response.setMessage(cachedResponse.getMessage());
//
//        return response;
//    }
//
//    /**
//     * @see io.apiman.gateway.engine.io.IReadStream#bodyHandler(io.apiman.gateway.engine.async.IAsyncHandler)
//     */
//    @Override
//    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
//        this.bodyHandler = bodyHandler;
//    }
//
//    /**
//     * @see io.apiman.gateway.engine.io.IReadStream#endHandler(io.apiman.gateway.engine.async.IAsyncHandler)
//     */
//    @Override
//    public void endHandler(IAsyncHandler<Void> endHandler) {
//        this.endHandler = endHandler;
//    }
//
//    /**
//     * @see io.apiman.gateway.engine.io.IStream#isFinished()
//     */
//    @Override
//    public boolean isFinished() {
//        return !connected;
//    }
//
//    /**
//     * @see io.apiman.gateway.engine.io.IAbortable#abort()
//     */
//    @Override
//    public void abort() {
//        connected = false;
//    }
//
//    @Override
//    public void transmit() {
//        try {
//            //InputStream is = cachedResponse.getInputStream();
//            //ByteBuffer buffer = new ByteBuffer(2048);
//            //IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
//            //IApimanBuffer buffer = bufferFactory.createBuffer(2048);
//            //            int numBytes = buffer.readFrom(is);
//            //            while (numBytes != -1) {
//            //                bodyHandler.handle(buffer);
//            //                numBytes = buffer.readFrom(is);
//            //            }
//            
//            connected = false;
//            endHandler.handle((Void) null);
//        } catch (Throwable e) {
//            // At this point we're sort of screwed, because we've already sent the response to
//            // the originating client - and we're in the process of sending the body data.  So
//            // I guess the only thing to do is abort() the connection and cross our fingers.
//            if (connected) {
//                abort();
//            }
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void write(IApimanBuffer chunk) {
//    }
//
//    @Override
//    public void end() {
//    }
//
//}
