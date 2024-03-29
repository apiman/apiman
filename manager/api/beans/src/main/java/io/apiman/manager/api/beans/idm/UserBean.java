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

import io.apiman.manager.api.beans.BeanUtils.LocaleConverter;
import io.apiman.manager.api.beans.notifications.NotificationPreferenceEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Models a single user.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "users")
public class UserBean implements Serializable {

    private static final long serialVersionUID = 865765107251347714L;

    @Id
    @Column(updatable=false, nullable=false)
    private String username;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "email")
    private String email;
    @Column(name = "joined_on", updatable=false)
    private Date joinedOn;
    @Convert(converter = LocaleConverter.class)
    @Column(name = "locale")
    private Locale locale = Locale.getDefault();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationPreferenceEntity> notificationPreferences = new HashSet<>();

    // Used only when returning information about the current user
    @Transient
    private boolean admin;

    /**
     * Constructor.
     */
    public UserBean() {
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
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
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
     * @return the joinedOn
     */
    public Date getJoinedOn() {
        return joinedOn;
    }

    /**
     * @param joinedOn the joinedOn to set
     */
    public void setJoinedOn(Date joinedOn) {
        this.joinedOn = joinedOn;
    }

    public Set<NotificationPreferenceEntity> getNotificationPreferences() {
        return notificationPreferences;
    }

    public UserBean setNotificationPreferences(Set<NotificationPreferenceEntity> notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
        return this;
    }

    /**
     * @return the admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @param admin the admin to set
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Get locale language tag
     *
     * @see Locale#toLanguageTag()
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Set locale
     *
     * @param locale the locale language tag
     * @see Locale#toLanguageTag()
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserBean userBean = (UserBean) o;
        return Objects.equals(username, userBean.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserBean.class.getSimpleName() + "[", "]")
                .add("username='" + username + "'")
                .add("fullName='" + fullName + "'")
                .add("email='" + email + "'")
                .add("joinedOn=" + joinedOn)
                .add("locale='" + locale + "'")
                .add("admin=" + admin)
                .toString();
    }
}
