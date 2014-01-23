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

import javax.xml.namespace.QName;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.EncryptionConstants;

/**
 * Constants in WS-Security spec.
 */
public class WSConstants {
    /*
     * All the various string and keywords required.
     * 
     * At first the WSS namespaces as per WSS specifications
     */
    public static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WSSE11_NS = "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd";
    public static final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    /*
     * The base UIRs for the various profiles.
     */
    public static final String SOAPMESSAGE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0";
    public static final String SOAPMESSAGE_NS11 = "http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1";
    public static final String USERNAMETOKEN_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0";
    public static final String X509TOKEN_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0";
    public static final String SAMLTOKEN_NS = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0";
    
    public static final String SAML2_ASSERTION_ID = "SAMLID";
    public static final String WSS_SAML2_NS = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#";
    public static final String WSS_SAML2_KI_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID";
    
    /*
     * The Element name (local name) of the security header
     */
    public static final String WSSE_LN = "Security";

    /*
     * The Thumbprint relative URI string (without #) Combine it with SOAPMESSAGE_NS11, #, to get
     * the full URL
     */
    public static final String THUMBPRINT = "ThumbprintSHA1";

    /*
     * The SAMLAssertionID relative URI string (without #)
     */
    public static final String SAML_ASSERTION_ID = "SAMLAssertionID";

    /*
     * The EncryptedKeyToken value type URI used in wsse:Reference
     */
    public static final String ENC_KEY_VALUE_TYPE = "EncryptedKey";

    /*
     * The relative URI to be used for encrypted key SHA1 (Without #) Combine it with
     * SOAPMESSAGE_NS11, #, to get the full URL
     */
    public static final String ENC_KEY_SHA1_URI = "EncryptedKeySHA1";

    /*
     * The namespace prefixes used. We uses the same prefix convention as shown in the
     * specifications
     */
    public static final String WSSE_PREFIX = "wsse";
    public static final String WSSE11_PREFIX = "wsse11";
    public static final String WSU_PREFIX = "wsu";
    public static final String DEFAULT_SOAP_PREFIX = "soapenv";
    public static final String SAML2_PREFIX = "saml";

    /*
     * Now the namespaces, local names, and prefixes of XML-SIG and XML-ENC
     */
    public static final String SIG_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String SIG_PREFIX = "ds";
    public static final String SIG_LN = "Signature";
    public static final String ENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    public static final String ENC_PREFIX = "xenc";
    public static final String ENC_KEY_LN = "EncryptedKey";
    public static final String ENC_DATA_LN = "EncryptedData";
    public static final String REF_LIST_LN = "ReferenceList";
    public final static String EX_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";

    /*
     * The standard namespace definitions
     */
    public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    /*
     * The local names and attribute names used by WSS
     */
    public static final String USERNAME_TOKEN_LN = "UsernameToken";
    public static final String BINARY_TOKEN_LN = "BinarySecurityToken";
    public static final String TIMESTAMP_TOKEN_LN = "Timestamp";
    public static final String USERNAME_LN = "Username";
    public static final String PASSWORD_LN = "Password";
    public static final String PASSWORD_TYPE_ATTR = "Type";
    public static final String NONCE_LN = "Nonce";
    public static final String CREATED_LN = "Created";
    public static final String EXPIRES_LN = "Expires";
    public static final String SIGNATURE_CONFIRMATION_LN = "SignatureConfirmation";
    public static final String SALT_LN = "Salt";
    public static final String ITERATION_LN = "Iteration";

    /*
     * The definitions for SAML
     */
    public static final String SAML_NS = "urn:oasis:names:tc:SAML:1.0:assertion";
    public static final String SAML2_NS = "urn:oasis:names:tc:SAML:2.0:assertion";
    public static final String SAMLP_NS = "urn:oasis:names:tc:SAML:1.0:protocol";
    public static final String ASSERTION_LN = "Assertion";
    public static final String WSS_SAML_NS = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#";
    public static final String WSS_SAML_ASSERTION = "SAMLAssertion-1.1";
    public static final String WSS_SAML_KI_VALUE_TYPE = WSS_SAML_NS + SAML_ASSERTION_ID;
    public static final String SAML_CONDITION = "Conditions";
    public static final String SAML_NOT_BEFORE = "NotBefore";
    public static final String SAML_NOT_AFTER = "NotOnOrAfter";

    //
    // SOAP-ENV Namespaces
    //
    public static final String URI_SOAP11_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String URI_SOAP12_ENV = "http://www.w3.org/2003/05/soap-envelope";

    public static final String[] URIS_SOAP_ENV = { URI_SOAP11_ENV, URI_SOAP12_ENV, };

    // Misc SOAP Namespaces / URIs
    public static final String URI_SOAP11_NEXT_ACTOR = "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String URI_SOAP12_NEXT_ROLE = "http://www.w3.org/2003/05/soap-envelope/role/next";
    public static final String URI_SOAP12_NONE_ROLE = "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String URI_SOAP12_ULTIMATE_ROLE = "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";

    public static final String ELEM_ENVELOPE = "Envelope";
    public static final String ELEM_HEADER = "Header";
    public static final String ELEM_BODY = "Body";

    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand";
    public static final String ATTR_ACTOR = "actor";
    public static final String ATTR_ROLE = "role";

    public static final String NULL_NS = "Null";
    /**
     * Sets the
     * {@link org.apache.ws.security.message.WSSAddUsernameToken#build(Document, String, String)
     * UserNameToken} method to use a password digest to send the password information
     * <p/>
     * This is a required method as defined by WS Specification, Username token profile.
     */
    public static final String PW_DIGEST = "PasswordDigest";
    /*
     * The password type URI used in the username token
     */
    public static final String PASSWORD_DIGEST = USERNAMETOKEN_NS + "#PasswordDigest";

    /**
     * Sets the
     * {@link org.apache.ws.security.message.WSSAddUsernameToken#build(Document, String, String)
     * UserNameToken} method to send the password in clear
     * <p/>
     * This is a required method as defined by WS Specification, Username token profile.
     */
    public static final String PW_TEXT = "PasswordText";
    /*
     * The password type URI used in the username token
     */
    public static final String PASSWORD_TEXT = USERNAMETOKEN_NS + "#PasswordText";

    /**
     * Sets the
     * {@link org.apache.ws.security.message.WSSAddUsernameToken#build(Document, String, String)
     * UserNameToken} method to send _no_ password related information.
     * <p/>
     * This is a required method as defined by WS Specification, Username token profile as passwords
     * are optional. Also see the WS-I documentation for scenario's using this feature in a trust
     * environment.
     */
    public static final String PW_NONE = "PasswordNone";

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to encrypt the symmetric data encryption key with the RSA algorithm.
     * <p/>
     * This is a required method as defined by XML encryption.
     */
    public static final String KEYTRANSPORT_RSA15 = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to encrypt the symmetric data encryption key with the RSA algorithm.
     * <p/>
     * This is a required method as defined by XML encryption.
     * <p/>
     * NOTE: This algorithm is not yet supported by WSS4J
     */
    public static final String KEYTRANSPORT_RSAOEP = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to use triple DES as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption. The String to use in WSDD file (in
     * accordance to w3c specifications: <br/>
     * http://www.w3.org/2001/04/xmlenc#tripledes-cbc
     */
    public static final String TRIPLE_DES = EncryptionConstants.ALGO_ID_BLOCKCIPHER_TRIPLEDES;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to use AES with 128 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption. The String to use in WSDD file (in
     * accordance to w3c specifications: <br/>
     * http://www.w3.org/2001/04/xmlenc#aes128-cbc
     */
    public static final String AES_128 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to use AES with 256 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a required method as defined by XML encryption. The String to use in WSDD file (in
     * accordance to w3c specifications: <br/>
     * http://www.w3.org/2001/04/xmlenc#aes256-cbc
     */
    public static final String AES_256 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to use AES with 192 bit key as the symmetric algorithm to encrypt data.
     * <p/>
     * This is a optional method as defined by XML encryption. The String to use in WSDD file (in
     * accordance to w3c specifications: <br/>
     * http://www.w3.org/2001/04/xmlenc#aes192-cbc
     */
    public static final String AES_192 = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192;

    /**
     * Sets the {@link org.apache.ws.security.message.WSSignEnvelope#build(Document, Crypto)
     * signature} method to use DSA with SHA1 (DSS) to sign data.
     * <p/>
     * This is a required method as defined by XML signature.
     */
    public static final String DSA = XMLSignature.ALGO_ID_SIGNATURE_DSA;

    /**
     * Sets the {@link org.apache.ws.security.message.WSSignEnvelope#build(Document, Crypto)
     * signature} method to use RSA with SHA to sign data.
     * <p/>
     * This is a recommended method as defined by XML signature.
     */
    public static final String RSA = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;

    public static final String C14N_OMIT_COMMENTS = Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    public static final String C14N_WITH_COMMENTS = Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS;
    public static final String C14N_EXCL_OMIT_COMMENTS = Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
    public static final String C14N_EXCL_WITH_COMMENTS = Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS;

    /**
     * 
     */
    public static final int KERBEROS = 0x1200; // KerberosToken
    public static final int KERBEROS_SIGN = 0x1400; // Perform Signature with KerberosToken
    public static final int KERBEROS_ENCR = 0x1600; // Perform Encryption with KerberosToken

    /**
     * Sets the {@link org.apache.ws.security.message.WSSignEnvelope#build(Document, Crypto)
     * signing} method to send the signing certificate as a <code>BinarySecurityToken</code>.
     * <p/>
     * The signing method takes the signing certificate, converts it to a
     * <code>BinarySecurityToken</code>, puts it in the security header, and inserts a
     * <code>Reference</code> to the binary security token into the
     * <code>wsse:SecurityReferenceToken</code>. Thus the whole signing certificate is transfered to
     * the receiver. The X509 profile recommends to use {@link #ISSUER_SERIAL} instead of sending
     * the whole certificate.
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 3.3.2 and to WS Security
     * specification, chapter 7.2
     * <p/>
     * Note: only local references to BinarySecurityToken are supported
     */
    public static final int BST_DIRECT_REFERENCE = 1;

    /**
     * Sets the {@link org.apache.ws.security.message.WSSignEnvelope#build(Document, Crypto)
     * signing} or the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to send the issuer name and the serial number of a certificate to the
     * receiver.
     * <p/>
     * In contrast to {@link #BST_DIRECT_REFERENCE} only the issuer name and the serial number of
     * the signing certificate are sent to the receiver. This reduces the amount of data being sent.
     * The encryption method uses the public key associated with this certificate to encrypt the
     * symmetric key used to encrypt data.
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 3.3.3
     */
    public static final int ISSUER_SERIAL = 2;

    /**
     * Sets the {@link org.apache.ws.security.message.WSEncryptBody#build(Document, Crypto)
     * encryption} method to send the certificate used to encrypt the symmetric key.
     * <p/>
     * The encryption method uses the public key associated with this certificate to encrypr the
     * symmetric key used to encrypt data. The certificate is converted into a
     * <code>KeyIdentfier</code> token and sent to the receiver. Thus the complete certificate data
     * is transfered to receiver. The X509 profile recommends to use {@link #ISSUER_SERIAL} instead
     * of sending the whole certificate.
     * <p/>
     * <p/>
     * Please refer to WS Security specification X509 profile, chapter 7.3
     */
    public static final int X509_KEY_IDENTIFIER = 3;
    /**
     * Sets the {@link org.apache.ws.security.message.WSSignEnvelope#build(Document, Crypto)
     * signing} method to send a <code>SubjectKeyIdentifier</code> to identify the signing
     * certificate.
     * <p/>
     * Refer to WS Security specification X509 profile, chapter 3.3.1 This identification token is
     * not yet fully tested by WSS4J. The WsDoAllSender does not include the X.509 certificate as
     * <code>BinarySecurityToken</code> in the request message.
     */
    public static final int SKI_KEY_IDENTIFIER = 4;

    /**
     * Embeds a keyinfo/key name into the EncryptedData element.
     * <p/>
     * Refer to WS Security specification X509 profile
     */
    public static final int EMBEDDED_KEYNAME = 5;
    /**
     * Embeds a keyinfo/wsse:SecurityTokenReference into EncryptedData element.
     */
    public static final int EMBED_SECURITY_TOKEN_REF = 6;

    /**
     * <code>UT_SIGNING</code> is used internally only to set a specific Signature behavior.
     * 
     * The signing token is constructed from values in the UsernameToken according to WS-Trust
     * specification.
     */
    public static final int UT_SIGNING = 7;

    /**
     * <code>THUMPRINT_IDENTIFIER</code> is used to set the specific key identifier ThumbprintSHA1.
     * 
     * This identifier uses the SHA-1 digest of a security token to identify the security token.
     * Please refer to chapter 7.2 of the OASIS WSS 1.1 specification.
     * 
     */
    public static final int THUMBPRINT_IDENTIFIER = 8;

    /**
     * <code>CUSTOM_SYMM_SIGNING</code> is used internally only to set a specific Signature
     * behavior.
     * 
     * The signing key, reference id and value type are set externally.
     */
    public static final int CUSTOM_SYMM_SIGNING = 9;

    /**
     * <code>ENCRYPTED_KEY_SHA1_IDENTIFIER</code> is used to set the specific key identifier
     * ThumbprintSHA1.
     * 
     * This identifier uses the SHA-1 digest of a security token to identify the security token.
     * Please refer to chapter 7.3 of the OASIS WSS 1.1 specification.
     * 
     */
    public static final int ENCRYPTED_KEY_SHA1_IDENTIFIER = 10;
    
    public static final int SAML_ASSERTION_IDENTIFIER = 15;

    /**
     * <code>CUSTOM_SYMM_SIGNING_DIRECT</code> is used internally only to set a specific Signature
     * behavior.
     * 
     * The signing key, reference id and value type are set externally.
     */
    public static final int CUSTOM_SYMM_SIGNING_DIRECT = 11;

    /**
     * <code>CUSTOM_KEY_IDENTIFIER</code> is used to set a KeyIdentifier to a particular ID
     * 
     * The reference id and value type are set externally.
     */
    public static final int CUSTOM_KEY_IDENTIFIER = 12;

    /**
     * <code>KEY_VALUE</code> is used to set a ds:KeyInfo/ds:KeyValue element to refer to either an
     * RSA or DSA public key.
     */
    public static final int KEY_VALUE = 13;
    
    public static final int KERBEROS_KEY_IDENTIFIER = 14;

    public static final String ENCRYPTED_HEADER = "EncryptedHeader";

    /*
     * The following values are bits that can be combined to for a set. Be careful when selecting
     * new values.
     */
    public static final int NO_SECURITY = 0;
    public static final int UT = 0x1; // perform UsernameToken
    public static final int SIGN = 0x2; // Perform Signature
    public static final int ENCR = 0x4; // Perform Encryption

    /*
     * Attention: the signed/Unsigned types identify if WSS4J uses the SAML token for signature,
     * signature key or not. It does not mean if the token contains an enveloped signature.
     */
    public static final int ST_UNSIGNED = 0x8; // perform SAMLToken unsigned
    public static final int ST_SIGNED = 0x10; // perform SAMLToken signed

    public static final int TS = 0x20; // insert Timestamp
    public static final int UT_SIGN = 0x40; // perform signature with UT secret key
    public static final int SC = 0x80; // this is a SignatureConfirmation

    public static final int NO_SERIALIZE = 0x100;
    public static final int SERIALIZE = 0x200;
    public static final int SCT = 0x400; // SecurityContextToken
    public static final int DKT = 0x800; // DerivedKeyToken
    public static final int BST = 0x1000; // BinarySecurityToken

    /**
     * Length of UsernameToken derived key used by .NET WSE to sign a message.
     */
    public static final int WSE_DERIVED_KEY_LEN = 16;
    public static final String LABEL_FOR_DERIVED_KEY = "WS-Security";

    /**
     * To validate the timestamp of the SAML assertion
     */
    public static final int SAML_TIMESTAMP = 17;

    /**
     * IssuerName of the token. Used to validate <wsp:IssuerName/> assertion
     */
    public static final String SAML_ISSUER_NAME = "Issuer";

    /**
     * Set of claims included in a SAML token as attributes.
     */
    public static final String SAML_CLAIM_SET = "Claims";

    /**
     * Version of the SAML token.
     */
    public static final String SAML_VERSION = "samlVersion";

    /**
     * SAML Assertion was signed or not
     */
    public static final String SAML_TOKEN_SIGNED = "samlTokenSigned";

    /**
     * WS-Trust namespace
     */
    public static final String WST_NS = "http://schemas.xmlsoap.org/ws/2005/02/trust";

    public final static String WSC_SCT = "http://schemas.xmlsoap.org/ws/2005/02/sc/sct";

    //
    // Fault codes defined in the WSS 1.1 spec under section 12, Error handling
    //

    /**
     * An unsupported token was provided
     */
    public static final QName UNSUPPORTED_SECURITY_TOKEN = new QName(WSSE_NS,
            "UnsupportedSecurityToken");

    /**
     * An unsupported signature or encryption algorithm was used
     */
    public static final QName UNSUPPORTED_ALGORITHM = new QName(WSSE_NS, "UnsupportedAlgorithm");

    /**
     * An error was discovered processing the <Security> header
     */
    public static final QName INVALID_SECURITY = new QName(WSSE_NS, "InvalidSecurity");

    /**
     * An invalid security token was provided
     */
    public static final QName INVALID_SECURITY_TOKEN = new QName(WSSE_NS, "InvalidSecurityToken");

    /**
     * The security token could not be authenticated or authorized
     */
    public static final QName FAILED_AUTHENTICATION = new QName(WSSE_NS, "FailedAuthentication");

    /**
     * The signature or decryption was invalid
     */
    public static final QName FAILED_CHECK = new QName(WSSE_NS, "FailedCheck");

    /**
     * Referenced security token could not be retrieved
     */
    public static final QName SECURITY_TOKEN_UNAVAILABLE = new QName(WSSE_NS,
            "SecurityTokenUnavailable");

    /**
     * The message has expired
     */
    public static final QName MESSAGE_EXPIRED = new QName(WSSE_NS, "MessageExpired");

    /**
     * Header type in <code>org.apache.ws.security.WSEncryptionPart</code>
     */
    public static final int PART_TYPE_HEADER = 1;

    /**
     * Body type in <code>org.apache.ws.security.WSEncryptionPart</code>
     */
    public static final int PART_TYPE_BODY = 2;

    /**
     * Element type in <code>org.apache.ws.security.WSEncryptionPart</code>
     */
    public static final int PART_TYPE_ELEMENT = 3;

}
