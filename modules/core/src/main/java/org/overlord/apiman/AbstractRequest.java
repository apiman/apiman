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

import org.overlord.apiman.AbstractMessage;
import org.overlord.apiman.Request;

/**
 * This class provides the abstract request implementation.
 *
 */
public abstract class AbstractRequest extends AbstractMessage implements Request {

	private String _serviceName=null;
	private String _serviceURI=null;
	private String _operation=null;
	private java.util.List<NameValuePair> _parameters=new java.util.ArrayList<NameValuePair>();
	
	/**
	 * {@inheritDoc}
	 */
	public Object getParameter(String name) {
		for (int i=0; i < _parameters.size(); i++) {
			NameValuePair nvp=_parameters.get(i);
			if (nvp.getName().equals(name)) {
				return (nvp.getValue());
			}
		}
		
		return (null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public java.util.List<NameValuePair> getParameters() {
		return (_parameters);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getServiceName() {
		return (_serviceName);
	}

	/**
	 * This method sets the service name.
	 * 
	 * @param serviceName The service name
	 */
	protected void setServiceName(String serviceName) {
		_serviceName = serviceName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServiceURI() {
		return (_serviceURI);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setServiceURI(String uri) {
		_serviceURI = uri;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOperation() {
		return (_operation);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOperation(String operation) {
		_operation = operation;
	}

}
