/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.util;

import org.testng.annotations.Test;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.*;

class KeyStoreUtilTest {

    @Test
    public void testIsCustomKeyStore() {

        assertTrue(KeyStoreUtil.isCustomKeyStore("CUSTOM/myKeyStore"));
        assertTrue(KeyStoreUtil.isCustomKeyStore("CUSTOM/@#$_keyStore"));
        assertFalse(KeyStoreUtil.isCustomKeyStore("abcd"));
        assertFalse(KeyStoreUtil.isCustomKeyStore(""));
        assertFalse(KeyStoreUtil.isCustomKeyStore(" "));
    }

    @Test
    public void testGetQNameWithCarbonNS() {

        QName qName1 = KeyStoreUtil.getQNameWithCarbonNS("localPart");
        assertEquals(ServerConstants.CARBON_SERVER_XML_NAMESPACE, qName1.getNamespaceURI());
        assertEquals("localPart", qName1.getLocalPart());

        QName qName2 = KeyStoreUtil.getQNameWithCarbonNS("");
        assertEquals(ServerConstants.CARBON_SERVER_XML_NAMESPACE, qName2.getNamespaceURI());
        assertEquals("", qName2.getLocalPart());
    }

    @Test
    public void testValidateKeyStoreConfigName() {

        assertThrows(CarbonException.class, () -> KeyStoreUtil.validateKeyStoreConfigName(""));
        assertThrows(CarbonException.class, () -> KeyStoreUtil.validateKeyStoreConfigName("ABC"));
        assertThrows(CarbonException.class, () -> KeyStoreUtil.validateKeyStoreConfigName(" "));

        try {
            KeyStoreUtil.validateKeyStoreConfigName("Location");
            KeyStoreUtil.validateKeyStoreConfigName("Type");
            KeyStoreUtil.validateKeyStoreConfigName("Password");
            KeyStoreUtil.validateKeyStoreConfigName("KeyAlias");
            KeyStoreUtil.validateKeyStoreConfigName("KeyPassword");
        } catch (CarbonException e) {
            fail();
        }
    }
}
