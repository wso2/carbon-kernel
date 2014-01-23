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

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.ParameterInclude;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Hashtable;
import java.util.Iterator;

public class TransactionConfiguration {

    private static final Log log = LogFactory.getLog(TransactionConfiguration.class);

    public static final int DEFAULT_TX_TIME_OUT = 300000 ; //30s
    public static final String TX_MANAGER_JNDI_NAME = "TransactionManagerJNDIName";

    private int transactionTimeout = DEFAULT_TX_TIME_OUT;
    private ThreadLocal threadTransactionManager = null;
    private Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
    private String transactionManagerJNDIName = null;

    public TransactionConfiguration() {
    }

    public TransactionConfiguration(ParameterInclude transactionParameters) throws DeploymentException {
       Iterator it = transactionParameters.getParameters().iterator();
        while (it.hasNext()) {
            Parameter parameter = (Parameter) it.next();
            jndiProperties.put(parameter.getName(), (String) parameter.getValue());
        }

        transactionManagerJNDIName = (String) transactionParameters.getParameter(TX_MANAGER_JNDI_NAME).getValue();

        if(transactionManagerJNDIName == null){
             throw new DeploymentException("Required transaction parameter " + TX_MANAGER_JNDI_NAME + " missing");
        }

        threadTransactionManager = new ThreadLocal();
    }

    public int getTransactionTimeout(){
        return transactionTimeout;
    }

    public void setTransactionTimeout(int transactionTimeout){
        this.transactionTimeout = transactionTimeout;
    }

    public synchronized TransactionManager getTransactionManager() throws AxisFault {
        TransactionManager transactionManager = (TransactionManager) threadTransactionManager.get();
        if (transactionManager == null) {
            transactionManager = lookupTransactionManager();
            threadTransactionManager.set(transactionManager);
            if (log.isDebugEnabled()) log.debug("JNDI lookup TransactionManager");
        } else {
            if (log.isDebugEnabled()) log.debug("Re-use previously JNDI lookup TransactionManager");
        }
        try{
            transactionManager.setTransactionTimeout(transactionTimeout);
        } catch(Exception ignore) {}

        return transactionManager;
    }

    public UserTransaction getUserTransaction() throws AxisFault {
        return new Axis2UserTransaction(getTransactionManager());
    }

    private synchronized TransactionManager lookupTransactionManager() throws AxisFault {

        try {

            Context context = new InitialContext(jndiProperties);
            Object transactionManager = context.lookup( transactionManagerJNDIName );
            if (transactionManager != null && transactionManager instanceof TransactionManager) {
                return (TransactionManager) transactionManager;
            } else {
                log.error("TransactionManager : " + transactionManagerJNDIName + " not found when looking up" +
                        " using JNDI properties : " + context.getEnvironment());
                throw new AxisFault("TransactionManager : " + transactionManagerJNDIName + " not found when looking up" +
                        " using JNDI properties : " + context.getEnvironment());
            }

        } catch (NamingException e) {

            log.error(new StringBuilder().append("Error looking up TransactionManager ")
                    .append(" using JNDI properties : ").append(jndiProperties));
            throw new AxisFault("TransactionManager not found when looking up" +
                    " using JNDI properties : " + jndiProperties);
        }
    }


}
