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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.jms.ctype.ContentTypeRuleFactory;
import org.apache.axis2.transport.jms.ctype.ContentTypeRuleSet;
import org.apache.axis2.transport.jms.ctype.MessageTypeRule;
import org.apache.axis2.transport.jms.ctype.PropertyRule;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.jms.BytesMessage;
import javax.jms.TextMessage;
import javax.naming.Context;

/**
 * Class that links an Axis2 service to a JMS destination. Additionally, it contains
 * all the required information to process incoming JMS messages and to inject them
 * into Axis2.
 */
public class JMSEndpoint extends ProtocolEndpoint {
    private static final Log log = LogFactory.getLog(JMSEndpoint.class);
    
    private final JMSListener listener;
    private final WorkerPool workerPool;
    
    private JMSConnectionFactory cf;
    private String jndiDestinationName;
    private int destinationType = JMSConstants.GENERIC;
    private String jndiReplyDestinationName;
    private String replyDestinationType = JMSConstants.DESTINATION_TYPE_GENERIC;
    private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();
    private ContentTypeRuleSet contentTypeRuleSet;
    private ServiceTaskManager serviceTaskManager;

    public JMSEndpoint(JMSListener listener, WorkerPool workerPool) {
        this.listener = listener;
        this.workerPool = workerPool;
    }

    public String getJndiDestinationName() {
        return jndiDestinationName;
    }

    private void setDestinationType(String destinationType) {
        if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
            this.destinationType = JMSConstants.TOPIC;
        } else if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
            this.destinationType = JMSConstants.QUEUE;
        } else {
            this.destinationType = JMSConstants.GENERIC;
        }
    }

    private void setReplyDestinationType(String destinationType) {
        if (JMSConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
            this.replyDestinationType = JMSConstants.DESTINATION_TYPE_TOPIC;
        } else if (JMSConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
            this.replyDestinationType = JMSConstants.DESTINATION_TYPE_QUEUE;
        } else {
            this.replyDestinationType = JMSConstants.DESTINATION_TYPE_GENERIC;
        }
    }

    public String getJndiReplyDestinationName() {
        return jndiReplyDestinationName;
    }

    public String getReplyDestinationType() {
        return replyDestinationType;
    }

    @Override
    public EndpointReference[] getEndpointReferences(AxisService service, String ip) {
        return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
    }

    private void computeEPRs() {
        List<EndpointReference> eprs = new ArrayList<EndpointReference>();
        for (Object o : getService().getParameters()) {
            Parameter p = (Parameter) o;
            if (JMSConstants.PARAM_PUBLISH_EPR.equals(p.getName()) && p.getValue() instanceof String) {
                if ("legacy".equalsIgnoreCase((String) p.getValue())) {
                    // if "legacy" specified, compute and replace it
                    endpointReferences.add(
                        new EndpointReference(getEPR()));
                } else {
                    endpointReferences.add(new EndpointReference((String) p.getValue()));
                }
            }
        }

        if (eprs.isEmpty()) {
            // if nothing specified, compute and return legacy EPR
            endpointReferences.add(new EndpointReference(getEPR()));
        }
    }

    /**
     * Get the EPR for the given JMS connection factory and destination
     * the form of the URL is
     * jms:/<destination>?[<key>=<value>&]*
     * Credentials Context.SECURITY_PRINCIPAL, Context.SECURITY_CREDENTIALS
     * JMSConstants.PARAM_JMS_USERNAME and JMSConstants.PARAM_JMS_USERNAME are filtered
     *
     * @return the EPR as a String
     */
    private String getEPR() {
        StringBuffer sb = new StringBuffer();

        sb.append(
            JMSConstants.JMS_PREFIX).append(jndiDestinationName);
        sb.append("?").
            append(JMSConstants.PARAM_DEST_TYPE).append("=").append(
            destinationType == JMSConstants.TOPIC ?
                JMSConstants.DESTINATION_TYPE_TOPIC : JMSConstants.DESTINATION_TYPE_QUEUE);

        if (contentTypeRuleSet != null) {
            String contentTypeProperty = contentTypeRuleSet.getDefaultContentTypeProperty();
            if (contentTypeProperty != null) {
                sb.append("&");
                sb.append(JMSConstants.CONTENT_TYPE_PROPERTY_PARAM);
                sb.append("=");
                sb.append(contentTypeProperty);
            }
        }

        for (Map.Entry<String,String> entry : cf.getParameters().entrySet()) {
            if (!Context.SECURITY_PRINCIPAL.equalsIgnoreCase(entry.getKey()) &&
                !Context.SECURITY_CREDENTIALS.equalsIgnoreCase(entry.getKey()) &&
                !JMSConstants.PARAM_JMS_USERNAME.equalsIgnoreCase(entry.getKey()) &&
                !JMSConstants.PARAM_JMS_PASSWORD.equalsIgnoreCase(entry.getKey())) {
                sb.append("&").append(
                    entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return sb.toString();
    }

    public ContentTypeRuleSet getContentTypeRuleSet() {
        return contentTypeRuleSet;
    }

    public JMSConnectionFactory getCf() {
        return cf;
    }

    public ServiceTaskManager getServiceTaskManager() {
        return serviceTaskManager;
    }

    public void setServiceTaskManager(ServiceTaskManager serviceTaskManager) {
        this.serviceTaskManager = serviceTaskManager;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        // We only support endpoints configured at service level
        if (!(params instanceof AxisService)) {
            return false;
        }
        
        AxisService service = (AxisService)params;
        
        cf = listener.getConnectionFactory(service);
        if (cf == null) {
            return false;
        }

        Parameter destParam = service.getParameter(JMSConstants.PARAM_DESTINATION);
        if (destParam != null) {
            jndiDestinationName = (String)destParam.getValue();
        } else {
            // Assume that the JNDI destination name is the same as the service name
            jndiDestinationName = service.getName();
        }
        
        Parameter destTypeParam = service.getParameter(JMSConstants.PARAM_DEST_TYPE);
        if (destTypeParam != null) {
            String paramValue = (String) destTypeParam.getValue();
            if (JMSConstants.DESTINATION_TYPE_QUEUE.equals(paramValue) ||
                    JMSConstants.DESTINATION_TYPE_TOPIC.equals(paramValue) )  {
                setDestinationType(paramValue);
            } else {
                throw new AxisFault("Invalid destinaton type value " + paramValue);
            }
        } else {
            log.debug("JMS destination type not given. default queue");
            destinationType = JMSConstants.QUEUE;
        }

        Parameter replyDestTypeParam = service.getParameter(JMSConstants.PARAM_REPLY_DEST_TYPE);
        if (replyDestTypeParam != null) {
            String paramValue = (String) replyDestTypeParam.getValue();
            if (JMSConstants.DESTINATION_TYPE_QUEUE.equals(paramValue) ||
                    JMSConstants.DESTINATION_TYPE_TOPIC.equals(paramValue) )  {
                setReplyDestinationType(paramValue);
            } else {
                throw new AxisFault("Invalid destination type value " + paramValue);
            }
        } else {
            log.debug("JMS reply destination type not given. default queue");
            replyDestinationType = JMSConstants.DESTINATION_TYPE_QUEUE;
        }
        
        jndiReplyDestinationName = ParamUtils.getOptionalParam(service,
                JMSConstants.PARAM_REPLY_DESTINATION);
        
        Parameter contentTypeParam = service.getParameter(JMSConstants.CONTENT_TYPE_PARAM);
        if (contentTypeParam == null) {
            contentTypeRuleSet = new ContentTypeRuleSet();
            contentTypeRuleSet.addRule(new PropertyRule(BaseConstants.CONTENT_TYPE));
            contentTypeRuleSet.addRule(new MessageTypeRule(BytesMessage.class, "application/octet-stream"));
            contentTypeRuleSet.addRule(new MessageTypeRule(TextMessage.class, "text/plain"));
        } else {
            contentTypeRuleSet = ContentTypeRuleFactory.parse(contentTypeParam);
        }

        computeEPRs(); // compute service EPR and keep for later use        
        
        serviceTaskManager = ServiceTaskManagerFactory.createTaskManagerForService(cf, service, workerPool);
        serviceTaskManager.setJmsMessageReceiver(new JMSMessageReceiver(listener, cf, this));
        
        return true;
    }
}
