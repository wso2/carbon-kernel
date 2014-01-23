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

package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.AttachmentType;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import static org.apache.axis2.jaxws.description.builder.MDQConstants.CONSTRUCTOR_METHOD;

import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Operation;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.SOAPBinding;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/** Utilities used throughout the Description package. */
public class DescriptionUtils {
    private static final Log log = LogFactory.getLog(DescriptionUtils.class);

    static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }

    static boolean isEmpty(QName qname) {
        return qname == null || isEmpty(qname.getLocalPart());
    }

    /** @return Returns TRUE if we find just one WebMethod Annotation with exclude flag set to false */
    static boolean falseExclusionsExist(DescriptionBuilderComposite dbc) {
        MethodDescriptionComposite mdc = null;
        Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator();

        while (iter.hasNext()) {
            mdc = iter.next();

            WebMethodAnnot wma = mdc.getWebMethodAnnot();
            if (wma != null) {
                if (wma.exclude() == false)
                    return true;
            }
        }

        return false;
    }

    /**
     * Gathers all MethodDescriptionCompsite's that contain a WebMethod Annotation with the exclude
     * set to FALSE
     *
     * @return Returns List<MethodDescriptionComposite>
     */
    static ArrayList<MethodDescriptionComposite> getMethodsWithFalseExclusions(
            DescriptionBuilderComposite dbc) {
        ArrayList<MethodDescriptionComposite> mdcList = new ArrayList<MethodDescriptionComposite>();
        Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator();

        if (DescriptionUtils.falseExclusionsExist(dbc)) {
            while (iter.hasNext()) {
                MethodDescriptionComposite mdc = iter.next();
                if (mdc.getWebMethodAnnot() != null) {
                    if (mdc.getWebMethodAnnot().exclude() == false) {
                        mdc.setDeclaringClass(dbc.getClassName());
                        mdcList.add(mdc);
                    }
                }
            }
        }

        return mdcList;
    }
    
    static boolean isWebmethodDefined(DescriptionBuilderComposite dbc){
        if(dbc == null){
            return false;
        }
        Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator();
        while (iter.hasNext()) {
            MethodDescriptionComposite mdc = iter.next();
            if (mdc!=null && mdc.getWebMethodAnnot() != null) {
                return true;
            }
        }
        return false;
    }
    
    /*
      * Check whether a MethodDescriptionComposite contains a WebMethod annotation with
      * exlude set to true
      */
    static boolean isExcludeTrue(MethodDescriptionComposite mdc) {

        if (mdc.getWebMethodAnnot() != null) {
            if (mdc.getWebMethodAnnot().exclude() == true) {
                return true;
            }
        }

        return false;
    }

    static String javifyClassName(String className) {
        if (className.indexOf("/") != -1) {
            return className.replaceAll("/", ".");
        }
        return className;
    }

    /**
     * Return the name of the class without any package qualifier.
     *
     * @param theClass
     * @return the name of the class sans package qualification.
     */
    static String getSimpleJavaClassName(String name) {
        String returnName = null;

        if (name != null) {
            String fqName = name;

            // We need the "simple name", so strip off any package information from the name
            int endOfPackageIndex = fqName.lastIndexOf('.');
            int startOfClassIndex = endOfPackageIndex + 1;
            returnName = fqName.substring(startOfClassIndex);
        }
        return returnName;
    }

    /**
     * Returns the package name from the class.  If no package, then returns null
     *
     * @param theClassName
     * @return
     */
    static String getJavaPackageName(String theClassName) {
        String returnPackage = null;
        if (theClassName != null) {
            String fqName = theClassName;
            // Get the package name, if there is one
            int endOfPackageIndex = fqName.lastIndexOf('.');
            if (endOfPackageIndex >= 0) {
                returnPackage = fqName.substring(0, endOfPackageIndex);
            }
        }
        return returnPackage;
    }

    /**
     * Create a JAX-WS namespace based on the package name
     *
     * @param packageName
     * @param protocol
     * @return
     */
    static final String NO_PACKAGE_HOST_NAME = "DefaultNamespace";

    static String makeNamespaceFromPackageName(String packageName, String protocol) {
        if (DescriptionUtils.isEmpty(protocol)) {
            protocol = "http";
        }
        if (DescriptionUtils.isEmpty(packageName)) {
            return protocol + "://" + NO_PACKAGE_HOST_NAME;
        }
        StringTokenizer st = new StringTokenizer(packageName, ".");
        String[] words = new String[ st.countTokens() ];
        for (int i = 0; i < words.length; ++i)
            words[i] = st.nextToken();

        StringBuffer sb = new StringBuffer(80);
        for (int i = words.length - 1; i >= 0; --i) {
            String word = words[i];
            // seperate with dot
            if (i != words.length - 1)
                sb.append('.');
            sb.append(word);
        }
        return protocol + "://" + sb.toString() + "/";
    }

    /**
     * Determines whether a method should have an OperationDescription created for it based on the
     * name. This is a convenience method to allow us to exlude methods such as constructors.
     *
     * @param methodName
     * @return
     */
    static boolean createOperationDescription(String methodName) {
        if (methodName.equals(CONSTRUCTOR_METHOD)) {
            return false;
        }
        return true;
    }

    /**
     * This is a helper method that will open a stream to an @HandlerChain configuration file.
     *
     * @param configFile  - The path to the file
     * @param className   - The class in which the annotation was declared. This is used in case the
     *                    file path is relative.
     * @param classLoader - ClassLoader used to load relative file paths.
     * @return
     */
    public static InputStream openHandlerConfigStream(String configFile, String className,
                                                      ClassLoader
                                                              classLoader) {
        InputStream configStream = null;
        URL configURL;
        if (log.isDebugEnabled()) {
            log.debug("Attempting to load @HandlerChain configuration file: " + configFile +
                    " relative to class: " + className);
        }
        // Attempt 1:
	// Try absolute loading
        try {
	    if (log.isDebugEnabled()) {
	        log.debug("Attempt 1: Try absolute load of (" + configFile + ")");
	    }
            configURL = new URL(configFile);
            if (configURL != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found absolute @HandlerChain configuration file: " + configFile);
                }
                configStream = configURL.openStream();
            }
        }
        catch (MalformedURLException e) {
            // try another method to obtain a stream to the configuration file
	    if (log.isDebugEnabled()) {
	        log.debug("Attempt 1 Failed with exception.  Try Attempt 2.  " +
	                  "The caught exception is : " + e);
	    }
        }
        catch (IOException e) {
            // report this since it was a valid URL but the openStream caused a problem
	    if (log.isDebugEnabled()) {
	        log.debug("The URL was valid, but opening the stream " +
	                      "caused a problem : " + e);
	    }
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("hcConfigLoadFail",
                                                                         configFile, className,
                                                                         e.toString()));
        }

	    // Attempt 2:
	    // Try relative uri loading from Classloaders
        if (configStream == null) {
            if (log.isDebugEnabled()) {
                log.debug("@HandlerChain.file attribute refers to a relative location: "
                        + configFile);
	        log.debug("Attempt 2: Try relative uri load of (" + configFile + ") " +
	            "from the classloaders");
            }
            className = className.replace(".", "/");
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Resolving @HandlerChain configuration file: " + configFile +
                            " relative to class file: " + className);
                }
                URI uri = new URI(className);
                uri = uri.resolve(configFile);
                String resolvedPath = uri.toString();
                if (log.isDebugEnabled()) {
                    log.debug("@HandlerChain.file resolved file path location: " + resolvedPath);
                }
                configStream = getInputStream_priv(resolvedPath, classLoader);
            }
	    catch (Throwable e) {
	      if (log.isDebugEnabled()) {
	          log.debug("Attempt 2 Failed with exception. " +
	                    "The caught exception is : " + e);
	      }
              throw ExceptionFactory.makeWebServiceException(Messages.getMessage("hcConfigLoadFail",
                                                                             configFile, className,
                                                                             e.toString()));
            }
        }
        if (configStream == null) {
            //throw ExceptionFactory.makeWebServiceException(Messages.getMessage("handlerChainNS",
            //                                                             configFile, className));
            // No longer throwing an exception here.  This method is best-effort, and the caller may
            // change the class to which the path is relative and try again.  The caller is responsible
            // for determining when to "give up"
            if (log.isDebugEnabled()) {
                log.debug("@HandlerChain configuration fail: " + configFile + " in class: " + className + " failed to load.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("@HandlerChain configuration file: " + configFile + " in class: " +
                        className + " was successfully loaded.");
            }
        }
        return configStream;
    }

    /**
     * A doPriv version of getInputStream
     * @return
     */
    private static InputStream getInputStream_priv(final String path, 
                                                   final ClassLoader classLoader) {
        return (InputStream) 
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return getInputStream(path, classLoader);
                }
            });
    }

    /**
     * Get the InputStream from the relative path and classloader
     * @param path
     * @param classLoader
     * @return
     */
    private static InputStream getInputStream(String path, ClassLoader classLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Start getInputStream with ("
                      + path + ") and classloader (" + classLoader  + ")");
        }
        InputStream configStream = classLoader.getResourceAsStream(path);
        if (configStream == null) {
            // try another classloader
            ClassLoader cl = System.class.getClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("Attempting with System classloader (" + cl + ")");
            }
            if (cl != null) {
                configStream = cl.getResourceAsStream(path);
            }
        }
        if (configStream == null) {
            // and another classloader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (log.isDebugEnabled()) {
                log.debug("Attempting with current thread " +
                            "classloader (" + cl + ")");
            }
            if (cl != null) {
                configStream = cl.getResourceAsStream(path);
            }
        }
        return configStream;
    }

    /**
     * Determine is this method is an async method
     * @param method - The method to examine
     * @return
     */
    public static boolean isAsync(Method method) {

        if (method == null) {
            return false;
        }

        String methodName = method.getName();
        Class returnType = method.getReturnType();

        if (methodName.endsWith("Async")
            && (returnType.isAssignableFrom(javax.xml.ws.Response.class) || returnType
                .isAssignableFrom(java.util.concurrent.Future.class))) {
            return true;
        } else {
            return false;
        }
    }
    
    public static HandlerChainsType loadHandlerChains(InputStream is, ClassLoader classLoader) {
        try {
            HandlerChainsParser parser = new HandlerChainsParser();
            return parser.loadHandlerChains(is);
        } catch (Exception e) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("loadHandlerChainErr", e.getMessage()));
        }       
    }
    
	
    /**
     * This method will loop through a list of extensibility elements looking for one
     * of four objects: SOAPBody, SOAP12Body, SOAPHeader, SOAP12Header. If any of these
     * objects are found the namespace URI from this object will be returned.
     */
    public static String getNamespaceFromSOAPElement(List extElements) {
        Iterator extIter = extElements.iterator();
        while (extIter.hasNext()) {
            Object extObj = extIter.next();
            if (extObj instanceof SOAPBody) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning SOAPBody namespace: "
                            + ((SOAPBody) extObj).getNamespaceURI());
                }
                return ((SOAPBody) extObj).getNamespaceURI();
            } else if (extObj instanceof SOAP12Body) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning SOAP12Body namespace: "
                            + ((SOAP12Body) extObj).getNamespaceURI());
                }
                return ((SOAP12Body) extObj).getNamespaceURI();
            } else if (extObj instanceof SOAPHeader) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning SOAPHeader namespace: "
                            + ((SOAPHeader) extObj).getNamespaceURI());
                }
                return ((SOAPHeader) extObj).getNamespaceURI();
            } else if (extObj instanceof SOAP12Header) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning SOAP12Header namespace: "
                            + ((SOAP12Header) extObj).getNamespaceURI());
                }
                return ((SOAP12Header) extObj).getNamespaceURI();
            }
            else if (extObj instanceof MIMEMultipartRelated) {
                if (log.isDebugEnabled()) {
                    log.debug("Found a MIMEMultipartRelated element.  Unwrapping to get SOAP binding.");
                }
                MIMEMultipartRelated mime = (MIMEMultipartRelated) extObj;
                List mimeParts = mime.getMIMEParts();
                
                Iterator itr = mimeParts.iterator();
                while (itr.hasNext()) {
                    MIMEPart mimePart = (MIMEPart) itr.next();
                    List elements = mimePart.getExtensibilityElements();
                    
                    String ns = getNamespaceFromSOAPElement(elements);
                    return ns;
                }
            }
        }
        return null;
    }
    /**
     * This method will process a WSDL Binding and build AttachmentDescription objects if the
     * WSDL dicatates attachments.
     */
    public static void getAttachmentFromBinding(OperationDescriptionImpl opDesc, Binding binding) {
        if (binding != null) {
            Iterator bindingOpIter = binding.getBindingOperations().iterator();
            while (bindingOpIter.hasNext()) {
                BindingOperation bindingOp = (BindingOperation) bindingOpIter.next();
                // found the BindingOperation that matches the current OperationDescription
                if (bindingOp.getName().equals(opDesc.getName().getLocalPart())) {
                    if (bindingOp.getBindingInput() != null) {
                        if (log.isDebugEnabled()) {
                                log.debug("Processing binding opertion input");
                        }
                        processBindingForMIME(bindingOp.getBindingInput().getExtensibilityElements(), 
                            opDesc, bindingOp.getOperation(), true);
                    }
                    if (bindingOp.getBindingOutput() != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Processing binding output");
                        }
                        processBindingForMIME(bindingOp.getBindingOutput().getExtensibilityElements(), 
                            opDesc, bindingOp.getOperation(), false);
                    }
                }
            }
        }
    }

    /**
     * This method will loop through the extensibility elements for a given BindingInput or
     * BindingOutput element and determine if it has any MIMEMultipartRelated content. If it 
     * does it will build up the appropriate AttachmentDescription objects.
     */
    private static void processBindingForMIME(List extensibilityElements,
                                              OperationDescriptionImpl opDesc, 
                                              Operation operation,
                                              boolean isRequest) {
        Iterator extensibilityIter = extensibilityElements.iterator();
        while (extensibilityIter.hasNext()) {
            Object obj = extensibilityIter.next();
            if (obj instanceof MIMEMultipartRelated) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found a mime:multipartRelated extensiblity element.");
                    }
                // Found mime information now process it and determine if we need to
                // create an AttachmentDescription
                MIMEMultipartRelated mime = (MIMEMultipartRelated) obj;
                Iterator partIter = mime.getMIMEParts().iterator();
                while (partIter.hasNext()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Found a mime:part child element.");
                        }
                    MIMEPart mimePart = (MIMEPart) partIter.next();
                    Iterator mExtIter = mimePart.getExtensibilityElements().iterator();
                    // Process each mime part to determine if there is mime content
                    while (mExtIter.hasNext()) {
                        Object obj2 = mExtIter.next();
                        // For mime content we need to potentially create an AttachmentDescription
                        if (obj2 instanceof MIMEContent) {
                            MIMEContent mimeContent = (MIMEContent) obj2;
                            String part = mimeContent.getPart();
                            String type = mimeContent.getType();
                            // if we have not already processed this part for the operation
                            if (opDesc.getPartAttachmentDescription(part) == null) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding new AttachmentDescription for part: " + part
                                            + " on operation: " + opDesc.getOperationName());
                                }
                                AttachmentDescription attachmentDesc =
                                        new AttachmentDescriptionImpl(AttachmentType.SWA,
                                                                      new String[] { type });
                                opDesc.addPartAttachmentDescription(part, attachmentDesc);
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Already created AttachmentDescription for part: "
                                            + part + " of type: " + type);
                                }
                            }
                        }
                        else if (obj2 instanceof SOAPBody || obj2 instanceof SOAP12Body) {
                            if (log.isDebugEnabled()) {
                                log.debug("Found a body element with potential nested mime content");                                    
                            }
                            
                            // Flag whether there's a potential nested attachment.
                            if (isRequest) {
                                opDesc.setHasRequestSwaRefAttachments(true);
                            }
                            else {
                                opDesc.setHasResponseSwaRefAttachments(true);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void registerHandlerHeaders(AxisService axisService, List<Handler> handlers) {
        if (handlers == null || axisService == null) {
            return;
        }
        
        ArrayList<QName> understoodHeaderQNames = new ArrayList<QName>();
        for (Handler handler : handlers) {
            if (handler instanceof SOAPHandler) {
                SOAPHandler soapHandler = (SOAPHandler) handler;
                
                Set<QName> headers = soapHandler.getHeaders();
                if (headers != null) {
                    for (QName header : headers) {
                        if (!understoodHeaderQNames.contains(header)) {
                            understoodHeaderQNames.add(header);
                        }
                    }
                } 
            }
        }
        
        if (!understoodHeaderQNames.isEmpty()) {
            Parameter headerQNParameter = 
                new Parameter(EndpointDescription.HANDLER_PARAMETER_QNAMES, understoodHeaderQNames);
            try {
                axisService.addParameter(headerQNParameter);
            } catch (AxisFault e) {
                log.warn(Messages.getMessage("regHandlerHeadersErr",axisService.getName(),e.getMessage()));
            }
        }  
    }
    
    /**
     * Given a binding type value based on a JAXWS anntation, return the corresponding WSDL
     * binding type.  The JAXWS annotation values understood are those returned by
     * mapBindingTypeWsdltoAnnotation.
     * 
     * @see #mapBindingTypeWsdlToAnnotation(String, String)
     * 
     * @param annotationBindingType The binding type as represented by a JAXWS annotation value
     * @return The binding type as represented by a WSDL binding extension namespace value
     */
    public static String mapBindingTypeAnnotationToWsdl(String annotationBindingType) {
        String wsdlBindingType = null;
        
        if (SOAPBinding.SOAP11HTTP_BINDING.equals(annotationBindingType)
                || MDQConstants.SOAP11JMS_BINDING.equals(annotationBindingType)) {
            wsdlBindingType = EndpointDescriptionWSDL.SOAP11_WSDL_BINDING;
        } else if (SOAPBinding.SOAP12HTTP_BINDING.equals(annotationBindingType)
                || MDQConstants.SOAP12JMS_BINDING.equals(annotationBindingType)) {
            wsdlBindingType = EndpointDescriptionWSDL.SOAP12_WSDL_BINDING;
        } else if (javax.xml.ws.http.HTTPBinding.HTTP_BINDING.equals(annotationBindingType)) {
            wsdlBindingType = EndpointDescriptionWSDL.HTTP_WSDL_BINDING;
        }
        
        return wsdlBindingType;
    }
    
    /**
     * Given a binding type value based on WSDL, return the corresponding JAXWS annotation value.
     * The WSDL binding type values are based on the namespace of the binding extension element.
     * The JAXWS annotation values correspond to the values to the HTTPBinding and SOAPBinding
     * annotations.  Additionally, proprietary values for JMS bindings are supported.  The JAXWS
     * binding type annotation values returned could be from SOAPBinding or HTTPBinding.
     * 
     * @param wsdlBindingType The binding type as represnted by the WSDL binding extension namespace
     * @param soapTransport The WSDL transport.  Used to determine if a JMS binding type should
     * be returned
     * @return The binding represented by a JAXWS Binding Type Annotation value from either 
     * SOAPBinding or HTTPBinding.
     */
    public static String mapBindingTypeWsdlToAnnotation(String wsdlBindingType, String soapTransport) {
        String soapBindingType = null;
        if (EndpointDescriptionWSDL.SOAP11_WSDL_BINDING.equals(wsdlBindingType)) {
            if (MDQConstants.SOAP11JMS_BINDING.equals(soapTransport)) {
                soapBindingType =  MDQConstants.SOAP11JMS_BINDING;
            } else {
                //REVIEW: We are making the assumption that if not JMS, then HTTP
                soapBindingType = SOAPBinding.SOAP11HTTP_BINDING;
            } 
        } else if (EndpointDescriptionWSDL.SOAP12_WSDL_BINDING.equals(wsdlBindingType)) {
            if (MDQConstants.SOAP12JMS_BINDING.equals(soapTransport)) {
                soapBindingType =  MDQConstants.SOAP12JMS_BINDING;
            } else {
                //REVIEW: We are making the assumption that if not JMS, then HTTP
                soapBindingType = SOAPBinding.SOAP12HTTP_BINDING;
            } 
        } else if (EndpointDescriptionWSDL.HTTP_WSDL_BINDING.equals(wsdlBindingType)) {
            soapBindingType = javax.xml.ws.http.HTTPBinding.HTTP_BINDING;
        }
        return soapBindingType;
    }
    
    /**
     * Dump the contents of the composite in a String for 
     * debug trace
     * @param composite
     * @return String
     */
    static String dumpString(DescriptionBuilderComposite composite) {
        try {
            return composite.toString();
        } catch (Throwable t) {
            return "Cannot dump DescriptionBuilderComposite due to : " + t;
        }
    }
    
    /**
     * Utility method for converting a String value into a boolean.
     * Case-insensitive forms of true, yes, and 1 correspond to true.
     * Case-insensitive forms of false, no, and 0 correspond to false.
     * Anything else will result in a false being returned.
     * 
     * @param value
    *        the property's value
    * @return
    *        true or false or null if neither
    */
    public static Boolean getBooleanValue(String value) {
        Boolean b = null;
        
        if (value.equalsIgnoreCase("true") ||
            value.equalsIgnoreCase("yes") ||
            value.equals("1")) {
            b = Boolean.TRUE;
        } else if (value.equalsIgnoreCase("false") ||
                   value.equalsIgnoreCase("no") ||
                   value.equals("0")) {
            b = Boolean.FALSE;
        }
        // Anything else will result in false
        return b;
    }
}
