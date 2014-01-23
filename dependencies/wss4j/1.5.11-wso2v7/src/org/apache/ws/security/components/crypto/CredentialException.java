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

package org.apache.ws.security.components.crypto;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * CredentialException.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class CredentialException extends Exception {
    public static final int FAILURE = -1;
    public static final int EXPIRED = 1;
    public static final int DEFECTIVE = 2;
    public static final int IO_ERROR = 3;
    public static final int SEC_ERROR = 4;
    private static ResourceBundle resources;
    private int errorCode;

    static {
        try {
            resources = ResourceBundle.getBundle("org.apache.ws.security.components.crypto.errors");
        } catch (MissingResourceException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param root
     */
    public CredentialException(int errorCode, String msgId, Throwable root) {
        this(errorCode, msgId, null, root);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     */
    public CredentialException(int errorCode, String msgId, Object[] args) {
        this(errorCode, msgId, args, null);
    }

    /**
     * Constructor.
     * <p/>
     *
     * @param errorCode
     * @param msgId
     * @param args
     * @param root
     */
    public CredentialException(int errorCode, String msgId, Object[] args, Throwable root) {
        super(getMessage(msgId, args), root);
        this.errorCode = errorCode;
    }

    /**
     * get the error code.
     * <p/>
     *
     * @return error code of this exception See values above.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * get the actual message.
     * <p/>
     *
     * @param msgId
     * @param args
     * @return the message translated from the property (message) file.
     */
    private static String getMessage(String msgId, Object[] args) {
        try {
            return MessageFormat.format(resources.getString(msgId), args);
        } catch (MissingResourceException e) {
            throw new RuntimeException("bad" + msgId, e);
        }
    }
}

