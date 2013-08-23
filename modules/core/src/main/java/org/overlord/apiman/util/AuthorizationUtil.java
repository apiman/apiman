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
package org.overlord.apiman.util;

/**
 * This class provides utility functions for authorization.
 *
 */
public class AuthorizationUtil {
	
	/**
	 * This method returns the username and password details as a string array.
	 * 
	 * @param auth The encoded basic auth
	 * @return The username and password as a two entry string array
	 */
	public static String[] getBasicAuthUsernamePassword(String auth) throws Exception {	
		
		if (auth == null) {
			throw new Exception("No authorization information available");
		}
		
		if (!auth.startsWith("Basic ")) {
			throw new Exception("No using 'Basic' authorization");
		}
		
		String encoded=auth.substring(6);
		
		byte[] b=org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
		
		String userpassword=new String(b);
		
		String[] up=userpassword.split(":");
		
		if (up.length != 2) {
			throw new Exception("Incorrectly formatted basic authorization string");
		}
		
		return (up);
	}

}
