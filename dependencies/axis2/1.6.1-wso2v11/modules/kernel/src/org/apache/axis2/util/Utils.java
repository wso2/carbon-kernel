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


package org.apache.axis2.util;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.ServiceObjectSupplier;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.description.Version;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static void addHandler(Flow flow, Handler handler, String phaseName) {
        HandlerDescription handlerDesc = new HandlerDescription(handler.getName());
        PhaseRule rule = new PhaseRule(phaseName);

        handlerDesc.setRules(rule);
        handler.init(handlerDesc);
        handlerDesc.setHandler(handler);
        flow.addHandler(handlerDesc);
    }

    /**
     * @see org.apache.axis2.util.MessageContextBuilder:createOutMessageContext()
     * @deprecated (post1.1branch)
     */
    public static MessageContext createOutMessageContext(MessageContext inMessageContext)
            throws AxisFault {
        return MessageContextBuilder.createOutMessageContext(inMessageContext);
    }

    public static AxisService createSimpleService(QName serviceName, String className, QName opName)
            throws AxisFault {
        return createSimpleService(serviceName, new RawXMLINOutMessageReceiver(), className,
                                   opName);
    }

    public static AxisService createSimpleServiceforClient(QName serviceName, String className,
                                                           QName opName)
            throws AxisFault {
        return createSimpleServiceforClient(serviceName, new RawXMLINOutMessageReceiver(),
                                            className,
                                            opName);
    }


    public static AxisService createSimpleInOnlyService(QName serviceName,
                                                        MessageReceiver messageReceiver,
                                                        QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(getContextClassLoader_DoPriv());

        AxisOperation axisOp = new InOnlyAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + opName.getLocalPart(),
                                     axisOp);

        return service;
    }

    private static ClassLoader getContextClassLoader_DoPriv() {
        return (ClassLoader) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }


    public static AxisService createSimpleService(QName serviceName,
                                                  MessageReceiver messageReceiver, String className,
                                                  QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(getContextClassLoader_DoPriv());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new InOutAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + opName.getLocalPart(),
                                     axisOp);

        return service;
    }

    public static AxisService createSimpleServiceforClient(QName serviceName,
                                                           MessageReceiver messageReceiver,
                                                           String className,
                                                           QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(getContextClassLoader_DoPriv());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new OutInAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public static ServiceContext fillContextInformation(AxisService axisService,
                                                        ConfigurationContext configurationContext)
            throws AxisFault {

        // 2. if null, create new opCtxt
        // fill the service group context and service context info
        return fillServiceContextAndServiceGroupContext(axisService, configurationContext);
    }

    private static ServiceContext fillServiceContextAndServiceGroupContext(AxisService axisService,
                                                                           ConfigurationContext configurationContext)
            throws AxisFault {
        String serviceGroupContextId = UIDGenerator.generateURNString();
        ServiceGroupContext serviceGroupContext =
                configurationContext.createServiceGroupContext(axisService.getAxisServiceGroup());

        serviceGroupContext.setId(serviceGroupContextId);
        configurationContext.addServiceGroupContextIntoSoapSessionTable(serviceGroupContext);
        return serviceGroupContext.getServiceContext(axisService);
    }

    /**
     * Break a full path into pieces
     *
     * @return an array where element [0] always contains the service, and element 1, if not null, contains
     *         the path after the first element. all ? parameters are discarded.
     */
    public static String[] parseRequestURLForServiceAndOperation(String path, String servicePath) {
        if (log.isDebugEnabled()) {
            log.debug("parseRequestURLForServiceAndOperation : [" + path + "][" + servicePath + "]");
        }
        if (path == null) {
            return null;
        }
        String[] values = new String[2];

        // TODO. This is kind of brittle. Any service with the name /services would cause fun.
        int index = path.lastIndexOf(servicePath);
        String service;

        if (-1 != index) {
            int serviceStart = index + servicePath.length();

            if (path.length() > serviceStart + 1) {
                service = path.substring(serviceStart + 1);

                int queryIndex = service.indexOf('?');

                if (queryIndex > 0) {
                    service = service.substring(0, queryIndex);
                }

                int operationIndex = service.indexOf('/');

                if (operationIndex > 0) {
                    values[0] = service.substring(0, operationIndex);
                    values[1] = service.substring(operationIndex + 1);
                    operationIndex = values[1].lastIndexOf('/');
                    if (operationIndex > 0) {
                        values[1] = values[1].substring(operationIndex + 1);
                    }
                } else {
                    values[0] = service;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse request URL [" + path + "][" + servicePath + "]");
            }
        }

        return values;
    }

    /**
     * Gives the service/operation part from the incoming EPR
     * Ex: ..services/foo/bar/Version/getVersion -> foo/bar/Version/getVersion
     * @param path - incoming EPR
     * @param servicePath - Ex: 'services'
     * @return - service/operation part
     */
    public static String getServiceAndOperationPart(String path, String servicePath) {
        if (path == null) {
            return null;
        }

        //with this chances that substring matching a different place in the URL is reduced
        if(!servicePath.endsWith("/")){
        	servicePath = servicePath+"/";
        }

        int index = path.lastIndexOf(servicePath);
        String serviceOpPart = null;

        if (-1 != index) {
            int serviceStart = index + servicePath.length();

            //get the string after services path
            if (path.length() > serviceStart) {
                serviceOpPart = path.substring(serviceStart);

                //remove everything after ?
                int queryIndex = serviceOpPart.indexOf('?');
                if (queryIndex > 0) {
                    serviceOpPart = serviceOpPart.substring(0, queryIndex);
                }
            }
        }
        return serviceOpPart;
    }

    /**
     * Compute the operation path from request URI using the servince name. Service name can be a
     * normal one or a hierarchical one.
     * Ex:  ../services/Echo/echoString -> echoString
     *      ../services/foo/1.0.0/Echo/echoString -> echoString
     *      ../services/Echo/ -> null
     * @param path - request URI
     * @param serviceName - service name
     * @return - operation name if any, else null
     */
    public static String getOperationName(String path, String serviceName) {
        if (path == null || serviceName == null) {
            return null;
        }
        String[] temp = path.split(serviceName + "/");
        String operationName = null;
        if (temp.length > 1) {
            operationName = temp[temp.length - 1];
        } else {
            //this scenario occurs if the endpoint name is there in the URL after service name
            temp = path.split(serviceName + "\\.");
            if (temp.length > 1) {
                operationName = temp[temp.length - 1];
                operationName = operationName.substring(operationName.indexOf('/') + 1);
            }
        }

        if (operationName != null) {
            //remove everyting after '?'
            int queryIndex = operationName.indexOf('?');
            if (queryIndex > 0) {
                operationName = operationName.substring(0, queryIndex);
            }
            //take the part upto / as the operation name
            if (operationName.indexOf("/") != -1) {
                operationName = operationName.substring(0, operationName.indexOf("/"));
            }
        }
        return operationName;
    }

    public static ConfigurationContext getNewConfigurationContext(String repositry)
            throws Exception {
        final File file = new File(repositry);
        boolean exists = exists(file);
        if (!exists) {
            throw new Exception("repository directory " + file.getAbsolutePath()
                                + " does not exists");
        }
        File axis2xml = new File(file, "axis.xml");
        String axis2xmlString = null;
        if (exists(axis2xml)) {
            axis2xmlString = axis2xml.getName();
        }
        String path = (String) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                    public String run() {
                        return file.getAbsolutePath();
                    }
                }
        );
        return ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(path, axis2xmlString);
    }

    private static boolean exists(final File file) {
        Boolean exists = (Boolean) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return new Boolean(file.exists());
                    }
                }
        );
        return exists.booleanValue();
    }

    public static String getParameterValue(Parameter param) {
        if (param == null) {
            return null;
        } else {
            return (String) param.getValue();
        }
    }

    public static String getModuleName(String moduleName, String moduleVersion) {
        if (moduleVersion != null && !moduleVersion.isEmpty()) {
            moduleName = moduleName + "-" + moduleVersion;
        }
        return moduleName;
    }

    private static final String ILLEGAL_CHARACTERS = "/\n\r\t\0\f`?*\\<>|\":";
    public static boolean isValidModuleName(String moduleName) {
        for (int i = 0; i < moduleName.length(); i++) {
            char c = moduleName.charAt(i);
            if ((c > 127) || (ILLEGAL_CHARACTERS.indexOf(c) >= 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * - if he trying to engage the same module then method will returen false
     * - else it will return true
     *
     */
    public static boolean checkVersion(Version module1version,
                                       Version module2version) throws AxisFault {
        if ((module1version !=null && !module1version.equals(module2version)) ||
                module2version !=null && !module2version.equals(module1version)) {
            throw new AxisFault("trying to engage two different module versions " +
                    module1version + " : " + module2version);
        }
        return true;
    }

    public static void calculateDefaultModuleVersion(HashMap modules,
                                                     AxisConfiguration axisConfig) {
        Iterator allModules = modules.values().iterator();
        Map<String,Version> defaultModules = new HashMap<String,Version>();
        while (allModules.hasNext()) {
            AxisModule axisModule = (AxisModule) allModules.next();
            String name = axisModule.getName();
            Version currentDefaultVersion = defaultModules.get(name);
            Version version = axisModule.getVersion();
            if (currentDefaultVersion == null ||
                    (version != null && version.compareTo(currentDefaultVersion) > 0)) {
                defaultModules.put(name, version);
            }
        }
        Iterator def_mod_itr = defaultModules.keySet().iterator();
        while (def_mod_itr.hasNext()) {
            String moduleName = (String) def_mod_itr.next();
            Version version = defaultModules.get(moduleName);
            axisConfig.addDefaultModuleVersion(moduleName, version == null ? null : version.toString());
        }
    }

    /**
     * Check if a MessageContext property is true.
     *
     * @param messageContext the MessageContext
     * @param propertyName   the property name
     * @return true if the property is Boolean.TRUE, "true", 1, etc. or false otherwise
     * @deprecated please use MessageContext.isTrue(propertyName) instead
     */
    public static boolean isExplicitlyTrue(MessageContext messageContext, String propertyName) {
        Object flag = messageContext.getProperty(propertyName);
        return JavaUtils.isTrueExplicitly(flag);
    }

    /**
     * Maps the String URI of the Message exchange pattern to a integer.
     * Further, in the first lookup, it will cache the looked
     * up value so that the subsequent method calls are extremely efficient.
     */
    @SuppressWarnings("deprecation")
    public static int getAxisSpecifMEPConstant(String messageExchangePattern) {


        int mepConstant = WSDLConstants.MEP_CONSTANT_INVALID;

        if (WSDL2Constants.MEP_URI_IN_OUT.equals(messageExchangePattern) ||
            WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(messageExchangePattern) ||
            WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT.equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_OUT;
        } else if (
                WSDL2Constants.MEP_URI_IN_ONLY.equals(messageExchangePattern) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(messageExchangePattern) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY
                        .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                .equals(messageExchangePattern) ||
                                                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT
                                                        .equals(messageExchangePattern) ||
                                                                                        WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT
                                                                                                .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT;
        } else if (WSDL2Constants.MEP_URI_OUT_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN
                           .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_IN;
        } else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants
                                                                   .MEP_URI_OUT_ONLY.equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_ONLY;
        } else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN;
        } else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY;
        }

        if (mepConstant == WSDLConstants.MEP_CONSTANT_INVALID) {
            throw new AxisError(Messages.getMessage("mepmappingerror"));
        }


        return mepConstant;
    }

    /**
     * Get an AxisFault object to represent the SOAPFault in the SOAPEnvelope attached
     * to the provided MessageContext. This first check for an already extracted AxisFault
     * and otherwise does a simple extract.
     * <p/>
     * MUST NOT be passed a MessageContext which does not contain a SOAPFault
     *
     * @param messageContext
     * @return
     */
    public static AxisFault getInboundFaultFromMessageContext(MessageContext messageContext) {
        // Get the fault if it's already been extracted by a handler
        AxisFault result = (AxisFault) messageContext.getProperty(Constants.INBOUND_FAULT_OVERRIDE);
        // Else, extract it from the SOAPBody
        if (result == null) {
            SOAPEnvelope envelope = messageContext.getEnvelope();
            SOAPFault soapFault;
            SOAPBody soapBody;
            if (envelope != null && (soapBody = envelope.getBody()) != null) {
                if ((soapFault = soapBody.getFault()) != null) {
                    return new AxisFault(soapFault, messageContext);
                }
                // If its a REST response the content is not a SOAP envelop and hence we will
                // Have use the soap body as the exception
                if (messageContext.isDoingREST() && soapBody.getFirstElement() != null) {
                	AxisFault fault = new AxisFault(soapBody.getFirstElement().toString());
                	fault.setDetail(soapBody.getFirstElement());
                	return fault;
                }

                // if axis2 receives an rest type fault for an soap message then message context
                // has not been set to isDoingREST() but in this case we can detect it by using
                // the message type. so if the message type is application/xml we assum it as an rest call
                if ((messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE) != null) &&
                        messageContext.getProperty(Constants.Configuration.MESSAGE_TYPE).equals(HTTPConstants.MEDIA_TYPE_APPLICATION_XML)){
                     if (soapBody.getFirstElement() != null){
                    	 AxisFault fault = new AxisFault(soapBody.getFirstElement().toString());
                     	 fault.setDetail(soapBody.getFirstElement());
                     	 return fault;
                     } else {
                         return new AxisFault("application/xml type error received.");
                     }
                }
            }
            // Not going to be able to
            throw new IllegalArgumentException(
                    "The MessageContext does not have an associated SOAPFault.");
        }
        return result;
    }

    /**
     * This method will provide the logic needed to retrieve an Object's classloader
     * in a Java 2 Security compliant manner.
     */
    public static ClassLoader getObjectClassLoader(final Object object) {
        if(object == null) {
            return null;
        }
        else {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return object.getClass().getClassLoader();
                }
            });
        }
    }

    public static int getMtomThreshold(MessageContext msgCtxt){
    	Integer value = null;
        if(!msgCtxt.isServerSide()){
	        value = (Integer)msgCtxt.getProperty(Constants.Configuration.MTOM_THRESHOLD);
        }else{
        	Parameter param = msgCtxt.getParameter(Constants.Configuration.MTOM_THRESHOLD);
        	if(param!=null){
        		value = (Integer)param.getValue();
        	}
        }
        int threshold = (value!=null)?value.intValue():0;
        if(log.isDebugEnabled()){
        	log.debug("MTOM optimized Threshold value ="+threshold);
        }
        return threshold;
    }
     /**
     * Returns the ip address to be used for the replyto epr
     * CAUTION:
     * This will go through all the available network interfaces and will try to return an ip address.
     * First this will try to get the first IP which is not loopback address (127.0.0.1). If none is found
     * then this will return this will return 127.0.0.1.
     * This will <b>not<b> consider IPv6 addresses.
     * <p/>
     * TODO:
     * - Improve this logic to genaralize it a bit more
     * - Obtain the ip to be used here from the Call API
     *
     * @return Returns String.
     * @throws java.net.SocketException
      */
    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    /**
     * First check whether the hostname parameter is there in AxisConfiguration (axis2.xml) ,
     * if it is there then this will retun that as the host name , o.w will return the IP address.
     */
    public static String getIpAddress(AxisConfiguration axisConfiguration) throws SocketException {
        if(axisConfiguration!=null){
            Parameter param = axisConfiguration.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                String  hostAddress = ((String) param.getValue()).trim();
                if(hostAddress!=null){
                    return hostAddress;
                }
            }
        }
        return getIpAddress();
    }

    /**
     * First check whether the hostname parameter is there in AxisConfiguration (axis2.xml) ,
     * if it is there then this will return that as the host name , o.w will return the IP address.
     * @param axisConfiguration
     * @return hostname
     */
    public static String getHostname(AxisConfiguration axisConfiguration) {
        if(axisConfiguration!=null){
            Parameter param = axisConfiguration.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                String  hostAddress = ((String) param.getValue()).trim();
                if(hostAddress!=null){
                    return hostAddress;
                }
            }
        }
        return null;
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }

    /**
     * Get the scheme part from a URI (or URL).
     *
     * @param uri the URI
     * @return the scheme of the URI
     */
    public static String getURIScheme(String uri) {
        int index = uri.indexOf(':');
        return index > 0 ? uri.substring(0, index) : null;
    }

    public static String sanitizeWebOutput(String text) {
        text = text.replaceAll("<", "&lt;");
        return text;
    }

    /**
     * Create a service object for a given service. The method first looks for
     * the {@link Constants#SERVICE_OBJECT_SUPPLIER} service parameter and if
     * this parameter is present, it will use the specified class to create the
     * service object. If the parameter is not present, it will create an
     * instance of the class specified by the {@link Constants#SERVICE_CLASS}
     * parameter.
     *
     * @param service
     *            the service
     * @return The service object or <code>null</code> if neither the
     *         {@link Constants#SERVICE_OBJECT_SUPPLIER} nor the
     *         {@link Constants#SERVICE_CLASS} parameter was found on the
     *         service, i.e. if the service doesn't specify how to create a
     *         service object. If the return value is non null, it will always
     *         be a newly created instance.
     * @throws AxisFault
     *             if an error occurred while attempting to instantiate the
     *             service object
     */
    public static Object createServiceObject(final AxisService service) throws AxisFault {
        try {
            ClassLoader classLoader = service.getClassLoader();

            // allow alternative definition of makeNewServiceObject
            Parameter serviceObjectSupplierParam =
                    service.getParameter(Constants.SERVICE_OBJECT_SUPPLIER);
            if (serviceObjectSupplierParam != null) {
                final Class<?> serviceObjectSupplierClass = Loader.loadClass(classLoader, ((String)
                        serviceObjectSupplierParam.getValue()).trim());
                if (ServiceObjectSupplier.class.isAssignableFrom(serviceObjectSupplierClass)) {
                    ServiceObjectSupplier serviceObjectSupplier = org.apache.axis2.java.security.AccessController.doPrivileged(
                            new PrivilegedExceptionAction<ServiceObjectSupplier>() {
                                public ServiceObjectSupplier run() throws InstantiationException, IllegalAccessException {
                                    return (ServiceObjectSupplier)serviceObjectSupplierClass.newInstance();
                                }
                            }
                    );
                    return serviceObjectSupplier.getServiceObject(service);
                } else {
                    // Prior to r439555 service object suppliers were actually defined by a static method
                    // with a given signature defined on an arbitrary class. The ServiceObjectSupplier
                    // interface was only introduced by r439555. We still support the old way, but
                    // issue a warning inviting the user to provide a proper ServiceObjectSupplier
                    // implementation.

                    // Find static getServiceObject() method, call it if there
                    final Method method = org.apache.axis2.java.security.AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Method>() {
                                public Method run() throws NoSuchMethodException {
                                    return serviceObjectSupplierClass.getMethod("getServiceObject",
                                            AxisService.class);
                                }
                            }
                    );
                    log.warn("The class specified by the " + Constants.SERVICE_OBJECT_SUPPLIER
                            + " property on service " + service.getName() + " does not implement the "
                            + ServiceObjectSupplier.class.getName() + " interface. This is deprecated.");
                    return org.apache.axis2.java.security.AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Object>() {
                                public Object run() throws InvocationTargetException, IllegalAccessException, InstantiationException {
                                    return method.invoke(serviceObjectSupplierClass.newInstance(), new Object[]{service});
                                }
                            }
                    );
                }
            } else {
                Parameter serviceClassParam = service.getParameter(Constants.SERVICE_CLASS);
                if (serviceClassParam != null) {
                    final Class<?> serviceClass = Loader.loadClass(
                            classLoader,
                            ((String) serviceClassParam.getValue()).trim());
                    String className = ((String) serviceClassParam.getValue()).trim();
                    Class serviceObjectMaker = Loader.loadClass(classLoader, className);
                    if (serviceObjectMaker.getModifiers() != Modifier.PUBLIC) {
                        throw new AxisFault("Service class " + className +
                                            " must have public as access Modifier");
                    }
                    return org.apache.axis2.java.security.AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Object>() {
                                public Object run() throws InstantiationException, IllegalAccessException {
                                    return serviceClass.newInstance();
                                }
                            }
                    );
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Get the service class for a given service. This method will first check
     * the {@link Constants#SERVICE_CLASS} service parameter and if that
     * parameter is not present, inspect the instance returned by the service
     * object supplier specified by {@link Constants#SERVICE_OBJECT_SUPPLIER}.
     *
     * @param service
     *            the service
     * @return The service class or <code>null</code> if neither the
     *         {@link Constants#SERVICE_CLASS} nor the
     *         {@link Constants#SERVICE_OBJECT_SUPPLIER} parameter was found on
     *         the service, i.e. if the service doesn't specify a service class.
     * @throws AxisFault
     *             if an error occurred while attempting to load the service
     *             class or to instantiate the service object
     */
    public static Class<?> getServiceClass(AxisService service) throws AxisFault {
        Parameter serviceClassParam = service.getParameter(Constants.SERVICE_CLASS);
        if (serviceClassParam != null) {
            try {
                return Loader.loadClass(service.getClassLoader(),
                        ((String) serviceClassParam.getValue()).trim());
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        } else {
            Object serviceObject = createServiceObject(service);
            return serviceObject == null ? null : serviceObject.getClass();
        }
    }

    /**
     * this is to make is backward compatible. Get rid of this at the next major release.
     * @param messageContext
     * @return
     */

    public static boolean isClientThreadNonBlockingPropertySet(MessageContext messageContext){
    	Object val =  messageContext.getProperty(
                MessageContext.CLIENT_API_NON_BLOCKING);
    	if(val != null && ((Boolean)val).booleanValue()){
    		return true;
    	}else{
    		//put the string inline as this is to be removed
    		val =  messageContext.getProperty("transportNonBlocking");
    		return val != null && ((Boolean)val).booleanValue();
    	}
    }
}
