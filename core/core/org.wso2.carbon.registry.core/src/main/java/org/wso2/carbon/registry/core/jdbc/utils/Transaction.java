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

package org.wso2.carbon.registry.core.jdbc.utils;

import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;


/**
 * This is the base class that manages the transactional database operations of the Registry. The
 * transactions API of the Registry is capable of handling nested transactions. If a nested
 * transaction is rollbacked, the whole outer transaction would also be rollbacked, so that the
 * database will not be in an inconsistent state.
 * <p/>
 * The transaction API also supports transactions across multiple databases. This allows various
 * portions of the Registry to be mounted from various databases and work as a part of a single
 * tree.
 * <p/>
 * The transaction is session scoped, and therefore, is available only on a single running thread.
 * Multiple threads would have its own transaction and thereby make it possible to manage
 * concurrency at a much lower level.
 */
public class Transaction {

    private static DataAccessManager dataAccessManager = null;

    /**
     * Initializes the Transaction Object. This method is responsible of initializing the
     * database transactions, which is required for executing queries in the future.
     *
     * @param dataAccessManager the data access manager to be used to communicate with the database.
     */
    public synchronized static void init(DataAccessManager dataAccessManager) {
        Transaction.dataAccessManager = dataAccessManager;
    }

    /**
     * Push current transaction to a stack and make room for a new one.
     */
    public static void pushTransaction() {
        Transaction.dataAccessManager.getDatabaseTransaction().pushTransaction();
    }

    /**
     * Pop stacked transaction and replace the existing one with that.
     */
    public static void popTransaction() {
        Transaction.dataAccessManager.getDatabaseTransaction().popTransaction();
    }

    /**
     * Method to determine whether a transaction is started or not.
     *
     * @return whether a transaction is started or not.
     */
    public static boolean isStarted() {
        return Transaction.dataAccessManager.getDatabaseTransaction().isStarted();
    }

    /**
     * Method to determine whether a transaction has been rollbacked or not.
     *
     * @return whether a transaction has been rollbacked or not.
     */
    public static boolean isRollbacked() {
        return Transaction.dataAccessManager.getDatabaseTransaction().isRollbacked();
    }

}
