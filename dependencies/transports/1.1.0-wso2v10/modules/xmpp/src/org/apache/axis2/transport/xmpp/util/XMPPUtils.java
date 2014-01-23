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

package org.apache.axis2.transport.xmpp.util;

import org.apache.axis2.AxisFault;


public class XMPPUtils {
    
	/**
	 * Extract XMPP server accountName section from transport URL passed in.  
	 * @param transportUrl
	 * @return String 
	 * @throws AxisFault
	 */
    public static String getAccountName(String transportUrl) throws AxisFault{
    	String accountName = "";
    	if(transportUrl == null){
    		return null;
    	}
    	
        if (!transportUrl.startsWith(XMPPConstants.XMPP)) {
            throw new AxisFault ("Invalid XMPP URL : " + transportUrl +
                    " Must begin with the prefix xmpp");
        }
        //eg: transportUrl is similar to xmpp://axisserver@sumedha/Version
        int start = transportUrl.indexOf("://") + 3;
        int end = transportUrl.lastIndexOf("/"); //first index
        if(start != -1 && end != -1){
        	accountName = transportUrl.substring(start, end);
        }else{
        	accountName = transportUrl;
        }
        return accountName;
    }

    /**
     * Extract Service name from transport URL passed in
     * @param transportUrl
     * @return
     * @throws AxisFault
     */
    public static String getServiceName(String transportUrl) throws AxisFault{
    	String serviceName = "";
    	if(transportUrl == null){
    		return null;
    	}
        if (!transportUrl.startsWith(XMPPConstants.XMPP)) {
            throw new AxisFault ("Invalid XMPP URL : " + transportUrl +
                    " Must begin with the prefix xmpp");
        }
        //eg: transportUrl is similar to xmpp://axisserver@sumedha/Version
        int start = transportUrl.lastIndexOf("/") + 1;
        int end = transportUrl.length();
        if(start != -1 && end != -1){
        	serviceName = transportUrl.substring(start, end);
        }
        return serviceName;
    }
    
}

