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

package org.apache.axis2.jaxws.sample.dlwminArrays;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;


@javax.jws.WebService (endpointInterface="org.apache.axis2.jaxws.sample.dlwminArrays.IGenericService", 
        targetNamespace="http://apache.org/axis2/jaxws/sample/dlwminArrays", 
        serviceName="GenericService", 
        portName="GenericServicePort")
public class GenericService implements IGenericService {

    public String sayHello(String text) {
        return "Hello " + text;
    }
    
    public WSUser[] getComplexArray() {
        WSUser[] result = new WSUser[2];
        result[0] = new WSUser("first");
        result[1] = new WSUser("second");
        return result;
    }

    public List<WSUser> getComplexList() {
        List<WSUser> result = new ArrayList<WSUser>();
        result.add(new WSUser("first"));
        result.add(new WSUser("second"));
        return result;
    }

    public String[] getSimpleArray() {
        String[] result = new String[2];
        result[0] = "first";
        result[1] = "second";
        return result;
    }

    public List<String> getSimpleList() {
        List<String> result = new ArrayList<String>();
        result.add("first");
        result.add("second");
        return result;
    }
    
    public List<WSUser> echoComplexList(List<WSUser> in ) {
        if (in.size() > 0 && in.get(0).getUserID().equals("FORCENULL")) {
            return null;
        }
        return in;
    }

    public List<WSUser> echo(List<WSUser> in, Holder<List<String>> ids) {
        List<String> l = ids.value;
        if (l != null) {
            for (int i=0; i< l.size(); i++) {
               String text = l.get(i).toUpperCase();
               l.set(i, text);
            }
        }
        return in;
    }
}

