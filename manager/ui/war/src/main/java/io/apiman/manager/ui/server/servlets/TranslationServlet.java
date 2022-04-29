/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.ui.server.servlets;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.AbstractMessages;
import io.apiman.manager.ui.server.i18n.Messages;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * A servlet that returns a JSONP response containing all of the UI strings
 * translated into the appropriate locale of the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class TranslationServlet extends AbstractUIServlet {

    private static final long serialVersionUID = -7209551552522960775L;
    private static final IApimanLogger LOG = ApimanLoggerFactory.getLogger(TranslationServlet.class);
    private static boolean EXT_MESSAGE_BUNDLES_LOADED = false;

    /**
     * Constructor.
     */
    public TranslationServlet() {
        super();
        if (!EXT_MESSAGE_BUNDLES_LOADED) {
            loadExternalMessageBundles();
            EXT_MESSAGE_BUNDLES_LOADED = true;
        }
    }

    private void loadExternalMessageBundles() {
        Path dir = super.getConfig().getExternalMessageBundlesDir();
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(dir, "*.properties")) {
            for (Path propFilePath : directory) {
                if (Files.isDirectory(propFilePath)) {
                    continue;
                }
                LOG.debug("Loading external i18n file: {0}...", propFilePath);
                String fName = propFilePath.getFileName().toString();
                // Remove .properties extension and isolate filename language tag (limit at 2 to prevent overshooting with more complex tags).
                String[] fNameSplit = fName.substring(0, fName.length() - 11).split("_", 2);
                String languageTag;
                if (fNameSplit.length >= 2) {
                    // Replace _ with - in the filename language tag to allow parsing as language tag/Locale (e.g. en_GB => en-GB).
                    languageTag = fNameSplit[1].replace("_", "-");
                } else {
                    // If no language tag, assume EN.
                    languageTag = "en";
                }
                // As we're not using any of the in-built methods to read the bundle from the classpath, the locale field will be null (resulting in 'und' Locale).
                // Above, we parse the language tag from the filename ourselves; below, we override the #getLocale method to return a valid locale.
                PropertyResourceBundle bundle = new PropertyResourceBundle(new FileReader(propFilePath.toFile())) {
                    @Override
                    public Locale getLocale() {
                        return Locale.forLanguageTag(languageTag);
                    }
                };
                // Special "External" basename just to make this a bit clearer when debugging, etc; the basename will disappear by the time it's on the wire.
                AbstractMessages.addResourceBundle("External", bundle);
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // TODO(msavy): look at user profile and/or token also. Might need to use ThreadLocal?
        Messages.setLocale(request.getLocale());
        try {
            Map<String, String> strings = Messages.i18n.all();
            // Externally defined strings
            strings.putAll(Messages.i18n.get("External"));

            response.getOutputStream().write("window.APIMAN_TRANSLATION_DATA = ".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            g.useDefaultPrettyPrinter();

            // Write string data here.
            g.writeStartObject();
            for (Entry<String, String> entry : strings.entrySet()) {
                g.writeStringField(entry.getKey(), entry.getValue());
            }
            g.writeEndObject();

            g.flush();
            response.getOutputStream().write(";".getBytes(StandardCharsets.UTF_8)); //$NON-NLS-1$ //$NON-NLS-2$
            g.close();
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            Messages.clearLocale();
        }
    }
}
