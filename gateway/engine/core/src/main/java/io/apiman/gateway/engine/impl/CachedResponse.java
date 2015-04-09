package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.io.IApimanBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * This class aims to embody all the data related to a service response. The instance will contain 
 * the response headers, code, message and body. The body will be persisted as a file in a temporary
 * folder
 * 
 * @author rubenrm1@gmail.com
 *
 */
public class CachedResponse {

    private static final Path tmpDir;
    
    static {
        try {
            tmpDir = Files.createTempDirectory(null);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    private String id;
    private Map<String, String> headers;
    private int code;
    private String message;
    private boolean writeFailed = false;
    
    private File tmpFile;
    private FileOutputStream fileOS;
    private FileInputStream fileIS;

    public CachedResponse(String id) {
        this.id = id;
        tmpFile = new File(tmpDir.toFile(), "response" + id.hashCode() + ".apiman"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public String getId() {
        return id;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = new HashMap<>(headers.size());
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(), entry.getValue());
        }
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setCode(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void write(IApimanBuffer chunk) {
        if (fileOS == null) {
            try {
                fileOS = new FileOutputStream(tmpFile);
                fileOS.write(chunk.getBytes());
            } catch (IOException e) {
                // TODO: Log the error. 
                // The response will not be cached as it might not contain a valid body
                writeFailed = true;
            }
        }
    }
    
    public void endWrite() {
        IOUtils.closeQuietly(fileOS);
        fileOS = null;
    }
    
    public InputStream getInputStream() throws IOException {
        fileIS = new FileInputStream(tmpFile);
        return fileIS;
    }
    
    public boolean isWriteFailed() {
        return writeFailed;
    }
    
}
