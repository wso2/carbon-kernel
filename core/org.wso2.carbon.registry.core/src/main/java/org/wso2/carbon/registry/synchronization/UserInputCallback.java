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
package org.wso2.carbon.registry.synchronization;

import org.wso2.carbon.registry.synchronization.message.Message;

/**
 * This is a representation designed to implement a callback that is capable of providing a option
 * for a user to make a selection and return the choice that the user made.
 */
public interface UserInputCallback {

    /**
     * Method to obtain a confirmation from a user.
     *
     * @param question            the question asked from the user.
     * @param confirmationContext the context used.
     *
     * @return the choice of the user.
     */
    boolean getConfirmation(Message question, String confirmationContext);

    /**
     * Method to display a message to a user.
     *
     * @param message the message to display.
     */
    void displayMessage(Message message);
}
