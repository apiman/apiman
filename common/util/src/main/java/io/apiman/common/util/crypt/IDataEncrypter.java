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
package io.apiman.common.util.crypt;

/**
 * Provides a way to encrypt and decrypt data. This is useful when encrypting sensitive
 * data prior to storing it in the database.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IDataEncrypter {

    public String encrypt(String plainText);

    public String decrypt(String encryptedText);

}
