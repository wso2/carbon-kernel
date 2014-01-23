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
package org.wso2.carbon.registry.core.dataaccess;

import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

/**
 * This class represents a database transaction, which is used to support consistency and
 * concurrency. A database transaction is utilized by the {@link Transaction} API, to provide
 * database access to the registry.
 */
public interface DatabaseTransaction {

    /**
     * Push current transaction to a stack and make room for a new one.
     */
    void pushTransaction();

    /**
     * Pop stacked transaction and replace the existing one with that.
     */
    void popTransaction();

    /**
     * Method to determine whether a transaction is started or not.
     *
     * @return whether a transaction is started or not.
     */
    boolean isStarted();
    
    /**
     * Method to set whether a transaction is started or not.
     *
     * @param started whether a transaction is started or not.
     */
    void setStarted(boolean started);

    /**
     * This method will increment the nested depth of the transaction on the current session.
     */
    void incNestedDepth();

    /**
     * This method will decrement the nested depth of the transaction on the current session.
     */
    void decNestedDepth();

    /**
     * Method to obtain the depth of nesting of this transaction at a given point in time.
     *
     * @return the nested depth.
     */
    int getNestedDepth();

    /**
     * Method to determine whether this transaction has been rollbacked at least once. This applies
     * to the out as well as inner transactions as a whole.
     *
     * @return whether this transaction has been rollbacked.
     */
    boolean isRollbacked();

    /**
     * Method to set whether this transaction has been rollbacked or not.
     *
     * @param rollbacked whether this transaction has been rollbacked or not.
     */
    void setRollbacked(boolean rollbacked);
    
}
