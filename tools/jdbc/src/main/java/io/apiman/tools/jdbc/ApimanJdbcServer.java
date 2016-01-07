/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.tools.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.io.FileUtils;
import org.h2.tools.Server;

/**
 * A jdbc server used for testing various apiman policies that require
 * JDBC.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ApimanJdbcServer {


    public static void main(String[] args) {
        try {
            File dataDir = new File("target/h2");
            String url = "jdbc:h2:tcp://localhost:9092/apiman";

            if (dataDir.exists()) {
                FileUtils.deleteDirectory(dataDir);
            }
            dataDir.mkdirs();

            Server.createTcpServer("-tcpPassword", "sa", "-baseDir", dataDir.getAbsolutePath(), "-tcpPort", "9092", "-tcpAllowOthers").start();
            Class.forName("org.h2.Driver");
            Connection connection = DriverManager.getConnection(url, "sa", "");
            System.out.println("Connection Established: " + connection.getMetaData().getDatabaseProductName() + "/" + connection.getCatalog());
            connection.prepareStatement("CREATE TABLE users ( username varchar(255) NOT NULL, password varchar(255) NOT NULL, PRIMARY KEY (username))").executeUpdate();
            connection.prepareStatement("INSERT INTO users (username, password) VALUES ('bwayne', 'ae2efd698aefdf366736a4eda1bc5241f9fbfec7')").executeUpdate();
            connection.prepareStatement("INSERT INTO users (username, password) VALUES ('ckent', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')").executeUpdate();
            connection.prepareStatement("INSERT INTO users (username, password) VALUES ('ballen', 'ea59f7ca52a2087c99374caba0ff29be1b2dcdbf')").executeUpdate();
            connection.prepareStatement("CREATE TABLE roles (rolename varchar(255) NOT NULL, username varchar(255) NOT NULL)").executeUpdate();
            connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('user', 'bwayne')").executeUpdate();
            connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('admin', 'bwayne')").executeUpdate();
            connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('ckent', 'user')").executeUpdate();
            connection.prepareStatement("INSERT INTO roles (rolename, username) VALUES ('ballen', 'user')").executeUpdate();
            connection.close();

            System.out.println("======================================================");
            System.out.println("JDBC (H2) server started successfully.");
            System.out.println("");
            System.out.println("  Data: " + dataDir.getAbsolutePath());
            System.out.println("  URL: " + url);
            System.out.println("  Authentication Query:   SELECT * FROM users u WHERE u.username = ? AND u.password = ?");
            System.out.println("  Authorization Query:    SELECT r.rolename FROM roles r WHERE r.username = ?");
            System.out.println("======================================================");
            System.out.println("");
            System.out.println("");
            System.out.println("Press Enter to stop the JDBC server.");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

            System.out.println("Shutting down the JDBC server...");

            Server.shutdownTcpServer("tcp://localhost:9092", "", true, true);

            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
