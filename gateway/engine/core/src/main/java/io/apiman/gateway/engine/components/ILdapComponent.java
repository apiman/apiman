package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapResult;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * Allows simple BIND and query operations to an LDAP server.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapComponent extends IComponent {

    /**
     * Open an LDAP connection to allow queries, an ongoing connection is
     * returned to the handler.
     *
     * @param config the configuration
     * @param handler the resulting connection
     */
    void connect(LdapConfigBean config, IAsyncResultHandler<ILdapClientConnection> handler);

    /**
     * LDAP BIND operation only. The connection is terminate on your behalf.
     *
     * @param config the configuration
     * @param handler the handler indicating the success of the LDAP BIND.
     */
    void bind(LdapConfigBean config, IAsyncResultHandler<ILdapResult> handler);

    /**
     * Set a custom socket factory for TLS
     * @return this
     */
    ILdapComponent setSocketFactory(SSLSocketFactory socketFactory);
}
