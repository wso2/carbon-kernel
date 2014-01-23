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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This class represents the messages in WSDL. There can be message element in services.xml
 * which are represented by this class.
 */
public class AxisMessage extends AxisDescription {

    private ArrayList<Handler> handlerChain;
    private String name;
    private ArrayList soapHeaders;

    //to keep data in WSDL message reference and to keep the Java2WSDL data
    // such as SchemaElementName , direction etc.
    private QName elementQname;
    private String direction;
    private String messagePartName;

    // To store deploy-time module refs
    private ArrayList<String> modulerefs;
    private String partName = Java2WSDLConstants.PARAMETERS;

    // private PolicyInclude policyInclude;

    //To chcek whether the message is wrapped or unwrapped
    private boolean wrapped = true;
    
    private volatile Policy effectivePolicy = null;
    private volatile Date lastPolicyCalcuatedTime = null;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    
    public String getMessagePartName() {
		return messagePartName;
	}

	public void setMessagePartName(String messagePartName) {
		this.messagePartName = messagePartName;
	}

	public AxisMessage() {
        soapHeaders = new ArrayList();
        handlerChain = new ArrayList<Handler>();
        modulerefs = new ArrayList<String>();
    }

    public ArrayList<Handler> getMessageFlow() {
        return handlerChain;
    }

    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean locked = false;

        if (getParent() != null) {
            locked = getParent().isParameterLocked(parameterName);
        }

        if (locked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    public void setMessageFlow(ArrayList<Handler> operationFlow) {
        this.handlerChain = operationFlow;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public QName getElementQName() {
        return this.elementQname;
    }

    public void setElementQName(QName element) {
        this.elementQname = element;
    }

    public Object getKey() {
        return this.elementQname;
    }

    public XmlSchemaElement getSchemaElement() {
        XmlSchemaElement xmlSchemaElement = null;
        AxisService service = getAxisOperation().getAxisService();
        ArrayList schemas = service.getSchema();
        for (Object schema : schemas) {
            xmlSchemaElement = getSchemaElement((XmlSchema)schema);
            if (xmlSchemaElement != null) {
                break;
            }
        }
        return xmlSchemaElement;
    }

    private XmlSchemaElement getSchemaElement(XmlSchema schema) {
        XmlSchemaElement xmlSchemaElement = null;
        if (schema != null) {
            xmlSchemaElement = schema.getElementByName(this.elementQname);
            if (xmlSchemaElement == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = schema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaElement = getSchemaElement(schema1);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaElement = getSchemaElement(schema1);
                        }
                        if (xmlSchemaElement != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaElement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This will return a list of WSDLExtensibilityAttribute
     * @return
     */
    public List getExtensibilityAttributes() {
        // TODO : Deepal implement this properly.

        // the list should contain list of WSDLExtensibilityAttribute
        return new ArrayList(0);
    }

    public void addSoapHeader(SOAPHeaderMessage soapHeaderMessage) {
        soapHeaders.add(soapHeaderMessage);
    }

    public ArrayList getSoapHeaders
            () {
        return soapHeaders;
    }

    /**
     * We do not support adding module operations when engaging a module to an AxisMessage
     * 
     * @param axisModule AxisModule to engage
     * @param engager
     * @throws AxisFault something went wrong
     */
    public void onEngage(AxisModule axisModule, AxisDescription engager) throws AxisFault {
        PhaseResolver phaseResolver = new PhaseResolver(getAxisConfiguration());
        phaseResolver.engageModuleToMessage(this, axisModule);
    }

    public ArrayList<String> getModulerefs() {
        return modulerefs;
    }

    public void addModuleRefs(String moduleName) {
        modulerefs.add(moduleName);
    }

    public AxisOperation getAxisOperation(){
        return (AxisOperation)parent;
    }


    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }


    public boolean isWrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public Policy getEffectivePolicy() {
        if (lastPolicyCalcuatedTime == null || isPolicyUpdated()) {
            try {
                reentrantLock.lock();
                if (lastPolicyCalcuatedTime == null || isPolicyUpdated()) {
                    effectivePolicy = calculateEffectivePolicy();
                    lastPolicyCalcuatedTime = new Date();
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        return effectivePolicy;
    }

	public Policy calculateEffectivePolicy() {
		PolicySubject policySubject;
		ArrayList<PolicyComponent> policyList = new ArrayList<PolicyComponent>();

		// AxisMessage
		policySubject = getPolicySubject();
		policyList.addAll(policySubject.getAttachedPolicyComponents());

		// AxisOperation
		AxisOperation axisOperation = getAxisOperation();
		if (axisOperation != null) {
			policyList.addAll(axisOperation.getPolicySubject()
					.getAttachedPolicyComponents());
		}

		// AxisService
		AxisService axisService = (axisOperation == null) ? null
				: axisOperation.getAxisService();
		if (axisService != null) {
			policyList.addAll(axisService.getPolicySubject()
					.getAttachedPolicyComponents());
		}

		// AxisConfiguration
		AxisConfiguration axisConfiguration = (axisService == null) ? null
				: axisService.getAxisConfiguration();
		if (axisConfiguration != null) {
			policyList.addAll(axisConfiguration.getPolicySubject()
					.getAttachedPolicyComponents());
		}

		Policy result = PolicyUtil.getMergedPolicy(policyList, axisService);
		return result;
	}

	public boolean isPolicyUpdated() {
		// AxisMessage
		if (getPolicySubject().getLastUpdatedTime().after(
				lastPolicyCalcuatedTime)) {
			return true;
		}
		// AxisOperation
		AxisOperation axisOperation = (AxisOperation) parent;
		if (axisOperation != null
				&& axisOperation.getPolicySubject().getLastUpdatedTime().after(
						lastPolicyCalcuatedTime)) {
			return true;
		}
		// AxisService
		AxisService axisService = (axisOperation == null) ? null
				: axisOperation.getAxisService();
		if (axisService != null
				&& axisService.getPolicySubject().getLastUpdatedTime().after(
						lastPolicyCalcuatedTime)) {
			return true;
		}
		// AxisConfiguration
		AxisConfiguration axisConfiguration = (axisService == null) ? null
				: axisService.getAxisConfiguration();
        return axisConfiguration != null
               && axisConfiguration.getPolicySubject().getLastUpdatedTime()
                .after(lastPolicyCalcuatedTime);
    }
}
