/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.base;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.format.BinaryFormatter;
import org.apache.axis2.format.PlainTextFormatter;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class BaseUtils {

    private static final Log log = LogFactory.getLog(BaseUtils.class);

    /**
     * Return a QName from the String passed in of the form {ns}element
     * @param obj a QName or a String containing a QName in {ns}element form
     * @return a corresponding QName object
     */
    public static QName getQNameFromString(Object obj) {
        String value;
        if (obj instanceof QName) {
            return (QName) obj;
        } else {
            value = obj.toString();
        }
        int open = value.indexOf('{');
        int close = value.indexOf('}');
        if (close > open && open > -1 && value.length() > close) {
            return new QName(value.substring(open+1, close-open), value.substring(close+1));
        } else {
            return new QName(value);
        }
    }

    /**
     * Marks the given service as faulty with the given comment
     *
     * @param serviceName service name
     * @param msg         comment for being faulty
     * @param axisCfg     configuration context
     */
    public static void markServiceAsFaulty(String serviceName, String msg,
                                           AxisConfiguration axisCfg) {
        if (serviceName != null) {
            try {
                AxisService service = axisCfg.getService(serviceName);
                if (service != null) {
                    axisCfg.getFaultyServices().put(service.getName(), msg);
                }
            } catch (AxisFault axisFault) {
                log.warn("Error marking service : " + serviceName + " as faulty", axisFault);
            }
        }
    }

    /**
     * Create a SOAP envelope using SOAP 1.1 or 1.2 depending on the namespace
     * @param in InputStream for the payload
     * @param namespace the SOAP namespace
     * @return the SOAP envelope for the correct version
     * @throws javax.xml.stream.XMLStreamException on error
     */
    public static SOAPEnvelope getEnvelope(InputStream in, String namespace) throws XMLStreamException {

        try {
            in.reset();
        } catch (IOException ignore) {}
        XMLStreamReader xmlreader =
            StAXUtils.createXMLStreamReader(in, MessageContext.DEFAULT_CHAR_SET_ENCODING);
        StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader, namespace);
        return (SOAPEnvelope) builder.getDocumentElement();
    }

    /**
     * Get the OMOutput format for the given message
     * @param msgContext the axis message context
     * @return the OMOutput format to be used
     */
    public static OMOutputFormat getOMOutputFormat(MessageContext msgContext) {

        OMOutputFormat format = new OMOutputFormat();
        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));
        msgContext.setDoingREST(TransportUtils.isDoingREST(msgContext));
        format.setSOAP11(msgContext.isSOAP11());
        format.setDoOptimize(msgContext.isDoingMTOM());
        format.setDoingSWA(msgContext.isDoingSwA());

        format.setCharSetEncoding(TransportUtils.getCharSetEncoding(msgContext));
        Object mimeBoundaryProperty = msgContext.getProperty(Constants.Configuration.MIME_BOUNDARY);
        if (mimeBoundaryProperty != null) {
            format.setMimeBoundary((String) mimeBoundaryProperty);
        }
        return format;
    }
    
    /**
     * Get the MessageFormatter for the given message.
     * @param msgContext the axis message context
     * @return the MessageFormatter to be used
     */
    public static MessageFormatter getMessageFormatter(MessageContext msgContext) {
        // check the first element of the SOAP body, do we have content wrapped using the
        // default wrapper elements for binary (BaseConstants.DEFAULT_BINARY_WRAPPER) or
        // text (BaseConstants.DEFAULT_TEXT_WRAPPER) ? If so, select the appropriate
        // message formatter directly ...
        OMElement firstChild = msgContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            if (BaseConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
                return new BinaryFormatter();
            } else if (BaseConstants.DEFAULT_TEXT_WRAPPER.equals(firstChild.getQName())) {
                return new PlainTextFormatter();
            }
        }
        
        // ... otherwise, let Axis choose the right message formatter:
        try {
            return MessageProcessorSelector.getMessageFormatter(msgContext);
        } catch (AxisFault axisFault) {
            throw new BaseTransportException("Unable to get the message formatter to use");
        }
    }

    protected static void handleException(String s) {
        log.error(s);
        throw new BaseTransportException(s);
    }

    protected static void handleException(String s, Exception e) {
        log.error(s, e);
        throw new BaseTransportException(s, e);
    }

    /**
     * Utility method to check if a string is null or empty
     * @param str the string to check
     * @return true if the string is null or empty
     */
    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;    
    }

    public static boolean isUsingTransport(AxisService service, String transportName) {
        boolean process = service.isEnableAllTransports();
        if (process) {
            return true;

        } else {
            List transports = service.getExposedTransports();
            for (Object transport : transports) {
                if (transportName.equals(transport)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extract the properties from an endpoint reference.
     *
     * @param url an endpoint reference
     * @return the extracted properties
     */
    public static Hashtable<String,String> getEPRProperties(String url) {
        Hashtable<String,String> h = new Hashtable<String,String>();
        int propPos = url.indexOf("?");
        if (propPos != -1) {
            StringTokenizer st = new StringTokenizer(url.substring(propPos + 1), "&");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int sep = token.indexOf("=");
                if (sep != -1) {
                    h.put(token.substring(0, sep), token.substring(sep + 1));
                } else {
                    // ignore, what else can we do?
                }
            }
        }
        return h;
    }

    /**
     * Loads the properties from a given property file path
     *
     * @param filePath Path of the property file
     * @return Properties loaded from given file
     */
    public static Properties loadProperties(String filePath) {

        Properties properties = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (log.isDebugEnabled()) {
            log.debug("Loading a file '" + filePath + "' from classpath");
        }

        InputStream in = cl.getResourceAsStream(filePath);
        if (in == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load file  '" + filePath + "'");
            }

            filePath = "conf" +
                    File.separatorChar + filePath;
            if (log.isDebugEnabled()) {
                log.debug("Loading a file '" + filePath + "' from classpath");
            }

            in = cl.getResourceAsStream(filePath);
            if (in == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to load file  ' " + filePath + " '");
                }
            }
        }
        if (in != null) {
            try {
                properties.load(in);
            } catch (IOException e) {
                String msg = "Error loading properties from a file at :" + filePath;
                log.error(msg, e);
                throw new BaseTransportException(msg, e);
            }
        }
        return properties;
    }
}
