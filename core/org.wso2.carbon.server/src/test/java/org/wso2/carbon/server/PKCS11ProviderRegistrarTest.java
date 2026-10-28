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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Tests for registering the PKCS11 providers configured in carbon.xml. A test factory stands in for SunPKCS11, so no
 * PKCS11 library is needed.
 */
public class PKCS11ProviderRegistrarTest {

    private File carbonHome;
    private final List<String> factoryCalls = new ArrayList<>();
    private final Function<String, Provider> testFactory = configFile -> {
        factoryCalls.add(configFile);
        // The provider name follows the 'name' line of the configuration file, like SunPKCS11.
        return new TestProvider("SunPKCS11-" + readName(configFile));
    };

    @BeforeMethod
    public void setUp() throws IOException {

        carbonHome = Files.createTempDirectory("pkcs11-registrar").toFile();
        factoryCalls.clear();
    }

    @AfterMethod
    public void tearDown() {

        for (Provider provider : Security.getProviders()) {
            if (provider instanceof TestProvider) {
                Security.removeProvider(provider.getName());
            }
        }
        deleteRecursively(carbonHome);
    }

    @Test
    public void testProvidersAreAppendedInOrder() throws IOException {

        writeConfig("token-a");
        writeConfig("token-b");
        File carbonXml = writeCarbonXml(providerEntry("token-a", "${carbon.home}/token-a.cfg"),
                providerEntry("token-b", "token-b.cfg"));
        int providersBefore = Security.getProviders().length;
        String firstProvider = Security.getProviders()[0].getName();

        List<String> registered = new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath());

        assertEquals(registered, Arrays.asList("SunPKCS11-token-a", "SunPKCS11-token-b"));
        Provider[] providers = Security.getProviders();
        assertEquals(providers.length, providersBefore + 2);
        assertEquals(providers[0].getName(), firstProvider, "JDK providers must stay first.");
        assertEquals(providers[providersBefore].getName(), "SunPKCS11-token-a");
        assertEquals(providers[providersBefore + 1].getName(), "SunPKCS11-token-b");
        // ${carbon.home} and relative paths both resolve against carbon home.
        assertEquals(factoryCalls, Arrays.asList(new File(carbonHome, "token-a.cfg").getAbsolutePath(),
                new File(carbonHome, "token-b.cfg").getAbsolutePath()));
    }

    @Test
    public void testAlreadyRegisteredProviderIsKept() throws IOException {

        Provider existing = new TestProvider("SunPKCS11-token-a");
        Security.addProvider(existing);
        File carbonXml = writeCarbonXml(providerEntry("token-a", "${carbon.home}/missing.cfg"));

        List<String> registered = new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath());

        assertTrue(registered.isEmpty());
        assertTrue(factoryCalls.isEmpty());
        assertSame(Security.getProvider("SunPKCS11-token-a"), existing);
    }

    @Test
    public void testNameMustMatchConfigurationFile() throws IOException {

        writeConfig("token-a");
        File carbonXml = writeCarbonXml(providerEntry("token-x", "${carbon.home}/token-a.cfg"));

        IllegalStateException e = expectThrows(IllegalStateException.class,
                () -> new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath()));

        assertTrue(e.getMessage().contains("SunPKCS11-token-a"), e.getMessage());
        assertNull(Security.getProvider("SunPKCS11-token-a"));
    }

    @Test
    public void testMissingConfigurationFileFails() throws IOException {

        File carbonXml = writeCarbonXml(providerEntry("token-a", "${carbon.home}/missing.cfg"));

        expectThrows(IllegalStateException.class,
                () -> new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath()));
        assertTrue(factoryCalls.isEmpty());
    }

    @Test
    public void testMissingNameFails() throws IOException {

        File carbonXml = writeCarbonXml("<Provider><ConfigFile>token-a.cfg</ConfigFile></Provider>");

        expectThrows(IllegalStateException.class,
                () -> new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath()));
    }

    @Test
    public void testNoProvidersConfigured() throws IOException {

        File carbonXml = new File(carbonHome, "carbon.xml");
        Files.write(carbonXml.toPath(), ("<Server xmlns=\"http://wso2.org/projects/carbon/carbon.xml\"><Security>" +
                "<KeyStore><Provider>SunPKCS11-token-a</Provider></KeyStore></Security></Server>")
                .getBytes(StandardCharsets.UTF_8));
        int providersBefore = Security.getProviders().length;

        assertEquals(new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath()),
                Collections.emptyList());
        assertEquals(new PKCS11ProviderRegistrar(testFactory).register(new File(carbonHome, "none.xml"),
                carbonHome.getPath()), Collections.emptyList());
        assertEquals(Security.getProviders().length, providersBefore);
        assertTrue(factoryCalls.isEmpty());
    }

    @Test
    public void testDoctypeIsRejected() throws IOException {

        File carbonXml = new File(carbonHome, "carbon.xml");
        Files.write(carbonXml.toPath(), ("<!DOCTYPE Server [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>" +
                "<Server><Security><PKCS11Providers><Provider><Name>&x;</Name><ConfigFile>a.cfg</ConfigFile>" +
                "</Provider></PKCS11Providers></Security></Server>").getBytes(StandardCharsets.UTF_8));

        expectThrows(IllegalStateException.class,
                () -> new PKCS11ProviderRegistrar(testFactory).register(carbonXml, carbonHome.getPath()));
    }

    private static String providerEntry(String name, String configFile) {

        return "<Provider><Name>" + name + "</Name><ConfigFile>" + configFile + "</ConfigFile></Provider>";
    }

    private File writeCarbonXml(String... providerEntries) throws IOException {

        File carbonXml = new File(carbonHome, "carbon.xml");
        Files.write(carbonXml.toPath(), ("<Server xmlns=\"http://wso2.org/projects/carbon/carbon.xml\"><Security>" +
                "<PKCS11Providers>" + String.join("", providerEntries) + "</PKCS11Providers>" +
                "<KeyStore><Provider>ignored</Provider></KeyStore></Security></Server>")
                .getBytes(StandardCharsets.UTF_8));
        return carbonXml;
    }

    private void writeConfig(String name) throws IOException {

        Files.write(new File(carbonHome, name + ".cfg").toPath(),
                ("name = " + name + "\nlibrary = /no/such/library.so\n").getBytes(StandardCharsets.UTF_8));
    }

    private static String readName(String configFile) {

        try {
            for (String line : Files.readAllLines(new File(configFile).toPath(), StandardCharsets.UTF_8)) {
                if (line.startsWith("name")) {
                    return line.substring(line.indexOf('=') + 1).trim();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException("No name in " + configFile);
    }

    private static void deleteRecursively(File file) {

        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    private static final class TestProvider extends Provider {

        private static final long serialVersionUID = 1L;

        TestProvider(String name) {

            super(name, "1.0", "Test stand-in for a configured SunPKCS11 provider");
        }
    }
}
