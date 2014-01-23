/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.synchronization.message;

import java.util.Arrays;
import java.util.List;

/**
 * A representation of a message presented to a user during the process of synchronizing a given
 * registry instance with a specified location on the filesystem.
 */
public class Message {

    private MessageCode messageCode = null;
    private List<String> parameters = null;

    /**
     * Constructor accepting the code of the message and a list of parameters.
     *
     * @param messageCode the message code.
     * @param parameters  the list of parameters.
     */
    public Message(MessageCode messageCode, String[] parameters) {
        this.messageCode = messageCode;
        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Method to retrieve the code of this message.
     *
     * @return the message code.
     */
    public MessageCode getMessageCode() {
        return messageCode;
    }

    /**
     * Method to retrieve the parameters of this message.
     *
     * @return the parameters of this message.
     */
    public String[] getParameters() {
        return parameters.toArray(new String[parameters.size()]);
    }
}
