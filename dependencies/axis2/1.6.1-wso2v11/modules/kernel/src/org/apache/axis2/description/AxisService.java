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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.dataretrieval.AxisDataLocator;
import org.apache.axis2.dataretrieval.AxisDataLocatorImpl;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.Data;
import org.apache.axis2.dataretrieval.DataRetrievalException;
import org.apache.axis2.dataretrieval.DataRetrievalRequest;
import org.apache.axis2.dataretrieval.LocatorType;
import org.apache.axis2.dataretrieval.OutputForm;
import org.apache.axis2.dataretrieval.SchemaSupplier;
import org.apache.axis2.dataretrieval.WSDLSupplier;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.util.ExcludeInfo;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.description.java2wsdl.DocLitBareSchemaGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.SchemaGenerator;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jsr181.JSR181Helper;
import org.apache.axis2.jsr181.WebMethodAnnotation;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.IOUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class AxisService
 */
public class AxisService extends AxisDescription {

	// ////////////////////////////////////////////////////////////////
	// Standard Parameter names

	/**
	 * If this param is true, and the service has exactly one AxisOperation,
	 * normal operation dispatch (via URI/soapAction/etc) will not be necessary,
	 * and we'll just default to funneling all messages to that op. This is
	 * useful for passthrough/ESB/embedded applications.
	 */
	public static final String SUPPORT_SINGLE_OP = "supportSingleOperation";
	// ////////////////////////////////////////////////////////////////

	public static final String IMPORT_TAG = "import";
	public static final String INCLUDE_TAG = "include";
	public static final String SCHEMA_LOCATION = "schemaLocation";

	private Map<String, AxisEndpoint> endpointMap = new HashMap<String, AxisEndpoint>();

	/*
	 * This is a map between the QName of the element of a message specified in
	 * the WSDL and an Operation. It enables SOAP Body-based dispatching for
	 * doc-literal bindings.
	 */
	private Map<QName, AxisOperation> messageElementQNameToOperationMap = new HashMap<QName, AxisOperation>();

	private int nsCount = 0;
	private static final Log log = LogFactory.getLog(AxisService.class);
	private URL fileName;

	// Maps httpLocations to corresponding operations. Used to dispatch rest
	// messages.
	private HashMap<String, AxisOperation> httpLocationDispatcherMap = null;

	// A map of (String alias, AxisOperation operation). The aliases might
	// include: SOAPAction,
	// WS-Addressing action, the operation name, the AxisInputMessage name. See:
	// - invalidOperationsAliases
	// - mapActionToOperatoin()
	// - getOperationByAction()
	// REVIEW: This really should be seperate maps for the different types of
	// aliases so they don't
	// conflict with each other. For example, so that an identical operation
	// name and soap action
	// on different operatoins don't cause a collision; the following can't be
	// routed because
	// "foo" is not unique across different operations:
	// operation 1: action = foo, name = bar
	// operation 2: action = bar, name = foo
	private HashMap<String, AxisOperation> operationsAliasesMap = null;

	// Collection of aliases that are invalid for this service because they are
	// duplicated across
	// multiple operations under this service.
	private List<String> invalidOperationsAliases = null;
	// private HashMap operations = new HashMap();

	// to store module ref at deploy time parsing
	private ArrayList<String> moduleRefs = null;

	// to keep the time that last update time of the service
	private long lastupdate;
	private HashMap<String, ModuleConfiguration> moduleConfigmap;
	private String name;
	private ClassLoader serviceClassLoader;

	// to keep the XMLScheam getting either from WSDL or java2wsdl
	private ArrayList<XmlSchema> schemaList;
	// private XmlSchema schema;

	// wsdl is there for this service or not (in side META-INF)
	private boolean wsdlFound = false;

	// to store the scope of the service
	private String scope;

	// to store default message receivers
	private HashMap<String, MessageReceiver> messageReceivers;

	// to set the handler chain available in phase info
	private boolean useDefaultChains = true;

	// to keep the status of the service , since service can stop at the run
	// time
	private boolean active = true;

	private boolean elementFormDefault = true;

	// to keep the service target name space
	private String targetNamespace = Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE;
	private String targetNamespacePrefix = Java2WSDLConstants.TARGETNAMESPACE_PREFIX;

	// to store the target namespace for the schema
	private String schematargetNamespace;// = Java2WSDLConstants.AXIS2_XSD;
	private String schematargetNamespacePrefix = Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX;

	private boolean enableAllTransports = true;
	private List<String> exposedTransports = new ArrayList<String>();

	// To keep reference to ServiceLifeCycle instance , if the user has
	// specified in services.xml
	private ServiceLifeCycle serviceLifeCycle;

	/**
	 * Keeps track whether the schema locations are adjusted
	 */
	private boolean schemaLocationsAdjusted = false;

	private boolean wsdlImportLocationAdjusted = false;

	/**
	 * A table that keeps a mapping of unique xsd names (Strings) against the
	 * schema objects. This is populated in the first instance the schemas are
	 * asked for and then used to serve the subsequent requests
	 */
	private Map schemaMappingTable = null;

	/**
	 * counter variable for naming the schemas
	 */
	private int count = 0;
	/**
	 * A custom schema Name prefix. if set this will be used to modify the
	 * schema names
	 */
	private String customSchemaNamePrefix = null;

	/**
	 * A custom schema name suffix. will be attached to the schema file name
	 * when the files are uniquely named. A good place to add a file extension
	 * if needed
	 */
	private String customSchemaNameSuffix = null;

	// ///////////////////////////////////////
	// WSDL related stuff ////////////////////
	// //////////////////////////////////////

	/** Map of prefix -> namespaceURI */
	private NamespaceMap namespaceMap;

	private String soapNsUri;
	private String endpointName;
	private String endpointURL;
    
    private List importedNamespaces;

	private boolean clientSide = false;

	// To keep a ref to ObjectSupplier instance
	private ObjectSupplier objectSupplier;

	// package to namespace mapping
	private Map p2nMap;

	// to keep the exclude property details
	private ExcludeInfo excludeInfo;

	private TypeTable typeTable;

	// Data Locators for WS-Mex Support
	private HashMap dataLocators;
	private HashMap<String, String> dataLocatorClassNames;
	private AxisDataLocatorImpl defaultDataLocator;
	// Define search sequence for datalocator based on Data Locator types.
	LocatorType[] availableDataLocatorTypes = new LocatorType[] {
			LocatorType.SERVICE_DIALECT, LocatorType.SERVICE_LEVEL,
			LocatorType.GLOBAL_DIALECT, LocatorType.GLOBAL_LEVEL,
			LocatorType.DEFAULT_AXIS };

	// name of the binding used : use in codegeneration
	private String bindingName;
        
	// List of MessageContextListeners that listen for events on the MessageContext
        private CopyOnWriteArrayList<MessageContextListener> messageContextListeners = 
            new CopyOnWriteArrayList<MessageContextListener>();

        // names list keep to preserve the parameter order
        private List operationsNameList;

	private String[] eprs;
	private boolean customWsdl = false;

	private HashMap policyMap = new HashMap();

	public AxisEndpoint getEndpoint(String key) {
		return (AxisEndpoint) endpointMap.get(key);
	}

	public void addEndpoint(String key, AxisEndpoint axisEndpoint) {
		this.endpointMap.put(key, axisEndpoint);
	}

	/**
	 * @deprecated Use AddressingHelper.getAddressingRequirementParemeterValue
	 */
	public String getWSAddressingFlag() {
		return AddressingHelper.getAddressingRequirementParemeterValue(this);
	}

	/**
	 * @deprecated Use AddressingHelper.setAddressingRequirementParemeterValue
	 */
	public void setWSAddressingFlag(String ar) {
		AddressingHelper.setAddressingRequirementParemeterValue(this, ar);
	}

	public boolean isSchemaLocationsAdjusted() {
		return schemaLocationsAdjusted;
	}

	public void setSchemaLocationsAdjusted(boolean schemaLocationsAdjusted) {
		this.schemaLocationsAdjusted = schemaLocationsAdjusted;
	}

	public Map getSchemaMappingTable() {
		return schemaMappingTable;
	}

	public void setSchemaMappingTable(Map schemaMappingTable) {
		this.schemaMappingTable = schemaMappingTable;
	}

	public String getCustomSchemaNamePrefix() {
		return customSchemaNamePrefix;
	}

	public void setCustomSchemaNamePrefix(String customSchemaNamePrefix) {
		this.customSchemaNamePrefix = customSchemaNamePrefix;
	}

	public String getCustomSchemaNameSuffix() {
		return customSchemaNameSuffix;
	}

	public void setCustomSchemaNameSuffix(String customSchemaNameSuffix) {
		this.customSchemaNameSuffix = customSchemaNameSuffix;
	}

	/**
	 * Constructor AxisService.
	 */
	public AxisService() {
		super();
		this.operationsAliasesMap = new HashMap();
		this.invalidOperationsAliases = new ArrayList();
		moduleConfigmap = new HashMap();
		// by default service scope is for the request
		scope = Constants.SCOPE_REQUEST;
		httpLocationDispatcherMap = new HashMap();
		messageReceivers = new HashMap();
		moduleRefs = new ArrayList();
		schemaList = new ArrayList();
		serviceClassLoader = (ClassLoader) org.apache.axis2.java.security.AccessController
				.doPrivileged(new PrivilegedAction<ClassLoader>() {
					public ClassLoader run() {
						return Thread.currentThread().getContextClassLoader();
					}
				});
		objectSupplier = new DefaultObjectSupplier();
		dataLocators = new HashMap();
		dataLocatorClassNames = new HashMap();
	}

	/**
	 * @return name of the port type
	 * @deprecated use AxisService#getEndpointName() instead.
	 */
	public String getPortTypeName() {
		return endpointName;
	}

	/**
	 * @param portTypeName
	 * @deprecated use AxisService#setEndpointName() instead
	 */
	public void setPortTypeName(String portTypeName) {
		this.endpointName = portTypeName;
	}

	public String getBindingName() {
		return bindingName;
	}

	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;
	}

	/**
	 * get the SOAPVersion
	 */
	public String getSoapNsUri() {
		return soapNsUri;
	}

	public void setSoapNsUri(String soapNsUri) {
		this.soapNsUri = soapNsUri;
	}

	/**
	 * get the endpointName
	 */
	public String getEndpointName() {
		return endpointName;
	}

	public void setEndpointName(String endpoint) {
		this.endpointName = endpoint;
	}

	/**
	 * Constructor AxisService.
	 */
	public AxisService(String name) {
		this();
		this.name = name;
	}

	@SuppressWarnings("deprecation")
    public void addMessageReceiver(String mepURI,
			MessageReceiver messageReceiver) {
		if (WSDL2Constants.MEP_URI_IN_ONLY.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_IN_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY,
					messageReceiver);
		} else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_ONLY
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_OUT_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_ONLY,
					messageReceiver);
		} else if (WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT
						.equals(mepURI)) {
			messageReceivers
					.put(WSDL2Constants.MEP_URI_IN_OUT, messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT,
					messageReceiver);
		} else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT,
					messageReceiver);
			messageReceivers
					.put(
							WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT,
							messageReceiver);
		} else if (WSDL2Constants.MEP_URI_OUT_IN.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN
						.equals(mepURI)) {
			messageReceivers
					.put(WSDL2Constants.MEP_URI_OUT_IN, messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN,
					messageReceiver);
		} else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN,
					messageReceiver);
			messageReceivers
					.put(
							WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN,
							messageReceiver);
		} else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY,
					messageReceiver);
			messageReceivers
					.put(
							WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY,
							messageReceiver);
		} else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI)
				|| WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY
						.equals(mepURI)
				|| WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY
						.equals(mepURI)) {
			messageReceivers.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY,
					messageReceiver);
			messageReceivers.put(
					WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY,
					messageReceiver);
		} else {
			messageReceivers.put(mepURI, messageReceiver);
		}
	}

	public MessageReceiver getMessageReceiver(String mepURL) {
		return messageReceivers.get(mepURL);
	}

	/**
	 * Adds module configuration , if there is moduleConfig tag in service.
	 * 
	 * @param moduleConfiguration
	 */
	public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
		moduleConfigmap.put(moduleConfiguration.getModuleName(),
				moduleConfiguration);
	}

	/**
	 * Add any control operations defined by a Module to this service.
	 * 
	 * @param module
	 *            the AxisModule which has just been engaged
	 * @throws AxisFault
	 *             if a problem occurs
	 */
	void addModuleOperations(AxisModule module) throws AxisFault {
		HashMap<QName, AxisOperation> map = module.getOperations();
		Collection<AxisOperation> col = map.values();
		PhaseResolver phaseResolver = new PhaseResolver(getAxisConfiguration());
		for (Iterator<AxisOperation> iterator = col.iterator(); iterator.hasNext();) {
			AxisOperation axisOperation = copyOperation((AxisOperation) iterator
					.next());
			if (this.getOperation(axisOperation.getName()) == null) {
				ArrayList<String> wsamappings = axisOperation.getWSAMappingList();
				if (wsamappings != null) {
					for (int j = 0, size = wsamappings.size(); j < size; j++) {
						String mapping = (String) wsamappings.get(j);
                        //If there is already an operation with this action
						//mapping (e.g. if the service has a matching operation)
						//then we're going to check to see if the module's
						//operation says that it's OK to be overridden and
						//if so, we'll simply ignore the mapping, otherwise
						//we continue as before
						AxisOperation mappedOperation = getOperationByAction(mapping);
						if ((mappedOperation != null)
						    && (axisOperation.isParameterTrue(DeploymentConstants.TAG_ALLOWOVERRIDE))) {
						  if (log.isDebugEnabled()) {
						    log
						    .debug("addModuleOperations: Mapping already exists for action: "
						           + mapping
						           + " to operation: "
						           + axisOperation
						           + " named: "
						           + axisOperation.getName()
						           + " and an override is allowed, so the module mapping for module: "
						           + module.getName()
						           + " is being ignored.");
						    log.debug(JavaUtils.callStackToString());
						  }
						} else {
						  mapActionToOperation(mapping, axisOperation);
						}
					}
				}
				// If we've set the "expose" parameter for this operation, it's
				// normal (non-
				// control) and therefore it will appear in generated WSDL. If
				// we haven't,
				// it's a control operation and will be ignored at WSDL-gen
				// time.
				if (axisOperation
						.isParameterTrue(DeploymentConstants.TAG_EXPOSE)) {
					axisOperation.setControlOperation(false);
				} else {
					axisOperation.setControlOperation(true);
				}

				phaseResolver.engageModuleToOperation(axisOperation, module);

				this.addOperation(axisOperation);
			}
		}
	}

	public void addModuleref(String moduleref) {
		moduleRefs.add(moduleref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.description.AxisService#addOperation(org.apache.axis2.description.AxisOperation)
	 */

	/**
	 * Method addOperation.
	 * 
	 * @param axisOperation
	 */
	public void addOperation(AxisOperation axisOperation) {
		axisOperation.setParent(this);
        
        if (log.isDebugEnabled()) {
            if (axisOperation.getName().equals(ServiceClient.ANON_OUT_ONLY_OP)
                    || (axisOperation.getName().equals(ServiceClient.ANON_OUT_ONLY_OP))
                    || (axisOperation.getName().equals(ServiceClient.ANON_OUT_ONLY_OP))) {
                log.debug("Client-defined operation name matches default operation name. "
                        + "this may cause interoperability issues.  Name is: " + axisOperation.getName().toString());
            }
        }

		Iterator<AxisModule> modules = getEngagedModules().iterator();

		while (modules.hasNext()) {
			AxisModule module = (AxisModule) modules.next();
			try {
				axisOperation.engageModule(module);
			} catch (AxisFault axisFault) {
				log.info(Messages.getMessage("modulealredyengagetoservice",
						module.getName()));
			}
		}
		if (axisOperation.getMessageReceiver() == null) {
			axisOperation.setMessageReceiver(loadDefaultMessageReceiver(
					axisOperation.getMessageExchangePattern(), this));
		}
		if (axisOperation.getInputAction() == null) {
			axisOperation.setSoapAction("urn:"
					+ axisOperation.getName().getLocalPart());
		}

		if (axisOperation.getOutputAction() == null) {
			axisOperation.setOutputAction("urn:"
					+ axisOperation.getName().getLocalPart()
					+ Java2WSDLConstants.RESPONSE);
		}
		addChild(axisOperation);

		String operationName = axisOperation.getName().getLocalPart();

		/*
		 * Some times name of the operation can be different from the name of
		 * the first child of the SOAPBody. This will put the correct mapping
		 * associating that name with the operation. This will be useful
		 * especially for the SOAPBodyBasedDispatcher
		 */

		Iterator axisMessageIter = axisOperation.getChildren();

		while (axisMessageIter.hasNext()) {
			AxisMessage axisMessage = (AxisMessage) axisMessageIter.next();
			String messageName = axisMessage.getName();
			if (messageName != null && !messageName.equals(operationName)) {
				mapActionToOperation(messageName, axisOperation);
			}
		}

		mapActionToOperation(operationName, axisOperation);

		String action = axisOperation.getInputAction();
		if (action.length() > 0) {
			mapActionToOperation(action, axisOperation);
		}

		ArrayList<String> wsamappings = axisOperation.getWSAMappingList();
		if (wsamappings != null) {
			for (int j = 0, size = wsamappings.size(); j < size; j++) {
				String mapping = (String) wsamappings.get(j);
				mapActionToOperation(mapping, axisOperation);
			}
		}

		if (axisOperation.getMessageReceiver() == null) {
			axisOperation.setMessageReceiver(loadDefaultMessageReceiver(
					axisOperation.getMessageExchangePattern(), this));
		}
	}

	private MessageReceiver loadDefaultMessageReceiver(String mepURL,
			AxisService service) {
		MessageReceiver messageReceiver;
		if (mepURL == null) {
			mepURL = WSDL2Constants.MEP_URI_IN_OUT;
		}
		if (service != null) {
			messageReceiver = service.getMessageReceiver(mepURL);
			if (messageReceiver != null) {
				return messageReceiver;
			}
		}
		if (getAxisConfiguration() != null) {
			return getAxisConfiguration().getMessageReceiver(mepURL);
		}
		return null;
	}

	/**
	 * Gets a copy from module operation.
	 * 
	 * @param axisOperation
	 * @return Returns AxisOperation.
	 * @throws AxisFault
	 */
	private AxisOperation copyOperation(AxisOperation axisOperation)
			throws AxisFault {
		AxisOperation operation = AxisOperationFactory
				.getOperationDescription(axisOperation
						.getMessageExchangePattern());

		operation.setMessageReceiver(axisOperation.getMessageReceiver());
		operation.setName(axisOperation.getName());

		Iterator<Parameter> parameters = axisOperation.getParameters().iterator();

		while (parameters.hasNext()) {
			Parameter parameter = (Parameter) parameters.next();

			operation.addParameter(parameter);
		}

		PolicyInclude policyInclude = new PolicyInclude(operation);
		PolicyInclude axisOperationPolicyInclude = axisOperation
				.getPolicyInclude();

		if (axisOperationPolicyInclude != null) {
			Policy policy = axisOperationPolicyInclude.getPolicy();
			if (policy != null) {
				policyInclude.setPolicy(axisOperationPolicyInclude.getPolicy());
			}
		}
		operation.setPolicyInclude(policyInclude);

		operation.setWsamappingList(axisOperation.getWSAMappingList());
		operation.setRemainingPhasesInFlow(axisOperation
				.getRemainingPhasesInFlow());
		operation.setPhasesInFaultFlow(axisOperation.getPhasesInFaultFlow());
		operation.setPhasesOutFaultFlow(axisOperation.getPhasesOutFaultFlow());
		operation.setPhasesOutFlow(axisOperation.getPhasesOutFlow());

		operation.setOutputAction(axisOperation.getOutputAction());
		String[] faultActionNames = axisOperation.getFaultActionNames();
		for (int i = 0; i < faultActionNames.length; i++) {
			operation.addFaultAction(faultActionNames[i], axisOperation
					.getFaultAction(faultActionNames[i]));
		}

		return operation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.description.AxisService#addToengagedModules(javax.xml.namespace.QName)
	 */

	/**
	 * Engages a module. It is required to use this method.
	 * 
	 * @param axisModule
	 * @param engager
	 */
	public void onEngage(AxisModule axisModule, AxisDescription engager)
			throws AxisFault {
		// adding module operations
		addModuleOperations(axisModule);

		Iterator<AxisOperation> operations = getOperations();
		while (operations.hasNext()) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			axisOperation.engageModule(axisModule, engager);
		}
	}

	/**
	 * Maps an alias (such as a SOAPAction, WSA action, or an operation name) to
	 * the given AxisOperation. This is used by dispatching (both SOAPAction-
	 * and WSAddressing- based dispatching) to figure out which operation a
	 * given message is for. Some notes on restrictions of "action" - A null or
	 * empty action will be ignored - An action that is a duplicate and
	 * references an idential operation is allowed - An acton that is a
	 * duplicate and references a different operation is NOT allowed. In this
	 * case, the action for the original operation is removed from the alias
	 * table, thus removing the ability to route based on this action. This is
	 * necessary to prevent mis-directing incoming message to the wrong
	 * operation based on SOAPAction.
	 * 
	 * Note that an alias could be a SOAPAction, WS-Addressing Action, the
	 * operation name, or some other alias.
	 * 
	 * @see #getOperationByAction(String)
	 * 
	 * @param action
	 *            the alias key
	 * @param axisOperation
	 *            the operation to map to
	 */
	public void mapActionToOperation(String action, AxisOperation axisOperation) {
		if (action == null || "".equals(action)) {
			if (log.isDebugEnabled()) {
				log
						.debug("mapActionToOperation: A null or empty action cannot be used to map to an operation.");
			}
			return;
		}
		if (log.isDebugEnabled()) {
			log
					.debug("mapActionToOperation: Mapping Action to Operation: action: "
							+ action
							+ "; operation: "
							+ axisOperation
							+ "named: " + axisOperation.getName());
              log.debug(JavaUtils.callStackToString());
		}
                      
		//If there is already an operation with this action
		//mapping then we're going to check to see if the
		//operation says that it's OK to be overridden and
		//if so, we'll simply ignore the mapping, otherwise
		//we continue as before
		AxisOperation mappedOperation = getOperationByAction(action);
		if ((mappedOperation != null)
		    && (axisOperation.isParameterTrue(DeploymentConstants.TAG_ALLOWOVERRIDE))) {
		  if (log.isDebugEnabled()) {
		    log
		    .debug("addModuleOperations: Mapping already exists for action: "
		           + action
		           + " to operation: "
		           + axisOperation
		           + " named: "
		           + axisOperation.getName()
		           + " and an override is allowed, so the mapping is being ignored.");
		    log.debug(JavaUtils.callStackToString());
		  }
		  return;		
		}

		// First check if this action has already been flagged as invalid
		// because it is a duplicate.
		if (invalidOperationsAliases.contains(action)) {
			// This SOAPAction has already been determined to be invalid; log a
			// message
			// and do not add it to the operation alias map.
			if (log.isDebugEnabled()) {
				log
						.debug("mapActionToOperation: The action: "
								+ action
								+ " can not be used for operation: "
								+ axisOperation
								+ " with operation name: "
								+ axisOperation.getName()
								+ " because that SOAPAction is not unique for this service.");
			}
			return;
		}

		// Check if the action is currently mapping to an operation.
		AxisOperation currentlyMappedOperation = getOperationByAction(action);
		if (currentlyMappedOperation != null) {
			if (currentlyMappedOperation == axisOperation) {
				// This maps to the same operation, then it is already in the
				// alias table, so
				// just silently ignore this mapping request.
				if (log.isDebugEnabled()) {
					log
							.debug("mapActionToOperation: This operation is already mapped to this action: "
									+ action
									+ "; AxisOperation: "
									+ currentlyMappedOperation
									+ " named: "
									+ currentlyMappedOperation.getName());
				}
			} else {
				// This action is already mapped, but it is to a different
				// operation. Remove
				// the action mapping from the alias table and add it to the
				// list of invalid mappings
				operationsAliasesMap.remove(action);
				invalidOperationsAliases.add(action);
				if (log.isDebugEnabled()) {
					log
							.debug("mapActionToOperation: The action is already mapped to a different "
									+ "operation.  The mapping of the action to any operations will be "
									+ "removed.  Action: "
									+ action
									+ "; original operation: "
									+ currentlyMappedOperation
									+ " named "
									+ currentlyMappedOperation.getName()
									+ "; new operation: "
									+ axisOperation
									+ " named " + axisOperation.getName());
				}
			}
		} else {
			operationsAliasesMap.put(action, axisOperation);
			// Adding operation name to the mapping table
			// operationsAliasesMap.put(axisOperation.getName().getLocalPart(),
			// axisOperation);
		}
	}

	/**
	 * Maps an constant string in the whttp:location to the given operation.
	 * This is used by RequestURIOperationDispatcher based dispatching to figure
	 * out which operation it is that a given message is for.
	 * 
	 * @param string
	 *            the constant drawn from whttp:location
	 * @param axisOperation
	 *            the operation to map to
	 */
	public void addHttpLocationDispatcherString(String string,
			AxisOperation axisOperation) {
		httpLocationDispatcherMap.put(string, axisOperation);
	}

	/**
	 * Prints the schema to the given output stream.
	 * @param out The output stream for the data to be written. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @throws AxisFault
	 */
	public void printSchema(OutputStream out) throws AxisFault {
		for (int i = 0; i < schemaList.size(); i++) {
			XmlSchema schema = addNameSpaces(i);
			schema.write(out);
		}
	}

	public XmlSchema getSchema(int index) {
		return addNameSpaces(index);
	}

	/**
	 * Release the list of schema objects. <p/> In some environments, this can
	 * provide significant relief of memory consumption in the java heap, as
	 * long as the need for the schema list has completed.
	 */
	public void releaseSchemaList() {
		if (schemaList != null) {
			// release the schema list
			schemaList.clear();
		}

		if (log.isDebugEnabled()) {
			log.debug("releaseSchemaList: schema list has been released.");
		}
	}

	private XmlSchema addNameSpaces(int i) {
		XmlSchema schema = (XmlSchema) schemaList.get(i);
		NamespaceMap map = (NamespaceMap) namespaceMap.clone();
		NamespacePrefixList namespaceContext = schema.getNamespaceContext();
		String prefixes[] = namespaceContext.getDeclaredPrefixes();
		for (int j = 0; j < prefixes.length; j++) {
			String prefix = prefixes[j];
			map.add(prefix, namespaceContext.getNamespaceURI(prefix));
		}
		schema.setNamespaceContext(map);
		return schema;
	}

	public void setEPRs(String[] eprs) {
		this.eprs = eprs;
	}

	public String[] getEPRs() {
		if (eprs != null && eprs.length != 0) {
			return eprs;
		}
		eprs = calculateEPRs();
		return eprs;
	}

	private String[] calculateEPRs() {
		try {
			String requestIP = org.apache.axis2.util.Utils.getIpAddress(getAxisConfiguration());
			return calculateEPRs(requestIP);
		} catch (SocketException e) {
			log.error("Cannot get local IP address", e);
		}
		return new String[0];
	}

	private String[] calculateEPRs(String requestIP) {
		AxisConfiguration axisConfig = getAxisConfiguration();
		if (axisConfig == null) {
			return null;
		}
		ArrayList<String> eprList = new ArrayList<String>();
		if (enableAllTransports) {
			for (Iterator<TransportInDescription> transports = axisConfig.getTransportsIn().values()
					.iterator(); transports.hasNext();) {
				TransportInDescription transportIn = (TransportInDescription) transports
						.next();
				TransportListener listener = transportIn.getReceiver();
				if (listener != null) {
					try {
						EndpointReference[] eprsForService = listener
								.getEPRsForService(this.name, requestIP);
						if (eprsForService != null) {
							for (int i = 0; i < eprsForService.length; i++) {
								EndpointReference endpointReference = eprsForService[i];
								if (endpointReference != null) {
									String address = endpointReference
											.getAddress();
									if (address != null) {
										eprList.add(address);
									}
								}
							}
						}
					} catch (AxisFault axisFault) {
						log.warn(axisFault.getMessage());
					}
				}
			}
		} else {
			List<String> trs = this.exposedTransports;
			for (int i = 0; i < trs.size(); i++) {
				String trsName = (String) trs.get(i);
				TransportInDescription transportIn = axisConfig
						.getTransportIn(trsName);
				if (transportIn != null) {
					TransportListener listener = transportIn.getReceiver();
					if (listener != null) {
						try {
							EndpointReference[] eprsForService = listener
									.getEPRsForService(this.name, requestIP);
							if (eprsForService != null) {
								for (int j = 0; j < eprsForService.length; j++) {
									EndpointReference endpointReference = eprsForService[j];
									if (endpointReference != null) {
										String address = endpointReference
												.getAddress();
										if (address != null) {
											eprList.add(address);
										}
									}
								}
							}
						} catch (AxisFault axisFault) {
							log.warn(axisFault.getMessage());
						}
					}
				}
			}
		}
		eprs = (String[]) eprList.toArray(new String[eprList.size()]);
		return eprs;
	}

	/**
	 * Prints the given definition object.
	 * @param definition The definition.
	 * @param out The output stream the data to be written to. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param requestIP The host IP address.
	 * @throws AxisFault
	 * @throws WSDLException
	 */
	private synchronized void printDefinitionObject(Definition definition, OutputStream out,
			String requestIP) throws AxisFault, WSDLException {
        // Synchronized this method to fix the NullPointer exception occurred when load is high.
        // This error happens because wsdl4j is not thread safe and we are using same WSDL Definition for printing the
        // WSDL.
        // Please refer AXIS2-4511,AXIS2-4517,AXIS2-3276.
		if (isModifyUserWSDLPortAddress()) {
			setPortAddress(definition, requestIP);
		}
		if (!wsdlImportLocationAdjusted) {
			changeImportAndIncludeLocations(definition);
			wsdlImportLocationAdjusted = true;
		}
		WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
		writer.writeWSDL(definition, out);
	}

	public void printUserWSDL(OutputStream out, String wsdlName)
			throws AxisFault {
			printUserWSDL(out, wsdlName, null);
	}

	/**
	 * Prints the user WSDL.
	 * @param out The output stream for the data to be written. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param wsdlName The name of the WSDL.
	 * @param ip The host IP address.
	 * @throws AxisFault
	 */
	public void printUserWSDL(OutputStream out, String wsdlName, String ip)
			throws AxisFault {
		Definition definition = null;
		// first find the correct wsdl definition
		Parameter wsdlParameter = getParameter(WSDLConstants.WSDL_4_J_DEFINITION);
		if (wsdlParameter != null) {
			definition = (Definition) wsdlParameter.getValue();
		}

		if (definition != null) {
			try {
				printDefinitionObject(getWSDLDefinition(definition, wsdlName),
						out, ip);
			} catch (WSDLException e) {
				throw AxisFault.makeFault(e);
			}
		} else {
			printWSDLError(out);
		}

	}

	/**
	 * find the defintion object for given name
	 * 
	 * @param parentDefinition
	 * @param name
	 * @return wsdl definition
	 */
	private Definition getWSDLDefinition(Definition parentDefinition,
			String name) {

		if (name == null)
			return parentDefinition;

		Definition importedDefinition = null;
		Iterator iter = parentDefinition.getImports().values().iterator();
		Vector values = null;
		Import wsdlImport = null;
		for (; iter.hasNext();) {
			values = (Vector) iter.next();
			for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
				wsdlImport = (Import) valuesIter.next();
				if (wsdlImport.getLocationURI().endsWith(name)) {
					importedDefinition = wsdlImport.getDefinition();
					break;
				} else {
					importedDefinition = getWSDLDefinition(wsdlImport
							.getDefinition(), name);
				}
				if (importedDefinition != null) {
					break;
				}
			}
			if (importedDefinition != null) {
				break;
			}
		}
		return importedDefinition;
	}

	/**
	 * this procesdue recursively adjust the wsdl imports locations and the
	 * schmea import and include locations.
	 * 
	 * @param definition
	 */
	private void changeImportAndIncludeLocations(Definition definition) throws AxisFault {

        // adjust the schema locations in types section
        Types types = definition.getTypes();
        if (types != null) {
            List extensibilityElements = types.getExtensibilityElements();
            Object extensibilityElement = null;
            Schema schema = null;
            for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
                extensibilityElement = iter.next();
                if (extensibilityElement instanceof Schema) {
                    schema = (Schema) extensibilityElement;
                    changeLocations(schema.getElement());
                }
            }
        }

        Iterator iter = definition.getImports().values().iterator();
        Vector values = null;
        Import wsdlImport = null;
        String originalImprotString = null;
        for (; iter.hasNext();) {
            values = (Vector) iter.next();
            for (Iterator valuesIter = values.iterator(); valuesIter.hasNext();) {
                wsdlImport = (Import) valuesIter.next();
                originalImprotString = wsdlImport.getLocationURI();
                if (originalImprotString.indexOf("://") == -1 && originalImprotString.indexOf("?wsdl=") == -1){
                    wsdlImport.setLocationURI(this.getServiceEPR() + "?wsdl=" + originalImprotString);
                }
                changeImportAndIncludeLocations(wsdlImport.getDefinition());
            }
        }

    }

	/**
	 * change the schema Location in the elemment
	 * 
	 * @param element
	 */

	private void changeLocations(Element element) throws AxisFault {
        NodeList nodeList = element.getChildNodes();
        String tagName;
        for (int i = 0; i < nodeList.getLength(); i++) {
            tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i));
            }
        }
    }

	private void updateSchemaLocation(XmlSchema schema) throws AxisFault {
        XmlSchemaObjectCollection includes = schema.getIncludes();
        for (int j = 0; j < includes.getCount(); j++) {
            Object item = includes.getItem(j);
            if (item instanceof XmlSchemaExternal) {
                XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal) item;
                XmlSchema s = xmlSchemaExternal.getSchema();
                updateSchemaLocation(s, xmlSchemaExternal);
            }
        }
    }
	   
	private void updateSchemaLocation(XmlSchema s, XmlSchemaExternal xmlSchemaExternal) throws AxisFault {
        if (s != null) {
            String schemaLocation = xmlSchemaExternal.getSchemaLocation();

            if (schemaLocation.indexOf("://") == -1 && schemaLocation.indexOf("?xsd=") == -1) {
                String newscheamlocation = this.getServiceEPR() + "?xsd=" + schemaLocation;
                xmlSchemaExternal.setSchemaLocation(newscheamlocation);
            }
        }
    }

    private void processImport(Node importNode) throws AxisFault {
        NamedNodeMap nodeMap = importNode.getAttributes();
        Node attribute;
        String attributeValue;
        for (int i = 0; i < nodeMap.getLength(); i++) {
            attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals(SCHEMA_LOCATION)) {
                attributeValue = attribute.getNodeValue();
                if (getParameter(Constants.CUSTOM_SCHEMA_NAME_PREFIX) != null) {
                    String customSchemaNamePrefix = getParameter(Constants.CUSTOM_SCHEMA_NAME_PREFIX).
                            getValue().toString();
                    URL customUrl = null;
                    try {
                        URL base = new URL(customSchemaNamePrefix);
                        customUrl = new URL(base, attributeValue);
                    } catch (MalformedURLException e) {
                        log.error("Malformed schema URL: " + customUrl, e);
                        throw AxisFault.makeFault(e);
                    }
                    attribute.setNodeValue(customUrl.toString());
                } else if (attributeValue.indexOf("://") == -1 && attributeValue.indexOf("?xsd=") == -1) {
                    attribute.setNodeValue(this.getServiceEPR() + "?xsd=" + attributeValue);
                }
            }
        }
    }

    private String getServiceEPR() {
        String serviceEPR = null;
        boolean fromServiceName = false;
        Parameter parameter = this.getParameter(Constants.Configuration.GENERATE_ABSOLUTE_LOCATION_URIS);
        if ((parameter != null) && JavaUtils.isTrueExplicitly(parameter.getValue())) {
            String[] eprs = this.getEPRs();
            for (int i = 0; i < eprs.length; i++) {
                if ((eprs[i] != null) && (eprs[i].startsWith("http:"))){
                    serviceEPR = eprs[i];
                    break;
                }
            }
            if (serviceEPR == null){
                serviceEPR = eprs[0];
            }
        } else {
            serviceEPR = this.name;
            fromServiceName = true;
        }
        if (serviceEPR.endsWith("/")){
            serviceEPR = serviceEPR.substring(0, serviceEPR.lastIndexOf("/"));
        }
        // if the service is a hierarchical service, we have to drop the hierarchical path
        if (fromServiceName && serviceEPR.indexOf('/') != -1) {
            serviceEPR = serviceEPR.substring(serviceEPR.lastIndexOf('/') + 1);
        }
        return serviceEPR;
    }

    /**
	 * Produces a XSD for this AxisService and prints it to the specified
	 * OutputStream.
	 * 
	 * @param out
	 *            destination stream, NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param xsd
	 *            schema name
	 * @return -1 implies not found, 0 implies redirect to root, 1 implies
	 *         found/printed a schema
	 * @throws IOException
	 */
	public int printXSD(OutputStream out, String xsd) throws IOException {

		// If we find a SchemaSupplier, use that
		SchemaSupplier supplier = (SchemaSupplier) getParameterValue("SchemaSupplier");
		if (supplier != null) {
			XmlSchema schema = supplier.getSchema(this, xsd);
			if (schema != null) {
			    updateSchemaLocation(schema);
				schema.write(new OutputStreamWriter(out, "UTF8"));
				out.flush();
				return 1;
			}
		}

		// call the populator
		populateSchemaMappings();
		Map schemaMappingtable = getSchemaMappingTable();
		ArrayList<XmlSchema> schemas = getSchema();

		// a name is present - try to pump the requested schema
        if ((xsd != null) && (!"".equals(xsd))) {
            XmlSchema schema = (XmlSchema) schemaMappingtable.get(xsd);
            if (schema == null) {
                int dotIndex = xsd.indexOf('.');
                if (dotIndex > 0) {
                    String schemaKey = xsd.substring(0, dotIndex);
                    schema = (XmlSchema) schemaMappingtable.get(schemaKey);
                }
            }
            if (schema != null) {
                // schema is there - pump it outs
                schema.write(new OutputStreamWriter(out, "UTF8"));
                out.flush();
            } else {
                // make sure we are only serving .xsd files and ignore requests with
                // ".." in the name.
                if (xsd.endsWith(".xsd") && xsd.indexOf("..") == -1) {
                    InputStream in = getClassLoader().getResourceAsStream(
                            DeploymentConstants.META_INF + "/" + xsd);
                    if (in != null) {
                        IOUtils.copy(in, out, true);
                    } else {
                        // Can't find the schema
                        return -1;
                    }
                } else {
                    // bad schema request
                    return -1;
                }
            }
		} else if (schemas.size() > 1) {
			// multiple schemas are present and the user specified
			// no name - in this case we cannot possibly pump a schema
			// so redirect to the service root
			return 0;
		} else {
			// user specified no name and there is only one schema
			// so pump that out
			ArrayList<XmlSchema> list = getSchema();
			if (list.size() > 0) {
				XmlSchema schema = getSchema(0);
				if (schema != null) {
					schema.write(new OutputStreamWriter(out, "UTF8"));
					out.flush();
				}
			} else {
				String xsdNotFound = "<error>"
						+ "<description>Unable to access schema for this service</description>"
						+ "</error>";
				out.write(xsdNotFound.getBytes());
				out.flush();
			}
		}
		return 1;
	}

	/**
	 * Produces a WSDL for this AxisService and prints it to the specified
	 * OutputStream.
	 * 
	 * @param out
	 *            destination stream. The WSDL will be sent here. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param requestIP
	 *            the hostname the WSDL request was directed at. This should be
	 *            the address that appears in the generated WSDL.
	 * @throws AxisFault
	 *             if an error occurs
	 */
	public void printWSDL(OutputStream out, String requestIP) throws AxisFault {
		// If we're looking for pre-existing WSDL, use that.
		if (isUseUserWSDL()) {
			printUserWSDL(out, null, requestIP);
			return;
		}

		// If we find a WSDLSupplier, use that
		WSDLSupplier supplier = (WSDLSupplier) getParameterValue("WSDLSupplier");
		if (supplier != null) {
			try {
				Definition definition = supplier.getWSDL(this);
				if (definition != null) {
				    changeImportAndIncludeLocations(definition);
                    printDefinitionObject(getWSDLDefinition(definition, null),
							out, requestIP);
				}
			} catch (Exception e) {
				printWSDLError(out, e);
			}
			return;
		}

        if (isSetEndpointsToAllUsedBindings()) {
            Utils.setEndpointsToAllUsedBindings(this);
        }

		// Otherwise, generate WSDL ourselves
		String[] eprArray = requestIP == null ? new String[] { this.endpointName }
				: calculateEPRs(requestIP);
		getWSDL(out, eprArray);
	}

    /**
     * users can use this parameter when they supply a wsdl file with the .aar file
     * but wants to generate the endpoints for all available transports. here it assume
     * that the user has not set the useOriginalwsdl
     * @return
     */
	public boolean isSetEndpointsToAllUsedBindings() {
		Parameter parameter = getParameter("setEndpointsToAllUsedBindings");
		if (parameter != null) {
			String value = (String) parameter.getValue();
			if ("true".equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Print the WSDL with a default URL. This will be called only during
	 * codegen time.
	 * 
	 * @param out The output stream for the data to be written. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @throws AxisFault
	 */
	public void printWSDL(OutputStream out) throws AxisFault {
		printWSDL(out, null);
	}

        private AxisEndpoint getAxisEndpoint(String port) {
            // if service has a single endpoint, this will cause the [serviceName] address
            // to be used in wsdl instead of the [serviceName].[endpointName]
            if (endpointMap.size() == 1 && endpointMap.containsKey(getEndpointName())) {
                return null;
            } else {
                return (AxisEndpoint)endpointMap.get(port);
            }
        }

	private void setPortAddress(Definition definition, String requestIP)
			throws AxisFault {
		Iterator serviceItr = definition.getServices().values().iterator();
		while (serviceItr.hasNext()) {
			Service serviceElement = (Service) serviceItr.next();
			Iterator portItr = serviceElement.getPorts().values().iterator();
			while (portItr.hasNext()) {
				Port port = (Port) portItr.next();
				AxisEndpoint endpoint = getAxisEndpoint(port.getName());
				List list = port.getExtensibilityElements();
				for (int i = 0; i < list.size(); i++) {
					Object extensibilityEle = list.get(i);
					if (extensibilityEle instanceof SOAPAddress) {
						SOAPAddress soapAddress = (SOAPAddress) extensibilityEle;
						String existingAddress = soapAddress.getLocationURI();
						if (existingAddress == null
								|| existingAddress
										.equals("REPLACE_WITH_ACTUAL_URL")) {
							if (endpoint != null) {
								((SOAPAddress) extensibilityEle)
										.setLocationURI(endpoint
												.calculateEndpointURL(requestIP));
							} else {
								((SOAPAddress) extensibilityEle)
										.setLocationURI(getEPRs()[0]);
							}
						} else {
							if (requestIP == null) {
								if (endpoint != null) {
									((SOAPAddress) extensibilityEle)
											.setLocationURI(endpoint
													.calculateEndpointURL());
								} else {
									((SOAPAddress) extensibilityEle)
											.setLocationURI(getLocationURI(
													getEPRs(), existingAddress));
								}
							} else {
								if (endpoint != null) {
									((SOAPAddress) extensibilityEle)
											.setLocationURI(endpoint
													.calculateEndpointURL(requestIP));
								} else {
									((SOAPAddress) extensibilityEle)
											.setLocationURI(getLocationURI(
													calculateEPRs(requestIP),
													existingAddress));
								}
							}
						}
					} else if (extensibilityEle instanceof SOAP12Address) {
						SOAP12Address soapAddress = (SOAP12Address) extensibilityEle;
						String exsistingAddress = soapAddress.getLocationURI();
						if (requestIP == null) {
							if (endpoint != null) {
								((SOAP12Address) extensibilityEle)
										.setLocationURI(endpoint
												.calculateEndpointURL());

							} else {
								((SOAP12Address) extensibilityEle)
										.setLocationURI(getLocationURI(
												getEPRs(), exsistingAddress));
							}
						} else {
							if (endpoint != null) {
								((SOAP12Address) extensibilityEle)
										.setLocationURI(endpoint
												.calculateEndpointURL(requestIP));
							} else {
								((SOAP12Address) extensibilityEle)
										.setLocationURI(getLocationURI(
												calculateEPRs(requestIP),
												exsistingAddress));

							}
						}
					} else if (extensibilityEle instanceof HTTPAddress) {
						HTTPAddress httpAddress = (HTTPAddress) extensibilityEle;
						String exsistingAddress = httpAddress.getLocationURI();
						if (requestIP == null) {
							if (endpoint != null) {
								((HTTPAddress) extensibilityEle)
										.setLocationURI(endpoint
												.calculateEndpointURL());
							} else {
								((HTTPAddress) extensibilityEle)
										.setLocationURI(getLocationURI(
												getEPRs(), exsistingAddress));
							}
						} else {
							if (endpoint != null) {
								((HTTPAddress) extensibilityEle)
										.setLocationURI(endpoint
												.calculateEndpointURL(requestIP));
							} else {
								((HTTPAddress) extensibilityEle)
										.setLocationURI(getLocationURI(
												calculateEPRs(requestIP),
												exsistingAddress));
							}
						}
					} else if (extensibilityEle instanceof UnknownExtensibilityElement){
                        UnknownExtensibilityElement unknownExtensibilityElement = (UnknownExtensibilityElement) extensibilityEle;
                        Element element = unknownExtensibilityElement.getElement();
                        if (AddressingConstants.ENDPOINT_REFERENCE.equals(element.getLocalName())){
                            NodeList nodeList = element.getChildNodes();
                            Node node = null;
                            Element currentElement = null;
                            for (int j = 0; j < nodeList.getLength(); j++) {
                                node = nodeList.item(j);
                                if (node instanceof Element){
                                    currentElement = (Element) node;
                                    if (AddressingConstants.EPR_ADDRESS.equals(currentElement.getLocalName())) {
                                        String exsistingAddress = currentElement.getTextContent();
                                        if (requestIP == null) {
                                            if (endpoint != null) {
                                                currentElement.setTextContent(endpoint.calculateEndpointURL());
                                            } else {
                                                currentElement.setTextContent(getLocationURI(getEPRs(), exsistingAddress));
                                            }
                                        } else {
                                            if (endpoint != null) {
                                                currentElement.setTextContent(endpoint.calculateEndpointURL(requestIP));
                                            } else {
                                                currentElement.setTextContent(getLocationURI(calculateEPRs(requestIP),exsistingAddress));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
				}
			}
		}
	}

	/**
	 * this method returns the new IP address corresponding to the already
	 * existing ip
	 * 
	 * @param eprs
	 * @param epr
	 * @return corresponding Ip address
	 */
	private String getLocationURI(String[] eprs, String epr) throws AxisFault {
		String returnIP = null;
		if (epr != null) {
		    String existingProtocol = org.apache.axis2.util.Utils.getURIScheme(epr);
			if (existingProtocol != null) {
				for (int i = 0; i < eprs.length; i++) {
					if (existingProtocol.equals(org.apache.axis2.util.Utils.getURIScheme(eprs[i]))) {
						returnIP = eprs[i];
						break;
					}
				}
				if (returnIP != null) {
					return returnIP;
				} else {
					throw new AxisFault(
							"Server does not have an epr for the wsdl epr==>"
									+ epr);
				}
			} else {
				throw new AxisFault("invalid epr is given epr ==> " + epr);
			}
		} else {
			throw new AxisFault("No epr is given in the wsdl port");
		}
	}

	/**
	 * Retrieves the WSDL data associated with the given serviceURL.
	 * @param out The output stream for the WSDL data to be written, NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param serviceURL The fist element of this array i.e. serviceURL[0] is taken in retrieving the target service.
	 */
	private void getWSDL(OutputStream out, String[] serviceURL)
			throws AxisFault {
		// Retrieve WSDL using the same data retrieval path for GetMetadata
		// request.
		DataRetrievalRequest request = new DataRetrievalRequest();
		request.putDialect(DRConstants.SPEC.DIALECT_TYPE_WSDL);
		request.putOutputForm(OutputForm.INLINE_FORM);

		MessageContext context = new MessageContext();
		context.setAxisService(this);
		context.setTo(new EndpointReference(serviceURL[0]));

		Data[] result = getData(request, context);
		OMElement wsdlElement;
		if (result != null && result.length > 0) {
			wsdlElement = (OMElement) (result[0].getData());
			try {
				XMLPrettyPrinter.prettify(wsdlElement, out);
				out.flush();
			} catch (Exception e) {
				throw AxisFault.makeFault(e);
			}
		}
	}

	/**
	 * Prints generic WSDL error to the given output stream.
	 * @param out The output stream the data to be written to. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @throws AxisFault
	 */
	private void printWSDLError(OutputStream out) throws AxisFault {
		printWSDLError(out, null);
	}

	/**
	 * Prints WSDL error condition that is given in the exception.
	 * @param out The output stream for the error message to be written. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param e The exception describing the error condition.
	 * @throws AxisFault
	 */
	private void printWSDLError(OutputStream out, Exception e) throws AxisFault {
		try {
			String wsdlntfound = "<error>"
					+ "<description>Unable to generate WSDL 1.1 for this service</description>"
					+ "<reason>If you wish Axis2 to automatically generate the WSDL 1.1, then please "
					+ "set useOriginalwsdl as false in your services.xml</reason>";
			out.write(wsdlntfound.getBytes());
			if (e != null) {
				PrintWriter pw = new PrintWriter(out);
				e.printStackTrace(pw);
				pw.flush();
			}
			out.write("</error>".getBytes());
			out.flush();
		} catch (IOException ex) {
			throw AxisFault.makeFault(ex);
		}
	}

	/**
	 * Print the WSDL2.0 with a default URL. This will be called only during
	 * codegen time.
	 * 
	 * @param out The output stream for the data to be written for. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @throws AxisFault
	 */
	public void printWSDL2(OutputStream out) throws AxisFault {
		printWSDL2(out, null);
	}

	/**
	 * Prints WSDL2.0 data for the service with the given host IP address.
	 * @param out The output stream for the data to be written for. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
	 * @param requestIP The host IP address.
	 * @throws AxisFault
	 */
	public void printWSDL2(OutputStream out, String requestIP) throws AxisFault {
		AxisService2WSDL20 axisService2WSDL2 = new AxisService2WSDL20(this);
		try {
			if (requestIP != null) {
				axisService2WSDL2.setEPRs(calculateEPRs(requestIP));
			}
			OMElement wsdlElement = axisService2WSDL2.generateOM();
			wsdlElement.serialize(out);
			out.flush();
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}
	}

    /**
     * Produces a WSDL2 for this AxisService and prints it to the specified
     * OutputStream.
     * 
     * @param out
     *            destination stream. NOTE: the stream is not closed after the operation, 
	 *            it is the responsibility of the caller to close the stream after usage.
     * @param wsdl
     *            wsdl name
     * @return -1 implies not found, 0 implies redirect to root, 1 implies
     *         found/printed wsdl
     * @throws IOException
     */
    public int printWSDL2(OutputStream out, String requestIP, String wsdl) 
        throws IOException, AxisFault {    
        // if the wsdl2 parameter is not empty or null in the requested URL, get the wsdl  from the META-INF and serve.
        //else construct the wsdl out of axis service and serve.
        if ((wsdl != null ) && (!"".equals(wsdl))) {
            // make sure we are only serving .wsdl files and ignore requests with
            // ".." in the name.
            if (wsdl.endsWith(".wsdl") && wsdl.indexOf("..") == -1) {
                InputStream in = getClassLoader().getResourceAsStream(
                                    DeploymentConstants.META_INF + "/" + wsdl);
                if (in != null) {
                    IOUtils.copy(in, out, true);
                } else {
                    // can't find the wsdl
                    return -1;
                }
            } else {
                // bad wsdl2 request
                return -1;
            }
        } else {
            printWSDL2(out, requestIP);
        }
        
        return 1;
    }
    
	/**
	 * Gets the description about the service which is specified in
	 * services.xml.
	 * 
	 * @return Returns String.
	 * @deprecated Use getDocumentation() instead
	 */
	public String getServiceDescription() {
		return getDocumentation();
	}

	/**
	 * Method getClassLoader.
	 * 
	 * @return Returns ClassLoader.
	 */
	public ClassLoader getClassLoader() {
		return this.serviceClassLoader;
	}

	/**
	 * Gets the control operation which are added by module like RM.
	 */
	public ArrayList<AxisOperation> getControlOperations() {
		Iterator<AxisOperation> op_itr = getOperations();
		ArrayList<AxisOperation> operationList = new ArrayList<AxisOperation>();

		while (op_itr.hasNext()) {
			AxisOperation operation = (AxisOperation) op_itr.next();

			if (operation.isControlOperation()) {
				operationList.add(operation);
			}
		}

		return operationList;
	}

	public URL getFileName() {
		return fileName;
	}

    /**
     * @deprecated please use getLastUpdate
     * @return
     */
    public long getLastupdate() {
		return lastupdate;
	}

    public long getLastUpdate() {
        return lastupdate;
    }

	public ModuleConfiguration getModuleConfig(String moduleName) {
		return (ModuleConfiguration) moduleConfigmap.get(moduleName);
	}

	public ArrayList<String> getModules() {
		return moduleRefs;
	}

	public String getName() {
		return name;
	}

	/**
	 * Method getOperation.
	 * 
	 * @param operationName
	 * @return Returns AxisOperation.
	 */
	public AxisOperation getOperation(QName operationName) {
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
            log.debug("Get operation for " + operationName);
                
		AxisOperation axisOperation = (AxisOperation) getChild(operationName);
		
        if (axisOperation == null) {
			axisOperation = (AxisOperation) getChild(new QName(
					getTargetNamespace(), operationName.getLocalPart()));
            
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                log.debug("Target namespace: " + getTargetNamespace());
		}
        
		if (axisOperation == null) {
			axisOperation = (AxisOperation) operationsAliasesMap
					.get(operationName.getLocalPart());
            
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                log.debug("Operations aliases map: " + operationsAliasesMap);
		}
        
        //The operation may be associated with a namespace other than the
        //target namespace, e.g. if the operation is from an imported wsdl.
        if (axisOperation == null) {            
            List namespaces = getImportedNamespaces();
            
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
                log.debug("Imported namespaces: " + namespaces);

            if (namespaces != null) {
                Iterator iterator = namespaces.iterator();
                
                while (iterator.hasNext()) {
                    String namespace = (String) iterator.next();
                    axisOperation = (AxisOperation) getChild(new QName(
                            namespace, operationName.getLocalPart()));
                    
                    if (axisOperation != null)
                        break;
                }
            }
        }

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled())
            log.debug("Found axis operation:  " + axisOperation);

		return axisOperation;
	}

	/**
	 * Returns the AxisOperation which has been mapped to the given alias.
	 * 
	 * @see #mapActionToOperation(String, AxisOperation)
	 * 
	 * @param action
	 *            the alias key
	 * @return Returns the corresponding AxisOperation or null if it isn't
	 *         found.
	 */
	public AxisOperation getOperationByAction(String action) {
		return (AxisOperation) operationsAliasesMap.get(action);
	}

	/**
	 * Returns the operation given a SOAP Action. This method should be called
	 * if only one Endpoint is defined for this Service. If more than one
	 * Endpoint exists, one of them will be picked. If more than one Operation
	 * is found with the given SOAP Action; null will be returned. If no
	 * particular Operation is found with the given SOAP Action; null will be
	 * returned. If the action is in the list of invaliad aliases, which means
	 * it did not uniquely identify an operation, a null will be returned.
	 * 
	 * @param soapAction
	 *            SOAP Action defined for the particular Operation
	 * @return Returns an AxisOperation if a unique Operation can be found with
	 *         the given SOAP Action otherwise will return null.
	 */
	public AxisOperation getOperationBySOAPAction(String soapAction) {

		// Check for illegal soapActions
		if ((soapAction == null) || soapAction.length() == 0) {
			if (log.isDebugEnabled()) {
				log.debug("getOperationBySOAPAction: " + soapAction
						+ " is null or ''. Returning null.");
			}
			return null;
		}

		// If the action maps to an alais that is not unique, then it can't be
		// used to map to
		// an operation.
		if (invalidOperationsAliases.contains(soapAction)) {
			if (log.isDebugEnabled()) {
				log.debug("getOperationBySOAPAction: " + soapAction
						+ " is an invalid operation alias. Returning null.");
			}
			return null;
		}

		// Get the operation from the action->operation map
		AxisOperation operation = (AxisOperation) operationsAliasesMap
				.get(soapAction);

		if (operation != null) {
			if (log.isDebugEnabled()) {
				log.debug("getOperationBySOAPAction: Operation (" + operation
						+ "," + operation.getName() + ") for soapAction: "
						+ soapAction + " found in action map.");
			}
			return operation;
		}

		// The final fallback is to check the operations for a matching name.

		Iterator children = getChildren();
		// I could not find any spec statement that explicitly forbids using a
		// short name in the SOAPAction header or wsa:Action element,
		// so I believe this to be valid. There may be customers using the
		// shortname as the SOAPAction in their client code that would
		// also require this support.
		while (children.hasNext() && (operation == null)) {
			AxisOperation op = (AxisOperation) children.next();
			if (op.getName().getLocalPart().equals(soapAction)) {
				operation = op;
			}
		}

		if (operation != null) {
			if (log.isDebugEnabled()) {
				log.debug("getOperationBySOAPAction: Operation (" + operation
						+ "," + operation.getName() + ") for soapAction: "
						+ soapAction + " found as child.");
			}
		}

		return operation;
	}

	/**
	 * Method getOperations.
	 * 
	 * @return Returns HashMap
	 */
	public Iterator<AxisOperation> getOperations() {
		return (Iterator<AxisOperation>) getChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.description.ParameterInclude#getParameter(java.lang.String)
	 */

	/**
	 * Gets only the published operations.
	 */
	public ArrayList<AxisOperation> getPublishedOperations() {
		Iterator<AxisOperation> op_itr = getOperations();
		ArrayList<AxisOperation> operationList = new ArrayList<AxisOperation>();

		while (op_itr.hasNext()) {
			AxisOperation operation = (AxisOperation) op_itr.next();

			if (!operation.isControlOperation()) {
				operationList.add(operation);
			}
		}

		return operationList;
	}

	/**
	 * Sets the description about the service which is specified in services.xml
	 * 
	 * @param documentation
	 * @deprecated Use setDocumentation() instead
	 */
	public void setServiceDescription(String documentation) {
		setDocumentation(documentation);
	}

	/**
	 * Method setClassLoader.
	 * 
	 * @param classLoader
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.serviceClassLoader = classLoader;
	}

	public void setFileName(URL fileName) {
		this.fileName = fileName;
	}

	/**
	 * Sets the current time as last update time of the service.
     * @deprecated please use setLastUpdate
	 */
	public void setLastupdate() {
		lastupdate = new Date().getTime();
	}

    /**
     * Sets the current time as last update time of the service.
     */
    public void setLastUpdate() {
        lastupdate = new Date().getTime();
    }

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<XmlSchema> getSchema() {
		return schemaList;
	}

	public void addSchema(XmlSchema schema) {
		if (schema != null) {
			schemaList.add(schema);
			if (schema.getTargetNamespace() != null) {
				addSchemaNameSpace(schema);
			}
		}
	}

	public void addSchema(Collection<XmlSchema> schemas) {
		Iterator<XmlSchema> iterator = schemas.iterator();
		while (iterator.hasNext()) {
			XmlSchema schema = (XmlSchema) iterator.next();
			schemaList.add(schema);
			addSchemaNameSpace(schema);
		}
	}

	public boolean isWsdlFound() {
		return wsdlFound;
	}

	public void setWsdlFound(boolean wsdlFound) {
		this.wsdlFound = wsdlFound;
	}

	public String getScope() {
		return scope;
	}

	/**
	 * @param scope -
	 *            Available scopes : Constants.SCOPE_APPLICATION
	 *            Constants.SCOPE_TRANSPORT_SESSION Constants.SCOPE_SOAP_SESSION
	 *            Constants.SCOPE_REQUEST.equals
	 */
	public void setScope(String scope) {
		if (Constants.SCOPE_APPLICATION.equals(scope)
				|| Constants.SCOPE_TRANSPORT_SESSION.equals(scope)
				|| Constants.SCOPE_SOAP_SESSION.equals(scope)
				|| Constants.SCOPE_REQUEST.equals(scope)) {
			this.scope = scope;
		}
	}

	public boolean isUseDefaultChains() {
		return useDefaultChains;
	}

	public void setUseDefaultChains(boolean useDefaultChains) {
		this.useDefaultChains = useDefaultChains;
	}

	public Object getKey() {
		return this.name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

    /**
     * @deprecated please use getSchemaTargetNamespace
     * @return
     */
    public String getSchematargetNamespace() {
        return schematargetNamespace;
    }

	public String getSchemaTargetNamespace() {
		return schematargetNamespace;
	}

	public void setSchemaTargetNamespace(String schematargetNamespace) {
		this.schematargetNamespace = schematargetNamespace;
	}

	public String getSchemaTargetNamespacePrefix() {
		return schematargetNamespacePrefix;
	}

    /**
     * @deprecated please use setSchemaTargetNamespacePrefix
     * @param schematargetNamespacePrefix
     */
    public void setSchematargetNamespacePrefix(
            String schematargetNamespacePrefix) {
        this.schematargetNamespacePrefix = schematargetNamespacePrefix;
    }

	public void setSchemaTargetNamespacePrefix(
			String schematargetNamespacePrefix) {
		this.schematargetNamespacePrefix = schematargetNamespacePrefix;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getTargetNamespacePrefix() {
		return targetNamespacePrefix;
	}

	public void setTargetNamespacePrefix(String targetNamespacePrefix) {
		this.targetNamespacePrefix = targetNamespacePrefix;
	}

	public XmlSchemaElement getSchemaElement(QName elementQName) {
		XmlSchemaElement element;
		for (int i = 0; i < schemaList.size(); i++) {
			XmlSchema schema = (XmlSchema) schemaList.get(i);
			if (schema != null) {
				element = schema.getElementByName(elementQName);
				if (element != null) {
					return element;
				}
			}
		}
		return null;
	}

	public boolean isEnableAllTransports() {
		return enableAllTransports;
	}

	/**
	 * To eneble service to be expose in all the transport
	 * 
	 * @param enableAllTransports
	 */
	public void setEnableAllTransports(boolean enableAllTransports) {
		this.enableAllTransports = enableAllTransports;
		eprs = calculateEPRs();
	}

	public List<String> getExposedTransports() {
		return this.exposedTransports;
	}

	public void setExposedTransports(List transports) {
		enableAllTransports = false;
		this.exposedTransports = transports;
		eprs = null; // Do not remove this. We need to force EPR
						// recalculation.
	}

	public void addExposedTransport(String transport) {
		enableAllTransports = false;
		if (!this.exposedTransports.contains(transport)) {
			this.exposedTransports.add(transport);
			try {
				eprs = calculateEPRs();
			} catch (Exception e) {
				eprs = null;
			}
		}
	}

	public void removeExposedTransport(String transport) {
		enableAllTransports = false;
		this.exposedTransports.remove(transport);
		try {
			eprs = calculateEPRs();
		} catch (Exception e) {
			eprs = null;
		}
	}

	public boolean isExposedTransport(String transport) {
		return exposedTransports.contains(transport);
	}

	public void onDisengage(AxisModule module) throws AxisFault {
		removeModuleOperations(module);
		for (Iterator operations = getChildren(); operations.hasNext();) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			axisOperation.disengageModule(module);
		}
		AxisConfiguration config = getAxisConfiguration();
		if (!config.isEngaged(module.getName())) {
			PhaseResolver phaseResolver = new PhaseResolver(config);
			phaseResolver.disengageModuleFromGlobalChains(module);
		}
	}

	/**
	 * Remove any operations which were added by a given module.
	 * 
	 * @param module
	 *            the module in question
	 */
	private void removeModuleOperations(AxisModule module) {
		HashMap<QName, AxisOperation> moduleOperations = module.getOperations();
		if (moduleOperations != null) {
			for (Iterator<AxisOperation> modOpsIter = moduleOperations.values().iterator(); modOpsIter
					.hasNext();) {
				AxisOperation operation = (AxisOperation) modOpsIter.next();
				removeOperation(operation.getName());
			}
		}
	}

	// #######################################################################################
	// APIs to create AxisService

	//

	/**
	 * To create a AxisService for a given WSDL and the created client is most
	 * suitable for client side invocation not for server side invocation. Since
	 * all the soap action and wsa action is added to operations
	 * 
	 * @param wsdlURL
	 *            location of the WSDL
	 * @param wsdlServiceName
	 *            name of the service to be invoke , if it is null then the
	 *            first one will be selected if there are more than one
	 * @param portName
	 *            name of the port , if there are more than one , if it is null
	 *            then the first one in the iterator will be selected
	 * @param options
	 *            Service client options, to set the target EPR
	 * @return AxisService , the created service will be return
	 */
	public static AxisService createClientSideAxisService(URL wsdlURL,
			QName wsdlServiceName, String portName, Options options)
			throws AxisFault {
		try {
			InputStream in = wsdlURL.openConnection().getInputStream();
			Document doc = XMLUtils.newDocument(in);
			WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
			reader.setFeature("javax.wsdl.importDocuments", true);
			Definition wsdlDefinition = reader.readWSDL(getBaseURI(wsdlURL
					.toString()), doc);
			if (wsdlDefinition != null) {
				wsdlDefinition.setDocumentBaseURI(getDocumentURI(wsdlURL
						.toString()));
			}
			return createClientSideAxisService(wsdlDefinition, wsdlServiceName,
					portName, options);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw AxisFault.makeFault(e);
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage(), e);
			throw AxisFault.makeFault(e);
		} catch (SAXException e) {
			log.error(e.getMessage(), e);
			throw AxisFault.makeFault(e);
		} catch (WSDLException e) {
			log.error(e.getMessage(), e);
			throw AxisFault.makeFault(e);
		}
	}

	private static String getBaseURI(String currentURI) {
		try {
			File file = new File(currentURI);
			if (file.exists()) {
				return file.getCanonicalFile().getParentFile().toURI()
						.toString();
			}
			String uriFragment = currentURI.substring(0, currentURI
					.lastIndexOf("/"));
			return uriFragment + (uriFragment.endsWith("/") ? "" : "/");
		} catch (IOException e) {
			return null;
		}
	}

	private static String getDocumentURI(String currentURI) {
		try {
			File file = new File(currentURI);
			return file.getCanonicalFile().toURI().toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static AxisService createClientSideAxisService(
			Definition wsdlDefinition, QName wsdlServiceName, String portName,
			Options options) throws AxisFault {
		WSDL11ToAxisServiceBuilder serviceBuilder = new WSDL11ToAxisServiceBuilder(
				wsdlDefinition, wsdlServiceName, portName);
		serviceBuilder.setServerSide(false);
		AxisService axisService = serviceBuilder.populateService();
		AxisEndpoint axisEndpoint = (AxisEndpoint) axisService.getEndpoints()
				.get(axisService.getEndpointName());

		if (axisEndpoint != null) {
            options.setTo(new EndpointReference(axisEndpoint.getEndpointURL()));
			options.setSoapVersionURI((String) axisEndpoint.getBinding()
					.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION));
		}
		return axisService;
	}

	/**
	 * To create an AxisService using given service impl class name first
	 * generate schema corresponding to the given java class , next for each
	 * methods AxisOperation will be created. If the method is in-out it will
	 * uses RPCMessageReceiver else RPCInOnlyMessageReceiver <p/> Note : Inorder
	 * to work this properly RPCMessageReceiver should be available in the class
	 * path otherewise operation can not continue
	 * 
	 * @param implClass
	 *            Service implementation class
	 * @param axisConfig
	 *            Current AxisConfiguration
	 * @return return created AxisSrevice the creted service , it can either be
	 *         null or valid service
	 */
	public static AxisService createService(String implClass,
			AxisConfiguration axisConfig) throws AxisFault {

		try {
			HashMap<String, MessageReceiver> messageReciverMap = new HashMap<String, MessageReceiver>();
			Class inOnlyMessageReceiver = Loader
					.loadClass("org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
			MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver
					.newInstance();
			messageReciverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
					messageReceiver);
			Class inoutMessageReceiver = Loader
					.loadClass("org.apache.axis2.rpc.receivers.RPCMessageReceiver");
			MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver
					.newInstance();
			messageReciverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
					inOutmessageReceiver);
			messageReciverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
					inOutmessageReceiver);

			return createService(implClass, axisConfig, messageReciverMap,
					null, null, axisConfig.getSystemClassLoader());
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}
	}

	/**
	 * messageReceiverClassMap will hold the MessageReceivers for given meps.
	 * Key will be the mep and value will be the instance of the MessageReceiver
	 * class. Ex: Map mrMap = new HashMap();
	 * mrMap.put("http://www.w3.org/2004/08/wsdl/in-only",
	 * RPCInOnlyMessageReceiver.class.newInstance());
	 * mrMap.put("http://www.w3.org/2004/08/wsdl/in-out",
	 * RPCMessageReceiver.class.newInstance());
	 * 
	 * @param implClass
	 * @param axisConfiguration
	 * @param messageReceiverClassMap
	 * @param targetNamespace
	 * @param schemaNamespace
	 * @throws AxisFault
	 */
	public static AxisService createService(String implClass,
			AxisConfiguration axisConfiguration, Map messageReceiverClassMap,
			String targetNamespace, String schemaNamespace, ClassLoader loader)
			throws AxisFault {
		int index = implClass.lastIndexOf(".");
		String serviceName;
		if (index > 0) {
			serviceName = implClass.substring(index + 1, implClass.length());
		} else {
			serviceName = implClass;
		}

		SchemaGenerator schemaGenerator;
		ArrayList excludeOpeartion = new ArrayList();
		AxisService service = new AxisService();
		service.setParent(axisConfiguration);
		service.setName(serviceName);

		try {
			Parameter generateBare = service
					.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
			if (generateBare != null && "true".equals(generateBare.getValue())) {
				schemaGenerator = new DocLitBareSchemaGenerator(loader,
						implClass, schemaNamespace,
						Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
			} else {
				schemaGenerator = new DefaultSchemaGenerator(loader, implClass,
						schemaNamespace,
						Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX, service);
			}
			schemaGenerator
					.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
			Utils.addExcludeMethods(excludeOpeartion);
			schemaGenerator.setExcludeMethods(excludeOpeartion);
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}

		return createService(implClass, serviceName, axisConfiguration,
				messageReceiverClassMap, targetNamespace, loader,
				schemaGenerator, service);
	}

	/**
	 * messageReceiverClassMap will hold the MessageReceivers for given meps.
	 * Key will be the mep and value will be the instance of the MessageReceiver
	 * class. Ex: Map mrMap = new HashMap();
	 * mrMap.put("http://www.w3.org/2004/08/wsdl/in-only",
	 * RPCInOnlyMessageReceiver.class.newInstance());
	 * mrMap.put("http://www.w3.org/2004/08/wsdl/in-out",
	 * RPCMessageReceiver.class.newInstance());
	 * 
	 * @param implClass
	 * @param axisConfiguration
	 * @param messageReceiverClassMap
	 * @param targetNamespace
	 * @throws AxisFault
	 */
	public static AxisService createService(String implClass,
			String serviceName, AxisConfiguration axisConfiguration,
			Map<String, MessageReceiver> messageReceiverClassMap, String targetNamespace,
			ClassLoader loader, SchemaGenerator schemaGenerator,
			AxisService axisService) throws AxisFault {
		Parameter parameter = new Parameter(Constants.SERVICE_CLASS, implClass);
		OMElement paraElement = Utils.getParameter(Constants.SERVICE_CLASS,
				implClass, false);
		parameter.setParameterElement(paraElement);
		axisService.setUseDefaultChains(false);
		axisService.addParameter(parameter);
		axisService.setName(serviceName);
		axisService.setClassLoader(loader);

		NamespaceMap map = new NamespaceMap();
		map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
				Java2WSDLConstants.AXIS2_XSD);
		map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
				Java2WSDLConstants.URI_2001_SCHEMA_XSD);
		axisService.setNamespaceMap(map);
		Utils.processBeanPropertyExclude(axisService);
		axisService.setElementFormDefault(false);
		try {
			axisService.addSchema(schemaGenerator.generateSchema());
		} catch (Exception e) {
			throw AxisFault.makeFault(e);
		}
		axisService.setSchemaTargetNamespace(schemaGenerator
				.getSchemaTargetNameSpace());
		axisService.setTypeTable(schemaGenerator.getTypeTable());
		if (targetNamespace == null) {
			targetNamespace = schemaGenerator.getSchemaTargetNameSpace();
		}
		if (targetNamespace != null && !"".equals(targetNamespace)) {
			axisService.setTargetNamespace(targetNamespace);
		}
		Method[] method = schemaGenerator.getMethods();
		PhasesInfo pinfo = axisConfiguration.getPhasesInfo();
		for (int i = 0; i < method.length; i++) {
			Method jmethod = method[i];

            String methodName = jmethod.getName();
            WebMethodAnnotation methodAnnon = JSR181Helper.INSTANCE.getWebMethodAnnotation(jmethod);
            if (methodAnnon != null) {
                if (methodAnnon.getOperationName() != null){
                    methodName = methodAnnon.getOperationName();
                }
            }
			AxisOperation operation = axisService.getOperation(new QName(methodName));

			String mep = operation.getMessageExchangePattern();
			MessageReceiver mr;
			if (messageReceiverClassMap != null) {

				if (messageReceiverClassMap.get(mep) != null) {
					Object obj = messageReceiverClassMap.get(mep);
					if (obj instanceof MessageReceiver) {
						mr = (MessageReceiver) obj;
						operation.setMessageReceiver(mr);
					} else {
						log
								.error("Object is not an instance of MessageReceiver, thus, default MessageReceiver has been set");
						mr = axisConfiguration.getMessageReceiver(operation
								.getMessageExchangePattern());
						operation.setMessageReceiver(mr);
					}
				} else {
					log
							.error("Required MessageReceiver couldn't be found, thus, default MessageReceiver has been used");
					mr = axisConfiguration.getMessageReceiver(operation
							.getMessageExchangePattern());
					operation.setMessageReceiver(mr);
				}
			} else {
				log
						.error("MessageRecevierClassMap couldn't be found, thus, default MessageReceiver has been used");
				mr = axisConfiguration.getMessageReceiver(operation
						.getMessageExchangePattern());
				operation.setMessageReceiver(mr);
			}
			pinfo.setOperationPhases(operation);
			axisService.addOperation(operation);
		}

		String endpointName = axisService.getEndpointName();
		if ((endpointName == null || endpointName.length() == 0)
				&& axisService.getAxisConfiguration() != null) {
			Utils.addEndpointsToService(axisService, axisService.getAxisConfiguration());
		}

		return axisService;

	}

	public void removeOperation(QName opName) {
		AxisOperation operation = getOperation(opName);
		if (operation != null) {
			removeChild(opName);
			ArrayList<String> mappingList = operation.getWSAMappingList();
			if (mappingList != null) {
				for (int i = 0; i < mappingList.size(); i++) {
					String actionMapping = (String) mappingList.get(i);
					operationsAliasesMap.remove(actionMapping);
					invalidOperationsAliases.remove(actionMapping);
				}
			}
			operationsAliasesMap.remove(operation.getName().getLocalPart());
			invalidOperationsAliases.remove(operation.getName().getLocalPart());
		}
	}

	/**
	 * Get the namespace map for this service.
	 * 
	 * @return a Map of prefix (String) to namespace URI (String)
	 * @deprecated please use getNamespaceMap()
	 */
	public Map<String, String> getNameSpacesMap() {
		return namespaceMap;
	}

	/**
	 * Get the namespace map for this service.
	 * 
	 * @return a Map of prefix (String) to namespace URI (String)
	 */
	public Map getNamespaceMap() {
		return namespaceMap;
	}
    
    /**
     * Get the namespaces associated with imported WSDLs
     * 
     * @return a <code>List</code> of namespace URIs (String)
     */
    public List getImportedNamespaces() {
        return importedNamespaces;
    }

    /**
     * Set the namespaces associated with imported WSDLs
     * 
     * @param importedNamespaces
     */
    public void setImportedNamespaces(List importedNamespaces) {
        this.importedNamespaces = importedNamespaces;
    }
    
    /**
     * @deprecated please use setNamespaceMap
     * @param nameSpacesMap
     */
    public void setNameSpacesMap(NamespaceMap nameSpacesMap) {
		this.namespaceMap = nameSpacesMap;
	}

	public void setNamespaceMap(NamespaceMap namespaceMap) {
		this.namespaceMap = namespaceMap;
	}

	private void addSchemaNameSpace(XmlSchema schema) {
		String targetNameSpace = schema.getTargetNamespace();
		String prefix = schema.getNamespaceContext().getPrefix(targetNameSpace);

		if (namespaceMap == null) {
			namespaceMap = new NamespaceMap();
		}

		if (!namespaceMap.values().contains(targetNameSpace)) {
			// i.e this target namespace not exists in the namesapce map
			// find a non exists prefix to add this target namesapce
			while ((prefix == null) || namespaceMap.keySet().contains(prefix)) {
				prefix = "ns" + nsCount++;
			}
			namespaceMap.put(prefix, targetNameSpace);
		}

	}

	public Map populateSchemaMappings() {
		// when calling from other than codegen. i.e from deployment
		// engine we don't have to override the absolute http locations.
		return populateSchemaMappings(false);
	}

	/**
	 * runs the schema mappings if it has not been run previously it is best
	 * that this logic be in the axis service since one can call the axis
	 * service to populate the schema mappings
	 */
	public Map populateSchemaMappings(boolean overrideAbsoluteAddress) {

		// populate the axis service with the necessary schema references
		ArrayList schema = this.schemaList;
		Map changedSchemaLocations = null;
		if (!this.schemaLocationsAdjusted) {
			Hashtable nameTable = new Hashtable();
			Hashtable sourceURIToNewLocationMap = new Hashtable();
			// calculate unique names for the schemas
			calculateSchemaNames(schema, nameTable, sourceURIToNewLocationMap,
					overrideAbsoluteAddress);
			// adjust the schema locations as per the calculated names
			changedSchemaLocations = adjustSchemaNames(schema, nameTable,
					sourceURIToNewLocationMap);
			// reverse the nametable so that there is a mapping from the
			// name to the schemaObject
			setSchemaMappingTable(swapMappingTable(nameTable));
			setSchemaLocationsAdjusted(true);
		}
		return changedSchemaLocations;
	}

	/**
	 * run 1 -calcualte unique names
	 * 
	 * @param schemas
	 */
	private void calculateSchemaNames(List schemas, Hashtable nameTable,
			Hashtable sourceURIToNewLocationMap, boolean overrideAbsoluteAddress) {
		// first traversal - fill the hashtable
		for (int i = 0; i < schemas.size(); i++) {
			XmlSchema schema = (XmlSchema) schemas.get(i);
			XmlSchemaObjectCollection includes = schema.getIncludes();

			for (int j = 0; j < includes.getCount(); j++) {
				Object item = includes.getItem(j);
				XmlSchema s;
				if (item instanceof XmlSchemaExternal) {
					XmlSchemaExternal externalSchema = (XmlSchemaExternal) item;
					s = externalSchema.getSchema();

					if (s != null
							&& getScheamLocationWithDot(
									sourceURIToNewLocationMap, s) == null) {
						// insert the name into the table
						insertIntoNameTable(nameTable, s,
								sourceURIToNewLocationMap,
								overrideAbsoluteAddress);
						// recursively call the same procedure
						calculateSchemaNames(Arrays
								.asList(new XmlSchema[] { s }), nameTable,
								sourceURIToNewLocationMap,
								overrideAbsoluteAddress);
					}
				}
			}
		}
	}

	/**
	 * A quick private sub routine to insert the names
	 * 
	 * @param nameTable
	 * @param s
	 */
	private void insertIntoNameTable(Hashtable nameTable, XmlSchema s,
			Hashtable sourceURIToNewLocationMap, boolean overrideAbsoluteAddress) {
		String sourceURI = s.getSourceURI();
		// check whether the sourece uri is an absolute one and are
		// we allowed to override it.
		// if the absolute uri overriding is not allowed the use the
		// original sourceURI as new one
		if (sourceURI.startsWith("http") && !overrideAbsoluteAddress) {
			nameTable.put(s, sourceURI);
			sourceURIToNewLocationMap.put(sourceURI, sourceURI);
		} else {
			String newURI = sourceURI.substring(sourceURI.lastIndexOf('/') + 1);
			if (newURI.endsWith(".xsd")) {
				// remove the .xsd extention
				newURI = newURI.substring(0, newURI.lastIndexOf("."));
			} else {
				newURI = "xsd" + count++;
			}

			newURI = customSchemaNameSuffix != null ? newURI
					+ customSchemaNameSuffix : newURI;
			// make it unique
			while (nameTable.containsValue(newURI)) {
				newURI = newURI + count++;
			}

			nameTable.put(s, newURI);
			sourceURIToNewLocationMap.put(sourceURI, newURI);
		}

	}

	/**
	 * Run 2 - adjust the names
	 */
	private Map adjustSchemaNames(List schemas, Hashtable nameTable,
			Hashtable sourceURIToNewLocationMap) {
		Hashtable importedSchemas = new Hashtable();
		// process the schemas in the main schema list
		for (int i = 0; i < schemas.size(); i++) {
			adjustSchemaName((XmlSchema) schemas.get(i), nameTable,
					importedSchemas, sourceURIToNewLocationMap);
		}
		// process all the rest in the name table
		Enumeration nameTableKeys = nameTable.keys();
		while (nameTableKeys.hasMoreElements()) {
			adjustSchemaName((XmlSchema) nameTableKeys.nextElement(),
					nameTable, importedSchemas, sourceURIToNewLocationMap);

		}
		return importedSchemas;
	}

	/**
	 * Adjust a single schema
	 * 
	 * @param parentSchema
	 * @param nameTable
	 */
	private void adjustSchemaName(XmlSchema parentSchema, Hashtable nameTable,
			Hashtable importedScheams, Hashtable sourceURIToNewLocationMap) {
		XmlSchemaObjectCollection includes = parentSchema.getIncludes();
		for (int j = 0; j < includes.getCount(); j++) {
			Object item = includes.getItem(j);
			if (item instanceof XmlSchemaExternal) {
				XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal) item;
				XmlSchema s = xmlSchemaExternal.getSchema();
				adjustSchemaLocation(s, xmlSchemaExternal, nameTable,
						importedScheams, sourceURIToNewLocationMap);
			}
		}

	}
		
	/**
	 * Adjusts a given schema location
	 * 
	 * @param s
	 * @param xmlSchemaExternal
	 * @param nameTable
	 */
    private void adjustSchemaLocation(XmlSchema s,
                                      XmlSchemaExternal xmlSchemaExternal, Hashtable nameTable,
                                      Hashtable importedScheams, Hashtable sourceURIToNewLocationMap) {
        if (s != null) {
            String schemaLocation = xmlSchemaExternal.getSchemaLocation();

            String newscheamlocation = customSchemaNamePrefix == null ?
                    // use the default mode
                    (this.getServiceEPR() + "?xsd=" + getScheamLocationWithDot(
                            sourceURIToNewLocationMap, s))
                    :
                    // custom prefix is present - add the custom prefix
                    (customSchemaNamePrefix + getScheamLocationWithDot(
                            sourceURIToNewLocationMap, s));
            xmlSchemaExternal.setSchemaLocation(newscheamlocation);
            importedScheams.put(schemaLocation, newscheamlocation);
        }
    }

	private Object getScheamLocationWithDot(
			Hashtable sourceURIToNewLocationMap, XmlSchema s) {
		String o = (String) sourceURIToNewLocationMap.get(s.getSourceURI());
		if (o != null && o.indexOf(".") < 0) {
			return o + ".xsd";
		}
		return o;
	}

	/**
	 * Swap the key,value pairs
	 * 
	 * @param originalTable
	 */
	private Map swapMappingTable(Map originalTable) {
		HashMap swappedTable = new HashMap(originalTable.size());
		Iterator keys = originalTable.keySet().iterator();
		Object key;
		while (keys.hasNext()) {
			key = keys.next();
			swappedTable.put(originalTable.get(key), key);
		}

		return swappedTable;
	}

	public boolean isClientSide() {
		return clientSide;
	}

	public void setClientSide(boolean clientSide) {
		this.clientSide = clientSide;
	}

	public boolean isElementFormDefault() {
		return elementFormDefault;
	}

	public void setElementFormDefault(boolean elementFormDefault) {
		this.elementFormDefault = elementFormDefault;
	}

	/**
	 * User can set a parameter in services.xml saying he want to show the
	 * original wsdl that he put into META-INF once someone ask for ?wsdl so if
	 * you want to use your own wsdl then add following parameter into
	 * services.xml <parameter name="useOriginalwsdl">true</parameter>
	 */
	public boolean isUseUserWSDL() {
		Parameter parameter = getParameter("useOriginalwsdl");
		if (parameter != null) {
			String value = (String) parameter.getValue();
			if ("true".equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * By default the port address in user WSDLs is modified, set the following
	 * parameter to override this behaviour <parameter
	 * name="modifyUserWSDLPortAddress">false</parameter>
	 */
	public boolean isModifyUserWSDLPortAddress() {
		Parameter parameter = getParameter("modifyUserWSDLPortAddress");
		if (parameter != null) {
			String value = (String) parameter.getValue();
			if ("false".equals(value)) {
				return false;
			}
		}
		return true;
	}

	public ServiceLifeCycle getServiceLifeCycle() {
		return serviceLifeCycle;
	}

	public void setServiceLifeCycle(ServiceLifeCycle serviceLifeCycle) {
		this.serviceLifeCycle = serviceLifeCycle;
	}

	public Map getP2nMap() {
		return p2nMap;
	}

	public void setP2nMap(Map p2nMap) {
		this.p2nMap = p2nMap;
	}

	public ObjectSupplier getObjectSupplier() {
		return objectSupplier;
	}

	public void setObjectSupplier(ObjectSupplier objectSupplier) {
		this.objectSupplier = objectSupplier;
	}

	public TypeTable getTypeTable() {
		return typeTable;
	}

	public void setTypeTable(TypeTable typeTable) {
		this.typeTable = typeTable;
	}

	/**
	 * Find a data locator from the available data locators (both configured and
	 * default ones) to retrieve Metadata or data specified in the request.
	 * 
	 * @param request
	 *            an {@link DataRetrievalRequest} object
	 * @param msgContext
	 *            message context
	 * @return array of {@link Data} object for the request.
	 * @throws AxisFault
	 */

	public Data[] getData(DataRetrievalRequest request,
			MessageContext msgContext) throws AxisFault {

		Data[] data;
		String dialect = request.getDialect();
		AxisDataLocator dataLocator = null;
		int nextDataLocatorIndex = 0;
		int totalLocators = availableDataLocatorTypes.length;
		for (int i = 0; i < totalLocators; i++) {
			dataLocator = getDataLocator(availableDataLocatorTypes[i], dialect);
			if (dataLocator != null) {
				nextDataLocatorIndex = i + 1;
				break;
			}
		}

		if (dataLocator == null) {
			return null;
		}

		data = dataLocator.getData(request, msgContext);
		// Null means Data Locator not understood request. Automatically find
		// Data Locator in the hierarchy to process the request.
		if (data == null) {
			if (nextDataLocatorIndex < totalLocators) {
				data = bubbleupDataLocators(nextDataLocatorIndex, request,
						msgContext);
			}

		}
		return data;
	}

	/*
	 * To search the next Data Locator from the available Data Locators that
	 * understood the data retrieval request.
	 */
	private Data[] bubbleupDataLocators(int nextIndex,
			DataRetrievalRequest request, MessageContext msgContext)
			throws AxisFault {
		Data[] data = null;
		if (nextIndex < availableDataLocatorTypes.length) {
			AxisDataLocator dataLocator = getDataLocator(
					availableDataLocatorTypes[nextIndex], request.getDialect());
			nextIndex++;
			if (dataLocator != null) {
				data = dataLocator.getData(request, msgContext);
				if (data == null) {
					data = bubbleupDataLocators(nextIndex, request, msgContext);
				} else {
					return data;
				}

			} else {
				data = bubbleupDataLocators(nextIndex, request, msgContext);
			}

		}
		return data;
	}

	/**
	 * Save data Locator configured at service level for this Axis Service
	 * 
	 * @param dialect-
	 *            an absolute URI represents the Dialect i.e. WSDL, Policy,
	 *            Schema or "ServiceLevel" for non-dialect service level data
	 *            locator.
	 * @param dataLocatorClassName -
	 *            class name of the Data Locator configured to support data
	 *            retrieval for the specified dialect.
	 */
	public void addDataLocatorClassNames(String dialect,
			String dataLocatorClassName) {
		dataLocatorClassNames.put(dialect, dataLocatorClassName);
	}

	/*
	 * Get data locator instance based on the LocatorType and dialect.
	 */
	public AxisDataLocator getDataLocator(LocatorType locatorType,
			String dialect) throws AxisFault {
		AxisDataLocator locator;
		if (locatorType == LocatorType.SERVICE_DIALECT) {
			locator = getServiceDataLocator(dialect);
		} else if (locatorType == LocatorType.SERVICE_LEVEL) {
			locator = getServiceDataLocator(DRConstants.SERVICE_LEVEL);
		} else if (locatorType == LocatorType.GLOBAL_DIALECT) {
			locator = getGlobalDataLocator(dialect);
		} else if (locatorType == LocatorType.GLOBAL_LEVEL) {
			locator = getGlobalDataLocator(DRConstants.GLOBAL_LEVEL);
		} else if (locatorType == LocatorType.DEFAULT_AXIS) {
			locator = getDefaultDataLocator();
		} else {
			locator = getDefaultDataLocator();
		}

		return locator;
	}

	// Return default Axis2 Data Locator
	private AxisDataLocator getDefaultDataLocator()
			throws DataRetrievalException {

		if (defaultDataLocator == null) {
			defaultDataLocator = new AxisDataLocatorImpl(this);
		}

		defaultDataLocator.loadServiceData();

		return defaultDataLocator;
	}

	/*
	 * Checks if service level data locator configured for specified dialect.
	 * Returns an instance of the data locator if exists, and null otherwise.
	 */
	private AxisDataLocator getServiceDataLocator(String dialect)
			throws AxisFault {
		AxisDataLocator locator;
		locator = (AxisDataLocator) dataLocators.get(dialect);
		if (locator == null) {
			String className = (String) dataLocatorClassNames.get(dialect);
			if (className != null) {
				locator = loadDataLocator(className);
				dataLocators.put(dialect, locator);
			}

		}

		return locator;

	}

	/*
	 * Checks if global level data locator configured for specified dialect.
	 * @param dialect- an absolute URI represents the Dialect i.e. WSDL, Policy,
	 * Schema or "GlobalLevel" for non-dialect Global level data locator.
	 * Returns an instance of the data locator if exists, and null otherwise.
	 */

	public AxisDataLocator getGlobalDataLocator(String dialect)
			throws AxisFault {
		AxisConfiguration axisConfig = getAxisConfiguration();
		AxisDataLocator locator = null;
		if (axisConfig != null) {
			locator = axisConfig.getDataLocator(dialect);
			if (locator == null) {
				String className = axisConfig.getDataLocatorClassName(dialect);
				if (className != null) {
					locator = loadDataLocator(className);
					axisConfig.addDataLocator(dialect, locator);
				}
			}
		}

		return locator;

	}

	protected AxisDataLocator loadDataLocator(String className)
			throws AxisFault {

		AxisDataLocator locator;

		try {
			Class dataLocator;
			dataLocator = Class.forName(className, true, serviceClassLoader);
			locator = (AxisDataLocator) dataLocator.newInstance();
		} catch (ClassNotFoundException e) {
			throw AxisFault.makeFault(e);
		} catch (IllegalAccessException e) {
			throw AxisFault.makeFault(e);
		} catch (InstantiationException e) {
			throw AxisFault.makeFault(e);

		}

		return locator;
	}

	/**
	 * Set the map of WSDL message element QNames to AxisOperations for this
	 * service. This map is used during SOAP Body-based routing for
	 * document/literal bare services to match the first child element of the
	 * SOAP Body element to an operation. (Routing for RPC and document/literal
	 * wrapped services occurs via the operationsAliasesMap.) <p/> From section
	 * 4.7.6 of the WS-I BP 1.1: the "operation signature" is "the fully
	 * qualified name of the child element of SOAP body of the SOAP input
	 * message described by an operation in a WSDL binding," and thus this map
	 * must be from a QName to an operation.
	 * 
	 * @param messageElementQNameToOperationMap
	 *            The map from WSDL message element QNames to AxisOperations.
	 */
	public void setMessageElementQNameToOperationMap(
			Map messageElementQNameToOperationMap) {
		this.messageElementQNameToOperationMap = messageElementQNameToOperationMap;
	}

	/**
	 * Look up an AxisOperation for this service based off of an element QName
	 * from a WSDL message element.
	 * 
	 * @param messageElementQName
	 *            The QName to search for.
	 * @return The AxisOperation registered to the QName or null if no match was
	 *         found.
	 * @see #setMessageElementQNameToOperationMap(Map)
	 */
	public AxisOperation getOperationByMessageElementQName(
			QName messageElementQName) {
		return (AxisOperation) messageElementQNameToOperationMap
				.get(messageElementQName);
	}

	/**
	 * Add an entry to the map between element QNames in WSDL messages and
	 * AxisOperations for this service.
	 * 
	 * @param messageElementQName
	 *            The QName of the element on the input message that maps to the
	 *            given operation.
	 * @param operation
	 *            The AxisOperation to be mapped to.
	 * @see #setMessageElementQNameToOperationMap(Map)
	 */
	public void addMessageElementQNameToOperationMapping(
			QName messageElementQName, AxisOperation operation) {
		// when setting an operation we have to set it only if the
		// messegeElementQName does not
		// exists in the map.
		// does exists means there are two or more operations which has the same
		// input element (in doc/literal
		// this is possible. In this case better to set it as null without
		// giving
		// a random operation.
		if (messageElementQNameToOperationMap.containsKey(messageElementQName)
				&& messageElementQNameToOperationMap.get(messageElementQName) != operation) {
			messageElementQNameToOperationMap.put(messageElementQName, null);
		} else {
			messageElementQNameToOperationMap.put(messageElementQName,
					operation);
		}

	}

	/**
	 * @deprecated use {@link AxisEndpoint#getEndpointURL()}
	 */
	public String getEndpointURL() {
		return endpointURL;
	}

	/**
	 * @deprecated use {@link AxisEndpoint#setEndpointURL(String)}
	 */
	public void setEndpointURL(String endpointURL) {
		this.endpointURL = endpointURL;
	}

	// TODO : Explain what goes in this map!
	public Map<String, AxisEndpoint> getEndpoints() {
		return endpointMap;
	}

	public boolean isCustomWsdl() {
		return customWsdl;
	}

	public void setCustomWsdl(boolean customWsdl) {
		this.customWsdl = customWsdl;
	}

	public List getOperationsNameList() {
		return operationsNameList;
	}

	public void setOperationsNameList(List operationsNameList) {
		this.operationsNameList = operationsNameList;
	}

	public AxisServiceGroup getAxisServiceGroup() {
		return (AxisServiceGroup) parent;
	}

	public void setParent(AxisServiceGroup parent) {
		this.parent = parent;
	}

	public String toString() {
		return getName();
	}

	public ExcludeInfo getExcludeInfo() {
		return excludeInfo;
	}

	public void setExcludeInfo(ExcludeInfo excludeInfo) {
		this.excludeInfo = excludeInfo;
	}

	public void registerPolicy(String key, Policy policy) {
		policyMap.put(key, policy);
	}

	public Policy lookupPolicy(String key) {
		return (Policy) policyMap.get(key);
	}
    
    /**
     * Add a ServiceContextListener
     * @param scl
     */
    public void addMessageContextListener(MessageContextListener scl) {
        messageContextListeners.add(scl);
    }
    
    /**
     * Remove a ServiceContextListener
     * @param scl
     */
    public void removeMessageContextListener(MessageContextListener scl) {
        messageContextListeners.remove(scl);
    }
    
    /**
     * @param cls Class of ServiceContextListener
     * @return true if ServiceContextLister is in the list
     */
    public boolean hasMessageContextListener(Class cls) {
        for (int i=0; i<messageContextListeners.size(); i++) {
            if (messageContextListeners.get(i).getClass() == cls) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Signal an Attach ServiceContext Event
     * @param sc ServiceContext
     * @param mc MessageContext
     */
    public void attachServiceContextEvent(ServiceContext sc, MessageContext mc) {
        for (int i=0; i<messageContextListeners.size(); i++) {
            messageContextListeners.get(i).attachServiceContextEvent(sc, mc);
        }
    }
    
    /**
     * Signal an Attach Envelope Event
     * @param mc MessageContext
     */
    public void attachEnvelopeEvent(MessageContext mc) {
        for (int i=0; i<messageContextListeners.size(); i++) {
            messageContextListeners.get(i).attachEnvelopeEvent(mc);
        }
    }
}
