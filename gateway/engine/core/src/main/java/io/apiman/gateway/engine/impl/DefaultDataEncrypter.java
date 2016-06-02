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
package io.apiman.gateway.engine.impl;

import io.apiman.common.util.AesEncrypter;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;

/**
 * A default data encrypter for the gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultDataEncrypter implements IDataEncrypter {

    /**
     * Constructor.
     */
    public DefaultDataEncrypter() {
    }

    /**
     * @see io.apiman.common.util.crypt.IDataEncrypter#encrypt(java.lang.String, io.apiman.common.util.crypt.DataEncryptionContext)
     */
    @Override
    public String encrypt(String plainText, DataEncryptionContext context) {
        return AesEncrypter.encrypt(plainText);
    }

    /**
     * @see io.apiman.common.util.crypt.IDataEncrypter#decrypt(java.lang.String, io.apiman.common.util.crypt.DataEncryptionContext)
     */
    @Override
    public String decrypt(String encryptedText, DataEncryptionContext context) {
        return AesEncrypter.decrypt(encryptedText);
    }
}
