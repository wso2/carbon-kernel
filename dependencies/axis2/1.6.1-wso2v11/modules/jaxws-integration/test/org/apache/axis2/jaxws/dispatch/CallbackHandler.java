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

package org.apache.axis2.jaxws.dispatch;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.message.util.Reader2Writer;

import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class CallbackHandler<T> implements AsyncHandler <T> {

    public void handleResponse(Response response) {
        TestLogger.logger.debug(">> Processing async reponse");
        try{
            T res = (T) response.get();
            
            if(res instanceof SOAPMessage){
            	SOAPMessage message = (SOAPMessage) res;
            	message.writeTo(System.out);
            	
            }
            
            if(res instanceof String){
                TestLogger.logger.debug("Response [" + res + "]");
            }
            else if(Source.class.isAssignableFrom(res.getClass())){
                Source source = (Source) res;
                
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                XMLStreamReader reader = inputFactory.createXMLStreamReader(source);
                Reader2Writer r2w = new Reader2Writer(reader);
                String responseText = r2w.getAsString();

                TestLogger.logger.debug(responseText);
            }
            TestLogger.logger.debug("---------------------------------------------");
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
