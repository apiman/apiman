package io.apiman.plugins.jsonp_policy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MediaType {

	private static final Pattern pattern = Pattern.compile("");
	private final String str;
	
	public MediaType(String str) {
		this.str = str;
	}

	public String getTypeSubtype() {
		return null;
	}

	public Map<String, String> getParameters() {
		return null;
	}
	
	@Override
	public String toString() {
		return str;
	}
	
}
