/**
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
package org.apache.ws.security.conversation;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConversationException extends Exception {
    
    private static final long serialVersionUID = 970894530660804319L;
    
    public final static String BAD_CONTEXT_TOKEN = "BadContextToken";
    public final static String UNSUPPORTED_CONTEXT_TOKEN = "UnsupportedContextToken";
    public final static String UNKNOWN_DERIVATION_SOURCE = "UnknownDerivationSource";
    public final static String RENEW_NEEDED = "RenewNeeded";
    public final static String UNABLE_TO_REVIEW = "UnableToRenew";
    
    private static ResourceBundle resources;

    private String faultCode;
    private String faultString;
    
    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.ws.security.conversation.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public ConversationException(String faultCode, Object[] args) {
        super(getMessage(faultCode, args));
        this.faultCode = getFaultCode(faultCode);
        this.faultString = getMessage(faultCode, args);
    }
    
    /**
     * Construct the fault properly code for the standard faults
     * @param faultCode2
     * @return
     */
    private String getFaultCode(String code) {
        if(BAD_CONTEXT_TOKEN.equals(code) ||
           UNABLE_TO_REVIEW.equals(code) ||
           UNKNOWN_DERIVATION_SOURCE.equals(code) ||
           UNSUPPORTED_CONTEXT_TOKEN.equals(code) ||
           RENEW_NEEDED.equals(code)) {
            return ConversationConstants.WSC_PREFIX+ ":" + code;
        } else {
            return code;
        }
    }

    public ConversationException(String faultCode) {
        this(faultCode, (Object[])null);
    }
    
    public ConversationException(String faultCode, Object[] args, Throwable e) {
        super(getMessage(faultCode, args),e);
        this.faultCode = faultCode;
        this.faultString = getMessage(faultCode, args);
    }
    
    public ConversationException(String faultCode, Throwable e) {
        this(faultCode, null, e);
    }

    /**
     * get the message from resource bundle.
     * <p/>
     *
     * @return the message translated from the property (message) file.
     */
    protected static String getMessage(String faultCode, Object[] args) {
        String msg = null;
        try {
            msg = MessageFormat.format(resources.getString(faultCode), args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + faultCode + "' resource property", e);
        }
        if(msg != null) {
            return msg;
        } else {
            return faultCode;
        }
    }

    /**
     * @return Returns the faultCode.
     */
    protected String getFaultCode() {
        return faultCode;
    }

    /**
     * @return Returns the faultString.
     */
    protected String getFaultString() {
        return faultString;
    }
    

}
