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
package io.apiman.common.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * A classloader that is capable of loading classes from an apiman
 * plugin artifact.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class PluginClassLoader extends ClassLoader {

    private ZipFile pluginArtifactZip;
    private List<ZipFile> dependencyZips;
    private File workDir;
    private boolean closed;

    /**
     * Constructor.
     * @param pluginArtifactFile plugin artifact
     * @throws IOException if an I/O error has occurred
     */
    public PluginClassLoader(File pluginArtifactFile) throws IOException {
        super();
        closed = false;
        this.pluginArtifactZip = new ZipFile(pluginArtifactFile);
        this.workDir = createWorkDir(pluginArtifactFile);
        indexPluginArtifact();
    }

    /**
     * Constructor.
     * @param pluginArtifactFile plugin artifact
     * @param parent parent classloader
     * @throws IOException if an I/O error has occurred
     */
    public PluginClassLoader(File pluginArtifactFile, ClassLoader parent) throws IOException {
        super(parent);
        this.pluginArtifactZip = new ZipFile(pluginArtifactFile);
        this.workDir = createWorkDir(pluginArtifactFile);
        indexPluginArtifact();
    }

    /**
     * Creates a work directory into which various resources discovered in the plugin
     * artifact can be extracted.
     * @param pluginArtifactFile plugin artifact
     * @throws IOException if an I/O error has occurred
     */
    protected File createWorkDir(File pluginArtifactFile) throws IOException {
        File tempDir = File.createTempFile(pluginArtifactFile.getName(), "");
        tempDir.delete();
        tempDir.mkdirs();
        return tempDir;
    }

    /**
     * Indexes the content of the plugin artifact.  This includes discovering all of the
     * dependency JARs as well as any configuration resources such as plugin definitions.
     * @throws IOException if an I/O error has occurred
     */
    private void indexPluginArtifact() throws IOException {
        dependencyZips = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = this.pluginArtifactZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().startsWith("WEB-INF/lib/") && zipEntry.getName().toLowerCase().endsWith(".jar")) {
                ZipFile dependencyZipFile = extractDependency(zipEntry);
                if (dependencyZipFile != null) {
                    dependencyZips.add(dependencyZipFile);
                }
            }
        }
    }

    /**
     * Extracts a dependency from the plugin artifact ZIP and saves it to the work
     * directory.  If the dependency has already been extracted (we're re-using the
     * work directory) then this simply returns what is already there.
     * @param zipEntry a ZIP file entry
     * @throws IOException if an I/O error has occurred
     */
    private ZipFile extractDependency(ZipEntry zipEntry) throws IOException {
        File dependencyWorkDir = new File(workDir, "lib");
        if (!dependencyWorkDir.exists()) {
            dependencyWorkDir.mkdirs();
        }
        String depFileName = new File(zipEntry.getName()).getName();
        File depFile = new File(dependencyWorkDir, depFileName);
        if (!depFile.isFile()) {
            File tmpFile = File.createTempFile("lib", ".tmp", dependencyWorkDir);
            tmpFile.deleteOnExit();

            InputStream input = null;
            OutputStream output = null;
            try {
                input = this.pluginArtifactZip.getInputStream(zipEntry);
                output = new FileOutputStream(tmpFile);
                IOUtils.copy(input, output);
                output.flush();
                IOUtils.closeQuietly(output);
                tmpFile.renameTo(depFile);
            } catch (IOException e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
            }
        }
        return new ZipFile(depFile);
    }

    /**
     * Extracts a resource from the plugin artifact ZIP and saves it to the work
     * directory.  If the resource has already been extracted (we're re-using the
     * work directory) then this simply returns what is already there.
     * @param zipEntry a ZIP file entry
     * @throws IOException if an I/O error has occurred
     */
    private URL extractResource(ZipEntry zipEntry) throws IOException {
        File resourceWorkDir = new File(workDir, "resources");
        if (!resourceWorkDir.exists()) {
            resourceWorkDir.mkdirs();
        }
        File resourceFile = new File(resourceWorkDir, zipEntry.getName());
        if (!resourceFile.isFile()) {
            resourceFile.getParentFile().mkdirs();
            File tmpFile = File.createTempFile("res", ".tmp", resourceWorkDir);
            tmpFile.deleteOnExit();
            InputStream input = null;
            OutputStream output = null;
            try {
                input = this.pluginArtifactZip.getInputStream(zipEntry);
                output = new FileOutputStream(tmpFile);
                IOUtils.copy(input, output);
                output.flush();
                IOUtils.closeQuietly(output);
                tmpFile.renameTo(resourceFile);
            } catch (IOException e) {
                throw e;
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
            }
        }
        return resourceFile.toURI().toURL();
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        InputStream inputStream = null;
        try {
            inputStream = findClassContent(name);
            if (inputStream == null) {
                return super.findClass(name);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, baos);
            byte[] bytes = baos.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Searches the plugin artifact ZIP and all dependency ZIPs for a zip entry for
     * the given fully qualified class name.
     * @param className name of class
     * @throws IOException if an I/O error has occurred
     */
    protected InputStream findClassContent(String className) throws IOException {
        String primaryArtifactEntryName = "WEB-INF/classes/" + className.replace('.', '/') + ".class";
        String dependencyEntryName = className.replace('.', '/') + ".class";
        ZipEntry entry = this.pluginArtifactZip.getEntry(primaryArtifactEntryName);
        if (entry != null) {
            return this.pluginArtifactZip.getInputStream(entry);
        }
        for (ZipFile zipFile : this.dependencyZips) {
            entry = zipFile.getEntry(dependencyEntryName);
            if (entry != null) {
                return zipFile.getInputStream(entry);
            }
        }
        return null;
    }

    /**
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    @Override
    protected URL findResource(String name) {
        Enumeration<? extends ZipEntry> entries = this.pluginArtifactZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().equalsIgnoreCase(name) || zipEntry.getName().equalsIgnoreCase("WEB-INF/classes/" + name)) {
                try {
                    return extractResource(zipEntry);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        ZipEntry entry;
        File file;
        for (ZipFile zipFile : this.dependencyZips) {
            entry = zipFile.getEntry(name);
            if (entry != null) {
                try {
                    file = new File(zipFile.getName());
                    URL zipUrl = file.toURI().toURL();
                    URL entryUrl = new URL("jar:" + zipUrl + "!/" + name);
                    return entryUrl;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return super.findResource(name);
    }

    /**
     * @see java.lang.ClassLoader#findResources(java.lang.String)
     */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        List<URL> resources = new ArrayList<>();

        // Search for the artifact in the plugin WAR itself
        Enumeration<? extends ZipEntry> entries = this.pluginArtifactZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().equalsIgnoreCase(name)) {
                try {
                    resources.add(extractResource(zipEntry));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Now also add any resources found in dependencies
        ZipEntry entry;
        File file;
        for (ZipFile zipFile : this.dependencyZips) {
            entry = zipFile.getEntry(name);
            if (entry != null) {
                try {
                    file = new File(zipFile.getName());
                    URL zipUrl = file.toURI().toURL();
                    URL entryUrl = new URL("jar:" + zipUrl + "!/" + name);
                    resources.add(entryUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Return the discovered resources as an Enumeration
        final Iterator<URL> iterator = resources.iterator();
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    /**
     * Closes any resources the plugin classloader is holding open.
     * @throws IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (closed) { return; }
        this.pluginArtifactZip.close();
        for (ZipFile zipFile : this.dependencyZips) {
            zipFile.close();
        }
        closed = true;
    }

    /**
     * @return gets any policy definition resources from the plugin artifact (located in META-INF/apiman/policyDefs/*.json
     */
    public List<URL> getPolicyDefinitionResources() {
        List<URL> resources = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = this.pluginArtifactZip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().toLowerCase().startsWith("meta-inf/apiman/policydefs/") && zipEntry.getName().toLowerCase().endsWith(".json")) {
                try {
                    resources.add(extractResource(zipEntry));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return resources;
    }
}
