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

package org.apache.axis2.jaxws.spi.migrator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.handler.MEPContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.handler.MessageContext.Scope;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class ApplicationContextMigratorUtil {

    private static final Log log = LogFactory.getLog(ApplicationContextMigrator.class);

    /**
     * Register a new ContextPropertyMigrator.
     *
     * @param configurationContext
     * @param contextMigratorListID The name of the property in the ConfigurationContext that
     *                              contains the list of migrators.
     * @param migrator
     */
    public static void addApplicationContextMigrator(ConfigurationContext configurationContext,
                                                     String contextMigratorListID,
                                                     ApplicationContextMigrator migrator) {
        List<ApplicationContextMigrator> migratorList =
                (List<ApplicationContextMigrator>)configurationContext
                        .getProperty(contextMigratorListID);

        if (migratorList == null) {
            migratorList = new LinkedList<ApplicationContextMigrator>();
            configurationContext.setProperty(contextMigratorListID, migratorList);
        }

        synchronized (migratorList) {
            // Check to make sure we haven't already added this migrator to the
            // list.
            ListIterator<ApplicationContextMigrator> itr = migratorList.listIterator();
            while (itr.hasNext()) {
                ApplicationContextMigrator m = itr.next();
                if (m.getClass().equals(migrator.getClass())) {
                    return;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Adding ApplicationContextMigrator: " + migrator.getClass().getName());
            }
            migratorList.add(migrator);
        }
    }

    /**
     * @param contextMigratorListID
     * @param requestContext
     * @param messageContext
     */
    public static void performMigrationToMessageContext(String contextMigratorListID,
                                                        Map<String, Object> requestContext,
                                                        MessageContext messageContext) {
        if (messageContext == null) {
        	
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("nullMsgCtxErr"));
        }

        ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
        if (sd != null) {
            ConfigurationContext configCtx = sd.getAxisConfigContext();
            List<ApplicationContextMigrator> migratorList = (List<ApplicationContextMigrator>) configCtx.getProperty(contextMigratorListID);
            if (migratorList != null) {
                
                // Create copy to avoid using shared list
                List listCPM = null;
                
                // synchronize on non-null migratorList
                synchronized(migratorList){
                     listCPM = new ArrayList(migratorList);
                }
                
                ListIterator<ApplicationContextMigrator> itr = listCPM.listIterator();   // Iterate over non-shared list
                while (itr.hasNext()) {
                    ApplicationContextMigrator cpm = itr.next();
                    if (log.isDebugEnabled()) {
                        log.debug("migrator: " + cpm.getClass().getName() + ".migratePropertiesToMessageContext");
                    }
                    
                    // TODO: Synchronizing here is expensive too.
                    // If a cpm requires synchronization, it should provide it inside of its migratePropertiesFromMessageContext implementation.
                    
                    cpm.migratePropertiesToMessageContext(new ApplicationPropertyMapReader(requestContext, messageContext.getMEPContext()), messageContext);
                }
            }
        }
    }

    /**
     * @param contextMigratorListID
     * @param responseContext
     * @param messageContext
     */
    public static void performMigrationFromMessageContext(String contextMigratorListID,
                                                          Map<String, Object> responseContext,
                                                          MessageContext messageContext) {
        if (messageContext == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("nullMsgCtxErr"));
        }

        ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
        if (sd != null) {
            ConfigurationContext configCtx = sd.getAxisConfigContext();
            List<ApplicationContextMigrator> migratorList =
                    (List<ApplicationContextMigrator>)configCtx.getProperty(contextMigratorListID);

            if (migratorList != null) {
                
                // Create copy to avoid using shared list
                List listCPM = null;
                
                // synchronize on non-null migratorList
                synchronized(migratorList){
                     listCPM = new ArrayList(migratorList);
                }
            
                ListIterator<ApplicationContextMigrator> itr = listCPM.listIterator();   // Iterate over non-shared list
                while (itr.hasNext()) {
                    ApplicationContextMigrator cpm = itr.next();
                    if (log.isDebugEnabled()) {
                        log.debug("migrator: " + cpm.getClass().getName() + ".migratePropertiesFromMessageContext");
                    }

                    // TODO: Synchronizing here is expensive too.
                    // If a cpm requires synchronization, it should provide it inside of its migratePropertiesFromMessageContext implementation.

                    cpm.migratePropertiesFromMessageContext(new ApplicationPropertyMapWriter(responseContext, messageContext.getMEPContext()), messageContext);
                }
            }
        }
    }
    

    /**
     *
     * ApplicationPropertyMapReader is a wrapper for the SOURCE property map passed to individual
     * property migrators.  When a property migrator copies properties from a request context map
     * to a JAXWS MessageContext object, all of those properties should be marked APPLICATION
     * scope so they can later be retrieved from the request context or response context
     * in the client application.
     *
     * We override the EntrySet and Iterator to make sure the scope is properly set in the
     * "request context to JAXWS message context" case where the property migrator uses
     * get(String key) or putAll(Map source).  This is not guaranteed to be correct, however,
     * because a property migrator could simply be doing a get(String key) to observe properties
     * rather than copy them.  This just means we might be setting scope for a property that
     * never actually makes its way into the JAXWS message context.  If someone (a hander,
     * perhaps) later sets a property with the same key, its scope may be "pre-set" and
     * therefore incorrect.
     * 
     * TODO:  find solution to above problem.  The MEPContext.put sets an explicit scope whenever
     * a property is and a scope is not already present for that property.  An example
     * of where this idea would produce unexpected results is where a scope was set to APPLICATION
     * in the property migrator for key/value pair "myKey/someValue", but myKey never actually made
     * it into the messagecontext.  Later a handler might put a "myKey/theHandlerValue".  In this
     * case the scope was already set to APPLICATION and would therefore not be set by the
     * MEPContext.put and therefore be incorrect.
     *
     * ApplicationPropertyMapReader only sets the scope if a migrator calls "get" on this map or
     * iterates over the entrySet, which may occur explicitly in the migrator, or implicitly when
     * this map is the source for a call such as otherMap.putAll(Map source).
     */
    private static class ApplicationPropertyMapReader extends HashMap<String, Object> {

        private Map<String, Object> userMap;
        private MEPContext mepCtx;
        
        public ApplicationPropertyMapReader(Map<String, Object> userMap, MEPContext mepCtx) {
            this.userMap = userMap;
            this.mepCtx = mepCtx;
        }
        
        @Override
        public Object put(String key, Object value) {
            //mepCtx.setScope(key, Scope.APPLICATION);
            return userMap.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            // we need to take advantage of the smarter put(String, Object)
            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                put((String)entry.getKey(), entry.getValue());
            }
        }
        
        @Override
        public boolean containsKey(Object key) {
            return userMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return userMap.containsValue(value);
        }

        @Override
        public Set entrySet() {
            return new ApplicationPropertyMapEntrySet(userMap.entrySet(), mepCtx);
        }

        @Override
        public Object get(Object key) {
            // WARNING:  there's no real guarantee that the reason a migrator is getting
            // a property is due to it being put on the MessageContext.
            // We would therefore be setting scope for a property that never actually makes
            // its way into the messageContext.
            Object obj = userMap.get(key);
            if (obj != null) {
                mepCtx.setScope((String)key, Scope.APPLICATION);
            }
            return obj;
        }

        @Override
        public boolean isEmpty() {
            return userMap.isEmpty();
        }

        @Override
        public Set keySet() {
            return userMap.keySet();
        }

        @Override
        public Object remove(Object key) {
            return userMap.remove(key);
        }

        @Override
        public int size() {
            return userMap.size();
        }

        @Override
        public Collection values() {
            return userMap.values();
        }
        
        private class ApplicationPropertyMapEntrySet extends AbstractSet {

            Set containedSet;
            MEPContext mepCtx;
            
            public ApplicationPropertyMapEntrySet(Set set, MEPContext mepCtx) {
                containedSet = set;
                this.mepCtx = mepCtx;
            }
            
            @Override
            public EntrySetIterator iterator() {
                return new EntrySetIterator(containedSet.iterator(), mepCtx);
            }

            @Override
            public int size() {
                return containedSet.size();
            }
            
        }
        
        private class EntrySetIterator implements Iterator {
            
            private Iterator containedIterator;
            private MEPContext mepCtx;
            
            private EntrySetIterator(Iterator containedIterator, MEPContext mepCtx) {
                this.containedIterator = containedIterator;
                this.mepCtx = mepCtx;
            }
            
            // override remove() to make this Iterator class read-only
            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return containedIterator.hasNext();
            }

            public Object next() {
                // WARNING:  there's no real guarantee that the reason a migrator is iterating
                // over the properties is due to this being the source object for a putAll(source)
                // We would therefore be setting scope for a property that never actually makes
                // its way into the messageContext
                Map.Entry entry = (Map.Entry)containedIterator.next();
                mepCtx.setScope((String)entry.getKey(), Scope.APPLICATION);
                return entry;
            }
        }
     }
    
    /**
     * ApplicationPropertyMapWriter is similar to the ApplicationPropertyMapReader in that it
     * observes scope to determine what can be returned to a property migrator.  Individual
     * property migrators should only be allowed to retrieve APPLICATION-scoped properties.
     * 
     * TODO:  There's quite a bit of expensive logic that would need to go into this to be
     * fully correct.  For example, if a migrator calls size, we cannot simply return
     * userMap.size().  Rather, we would have to count only the APPLICATION scoped properties
     * and return those.
     */
    private static class ApplicationPropertyMapWriter extends HashMap<String, Object> {

        private Map<String, Object> userMap;
        private MEPContext mepCtx;
        
        public ApplicationPropertyMapWriter(Map<String, Object> userMap, MEPContext mepCtx) {
            this.userMap = userMap;
            this.mepCtx = mepCtx;
        }
        
        @Override
        public Object put(String key, Object value) {
            // notice the logic here!  We won't put a property on the userMap that is not APPLICATION scoped
            if (mepCtx.getScope(key) == Scope.APPLICATION) {
                return userMap.put(key, value);
            }
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {
            // we need to take advantage of the smarter put(String, Object)
            for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry)it.next();
                put((String)entry.getKey(), entry.getValue());
            }
        }
        
        @Override
        public boolean containsKey(Object key) {
            if (mepCtx.getScope((String)key) == Scope.APPLICATION) {
                return userMap.containsKey(key);
            }
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return userMap.containsValue(value);
        }

        @Override
        public Set entrySet() {
            return userMap.entrySet();
        }

        @Override
        public Object get(Object key) {
            return userMap.get(key);
        }

        @Override
        public boolean isEmpty() {
            return userMap.isEmpty();
        }

        @Override
        public Set keySet() {
            return userMap.keySet();
        }

        @Override
        public Object remove(Object key) {
            return userMap.remove(key);
        }

        @Override
        public int size() {
            return userMap.size();
        }

        @Override
        public Collection values() {
            return userMap.values();
        }
     }
}
