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
package io.apiman.manager.api.micro.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

import org.eclipse.jetty.util.resource.Resource;

/**
 * @author eric.wittmann@redhat.com
 */
public class StringResource extends Resource {

    private String value;

    /**
     * Constructor.
     */
    public StringResource(String value) {
        this.value = value;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#isContainedIn(org.eclipse.jetty.util.resource.Resource)
     */
    @Override
    public boolean isContainedIn(Resource r) throws MalformedURLException {
        return false;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#close()
     */
    @Override
    public void close() {
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#exists()
     */
    @Override
    public boolean exists() {
        return true;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#lastModified()
     */
    @Override
    public long lastModified() {
        return System.currentTimeMillis();
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#length()
     */
    @Override
    public long length() {
        return value.length();
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#getURL()
     */
    @Override
    public URL getURL() {
        return null;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#getFile()
     */
    @Override
    public File getFile() throws IOException {
        return null;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#getName()
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(value.getBytes("UTF-8")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#getReadableByteChannel()
     */
    @Override
    public ReadableByteChannel getReadableByteChannel() throws IOException {
        return null;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#delete()
     */
    @Override
    public boolean delete() throws SecurityException {
        return false;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#renameTo(org.eclipse.jetty.util.resource.Resource)
     */
    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        return false;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#list()
     */
    @Override
    public String[] list() {
        return null;
    }

    /**
     * @see org.eclipse.jetty.util.resource.Resource#addPath(java.lang.String)
     */
    @Override
    public Resource addPath(String path) throws IOException, MalformedURLException {
        return null;
    }

}
