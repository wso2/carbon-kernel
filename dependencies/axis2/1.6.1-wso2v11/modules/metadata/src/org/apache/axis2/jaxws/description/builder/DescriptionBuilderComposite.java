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

/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class DescriptionBuilderComposite implements TMAnnotationComposite, TMFAnnotationComposite {

    private static final Log log = LogFactory.getLog(DescriptionBuilderComposite.class);

    /*
      * This structure contains the full reflected class, as well as, the
      * possible annotations found for this class...the class description
      * must be complete enough for full validation between class info and annotations
      * The annotations will be added to the corresponding class members.
      */

    public DescriptionBuilderComposite() {
        this((ConfigurationContext)null);
    }

    public DescriptionBuilderComposite(ConfigurationContext configContext) {
        myConfigContext = configContext;
        methodDescriptions = new ArrayList<MethodDescriptionComposite>();
        fieldDescriptions = new ArrayList<FieldDescriptionComposite>();
        webServiceRefAnnotList = new ArrayList<WebServiceRefAnnot>();
        interfacesList = new ArrayList<String>();
        genericAnnotationInstances = new ArrayList<CustomAnnotationInstance>();
        genericAnnotationProcessors = new HashMap<String, CustomAnnotationProcessor>();
        properties = new HashMap<String, Object>();
    }

    //Note: a WSDL is not necessary
    private Definition wsdlDefinition = null;
    private URL wsdlURL = null;
    private WSDL4JWrapper wsdlWrapper = null;

    private ConfigurationContext myConfigContext;
    
    // Class-level annotations
    private WebServiceAnnot webServiceAnnot;
    private WebServiceProviderAnnot webServiceProviderAnnot;
    private ServiceModeAnnot serviceModeAnnot;
    private WebServiceClientAnnot webServiceClientAnnot;
    private WebFaultAnnot webFaultAnnot;
    private HandlerChainAnnot handlerChainAnnot;
    private SoapBindingAnnot soapBindingAnnot;
    private List<WebServiceRefAnnot> webServiceRefAnnotList;
    private BindingTypeAnnot bindingTypeAnnot;
    
    // Collection of PortComposite objects which were created from
    // this DescriptionBuilderComposite instance
    private List<PortComposite> portCompositeList = new ArrayList<PortComposite>();
    
    private List<Annotation> features;
    
    private Map<QName, Definition> wsdlDefs = new HashMap<QName, Definition>();
    
    private Map<QName, URL> wsdlURLs = new HashMap<QName, URL>();
    
    private Set<QName> serviceQNames = new HashSet<QName>();
    
    private Map<QName, List<PortComposite>> sQNameToPC = new HashMap<QName, List<PortComposite>>();
    
    // Class information
    private String className;
    /**
     * Get an annotation by introspecting on a class.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotationFromClass(final Class cls, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                
                Annotation a = cls.getAnnotation(annotation);
                return a;
            }
        });
    }

    private String[] classModifiers; //public, abstract, final, strictfp...
    private String extendsClass;    //Set to the name of the super class
    private List<String> interfacesList; //Set this for all implemented interfaces
    private boolean isInterface = false;
    private QName preferredPort;        // Port to use if no port QName given.  May be null
    private boolean isMTOMEnabled = false;
    
    private List<MethodDescriptionComposite> methodDescriptions;
    private List<FieldDescriptionComposite> fieldDescriptions;
    
    // we can keep these in a singular list b/c for a given type-level annotation
    // there can only one instance of the annotation
    private List<CustomAnnotationInstance> genericAnnotationInstances;
    
    // a map that stores all the type-targetted GenericAnnotationProcessor instances
    private Map<String, CustomAnnotationProcessor> genericAnnotationProcessors;

    private WsdlGenerator wsdlGenerator;
    private ClassLoader classLoader;
    
    // JAXB object used to represent handler chain configuration info
    // either this or the HandlerChainAnnot may be present, but 
    // not both, they may both be null
    private HandlerChainsType handlerChainsType = null;
    
    // Does this composite represent a service requester or service provider.
    // We default to service provider since composites were orginally not used by requesters.
    private boolean isServiceProvider = true;
    
    // For a service requester, this will be the client-side class associated with this composite; 
    // It could be the Service class or the SEI class.  On the service provider this will be null
    // unless the deprecated service construction logic in DescriptionFactory was used.
    private Class theCorrespondingClass;
    
    // Service-requesters (aka clients) can specify a sprase composite that may contain annotation
    // information corresponding to information in a deployment descriptor or an injected 
    // resource.
    private WeakHashMap<Object, DescriptionBuilderComposite> sparseCompositeMap = new WeakHashMap<Object, DescriptionBuilderComposite>();
    
    // Allow a unique XML CatalogManager per service description.
    private JAXWSCatalogManager catalogManager = null;
    
    // This is a bag of properties that apply to the DBC. Currently these properties will be
    // copied over from the DBC to the description hierarchy. This will only occur on the
    // server-side for now
    private Map<String, Object> properties = null;
    
    public void setSparseComposite(Object key, DescriptionBuilderComposite sparseComposite) {
        if (key != null && sparseComposite != null) {
            this.sparseCompositeMap.put(key, sparseComposite);
        }
    }
    public DescriptionBuilderComposite getSparseComposite(Object key) {
        return sparseCompositeMap.get(key);
    }

    /**
     * For a service requester, set the QName of the preferred port for this service.  This
     * indicates which port (i.e. which EndpointDescription) should be returned if a port QName
     * isn't specified.  This may be null, indicating the first valid port in the WSDL should be
     * returned.
     * 
     * @param preferredPort
     */
    public void setPreferredPort(QName preferredPort) {
        this.preferredPort = preferredPort;
    }
    
    /**
     * For a service requester, the QName of the prefered port for this service.  This indicates
     * which port should be returned if a port QName wasn't specified.  This may be null, 
     * indicating the first valid port in the WSDL should be returned.
     * @return
     */
    public QName getPreferredPort() {
        return preferredPort;
    }
    public QName getPreferredPort(Object key) {
        QName returnPreferredPort = null;
        // See if there's a sparse composite override for this composite
        if (key != null) {
            DescriptionBuilderComposite sparse = getSparseComposite(key);
            if (sparse != null 
                && !DescriptionBuilderUtils.isEmpty(sparse.getPreferredPort())) {
                returnPreferredPort = sparse.getPreferredPort();
            } else {
                returnPreferredPort = getPreferredPort();
            }
        } else {
            returnPreferredPort = getPreferredPort();
        }
        
        return returnPreferredPort;
        
    }
    
    public void setIsMTOMEnabled(boolean isMTOMEnabled) {
        this.isMTOMEnabled = isMTOMEnabled;
    }
    
    public boolean isMTOMEnabled() {
        return isMTOMEnabled;
    }
    
    public boolean isMTOMEnabled(Object key) {
        boolean returnIsMTOMEnabled = false;
        if (key != null) {
            DescriptionBuilderComposite sparseDBC = getSparseComposite(key);
            if (sparseDBC != null && sparseDBC.isMTOMEnabled()) {
                returnIsMTOMEnabled = sparseDBC.isMTOMEnabled();
            } else {
                returnIsMTOMEnabled = isMTOMEnabled();
            }
            
        } else {
            returnIsMTOMEnabled = isMTOMEnabled();
        }
        
        return returnIsMTOMEnabled;
    }
    
    // Methods
    public WebServiceAnnot getWebServiceAnnot() {
        return webServiceAnnot = 
            (WebServiceAnnot) getCompositeAnnotation(webServiceAnnot,
                                                     WebServiceAnnot.class,
                                                     javax.jws.WebService.class);
    }
    
    /** @return Returns the classModifiers. */
    public String[] getClassModifiers() {
        return classModifiers;
    }

    /** @return Returns the className. */
    public String getClassName() {
        if (className != null) {
            return className;
        }
        else if (theCorrespondingClass != null) {
            return theCorrespondingClass.getName();
        }
        else {
            return null;
        }
    }
    

    /** @return Returns the super class name. */
    public String getSuperClassName() {
        return extendsClass;
    }

    /** @return Returns the list of implemented interfaces. */
    public List<String> getInterfacesList() {
        return interfacesList;
    }

    /** @return Returns the handlerChainAnnotImpl. */
    public HandlerChainAnnot getHandlerChainAnnot() {
        return handlerChainAnnot = 
            (HandlerChainAnnot) getCompositeAnnotation(handlerChainAnnot,
                                                       HandlerChainAnnot.class,
                                                       javax.jws.HandlerChain.class);
    }

    /** @return Returns the serviceModeAnnot. */
    public ServiceModeAnnot getServiceModeAnnot() {
        return serviceModeAnnot = 
            (ServiceModeAnnot) getCompositeAnnotation(serviceModeAnnot,
                                                      ServiceModeAnnot.class,
                                                      javax.xml.ws.ServiceMode.class);
    }

    /** @return Returns the soapBindingAnnot. */
    public SoapBindingAnnot getSoapBindingAnnot() {
        return soapBindingAnnot = 
            (SoapBindingAnnot) getCompositeAnnotation(soapBindingAnnot,
                                                      SoapBindingAnnot.class,
                                                      javax.jws.soap.SOAPBinding.class);
    }

    /** @return Returns the webFaultAnnot. */
    public WebFaultAnnot getWebFaultAnnot() {
        return webFaultAnnot = 
            (WebFaultAnnot) getCompositeAnnotation(webFaultAnnot, 
                                                   WebFaultAnnot.class, 
                                                   javax.xml.ws.WebFault.class);
    }

    /** @return Returns the webServiceClientAnnot. */
    public WebServiceClientAnnot getWebServiceClientAnnot() {
        return webServiceClientAnnot = 
            (WebServiceClientAnnot) getCompositeAnnotation(webServiceClientAnnot, 
                                                           WebServiceClientAnnot.class,
                                                           javax.xml.ws.WebServiceClient.class);
    }
    
    public WebServiceClientAnnot getWebServiceClientAnnot(Object key) {
        WebServiceClientAnnot annot = getWebServiceClientAnnot();
        DescriptionBuilderComposite sparseComposite = getSparseComposite(key);
        WebServiceClientAnnot sparseAnnot = null;
        if (sparseComposite != null) {
            sparseAnnot = sparseComposite.getWebServiceClientAnnot();
        }
        return WebServiceClientAnnot.createFromAnnotation(annot, sparseAnnot);
    }
    
    /**
     * Return a composite annotation of the specified type.  If the composite annotation is 
     * null, then the associated class (if not null) will be examined for the appropriate java
     * annotation.  If one is found, it will be used to create a new composite annotation.
     * 
     * @param compositeAnnotation May be null.  The current composite annotation.  If this is
     * non-null, it will simply be returned.
     * @param compositeAnnotClass The class of the composite annotation.  This is a subclass of
     * the java annotation class.
     * @param javaAnnotationClass The java annotation class.  The associated class will be 
     * reflected on to see if this annotation exists.  If so, it is used to create an instance of
     * the composite annotation class.
     * @return
     */
    private Annotation getCompositeAnnotation(Annotation compositeAnnotation,
                                              Class compositeAnnotClass,
                                              Class javaAnnotationClass) {
        Annotation returnAnnotation = compositeAnnotation;
        if (returnAnnotation == null && theCorrespondingClass != null) {
            // Get the annotation from the class and if one exists, construct a composite annot for it
            Annotation annotationFromClass = getAnnotationFromClass(theCorrespondingClass, javaAnnotationClass);
            if (annotationFromClass != null) {
                try {
                    Method createAnnot = compositeAnnotClass.getMethod("createFromAnnotation", Annotation.class);
                    returnAnnotation = (Annotation) createAnnot.invoke(null, annotationFromClass);
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to create composite annotation due to exception."
                                  + "  Composite Annotation: " + compositeAnnotation
                                  + "; Composite Annot class: " + compositeAnnotClass 
                                  + "; Java Annot class: " + javaAnnotationClass, e);
                    }
                    String msg = Messages.getMessage("DescriptionBuilderErr1", 
                                                     compositeAnnotClass.toString(),
                                                     e.toString());
                    throw ExceptionFactory.makeWebServiceException(msg, e);
                }
            }
        }
        return returnAnnotation;
    }

    /** @return Returns the webServiceProviderAnnot. */
    public WebServiceProviderAnnot getWebServiceProviderAnnot() {
        return webServiceProviderAnnot = 
            (WebServiceProviderAnnot) getCompositeAnnotation(webServiceProviderAnnot,
                                                             WebServiceProviderAnnot.class, 
                                                             javax.xml.ws.WebServiceProvider.class);
    }

    /** @return Returns the webServiceRefAnnot list. */
    public List<WebServiceRefAnnot> getAllWebServiceRefAnnots() {
        return webServiceRefAnnotList;
    }

    /** @return Returns the webServiceRefAnnot. */
    public WebServiceRefAnnot getWebServiceRefAnnot(String name) {

        WebServiceRefAnnot wsra = null;
        Iterator<WebServiceRefAnnot> iter =
                webServiceRefAnnotList.iterator();

        while (iter.hasNext()) {
            wsra = iter.next();
            if (wsra.name().equals(name))
                return wsra;
        }
        return wsra;
    }

    /** @return Returns the webServiceRefAnnot. */
    public BindingTypeAnnot getBindingTypeAnnot() {
        return (BindingTypeAnnot) getCompositeAnnotation(bindingTypeAnnot,
                                                         BindingTypeAnnot.class,
                                                         javax.xml.ws.BindingType.class);
    }
    
    public List<Annotation> getWebServiceFeatures() {
        return features;
    }
    
    public void setWebServiceFeatures(List<Annotation> list) {
        features = list;
    }
    
    public void addWebServiceFeature(Annotation a) {
        if (features == null)
            features = new ArrayList<Annotation>();
        
        features.add(a);
    }
    
    /** @return Returns the wsdlDefinition */
    public Definition getWsdlDefinition() {
        if (wsdlDefinition != null) {
            return wsdlDefinition;
        } else if (wsdlWrapper != null) {
            wsdlDefinition = wsdlWrapper.getDefinition();
        } else {
            wsdlDefinition = createWsdlDefinition(wsdlURL);
        }
        return wsdlDefinition;
    }

    /** @return Returns the wsdlURL */
    public URL getWsdlURL() {
        return this.wsdlURL;
    }

    /** Returns a collection of all MethodDescriptionComposites that match the specified name */
    public List<MethodDescriptionComposite> getMethodDescriptionComposite(String methodName) {
        ArrayList<MethodDescriptionComposite> matchingMethods =
                new ArrayList<MethodDescriptionComposite>();
        Iterator<MethodDescriptionComposite> iter = methodDescriptions.iterator();
        while (iter.hasNext()) {
            MethodDescriptionComposite composite = iter.next();

            if (composite.getMethodName() != null) {
                if (composite.getMethodName().equals(methodName)) {
                    matchingMethods.add(composite);
                }
            }
        }

        return matchingMethods;
    }

    /**
     * Returns the nth occurence of this MethodComposite. Since method names are not unique, we have
     * to account for multiple occurrences
     *
     * @param methodName
     * @param occurence  The nth occurance to return; not this is NOT 0 based
     * @return Returns the methodDescriptionComposite
     */
    public MethodDescriptionComposite getMethodDescriptionComposite(
            String methodName,
            int occurence) {
        MethodDescriptionComposite returnMDC = null;
        List<MethodDescriptionComposite> matchingMethods =
                getMethodDescriptionComposite(methodName);
        if (matchingMethods != null && !matchingMethods.isEmpty() &&
                occurence > 0 && occurence <= matchingMethods.size()) {
            returnMDC = matchingMethods.get(--occurence);
        }
        return returnMDC;
    }

    public List<MethodDescriptionComposite> getMethodDescriptionsList() {
        return methodDescriptions;
    }

    /** @return Returns the methodDescriptionComposite..null if not found */
    public FieldDescriptionComposite getFieldDescriptionComposite(String fieldName) {

        FieldDescriptionComposite composite = null;
        Iterator<FieldDescriptionComposite> iter =
                fieldDescriptions.iterator();

        while (iter.hasNext()) {
            composite = iter.next();
            if (composite.getFieldName().equals(fieldName))
                return composite;
        }
        return composite;
    }

    /** @return Returns the ModuleClassType. */
    public WsdlGenerator getCustomWsdlGenerator() {

        return this.wsdlGenerator;
    }

    /** @return Returns the ClassLoader. */
    public ClassLoader getClassLoader() {

        return this.classLoader;
    }

    /** @return Returns true if this is an interface */
    public boolean isInterface() {

        return isInterface;
    }

    //++++++++
    //Setters
    //++++++++
    public void setWebServiceAnnot(WebServiceAnnot webServiceAnnot) {
        this.webServiceAnnot = webServiceAnnot;
    }

    /** @param classModifiers The classModifiers to set. */
    public void setClassModifiers(String[] classModifiers) {
        this.classModifiers = classModifiers;
    }

    /** @param className The className to set. */
    public void setClassName(String className) {
        this.className = className;
    }

    /** @param extendsClass The name of the super class to set. */
    public void setSuperClassName(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    /** @param interfacesList The interfacesList to set. */
    public void setInterfacesList(List<String> interfacesList) {
        this.interfacesList = interfacesList;
    }

    /** @param handlerChainAnnot The handlerChainAnnot to set. */
    public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
        this.handlerChainAnnot = handlerChainAnnot;
    }

    /** @param serviceModeAnnot The serviceModeAnnot to set. */
    public void setServiceModeAnnot(ServiceModeAnnot serviceModeAnnot) {
        this.serviceModeAnnot = serviceModeAnnot;
    }

    /** @param soapBindingAnnot The soapBindingAnnot to set. */
    public void setSoapBindingAnnot(SoapBindingAnnot soapBindingAnnot) {
        this.soapBindingAnnot = soapBindingAnnot;
    }

    /** @param webFaultAnnot The webFaultAnnot to set. */
    public void setWebFaultAnnot(WebFaultAnnot webFaultAnnot) {
        this.webFaultAnnot = webFaultAnnot;
    }

    /** @param webServiceClientAnnot The webServiceClientAnnot to set. */
    public void setWebServiceClientAnnot(
            WebServiceClientAnnot webServiceClientAnnot) {
        this.webServiceClientAnnot = webServiceClientAnnot;
    }

    /** @param webServiceProviderAnnot The webServiceProviderAnnot to set. */
    public void setWebServiceProviderAnnot(
            WebServiceProviderAnnot webServiceProviderAnnot) {
        this.webServiceProviderAnnot = webServiceProviderAnnot;
    }

    /** @param webServiceRefAnnot The webServiceRefAnnot to add to the list. */
    public void addWebServiceRefAnnot(
            WebServiceRefAnnot webServiceRefAnnot) {
        webServiceRefAnnotList.add(webServiceRefAnnot);
    }

    public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
        addWebServiceRefAnnot(webServiceRefAnnot);
    }
    
    public void addCustomAnnotationProcessor(CustomAnnotationProcessor processor) {
        genericAnnotationProcessors.put(processor.getAnnotationInstanceClassName(), processor);
    }
    
    public Map<String, CustomAnnotationProcessor> getCustomAnnotationProcessors() {
        return genericAnnotationProcessors;
    }
    
    public void addCustomAnnotationInstance(CustomAnnotationInstance annotation) {
        genericAnnotationInstances.add(annotation);
    }
    
    public List<CustomAnnotationInstance> getCustomAnnotationInstances() {
        return genericAnnotationInstances;
    }


    /**
     * @param wsdlDefinition The wsdlDefinition to set.
     */
    public void setWsdlDefinition(Definition wsdlDef) {

        Definition def = null;

        if (wsdlDef != null) {
            if (wsdlDef instanceof WSDL4JWrapper) {
                wsdlWrapper = (WSDL4JWrapper) wsdlDef;

                def = wsdlWrapper.getDefinition();
            } else {
                try {
                    if (myConfigContext != null) {
                        // Construct WSDL4JWrapper with configuration information
                        wsdlWrapper = new WSDL4JWrapper(wsdlDef, 
                                                        myConfigContext);
                    } else {
                        // If there is no configuration, default to using a 
                        // memory sensitive wrapper
                        wsdlWrapper = new WSDL4JWrapper(wsdlDef, true, 2);
                    }
                    def = wsdlWrapper.getDefinition();
                } catch (Exception ex) {
                    // absorb
                }
            }

            if (def != null) {
                String wsdlDefinitionBaseURI = def.getDocumentBaseURI();

                if ((wsdlDefinitionBaseURI != null) && (wsdlURL == null)) {
                    try {
                        wsdlURL = new URL(wsdlDefinitionBaseURI);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("DescriptionBuilderComposite:setWsdlDefinition(): "
                                    +"Caught exception creating WSDL URL :" 
                                    + wsdlDefinitionBaseURI + "; exception: " 
                                    +e.toString(),e); 
                        }
                    }
                }
            }
        }
    }

    /** @param wsdlURL The wsdlURL to set. */
    public void setwsdlURL(URL wsdlURL) {
        this.wsdlURL = wsdlURL;
    }

    /** @param BindingTypeAnnot The BindingTypeAnnot to set. */
    public void setBindingTypeAnnot(
            BindingTypeAnnot bindingTypeAnnot) {
        this.bindingTypeAnnot = bindingTypeAnnot;
    }

    /** @param isInterface Sets whether this composite represents a class or interface */
    public void setIsInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    /** @param methodDescription The methodDescription to add to the set. */
    public void addMethodDescriptionComposite(MethodDescriptionComposite methodDescription) {
        methodDescriptions.add(methodDescription);
    }

    /** @param methodDescription The methodDescription to add to the set. */
    public void addFieldDescriptionComposite(FieldDescriptionComposite fieldDescription) {
        fieldDescriptions.add(fieldDescription);
    }

    public void setCustomWsdlGenerator(WsdlGenerator wsdlGenerator) {

        this.wsdlGenerator = wsdlGenerator;
    }

    public void setClassLoader(ClassLoader classLoader) {

        this.classLoader = classLoader;
    }
    
    public HandlerChainsType getHandlerChainsType() {
    	return handlerChainsType;
    }
    
    public void setHandlerChainsType(HandlerChainsType handlerChainsType) {
    	this.handlerChainsType = handlerChainsType;
    }
    
    /**
     * Answer does this composite represent a service requester (aka client) or a service
     * provider (aka server).
     * 
     * @return true if this is a service provider (aka an endpoint or a service implementation
     * or a server)
     * 
     */
    public boolean isServiceProvider() {
        return isServiceProvider;
    }

    /**
     * Set the indication of whether this composite represents a service requester (aka client) or
     * a service provider (aka server).
     */
    public void setIsServiceProvider(boolean value) {
        isServiceProvider = value;
    }
    
    /**
     * Set the class associated with this composite.  For a service requester, this could be the
     * Service class or the SEI class.  For a service provider this will be null (unless the 
     * deprecated service construction logic in DescriptionFactory is used)
     * @param theClass
     */
    public void setCorrespondingClass(Class theClass) {
        this.theCorrespondingClass = theClass;
    }
    
    /**
     * Returns the corresponding class associated with this composite, if any.
     * @return
     */
    public Class getCorrespondingClass() {
        return theCorrespondingClass;
    }

    /**
     * Set the Catalog Manager associated with this composite.  
     * @param theCatalogManger
     */
    public void setCatalogManager(JAXWSCatalogManager theCatalogManager) {
    	this.catalogManager = theCatalogManager;
    }
    
    /**
     * Returns the catalog manager associated with this composite, if any.
     * @return
     */
    public JAXWSCatalogManager getCatalogManager() {
    	return catalogManager;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Store a WSDL Definition keyed by a service QName
     */
    public void setWsdlDefinition(QName serviceQName, Definition definition) {
        this.wsdlDefs.put(serviceQName, definition);
    }
    
    /**
     * Retrive a WSDL Definition by a service QName
     */
    public Definition getWsdlDefinition(QName serviceQName) {
        return wsdlDefs.get(serviceQName);
    }
    
    /**
     * Store a WSDL URL keyed by a service QName
     */
    public void setwsdlURL(QName serviceQName, URL url) {
        wsdlURLs.put(serviceQName, url);
    }
    
    /**
     * Retrive a WSDL URL by a service QName
     */
    public URL getWsdlURL(QName serviceQName) {
        return wsdlURLs.get(serviceQName);
    }
    
    /**
     * Add the set of wsdl:service QNames that are represented by this DBC's metadata
     */
    public void setServiceQNames(Set<QName> serviceQNames) {
        this.serviceQNames = serviceQNames;
    }
    
    /**
     * Get the set of wsdl:service QNames represented by this DBC's metadata
     * @return
     */
    public Set<QName> getServiceQNames() {
        return serviceQNames;
    }
    

    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */

    public String toString() {
        StringBuffer sb = new StringBuffer();
        final String newLine = "\n";
        final String sameLine = "; ";
        sb.append(super.toString());
        sb.append(newLine);
        sb.append("ClassName: " + className);
        sb.append(sameLine);
        sb.append("SuperClass:" + extendsClass);

        sb.append(newLine);
        sb.append("Class modifiers: ");
        if (classModifiers != null) {
            for (int i = 0; i < classModifiers.length; i++) {
                sb.append(classModifiers[i]);
                sb.append(sameLine);
            }
        }

        sb.append(newLine);
        sb.append("is Service Provider: " + isServiceProvider() );

        sb.append(newLine);
        sb.append("wsdlURL: " + getWsdlURL() );

        sb.append(newLine);
        sb.append("has wsdlDefinition?: ");
        if (wsdlDefinition !=null) {
            sb.append("true");
        } else {
            sb.append("false");
        }

        sb.append(newLine);
        sb.append("Interfaces: ");
        Iterator<String> intIter = interfacesList.iterator();
        while (intIter.hasNext()) {
            String inter = intIter.next();
            sb.append(inter);
            sb.append(sameLine);
        }

        if (webServiceAnnot != null) {
            sb.append(newLine);
            sb.append("WebService: ");
            sb.append(webServiceAnnot.toString());
        }

        if (webServiceProviderAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceProvider: ");
            sb.append(webServiceProviderAnnot.toString());
        }

        if (bindingTypeAnnot != null) {
            sb.append(newLine);
            sb.append("BindingType: ");
            sb.append(bindingTypeAnnot.toString());
        }

        if (webServiceClientAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceClient: ");
            sb.append(webServiceClientAnnot.toString());
        }

        if (webFaultAnnot != null) {
            sb.append(newLine);
            sb.append("WebFault: ");
            sb.append(webFaultAnnot.toString());
        }

        if (serviceModeAnnot != null) {
            sb.append(newLine);
            sb.append("ServiceMode: ");
            sb.append(serviceModeAnnot.toString());
        }

        if (soapBindingAnnot != null) {
            sb.append(newLine);
            sb.append("SOAPBinding: ");
            sb.append(soapBindingAnnot.toString());
        }

        if (handlerChainAnnot != null) {
            sb.append(newLine);
            sb.append("HandlerChain: ");
            sb.append(handlerChainAnnot.toString());
        }

        if (webServiceRefAnnotList.size() > 0) {
            sb.append(newLine);
            sb.append("Number of WebServiceRef:  " + webServiceRefAnnotList.size());
            Iterator<WebServiceRefAnnot> wsrIter = webServiceRefAnnotList.iterator();
            while (wsrIter.hasNext()) {
                WebServiceRefAnnot wsr = wsrIter.next();
                sb.append(wsr.toString());
                sb.append(sameLine);
            }
        }

        sb.append(newLine);
        sb.append("Number of Method Descriptions: " + methodDescriptions.size());
        Iterator<MethodDescriptionComposite> mdcIter = methodDescriptions.iterator();
        while (mdcIter.hasNext()) {
            sb.append(newLine);
            MethodDescriptionComposite mdc = mdcIter.next();
            sb.append(mdc.toString());
        }

        sb.append(newLine);
        sb.append("Number of Field Descriptions: " + fieldDescriptions.size());
        Iterator<FieldDescriptionComposite> fdcIter = fieldDescriptions.iterator();
        while (fdcIter.hasNext()) {
            sb.append(newLine);
            FieldDescriptionComposite fdc = fdcIter.next();
			sb.append(fdc.toString());
		}

        if (features != null && !features.isEmpty()) {
            sb.append(newLine);
            sb.append(newLine);
            sb.append("WebService Feature Objects (as annotations):");
            sb.append(newLine);
            for (Annotation annotation : features) {
                sb.append(annotation.toString());
                sb.append(newLine);
            }
        }
        
        if(portCompositeList != null
                &&
                !portCompositeList.isEmpty()) {
            sb.append(newLine);
            sb.append(newLine);
            sb.append("** PortComposite Objects**");
            sb.append(newLine);
            for(PortComposite pc : portCompositeList) {
                sb.append("PortComposite");
                sb.append(newLine);
                sb.append(pc.toString());
                sb.append(newLine);
            }
        }
        
		return sb.toString();
    }


    /**
     * Create a wsdl definition from the supplied 
     * location.
     * 
     * @param _wsdlURL The URL where the wsdl is located
     * @return The WSDL Definition or NULL
     */
    private Definition createWsdlDefinition(URL _wsdlURL) {
        if (_wsdlURL == null) {
            return null;
        }

        Definition wsdlDef = null;
        try {
            if (log.isDebugEnabled() ) {
                log.debug("new WSDL4JWrapper(" + _wsdlURL.toString() + ",ConfigurationContext" ); 
            }
            
            wsdlWrapper = new WSDL4JWrapper(_wsdlURL, myConfigContext);
            
            if (wsdlWrapper != null) {
                wsdlDef = wsdlWrapper.getDefinition();
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("DescriptionBuilderComposite:createWsdlDefinition("
                        + _wsdlURL.toString()
                        + "): Caught exception trying to create WSDL Definition: "
                        +e, e); 
            }
        }

        return wsdlDef;
    }

    public ConfigurationContext getConfigurationContext() {
        return myConfigContext;
    }
    
    /**
     * Adds a PortComposite to the generic list. This list of PortComposite objects
     * is not keyed by wsdl:service QName.
     */
    public void addPortComposite(PortComposite portDBC) {
        portCompositeList.add(portDBC);
    }
    
    /**
     * Adds a PortComposite to a list that is keyed by a wsdl:service QName.
     */
    public void addPortComposite(QName serviceQName, PortComposite portDBC) {
        List<PortComposite> pcList = sQNameToPC.get(serviceQName);
        if(pcList == null) {
            pcList = new LinkedList<PortComposite>();
            sQNameToPC.put(serviceQName, pcList);
        }
        pcList.add(portDBC);
    }
    
    /**
     * Gets the generic PortComposite instances.
     */
    public List<PortComposite> getPortComposites() {
        return portCompositeList;
    }
    
    /**
     * Gets all the PortComposite instances associated with a particular wsdl:service QName.
     * @return
     */
    public List<PortComposite> getPortComposites(QName serviceQName) {
        return sQNameToPC.get(serviceQName);
    }
    
    
    /**
     * Static utility method that, given a sparse composite, returns the SERVICE_REF_NAME value from the property 
     * on that sparse composite or null if the property was not specified.
     * @param sparseComposite The sparse composite instance to get the SERVICE_REF_NAME parameter from
     * @return A String containing the Service Ref Name or null if the parameter was not found.
     */
    public static String getServiceRefName(DescriptionBuilderComposite sparseComposite) {
        String serviceRefName = null;
        if (sparseComposite != null) {
            serviceRefName = (String) sparseComposite.getProperties().get(MDQConstants.SERVICE_REF_NAME);
        }
        return serviceRefName;
    }
    
    /**
     * For the current composite, return the serivce ref name from the sparse composite associted with the service 
     * delegate key.
     * @param serviceDelegateKey The instance of the service delegate associated with the sparse composite from which
     * the service ref name is to be retrieved.
     * @return The service ref name associated with the service delegate key or null if one was not found.
     */
    public String getServiceRefName(Object serviceDelegateKey) {
        String serviceRefName = null;
        if (serviceDelegateKey != null) {
            serviceRefName = getServiceRefName(getSparseComposite(serviceDelegateKey));
        }
        return serviceRefName;
    }
    
}
