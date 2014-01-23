/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.core.services.authentication;


import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.core.common.AuthenticationException;

import java.util.HashMap;
import java.util.Map;


/**
 * Test class for AuthenticationUtil.
 */
public class AuthenticationUtilTest extends TestCase {

    private MessageContext getMessageContext(Map<String, String> map) {
        MessageContext messageContext = new MessageContext();

        messageContext.setProperty(MessageContext.TRANSPORT_HEADERS, map);

        return messageContext;


    }

    public void testOriginatingIPRemoteAddress() throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_ORIGINATING_IP_FORM_1, "172.192.2.1");

        MessageContext msgContext = getMessageContext(map);

        String address = AuthenticationUtil.getRemoteAddress(msgContext);

        Assert.assertEquals("172.192.2.1", address);
    }

    public void testOriginatingIPRemoteAddressNegative() {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_ORIGINATING_IP_FORM_1, "zdfsdfdfsd");

        MessageContext msgContext = getMessageContext(map);

        try {
            AuthenticationUtil.getRemoteAddress(msgContext);
            Assert.fail("Not a valid originating IP, should fail");
        } catch (AuthenticationException e) {

        }
    }

    public void testXIPRemoteAddress() throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_ORIGINATING_IP_FORM_2, "172.192.2.1");

        MessageContext msgContext = getMessageContext(map);

        String address = AuthenticationUtil.getRemoteAddress(msgContext);

        Assert.assertEquals("172.192.2.1", address);
    }

    public void testXIPRemoteAddressNegative() {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_ORIGINATING_IP_FORM_2, "zdfsdfdfsd");

        MessageContext msgContext = getMessageContext(map);

        try {
            AuthenticationUtil.getRemoteAddress(msgContext);
            Assert.fail("Not a valid originating IP, should fail");
        } catch (AuthenticationException e) {

        }
    }

    public void testForwardedForRemoteAddress() throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_FORWARDED_FOR, "172.192.2.1 , 172.192.2.0, 172.192.5.1, 172.193.2.1");

        MessageContext msgContext = getMessageContext(map);

        String address = AuthenticationUtil.getRemoteAddress(msgContext);

        Assert.assertEquals("172.192.2.1", address);
    }

    public void testForwardedForRemoteAddressNegative() {

        Map<String, String> map = new HashMap<String, String>();

        map.put(AuthenticationUtil.HEADER_X_ORIGINATING_IP_FORM_2, "zdfsdfdfsd , 172.192.2.0, 172.192.5.1, 172.193.2.1");

        MessageContext msgContext = getMessageContext(map);

        try {
            AuthenticationUtil.getRemoteAddress(msgContext);
            Assert.fail("Not a valid originating IP, should fail");
        } catch (AuthenticationException e) {

        }
    }

    public void testRemoteAddress() throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        MessageContext msgContext = getMessageContext(map);

        msgContext.setProperty(MessageContext.REMOTE_ADDR, "172.192.2.1");

        String address = AuthenticationUtil.getRemoteAddress(msgContext);

        Assert.assertEquals("172.192.2.1", address);
    }

}
