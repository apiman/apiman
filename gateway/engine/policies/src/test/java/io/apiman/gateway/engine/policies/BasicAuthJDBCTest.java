/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.BasicAuthenticationConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "javadoc" })
public class BasicAuthJDBCTest {

    private static final String JDBC_USER = "bwayne";
    private static final String JDBC_PASSWORD = "bwayne123!";

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitialContextFactoryForTest.class.getName());

        // Create a test datasource and bind it to JNDI
        BasicDataSource ds = createInMemoryDatasource();
        InitialContext ctx = new InitialContext();
        ensureCtx(ctx, "java:comp/env"); //$NON-NLS-1$
        ensureCtx(ctx, "java:comp/env/jdbc"); //$NON-NLS-1$
        ctx.bind("java:comp/env/jdbc/BasicAuthJDBCTest", ds); //$NON-NLS-1$
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#apply(ServiceRequest, IPolicyContext, Object, IPolicyChain)}.
     */
    @Test
    public void testApplyJdbcNoRoles() throws Exception {
        // A live LDAP server is required to run this test!
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        String json =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"jdbcIdentity\" : {\r\n" +
                "        \"datasourcePath\" : \"jdbc/BasicAuthJDBCTest\",\r\n" +
                "        \"query\" : \"SELECT * FROM users WHERE username = ? AND password = ?\",\r\n" +
                "        \"hashAlgorithm\" : \"SHA1\"\r\n" +
                "    }\r\n" +
                "}";
        BasicAuthenticationConfig config = policy.parseConfiguration(json);
        ServiceRequest request = new ServiceRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

        // Failure
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        // Failure
        request.getHeaders().put("Authorization", createBasicAuthorization(JDBC_USER, "invalid_password"));
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        // Success
        request.getHeaders().put("Authorization", createBasicAuthorization(JDBC_USER, JDBC_PASSWORD));
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.BasicAuthenticationPolicy#apply(ServiceRequest, IPolicyContext, Object, IPolicyChain)}.
     */
    @Test
    public void testApplyJdbcWithRoles() throws Exception {
        // A live LDAP server is required to run this test!
        BasicAuthenticationPolicy policy = new BasicAuthenticationPolicy();
        String json =
                "{\r\n" +
                "    \"realm\" : \"TestRealm\",\r\n" +
                "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
                "    \"jdbcIdentity\" : {\r\n" +
                "        \"datasourcePath\" : \"jdbc/BasicAuthJDBCTest\",\r\n" +
                "        \"query\" : \"SELECT * FROM users WHERE username = ? AND password = ?\",\r\n" +
                "        \"hashAlgorithm\" : \"SHA1\",\r\n" +
                "        \"extractRoles\" : true,\r\n" +
                "        \"roleQuery\" : \"SELECT r.rolename FROM roles r WHERE r.username = ?\"\r\n" +
                "    }\r\n" +
                "}";
        BasicAuthenticationConfig config = policy.parseConfiguration(json);
        ServiceRequest request = new ServiceRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

        // Success
        request.getHeaders().put("Authorization", createBasicAuthorization(JDBC_USER, JDBC_PASSWORD));
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);
        Set<String> expectedRoles = new HashSet<>();
        expectedRoles.add("admin");
        expectedRoles.add("user");
        Mockito.verify(context).setAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, expectedRoles);
    }

    /**
     * Creates the http Authorization string for the given credentials.
     * @param username
     * @param password
     */
    private String createBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(creds.getBytes()));
        return builder.toString();
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa"); //$NON-NLS-1$
        ds.setPassword(""); //$NON-NLS-1$
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"); //$NON-NLS-1$
        Connection connection = ds.getConnection();
        connection.prepareStatement("CREATE TABLE users ( username varchar(255) NOT NULL, password varchar(255) NOT NULL, PRIMARY KEY (username))").executeUpdate();
        connection.prepareStatement("INSERT INTO users (username, password) VALUES ('bwayne', 'ae2efd698aefdf366736a4eda1bc5241f9fbfec7')").executeUpdate();
        connection.prepareStatement("INSERT INTO users (username, password) VALUES ('ckent', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')").executeUpdate();
        connection.prepareStatement("INSERT INTO users (username, password) VALUES ('ballen', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')").executeUpdate();
        connection.prepareStatement("CREATE TABLE roles (rolename varchar(255) NOT NULL, username varchar(255) NOT NULL)").executeUpdate();
        connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('user', 'bwayne')").executeUpdate();
        connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('admin', 'bwayne')").executeUpdate();
        connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('ckent', 'user')").executeUpdate();
        connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('ballen', 'user')").executeUpdate();
        connection.close();
        return ds;
    }

    /**
     * Ensure that the given name is bound to a context.
     * @param ctx
     * @param name
     * @throws NamingException
     */
    private static void ensureCtx(InitialContext ctx, String name) throws NamingException {
        try {
            ctx.bind(name, new InitialContext());
        } catch (NameAlreadyBoundException e) {
            // this is ok
        }
    }
}
