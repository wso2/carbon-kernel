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

package org.apache.axis2.jaxws.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.common.config.WSDLExtensionValidator;
import org.apache.axis2.jaxws.common.config.WSDLValidatorElement;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.wsdl.Definition;

/**
 * This is a utility class to make it easier/cleaner for the JAX-WS code
 * to invoke the WSDLExtensionValidators.
 */
public class WSDLExtensionValidatorUtil
{
  private static final Log log = LogFactory.getLog(WSDLExtensionValidatorUtil.class);
  private static final boolean debug = log.isDebugEnabled();
 
  /**
   * Register a new WSDLExtensionValidator
   *
   * @param configurationContext
   * @param validator
   */
  public static void addWSDLExtensionValidator(ConfigurationContext configurationContext,
                                               WSDLExtensionValidator validator)
  throws AxisFault {
      AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
      addWSDLExtensionValidator(axisConfiguration, validator);
  }

  /**
   * Register a new WSDLExtensionValidator.
   *
   * @param axisConfiguration
   * @param validator
   */
    public static void addWSDLExtensionValidator(AxisConfiguration axisConfiguration, WSDLExtensionValidator validator) throws AxisFault {
        Parameter param = axisConfiguration.getParameter(Constants.WSDL_EXTENSION_VALIDATOR_LIST_ID);

        if (param == null) {
            param = new Parameter(Constants.WSDL_EXTENSION_VALIDATOR_LIST_ID, new LinkedList());
            axisConfiguration.addParameter(param);
        }

        List validatorList = (List) param.getValue();
        validatorList.add(validator);

        if (debug) {
            log.debug("Registered WSDLExtensionValidator [" + validator + "] with AxisConfiguration: " + axisConfiguration);
        }
    }

  /**
   * Activate any registered WSDLExtensionValidators to perform the validation
   * of...
   *
   * @param wsdlExtensionValidatorListID The name of the parameter in the
   *                                     AxisConfiguration that contains
   *                                     the list of validators.
   * @param msgContext
   * @throws AxisFault
   */
    public static void performValidation(AxisConfiguration axisConfiguration, Set<WSDLValidatorElement> extensionSet, Definition wsdlDefinition,
    EndpointDescription endpointDescription) {
        if (debug) {
            log.debug("Entered performValidation(AxisConfiguration, Set<WSDLValidatorElement>, Definition, EndpointDescription)");
            log.debug("axisConfiguration=" + axisConfiguration);
        }
        
        if (axisConfiguration == null) {
            if (debug) {
                log.debug("The AxisConfiguration was null, so we can't fetch any validators");
            }
            return;
        }

        if ((extensionSet == null) || (extensionSet.isEmpty())) {
            if (debug) {
                log.debug("There were no extensions to validate");
            }
            return;
        }

        Parameter param = axisConfiguration.getParameter(Constants.WSDL_EXTENSION_VALIDATOR_LIST_ID);

        if (param != null) {
            List validatorList = (List) param.getValue();
            ListIterator wsdlExtensionValidators = validatorList.listIterator();
            while (wsdlExtensionValidators.hasNext()) {
                WSDLExtensionValidator wev = (WSDLExtensionValidator) wsdlExtensionValidators.next();
                if (debug) {
                    log.debug("Calling validate() on WSDLExtensionValidator: " + wev);
                }
                
                wev.validate(extensionSet, wsdlDefinition, endpointDescription);
                
                if (debug) {
                    log.debug("Returned from WSDLExtensionValidator: " + wev);
                }
            }
        }
    }
}
