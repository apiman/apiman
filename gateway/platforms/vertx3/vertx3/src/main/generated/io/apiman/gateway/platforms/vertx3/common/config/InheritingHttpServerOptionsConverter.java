package io.apiman.gateway.platforms.vertx3.common.config;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpServerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.apiman.gateway.platforms.vertx3.common.config.InheritingHttpServerOptions} original class using Vert.x codegen.
 */
public class InheritingHttpServerOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, InheritingHttpServerOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "acceptBacklog":
          if (member.getValue() instanceof Number) {
            obj.setAcceptBacklog(((Number)member.getValue()).intValue());
          }
          break;
        case "acceptUnmaskedFrames":
          if (member.getValue() instanceof Boolean) {
            obj.setAcceptUnmaskedFrames((Boolean)member.getValue());
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
        case "clientAuth":
          if (member.getValue() instanceof String) {
            obj.setClientAuth(io.vertx.core.http.ClientAuth.valueOf((String)member.getValue()));
          }
          break;
        case "clientAuthRequired":
          if (member.getValue() instanceof Boolean) {
            obj.setClientAuthRequired((Boolean)member.getValue());
          }
          break;
        case "compressionLevel":
          if (member.getValue() instanceof Number) {
            obj.setCompressionLevel(((Number)member.getValue()).intValue());
          }
          break;
        case "compressionSupported":
          if (member.getValue() instanceof Boolean) {
            obj.setCompressionSupported((Boolean)member.getValue());
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
                obj.addCrlValue(io.vertx.core.buffer.Buffer.buffer(java.util.Base64.getDecoder().decode((String)item)));
            });
          }
          break;
        case "decoderInitialBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setDecoderInitialBufferSize(((Number)member.getValue()).intValue());
          }
          break;
        case "decompressionSupported":
          if (member.getValue() instanceof Boolean) {
            obj.setDecompressionSupported((Boolean)member.getValue());
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
        case "handle100ContinueAutomatically":
          if (member.getValue() instanceof Boolean) {
            obj.setHandle100ContinueAutomatically((Boolean)member.getValue());
          }
          break;
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "http2ConnectionWindowSize":
          if (member.getValue() instanceof Number) {
            obj.setHttp2ConnectionWindowSize(((Number)member.getValue()).intValue());
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
            obj.setInitialSettings(new io.vertx.core.http.Http2Settings((JsonObject)member.getValue()));
          }
          break;
        case "jdkSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setJdkSslEngineOptions(new io.vertx.core.net.JdkSSLEngineOptions((JsonObject)member.getValue()));
          }
          break;
        case "keyStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setKeyStoreOptions(new io.vertx.core.net.JksOptions((JsonObject)member.getValue()));
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
        case "maxWebsocketFrameSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWebsocketFrameSize(((Number)member.getValue()).intValue());
          }
          break;
        case "maxWebsocketMessageSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxWebsocketMessageSize(((Number)member.getValue()).intValue());
          }
          break;
        case "openSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setOpenSslEngineOptions(new io.vertx.core.net.OpenSSLEngineOptions((JsonObject)member.getValue()));
          }
          break;
        case "pemKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemKeyCertOptions(new io.vertx.core.net.PemKeyCertOptions((JsonObject)member.getValue()));
          }
          break;
        case "pemTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemTrustOptions(new io.vertx.core.net.PemTrustOptions((JsonObject)member.getValue()));
          }
          break;
        case "perFrameWebsocketCompressionSupported":
          if (member.getValue() instanceof Boolean) {
            obj.setPerFrameWebsocketCompressionSupported((Boolean)member.getValue());
          }
          break;
        case "perMessageWebsocketCompressionSupported":
          if (member.getValue() instanceof Boolean) {
            obj.setPerMessageWebsocketCompressionSupported((Boolean)member.getValue());
          }
          break;
        case "pfxKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxKeyCertOptions(new io.vertx.core.net.PfxOptions((JsonObject)member.getValue()));
          }
          break;
        case "pfxTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxTrustOptions(new io.vertx.core.net.PfxOptions((JsonObject)member.getValue()));
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
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
        case "sni":
          if (member.getValue() instanceof Boolean) {
            obj.setSni((Boolean)member.getValue());
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
        case "trafficClass":
          if (member.getValue() instanceof Number) {
            obj.setTrafficClass(((Number)member.getValue()).intValue());
          }
          break;
        case "trustStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setTrustStoreOptions(new io.vertx.core.net.JksOptions((JsonObject)member.getValue()));
          }
          break;
        case "useAlpn":
          if (member.getValue() instanceof Boolean) {
            obj.setUseAlpn((Boolean)member.getValue());
          }
          break;
        case "usePooledBuffers":
          if (member.getValue() instanceof Boolean) {
            obj.setUsePooledBuffers((Boolean)member.getValue());
          }
          break;
        case "websocketAllowServerNoContext":
          if (member.getValue() instanceof Boolean) {
            obj.setWebsocketAllowServerNoContext((Boolean)member.getValue());
          }
          break;
        case "websocketCompressionLevel":
          if (member.getValue() instanceof Number) {
            obj.setWebsocketCompressionLevel(((Number)member.getValue()).intValue());
          }
          break;
        case "websocketPreferredClientNoContext":
          if (member.getValue() instanceof Boolean) {
            obj.setWebsocketPreferredClientNoContext((Boolean)member.getValue());
          }
          break;
        case "websocketSubProtocols":
          if (member.getValue() instanceof String) {
            obj.setWebsocketSubProtocols((String)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(InheritingHttpServerOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(InheritingHttpServerOptions obj, java.util.Map<String, Object> json) {
    json.put("acceptBacklog", obj.getAcceptBacklog());
    json.put("acceptUnmaskedFrames", obj.isAcceptUnmaskedFrames());
    if (obj.getAlpnVersions() != null) {
      JsonArray array = new JsonArray();
      obj.getAlpnVersions().forEach(item -> array.add(item.name()));
      json.put("alpnVersions", array);
    }
    if (obj.getClientAuth() != null) {
      json.put("clientAuth", obj.getClientAuth().name());
    }
    json.put("clientAuthRequired", obj.isClientAuthRequired());
    json.put("compressionLevel", obj.getCompressionLevel());
    json.put("compressionSupported", obj.isCompressionSupported());
    if (obj.getCrlPaths() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlPaths().forEach(item -> array.add(item));
      json.put("crlPaths", array);
    }
    if (obj.getCrlValues() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlValues().forEach(item -> array.add(java.util.Base64.getEncoder().encodeToString(item.getBytes())));
      json.put("crlValues", array);
    }
    json.put("decoderInitialBufferSize", obj.getDecoderInitialBufferSize());
    json.put("decompressionSupported", obj.isDecompressionSupported());
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
    json.put("handle100ContinueAutomatically", obj.isHandle100ContinueAutomatically());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("http2ConnectionWindowSize", obj.getHttp2ConnectionWindowSize());
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
    if (obj.getKeyStoreOptions() != null) {
      json.put("keyStoreOptions", obj.getKeyStoreOptions().toJson());
    }
    json.put("logActivity", obj.getLogActivity());
    json.put("maxChunkSize", obj.getMaxChunkSize());
    json.put("maxHeaderSize", obj.getMaxHeaderSize());
    json.put("maxInitialLineLength", obj.getMaxInitialLineLength());
    json.put("maxWebsocketFrameSize", obj.getMaxWebsocketFrameSize());
    json.put("maxWebsocketMessageSize", obj.getMaxWebsocketMessageSize());
    if (obj.getOpenSslEngineOptions() != null) {
      json.put("openSslEngineOptions", obj.getOpenSslEngineOptions().toJson());
    }
    if (obj.getPemKeyCertOptions() != null) {
      json.put("pemKeyCertOptions", obj.getPemKeyCertOptions().toJson());
    }
    if (obj.getPemTrustOptions() != null) {
      json.put("pemTrustOptions", obj.getPemTrustOptions().toJson());
    }
    json.put("perFrameWebsocketCompressionSupported", obj.getPerFrameWebsocketCompressionSupported());
    json.put("perMessageWebsocketCompressionSupported", obj.getPerMessageWebsocketCompressionSupported());
    if (obj.getPfxKeyCertOptions() != null) {
      json.put("pfxKeyCertOptions", obj.getPfxKeyCertOptions().toJson());
    }
    if (obj.getPfxTrustOptions() != null) {
      json.put("pfxTrustOptions", obj.getPfxTrustOptions().toJson());
    }
    json.put("port", obj.getPort());
    json.put("receiveBufferSize", obj.getReceiveBufferSize());
    json.put("reuseAddress", obj.isReuseAddress());
    json.put("reusePort", obj.isReusePort());
    json.put("sendBufferSize", obj.getSendBufferSize());
    json.put("sni", obj.isSni());
    json.put("soLinger", obj.getSoLinger());
    json.put("ssl", obj.isSsl());
    json.put("tcpCork", obj.isTcpCork());
    json.put("tcpFastOpen", obj.isTcpFastOpen());
    json.put("tcpKeepAlive", obj.isTcpKeepAlive());
    json.put("tcpNoDelay", obj.isTcpNoDelay());
    json.put("tcpQuickAck", obj.isTcpQuickAck());
    json.put("trafficClass", obj.getTrafficClass());
    if (obj.getTrustStoreOptions() != null) {
      json.put("trustStoreOptions", obj.getTrustStoreOptions().toJson());
    }
    json.put("useAlpn", obj.isUseAlpn());
    json.put("usePooledBuffers", obj.isUsePooledBuffers());
    json.put("websocketAllowServerNoContext", obj.getWebsocketAllowServerNoContext());
    json.put("websocketCompressionLevel", obj.getWebsocketCompressionLevel());
    json.put("websocketPreferredClientNoContext", obj.getWebsocketPreferredClientNoContext());
    if (obj.getWebsocketSubProtocols() != null) {
      json.put("websocketSubProtocols", obj.getWebsocketSubProtocols());
    }
  }
}
