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

package org.apache.axis2.jaxws.registry;

import org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.factory.impl.Axis2EndpointReferenceFactoryImpl;
import org.apache.axis2.jaxws.addressing.factory.impl.JAXWSEndpointReferenceFactoryImpl;
import org.apache.axis2.jaxws.api.MessageAccessorFactory;
import org.apache.axis2.jaxws.api.MessageAccessorFactoryImpl;
import org.apache.axis2.jaxws.core.controller.InvocationControllerFactory;
import org.apache.axis2.jaxws.core.controller.impl.InvocationControllerFactoryImpl;
import org.apache.axis2.jaxws.handler.factory.HandlerInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.HandlerPostInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.HandlerPreInvokerFactory;
import org.apache.axis2.jaxws.handler.factory.impl.HandlerInvokerFactoryImpl;
import org.apache.axis2.jaxws.handler.factory.impl.HandlerPostInvokerFactoryImpl;
import org.apache.axis2.jaxws.handler.factory.impl.HandlerPreInvokerFactoryImpl;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.message.databinding.impl.JAXBBlockFactoryImpl;
import org.apache.axis2.jaxws.message.databinding.impl.OMBlockFactoryImpl;
import org.apache.axis2.jaxws.message.databinding.impl.ParsedEntityReaderImpl;
import org.apache.axis2.jaxws.message.databinding.impl.SOAPEnvelopeBlockFactoryImpl;
import org.apache.axis2.jaxws.message.databinding.impl.SourceBlockFactoryImpl;
import org.apache.axis2.jaxws.message.databinding.impl.XMLStringBlockFactoryImpl;
import org.apache.axis2.jaxws.message.databinding.impl.DataSourceBlockFactoryImpl;
import org.apache.axis2.jaxws.message.factory.ClassFinderFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.factory.ParsedEntityReaderFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.factory.DataSourceBlockFactory;
import org.apache.axis2.jaxws.message.impl.MessageFactoryImpl;
import org.apache.axis2.jaxws.message.impl.XMLPartFactoryImpl;
import org.apache.axis2.jaxws.message.util.impl.SAAJConverterFactoryImpl;
import org.apache.axis2.jaxws.server.AsyncHandlerProxyFactory;
import org.apache.axis2.jaxws.server.AsyncHandlerProxyFactoryImpl;
import org.apache.axis2.jaxws.server.ServiceInstanceFactory;
import org.apache.axis2.jaxws.server.ServiceInstanceFactoryImpl;
import org.apache.axis2.jaxws.server.dispatcher.factory.EndpointDispatcherFactory;
import org.apache.axis2.jaxws.server.dispatcher.factory.EndpointDispatcherFactoryImpl;
import org.apache.axis2.jaxws.server.endpoint.injection.WebServiceContextInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.impl.WebServiceContextInjectorImpl;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.impl.EndpointLifecycleManagerFactoryImpl;
import org.apache.axis2.jaxws.utility.ExecutorFactory;
import org.apache.axis2.jaxws.utility.JAXWSExecutorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.WebServiceContext;
import java.util.HashMap;
import java.util.Map;

/** 
 * FactoryRegistry Registry containing Factories related to the JAX-WS Implementation.
 * The expected scenario is:
 *   1) Most or all of the factories are registered during startup.
 *   2) There are a large number of getFactory calls
 *   3) There may be an infrequent call to setFactory.
 *   
 * Thus a "copy on put" approach is used.  This ensures that the "gets" are 
 * fast (because they are unsynchronized).  The "puts" are slower because they
 * create a new copy of the HashMap.
 * See http://www.ibm.com/developerworks/java/library/j-hashmap.html
 **/
public class FactoryRegistry {

    private static volatile Map<Class, Object> table;
    private static Object lockbox = new Object();
    private static final Log log = LogFactory.getLog(FactoryRegistry.class);
    
    static {
        try {
            init();
        } catch (Throwable t){
            log.error(t.getMessage(), t);
        }
    }
    
    private static final void init() {
        
        // An unsynchronized Map is used to ensure that gets are fast.
        table = new HashMap<Class, Object>(64, .5f);
        
        // Load Factories
        table.put(XMLStringBlockFactory.class, new XMLStringBlockFactoryImpl());
        table.put(EndpointDispatcherFactory.class, new EndpointDispatcherFactoryImpl());
        table.put(JAXBBlockFactory.class, new JAXBBlockFactoryImpl());
        table.put(OMBlockFactory.class, new OMBlockFactoryImpl());
        table.put(SourceBlockFactory.class, new SourceBlockFactoryImpl());
        table.put(DataSourceBlockFactory.class, new DataSourceBlockFactoryImpl());
        table.put(SOAPEnvelopeBlockFactory.class, new SOAPEnvelopeBlockFactoryImpl());
        table.put(MessageFactory.class, new MessageFactoryImpl());
        table.put(XMLPartFactory.class, new XMLPartFactoryImpl());
        table.put(SAAJConverterFactory.class, new SAAJConverterFactoryImpl());
        table.put(EndpointLifecycleManagerFactory.class, new EndpointLifecycleManagerFactoryImpl());
        table.put(HandlerLifecycleManagerFactory.class, new HandlerLifecycleManagerFactory());
        table.put(ClassFinderFactory.class, new ClassFinderFactory());
        table.put(JAXWSEndpointReferenceFactory.class, new JAXWSEndpointReferenceFactoryImpl());
        table.put(Axis2EndpointReferenceFactory.class, new Axis2EndpointReferenceFactoryImpl());
        table.put(ExecutorFactory.class, new JAXWSExecutorFactory());
        table.put(ServiceInstanceFactory.class, new ServiceInstanceFactoryImpl());
        table.put(InvocationControllerFactory.class, new InvocationControllerFactoryImpl());
        table.put(HandlerPreInvokerFactory.class, new HandlerPreInvokerFactoryImpl());
        table.put(HandlerPostInvokerFactory.class, new HandlerPostInvokerFactoryImpl());
        table.put(ParsedEntityReaderFactory.class, new ParsedEntityReaderFactory());
        // register the implementation responsible for both WebServiceContext 
        // injection and the updating of the WebServiceContext instances that
        // have already been injected, we will register these by two different
        // classes because it is possible that the implementation is in different
        // classes
        WebServiceContextInjectorImpl wsciImpl = new WebServiceContextInjectorImpl();
        table.put(WebServiceContext.class, wsciImpl);
        table.put(WebServiceContextInjector.class, wsciImpl);
        table.put(HandlerInvokerFactory.class, new HandlerInvokerFactoryImpl());
        table.put(AsyncHandlerProxyFactory.class, new AsyncHandlerProxyFactoryImpl());
        table.put(MessageAccessorFactory.class, new MessageAccessorFactoryImpl());
    }

    /** FactoryRegistry is currently a static singleton */
    private FactoryRegistry() {
    }

    /**
     * Get the factory.  This may be called frequently.
     * @param intface of the Factory
     * @return Object that is the factory implementation for the intface
     */
    public static Object getFactory(Class intface) {
        Map m = table;
        return m.get(intface);
    }

    /**
     * Add the factory.  This should be called infrequently.
     * @param intface
     * @param factoryObject
     */
    public static void setFactory(Class intface, Object factoryObject) {
        synchronized(lockbox) {
            // Use copy and put approach to ensure that "get" speed is fast.
            Map<Class, Object> newMap = new HashMap<Class, Object>(table);
            newMap.put(intface, factoryObject);
            table = newMap;
        }
    }
}
