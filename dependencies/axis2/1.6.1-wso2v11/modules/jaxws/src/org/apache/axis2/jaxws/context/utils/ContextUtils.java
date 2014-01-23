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

package org.apache.axis2.jaxws.context.utils;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.addressing.util.ReferenceParameterList;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.impl.EndpointLifecycleManagerImpl;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class ContextUtils {
    private static final Log log = LogFactory.getLog(ContextUtils.class);
    
    private static final String WEBSERVICE_MESSAGE_CONTEXT = "javax.xml.ws.WebServiceContext";

    /**
     * Adds the appropriate properties to the MessageContext that the user will see
     *
     * @param soapMessageContext
     * @param jaxwsMessageContext
     */
    public static void addProperties(SOAPMessageContext soapMessageContext,
                                     MessageContext jaxwsMessageContext) {

        // Copy Axis2 MessageContext properties.  It's possible that some set of Axis2 handlers
        // have run and placed some properties in the context that need to be visible.
        soapMessageContext.putAll(jaxwsMessageContext.getProperties());

        EndpointDescription description = jaxwsMessageContext.getEndpointDescription();
        if (description !=null) {
            // Set the WSDL properties
            ServiceDescription sd =
                    description.getServiceDescription();
            if (sd != null) {
                String wsdlLocation = ((ServiceDescriptionWSDL)sd).getWSDLLocation();
                if (wsdlLocation != null && !"".equals(wsdlLocation)) {
                    URI wsdlLocationURI = JavaUtils.createURI(wsdlLocation);
                    if (wsdlLocationURI == null) {
                        log.warn(Messages.getMessage("addPropertiesErr",
                                 wsdlLocation.toString(),description.getServiceQName().toString()));
                    }
                    setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_DESCRIPTION, wsdlLocationURI, true);
                }
                setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_SERVICE, description.getServiceQName(), true);
            }
        }

        //Lazily provide a list of available reference parameters.
        org.apache.axis2.context.MessageContext msgContext =
            jaxwsMessageContext.getAxisMessageContext();
        SOAPHeader header = null;
        if (msgContext != null &&
            msgContext.getEnvelope() != null) {
            header = msgContext.getEnvelope().getHeader();
        }
        List<Element> list = new ReferenceParameterList(header);
        
        setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.REFERENCE_PARAMETERS, list);
        if (log.isDebugEnabled()) {
            log.debug("Added reference parameter list.");
        }
        
        // If we are running within a servlet container, then JAX-WS requires that the
        // servlet related properties be set on the MessageContext
        ServletContext servletContext = 
            (ServletContext)jaxwsMessageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT);
        if (servletContext != null) {
            log.debug("Servlet Context Set");
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT, servletContext);
        } else {
            log.debug("Servlet Context not found");
        }

        HttpServletRequest req = (HttpServletRequest)jaxwsMessageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        if (req == null) {
            if (log.isDebugEnabled()) {
                log.debug("HTTPServletRequest not found");
            }
        } else {
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.SERVLET_REQUEST, req);
            if (log.isDebugEnabled()) {
                log.debug("SERVLET_REQUEST Set");
            }

            String pathInfo = null;
            try {
                pathInfo = req.getPathInfo();
            } catch (Throwable t){
                log.debug("exception in getPathInfo", t);
            }
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.PATH_INFO, pathInfo);
            if (log.isDebugEnabled()) {
                if (pathInfo != null) {
                    log.debug("HTTP_REQUEST_PATHINFO Set");
                } else {
                    log.debug("HTTP_REQUEST_PATHINFO not found");
                }
            }
            String queryString = req.getQueryString();
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.QUERY_STRING, queryString);
            if (log.isDebugEnabled()) {
                if (queryString != null) {
                    log.debug("HTTP_REQUEST_QUERYSTRING Set");
                } else {
                    log.debug("HTTP_REQUEST_QUERYSTRING not found");
                }
            }
            String method = req.getMethod();
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD, method);
            if (log.isDebugEnabled()) {
                if (method != null) {
                    log.debug("HTTP_REQUEST_METHOD Set");
                } else {
                    log.debug("HTTP_REQUEST_METHOD not found");
                }
            }

        }
        HttpServletResponse res = (HttpServletResponse)jaxwsMessageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        if (res == null) {
            if (log.isDebugEnabled()) {
                log.debug("Servlet Response not found");
            }
        } else {
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE, res);
            if (log.isDebugEnabled()) {
                log.debug("SERVLET_RESPONSE Set");
            }
        }
        
    }

    public static void addWSDLProperties(MessageContext jaxwsMessageContext) {
        addWSDLProperties(jaxwsMessageContext, getSOAPMessageContext(jaxwsMessageContext));                
    }
    
    public static void addWSDLProperties(MessageContext jaxwsMessageContext,
                                         SOAPMessageContext soapMessageContext) {
        OperationDescription op = jaxwsMessageContext.getOperationDescription();

        if (op != null && soapMessageContext != null) {
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_OPERATION, op.getName(), true);

            EndpointInterfaceDescription eid = op.getEndpointInterfaceDescription();
            if (eid != null) {
                EndpointDescription ed = eid.getEndpointDescription();
                QName portType = eid.getPortType();
                if (portType == null || portType.getLocalPart().length() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Did not get port type from EndpointInterfaceDescription, attempting to get PortType from EndpointDescription");
                    }
                }
                if (ed != null) {
                    setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_PORT, ed.getPortQName(), true);
                }
                setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_INTERFACE, portType, true);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to read WSDL operation, port and interface properties");
            }
        }
    }
    
    public static void addWSDLProperties_provider(MessageContext jaxwsMessageContext) {
        addWSDLProperties_provider(jaxwsMessageContext, getSOAPMessageContext(jaxwsMessageContext));
    }
    
    public static void addWSDLProperties_provider(MessageContext jaxwsMessageContext,
                                                  SOAPMessageContext soapMessageContext) {
        QName op = jaxwsMessageContext.getOperationName();    	
        
        if (op != null && soapMessageContext != null) {
            setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_OPERATION, op, true);

            //EndpointInterfaceDescription eid = op.getEndpointInterfaceDescription();
            EndpointDescription ed = jaxwsMessageContext.getEndpointDescription();
            
            if (ed != null) {
                setProperty(soapMessageContext, javax.xml.ws.handler.MessageContext.WSDL_PORT, ed.getPortQName(), true);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to read WSDL operation, port and interface properties");
            }
        }
    }
    
    private static SOAPMessageContext getSOAPMessageContext(MessageContext jaxwsMessageContext) {
        org.apache.axis2.context.MessageContext msgContext =
            jaxwsMessageContext.getAxisMessageContext();
        ServiceContext serviceContext = msgContext.getServiceContext();
        SOAPMessageContext soapMessageContext = null;
        if (serviceContext != null) {
            WebServiceContext wsc =
                (WebServiceContext)serviceContext.getProperty(EndpointLifecycleManagerImpl.WEBSERVICE_MESSAGE_CONTEXT);
            if (wsc != null) {
                soapMessageContext = (SOAPMessageContext)wsc.getMessageContext();
            }
        }
        return soapMessageContext;
    }
       
    private static void setProperty(SOAPMessageContext context, String name, Object value) {
        setProperty(context, name, value, false);
    }
    
    private static void setProperty(SOAPMessageContext context, String name, Object value, boolean logMessage) {
        context.put(name, value);
        context.setScope(name, Scope.APPLICATION);
        if (logMessage && log.isDebugEnabled()) {
            log.debug(name + " :" + value);
        }
    }
    
    /**
     * Release the contents of the WebServiceContext.
     * @param mc
     */
    public static void releaseWebServiceContextResources(MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("Find and release WebServiceContext resources");
        }
        WebServiceContext wsc = null;
        // If a WebServiceContext was created, get it from the MessageContext
        if (mc != null) {
            wsc = (WebServiceContext) mc.getProperty(WEBSERVICE_MESSAGE_CONTEXT);
        } 
        
        if (wsc != null && wsc instanceof WebServiceContextImpl) {
            ((WebServiceContextImpl) wsc).releaseResources();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("A WebServiceContext was not found");
            }
        }
    }
    
    /**
     * isJAXBRemoveIllegalChars
     * 
     * Determine if illegal characters should be removed when JAXB beans are written
     * 
     * @see Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS 
     * 
     * @param mc
     * @return true if the property is set
     */
    public static boolean isJAXBRemoveIllegalChars(org.apache.axis2.context.MessageContext mc) {
        boolean rc = _isJAXBRemoveIllegalChars(mc);

        // If not true, check the related MessageContext
        if (!rc) {
            if (mc != null && mc != null) {
                rc = _isJAXBRemoveIllegalChars(getRelatedMessageContext(mc));
            }
        }
        return rc;
    }
    
    /**
     * getRelatedMessageContext
     * @param mc Axis2 MessageContext
     * @return related MessageContext
     */
    private static org.apache.axis2.context.MessageContext 
        getRelatedMessageContext(org.apache.axis2.context.MessageContext mc) {
        if (log.isDebugEnabled()) {
            log.debug("Enter getRelatedMessageContext for:" + mc);
        }
        org.apache.axis2.context.MessageContext relatedMC = null;
        if (mc != null) {
            OperationContext oc = mc.getOperationContext();
            if (oc != null) {
                try {
                    relatedMC = oc.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (relatedMC == mc) {
                        relatedMC = oc.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    }
                } catch (AxisFault e) {
                    // TODO This should never occur in this scenario, swallow and continue
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Exit getRelatedMessageContext related messageContext is" + relatedMC);
        }
        return relatedMC;
    }

    /**
     * _isJAXBRemoveIllegalChars
     * 
     * Determine if illegal characters should be removed when JAXB beans are written
     * 
     * @see Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS 
     * 
     * @param mc
     * @return true if property is set
     */
    private static boolean _isJAXBRemoveIllegalChars(MessageContext mc) {
        
        boolean value = false;
        if (mc == null) {
            if (log.isDebugEnabled()) {
                log.debug("_isJAXBRemoveIllegalChars returns false due to missing MessageContext");
            }
            return false;
        }
        
        // If not found, delegate to the Axis2 MessageContext
        if (mc.getAxisMessageContext() != null) {
            return _isJAXBRemoveIllegalChars(mc.getAxisMessageContext());
        }
        return value;
    }
    
    /**
     * _isJAXBRemoveIllegalChars
     * 
     * Determine if illegal characters should be removed when JAXB beans are written
     * 
     * @see Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS 
     * 
     * @param mc
     * @return true if property is set
     */
    private static boolean _isJAXBRemoveIllegalChars(org.apache.axis2.context.MessageContext mc) {
        
        boolean value = false;
        if (mc == null) {
            if (log.isDebugEnabled()) {
                log.debug("_isJAXBRemoveIllegalChars returns false due to missing MessageContext");
            }
            return false;
        }
        
        // First examine the local property on the axis2 MessageContext 
        Boolean property = (Boolean) mc.getLocalProperty(
                Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS, false);
        if (property != null) {
            value = property.booleanValue();
            if (log.isDebugEnabled()) {
                log.debug("_isJAXBRemoveIllegalChars returns " + value + " per axis2 MessageContext property " + 
                        Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS);
            }
            return value;
        }
        
        
        // Now look at the configuration parameter
        ConfigurationContext cc = mc.getConfigurationContext();
        if (cc != null) {
            AxisConfiguration baseConfig = cc.getAxisConfiguration();
            if (baseConfig  != null) {
                Parameter p = baseConfig.getParameter(Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS);
                if (p != null) {
                    value = JavaUtils.isTrue(p.getValue());
                    if (log.isDebugEnabled()) {
                        log.debug("isJAXBRemoveIllegalChars returns " + value + " per inspection of Configuration property " + 
                                Constants.JAXWS_JAXB_WRITE_REMOVE_ILLEGAL_CHARS);
                    }
                    return value;
                }
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("isJAXBRemoveIllegalChars returns the default: false");
        }
        return false;
    }
}
