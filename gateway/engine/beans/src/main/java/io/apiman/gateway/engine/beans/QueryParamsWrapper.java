package io.apiman.gateway.engine.beans;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryParamsWrapper implements Map<String, String> {

    private ApiRequest apiRequest;
    
    public QueryParamsWrapper(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is read-only. Please use getQueryParameters().");
    }

    @Override
    public boolean containsKey(Object arg0) {
        return apiRequest.getQueryParameters().containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object arg0) {
        //too difficult to implement. Nobody uses this anyway.
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is deprecated. This class is just wrapping the new ApiRequest.getQueryParameters(). This method has not been implemented. Please use getQueryParameters().");
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        Set<java.util.Map.Entry<String, String>> set = new HashSet<>();
        for (java.util.Map.Entry<String, List<String>> entry : apiRequest.getQueryParameters().entrySet()) {
            set.add(new AbstractMap.SimpleEntry<String, String>(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1)));
        }
        return set;
    }

    @Override
    public String get(Object arg0) {
        List<String> list = apiRequest.getQueryParameters().get(arg0);
        if (list == null) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    @Override
    public boolean isEmpty() {
        return apiRequest.getQueryParameters().isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return apiRequest.getQueryParameters().keySet();
    }

    @Override
    public String put(String arg0, String arg1) {
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is read-only. Please use getQueryParameters().");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> arg0) {
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is read-only. Please use getQueryParameters().");
    }

    @Override
    public String remove(Object arg0) {
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is read-only. Please use getQueryParameters().");
    }

    @Override
    public int size() {
        return apiRequest.getQueryParameters().size();
    }

    @Override
    public Collection<String> values() {
        // difficult to implement. Nobody uses this anyway.
        throw new UnsupportedOperationException("Since APIMAN-953, ApiRequest.getQueryParams() is deprecated. This class is just wrapping the new ApiRequest.getQueryParameters(). This method has not been implemented. Please use getQueryParameters().");
    }

}
