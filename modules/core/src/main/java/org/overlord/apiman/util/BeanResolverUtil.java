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

import java.util.logging.Logger;

/**
 * This class provides bean resolution utility functions.
 *
 */
public class BeanResolverUtil {

    private static final Logger LOG=Logger.getLogger(BeanResolverUtil.class.getName());
    
	/**
	 * The default constructor.
	 */
	public BeanResolverUtil() {
	}

	/**
	 * This method returns a resolved object for the supplied type.
	 * 
	 * @param cls The required type
	 * @return The resolved object, or null if not found
	 */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> cls) {
        T ret=null;
        
        try {
            javax.enterprise.inject.spi.BeanManager bm=javax.naming.InitialContext.doLookup("java:comp/BeanManager");
            
            java.util.Set<javax.enterprise.inject.spi.Bean<?>> beans=bm.getBeans(cls);
            
            for (javax.enterprise.inject.spi.Bean<?> b : beans) {                
                javax.enterprise.context.spi.CreationalContext<Object> cc=new javax.enterprise.context.spi.CreationalContext<Object>() {
                    public void push(Object arg0) {
                    }
                    public void release() {
                    }                   
                };
                
                ret = (T)((javax.enterprise.inject.spi.Bean<Object>)b).create(cc);
                
                if (LOG.isLoggable(java.util.logging.Level.FINE)) {
                    LOG.fine("Resolved object="+ret+" for bean="+b);
                }
                
                if (ret != null) {
                    break;
                }
            }
        } catch (Throwable e) {
            LOG.log(java.util.logging.Level.SEVERE, "Failed to resolve object of type '"+cls+"'", e);
        }
        
        return (ret);
    }

    /**
     * This method resolves multiple objects of the required type.
     * 
     * @param cls The required type
     * @param list The result list
     */
    public static <T> void getBeans(Class<T> cls, java.util.List<T> list) {
        try {
            javax.enterprise.inject.spi.BeanManager bm=javax.naming.InitialContext.doLookup("java:comp/BeanManager");
            
            java.util.Set<javax.enterprise.inject.spi.Bean<?>> beans=bm.getBeans(cls);
            
            for (javax.enterprise.inject.spi.Bean<?> b : beans) {                
                javax.enterprise.context.spi.CreationalContext<Object> cc=new javax.enterprise.context.spi.CreationalContext<Object>() {
                    public void push(Object arg0) {
                    }
                    public void release() {
                    }                   
                };
                
                @SuppressWarnings("unchecked")
                T entry=(T)((javax.enterprise.inject.spi.Bean<Object>)b).create(cc);
                
                if (LOG.isLoggable(java.util.logging.Level.FINE)) {
                    LOG.fine("Entry="+entry+" for bean="+b);
                }
                
                list.add(entry);
            }
        } catch (Throwable e) {
            LOG.log(java.util.logging.Level.SEVERE, "Failed to resolve elements of type '"+cls+"'", e);
        }
    }
	
}
