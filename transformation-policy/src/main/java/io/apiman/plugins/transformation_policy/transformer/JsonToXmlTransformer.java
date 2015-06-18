package io.apiman.plugins.transformation_policy.transformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class JsonToXmlTransformer implements DataTransformer {

	public String transform(String json) {
		Object value;
		try {
			value = new JSONObject(json);
		} catch (JSONException e) {
			value = new JSONArray(json);
		}
		return XML.toString(new JSONObject().put("root", value));
	}
	
}
