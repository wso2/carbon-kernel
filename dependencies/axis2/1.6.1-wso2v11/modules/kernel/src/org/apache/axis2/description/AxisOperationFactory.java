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
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;

public class AxisOperationFactory implements WSDLConstants {

    public static AxisOperation getAxisOperation(int mepURI) throws AxisFault {
        AxisOperation abOpdesc;

        switch (mepURI) {
            case WSDLConstants.MEP_CONSTANT_IN_ONLY : {
                abOpdesc = new InOnlyAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_ONLY);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_OUT_ONLY : {
                abOpdesc = new OutOnlyAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_ONLY);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_IN_OUT : {
                abOpdesc = new InOutAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_OUT);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT : {
                abOpdesc = new InOutAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY : {
                abOpdesc = new InOutAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_OUT_IN : {
                abOpdesc = new OutInAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_IN);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN : {
                abOpdesc = new OutInAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN);
                break;
            }
            case WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY : {
                abOpdesc = new RobustOutOnlyAxisOperation();
                abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY);
                break;
            }
            default : {
                throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
            }
        }
        return abOpdesc;
    }

    //FIXME add in the latest MEP URIs AND needs to double check on about the mep we had in 2004
    @SuppressWarnings("deprecation")
    public static AxisOperation getOperationDescription(String mepURI) throws AxisFault {
        AxisOperation abOpdesc;
        if (WSDL2Constants.MEP_URI_IN_ONLY.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_IN_ONLY.equals(mepURI)) {
            abOpdesc = new InOnlyAxisOperation();
        } else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_OUT_ONLY.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_OUT_ONLY.equals(mepURI)) {
            abOpdesc = new OutOnlyAxisOperation();
        } else if (WSDL2Constants.MEP_URI_IN_OUT.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_IN_OUT.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_IN_OUT.equals(mepURI)) {
            abOpdesc = new InOutAxisOperation();
        } else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)) {
            abOpdesc = new InOutAxisOperation();
        } else if (WSDL2Constants.MEP_URI_OUT_IN.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_OUT_IN.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_OUT_IN.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
        } else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
        } else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)) {
            abOpdesc = new OutInAxisOperation();
            abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY);
        } else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI) ||
                WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI) ||
                WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI)) {
            abOpdesc = new InOnlyAxisOperation();
            abOpdesc.setMessageExchangePattern(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY);
        } else {
            throw new AxisFault(Messages.getMessage("unSupportedMEP", "ID is " + mepURI));
        }
        return abOpdesc;
    }
}
