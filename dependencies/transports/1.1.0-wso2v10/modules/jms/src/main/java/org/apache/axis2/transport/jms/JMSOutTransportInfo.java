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

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * The JMS OutTransportInfo is a holder of information to send an outgoing message
 * (e.g. a Response) to a JMS destination. Thus at a minimum a reference to a
 * ConnectionFactory and a Destination are held
 */
public class JMSOutTransportInfo implements OutTransportInfo {

    private static final Log log = LogFactory.getLog(JMSOutTransportInfo.class);

    /**
     * this is a reference to the underlying JMS ConnectionFactory when sending messages
     * through connection factories not defined at the TransportSender level
     */
    private ConnectionFactory connectionFactory = null;
    /**
     * this is a reference to a JMS Connection Factory instance, which has a reference
     * to the underlying actual connection factory, an open connection to the JMS provider
     * and optionally a session already available for use
     */
    private JMSConnectionFactory jmsConnectionFactory = null;
    /** the Destination queue or topic for the outgoing message */
    private Destination destination = null;
    /** the Destination queue or topic for the outgoing message
     * i.e. JMSConstants.DESTINATION_TYPE_QUEUE, DESTINATION_TYPE_TOPIC or DESTINATION_TYPE_GENERIC
     */
    private String destinationType = JMSConstants.DESTINATION_TYPE_GENERIC;
    /** the Reply Destination queue or topic for the outgoing message */
    private Destination replyDestination = null;
    /** the Reply Destination name */
    private String replyDestinationName = null;
    /** the Reply Destination queue or topic for the outgoing message
     * i.e. JMSConstants.DESTINATION_TYPE_QUEUE, DESTINATION_TYPE_TOPIC or DESTINATION_TYPE_GENERIC
     */
    private String replyDestinationType = JMSConstants.DESTINATION_TYPE_GENERIC;
    /** the EPR properties when the out-transport info is generated from a target EPR */
    private Hashtable<String,String> properties = null;
    /** the target EPR string where applicable */
    private String targetEPR = null;
    /** the message property name that stores the content type of the outgoing message */
    private String contentTypeProperty;

    /**
     * Creates an instance using the given JMS connection factory and destination
     *
     * @param jmsConnectionFactory the JMS connection factory
     * @param dest the destination
     * @param contentTypeProperty the content type
     */
    JMSOutTransportInfo(JMSConnectionFactory jmsConnectionFactory, Destination dest,
            String contentTypeProperty) {
        this.jmsConnectionFactory = jmsConnectionFactory;
        this.destination = dest;
        destinationType = dest instanceof Topic ? JMSConstants.DESTINATION_TYPE_TOPIC
                                                : JMSConstants.DESTINATION_TYPE_QUEUE;
        this.contentTypeProperty = contentTypeProperty;
    }

    /**
     * Creates and instance using the given URL
     *
     * @param targetEPR the target EPR
     */
    JMSOutTransportInfo(String targetEPR) {

        this.targetEPR = targetEPR;
        if (!targetEPR.startsWith(JMSConstants.JMS_PREFIX)) {
            handleException("Invalid prefix for a JMS EPR : " + targetEPR);

        } else {
            properties = BaseUtils.getEPRProperties(targetEPR);
            String destinationType = properties.get(JMSConstants.PARAM_DEST_TYPE);
            if (destinationType != null) {
                setDestinationType(destinationType);
            }

            String replyDestinationType = properties.get(JMSConstants.PARAM_REPLY_DEST_TYPE);
            if (replyDestinationType != null) {
                setReplyDestinationType(replyDestinationType);
            }

            replyDestinationName = properties.get(JMSConstants.PARAM_REPLY_DESTINATION);
            contentTypeProperty = properties.get(JMSConstants.CONTENT_TYPE_PROPERTY_PARAM);
        }
    }

    /**
     * Provides a lazy load when created with a target EPR. This method performs actual
     * lookup for the connection factory and destination
     */
    private void loadConnectionFactoryFromProperties() {
        if (properties != null) {
            // This condition passes only when the JMSOutTransportInfo is created from an EPR
            InitialContext context = null;
            try {
                // It's a little expensive to do this for each and every outgoing request, but
                // the user can avoid this by defining a connection factory for JMS sender
                // TODO: See if this can be further optimized by caching the destination by EPR
                context = new InitialContext(properties);
            } catch (NamingException e) {
                handleException("Could not get an initial context using " + properties, e);
            }

            if (destination == null) {
                destination = getDestination(context, targetEPR);
                replyDestination = getReplyDestination(context, targetEPR);
            }
            connectionFactory = getConnectionFactory(context, properties);

            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    log.warn("Error while cleaning up the InitialContext", e);
                }
            }
        }
    }

    /**
     * Get the referenced ConnectionFactory using the properties from the context
     *
     * @param context the context to use for lookup
     * @param props   the properties which contains the JNDI name of the factory
     * @return the connection factory
     */
    private ConnectionFactory getConnectionFactory(Context context, Hashtable<String,String> props) {
        try {

            String conFacJndiName = props.get(JMSConstants.PARAM_CONFAC_JNDI_NAME);
            if (conFacJndiName != null) {
                return JMSUtils.lookup(context, ConnectionFactory.class, conFacJndiName);
            } else {
                handleException("Connection Factory JNDI name cannot be determined");
            }
        } catch (NamingException e) {
            handleException("Failed to look up connection factory from JNDI", e);
        }
        return null;
    }

    /**
     * Get the JMS destination specified by the given URL from the context
     *
     * @param context the Context to lookup
     * @param url     URL
     * @return the JMS destination, or null if it does not exist
     */
    private Destination getDestination(Context context, String url) {
        String destinationName = JMSUtils.getDestination(url);
        if (log.isDebugEnabled()) {
            log.debug("Lookup the JMS destination " + destinationName + " of type "
                    + destinationType + " extracted from the URL " + url);
        }
        
        try {
            return JMSUtils.lookupDestination(context, destinationName, destinationType);
        } catch (NamingException e) {
            handleException("Couldn't locate the JMS destination " + destinationName
                    + " of type " + destinationType + " extracted from the URL " + url, e);
        }

        // never executes but keeps the compiler happy
        return null;
    }

    /**
     * Get the JMS reply destination specified by the given URL from the context
     *
     * @param context the Context to lookup
     * @param url     URL
     * @return the JMS destination, or null if it does not exist
     */
    private Destination getReplyDestination(Context context, String url) {
        String replyDestinationName = properties.get(JMSConstants.PARAM_REPLY_DESTINATION);
        if (log.isDebugEnabled()) {
            log.debug("Lookup the JMS destination " + replyDestinationName + " of type "
                    + replyDestinationType + " extracted from the URL " + url);
        }
        
        try {
            return JMSUtils.lookupDestination(context, replyDestinationName, replyDestinationType);
        } catch (NamingException e) {
            handleException("Couldn't locate the JMS destination " + replyDestinationName
                    + " of type " + replyDestinationType + " extracted from the URL " + url, e);
        }

        // never executes but keeps the compiler happy
        return null;
    }

    /**
     * Look up for the given destination
     * @param replyDest the JNDI name to lookup Destination required
     * @return Destination for the JNDI name passed
     */
    public Destination getReplyDestination(String replyDest) {
        if (log.isDebugEnabled()) {
            log.debug("Lookup the JMS destination " + replyDest + " of type "
                    + replyDestinationType);
        }

        try {
            return JMSUtils.lookupDestination(
                    jmsConnectionFactory.getContext(), replyDest, replyDestinationType);
        } catch (NamingException e) {
            handleException("Couldn't locate the JMS destination " + replyDest
                    + " of type " + replyDestinationType, e);
        }

        // never executes but keeps the compiler happy
        return null;
    }


    private void handleException(String s) {
        log.error(s);
        throw new AxisJMSException(s);
    }

    private void handleException(String s, Exception e) {
        log.error(s, e);
        throw new AxisJMSException(s, e);
    }

    public Destination getDestination() {
        return destination;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public JMSConnectionFactory getJmsConnectionFactory() {
        return jmsConnectionFactory;
    }

    public void setContentType(String contentType) {
        // this is a useless Axis2 method imposed by the OutTransportInfo interface :(
    }

    public Hashtable<String,String> getProperties() {
        return properties;
    }

    public String getTargetEPR() {
        return targetEPR;
    }

    public String getDestinationType() {
      return destinationType;
    }

    public void setDestinationType(String destinationType) {
      if (destinationType != null) {
        this.destinationType = destinationType;
      }
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Destination replyDestination) {
        this.replyDestination = replyDestination;
    }

    public String getReplyDestinationType() {
        return replyDestinationType;
    }

    public void setReplyDestinationType(String replyDestinationType) {
        this.replyDestinationType = replyDestinationType;
    }

    public String getReplyDestinationName() {
        return replyDestinationName;
    }

    public void setReplyDestinationName(String replyDestinationName) {
        this.replyDestinationName = replyDestinationName;
    }

    public String getContentTypeProperty() {
        return contentTypeProperty;
    }

    public void setContentTypeProperty(String contentTypeProperty) {
        this.contentTypeProperty = contentTypeProperty;
    }

    /**
     * Create a one time MessageProducer for this JMS OutTransport information.
     * For simplicity and best compatibility, this method uses only JMS 1.0.2b API.
     * Please be cautious when making any changes
     *
     * @return a JMSSender based on one-time use resources
     * @throws JMSException on errors, to be handled and logged by the caller 
     */
    public JMSMessageSender createJMSSender() throws JMSException {

        // digest the targetAddress and locate CF from the EPR
        loadConnectionFactoryFromProperties();

        // create a one time connection and session to be used
        String user = properties != null ? properties.get(JMSConstants.PARAM_JMS_USERNAME) : null;
        String pass = properties != null ? properties.get(JMSConstants.PARAM_JMS_PASSWORD) : null;

        QueueConnectionFactory qConFac = null;
        TopicConnectionFactory tConFac = null;

        int destType = -1;
        // TODO: there is something missing here for destination type generic
        if (JMSConstants.DESTINATION_TYPE_QUEUE.equals(destinationType)) {
            destType = JMSConstants.QUEUE;
            qConFac = (QueueConnectionFactory) connectionFactory;

        } else if (JMSConstants.DESTINATION_TYPE_TOPIC.equals(destinationType)) {
            destType = JMSConstants.TOPIC;
            tConFac = (TopicConnectionFactory) connectionFactory;
        } else{
        	//treat jmsdestination type=queue(default is queue)
        	 destType = JMSConstants.QUEUE;
             qConFac = (QueueConnectionFactory) connectionFactory;
        }        
        
        Connection connection = null;
        if (user != null && pass != null) {
            if (qConFac != null) {
                connection = qConFac.createQueueConnection(user, pass);
            } else if (tConFac != null) {
                connection = tConFac.createTopicConnection(user, pass);
            }
        } else {
           if (qConFac != null) {
                connection = qConFac.createQueueConnection();
            } else if (tConFac != null)  {
                connection = tConFac.createTopicConnection();
            }
        }

        if (connection == null) {
            connection = jmsConnectionFactory != null ? jmsConnectionFactory.getConnection() : null;
        }

        Session session = null;
        MessageProducer producer = null;

        if (connection != null) {
            if (destType == JMSConstants.QUEUE) {
                session = ((QueueConnection) connection).
                        createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = ((QueueSession) session).createSender((Queue) destination);
            } else {
                session = ((TopicConnection) connection).
                        createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = ((TopicSession) session).createPublisher((Topic) destination);
            }
        }

        return new JMSMessageSender(
                connection,
                session,
                producer,
                destination,
                jmsConnectionFactory == null ?
                        JMSConstants.CACHE_NONE : jmsConnectionFactory.getCacheLevel(),
                false,
                destType == -1 ?
                        null : destType == JMSConstants.QUEUE ? Boolean.TRUE : Boolean.FALSE
        );

    }
}
