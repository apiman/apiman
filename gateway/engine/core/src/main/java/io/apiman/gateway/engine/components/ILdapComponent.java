package io.apiman.gateway.engine.components;

import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapComponent extends IComponent {

    /**
     * Open an LDAP connection to allow queries.
     *
     * @param config the configuration
     * @param handler the resulting connection
     */
    void connect(LdapConfigBean config, IAsyncResultHandler<ILdapClientConnection> handler);

    /**
     * LDAP BIND operation only.
     *
     * @param config the configuration
     * @param handler the handler indicating the success of the LDAP BIND.
     */
    void bind(LdapConfigBean config, IAsyncResultHandler<Boolean> handler);
}
