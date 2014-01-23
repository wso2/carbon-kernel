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


package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;

/**
 * This is the factory for OperationContext.
 */
public class OperationContextFactory implements WSDLConstants {

    public static OperationContext createOperationContext(int mepURI, AxisOperation axisOp,
                                                          ServiceContext serviceContext)
            throws AxisFault {
        if ((WSDLConstants.MEP_CONSTANT_IN_OUT == mepURI) ||
                (WSDLConstants.MEP_CONSTANT_IN_ONLY == mepURI)
                || (WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT == mepURI)
                || (WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY == mepURI) ||
                (WSDLConstants.MEP_CONSTANT_OUT_ONLY == mepURI)
                || (WSDLConstants.MEP_CONSTANT_OUT_IN == mepURI) ||
                (WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN == mepURI)
                || (WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY == mepURI)) {
            return serviceContext.createOperationContext(axisOp);
        } else {
            throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
        }
    }
}
