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


import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * There are cases where a WSDL definition is kept in memory
 * in case it needs to be accessed during runtime.  In situations where 
 * there are lots of WSDL definitions or big WSDLs, the memory footprint can be 
 * huge.
 *
 * This class provides support for processing a WSDL4J definition
 * with a lower memory footprint.  This is useful for certain
 * environments.
 *
 * This class makes the decision on which implementation to use
 * to reduce memory footprint.  This allows other implementations
 * to be used for specific environments without adding lots of extra
 * overhead to every environment.
 *
 */
public class WSDLDefinitionWrapper implements Definition {

    private static final Log log = LogFactory.getLog(WSDLDefinitionWrapper.class);
    private static final boolean isDebugEnabled = log.isDebugEnabled();
    private static final String myClassName = "WSDLDefinitionWrapper";

    // javax.wsdl.Definition implements java.io.Serializable
    static final long serialVersionUID = -2788807375814097409L;


    // the setting used to indicate whether the in-memory copy of the 
    // WSDL definition should be manipulated to reduce memory footprint
    private boolean reduceWSDLMemoryCache = false;

    // the optional setting used to specify which type of reduction to use
    private int reduceWSDLMemoryType = 0;

    // the wrapper implementation to use 
    private WSDLWrapperImpl wrapperImpl = null;


    //-------------------------------------------------------------------------
    // constructors
    //-------------------------------------------------------------------------

    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @deprecated because this constructor does not provide any guidance for 
     * memory usage
     */
    public WSDLDefinitionWrapper(Definition def) {
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition) entry");
        }
        prepare(def, null);
    }
    
    /**
     * @param def
     * @param limitMemory true if you want to use a memory sensitive wrapper
     */
    public WSDLDefinitionWrapper(Definition def, boolean limitMemory, int memoryType) {
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition, boolean) entry");
        }
        reduceWSDLMemoryCache  = limitMemory;
        reduceWSDLMemoryType = memoryType;
        prepare(def, null);
    }

    /**
     * @param def WDDL Definition
     * @param axisConfig Axis Configuration
     */
    public WSDLDefinitionWrapper(Definition def,AxisConfiguration axisConfig ) {
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,AxisConfiguration) entry ");
        }
        setupMemoryParms(axisConfig);
        prepare(def, null);
    }

    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     * @deprecated use a constructor with a AxisConfiguration or memory limit parameter
     */
    public WSDLDefinitionWrapper(Definition def, URL wURL) {
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,URL) entry");
        }
        prepare(def, wURL);
    }


    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     * @param limitInMemory  The setting indicating whether the in-memory WSDL copy
     *                       should be manipulated to reduce memory footprint
     */
    public WSDLDefinitionWrapper(Definition def, URL wURL, boolean limitInMemory) {
        reduceWSDLMemoryCache = limitInMemory;
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,URL,boolean) entry");
        }
        prepare(def, wURL);
    }
    
    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     * @param limitInMemory  The setting indicating whether the in-memory WSDL copy
     *                       should be manipulated to reduce memory footprint
     * @param memoryType
     */
    public WSDLDefinitionWrapper(Definition def, URL wURL, boolean limitInMemory, int memoryType) {
        reduceWSDLMemoryCache = limitInMemory;
        this.reduceWSDLMemoryType = memoryType;
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,URL,boolean) entry");
        }
        prepare(def, wURL);
    }


    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     * @param limitType  The setting indicating which reduction technique
     *                   to use 
     */
    public WSDLDefinitionWrapper(Definition def, URL wURL, int limitType) {
        reduceWSDLMemoryCache = true;
        reduceWSDLMemoryType = limitType;
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,URL,int) entry");
        }
        prepare(def, wURL);
    }


    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     * @param axisCfg  The AxisConfiguration object, to be used to get configuration settings  
     */
    public WSDLDefinitionWrapper(Definition def, URL wURL, AxisConfiguration axisCfg) {
        if (log.isDebugEnabled() ) {
            log.debug("WSDLDefinitionWrapper(Definition,URL,AxisConfiguration) entry");
        }

        // determine what the setting for the memory optimization is
        setupMemoryParms(axisCfg);
        prepare(def, wURL);
    }



    private void setupMemoryParms( AxisConfiguration axisCfg) {
        if (log.isDebugEnabled() ) {
            log.debug("setupMemoryParms(AxisConfiguration) entry");
        }

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
            if (log.isDebugEnabled() ) {
                log.debug("reduceWSDLMemoryCache:"+ reduceWSDLMemoryCache + ", reduceWSDLMemoryType:" + reduceWSDLMemoryType );
            }
        } else {
            if (log.isDebugEnabled() ) {
                log.debug("AxisConfiguration is null.  This is unexpected" );
            }
        }
        
    }


    /**
     * Initialize the wsdl definition wrapper
     * 
     * @param def    The WSDL4J definition
     * @param wURL   The URL where the WSDL is obtained
     */
    private void prepare(Definition def, URL wURL) {
        
        if (reduceWSDLMemoryCache) {

            // if the type is specified, then use it
            // otherwise, default to the serialization technique

            if (reduceWSDLMemoryType == 2) {
                
                // See if the definition is reloadable
                if (WSDLWrapperReloadImpl.isReloadable(def, wURL)) {
                    // a wrapper implementation that uses release & reload on the 
                    // underlying WSDL4J object
                    // this would be desirable for those environments where 
                    // many of the WSDL definitions are not serializable 
                    wrapperImpl = new WSDLWrapperReloadImpl(def, wURL);
                } else {
                    // a wrapper implementation that is just a passthrough to the 
                    // underlying WSDL4J object
                    if (log.isDebugEnabled() ) {
                        log.debug("WSDLDefinitionWrapper could not create a reloadable WSDL wrapper object.");
                    }
                    wrapperImpl = new WSDLWrapperBasicImpl(def, wURL);
                }
            }
            else {
                // a wrapper implementation that uses serialization to save the  
                // underlying WSDL4J object
                wrapperImpl = new WSDLWrapperSaveImpl(def, wURL);
            }
        }
        else {
            // a wrapper implementation that is just a passthrough to the 
            // underlying WSDL4J object
            wrapperImpl = new WSDLWrapperBasicImpl(def, wURL);
        }

        wrapperImpl.releaseResources();
    }


    //-------------------------------------------------------------------------
    // public WSDLDefinitionWrapper methods
    //-------------------------------------------------------------------------

    /*
     * Returns the WSDL4J Definition object that is being wrapped
     */
    public Definition getUnwrappedDefinition() {

        return wrapperImpl.getUnwrappedDefinition();
    }
    
    public int getMemoryLimitType() {
        return this.reduceWSDLMemoryType;
    }


    //-------------------------------------------------------------------------
    // javax.wsdl.Defintion interface methods
    //-------------------------------------------------------------------------

    public void setDocumentBaseURI(String d) {
        // Set the URI of the base document for the Definition.
        // This identifies the origin of the Definition.
        // Note that this is the URI of the base document, not the imports.

        wrapperImpl.setDocumentBaseURI(d);
    }

    public String getDocumentBaseURI() {
        // Get the URI of the base document for the Definition.
        // This identifies the origin of the Definition.
        // Note that this is the URI of the base document, not the imports.

        return wrapperImpl.getDocumentBaseURI();
    }

    public void setQName(QName n) {
        wrapperImpl.setQName(n);
    }

    public QName getQName() {
        return wrapperImpl.getQName();
    }

    public void setTargetNamespace(String t) {
        wrapperImpl.setTargetNamespace(t);
    }

    public String getTargetNamespace() {
        return wrapperImpl.getTargetNamespace();
    }

    public void addNamespace(String prefix, String namespaceURI) {
        wrapperImpl.addNamespace(prefix, namespaceURI);
    }

    public String removeNamespace(String prefix) {
        return wrapperImpl.removeNamespace(prefix);
    }

    public String getNamespace(String prefix) {
        return wrapperImpl.getNamespace(prefix);
    }

    public String getPrefix(String namespaceURI) {
        return wrapperImpl.getPrefix(namespaceURI);
    }

    public Map getNamespaces() {
        return wrapperImpl.getNamespaces();
    }

    public List getNativeAttributeNames() {
        return wrapperImpl.getNativeAttributeNames();
    }

    public void setTypes(Types types) {
        wrapperImpl.setTypes(types);
    }


    public Types getTypes() {
        return wrapperImpl.getTypes();
    }

    public void addImport(Import importDef) {
        wrapperImpl.addImport(importDef);
    }

    public Import removeImport(Import importDef) {
        return wrapperImpl.removeImport(importDef);
    }

    public List getImports(String namespaceURI) {
        return wrapperImpl.getImports(namespaceURI);
    }

    public Map getImports() {
        return wrapperImpl.getImports();
    }

    public void addMessage(Message message) {
        wrapperImpl.addMessage(message);
    }

    public Message getMessage(QName name) {
        return wrapperImpl.getMessage(name);
    }

    public Message removeMessage(QName name) {
        return wrapperImpl.removeMessage(name);
    }

    public Map getMessages() {
        return wrapperImpl.getMessages();
    }

    public void addBinding(Binding binding) {
        wrapperImpl.addBinding(binding);
    }

    public Binding getBinding(QName name) {
        return wrapperImpl.getBinding(name);
    }

    public Binding removeBinding(QName name) {
        return wrapperImpl.removeBinding(name);
    }

    public Map getBindings() {
        return wrapperImpl.getBindings();
    }

    public Map getAllBindings() {
        return wrapperImpl.getAllBindings();
    }

    public void addPortType(PortType portType) {
        wrapperImpl.addPortType(portType);
    }

    public PortType getPortType(QName name) {
        return wrapperImpl.getPortType(name);
    }

    public PortType removePortType(QName name) {
        return wrapperImpl.removePortType(name);
    }

    public Map getPortTypes() {
        return wrapperImpl.getPortTypes();
    }

    public Map getAllPortTypes() {
        return wrapperImpl.getAllPortTypes();
    }

    public void addService(Service service) {
        wrapperImpl.addService(service);
    }

    public Service getService(QName name) {
        return wrapperImpl.getService(name);
    }

    public Service removeService(QName name) {
        return wrapperImpl.removeService(name);
    }

    public Map getServices() {
        return wrapperImpl.getServices();
    }

    public Map getAllServices() {
        return wrapperImpl.getAllServices();
    }

    public void setDocumentationElement(org.w3c.dom.Element docEl) {
        wrapperImpl.setDocumentationElement(docEl);
    }

    public org.w3c.dom.Element getDocumentationElement() {
        return  wrapperImpl.getDocumentationElement();
    }

    public void addExtensibilityElement(ExtensibilityElement extElement) {
        wrapperImpl.addExtensibilityElement(extElement);
    }

    public List getExtensibilityElements() {
        return wrapperImpl.getExtensibilityElements();
    }

    public Binding createBinding() {
        return wrapperImpl.createBinding();
    }

    public BindingFault createBindingFault() {
        return wrapperImpl.createBindingFault();
    }

    public BindingInput createBindingInput() {
        return wrapperImpl.createBindingInput();
    }

    public BindingOperation createBindingOperation() {
        return wrapperImpl.createBindingOperation();
    }

    public BindingOutput createBindingOutput() {
        return wrapperImpl.createBindingOutput();
    }

    public Fault createFault() {
        return wrapperImpl.createFault();
    }

    public Import createImport() {
        return wrapperImpl.createImport();
    }

    public Input createInput() {
        return wrapperImpl.createInput();
    }

    public Message createMessage() {
        return wrapperImpl.createMessage();
    }

    public Operation createOperation() {
        return wrapperImpl.createOperation();
    }

    public Output createOutput() {
        return wrapperImpl.createOutput();
    }

    public Part createPart() {
        return wrapperImpl.createPart();
    }

    public Port createPort() {
        return wrapperImpl.createPort();
    }

    public PortType createPortType() {
        return wrapperImpl.createPortType();
    }

    public Service createService() {
        return wrapperImpl.createService();
    }

    public Types createTypes() {
        return wrapperImpl.createTypes();
    }

    public void setExtensionRegistry(ExtensionRegistry extReg) {
        wrapperImpl.setExtensionRegistry(extReg);
    }

    public ExtensionRegistry getExtensionRegistry() {
        return wrapperImpl.getExtensionRegistry();
    }

    public String toString() {
        return wrapperImpl.toString();
    }

    //-------------------------------------------------------------------------
    // other AbstractWSDLElement methods
    //-------------------------------------------------------------------------

    public ExtensibilityElement removeExtensibilityElement(ExtensibilityElement extElement) {
        return wrapperImpl.removeExtensibilityElement(extElement);
    }

    public java.lang.Object getExtensionAttribute(QName name) {
        return wrapperImpl.getExtensionAttribute(name);
    }

    public Map getExtensionAttributes() {
        return wrapperImpl.getExtensionAttributes();
    }

    public void setExtensionAttribute(QName name, java.lang.Object value) {
        wrapperImpl.setExtensionAttribute(name, value);
    }

}
