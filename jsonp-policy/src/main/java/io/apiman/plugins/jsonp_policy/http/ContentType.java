package io.apiman.plugins.jsonp_policy.http;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A value object that represents the Content-Type header.
 * 
 * @author Alexandre Kieling <alex.kieling@gmail.com>
 */
public class ContentType {

    private static final Pattern DELIMITING_PATTERN = Pattern.compile(";");
    private static final Pattern TYPE_SUBTYPE_PATTERN = Pattern.compile("([^/]+/[^/]+)");
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("([^=]+)=([^=]+)");
    private String typeSubtype;
    private SortedMap<String, String> parameters;

    /**
     * Constructor that takes a Content-Type string.
     * 
     * @param str Content-Type string
     */
    public ContentType(String str) {
        String[] parts = DELIMITING_PATTERN.split(str);
        Matcher matcher = TYPE_SUBTYPE_PATTERN.matcher(parts[0]);

        if (matcher.find()) {
            typeSubtype = matcher.group(1).trim();
        }

        if (parts.length > 1) {
            parameters = new TreeMap<String, String>();
            for (int i = 1; i < parts.length; i++) {
                matcher = PARAMETER_PATTERN.matcher(parts[i]);
                if (matcher.find()) {
                    String key = matcher.group(1).trim();
                    String value = matcher.group(2).trim();
                    parameters.put(key, value);
                }
            }
        }
    }

    /**
     * Return the type/subtype value.
     * 
     * @return type/subtype
     */
    public String getTypeSubtype() {
        return typeSubtype;
    }

    /**
     * Set the type/subtype value.
     * 
     * @param typeSubtype type/subtype
     */
    public void setTypeSubtype(String typeSubtype) {
        this.typeSubtype = typeSubtype;
    }

    /**
     * Return the charset parameter value.
     * 
     * @return charset
     */
    public String getCharset() {
        return parameters != null ? parameters.get("charset") : null;
    }

    /**
     * Generates string representation of this content type which can be used as the value of a Content-Type header.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeSubtype);
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append("; ").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

}
