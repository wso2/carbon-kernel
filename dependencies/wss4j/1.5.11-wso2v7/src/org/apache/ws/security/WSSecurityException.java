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
package org.apache.ws.security;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Exception class for WS-Security.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class WSSecurityException extends RemoteException {
    public static final int FAILURE = 0;
    public static final int UNSUPPORTED_SECURITY_TOKEN = 1;
    public static final int UNSUPPORTED_ALGORITHM = 2;
    public static final int INVALID_SECURITY = 3;
    public static final int INVALID_SECURITY_TOKEN = 4;
    public static final int FAILED_AUTHENTICATION = 5;
    public static final int FAILED_CHECK = 6;
    public static final int SECURITY_TOKEN_UNAVAILABLE = 7;
    public static final int MESSAGE_EXPIRED = 8;
    public static final int FAILED_ENCRYPTION = 9;
    public static final int FAILED_SIGNATURE = 10;
    private static ResourceBundle resources;
    /*
     * This is an Integer -> QName map. Its function is to map the integer error codes
     * given above to the QName fault codes as defined in the SOAP Message Security 1.1
     * specification. A client application can simply call getFaultCode rather than do
     * any parsing of the error code. Note that there are no mappings for "FAILURE", 
     * "FAILED_ENCRYPTION" and "FAILED_SIGNATURE" as these are not standard error messages.
     */
    private static final java.util.Map FAULT_CODE_MAP = new java.util.HashMap();

    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.ws.security.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
        FAULT_CODE_MAP.put(
            new Integer(WSSecurityException.UNSUPPORTED_SECURITY_TOKEN), 
            WSConstants.UNSUPPORTED_SECURITY_TOKEN
        );
        FAULT_CODE_MAP.put(
            new Integer(UNSUPPORTED_ALGORITHM), 
            WSConstants.UNSUPPORTED_ALGORITHM
        );
        FAULT_CODE_MAP.put(
            new Integer(INVALID_SECURITY), 
            WSConstants.INVALID_SECURITY
        );
        FAULT_CODE_MAP.put(
            new Integer(INVALID_SECURITY_TOKEN), 
            WSConstants.INVALID_SECURITY_TOKEN
        );
        FAULT_CODE_MAP.put(
            new Integer(FAILED_AUTHENTICATION), 
            WSConstants.FAILED_AUTHENTICATION
         );
        FAULT_CODE_MAP.put(
            new Integer(FAILED_CHECK), 
            WSConstants.FAILED_CHECK
         );
        FAULT_CODE_MAP.put(
            new Integer(SECURITY_TOKEN_UNAVAILABLE),
            WSConstants.SECURITY_TOKEN_UNAVAILABLE
         );
        FAULT_CODE_MAP.put(
            new Integer(MESSAGE_EXPIRED), 
            WSConstants.MESSAGE_EXPIRED
        );
    }

    private int errorCode;

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     * @param exception
     */
    public WSSecurityException(int errorCode, String msgId, Object[] args, Throwable exception) {
        super(getMessage(errorCode, msgId, args), exception);
        this.errorCode = errorCode;
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     */
    public WSSecurityException(int errorCode, String msgId, Object[] args) {
        super(getMessage(errorCode, msgId, args));
        this.errorCode = errorCode;
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     */
    public WSSecurityException(int errorCode, String msgId) {
        this(errorCode, msgId, null);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     */
    public WSSecurityException(int errorCode) {
        this(errorCode, null, null);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorMessage
     */
    public WSSecurityException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorMessage
     */
    public WSSecurityException(String errorMessage, Throwable t) {
        super(errorMessage, t);
    }

    /**
     * Get the error code.
     * <p/>
     *
     * @return error code of this exception See values above.
     */
    public int getErrorCode() {
        return this.errorCode;
    }
    
    /**
     * Get the fault code QName for this associated error code.
     * <p/>
     * 
     * @return the fault code QName of this exception
     */
    public javax.xml.namespace.QName getFaultCode() {
        Object ret = FAULT_CODE_MAP.get(new Integer(this.errorCode));
        if (ret != null) {
            return (javax.xml.namespace.QName)ret;
        }
        return null;
    }

    /**
     * get the message from resource bundle.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     * @return the message translated from the property (message) file.
     */
    private static String getMessage(int errorCode, String msgId, Object[] args) {
        String msg = null;
        try {
            msg = resources.getString(String.valueOf(errorCode));
            if (msgId != null) {
                return msg += (" (" + MessageFormat.format(resources.getString(msgId), args) + ")");
            }
        } catch (MissingResourceException e) {
            throw new RuntimeException("Undefined '" + msgId + "' resource property", e);
        }
        return msg;
    }
}
