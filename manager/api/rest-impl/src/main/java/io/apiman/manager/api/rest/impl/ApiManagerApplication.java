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

package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.exportimport.manager.ExportImportManager;
import io.apiman.manager.api.providers.JacksonObjectMapperProvider;
import io.apiman.manager.api.rest.exceptions.mappers.BeanValidationExceptionMapper;
import io.apiman.manager.api.rest.exceptions.mappers.IllegalArgumentExceptionMapper;
import io.apiman.manager.api.rest.exceptions.mappers.RestExceptionMapper;
import io.apiman.manager.api.rest.interceptors.BlobResourceInterceptorProvider;
import io.apiman.manager.api.rest.interceptors.TotalCountInterceptorProvider;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The jax-rs application for the API Manager rest api.
 *
 * @author eric.wittmann@redhat.com
 */
//@OpenAPIDefinition(info = @Info(
//        title = "Apiman Manager API",
//        description = "Apiman's manager has its own standalone API which enables you to perform all functionality on the platform."
//    )
//)
@ApplicationPath("/")
@ApplicationScoped
public class ApiManagerApplication extends Application {
    private IApimanLogger log = ApimanLoggerFactory.getLogger(ApiManagerApplication.class);

    @Inject
    private ExportImportManager manager;

    private Set<Class<?>> classes = new HashSet<>();

    /**
     * Constructor.
     */
    public ApiManagerApplication() {
        //add swagger 2.0 config
        //BeanConfig beanConfig = new BeanConfig();
        //beanConfig.setVersion(new Version().getVersionString());
        //beanConfig.setBasePath(getBasePath()); //$NON-NLS-1$
        //beanConfig.setResourcePackage("io.apiman.manager.api.rest"); //$NON-NLS-1$
        //beanConfig.setTitle("API Manager REST API");
        //beanConfig.setDescription("The API Manager REST API is used by the API Manager UI to get stuff done. You can use it to automate any API Management task you wish. For example, create new Organizations, Plans, Clients, and APIs.");
        //beanConfig.setScan(true);


        classes.add(SystemResourceImpl.class);
        classes.add(SearchResourceImpl.class);
        classes.add(RoleResourceImpl.class);
        classes.add(UserResourceImpl.class);
        classes.add(OrganizationResourceImpl.class);
        classes.add(PolicyDefinitionResourceImpl.class);
        classes.add(GatewayResourceImpl.class);
        classes.add(PluginResourceImpl.class);
        classes.add(ActionResourceImpl.class);
        classes.add(DownloadResourceImpl.class);
        classes.add(DeveloperResourceImpl.class);
        classes.add(BlobResourceImpl.class);
        classes.add(EventResourceImpl.class);
        classes.add(DeveloperPortalResourceImpl.class);
        classes.add(ProtectedDeveloperPortalResourceWrapper.class);

        //add swagger 2.0 resource
        classes.add(OpenApiResource.class);

        classes.add(RestExceptionMapper.class);
        classes.add(IllegalArgumentExceptionMapper.class);
        classes.add(BeanValidationExceptionMapper.class);

        registerProviders(
                JacksonObjectMapperProvider.class,
                BlobResourceInterceptorProvider.class,
                TotalCountInterceptorProvider.class,
                RestExceptionMapper.class
                // EagerProvider.class
        );

        OpenAPI oas = new OpenAPI();
        oas.setInfo(new Info()
                .title("API Manager REST API")
                .version(new Version().getVersionString())
                .description("The API Manager REST API is used by the API Manager UI to get stuff done. " +
                        "You can use it to automate any API Management task you wish. " +
                        "For example, create new Organizations, Plans, Clients, and APIs.")
        );
        oas.addServersItem(new Server().url(getBasePath()));

        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .resourcePackages(Set.of("io.apiman.manager.api.rest"))
                .resourceClasses(klazzes)
                .prettyPrint(true);

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .application(this)
                    .openApiConfiguration(oasConfig)
                    .resourcePackages(Set.of("io.apiman.manager.api.rest"))
                    .resourceClasses(klazzes)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static final Set<String> klazzes = new LinkedHashSet<>();
    private void registerProviders(Class<?>... classes) {
        for (Class<?> klazz : classes) {
            log.info("Registering provider: {0}", klazz.getCanonicalName());
            ResteasyProviderFactory.getInstance().register(klazz);
            klazzes.add(klazz.getCanonicalName());
        }
    }

    @PostConstruct
    protected void postConstruct() {
        if (manager.isImportExport()) {
            manager.doImportExport();
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    private String getBasePath() {
        String basePath = "/apiman";
        String prefix = System.getenv("SYSTEM_PREFIX");
        return prefix == null ? basePath : prefix.trim().toLowerCase() + basePath;
    }
}
