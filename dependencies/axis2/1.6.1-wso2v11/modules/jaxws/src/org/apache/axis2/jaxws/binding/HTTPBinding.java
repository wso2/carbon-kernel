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

package org.apache.axis2.jaxws.binding;

import java.util.List;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.axis2.jaxws.description.EndpointDescription;

public class HTTPBinding extends BindingImpl implements javax.xml.ws.http.HTTPBinding {

    public HTTPBinding(EndpointDescription ed) {
        super(ed);
    }

    @Override
    public void setHandlerChain(List<Handler> list) {
        if (list != null) {
            for (Handler handler : list) {
                if (handler instanceof SOAPHandler) {
                    throw new WebServiceException("Chain contains incompatibile handler");
                }
            }
        }
        super.setHandlerChain(list);
    }
}
