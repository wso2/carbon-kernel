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

package org.apache.axis2.transaction;

import javax.transaction.*;


public class Axis2UserTransaction implements UserTransaction {

    private TransactionManager transactionManager = null;

    public Axis2UserTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void begin() throws NotSupportedException, SystemException {
        this.transactionManager.begin();
    }

    public void commit() throws HeuristicMixedException,
            HeuristicRollbackException,
            IllegalStateException,
            RollbackException,
            SecurityException, SystemException {
        this.transactionManager.commit();
    }

    public int getStatus() throws SystemException {
        return this.transactionManager.getStatus();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        this.transactionManager.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
       this.transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int i) throws SystemException {
       this.transactionManager.setTransactionTimeout(i);
    }
}
