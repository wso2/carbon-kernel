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

package org.apache.axis2.jaxws.client;

import org.apache.axis2.Constants;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.spi.migrator.ApplicationContextMigrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * The PropertyMigrator implements the ApplicationContextMigrator in order to perform the necessary
 * manipulations of properties during a request or response flow.
 */
public class PropertyMigrator implements ApplicationContextMigrator, Serializable {
    private static final Log log = LogFactory.getLog(PropertyMigrator.class);
    public void migratePropertiesFromMessageContext(Map<String, Object> userContext,
                                                    MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("Starting migratePropertyFromMessageContext");
        }
        MEPContext mepContext = messageContext.getMEPContext();
        if (mepContext != null) {
            if (log.isDebugEnabled()) {
                log.debug("Reading ApplicationScopedProperties from MEPContext");
            }
            userContext.putAll(mepContext.getApplicationScopedProperties());
        }
        if (log.isDebugEnabled()) {
            log.debug("migratePropertyFromMessageContext Complete");
        }
    }

    public void migratePropertiesToMessageContext(Map<String, Object> userContext,
                                                  MessageContext messageContext) {

        // Avoid using putAll as this causes copies of the propery set
        if (userContext != null) {
            // should not use iterator here because this map may be modified
            // on different threads by the user or other JAX-WS code
            String[] keys = new String[userContext.keySet().size()];
            keys = userContext.keySet().toArray(keys);
            for(int i=0; i < keys.length; i++) {
                String key = keys[i];
                Object value = userContext.get(key);
                // Make sure mtom state in the user context, the message context, 
                // the MEP context are the same.
                if(key.equalsIgnoreCase(Constants.Configuration.ENABLE_MTOM)){
                    value = messageContext.getMessage().isMTOMEnabled();
                    messageContext.getMEPContext().put(key, value);
                }
                messageContext.setProperty(key, value);
            }
        }
    }

}
