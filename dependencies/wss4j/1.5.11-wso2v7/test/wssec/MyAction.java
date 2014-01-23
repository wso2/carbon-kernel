/**
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

package wssec;

import org.apache.ws.security.action.Action;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.WSSecurityException;

/**
 * a custom action that leaves a breadcumb
 */
public class MyAction implements Action {
    
    public void 
    execute(
        WSHandler handler, 
        int actionToDo, 
        org.w3c.dom.Document doc,
        RequestData reqData
    ) throws WSSecurityException {
        //
        // leave a breadcrumb, if asked...
        //
        if (reqData.getMsgContext().equals("bread")) {
            reqData.setMsgContext("crumb");
        }
    }
}
