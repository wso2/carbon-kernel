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

package org.apache.axis2.jaxws.util;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.wsdl.WSDLReaderConfigurator;
import org.apache.axis2.metadata.factory.ResourceFinderFactory;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.metadata.resource.ResourceFinder;
import org.apache.axis2.wsdl.util.WSDLDefinitionWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Implementation of WSDLWrapper interface which controls access
 * to the underlying Definition (WSDLDefinitionWrapper).
 * The WSDLDefinitionWrapper implementation uses various strategies
 * to control its in-memory footprint.
 */
public class WSDL4JWrapper implements WSDLWrapper {
    private static final Log log = LogFactory.getLog(WSDL4JWrapper.class);

    private WSDLDefinitionWrapper wsdlDefinition = null;

    private URL wsdlURL;
    private String wsdlExplicitURL;
    private ConfigurationContext configContext;
    private JAXWSCatalogManager catalogManager = null;
    
    // By default, use a reload strategy for the WSDLWrapper
    private boolean limitMemory = true;
    private int memoryType = 2;
    
   /**
    * Constructor
    *
    * @param URL   The URL for the WSDL
    * @deprecated Use a constructor that passes in the ConfigContext, or memoryLimit parameter
    */
    public WSDL4JWrapper(URL wsdlURL) throws FileNotFoundException, UnknownHostException,
            ConnectException, IOException, WSDLException {
        super();
        this.commonPartsURLConstructor(wsdlURL, (ConfigurationContext)null);
    }
    
    /**
     * @param wsdlURL
     * @param limitMemory true if memory should be limited
     * @throws FileNotFoundException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws IOException
     * @throws WSDLException
     */
    public WSDL4JWrapper(URL wsdlURL, boolean limitMemory, int memoryType) throws FileNotFoundException, UnknownHostException,
    ConnectException, IOException, WSDLException {
        super();
        this.limitMemory = limitMemory;
        this.memoryType = memoryType;
        this.commonPartsURLConstructor(wsdlURL, (ConfigurationContext)null);
    }

    /**
     * @param wsdlURL
     * @param catalogManager
     * @throws FileNotFoundException
     * @throws UnknownHostException
     * @throws ConnectException
     * @throws IOException
     * @throws WSDLException
     * @deprecated use a constructor with a ConfigurationContext or limitMemory parameter
     */
    public WSDL4JWrapper(URL wsdlURL, JAXWSCatalogManager catalogManager) throws FileNotFoundException, 
    UnknownHostException, ConnectException, IOException, WSDLException {
        this(wsdlURL, catalogManager, false, 0);
    }
    
    public WSDL4JWrapper(URL wsdlURL, JAXWSCatalogManager catalogManager, boolean limitMemory) throws FileNotFoundException, 
    UnknownHostException, ConnectException, IOException, WSDLException {
        super();
        this.catalogManager = catalogManager;
        this.limitMemory = limitMemory;
        this.commonPartsURLConstructor(wsdlURL, (ConfigurationContext)null);
    }
    public WSDL4JWrapper(URL wsdlURL, JAXWSCatalogManager catalogManager, boolean limitMemory, int memoryType) throws FileNotFoundException, 
    UnknownHostException, ConnectException, IOException, WSDLException {
        super();
        this.catalogManager = catalogManager;
        this.limitMemory = limitMemory;
        this.memoryType = memoryType;
        this.commonPartsURLConstructor(wsdlURL, (ConfigurationContext)null);
    }
        
    public WSDL4JWrapper(URL wsdlURL, ConfigurationContext configContext, 
            JAXWSCatalogManager catalogManager) throws FileNotFoundException, 
    UnknownHostException, ConnectException, IOException, WSDLException {
        super();
        this.catalogManager = catalogManager;
        this.commonPartsURLConstructor(wsdlURL, configContext);
    }
    
    public WSDL4JWrapper(URL wsdlURL, ConfigurationContext configContext) throws FileNotFoundException, 
    UnknownHostException, ConnectException, IOException, WSDLException {
        super();
        this.commonPartsURLConstructor(wsdlURL, configContext);
    }
    
    private void commonPartsURLConstructor(URL wsdlURL, ConfigurationContext configContext) throws FileNotFoundException, UnknownHostException,
            ConnectException, IOException, WSDLException {
        this.configContext = configContext;
    // debugMemoryParms(configContext);
        if(log.isDebugEnabled()) {
            log.debug("WSDL4JWrapper(URL,ConfigurationContext) - Looking for wsdl file on client: " + (wsdlURL != null ? 
                    wsdlURL.getPath():null));
        }
        ClassLoader classLoader = (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        this.wsdlURL = wsdlURL;
       
        URLConnection urlCon;
        try {        
        	
        	urlCon = getPrivilegedURLConnection(this.wsdlURL);

        	InputStream is = null;
            
            try {
                is = getInputStream(urlCon);
            }
            catch(IOException e) {
                if(log.isDebugEnabled()) {
                    log.debug("Could not open url connection. Trying to use " +
                    "classloader to get another URL.");
                }
                String filePath = wsdlURL != null ? wsdlURL.getPath() : null;
                if(filePath != null) {
                    URL url = getAbsoluteURL(classLoader, filePath);
                    if(url == null) {
                        if(log.isDebugEnabled()) {
                            log.debug("Could not locate URL for wsdl. Reporting error");
                        }
                            throw new WSDLException("WSDL4JWrapper : ", e.getMessage(), e);
                        }
                    else {
                        urlCon = openConnection(url);
                        if(log.isDebugEnabled()) {
                             log.debug("Found URL for WSDL from jar");
                        }
                    }
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Could not get URL from classloader. Reporting " +
                        "error due to no file path.");
                    }
                    throw new WSDLException("WSDL4JWrapper : ", e.getMessage(), e);
                }
            }
            if(is != null) {
                is.close();
            }
            this.wsdlExplicitURL = urlCon.getURL().toString();
            getDefinition();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (UnknownHostException ex) {
            throw ex;
        } catch (ConnectException ex) {
            throw ex;
        } catch(IOException ex)  {
            throw ex;
        } catch (Exception ex) {
            throw new WSDLException("WSDL4JWrapper : ", ex.getMessage(), ex);
        }
    }
    
    /**
     * This is a helper method to retrieve a URLConnection object
     * based on a URL for the WSDL document.
     */
    private URLConnection getURLConnection(URL url) throws IOException {
        String filePath = null;
        boolean isFileProtocol =
                (url != null && "file".equals(url.getProtocol())) ? true : false;
        if (isFileProtocol) {
            filePath = (url != null) ? url.getPath() : null;
            //Check is the uri has relative path i.e path is not absolute and is not starting with a "/"
            boolean isRelativePath =
                    (filePath != null && !new File(filePath).isAbsolute()) ? true : false;
            if (isRelativePath) {
                if (log.isDebugEnabled()) {
                    log.debug("WSDL URL has a relative path");
                }
                //Lets read the complete WSDL URL for relative path from class loader
                //Use relative path of url to fetch complete URL.
                url = getAbsoluteURL(getThreadClassLoader(), filePath);
                if (url == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL URL for relative path not found in ClassLoader");
                        log.warn(
                                "Unable to read WSDL from relative path, check the relative path");
                        log.info("Relative path example: file:/WEB-INF/wsdl/<wsdlfilename>");
                        log.warn(
                                "Using relative path as default wsdl URL to create wsdl Definition.");
                    }
                    url = wsdlURL;
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("WSDL URL found for relative path: " + filePath + " scheme: " +
                                url.getProtocol());
                    }
                }
            }
        }
        URLConnection connection = null;
        if(url != null) {
            if(log.isDebugEnabled()) {
                log.debug("Retrieving URLConnection from WSDL URL");
            }
            connection = openConnection(url);
        }
        return connection;
    }
    
    private URLConnection getPrivilegedURLConnection(final URL url) throws IOException {
        try {

        	return (URLConnection) AccessController.doPrivileged( new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return (getURLConnection(url));
                }
            });
        
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
    }

    private URLConnection openConnection(final URL url) throws IOException {
        try {
            return (URLConnection) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return url.openConnection();
                }
            });
        } catch (PrivilegedActionException e) {
           throw (IOException) e.getException();
        }
    }
    
    private ClassLoader getThreadClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private URL getAbsoluteURL(final ClassLoader classLoader, final String filePath){
        URL url = (URL) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return classLoader.getResource(filePath);
                    }
                }
        );
        if(url == null) {
            if(log.isDebugEnabled()) {
                log.debug("Could not get URL from classloader. Looking in a jar.");
            }
            if(classLoader instanceof URLClassLoader){
                final URLClassLoader urlLoader = (URLClassLoader)classLoader;
                
                url = (URL) AccessController.doPrivileged( new PrivilegedAction() {
                    public Object run()  {
                        return (getURLFromJAR(urlLoader, wsdlURL));
                    }
                });

            }
            else {
                final URLClassLoader nestedLoader = (URLClassLoader) getNestedClassLoader(URLClassLoader.class, classLoader);
                if (nestedLoader != null) {
                    url = (URL) AccessController.doPrivileged( new PrivilegedAction() {
                        public Object run()  {
                            return (getURLFromJAR(nestedLoader, wsdlURL));
                        }
                    });
                }
            }
        }
        return url;    
    }
    
    private ClassLoader getNestedClassLoader(Class type, ClassLoader root) {
        if (log.isDebugEnabled()) {
            log.debug("Searching for nested URLClassLoader");
        }
        while (!(root instanceof URLClassLoader)) {
            if (root == null) {
                break;
            }
            
            final ClassLoader current = root;
            root = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return current.getParent();
                        }
                    }
            );
            if (log.isDebugEnabled() && root != null) {
                log.debug("Checking parent ClassLoader: " + root.getClass().getName());
            }
        }

        return root;
    }
    
    private URL getURLFromJAR(URLClassLoader urlLoader, URL relativeURL) {
        URL[] urlList = null;
        ResourceFinderFactory rff =(ResourceFinderFactory)MetadataFactoryRegistry.getFactory(ResourceFinderFactory.class);
        ResourceFinder cf = rff.getResourceFinder();
        if (log.isDebugEnabled()) {
            log.debug("ResourceFinderFactory: " + rff.getClass().getName());
            log.debug("ResourceFinder: " + cf.getClass().getName());
        }
        
        urlList = cf.getURLs(urlLoader);
        if(urlList == null){
            if(log.isDebugEnabled()){
                log.debug("No URL's found in URL ClassLoader");
            }
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("WSDL4JWrapperErr1"));
        }

        for (URL url : urlList) {
            if ("file".equals(url.getProtocol())) {
                // Insure that Windows spaces are properly handled in the URL
                final File f = new File(url.getPath().replaceAll("%20", " "));
                // If file is not of type directory then its a jar file
                if (isAFile(f)) { 
                    try {
                        JarFile jf = (JarFile) AccessController.doPrivileged(
                                new PrivilegedExceptionAction() {
                                    public Object run() throws IOException {
                                        return new JarFile(f);
                                    }
                                }
                        );
                        Enumeration<JarEntry> entries = jf.entries();
                        // read all entries in jar file and return the first
                        // wsdl file that matches
                        // the relative path
                        while (entries.hasMoreElements()) {
                            JarEntry je = entries.nextElement();
                            String name = je.getName();
                            if (name.endsWith(".wsdl")) {
                                String relativePath = relativeURL.getPath();
                                if (relativePath.endsWith(name)) {
                                    String path = f.getAbsolutePath();
                                    // This check is necessary because Unix/Linux file paths begin
                                    // with a '/'. When adding the prefix 'jar:file:/' we may end
                                    // up with '//' after the 'file:' part. This causes the URL 
                                    // object to treat this like a remote resource
                                    if(path != null && path.indexOf("/") == 0) {
                                        path = path.substring(1, path.length());
                                    }

                                    URL absoluteUrl = new URL("jar:file:/"
                                            + path + "!/"
                                            + je.getName());
                                    return absoluteUrl;
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                }
            }
        }

        return null;
    }

    private boolean isAFile(final File f) {
        Boolean ret = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return new Boolean(f.exists() && !f.isDirectory());
                    }
                }
        );
        return ret.booleanValue();
    }

    private static WSDLReader getWSDLReader() throws WSDLException {
        // Keep this method private
        WSDLReader reader;
        try {
            reader = (WSDLReader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws WSDLException {
                            WSDLFactory factory = WSDLFactory.newInstance();
                            return factory.newWSDLReader();
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (WSDLException)e.getException();
        }
        WSDLReaderConfigurator configurator = (WSDLReaderConfigurator) MetadataFactoryRegistry.
            getFactory(WSDLReaderConfigurator.class);
        if(configurator != null) {
            if(log.isDebugEnabled()) {
                log.debug("Calling configureReaderInstance with: " + configurator.getClass().getName());
            }
            configurator.configureReaderInstance(reader);
        }
        return reader;
    }

   /**
    * Constructor
    *
    * @param URL   The URL for the WSDL
    * @param Definition   Definition for the WSDL
    * @deprecated Use a constructor that has a ConfigContext or memoryLimit parameter
    */
    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition) throws WSDLException {
        this(wsdlURL, wsdlDefinition, null, null);
    }
    
    /**
     * Constructor
     *
     * @param URL   The URL for the WSDL
     * @param Definition   Definition for the WSDL
     * @param ConfigurationContext
     */
     public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, 
                          ConfigurationContext configContext) throws WSDLException {
         this(wsdlURL, wsdlDefinition, configContext, null);
     }
     
     /**
      * Constructor
      *
      * @param URL   The URL for the WSDL
      * @param Definition   Definition for the WSDL
      * @param limitMemory boolean
      */
      public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, boolean limitMemory, int memoryType) throws WSDLException {
          this(wsdlURL, wsdlDefinition, null, null, limitMemory, memoryType);
      }

    /**
     * Constructor
     *
     * @param URL   The URL for the WSDL
     * @param Definition   Definition for the WSDL
     * @param JAXWSCatalogManager Catalog Manager to use for locating external resources
     */
    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, 
                         JAXWSCatalogManager catalogManager, 
                         boolean limitMemory, int memoryType) throws WSDLException {
        this(wsdlURL, wsdlDefinition, null, catalogManager, limitMemory, memoryType);
    }
    
    /**
     * Constructor
     *
     * @param URL   The URL for the WSDL
     * @param Definition   Definition for the WSDL
     * @param JAXWSCatalogManager Catalog Manager to use for locating external resources
     * @deprecated Use a constructor with a ConfigurationContext or memory limit setting
     */
    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, JAXWSCatalogManager catalogManager) throws WSDLException {
        this(wsdlURL, wsdlDefinition, null, catalogManager, false, 0);
    }
    
    /**
    * Constructor
    *
    * @param URL   The URL for the WSDL
    * @param Definition   Definition for the WSDL
    * @param ConfigurationContext  to get parameters for WSDL building 
     * @param JAXWSCatalogManager Catalog Manager to use for locating external resources
    */
    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, ConfigurationContext configContext,
            JAXWSCatalogManager catalogManager) throws WSDLException {
        this(wsdlURL, wsdlDefinition, configContext, catalogManager, false, 0);
    }
    
    /**
     * Full Constructor
     */
     private WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition, ConfigurationContext configContext,
             JAXWSCatalogManager catalogManager, boolean limitMemory, int memoryType) throws WSDLException {
         super();
         if (log.isDebugEnabled() ) { log.debug("WSDL4JWrapper(...) entry"); }

         this.configContext = configContext;
         this.catalogManager = catalogManager;
         this.wsdlURL = wsdlURL;
         this.limitMemory = limitMemory;  // Only used if configContext is not present
         this.memoryType = memoryType;
         if ((wsdlDefinition != null) && !(wsdlDefinition instanceof WSDLDefinitionWrapper)) {
             if (configContext != null && configContext.getAxisConfiguration() != null) {
                 this.wsdlDefinition = 
                     new WSDLDefinitionWrapper(wsdlDefinition, wsdlURL, 
                                               configContext.getAxisConfiguration() );
             } else {
                 this.wsdlDefinition = 
                     new WSDLDefinitionWrapper(wsdlDefinition, wsdlURL, 
                                               limitMemory, 2);
             }
         } else {
             this.wsdlDefinition = (WSDLDefinitionWrapper) wsdlDefinition;
         }
     }


   /**
    * Constructor
    *
    * @param Definition   Definition for the WSDL
    * @deprecated Use WSDL4JWrapper(Definition,ConfigurationContext)
    */
    public WSDL4JWrapper(Definition wsdlDefinition) throws WSDLException {
        this(wsdlDefinition, false, 0);
        
    }
    
    /**
     * Constructor
     *
     * @param Definition   Definition for the WSDL
     * @boolean limitMemory
     */
     public WSDL4JWrapper(Definition wsdlDefinition, boolean limitMemory, int memoryType) throws WSDLException {
         if (log.isDebugEnabled() ) { log.debug("WSDL4JWrapper(Definition, boolean) entry"); }

         this.limitMemory = limitMemory;
         this.memoryType = memoryType;
         if ((wsdlDefinition != null) && !(wsdlDefinition instanceof WSDLDefinitionWrapper)) {
             this.wsdlDefinition = new WSDLDefinitionWrapper(wsdlDefinition, null, limitMemory, memoryType);
         } else {
             this.wsdlDefinition = (WSDLDefinitionWrapper) wsdlDefinition;
         }

         if (this.wsdlDefinition != null) {
             String baseURI = wsdlDefinition.getDocumentBaseURI();
             try {
                 wsdlURL = new URL(baseURI);
             } catch (Exception ex) {
                 // just absorb the error
             }
         }
     }

    /**
     * Constructor
     *
     * @param Definition   Definition for the WSDL
     * @param ConfigurationContext
     */
     public WSDL4JWrapper(Definition wsdlDefinition, 
                          ConfigurationContext configContext) throws WSDLException {
         super();
         if (log.isDebugEnabled() ) { log.debug("WSDL4JWrapper(Definition) entry"); }

         if ((wsdlDefinition != null) && !(wsdlDefinition instanceof WSDLDefinitionWrapper)) {
             this.wsdlDefinition = new WSDLDefinitionWrapper(wsdlDefinition, configContext.getAxisConfiguration());
         } else {
             this.wsdlDefinition = (WSDLDefinitionWrapper) wsdlDefinition;
         }

         if (this.wsdlDefinition != null) {
             String baseURI = wsdlDefinition.getDocumentBaseURI();
             try {
                 wsdlURL = new URL(baseURI);
             } catch (Exception ex) {
                 // just absorb the error
             }
         }
     }
    //TODO: Perform validations for each method to check for null parameters on QName.

    /*
     * Returns a wrapped WSDL4J wSDL definition
     */
    public Definition getDefinition() {
        if (wsdlDefinition == null) {
            Definition def = loadDefinition();
            if (def != null) {
                if (configContext != null) {
                    wsdlDefinition = new WSDLDefinitionWrapper(def, configContext.getAxisConfiguration() );
                } else {
                    wsdlDefinition = new WSDLDefinitionWrapper(def, wsdlURL, limitMemory, memoryType);
                }
            }
        }
        return wsdlDefinition;
    }

    /*
     * Returns an unwrapped WSDL4J wSDL definition
     */
    public Definition getUnwrappedDefinition() {
        Definition def;
        if (wsdlDefinition == null) {
            def = loadDefinition();
        } else if (wsdlDefinition instanceof WSDLDefinitionWrapper) {
            def = wsdlDefinition.getUnwrappedDefinition();
        } else {
            def = wsdlDefinition;
        }
        return def;
    }


    /*
     * Load a WSDL4J WSDL definition from a URL
     */
    public Definition loadDefinition() {

        Definition def = null;

        if (wsdlExplicitURL != null) {
            try {

                URLConnection urlConn = getPrivilegedURLConnection(this.wsdlURL);
                if(urlConn != null) {
                    try {
                        InputStream is = getInputStream(urlConn);
                        if(is != null) {
                            if (catalogManager == null) {
                                catalogManager = new OASISCatalogManager();
                            }
                            final CatalogWSDLLocator locator = new CatalogWSDLLocator(wsdlExplicitURL, is, 
                                    getThreadClassLoader(), catalogManager);
                            if(log.isDebugEnabled()) {
                                log.debug("Loading WSDL using ModuleWSDLLocator from base " +
                                        "location: " + wsdlExplicitURL);
                            }
                            def = (Definition) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                public Object run() throws WSDLException {
                                    WSDLReader reader = getWSDLReader();
                                    return reader.readWSDL(locator);
                                }
                            });
                        }
                    }
                    catch(Exception e) {
                        if(log.isDebugEnabled()) {
                            log.debug("Using ModuleWSDLLocator was not successful for loading " +
                                    "WSDL due to the following error: " + e.toString() + ". The " +
                                    "WSDL will be read from the WSDL location: " + wsdlExplicitURL);
                        }
                    }
                }
                if(def == null) {
                    if(log.isDebugEnabled()) {
                        log.debug("Loading WSDL from location: " + wsdlExplicitURL);
                    }
                    def = (Definition) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws WSDLException {
                            WSDLReader reader = getWSDLReader();
                            return reader.readWSDL(wsdlExplicitURL);
                        }
                    });
                }
                
            } catch (PrivilegedActionException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from AccessController: " + e);
                }
                throw ExceptionFactory.makeWebServiceException(e.getException());
            }
            catch(IOException ioe) {
                if(log.isDebugEnabled()) {
                    log.debug("An error occurred while attempting to load the WSDL " +
                            "file at the following location: " + wsdlExplicitURL);
                }
                throw ExceptionFactory.makeWebServiceException(ioe);
            }
        }


        if (log.isDebugEnabled()) {
            if (def != null) {
                log.debug("loadDefinition() returning a NON-NULL definition");
            } else {
                log.debug("loadDefinition() returning a NULL definition");
            }
        }

        return def;
    }

    public Binding getFirstPortBinding(QName serviceQname) {
        Service service = getService(serviceQname);
        if (service == null) {
            return null;
        }
        Map map = getService(serviceQname).getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        for (Object listObject : map.values()) {
            Port wsdlPort = (Port)listObject;
            return wsdlPort.getBinding();

        }
        return null;

    }

    public String getOperationName(QName serviceQname, QName portQname) {
        Port port = getPort(serviceQname, portQname);
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }

        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            return operation.getName();
        }
        return null;
    }

    private Port getPort(QName serviceQname, QName eprQname) {
        Service service = getService(serviceQname);
        if (service == null) {
            return null;
        }
        return service.getPort(eprQname.getLocalPart());

    }

    public ArrayList getPortBinding(QName serviceQname) {
        Map map = this.getService(serviceQname).getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        ArrayList<Binding> portBindings = new ArrayList<Binding>();
        for (Object listObject : map.values()) {
            Port wsdlPort = (Port)listObject;
            Binding binding = wsdlPort.getBinding();
            if (binding != null) {
                portBindings.add(binding);
            }

        }
        return portBindings;

    }

    public String getPortBinding(QName serviceQname, QName portQname) {
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        return binding.getQName().getLocalPart();
    }

    public String[] getPorts(QName serviceQname) {
        String[] portNames = null;
        Service service = this.getService(serviceQname);
        if (service == null) {
            return null;
        }
        Map map = service.getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        portNames = new String[map.values().size()];
        Iterator iter = map.values().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            Port wsdlPort = (Port)iter.next();
            if (wsdlPort != null) {
                portNames[i] = wsdlPort.getName();
            }
        }
        return portNames;
    }

    public Service getService(QName serviceQname) {
        if (serviceQname == null) {
            return null;
        }

        Definition def = getDefinition();
        if (def != null) {
            return def.getService(serviceQname);
        } else {
            return null;
        }
    }

    public String getSOAPAction(QName serviceQname) {
        Binding binding = getFirstPortBinding(serviceQname);
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            List exElements = operation.getExtensibilityElements();
            for (Object elObj : exElements) {
                ExtensibilityElement exElement = (ExtensibilityElement)elObj;
                if (isSoapOperation(exElement)) {
                    SOAPOperation soapOperation = (SOAPOperation)exElement;
                    return soapOperation.getSoapActionURI();
                }
            }
        }
        return null;
    }

    public String getSOAPAction(QName serviceQname, QName portQname) {
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            List exElements = operation.getExtensibilityElements();
            for (Object elObj : exElements) {
                ExtensibilityElement exElement = (ExtensibilityElement)elObj;
                if (isSoapOperation(exElement)) {
                    SOAPOperation soapOperation = (SOAPOperation)exElement;
                    return soapOperation.getSoapActionURI();
                }
            }
        }
        return null;
    }

    public String getSOAPAction(QName serviceQname, QName portQname, QName operationQname) {
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        if (operations == null) {
            return null;
        }
        BindingOperation operation = null;
        for (Object opObj : operations) {
            operation = (BindingOperation)opObj;
        }
        List exElements = operation.getExtensibilityElements();
        for (Object elObj : exElements) {
            ExtensibilityElement exElement = (ExtensibilityElement)elObj;
            if (isSoapOperation(exElement)) {
                SOAPOperation soapOperation = (SOAPOperation)exElement;
                if (soapOperation.getElementType().equals(operationQname)) {
                    return soapOperation.getSoapActionURI();
                }
            }
        }

        return null;
    }

    public URL getWSDLLocation() {
        return this.wsdlURL;
    }

    private boolean isSoapOperation(ExtensibilityElement exElement) {
        return WSDLWrapper.SOAP_11_OPERATION.equals(exElement.getElementType());
        //TODO: Add Soap12 support later
        // || WSDLWrapper.SOAP_12_OPERATION.equals(exElement.getElementType());
    }

    public String getTargetNamespace() {
        Definition def = getDefinition();
        if (def != null) {
            return def.getTargetNamespace();
        } else {
            return null;
        }
    }
    
    /**
     * This method provides a Java2 Security compliant way to obtain the InputStream
     * for a given URLConnection object. This is needed as a given URLConnection object
     * may be an instance of a FileURLConnection object which would require access 
     * permissions if Java2 Security was enabled.
     */
    private InputStream getInputStream(URLConnection urlCon) throws Exception {
        final URLConnection finalURLCon = urlCon;
        InputStream is = null;
        try {
            is = (InputStream) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            return finalURLCon.getInputStream();
                        }
                    });
        }
        catch(PrivilegedActionException e) {
            throw e.getException();
        }
        return is;
    }

    private void debugMemoryParms(ConfigurationContext configContext) {
        if (configContext != null) {
            AxisConfiguration axisCfg = configContext.getAxisConfiguration();

            boolean reduceWSDLMemoryCache = false;
            int reduceWSDLMemoryType = 9;
            // determine what the setting for the memory optimization is
            if (axisCfg != null) {
                Parameter param = axisCfg.getParameter(Constants.Configuration.REDUCE_WSDL_MEMORY_CACHE);

                reduceWSDLMemoryCache =
                    param != null && ((String) param.getValue()).equalsIgnoreCase("true");


                param = axisCfg.getParameter(Constants.Configuration.REDUCE_WSDL_MEMORY_TYPE);

                if (param != null) {
                    String value = (String) param.getValue();

                    if (value != null) {
                        Integer i = new Integer(value);
                        reduceWSDLMemoryType = i.intValue();
                    }
                }
                log.debug("reduceWSDLMemoryCache:"+ reduceWSDLMemoryCache + ", reduceWSDLMemoryType:" + reduceWSDLMemoryType );
            } else {
                log.debug("AxisConfiguration is null");
            }
        } else {
            log.debug("ConfigContext is null");
        }
    }

}
