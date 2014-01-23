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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.wsdl.WSDL11ActionHelper;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.util.WSDLDefinitionWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.http.HTTPUrlEncoded;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMimeXml;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class WSDL11ToAxisServiceBuilder extends WSDLToAxisServiceBuilder {

    public static final int COMPONENT_PORT_TYPE = 1;
    public static final int COMPONENT_MESSAGE = 2;
    public static final int COMPONENT_BINDING = 3;

    protected static final Log log = LogFactory
            .getLog(WSDL11ToAxisServiceBuilder.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();

    protected String portName;

    private static final String BINDING = "Binding";

    private static final String SERVICE = "Service";

    private static final String PORT = "Port";

    private static final String PORT_TYPE = "PortType";

    private static final String PORT_TYPE_OPERATION = "PortType.Operation";

    private static final String PORT_TYPE_OPERATION_INPUT = "PortType.Operation.Input";

    private static final String PORT_TYPE_OPERATION_OUTPUT = "PortType.Operation.Output";

    private static final String PORT_TYPE_OPERATION_FAULT = "PortType.Operation.Fault";

    private static final String BINDING_OPERATION = "Binding.Operation";

    private static final String BINDING_OPERATION_INPUT = "Binding.Operation.Input";

    private static final String BINDING_OPERATION_OUTPUT = "Binding.Operation.Output";

    protected Definition wsdl4jDefinition = null;
    protected String     wsdlBaseDocumentURI = null;

    private WSDLLocator customWSDLResolver;

    public static final String RPC_STYLE = "rpc";

    public static final String DOCUMENT_STYLE = "document";

    public static final String ENCODED_USE = "encoded";

    /**
     * List of BindingOperationEntry objects.
     * Each object in the list may require a wrapped schema element for 
     * the input/output or both.
     */
    private List wrappableBOEs = new ArrayList();
    // used to keep the binding type of the selected binding
    private String bindingType;

    public static final String WRAPPED_OUTPUTNAME_SUFFIX = "Response";

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    public static final String NAMESPACE_DECLARATION_PREFIX = "xmlns:";

    private static int prefixCounter = 0;

    public static final String NAMESPACE_URI = "namespace";

    public static final String TRAGET_NAMESPACE = "targetNamespace";

    public static final String BINDING_TYPE_SOAP = "soap";
    public static final String BINDING_TYPE_HTTP = "http";

    /**
     * keep track of whether setup code related to the entire wsdl is complete.
     * Note that WSDL11ToAllAxisServices will call setup multiple times, so this
     * field is used to make subsequent calls no-ops.
     */
    private boolean setupComplete = false;

    private Map schemaMap = null;

    private static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";

    // As bindings are processed add it to this array so that we dont process the same binding twice
    private Map processedBindings;

    private boolean isAllPorts;

    /**
     * constructor taking in the service name and the port name
     *
     * @param in - InputStream for the WSDL
     * @param serviceName - The service Name
     * @param portName - The port name
     */
    public WSDL11ToAxisServiceBuilder(InputStream in, QName serviceName,
                                      String portName) {
        super(in, serviceName);
        this.portName = portName;
    }

    /**
     * @param def - The WSDL4J Definition object
     * @param serviceName - The service Name
     * @param portName - The port name
     */
    public WSDL11ToAxisServiceBuilder(Definition def, QName serviceName,
                                      String portName) {
        super(null, serviceName);
        this.wsdl4jDefinition = def;
        this.portName = portName;
        this.isAllPorts = false;
    }

    /**
     * @param def - The WSDL4J Definition object
     * @param serviceName - The service Name
     * @param portName - The port name
     * @param isAllPorts - boolean representing whether to generate code for all ports or not
     */
    public WSDL11ToAxisServiceBuilder(Definition def,
                                      QName serviceName,
                                      String portName,
                                      boolean isAllPorts) {
        this(def, serviceName, portName);
        this.isAllPorts = isAllPorts;
    }


    /**
     * @param in
     * @param service
     */
    public WSDL11ToAxisServiceBuilder(InputStream in, AxisService service) {
        super(in, service);
    }

    /**
     * @param in
     */
    public WSDL11ToAxisServiceBuilder(InputStream in) {
        this(in, null, null);
    }

    /**
     * @deprecated
     * @see setCustomWSDLResolver
     */
    public void setCustomWSLD4JResolver(WSDLLocator customResolver) {
        setCustomWSDLResolver(customResolver);
    }


    /**
     * sets a custom WSDL locator
     *
     * @param customResolver
     */
    public void setCustomWSDLResolver(WSDLLocator customResolver) {
        this.customWSDLResolver = customResolver;
        setDocumentBaseUri(this.customWSDLResolver.getBaseURI());
    }


    /**
     * Sets the URI to the base document associated with the WSDL definition.
     * This identifies the origin of the Definition and allows the
     * Definition to be reloaded.  Note that this is the URI of the base
     * document, not the imports.
     *
     * @param baseUri
     */
    public void setDocumentBaseUri(String baseUri) {
        if (wsdl4jDefinition != null) {
            wsdl4jDefinition.setDocumentBaseURI(baseUri);
        }
        wsdlBaseDocumentURI = baseUri;
    }

    /**
     * Gets the URI to the base document associated with the WSDL definition.
     * This identifies the origin of the Definition and allows the
     * Definition to be reloaded.  Note that this is the URI of the base
     * document, not the imports.
     *
     */
    public String getDocumentBaseUri() {
        return wsdlBaseDocumentURI;
    }




    /**
     * Populates a given service.
     *
     * @throws AxisFault
     */
    public AxisService populateService() throws AxisFault {
        try {
            setup();

            // NOTE: set the axisService with the Parameter for the WSDL
            // Definition after the rest of the work

            if (wsdl4jDefinition == null) {
                return null;
            }

            // setting target name space
            axisService.setTargetNamespace(wsdl4jDefinition.getTargetNamespace());
            axisService.setNamespaceMap(new NamespaceMap(wsdl4jDefinition.getNamespaces()));
            
            Map importsMap = wsdl4jDefinition.getImports();
            
            if (importsMap != null) {
                List imports = new ArrayList(importsMap.keySet());
                axisService.setImportedNamespaces(imports);
            }
            
            //TODO : find the service also in imported wsdls
            Service wsdl4jService = findService(wsdl4jDefinition);

            Binding binding = findBinding(wsdl4jDefinition, wsdl4jService);
            Definition bindingWSDL = getParentDefinition(wsdl4jDefinition,
                    binding.getQName(), COMPONENT_BINDING, new HashSet());
            Definition portTypeWSDL = getParentDefinition(bindingWSDL,
                    binding.getPortType().getQName(), COMPONENT_PORT_TYPE, new HashSet());
            PortType portType = portTypeWSDL.getPortType(binding.getPortType().getQName());


            if (portType == null) {
                throw new AxisFault("There is no port type associated with the binding");
            }

            // create new Schema extensions element for wrapping
            // (if its present)
            Element[] schemaElements = generateWrapperSchema(schemaMap, binding, portType);

            processTypes(wsdl4jDefinition, axisService);

            // add the newly created schemas
            if (schemaElements != null && schemaElements.length > 0) {
                for (int i = 0; i < schemaElements.length; i++) {
                    Element schemaElement = schemaElements[i];
                    if (schemaElement != null) {
                        axisService.addSchema(getXMLSchema(schemaElement, null));
                    }
                }
            }
            // copy the documentation element content to the description
            Element documentationElement = wsdl4jDefinition.getDocumentationElement();
            addDocumentation(axisService, documentationElement);

            axisService.setName(wsdl4jService.getQName().getLocalPart());
            populateEndpoints(binding, bindingWSDL,wsdl4jService, portType, portTypeWSDL);
            processPoliciesInDefintion(wsdl4jDefinition);
            axisService.getPolicyInclude().setPolicyRegistry(registry);


            // Setting wsdl4jdefintion to the axisService parameter include list,
            // so if someone needs to use the definition directly,
            // he can do that by getting the parameter
            Parameter wsdlDefinitionParameter = new Parameter();
            wsdlDefinitionParameter.setName(WSDLConstants.WSDL_4_J_DEFINITION);

            if (!(wsdl4jDefinition instanceof WSDLDefinitionWrapper)) {
                AxisConfiguration ac = axisService.getAxisConfiguration();
                if (ac == null) {
                    ac = this.axisConfig;
                }
                WSDLDefinitionWrapper wrapper = new WSDLDefinitionWrapper(wsdl4jDefinition, ac);
                wsdlDefinitionParameter.setValue(wrapper);
            } else {
                wsdlDefinitionParameter.setValue(wsdl4jDefinition);
            }

            axisService.addParameter(wsdlDefinitionParameter);
            axisService.setWsdlFound(true);
            axisService.setCustomWsdl(true);


            return axisService;

        } catch (WSDLException e) {
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e);
            }
            throw AxisFault.makeFault(e);
        } catch (Exception e) {
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e);
            }
            throw AxisFault.makeFault(e);
        }
    }

    private void processTypes(Definition wsdlDefinition, AxisService axisService)
            throws AxisFault {
        processTypes(wsdlDefinition, axisService, new HashSet());
    }

    private void processTypes(Definition wsdlDefinition,
                              AxisService axisService,
                              Set visitedWSDLs)
            throws AxisFault {
        visitedWSDLs.add(wsdlDefinition.getDocumentBaseURI());
        // process all the types in all the wsdls
        Types types = wsdlDefinition.getTypes();
        if (types != null) {
            copyExtensibleElements(types.getExtensibilityElements(),
                                   wsdlDefinition,
                                   axisService,
                                   TYPES);
        }

        // process the types in other wsdls
        Iterator iter = wsdlDefinition.getImports().values().iterator();
        Vector values = null;
        Import wsdlImport = null;
        for (; iter.hasNext();) {
            values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                wsdlImport = (Import) valuesIter.next();
                // process the types recuresiveilt
                Definition innerDefinition = wsdlImport.getDefinition();
                if(!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())){
                    processTypes(innerDefinition, axisService, visitedWSDLs);
                }
            }
        }
    }

    private void addDocumentation(AxisDescription axisDescription, Element documentationElement) {
        if ((documentationElement != null) && (documentationElement.getFirstChild() != null)) {
            Node firstChild = documentationElement.getFirstChild();
            String documentation = DOM2Writer.nodeToString(firstChild);
            if (!"".equals(documentation)) {
                axisDescription.setDocumentation(documentation);
            }
        }
    }

    /**
     * @param binding
     * @param wsdl4jService must have atlease one port
     * @throws AxisFault
     */
    private void populateEndpoints(Binding binding,
                                   Definition bindingWSDL,
                                   Service wsdl4jService,
                                   PortType portType,
                                   Definition portTypeWSDL) throws AxisFault {

        Map wsdl4jPorts = wsdl4jService.getPorts();
        QName bindingName = binding.getQName();

        Port port;
        AxisEndpoint axisEndpoint = null;

        processedBindings = new HashMap();

        // process the port type for this binding
        // although we support multiports they must be belongs to same port type and should have the
        // same soap style
        populatePortType(portType, portTypeWSDL);

        Binding currentBinding;
        Definition currentBindingWSDL = null;

        for (Iterator iterator = wsdl4jPorts.values().iterator(); iterator.hasNext();) {
            port = (Port) iterator.next();
            // if the user has picked a port then we have to process only that port
            if ((this.portName == null) || (this.portName.equals(port.getName()))) {
                // we process the port only if it has the same port type as the selected binding
                currentBindingWSDL = getParentDefinition(wsdl4jDefinition,
                        port.getBinding().getQName(), COMPONENT_BINDING, new HashSet());
                currentBinding = currentBindingWSDL.getBinding(port.getBinding().getQName());

                if (currentBinding.getPortType().getQName().equals(binding.getPortType().getQName())) {
                    axisEndpoint = new AxisEndpoint();
                    axisEndpoint.setName(port.getName());

                    if (axisService.getEndpointName() == null &&
                            bindingName.equals(port.getBinding().getQName())) {
                        populateEndpoint(axisEndpoint, port, currentBinding,
                                bindingWSDL, portType, portTypeWSDL, true);
                        axisService.setEndpointName(axisEndpoint.getName());
                        axisService.setBindingName(axisEndpoint.getBinding().getName().getLocalPart());
                    } else {
                        populateEndpoint(axisEndpoint, port, currentBinding,
                                bindingWSDL, portType, portTypeWSDL, false);
                    }

                    axisEndpoint.setParent(axisService);
                    axisService.addEndpoint(port.getName(), axisEndpoint);
                }
            }
        }
    }

    /**
     * setting message qname is a binding dependent process for an example message element depends on the
     * soap style (rpc or document) and parts elememet of the soap body
     * On the otherhand we keep only one set of axis operations belongs to a selected port type in axis service
     * So setting qname refetences done only with the selected binding processing
     *
     * @param axisEndpoint
     * @param wsdl4jPort
     * @param isSetMessageQNames
     * @throws AxisFault
     */
    private void populateEndpoint(AxisEndpoint axisEndpoint,
                                  Port wsdl4jPort,
                                  Binding wsdl4jBinding,
                                  Definition bindingWSDL,
                                  PortType portType,
                                  Definition portTypeWSDL,
                                  boolean isSetMessageQNames)
            throws AxisFault {

        copyExtensibleElements(wsdl4jPort.getExtensibilityElements(), wsdl4jDefinition,
                               axisEndpoint, BINDING);
        processEmbeddedEPR(wsdl4jPort.getExtensibilityElements(), axisEndpoint);
        addDocumentation(axisEndpoint, wsdl4jPort.getDocumentationElement());
        if (processedBindings.containsKey(wsdl4jBinding.getQName())) {
            axisEndpoint.setBinding(
                    (AxisBinding) processedBindings.get(wsdl4jBinding.getQName()));
        } else {
            AxisBinding axisBinding = new AxisBinding();
            axisBinding.setName(wsdl4jBinding.getQName());
            axisBinding.setParent(axisEndpoint);
            axisEndpoint.setBinding(axisBinding);
            axisBinding.setParent(axisEndpoint);
            populateBinding(axisBinding,
                    wsdl4jBinding,
                    bindingWSDL,
                    portType,
                    portTypeWSDL,
                    isSetMessageQNames);
            processedBindings.put(wsdl4jBinding.getQName(), axisBinding);
        }
    }

    private void processEmbeddedEPR(List extensibilityElements, AxisEndpoint axisEndpoint) {
    	Iterator eelts = extensibilityElements.iterator();
    	while(eelts.hasNext()){
    		ExtensibilityElement ee = (ExtensibilityElement)eelts.next();
    		if(AddressingConstants.Final.WSA_ENDPOINT_REFERENCE.equals(ee.getElementType())){
    			try {
    				Element elt = ((UnknownExtensibilityElement)ee).getElement();
    				OMElement eprOMElement = XMLUtils.toOM(elt);
    				EndpointReference epr = EndpointReferenceHelper.fromOM(eprOMElement);
    				Map referenceParameters = epr.getAllReferenceParameters();
    				if(referenceParameters != null){
    					axisEndpoint.addParameter(AddressingConstants.REFERENCE_PARAMETER_PARAMETER, new ArrayList(referenceParameters.values()));
    				}
    			} catch (Exception e) {
    				if(log.isDebugEnabled()){
    					log.debug("Exception encountered processing embedded wsa:EndpointReference", e);
    				}
    			}
    		}
    	}
    }

	private void populatePortType(PortType wsdl4jPortType,
                                  Definition portTypeWSDL) throws AxisFault {
		copyExtensionAttributes(wsdl4jPortType.getExtensionAttributes(),
				axisService, PORT_TYPE);
        List wsdl4jOperations = wsdl4jPortType.getOperations();

        // Added to use in ?wsdl2 as the interface name
        axisService.addParameter(new Parameter(WSDL2Constants.INTERFACE_LOCAL_NAME,
                                               wsdl4jPortType.getQName().getLocalPart()));
        if (wsdl4jOperations.size() < 1) {
            throw new AxisFault("No operation found in the portType element");
        }

        AxisOperation axisOperation;
        List operationNames = new ArrayList();

        QName opName;
        Operation wsdl4jOperation;

        for (Iterator iterator = wsdl4jOperations.iterator(); iterator.hasNext();) {
            wsdl4jOperation = (Operation) iterator.next();

            axisOperation = populateOperations(wsdl4jOperation, wsdl4jPortType, portTypeWSDL);
            addDocumentation(axisOperation, wsdl4jOperation.getDocumentationElement());
            if (wsdl4jOperation.getInput() != null) {
                addMessageDocumentation(axisOperation, wsdl4jOperation.getInput().getDocumentationElement(), WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            }
            if (wsdl4jOperation.getOutput() != null) {
                addMessageDocumentation(axisOperation, wsdl4jOperation.getOutput().getDocumentationElement(), WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            }
            axisOperation.setParent(axisService);
            axisService.addChild(axisOperation);
            operationNames.add(axisOperation.getName());
        }

        // this is used only in codegen to preserve operation order
        if (isCodegen) {
            axisService.setOperationsNameList(operationNames);
        }

    }

    /**
     * This method is used for adding documentation for the message types of the service operations
     * eg: input message
     *     output message
     *     fault messages 
     *
     * @param axisOperation
     * @param documentationElement
     * @param messageLabel
     */
    private void addMessageDocumentation(AxisOperation axisOperation, Element documentationElement, String messageLabel) {
        if ((documentationElement != null) && (documentationElement.getFirstChild() != null)) {
            Node firstChild = documentationElement.getFirstChild();
            String documentation = DOM2Writer.nodeToString(firstChild);

            if (!"".equals(documentation)) {
                (axisOperation.getMessage(messageLabel)).setDocumentation(documentation);
            }
        }
    }
    
    private void populateBinding(AxisBinding axisBinding,
                                 Binding wsdl4jBinding,
                                 Definition bindingWSDL,
                                 PortType portType,
                                 Definition portTypeWSDL,
                                 boolean isSetMessageQNames)
            throws AxisFault {

        copyExtensibleElements(wsdl4jBinding.getExtensibilityElements(), bindingWSDL,
                               axisBinding, BINDING);

        List wsdl4jBidingOperations = wsdl4jBinding.getBindingOperations();

        if (wsdl4jBidingOperations.size() < 1) {
            throw new AxisFault("No operation found for the binding");
        }

        addDocumentation(axisBinding, wsdl4jBinding.getDocumentationElement());

        AxisOperation axisOperation;
        Operation wsdl4jOperation;

        AxisBindingOperation axisBindingOperation;
        BindingOperation wsdl4jBindingOperation;

        Map httpLocationMap = createHttpLocationTable();
        String httpLocation = null;

        for (Iterator iterator = wsdl4jBidingOperations.iterator(); iterator.hasNext();) {

            axisBindingOperation = new AxisBindingOperation();
            wsdl4jBindingOperation = (BindingOperation) iterator.next();
            wsdl4jOperation = findOperation(portType, wsdl4jBindingOperation);

            axisBindingOperation.setName(new QName(bindingWSDL.getTargetNamespace(), wsdl4jBindingOperation.getName()));
            addDocumentation(axisBindingOperation, wsdl4jBindingOperation.getDocumentationElement());

            axisOperation = axisService.getOperation(new QName(portTypeWSDL.getTargetNamespace(), wsdl4jOperation.getName()));
            axisBindingOperation.setAxisOperation(axisOperation);

            // process ExtensibilityElements of the wsdl4jBinding
            copyExtensibleElements(wsdl4jBindingOperation.getExtensibilityElements(),
                                   wsdl4jDefinition, axisBindingOperation, BINDING_OPERATION);

            httpLocation =
                    (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
            String httpMethod =
                    (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD);
            if (httpMethod == null || "".equals(httpMethod)) {
                httpMethod = HTTPConstants.HEADER_POST;
            }
            if (httpLocation != null) {
                httpLocationMap.put(WSDLUtil.getConstantFromHTTPLocation(httpLocation, httpMethod),
                                    axisBindingOperation.getAxisOperation());
            }

            BindingInput wsdl4jBindingInput = wsdl4jBindingOperation.getBindingInput();

            if (isServerSide) {
                if (wsdl4jBindingInput != null &&
                        WSDLUtil.isInputPresentForMEP(axisOperation.getMessageExchangePattern())) {
                    AxisBindingMessage axisBindingInMessage = new AxisBindingMessage();
                    axisBindingInMessage.setParent(axisBindingOperation);
                    addDocumentation(axisBindingInMessage, wsdl4jBindingInput.getDocumentationElement());
                    copyExtensibleElements(wsdl4jBindingInput.getExtensibilityElements(),
                            wsdl4jDefinition,
                            axisBindingInMessage, BINDING_OPERATION_INPUT);

                    AxisMessage axisInMessage =
                            axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    //This is a hack to get AXIS2-2771 working , I had to copy soap headers
                    //  from binding message to AxisMessage
                    List soapHeaders = (List) axisBindingInMessage.getProperty(
                            WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders != null) {
                        for (int i = 0; i < soapHeaders.size(); i++) {
                            SOAPHeaderMessage headerMessage = (SOAPHeaderMessage) soapHeaders.get(i);
                            axisInMessage.addSoapHeader(headerMessage);
                        }
                    }

                    if (isSetMessageQNames) {
                        BindingOperationEntry boe = find(wrappableBOEs, wsdl4jBindingOperation);
                        boolean isWrapped = (boe == null) ? false : boe.isWrappedInput();
                        addQNameReference(axisInMessage, wsdl4jOperation,
                                wsdl4jBindingInput,
                                isWrapped);
                    }

                    axisBindingInMessage.setAxisMessage(axisInMessage);
                    axisBindingInMessage.setName(axisInMessage.getName());
                    axisBindingInMessage.setDirection(axisInMessage.getDirection());

                    axisBindingOperation
                            .addChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE, axisBindingInMessage);
                }
            } else {
                if (wsdl4jBindingInput != null &&
                        WSDLUtil.isOutputPresentForMEP(axisOperation.getMessageExchangePattern())) {
                    AxisBindingMessage axisBindingOutMessage = new AxisBindingMessage();
                    axisBindingOutMessage.setParent(axisBindingOperation);
                    addDocumentation(axisBindingOutMessage, wsdl4jBindingInput.getDocumentationElement());
                    copyExtensibleElements(wsdl4jBindingInput.getExtensibilityElements(),
                            wsdl4jDefinition,
                            axisBindingOutMessage, BINDING_OPERATION_OUTPUT);

                    AxisMessage axisOutMessage =
                            axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    //This is a hack to get AXIS2-2771 working , I had to copy soap headers
                    //  from binding message to AxisMessage
                    List soapHeaders = (List) axisBindingOutMessage.getProperty(
                            WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders != null) {
                        for (int i = 0; i < soapHeaders.size(); i++) {
                            SOAPHeaderMessage headerMessage = (SOAPHeaderMessage) soapHeaders.get(i);
                            axisOutMessage.addSoapHeader(headerMessage);
                        }
                    }

                    if (isSetMessageQNames) {
                        BindingOperationEntry boe = find(wrappableBOEs, wsdl4jBindingOperation);
                        boolean isWrapped = (boe == null) ? false : boe.isWrappedInput();
                        addQNameReference(axisOutMessage, wsdl4jOperation,
                                wsdl4jBindingInput,
                                isWrapped);
                    }

                    axisBindingOutMessage.setAxisMessage(axisOutMessage);
                    axisBindingOutMessage.setName(axisOutMessage.getName());
                    axisBindingOutMessage.setDirection(axisOutMessage.getDirection());

                    axisBindingOperation
                            .addChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE, axisBindingOutMessage);
                }
            }

            BindingOutput wsdl4jBindingOutput = wsdl4jBindingOperation.getBindingOutput();

            if (isServerSide) {
                if (wsdl4jBindingOutput != null &&
                        WSDLUtil.isOutputPresentForMEP(axisOperation.getMessageExchangePattern())) {

                    AxisBindingMessage axisBindingOutMessage = new AxisBindingMessage();
                    axisBindingOutMessage.setParent(axisBindingOperation);
                    addDocumentation(axisBindingOutMessage, wsdl4jBindingOutput.getDocumentationElement());
                    AxisMessage axisOutMessage =
                            axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

                    copyExtensibleElements(wsdl4jBindingOutput.getExtensibilityElements(),
                            wsdl4jDefinition,
                            axisBindingOutMessage, BINDING_OPERATION_OUTPUT);

                    //This is a hack to get AXIS2-2771 working , I had to copy soap headers
                    //  from binding message to AxisMessage
                    List soapHeaders =
                            (List) axisBindingOutMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders != null) {
                        for (int i = 0; i < soapHeaders.size(); i++) {
                            SOAPHeaderMessage headerMessage = (SOAPHeaderMessage) soapHeaders.get(i);
                            axisOutMessage.addSoapHeader(headerMessage);
                        }
                    }

                    if (isSetMessageQNames) {
                        BindingOperationEntry boe = find(wrappableBOEs, wsdl4jBindingOperation);
                        boolean isWrapped = (boe == null) ? false : boe.isWrappedOutput();
                        axisOutMessage.setWrapped(isWrapped);
                        addQNameReference(axisOutMessage, wsdl4jOperation,
                                wsdl4jBindingOutput,
                                isWrapped);
                    }


                    axisBindingOutMessage.setAxisMessage(axisOutMessage);
                    axisBindingOutMessage.setName(axisOutMessage.getName());
                    axisBindingOutMessage.setDirection(axisOutMessage.getDirection());

                    axisBindingOperation
                            .addChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE, axisBindingOutMessage);
                }
            } else {
                if (wsdl4jBindingOutput != null &&
                        WSDLUtil.isInputPresentForMEP(axisOperation.getMessageExchangePattern())) {

                    AxisBindingMessage axisBindingInMessage = new AxisBindingMessage();
                    axisBindingInMessage.setParent(axisBindingOperation);
                    addDocumentation(axisBindingInMessage, wsdl4jBindingOutput.getDocumentationElement());
                    AxisMessage axisInMessage =
                            axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                    copyExtensibleElements(wsdl4jBindingOutput.getExtensibilityElements(),
                            wsdl4jDefinition,
                            axisBindingInMessage, BINDING_OPERATION_INPUT);

                    //This is a hack to get AXIS2-2771 working , I had to copy soap headers
                    //  from binding message to AxisMessage
                    List soapHeaders =
                            (List) axisBindingInMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders != null) {
                        for (int i = 0; i < soapHeaders.size(); i++) {
                            SOAPHeaderMessage headerMessage = (SOAPHeaderMessage) soapHeaders.get(i);
                            axisInMessage.addSoapHeader(headerMessage);
                        }
                    }

                    if (isSetMessageQNames) {
                        BindingOperationEntry boe = find(wrappableBOEs, wsdl4jBindingOperation);
                        boolean isWrapped = (boe == null) ? false : boe.isWrappedOutput();
                        axisInMessage.setWrapped(isWrapped);
                        addQNameReference(axisInMessage, wsdl4jOperation,
                                wsdl4jBindingOutput,
                                isWrapped);
                    }


                    axisBindingInMessage.setAxisMessage(axisInMessage);
                    axisBindingInMessage.setName(axisInMessage.getName());
                    axisBindingInMessage.setDirection(axisInMessage.getDirection());

                    axisBindingOperation
                            .addChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE, axisBindingInMessage);
                }
            }

            Map bindingFaultsMap = wsdl4jBindingOperation.getBindingFaults();

            /* process the binding faults */
            for (Iterator bindingFaults = bindingFaultsMap.values().iterator();
                 bindingFaults.hasNext();) {

                BindingFault bindingFault = (BindingFault) bindingFaults.next();
                if (bindingFault.getName() == null) {
                    throw new AxisFault("Binding name is null for the binding fault in " +
                            " binding operation ==> " + wsdl4jBindingOperation.getName());
                } else {
                    Fault wsdl4jFault = wsdl4jOperation.getFault(bindingFault.getName());
                    if (wsdl4jFault == null){
                        throw new AxisFault("Can not find the corresponding fault element in " +
                                "wsdl operation " + wsdl4jOperation.getName() + " for the fault" +
                                " name " + bindingFault.getName());
                    } else {
                        Message wsdl4jFaultMessge = wsdl4jFault.getMessage();

                        AxisMessage faultMessage = findFaultMessage(
                                wsdl4jFault.getName(),
                                axisOperation.getFaultMessages());

                        AxisBindingMessage axisBindingFaultMessage = new AxisBindingMessage();
                        addDocumentation(axisBindingFaultMessage, wsdl4jFaultMessge.getDocumentationElement());
                        axisBindingFaultMessage.setFault(true);
                        axisBindingFaultMessage.setAxisMessage(faultMessage);
                        axisBindingFaultMessage.setName(faultMessage.getName());
                        axisBindingFaultMessage.setParent(axisBindingOperation);

                        axisBindingOperation.addFault(axisBindingFaultMessage);
                        if (isSetMessageQNames) {
                            addQNameReference(faultMessage, wsdl4jFault.getMessage());
                        }
                    }
                }
            }

            axisBindingOperation.setParent(axisBinding);
            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);
        }
        axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationMap);
    }

    /**
     * contains all code which gathers non-service specific information from the
     * wsdl. <p/> After all the setup completes successfully, the setupComplete
     * field is set so that any subsequent calls to setup() will result in a
     * no-op. Note that subclass WSDL11ToAllAxisServicesBuilder will call
     * populateService for each port in the WSDL. Separating the non-service
     * specific information here allows WSDL11ToAllAxisServicesBuilder to only
     * do this work 1 time per WSDL, instead of for each port on each service.
     *
     * @throws WSDLException if readInTheWSDLFile fails
     */
    protected void setup() throws WSDLException {
        if (setupComplete) { // already setup, just do nothing and return
            return;
        }
        if (wsdl4jDefinition == null) {
            wsdl4jDefinition = readInTheWSDLFile(in);
        }
        if (wsdl4jDefinition == null) {
            return; // can't continue without wsdl
        }


        // setup the schemaMap
        this.schemaMap = new HashMap();
        populateSchemaMap(wsdl4jDefinition, new HashSet());

        setPolicyRegistryFromService(axisService);

        setupComplete = true; // if any part of setup fails, don't mark
        // setupComplete
    }


    /**
     * Populate a map of targetNamespace vs DOM schema element This is used to
     * grab the correct schema element when adding a new element
     *
     * @param definition
     */

    private void populateSchemaMap(Definition definition, Set visitedWSDLs) {
        visitedWSDLs.add(definition.getDocumentBaseURI());
        // first process the types in the given wsdl
        Types types = definition.getTypes();
        Object extensibilityElement;
        if (types != null) {
            for (Iterator iterator = types.getExtensibilityElements().iterator(); iterator.hasNext();)
            {
                extensibilityElement = iterator.next();
                if (extensibilityElement instanceof Schema) {
                    Element schemaElement = ((Schema) extensibilityElement).getElement();
                    schemaMap.put(schemaElement.getAttribute(XSD_TARGETNAMESPACE), schemaElement);
                }
            }
        }

        // popualte the imports as well
        Iterator iter = definition.getImports().values().iterator();
        Vector values = null;
        Import wsdlImport = null;
        for (; iter.hasNext();) {
            values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                wsdlImport = (Import) valuesIter.next();
                Definition innerDefinition = wsdlImport.getDefinition();
                if(!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())) {
                    populateSchemaMap(innerDefinition, visitedWSDLs);
                }
            }
        }
    }


    /**
     * return the service to process
     * if user has specified we check whether it exists
     * else pick a random service and throws an exception if not found any thing
     *
     * @param definition
     * @return service to process
     * @throws AxisFault
     */

    private Service findService(Definition definition) throws AxisFault {
        Map services = definition.getServices();
        Service service = null;
        if (serviceName != null) {
            // i.e if a user has specified a pirticular port
            service = (Service) services.get(serviceName);
            if (service == null) {
                throw new AxisFault("Service " + serviceName
                                    + " was not found in the WSDL");
            }
        } else {
            if (services.size() > 0) {
                for (Iterator iter = services.values().iterator(); iter.hasNext();) {
                    service = (Service) iter.next();
                    if (service.getPorts().size() > 0) {
                        //i.e we have found a service with ports
                        break;
                    }
                }
                if ((service == null) || (service.getPorts().size() == 0)) {
                    throw new AxisFault("there is no service with ports to pick");
                }

            } else {
                throw new AxisFault("No services found in the WSDL at " +
                                    definition.getDocumentBaseURI()
                                    + " with targetnamespace "
                                    + definition.getTargetNamespace());
            }
        }
        return service;
    }

    /**
     * Look for the relevant binding!
     * if user has spcifed a port get it
     * otherwise find first soap port or pick random one if there is no soap port
     *
     * @param dif
     * @param service service can not be null
     * @throws AxisFault
     */
    private Binding findBinding(Definition dif, Service service) throws AxisFault {

        Binding binding = null;
        Port port = null;
        copyExtensibleElements(service.getExtensibilityElements(), dif, axisService, SERVICE);
        if (portName != null) {
            // i.e if user has specified a service
            port = service.getPort(portName);
            if (port == null) {
                throw new AxisFault("No port found for the given name :" + portName);
            }
        } else {
            Map ports = service.getPorts();
            if (ports != null && ports.size() > 0) {
                // pick the port with the SOAP address as the default port
                port = findPort(ports);
                if (port == null) {
                    // a SOAP port was not found - log a warning
                    // and use the first port in the list
                    log.info("A SOAP port was not found - "
                             + "picking a random port!");
                    port = (Port) ports.values().toArray()[0];
                }

                if (port != null) {
                    // i.e we have find a correct port
                    // this is only use full in codegen time.
                    if (this.isCodegen && !this.isAllPorts) {
                        // if user has not set all option
                        // we have to generate code only for that option.
                        this.portName = port.getName();
                    }
                }
            }
        }

        axisService.setName(service.getQName().getLocalPart());

        if (port != null) {
            copyExtensibleElements(port.getExtensibilityElements(), dif,
                                   axisService, PORT);
            Definition parentDefinition = getParentDefinition(dif,
                    port.getBinding().getQName(), COMPONENT_BINDING, new HashSet());
            binding = parentDefinition.getBinding(port.getBinding().getQName());
            if (binding == null) {
                binding = port.getBinding();
            }
        }

        return binding;
    }

    /**
     * Finds a SOAP port given the port map
     */
    private Port findPort(Map ports) {
        Port port;
        Port returnPort = null;
        for (Iterator portsIterator = ports.values().iterator(); portsIterator.hasNext();) {
            port = (Port) portsIterator.next();
            List extensibilityElements = port.getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++) {
                Object extElement = extensibilityElements.get(i);
                if (extElement instanceof SOAP12Address) {
                    // SOAP 1.2 address found - keep this and loop until http address is found
                    returnPort = port;
                    String location = ((SOAP12Address)extElement).getLocationURI().trim();
                    if ((location != null) && location.startsWith("http:")){
                        // i.e we have found an http port so return from here
                       break;
                    }
                }
            }
        }

        if (returnPort != null){
            return returnPort;
        }

        for (Iterator portsIterator = ports.values().iterator(); portsIterator
                .hasNext();) {
            port = (Port) portsIterator.next();
            List extensibilityElements = port.getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++) {
                Object extElement = extensibilityElements.get(i);
                if (extElement instanceof SOAPAddress) {
                    // SOAP 1.1 address found - keep this and loop until http address is found
                    returnPort = port;
                    String location = ((SOAPAddress)extElement).getLocationURI().trim();
                    if ((location != null) && location.startsWith("http:")){
                        // i.e we have found an http port so return from here
                       break;
                    }
                }
            }
        }

        if (returnPort != null){
            return returnPort;
        }

        for (Iterator portsIterator = ports.values().iterator(); portsIterator
                .hasNext();) {
            port = (Port) portsIterator.next();
            List extensibilityElements = port.getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++) {
                Object extElement = extensibilityElements.get(i);
                if (extElement instanceof HTTPAddress) {
                    // HTTP address found - keep this and loop until http address is found
                    returnPort = port;
                    String location = ((HTTPAddress)extElement).getLocationURI().trim();
                    if ((location != null) && location.startsWith("http:")){
                        // i.e we have found an http port so return from here
                       break;
                    }
                }
            }
        }
        return returnPort;
    }

    private Operation findOperation(PortType portType,
                                    BindingOperation wsdl4jBindingOperation) {
        Operation op = wsdl4jBindingOperation.getOperation();
        String input = null;
        if (op != null && op.getInput() != null) {
            input = op.getInput().getName();
            if (":none".equals(input)) {
                input = null;
            }
        }
        String output = null;
        if (op != null && op.getOutput() != null) {
            output = op.getOutput().getName();
            if (":none".equals(output)) {
                output = null;
            }
        }
        Operation op2 = portType.getOperation(op.getName(), input, output);
        return ((op2 == null) ? op : op2);
    }

    /**
     * Find the fault message relevant to a given name from the fault message
     * list
     *
     * @param name
     * @param faultMessages
     */
    private AxisMessage findFaultMessage(String name, ArrayList faultMessages) {
        AxisMessage tempMessage;
        for (int i = 0; i < faultMessages.size(); i++) {
            tempMessage = (AxisMessage) faultMessages.get(i);
            if (name.equals(tempMessage.getName())) {
                return tempMessage;
            }

        }
        return null;
    }

    /**
     * Add the QName for the binding input
     *
     * @param inMessage
     * @param wsdl4jOperation
     * @param bindingInput
     * @param isWrapped       - basically whether the operation is soap/rpc or not
     */
    private void addQNameReference(AxisMessage inMessage,
                                   Operation wsdl4jOperation, BindingInput bindingInput,
                                   boolean isWrapped) {

        List extensibilityElements = bindingInput.getExtensibilityElements();
        Message wsdl4jMessage = wsdl4jOperation.getInput().getMessage();

        addQNameReference(inMessage,
                          wsdl4jOperation,
                          isWrapped,
                          extensibilityElements,
                          wsdl4jMessage,
                          wsdl4jOperation.getName());
    }

    /**
     * Add the QName for the binding output
     *
     * @param outMessage
     * @param wsdl4jOperation
     * @param isWrapped
     */
    private void addQNameReference(AxisMessage outMessage,
                                   Operation wsdl4jOperation, BindingOutput bindingOutput,
                                   boolean isWrapped) {

        if (bindingOutput != null) {
            List extensibilityElements = bindingOutput.getExtensibilityElements();
            if (wsdl4jOperation.getOutput() == null) {
                return;
            }
            Message wsdl4jMessage = wsdl4jOperation.getOutput().getMessage();

            addQNameReference(outMessage,
                              wsdl4jOperation,
                              isWrapped,
                              extensibilityElements,
                              wsdl4jMessage,
                              wsdl4jOperation.getName() + WRAPPED_OUTPUTNAME_SUFFIX);
        }
    }

    private void addQNameReference(AxisMessage message,
                                   Operation wsdl4jOperation,
                                   boolean isWrapped,
                                   List extensibilityElements,
                                   Message wsdl4jMessage,
                                   String rpcOperationName) {
        if (isWrapped) {
            // we have already validated and process the qname references
            // so set it here
            // The schema for this should be already made ! Find the
            // QName from
            // the list and add it - the name for this is just the
            message.setElementQName((QName) resolvedRpcWrappedElementMap
                    .get(rpcOperationName));
            message.getAxisOperation().getAxisService().addMessageElementQNameToOperationMapping(
                    (QName) resolvedRpcWrappedElementMap.get(rpcOperationName),
                    message.getAxisOperation());
        } else {
            // now we are sure this is an document literal type element
            List bindingPartsList = getPartsListFromSoapBody(extensibilityElements);
            if (bindingPartsList == null) {
                // i.e user has not given any part list so we go to message and pick the firest part if
                // available
                if ((wsdl4jMessage.getParts() != null) && (wsdl4jMessage.getParts().size() > 0)) {
                    if (wsdl4jMessage.getParts().size() == 1) {
                        Part part = (Part) wsdl4jMessage.getParts().values().iterator().next();
                        QName elementName = part.getElementName();
                        if (elementName != null) {
                            message.setElementQName(elementName);
                            message.setMessagePartName(part.getName());
                            AxisOperation operation = message.getAxisOperation();
                            AxisService service = operation.getAxisService();
                            service.addMessageElementQNameToOperationMapping(elementName,
                                                                             operation);
                        } else {
                            throw new WSDLProcessingException(
                                    "No element type is defined for message " +
                                    wsdl4jMessage.getQName().getLocalPart());
                        }
                    } else {
                        // user has specified more than one parts with out specifing a part in
                        // soap body
                        throw new WSDLProcessingException("More than one part for message " +
                                                          wsdl4jMessage.getQName().getLocalPart());
                    }
                } else {
                    // This is allowed in the spec in this case element qname is null and nothing is send
                    // in the soap body.  This is true for Doc/Lit/Bare operations that do not have any paramaters.
                    // In this case, add a mapping of the operation to a null key so the empty body can be routed on
                    // (for example in SOAPMessageBodyBasedDispatcher).
                    message.setElementQName(null);
                    AxisOperation operation = message.getAxisOperation();
                    AxisService service = operation.getAxisService();
                    service.addMessageElementQNameToOperationMapping(null, operation);
                }
            } else {
                if (bindingPartsList.size() == 0) {
                    // we donot have to set the element qname
                    message.setElementQName(null);
                } else if (bindingPartsList.size() == 1) {
                    Part part = wsdl4jMessage.getPart((String) bindingPartsList.get(0));
                    if (part != null) {
                        QName elementName = part.getElementName();
                        if (elementName != null) {
                            message.setElementQName(elementName);
                            message.setMessagePartName(part.getName());
                            AxisOperation operation = message.getAxisOperation();
                            AxisService service = operation.getAxisService();
                            service.addMessageElementQNameToOperationMapping(elementName,
                                                                             operation);
                        } else {
                            throw new WSDLProcessingException(
                                    "No element type is defined for message" +
                                    wsdl4jMessage.getQName().getLocalPart());
                        }
                    } else {
                        throw new WSDLProcessingException("Missing part named "
                                                          + bindingPartsList.get(0) + " ");
                    }

                } else {
                    // i.e more than one part specified in this case we have
                    // to send an exception
                    throw new WSDLProcessingException(
                            "More than one element part is not allwed in document literal " +
                            " type binding operation " + wsdl4jOperation.getName());
                }
            }

        }

    }

    /**
     * Add the QName for the binding output
     */
    private void addQNameReference(AxisMessage faultMessage,
                                   Message wsdl4jMessage) throws AxisFault {

        // for a fault this is trivial - All faults are related directly to a
        // message by the name and are supposed to have a single part. So it is
        // a matter of copying the right QName from the message part

        // get the part
        Map parts = wsdl4jMessage.getParts();
        if (parts == null || parts.size() == 0) {
            String message = "There are no parts"
                             + " for fault message : "
                             + wsdl4jMessage.getQName();
            log.error(message);
            throw new WSDLProcessingException(message);
        }
        Part wsdl4jMessagePart = (Part) parts.values()
                .toArray()[0];
        if (wsdl4jMessagePart == null) {
            throw new WSDLProcessingException();
        }
        QName name = wsdl4jMessagePart.getElementName();
        if (name == null) {
            String message = "Part '"
                             + wsdl4jMessagePart.getName()
                             + "' of fault message '"
                             + wsdl4jMessage.getQName()
                             + "' must be defined with 'element=QName' and not 'type=QName'";
            log.error(message);
            throw new AxisFault(message);
        }

        faultMessage.setMessagePartName(wsdl4jMessagePart.getName());
        faultMessage.setElementQName(name);
    }

    /**
     * A util method that returns the SOAP style included in the binding
     * operation
     *
     * @param bindingOp
     */
    private String getSOAPStyle(BindingOperation bindingOp) {
        List extensibilityElements = bindingOp.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object extElement = extensibilityElements.get(i);
            if (extElement instanceof SOAPOperation) {
                return ((SOAPOperation) extElement).getStyle();
            } else if (extElement instanceof SOAP12Operation) {
                return ((SOAP12Operation) extElement).getStyle();
            }

        }
        return null;
    }

    /**
     * Copy the component from the operation
     *
     * @param wsdl4jOperation
     * @param dif
     * @throws AxisFault
     */
    private AxisOperation populateOperations(Operation wsdl4jOperation,
                                             PortType wsdl4jPortType, Definition dif)
            throws AxisFault {
        QName opName = new QName(dif.getTargetNamespace(), wsdl4jOperation.getName());
        // Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);
        if (axisOperation == null) {
            String MEP = getMEP(wsdl4jOperation);
            axisOperation = AxisOperationFactory.getOperationDescription(MEP);
            axisOperation.setName(opName);

            // setting the PolicyInclude property of the AxisOperation
            PolicyInclude policyInclude = new PolicyInclude(axisOperation);
            axisOperation.setPolicyInclude(policyInclude);
        }

        copyExtensionAttributes(wsdl4jOperation.getExtensionAttributes(), 
                               axisOperation, PORT_TYPE_OPERATION);

        Input wsdl4jInputMessage = wsdl4jOperation.getInput();

        if (isServerSide) {
            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {
                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensionAttributes(wsdl4jInputMessage.getExtensionAttributes(),
                                           inMessage, PORT_TYPE_OPERATION_INPUT);

                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                ArrayList inputActions = axisOperation.getWSAMappingList();
                String action = null;
                if (inputActions == null || inputActions.size() == 0) {
                    action = WSDL11ActionHelper
                            .getActionFromInputElement(dif, wsdl4jPortType,
                                                       wsdl4jOperation, wsdl4jInputMessage);
                }
                if (action != null) {
                    if (inputActions == null) {
                        inputActions = new ArrayList();
                        axisOperation.setWsamappingList(inputActions);
                    }
                    inputActions.add(action);
                    axisService.mapActionToOperation(action, axisOperation);
                }
            }
            // Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {

                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensionAttributes(wsdl4jOutputMessage.getExtensionAttributes(),
                                           outMessage, PORT_TYPE_OPERATION_OUTPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                String action = axisOperation.getOutputAction();
                if (action == null) {
                    action = WSDL11ActionHelper.getActionFromOutputElement(dif,
                                                                           wsdl4jPortType,
                                                                           wsdl4jOperation,
                                                                           wsdl4jOutputMessage);
                }
                if (action != null) {
                    axisOperation.setOutputAction(action);
                }
            }
        } else {

            // for the client side we have to do something that is a bit
            // weird. The in message is actually taken from the output
            // and the output is taken from the in

            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {

                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensionAttributes(wsdl4jInputMessage.getExtensionAttributes(),
                                           inMessage, PORT_TYPE_OPERATION_OUTPUT);

                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                String action = axisOperation.getOutputAction();
                if (action == null) {
                    action = WSDL11ActionHelper
                            .getActionFromInputElement(dif, wsdl4jPortType,
                                                       wsdl4jOperation, wsdl4jInputMessage);
                }
                if (action != null) {
                    axisOperation.setOutputAction(action);
                }
            }
            // Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {

                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensionAttributes(wsdl4jOutputMessage.getExtensionAttributes(),
                                           outMessage, PORT_TYPE_OPERATION_INPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                ArrayList inputActions = axisOperation.getWSAMappingList();
                String action = null;
                if (inputActions == null || inputActions.size() == 0) {
                    action = WSDL11ActionHelper.getActionFromOutputElement(dif,
                                                                           wsdl4jPortType,
                                                                           wsdl4jOperation,
                                                                           wsdl4jOutputMessage);
                }
                if (action != null) {
                    if (inputActions == null) {
                        inputActions = new ArrayList();
                        axisOperation.setWsamappingList(inputActions);
                    }
                    inputActions.add(action);
                }
            }
        }

        Map faults = wsdl4jOperation.getFaults();
        Iterator faultKeyIterator = faults.keySet().iterator();

        while (faultKeyIterator.hasNext()) {
            Fault fault = (Fault) faults.get(faultKeyIterator.next());
            AxisMessage axisFaultMessage = new AxisMessage();
            addDocumentation(axisFaultMessage,fault.getDocumentationElement());
            Message faultMessage = fault.getMessage();
            if (null != faultMessage) {
                axisFaultMessage
                        .setName(fault.getName());

                copyExtensibleElements(faultMessage.getExtensibilityElements(),
                                       dif, axisFaultMessage, PORT_TYPE_OPERATION_FAULT);

            }

            // Check if the action is already set as we don't want to override
            // it
            // with the Default Action Pattern
            String action = axisOperation.getFaultAction(fault.getName());
            if (action == null) {
                action = WSDL11ActionHelper.getActionFromFaultElement(dif,
                                                                      wsdl4jPortType,
                                                                      wsdl4jOperation, fault);
            }
            if (action != null) {
                axisOperation.addFaultAction(fault.getName(), action);
            }
            
            // Also add mapping from Exception name to fault action
            String faultMessageName = axisFaultMessage.getName();
            if (null != faultMessageName) {
                char firstChar = faultMessageName.charAt(0);
                String upperChar = String.valueOf(firstChar).toUpperCase();
                String nameWithoutFirstChar = faultMessageName.substring(1);
                String exceptionClassName = upperChar.concat(nameWithoutFirstChar);
                if (log.isDebugEnabled()) {
                    log.debug("Searching for fault action using faultMessageName = "+faultMessageName+", exceptionClassName = "+exceptionClassName);
                }
            
                String faultAction = axisOperation.getFaultAction(exceptionClassName);
                if (faultAction == null) {
                    faultAction = WSDL11ActionHelper.getActionFromFaultElement(dif,
                                                                          wsdl4jPortType,
                                                                          wsdl4jOperation, fault);
                    if (log.isDebugEnabled()) {
                        log.debug("Fault action didn't previously exist, getting it from WSDL: "+faultAction);
                    }
                }
                if (faultAction != null) {
                    axisOperation.addFaultAction(exceptionClassName, faultAction);
                    axisOperation.addFaultAction(exceptionClassName+"_Exception", faultAction);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding fault action entry: "+exceptionClassName+"="+faultAction);
                        log.debug("Adding fault action entry: "+exceptionClassName+"_Exception"+"="+faultAction);
                    }
                }
            }
            
            axisOperation.setFaultMessages(axisFaultMessage);
        }
        return axisOperation;
    }

    /**
     * Generates a list of wrapper schemas
     *
     * @param wsdl4jBinding
     */
    private Element[] generateWrapperSchema(Map schemaMap,
                                            Binding wsdl4jBinding,
                                            PortType portType) {

        List schemaElementList = new ArrayList();
        // target namespace for this should be the namespace URI for
        // the porttype
        String porttypeNamespaceURI = portType.getQName().getNamespaceURI();

        // //////////////////////////////////////////////////////////////////////
        // if there are any bindings present then we have to process them. we
        // have to generate a schema per wsdl4jBinding (that is the safest
        // option).
        // if not we just resolve to
        // the good old port type
        // list, in which case we'll generate a schema per porttype
        // //////////////////////////////////////////////////////////////////////

        // findwrappable operations return either the rpc soap operations or
        // Http binding operations

        List wrappableBOEList = findWrappableBindingOperations(wsdl4jBinding);

        // this method returns all the new schemas created when processing the rpc messages
        Map newSchemaMap = createSchemaForPorttype(porttypeNamespaceURI,
                                                   wrappableBOEList,
                                                   schemaMap);

        schemaElementList.addAll(newSchemaMap.values());
        return (Element[]) schemaElementList
                .toArray(new Element[schemaElementList.size()]);
    }

    /**
     * Create a schema by looking at the port type
     *
     * @param namespaceURI - namespace of the porttype uri.
     *  we use this only if a user has not specified a namespace in soap:body
     * @param boeListToProcess - List of BindingOperationEntry objects which require wrappering
     *                     
     * @return null if there is no element
     */
    private Map createSchemaForPorttype(String namespaceURI,
                                        List boeListToProcess,
                                        Map existingSchemaMap) {

        // this map is used to keep the newly added schemas
        Map newSchemaMap = new HashMap();
        // first of all look at the operations list
        // we can return immediately if we get the operations list
        // as empty
        if (boeListToProcess.isEmpty()) {
            return newSchemaMap;
        }

        // loop through the messages. We'll populate thins map with the relevant
        // messages
        // from the operations
        Map messageQnameToMessageMap = new HashMap();
        Map boeToInputMessageMap = new HashMap();
        Map boeToOutputMessageMap = new HashMap();

        // this contains the required namespace imports. the key in this
        // map would be the namaspace URI
        Map namespaceImportsMap = null;
        // list namespace prefix map. This map will include uri -> prefix
        Map namespacePrefixMap = null;

        // //////////////////////////////////////////////////////////////////////////////////////////////////
        // First thing is to populate the message map with the messages to
        // process.
        // //////////////////////////////////////////////////////////////////////////////////////////////////

        // we really need to do this for a single porttype!
        BindingOperationEntry boe;
        for (int k = 0; k < boeListToProcess.size(); k++) {
            boe = (BindingOperationEntry) boeListToProcess.get(k);
            Input input = boe.getBindingOperation().getOperation().getInput();
            Message message;
            if (input != null) {
                message = input.getMessage();
                messageQnameToMessageMap.put(message.getQName(), message);
                boeToInputMessageMap.put(boe, message);
            }

            Output output = boe.getBindingOperation().getOperation().getOutput();
            if (output != null) {
                message = output.getMessage();
                messageQnameToMessageMap.put(message.getQName(), message);
                boeToOutputMessageMap.put(boe, message);
            }

            // we do not want to process fault messages since they can only be used as document type
            // see basic profile 4.4.2
        }

        // find the xsd prefix
        String xsdPrefix = findSchemaPrefix();
        // DOM document that will be the ultimate creator
        Document document = getDOMDocumentBuilder().newDocument();

        Element elementDeclaration;

        //loop through the input op map and generate the elements
        BindingOperationEntry boEntry;
        for (Iterator boeIter = boeToInputMessageMap.keySet().iterator();
             boeIter.hasNext();) {

            boEntry = (BindingOperationEntry) boeIter.next();
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                             + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, boEntry.getBindingOperation().getName());

            //when creating the inner complex type we have to find the parts list from the binding input
            BindingInput bindingInput = boEntry.getBindingOperation().getBindingInput();
            Message message = (Message) boeToInputMessageMap.get(boEntry);

            if (bindingInput != null) {

                Collection partsCollection = null;
                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    // first see the body parts list
                    List bodyPartsList =
                        getPartsListFromSoapBody(bindingInput.getExtensibilityElements());
                    partsCollection = message.getOrderedParts(bodyPartsList);
                } else {
                    // i.e http binding
                    partsCollection = message.getParts().values();
                }

                List parameterOrder = 
                   boEntry.getBindingOperation().getOperation().getParameterOrdering();
                namespaceImportsMap = new HashMap();
                namespacePrefixMap = new HashMap();

                Node newComplexType = getNewComplextType(document,
                                                         xsdPrefix,
                                                         partsCollection,
                                                         parameterOrder,
                                                         false,
                                                         namespaceImportsMap,
                                                         namespacePrefixMap, 
                                                         boEntry);

                elementDeclaration.appendChild(newComplexType);
                String namespaceToUse = namespaceURI;

                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    String bodyNamespace =
                            getNamespaceFromSoapBody(bindingInput.getExtensibilityElements());
                    namespaceToUse = bodyNamespace != null ? bodyNamespace : namespaceURI;
                }

                if (existingSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is already exists with the original wsdl schemas
                    addElementToAnExistingSchema((Element) existingSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else if (newSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is with a newly created schema
                    addElementToAnExistingSchema((Element) newSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else {
                    // i.e this element namespace has not found yet so
                    // we have to create new schema for it
                    Element newSchema = createNewSchemaWithElement(elementDeclaration,
                                                                   namespacePrefixMap,
                                                                   namespaceImportsMap,
                                                                   namespaceToUse,
                                                                   document,
                                                                   xsdPrefix);
                    newSchemaMap.put(namespaceToUse, newSchema);
                }
                resolvedRpcWrappedElementMap.put(boEntry.getBindingOperation().getName(), new QName(
                        namespaceToUse, boEntry.getBindingOperation().getName(), AXIS2WRAPPED));

            } else {
                throw new WSDLProcessingException(
                        "No binding input is defiend for binding operation ==> "
                        + boEntry.getBindingOperation().getName());
            }

        }

        // loop through the output to map and generate the elements
        for (Iterator boeIterator = boeToOutputMessageMap.keySet().iterator();
             boeIterator.hasNext();) {
            boEntry = (BindingOperationEntry) boeIterator.next();
            String baseoutputOpName = boEntry.getBindingOperation().getName();
            // see basic profile 4.7.19
            String outputOpName = baseoutputOpName + WRAPPED_OUTPUTNAME_SUFFIX;
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                             + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, outputOpName);

            BindingOutput bindingOutput = boEntry.getBindingOperation().getBindingOutput();
            Message message = (Message) boeToOutputMessageMap.get(boEntry);

            if (bindingOutput != null) {
                Collection partsCollection = null;
                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    // first see the body parts list
                    List bodyPartsList =
                        getPartsListFromSoapBody(bindingOutput.getExtensibilityElements());
                    partsCollection = message.getOrderedParts(bodyPartsList);

                } else {
                    // i.e if http binding
                    partsCollection = message.getParts().values();
                }

                List parameterOrder = 
                    boEntry.getBindingOperation().getOperation().getParameterOrdering();

                // we have to initialize the hash maps always since we add the elements onece we
                // generate it
                namespacePrefixMap = new HashMap();
                namespaceImportsMap = new HashMap();

                Node newComplexType = getNewComplextType(document,
                                                         xsdPrefix,
                                                         partsCollection,
                                                         parameterOrder,
                                                         true,
                                                         namespaceImportsMap,
                                                         namespacePrefixMap,
                                                         boEntry);
                elementDeclaration.appendChild(newComplexType);

                String namespaceToUse = namespaceURI;

                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    String bodyNamespace =
                            getNamespaceFromSoapBody(bindingOutput.getExtensibilityElements());
                    namespaceToUse = bodyNamespace != null ? bodyNamespace : namespaceURI;
                }

                if (existingSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is already exists with the original wsdl schemas
                    addElementToAnExistingSchema((Element) existingSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else if (newSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is with a newly created schema
                    addElementToAnExistingSchema((Element) newSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else {
                    // i.e this element namespace has not found yet so
                    // we have to create new schema for it
                    Element newSchema = createNewSchemaWithElement(elementDeclaration,
                                                                   namespacePrefixMap,
                                                                   namespaceImportsMap,
                                                                   namespaceToUse,
                                                                   document,
                                                                   xsdPrefix);
                    newSchemaMap.put(namespaceToUse, newSchema);
                }
                resolvedRpcWrappedElementMap.put(outputOpName, new QName(
                        namespaceToUse, outputOpName, AXIS2WRAPPED));

            } else {
                throw new WSDLProcessingException(
                        "No binding out put is defined for binding operation ==>" +
                        boEntry.getBindingOperation().getName());
            }
        }

        return newSchemaMap;
    }

    private void addElementToAnExistingSchema(Element schemaElement,
                                              Element newElement,
                                              Map namespacePrefixMap,
                                              Map namespaceImportsMap,
                                              String targetNamespace) {

        Document ownerDocument = schemaElement.getOwnerDocument();

        String newElementName = newElement.getAttribute(XSD_NAME);

        // check whether this element already exists.
        NodeList nodeList = schemaElement.getChildNodes();
        Element nodeElement = null;
        for (int i = 1; i < nodeList.getLength(); i++) {
            if (nodeList.item(i) instanceof Element){
                nodeElement = (Element) nodeList.item(i);
                if (nodeElement.getLocalName().equals(XML_SCHEMA_ELEMENT_LOCAL_NAME)){
                    if (nodeElement.getAttribute(XSD_NAME).equals(newElementName)){
                        // if the element already exists we do not add this element
                        // and just return.
                        return;
                    }
                }
            }

        }

        // loop through the namespace declarations first and add them
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
                .keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            checkAndAddNamespaceDeclarations(s, namespacePrefixMap,
                                             schemaElement);
        }

        // add imports - check whether it is the targetnamespace before
        // adding
        Element[] namespaceImports = (Element[]) namespaceImportsMap
                .values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            if (!targetNamespace.equals(namespaceImports[i]
                    .getAttribute(NAMESPACE_URI))) {
                schemaElement.appendChild(ownerDocument.importNode(
                        namespaceImports[i], true));
            }
        }

        schemaElement.appendChild(ownerDocument.importNode(newElement, true));

    }

    private Element createNewSchemaWithElement(Element newElement,
                                               Map namespacePrefixMap,
                                               Map namespaceImportsMap,
                                               String targetNamespace,
                                               Document document,
                                               String xsdPrefix) {

        Element schemaElement = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                         + XML_SCHEMA_LOCAL_NAME);

        // loop through the namespace declarations first
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
                .keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX
                                         + namespacePrefixMap.get(s).toString(), s);
        }

        if (schemaElement.getAttributeNS(XML_NAMESPACE_URI, xsdPrefix).length() == 0) {
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX + xsdPrefix,
                                         XMLSCHEMA_NAMESPACE_URI);
        }

        // add the targetNamespace
        schemaElement.setAttributeNS(XML_NAMESPACE_URI, XMLNS_AXIS2WRAPPED, targetNamespace);
        schemaElement.setAttribute(XSD_TARGETNAMESPACE, targetNamespace);
        schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT, XSD_UNQUALIFIED);

        // add imports
        Element[] namespaceImports = (Element[]) namespaceImportsMap
                .values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            schemaElement.appendChild(namespaceImports[i]);

        }

        schemaElement.appendChild(newElement);
        return schemaElement;
    }

    private List getPartsListFromSoapBody(List extensibilityElements) {
        List partsList = null;
        ExtensibilityElement extElement;
        for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
            
            
            extElement = (ExtensibilityElement) iter.next();
            
            if (log.isDebugEnabled()) {
                log.debug("Extensibility Element type is:" + extElement.getElementType());
                log.debug("Extensibility Element class is:" + extElement.getClass().getName());
            }
            // SOAP 1.1 body element found!
            if (extElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody) extElement;
                partsList = soapBody.getParts();
            } else if (extElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body) extElement;
                partsList = soapBody.getParts();
            } else if (extElement instanceof MIMEMultipartRelated) {
                MIMEMultipartRelated minMimeMultipartRelated = (MIMEMultipartRelated) extElement;
                List mimePartsList = minMimeMultipartRelated.getMIMEParts();
                MIMEPart mimePart = null;
                Object object;
                List mimePartElements;
                ExtensibilityElement mimePartExtensibilityElement;
                for (Iterator mimePartsIter = mimePartsList.iterator(); mimePartsIter.hasNext();) {
                    object = mimePartsIter.next();
                    if (object instanceof MIMEPart) {
                        mimePart = (MIMEPart) object;
                        mimePartElements = mimePart.getExtensibilityElements();
                        for (Iterator mimePartElementsIter = mimePartElements.iterator(); mimePartElementsIter.hasNext();)
                        {
                            mimePartExtensibilityElement = (ExtensibilityElement) mimePartElementsIter.next();
                            if (mimePartExtensibilityElement instanceof SOAPBody) {
                                SOAPBody soapBody = (SOAPBody) mimePartExtensibilityElement;
                                partsList = soapBody.getParts();
                            } else if (mimePartExtensibilityElement instanceof SOAP12Body) {
                                SOAP12Body soapBody = (SOAP12Body) mimePartExtensibilityElement;
                                partsList = soapBody.getParts();
                            }
                        }
                    }
                }
            } 
        }
        if (partsList == null) {
            log.debug("SOAP body parts have not been set. All the parts in the message were added to the message.");
        }
        return partsList;
    }

    private String getNamespaceFromSoapBody(List extensibilityElements) {

        ExtensibilityElement extElement;
        String namespace = null;
        for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
            extElement = (ExtensibilityElement) iter.next();
            // SOAP 1.1 body element found!
            if (extElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody) extElement;
                namespace = soapBody.getNamespaceURI();
            } else if (extElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body) extElement;
                namespace = soapBody.getNamespaceURI();
            }
        }
        return namespace;
    }

    /**
     * creates a new shema complex element according to the elements sequence difined
     * this parts list is always for a message refering from the
     * soap rpc type operation
     *
     * @param document
     * @param xsdPrefix
     * @param partsCollection     - parts to be added
     * @param parameterOrder      - param Order list if it is given
     * @param isOutMessage
     * @param namespaceImportsMap
     * @param namespacePrefixMap
     * @param boe BindingOperationEntry for this partCollection
     * @return new element
     */
    private Element getNewComplextType(Document document,
                                       String xsdPrefix,
                                       Collection partsCollection,
                                       List parameterOrder,
                                       boolean isOutMessage,
                                       Map namespaceImportsMap,
                                       Map namespacePrefixMap,
                                       BindingOperationEntry boe) {
        // add the complex type
        Element newComplexType = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                         + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);

        Element cmplxTypeSequence = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                         + XML_SCHEMA_SEQUENCE_LOCAL_NAME);

        Part part;
        if ((parameterOrder == null) || (parameterOrder.size() == 0)) {
            // no parameter order then just add the elements in the parts collection
            for (Iterator partsIter = partsCollection.iterator(); partsIter.hasNext();) {
                part = (Part) partsIter.next();
                // the part name
                addPartToElement(part,
                                 document,
                                 xsdPrefix,
                                 namespaceImportsMap,
                                 namespacePrefixMap,
                                 cmplxTypeSequence,
                                 isOutMessage,
                                 boe);

            }
        } else {
            // i.e an parts order is given
            // first populate all the parts to a map
            Map partsMap = new HashMap();
            for (Iterator partsIter = partsCollection.iterator(); partsIter.hasNext();) {
                part = (Part) partsIter.next();
                partsMap.put(part.getName(), part);
            }

            String partName;
            for (Iterator paramOrderIter = parameterOrder.iterator(); paramOrderIter.hasNext();) {
                partName = (String) paramOrderIter.next();
                part = (Part) partsMap.get(partName);
                if (part != null) {
                    addPartToElement(part,
                                     document,
                                     xsdPrefix,
                                     namespaceImportsMap,
                                     namespacePrefixMap,
                                     cmplxTypeSequence, 
                                     isOutMessage,
                                     boe);
                    partsMap.remove(partName);
                }
            }
            // if this is an output message then we have to set the
            // return type if exists
            if (isOutMessage) {
                if (partsMap.size() > 0) {
                    if (partsMap.size() == 1) {
                        part = (Part) partsMap.values().iterator().next();
                        // change the name of this part
                        // this is the return type and its name should be result
                        // part.setName("result");
                        addPartToElement(part,
                                         document,
                                         xsdPrefix,
                                         namespaceImportsMap,
                                         namespacePrefixMap,
                                         cmplxTypeSequence,
                                         isOutMessage,
                                         boe);
                    } else {
                        throw new WSDLProcessingException("the parameter order can left atmost" +
                                                          " one part");
                    }
                }
            }
        }

        newComplexType.appendChild(cmplxTypeSequence);
        return newComplexType;
    }

    /**
     * @param part
     * @param document
     * @param xsdPrefix
     * @param namespaceImportsMap
     * @param namespacePrefixMap
     * @param cmplxTypeSequence
     * @param isOutMessage (true is part is referenced by the output clause)
     * @param boe BindingOperationEntry that caused the creation of this part.
     */
    private void addPartToElement(Part part,
                                  Document document,
                                  String xsdPrefix,
                                  Map namespaceImportsMap,
                                  Map namespacePrefixMap,
                                  Element cmplxTypeSequence,
                                  boolean isOutMessage,
                                  BindingOperationEntry boe) {
        Element child;
        String elementName = part.getName();

        // the type name
        QName schemaTypeName = part.getTypeName();

        if (schemaTypeName != null) {

            child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI,
                                             xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            // always child attribute should be in no namespace
            child.setAttribute("form", "unqualified");
            child.setAttribute("nillable", "true");

            String prefix;
            if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())) {
                prefix = xsdPrefix;
                if(log.isDebugEnabled()) {
                    log.debug("Unable to find a namespace for " + xsdPrefix + ". Creating a new namespace attribute");
                }
                cmplxTypeSequence.setAttributeNS("http://www.w3.org/2000/xmlns/",
                        "xmlns:" + xsdPrefix,
                        XMLSCHEMA_NAMESPACE_URI);
            } else {
                // this schema is a third party one. So we need to have
                // an import statement in our generated schema
                String uri = schemaTypeName.getNamespaceURI();
                if (!namespaceImportsMap.containsKey(uri)) {
                    // create Element for namespace import
                    Element namespaceImport = document.createElementNS(
                            XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                                     + XML_SCHEMA_IMPORT_LOCAL_NAME);
                    namespaceImport.setAttribute(NAMESPACE_URI, uri);
                    // add this to the map
                    namespaceImportsMap.put(uri, namespaceImport);
                    // we also need to associate this uri with a prefix
                    // and include that prefix
                    // in the schema's namspace declarations. So add
                    // theis particular namespace to the
                    // prefix map as well
                    prefix = getTemporaryNamespacePrefix();
                    namespacePrefixMap.put(uri, prefix);
                } else {
                    // this URI should be already in the namspace prefix
                    // map
                    prefix = (String) namespacePrefixMap.get(uri);
                }

            }

            child.setAttribute(XSD_NAME, elementName);
            child.setAttribute(XSD_TYPE, prefix + ":" + schemaTypeName.getLocalPart());
            cmplxTypeSequence.appendChild(child);

        } else {
            String bindingOperationName = boe.getBindingOperation().getName();
            String partName = part.getName();
            if (boe.isRPC()) {
                // see the basic profile 4.4.1 for rpc-literal.
                // messages parts can have only types
                throw new WSDLProcessingException(
                   "The binding operation " + bindingOperationName + " is RPC/literal. " +
                   "The message parts for this operation must use the type " +
                   "attribute as specificed by " +
                   "WS-I Basic Profile specification (4.4.1).  Message part, " + 
                   partName + ", violates" +
                   "this rule.  Please remove the element attribute " +
                   "and use the type attribute.");
            } else {
                // The presense of an element means that a wrapper xsd element is not needed.
                boe.setWrappedOutput(false);
                if (log.isDebugEnabled()) {
                    log.debug("The binding operation " + bindingOperationName + 
                              " references message part " +
                              partName + ".  This part does not use the " +
                              "type attribute, so a wrappering element is not added.");
                }
            }
            
        }
    }

    /**
     * @param prefixMap
     */
    private void checkAndAddNamespaceDeclarations(String namespace,
                                                  Map prefixMap, Element schemaElement) {
        // get the attribute for the current namespace
        String prefix = (String) prefixMap.get(namespace);
        // A prefix must be found at this point!
        String existingURL = schemaElement.getAttributeNS(XML_NAMESPACE_URI,
                                                          NAMESPACE_DECLARATION_PREFIX + prefix);
        if (existingURL == null || existingURL.length() == 0) {
            // there is no existing URL by that prefix - declare a new namespace
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX + prefix, namespace);
        } else if (existingURL.equals(namespace)) {
            // this namespace declaration is already there with the same prefix
            // ignore it
        } else {
            // there is a different namespace declared in the given prefix
            // change the prefix in the prefix map to a new one and declare it

            // create a prefix
            String generatedPrefix = "ns" + prefixCounter++;
            while (prefixMap.containsKey(generatedPrefix)) {
                generatedPrefix = "ns" + prefixCounter++;
            }
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX + generatedPrefix, namespace);
            // add to the map
            prefixMap.put(namespace, generatedPrefix);
        }

    }

    /**
     * Read the WSDL file given the inputstream for the WSDL source
     *
     * @param in
     * @throws WSDLException
     */
    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode for all usecases
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE_KEY, false);
        reader.setFeature("javax.wsdl.importDocuments", true);

        Definition def;
        // if the custem resolver is present then use it
        if (customWSDLResolver != null) {
            // make sure the wsdl definition has the URI for the base document set
            def = reader.readWSDL(customWSDLResolver);
            def.setDocumentBaseURI(customWSDLResolver.getBaseURI());
            return def;
        } else {
            Document doc;
            try {
                doc = XMLUtils.newDocument(in);
            } catch (ParserConfigurationException e) {
                throw new WSDLException(WSDLException.PARSER_ERROR,
                                        "Parser Configuration Error", e);
            } catch (SAXException e) {
                throw new WSDLException(WSDLException.PARSER_ERROR,
                                        "Parser SAX Error", e);

            } catch (IOException e) {
                throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error",
                                        e);
            }

            // Log when and from where the WSDL is loaded.
            if (log.isDebugEnabled()) {
                log.debug("Reading 1.1 WSDL with base uri = " + getBaseUri());
                log.debug("  the document base uri = " + getDocumentBaseUri());
                log.trace("  the stack at this point is: " + stackToString());
            }
            def = reader.readWSDL(getBaseUri(), doc);
            def.setDocumentBaseURI(getDocumentBaseUri());
            return def;
        }
    }

    /**
     * Get the Extensible elements form wsdl4jExtensibleElements
     * <code>Vector</code> if any and copy them to <code>Component</code>
     * <p/> Note - SOAP body extensible element will be processed differently
     *
     * @param wsdl4jExtensibleElements
     * @param description                   where is the ext element (port , portype , biding)
     * @param wsdl4jDefinition
     * @param originOfExtensibilityElements -
     *                                      this will indicate the place this extensibility element came
     *                                      from.
     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Definition wsdl4jDefinition, AxisDescription description,
                                        String originOfExtensibilityElements) throws AxisFault {

        ExtensibilityElement wsdl4jExtensibilityElement;

        for (Iterator iterator = wsdl4jExtensibleElements.iterator(); iterator.hasNext();) {

            wsdl4jExtensibilityElement = (ExtensibilityElement) iterator.next();

            if (wsdl4jExtensibilityElement instanceof UnknownExtensibilityElement) {

                UnknownExtensibilityElement unknown =
                        (UnknownExtensibilityElement) (wsdl4jExtensibilityElement);
                QName type = unknown.getElementType();
                
                // <wsp:Policy>
                if (WSDLConstants.WSDL11Constants.POLICY.equals(type)) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: PolicyElement found " + unknown);
                    }
                    Policy policy = (Policy) PolicyUtil.getPolicyComponent(unknown.getElement());
                    description.getPolicySubject().attachPolicy(policy);
                    
//                    int attachmentScope =
//                            getPolicyAttachmentPoint(description, originOfExtensibilityElements);
//                    if (attachmentScope > -1) {
//                        description.getPolicyInclude().addPolicyElement(
//                                attachmentScope, policy);
//                    }
                    // <wsp:PolicyReference>
                } else if (WSDLConstants.WSDL11Constants.POLICY_REFERENCE
                        .equals(type)) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: PolicyReference found " + unknown);
                    }
                    PolicyReference policyReference = (PolicyReference) PolicyUtil
                            .getPolicyComponent(unknown.getElement());
                    description.getPolicySubject().attachPolicyReference(policyReference);
                    
//                    int attachmentScope =
//                            getPolicyAttachmentPoint(description, originOfExtensibilityElements);
//                    if (attachmentScope > -1) {
//                        description.getPolicyInclude().addPolicyRefElement(
//                                attachmentScope, policyReference);
//                    }
                } else if (AddressingConstants.Final.WSAW_USING_ADDRESSING
                        .equals(type)
                           || AddressingConstants.Submission.WSAW_USING_ADDRESSING
                        .equals(unknown.getElementType())) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: wsaw:UsingAddressing found " + unknown);
                    }
                    // FIXME We need to set this the appropriate Axis Description AxisEndpoint or
                    // AxisBinding .
                    if (originOfExtensibilityElements.equals(PORT)
                        || originOfExtensibilityElements.equals(BINDING)) {
                        if (Boolean.TRUE.equals(unknown.getRequired())) {
                        	AddressingHelper.setAddressingRequirementParemeterValue(axisService, AddressingConstants.ADDRESSING_REQUIRED);
                        } else {
                        	AddressingHelper.setAddressingRequirementParemeterValue(axisService, AddressingConstants.ADDRESSING_OPTIONAL);
                        }
                    }

                }    else if (wsdl4jExtensibilityElement.getElementType() != null &&
                        wsdl4jExtensibilityElement.getElementType().getNamespaceURI().equals(
                                org.apache.axis2.namespace.Constants.FORMAT_BINDING)) {
                    Element typeMapping = unknown.getElement();
                    
                    NodeList typeMaps = typeMapping.getElementsByTagNameNS(
                            org.apache.axis2.namespace.Constants.FORMAT_BINDING,
                            "typeMap");
                    int count = typeMaps.getLength();
                    HashMap typeMapper = new HashMap();
                    for (int index = 0; index < count; index++) {
                        Node node = typeMaps.item(index);
                        NamedNodeMap attributes = node.getAttributes();
                        Node typeName = attributes.getNamedItem("typeName");

                        if (typeName != null) {
                            String prefix = getPrefix(typeName.getNodeValue());

                            if (prefix != null) {
                                String ns = (String) wsdl4jDefinition.getNamespaces().get(prefix);
                                if (ns != null) {
                                    Node formatType = attributes.getNamedItem("formatType");
                                    typeMapper.put(new QName(ns, getTypeName(
                                            typeName.getNodeValue())), formatType.getNodeValue());
                                }

                            }
                        }
                    }
                } else if (wsdl4jExtensibilityElement.getElementType() != null &&
                        wsdl4jExtensibilityElement.getElementType().getNamespaceURI().equals(
                                org.apache.axis2.namespace.Constants.JAVA_NS)) {
                    Element unknowJavaElement = unknown.getElement();
                    if (unknowJavaElement.getLocalName().equals("address") ) {
                        NamedNodeMap nameAttributes = unknowJavaElement.getAttributes();
                        Node node = nameAttributes.getNamedItem("className");
                        Parameter serviceClass = new Parameter();
                        serviceClass.setName("className");
                        serviceClass.setValue(node.getNodeValue());
                        axisService.addParameter(serviceClass);
                        Parameter transportName = new Parameter();
                        transportName.setName("TRANSPORT_NAME");
                        transportName.setValue("java");
                        axisService.addParameter(transportName);
                    }
                }  else {
                    // Ignore this element - it is a totally unknown element
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: Unknown Extensibility Element found " +
                                  unknown);
                    }
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAP12Address) {
                SOAP12Address soapAddress = (SOAP12Address) wsdl4jExtensibilityElement;
                if (description instanceof AxisEndpoint) {
                	setEndpointURL((AxisEndpoint) description, soapAddress.getLocationURI());
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) wsdl4jExtensibilityElement;
                if (description instanceof AxisEndpoint) {
                	setEndpointURL((AxisEndpoint) description, soapAddress.getLocationURI());
                }
            } else if (wsdl4jExtensibilityElement instanceof HTTPAddress) {
                HTTPAddress httpAddress = (HTTPAddress) wsdl4jExtensibilityElement;
                if (description instanceof AxisEndpoint) {
                	setEndpointURL((AxisEndpoint) description, httpAddress.getLocationURI());
                }

            } else if (wsdl4jExtensibilityElement instanceof Schema) {
                Schema schema = (Schema) wsdl4jExtensibilityElement;
                // just add this schema - no need to worry about the imported
                // ones
                axisService.addSchema(getXMLSchema(schema.getElement(), schema
                        .getDocumentBaseURI()));

            } else if (wsdl4jExtensibilityElement instanceof SOAP12Operation) {
                SOAP12Operation soapOperation = (SOAP12Operation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String style = soapOperation.getStyle();
                if (style != null) {
                    axisBindingOperation.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String soapActionURI = soapOperation.getSoapActionURI();
                
                if (this.isCodegen && ((soapActionURI == null) || (soapActionURI.equals("")))) {
                    soapActionURI = axisBindingOperation.getAxisOperation().getInputAction();
                }
                
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                    log.debug("WSDL Binding Operation: " + axisBindingOperation.getName() +
                            ", SOAPAction: " + soapActionURI);
                
                if (soapActionURI != null && !soapActionURI.equals("")) {
                    axisBindingOperation
                            .setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, soapActionURI);
                    axisBindingOperation.getAxisOperation().setSoapAction(soapActionURI);
                    if (!isServerSide) {
                        axisBindingOperation.getAxisOperation().setOutputAction(soapActionURI);
                    }

                    axisService.mapActionToOperation(soapActionURI,
                                                     axisBindingOperation.getAxisOperation());
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String style = soapOperation.getStyle();
                if (style != null) {
                    axisBindingOperation.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String soapAction = soapOperation.getSoapActionURI();
                if (this.isCodegen && ((soapAction == null) || (soapAction.equals("")))) {
                    soapAction = axisBindingOperation.getAxisOperation().getInputAction();
                }
                
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                    log.debug("WSDL Binding Operation: " + axisBindingOperation.getName() +
                            ", SOAPAction: " + soapAction);
                
                if (soapAction != null) {
                    axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
                    axisBindingOperation.getAxisOperation().setSoapAction(soapAction);
                    if (!isServerSide) {
                        axisBindingOperation.getAxisOperation().setOutputAction(soapAction);
                    }

                    axisService.mapActionToOperation(soapAction,
                                                     axisBindingOperation.getAxisOperation());
                }
            } else if (wsdl4jExtensibilityElement instanceof HTTPOperation) {
                HTTPOperation httpOperation = (HTTPOperation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String httpLocation = httpOperation.getLocationURI();
                if (httpLocation != null) {
                    // change the template to make it same as WSDL 2 template
                    httpLocation = httpLocation.replaceAll("\\(", "{");
                    httpLocation = httpLocation.replaceAll("\\)", "}");
                    axisBindingOperation
                            .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);

                }
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                                                 HTTPConstants.MEDIA_TYPE_TEXT_XML);


            } else if (wsdl4jExtensibilityElement instanceof SOAP12Header) {

                SOAP12Header soapHeader = (SOAP12Header) wsdl4jExtensibilityElement;
                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();

                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
                headerMessage.setUse(soapHeader.getUse());

                Boolean required = soapHeader.getRequired();

                if (required != null) {
                    headerMessage.setRequired(required.booleanValue());
                }

                if (wsdl4jDefinition != null) {
                    // find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());

                    if (msg == null) {
                        msg = getMessage(wsdl4jDefinition, soapHeader
                            .getMessage(), new HashSet());
                    }
                    
                    if (msg == null) {
                        // TODO i18n this
                        throw new AxisFault("message "
                                            + soapHeader.getMessage()
                                            + " not found in the WSDL ");
                    }
                    Part msgPart = msg.getPart(soapHeader.getPart());

                    if (msgPart == null) {
                        // TODO i18n this
                        throw new AxisFault("message part "
                                            + soapHeader.getPart()
                                            + " not found in the WSDL ");
                    }
                    // see basic profile 4.4.2 Bindings and Faults header, fault and headerfaults
                    // can only have elements
                    headerMessage.setElement(msgPart.getElementName());
                }

                headerMessage.setMessage(soapHeader.getMessage());
                headerMessage.setPart(soapHeader.getPart());

                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    List soapHeaders =
                            (List) bindingMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders == null) {
                        soapHeaders = new ArrayList();
                        bindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER, soapHeaders);
                    }
                    soapHeaders.add(headerMessage);
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPHeader) {

                SOAPHeader soapHeader = (SOAPHeader) wsdl4jExtensibilityElement;
                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();
                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
                headerMessage.setUse(soapHeader.getUse());
                Boolean required = soapHeader.getRequired();
                if (null != required) {
                    headerMessage.setRequired(required.booleanValue());
                }
                if (null != wsdl4jDefinition) {
                    // find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());
                    
                    if (msg == null) {
                        msg = getMessage(wsdl4jDefinition, soapHeader
                            .getMessage(), new HashSet());
                    }

                    if (msg == null) {
                        // todo i18n this
                        throw new AxisFault("message "
                                            + soapHeader.getMessage()
                                            + " not found in the WSDL ");
                    }
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    if (msgPart == null) {
                        // todo i18n this
                        throw new AxisFault("message part "
                                            + soapHeader.getPart()
                                            + " not found in the WSDL ");
                    }
                    headerMessage.setElement(msgPart.getElementName());
                }
                headerMessage.setMessage(soapHeader.getMessage());

                headerMessage.setPart(soapHeader.getPart());

                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    List soapHeaders =
                            (List) bindingMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders == null) {
                        soapHeaders = new ArrayList();
                        bindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER, soapHeaders);
                    }
                    soapHeaders.add(headerMessage);
                }
            } else if (wsdl4jExtensibilityElement instanceof SOAPBinding) {

                SOAPBinding soapBinding = (SOAPBinding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;

                axisBinding.setType(soapBinding.getTransportURI());

                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

                String style = soapBinding.getStyle();
                if (style != null) {
                    axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAP12Binding) {

                SOAP12Binding soapBinding = (SOAP12Binding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;

                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

                String style = soapBinding.getStyle();
                if (style != null) {
                    axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String transportURI = soapBinding.getTransportURI();
                axisBinding.setType(transportURI);

            } else if (wsdl4jExtensibilityElement instanceof HTTPBinding) {
                HTTPBinding httpBinding = (HTTPBinding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;
                // set the binding style same as the wsd2 to process smoothly
                axisBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);
                axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpBinding.getVerb());
            } else if (wsdl4jExtensibilityElement instanceof MIMEContent) {
                if (description instanceof AxisBindingMessage) {
                    MIMEContent mimeContent = (MIMEContent) wsdl4jExtensibilityElement;
                    String messageSerialization = mimeContent.getType();
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    setMessageSerialization(
                        (AxisBindingOperation)bindingMessage.getParent(),
                        originOfExtensibilityElements, messageSerialization);
                }
            } else if (wsdl4jExtensibilityElement instanceof MIMEMimeXml) {
                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    setMessageSerialization(
                        (AxisBindingOperation)bindingMessage.getParent(),
                        originOfExtensibilityElements,
                        HTTPConstants.MEDIA_TYPE_TEXT_XML);
                }
            } else if (wsdl4jExtensibilityElement instanceof HTTPUrlEncoded) {
                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    setMessageSerialization(
                        (AxisBindingOperation)bindingMessage.getParent(),
                        originOfExtensibilityElements,
                        HTTPConstants.MEDIA_TYPE_X_WWW_FORM);
                }
            }
        }
    }
    
    private static void setMessageSerialization(AxisBindingOperation bindingOperation,
                                                String originOfExtensibilityElements, 
                                                String messageSerialization) {
        if (bindingOperation != null) {
            if (BINDING_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                bindingOperation.setProperty(
                    WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                    messageSerialization);
            } else if (BINDING_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
                bindingOperation.setProperty(
                    WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION,
                    messageSerialization);
            }
        }
    }
    
    /**
     * Look deeper in the imports to find the requested Message. Use a HashSet to make 
     * sure we don't traverse the same Definition object twice (avoid recursion).
     * 
     * @param definition
     * @param message
     * @param instances
     * @return
     */
    private Message getMessage(Definition definition, QName message, Set seen) {
        
        Message msg = definition.getMessage(message);
        
        if (msg != null) { 
            if (log.isDebugEnabled()) {
                log.debug("getMessage returning = " + ((message.getLocalPart() != null) ? message.getLocalPart(): "NULL"));
            }

            return msg;
        }
        
        seen.add(definition);
        
        Iterator iter = definition.getImports().values().iterator();
        
        while (iter.hasNext()) {
            Vector values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                Import wsdlImport = (Import) valuesIter.next();
                Definition innerDefinition = wsdlImport.getDefinition();
                
                if (seen.contains(innerDefinition)) { 
                    continue; // Skip seen
                }
                Message result = getMessage(innerDefinition, message, seen);
                if (result != null) { 
                    return result;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("getMessage: Unable to find message, returning NULL");
        }

        return null;
    }
 
    
    
    
    private int getPolicyAttachmentPoint(AxisDescription description,
                                         String originOfExtensibilityElements) {
        int result = -1; // Attachment Point Not Identified

        if (SERVICE.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.SERVICE_POLICY;
        } else if (PORT.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.PORT_POLICY;
        } else if (BINDING.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.BINDING_POLICY;
        } else if (BINDING_OPERATION.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.BINDING_OPERATION_POLICY;
        } else if (BINDING_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.BINDING_INPUT_POLICY;
        } else if (BINDING_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.BINDING_OUTPUT_POLICY;
        } else if (PORT_TYPE.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.PORT_TYPE_POLICY;
        } else if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.OPERATION_POLICY;
        } else if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.INPUT_POLICY;
        } else if (PORT_TYPE_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
            result = PolicyInclude.OUTPUT_POLICY;
        }

        if (isTraceEnabled) {
            log.trace("getPolicyAttachmentPoint:: axisDescription=" + description +
                      " extensibilityPoint=" + originOfExtensibilityElements + " result=" + result);
        }

        return result;
    }

    /**
     * Look for the wrappable operations depending on the style
     *
     * @param binding
     * @return List of BindingOperationEntry objects
     */
    private List findWrappableBindingOperations(Binding binding) {
        // first find the global style declaration.
        // for a SOAP binding this can be only rpc or document
        // as per the WSDL spec (section 3.4) the default style is document

        // now we have to handle the http bindings case as well
        //

        boolean isRPC = false;
        boolean isSOAPBinding = false;
        boolean isHttpBinding = false;

        List extElements = binding.getExtensibilityElements();
        for (int i = 0; i < extElements.size(); i++) {
            if (extElements.get(i) instanceof SOAPBinding) {
                // we have a global SOAP binding!
                isSOAPBinding = true;
                SOAPBinding soapBinding = (SOAPBinding) extElements.get(i);
                if (RPC_STYLE.equals(soapBinding.getStyle())) {
                    // set the global style to rpc
                    isRPC = true;
                }
                this.bindingType = BINDING_TYPE_SOAP;
                break;
            } else if (extElements.get(i) instanceof SOAP12Binding) {
                // we have a global SOAP binding!
                isSOAPBinding = true;
                SOAP12Binding soapBinding = (SOAP12Binding) extElements.get(i);
                if (RPC_STYLE.equals(soapBinding.getStyle())) {
                    // set the global style to rpc
                    isRPC = true;
                }
                this.bindingType = BINDING_TYPE_SOAP;
                break;
            } else if (extElements.get(i) instanceof HTTPBinding) {
                isHttpBinding = true;
                this.bindingType = BINDING_TYPE_HTTP;
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Binding Name    =" + binding.getQName());
            log.debug("  isSOAPBinding =" + isSOAPBinding );
            log.debug("  isHttpBinding =" + isHttpBinding );
            log.debug("  isRPC         =" + isRPC );
        }

        // go through every operation and get their styles.
        // each one can have a style override from the global
        // styles. Depending on the style add the relevant operations
        // to the return list
        List returnList = new ArrayList();

        if (isHttpBinding || isSOAPBinding) {
            BindingOperation bindingOp;
            for (Iterator bindingOperationsIterator =
                    binding.getBindingOperations().iterator(); bindingOperationsIterator.hasNext();)
            {
                bindingOp = (BindingOperation) bindingOperationsIterator.next();
                
                if (log.isDebugEnabled()) {
                    log.debug("  Binding Operation  =" + bindingOp.getName());
                }
                if (isSOAPBinding) {
                    String style = getSOAPStyle(bindingOp);
                    if (log.isDebugEnabled()) {
                        log.debug("    SOAPStyle  =" + style);
                    }
                    if (style == null) {
                        // no style specified
                        // use the global style to determine whether to put this one or
                        // not
                        if (isRPC) {
                            if (log.isDebugEnabled()) {
                                log.debug("    schema wrappering required");
                            }
                            BindingOperationEntry boe = 
                                new BindingOperationEntry(bindingOp, 
                                                          true, 
                                                          false,
                                                          style,
                                                          true);
                            returnList.add(boe);
                        }
                    } else if (RPC_STYLE.equals(style)) {
                        // add to the list
                        if (log.isDebugEnabled()) {
                            log.debug("    schema wrappering required");
                        }
                        BindingOperationEntry boe = 
                            new BindingOperationEntry(bindingOp, 
                                                      true, 
                                                      false,
                                                      style,
                                                      true);
                        returnList.add(boe);
                    }
                    // if not RPC we just leave it - default is doc
                } else {
                    // i.e an http binding then we have to add the operation any way
                    if (log.isDebugEnabled()) {
                        log.debug("    schema wrappering required");
                    }
                    BindingOperationEntry boe = 
                        new BindingOperationEntry(bindingOp, 
                                                  false, 
                                                  true,
                                                  null,
                                                  false);
                    returnList.add(boe);
                }
            }
        }

        // if the binding is not either soap or http binding then we return and empty list

        // set this to the global list
        wrappableBOEs = returnList;
        return returnList;
    }

    /**
     * Guess the MEP based on the order of messages
     *
     * @param operation
     * @throws AxisFault
     */
    private String getMEP(Operation operation) throws AxisFault {
        OperationType operationType = operation.getStyle();
        if (isServerSide) {
            if (operationType != null) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    return WSDL2Constants.MEP_URI_IN_OUT;
                }

                if (operationType.equals(OperationType.ONE_WAY)) {
                    if (operation.getFaults().size() > 0) {
                        return WSDL2Constants.MEP_URI_ROBUST_IN_ONLY;
                    }
                    return WSDL2Constants.MEP_URI_IN_ONLY;
                }

                if (operationType.equals(OperationType.NOTIFICATION)) {
                    return WSDL2Constants.MEP_URI_OUT_ONLY;
                }

                if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    return WSDL2Constants.MEP_URI_OUT_IN;
                }
                throw new AxisFault("Cannot Determine the MEP");
            }
        } else {
            if (operationType != null) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    return WSDL2Constants.MEP_URI_OUT_IN;
                }

                if (operationType.equals(OperationType.ONE_WAY)) {
                    return WSDL2Constants.MEP_URI_OUT_ONLY;
                }

                if (operationType.equals(OperationType.NOTIFICATION)) {
                    return WSDL2Constants.MEP_URI_IN_ONLY;
                }

                if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    return WSDL2Constants.MEP_URI_IN_OUT;
                }
                throw new AxisFault("Cannot Determine the MEP");
            }
        }
        throw new AxisFault("Cannot Determine the MEP");
    }

    /**
     * Copies the extension attributes
     *
     * @param extAttributes
     * @param description
     * @param origin
     */
    private void copyExtensionAttributes(Map extAttributes,
			AxisDescription description, String origin) {

		QName key;
		QName value;

		for (Iterator iterator = extAttributes.keySet().iterator(); iterator
				.hasNext();) {
			key = (QName) iterator.next();

			if (Constants.URI_POLICY_NS.equals(key.getNamespaceURI())
					&& "PolicyURIs".equals(key.getLocalPart())) {

				value = (QName) extAttributes.get(key);
				String policyURIs = value.getLocalPart();

				if (policyURIs.length() != 0) {
					String[] uris = policyURIs.split(" ");

					PolicyReference ref;
					for (int i = 0; i < uris.length; i++) {
						ref = new PolicyReference();
						ref.setURI(uris[i]);

						if (PORT_TYPE.equals(origin)
						        || PORT_TYPE_OPERATION.equals(origin)
						        || PORT_TYPE_OPERATION_INPUT.equals(origin) 
						        || PORT_TYPE_OPERATION_OUTPUT.equals(origin)) {
                      
						    if (description != null) {
						        PolicySubject subject = description.getPolicySubject();
                             
						        if (subject != null) {
						            subject.attachPolicyReference(ref);
						        }
						    }
						}
					}
				}
			}
		}
	}

    /**
	 * Process the policy definitions
	 * 
	 * @param definition
	 */
    private void processPoliciesInDefintion(Definition definition) {
        processPoliciesInDefintion(definition, new HashSet());
    }

    /**
	 * Process the policy definitions
	 * 
	 * @param definition
	 */
    private void processPoliciesInDefintion(Definition definition, Set visitedWSDLs) {
        visitedWSDLs.add(definition.getDocumentBaseURI());
        List extElements = definition.getExtensibilityElements();
        ExtensibilityElement extElement;
        UnknownExtensibilityElement unknown = null;
        Policy policy = null;

        for (Iterator iterator = extElements.iterator(); iterator.hasNext();) {
            extElement = (ExtensibilityElement) iterator.next();

            if (extElement instanceof UnknownExtensibilityElement) {
                unknown = (UnknownExtensibilityElement) extElement;
                if (WSDLConstants.WSDL11Constants.POLICY.equals(unknown.getElementType())) {

                    policy = (Policy) PolicyUtil.getPolicyComponent(unknown.getElement());
                    String key;
                    if ((key = policy.getName()) != null || (key = policy.getId()) != null) {
                    	axisService.registerPolicy(key, policy);
//                        registry.register(key, policy);
//                        registry.register("#" + key, policy);
                    }

                }
            }
        }
        // include policices in other imported wsdls
        Iterator iter = definition.getImports().values().iterator();
        Vector values = null;
        Import wsdlImport = null;
        for (; iter.hasNext();) {
            values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                wsdlImport = (Import) valuesIter.next();
                Definition innerDefinition = wsdlImport.getDefinition();
                // find the binding recursively
                if(!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())) {
                    processPoliciesInDefintion(innerDefinition, visitedWSDLs);
                }
            }
        }
    }
    
    private void setEndpointURL(AxisEndpoint axisEndpoint, String endpointURL) throws AxisFault {
    	axisEndpoint.setEndpointURL(endpointURL);
    	try {
    		URL url = new URL(endpointURL);
    		axisEndpoint.setTransportInDescription(url.getProtocol());
    	} catch (Exception e) {
    		log.debug(e.getMessage(),e);
		}    	
    }


    /**
     * Inner class declaration for the processing exceptions
     */
    public static class WSDLProcessingException extends RuntimeException {
        public WSDLProcessingException() {
        }

        public WSDLProcessingException(String message) {
            super(message);
        }

        public WSDLProcessingException(Throwable cause) {
            super(cause);
        }

        public WSDLProcessingException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public boolean isAllPorts() {
        return isAllPorts;
    }

    public void setAllPorts(boolean allPorts) {
        isAllPorts = allPorts;
    }

//    private void processPoliciesInDefinition() {
//
//        Object obj;
//        for (Iterator iterator = wsdl4jDefinition.getExtensibilityElements().iterator(); iterator.hasNext();) {
//            obj = iterator.next();
//
//            if (obj instanceof UnknownExtensibilityElement) {
//                Element e = ((UnknownExtensibilityElement) obj).getElement();
//                if (WSDLConstants.WSDL11Constants.POLICY.getNamespaceURI().equals(e.getNamespaceURI()) &&
//                        WSDLConstants.WSDL11Constants.POLICY.getLocalPart().equals(e.getLocalName())) {
//                    Policy p = (Policy) PolicyUtil.getPolicyComponent(e);
//                    reg.register(p.getId(), p);
//                }
//            }
//        }
//    }

    /**
     * This method is to split attribute like abc:cde and get the prefix part of it
     * so the method will retuen abc if the ":" is present in the the string else it
     * will return null
     *
     * @param attributeValue  : String
     * @return String
     */
    public static String getPrefix(String attributeValue) {
        if (attributeValue != null) {
            int splitIdex = attributeValue.indexOf(':');
            if (splitIdex > 0) {
                return attributeValue.substring(0, splitIdex);
            }
        }
        return null;
    }

    public static String getTypeName(String attributeValue) {
        if (attributeValue != null) {
            int splitIdex = attributeValue.indexOf(':');
            if (splitIdex > 0) {
                return attributeValue.substring(splitIdex + 1);
            } else {
                return attributeValue;
            }
        }
        return null;
    }

    /**
     * returns the wsld defintion for the given component.
     * @param definition
     * @param qname
     * @param componentType
     * @param visitedWSDLs
     * @return definition containing the component.
     */
    private Definition getParentDefinition(Definition definition,
                                           QName qname,
                                           int componentType,
                                           Set visitedWSDLs){
        visitedWSDLs.add(definition.getDocumentBaseURI());
        Definition newParentDefinition = null;
        // first find through imports
        Iterator iter = definition.getImports().values().iterator();
        Vector values = null;
        Import wsdlImport = null;
        for (; iter.hasNext();) {
            values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                wsdlImport = (Import) valuesIter.next();
                Definition innerDefinition = wsdlImport.getDefinition();
                if (!visitedWSDLs.contains(innerDefinition.getDocumentBaseURI())){
                    newParentDefinition = getParentDefinition(innerDefinition,qname,componentType,visitedWSDLs);
                    if (newParentDefinition != null){
                        break;
                    }
                }
            }
            if (newParentDefinition != null) {
                break;
            }
        }

        // if it not available in imports we check for the current definition.
        if (newParentDefinition == null) {
            // this can be in a imported wsdl
            if (isComponetAvailable(definition, qname, componentType)) {
                newParentDefinition = definition;
            }
        }

        return newParentDefinition;
    }

    private boolean isComponetAvailable(Definition definition,
                                        QName qname,
                                        int componentType){
        boolean isAvailable = false;
        switch (componentType){
            case COMPONENT_BINDING : {
                isAvailable = (definition.getBinding(qname) != null) &&
                        (definition.getBinding(qname).getPortType() != null);
                break;
            }
            case COMPONENT_PORT_TYPE : {
                isAvailable = (definition.getPortType(qname) != null);
                break;
            }
            case COMPONENT_MESSAGE : {
                isAvailable = (definition.getMessage(qname) != null);
                break;
            }
        }
        return isAvailable;
    }
    
    /**
     * Find BindingOperationEntry
     * @param boes List of BindingOperationEntry
     * @param bo BindingOperation 
     * @return BindingOperation or null
     */
    private BindingOperationEntry find(List boes, BindingOperation bo) {
        for (int i=0; i < boes.size(); i++) {
            BindingOperationEntry boe = (BindingOperationEntry) boes.get(i);
            if (boe.getBindingOperation() == bo) {
                return boe;
            }
        }
        return null;
    }
    /**
     * BindingOperation plus state information
     */
    class BindingOperationEntry {
        
        private BindingOperation bindingOperation;
        private boolean isSOAPBinding;
        private boolean isHTTPBinding;
        private String soapStyle;
        private boolean isRPC;
        private boolean wrappedInput = true;
        private boolean wrappedOutput = true;
        
        public BindingOperationEntry(BindingOperation bindingOperation, boolean isSOAPBinding, boolean isHTTPBinding, String soapStyle, boolean isRPC) {
            super();
            this.bindingOperation = bindingOperation;
            this.isSOAPBinding = isSOAPBinding;
            this.isHTTPBinding = isHTTPBinding;
            this.soapStyle = soapStyle;
            this.isRPC = isRPC;
        }
        
        public boolean isHTTPBinding() {
            return isHTTPBinding;
        }
        public boolean isSOAPBinding() {
            return isSOAPBinding;
        }
        public String getSoapStyle() {
            return soapStyle;
        }
        
        public boolean isRPC() {
            return isRPC;
        }
        public BindingOperation getBindingOperation() {
            return bindingOperation;
        }

        /**
         * @return true if wrapper xsd is used
         */
        public boolean isWrappedInput() {
            return wrappedInput;
        }

        /**
         * @param wrappedInput indicate if wrapper xsd is needed
         */
        public void setWrappedInput(boolean wrappedInput) {
            this.wrappedInput = wrappedInput;
        }

        /**
         * @return true if wrapper xsd is needed for the output message
         */
        public boolean isWrappedOutput() {
            return wrappedOutput;
        }

        /**
         * @param wrappedOutput indicate if wrapper xsd is needed on the output message
         */
        public void setWrappedOutput(boolean wrappedOutput) {
            this.wrappedOutput = wrappedOutput;
        }
    }
}
