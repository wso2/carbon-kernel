/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Created by nipuni on 6/7/16.  //todo
 */
public class BaseCipher {

    private static final Logger logger = LoggerFactory.getLogger(BaseCipher.class);
    private static final String DEFAULT_ALGORITHM = "RSA/ECB/NoPadding";
    /* Underlying cipher instance*/
    private Cipher cipher;

    public BaseCipher(CipherInformation cipherInformation, Key key) {
        init(cipherInformation, key);
    }

    private void init(CipherInformation cipherInformation, Key key) {  //todo how to find the public key?

        String algorithm = DEFAULT_ALGORITHM;
        try {
            cipher = Cipher.getInstance(algorithm);
            logger.info(">>>>>>>>>>>>>>>>>>>>cipher algo after creation " + cipher.getAlgorithm());
            if (cipherInformation.getCipherOperationMode() == CipherOperationMode.ENCRYPT) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else if (cipherInformation.getCipherOperationMode() == CipherOperationMode.DECRYPT) {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } else {
                logger.error("Invalid operation mode");
//                throw new SecureVaultException("Invalid mode : " + opMode, logger); //todo
            }

        } catch (NoSuchAlgorithmException e) {
            logger.error("There is no algorithm support for " +
                    "'" + algorithm + "' in the operation mode '" +
                    cipherInformation.getCipherOperationMode() + "'" + e, logger);
        } catch (NoSuchPaddingException e) {
            logger.error("There is no padding scheme  for " +
                    "'" + algorithm + "' in the operation mode '" +
                    cipherInformation.getCipherOperationMode() + "'" + e, logger);
        } catch (InvalidKeyException e) {
            logger.error("Invalid key ", e, logger);
        }
    }

    private byte[] doCipherOperation(byte[] inputStream) {
        byte[] cipherText = null;
        try {
            cipherText = cipher.doFinal(inputStream);
        } catch (IllegalBlockSizeException e) {
            logger.error("...", e);     //todo
        } catch (BadPaddingException e) {
            logger.error(".xx..", e);     //todo
        }
        return cipherText;
    }

    public byte[] encrypt(byte[] plainText) {
        return doCipherOperation(plainText);
    }

    public byte[] decrypt(byte[] cipherText) {
        logger.info(">>>>>>>> inside decrypt method");
        return doCipherOperation(cipherText);
    }
}
