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


package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.OnDemandLogger;
import org.apache.axis2.util.Utils;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is the top most level of the Context hierarchy and is a bag of properties.
 */
public abstract class AbstractContext {

    private static final OnDemandLogger log = new OnDemandLogger(AbstractContext.class);
    
    private static final int DEFAULT_MAP_SIZE = 64;
    private static boolean DEBUG_ENABLED = log.isTraceEnabled();
    private static boolean DEBUG_PROPERTY_SET = false;
    private boolean isClusteringOn = false;
    private boolean isClusteringCheckDone = false;
    
    /**
     * Property used to indicate copying of properties is needed by context.
     */
    public static final String COPY_PROPERTIES = "CopyProperties";

    protected long lastTouchedTime;

    protected transient AbstractContext parent;
    protected transient Map<String, Object> properties;
    private transient Map<String, Object> propertyDifferences;

    protected AbstractContext(AbstractContext parent) {
        this.parent = parent;
    }

    protected AbstractContext() {
    }

    /**
     * @return Returns the parent of this context.
     */
    public AbstractContext getParent() {
        return parent;
    }

    /**
     * @param context
     * @return true if the context is an ancestor
     */
    public boolean isAncestor(AbstractContext context) {
        if (context == null) {
            return false;
        }
        for (AbstractContext ancestor = getParent();
            ancestor != null;
            ancestor = ancestor.getParent()) {
            if (ancestor == context) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return The properties
     * @deprecated Use {@link #getPropertyNames()}, {@link #getProperty(String)},
     *             {@link #setProperty(String, Object)} & {@link #removeProperty(String)}instead.
     */
    public Map<String, Object> getProperties() {
        initPropertiesMap();
        return properties;
    }

    /**
     * An iterator over a collection of <code>String</code> objects, which are the
     * keys in the properties object.
     *
     * @return Iterator over a collection of keys
     */
    public Iterator<String> getPropertyNames() {
        initPropertiesMap();
        while (true) {
            try {
                return new HashSet<String>(properties.keySet()).iterator();
            } catch (Exception cme) {
            }
        }
    }

    /**
     * Retrieves an object given a key.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getProperty(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if (obj!=null) {
            // Assume that a property which is read may be updated.
            // i.e. The object pointed to by 'value' may be modified after it is read
            if(!isClusteringCheckDone) {
                isClusteringCheckDone = true;
                isClusteringOn = needPropertyDifferences();
            }
            if(isClusteringOn) {
                addPropertyDifference(key, obj, false);
            }
        } else if (parent!=null) {
            obj = parent.getProperty(key);
        } 
        return obj;
    }

    /**
     * Retrieves an object given a key. Only searches at this level
     * i.e. getLocalProperty on MessageContext does not look in
     * the OperationContext properties map if a local result is not
     * found.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getLocalProperty(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if ((obj == null) && (parent != null)) {
            // This is getLocalProperty() don't search the hierarchy.
        } else {
            if(!isClusteringCheckDone) {
                isClusteringCheckDone = true;
                isClusteringOn = needPropertyDifferences();
            }
            if(isClusteringOn) {
                // Assume that a property is which is read may be updated.
                // i.e. The object pointed to by 'value' may be modified after it is read
                addPropertyDifference(key, obj, false);
            }
        }
        return obj;
    }
    
    /**
     * Retrieves an object given a key. The retrieved property will not be replicated to
     * other nodes in the clustered scenario.
     *
     * @param key - if not found, will return null
     * @return Returns the property.
     */
    public Object getPropertyNonReplicable(String key) {
        Object obj = properties == null ? null : properties.get(key);
        if ((obj == null) && (parent != null)) {
            obj = parent.getPropertyNonReplicable(key);
        }
        return obj;
    }

    /**
     * Store a property in this context
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        initPropertiesMap();
        while (true) {
            try {
                properties.put(key, value);
                break;
            } catch (ConcurrentModificationException cme) {
            }
        }
        if(!isClusteringCheckDone) {
            isClusteringCheckDone = true;
            isClusteringOn = needPropertyDifferences();
        }
        if(isClusteringOn) {
            addPropertyDifference(key, value, false);
        }
        if (DEBUG_ENABLED) {
            debugPropertySet(key, value);
        }
    }

    private void addPropertyDifference(String key, Object value,  boolean isRemoved) {
        // Narrowed the synchronization so that we only wait
        // if a property difference is added.
        synchronized(this) {
            // Lazizly create propertyDifferences map
            if (propertyDifferences == null) {
                propertyDifferences = new HashMap<String, Object>(DEFAULT_MAP_SIZE);
            }
            propertyDifferences.put(key, new PropertyDifference(key, value, isRemoved));
        }
    }
    
    /**
     * @return true if we need to store property differences for this 
     * context in this scenario.
     */
    private boolean needPropertyDifferences() {
        
        // Don't store property differences if there are no 
        // cluster members.
        
        ConfigurationContext cc = getRootContext();
        if (cc == null) {
            return false;
        }
        // Add the property differences only if Context replication is enabled,
        // and there are members in the cluster
        ClusteringAgent clusteringAgent = cc.getAxisConfiguration().getClusteringAgent();
        if (clusteringAgent == null ||
            clusteringAgent.getStateManager() == null) {
            return false;
        }
        return true;
    }

    /**
     * Store a property in this context.
     * But these properties should not be replicated when Axis2 is clustered.
     *
     * @param key
     * @param value
     */
    public void setNonReplicableProperty(String key, Object value) {
        initPropertiesMap();
        while (true) {
            try {
                properties.put(key, value);
                break;
            } catch (ConcurrentModificationException cme) {
            }
        }
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     *
     * @param key
     */
    public synchronized void removeProperty(String key) {
        if(properties == null){
            return;
        }
        Object value = properties.get(key);
        if (value != null) {
            if (properties != null) {
                while (true) {
                    try {
                        properties.remove(key);
                        break;
                    } catch (ConcurrentModificationException cme) {
                    }
                }
            }
            if(!isClusteringCheckDone) {
                isClusteringCheckDone = true;
                isClusteringOn = needPropertyDifferences();
            }
            if(isClusteringOn) {
                addPropertyDifference(key, value, true);
            }                           
        }
    }

    /**
     * Remove a property. Only properties at this level will be removed.
     * Properties of the parents cannot be removed using this method.
     * The removal of the property will not be replicated when Axis2 is clustered.
     *
     * @param key
     */
    public synchronized void removePropertyNonReplicable(String key) {
        if (properties != null) {
            while (true) {
                try {
                    properties.remove(key);
                    break;
                } catch (ConcurrentModificationException cme) {
                }
            }
        }
    }

    /**
     * Get the property differences since the last transmission by the clustering
     * mechanism
     *
     * @return The property differences
     */
    public synchronized Map<String, Object> getPropertyDifferences() {
        if (propertyDifferences == null) {
            propertyDifferences = new HashMap<String, Object>(DEFAULT_MAP_SIZE);
        }
        return propertyDifferences;
    }

    /**
     * Once the clustering mechanism transmits the property differences,
     * it should call this method to avoid retransmitting stuff that has already
     * been sent.
     */
    public synchronized void clearPropertyDifferences() {
        if (propertyDifferences != null) {
            propertyDifferences.clear();
        }
    }

    /**
     * @param context
     */
    public void setParent(AbstractContext context) {
        parent = context;
    }

    /**
     * This will set the properties to the context. But in setting that one may need to "copy" all
     * the properties from the source properties to the target properties. To enable this we introduced
     * a property ({@link #COPY_PROPERTIES}) so that if set to true, this code
     * will copy the whole thing, without just referencing to the source.
     *
     * @param properties
     */
    public void setProperties(Map<String, Object> properties) {
        if (properties == null) {
            this.properties = null;
        } else {
            Boolean copyProperties = ((Boolean) properties.get(COPY_PROPERTIES));

            if ((copyProperties != null) && copyProperties.booleanValue()) {
                mergeProperties(properties);
            } else {
                
                if (this.properties != properties) {
                    if (DEBUG_ENABLED) {
                        for (Iterator<Entry<String, Object>> iterator = properties.entrySet().iterator();
                        iterator.hasNext();) {
                            Entry<String, Object> entry = iterator.next();
                            debugPropertySet((String) entry.getKey(), entry.getValue());

                        }
                    }
                }
                // The Map we got argument is probably NOT an instance of the Concurrent 
                // map we use to store properties, so create a new one using the values from the
                // argument map.
                while (true) {
                    try {
                        this.properties = new HashMap(properties);
                        break;
                    } catch (ConcurrentModificationException cme) {
                    }
                }
            }
        }
    }

    /**
     * This will do a copy of the given properties to the current properties
     * table.
     *
     * @param props The table of properties to copy
     */
    public void mergeProperties(Map<String, Object> props) {
        if (props != null) {
            initPropertiesMap();
            for (Iterator<String> iterator = props.keySet().iterator();
                 iterator.hasNext();) {
                String key = iterator.next();
                Object value = props.get(key);
                while (true) {
                    try {
                        properties.put(key, value);
                        break;
                    } catch (ConcurrentModificationException cme) {
                    }
                }
                if (DEBUG_ENABLED) {
                    debugPropertySet((String) key, value);
                }
            }
        }
    }

    /**
     * ServiceContext and ServiceGroupContext are not getting automatically garbage collected. And there
     * is no specific way for some one to go and make it garbage collectible.
     * So the current solution is to make them time out. So the logic is that, there is a timer task
     * in each and every service group which will check for the last touched time. And if it has not
     * been touched for some time, the timer task will remove it from the memory.
     * The touching logic happens like this. Whenever there is a call to addMessageContext in the operationContext
     * it will go and update operationCOntext -> serviceContext -> serviceGroupContext.
     */
    protected void touch() {
        lastTouchedTime = System.currentTimeMillis();
        if (parent != null) {
            parent.touch();
        }
    }

    public long getLastTouchedTime() {
        return lastTouchedTime;
    }

    public void setLastTouchedTime(long t) {
        lastTouchedTime = t;
    }

    public void flush() throws AxisFault {
        Replicator.replicate(this);
    }

    public abstract ConfigurationContext getRootContext();

    /**
     * Debug for for property key and value.
     * @param key
     * @param value
     */
    private void debugPropertySet(String key, Object value) {
        if (DEBUG_PROPERTY_SET) {
            String className = (value == null) ? "null" : value.getClass().getName();
            String classloader = "null";
            if(value != null) {
                ClassLoader cl = Utils.getObjectClassLoader(value);
                if(cl != null) {
                    classloader = cl.toString();
                }
            }
            String valueText = (value instanceof String) ? value.toString() : null;
            String identity = getClass().getName() + '@' + 
                Integer.toHexString(System.identityHashCode(this));
            
            log.debug("==================");
            log.debug(" Property set on object " + identity);
            log.debug("  Key =" + key);
            if (valueText != null) {
                log.debug("  Value =" + valueText);
            }
            log.debug("  Value Class = " + className);
            log.debug("  Value Classloader = " + classloader);
            log.debug(  "Call Stack = " + JavaUtils.callStackToString());
            log.debug("==================");
        }
    }
    
    /**
     * If the 'properties' map has not been allocated yet, then allocate it. 
     */
    private void initPropertiesMap() {
        if (properties == null) {
            // This needs to be a concurrent collection to prevent ConcurrentModificationExcpetions
            // for async-on-the-wire.  It was originally: 
//            properties = new HashMap(DEFAULT_MAP_SIZE);
            properties = new HashMap<String,Object>(DEFAULT_MAP_SIZE);
        }
    }
}
