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


package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.FaultDescriptionJava;
import org.apache.axis2.jaxws.description.FaultDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;

import javax.xml.ws.WebFault;
import java.lang.reflect.Method;
import java.util.StringTokenizer;


/** @see ../FaultDescription */


class FaultDescriptionImpl implements FaultDescription, FaultDescriptionJava, FaultDescriptionWSDL {

    private Class exceptionClass;
    private DescriptionBuilderComposite composite;
    private WebFault annotation;
    private OperationDescription parent;


    private String name = "";  // WebFault.name
    private String faultBean = "";  // WebFault.faultBean
    private String targetNamespace = ""; // WebFault.targetNamespace
    private String messageName = ""; //WebFault.messageName
    private String faultInfo = null;

    private static final String FAULT = "Fault";


    /**
     * The FaultDescriptionImpl class will only be used to describe exceptions declared to be thrown
     * by a service that has a WebFault annotation.  No generic exception should ever have a
     * FaultDescription associated with it.  It is the responsibility of the user of the
     * FaultDescriptionImpl class to avoid instantiating this object for non-annotated generic
     * exceptions.
     *
     * @param exceptionClass an exception declared to be thrown by the service on which this
     *                       FaultDescription may apply.
     * @param beanName       fully qualified package+classname of the bean associated with this
     *                       exception
     * @param annotation     the WebFault annotation object on this exception class
     * @param parent         the OperationDescription that is the parent of this FaultDescription
     */
    FaultDescriptionImpl(Class exceptionClass, WebFault annotation, OperationDescription parent) {
        this.exceptionClass = exceptionClass;
        this.annotation = annotation;
        this.parent = parent;
    }

    FaultDescriptionImpl(DescriptionBuilderComposite faultDBC, OperationDescription parent) {
        this.composite = faultDBC;
        this.parent = parent;
    }

    public WebFault getAnnoWebFault() {

        if (annotation == null) {
            if (isDBC()) {
                annotation = this.composite.getWebFaultAnnot();
            }
        }

        return annotation;
    }

    public String getExceptionClassName() {
        if (!isDBC()) {
            // no need for defaults here.  The exceptionClass stored in this
            // FaultDescription object is one that has been declared to be
            // thrown from the service method
            return exceptionClass.getName();
        } else {
            return composite.getClassName();
        }
    }

    public String getFaultInfo() {
        if (faultInfo != null) {
            return faultInfo;
        }
        if (!isDBC()) {
            try {
                Method method = exceptionClass.getMethod("getFaultInfo", null);
                faultInfo = method.getReturnType().getCanonicalName();
            } catch (Exception e) {
                // This must be a legacy exception
                faultInfo = "";
            }
        } else {
            MethodDescriptionComposite mdc =
                    composite.getMethodDescriptionComposite("getFaultInfo", 1);
            if (mdc != null) {
                faultInfo = mdc.getReturnType();
            } else {
                faultInfo = "";
            }
        }
        return faultInfo;
    }

    public String getFaultBean() {
        if (faultBean != null && faultBean.length() > 0) {
            // Return the faultBean if it was already calculated
            return faultBean;
        } else {
            // Load up the WebFault annotation and get the faultBean.
            // @WebFault may not be present
            WebFault annotation = getAnnoWebFault();

            if (annotation != null && annotation.faultBean() != null &&
                    annotation.faultBean().length() > 0) {
                faultBean = annotation.faultBean();
            } else {
                // There is no default.  But it seems reasonable to return
                // the fault info type.
                faultBean = getFaultInfo();

                // The faultBean still may be "" at this point.  The JAXWS runtime
                // is responsible for finding/buildin a representative fault bean.
            }
        }
        return faultBean;
    }

    public String getName() {
        if (name.length() > 0) {
            return name;
        } else {
            // Load the annotation. The annotation may not be present in WSGen cases
            WebFault annotation = this.getAnnoWebFault();
            if (annotation != null &&
                    annotation.name().length() > 0) {
                name = annotation.name();
            } else {
                // The default is undefined.
                // The JAX-WS layer may use the fault bean information to determine the name
            }
        }
        return name;
    }

    public String getMessageName(){
    	if(messageName.length()>0){
    		return name;
    	}else{
    		WebFault annotation= this.getAnnoWebFault();
    		if(annotation!=null && annotation.messageName().length()>0){
    			messageName=annotation.messageName();
    		}else{
    			// The default is undefined.
                // The JAX-WS layer may use the fault bean information to determine the name
    		}
    	}
    	return messageName;
    }
    
    public String getTargetNamespace() {
        if (targetNamespace.length() > 0) {
            return targetNamespace;
        } else {
            // Load the annotation. The annotation may not be present in WSGen cases
            WebFault annotation = this.getAnnoWebFault();
            if (annotation != null &&
                    annotation.targetNamespace().length() > 0) {
                targetNamespace = annotation.targetNamespace();
            } else {
                // The default is undefined
                // The JAX-WS layer may use the fault bean information to determine the name
            }
        }
        return targetNamespace;
    }


    public OperationDescription getOperationDescription() {
        return parent;
    }

    /**
     * utility method to get the last token in a "."-delimited package+classname string
     *
     * @return
     */
    private static String getSimpleName(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        String out = null;
        StringTokenizer tokenizer = new StringTokenizer(in, ".");
        if (tokenizer.countTokens() == 0)
            out = in;
        else {
            while (tokenizer.hasMoreTokens()) {
                out = tokenizer.nextToken();
            }
        }
        return out;
    }

    private boolean isDBC() {
        if (this.composite != null)
            return true;
        else
            return false;
    }

    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        try {
            string.append(super.toString());
            string.append(newline);
            string.append("Exception class: " + getExceptionClassName());
            string.append(newline);
            string.append("Name: " + getName());
            string.append(newline);
            string.append("Namespace: " + getTargetNamespace());
            string.append(newline);
            string.append("FaultBean: " + getFaultBean());
            string.append(newline);
            string.append("FaultInfo Type Name  : " + getFaultInfo());

        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "FaultDescription");
            return string.toString();
        }
        return string.toString();
    }
}
