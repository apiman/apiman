package io.apiman.manager.sso.keycloak.event;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface ApimanEventProviderConfigConstants {
    String APIMAN_MANAGER_API_URI = "apiman-manager-url";
    // How do we best auth from KC to Apiman. Can we use a service acct?
}

//
// <spi name="eventsListener">
// <provider name="apiman-push-events" enabled="true">
// <properties>
// <property name="apiman-manager-url" value="http://localhost:8080/apiman"/>
// <property name="username" value="${env.KK_TO_RMQ_USERNAME:guest}"/>
// <property name="password" value="${env.KK_TO_RMQ_PASSWORD:guest}"/>
// </properties>
// </provider>
// </spi>
