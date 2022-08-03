///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.passay:passay:1.6.1
//DEPS org.keycloak:keycloak-common:18.0.2
/*
 * Copyright 2022 Black Parrot Labs Ltd
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.common.util.CertificateUtils;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

/**
 * Set up docker-compose project.
 *
 * Run as `jbang setup.java`.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class setup {
    private static final CharacterRule digits = new CharacterRule(EnglishCharacterData.Digit);
    private static final CharacterRule chars = new CharacterRule(EnglishCharacterData.Alphabetical);
    private static final PasswordGenerator PW_GEN = new PasswordGenerator();
    private static final String REPLACEMENT_TEMPLATE = "REPLACE_THIS_SECRET";

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(".env").toAbsolutePath();
        StringBuilder content = new StringBuilder(Files.readString(path, StandardCharsets.UTF_8));
        System.out.println("Generating random Keycloak secrets in .env...");
        updateEnvSecrets(content);
        System.out.println("Generating random certificates and keys...");
        generateRandomCertsAndKeys(content);
        Files.write(path, content.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void generateRandomCertsAndKeys(StringBuilder content) throws IOException {
        int keySize = Optional.ofNullable(System.getenv("RSA_KEY_SIZE"))
                              .map(Integer::valueOf)
                              .orElse(2048);

        KeyPair keyPair = KeyUtils.generateRsaKeyPair(keySize);
        X509Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keyPair, "apiman");
        String privateKeyPem = PemUtils.encodeKey(keyPair.getPrivate());
        String publicKeyPem = PemUtils.encodeKey(keyPair.getPublic());
        String certPem = PemUtils.encodeCertificate(certificate);

        Files.writeString(Paths.get("data/keys/apiman-private-key.pem"), privateKeyPem, StandardOpenOption.CREATE_NEW);
        Files.writeString(Paths.get("data/keys/apiman-public-key.pem"), publicKeyPem, StandardOpenOption.CREATE_NEW);
        Files.writeString(Paths.get("data/keys/apiman-self-signed-cert.pem"), certPem, StandardOpenOption.CREATE_NEW);

        insertValue(content, "KEYCLOAK_REALM_PRIVATE_KEY=", privateKeyPem);
        insertValue(content, "KEYCLOAK_REALM_PUBLIC_KEY=", publicKeyPem);
        insertValue(content, "KEYCLOAK_REALM_CERTIFICATE=", certPem);
    }

    private static void insertValue(StringBuilder content, String key, String pem) {
        Matcher matcher = Pattern.compile(key, Pattern.MULTILINE).matcher(content);
        if (matcher.find()) {
            content.insert(matcher.end(), pem);
        } else {
            throw new IllegalArgumentException("Can't find .env key " + key);
        }
    }

    private static void updateEnvSecrets(StringBuilder content) {
        Matcher matcher = Pattern.compile(REPLACEMENT_TEMPLATE).matcher(content);
        while (matcher.find()) {
            // Make same length as password placeholder template
            String pw = PW_GEN.generatePassword(REPLACEMENT_TEMPLATE.length(), digits, chars);
            content.replace(matcher.start(), matcher.end(), pw);
        }
    }
}
