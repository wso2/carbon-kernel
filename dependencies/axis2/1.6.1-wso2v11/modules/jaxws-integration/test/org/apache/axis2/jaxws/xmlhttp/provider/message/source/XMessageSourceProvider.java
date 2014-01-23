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

package org.apache.axis2.jaxws.xmlhttp.provider.message.source;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;

import org.apache.axis2.transport.http.HTTPConstants;

/**
 * Sample XML/HTTP String Provider 
 */
@WebServiceProvider(serviceName="XMessageSourceProvider")
@BindingType(HTTPBinding.HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class XMessageSourceProvider implements Provider<Source> {

    @Resource
    public WebServiceContext ctx;
    
    public Source invoke(Source input) {
        String method = (String)ctx.getMessageContext().get(HTTPConstants.HTTP_METHOD);
        if (input == null) {
            String request = "<response>" + method + "</response>";
            ByteArrayInputStream stream = new ByteArrayInputStream(request.getBytes());
            input = new StreamSource((InputStream) stream);
        }
        return input;
    }

}
