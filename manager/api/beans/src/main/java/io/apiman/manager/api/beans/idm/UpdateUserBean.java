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

package io.apiman.manager.api.beans.idm;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * The bean used when updating a user.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateUserBean implements Serializable {

    private static final long serialVersionUID = 7773886494093983234L;

    private String fullName;
    private String email;
    private Locale locale;

    /**
     * Constructor.
     */
    public UpdateUserBean() {
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the preferred locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * This should be a valid IANA language subtag (e.g. en, en-GB, de, etc).
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Locale.html">Java Locale</a>
     * @see <a href="https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry">IANA subtag registry</a>
     * @param locale the user's preferred locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UpdateUserBean.class.getSimpleName() + "[", "]")
                .add("fullName='" + fullName + "'")
                .add("email='" + email + "'")
                .add("locale='" + locale + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateUserBean that = (UpdateUserBean) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(email, that.email) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, email, locale);
    }
}
