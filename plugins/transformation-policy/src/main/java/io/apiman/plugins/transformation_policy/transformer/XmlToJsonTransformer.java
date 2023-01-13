package io.apiman.plugins.transformation_policy.transformer;

import org.json.XML;

public class XmlToJsonTransformer implements DataTransformer {

	public String transform(String xml) {
		return XML.toJSONObject(xml).toString();
	}
	
}
