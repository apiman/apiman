package io.apiman.manager.sso.keycloak;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface ApimanEventProviderConfigConstants {
    String APIMAN_MANAGER_API_URI = "apiman-manager-url";
    // How do we best auth from KC to Apiman. Can we use a service acct?
}

//
// <spi name="eventsListener">
// <provider name="mqtt" enabled="true">
// <properties>
// <property name="url" value="${env.KK_TO_RMQ_URL:localhost}"/>
// <property name="port" value="${env.KK_TO_RMQ_PORT:5672}"/>
// <property name="vhost" value="${env.KK_TO_RMQ_VHOST:}"/>
// <property name="exchange" value="${env.KK_TO_RMQ_EXCHANGE:amq.topic}"/>
//
// <property name="username" value="${env.KK_TO_RMQ_USERNAME:guest}"/>
// <property name="password" value="${env.KK_TO_RMQ_PASSWORD:guest}"/>
// </properties>
// </provider>
// </spi>
