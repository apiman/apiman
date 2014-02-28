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
package org.overlord.apiman.dt.test.server;

/**
 * Test users.
 *
 * @author eric.wittmann@redhat.com
 */
public final class TestUsers {

    // UserName, Password, Full Name, Email
    public static final String[][] USERS = { 
        { "admin", "admin", "Admin", "admin@example.org" },
        { "admin2", "admin2", "Admin 2", "admin2@example.org" },
        { "user1", "user1", "User 1", "user1@example.org" },
        { "user2", "user2", "User 2", "user2@example.org" },
        { "user3", "user3", "User 3", "user3@example.org" },
        { "user4", "user4", "User 4", "user4@example.org" },
        { "user5", "user5", "User 5", "user5@example.org" },
        { "bwayne", "bwayne", "Bruce Wayne", "bwayne@wayne-enterprises.com" },
        { "ckent", "ckent", "Clark Kent", "ckent@dailyplanet.com" },
        { "dprince", "dprince", "Diana Prince", "dprince@themyscira.gov" }
    };

}
