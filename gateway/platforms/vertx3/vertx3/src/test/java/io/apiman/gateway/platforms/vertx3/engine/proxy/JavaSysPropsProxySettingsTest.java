/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.platforms.vertx3.engine.proxy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaSysPropsProxySettingsTest {

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

        JavaSysPropsProxySettings settings =  JavaSysPropsProxySettings.createHttpProxy(
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

        JavaSysPropsProxySettings settings =  JavaSysPropsProxySettings.createHttpProxy(
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

        JavaSysPropsProxySettings settings =  JavaSysPropsProxySettings.createHttpProxy(
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

        JavaSysPropsProxySettings settings =  JavaSysPropsProxySettings.createHttpProxy(
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