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

package org.apache.axiom.soap;

import org.apache.axiom.om.OMException;

public class SOAPProcessingException extends OMException {

    private static final long serialVersionUID = -5432612295193716211L;

    private String soapFaultCode;

    /** Eran Chinthaka (chinthaka@apache.org) */
    public SOAPProcessingException(String message) {
        super(message);
    }

    public SOAPProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP
     *                    Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP
     *                    Fault
     * @param cause       - this will appear under the Detail information item of SOAP Fault
     */
    public SOAPProcessingException(String messageText, String faultCode, Throwable cause) {
        super(messageText, cause);
        this.soapFaultCode = faultCode;
    }

    /**
     * @param messageText - this will appear as the Text in the Reason information item of SOAP
     *                    Fault
     * @param faultCode   - this will appear as the Value in the Code information item of SOAP
     *                    Fault
     */
    public SOAPProcessingException(String messageText, String faultCode) {
        super(messageText);
        this.soapFaultCode = faultCode;
    }

    public String getFaultCode() {
        return soapFaultCode;
    }

    public void setFaultCode(String soapFaultCode) {
        this.soapFaultCode = soapFaultCode;
    }
}
