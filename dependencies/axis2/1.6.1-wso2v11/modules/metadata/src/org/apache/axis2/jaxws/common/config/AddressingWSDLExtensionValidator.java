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
package org.apache.axis2.jaxws.common.config;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

public class AddressingWSDLExtensionValidator implements WSDLExtensionValidator
{
  private static final Log log = LogFactory.getLog(AddressingWSDLExtensionValidator.class);
  private static final boolean debug = log.isDebugEnabled();

  /**
   * Performs validation of input extensionSets from RespectBindingConfigurator.
   * @param extensionSet - Set of found required=true extensions from WSDL, read WSDLValidatorElement object definition.
   * @param wsdlDefinition - a WSDL definition instance.
   * @param endpointDesc - EndpointDescription that describes JAX-WS Endpoint definition.
   */
  public void validate(Set<WSDLValidatorElement> extensionSet, Definition wsdlDefinition, EndpointDescription endpointDesc)
  {
    if (debug) {
      log.debug("Looking for WSDL extension elements to validate");
    }
    
    if (extensionSet.isEmpty()) {
      if (debug) {
        log.debug("No WSDL extension elements found to validate");
      }
      return;
    }
    
    if (debug) {
      log.debug("Processing WSDL extension elements");
    }
    
    Iterator<WSDLValidatorElement> extensionIterator = extensionSet.iterator();
    
    WSDLValidatorElement elementToValidate;
    while (extensionIterator.hasNext()) {
      elementToValidate = extensionIterator.next();
      
      if (elementToValidate.getState() == WSDLValidatorElement.State.NOT_RECOGNIZED)
      {
        ExtensibilityElement ee = elementToValidate.getExtensionElement();
        QName name = (ee!=null)?ee.getElementType():null;
        if ((name!=null) && (name.equals(new QName("http://www.w3.org/2006/05/addressing/wsdl", "UsingAddressing")))) {
          
          if (debug) {
            log.debug("Found WSDL extension element {http://www.w3.org/2006/05/addressing/wsdl,UsingAddressing} -- validating");
          }
          
          EndpointDescriptionJava edj = (EndpointDescriptionJava) endpointDesc;
          Annotation anno = edj.getAnnoFeature(AddressingFeature.ID);
          if (getFeatureFromAnnotation(anno) == null) {
            if (debug) {
              log.debug("Enabling addressing annotation was not found, so this extension element is not supported");
            }
            elementToValidate.setState(WSDLValidatorElement.State.NOT_SUPPORTED);
          }
          else {
            if (debug) {
              log.debug("The matching addressing annotation was found, so this extension element is supported");
            }
            elementToValidate.setState(WSDLValidatorElement.State.SUPPORTED);
          }
        }
      }
    }
  }

  private WebServiceFeatureAnnotation getFeatureFromAnnotation(Annotation a) {
    return a.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
}


}