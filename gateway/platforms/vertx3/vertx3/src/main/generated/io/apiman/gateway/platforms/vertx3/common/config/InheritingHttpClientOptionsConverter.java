package io.apiman.gateway.platforms.vertx3.common.config;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpClientOptions} original class using Vert.x codegen.
 */
public class InheritingHttpClientOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, InheritingHttpClientOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "activityLogDataFormat":
          if (member.getValue() instanceof String) {
            obj.setActivityLogDataFormat(io.netty.handler.logging.ByteBufFormat.valueOf((String)member.getValue()));
          }
          break;
        case "alpnVersions":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.core.http.HttpVersion> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add(io.vertx.core.http.HttpVersion.valueOf((String)item));
            });
            obj.setAlpnVersions(list);
          }
          break;
        case "connectTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "crlPaths":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addCrlPath((String)item);
            });
          }
          break;
        case "crlValues":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addCrlValue(io.vertx.core.buffer.Buffer.buffer(BASE64_DECODER.decode((String)item)));
            });
          }
          break;
        case "decoderInitialBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setDecoderInitialBufferSize(((Number)member.getValue()).intValue());
          }
          break;
        case "defaultHost":
          if (member.getValue() instanceof String) {
            obj.setDefaultHost((String)member.getValue());
          }
          break;
        case "defaultPort":
          if (member.getValue() instanceof Number) {
            obj.setDefaultPort(((Number)member.getValue()).intValue());
          }
          break;
        case "enabledCipherSuites":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addEnabledCipherSuite((String)item);
            });
          }
          break;
        case "enabledSecureTransportProtocols":
          if (member.getValue() instanceof JsonArray) {
            java.util.LinkedHashSet<java.lang.String> list =  new java.util.LinkedHashSet<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setEnabledSecureTransportProtocols(list);
          }
          break;
        case "forceSni":
          if (member.getValue() instanceof Boolean) {
            obj.setForceSni((Boolean)member.getValue());
          }
          break;
        case "http2ClearTextUpgrade":
          if (member.getValue() instanceof Boolean) {
            obj.setHttp2ClearTextUpgrade((Boolean)member.getValue());
          }
          break;
        case "http2ConnectionWindowSize":
          if (member.getValue() instanceof Number) {
            obj.setHttp2ConnectionWindowSize(((Number)member.getValue()).intValue());
          }
          break;
        case "http2KeepAliveTimeout":
          if (member.getValue() instanceof Number) {
            obj.setHttp2KeepAliveTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "http2MaxPoolSize":
          if (member.getValue() instanceof Number) {
            obj.setHttp2MaxPoolSize(((Number)member.getValue()).intValue());
          }
          break;
        case "http2MultiplexingLimit":
          if (member.getValue() instanceof Number) {
            obj.setHttp2MultiplexingLimit(((Number)member.getValue()).intValue());
          }
          break;
        case "idleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "idleTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setIdleTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "initialSettings":
          if (member.getValue() instanceof JsonObject) {
            obj.setInitialSettings(new io.vertx.core.http.Http2Settings((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "jdkSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setJdkSslEngineOptions(new io.vertx.core.net.JdkSSLEngineOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "keepAlive":
          if (member.getValue() instanceof Boolean) {
            obj.setKeepAlive((Boolean)member.getValue());
          }
          break;
        case "keepAliveTimeout":
          if (member.getValue() instanceof Number) {
            obj.setKeepAliveTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "keyStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setKeyStoreOptions(new io.vertx.core.net.JksOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "localAddress":
          if (member.getValue() instanceof String) {
            obj.setLocalAddress((String)member.getValue());
          }
          break;
        case "logActivity":
          if (member.getValue() instanceof Boolean) {
            obj.setLogActivity((Boolean)member.getValue());
          }
          break;
        case "maxChunkSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxChunkSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxHeaderSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxHeaderSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxInitialLineLength":
          if (member.getValue() instanceof Number) {
            obj.setMaxInitialLineLength(((Number)member.getValue()).intValue());
          }
          break;
        case "maxPoolSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxPoolSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxRedirects":
          if (member.getValue() instanceof Number) {
            obj.setMaxRedirects(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWaitQueueSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWaitQueueSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWebSocketFrameSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWebSocketFrameSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWebSocketMessageSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWebSocketMessageSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWebSockets":
          if (member.getValue() instanceof Number) {
            obj.setMaxWebSockets(((Number)member.getValue()).intValue());
          }
          break;
        case "metricsName":
          if (member.getValue() instanceof String) {
            obj.setMetricsName((String)member.getValue());
          }
          break;
        case "name":
          if (member.getValue() instanceof String) {
            obj.setName((String)member.getValue());
          }
          break;
        case "nonProxyHosts":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<java.lang.String> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setNonProxyHosts(list);
          }
          break;
        case "openSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setOpenSslEngineOptions(new io.vertx.core.net.OpenSSLEngineOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pemKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemKeyCertOptions(new io.vertx.core.net.PemKeyCertOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pemTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemTrustOptions(new io.vertx.core.net.PemTrustOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pfxKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxKeyCertOptions(new io.vertx.core.net.PfxOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pfxTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxTrustOptions(new io.vertx.core.net.PfxOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pipelining":
          if (member.getValue() instanceof Boolean) {
            obj.setPipelining((Boolean)member.getValue());
          }
          break;
        case "pipeliningLimit":
          if (member.getValue() instanceof Number) {
            obj.setPipeliningLimit(((Number)member.getValue()).intValue());
          }
          break;
        case "poolCleanerPeriod":
          if (member.getValue() instanceof Number) {
            obj.setPoolCleanerPeriod(((Number)member.getValue()).intValue());
          }
          break;
        case "poolEventLoopSize":
          if (member.getValue() instanceof Number) {
            obj.setPoolEventLoopSize(((Number)member.getValue()).intValue());
          }
          break;
        case "protocolVersion":
          if (member.getValue() instanceof String) {
            obj.setProtocolVersion(io.vertx.core.http.HttpVersion.valueOf((String)member.getValue()));
          }
          break;
        case "proxyOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setProxyOptions(new io.vertx.core.net.ProxyOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "readIdleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setReadIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "receiveBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setReceiveBufferSize(((Number)member.getValue()).intValue());
          }
          break;
        case "reuseAddress":
          if (member.getValue() instanceof Boolean) {
            obj.setReuseAddress((Boolean)member.getValue());
          }
          break;
        case "reusePort":
          if (member.getValue() instanceof Boolean) {
            obj.setReusePort((Boolean)member.getValue());
          }
          break;
        case "sendBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setSendBufferSize(((Number)member.getValue()).intValue());
          }
          break;
        case "sendUnmaskedFrames":
          if (member.getValue() instanceof Boolean) {
            obj.setSendUnmaskedFrames((Boolean)member.getValue());
          }
          break;
        case "shared":
          if (member.getValue() instanceof Boolean) {
            obj.setShared((Boolean)member.getValue());
          }
          break;
        case "soLinger":
          if (member.getValue() instanceof Number) {
            obj.setSoLinger(((Number)member.getValue()).intValue());
          }
          break;
        case "ssl":
          if (member.getValue() instanceof Boolean) {
            obj.setSsl((Boolean)member.getValue());
          }
          break;
        case "sslHandshakeTimeout":
          if (member.getValue() instanceof Number) {
            obj.setSslHandshakeTimeout(((Number)member.getValue()).longValue());
          }
          break;
        case "sslHandshakeTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setSslHandshakeTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "tcpCork":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpCork((Boolean)member.getValue());
          }
          break;
        case "tcpFastOpen":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpFastOpen((Boolean)member.getValue());
          }
          break;
        case "tcpKeepAlive":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpKeepAlive((Boolean)member.getValue());
          }
          break;
        case "tcpNoDelay":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpNoDelay((Boolean)member.getValue());
          }
          break;
        case "tcpQuickAck":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpQuickAck((Boolean)member.getValue());
          }
          break;
        case "tcpUserTimeout":
          if (member.getValue() instanceof Number) {
            obj.setTcpUserTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "tracingPolicy":
          if (member.getValue() instanceof String) {
            obj.setTracingPolicy(io.vertx.core.tracing.TracingPolicy.valueOf((String)member.getValue()));
          }
          break;
        case "trafficClass":
          if (member.getValue() instanceof Number) {
            obj.setTrafficClass(((Number)member.getValue()).intValue());
          }
          break;
        case "trustAll":
          if (member.getValue() instanceof Boolean) {
            obj.setTrustAll((Boolean)member.getValue());
          }
          break;
        case "trustStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setTrustStoreOptions(new io.vertx.core.net.JksOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "tryUseCompression":
          if (member.getValue() instanceof Boolean) {
            obj.setTryUseCompression((Boolean)member.getValue());
          }
          break;
        case "tryUsePerFrameWebSocketCompression":
          if (member.getValue() instanceof Boolean) {
            obj.setTryUsePerFrameWebSocketCompression((Boolean)member.getValue());
          }
          break;
        case "tryUsePerMessageWebSocketCompression":
          if (member.getValue() instanceof Boolean) {
            obj.setTryUsePerMessageWebSocketCompression((Boolean)member.getValue());
          }
          break;
        case "tryWebSocketDeflateFrameCompression":
          break;
        case "useAlpn":
          if (member.getValue() instanceof Boolean) {
            obj.setUseAlpn((Boolean)member.getValue());
          }
          break;
        case "verifyHost":
          if (member.getValue() instanceof Boolean) {
            obj.setVerifyHost((Boolean)member.getValue());
          }
          break;
        case "webSocketClosingTimeout":
          if (member.getValue() instanceof Number) {
            obj.setWebSocketClosingTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "webSocketCompressionAllowClientNoContext":
          if (member.getValue() instanceof Boolean) {
            obj.setWebSocketCompressionAllowClientNoContext((Boolean)member.getValue());
          }
          break;
        case "webSocketCompressionLevel":
          if (member.getValue() instanceof Number) {
            obj.setWebSocketCompressionLevel(((Number)member.getValue()).intValue());
          }
          break;
        case "webSocketCompressionRequestServerNoContext":
          if (member.getValue() instanceof Boolean) {
            obj.setWebSocketCompressionRequestServerNoContext((Boolean)member.getValue());
          }
          break;
        case "writeIdleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setWriteIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(InheritingHttpClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(InheritingHttpClientOptions obj, java.util.Map<String, Object> json) {
    if (obj.getActivityLogDataFormat() != null) {
      json.put("activityLogDataFormat", obj.getActivityLogDataFormat().name());
    }
    if (obj.getAlpnVersions() != null) {
      JsonArray array = new JsonArray();
      obj.getAlpnVersions().forEach(item -> array.add(item.name()));
      json.put("alpnVersions", array);
    }
    json.put("connectTimeout", obj.getConnectTimeout());
    if (obj.getCrlPaths() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlPaths().forEach(item -> array.add(item));
      json.put("crlPaths", array);
    }
    if (obj.getCrlValues() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlValues().forEach(item -> array.add(BASE64_ENCODER.encodeToString(item.getBytes())));
      json.put("crlValues", array);
    }
    json.put("decoderInitialBufferSize", obj.getDecoderInitialBufferSize());
    if (obj.getDefaultHost() != null) {
      json.put("defaultHost", obj.getDefaultHost());
    }
    json.put("defaultPort", obj.getDefaultPort());
    if (obj.getEnabledCipherSuites() != null) {
      JsonArray array = new JsonArray();
      obj.getEnabledCipherSuites().forEach(item -> array.add(item));
      json.put("enabledCipherSuites", array);
    }
    if (obj.getEnabledSecureTransportProtocols() != null) {
      JsonArray array = new JsonArray();
      obj.getEnabledSecureTransportProtocols().forEach(item -> array.add(item));
      json.put("enabledSecureTransportProtocols", array);
    }
    json.put("forceSni", obj.isForceSni());
    json.put("http2ClearTextUpgrade", obj.isHttp2ClearTextUpgrade());
    json.put("http2ConnectionWindowSize", obj.getHttp2ConnectionWindowSize());
    json.put("http2KeepAliveTimeout", obj.getHttp2KeepAliveTimeout());
    json.put("http2MaxPoolSize", obj.getHttp2MaxPoolSize());
    json.put("http2MultiplexingLimit", obj.getHttp2MultiplexingLimit());
    json.put("idleTimeout", obj.getIdleTimeout());
    if (obj.getIdleTimeoutUnit() != null) {
      json.put("idleTimeoutUnit", obj.getIdleTimeoutUnit().name());
    }
    if (obj.getInitialSettings() != null) {
      json.put("initialSettings", obj.getInitialSettings().toJson());
    }
    if (obj.getJdkSslEngineOptions() != null) {
      json.put("jdkSslEngineOptions", obj.getJdkSslEngineOptions().toJson());
    }
    json.put("keepAlive", obj.isKeepAlive());
    json.put("keepAliveTimeout", obj.getKeepAliveTimeout());
    if (obj.getKeyStoreOptions() != null) {
      json.put("keyStoreOptions", obj.getKeyStoreOptions().toJson());
    }
    if (obj.getLocalAddress() != null) {
      json.put("localAddress", obj.getLocalAddress());
    }
    json.put("logActivity", obj.getLogActivity());
    json.put("maxChunkSize", obj.getMaxChunkSize());
    json.put("maxHeaderSize", obj.getMaxHeaderSize());
    json.put("maxInitialLineLength", obj.getMaxInitialLineLength());
    json.put("maxPoolSize", obj.getMaxPoolSize());
    json.put("maxRedirects", obj.getMaxRedirects());
    json.put("maxWaitQueueSize", obj.getMaxWaitQueueSize());
    json.put("maxWebSocketFrameSize", obj.getMaxWebSocketFrameSize());
    json.put("maxWebSocketMessageSize", obj.getMaxWebSocketMessageSize());
    json.put("maxWebSockets", obj.getMaxWebSockets());
    if (obj.getMetricsName() != null) {
      json.put("metricsName", obj.getMetricsName());
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
    if (obj.getNonProxyHosts() != null) {
      JsonArray array = new JsonArray();
      obj.getNonProxyHosts().forEach(item -> array.add(item));
      json.put("nonProxyHosts", array);
    }
    if (obj.getOpenSslEngineOptions() != null) {
      json.put("openSslEngineOptions", obj.getOpenSslEngineOptions().toJson());
    }
    if (obj.getPemKeyCertOptions() != null) {
      json.put("pemKeyCertOptions", obj.getPemKeyCertOptions().toJson());
    }
    if (obj.getPemTrustOptions() != null) {
      json.put("pemTrustOptions", obj.getPemTrustOptions().toJson());
    }
    if (obj.getPfxKeyCertOptions() != null) {
      json.put("pfxKeyCertOptions", obj.getPfxKeyCertOptions().toJson());
    }
    if (obj.getPfxTrustOptions() != null) {
      json.put("pfxTrustOptions", obj.getPfxTrustOptions().toJson());
    }
    json.put("pipelining", obj.isPipelining());
    json.put("pipeliningLimit", obj.getPipeliningLimit());
    json.put("poolCleanerPeriod", obj.getPoolCleanerPeriod());
    json.put("poolEventLoopSize", obj.getPoolEventLoopSize());
    if (obj.getProtocolVersion() != null) {
      json.put("protocolVersion", obj.getProtocolVersion().name());
    }
    if (obj.getProxyOptions() != null) {
      json.put("proxyOptions", obj.getProxyOptions().toJson());
    }
    json.put("readIdleTimeout", obj.getReadIdleTimeout());
    json.put("receiveBufferSize", obj.getReceiveBufferSize());
    json.put("reuseAddress", obj.isReuseAddress());
    json.put("reusePort", obj.isReusePort());
    json.put("sendBufferSize", obj.getSendBufferSize());
    json.put("sendUnmaskedFrames", obj.isSendUnmaskedFrames());
    json.put("shared", obj.isShared());
    json.put("soLinger", obj.getSoLinger());
    json.put("ssl", obj.isSsl());
    json.put("sslHandshakeTimeout", obj.getSslHandshakeTimeout());
    if (obj.getSslHandshakeTimeoutUnit() != null) {
      json.put("sslHandshakeTimeoutUnit", obj.getSslHandshakeTimeoutUnit().name());
    }
    json.put("tcpCork", obj.isTcpCork());
    json.put("tcpFastOpen", obj.isTcpFastOpen());
    json.put("tcpKeepAlive", obj.isTcpKeepAlive());
    json.put("tcpNoDelay", obj.isTcpNoDelay());
    json.put("tcpQuickAck", obj.isTcpQuickAck());
    json.put("tcpUserTimeout", obj.getTcpUserTimeout());
    if (obj.getTracingPolicy() != null) {
      json.put("tracingPolicy", obj.getTracingPolicy().name());
    }
    json.put("trafficClass", obj.getTrafficClass());
    json.put("trustAll", obj.isTrustAll());
    if (obj.getTrustStoreOptions() != null) {
      json.put("trustStoreOptions", obj.getTrustStoreOptions().toJson());
    }
    json.put("tryUseCompression", obj.isTryUseCompression());
    json.put("tryUsePerMessageWebSocketCompression", obj.getTryUsePerMessageWebSocketCompression());
    json.put("tryWebSocketDeflateFrameCompression", obj.getTryWebSocketDeflateFrameCompression());
    json.put("useAlpn", obj.isUseAlpn());
    json.put("verifyHost", obj.isVerifyHost());
    json.put("webSocketClosingTimeout", obj.getWebSocketClosingTimeout());
    json.put("webSocketCompressionAllowClientNoContext", obj.getWebSocketCompressionAllowClientNoContext());
    json.put("webSocketCompressionLevel", obj.getWebSocketCompressionLevel());
    json.put("webSocketCompressionRequestServerNoContext", obj.getWebSocketCompressionRequestServerNoContext());
    json.put("writeIdleTimeout", obj.getWriteIdleTimeout());
  }
}
