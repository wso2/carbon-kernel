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


package org.apache.axis2.handlers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Handler.InvocationResponse;

/**
 * Class AbstractHandler
 */
public abstract class AbstractHandler implements Handler {

    /**
     * Field handlerDesc
     */
    protected HandlerDescription handlerDesc;

    /**
     * Constructor AbstractHandler.
     */
    public AbstractHandler() {
        handlerDesc = new HandlerDescription("DefaultHandler");
    }

    /**
     * Since this might change the whole behavior of Axis2 handlers, and since this is still under discussion
     * (http://marc.theaimsgroup.com/?l=axis-dev&m=114504084929285&w=2) implementation of this method is deferred.
     * Note : This method will not be automatically called, from Axis2 engine, until this is fully implemented.
     */
    public void cleanup() {
    }

    /**
     * Method init.
     *
     */
    public void init(HandlerDescription handlerdesc) {
        this.handlerDesc = handlerdesc;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String name = this.getName();

        return (name != null)
                ? name
                : null;
    }

    /**
     * Gets the phaseRule of a handler.
     *
     * @return Returns HandlerDescription.
     */
    public HandlerDescription getHandlerDesc() {
        return handlerDesc;
    }

    /**
     * Method getName.
     *
     * @return Returns QName.
     */
    public String getName() {
        return handlerDesc.getName();
    }

    /**
     * Method getParameter.
     *
     * @param name name of the parameter
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        return handlerDesc.getParameter(name);
    }

    public void flowComplete(MessageContext msgContext) {
    }

}
