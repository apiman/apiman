package io.apiman.plugins.keycloak_oauth_policy;

import io.apiman.plugins.keycloak_oauth_policy.beans.KeycloakOauthConfigBean;

import org.junit.*;

/**
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class KeycloakOauthBeanTest {
    
    private String exampleCer = "MIIBlTCB/wIGAUo/KSOfMA0GCSqGSIb3DQEBCwUAMBExDzANBgNVBAMTBmFwaW1hbj"
            + "AeFw0xNDEyMTIxNTM5MjhaFw0yNDEyMTIxNTQxMDhaMBExDzANBgNVBAMTBmFwaW1hbjCBnzANBgkqhkiG9w"
            + "0BAQEFAAOBjQAwgYkCgYEAq1awrk7QK24Gmcy9Yb4dMbS+ZnO6NDaj1Z2F5C74HMIgtwYyxsNbRhBqCWlw7k"
            + "mkZZaG5udyQYY8d91Db/uc/1DBuJMrQVsYXjVSpy+hoKpTWmzGhXzyzwhfJAICp7Iu/TTKPp+ip0mPGHlJnn"
            + "P6dr4ztjY7EgFXFhEDFYSd9S8CAwEAATANBgkqhkiG9w0BAQsFAAOBgQCIh0VYWdJElxtR8vgsbZxcCzNoJU"
            + "WvGbXPVdT5bQegTYxS8wFEitt+iGpXFPTnwZd2kZePhHCYMDpVOuwknOfVqoxb/4hroZzcn5xKd8PepMbcY0"
            + "dAoUxhGqGb2zenuG8YKc36AFMRWZ25zv0/8MTMWCC4wqROdPxUj62hzGosiQ==";
    
    private KeycloakOauthConfigBean bean;

    @Before
    public void before() {
        bean = new KeycloakOauthConfigBean();
    }

    @Test
    public void shouldAllowPEMCertificateEntry() {
        bean.setRealmCertificateString(exampleCer);
        Assert.assertNotNull(bean.getRealmCertificate());
    }
}
