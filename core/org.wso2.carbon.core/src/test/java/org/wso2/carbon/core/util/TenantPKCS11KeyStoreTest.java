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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.util;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests for {@link TenantPKCS11KeyStore}: the descriptor, the binding of the descriptor to the tenant, the read-only
 * single-entry view over a shared token and deleting a tenant's token entry.
 */
public class TenantPKCS11KeyStoreTest {

    private static final String PROVIDER_NAME = "TenantPKCS11Test";
    private static final char[] TOKEN_PASSWORD = "tokenpin".toCharArray();
    private static final char[] PIN = "1234".toCharArray();
    private static final char[] WRONG_PIN = "0000".toCharArray();
    private static final int TENANT_A_ID = 1;
    private static final int TENANT_B_ID = 2;
    private static final String TENANT_A_LABEL = "wso2-tenant-1";
    private static final String TENANT_B_LABEL = "wso2-tenant-2";
    private static final String TENANT_A_DOMAIN = "a.com";

    private InMemoryTokenProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {

        // Token with the key pairs of two tenants.
        try (InputStream in = new FileInputStream(Paths.get(System.getProperty("user.dir"), "src", "test",
                "resources", "security", "pkcs11-token-test.p12").toFile())) {
            provider = new InMemoryTokenProvider(PROVIDER_NAME, InMemoryTokenProvider.loadPKCS12(in, TOKEN_PASSWORD),
                    TOKEN_PASSWORD, PIN);
        }
        Security.addProvider(provider);
    }

    @AfterMethod
    public void tearDown() {

        Security.removeProvider(PROVIDER_NAME);
    }

    @Test
    public void testDescriptorRoundTrip() throws Exception {

        assertEquals(TenantPKCS11KeyStore.labelFor(TENANT_A_ID), TENANT_A_LABEL);
        byte[] descriptor = TenantPKCS11KeyStore.createDescriptor(TENANT_A_LABEL);
        assertEquals(TenantPKCS11KeyStore.getLabel(descriptor), TENANT_A_LABEL);
        expectThrows(IllegalArgumentException.class, () -> TenantPKCS11KeyStore.createDescriptor(" "));
        expectThrows(IllegalArgumentException.class, () -> TenantPKCS11KeyStore.createDescriptor(null));
        expectThrows(IllegalArgumentException.class, () -> TenantPKCS11KeyStore.createDescriptor("a\\b"));
        expectThrows(IllegalArgumentException.class, () -> TenantPKCS11KeyStore.createDescriptor("a=b"));
        expectThrows(IllegalArgumentException.class, () -> TenantPKCS11KeyStore.createDescriptor("t\u00e4"));
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.getLabel(new byte[0]));
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.getLabel("other=x".getBytes()));
    }

    @Test
    public void testViewExposesOnlyTheTenantEntry() throws Exception {

        KeyStore view = loadTenantAView();
        KeyStore token = provider.getToken();

        assertEquals(Collections.list(view.aliases()), Collections.singletonList(TENANT_A_DOMAIN));
        assertEquals(view.size(), 1);
        assertTrue(view.isKeyEntry(TENANT_A_DOMAIN));
        assertFalse(view.isCertificateEntry(TENANT_A_DOMAIN));
        assertTrue(view.containsAlias(TENANT_A_DOMAIN));

        // The tenant's key and certificate, read through the alias the tenant code uses.
        Key key = view.getKey(TENANT_A_DOMAIN, null);
        assertTrue(key instanceof PrivateKey);
        assertEquals(key, token.getKey(TENANT_A_LABEL, TOKEN_PASSWORD));
        Certificate certificate = view.getCertificate(TENANT_A_DOMAIN);
        assertEquals(certificate, token.getCertificate(TENANT_A_LABEL));
        assertEquals(view.getCertificateChain(TENANT_A_DOMAIN).length, 1);
        assertEquals(view.getCertificateAlias(certificate), TENANT_A_DOMAIN);

        // Nothing of the other tenant is reachable through the view.
        assertFalse(view.containsAlias(TENANT_B_LABEL));
        assertNull(view.getKey(TENANT_B_LABEL, null));
        assertNull(view.getCertificate(TENANT_B_LABEL));
        assertNull(view.getCertificateAlias(token.getCertificate(TENANT_B_LABEL)));

        // Signers bound to the view's provider use the token.
        assertSame(view.getProvider(), provider);
        assertEquals(view.getType(), "PKCS11");
    }

    @Test
    public void testViewIsReadOnly() throws Exception {

        KeyStore view = loadTenantAView();
        Key key = view.getKey(TENANT_A_DOMAIN, null);
        Certificate[] chain = view.getCertificateChain(TENANT_A_DOMAIN);

        expectThrows(KeyStoreException.class, () -> view.setKeyEntry("x", key, null, chain));
        expectThrows(KeyStoreException.class, () -> view.setCertificateEntry("x", chain[0]));
        expectThrows(KeyStoreException.class, () -> view.deleteEntry(TENANT_A_DOMAIN));
        expectThrows(IOException.class, () -> view.store(new ByteArrayOutputStream(), PIN));
        assertTrue(provider.getToken().containsAlias(TENANT_A_LABEL));
    }

    @Test
    public void testDescriptorOfAnotherTenantIsRefused() {

        // A record of tenant A that names tenant B's entry can neither load nor delete it.
        byte[] tenantBDescriptor = TenantPKCS11KeyStore.createDescriptor(TENANT_B_LABEL);
        KeyStoreException loadError = expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(
                PROVIDER_NAME, PIN, tenantBDescriptor, TENANT_A_ID, TENANT_A_DOMAIN));
        assertTrue(loadError.getMessage().contains("does not belong to the tenant"));
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.deleteEntry(PROVIDER_NAME, PIN,
                tenantBDescriptor, TENANT_A_ID));
        assertTrue(containsTokenAlias(TENANT_B_LABEL));

        byte[] otherLabel = TenantPKCS11KeyStore.createDescriptor("my-key");
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(PROVIDER_NAME, PIN, otherLabel,
                TENANT_A_ID, TENANT_A_DOMAIN));
    }

    @Test
    public void testPinIsCheckedOnlyAtLogin() throws Exception {

        // Like SunPKCS11, a wrong PIN fails only while the token is not logged in. The PIN is therefore no access
        // check between tenants. (After the first load, the shared token key store is used without a new login.)
        byte[] descriptor = TenantPKCS11KeyStore.createDescriptor(TENANT_A_LABEL);
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(PROVIDER_NAME, WRONG_PIN,
                descriptor, TENANT_A_ID, TENANT_A_DOMAIN));
        assertFalse(provider.isLoggedIn());
        loadTenantAView();
        assertTrue(provider.isLoggedIn());
        assertTrue(TenantPKCS11KeyStore.load(PROVIDER_NAME, WRONG_PIN, descriptor, TENANT_A_ID, TENANT_A_DOMAIN)
                .isKeyEntry(TENANT_A_DOMAIN));
    }

    @Test
    public void testLoadFailures() {

        int missingTenantId = 404;
        byte[] missing = TenantPKCS11KeyStore.createDescriptor(TenantPKCS11KeyStore.labelFor(missingTenantId));
        KeyStoreException e = expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(PROVIDER_NAME,
                PIN, missing, missingTenantId, TENANT_A_DOMAIN));
        assertTrue(e.getMessage().contains("No key entry with label 'wso2-tenant-404'"));
        byte[] descriptor = TenantPKCS11KeyStore.createDescriptor(TENANT_A_LABEL);
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(" ", PIN, descriptor, TENANT_A_ID,
                TENANT_A_DOMAIN));
        expectThrows(NoSuchProviderException.class, () -> TenantPKCS11KeyStore.load("NoSuchProvider", PIN,
                descriptor, TENANT_A_ID, TENANT_A_DOMAIN));
        expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(PROVIDER_NAME, PIN, descriptor,
                TENANT_A_ID, ""));
    }

    @Test
    public void testSharedLabelIsReported() throws Exception {

        // A label that more than one certificate on the token has: SunPKCS11 names that entry
        // <label>/<issuer>/<serial number> and refuses all changes to the token key store.
        int sharedTenantId = 3;
        String sharedLabel = TenantPKCS11KeyStore.labelFor(sharedTenantId);
        KeyStore token = provider.getToken();
        token.setKeyEntry(sharedLabel + "/cn=shared/1", token.getKey(TENANT_B_LABEL, TOKEN_PASSWORD), TOKEN_PASSWORD,
                token.getCertificateChain(TENANT_B_LABEL));

        KeyStoreException loadError = expectThrows(KeyStoreException.class, () -> TenantPKCS11KeyStore.load(
                PROVIDER_NAME, PIN, TenantPKCS11KeyStore.createDescriptor(sharedLabel), sharedTenantId,
                "c.com"));
        assertTrue(loadError.getMessage().contains("labels must be unique"));

        // Other tenants still load, but no entry can be deleted until the duplicate label is removed.
        assertTrue(loadTenantAView().isKeyEntry(TENANT_A_DOMAIN));
        KeyStoreException deleteError = expectThrows(KeyStoreException.class, () ->
                TenantPKCS11KeyStore.deleteEntry(PROVIDER_NAME, PIN,
                        TenantPKCS11KeyStore.createDescriptor(TENANT_A_LABEL), TENANT_A_ID));
        assertTrue(deleteError.getMessage().contains("not unique"));
        assertTrue(containsTokenAlias(TENANT_A_LABEL));
    }

    @Test
    public void testTokenIsLoadedOncePerProvider() throws Exception {

        loadTenantAView();
        KeyStore tenantBView = TenantPKCS11KeyStore.load(PROVIDER_NAME, PIN,
                TenantPKCS11KeyStore.createDescriptor(TENANT_B_LABEL), TENANT_B_ID, "b.com");
        loadTenantAView();

        // All tenant views share one loaded token key store, so the token is listed once.
        assertEquals(provider.getLoadCount(), 1);
        assertTrue(tenantBView.isKeyEntry("b.com"));
        assertEquals(Collections.list(tenantBView.aliases()), Collections.singletonList("b.com"));
    }

    @Test
    public void testEntryAddedAfterLoadIsFound() throws Exception {

        loadTenantAView();
        // A new tenant's key pair is added through another key store instance, after the shared one was loaded.
        int newTenantId = 3;
        String newLabel = TenantPKCS11KeyStore.labelFor(newTenantId);
        KeyStore token = provider.getToken();
        token.setKeyEntry(newLabel, token.getKey(TENANT_B_LABEL, TOKEN_PASSWORD), TOKEN_PASSWORD,
                token.getCertificateChain(TENANT_B_LABEL));
        byte[] descriptor = TenantPKCS11KeyStore.createDescriptor(newLabel);

        KeyStore view = TenantPKCS11KeyStore.load(PROVIDER_NAME, PIN, descriptor, newTenantId, "c.com");
        assertTrue(view.isKeyEntry("c.com"));
        assertEquals(provider.getLoadCount(), 2);
        // Views created before the reload still work.
        assertTrue(loadTenantAView().isKeyEntry(TENANT_A_DOMAIN));
        assertEquals(provider.getLoadCount(), 2);

        assertTrue(TenantPKCS11KeyStore.deleteEntry(PROVIDER_NAME, PIN, descriptor, newTenantId));
        assertFalse(containsTokenAlias(newLabel));
    }

    @Test
    public void testDeleteEntry() throws Exception {

        byte[] descriptor = TenantPKCS11KeyStore.createDescriptor(TENANT_B_LABEL);

        assertTrue(TenantPKCS11KeyStore.deleteEntry(PROVIDER_NAME, PIN, descriptor, TENANT_B_ID));
        assertFalse(containsTokenAlias(TENANT_B_LABEL));
        // The other tenant's entry stays.
        assertTrue(containsTokenAlias(TENANT_A_LABEL));
        assertFalse(TenantPKCS11KeyStore.deleteEntry(PROVIDER_NAME, PIN, descriptor, TENANT_B_ID));
    }

    private boolean containsTokenAlias(String alias) {

        try {
            return provider.getToken().containsAlias(alias);
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyStore loadTenantAView() throws Exception {

        return TenantPKCS11KeyStore.load(PROVIDER_NAME, PIN, TenantPKCS11KeyStore.createDescriptor(TENANT_A_LABEL),
                TENANT_A_ID, TENANT_A_DOMAIN);
    }
}
