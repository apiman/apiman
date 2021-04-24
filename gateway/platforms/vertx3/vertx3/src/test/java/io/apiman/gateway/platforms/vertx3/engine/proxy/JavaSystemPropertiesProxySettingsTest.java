package io.apiman.gateway.platforms.vertx3.engine.proxy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaSystemPropertiesProxySettingsTest {

    @Test
    public void httpProxy_withValidSettings_shouldExtractValidProxySettings() {
        setSysProp("http.proxyHost", "proxy.local");
        setSysProp("http.proxyPort", "1234");
        setSysProp("http.proxyUser", "secret");
        setSysProp("http.proxyPassword", "password!");
        setSysProp("http.nonProxyHosts", "localhost");

        HttpProxy expectedProxy = new HttpProxy(
          "proxy.local",
                1234,
            "secret",
            "password!"
        );

        JavaSystemPropertiesProxySettings settings =  JavaSystemPropertiesProxySettings.createHttpProxy(
          "http",
          80
        );

        assertThat(settings.getProxy())
            .isNotNull()
            .isEqualTo(expectedProxy);

        assertThat(settings.isNonProxyHost("localhost")).isTrue();
        assertThat(settings.isNonProxyHost("somerandom.address")).isFalse();
    }

    @Test
    public void httpProxy_withNonProxyIPv4Mask_shouldRecogniseNonProxyFromWholeRange() {
        setSysProp("http.proxyHost", "proxy.local");
        setSysProp("http.proxyPort", "1234");
        setSysProp("http.proxyUser", "secret");
        setSysProp("http.proxyPassword", "password!");
        setSysProp("http.nonProxyHosts", "169.254/16");

        JavaSystemPropertiesProxySettings settings =  JavaSystemPropertiesProxySettings.createHttpProxy(
            "http",
            80
        );

        // Following address falls under the nonProxyHosts mask 169.254/16
        assertThat(settings.isNonProxyHost("169.254.12.45")).isTrue();
    }

    @Test
    public void httpProxy_withNonProxyIPv4Wildcard_shouldRecogniseNonProxyFromWholeRange() {
        setSysProp("http.proxyHost", "proxy.local");
        setSysProp("http.proxyPort", "1234");
        setSysProp("http.proxyUser", "secret");
        setSysProp("http.proxyPassword", "password!");
        setSysProp("http.nonProxyHosts", "169.168.1.*");

        JavaSystemPropertiesProxySettings settings =  JavaSystemPropertiesProxySettings.createHttpProxy(
            "http",
            80
        );

        // Following address falls under the nonProxyHosts mask 169.254/16
        assertThat(settings.isNonProxyHost("169.168.1.1")).isTrue();
    }

    @Test
    public void httpProxy_withNonProxyIPv4_shouldRecogniseSingleIp() {
        setSysProp("http.proxyHost", "proxy.local");
        setSysProp("http.proxyPort", "1234");
        setSysProp("http.proxyUser", "secret");
        setSysProp("http.proxyPassword", "password!");
        setSysProp("http.nonProxyHosts", "169.168.1.1");

        JavaSystemPropertiesProxySettings settings =  JavaSystemPropertiesProxySettings.createHttpProxy(
            "http",
            80
        );

        // Following address falls under the nonProxyHosts mask 169.254/16
        assertThat(settings.isNonProxyHost("169.168.1.1")).isTrue();
    }

    private void setSysProp(String key, String value) {
        System.setProperty(key, value);
    }
}