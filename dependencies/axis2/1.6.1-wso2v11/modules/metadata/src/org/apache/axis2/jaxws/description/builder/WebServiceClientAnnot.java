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

package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

import java.lang.annotation.Annotation;


public class WebServiceClientAnnot implements javax.xml.ws.WebServiceClient {

    private String name;
    private String targetNamespace;
    private String wsdlLocation;

    /** A WebServiceClientAnnot cannot be instantiated. */
    private WebServiceClientAnnot() {

    }

    private WebServiceClientAnnot(
            String name,
            String targetNamespace,
            String wsdlLocation) {
        this.name = name;
        this.targetNamespace = targetNamespace;
        this.wsdlLocation = wsdlLocation;
    }

    public static WebServiceClientAnnot createWebServiceClientAnnotImpl() {
        return new WebServiceClientAnnot();
    }

    public static WebServiceClientAnnot createWebServiceClientAnnotImpl(
            String name,
            String targetNamespace,
            String wsdlLocation
    ) {
        return new WebServiceClientAnnot(name,
                                         targetNamespace,
                                         wsdlLocation);
    }

    /**
     * Create an instance of this annotation using the values from the annotation instance
     * passed in. 
     * 
     * @param annotation Use the values to create a new instance of annotation.  Note this could
     * be an instance of the java annotation superclass as well.
     * @return a new instance of this annotation or null if one could not be created with the
     * annotation passed in.
     */
    public static WebServiceClientAnnot createFromAnnotation(Annotation annotation) {
        WebServiceClientAnnot returnAnnot = null;
        if (annotation != null && annotation instanceof javax.xml.ws.WebServiceClient) {
            javax.xml.ws.WebServiceClient wsc = (javax.xml.ws.WebServiceClient) annotation;
            returnAnnot = new WebServiceClientAnnot(wsc.name(),
                                                    wsc.targetNamespace(),
                                                    wsc.wsdlLocation());
        }
        return returnAnnot;
    }
    
    /**
     * Create a new instance of this annotation using the values from the two annotations passed
     * in as arguments.  If either is null, the new annotation is created with the non-null 
     * annotation's values.  If both are null, then no annotation is created.  Non-empty values in 
     * the sparse annotation (if any) will override the values in the base annotation. 
     *  
     * @param baseAnnotation Initial values to be used in creating the annotation.  May be null.
     * @param sparseAnnotation Non-empty values (not null and not "") will override values in 
     * the base annotation.
     * @return A new annotation created from the arguments, or null if one could not be created.
     */
    public static WebServiceClientAnnot createFromAnnotation(Annotation baseAnnotation,
                                                             Annotation sparseAnnotation) {
        WebServiceClientAnnot returnAnnot = null;
        javax.xml.ws.WebServiceClient baseWSCAnnotation = null;
        javax.xml.ws.WebServiceClient sparseWSCAnnotation = null;
        
        if (baseAnnotation != null && baseAnnotation instanceof javax.xml.ws.WebServiceClient) {
            baseWSCAnnotation = (javax.xml.ws.WebServiceClient) baseAnnotation;
        }
        
        if (sparseAnnotation != null && sparseAnnotation instanceof javax.xml.ws.WebServiceClient) {
            sparseWSCAnnotation = (javax.xml.ws.WebServiceClient) sparseAnnotation;
        }
        
        if (baseWSCAnnotation != null && sparseWSCAnnotation != null) {
            // Both specified, create based on the base annotation merged with the sparse
            // annotation
            returnAnnot = WebServiceClientAnnot.createFromAnnotation(baseWSCAnnotation);
            if (!DescriptionBuilderUtils.isEmpty(sparseWSCAnnotation.name())) {
                returnAnnot.setName(sparseWSCAnnotation.name());
            }
            if (!DescriptionBuilderUtils.isEmpty(sparseWSCAnnotation.targetNamespace())) {
                returnAnnot.setTargetNamespace(sparseWSCAnnotation.targetNamespace());
            }
            if (!DescriptionBuilderUtils.isEmpty(sparseWSCAnnotation.wsdlLocation())) {
                returnAnnot.setWsdlLocation(sparseWSCAnnotation.wsdlLocation());
            }
        } else if (baseWSCAnnotation != null && sparseWSCAnnotation == null) {
            // There's no sparse information, so just create from the base annotation
            returnAnnot = WebServiceClientAnnot.createFromAnnotation(baseWSCAnnotation);
        } else if (baseWSCAnnotation == null && sparseWSCAnnotation != null) {
            // There's only sparse information, so create a new annotation based on that
            returnAnnot = WebServiceClientAnnot.createFromAnnotation(sparseWSCAnnotation);
        } else if (baseWSCAnnotation == null && sparseWSCAnnotation == null) {
            // No anntotation specifed, so just return null which was initialized above
        } else {
            // This should never happen; all the cases are covered above
            String msg = Messages.getMessage("DescriptionBuilderErr2",
                                             (sparseAnnotation == null) ? null : sparseAnnotation.toString(),
                                             (baseAnnotation == null) ? null : baseAnnotation.toString());
            throw ExceptionFactory.makeWebServiceException(msg);
        }
        return returnAnnot;
    }

    /** @return Returns the name. */
    public String name() {
        return name;
    }

    /** @return Returns the targetNamespace. */
    public String targetNamespace() {
        return targetNamespace;
    }

    /** @return Returns the wsdlLocation. */
    public String wsdlLocation() {
        return wsdlLocation;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /** @param wsdlLocation The wsdlLocation to set. */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    //hmm, should we really do this
    public Class<Annotation> annotationType() {
        return Annotation.class;
    }

    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@WebServiceClient.name= " + name);
        sb.append(newLine);
        sb.append("@WebServiceClient.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebServiceClient.wsdlLocation= " + wsdlLocation);
        sb.append(newLine);
        return sb.toString();
	}
}
