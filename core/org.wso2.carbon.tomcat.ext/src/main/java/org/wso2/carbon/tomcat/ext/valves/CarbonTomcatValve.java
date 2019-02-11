/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.tomcat.ext.valves;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 *  Maps to the {@link org.apache.catalina.Valve} in Tomcat
 */
public abstract class CarbonTomcatValve {

    private CarbonTomcatValve next;


    /**
     * Invoke the valve
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     */
    public abstract void invoke(Request request, Response response, CompositeValve compositeValve);

    /**
     *
     * @return CarbonTomcatValve
     */
    public CarbonTomcatValve getNext(){
        if(next == null){
            return new CarbonTomcatValve() {
                @Override
                public void invoke(Request request, Response response, CompositeValve compositeValve) {
                    // With the final valve
                    compositeValve.continueInvocation(request, response);
                }
            };
        }
        return next;
    }

    /**
     *
     * @param valve
     */
    void setNext(CarbonTomcatValve valve){
        next = valve;
    }
}
