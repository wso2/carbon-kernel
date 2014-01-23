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

package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;

public class PortInfoImpl implements PortInfo {
    private QName serviceName = null;
    private QName portName = null;
    private String bindingId = null;

    /**
     * @param serviceName
     * @param portName
     * @param bindingId
     */
    PortInfoImpl(QName serviceName, QName portName, String bindingId) {
        super();
        if (serviceName == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("portInfoErr0", "<null>"));
        }
        if (portName == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("portInfoErr1", "<null>"));
        }
        if (bindingId == null) {
            throw ExceptionFactory
                    .makeWebServiceException(Messages.getMessage("portInfoErr2", "<null>"));
        }
        this.serviceName = serviceName;
        this.portName = portName;
        this.bindingId = bindingId;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public QName getPortName() {
        return portName;
    }

    public String getBindingID() {
        return bindingId;
    }
}
