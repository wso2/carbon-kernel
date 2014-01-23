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


package org.apache.axis2.transport;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.CachedFileDataSource;
import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.SOAPMessageFormatter;
import org.apache.axis2.transport.http.XFormURLEncodedFormatter;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataSource;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class TransportUtils {

    private static final Log log = LogFactory.getLog(TransportUtils.class);

    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext) throws AxisFault {
        return createSOAPMessage(msgContext, false);
    }

    /**
     * This method will create a SOAPEnvelope based on the InputStream stored on
     * the MessageContext. The 'detach' parameter controls whether or not the
     * underlying DetachableInputStream is detached at the end of the method. Note,
     * detaching the DetachableInputStream closes the underlying InputStream that
     * is stored on the MessageContext.
     */
    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
                                                 boolean detach) throws AxisFault {
//        final SOAPEnvelope envelope = msgContext.getEnvelope();
//        if (envelope != null) {
//            if (envelope.isComplete())
//                return envelope;
//        }
        try {
            InputStream inStream = (InputStream) msgContext
                    .getProperty(MessageContext.TRANSPORT_IN);
            msgContext.setProperty(MessageContext.TRANSPORT_IN, null);

            // this inputstram is set by the TransportSender represents a two
            // way transport or a Transport Recevier
            if (inStream == null) {
                throw new AxisFault(Messages.getMessage("inputstreamNull"));
            }

            String contentType = (String) msgContext
                    .getProperty(Constants.Configuration.CONTENT_TYPE);

            // get the type of char encoding
            String charSetEnc = (String) msgContext
                    .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            if (charSetEnc == null && contentType != null) {
                charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
            } else if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

            SOAPEnvelope env = createSOAPMessage(msgContext, inStream, contentType);

            // if we were told to detach, we will make the call here, this is only applicable
            // if a DetachableInputStream instance is found on the MessageContext
            if(detach) {
                DetachableInputStream dis = (DetachableInputStream) msgContext.getProperty(Constants.DETACHABLE_INPUT_STREAM);
                if(dis != null) {
                    if(log.isDebugEnabled()) {
                        log.debug("Detaching input stream after SOAPEnvelope construction");
                    }
                    dis.detach();
                }
            }
            return env;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Objective of this method is to capture the SOAPEnvelope creation logic
     * and make it a common for all the transports and to in/out flows.
     *
     * @param msgContext
     * @param inStream
     * @param contentType
     * @return the SOAPEnvelope
     * @throws AxisFault
     * @throws OMException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    public static SOAPEnvelope createSOAPMessage(MessageContext msgContext,
                                                 InputStream inStream,
                                                 String contentType)
            throws AxisFault, OMException, XMLStreamException,
            FactoryConfigurationError {
        OMElement documentElement = createDocumentElement(contentType, msgContext, inStream);
        return createSOAPEnvelope(documentElement);
    }

    public static SOAPEnvelope createSOAPEnvelope(OMElement documentElement) {
        SOAPEnvelope envelope;
        // Check whether we have received a SOAPEnvelope or not
        if (documentElement instanceof SOAPEnvelope) {
            envelope = (SOAPEnvelope) documentElement;
        } else {
            // If it is not a SOAPEnvelope we wrap that with a fake
            // SOAPEnvelope.
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            envelope = soapFactory.getDefaultEnvelope();
            if (documentElement != null) {
                envelope.getBody().addChild(documentElement);
            }
        }
        return envelope;
    }

    public static OMElement createDocumentElement(String contentType,
                                                  MessageContext msgContext,
                                                  InputStream inStream) throws AxisFault, XMLStreamException {
        OMElement documentElement = null;
        String type = null;
        if (contentType != null) {
            type = getContentType(contentType, msgContext);
            Builder builder = MessageProcessorSelector.getMessageBuilder(type, msgContext);
            if (builder != null) {
	            if (log.isDebugEnabled()) {
	                log.debug("createSOAPEnvelope using Builder (" +
	                          builder.getClass() + ") selected from type (" + type +")");
	            }
                documentElement = builder.processDocument(inStream, contentType, msgContext);
            }
        }
        if (documentElement == null) {
            if (msgContext.isDoingREST()) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not find a Builder for type (" + type + ").  Using REST.");
                }
                StAXBuilder builder = BuilderUtil.getPOXBuilder(inStream, null);
                documentElement = builder.getDocumentElement();
            } else {
                // FIXME making soap defualt for the moment..might effect the
                // performance
                if (log.isDebugEnabled()) {
                    log.debug("Could not find a Builder for type (" + type + ").  Using SOAP.");
                }
                String charSetEnc = (String) msgContext
                        .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
                StAXBuilder builder = BuilderUtil.getSOAPBuilder(inStream, charSetEnc);
                documentElement = builder.getDocumentElement();
            }
        }
        return documentElement;
    }

    public static String getContentType(String contentType, MessageContext msgContext) {
        String type;
        int index = contentType.indexOf(';');
        if (index > 0) {
            type = contentType.substring(0, index);
        } else {
            int commaIndex = contentType.indexOf(',');
            if (commaIndex > 0) {
                type = contentType.substring(0, commaIndex);
            } else {
                type = contentType;
            }
        }
        // Some services send REST responces as text/xml. We should convert it to
        // application/xml if its a REST response, if not it will try to use the SOAPMessageBuilder.
        // isDoingREST should already be properly set by HTTPTransportUtils.initializeMessageContext
        if (msgContext.isDoingREST() && HTTPConstants.MEDIA_TYPE_TEXT_XML.equals(type)) {
            if (msgContext.isServerSide()) {
                if (msgContext.getSoapAction() == null) {
                	// TODO - remove this logic.
                    // type = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
                }
            } else if (!msgContext.isPropertyTrue(Constants.Configuration.SOAP_RESPONSE_MEP)) {
                type = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
            }
        }
        return type;
    }

    /**
     * Extracts and returns the character set encoding from the
     * Content-type header
     * Example:
     * Content-Type: text/xml; charset=utf-8
     *
     * @param contentType
     */
    public static String getCharSetEncoding(String contentType) {
        if (log.isDebugEnabled()) {
            log.debug("Input contentType (" + contentType + ")");
        }
        int index = contentType.indexOf(HTTPConstants.CHAR_SET_ENCODING);

        if (index == -1) {    // Charset encoding not found in the content-type header
            // Using the default UTF-8
            if (log.isDebugEnabled()) {
                log.debug("CharSetEncoding defaulted (" + MessageContext.DEFAULT_CHAR_SET_ENCODING + ")");
            }
            return MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        // If there are spaces around the '=' sign
        int indexOfEq = contentType.indexOf("=", index);

        // There can be situations where "charset" is not the last parameter of the Content-Type header
        int indexOfSemiColon = contentType.indexOf(";", indexOfEq);
        String value;

        if (indexOfSemiColon > 0) {
            value = (contentType.substring(indexOfEq + 1, indexOfSemiColon));
        } else {
            value = (contentType.substring(indexOfEq + 1, contentType.length())).trim();
        }

        // There might be "" around the value - if so remove them
        if (value.indexOf('\"') != -1) {
            value = value.replaceAll("\"", "");
        }
        value = value.trim();
        if (log.isDebugEnabled()) {
            log.debug("CharSetEncoding from content-type (" + value + ")");
        }
        return value;
    }

    public static void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if ((envelope != null) && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();

                // Pick the char set encoding from the msgContext
                String charSetEnc =
                        (String) msgContext
                                .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

                format.setDoOptimize(false);
                format.setDoingSWA(false);
                format.setCharSetEncoding(charSetEnc);
                outputMessage.serializeAndConsume(out, format);
                out.flush();
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("outMessageNull"));
        }
    }

   
    /**
     * @param contentType          The contentType of the incoming message.  It may be null
     * @param defaultSOAPNamespace Usually set the version that is expected.  This a fallback if the contentType is unavailable or
     *                             does not match our expectations
     * @return null or the soap namespace.  A null indicates that the message will be interpretted as a non-SOAP (i.e. REST) message
     */
    private static String getSOAPNamespaceFromContentType(String contentType,
                                                          String defaultSOAPNamespace) {

        String returnNS = defaultSOAPNamespace;
        // Discriminate using the content Type
        if (contentType != null) {

            /*
            * SOAP11 content-type is "text/xml"
            * SOAP12 content-type is "application/soap+xml"
            *
            * What about other content-types?
            *
            * TODO: I'm not fully convinced this method is complete, given the media types
            * listed in HTTPConstants.  Should we assume all application/* is SOAP12?
            * Should we assume all text/* is SOAP11?
            *
            * So, we'll follow this pattern:
            * 1)  find the content-type main setting
            * 2)  if (1) not understood, find the "type=" param
            * Thilina: I merged (1) & (2)
            */

            if (JavaUtils.indexOfIgnoreCase(contentType, SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1)
            {
                returnNS = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }
            // search for "type=text/xml"
            else
            if (JavaUtils.indexOfIgnoreCase(contentType, SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1)
            {
                returnNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }
        }

        if (returnNS == null) {
            if (log.isDebugEnabled()) {
                log.debug("No content-type or \"type=\" parameter was found in the content-type " +
                        "header and no default was specified, thus defaulting to SOAP 1.1.");
            }
            returnNS = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
        }

        if (log.isDebugEnabled()) {
            log.debug("content-type: " + contentType);
            log.debug("defaultSOAPNamespace: " + defaultSOAPNamespace);
            log.debug("Returned namespace: " + returnNS);
        }
        return returnNS;

    }

    public static void processContentTypeForAction(String contentType, MessageContext msgContext) {
        //Check for action header and set it in as soapAction in MessageContext
        int index = contentType.indexOf("action");
        if (index > -1) {
            String transientString = contentType.substring(index, contentType.length());
            int equal = transientString.indexOf("=");
            int firstSemiColon = transientString.indexOf(";");
            String soapAction; // This will contain "" in the string
            if (firstSemiColon > -1) {
                soapAction = transientString.substring(equal + 1, firstSemiColon);
            } else {
                soapAction = transientString.substring(equal + 1, transientString.length());
            }
            if ((soapAction != null) && soapAction.startsWith("\"")
                    && soapAction.endsWith("\"")) {
                soapAction = soapAction
                        .substring(1, soapAction.length() - 1);
            }
            msgContext.setSoapAction(soapAction);
        }
    }





        /**
         * This is a helper method to get the response written flag from the RequestResponseTransport
         * instance.
         */
        public static boolean isResponseWritten(MessageContext messageContext) {
            RequestResponseTransport reqResTransport = getRequestResponseTransport(messageContext);
            if (reqResTransport != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found RequestResponseTransport returning isResponseWritten()");
                }
                return reqResTransport.isResponseWritten();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Did not find RequestResponseTransport returning false from get"
                            + "ResponseWritten()");
                }
                return false;
            }
        }

       /**
         * This is a helper method to set the response written flag on the RequestResponseTransport
         * instance.
        */
       public static void setResponseWritten(MessageContext messageContext, boolean responseWritten) {
            RequestResponseTransport reqResTransport = getRequestResponseTransport(messageContext);
            if (reqResTransport != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found RequestResponseTransport setting response written");
                }
                reqResTransport.setResponseWritten(responseWritten);
            } else {
                if (log.isDebugEnabled()) {
                   log.debug("Did not find RequestResponseTransport cannot set response written");
               }
           }
       }

       /**
        * This is an internal helper method to retrieve the RequestResponseTransport instance
        * from the MessageContext object. The MessageContext may be the response MessageContext so
        * in that case we will have to retrieve the request MessageContext from the OperationContext.
        */
       private static RequestResponseTransport getRequestResponseTransport(MessageContext messageContext) {
    	   try {
    		   // If this is the request MessageContext we should find it directly by the getProperty()
               // method
    	       RequestResponseTransport transportControl = (RequestResponseTransport)
    	           messageContext.getProperty(RequestResponseTransport.TRANSPORT_CONTROL);
    	       
               if (transportControl != null) {
                   return transportControl;
               }
               // If this is the response MessageContext we need to look for the request MessageContext
        	   else if (messageContext.getOperationContext() != null
        				&& messageContext.getOperationContext()
                      		.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE) != null) {
        						return (RequestResponseTransport) messageContext.
        						getOperationContext().getMessageContext(
        								WSDLConstants.MESSAGE_LABEL_IN_VALUE).getProperty(
        										RequestResponseTransport.TRANSPORT_CONTROL);
        		}
        		else {
        			return null;
        		}
    	   }
           catch(AxisFault af) {
           	// probably should not be fatal, so just log the message
           	String msg = Messages.getMessage("getMessageContextError", af.toString());
           	log.debug(msg);
           	return null;
           }
    }

       /**
        * Clean up cached attachment file
        * @param msgContext
        */
       public static void deleteAttachments(MessageContext msgContext) {
       	if (log.isDebugEnabled()) {
               log.debug("Entering deleteAttachments()");
           }

       	Attachments attachments = msgContext.getAttachmentMap();

           if (attachments != null) {
               // Get the list of Content IDs for the attachments...but does not try to pull the stream for new attachments.
               // (Pulling the stream for new attachments will probably fail...the stream is probably closed)
               List keys = attachments.getContentIDList();
               if (keys != null && keys.size() > 0) {
               	String key = null;
               	File file = null;
               	   LifecycleManager lcm = (LifecycleManager)msgContext.getRootContext().getAxisConfiguration().getParameterValue(DeploymentConstants.ATTACHMENTS_LIFECYCLE_MANAGER);               		
               	DataSource dataSource = null;
                   for (int i = 0; i < keys.size(); i++) {
                       try {
                           key = (String) keys.get(i);
                           dataSource = attachments.getDataHandler(key).getDataSource();
                           if(dataSource instanceof CachedFileDataSource){
                           	file = ((CachedFileDataSource)dataSource).getFile();
                           	if (log.isDebugEnabled()) {
                                   log.debug("Delete cache attachment file: "+file.getName());
                            }
                           	if(lcm!=null){
                                if(log.isDebugEnabled()){
                                    log.debug("deleting file using lifecyclemanager");
                                }
                                lcm.delete(file);
                            }else{
                                file.delete();
                            }
                           }
                       }
                       catch (Exception e) {
                    	   if (log.isDebugEnabled()) {
                               log.debug("Delete cache attachment file failed"+ e.getMessage());
                           }

                           if (file != null) {
                               if(lcm!=null){
                                   try{
                                       lcm.deleteOnExit(file);
                                   }catch(Exception ex){
                                       file.deleteOnExit();
                                   }
                               }
                               else{
                                   file.deleteOnExit();
                               }
                           }
                       }
                   }
               }
           }

           if (log.isDebugEnabled()) {
               log.debug("Exiting deleteAttachments()");
           }
       }

       /**
        * This method can be called by components wishing to detach the DetachableInputStream
        * object that is present on the MessageContext. This is meant to shield components
        * from any logic that needs to be executed on the DetachableInputStream in order to
        * have it effectively detached. If the DetachableInputStream is not present, or if
        * the supplied MessageContext is null, no action will be taken.
        */
       public static void detachInputStream(MessageContext msgContext) throws AxisFault {
           try {
               if(msgContext != null
                       &&
                       msgContext.getProperty(Constants.DETACHABLE_INPUT_STREAM) != null) {
                   DetachableInputStream dis = (DetachableInputStream) msgContext.getProperty(Constants.DETACHABLE_INPUT_STREAM);
                   if(log.isDebugEnabled()) {
                       log.debug("Detaching DetachableInputStream: " + dis);
                   }
                   dis.detach();
               }
               else {
                   if(log.isDebugEnabled()) {
                       log.debug("Detach not performed for MessageContext: " + msgContext);
                   }
               }
           }
           catch(Throwable t) {
               throw AxisFault.makeFault(t);
           }
       }

    /**
     * <p>
     * Checks whether MTOM needs to be enabled for the message represented by
     * the msgContext. We check value assigned to the "enableMTOM" property
     * either using the config files (axis2.xml, services.xml) or
     * programatically. Programatic configuration is given priority. If the
     * given value is "optional", MTOM will be enabled only if the incoming
     * message was an MTOM message.
     * </p>
     *
     * @param msgContext the active MessageContext
     * @return true if SwA needs to be enabled
     */
    public static boolean doWriteMTOM(MessageContext msgContext) {
        boolean enableMTOM;
        Object enableMTOMObject = null;
        // First check the whether MTOM is enabled by the configuration
        // (Eg:Axis2.xml, services.xml)
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_MTOM);
        if (parameter != null) {
            enableMTOMObject = parameter.getValue();
        }
        // Check whether the configuration is overridden programatically..
        // Priority given to programatically setting of the value
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_MTOM);
        if (property != null) {
            enableMTOMObject = property;
        }
        enableMTOM = JavaUtils.isTrueExplicitly(enableMTOMObject);
        // Handle the optional value for enableMTOM
        // If the value for 'enableMTOM' is given as optional and if the request
        // message was a MTOM message we sent out MTOM
        if (!enableMTOM && msgContext.isDoingMTOM() && (enableMTOMObject instanceof String)) {
            if (((String) enableMTOMObject).equalsIgnoreCase(Constants.VALUE_OPTIONAL)) {
                //In server side, we check whether request was MTOM
                if (msgContext.isServerSide()) {
                    if (msgContext.isDoingMTOM()) {
                        enableMTOM = true;
                    }
                    // in the client side, we enable MTOM if it is optional
                } else {
                    enableMTOM = true;
                }
            }
        }
        return enableMTOM;
    }

    /**
     * <p>
     * Checks whether SOAP With Attachments (SwA) needs to be enabled for the
     * message represented by the msgContext. We check value assigned to the
     * "enableSwA" property either using the config files (axis2.xml,
     * services.xml) or programatically. Programatic configuration is given
     * priority. If the given value is "optional", SwA will be enabled only if
     * the incoming message was SwA type.
     * </p>
     *
     * @param msgContext the active MessageContext
     * @return true if SwA needs to be enabled
     */
    public static boolean doWriteSwA(MessageContext msgContext) {
        boolean enableSwA;
        Object enableSwAObject = null;
        // First check the whether SwA is enabled by the configuration
        // (Eg:Axis2.xml, services.xml)
        Parameter parameter = msgContext.getParameter(Constants.Configuration.ENABLE_SWA);
        if (parameter != null) {
            enableSwAObject = parameter.getValue();
        }
        // Check whether the configuration is overridden programatically..
        // Priority given to programatically setting of the value
        Object property = msgContext.getProperty(Constants.Configuration.ENABLE_SWA);
        if (property != null) {
            enableSwAObject = property;
        }
        enableSwA = JavaUtils.isTrueExplicitly(enableSwAObject);
        // Handle the optional value for enableSwA
        // If the value for 'enableSwA' is given as optional and if the request
        // message was a SwA message we sent out SwA
        if (!enableSwA && msgContext.isDoingSwA() && (enableSwAObject instanceof String)) {
            if (((String) enableSwAObject).equalsIgnoreCase(Constants.VALUE_OPTIONAL)) {
                enableSwA = true;
            }
        }
        return enableSwA;
    }

    public static boolean isDoingREST(MessageContext msgContext) {
        boolean enableREST = false;

        // check whether isDoingRest is already true in the message context
        if (msgContext.isDoingREST()) {
            return true;
        }

        Object enableRESTProperty = msgContext.getProperty(Constants.Configuration.ENABLE_REST);
        if (enableRESTProperty != null) {
            enableREST = JavaUtils.isTrueExplicitly(enableRESTProperty);
        }

        msgContext.setDoingREST(enableREST);

        return enableREST;
    }

    /**
     * Utility method to query CharSetEncoding. First look in the
     * MessageContext. If it's not there look in the OpContext. Use the defualt,
     * if it's not given in either contexts.
     *
     * @param msgContext the active MessageContext
     * @return String the CharSetEncoding
     */
    public static String getCharSetEncoding(MessageContext msgContext) {
        String charSetEnc = (String) msgContext
            .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charSetEnc == null) {
            OperationContext opctx = msgContext.getOperationContext();
            if (opctx != null) {
                charSetEnc = (String) opctx
                    .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            }
            /**
             * If the char set enc is still not found use the default
             */
            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
        }
        return charSetEnc;
    }

}
	