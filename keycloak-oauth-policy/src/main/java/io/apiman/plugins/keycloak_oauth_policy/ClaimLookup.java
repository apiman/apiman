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

package io.apiman.plugins.keycloak_oauth_policy;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

/**
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
@SuppressWarnings("nls")
public class ClaimLookup {
    private static final Map<String, List<Field>> STANDARD_CLAIMS_FIELD_MAP = new LinkedHashMap<>();

    static {
        Class<?> clazz = IDToken.class;
        do {
            getProperties(clazz, "", new ArrayDeque<Field>());
        } while ((clazz = clazz.getSuperclass()) != null);
    }

    private static void getProperties(Class<?> klazz, String path, Deque<Field> fieldChain) {
        for (Field f: klazz.getDeclaredFields()) {
            f.setAccessible(true);
            JsonProperty jsonProperty = f.getAnnotation(JsonProperty.class);
            if (jsonProperty != null) {
                fieldChain.push(f);
                // If the inspected type has nested @JsonProperty annotations, we need to inspect it
                if (hasJsonPropertyAnnotation(f)) {
                    getProperties(f.getType(), f.getName() + ".", fieldChain); // Add "." when traversing into new object.
                } else { // Otherwise, just assume it's simple as the best we can do is #toString
                    List<Field> lz = new ArrayList<>(fieldChain);
                    Collections.reverse(lz);
                    STANDARD_CLAIMS_FIELD_MAP.put(path + jsonProperty.value(), lz);
                    fieldChain.pop(); // Pop, as we have now reached end of this chain.
                }
            }
        }
    }

    private static boolean hasJsonPropertyAnnotation(Field f) {
        for (Field g : f.getType().getDeclaredFields()) {
            g.setAccessible(true);
            if (g.getAnnotation(JsonProperty.class) != null)
                return true;
        }
        return false;
    }

    public static String getClaim(IDToken token, String claim) throws IllegalArgumentException, IllegalAccessException {
      if (claim == null || token == null)
          return null;
      // Get the standard claim field, if available
      if (STANDARD_CLAIMS_FIELD_MAP.containsKey(claim)) {
          return callClaimChain(token, STANDARD_CLAIMS_FIELD_MAP.get(claim));
      } else { // Otherwise look up 'other claims'
          Object otherClaim = getOtherClaimValue(token, claim);
          return otherClaim == null ? "" : (String) otherClaim;
      }
    }

    private static String callClaimChain(Object rootObject, List<Field> list) throws IllegalArgumentException, IllegalAccessException {
        if (list.size() == 1)
            return list.get(0).get(rootObject).toString();

        Object candidate = rootObject;
        for (Field f : list) {
            if ((candidate = f.get(candidate)) == null)
                break;
        }

        return (candidate == null) ? null : candidate.toString();
    }

    @SuppressWarnings("unchecked") // KC code - thanks.
    private static Object getOtherClaimValue(JsonWebToken token, String claim) {
        String[] split = claim.split("\\.");
        Map<String, Object> jsonObject = token.getOtherClaims();
        for (int i = 0; i < split.length; i++) {
            if (i == split.length - 1) {
                return jsonObject.get(split[i]);
            } else {
                Object val = jsonObject.get(split[i]);
                if (!(val instanceof Map))
                    return null;
                jsonObject = (Map<String, Object>) val;
            }
        }
        return null;
    }
}
