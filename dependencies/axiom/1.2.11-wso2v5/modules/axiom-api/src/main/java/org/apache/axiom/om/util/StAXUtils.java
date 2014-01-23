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

package org.apache.axiom.om.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMConstants;
import org.apache.axiom.util.stax.XMLEventUtils;
import org.apache.axiom.util.stax.dialect.StAXDialect;
import org.apache.axiom.util.stax.dialect.StAXDialectDetector;
import org.apache.axiom.util.stax.wrapper.ImmutableXMLInputFactory;
import org.apache.axiom.util.stax.wrapper.ImmutableXMLOutputFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

/**
 * Utility class containing StAX related methods.
 * <p>This class defines a set of methods to get {@link XMLStreamReader} and {@link XMLStreamWriter}
 * instances. This class caches the corresponding factories ({@link XMLInputFactory}
 * and {@link XMLOutputFactory} objects) by classloader (default) or as singletons.
 * The behavior can be changed using {@link #setFactoryPerClassLoader(boolean)}.</p>
 * <p>Default properties for these factories can be specified using
 * <tt>XMLInputFactory.properties</tt> and <tt>XMLOutputFactory.properties</tt> files.
 * When a new factory is instantiated, this class will attempt to load the corresponding file using
 * the context classloader. This class supports properties with boolean, integer and string values.
 * Both standard StAX properties and implementation specific properties can be specified. This
 * feature should be used with care since changing some properties to non default values will break
 * Axiom. Good candidates for <tt>XMLInputFactory.properties</tt> are:</p>
 * <dl>
 *   <dt><tt>javax.xml.stream.isCoalescing</tt></dt>
 *   <dd>Requires the processor to coalesce adjacent character data (text nodes and CDATA
 *       sections). This property also controls whether CDATA sections are reported or not.</dd>
 *   <dt><tt>com.ctc.wstx.inputBufferLength</tt></dt>
 *   <dd>Size of input buffer (in chars), to use for reading XML content from input stream/reader.
 *       This property is Woodstox specific.</dd>
 *   <dt><tt>com.ctc.wstx.minTextSegment</tt></dt>
 *   <dd>Property to specify shortest non-complete text segment (part of CDATA section or text
 *       content) that the parser is allowed to return, if not required to coalesce text.
 *       This property is Woodstox specific.</dt>
 * </dl>
 * <p>Good candidates for <tt>XMLOutputFactory.properties</tt> are:</p>
 * <dl>
 *   <dt><tt>com.ctc.wstx.outputEscapeCr</tt></dt>
 *   <dd>Property that determines whether Carriage Return (\r) characters are to be escaped when
 *       output or not. If enabled, all instances of of character \r are escaped using a character
 *       entity (where possible, that is, within CHARACTERS events, and attribute values).
 *       Otherwise they are output as is. The main reason to enable this property is to ensure
 *       that carriage returns are preserved as is through parsing, since otherwise they will be
 *       converted to canonical XML linefeeds (\n), when occurring along or as part of \r\n pair.
 *       This property is Woodstox specific.</dd>
 * </dl>
 */
public class StAXUtils {
    private static Log log = LogFactory.getLog(StAXUtils.class);
    private static boolean isDebugEnabled = log.isDebugEnabled();
    
    // If isFactoryPerClassLoader is true (default), then 
    // a separate singleton XMLInputFactory and XMLOutputFactory is maintained
    // for the each classloader.  The different classloaders may be using different
    // implementations of STAX.
    // 
    // If isFactoryPerClassLoader is false, then
    // a single XMLInputFactory and XMLOutputFactory is constructed using
    // the classloader that loaded StAXUtils. 
    private static boolean isFactoryPerClassLoader = true;
    
    // These static singletons are used when the XML*Factory is created with
    // the StAXUtils classloader.
    private static final Map/*<StAXParserConfiguration,XMLInputFactory>*/ inputFactoryMap
            = Collections.synchronizedMap(new WeakHashMap());
    private static final Map/*<StAXWriterConfiguration,XMLOutputFactory>*/ outputFactoryMap
            = Collections.synchronizedMap(new WeakHashMap());
    
    // These maps are used for the isFactoryPerClassLoader==true case
    // The maps are synchronized and weak.
    private static final Map/*<StAXParserConfiguration,Map<ClassLoader,XMLInputFactory>>*/ inputFactoryPerCLMap
            = Collections.synchronizedMap(new WeakHashMap());
    private static final Map/*<StAXWriterConfiguration,Map<ClassLoader,XMLInputFactory>>*/ outputFactoryPerCLMap
            = Collections.synchronizedMap(new WeakHashMap());
    
    /**
     * Get a cached {@link XMLInputFactory} instance using the default
     * configuration and cache policy (i.e. one instance per class loader).
     * 
     * @return an {@link XMLInputFactory} instance.
     */
    public static XMLInputFactory getXMLInputFactory() {
        return getXMLInputFactory(null, isFactoryPerClassLoader);
    }
    
    /**
     * Get a cached {@link XMLInputFactory} instance using the specified
     * configuration and the default cache policy.
     * 
     * @param configuration
     *            the configuration applied to the requested factory
     * @return an {@link XMLInputFactory} instance.
     */
    public static XMLInputFactory getXMLInputFactory(StAXParserConfiguration configuration) {
        return getXMLInputFactory(configuration, isFactoryPerClassLoader);
    }
    
    /**
     * Get a cached {@link XMLInputFactory} instance using the default
     * configuration and the specified cache policy.
     * 
     * @param factoryPerClassLoaderPolicy
     *            the cache policy; see
     *            {@link #getXMLInputFactory(StAXParserConfiguration, boolean)}
     *            for more details
     * @return an {@link XMLInputFactory} instance.
     */
    public static XMLInputFactory getXMLInputFactory(boolean factoryPerClassLoaderPolicy) {
        return getXMLInputFactory(null, factoryPerClassLoaderPolicy);
    }
    
    /**
     * Get a cached {@link XMLInputFactory} instance using the specified
     * configuration and cache policy.
     * 
     * @param configuration
     *            the configuration applied to the requested factory
     * @param factoryPerClassLoaderPolicy
     *            If set to <code>true</code>, the factory cached for the
     *            current class loader will be returned. If set to
     *            <code>false</code>, the singleton factory (instantiated using
     *            the class loader that loaded {@link StAXUtils}) will be
     *            returned.
     * @return an {@link XMLInputFactory} instance.
     */
    public static XMLInputFactory getXMLInputFactory(StAXParserConfiguration configuration,
            boolean factoryPerClassLoaderPolicy) {
        
        if (factoryPerClassLoaderPolicy) {
            return getXMLInputFactory_perClassLoader(configuration);
        } else {
            return getXMLInputFactory_singleton(configuration);
        }
    }

    /**
     * @deprecated
     * Returns an XMLInputFactory instance for reuse.
     *
     * @param factory An XMLInputFactory instance that is available for reuse
     */
    public static void releaseXMLInputFactory(XMLInputFactory factory) {
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding)
            throws XMLStreamException {
        
        return createXMLStreamReader(null, in, encoding);
    }
    
    public static XMLStreamReader createXMLStreamReader(StAXParserConfiguration configuration,
            final InputStream in, final String encoding) throws XMLStreamException {
        
        final XMLInputFactory inputFactory = getXMLInputFactory(configuration);
        try {
            XMLStreamReader reader = 
                (XMLStreamReader) 
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return inputFactory.createXMLStreamReader(in, encoding);
                    }
                }
                );
            if (isDebugEnabled) {
                log.debug("XMLStreamReader is " + reader.getClass().getName());
            }
            return reader;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in)
            throws XMLStreamException {
        
        return createXMLStreamReader(null, in);
    }
    
    public static XMLStreamReader createXMLStreamReader(StAXParserConfiguration configuration,
            final InputStream in) throws XMLStreamException {
        
        final XMLInputFactory inputFactory = getXMLInputFactory(configuration);
        try {
            XMLStreamReader reader = 
                (XMLStreamReader)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return inputFactory.createXMLStreamReader(in);
                    }
                }
                );
            
            if (isDebugEnabled) {
                log.debug("XMLStreamReader is " + reader.getClass().getName());
            }
            return reader;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    public static XMLStreamReader createXMLStreamReader(Reader in)
            throws XMLStreamException {
        
        return createXMLStreamReader(null, in);
    }
    
    public static XMLStreamReader createXMLStreamReader(StAXParserConfiguration configuration,
            final Reader in) throws XMLStreamException {
        
        final XMLInputFactory inputFactory = getXMLInputFactory(configuration);
        try {
            XMLStreamReader reader = 
                (XMLStreamReader)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return inputFactory.createXMLStreamReader(in);
                    }
                }
                );
            if (isDebugEnabled) {
                log.debug("XMLStreamReader is " + reader.getClass().getName());
            }
            return reader;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    /**
     * Get a cached {@link XMLOutputFactory} instance using the default
     * configuration and cache policy (i.e. one instance per class loader).
     * 
     * @return an {@link XMLOutputFactory} instance.
     */
    public static XMLOutputFactory getXMLOutputFactory() {
        return getXMLOutputFactory(null, isFactoryPerClassLoader);
    }
    
    /**
     * Get a cached {@link XMLOutputFactory} instance using the specified
     * configuration and the default cache policy.
     * 
     * @param configuration
     *            the configuration applied to the requested factory
     * @return an {@link XMLOutputFactory} instance.
     */
    public static XMLOutputFactory getXMLOutputFactory(StAXWriterConfiguration configuration) {
        return getXMLOutputFactory(configuration, isFactoryPerClassLoader);
    }
    
    /**
     * Get a cached {@link XMLOutputFactory} instance using the default
     * configuration and the specified cache policy.
     * 
     * @param factoryPerClassLoaderPolicy
     *            the cache policy; see
     *            {@link #getXMLOutputFactory(StAXWriterConfiguration, boolean)}
     *            for more details
     * @return an {@link XMLOutputFactory} instance.
     */
    public static XMLOutputFactory getXMLOutputFactory(boolean factoryPerClassLoaderPolicy) {
        return getXMLOutputFactory(null, factoryPerClassLoaderPolicy);
    }
    
    /**
     * Get a cached {@link XMLOutputFactory} instance using the specified
     * configuration and cache policy.
     * 
     * @param configuration
     *            the configuration applied to the requested factory
     * @param factoryPerClassLoaderPolicy
     *            If set to <code>true</code>, the factory cached for the
     *            current class loader will be returned. If set to
     *            <code>false</code>, the singleton factory (instantiated using
     *            the class loader that loaded {@link StAXUtils}) will be
     *            returned.
     * @return an {@link XMLOutputFactory} instance.
     */
    public static XMLOutputFactory getXMLOutputFactory(StAXWriterConfiguration configuration,
            boolean factoryPerClassLoaderPolicy) {
        
        if (factoryPerClassLoaderPolicy) {
            return getXMLOutputFactory_perClassLoader(configuration);
        } else {
            return getXMLOutputFactory_singleton(configuration);
        }
    }

    /**
     * Set the policy for how to maintain the XMLInputFactory and XMLOutputFactory
     * @param value (if false, then one singleton...if true...then singleton per class loader 
     *  (default is true)
     */
    public static void setFactoryPerClassLoader(boolean value) {
        isFactoryPerClassLoader = value;
    }

    /**
     * @deprecated
     * Returns an XMLOutputFactory instance for reuse.
     *
     * @param factory An XMLOutputFactory instance that is available for reuse.
     */
    public static void releaseXMLOutputFactory(XMLOutputFactory factory) {
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out)
            throws XMLStreamException {
        
        return createXMLStreamWriter(null, out);
    }
    
    public static XMLStreamWriter createXMLStreamWriter(StAXWriterConfiguration configuration,
            final OutputStream out) throws XMLStreamException {
        final XMLOutputFactory outputFactory = getXMLOutputFactory(configuration);
        try {
            XMLStreamWriter writer = 
                (XMLStreamWriter)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return outputFactory.createXMLStreamWriter(out, OMConstants.DEFAULT_CHAR_SET_ENCODING);
                    }
                }
                );
                
            if (isDebugEnabled) {
                log.debug("XMLStreamWriter is " + writer.getClass().getName());
            }
            return writer;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding)
            throws XMLStreamException {
        
        return createXMLStreamWriter(null, out, encoding);
    }
    
    public static XMLStreamWriter createXMLStreamWriter(StAXWriterConfiguration configuration,
            final OutputStream out, final String encoding) throws XMLStreamException {
        final XMLOutputFactory outputFactory = getXMLOutputFactory(configuration);
        try {
            XMLStreamWriter writer = 
                (XMLStreamWriter)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return outputFactory.createXMLStreamWriter(out, encoding);
                    }
                }
                );
            
            if (isDebugEnabled) {
                log.debug("XMLStreamWriter is " + writer.getClass().getName());
            }
            return writer;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    public static XMLStreamWriter createXMLStreamWriter(final Writer out)
            throws XMLStreamException {
        
        return createXMLStreamWriter(null, out);
    }
    
    public static XMLStreamWriter createXMLStreamWriter(StAXWriterConfiguration configuration,
            final Writer out) throws XMLStreamException {
        final XMLOutputFactory outputFactory = getXMLOutputFactory(configuration);
        try {
            XMLStreamWriter writer = 
                (XMLStreamWriter)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws XMLStreamException {
                        return outputFactory.createXMLStreamWriter(out);
                    }
                }
                );
            if (isDebugEnabled) {
                log.debug("XMLStreamWriter is " + writer.getClass().getName());
            }
            return writer;
        } catch (PrivilegedActionException pae) {
            throw (XMLStreamException) pae.getException();
        }
    }

    /**
     * @deprecated
     */
    public static void reset() {
    }
    
    /**
     * Load factory properties from a resource. The context class loader is used to locate
     * the resource. The method converts boolean and integer values to the right Java types.
     * All other values are returned as strings.
     * 
     * @param name
     * @return
     */
    // This has package access since it is used from within anonymous inner classes
    static Map loadFactoryProperties(String name) {
        ClassLoader cl = getContextClassLoader();
        InputStream in = cl.getResourceAsStream(name);
        if (in == null) {
            return null;
        } else {
            try {
                Properties rawProps = new Properties();
                Map props = new HashMap();
                rawProps.load(in);
                for (Iterator it = rawProps.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String strValue = (String)entry.getValue();
                    Object value;
                    if (strValue.equals("true")) {
                        value = Boolean.TRUE;
                    } else if (strValue.equals("false")) {
                        value = Boolean.FALSE;
                    } else {
                        try {
                            value = Integer.valueOf(strValue);
                        } catch (NumberFormatException ex) {
                            value = strValue;
                        }
                    }
                    props.put(entry.getKey(), value);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Loaded factory properties from " + name + ": " + props);
                }
                return props;
            } catch (IOException ex) {
                log.error("Failed to read " + name, ex);
                return null;
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
    }
    
    private static XMLInputFactory newXMLInputFactory(final ClassLoader classLoader,
            final StAXParserConfiguration configuration) {
        
        return (XMLInputFactory)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader savedClassLoader;
                if (classLoader == null) {
                    savedClassLoader = null;
                } else {
                    savedClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoader);
                }
                try {
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    // Woodstox by default creates coalescing parsers. Even if this violates
                    // the StAX specs, for compatibility with Woodstox, we always enable the
                    // coalescing mode. Note that we need to do that before loading
                    // XMLInputFactory.properties so that this setting can be overridden.
                    factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
                    Map props = loadFactoryProperties("XMLInputFactory.properties");
                    if (props != null) {
                        for (Iterator it = props.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry)it.next();
                            factory.setProperty((String)entry.getKey(), entry.getValue());
                        }
                    }
                    StAXDialect dialect = StAXDialectDetector.getDialect(factory.getClass());
                    if (configuration != null) {
                        factory = configuration.configure(factory, dialect);
                    }
                    return new ImmutableXMLInputFactory(dialect.normalize(
                            dialect.makeThreadSafe(factory)));
                } finally {
                    if (savedClassLoader != null) {
                        Thread.currentThread().setContextClassLoader(savedClassLoader);
                    }
                }
            }
        });
    }

    /**
     * @return XMLInputFactory for the current classloader
     */
    private static XMLInputFactory getXMLInputFactory_perClassLoader(StAXParserConfiguration configuration) {
        
        ClassLoader cl = getContextClassLoader();
        XMLInputFactory factory;
        if (cl == null) {
            factory = getXMLInputFactory_singleton(configuration);
        } else {
            // Check the cache
            if (configuration == null) {
                configuration = StAXParserConfiguration.DEFAULT;
            }
            Map map = (Map)inputFactoryPerCLMap.get(configuration);
            if (map == null) {
                map = Collections.synchronizedMap(new WeakHashMap());
                inputFactoryPerCLMap.put(configuration, map);
                factory = null;
            } else {
                factory = (XMLInputFactory)map.get(cl);
            }
            
            // If not found in the cache map, crate a new factory
            if (factory == null) {

                if (log.isDebugEnabled()) {
                    log.debug("About to create XMLInputFactory implementation with " +
                                "classloader=" + cl);
                    log.debug("The classloader for javax.xml.stream.XMLInputFactory is: "
                              + XMLInputFactory.class.getClassLoader());
                }
                try {
                    factory = newXMLInputFactory(null, configuration);
                } catch (ClassCastException cce) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed creation of XMLInputFactory implementation with " +
                                        "classloader=" + cl);
                        log.debug("Exception is=" + cce);
                        log.debug("Attempting with classloader: " + 
                                  XMLInputFactory.class.getClassLoader());
                    }
                    factory = newXMLInputFactory(XMLInputFactory.class.getClassLoader(),
                            configuration);
                }
                    
                if (factory != null) {
                    // Cache the new factory
                    map.put(cl, factory);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Created XMLInputFactory = " + factory.getClass() + 
                                  " with classloader=" + cl);
                        log.debug("Configuration = " + configuration);
                        log.debug("Size of XMLInputFactory map for this configuration = " + map.size());
                        log.debug("Configurations for which factories have been cached = " +
                                inputFactoryPerCLMap.keySet());
                    }
                } else {
                    factory = getXMLInputFactory_singleton(configuration);
                }
            }
            
        }
        return factory;
    }
    
    /**
     * @return singleton XMLInputFactory loaded with the StAXUtils classloader
     */
    private static XMLInputFactory getXMLInputFactory_singleton(StAXParserConfiguration configuration) {
        if (configuration == null) {
            configuration = StAXParserConfiguration.DEFAULT;
        }
        XMLInputFactory f = (XMLInputFactory)inputFactoryMap.get(configuration);
        if (f == null) {
            f = newXMLInputFactory(StAXUtils.class.getClassLoader(), configuration);
            inputFactoryMap.put(configuration, f);
            if (log.isDebugEnabled()) {
                if (f != null) {
                    log.debug("Created singleton XMLInputFactory " + f.getClass() + " with configuration " + configuration);
                }
            }
        }
        
        return f;
    }
    
    private static XMLOutputFactory newXMLOutputFactory(final ClassLoader classLoader,
            final StAXWriterConfiguration configuration) {
        return (XMLOutputFactory)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ClassLoader savedClassLoader;
                if (classLoader == null) {
                    savedClassLoader = null;
                } else {
                    savedClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoader);
                }
                try {
                    XMLOutputFactory factory = XMLOutputFactory.newInstance();
                    factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, 
                                        Boolean.FALSE);
                    Map props = loadFactoryProperties("XMLOutputFactory.properties");
                    if (props != null) {
                        for (Iterator it = props.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry)it.next();
                            factory.setProperty((String)entry.getKey(), entry.getValue());
                        }
                    }
                    StAXDialect dialect = StAXDialectDetector.getDialect(factory.getClass());
                    if (configuration != null) {
                        factory = configuration.configure(factory, dialect);
                    }
                    return new ImmutableXMLOutputFactory(dialect.normalize(
                            dialect.makeThreadSafe(factory)));
                } finally {
                    if (savedClassLoader != null) {
                        Thread.currentThread().setContextClassLoader(savedClassLoader);
                    }
                }
            }
        });
    }
    
    /**
     * @return XMLOutputFactory for the current classloader
     */
    private static XMLOutputFactory getXMLOutputFactory_perClassLoader(StAXWriterConfiguration configuration) {
        ClassLoader cl = getContextClassLoader();
        XMLOutputFactory factory;
        if (cl == null) {
            factory = getXMLOutputFactory_singleton(configuration);
        } else {
            if (configuration == null) {
                configuration = StAXWriterConfiguration.DEFAULT;
            }
            Map map = (Map)outputFactoryPerCLMap.get(configuration);
            if (map == null) {
                map = Collections.synchronizedMap(new WeakHashMap());
                outputFactoryPerCLMap.put(configuration, map);
                factory = null;
            } else {
                factory = (XMLOutputFactory)map.get(cl);
            }
            
            if (factory == null) {
                if (log.isDebugEnabled()) {
                    log.debug("About to create XMLOutputFactory implementation with " +
                                "classloader=" + cl);
                    log.debug("The classloader for javax.xml.stream.XMLOutputFactory is: " + 
                              XMLOutputFactory.class.getClassLoader());
                }
                try {
                    factory = newXMLOutputFactory(null, configuration);
                } catch (ClassCastException cce) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed creation of XMLOutputFactory implementation with " +
                                        "classloader=" + cl);
                        log.debug("Exception is=" + cce);
                        log.debug("Attempting with classloader: " + 
                                  XMLOutputFactory.class.getClassLoader());
                    }
                    factory = newXMLOutputFactory(XMLOutputFactory.class.getClassLoader(),
                            configuration);
                }
                if (factory != null) {
                    map.put(cl, factory);
                    if (log.isDebugEnabled()) {
                        log.debug("Created XMLOutputFactory = " + factory.getClass() 
                                  + " for classloader=" + cl);
                        log.debug("Configuration = " + configuration);
                        log.debug("Size of XMLOutFactory map for this configuration = " + map.size());
                        log.debug("Configurations for which factories have been cached = " +
                                outputFactoryPerCLMap.keySet());
                    }
                } else {
                    factory = getXMLOutputFactory_singleton(configuration);
                }
            }
            
        }
        return factory;
    }
    
    /**
     * @return XMLOutputFactory singleton loaded with the StAXUtils classloader
     */
    private static XMLOutputFactory getXMLOutputFactory_singleton(StAXWriterConfiguration configuration) {
        if (configuration == null) {
            configuration = StAXWriterConfiguration.DEFAULT;
        }
        XMLOutputFactory f = (XMLOutputFactory)outputFactoryMap.get(configuration);
        if (f == null) {
            f = newXMLOutputFactory(StAXUtils.class.getClassLoader(), configuration);
            outputFactoryMap.put(configuration, f);
            if (log.isDebugEnabled()) {
                if (f != null) {
                    log.debug("Created singleton XMLOutputFactory " + f.getClass() + " with configuration " + configuration);
                }
            }
        }
        return f;
    }
    
    /**
     * @return Trhead Context ClassLoader
     */
    private static ClassLoader getContextClassLoader() {
        ClassLoader cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run()  {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        
        return cl;
    }

    /**
     * @deprecated use {@link #createXMLStreamReader(StAXParserConfiguration, InputStream, String)}
     *             with {@link StAXParserConfiguration#STANDALONE}
     */
    public static XMLStreamReader createNetworkDetachedXMLStreamReader(InputStream in, String encoding)
            throws XMLStreamException {
        
        return createXMLStreamReader(StAXParserConfiguration.STANDALONE, in, encoding);
    }

    /**
     * @deprecated use {@link #getXMLInputFactory(StAXParserConfiguration)} with
     *             {@link StAXParserConfiguration#STANDALONE}
     */
    public static XMLInputFactory getNetworkDetachedXMLInputFactory() {
        return getXMLInputFactory(StAXParserConfiguration.STANDALONE);
    }
    
    /**
     * @deprecated use {@link #createXMLStreamReader(StAXParserConfiguration, InputStream)}
     *             with {@link StAXParserConfiguration#STANDALONE}
     */
    public static XMLStreamReader createNetworkDetachedXMLStreamReader(InputStream in)
            throws XMLStreamException {
        
        return createXMLStreamReader(StAXParserConfiguration.STANDALONE, in);
    }

    /**
     * @deprecated use {@link #createXMLStreamReader(StAXParserConfiguration, Reader)}
     *             with {@link StAXParserConfiguration#STANDALONE}
     */
    public static XMLStreamReader createNetworkDetachedXMLStreamReader(Reader in)
            throws XMLStreamException {
        
        return createXMLStreamReader(StAXParserConfiguration.STANDALONE, in);
    }

    /**
     * @deprecated Use {@link XMLEventUtils#getEventTypeString(int)} instead
     */
    public static String getEventTypeString(int event) {
        return XMLEventUtils.getEventTypeString(event);
    }
}
