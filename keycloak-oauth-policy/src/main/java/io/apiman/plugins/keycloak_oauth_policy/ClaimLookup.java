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

import java.util.Map;

import org.keycloak.representations.AddressClaimSet;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

/**
* @author Marc Savy {@literal <msavy@redhat.com>}
*/
@SuppressWarnings("nls")
public class ClaimLookup {

    public static String lookupClaim(IDToken token, String key) {
        if (key == null || token == null)
            return null;

        switch(key) {
        case IDToken.NONCE:
            return token.getNonce();
        case IDToken.SESSION_STATE:
            return token.getSessionState();
        case IDToken.NAME:
            return token.getName();
        case IDToken.GIVEN_NAME:
            return token.getGivenName();
        case IDToken.FAMILY_NAME:
            return token.getFamilyName();
        case IDToken.MIDDLE_NAME:
            return token.getMiddleName();
        case IDToken.NICKNAME: //Not consistent, so we can't reliably look up with introspection.
            return token.getNickName();
        case IDToken.PREFERRED_USERNAME:
            return token.getPreferredUsername();
        case IDToken.PROFILE:
            return token.getProfile();
        case IDToken.PICTURE:
            return token.getPicture();
        case IDToken.WEBSITE:
            return token.getWebsite();
        case IDToken.EMAIL:
            return token.getEmail();
        case IDToken.EMAIL_VERIFIED:
            return token.getEmailVerified().toString();
        case IDToken.GENDER:
            return token.getGender();
        case IDToken.BIRTHDATE:
            return token.getBirthdate();
        case IDToken.ZONEINFO:
            return token.getZoneinfo();
        case IDToken.LOCALE:
            return token.getLocale();
        case IDToken.PHONE_NUMBER:
            return token.getPhoneNumber();
        case IDToken.PHONE_NUMBER_VERIFIED:
            return token.getPhoneNumber();
        case IDToken.ADDRESS: // Would be useless otherwise, i think
            return token.getAddress().getFormattedAddress().toString();
        case IDToken.ADDRESS + "." + AddressClaimSet.COUNTRY:
            return token.getAddress().getCountry();
        case IDToken.ADDRESS + "." + AddressClaimSet.FORMATTED:
            return token.getAddress().getFormattedAddress();
        case IDToken.ADDRESS + "." + AddressClaimSet.LOCALITY:
            return token.getAddress().getLocality();
        case IDToken.ADDRESS + "." + AddressClaimSet.POSTAL_CODE:
            return token.getAddress().getPostalCode();
        case IDToken.ADDRESS + "." + AddressClaimSet.REGION:
            return token.getAddress().getRegion();
        case IDToken.ADDRESS + "." + AddressClaimSet.STREET_ADDRESS:
            return token.getAddress().getStreetAddress();
        case IDToken.UPDATED_AT:
            return token.getUpdatedAt().toString();
        case IDToken.CLAIMS_LOCALES:
            return token.getClaimsLocales();
        default:
            return getOtherClaimValue(token, key).toString();
        }
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
