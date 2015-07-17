/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.apiman.manager.api.micro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Hard coded users.  Only useful for testing/bootstrapping.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public final class Users {
    
    private static final String USERS_FILE_PROP = "apiman.micro.users-file";

    public static final List<User> getUsers() {
        List<User> rval = new ArrayList<>();
        
        String usersFileLoc = System.getProperty(USERS_FILE_PROP);
        if (usersFileLoc == null) {
            usersFileLoc = "";
        }
        File usersFile = new File(usersFileLoc);
        if (usersFile.isFile()) {
            System.out.println("Loading users from: " + usersFileLoc);
            InputStream in = null;
            BufferedReader reader = null;
            try {
                in = new FileInputStream(usersFile);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();
                while (line != null) {
                    String [] split = line.split(",");
                    User user = new User();
                    user.setId(split[0]);
                    user.setPassword(split[1]);
                    user.getRoles().add("apiuser");
                    boolean isAdmin = "true".equals(split[2]);
                    if (isAdmin) {
                        user.getRoles().add("apiadmin");
                    }
                    rval.add(user);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(reader);
            }
        } else {
            System.out.println("Using default users.");
            User user = new User();
            user.setId("admin");
            user.setPassword("admin123!");
            user.getRoles().add("apiuser");
            user.getRoles().add("apiadmin");
            rval.add(user);
        }
        
        return rval;
    }

}
