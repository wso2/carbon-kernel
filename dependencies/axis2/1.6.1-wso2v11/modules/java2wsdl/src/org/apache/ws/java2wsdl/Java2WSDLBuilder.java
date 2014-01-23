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

package org.apache.ws.java2wsdl;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisService2WSDL11;
import org.apache.axis2.description.AxisService2WSDL20;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.java2wsdl.DefaultNamespaceGenerator;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.description.java2wsdl.DocLitBareSchemaGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.Java2WSDLUtils;
import org.apache.axis2.description.java2wsdl.NamespaceGenerator;
import org.apache.axis2.description.java2wsdl.SchemaGenerator;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.axis2.Constants;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Java2WSDLBuilder implements Java2WSDLConstants {

	public static final String ALL = "all";
	private OutputStream out;
	private String className;
	private ClassLoader classLoader;
	private String wsdlPrefix = "wsdl";

	private String serviceName = null;

	// these apply for the WSDL
	private String targetNamespace = null;
	private String targetNamespacePrefix = null;

	private String attrFormDefault = null;
	private String elementFormDefault = null;
	private String schemaTargetNamespace = null;
	private String schemaTargetNamespacePrefix = null;
	private String style = Java2WSDLConstants.DOCUMENT;
	private String use = Java2WSDLConstants.LITERAL;
	private String locationUri;
	private ArrayList<String> extraClasses;
    private boolean nillableElementsAllowed = true;

	private String nsGenClassName = null;
	private Map<String,String> pkg2nsMap = null;
	private boolean pretty = true;
	private String wsdlVersion = WSDL_VERSION_1;
	private String schemaGenClassName = null;
	private boolean generateDocLitBare = false;
	private AxisConfiguration axisConfig;
    private String customSchemaLocation;
    // location of the class name to package mapping file
    // File is simple file with qualifiedClassName:SchemaQName
    private String mappingFileLocation;
    private HashMap<String,MessageReceiver> messageReceivers = null;

    private boolean disableSOAP11 = false;
    private boolean disableSOAP12 = false;
    private boolean disableREST = false;

    private String messagePartName;

    private String portTypeName;

    private String soap11BindingName;
    private String soap12BindingName;
    private String restBindingName;

    private String requestElementSuffix;

    private boolean anonymousTypesAllowed = true;

    public Java2WSDLBuilder() {
		try {
			ConfigurationContext configCtx = ConfigurationContextFactory
					.createDefaultConfigurationContext();
			axisConfig = configCtx.getAxisConfiguration();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Java2WSDLBuilder(AxisConfiguration axisConfig) {
		this.axisConfig = axisConfig;
	}

	public Java2WSDLBuilder(OutputStream out, String className,
			ClassLoader classLoader) {
		try {
			ConfigurationContext configCtx = ConfigurationContextFactory
					.createDefaultConfigurationContext();
			axisConfig = configCtx.getAxisConfiguration();
			this.out = out;
			this.className = className;
			this.classLoader = classLoader;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public Java2WSDLBuilder(OutputStream out, String className,
                            ClassLoader classLoader, HashMap<String,MessageReceiver> messageReceivers) {
        this(out, className, classLoader);
        this.messageReceivers = messageReceivers;
    }

    public String getSchemaTargetNamespace() throws Exception {
		if (schemaTargetNamespace == null) {
			schemaTargetNamespace = Java2WSDLUtils
					.schemaNamespaceFromClassName(className, classLoader,
							resolveNSGen()).toString();
		}
		return schemaTargetNamespace;
	}

	public String getStyle() {
		return style;
	}

	public String getLocationUri() {
		return locationUri;
	}

	public void setLocationUri(String locationUri) {
		this.locationUri = locationUri;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	public void setSchemaTargetNamespace(String schemaTargetNamespace) {
		this.schemaTargetNamespace = schemaTargetNamespace;
	}

	public String getSchemaTargetNamespacePrefix() {
		if (schemaTargetNamespacePrefix == null) {
			this.schemaTargetNamespacePrefix = SCHEMA_NAMESPACE_PRFIX;
		}
		return schemaTargetNamespacePrefix;
	}

	public void setSchemaTargetNamespacePrefix(
			String schemaTargetNamespacePrefix) {
		this.schemaTargetNamespacePrefix = schemaTargetNamespacePrefix;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getTargetNamespacePrefix() {
		if (targetNamespacePrefix == null) {
			this.targetNamespacePrefix = TARGETNAMESPACE_PREFIX;
		}
		return targetNamespacePrefix;
	}

	public void setTargetNamespacePrefix(String targetNamespacePrefix) {
		this.targetNamespacePrefix = targetNamespacePrefix;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getWsdlPrefix() {
		return wsdlPrefix;
	}

	public void setWsdlPrefix(String wsdlPrefix) {
		this.wsdlPrefix = wsdlPrefix;
	}

	public boolean isGenerateDocLitBare() {
		return generateDocLitBare;
	}

	public void setGenerateDocLitBare(boolean generateDocLitBare) {
		this.generateDocLitBare = generateDocLitBare;
	}

	public void generateWSDL() throws Exception {
		SchemaGenerator schemaGenerator = resolveSchemaGen(classLoader,
				className, getSchemaTargetNamespace(),
				getSchemaTargetNamespacePrefix());

		ArrayList<String> excludedOperation = new ArrayList<String>();
		Utils.addExcludeMethods(excludedOperation);
		schemaGenerator.setExcludeMethods(excludedOperation);
		schemaGenerator.setAttrFormDefault(getAttrFormDefault());
		schemaGenerator.setElementFormDefault(getElementFormDefault());
		schemaGenerator.setExtraClasses(getExtraClasses());
		schemaGenerator.setNsGen(resolveNSGen());
		schemaGenerator.setPkg2nsmap(getPkg2nsMap());
        schemaGenerator.setMappingFileLocation(getMappingFileLocation());
        schemaGenerator.setCustomSchemaLocation(getCustomSchemaLocation());
        if (getPkg2nsMap() != null
				&& !getPkg2nsMap().isEmpty()
				&& (getPkg2nsMap().containsKey(ALL) || getPkg2nsMap()
						.containsKey(ALL.toUpperCase()))) {
			schemaGenerator.setUseWSDLTypesNamespace(true);
		}

        if(messageReceivers == null) {
            messageReceivers = new HashMap<String,MessageReceiver>();
            Class<?> inOnlyMessageReceiver = Loader
                    .loadClass("org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
            MessageReceiver messageReceiver = (MessageReceiver) inOnlyMessageReceiver
                    .newInstance();
            messageReceivers.put(WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver);
            Class<?> inoutMessageReceiver = Loader
                    .loadClass("org.apache.axis2.rpc.receivers.RPCMessageReceiver");
            MessageReceiver inOutmessageReceiver = (MessageReceiver) inoutMessageReceiver
                    .newInstance();
            messageReceivers.put(WSDL2Constants.MEP_URI_IN_OUT,
                    inOutmessageReceiver);
        }
        AxisService service = new AxisService();

        if (!nillableElementsAllowed) {
            service.addParameter(Java2WSDLConstants.DISALLOW_NILLABLE_ELEMENTS_OPTION_LONG, "true");
        }

        if (messagePartName != null){
            service.addParameter(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG, messagePartName);
        }

        if (portTypeName != null){
            service.addParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG, portTypeName);
        }

        if (soap11BindingName != null){
            service.addParameter(Java2WSDLConstants.SOAP11_BINDING_NAME_OPTION_LONG, soap11BindingName);
        }

        if (soap12BindingName != null){
            service.addParameter(Java2WSDLConstants.SOAP12_BINDING_NAME_OPTION_LONG, soap12BindingName);
        }

        if (restBindingName != null){
            service.addParameter(Java2WSDLConstants.REST_BINDING_NAME_OPTION_LONG, restBindingName);
        }

        if (messagePartName != null){
            service.addParameter(Java2WSDLConstants.MESSAGE_PART_NAME_OPTION_LONG, messagePartName);
        }

        if (requestElementSuffix != null){
            service.addParameter(Java2WSDLConstants.REQUEST_ELEMENT_SUFFIX_OPTION_LONG, requestElementSuffix);
        }

        if (!anonymousTypesAllowed) {
            service.addParameter(Java2WSDLConstants.DISALLOW_ANON_TYPES_OPTION_LONG, "true");
        }

		schemaGenerator.setAxisService(service);
		AxisService axisService = AxisService.createService(className,
				serviceName == null ? Java2WSDLUtils
						.getSimpleClassName(className) : serviceName,
				axisConfig, messageReceivers,
				targetNamespace == null ? Java2WSDLUtils
						.namespaceFromClassName(className, classLoader,
								resolveNSGen()).toString() : targetNamespace,
				classLoader, schemaGenerator, service);
		schemaGenerator.setAxisService(axisService);
		axisService.setTargetNamespacePrefix(getTargetNamespacePrefix());
		axisService.setSchemaTargetNamespace(getSchemaTargetNamespace());
		axisService
				.setSchemaTargetNamespacePrefix(getSchemaTargetNamespacePrefix());

        if (disableREST) {
            axisService.addParameter(Constants.Configuration.DISABLE_REST, disableREST);
        }

        if (disableSOAP11) {
            axisService.addParameter(Constants.Configuration.DISABLE_SOAP11, disableSOAP11);
        }

        if (disableSOAP12) {
            axisService.addParameter(Constants.Configuration.DISABLE_SOAP12, disableSOAP12);
        }

		String uri = locationUri;
		if (uri == null) {
			uri = DEFAULT_LOCATION_URL
					+ (serviceName == null ? Java2WSDLUtils
							.getSimpleClassName(className) : serviceName);
		}
		// axisService.setEPRs(new String[]{uri});
		setServiceEPR(axisService, uri);
		axisConfig.addService(axisService);

		if (WSDL_VERSION_1.equals(wsdlVersion)) {
			AxisService2WSDL11 g = new AxisService2WSDL11(axisService);
			g.setStyle(this.style);
			g.setUse(this.use);
			OMElement wsdlElement = g.generateOM();
			if (!isPretty()) {
				wsdlElement.serialize(out);
			} else {
				XMLPrettyPrinter.prettify(wsdlElement, out);
			}
		} else {
			AxisService2WSDL20 g = new AxisService2WSDL20(axisService);
			OMElement wsdlElement = g.generateOM();
			if (!isPretty()) {
				wsdlElement.serialize(out);
			} else {
				XMLPrettyPrinter.prettify(wsdlElement, out);
			}
		}
		
		out.flush();
		out.close();
	}

	public String getAttrFormDefault() {
		return attrFormDefault;
	}

	public void setAttrFormDefault(String attrFormDefault) {
		this.attrFormDefault = attrFormDefault;
	}

	public String getElementFormDefault() {
		return elementFormDefault;
	}

	public void setElementFormDefault(String elementFormDefault) {
		this.elementFormDefault = elementFormDefault;
	}

	public ArrayList<String> getExtraClasses() {
		return extraClasses;
	}

	public void setExtraClasses(ArrayList<String> extraClasses) {
		this.extraClasses = extraClasses;
	}

	public String getNsGenClassName() {
		return nsGenClassName;
	}

	public void setNsGenClassName(String nsGenClassName) {
		this.nsGenClassName = nsGenClassName;
	}

	public String getSchemaGenClassName() {
		return schemaGenClassName;
	}

	public void setSchemaGenClassName(String schemaGenClassName) {
		this.schemaGenClassName = schemaGenClassName;
	}

	public Map<String,String> getPkg2nsMap() {
		return pkg2nsMap;
	}

	public void setPkg2nsMap(Map<String,String> pkg2nsMap) {
		this.pkg2nsMap = pkg2nsMap;
	}

	private NamespaceGenerator resolveNSGen() {
		NamespaceGenerator nsGen;
		if (this.nsGenClassName == null) {
			nsGen = new DefaultNamespaceGenerator();
		} else {
			try {
				nsGen = (NamespaceGenerator) Class.forName(this.nsGenClassName)
						.newInstance();
			} catch (Exception e) {
				nsGen = new DefaultNamespaceGenerator();
			}
		}
		return nsGen;
	}

	private SchemaGenerator resolveSchemaGen(ClassLoader loader,
			String className, String schematargetNamespace,
			String schematargetNamespacePrefix) throws Exception {
		SchemaGenerator schemaGen;
		if (this.schemaGenClassName == null) {
			if (generateDocLitBare) {
				schemaGen = new DocLitBareSchemaGenerator(loader, className,
						schematargetNamespace, schematargetNamespacePrefix,
						null);
			} else {
				schemaGen = new DefaultSchemaGenerator(loader, className,
						schematargetNamespace, schematargetNamespacePrefix,
						null);
			}

		} else {
			try {
				Class<?> clazz = Class.forName(this.schemaGenClassName);
				Constructor<?> constructor = clazz.getConstructor(new Class[] {
						ClassLoader.class, String.class, String.class,
						String.class });
				schemaGen = (SchemaGenerator) constructor
						.newInstance(new Object[] { loader, className,
								schematargetNamespace,
								schematargetNamespacePrefix });
			} catch (Exception e) {
				if (generateDocLitBare) {
					schemaGen = new DocLitBareSchemaGenerator(loader,
							className, schematargetNamespace,
							schematargetNamespacePrefix, null);
				} else {
					schemaGen = new DefaultSchemaGenerator(loader, className,
							schematargetNamespace, schematargetNamespacePrefix,
							null);
				}

			}
		}
		return schemaGen;
	}

	public boolean isPretty() {
		return pretty;
	}

	public void setPretty(boolean pretty) {
		this.pretty = pretty;
	}

	public void setWSDLVersion(String wsdlVersion) {
		this.wsdlVersion = wsdlVersion;
	}

	private void setServiceEPR(AxisService axisService, String urlString)
			throws Exception {
		Utils.addSoap11Endpoint(axisService, urlString);
		Utils.addSoap12Endpoint(axisService, urlString);
		
		if ("http".equals(org.apache.axis2.util.Utils.getURIScheme(urlString))) {
			Utils.addHttpEndpoint(axisService, urlString);
		}
	}


    public String getCustomSchemaLocation() {
        return customSchemaLocation;
    }

    public void setCustomSchemaLocation(String customSchemaLocation) {
        this.customSchemaLocation = customSchemaLocation;
    }

    public String getMappingFileLocation() {
        return mappingFileLocation;
    }

    public void setMappingFileLocation(String mappingFileLocation) {
        this.mappingFileLocation = mappingFileLocation;
    }

    public boolean isNillableElementsAllowed() {
        return nillableElementsAllowed;
    }

    public void setNillableElementsAllowed(boolean nillableElementsAllowed) {
        this.nillableElementsAllowed = nillableElementsAllowed;
    }

    public boolean isDisableREST() {
        return disableREST;
    }

    public void setDisableREST(boolean disableREST) {
        this.disableREST = disableREST;
    }

    public boolean isDisableSOAP12() {
        return disableSOAP12;
    }

    public void setDisableSOAP12(boolean disableSOAP12) {
        this.disableSOAP12 = disableSOAP12;
    }

    public boolean isDisableSOAP11() {
        return disableSOAP11;
    }

    public void setDisableSOAP11(boolean disableSOAP11) {
        this.disableSOAP11 = disableSOAP11;
    }

    public String getMessagePartName() {
        return messagePartName;
    }

    public void setMessagePartName(String messagePartName) {
        this.messagePartName = messagePartName;
    }

    public String getPortTypeName() {
        return portTypeName;
    }

    public void setPortTypeName(String portTypeName) {
        this.portTypeName = portTypeName;
    }

    public String getSoap11BindingName() {
        return soap11BindingName;
    }

    public void setSoap11BindingName(String soap11BindingName) {
        this.soap11BindingName = soap11BindingName;
    }

    public String getSoap12BindingName() {
        return soap12BindingName;
    }

    public void setSoap12BindingName(String soap12BindingName) {
        this.soap12BindingName = soap12BindingName;
    }

    public String getRestBindingName() {
        return restBindingName;
    }

    public void setRestBindingName(String restBindingName) {
        this.restBindingName = restBindingName;
    }

    public String getRequestElementSuffix() {
        return requestElementSuffix;
    }

    public void setRequestElementSuffix(String requestElementSuffix) {
        this.requestElementSuffix = requestElementSuffix;
    }

    public boolean isAnonymousTypesAllowed() {
        return anonymousTypesAllowed;
    }

    public void setAnonymousTypesAllowed(boolean anonymousTypesAllowed) {
        this.anonymousTypesAllowed = anonymousTypesAllowed;
    }
}
