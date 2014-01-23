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

package org.apache.axis2.description;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.XMLElement;
import org.apache.woden.internal.DOMWSDLFactory;
import org.apache.woden.internal.wsdl20.BindingFaultImpl;
import org.apache.woden.internal.wsdl20.BindingOperationImpl;
import org.apache.woden.internal.wsdl20.extensions.InterfaceOperationExtensionsImpl;
import org.apache.woden.internal.wsdl20.extensions.http.HTTPBindingExtensionsImpl;
import org.apache.woden.internal.wsdl20.extensions.http.HTTPHeaderImpl;
import org.apache.woden.internal.wsdl20.extensions.soap.SOAPBindingExtensionsImpl;
import org.apache.woden.resolver.URIResolver;
import org.apache.woden.schema.Schema;
import org.apache.woden.types.NamespaceDeclaration;
import org.apache.woden.wsdl20.Binding;
import org.apache.woden.wsdl20.BindingFault;
import org.apache.woden.wsdl20.BindingFaultReference;
import org.apache.woden.wsdl20.BindingMessageReference;
import org.apache.woden.wsdl20.BindingOperation;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.ElementDeclaration;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Interface;
import org.apache.woden.wsdl20.InterfaceFault;
import org.apache.woden.wsdl20.InterfaceFaultReference;
import org.apache.woden.wsdl20.InterfaceMessageReference;
import org.apache.woden.wsdl20.InterfaceOperation;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.enumeration.MessageLabel;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingFaultExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingMessageReferenceExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingOperationExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPEndpointExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPHeader;
import org.apache.woden.wsdl20.extensions.http.HTTPLocation;
import org.apache.woden.wsdl20.extensions.rpc.Argument;
import org.apache.woden.wsdl20.extensions.rpc.RPCInterfaceOperationExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingFaultExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingFaultReferenceExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingMessageReferenceExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingOperationExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPEndpointExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPHeaderBlock;
import org.apache.woden.wsdl20.extensions.soap.SOAPModule;
import org.apache.woden.wsdl20.xml.DescriptionElement;
import org.apache.woden.wsdl20.xml.DocumentableElement;
import org.apache.woden.wsdl20.xml.DocumentationElement;
import org.apache.woden.wsdl20.xml.InterfaceMessageReferenceElement;
import org.apache.woden.wsdl20.xml.TypesElement;
import org.apache.woden.xml.XMLAttr;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WSDL20ToAxisServiceBuilder extends WSDLToAxisServiceBuilder {

    protected static final Log log = LogFactory.getLog(WSDL20ToAxisServiceBuilder.class);
    protected Description description;

    private String wsdlURI;

    protected String interfaceName;

    private String savedTargetNamespace;

    private NamespaceDeclaration[] namespacemap;

    private List operationNames = new ArrayList();

    private NamespaceMap stringBasedNamespaceMap;

    private boolean setupComplete = false;

    private Service wsdlService;

    private boolean isAllPorts;

    private URIResolver customWSDLResolver;

//    As bindings are processed add it to this array so that we dont process the same binding twice
    private Map processedBindings;


    public WSDL20ToAxisServiceBuilder(InputStream in, QName serviceName,
                                      String interfaceName) {
        this.in = in;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        this.axisService = new AxisService();
        setPolicyRegistryFromService(axisService);
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri,
                                      String name, String interfaceName) throws WSDLException {
        String fullPath = wsdlUri;
        if (!wsdlUri.startsWith("http://")) {
            File file = new File(wsdlUri);
            fullPath = file.getAbsolutePath();
        }
        setBaseUri(fullPath);
        Description description;
        try {
            description = readInTheWSDLFile(fullPath);
        } catch (AxisFault axisFault) {
            throw new WSDLException("ERROR", "Exception occured while reading WSDL 2.0 doc", axisFault);
        }

        DescriptionElement descriptionElement = description.toElement();
        savedTargetNamespace = descriptionElement.getTargetNamespace()
                .toString();
        namespacemap = descriptionElement.getDeclaredNamespaces();
        this.description = description;
        this.serviceName = null;
        if (name != null) {
            serviceName = new QName(descriptionElement.getTargetNamespace().toString(), name);
        }
        this.interfaceName = interfaceName;
        this.axisService = new AxisService();
        setPolicyRegistryFromService(axisService);
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri,
                                      String name, String interfaceName, boolean isAllPorts) throws WSDLException {
        this(wsdlUri, name, interfaceName);
        this.isAllPorts = isAllPorts;
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri, QName serviceName) {
        super(null, serviceName);
        this.wsdlURI = wsdlUri;
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri, AxisService service) {
        super(null, service);
        this.wsdlURI = wsdlUri;
    }

    public boolean isAllPorts() {
        return isAllPorts;
    }

    public void setAllPorts(boolean allPorts) {
        isAllPorts = allPorts;
    }

    /**
     * sets a custom WSDL locator
     *
     * @param customResolver - A custom Resolver that can resolve imports and includes
     */
    public void setCustomWSDLResolver(URIResolver customResolver) {
        this.customWSDLResolver = customResolver;
    }

    public AxisService populateService() throws AxisFault {

        try {
            setup();
            // Setting wsdl4jdefintion to axisService , so if some one want
            // to play with it he can do that by getting the parameter
            Parameter wsdlDescriptionParamter = new Parameter();
            wsdlDescriptionParamter.setName(WSDLConstants.WSDL_20_DESCRIPTION);
            wsdlDescriptionParamter.setValue(description);
            axisService.addParameter(wsdlDescriptionParamter);

            if (isCodegen) {
                axisService.addParameter("isCodegen", Boolean.TRUE);
            }
            if (description == null) {
                return null;
            }
            // setting target name space
            axisService.setTargetNamespace(savedTargetNamespace);

            // if there are documentation elements in the root. Lets add them as the wsdlService description
            // but since there can be multiple documentation elements, lets only add the first one
            addDocumentation(axisService, description.toElement());
//            DocumentationElement[] documentationElements = description.toElement().getDocumentationElements();
//            if (documentationElements != null && documentationElements.length > 0) {
//                axisService.setServiceDescription(documentationElements[0].getContent().toString());
//            }

            // adding ns in the original WSDL
            // processPoliciesInDefintion(wsdl4jDefinition); TODO : Defering policy handling for now - Chinthaka
            // policy support

            // schema generation

            // Create the namespacemap

            axisService.setNamespaceMap(stringBasedNamespaceMap);
            // TypeDefinition[] typeDefinitions =
            // description.getTypeDefinitions();
            // for(int i=0; i<typeDefinitions.length; i++){
            // if("org.apache.ws.commons.schema".equals(typeDefinitions[i].getContentModel())){
            // axisService.addSchema((XmlSchema)typeDefinitions[i].getContent());
            // }else
            // if("org.w3c.dom".equals(typeDefinitions[i].getContentModel())){
            // axisService.addSchema(getXMLSchema((Element)typeDefinitions[i].getContent(),
            // null));
            // }
            //
            // }

            DescriptionElement descriptionElement = description.toElement();
            TypesElement typesElement = descriptionElement
                    .getTypesElement();
            if (typesElement != null) {
                Schema[] schemas = typesElement.getSchemas();
                for (int i = 0; i < schemas.length; i++) {
                    XmlSchema schemaDefinition = schemas[i].getSchemaDefinition();

                    // WSDL 2.0 spec requires that even the built-in schema should be returned
                    // once asked for schema definitions. But for data binding purposes we can ignore that
                    if (schemaDefinition != null && !Constants.URI_2001_SCHEMA_XSD
                            .equals(schemaDefinition.getTargetNamespace())) {
                        axisService.addSchema(schemaDefinition);
                    }
                }
            }

            processService();
            return axisService;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private void processEndpoints(Interface serviceInterface) throws AxisFault {
        Endpoint[] endpoints = wsdlService.getEndpoints();

        if (endpoints.length == 0) {
            throw new AxisFault("No endpoints found in the WSDL");
        }

        processedBindings = new HashMap();
        Endpoint endpoint = null;

        // If the interface name is not null thats means that this is a call from the codegen engine
        // and we need to populate a single endpoint. Hence find the endpoint and populate it.
        // If that was not the case then we need to check whether the call is from the codegen
        // engine with thw allports check false. If its so no need to populate all endpoints, we
        // select an enspoint accrding to the following criteria.
        // 1. Find the first SOAP 1.2 endpoint
        // 2. Find the first SOAP 1.1 endpoint
        // 3. Use the first endpoint
        if (this.interfaceName != null) {
            for (int i = 0; i < endpoints.length; ++i) {
                if (this.interfaceName.equals(endpoints[i].getName().toString())) {
                    endpoint = endpoints[i];
                    break;  // found it.  Stop looking
                }
            }
            if (endpoint == null) {
                throw new AxisFault("No endpoint found for the given name :"
                        + this.interfaceName);
            }

            axisService
                    .addEndpoint(endpoint.getName().toString(),
                                 processEndpoint(endpoint, serviceInterface));
        } else if (this.isCodegen && !this.isAllPorts) {
            Endpoint soap11Endpoint = null;
            for (int i = 0; i < endpoints.length; ++i) {
                Binding binding = endpoints[i].getBinding();
                if (WSDL2Constants.URI_WSDL2_SOAP.equals(binding.getType().toString())) {
                    SOAPBindingExtensions soapBindingExtensions;
                    try {
                        soapBindingExtensions = (SOAPBindingExtensionsImpl) binding
                                .getComponentExtensionContext(
                                        new URI(WSDL2Constants.URI_WSDL2_SOAP));
                    } catch (URISyntaxException e) {
                        throw new AxisFault("Soap Binding Extention not found");
                    }
                    if (!WSDL2Constants.SOAP_VERSION_1_1
                            .equals(soapBindingExtensions.getSoapVersion())) {
                        endpoint = endpoints[i];
                        break;  // found it.  Stop looking
                    } else if (soap11Endpoint == null){
                        soap11Endpoint = endpoints[i];
                    }
                }
            }
            if (endpoint == null) {
                endpoint = endpoints[0];
            }

            axisService
                    .addEndpoint(endpoint.getName().toString(),
                                 processEndpoint(endpoint, serviceInterface));
        } else {
            for (int i = 0; i < endpoints.length; i++) {
                axisService
                        .addEndpoint(endpoints[i].getName().toString(),
                                     processEndpoint(endpoints[i], serviceInterface));
            }
        }

        if (endpoint == null && endpoints.length > 0) {
            endpoint = endpoints[0];
        }

        if (endpoint != null) {
            axisService.setEndpointName(endpoint.getName().toString());
            axisService.setBindingName(endpoint.getBinding().getName().getLocalPart());
            axisService.setEndpointURL(endpoint.getAddress().toString());
        }
    }

    private void processService() throws AxisFault {
        Service[] services = description.getServices();
        if (services.length == 0) {
            throw new AxisFault("No wsdlService found in the WSDL");
        }

        if (serviceName != null) {
            for (int i = 0; i < services.length; i++) {
                if (serviceName.equals(services[i].getName())) {
                    wsdlService = services[i];
                    break;  // found it. Stop looking.
                }
            }
            if (wsdlService == null) {
                throw new AxisFault("Service with the specified name not found in the WSDL : "
                        + serviceName.getLocalPart());
            }
        } else {
           wsdlService = services[0];
        }

        axisService.setName(wsdlService.getName().getLocalPart());
        Interface serviceInterface = wsdlService.getInterface();
        axisService.addParameter(new Parameter(WSDL2Constants.INTERFACE_LOCAL_NAME, serviceInterface.getName().getLocalPart()));
        processInterface(serviceInterface);
        if (isCodegen) {
            axisService.setOperationsNameList(operationNames);
        }
        processEndpoints(serviceInterface);

    }

    private AxisEndpoint processEndpoint(Endpoint endpoint, Interface serviceInterface) throws AxisFault {
        AxisEndpoint axisEndpoint = new AxisEndpoint();
        axisEndpoint.setParent(axisService);
        axisEndpoint.setName(endpoint.getName().toString());
        setEndpointURL(axisEndpoint, endpoint.getAddress().toString());
        Binding binding = endpoint.getBinding();
        AxisBinding axisBinding;
        if (processedBindings.containsKey(binding.getName())) {
            axisBinding = (AxisBinding) processedBindings.get(binding.getName());
        } else {
            axisBinding = processBinding(binding, serviceInterface);
        }
        axisEndpoint.setBinding(axisBinding);
        
        String bindingType = binding.getType().toString();
        if (bindingType.equals(WSDL2Constants.URI_WSDL2_SOAP)) {
            processSOAPBindingEndpointExtensions(endpoint, axisEndpoint);
        } else if (bindingType.equals(WSDL2Constants.URI_WSDL2_HTTP)) {
            processHTTPBindingEndpointExtensions(endpoint, axisEndpoint);
        }
        addDocumentation(axisEndpoint, endpoint.toElement());
        return axisEndpoint;
    }

    private void processSOAPBindingEndpointExtensions(Endpoint endpoint, AxisEndpoint axisEndpoint) throws AxisFault {
        SOAPEndpointExtensions soapEndpointExtensions;
        try {
            soapEndpointExtensions = (SOAPEndpointExtensions) endpoint
                    .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("SOAP Binding Endpoint Extension not found");
        }

        if (soapEndpointExtensions != null) {
            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_TYPE,
                                     soapEndpointExtensions.getHttpAuthenticationScheme());
            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_REALM,
                                     soapEndpointExtensions.getHttpAuthenticationRealm());
        }
    }

    private void processHTTPBindingEndpointExtensions(Endpoint endpoint, AxisEndpoint axisEndpoint) throws AxisFault {
        HTTPEndpointExtensions httpEndpointExtensions;
        try {
            httpEndpointExtensions = (HTTPEndpointExtensions) endpoint
                    .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_HTTP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Endpoint Extension not found");
        }

        if (httpEndpointExtensions != null) {
            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_TYPE,
                                     httpEndpointExtensions.getHttpAuthenticationScheme());
            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_REALM,
                                     httpEndpointExtensions.getHttpAuthenticationRealm());
        }
    }

    /**
     * contains all code which gathers non-wsdlService specific information from the
     * wsdl.
     * <p/>
     * After all the setup completes successfully, the setupComplete field is
     * set so that any subsequent calls to setup() will result in a no-op. Note
     * that subclass WSDL20ToAllAxisServicesBuilder will call populateService
     * for each endpoint in the WSDL. Separating the non-wsdlService specific
     * information here allows WSDL20ToAllAxisServicesBuilder to only do this
     * work 1 time per WSDL, instead of for each endpoint on each wsdlService.
     *
     * @throws AxisFault - Thrown in case the necessary resources are not available in the WSDL
     * @throws WSDLException - Thrown in case Woden throws an exception
     */
    protected void setup() throws AxisFault, WSDLException {
        if (setupComplete) { // already setup, just do nothing and return
            return;
        }
        try {
            if (description == null) {

                Description description;
                DescriptionElement descriptionElement;
                if (wsdlURI != null && !"".equals(wsdlURI)) {
                    description = readInTheWSDLFile(wsdlURI);
                    descriptionElement = description.toElement();
                } else if (in != null) {
                    description = readInTheWSDLFile(in);
                    descriptionElement = description.toElement();
                } else {
                    throw new AxisFault("No resources found to read the wsdl");
                }

                savedTargetNamespace = descriptionElement.getTargetNamespace().toString();
                namespacemap = descriptionElement.getDeclaredNamespaces();
                this.description = description;

            }
            // Create the namespacemap

            stringBasedNamespaceMap = new NamespaceMap();
            for (int i = 0; i < namespacemap.length; i++) {
                NamespaceDeclaration namespaceDeclaration = namespacemap[i];
                stringBasedNamespaceMap.put(namespaceDeclaration.getPrefix(),
                                            namespaceDeclaration.getNamespaceURI().toString());
            }

            setupComplete = true;
        } catch (AxisFault e) {
            throw e; // just rethrow AxisFaults
        } catch (WSDLException e) {
            // Preserve the WSDLException
            throw e;
        } catch(Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private AxisBinding processBinding(Binding binding, Interface serviceInterface)
            throws AxisFault {
        AxisBinding axisBinding = new AxisBinding();
        axisBinding.setName(binding.getName());
        String bindingType = binding.getType().toString();
        axisBinding.setType(bindingType);

        if (bindingType.equals(WSDL2Constants.URI_WSDL2_SOAP)) {
            processSOAPBindingExtention(binding, axisBinding, serviceInterface);
        } else if (bindingType.equals(WSDL2Constants.URI_WSDL2_HTTP)) {
            processHTTPBindingExtention(binding, axisBinding, serviceInterface);
        }

        // We should process the interface based on the service not on a binding

        processedBindings.put(binding.getName(), axisBinding);
        addDocumentation(axisBinding, binding.toElement());
        return axisBinding;
    }

    private void processSOAPBindingExtention(Binding binding, AxisBinding axisBinding, Interface serviceInterface)
            throws AxisFault {

        // Capture all the binding specific properties

        // Set a comparator so tha httpLocations are stored in decending order
        Map httpLocationTable = new TreeMap(new Comparator(){
            public int compare(Object o1, Object o2) {
                return (-1 * ((Comparable)o1).compareTo(o2));
            }
        });
        SOAPBindingExtensions soapBindingExtensions;
        try {
            soapBindingExtensions = (SOAPBindingExtensionsImpl) binding
                    .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("Soap Binding Extention not found");
        }

        String soapVersion;
        if ((soapVersion = soapBindingExtensions.getSoapVersion()) != null) {
            if (soapVersion.equals(WSDL2Constants.SOAP_VERSION_1_1)) {
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            } else {
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            }
        } else {
            // Set the default to soap 1.2
            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                    WSDL20DefaultValueHolder.getDefaultValue(
                                            WSDL2Constants.ATTR_WSOAP_VERSION));
        }

        URI soapUnderlyingProtocol = soapBindingExtensions.getSoapUnderlyingProtocol();
        if (soapUnderlyingProtocol != null) {
            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_PROTOCOL,
                                    soapUnderlyingProtocol.toString());
        }
        URI soapMepDefault = soapBindingExtensions.getSoapMepDefault();
        if (soapMepDefault != null) {
            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_MEP,
                                    soapMepDefault.toString());
        }
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                soapBindingExtensions.getHttpContentEncodingDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                                createSoapModules(soapBindingExtensions.getSoapModules()));
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                                soapBindingExtensions.getHttpQueryParameterSeparatorDefault());

        // Capture all the fault specific properties

        BindingFault[] bindingFaults = binding.getBindingFaults();
        for (int i = 0; i < bindingFaults.length; i++) {
            BindingFaultImpl bindingFault = (BindingFaultImpl) bindingFaults[i];
            InterfaceFault interfaceFault = serviceInterface.getFromAllInterfaceFaults(bindingFault.getRef());
            AxisBindingMessage axisBindingFault = new AxisBindingMessage();
            axisBindingFault.setFault(true);
            axisBindingFault.setName(interfaceFault.getName().getLocalPart());
            axisBindingFault.setParent(axisBinding);

            addDocumentation(axisBindingFault, interfaceFault.toElement());
            SOAPBindingFaultExtensions soapBindingFaultExtensions;

            try {
                soapBindingFaultExtensions = (SOAPBindingFaultExtensions) bindingFault
                        .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_SOAP));
            } catch (URISyntaxException e) {
                throw new AxisFault("Soap Binding Extention not found");
            }

            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                                         createHttpHeaders(
                                                 soapBindingFaultExtensions.getHttpHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                         soapBindingFaultExtensions.getHttpContentEncoding());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_CODE,
                                         soapBindingFaultExtensions.getSoapFaultCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_SUBCODES,
                                         soapBindingFaultExtensions.getSoapFaultSubcodes());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                                         createSoapHeaders(
                                                 soapBindingFaultExtensions.getSoapHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                                         createSoapModules(
                                                 soapBindingFaultExtensions.getSoapModules()));

            axisBinding.addFault(axisBindingFault);

        }

        // Capture all the binding operation specific properties

        BindingOperation[] bindingOperations = binding.getBindingOperations();
        for (int i = 0; i < bindingOperations.length; i++) {
            BindingOperationImpl bindingOperation = (BindingOperationImpl) bindingOperations[i];

            AxisBindingOperation axisBindingOperation = new AxisBindingOperation();
            InterfaceOperation interfaceOperation = serviceInterface.getFromAllInterfaceOperations(bindingOperation.getRef());
            AxisOperation axisOperation =
                    axisService.getOperation(interfaceOperation.getName());

            axisBindingOperation.setAxisOperation(axisOperation);
            axisBindingOperation.setParent(axisBinding);
            axisBindingOperation.setName(axisOperation.getName());
            addDocumentation(axisBindingOperation, bindingOperation.toElement());
            SOAPBindingOperationExtensions soapBindingOperationExtensions;
            try {
                soapBindingOperationExtensions = ((SOAPBindingOperationExtensions)
                        bindingOperation.getComponentExtensionContext(
                                new URI(WSDL2Constants.URI_WSDL2_SOAP)));
            } catch (URISyntaxException e) {
                throw new AxisFault("Soap Binding Extention not found");
            }

            URI soapAction = soapBindingOperationExtensions.getSoapAction();
            if (soapAction != null && !"\"\"".equals(soapAction.toString())) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION,
                                                 soapAction.toString());
            }
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                                             createSoapModules(
                                                     soapBindingOperationExtensions.getSoapModules()));
            URI soapMep = soapBindingOperationExtensions.getSoapMep();
            if (soapMep != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_MEP,
                                                 soapMep.toString());
            }
            HTTPLocation httpLocation = soapBindingOperationExtensions.getHttpLocation();
            // If httpLocation is not null we should extract a constant part from it and add its value and the
            // corresponding AxisOperation to a map in order to dispatch rest messages. If httpLocation is null we add
            // the operation name into this map.
            String httpLocationString = null;
            if (httpLocation != null) {
                String httpLocationTemplete = httpLocation.getOriginalLocation();
                axisBindingOperation
                        .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocationTemplete);
                httpLocationString = WSDLUtil.getConstantFromHTTPLocation(httpLocationTemplete, HTTPConstants.HEADER_POST);

            }
            if (httpLocationString != null){
                // this map is used to dispatch operation based on request URI , in the HTTPLocationBasedDispatcher
                httpLocationTable.put(httpLocationString, axisOperation);
            }
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                             soapBindingOperationExtensions.getHttpContentEncodingDefault());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                                             soapBindingOperationExtensions.getHttpQueryParameterSeparator());


            BindingMessageReference[] bindingMessageReferences =
                    bindingOperation.getBindingMessageReferences();
            for (int j = 0; j < bindingMessageReferences.length; j++) {
                BindingMessageReference bindingMessageReference = bindingMessageReferences[j];

                AxisBindingMessage axisBindingMessage = new AxisBindingMessage();
                axisBindingMessage.setParent(axisBindingOperation);
                addDocumentation(axisBindingMessage, bindingMessageReference.toElement());
                AxisMessage axisMessage = axisOperation.getMessage(bindingMessageReference
                        .getInterfaceMessageReference().getMessageLabel().toString());

                axisBindingMessage.setAxisMessage(axisMessage);
                axisBindingMessage.setName(axisMessage.getName());
                axisBindingMessage.setDirection(axisMessage.getDirection());


                SOAPBindingMessageReferenceExtensions soapBindingMessageReferenceExtensions;
                try {
                    soapBindingMessageReferenceExtensions =
                            (SOAPBindingMessageReferenceExtensions) bindingMessageReference
                                    .getComponentExtensionContext(
                                            new URI(WSDL2Constants.URI_WSDL2_SOAP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("Soap Binding Extention not found");
                }

                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                                               createHttpHeaders(
                                                       soapBindingMessageReferenceExtensions.getHttpHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                               soapBindingMessageReferenceExtensions.getHttpContentEncoding());
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                                               createSoapHeaders(
                                                       soapBindingMessageReferenceExtensions.getSoapHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                                               createSoapModules(
                                                       soapBindingMessageReferenceExtensions.getSoapModules()));

                axisBindingOperation.addChild(axisMessage.getDirection(), axisBindingMessage);

            }

            BindingFaultReference [] bindingFaultReferences =
                    bindingOperation.getBindingFaultReferences();
            for (int j = 0; j < bindingFaultReferences.length; j++) {
                BindingFaultReference bindingFaultReference = bindingFaultReferences[j];

                AxisBindingMessage axisBindingMessageFault = new AxisBindingMessage();
                addDocumentation(axisBindingMessageFault, bindingFaultReference.toElement());
                axisBindingMessageFault.setParent(axisBindingOperation);
                axisBindingMessageFault.setFault(true);
                axisBindingMessageFault.setName(bindingFaultReference.getInterfaceFaultReference()
                        .getInterfaceFault().getName().getLocalPart());

                SOAPBindingFaultReferenceExtensions soapBindingFaultReferenceExtensions;
                try {
                    soapBindingFaultReferenceExtensions =
                            (SOAPBindingFaultReferenceExtensions) bindingFaultReference
                                    .getComponentExtensionContext(
                                            new URI(WSDL2Constants.URI_WSDL2_SOAP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("Soap Binding Extention not found");
                }

                axisBindingMessageFault.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                                                    createSoapModules(
                                                            soapBindingFaultReferenceExtensions.getSoapModules()));

                axisBindingOperation.addFault(axisBindingMessageFault);

            }
            axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);


        }
    }

    private void processHTTPBindingExtention(Binding binding, AxisBinding axisBinding, Interface serviceInterface)
            throws AxisFault {

        Map httpLocationTable = createHttpLocationTable();
        // Capture all the binding specific properties

        HTTPBindingExtensionsImpl httpBindingExtensions;
        try {
            httpBindingExtensions = (HTTPBindingExtensionsImpl) binding
                    .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_HTTP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Extention not found");
        }

        String httpMethodDefault = httpBindingExtensions.getHttpMethodDefault();
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD,
                                httpMethodDefault);
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                                httpBindingExtensions.getHttpQueryParameterSeparatorDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                httpBindingExtensions.getHttpContentEncodingDefault());

        // Capture all the fault specific properties

        BindingFault[] bindingFaults = binding.getBindingFaults();
        for (int i = 0; i < bindingFaults.length; i++) {
            BindingFaultImpl bindingFault = (BindingFaultImpl) bindingFaults[i];
            InterfaceFault interfaceFault =
                    serviceInterface.getFromAllInterfaceFaults(bindingFault.getRef());
            AxisBindingMessage axisBindingFault = new AxisBindingMessage();
            axisBindingFault.setFault(true);
            axisBindingFault.setName(interfaceFault.getName().getLocalPart());
            axisBindingFault.setParent(axisBinding);

            addDocumentation(axisBindingFault, interfaceFault.toElement());
            HTTPBindingFaultExtensions httpBindingFaultExtensions;

            try {
                httpBindingFaultExtensions = (HTTPBindingFaultExtensions) bindingFault
                        .getComponentExtensionContext(new URI(WSDL2Constants.URI_WSDL2_HTTP));
            } catch (URISyntaxException e) {
                throw new AxisFault("HTTP Binding Extention not found");
            }

            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CODE,
                                         httpBindingFaultExtensions
                                                 .getHttpErrorStatusCode().getCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                                         createHttpHeaders(
                                                 httpBindingFaultExtensions.getHttpHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                         httpBindingFaultExtensions.getHttpContentEncoding());
            axisBinding.addFault(axisBindingFault);

        }

        // Capture all the binding operation specific properties

        BindingOperation[] bindingOperations = binding.getBindingOperations();
        for (int i = 0; i < bindingOperations.length; i++) {
            BindingOperationImpl bindingOperation = (BindingOperationImpl) bindingOperations[i];

            AxisBindingOperation axisBindingOperation = new AxisBindingOperation();
            InterfaceOperation interfaceOperation = serviceInterface.getFromAllInterfaceOperations(bindingOperation.getRef());
            AxisOperation axisOperation =
                    axisService.getOperation(interfaceOperation.getName());

            axisBindingOperation.setAxisOperation(axisOperation);
            axisBindingOperation.setParent(axisBinding);
            axisBindingOperation.setName(axisOperation.getName());

            addDocumentation(axisBindingOperation, bindingOperation.toElement());
            HTTPBindingOperationExtensions httpBindingOperationExtensions;
            try {
                httpBindingOperationExtensions = ((HTTPBindingOperationExtensions)
                        bindingOperation.getComponentExtensionContext(
                                new URI(WSDL2Constants.URI_WSDL2_HTTP)));
            } catch (URISyntaxException e) {
                throw new AxisFault("HTTP Binding Extention not found");
            }

            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_FAULT_SERIALIZATION,
                                             httpBindingOperationExtensions.getHttpFaultSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                                             httpBindingOperationExtensions.getHttpInputSerialization());
            String httpMethod = httpBindingOperationExtensions.
                    getHttpMethod();
            if (httpMethod == null) {
                if (httpMethodDefault != null) {
                    httpMethod = httpMethodDefault;
                } else {
                    Boolean safeParameter =
                            (Boolean) axisOperation.getParameterValue(WSDL2Constants.ATTR_WSDLX_SAFE);
                    if (safeParameter != null && safeParameter.booleanValue()){
                        httpMethod = HTTPConstants.HEADER_GET;
                    } else {
                        httpMethod = HTTPConstants.HEADER_POST;
                    }
                }
            }
            axisBindingOperation
                    .setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpMethod);
            HTTPLocation httpLocation = httpBindingOperationExtensions.getHttpLocation();

            // If httpLocation is not null we should extract a constant part from it and add its value and the
            // corresponding AxisOperation to a map in order to dispatch rest messages. If httpLocation is null we add
            // the operation name into this map.
            String httpLocationString = "";
            if (httpLocation != null) {
                String httpLocationTemplete = httpLocation.getOriginalLocation();
                axisBindingOperation
                        .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocationTemplete);
                httpLocationString = WSDLUtil.getConstantFromHTTPLocation(httpLocationTemplete, httpMethod);

            }

            httpLocationTable.put(httpLocationString, axisOperation);

            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED,
                                             httpBindingOperationExtensions.
                                                     isHttpLocationIgnoreUncited());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION,
                                             httpBindingOperationExtensions.getHttpOutputSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                                             httpBindingOperationExtensions.getHttpQueryParameterSeparator());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                             httpBindingOperationExtensions.getHttpContentEncodingDefault());

            BindingMessageReference[] bindingMessageReferences =
                    bindingOperation.getBindingMessageReferences();
            for (int j = 0; j < bindingMessageReferences.length; j++) {
                BindingMessageReference bindingMessageReference = bindingMessageReferences[j];

                AxisBindingMessage axisBindingMessage = new AxisBindingMessage();
                axisBindingMessage.setParent(axisBindingOperation);

                AxisMessage axisMessage = axisOperation.getMessage(bindingMessageReference
                        .getInterfaceMessageReference().getMessageLabel().toString());

                axisBindingMessage.setAxisMessage(axisMessage);
                axisBindingMessage.setName(axisMessage.getName());
                axisBindingMessage.setDirection(axisMessage.getDirection());

                addDocumentation(axisBindingMessage, bindingMessageReference.toElement());
                HTTPBindingMessageReferenceExtensions httpBindingMessageReferenceExtensions;
                try {
                    httpBindingMessageReferenceExtensions =
                            (HTTPBindingMessageReferenceExtensions) bindingMessageReference
                                    .getComponentExtensionContext(
                                            new URI(WSDL2Constants.URI_WSDL2_HTTP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("HTTP Binding Extention not found");
                }

                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                                               createHttpHeaders(
                                                       httpBindingMessageReferenceExtensions.getHttpHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                               httpBindingMessageReferenceExtensions.getHttpContentEncoding());
                axisBindingOperation.addChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE, axisBindingMessage);

            }

            BindingFaultReference[] bindingFaultReferences =
                    bindingOperation.getBindingFaultReferences();
            for (int j = 0; j < bindingFaultReferences.length; j++) {
                BindingFaultReference bindingFaultReference = bindingFaultReferences[j];

                AxisBindingMessage axisBindingMessageFault = new AxisBindingMessage();
                axisBindingMessageFault.setFault(true);
                axisBindingMessageFault.setName(bindingFaultReference.getInterfaceFaultReference()
                        .getInterfaceFault().getName().getLocalPart());
                axisBindingMessageFault.setParent(axisBindingOperation);
                axisBindingOperation.addFault(axisBindingMessageFault);
                addDocumentation(axisBindingMessageFault, bindingFaultReference.toElement());

            }

            axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);

        }
    }

    private void processInterface(Interface serviceInterface)
            throws AxisFault {

        // TODO copy the policy elements
        // copyExtensionAttributes(wsdl4jPortType.getExtensionAttributes(),
        // axisService, PORT_TYPE);

        InterfaceOperation[] interfaceOperations = serviceInterface
                .getInterfaceOperations();
        for (int i = 0; i < interfaceOperations.length; i++) {
            axisService.addOperation(populateOperations(interfaceOperations[i]));
            operationNames.add(interfaceOperations[i].getName());
        }

        Interface[] extendedInterfaces = serviceInterface.getExtendedInterfaces();
        for (int i = 0; i < extendedInterfaces.length; i++) {
            Interface extendedInterface = extendedInterfaces[i];
            processInterface(extendedInterface);
        }

    }

    private AxisOperation populateOperations(InterfaceOperation operation) throws AxisFault {
        QName opName = operation.getName();
        // Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);

        if (axisOperation == null) {
            URI pattern = operation.getMessageExchangePattern();
            String MEP;
            if (pattern == null) {
                MEP = WSDL20DefaultValueHolder.getDefaultValue(WSDL2Constants.ATTR_WSOAP_MEP);
            } else {
                MEP = pattern.toString();
            }
            if(!isServerSide){
            	// If in the client, need to toggle in-out to out-in etc.
            	if(WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)){
            		MEP = WSDL2Constants.MEP_URI_OUT_IN;
            	}
            	if(WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)){
            		MEP = WSDL2Constants.MEP_URI_OUT_ONLY;
            	}
            	if(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)){
            		MEP = WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN;
            	}
            }
            axisOperation = AxisOperationFactory.getOperationDescription(MEP);
            axisOperation.setName(opName);

        }
        URI[] operationStyle = operation.getStyle();
        if (operationStyle != null && operationStyle.length > 0) {
            Parameter opStyleParameter = new Parameter();
            opStyleParameter.setName(WSDL2Constants.OPERATION_STYLE);
            opStyleParameter.setValue(operationStyle);
            axisOperation.addParameter(opStyleParameter);
        }
        addDocumentation(axisOperation, operation.toElement());

        // assuming the style of the operations of WSDL 2.0 is always document, for the time being :)
        // The following can be used to capture the wsdlx:safe attribute

        InterfaceOperationExtensionsImpl interfaceOperationExtensions;
        try {
            interfaceOperationExtensions = (InterfaceOperationExtensionsImpl) operation
                    .getComponentExtensionContext(
                            new URI(WSDL2Constants.URI_WSDL2_EXTENSIONS));
        } catch (URISyntaxException e) {
            throw new AxisFault("WSDL2 extensions not defined for this operation");
        }

        if (interfaceOperationExtensions != null) {
            Parameter parameter = new Parameter(WSDL2Constants.ATTR_WSDLX_SAFE, Boolean.valueOf(
                    interfaceOperationExtensions.isSafe()));
            axisOperation.addParameter(parameter);
        }

        RPCInterfaceOperationExtensions rpcInterfaceOperationExtensions;
        try {
            rpcInterfaceOperationExtensions = (RPCInterfaceOperationExtensions) operation
                    .getComponentExtensionContext(
                            new URI(WSDL2Constants.URI_WSDL2_RPC));
        } catch (URISyntaxException e) {
            throw new AxisFault("WSDL2 extensions not defined for this operation");
        }

        if (rpcInterfaceOperationExtensions != null) {
            String rpcsig = "";
            Argument[] signatures = rpcInterfaceOperationExtensions.getRPCSignature();
            for (int i = 0; i < signatures.length; i++) {
                Argument sigArgument = signatures[i];
                rpcsig = rpcsig + sigArgument.getName().getLocalPart() + " " + sigArgument.getDirection() + " ";
            }
            Parameter parameter = new Parameter(WSDL2Constants.ATTR_WRPC_SIGNATURE, rpcsig);
            axisOperation.addParameter(parameter);
        }

        InterfaceMessageReference[] interfaceMessageReferences = operation
                .getInterfaceMessageReferences();
        for (int i = 0; i < interfaceMessageReferences.length; i++) {
            InterfaceMessageReference messageReference = interfaceMessageReferences[i];
            if (messageReference.getMessageLabel().equals(
                    MessageLabel.IN)) {
                // Its an input message

                if (isServerSide) {
                    createAxisMessage(axisOperation, messageReference,
                                      WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                } else {
                    createAxisMessage(axisOperation, messageReference,
                                      WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                }
            } else if (messageReference.getMessageLabel().equals(
                    MessageLabel.OUT)) {
                if (isServerSide) {
                    createAxisMessage(axisOperation, messageReference,
                                      WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                } else {
                    createAxisMessage(axisOperation, messageReference,
                                      WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                }
            }

        }

        // add operation level faults

        InterfaceFaultReference[] faults = operation.getInterfaceFaultReferences();
        for (int i = 0; i < faults.length; i++) {
            AxisMessage faultMessage = new AxisMessage();

            InterfaceFaultReference interfaceFaultReference = faults[i];
            faultMessage.setDirection(interfaceFaultReference.getDirection().toString());

            InterfaceFault interfaceFault = interfaceFaultReference.getInterfaceFault();

            if (interfaceFault == null) {
                throw new AxisFault("Interface Fault reference defined in operation " + opName + " cannot be found in interface");
            }

            faultMessage.setElementQName(interfaceFault.getElementDeclaration().getName());
            faultMessage.setName(interfaceFault.getName().getLocalPart());

            axisOperation.setFaultMessages(faultMessage);
        }


        return axisOperation;
    }

    private void createAxisMessage(AxisOperation axisOperation,
                                   InterfaceMessageReference messageReference, String messageLabel)
            throws AxisFault {
        AxisMessage message = axisOperation
                .getMessage(messageLabel);

        String messageContentModelName = messageReference.getMessageContentModel();
        QName elementQName = null;

        if (WSDL2Constants.NMTOKEN_ELEMENT.equals(messageContentModelName)) {
            ElementDeclaration elementDeclaration = messageReference.getElementDeclaration();
            if (elementDeclaration == null) {
                InterfaceMessageReferenceElement messageReferenceElement =
                        messageReference.toElement();
                QName qName = messageReferenceElement.getElement().getQName();
                throw new AxisFault("Unable to find element " + qName.toString() + " reffered to by operation " + axisOperation.getName().getLocalPart());
            }
            elementQName = elementDeclaration.getName();
        } else if (WSDL2Constants.NMTOKEN_ANY.equals(messageContentModelName)) {
            elementQName = Constants.XSD_ANY;
        } else
        if (WSDL2Constants.NMTOKEN_NONE.equals(messageContentModelName)) {
            // nothing to do here keep the message element as null
        } else {
            throw new AxisFault("Sorry we do not support " + messageContentModelName +
                    ". We do only support #any, #none and #element as message content models.");
        }

        message.setElementQName(elementQName);
        message.setName(elementQName != null ? elementQName.getLocalPart() : axisOperation.getName().getLocalPart());
        axisOperation.addMessage(message, messageLabel);

        
        if(WSDLConstants.MESSAGE_LABEL_IN_VALUE.equals(messageLabel)){
        	XMLAttr xa = messageReference.toElement().getExtensionAttribute(new QName("http://www.w3.org/2006/05/addressing/wsdl","Action"));
        	if(xa!=null){
        		String value = (String)xa.getContent();
        		if(value != null){
        			ArrayList al = axisOperation.getWSAMappingList();
        			if(al == null){
        				al = new ArrayList();
        				axisOperation.setWsamappingList(al);
        			}
        			al.add(value);
        		}
        	}
        }else{
        	XMLAttr xa = messageReference.toElement().getExtensionAttribute(new QName("http://www.w3.org/2006/05/addressing/wsdl","Action"));
        	if(xa!=null){
        		String value = (String)xa.getContent();
        		if(value != null){
        			axisOperation.setOutputAction(value);
        		}
        	}
        }
        
        // populate this map so that this can be used in SOAPBody based dispatching
        if (elementQName != null) {
            axisService
                    .addMessageElementQNameToOperationMapping(elementQName, axisOperation);
        }
    }

    private Description readInTheWSDLFile(String wsdlURI) throws WSDLException, AxisFault {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        Document document;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(wsdlURI);
        } catch (ParserConfigurationException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (SAXException e) {
            throw AxisFault.makeFault(e);
        }
        return readInTheWSDLFile(document);
    }

    private Description readInTheWSDLFile(InputStream inputStream) throws WSDLException, AxisFault {

       DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        Document document;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        } catch (SAXException e) {
            throw AxisFault.makeFault(e);
        }
        return readInTheWSDLFile(document);
    }

    private Description readInTheWSDLFile(Document document) throws WSDLException {
        WSDLReader reader = DOMWSDLFactory.newInstance().newWSDLReader();
        if (customWSDLResolver != null) {
            reader.setURIResolver(customWSDLResolver);
        }
        // This turns on WSDL validation which is set off by default.
        reader.setFeature(WSDLReader.FEATURE_VALIDATION, true);
        WSDLSource wsdlSource = reader.createWSDLSource();
        wsdlSource.setSource(document.getDocumentElement());
        String uri = getBaseUri();
        if (uri != null && !"".equals(uri)) {
            try {
                wsdlSource.setBaseURI(new URI(uri));
            } catch (URISyntaxException e) {
                File f = new File(uri);
                if(f.exists()) {
                    wsdlSource.setBaseURI(f.toURI());
                } else {
                    log.error(e.toString(), e);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Reading 2.0 WSDL with wsdl uri = " + wsdlURI);
            log.trace("  the stack at this point is: " + stackToString());
        }
        return reader.readWSDL(wsdlSource);
    }

    /**
     * Convert woden dependent SOAPHeaderBlock objects to SOAPHeaderMessage objects
     *
     * @param soapHeaderBlocks - An array of SOAPHeaderBlock objects
     * @return ArrayList - An ArrayList of SOAPHeaderMessage objects
     */
    private ArrayList createSoapHeaders(SOAPHeaderBlock soapHeaderBlocks[]) {

        if (soapHeaderBlocks.length == 0) {
            return null;
        }
        ArrayList soapHeaderMessages = new ArrayList();

        for (int i = 0; i < soapHeaderBlocks.length; i++) {
            SOAPHeaderBlock soapHeaderBlock = soapHeaderBlocks[i];
            ElementDeclaration elementDeclaration = soapHeaderBlock.getElementDeclaration();

            if (elementDeclaration != null) {
                QName name = elementDeclaration.getName();
                SOAPHeaderMessage soapHeaderMessage = new SOAPHeaderMessage();
                soapHeaderMessage.setElement(name);
                soapHeaderMessage.setRequired(soapHeaderBlock.isRequired().booleanValue());
                soapHeaderMessage
                        .setMustUnderstand(soapHeaderBlock.mustUnderstand().booleanValue());
                soapHeaderMessages.add(soapHeaderMessage);
            }
        }
        return soapHeaderMessages;
    }

    /**
     * Convert woden dependent SOAPHeaderBlock objects to SOAPHeaderMessage objects
     *
     * @param soapModules - An array of SOAPModule objects
     * @return ArrayList - An ArrayList of SOAPHeaderMessage objects
     */
    private ArrayList createSoapModules(SOAPModule soapModules[]) {

        if (soapModules.length == 0) {
            return null;
        }
        ArrayList soapModuleMessages = new ArrayList();

        for (int i = 0; i < soapModules.length; i++) {
            SOAPModule soapModule = soapModules[i];
            SOAPModuleMessage soapModuleMessage = new SOAPModuleMessage();
            soapModuleMessage.setUri(soapModule.getRef().toString());
            soapModuleMessages.add(soapModuleMessage);
        }
        return soapModuleMessages;
    }

    /**
     * Convert woden dependent HTTPHeader objects to Header objects
     *
     * @param httpHeaders - An array of HTTPHeader objects
     * @return ArrayList - An ArrayList of Header objects
     */
    private ArrayList createHttpHeaders(HTTPHeader httpHeaders[]) {

        if (httpHeaders.length == 0) {
            return null;
        }
        ArrayList httpHeaderMessages = new ArrayList();

        for (int i = 0; i < httpHeaders.length; i++) {
            HTTPHeaderImpl httpHeader = (HTTPHeaderImpl) httpHeaders[i];
            HTTPHeaderMessage httpHeaderMessage = new HTTPHeaderMessage();
            httpHeaderMessage.setqName(httpHeader.getTypeName());
            httpHeaderMessage.setName(httpHeader.getName());
            httpHeaderMessage.setRequired(httpHeader.isRequired().booleanValue());
            httpHeaderMessages.add(httpHeaderMessage);
        }
        return httpHeaderMessages;
    }

    /**
     * Adds documentation details to a given AxisDescription.
     * The documentation details is extracted from the WSDL element given.
     * @param axisDescription - The documentation will be added to this
     * @param element - The element that the documentation is extracted from.
     */
    private void addDocumentation(AxisDescription axisDescription, DocumentableElement element) {
        DocumentationElement[] documentationElements = element.getDocumentationElements();
        String documentation = "";
        for (int i = 0; i < documentationElements.length; i++) {
            DocumentationElement documentationElement = documentationElements[i];
            XMLElement contentElement = documentationElement.getContent();
            Element content = (Element)contentElement.getSource();
            if (content != null) {
                documentation = documentation + DOM2Writer.nodeToString(content.getFirstChild());
            }
        }
        if (!"".equals(documentation)) {
            axisDescription.setDocumentation(documentation);
        }
    }
    
    private void setEndpointURL(AxisEndpoint axisEndpoint, String endpointURL) {
    	axisEndpoint.setEndpointURL(endpointURL);
    	try {
    		URL url = new URL(endpointURL);
    		axisEndpoint.setTransportInDescription(url.getProtocol());
    	} catch (Exception e) {
    		log.debug(e.getMessage(), e);
		}    	
    }
}
