package io.apiman.plugins.transformation_policy.transformer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class JsonToXmlTransformer implements DataTransformer {

    private static final String ROOT = "root"; //$NON-NLS-1$
    private static final String ELEMENT = "element"; //$NON-NLS-1$

    @Override
    public String transform(String json) {
        JSONObject jsonObject = null;
	    if (json.trim().startsWith("{")) { //$NON-NLS-1$
	        jsonObject = new JSONObject(json);
	    } else {
	        JSONArray jsonArray = new JSONArray(json);
	        jsonObject = new JSONObject().put(ELEMENT, jsonArray);
	    }
        return XML.toString(jsonObject, ROOT);
	}

}
