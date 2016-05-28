/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi.internal;

import org.wso2.carbon.jndi.internal.impl.NamingContext;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class InMemoryInitialContextFactory implements InitialContextFactory {

    protected static volatile Context initialContext = null;
    public static final String MAIN = "initialContext";

    @SuppressWarnings("unchecked")
    @Override
    public synchronized Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {

        if (initialContext == null) {
            initialContext = new NamingContext((Hashtable<String, Object>) environment, MAIN);
        }
        return initialContext;
    }
}
