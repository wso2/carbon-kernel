/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.server;

import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JCA provider for RSA-OAEP decryption with RSA keys held in PKCS#11 tokens through SunPKCS11.
 * <p>
 * SunPKCS11 offers only the PKCS#1 v1.5 and raw RSA ciphers, although tokens support {@code CKM_RSA_PKCS_OAEP}.
 * This provider adds the missing cipher ({@link PKCS11OAEPCipher}). Its service accepts only SunPKCS11 RSA private
 * keys, so JCA selects it only for those keys when no provider is named: software keys keep using SunJCE, and
 * PKCS#1 v1.5 decryption keeps using SunPKCS11.
 */
public final class PKCS11OAEPProvider extends Provider {

    public static final String NAME = "WSO2PKCS11OAEP";

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PKCS11OAEPProvider.class.getName());
    private static final String SUN_PKCS11_PREFIX = "SunPKCS11-";

    PKCS11OAEPProvider(JdkPKCS11 jdk) {

        super(NAME, "1.0", "RSA-OAEP decryption with RSA keys held in PKCS11 tokens (SunPKCS11)");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("SupportedModes", "ECB|NONE");
        attributes.put("SupportedPaddings", "OAEPPADDING|OAEPWITH(SHA-?1|SHA-?224|SHA-?256|SHA-?384|SHA-?512)" +
                "ANDMGF1PADDING");
        putService(new CipherService(this, jdk, attributes));
    }

    /**
     * Add the provider after the providers that are already registered, if the JVM gives access to the SunPKCS11
     * internals. Without that access, RSA-OAEP decryption with token keys is not available; a warning is logged
     * when a SunPKCS11 token is configured.
     *
     * @return True if the provider was added.
     */
    public static boolean register() {

        if (Security.getProvider(NAME) != null) {
            return false;
        }
        JdkPKCS11 jdk;
        try {
            jdk = JdkPKCS11.load();
        } catch (ReflectiveOperationException | RuntimeException e) {
            Level level = isTokenConfigured() ? Level.WARNING : Level.FINE;
            logger.log(level, "RSA-OAEP decryption with keys held in PKCS11 tokens is not available. Start the JVM " +
                    "with " + JdkPKCS11.ADD_OPENS_OPTIONS + " to enable it. Cause: " + e);
            return false;
        }
        int position = Security.addProvider(new PKCS11OAEPProvider(jdk));
        logger.log(Level.INFO, "Registered provider " + NAME + " (RSA-OAEP decryption with PKCS11 token keys) at " +
                "position " + position + ".");
        return true;
    }

    private static boolean isTokenConfigured() {

        for (Provider provider : Security.getProviders()) {
            if (provider.getName().startsWith(SUN_PKCS11_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private static final class CipherService extends Service {

        private final transient JdkPKCS11 jdk;

        CipherService(Provider provider, JdkPKCS11 jdk, Map<String, String> attributes) {

            super(provider, "Cipher", "RSA", PKCS11OAEPCipher.class.getName(), null, attributes);
            this.jdk = jdk;
        }

        @Override
        public boolean supportsParameter(Object parameter) {

            return parameter instanceof Key && jdk.isTokenRSAPrivateKey((Key) parameter);
        }

        @Override
        public Object newInstance(Object constructorParameter) {

            return new PKCS11OAEPCipher(jdk);
        }
    }
}
