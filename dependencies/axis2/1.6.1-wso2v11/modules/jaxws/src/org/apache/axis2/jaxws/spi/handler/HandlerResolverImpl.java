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

package org.apache.axis2.jaxws.spi.handler;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainType;
import org.apache.axis2.jaxws.description.xml.handler.HandlerType;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.lifecycle.LifecycleException;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is an implementation of {@link HandlerResolver} that can be used with a JAX-WS client
 * to set the handler list.
 *
 * @see javax.xml.ws.Service#setHandlerResolver(HandlerResolver)
 */
public class HandlerResolverImpl extends BaseHandlerResolver {

    private static Log log = LogFactory.getLog(HandlerResolverImpl.class);

    /**
     * Constructor
     * 
     * @param filePath the path to the handler configuration file in URI format
     */
    public HandlerResolverImpl(String filePath) {
        super(filePath);
    }
    
    /**
     * Constructor
     * 
     * @param fileURI the <code>URI</code> of the handler configuration file
     */
    public HandlerResolverImpl(URI fileURI) {
        this(fileURI.toString());
    }
    
    /**
     * Constructor
     * 
     * @param file the handler configuration file
     */
    public HandlerResolverImpl(File file) {
        this(file.toURI());
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.xml.ws.handler.HandlerResolver#getHandlerChain(javax.xml.ws.handler.PortInfo)
     */
    public List<Handler> getHandlerChain(PortInfo portinfo) {
        ArrayList<Handler> handlers = new ArrayList<Handler>();
        Iterator it = handlerChainsType == null ? null : handlerChainsType.getHandlerChain().iterator();

        while ((it != null) && (it.hasNext())) {
            HandlerChainType handlerChainType = ((HandlerChainType)it.next());
            
            // if !match, continue (to next chain)
            if (!(chainResolvesToPort(handlerChainType, portinfo)))
                continue;
            
            List<HandlerType> handlerTypeList = handlerChainType.getHandler();
            Iterator ht = handlerTypeList.iterator();
            while (ht.hasNext()) {
                
                HandlerType handlerType = (HandlerType)ht.next();
                
                // TODO must do better job comparing the handlerType with the PortInfo param
                // to see if the current iterator handler is intended for this service.

                // TODO review: need to check for null getHandlerClass() return?
                // or will schema not allow it?
                String portHandler = handlerType.getHandlerClass().getValue();
                Handler handler = null;
                    
                //  instantiate portHandler class 
                try {
                    handler = createHandlerInstance(loadClass(portHandler));
                } catch (Exception e) {
                    // TODO: should we just ignore this problem?
                    throw ExceptionFactory.makeWebServiceException(e);
                }
                
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("Successfully instantiated the class: " + handler.getClass());
                }
                
                // 9.2.1.2 sort them by Logical, then SOAP
                if (LogicalHandler.class.isAssignableFrom(handler.getClass()))
                    handlers.add((LogicalHandler) handler);
                else if (SOAPHandler.class.isAssignableFrom(handler.getClass()))
                    // instanceof ProtocolHandler
                    handlers.add((SOAPHandler) handler);
                else if (Handler.class.isAssignableFrom(handler.getClass())) {
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr1", handler
                                    .getClass().getName()));
                } else {
                    throw ExceptionFactory.makeWebServiceException(Messages
                            .getMessage("handlerChainErr2", handler
                                    .getClass().getName()));
                }
            }
        }

        return handlers;
    }
    
    //Create an instance of the handler class and perform appropriate lifecycle activities
    //to it.
    private Handler createHandlerInstance(Class handlerClass) throws LifecycleException {
        if (handlerClass == null) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("createHandlerInstanceErr"));
        }
        
        //Resource injection not supported.
        
        Object object = null;
        
        try {
            object = handlerClass.newInstance();
        } catch (InstantiationException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        } catch (IllegalAccessException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
                
        //Invoke PostConstruct
        LifecycleManager manager = new LifecycleManager(object);
        manager.invokePostConstruct();
        
        return (Handler) object;
    }
}
