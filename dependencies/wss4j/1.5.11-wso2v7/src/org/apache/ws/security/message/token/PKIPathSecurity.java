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

package org.apache.ws.security.message.token;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;

/**
 * PKIPath Security Token.
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class PKIPathSecurity extends BinarySecurity {
    private static final String type = WSConstants.X509TOKEN_NS + "#X509PKIPathv1";

    /**
     * Constructor.
     *
     * @throws WSSecurityException
     */
    public PKIPathSecurity(Element elem)
        throws WSSecurityException {
        super(elem);
        if (!getValueType().equals(getType())) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN,
                "invalidValueType",
                new Object[]{type, getValueType()}
            );
        }
    }

    /**
     * Constructor.
     */
    public PKIPathSecurity(Document doc) {
        super(doc);
        setValueType(getType());
    }

    /**
     * get the X509Certificate array.
     *
     * @param reverse
     * @param crypto
     * @return array of certificates 
     * @throws WSSecurityException
     */
    public X509Certificate[] getX509Certificates(boolean reverse, Crypto crypto)
        throws WSSecurityException {
        byte[] data = getToken();
        if (data == null) {
            return null;
        }
        return crypto.getX509Certificates(data, reverse);
    }

    /**
     * set the X509Certificate array.
     *
     * @param certs
     * @param reverse
     * @param crypto
     * @throws WSSecurityException
     */
    public void setX509Certificates(
        X509Certificate[] certs,
        boolean reverse,
        Crypto crypto
    ) throws WSSecurityException {
        if (certs == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCert");
        }
        byte[] data = crypto.getCertificateData(reverse, certs);
        setToken(data);
    }

    public static String getType() {
        return type;
    }
}
