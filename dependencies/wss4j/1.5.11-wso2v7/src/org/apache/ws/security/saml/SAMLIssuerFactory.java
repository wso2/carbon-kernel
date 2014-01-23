/**
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

package org.apache.ws.security.saml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.util.Loader;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Properties;

/**
 * CryptoFactory.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public abstract class SAMLIssuerFactory {
    private static final Log log = LogFactory.getLog(SAMLIssuerFactory.class);
    private static final boolean doDebug = log.isDebugEnabled();
    private static final String defaultSAMLClassName =
            "org.apache.ws.security.saml.SAMLIssuerImpl";

    /**
     * getInstance
     * <p/>
     * Returns an instance of SAMLIssuer. This method uses the file
     * <code>saml.properties</code> to determine which implementation to
     * use. Thus the property <code>org.apache.ws.security.saml.issuerClass</code>
     * must define the classname of the SAMLIssuer implementation. The file
     * may contain other property definitions as well. These properties are
     * handed over to the  SAMLIssuer implementation. The file
     * <code>saml.properties</code> is loaded with the
     * <code>Loader.getResource()</code> method.
     * <p/>
     *
     * @return The SAMLIssuer implementation was defined
     */
    public static SAMLIssuer getInstance() {
        return getInstance("saml.properties");
    }

    /**
     * getInstance
     * <p/>
     * Returns an instance of SAMLIssuer. The properties are handed over the the SAMLIssuer
     * implementation. The porperties can be <code>null</code>. It is depenend on the
     * SAMLIssuer implementation how the initialization is done in this case.
     * <p/>
     *
     * @param samlClassName This is the SAMLIssuer implementation class. No default is
     *                      provided here.
     * @param properties    The Properties that are forwarded to the SAMLIssuer implementaion.
     *                      These properties are dependend on the SAMLIssuer implementatin
     * @return The SAMLIssuer implementation or null if no samlClassName was defined
     */
    public static SAMLIssuer getInstance(String samlClassName,
                                         Properties properties) {
        return loadClass(samlClassName, properties);
    }

    /**
     * getInstance
     * <p/>
     * Returns an instance of SAMLIssuer. This method uses the specifed filename
     * to load a property file. This file shall use the property
     * <code>org.apache.ws.security.saml.issuerClass</code>
     * to define the classname of the SAMLIssuer implementation. The file
     * may contain other property definitions as well. These properties are
     * handed over to the SAMLIssuer implementation. The specified file
     * is loaded with the <code>Loader.getResource()</code> method.
     * <p/>
     *
     * @param propFilename The name of the property file to load
     * @return The SAMLIssuer implementation that was defined
     */
    public static SAMLIssuer getInstance(String propFilename) {
        Properties properties = null;
        String samlClassName = null;

        if ((samlClassName == null) || (samlClassName.length() == 0)) {
            properties = getProperties(propFilename);
            samlClassName =
                    properties.getProperty("org.apache.ws.security.saml.issuerClass",
                            defaultSAMLClassName);
        }
        return loadClass(samlClassName, properties);
    }

    private static SAMLIssuer loadClass(String samlClassName,
                                        Properties properties) {
        Class samlIssuerClass = null;
        SAMLIssuer samlIssuer = null;
        try {
            // instruct the class loader to load the crypto implementation
            samlIssuerClass = Loader.loadClass(samlClassName);
        } catch (ClassNotFoundException ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex);
            }
            throw new RuntimeException(samlClassName + " Not Found", ex);
        }
        log.info("Using Crypto Engine [" + samlClassName + "]");
        try {
            Class[] classes = new Class[]{Properties.class};
            Constructor c = samlIssuerClass.getConstructor(classes);
            samlIssuer =
                    (SAMLIssuer) c.newInstance(new Object[]{properties});
            return samlIssuer;
        } catch (java.lang.Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex);
            }
        }
        try {
            // try to instantiate the Crypto subclass
            samlIssuer = (SAMLIssuer) samlIssuerClass.newInstance();
            return samlIssuer;
        } catch (java.lang.Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex);
            }
            throw new RuntimeException(samlClassName + " cannot create instance", ex);
        }
    }

    /**
     * Gets the properties for SAML issuer.
     * The functions loads the property file via
     * {@link Loader.getResource(String)}, thus the property file
     * should be accessible via the classpath
     *
     * @param propFilename the properties file to load
     * @return a <code>Properties</code> object loaded from the filename
     */
    private static Properties getProperties(String propFilename) {
        Properties properties = new Properties();
        try {
            URL url = Loader.getResource(propFilename);
            properties.load(url.openStream());
        } catch (Exception e) {
            if (doDebug) {
                log.debug("Cannot find SAML property file: " + propFilename, e);
            }
            throw new RuntimeException("SAMLIssuerFactory: Cannot load properties: " + propFilename, e);
        }
        return properties;
    }
}
