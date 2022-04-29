/*
 * Copyright 2022 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.search.searchResults;

/**
 * Models the result data of a search.
 * Used to hide sensitive data in the search result such as emails
 */
public class UserSearchResult {

    private String username;
    private String fullName;

    /**
     * Constructor
     *
     * @param username the username to set
     * @param fullName the fullName to set
     */
    public UserSearchResult(String username, String fullName) {
        this.username = username;
        this.fullName = fullName;
    }

    /**
     * Get the username
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the fullName
     *
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Set the fullName
     *
     * @param fullName the fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
