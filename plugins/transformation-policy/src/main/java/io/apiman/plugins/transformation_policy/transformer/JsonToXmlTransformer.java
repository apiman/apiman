package io.apiman.plugins.transformation_policy.transformer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class JsonToXmlTransformer implements DataTransformer {

    private static final String ELEMENT = "element"; //$NON-NLS-1$

    @Override
    public String transform(String json) {
	String ROOT = null;
        JSONObject jsonObject = null;
	    if (json.trim().startsWith("{")) { //$NON-NLS-1$
	        jsonObject = new JSONObject(json);
	        if (jsonObject.length() > 1) {
	            ROOT = "root"; //$NON-NLS-1$
	        }
	    } else {
	        JSONArray jsonArray = new JSONArray(json);
	        jsonObject = new JSONObject().put(ELEMENT, jsonArray);
	        ROOT = "root";  //$NON-NLS-1$
	    }
        return XML.toString(jsonObject, ROOT);
	}

}
