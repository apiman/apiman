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

package io.apiman.common.util.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

@SuppressWarnings("nls")
public class KeyStoreUtil {

    /**
     * Gets the array of key managers for a given info store+info.
     *
     * @param pathInfo
     * @throws Exception
     */
    public static KeyManager[] getKeyManagers(Info pathInfo) throws Exception {
        if (pathInfo.store == null) {
            return null;
        }
        File clientKeyStoreFile = new File(pathInfo.store);
        if (!clientKeyStoreFile.isFile()) {
            throw new Exception("No KeyManager: " + pathInfo.store + " does not exist or is not a file.");
        }
        String clientKeyStorePassword = pathInfo.password;
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance("JKS");

        FileInputStream clientFis = new FileInputStream(pathInfo.store);
        keyStore.load(clientFis, clientKeyStorePassword.toCharArray());
        clientFis.close();
        kmf.init(keyStore, clientKeyStorePassword.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Gets an array of trust managers for a given store+password.
     *
     * @param pathInfo
     * @return
     * @throws Exception
     */
    public static TrustManager[] getTrustManagers(Info pathInfo) throws Exception {
        File trustStoreFile = new File(pathInfo.store);
        if (!trustStoreFile.isFile()) {
            throw new Exception("No TrustManager: " + pathInfo.store + " does not exist.");
        }
        String trustStorePassword = pathInfo.password;
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore truststore = KeyStore.getInstance("JKS");

        FileInputStream fis = new FileInputStream(pathInfo.store);
        truststore.load(fis, trustStorePassword.toCharArray());
        fis.close();
        tmf.init(truststore);
        return tmf.getTrustManagers();
    }

    public static class Info {

        String store;
        String password;

        /**
         * Constructor.
         *
         * @param store
         * @param password
         */
        public Info(String store, String password) {
            this.store = store;
            this.password = password;
        }

    }
}