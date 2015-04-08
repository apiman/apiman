/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Basic reflection utilities.
 * @author Marc Savy <msavy@redhat.com>
 */
public class ReflectionUtils {
    
    /**
     * Call a method if it exists. Use very sparingly and generally prefer interfaces.
     * @param object The object
     * @param methodName Method name to call on the object
     * @throws SecurityException reflection - security manager to indicate a security violation
     * @throws IllegalAccessException reflection - does not allow access
     * @throws IllegalArgumentException reflection - argument not allowed
     * @throws InvocationTargetException reflection - exception thrown by an invoked method or constructor
     */
    public static <T> void callIfExists(T object, String methodName) throws SecurityException, IllegalAccessException, 
    IllegalArgumentException, InvocationTargetException {
        try {
            Method method = object.getClass().getMethod(methodName);
            method.invoke(object);
        } catch(NoSuchMethodException e) {}
    }
}
