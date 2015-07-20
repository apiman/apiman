package io.apiman.plugins.transformation_policy.beans;

public enum DataFormat {

    XML("application/xml"), //$NON-NLS-1$
    JSON("application/json"); //$NON-NLS-1$

    private final String contentType;

    private DataFormat(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

}
