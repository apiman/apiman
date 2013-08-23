/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman;

/**
 * This class provides a default implementation of the NameValuePair
 * interface.
 *
 */
public class DefaultNameValuePair implements NameValuePair {
	
	private String _name=null;
	private Object _value=null;
	
	/**
	 * This constructor initializes the name and value properties.
	 * 
	 * @param name The name
	 * @param value The value
	 */
	public DefaultNameValuePair(String name, Object value) {
		_name = name;
		_value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return (_name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getValue() {
		return (_value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setValue(Object value) {
		_value = value;
	}
	
}
