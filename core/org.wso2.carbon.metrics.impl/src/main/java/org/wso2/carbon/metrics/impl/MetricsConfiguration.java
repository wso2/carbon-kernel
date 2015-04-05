/*
 * Copyright 2014 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

/**
 * Global Configuration Reader for Metrics
 */
public class MetricsConfiguration {

    private static final Log log = LogFactory.getLog(MetricsConfiguration.class);

    private static final MetricsConfiguration INSTANCE = new MetricsConfiguration();

    private Map<String, List<String>> configurationMap = new ConcurrentHashMap<String, List<String>>();

    private SecretResolver secretResolver;

    /**
     * Flag to check whether the configuration was initialized
     */
    private boolean initialized;

    /**
     * The regex pattern to identify a system property
     */
    private static final Pattern SYSTEM_PROPERTY_PATTERN;

    static {
        SYSTEM_PROPERTY_PATTERN = Pattern.compile("\\$\\{([\\w\\.]*)\\}");
    }

    public static MetricsConfiguration getInstance() {
        return INSTANCE;
    }

    private MetricsConfiguration() {
    }

    /**
     * Load Metrics related configurations by reading an XML file.
     *
     * @param filePath Path of the XML file
     * @throws MetricsConfigException If an error occurs while reading the XML configuration
     */
    public void load(String filePath) throws MetricsConfigException {
        if (initialized) {
            return;
        }
        InputStream in = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Loading Metrics Configuration from %s", filePath));
            }
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
        } catch (IOException e) {
            throw new MetricsConfigException("I/O error while reading the configuration file: " + filePath, e);
        } catch (XMLStreamException e) {
            throw new MetricsConfigException("Error while parsing the configuration file: " + filePath, e);
        } catch (OMException e) {
            throw new MetricsConfigException("Error while parsing the configuration file: " + filePath, e);
        } catch (Exception e) {
            throw new MetricsConfigException("Unexpected error occurred while parsing configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String getFirstProperty(String key) {
        List<String> value = configurationMap.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    public List<String> getProperty(String key) {
        return configurationMap.get(key);
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {
        for (Iterator<?> childElements = serverConfig.getChildElements(); childElements.hasNext();) {
            OMElement element = (OMElement) childElements.next();
            String localName = element.getLocalName();
            nameStack.push(localName);
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                String value = element.getText();
                if (secretResolver.isInitialized() && secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                addToConfiguration(key, replaceSystemProperties(value));
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && text.trim().length() > 0;
    }

    private void addToConfiguration(String key, String value) {
        if (log.isTraceEnabled()) {
            log.trace(String.format("Adding configuration '%s' with value '%s'", key, value));
        }
        List<String> list = configurationMap.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configurationMap.put(key, list);
        } else {
            list.add(value);
        }
    }

    public static String replaceSystemProperties(String text) {
        Matcher matcher = SYSTEM_PROPERTY_PATTERN.matcher(text);
        boolean found = matcher.find();
        if (!found) {
            return text;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String name = matcher.group(1);
            String value = System.getProperty(name);
            if (value != null) {
                matcher.appendReplacement(sb, value);
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        String replaced = sb.toString();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Replaced. Old: '%s', New: '%s'", text, replaced));
        }
        return replaced;
    }
}
