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

package org.apache.ws.security;

import org.apache.ws.security.message.token.BinarySecurity;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.message.token.SignatureConfirmation;
import org.apache.ws.security.message.token.Timestamp;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Werner Dittmann (Werner.Dittmann@t-online.de)
 */
public class WSSecurityEngineResult extends java.util.HashMap {

    /**
     * Tag denoting the cryptographic operation performed
     *
     * The value under this tag is of type java.lang.Integer
     */
    public static final java.lang.String TAG_ACTION =
        "action";

    /**
     * Tag denoting the security principal found, if applicable.
     *
     * The value under this tag is of type java.security.Principal.
     */
    public static final java.lang.String TAG_PRINCIPAL =
        "principal";

    /**
     * Tag denoting the X.509 certificate found, if applicable.
     *
     * The value under this tag is of type java.security.cert.X509Certificate.
     */
    public static final java.lang.String TAG_X509_CERTIFICATE =
        "x509-certificate";

    /**
     * Tag denoting the SAML Assertion found, if applicable.
     *
     * The value under this tag is of type org.opensaml.SAMLAssertion.
     */
    public static final java.lang.String TAG_SAML_ASSERTION =
        "saml-assertion";

    /**
     * Tag denoting the timestamp found, if applicable.
     *
     * The value under this tag is of type
     * org.apache.ws.security.message.token.Timestamp.
     */
    public static final java.lang.String TAG_TIMESTAMP =
        "timestamp";

    /**
     * Tag denoting the wsu:Ids of signed elements, if applicable.
     *
     * The value under this tag is of type java.util.Set, where
     * each element of the set is of type java.lang.String.
     */
    public static final java.lang.String TAG_SIGNED_ELEMENT_IDS =
        "signed-element-ids";

    /**
     * Tag denoting the signature value of a signed element, if applicable.
     *
     * The value under this tag is of type byte[].
     */
    public static final java.lang.String TAG_SIGNATURE_VALUE =
        "signature-value";

    /**
     * Tag denoting the signature confirmation of a signed element,
     * if applicable.
     *
     * The value under this tag is of type
     * org.apache.ws.security.message.token.SignatureConfirmation.
     */
    public static final java.lang.String TAG_SIGNATURE_CONFIRMATION =
        "signature-confirmation";

    /**
     * Tag denoting references to the DOM elements that have been
     * cryptographically protected.
     *
     * The value under this tag is of type java.util.Set, where
     * each element in the set is of type org.w3c.dom.Element.
     */
    public static final java.lang.String TAG_PROTECTED_ELEMENTS =
        "protected-elements";

    /**
     * Tag denoting references to the DOM elements that have been
     * cryptographically protected.
     *
     * The value under this tag is of type SecurityContextToken.
     */
    public static final java.lang.String TAG_SECURITY_CONTEXT_TOKEN =
        "security-context-token";

    /**
     * Tag denoting a reference to the decrypted key
     *
     * The value under this tag is of type byte[].
     */
    public static final java.lang.String TAG_DECRYPTED_KEY =
        "decrypted-key";

    /**
     * Tag denoting references to the encrypted key id.
     *
     * The value under this tag is of type String.
     */
    public static final java.lang.String TAG_ENCRYPTED_KEY_ID =
        "encrypted-key-id";

    /**
     * Tag denoting references to a List of Data ref URIs.
     *
     * The value under this tag is of type List.
     */
    public static final java.lang.String TAG_DATA_REF_URIS =
        "data-ref-uris";

    /**
     * Tag denoting the X.509 certificate chain found, if applicable.
     *
     * The value under this tag is of type java.security.cert.X509Certificate[].
     */
    public static final java.lang.String TAG_X509_CERTIFICATES =
        "x509-certificates";

    /**
     * Tag denoting the X.509 certificate found, if applicable.
     *
     * The value under this tag is of type java.security.cert.X509Certificate.
     */
    public static final java.lang.String TAG_BINARY_SECURITY_TOKEN =
        "binary-security-token";

    /**
     * Tag denoting the encrypted key bytes
     *
     * The value under this tag is a byte array 
     */
    public static final Object TAG_ENCRYPTED_EPHEMERAL_KEY = "encrypted-ephemeral-key-bytes";
    
    /**
     * Tag denoting the encrypted key transport algorithm.
     *
     * The value under this tag is of type String.
     */
    public static final Object TAG_ENCRYPTED_KEY_TRANSPORT_METHOD = "encrypted-key-transport-method";
    
    /**
     * Tag denoting the algorithm that was used to sign the message
     *
     * The value under this tag is of type String.
     */
    public static final Object TAG_SIGNATURE_METHOD = "signature-method";

    /**
     * Tag denoting the algorithm that was used to do canonicalization
     *
     * The value under this tag is of type String.
     */
    public static final Object TAG_CANONICALIZATION_METHOD = "canonicalization-method";

    public WSSecurityEngineResult(
        int act, 
        Object ass
    ) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_SAML_ASSERTION, ass);
    }

    public WSSecurityEngineResult(
        int act, 
        Principal princ,
        X509Certificate certificate, 
        Set elements, 
        byte[] sv
    ) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_PRINCIPAL, princ);
        put(TAG_X509_CERTIFICATE, certificate);
        put(TAG_SIGNED_ELEMENT_IDS, elements);
        put(TAG_SIGNATURE_VALUE, sv);
    }

    public
    WSSecurityEngineResult(
        int act,
        Principal princ,
        X509Certificate certificate,
        Set elements,
        Set protectedElements,
        byte[] sv
    ) {
        this(act, princ, certificate, elements, sv);
        put(TAG_PROTECTED_ELEMENTS, protectedElements);
    }
    public
    WSSecurityEngineResult(
        int act,
        Principal princ,
        X509Certificate certificate,
        Set elements,
        List dataRefs,
        byte[] sv
    ) {
        this(act, princ, certificate, elements, sv);
        put(TAG_DATA_REF_URIS, dataRefs);
    }
    public WSSecurityEngineResult(
        int act, 
        byte[] decryptedKey, 
        byte[] encryptedKeyBytes,
        String encyptedKeyId, 
        List dataRefUris
    ) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_DECRYPTED_KEY, decryptedKey);
        put(TAG_ENCRYPTED_EPHEMERAL_KEY, encryptedKeyBytes);
        put(TAG_ENCRYPTED_KEY_ID, encyptedKeyId);
        put(TAG_DATA_REF_URIS, dataRefUris);
    }
    public WSSecurityEngineResult(
                                  int act, 
                                  byte[] decryptedKey, 
                                  byte[] encryptedKeyBytes,
                                  String encyptedKeyId, 
                                  List dataRefUris,
                                  X509Certificate cert
    ) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_DECRYPTED_KEY, decryptedKey);
        put(TAG_ENCRYPTED_EPHEMERAL_KEY, encryptedKeyBytes);
        put(TAG_ENCRYPTED_KEY_ID, encyptedKeyId);
        put(TAG_DATA_REF_URIS, dataRefUris);
        put(TAG_X509_CERTIFICATE, cert);
    }
    
    public WSSecurityEngineResult(int act, ArrayList dataRefUris) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_DATA_REF_URIS, dataRefUris);
    }
    
    public WSSecurityEngineResult(int act, Timestamp tstamp) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_TIMESTAMP, tstamp);
    }
    
    public WSSecurityEngineResult(int act, SecurityContextToken sct) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_SECURITY_CONTEXT_TOKEN, sct);
    }
    
    public WSSecurityEngineResult(int act, SignatureConfirmation sc) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_SIGNATURE_CONFIRMATION, sc);
    }

    public WSSecurityEngineResult(int act, BinarySecurity token,
            X509Certificate[] certificates) {
        put(TAG_ACTION, new Integer(act));
        put(TAG_BINARY_SECURITY_TOKEN, token);
        put(TAG_X509_CERTIFICATES, certificates);
    }

    /**
     * @return the actions vector. These actions were performed by the the
     *         security engine.
     *
     * @deprecated      use ((java.lang.Integer) #get(#TAG_ACTION)).intValue() 
     *                  instead
     */
    public int getAction() {
        return ((java.lang.Integer) get(TAG_ACTION)).intValue();
    }

    /**
     * @return the principals found if UsernameToken or Signature
     *         processing were done
     *
     * @deprecated      use (Principal) #get(#TAG_PRINCIPAL) instead
     */
    public Principal getPrincipal() {
        return (Principal) get(TAG_PRINCIPAL);
    }

    /**
     * @return the Certificate found if Signature
     *         processing were done
     *
     * @deprecated      use (X509Certificate)
     *                  #get(#TAG_X509_CERTIFICATE) instead
     */
    public X509Certificate getCertificate() {
        return (X509Certificate) get(TAG_X509_CERTIFICATE);
    }

    /**
     * @return the timestamp found
     *
     * @deprecated      use (Timestamp)
     *                  #get(#TAG_TIMESTAMP) instead
     */
    public Timestamp getTimestamp() {
        return (Timestamp) get(TAG_TIMESTAMP);
    }

    /**
     * @return Returns the signedElements.
     *
     * @deprecated      use (java.util.Set)
     *                  #get(#TAG_SIGNED_ELEMENT_IDS) instead
     */
    public Set getSignedElements() {
        return (java.util.Set) get(TAG_SIGNED_ELEMENT_IDS);
    }

    /**
     * @return Returns the signatureValue.
     *
     * @deprecated      use (byte[])
     *                  #get(#TAG_SIGNATURE_VALUE) instead
     */
    public byte[] getSignatureValue() {
        return (byte[]) get(TAG_SIGNATURE_VALUE);
    }

    /**
     * @return Returns the sigConf.
     *
     * @deprecated      use (SignatureConfirmation)
     *                  #get(#TAG_SIGNATURE_CONFIRMATION) instead
     */
    public SignatureConfirmation getSigConf() {
        return (SignatureConfirmation) get(TAG_SIGNATURE_CONFIRMATION);
    }

    /**
     * @param signatureValue The signatureValue to set.
     *
     * @deprecated      use put(#TAG_SIGNATURE_VALUE, signatureValue) instead
     */
    public void setSignatureValue(byte[] signatureValue) {
        put(TAG_SIGNATURE_VALUE, signatureValue);
    }

    /**
     * @return          the security context token acquired off the message
     *
     * @deprecated      use
     *                  #get(#TAG_SECURITY_CONTEXT_TOKEN) instead
     */
    public SecurityContextToken getSecurityContextToken() {
        return (SecurityContextToken) get(TAG_SECURITY_CONTEXT_TOKEN);
    }

    /**
     * @return          the decrypted key
     *
     * @deprecated      use
     *                  #get(#TAG_DECRYPTED_KEY) instead
     */
    public byte[] getDecryptedKey() {
        return (byte[]) get(TAG_DECRYPTED_KEY);
    }

    /**
     * @return          the encrypted key id
     *
     * @deprecated      use
     *                  #get(#TAG_ENCRYPTED_KEY) instead
     */
    public String getEncryptedKeyId() {
        return (String) get(TAG_ENCRYPTED_KEY_ID);
    }

    /**
     * @return          the list of data ref URIs
     *
     * @deprecated      use
     *                  #get(#TAG_DATA_REF_URIS) instead
     */
    public ArrayList getDataRefUris() {
        return (ArrayList) get(TAG_DATA_REF_URIS);
    }
    
}
