package io.apiman.gateway.engine.policies.config;

public class CachingResourcesSettingsEntry {

    /**
     * Used to match all possible http methods and status codes.
     */
    public final static String MATCH_ALL = "*"; //$NON-NLS-1$

    private String statusCode;
    private String pathPattern;
    private String httpMethod;

    /**
     * Constructor.
     */
    public CachingResourcesSettingsEntry(){
    }

    /**
     * @return status code setting
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * @param statusCode the status code setting to set
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return pathPattern the path pattern setting
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @param pathPattern the path pattern setting to set
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * @return httpMethod the http method setting
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @param httpMethod the http method setting to set
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        return 31 * statusCode.hashCode() + 31 * pathPattern.hashCode() + 31 * httpMethod.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CachingResourcesSettingsEntry
                && ((CachingResourcesSettingsEntry) obj).httpMethod.equals(this.httpMethod)
                && ((CachingResourcesSettingsEntry) obj).pathPattern.equals(this.pathPattern)
                && ((CachingResourcesSettingsEntry) obj).statusCode.equals(this.statusCode);
    }
}
