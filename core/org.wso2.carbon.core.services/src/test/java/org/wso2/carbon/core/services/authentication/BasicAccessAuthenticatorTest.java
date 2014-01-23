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
import org.apache.axis2.transport.http.HTTPConstants;

/**
 * Test class for Basic access authenticator.
 */
public class BasicAccessAuthenticatorTest extends TestCase {


    public void testGetUserName() {
        BasicAccessAuthenticator basicAccessAuthenticator = new BasicAccessAuthenticator();

        // user name "Aladdin", password "open sesame"
        // reference  http://en.wikipedia.org/wiki/Basic_access_authentication
        String authorizationHeader = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";

        MessageContext messageContext = getMessageContext(authorizationHeader);

        String userName = basicAccessAuthenticator.getUserNameFromRequest(messageContext);
        Assert.assertEquals("Aladdin", userName);

        String password = (String)messageContext.getProperty("CARBON_BASIC_AUTH_PASSWORD");
        Assert.assertEquals("open sesame", password);
    }

    private MessageContext getMessageContext(String authHeader) {
        MessageContext messageContext = new MessageContext();
        TestServletRequest testServletRequest = new TestServletRequest(authHeader);

        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, testServletRequest);
        return messageContext;
    }

    public void testCanHandle() {

        BasicAccessAuthenticator basicAccessAuthenticator = new BasicAccessAuthenticator();

        // user name "Aladdin", password "open sesame"
        // reference  http://en.wikipedia.org/wiki/Basic_access_authentication
        String authorizationHeader = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";

        MessageContext messageContext = getMessageContext(authorizationHeader);

        boolean  b = basicAccessAuthenticator.canHandle(messageContext);

        Assert.assertTrue(b);
    }

    public void testCanHandleWOHeader() {

        BasicAccessAuthenticator basicAccessAuthenticator = new BasicAccessAuthenticator();
        MessageContext messageContext = getMessageContext(null);

        boolean  b = basicAccessAuthenticator.canHandle(messageContext);

        Assert.assertTrue(b);
    }

    public void testCanHandleWOBasic() {

        BasicAccessAuthenticator basicAccessAuthenticator = new BasicAccessAuthenticator();

        String authorizationHeader = "Digest QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
        MessageContext messageContext = getMessageContext(authorizationHeader);

        boolean  b = basicAccessAuthenticator.canHandle(messageContext);

        Assert.assertFalse(b);
    }
}
