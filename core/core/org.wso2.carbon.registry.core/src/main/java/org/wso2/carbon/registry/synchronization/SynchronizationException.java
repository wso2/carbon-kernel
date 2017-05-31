/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.synchronization;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.synchronization.message.Message;
import org.wso2.carbon.registry.synchronization.message.MessageCode;

/**
 * This type of exception is thrown when an error occurs while performing a synchronization
 * operation on the Registry.
 */
public class SynchronizationException extends RegistryException {

    private Message message;

    private static final String DEFAULT_SYNCHRONIZATION_OPERATION_FAILED_MESSAGE =
            "Synchronization Operation Failed.";

    /**
     * Constructs a new exception with the specified message code and cause.
     *
     * @param code  the message code of the error.
     * @param cause the cause of this exception.
     */
    public SynchronizationException(MessageCode code, Throwable cause) {
        this(code, cause, null);
    }

    /**
     * Constructs a new exception with the specified message code and cause and parameters related
     * to this exception's detailed message.
     *
     * @param code       the message code of the error.
     * @param cause      the cause of this exception.
     * @param parameters the parameters related to this exception.
     */
    public SynchronizationException(MessageCode code, Throwable cause, String[] parameters) {
        super(DEFAULT_SYNCHRONIZATION_OPERATION_FAILED_MESSAGE, cause);
        this.message = new Message(code, parameters != null ? parameters : new String[0]);
    }

    /**
     * Constructs a new exception with the specified message code and cause and parameters related
     * to this exception's detailed message.
     *
     * @param code       the message code of the error.
     * @param parameters the parameters related to this exception.
     */
    public SynchronizationException(MessageCode code, String[] parameters) {
        this(code, null, parameters);
    }

    /**
     * Constructs a new exception with the specified message code.
     *
     * @param code the message code of the error.
     */
    public SynchronizationException(MessageCode code) {
        super(DEFAULT_SYNCHRONIZATION_OPERATION_FAILED_MESSAGE);
        this.message = new Message(code, new String[0]);
    }

    /**
     * Method to retrieve the message code of the detailed message of this exception.
     *
     * @return the message code.
     */
    @SuppressWarnings("unused")
    public MessageCode getCode() {
        return message.getMessageCode();
    }

    /**
     * Method to retrieve the parameters of the detailed message of this exception.
     *
     * @return the parameters of the detailed message.
     */
    @SuppressWarnings("unused")
    public String[] getParameters() {
        return message.getParameters();
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return  the detail message string of this <tt>Throwable</tt> instance
     *          (which may be <tt>null</tt>).
     */
    public String getMessage() {
        StringBuilder msg = new StringBuilder("message code: ").append(message.getMessageCode());
        if (message.getParameters() != null) {
            msg.append(", parameters: {");
        	for(int i = 0; i < message.getParameters().length; i ++) {
        		if (i != 0) {
                    msg.append(", ");
        		}
                msg.append(message.getParameters()[i]);
            }
        }
        return msg.toString();
    }
}
