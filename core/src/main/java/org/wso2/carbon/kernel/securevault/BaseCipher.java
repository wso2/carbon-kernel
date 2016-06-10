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
    private static final String DEFAULT_ALGORITHM = "RSA";
    /* Underlying cipher instance*/
    private Cipher cipher;

    public BaseCipher(CipherInformation cipherInformation, Key key) {
        init(cipherInformation, key);
    }

    private void init(CipherInformation cipherInformation, Key key) {  //todo how to find the public key?

        String algorithm = DEFAULT_ALGORITHM;
        try {
            cipher = Cipher.getInstance(algorithm);
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

    public byte[] decrypt(byte[] inputStream) {
        return doCipherOperation(inputStream);
    }

//    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
//        // string initialize 2 buffers.
//        // scrambled will hold intermediate results
//        byte[] scrambled;
//
//        // toReturn will hold the total result
//        byte[] toReturn = new byte[0];
//        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
//        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;
//        logger.info("length is : " + length);
//        // another buffer. this one will hold the bytes that have to be modified in this step
//        byte[] buffer = new byte[length];
//
//        for (int i = 0; i < bytes.length; i++) {
//            // if we filled our buffer array we have our block ready for de- or encryption
//            if ((i > 0) && (i % length == 0)) {
//                logger.info(">>>>>>>> inside the if block: buffer :" + bytes[i]);
//                //execute the operation
//                scrambled = cipher.doFinal(buffer);
//                // add the result to our total result.
//                logger.info(">>>>>>>> scrambled buffer : " + scrambled.length);
//                toReturn = append(toReturn, scrambled);
//                // here we calculate the length of the next buffer required                           ]
//                int newlength = length;
//                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
//                if (i + length > bytes.length) {
//                    newlength = bytes.length - i;
//                }
//                logger.info(">>>>>>>> outside the for loop: new length :" + newlength);
//                // clean the buffer array
//                buffer = new byte[newlength];
//            }
//            // copy byte into our buffer.
//            buffer[i % length] = bytes[i];
//        }
//
//        // this step is needed if we had a trailing buffer. should only happen when encrypting.
//        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes.
//        // they are in the buffer array
//        scrambled = cipher.doFinal(buffer);
//
//        // final step before we can return the modified data.
//        toReturn = append(toReturn, scrambled);
//        logger.info(">>>>>>>> returned buffer : " + toReturn.length);
//        return toReturn;
//    }

//    private byte[] append(byte[] prefix, byte[] suffix) {
//        byte[] toReturn = new byte[prefix.length + suffix.length];
//        for (int i = 0; i < prefix.length; i++) {
//            toReturn[i] = prefix[i];
//        }
//        for (int i = 0; i < suffix.length; i++) {
//            toReturn[i + prefix.length] = suffix[i];
//        }
//        return toReturn;
//    }
}
