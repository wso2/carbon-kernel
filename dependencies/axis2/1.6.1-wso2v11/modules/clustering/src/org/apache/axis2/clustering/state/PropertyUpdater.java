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

package org.apache.axis2.clustering.state;

import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.PropertyDifference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 */
public class PropertyUpdater implements Serializable {
    private static final Log log = LogFactory.getLog(PropertyUpdater.class);

    private Map properties;

    public void updateProperties(AbstractContext abstractContext) {
        if (log.isDebugEnabled()) {
            log.debug("Updating props in " + abstractContext);
        }
        if (abstractContext != null) {
            for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                PropertyDifference propDiff =
                        (PropertyDifference) properties.get(key);
                if (propDiff.isRemoved()) {
                    abstractContext.removePropertyNonReplicable(key);
                } else {  // it is updated/added
                    abstractContext.setNonReplicableProperty(key, propDiff.getValue());
                    if (log.isDebugEnabled()) {
                        log.debug("Added prop=" + key + ", value=" + propDiff.getValue() +
                                  " to context " + abstractContext);
                    }
                }
            }
        }
    }

    public void addContextProperty(PropertyDifference diff) {
        properties.put(diff.getKey(), diff);
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public Map getProperties() {
        return properties;
    }
}
