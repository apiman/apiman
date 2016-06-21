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

    private ReflectionUtils() {
    }

    /**
     * Call a method if it exists. Use very sparingly and generally prefer interfaces.
     * @param object The object
     * @param methodName Method name to call on the object
     * @throws SecurityException reflection - security manager to indicate a security violation
     * @throws IllegalAccessException reflection - does not allow access
     * @throws IllegalArgumentException reflection - argument not allowed
     * @throws InvocationTargetException reflection - exception thrown by an invoked method or constructor
     */
    public static <T> void callIfExists(T object, String methodName) throws SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            Method method = object.getClass().getMethod(methodName);
            method.invoke(object);
        } catch (NoSuchMethodException e) {
        }
    }

    /**
     * Loads a class.
     * @param classname
     */
    public static Class<?> loadClass(String classname) {
        Class<?> c = null;

        // First try a simple Class.forName()
        try { c = Class.forName(classname); } catch (ClassNotFoundException e) { }
        // Didn't work?  Try using this class's classloader.
        if (c == null) {
            try { c = ReflectionUtils.class.getClassLoader().loadClass(classname); } catch (ClassNotFoundException e) { }
        }
        // Still didn't work?  Try the thread's context classloader.
        if (c == null) {
            try { c = Thread.currentThread().getContextClassLoader().loadClass(classname); } catch (ClassNotFoundException e) { }
        }

        return c;
    }

    /**
     * Squishy way to find a setter method.
     * @param onClass, targetClass
     */
    public static Method findSetter(Class<?> onClass, Class<?> targetClass) {
        Method[] methods = onClass.getMethods();
        for (Method method : methods) {
            Class<?>[] ptypes = method.getParameterTypes();
            if (method.getName().startsWith("set") && ptypes.length == 1 && ptypes[0] == targetClass) { //$NON-NLS-1$
                return method;
            }
        }
        return null;
    }
}
