package io.apiman.common.util;

import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("nls")
public class Basic {

    public static String encode(String username, String password) {
        String up = username + ':' + password;
        StringBuilder builder = new StringBuilder();
        builder.append("BASIC ");
        builder.append(Base64.encodeBase64String(up.getBytes()));
        return builder.toString();
    }
}
