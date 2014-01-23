/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.axis2.wsdl.util;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.i18n.CodegenMessages;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class XSLTIncludeResolver implements URIResolver, Constants {

    private CodeGenConfiguration configuration;

    public static final String EMPTY_TEMPLATE = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"/>";

    public XSLTIncludeResolver() {
    }

    /** @param config  */
    public XSLTIncludeResolver(CodeGenConfiguration config) {
        this.configuration = config;
    }


    /**
     * Resolves a given href and base combination
     *
     * @param href
     * @param base
     * @throws TransformerException
     */
    public Source resolve(String href, String base) throws TransformerException {
        String templateName;
        Map externalPropertyMap = configuration.getProperties();

        if (XSLT_INCLUDE_DATABIND_SUPPORTER_HREF_KEY.equals(href)) {
            //use the language name from the configuration to search the key
            //our search only consists of looking for the data binding name
            //in the key
            Map dbSupporterMap = ConfigPropertyFileLoader.getDbSupporterTemplatesMap();
            String key;
            for (Iterator keys = dbSupporterMap.keySet().iterator(); keys.hasNext();) {
                key = (String)keys.next();
                if (key.indexOf(configuration.getDatabindingType()) != -1) {
                    return getSourceFromTemplateName((String)dbSupporterMap.get(key));
                }
            }
        }

        if (XSLT_INCLUDE_TEST_OBJECT_HREF_KEY.equals((href))) {
            return getSourceFromTemplateName(ConfigPropertyFileLoader.getTestObjectTemplateName());
        }

        if (externalPropertyMap.get(href) != null) {
            templateName = externalPropertyMap.get(href).toString();
            return getSourceFromTemplateName(templateName);
        } else if (href.startsWith("/")) {
            // This is a classpath resource
            return getSourceFromTemplateName(href);
        } else if (href.endsWith(".xsl")) {
            // This is a relative import/include. Let the processor take care of resolving it.
            return null;
        } else {
            // This is an unresolved property; return an empty source.
            return getEmptySource();
        }
    }

    /**
     * load the template from a given resource path
     *
     * @param templateName
     * @return the loaded transform source
     * @throws TransformerException
     */
    private Source getSourceFromTemplateName(String templateName) throws TransformerException {
        if (templateName != null) {
            // Use URL instead of InputStream here, so that the processor may resolve
            // imports/includes with relative hrefs.
            URL templateUrl = getClass().getResource(templateName);
            return templateUrl == null ? null : new StreamSource(templateUrl.toExternalForm());
        } else {
            throw new TransformerException(
                    CodegenMessages.getMessage("resolver.templateNotFound", templateName));
        }
    }

    /**
     * returns an empty source
     *
     * @return stream source
     */
    private Source getEmptySource() {
        return new StreamSource(new ByteArrayInputStream(EMPTY_TEMPLATE.getBytes()));
    }
}
