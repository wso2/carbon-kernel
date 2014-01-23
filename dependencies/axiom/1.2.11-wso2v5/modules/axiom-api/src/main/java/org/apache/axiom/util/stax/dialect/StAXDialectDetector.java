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

package org.apache.axiom.util.stax.dialect;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Detects StAX dialects and normalizes factories for a given StAX implementation.
 * <p>
 * Note that this class internally maintains a cache of detected dialects. The overhead caused by
 * invocations of methods in this class is thus small.
 */
public class StAXDialectDetector {
    private static final Log log = LogFactory.getLog(StAXDialectDetector.class);
    
    private static final Attributes.Name IMPLEMENTATION_TITLE =
            new Attributes.Name("Implementation-Title");
    
    private static final Attributes.Name IMPLEMENTATION_VENDOR =
        new Attributes.Name("Implementation-Vendor");

    private static final Attributes.Name IMPLEMENTATION_VERSION =
            new Attributes.Name("Implementation-Version");
    
    private static final Attributes.Name BUNDLE_SYMBOLIC_NAME =
            new Attributes.Name("Bundle-SymbolicName");
    
    private static final Attributes.Name BUNDLE_VENDOR =
            new Attributes.Name("Bundle-Vendor");

    private static final Attributes.Name BUNDLE_VERSION =
            new Attributes.Name("Bundle-Version");

    /**
     * Map that stores detected dialects by location. The location is the URL corresponding to the
     * root folder of the classpath entry from which the StAX implementation is loaded. Note that
     * in the case of a JAR file, this is not the URL pointing to the JAR, but a <tt>jar:</tt>
     * URL that points to the root folder of the archive.
     */
    private static final Map/*<URL,StAXDialect>*/ dialectByUrl =
            Collections.synchronizedMap(new HashMap());

    private StAXDialectDetector() {}
    
    /**
     * Get the URL corresponding to the root folder of the classpath entry from which a given
     * resource is loaded. This URL can be used to load other resources from the same classpath
     * entry (JAR file or directory).
     * 
     * @return the root URL or <code>null</code> if the resource can't be found or if it is not
     *         possible to determine the root URL
     */
    private static URL getRootUrlForResource(ClassLoader classLoader, String resource) {
        if (classLoader == null) {
            // A null class loader means the bootstrap class loader. In this case we use the
            // system class loader. This is safe since we can assume that the system class
            // loader uses parent first as delegation policy.
            classLoader = ClassLoader.getSystemClassLoader();
        }
        URL url = classLoader.getResource(resource);
        if (url == null) {
            return null;
        }
        String file = url.getFile();
        if (file.endsWith(resource)) {
            try {
                return new URL(url.getProtocol(), url.getHost(), url.getPort(),
                        file.substring(0, file.length()-resource.length()));
            } catch (MalformedURLException ex) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private static URL getRootUrlForClass(Class cls) {
        return getRootUrlForResource(cls.getClassLoader(),
                cls.getName().replace('.', '/') + ".class");
    }
    
    /**
     * Detect the dialect of a given {@link XMLInputFactory} and normalize it.
     * 
     * @param factory the factory to normalize
     * @return the normalized factory
     * 
     * @see StAXDialect#normalize(XMLInputFactory)
     */
    public static XMLInputFactory normalize(XMLInputFactory factory) {
        return getDialect(factory.getClass()).normalize(factory);
    }
    
    /**
     * Detect the dialect of a given {@link XMLOutputFactory} and normalize it.
     * 
     * @param factory the factory to normalize
     * @return the normalized factory
     * 
     * @see StAXDialect#normalize(XMLOutputFactory)
     */
    public static XMLOutputFactory normalize(XMLOutputFactory factory) {
        return getDialect(factory.getClass()).normalize(factory);
    }
    
    /**
     * Detect the dialect of a given StAX implementation.
     * 
     * @param implementationClass
     *            any class that is part of the StAX implementation; typically this should be a
     *            {@link XMLInputFactory}, {@link XMLOutputFactory},
     *            {@link javax.xml.stream.XMLStreamReader} or
     *            {@link javax.xml.stream.XMLStreamWriter} implementation
     * @return the detected dialect
     */
    public static StAXDialect getDialect(Class implementationClass) {
        URL rootUrl = getRootUrlForClass(implementationClass);
        if (rootUrl == null) {
            log.warn("Unable to determine location of StAX implementation containing class "
                    + implementationClass.getName() + "; using default dialect");
            return UnknownStAXDialect.INSTANCE;
        }
        return getDialect(implementationClass.getClassLoader(), rootUrl);
    }

    private static StAXDialect getDialect(ClassLoader classLoader, URL rootUrl) {
        StAXDialect dialect = (StAXDialect)dialectByUrl.get(rootUrl);
        if (dialect != null) {
            return dialect;
        } else {
            dialect = detectDialect(classLoader, rootUrl);
            dialectByUrl.put(rootUrl, dialect);
            return dialect;
        }
    }
    
    private static StAXDialect detectDialect(ClassLoader classLoader, URL rootUrl) {
        StAXDialect dialect = detectDialectFromJarManifest(rootUrl);
        if (dialect == null) {
            // Note: We look for well defined classes instead of just checking the package name
            // of the class passed to getDialect(Class) because in some parsers, the implementations
            // of the StAX interfaces (factories, readers and writers) are not in the same package.
            dialect = detectDialectFromClasses(classLoader, rootUrl);
        }
        if (dialect == null) {
            log.warn("Unable to determine dialect of the StAX implementation at " + rootUrl);
            return UnknownStAXDialect.INSTANCE;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Detected StAX dialect: " + dialect.getName());
            }
            return dialect;
        }
    }
    
    private static StAXDialect detectDialectFromJarManifest(URL rootUrl) {
        Manifest manifest;
        try {
            URL metaInfUrl = new URL(rootUrl, "META-INF/MANIFEST.MF");
            InputStream is = metaInfUrl.openStream();
            try {
                manifest = new Manifest(is);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            log.warn("Unable to load manifest for StAX implementation at " + rootUrl);
            return UnknownStAXDialect.INSTANCE;
        }
        Attributes attrs = manifest.getMainAttributes();
        String title = attrs.getValue(IMPLEMENTATION_TITLE);
        String symbolicName = attrs.getValue(BUNDLE_SYMBOLIC_NAME);
        if (symbolicName != null) {
            int i = symbolicName.indexOf(';');
            if (i != -1) {
                symbolicName = symbolicName.substring(0, i);
            }
        }
        String vendor = attrs.getValue(IMPLEMENTATION_VENDOR);
        if (vendor == null) {
            vendor = attrs.getValue(BUNDLE_VENDOR);
        }
        String version = attrs.getValue(IMPLEMENTATION_VERSION);
        if (version == null) {
            version = attrs.getValue(BUNDLE_VERSION);
        }
        if (log.isDebugEnabled()) {
            log.debug("StAX implementation at " + rootUrl + " is:\n" +
                    "  Title:         " + title + "\n" +
                    "  Symbolic name: " + symbolicName + "\n" +
                    "  Vendor:        " + vendor + "\n" +
                    "  Version:       " + version);
        }
        
        // For the moment, the dialect detection is quite simple, but in the future we will probably
        // have to differentiate by version number
        if (vendor != null && vendor.toLowerCase().indexOf("woodstox") != -1) {
            return WoodstoxDialect.INSTANCE;
        } else if (title != null && title.indexOf("SJSXP") != -1) {
            return new SJSXPDialect(false);
        } else if ("com.bea.core.weblogic.stax".equals(symbolicName)) {
            // Weblogic's StAX implementation doesn't support CDATA section reporting and there are
            // a couple of additional test cases (with respect to BEA's reference implementation)
            // that fail.
            log.warn("Weblogic's StAX implementation is unsupported and some Axiom features will not work " +
            		"as expected! Please use Woodstox instead.");
            // This is the best match we can return in this case.
            return BEADialect.INSTANCE;
        } else if ("BEA".equals(vendor)) {
            return BEADialect.INSTANCE;
        } else if ("com.ibm.ws.prereq.banshee".equals(symbolicName)) {
            return XLXP2Dialect.INSTANCE;
        } else {
            return null;
        }
    }

    private static Class loadClass(ClassLoader classLoader, URL rootUrl, String name) {
        try {
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            Class cls = classLoader.loadClass(name);
            // Cross check if the class was loaded from the same location (JAR)
            return rootUrl.equals(getRootUrlForClass(cls)) ? cls : null;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
    private static StAXDialect detectDialectFromClasses(ClassLoader classLoader, URL rootUrl) {
        Class cls;
        
        // Try Sun's implementation found in JREs
        cls = loadClass(classLoader, rootUrl, "com.sun.xml.internal.stream.XMLOutputFactoryImpl");
        if (cls != null) {
            // Some JREs (such as IBM Java 1.7) include com.sun.xml.internal.stream.XMLOutputFactoryImpl
            // for compatibility (in which case it extends the XMLOutputFactory implementation from
            // another StAX implementation, e.g. XLXP). Detect this situation by checking the superclass.
            Class superClass = cls.getSuperclass();
            if (superClass == XMLOutputFactory.class || superClass.getName().startsWith("com.sun.")) {
                // Check if the implementation has the bug fixed here:
                // https://sjsxp.dev.java.net/source/browse/sjsxp/zephyr/src/com/sun/xml/stream/ZephyrWriterFactory.java?rev=1.8&r1=1.4&r2=1.5
                boolean isUnsafeStreamResult;
                try {
                    cls.getDeclaredField("fStreamResult");
                    isUnsafeStreamResult = true;
                } catch (NoSuchFieldException ex) {
                    isUnsafeStreamResult = false;
                }
                return new SJSXPDialect(isUnsafeStreamResult);
            }
        }
        
        // Try IBM's XL XP-J
        cls = loadClass(classLoader, rootUrl, "com.ibm.xml.xlxp.api.stax.StAXImplConstants");
        if (cls != null) {
            boolean isSetPrefixBroken;
            try {
                cls.getField("IS_SETPREFIX_BEFORE_STARTELEMENT");
                isSetPrefixBroken = false;
            } catch (NoSuchFieldException ex) {
                isSetPrefixBroken = true;
            }
            return new XLXP1Dialect(isSetPrefixBroken);
        }
        cls = loadClass(classLoader, rootUrl, "com.ibm.xml.xlxp2.api.stax.StAXImplConstants");
        if (cls != null) {
            return new XLXP2Dialect();
        }
        
        return null;
    }
}
