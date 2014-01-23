/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis2.transport.jms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.format.DataSourceMessageBuilder;
import org.apache.axis2.format.TextMessageBuilder;
import org.apache.axis2.format.TextMessageBuilderAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageDataSource;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageInputStream;

import javax.jms.*;
import javax.jms.Queue;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.naming.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Miscallaneous methods used for the JMS transport
 */
public class JMSUtils extends BaseUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);
    private static final Class<?>[]  NOARGS  = new Class<?>[] {};
    private static final Object[] NOPARMS = new Object[] {};

    /**
     * Should this service be enabled over the JMS transport?
     *
     * @param service the Axis service
     * @return true if JMS should be enabled
     */
    public static boolean isJMSService(AxisService service) {
        if (service.isEnableAllTransports()) {
            return true;

        } else {
            for (String transport : service.getExposedTransports()) {
                if (JMSListener.TRANSPORT_NAME.equals(transport)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get a String property from the JMS message
     *
     * @param message  JMS message
     * @param property property name
     * @return property value
     */
    public static String getProperty(Message message, String property) {
        try {
            return message.getStringProperty(property);
        } catch (JMSException e) {
            return null;
        }
    }

    /**
     * Return the destination name from the given URL
     *
     * @param url the URL
     * @return the destination name
     */
    public static String getDestination(String url) {
        String tempUrl = url.substring(JMSConstants.JMS_PREFIX.length());
        int propPos = tempUrl.indexOf("?");

        if (propPos == -1) {
            return tempUrl;
        } else {
            return tempUrl.substring(0, propPos);
        }
    }

    /**
     * Set the SOAPEnvelope to the Axis2 MessageContext, from the JMS Message passed in
     * @param message the JMS message read
     * @param msgContext the Axis2 MessageContext to be populated
     * @param contentType content type for the message
     * @throws AxisFault
     * @throws JMSException
     */
    public static void setSOAPEnvelope(Message message, MessageContext msgContext, String contentType)
        throws AxisFault, JMSException {

        if (contentType == null) {
            if (message instanceof TextMessage) {
                contentType = "text/plain";
            } else {
                contentType = "application/octet-stream";
            }
            if (log.isDebugEnabled()) {
                log.debug("No content type specified; assuming " + contentType);
            }
        }
        
        int index = contentType.indexOf(';');
        String type = index > 0 ? contentType.substring(0, index) : contentType;
        Builder builder = BuilderUtil.getBuilderFromSelector(type, msgContext);
        if (builder == null) {
            if (log.isDebugEnabled()) {
                log.debug("No message builder found for type '" + type + "'. Falling back to SOAP.");
            }
            builder = new SOAPBuilder();
        }
        
        OMElement documentElement;
        if (message instanceof BytesMessage) {
            // Extract the charset encoding from the content type and
            // set the CHARACTER_SET_ENCODING property as e.g. SOAPBuilder relies on this.
            String charSetEnc = null;
            try {
                if (contentType != null) {
                    charSetEnc = new ContentType(contentType).getParameter("charset");
                }
            } catch (ParseException ex) {
                // ignore
            }
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            
            if (builder instanceof DataSourceMessageBuilder) {
                documentElement = ((DataSourceMessageBuilder)builder).processDocument(
                        new BytesMessageDataSource((BytesMessage)message), contentType,
                        msgContext);
            } else {
                documentElement = builder.processDocument(
                        new BytesMessageInputStream((BytesMessage)message), contentType,
                        msgContext);
            }
        } else if (message instanceof TextMessage) {
            TextMessageBuilder textMessageBuilder;
            if (builder instanceof TextMessageBuilder) {
                textMessageBuilder = (TextMessageBuilder)builder;
            } else {
                textMessageBuilder = new TextMessageBuilderAdapter(builder);
            }
            String content = ((TextMessage)message).getText();
            documentElement = textMessageBuilder.processDocument(content, contentType, msgContext);
        } else if (message instanceof MapMessage) {
            documentElement = convertJMSMapToXML((MapMessage) message);
        }
        else {
            handleException("Unsupported JMS message type " + message.getClass().getName());
            return; // Make compiler happy
        }
        msgContext.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
    }

    /**
     *
     * @param message JMSMap message
     * @return XML representation of JMS Map message
     */
    public static OMElement convertJMSMapToXML(MapMessage message) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace jmsMapNS = OMAbstractFactory.getOMFactory().createOMNamespace(JMSConstants.JMS_MAP_NS, "");
        OMElement jmsMap = fac.createOMElement(JMSConstants.JMS_MAP_ELEMENT_NAME,jmsMapNS);
        try {
            Enumeration names = message.getMapNames();
            while (names.hasMoreElements()) {
                String nextName = names.nextElement().toString();
                String nextVal = message.getString(nextName);
                OMElement next = fac.createOMElement(nextName.replace(" ", ""), jmsMapNS);
                next.setText(nextVal);
                jmsMap.addChild(next);
            }
        } catch (JMSException e) {
            handleException("Unable to process JMSMap message", e);
        }
        return jmsMap;
    }


    public static void convertXMLtoJMSMap(OMElement element, MapMessage message) throws JMSException{

        Iterator itr = element.getChildElements();
        while (itr.hasNext()){
            OMElement elem = (OMElement)itr.next();
            message.setString(elem.getLocalName(),elem.getText());
        }
    }


    /**
     * Set the JMS ReplyTo for the message
     *
     * @param replyDestination the JMS Destination where the reply is expected
     * @param session the session to use to create a temp Queue if a response is expected
     * but a Destination has not been specified
     * @param message the JMS message where the final Destinatio would be set as the JMS ReplyTo
     * @return the JMS ReplyTo Destination for the message
     */
    public static Destination setReplyDestination(Destination replyDestination, Session session,
        Message message) {

        if (replyDestination == null) {
           try {
               // create temporary queue to receive the reply
               replyDestination = createTemporaryDestination(session);
           } catch (JMSException e) {
               handleException("Error creating temporary queue for response", e);
           }
        }

        try {
            message.setJMSReplyTo(replyDestination);
        } catch (JMSException e) {
            log.warn("Error setting JMS ReplyTo destination to : " + replyDestination, e);
        }

        if (log.isDebugEnabled()) {
            try {
                assert replyDestination != null;
                log.debug("Expecting a response to JMS Destination : " +
                    (replyDestination instanceof Queue ?
                        ((Queue) replyDestination).getQueueName() :
                        ((Topic) replyDestination).getTopicName()));
            } catch (JMSException ignore) {}
        }
        return replyDestination;
    }

    /**
     * Set transport headers from the axis message context, into the JMS message
     *
     * @param msgContext the axis message context
     * @param message the JMS Message
     * @throws JMSException on exception
     */
    public static void setTransportHeaders(MessageContext msgContext, Message message)
        throws JMSException {

        Map<?,?> headerMap = (Map<?,?>) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headerMap == null) {
            return;
        }

        for (Object headerName : headerMap.keySet()) {

            String name = (String) headerName;

            if (name.startsWith(JMSConstants.JMSX_PREFIX) &&
                !(name.equals(JMSConstants.JMSX_GROUP_ID) || name.equals(JMSConstants.JMSX_GROUP_SEQ))) {
                continue;
            }

            if (JMSConstants.JMS_COORELATION_ID.equals(name)) {
                message.setJMSCorrelationID(
                        (String) headerMap.get(JMSConstants.JMS_COORELATION_ID));
            } else if (JMSConstants.JMS_DELIVERY_MODE.equals(name)) {
                Object o = headerMap.get(JMSConstants.JMS_DELIVERY_MODE);
                if (o instanceof Integer) {
                    message.setJMSDeliveryMode((Integer) o);
                } else if (o instanceof String) {
                    try {
                        message.setJMSDeliveryMode(Integer.parseInt((String) o));
                    } catch (NumberFormatException nfe) {
                        log.warn("Invalid delivery mode ignored : " + o, nfe);
                    }
                } else {
                    log.warn("Invalid delivery mode ignored : " + o);
                }

            } else if (JMSConstants.JMS_EXPIRATION.equals(name)) {
                message.setJMSExpiration(
                    Long.parseLong((String) headerMap.get(JMSConstants.JMS_EXPIRATION)));
            } else if (JMSConstants.JMS_MESSAGE_ID.equals(name)) {
                message.setJMSMessageID((String) headerMap.get(JMSConstants.JMS_MESSAGE_ID));
            } else if (JMSConstants.JMS_PRIORITY.equals(name)) {
                message.setJMSPriority(
                    Integer.parseInt((String) headerMap.get(JMSConstants.JMS_PRIORITY)));
            } else if (JMSConstants.JMS_TIMESTAMP.equals(name)) {
                message.setJMSTimestamp(
                    Long.parseLong((String) headerMap.get(JMSConstants.JMS_TIMESTAMP)));
            } else if (JMSConstants.JMS_MESSAGE_TYPE.equals(name)) {
                message.setJMSType((String) headerMap.get(JMSConstants.JMS_MESSAGE_TYPE));

            } else {
                Object value = headerMap.get(name);
                if (value instanceof String) {
                    message.setStringProperty(name, (String) value);
                } else if (value instanceof Boolean) {
                    message.setBooleanProperty(name, (Boolean) value);
                } else if (value instanceof Integer) {
                    message.setIntProperty(name, (Integer) value);
                } else if (value instanceof Long) {
                    message.setLongProperty(name, (Long) value);
                } else if (value instanceof Double) {
                    message.setDoubleProperty(name, (Double) value);
                } else if (value instanceof Float) {
                    message.setFloatProperty(name, (Float) value);
                }
            }
        }
    }

    /**
     * Read the transport headers from the JMS Message and set them to the axis2 message context
     *
     * @param message the JMS Message received
     * @param responseMsgCtx the axis message context
     * @throws AxisFault on error
     */
    public static void loadTransportHeaders(Message message, MessageContext responseMsgCtx)
        throws AxisFault {
        responseMsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, getTransportHeaders(message));
    }

    /**
     * Extract transport level headers for JMS from the given message into a Map
     *
     * @param message the JMS message
     * @return a Map of the transport headers
     */
    public static Map<String, Object> getTransportHeaders(Message message) {
        // create a Map to hold transport headers
        Map<String, Object> map = new HashMap<String, Object>();

        // correlation ID
        try {
            if (message.getJMSCorrelationID() != null) {
                map.put(JMSConstants.JMS_COORELATION_ID, message.getJMSCorrelationID());
            }
        } catch (JMSException ignore) {}

        // set the delivery mode as persistent or not
        try {
            map.put(JMSConstants.JMS_DELIVERY_MODE, Integer.toString(message.getJMSDeliveryMode()));
        } catch (JMSException ignore) {}

        // destination name
        try {
            if (message.getJMSDestination() != null) {
                Destination dest = message.getJMSDestination();
                map.put(JMSConstants.JMS_DESTINATION,
                    dest instanceof Queue ?
                        ((Queue) dest).getQueueName() : ((Topic) dest).getTopicName());
            }
        } catch (JMSException ignore) {}

        // expiration
        try {
            map.put(JMSConstants.JMS_EXPIRATION, Long.toString(message.getJMSExpiration()));
        } catch (JMSException ignore) {}

        // if a JMS message ID is found
        try {
            if (message.getJMSMessageID() != null) {
                map.put(JMSConstants.JMS_MESSAGE_ID, message.getJMSMessageID());
            }
        } catch (JMSException ignore) {}

        // priority
        try {
            map.put(JMSConstants.JMS_PRIORITY, Long.toString(message.getJMSPriority()));
        } catch (JMSException ignore) {}

        // redelivered
        try {
            map.put(JMSConstants.JMS_REDELIVERED, Boolean.toString(message.getJMSRedelivered()));
        } catch (JMSException ignore) {}

        // replyto destination name
        try {
            if (message.getJMSReplyTo() != null) {
                Destination dest = message.getJMSReplyTo();
                map.put(JMSConstants.JMS_REPLY_TO,
                    dest instanceof Queue ?
                        ((Queue) dest).getQueueName() : ((Topic) dest).getTopicName());
            }
        } catch (JMSException ignore) {}

        // priority
        try {
            map.put(JMSConstants.JMS_TIMESTAMP, Long.toString(message.getJMSTimestamp()));
        } catch (JMSException ignore) {}

        // message type
        try {
            if (message.getJMSType() != null) {
                map.put(JMSConstants.JMS_TYPE, message.getJMSType());
            }
        } catch (JMSException ignore) {}

        // any other transport properties / headers
        Enumeration<?> e = null;
        try {
            e = message.getPropertyNames();
        } catch (JMSException ignore) {}

        if (e != null) {
            while (e.hasMoreElements()) {
                String headerName = (String) e.nextElement();
                try {
                    map.put(headerName, message.getStringProperty(headerName));
                    continue;
                } catch (JMSException ignore) {}
                try {
                    map.put(headerName, message.getBooleanProperty(headerName));
                    continue;
                } catch (JMSException ignore) {}
                try {
                    map.put(headerName, message.getIntProperty(headerName));
                    continue;
                } catch (JMSException ignore) {}
                try {
                    map.put(headerName, message.getLongProperty(headerName));
                    continue;
                } catch (JMSException ignore) {}
                try {
                    map.put(headerName, message.getDoubleProperty(headerName));
                    continue;
                } catch (JMSException ignore) {}
                try {
                    map.put(headerName, message.getFloatProperty(headerName));
                } catch (JMSException ignore) {}
            }
        }

        return map;
    }


    /**
     * Create a MessageConsumer for the given Destination
     * @param session JMS Session to use
     * @param dest Destination for which the Consumer is to be created
     * @param messageSelector the message selector to be used if any
     * @return a MessageConsumer for the specified Destination
     * @throws JMSException
     */
    public static MessageConsumer createConsumer(Session session, Destination dest, String messageSelector)
        throws JMSException {

        if (dest instanceof Queue) {
            return ((QueueSession) session).createReceiver((Queue) dest, messageSelector);
        } else {
            return ((TopicSession) session).createSubscriber((Topic) dest, messageSelector, false);
        }
    }

    /**
     * Create a temp queue or topic for synchronous receipt of responses, when a reply destination
     * is not specified
     * @param session the JMS Session to use
     * @return a temporary Queue or Topic, depending on the session
     * @throws JMSException
     */
    public static Destination createTemporaryDestination(Session session) throws JMSException {

        if (session instanceof QueueSession) {
            return session.createTemporaryQueue();
        } else {
            return session.createTemporaryTopic();
        }
    }

    /**
     * Return the body length in bytes for a bytes message
     * @param bMsg the JMS BytesMessage
     * @return length of body in bytes
     */
    public static long getBodyLength(BytesMessage bMsg) {
        try {
            Method mtd = bMsg.getClass().getMethod("getBodyLength", NOARGS);
            if (mtd != null) {
                return (Long) mtd.invoke(bMsg, NOPARMS);
            }
        } catch (Exception e) {
            // JMS 1.0
            if (log.isDebugEnabled()) {
                log.debug("Error trying to determine JMS BytesMessage body length", e);
            }
        }

        // if JMS 1.0
        long length = 0;
        try {
            byte[] buffer = new byte[2048];
            bMsg.reset();
            for (int bytesRead = bMsg.readBytes(buffer); bytesRead != -1;
                 bytesRead = bMsg.readBytes(buffer)) {
                    length += bytesRead;
            }
        } catch (JMSException ignore) {}
        return length;
    }

    public static long getBodyLength(MapMessage mMsg) {
        long length = 0;
        try {
            for (Enumeration e = mMsg.getMapNames(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                Object value = mMsg.getObject(key);
                if (value != null) {
                    if (value instanceof Boolean || value instanceof Byte) {
                        length += 1;
                    } else if (value instanceof Character || value instanceof Short) {
                        length += 2;
                    } else if (value instanceof Integer || value instanceof Float) {
                        length += 4;
                    } else if (value instanceof Long || value instanceof Double) {
                        length += 8;
                    } else if (value instanceof byte[]) {
                        length += ((byte[]) value).length;
                    } else if (value instanceof String) {
                        length += ((String) value).getBytes().length;
                    } else {
                        log.error("Unable to determine message size. Invalid Object Type : " + value.getClass().getName());
                        return 0;
                    }
                } else {
                    log.warn("Ignoring key " + key + " that did not return any value");
                }
                length += key.getBytes().length;
            }
        } catch (JMSException e) {
            handleException("Error reading JMS map message payload", e);
        }
        return length;
    }

    /**
     * Get the length of the message in bytes
     * @param message
     * @return message size (or approximation) in bytes
     * @throws JMSException
     */
    public static long getMessageSize(Message message) throws JMSException {
        if (message instanceof BytesMessage) {
            return JMSUtils.getBodyLength((BytesMessage) message);
        } else if (message instanceof TextMessage) {
            // TODO: Converting the whole message to a byte array is too much overhead just to determine the message size.
            //       Anyway, the result is not accurate since we don't know what encoding the JMS provider uses.
            return ((TextMessage) message).getText().getBytes().length;
        } else if (message instanceof MapMessage) {
            return JMSUtils.getBodyLength((MapMessage) message);
        } else {
            log.warn("Can't determine size of JMS message; unsupported message type : "
                    + message.getClass().getName());
            return 0;
        }
    }
    
    public static <T> T lookup(Context context, Class<T> clazz, String name)
        throws NamingException {
        
        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            // Instead of a ClassCastException, throw an exception with some
            // more information.
            if (object instanceof Reference) {
                Reference ref = (Reference)object;
                handleException("JNDI failed to de-reference Reference with name " +
                        name + "; is the factory " + ref.getFactoryClassName() +
                        " in your classpath?");
                return null;
            } else {
                handleException("JNDI lookup of name " + name + " returned a " +
                        object.getClass().getName() + " while a " + clazz + " was expected");
                return null;
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a Connection. Please be cautious when
     * making any changes
     *
     * @param conFac the ConnectionFactory to use
     * @param user optional user name
     * @param pass optional password
     * @param jmsSpec11 should we use JMS 1.1 API ?
     * @param isQueue is this to deal with a Queue?
     * @return a JMS Connection as requested
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public static Connection createConnection(ConnectionFactory conFac,
        String user, String pass, boolean jmsSpec11, Boolean isQueue,
        boolean isDurable, String clientID) throws JMSException {

        Connection connection = null;
        if (log.isDebugEnabled()) {
            log.debug("Creating a " + (isQueue == null ? "Generic" : isQueue ? "Queue" : "Topic") +
                "Connection using credentials : (" + user + "/" + pass + ")");
        }

        if (jmsSpec11 || isQueue == null) {
            if (user != null && pass != null) {
                connection = conFac.createConnection(user, pass);
            } else {
                connection = conFac.createConnection();
            }
            if(isDurable){
                connection.setClientID(clientID);
            }

        } else {
            QueueConnectionFactory qConFac = null;
            TopicConnectionFactory tConFac = null;
            if (isQueue) {
                qConFac = (QueueConnectionFactory) conFac;
            } else {
                tConFac = (TopicConnectionFactory) conFac;
            }

            if (user != null && pass != null) {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection(user, pass);
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection(user, pass);
                }
            } else {
                if (qConFac != null) {
                    connection = qConFac.createQueueConnection();
                } else if (tConFac != null) {
                    connection = tConFac.createTopicConnection();
                }
            }
            if(isDurable){
                connection.setClientID(clientID);
            }
        }
        return connection;
    }

    /**
     * This is a JMS spec independent method to create a Session. Please be cautious when
     * making any changes
     *
     * @param connection the JMS Connection
     * @param transacted should the session be transacted?
     * @param ackMode the ACK mode for the session
     * @param jmsSpec11 should we use the JMS 1.1 API?
     * @param isQueue is this Session to deal with a Queue?
     * @return a Session created for the given information
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public static Session createSession(Connection connection, boolean transacted, int ackMode,
        boolean jmsSpec11, Boolean isQueue) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return connection.createSession(transacted, ackMode);

        } else {
            if (isQueue) {
                return ((QueueConnection) connection).createQueueSession(transacted, ackMode);
            } else {
                return ((TopicConnection) connection).createTopicSession(transacted, ackMode);
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a MessageConsumer. Please be cautious when
     * making any changes
     *
     * @param session JMS session
     * @param destination the Destination
     * @param isQueue is the Destination a queue?
     * @param subscriberName optional client name to use for a durable subscription to a topic
     * @param messageSelector optional message selector
     * @param pubSubNoLocal should we receive messages sent by us during pub-sub?
     * @param isDurable is this a durable topic subscription?
     * @param jmsSpec11 should we use JMS 1.1 API ?
     * @return a MessageConsumer to receive messages
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public static MessageConsumer createConsumer(
        Session session, Destination destination, Boolean isQueue,
        String subscriberName, String messageSelector, boolean pubSubNoLocal,
        boolean isDurable, boolean jmsSpec11) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            if (isDurable) {
                return session.createDurableSubscriber(
                    (Topic) destination, subscriberName, messageSelector, pubSubNoLocal);
            } else {
                return session.createConsumer(destination, messageSelector, pubSubNoLocal);
            }
        } else {
            if (isQueue) {
                return ((QueueSession) session).createReceiver((Queue) destination, messageSelector);
            } else {
                if (isDurable) {
                    return ((TopicSession) session).createDurableSubscriber(
                        (Topic) destination, subscriberName, messageSelector, pubSubNoLocal);
                } else {
                    return ((TopicSession) session).createSubscriber(
                        (Topic) destination, messageSelector, pubSubNoLocal);
                }
            }
        }
    }

    /**
     * This is a JMS spec independent method to create a MessageProducer. Please be cautious when
     * making any changes
     *
     * @param session JMS session
     * @param destination the Destination
     * @param isQueue is the Destination a queue?
     * @param jmsSpec11 should we use JMS 1.1 API ?
     * @return a MessageProducer to send messages to the given Destination
     * @throws JMSException on errors, to be handled and logged by the caller
     */
    public static MessageProducer createProducer(
        Session session, Destination destination, Boolean isQueue, boolean jmsSpec11) throws JMSException {

        if (jmsSpec11 || isQueue == null) {
            return session.createProducer(destination);
        } else {
            if (isQueue) {
                return ((QueueSession) session).createSender((Queue) destination);
            } else {
                return ((TopicSession) session).createPublisher((Topic) destination);               
            }
        }
    }

    /**
     * Return a String representation of the destination type
     * @param destType the destination type indicator int
     * @return a descriptive String
     */
    public static String getDestinationTypeAsString(int destType) {
        if (destType == JMSConstants.QUEUE) {
            return "queue";
        } else if (destType == JMSConstants.TOPIC) {
            return "topic";
        } else {
            return "generic";
        }
    }

    /**
     * Return the JMS destination with the given destination name looked up from the context
     * 
     * @param context the Context to lookup
     * @param destinationName name of the destination to be looked up
     * @param destinationType type of the destination to be looked up
     * @return the JMS destination, or null if it does not exist
     */
    public static Destination lookupDestination(Context context, String destinationName,
                                                String destinationType) throws NamingException {

        if (destinationName == null) {
            return null;
        }

        try {
            return JMSUtils.lookup(context, Destination.class, destinationName);
        } catch (NameNotFoundException e) {
            try {
                Properties initialContextProperties = new Properties();
                if (context.getEnvironment() != null) {
                    if (context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL) != null) {
                        initialContextProperties.put(JMSConstants.NAMING_FACTORY_INITIAL, context.getEnvironment().get(JMSConstants.NAMING_FACTORY_INITIAL));
                    }
                    if (context.getEnvironment().get(JMSConstants.CONNECTION_STRING) != null) {
                        initialContextProperties.put(JMSConstants.CONNECTION_STRING, context.getEnvironment().get(JMSConstants.CONNECTION_STRING));
                    }
                    if(context.getEnvironment().get(JMSConstants.PROVIDER_URL) != null){
                        initialContextProperties.put(JMSConstants.PROVIDER_URL, context.getEnvironment().get(JMSConstants.PROVIDER_URL));
                    }
                }
                if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
                    initialContextProperties.put(JMSConstants.TOPIC_PREFIX + destinationName, destinationName);
                } else if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)
                        || JMSConstants.DESTINATION_TYPE_GENERIC.equalsIgnoreCase(destinationType)) {
                    initialContextProperties.put(JMSConstants.QUEUE_PREFIX + destinationName, destinationName);
                }
                InitialContext initialContext = new InitialContext(initialContextProperties);
                try {
                    return JMSUtils.lookup(initialContext, Destination.class, destinationName);
                } catch (NamingException e1) {
                    return JMSUtils.lookup(context, Destination.class,
                            (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType) ?
                                    "dynamicTopics/" : "dynamicQueues/") + destinationName);
                }


            } catch (NamingException x) {
                log.warn("Cannot locate destination : " + destinationName);
                throw x;
            }
        } catch (NamingException e) {
            log.warn("Cannot locate destination : " + destinationName, e);
            throw e;
        }
    }
}
