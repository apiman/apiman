/*
 * Copyright 2014 JBoss Inc
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

/**
 * Some simple string utils. Useful in places where we want to avoid pulling in large dependencies for small
 * tasks.
 * 
 * Where possible, handles null cases gracefully by returning the original string or null instead of
 * exceptions.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class SimpleStringUtils {

    private SimpleStringUtils() {
    }

    /**
     * Trim string of whitespace.
     * 
     * @param string string to trim
     * @return trimmed string, or null if null was provided.
     */
    public static String trim(String string) {
        return string == null ? null : string.trim();
    }

    /**
     * Join together varargs using a join sequence.
     * <p>
     * <tt>join("-", a, b, c) => a-b-c</tt>
     *
     * @param joinChar character to join string
     * @param args strings to join
     * @return joined string
     */
    public static String join(String joinChar, String... args) {
        String next = ""; //$NON-NLS-1$
        StringBuilder result = new StringBuilder(length(args) + (args.length - 1));

        for (String arg : args) {
            result.append(next);
            result.append(arg);
            next = joinChar;
        }
        return result.toString();
    }

    /**
     * Cumulative length of strings in varargs
     * 
     * @param args vararg strings
     * @return cumulative length of strings
     */
    public static int length(String... args) {
        if (args == null)
            return 0;

        int acc = 0;
        for (String arg : args) {
            acc += arg.length();
        }
        return acc;
    }
}
