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

package org.apache.axis2.description;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;

/**
 * A MessageContextListener is registered on the AxisService.
 * When a ServiceContext is attached to the MessageContext, 
 * the attachServiceContextEvent is triggered.
 * When an Envelope is attached to the MessageContext, 
 * the attachEnvelopeEvent is triggered.
 * 
 * These two events occur at critical points in the message sending
 * or receiving.  An implementation of the MessageContextListener
 * may log information, set special properties or trigger events.
 * 
 * For example the JAXWS module uses a MessageContextListener to 
 * register a JAXBCustomBuilder on the envelope's StAXOMBuilder.
 */
public interface MessageContextListener {

    public void attachServiceContextEvent(ServiceContext sc, MessageContext mc);
    
    public void attachEnvelopeEvent(MessageContext mc);
}
