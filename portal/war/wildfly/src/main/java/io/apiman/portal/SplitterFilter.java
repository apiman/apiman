/*
 * Copyright 2022. Black Parrot Labs Ltd
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

package io.apiman.portal;

import io.apiman.common.config.ConfigDirectoryFinder;
import io.apiman.common.config.ConfigFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.spec.ServletContextImpl;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SplitterFilter implements Filter {
    private final Set<String> resourceExistsSet = new HashSet<>();
    private final Map<String, String> configCache = new HashMap<>();
    private CompositeConfiguration config;
    private Path resourceRoot;
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.resourceRoot = ConfigDirectoryFinder.getConfigDirectory().resolve("portal");
        this.config = ConfigFactory.createConfig();
        // Some code sleuthing in Undertow let me find out how to provide a custom resource manager, hence letting the default HTTP servlet do its work in a different
        // directory. Unfortunately, this will not be portable, so need to think of a more vendor-neutral way to achieve this, potentially.
        ServletContextImpl sc = (ServletContextImpl) filterConfig.getServletContext();
        FileResourceManager resManager = new FileResourceManager(resourceRoot.toFile());
        sc.getDeployment().getDeploymentInfo().setResourceManager(resManager);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = trim(req.getRequestURI().substring(req.getContextPath().length()));
        // Would be better if we can put all the assets (other than index) under dist, so we can return real 404s
        // Currently this breaks the loading of the config file, though...
        // if (!path.startsWith("/dist")) {
        //     chain.doFilter(request, response);
        // } else {
        //     request.getRequestDispatcher("/spa/index.html").forward(request, response);
        // }
        if (resourceExists(path) && !path.isBlank()) {
            // If file exists, serve it up.
            if (path.endsWith(".json5") || path.endsWith(".json")) {
                String newResponse = configCache.computeIfAbsent(path, this::parseAndSubstitute);
                response.setContentType("application/json");
                response.setContentLength(newResponse.length());
                response.getWriter().write(newResponse);
                response.flushBuffer();
            } else {
                chain.doFilter(request, response);
            }
        } else {
            // Else, send the spa index.
            // String index = FileUtils.readFileToString(resourceRoot.resolve("index.html").toFile(), StandardCharsets.UTF_8);
            // response.setContentLength(index.length());
            // response.setContentType("text/html; charset=UTF-8");
            // response.getWriter().write(index);
            // response.flushBuffer();
            req.getRequestDispatcher("/index.html").forward(req, response);
        }
    }

    private String trim(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String parseAndSubstitute(String path) {
        try {
            String str = FileUtils.readFileToString(resourceRoot.resolve(path).toFile(), StandardCharsets.UTF_8);
            return config.getSubstitutor().replace(str);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean resourceExists(String path) {
        if (resourceExistsSet.contains(path)) {
            return true;
        }
        if (Files.exists(resourceRoot.resolve(path))) {
            resourceExistsSet.add(path);
            return true;
        } else {
            return false;
        }
    }
}
