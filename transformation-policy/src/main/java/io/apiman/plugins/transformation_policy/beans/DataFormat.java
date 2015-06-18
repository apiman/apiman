package io.apiman.plugins.transformation_policy.beans;

public enum DataFormat {

    XML("application/xml"),
    JSON("application/json");
    
    private final String contentType;
    
    private DataFormat(String contentType) {
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }
    
}
