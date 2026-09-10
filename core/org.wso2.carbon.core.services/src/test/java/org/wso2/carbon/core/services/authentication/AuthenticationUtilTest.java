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

    public void testIPv6RemoteAddress() throws Exception {

        Map<String, String> map = new HashMap<String, String>();

        MessageContext msgContext = getMessageContext(map);

        msgContext.setProperty(MessageContext.REMOTE_ADDR, "fd00:0:0:0:0:0:0:3");

        String address = AuthenticationUtil.getRemoteAddress(msgContext);

        Assert.assertEquals("fd00:0:0:0:0:0:0:3", address);
    }

    public void testValidateIPv4Address() throws Exception {

        AuthenticationUtil.validateRemoteAddress("192.168.1.10");
        AuthenticationUtil.validateRemoteAddress("127.0.0.1");
    }

    public void testValidateIPv6Address() throws Exception {

        // Fully expanded, as InetAddress hands it over from the socket.
        AuthenticationUtil.validateRemoteAddress("fd00:0:0:0:0:0:0:3");
        // Compressed.
        AuthenticationUtil.validateRemoteAddress("fd00::3");
        // Loopback.
        AuthenticationUtil.validateRemoteAddress("::1");
        // IPv4 mapped.
        AuthenticationUtil.validateRemoteAddress("::ffff:192.168.1.1");
        // Link local carrying a zone index.
        AuthenticationUtil.validateRemoteAddress("fe80::1%eth0");
        AuthenticationUtil.validateRemoteAddress("fe80:0:0:0:67:3a41:aeea:d8b7%14");
    }

    public void testValidateBracketedAddress() throws Exception {

        // A bracketed literal is not treated as an IPv6 literal, but InetAddress resolves it, so the
        // DNS check accepts it. Recorded here so the behaviour is not changed by accident.
        AuthenticationUtil.validateRemoteAddress("[fd00::3]");
    }

    public void testValidateAddressNegative() {

        try {
            AuthenticationUtil.validateRemoteAddress("fd00::3::4");
            Assert.fail("Not a valid IPv6 address, should fail");
        } catch (AuthenticationException e) {

        }
    }


}
