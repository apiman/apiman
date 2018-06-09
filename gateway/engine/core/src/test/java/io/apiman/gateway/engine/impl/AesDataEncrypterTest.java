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
package io.apiman.gateway.engine.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.apiman.common.util.crypt.AesDataEncrypter;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 *
 * @author Rachel Yordán <ryordan@redhat.com>
 */
@SuppressWarnings({ "nls" })
public class AesDataEncrypterTest {

    private static final DataEncryptionContext encryptionCtx = new DataEncryptionContext();

    @Test
    public void dataEncrypterWithKey() {
        Map<String, String> config = new HashMap<>();
        config.put("secretKey", "a2a0aa80-84Zd2a6");
        IDataEncrypter dataEncrypter = new AesDataEncrypter(config);

        String result = dataEncrypter.encrypt("Hello, world.", encryptionCtx);
        assertNotNull(result);
        assertEquals("$CRYPT::XtwdsXC3Tv6vlQXQQPrxdg==", result);

        result = dataEncrypter.decrypt(result, encryptionCtx);
        assertEquals("Hello, world.", result);
    }

    @Test
    public void dataEncrypterWithDifferentKey() {
        Map<String, String> config = new HashMap<>();
        config.put("secretKey", "H2a9a780-m4Zd2a0");
        IDataEncrypter dataEncrypter = new AesDataEncrypter(config);

        String result = dataEncrypter.encrypt("Hello, world.", encryptionCtx);
        assertNotNull(result);

        assertEquals("$CRYPT::dLklbimUARc6EfsrxpSG2Q==", result);

        result = dataEncrypter.decrypt(result, encryptionCtx);
        assertEquals("Hello, world.", result);
    }


    @Test(expected = RuntimeException.class)
    public void dataEncrypterWithoutKey() {
        Map<String, String> config = new HashMap<>();
        IDataEncrypter dataEncrypter = new AesDataEncrypter(config);

        dataEncrypter.encrypt("Hello, world.", encryptionCtx);
    }

}
