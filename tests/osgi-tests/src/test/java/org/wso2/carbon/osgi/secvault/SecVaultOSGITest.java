/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.osgi.secvault;

import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import javax.inject.Inject;

/**
 * This class test basic secure vault functionality. i.e. encrypt, decrypt, resolve.
 *
 * @since 5.2.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SecVaultOSGITest {

    @Inject
    private SecureVault secureVaultService;

    @Test
    public void testSecureVaultService() throws SecureVaultException {
        String toBeEncrypted = "testPassword";
        byte[] encryptedPassword = secureVaultService.encrypt(toBeEncrypted.getBytes());
        byte[] decryptedPassword = secureVaultService.decrypt(encryptedPassword);
        Assert.assertEquals(new String(decryptedPassword), toBeEncrypted,
                "Decrypted password should be matched with the original password.");
        Assert.assertEquals(String.valueOf(secureVaultService.resolve("wso2.sample.password2")), "ABC@1234");
    }

}
