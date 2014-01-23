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


import org.apache.axis2.util.Counter;
import org.apache.axis2.util.JavaUtils;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 */
public class WSDLWrapperSaveImpl implements WSDLWrapperImpl {

    private static final Log log = LogFactory.getLog(WSDLWrapperSaveImpl.class);
    private static final boolean isDebugEnabled = log.isDebugEnabled();
    private static final String myClassName = "WSDLWrapperSaveImpl";

    // javax.wsdl.Definition implements java.io.Serializable
    static final long serialVersionUID = -2788807375814097409L;

    // the wsdl4j wsdl definition object that is being wrapped
    private Definition wsdlDefinition = null;

    // the location of the base document used in the wsdl4j definition
    private URL wsdlURL = null;
    private String wsdlExplicitURI = null;
    private String wsdlDocumentBaseURI = null;

    // an object that maintains a synchronized counter
    private Counter accessCount = null;

    // serialization-related information ---------------------------
    //
    //   'safeToSerialize' indicates whether the wrapped WSDL definition object is
    //                     safe to serialize.  This is set to false if the underlying
    //                     WSDL4J's WSDL definition had an error when we tried to 
    //                     serialize it.
    // 
    //   'hasBeenSaved'    indicates whether the wrapped WSDL definition object
    //                     has been successfully saved
    // 
    //   'hasBeenUpdatedSinceSaving'  indicates whether the wrapped WSDL definition
    //                     object has been updated since the last saved/serialziation
    // 
    // 
    private boolean safeToSerialize = true;
    private boolean hasBeenSaved = false;
    private boolean hasBeenUpdatedSinceSaving = false;

    private File savedDefinitionFile = null;
    private String savedFilename = null;


    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     */
    public WSDLWrapperSaveImpl(Definition def) {
        if (log.isDebugEnabled()) {
            log.debug("WSDLWrapperSaveImpl(Definition def) called");
            log.trace(JavaUtils.callStackToString());
        }
        prepare(def, null);
    }


    /**
     * Constructor
     * 
     * @param def    The WSDL Definition
     * @param wURL   The URL for the wsdl
     */
    public WSDLWrapperSaveImpl(Definition def, URL wURL) {
        if (log.isDebugEnabled()) {
            log.debug("WSDLWrapperSaveImpl(Definition def, URL wURL) called");
            log.trace(JavaUtils.callStackToString());
        }
        prepare(def, wURL);
    }



    /**
     * Initialize the wsdl definition wrapper
     * 
     * @param def    The WSDL4J definition
     * @param wURL   The URL where the WSDL is obtained
     */
    private void prepare(Definition def, URL wURL) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".prepare()");
        }
        
        wsdlDefinition = def;
        wsdlURL = wURL;

        accessCount = new Counter();

        releaseResources();
    }


    //-------------------------------------------------------------------------
    // public WSDLDefinitionWrapper methods
    //-------------------------------------------------------------------------

    /*
     * Returns the WSDL4J Definition object that is being wrapped
     */
    public Definition getUnwrappedDefinition() {

        getWrappedDefinitionForUse();

        Definition def;

        if ((wsdlDefinition != null) &&
            (wsdlDefinition instanceof WSDLDefinitionWrapper)) {
            def = ((WSDLDefinitionWrapper) wsdlDefinition).getUnwrappedDefinition();
        } else {
            def = wsdlDefinition;
        }

        return def;
    }



    /**
     * Sets the WSDL4J Definition object that is being wrapped
     *
     * @param d  the WSDL4J Definition object
     */
    public void setDefinitionToWrap(Definition d) {
        wsdlDefinition = d;
    }


    /**
     * Sets the location for the WSDL4J Definition object that is being wrapped
     */
    public void setWSDLLocation(String uriLocation) {
        if (uriLocation != null) {
            try {
                wsdlURL = new URL(uriLocation);
            }
            catch (Exception e) {
                // todo
            }
        }
    }


    /**
     * Gets the location for the WSDL4J Definition object that is being wrapped
     */
    public String getWSDLLocation() {
        if (wsdlURL != null) {
            return wsdlURL.toString();
        }
        else {
            return null;
        }
    }


    /**
     * Closes the use of the wrapper implementation and allows 
     * internal resources to be released.
     */
    public void close() {
        // TODO release temporary files
    }



    //-------------------------------------------------------------------------
    // javax.wsdl.Defintion interface methods
    //-------------------------------------------------------------------------

    public void setDocumentBaseURI(String d) {

        // Set the URI of the base document for the Definition.
        // This identifies the origin of the Definition.
        // Note that this is the URI of the base document, not the imports.

        if (isDebugEnabled) {
            log.debug(myClassName + ".setDocumentBaseURI(" + d + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setDocumentBaseURI(d);
        }

        doneUsingWrappedDefinition();
    }

    public String getDocumentBaseURI() {

        // Get the URI of the base document for the Definition.
        // This identifies the origin of the Definition.
        // Note that this is the URI of the base document, not the imports.

        if (isDebugEnabled) {
            log.debug(myClassName + ".getDocumentBaseURI()");
        }

        getWrappedDefinitionForUse();

        String results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getDocumentBaseURI();
        }

        doneUsingWrappedDefinition();

        return results;
    }

    public void setQName(QName n) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".setQName(" + n + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setQName(n);
        }

        doneUsingWrappedDefinition();
    }

    public QName getQName() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getQName()");
        }

        getWrappedDefinitionForUse();

        QName results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getQName();
        }

        doneUsingWrappedDefinition();

        return results;
    }

    public void setTargetNamespace(String t) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".setTargetNamespace(" + t + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setTargetNamespace(t);
        }

        doneUsingWrappedDefinition();
    }

    public String getTargetNamespace() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getTargetNamespace()");
        }

        getWrappedDefinitionForUse();

        String results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getTargetNamespace();
        }

        doneUsingWrappedDefinition();

        return results;
    }

    public void addNamespace(String prefix, String namespaceURI) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addNamespace(" + prefix + ", " + namespaceURI + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addNamespace(prefix, namespaceURI);
        }
        doneUsingWrappedDefinition();
    }

    public String removeNamespace(String prefix) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeNamespace(" + prefix + ")");
        }

        getWrappedDefinitionForUse();

        String results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeNamespace(prefix);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public String getNamespace(String prefix) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getNamespace(" + prefix + ")");
        }

        getWrappedDefinitionForUse();

        String results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getNamespace(prefix);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public String getPrefix(String namespaceURI) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getPrefix(" + namespaceURI + ")");
        }

        getWrappedDefinitionForUse();

        String results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getPrefix(namespaceURI);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getNamespaces() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getNamespaces()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getNamespaces();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public List getNativeAttributeNames() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getNativeAttributeNames()");
        }

        getWrappedDefinitionForUse();

        List results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getNativeAttributeNames();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void setTypes(Types types) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".setTypes()");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setTypes(types);
        } 
        doneUsingWrappedDefinition();
    }


    public Types getTypes() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getTypes()");
        }

        getWrappedDefinitionForUse();

        Types results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getTypes();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addImport(Import importDef) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addImport(" + importDef + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addImport(importDef);
        }
        doneUsingWrappedDefinition();
    }

    public Import removeImport(Import importDef) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeImport(" + importDef + ")");
        }

        getWrappedDefinitionForUse();

        Import results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeImport(importDef);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public List getImports(String namespaceURI) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getImports(" + namespaceURI + ")");
        }

        getWrappedDefinitionForUse();

        List results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getImports(namespaceURI);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getImports() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getImports()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getImports();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addMessage(Message message) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addMessage(" + message + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addMessage(message);
        }
        doneUsingWrappedDefinition();
    }

    public Message getMessage(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getMessage(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Message results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getMessage(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Message removeMessage(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeMessage(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Message results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeMessage(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getMessages() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getMessages()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getMessages();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addBinding(Binding binding) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addBinding(" + binding + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addBinding(binding);
        }
        doneUsingWrappedDefinition();
    }

    public Binding getBinding(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getBinding(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Binding results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getBinding(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Binding removeBinding(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeBinding(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Binding results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeBinding(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getBindings() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getBindings()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getBindings();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getAllBindings() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getAllBindings()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getAllBindings();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addPortType(PortType portType) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addPortType(" + portType + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addPortType(portType);
        }
        doneUsingWrappedDefinition();
    }

    public PortType getPortType(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getPortType(" + name + ")");
        }

        getWrappedDefinitionForUse();

        PortType results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getPortType(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public PortType removePortType(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removePortType(" + name + ")");
        }

        getWrappedDefinitionForUse();

        PortType results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removePortType(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getPortTypes() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getPortTypes()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getPortTypes();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getAllPortTypes() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getAllPortTypes()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getAllPortTypes();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addService(Service service) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addService(" + service + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addService(service);
        }
        doneUsingWrappedDefinition();
    }

    public Service getService(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getService(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Service results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getService(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Service removeService(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeService(" + name + ")");
        }

        getWrappedDefinitionForUse();

        Service results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeService(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getServices() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getServices()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getServices();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getAllServices() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getAllServices()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getAllServices();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void setDocumentationElement(org.w3c.dom.Element docEl) {

        if (isDebugEnabled) {
            log.debug(myClassName + ".setDocumentationElement()");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setDocumentationElement(docEl);
        }
        doneUsingWrappedDefinition();
    }

    public org.w3c.dom.Element getDocumentationElement() {

        if (isDebugEnabled) {
            log.debug(myClassName + ".getDocumentationElement()");
        }

        getWrappedDefinitionForUse();

        org.w3c.dom.Element results = null;

        if (wsdlDefinition != null) {
            return  wsdlDefinition.getDocumentationElement();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void addExtensibilityElement(ExtensibilityElement extElement) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".addExtensibilityElement(" + extElement + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.addExtensibilityElement(extElement);
        }
        doneUsingWrappedDefinition();
    }

    public List getExtensibilityElements() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getExtensibilityElements()");
        }

        getWrappedDefinitionForUse();

        List results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getExtensibilityElements();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Binding createBinding() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createBinding()");
        }

        getWrappedDefinitionForUse();

        Binding results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createBinding();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public BindingFault createBindingFault() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createBindingFault()");
        }

        getWrappedDefinitionForUse();

        BindingFault results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createBindingFault();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public BindingInput createBindingInput() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createBindingInput()");
        }

        getWrappedDefinitionForUse();

        BindingInput results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createBindingInput();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public BindingOperation createBindingOperation() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createBindingOperation()");
        }

        getWrappedDefinitionForUse();

        BindingOperation results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createBindingOperation();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public BindingOutput createBindingOutput() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createBindingOutput()");
        }

        getWrappedDefinitionForUse();

        BindingOutput results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createBindingOutput();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Fault createFault() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createFault()");
        }

        getWrappedDefinitionForUse();

        Fault results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createFault();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Import createImport() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createImport()");
        }

        getWrappedDefinitionForUse();

        Import results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createImport();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Input createInput() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createInput()");
        }

        getWrappedDefinitionForUse();

        Input results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createInput();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Message createMessage() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createMessage()");
        }

        getWrappedDefinitionForUse();

        Message results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createMessage();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Operation createOperation() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createOperation()");
        }

        getWrappedDefinitionForUse();

        Operation results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createOperation();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Output createOutput() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createOutput()");
        }

        getWrappedDefinitionForUse();

        Output results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createOutput();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Part createPart() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createPart()");
        }

        getWrappedDefinitionForUse();

        Part results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createPart();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Port createPort() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createPort()");
        }

        getWrappedDefinitionForUse();

        Port results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createPort();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public PortType createPortType() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createPortType()");
        }

        getWrappedDefinitionForUse();

        PortType results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createPortType();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Service createService() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createService()");
        }

        getWrappedDefinitionForUse();

        Service results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createService();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Types createTypes() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".createTypes()");
        }

        getWrappedDefinitionForUse();

        Types results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.createTypes();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void setExtensionRegistry(ExtensionRegistry extReg) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".setExtensionRegistry(" + extReg + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setExtensionRegistry(extReg);
        }
        doneUsingWrappedDefinition();
    }

    public ExtensionRegistry getExtensionRegistry() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getExtensionRegistry()");
        }

        getWrappedDefinitionForUse();

        ExtensionRegistry results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getExtensionRegistry();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public String toString() {
        getWrappedDefinitionForUse();
        String results = "";
        if (wsdlDefinition != null) {
            results = wsdlDefinition.toString();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    //-------------------------------------------------------------------------
    // other AbstractWSDLElement methods
    //-------------------------------------------------------------------------

    public ExtensibilityElement removeExtensibilityElement(ExtensibilityElement extElement) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".removeExtensibilityElement(" + extElement + ")");
        }

        getWrappedDefinitionForUse();

        ExtensibilityElement results = null;

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            results = wsdlDefinition.removeExtensibilityElement(extElement);
        }
        doneUsingWrappedDefinition();
        return results;

    }

    public java.lang.Object getExtensionAttribute(QName name) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getExtensionAttribute(" + name + ")");
        }

        getWrappedDefinitionForUse();

        java.lang.Object results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getExtensionAttribute(name);
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public Map getExtensionAttributes() {
        if (isDebugEnabled) {
            log.debug(myClassName + ".getExtensionAttributes()");
        }

        getWrappedDefinitionForUse();

        Map results = null;

        if (wsdlDefinition != null) {
            results = wsdlDefinition.getExtensionAttributes();
        }
        doneUsingWrappedDefinition();
        return results;
    }

    public void setExtensionAttribute(QName name, java.lang.Object value) {
        if (isDebugEnabled) {
            log.debug(myClassName + ".setExtensionAttribute(" + name + ",  " + value + ")");
        }

        getWrappedDefinitionForUse();

        if (wsdlDefinition != null) {
            if (hasBeenSaved) {
                hasBeenUpdatedSinceSaving = true;
            }
            wsdlDefinition.setExtensionAttribute(name, value);
        }
        doneUsingWrappedDefinition();
    }

    //-------------------------------------------------------------------------
    // private utility methods
    //-------------------------------------------------------------------------


    /**
     * This is an internal utility to ensure that the
     * WSDL definition being wrapped is available for 
     * use.  
     * <P>
     * For example, if the WSDL4J WSDL definition object
     * had been saved, this will reload it.
     */
    private void getWrappedDefinitionForUse() {
        if (wsdlDefinition == null) {
            loadResources();
        }
        if (wsdlDefinition != null) {
            accessCount.incrementAndGet();
        }
    }


    /**
     * This is an internal utility to indicate that the
     * use of WSDL definition has completed. 
     */
    private void doneUsingWrappedDefinition() {

        long assessors = accessCount.decrementAndGet();

        if (assessors == 0) {
            releaseResources();
        }
    }


    /*
     * Reduces memory footprint of the in-memory caching of the WSDL definition
     * based on the configuration settings
     *
     */
    public void releaseResources() {

        boolean saved = save();

        if (saved) {
            // release the in-memory copy of the WSDL4J 
            wsdlDefinition = null;
        }
    }

    /*
     * Loads the the WSDL definition back into memory
     */
    public void loadResources() {

        if (wsdlDefinition == null) {
            wsdlDefinition = restore();
        }
    }


    /**
     * Saves the current WSDL definition object that this wrapper contains.
     * This method has a number of checks to determine how to proceed with
     * the saving of the WSDL definition object, so the caller should not
     * need to make checks on whether to save or not.
     * <P>
     * The caller is responsible for handling the wrapped WSDL
     * definition object, for example, deciding when to release
     * it or reload it.
     * 
     * @return True - if the save succeeded
     *         False - if the save failed
     */
    private boolean save() {

        // if the wsdl definiton failed to serialize from a previous attempt
        // to save it, then don't try to save it
        if (safeToSerialize == false) {
            // exit quickly
            return false;
        }

        // at this point, we think it is safe to proceed with saving the 
        // wsdl definition

        // make sure there is a file to use for saving the wsdl definition
        if (savedDefinitionFile == null) {
            try {
                savedDefinitionFile = File.createTempFile("wsdlDef_", null);
                savedFilename = savedDefinitionFile.getName();
                log.debug(myClassName + ".save(): temp file = [" + savedFilename + "]");
            }
            catch (Exception ex) {
                log.debug(myClassName + ".save(): error creating temp file = [" + ex.getMessage() + "]");
                savedDefinitionFile = null;
                savedFilename = null;

                // can't save the wsdl definition at this time
                // might be able to do so later
                hasBeenSaved = false;
                return false;
            }
        }

        if (savedDefinitionFile != null) {

            // the File object exists, check to see if the wsdl definition has
            // been previously saved

            if (hasBeenSaved && !hasBeenUpdatedSinceSaving) {
                // no need to save because we saved it already
                // and there were no updates to the wsdl definition object
                // since the previous save
                return true;
            }


            // ---------------------------------------------------------
            // save to the file
            // ---------------------------------------------------------
            FileOutputStream outStream = null;
            ObjectOutputStream outObjStream = null;

            try {
                // setup an output stream to a physical file
                outStream = new FileOutputStream(savedDefinitionFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                outObjStream = new ObjectOutputStream(outStream);

                // try to save the wsdl object
                log.debug(myClassName + ".save(): saving the wsdl definition.....");
                outObjStream.writeObject(wsdlDefinition);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                hasBeenSaved = true;
                hasBeenUpdatedSinceSaving = false;

                log.debug(myClassName + ".save(): ....saved the wsdl definition.....");

                long filesize = savedDefinitionFile.length();
                log.debug(myClassName + ".save(): file size after save [" + filesize +
                        "]   temp file = [" + savedFilename + "]");

                log.debug(myClassName + ".save(): end - - - - - - - - - - - - - - - -");
                return true;

            }
            catch (Exception ex2) {
                
                // disable future tries at saving this WSDL definition object
                safeToSerialize = false;

                // indicate that the file cannot be used to restore from
                hasBeenSaved = false;

                log.debug(myClassName + ".save(): error with saving the wsdl definition = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]", ex2);


                if (savedDefinitionFile != null) {
                    try {
                        savedDefinitionFile.delete();
                        savedDefinitionFile = null;
                        savedFilename = null;
                    }
                    catch (Exception e) {
                        // just absorb it
                    }
                }

                if (outObjStream != null) {
                    try {
                        outObjStream.close();
                    }
                    catch (Exception e) {
                        // just absorb it
                    }
                }

                if (outStream != null) {
                    try {
                        outStream.close();
                    }
                    catch (Exception e) {
                        // just absorb it
                    }
                }

                log.debug(myClassName + ".save(): error exit - - - - - - - - - - - - - - - -");
                return false;
            }
        }

        return false;
    }

    /**
     * Restores the WSDL definition from a previously saved copy.
     * <P>
     * The caller is responsible for handling the wrapped WSDL
     * definition object, for example, deciding when to release
     * it or reload it.
     * 
     * @return The restored WSDL definition object, or NULL
     */
    private Definition restore() {

        if (!hasBeenSaved) {
            // the wsdl defintion has not been saved, or the previous saved version
            // should not be used for some reason
            return null;
        }

        if (savedDefinitionFile == null) {
            // no file to restore from
            return null;
        }

        // ---------------------------------------------------------
        // restore from the temporary file
        // ---------------------------------------------------------
        Definition restoredDefinition = null;
        FileInputStream inStream = null;
        ObjectInputStream inObjStream = null;

        try {
            // setup an input stream to the file
            inStream = new FileInputStream(savedDefinitionFile);

            // attach a stream capable of reading objects from the 
            // stream connected to the file
            inObjStream = new ObjectInputStream(inStream);

            // try to restore the wrapped wsdl definition
            log.debug(myClassName + ".restore(): restoring the WSDL definition .....");

            restoredDefinition = (Definition) inObjStream.readObject();
            inObjStream.close();
            inStream.close();

            log.debug(myClassName + ".restore(): ....restored the WSDL definition .....");

        }
        catch (Exception ex2) {
            log.debug(myClassName + ".restore(): error with restoring the WSDL definition = [" +
                    ex2.getClass().getName() + " : " + ex2.getMessage() + "]", ex2);

            if (inObjStream != null) {
                try {
                    inObjStream.close();
                }
                catch (Exception e) {
                    // just absorb it
                }

            }

            if (inStream != null) {
                try {
                    inStream.close();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }


        log.debug(myClassName + ".restore(): end - - - - - - - - - - - - - - - -");
        return restoredDefinition;

    }

}
