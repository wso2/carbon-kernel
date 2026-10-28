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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Adds the SunPKCS11 providers configured in carbon.xml ({@code [[security.pkcs11_provider]]} in deployment.toml)
 * to the JVM. It runs in the launcher, before OSGi starts, so the providers exist before key stores are loaded and
 * before Tomcat starts its HTTPS connector.
 * <p>
 * Providers are appended after the providers that are already registered, so the JDK providers stay first. A
 * provider that is already registered under the same name (for example through {@code -Djava.security.properties})
 * is kept and the carbon.xml entry is skipped.
 */
public class PKCS11ProviderRegistrar {

    private static final Logger logger = Logger.getLogger(PKCS11ProviderRegistrar.class.getName());

    static final String SUN_PKCS11 = "SunPKCS11";
    private static final String PROVIDER_NAME_PREFIX = SUN_PKCS11 + "-";
    private static final String CARBON_HOME_PLACEHOLDER = "${carbon.home}";
    private static final String PKCS11_PROVIDERS_ELEMENT = "PKCS11Providers";
    private static final String PROVIDER_ELEMENT = "Provider";
    private static final String NAME_ELEMENT = "Name";
    private static final String CONFIG_FILE_ELEMENT = "ConfigFile";

    private final Function<String, Provider> providerFactory;

    public PKCS11ProviderRegistrar() {

        this(PKCS11ProviderRegistrar::configureSunPKCS11);
    }

    /**
     * @param providerFactory Creates a configured provider from the path of a SunPKCS11 configuration file.
     */
    PKCS11ProviderRegistrar(Function<String, Provider> providerFactory) {

        this.providerFactory = providerFactory;
    }

    /**
     * Register the PKCS11 providers configured in the given carbon.xml.
     *
     * @param carbonXml  Rendered carbon.xml.
     * @param carbonHome Carbon home, used for ${carbon.home} and relative configuration file paths.
     * @return Names of the providers added to the JVM.
     * @throws IllegalStateException If the configuration is invalid or a provider can't be created.
     */
    public List<String> register(File carbonXml, String carbonHome) {

        if (!carbonXml.exists()) {
            return Collections.emptyList();
        }
        List<String> registered = new ArrayList<>();
        for (Element providerElement : getProviderElements(carbonXml)) {
            String name = getChildText(providerElement, NAME_ELEMENT);
            String configFile = getChildText(providerElement, CONFIG_FILE_ELEMENT);
            if (name.isEmpty() || configFile.isEmpty()) {
                throw new IllegalStateException("Each PKCS11 provider in " + carbonXml + " needs a name and a " +
                        "config_file.");
            }
            String providerName = PROVIDER_NAME_PREFIX + name;
            if (Security.getProvider(providerName) != null) {
                logger.log(Level.INFO, "PKCS11 provider " + providerName + " is already registered. Skipping " +
                        "the carbon.xml entry.");
                continue;
            }
            File file = resolveConfigFile(configFile, carbonHome);
            if (!file.isFile()) {
                throw new IllegalStateException("PKCS11 provider configuration file of " + providerName +
                        " not found: " + file);
            }
            Provider provider = providerFactory.apply(file.getAbsolutePath());
            if (!providerName.equals(provider.getName())) {
                throw new IllegalStateException("PKCS11 provider configuration file " + file + " creates provider " +
                        provider.getName() + ", but the configured name is " + name + ". Use the same value as " +
                        "the 'name' in the configuration file.");
            }
            int position = Security.addProvider(provider);
            logger.log(Level.INFO, "Registered PKCS11 provider " + providerName + " at position " + position + ".");
            registered.add(providerName);
        }
        return registered;
    }

    private static Provider configureSunPKCS11(String configFile) {

        Provider sunPKCS11 = Security.getProvider(SUN_PKCS11);
        if (sunPKCS11 == null) {
            throw new IllegalStateException("The " + SUN_PKCS11 + " provider is not available in this JVM.");
        }
        try {
            return sunPKCS11.configure(configFile);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error while creating the " + SUN_PKCS11 + " provider from " +
                    configFile + ".", e);
        }
    }

    private static File resolveConfigFile(String configFile, String carbonHome) {

        File file = new File(configFile.replace(CARBON_HOME_PLACEHOLDER, carbonHome));
        return file.isAbsolute() ? file : new File(carbonHome, file.getPath());
    }

    private static List<Element> getProviderElements(File carbonXml) {

        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(carbonXml);
        } catch (Exception e) {
            throw new IllegalStateException("Error while reading PKCS11 providers from " + carbonXml + ".", e);
        }
        List<Element> providers = new ArrayList<>();
        NodeList sections = document.getElementsByTagName(PKCS11_PROVIDERS_ELEMENT);
        for (int i = 0; i < sections.getLength(); i++) {
            NodeList children = sections.item(i).getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child instanceof Element && PROVIDER_ELEMENT.equals(child.getNodeName())) {
                    providers.add((Element) child);
                }
            }
        }
        return providers;
    }

    private static String getChildText(Element element, String childName) {

        NodeList children = element.getElementsByTagName(childName);
        return children.getLength() == 0 ? "" : children.item(0).getTextContent().trim();
    }
}
