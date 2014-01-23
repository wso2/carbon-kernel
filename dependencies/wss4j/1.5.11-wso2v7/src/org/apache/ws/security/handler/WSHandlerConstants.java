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

package org.apache.ws.security.handler;

import org.apache.ws.security.WSConstants;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class defines the names, actions, and other string for the deployment
 * data of the WSS handler. Both the Axis handler as well as the JAX-RPC handler
 * use this class.
 *  
 * @author Werner Dittmann (werner@apache.org)
 */

public class WSHandlerConstants {
    /**
     * The action parameter. The
     * handlers use the value of this parameter to determine how
     * to process the SOAP Envelope. For example in a Axis WSDD file:
     * <pre>
     * &lt;handler type="java:org.apache.ws.axis.security.WSDoAllSender">
     * &lt;parameter name="action" value="UsernameToken"/>
     * ...
     * </pre>
     * orders the handler to attach a <code>UsernameToken</code> to the SOAP
     * enevelope. It is a blank separated list of actions to perform.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting)
     */
    public static final String ACTION = "action";
    public static final String SEND = "send";
    public static final String RECEIVE = "receive";

    /**
     * Perform nothing.
     */
    public static final String NO_SECURITY = "NoSecurity";

    /**
     * Perform a UsernameToken identification only.
     */
    public static final String USERNAME_TOKEN = "UsernameToken";

    /**
     * Perform a SAML Token identification.
     */
    public static final String SAML_TOKEN_UNSIGNED = "SAMLTokenUnsigned";
    public static final String SAML_TOKEN_SIGNED = "SAMLTokenSigned";

    /**
     * Perform Signature.
     * The signature specific parameters define how to sign, which keys
     * to use, and so on
     */
    public static final String SIGNATURE = "Signature";

    /**
     * Perform Encryption.
     * The encryption specific parameters define how to encrypt, which keys
     * to use, and so on.
     */
    public static final String ENCRYPT = "Encrypt";

    /**
     * Add a timestamp to the security header.
     */
    public static final String TIMESTAMP = "Timestamp";

    /**
     * Suppress the serialization of the SOAP message.
     * <p/>
     * Usually the handler serializes the processed SOAP message into a string
     * and sets it into the Axis message context as new current message. To
     * suppress this action, define this action. In this case the handler
     * stores the processed SOAP message as <code>Document</code> in the
     * Axis message context with the property name <code>SND_SECURITY</code>.
     * <p/>
     * A chained handler can retrieve the SOAP message and process it. The
     * last handler in the chain must set the processed SOAP message as
     * current message in Axis message context.
     */
    public static final String NO_SERIALIZATION = "NoSerialization";

    /**
     * Use this to use a specific signature mechanism for .Net.
     * This signature mechanism uses data from the username token and
     * a well defined constant string and constructs a signature
     * key. 
     */
    public static final String SIGN_WITH_UT_KEY = "UsernameTokenSignature";

    /**
     * This is an interal property name to support handler chaining.
     * The Axis WSS4J handlers use this message context property to
     * hand over the SOAP partially processed envelope document to
     * the next WSS4J handler in the chain.
     */
    public static final String SND_SECURITY = "SND_SECURITY";

    /**
     * The actor name of the <code>wsse:Security</code> header.
     * <p/>
     * If this parameter is omitted, the actor name is not set. Please
     * refer to {@link org.apache.axis.Constants#ATTR_ACTOR} and {@link org.apache.axis.Constants#ATTR_ROLE}
     * about the parameter names. They are set to <code>"actor"</code>
     * and <code>"role"</code> respectively.
     * <p/>
     * The value of the actor or role has to match the receiver's setting
     * or may contain standard values.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ACTOR, "ActorName");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     *
     * @see org.apache.axis.Constants#URI_SOAP11_NEXT_ACTOR
     * @see org.apache.axis.Constants#URI_SOAP12_NEXT_ROLE
     */
    public static final String ACTOR = "actor";

    /**
     * The role name of the <code>wsse:Security</code> header.
     * This is used for SOAP 1.2. Refer also to {@link #ACTOR}.
     */
    public static final String ROLE = "role";

    /**
     * Sets the <code>mustUnderstand</code> flag.
     * <p/>
     * If the parameter has the value <code>1</code>
     * or <code>true</code> the <code>mustUnderstand</code> is set.
     * The values <code>0</code> or <code>false</code> supress the
     * flag.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.MUST_UNDERSTAND, "false");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * The default setting is <code>true</code>
     * <p/>
     * Please refer to {@link org.apache.axis.Constants#ATTR_MUST_UNDERSTAND}
     * about the parameter name (<code>"mustUnderstand"</code>).
     */
    public static final String MUST_UNDERSTAND = "mustUnderstand";

    /**
     * The user's name. It is used differently by the WS Security functions.
     * <ul>
     * <li>The <i>UsernameToken</i> function sets this name in the
     * <code>UsernameToken</code>.
     * </li>
     * <li>The <i>Signing</i> function uses this name as the alias name
     * in the keystore to get user's certificate and private key to
     * perform signing if {@link #SIGNATURE_USER} is not used.
     * </li>
     * <li>The <i>encryption</i>
     * functions uses this parameter as fallback if {@link #ENCRYPTION_USER}
     * is not used.
     * </li>
     * </ul>
     * It is also possible to set the user's name and the according password
     * via the call function, for example:
     * <pre>
     * ...
     * call.setUsername("name");
     * call.setPassword("WSS4Java");
     * ...
     * </pre>
     * The user parameter in the deployment descritor (WSDD) file overwrites
     * the application's setting.
     * </p>
     * For an additional way to set the password refer to
     * {@link #PW_CALLBACK_CLASS} and {@link #PW_CALLBACK_REF}.
     * <p/>
     * If the security functions uses the username from the message context, it
     * clears the username from the message context
     * after they copied it. This prevents sending of the username in the
     * HTTP header.
     * <p/>
     * In this case the HTTP authentication mechansisms do <b>not</b> work
     * anymore. User authentication shall be done via the username token or
     * the certificate verification of the signature certificate.
     */
    public static final String USER = "user";

    /**
     * The Axis WSS4J handlers provide several ways to get the password required
     * to construct a username token or to sign a message.
     * In addition the callback class may check if a username/password
     * combination is valid. Refer to the documentation of 
     * {@link org.apache.ws.security.WSPasswordCallback} for more information
     * about this feature.
     * <ul>
     * <li> A class that implements a callback interface (see below). The
     * handler loads this class and calls the callback method. This
     * class must have a public default constructor with not parameters.
     * </li>
     * <li> The application (or a preceeding handler) sets a reference to an
     * object that implements the callback interface
     * </li>
     * <li> The application sets the password directly using the
     * <code>setPassword</code> function of the <code>Call</code>.
     * </ul>
     * The callback class or callback object shall implement specific password
     * getter methods, for example reading a database or directory.
     * <p/>
     * The handler first checks if it can get a the password via a callback
     * class. If that fails it checks if it can get the password from the
     * object reference, if that also fails the handler tries the password
     * property.
     * <p/>
     * The following parameter defines a class that implements a callback
     * handler interface. The handler loads the class and calls the callback
     * handler method to get the password. The callback
     * class needs to implement the
     * {@link javax.security.auth.callback.CallbackHandler} interface.
     * <p/>
     * The callback function
     * {@link javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])}
     * gets an array of {@link org.apache.ws.security.WSPasswordCallback}
     * objects. Only the first entry of the array is used. This object
     * contains the username/keyname as identifier. The callback handler must
     * set the password or key associated with this identifier before it returns.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.PW_CALLBACK_CLASS, "PWCallbackClass");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * Refer also to comment in {@link #USER} about HTTP authentication
     * functions.
     */
    public static final String PW_CALLBACK_CLASS = "passwordCallbackClass";

    /**
     * An application may set an object reference to an object that implements
     * the {@link javax.security.auth.callback.CallbackHandler} interface.
     * Only the application can set this property using:
     * <pre>
     * call.setProperty(WSHandlerConstants.PW_CALLBACK_REF, anPWCallbackObject);
     * </pre>
     * Refer to {@link #PW_CALLBACK_CLASS} for further information about
     * password callback handling and the priority of the different
     * methods.
     * <p/>
     * Note: every handler that preceeds this handler in the chain can set
     * this property too. This may be useful on the server side.
     */
    public static final String PW_CALLBACK_REF = "passwordCallbackRef";

    /**
     * The user's name for encryption.
     * <p/>
     * The encryption functions uses the public key of this user's certificate
     * to encrypt the generated symmetric key.
     * <p/>
     * If this parameter is not set, then the encryption
     * function falls back to the {@link #USER} parameter to get the
     * certificate.
     * <p/>
     * If <b>only</b> encryption of the SOAP body data is requested,
     * it is recommended to use this parameter to define the username.
     * The application can then use the standard user and password
     * functions (see example at {@link #USER} to enable HTTP authentication
     * functions.
     * <p/>
     * Encryption only does not authenticate a user / sender, therefore it
     * does not need a password.
     * <p/>
     * Placing the username of the encryption certificate in the WSDD is not
     * a security risk, because the public key of that certificate is used
     * only.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENCYRPTION_USER, "encryptionUser");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String ENCRYPTION_USER = "encryptionUser";
    
    /**
     * The user's name for signature.
     * <p/>
     * This name is used as the alias name in the keystore to get user's
     * certificate and private key to perform signing.
     * <p/>
     * If this parameter is not set, then the signature
     * function falls back to the {@link #USER} parameter.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.SIGNATURE_USER, "signatureUser");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String SIGNATURE_USER = "signatureUser";

    /**
     * Specifying this name as {@link #ENCRYPTION_USER}
     * triggers a special action to get the public key to use for encryption.
     * <p/>
     * The handler uses the public key of the sender's certificate. Using this
     * way to define an encryption key simplifies certificate management to
     * a large extend.
     */
    public static final String USE_REQ_SIG_CERT = "useReqSigCert";

    /**
     * This parameter works in the same way as {@link #PW_CALLBACK_CLASS} but
     * the Axis WSS4J handler uses it to get the key associated with a key name.
     */
    public static final String ENC_CALLBACK_CLASS = "EmbeddedKeyCallbackClass";

    /**
     * This parameter works in the same way as {@link #PW_CALLBACK_REF} but
     * the Axis WSS4J handler uses it to get the key associated with a key name.
     */
    public static final String ENC_CALLBACK_REF = "EmbeddedKeyCallbackRef";

    /**
     * Text of the key name that needs to be sent
     */
    public static final String ENC_KEY_NAME = "EmbeddedKeyName";

    /**
     * The name of the crypto property file to use for SOAP Signature.
     * <p/>
     * The classloader loads this file. Therefore it must be accessible
     * via the classpath.
     * <p/>
     * To locate the implementation of the
     * {@link org.apache.ws.security.components.crypto.Crypto Crypto}
     * interface implementation the property file must contain the property
     * <code>org.apache.ws.security.crypto.provider</code>. The value of
     * this property is the classname of the implementation class.
     * <p/>
     * The following line defines the standard implementation:
     * <pre>
     * org.apache.ws.security.crypto.provider=org.apache.ws.security.components.crypto.Merlin
     * </pre>
     * The other contents of the property file depend on the implementation
     * of the {@link org.apache.ws.security.components.crypto.Crypto Crypto}
     * interface implementation.
     * <p/>
     * The property file of the standard implementation
     * {@link org.apache.ws.security.components.crypto.Merlin} uses
     * the following properties:
     * <pre>
     * org.apache.ws.security.crypto.provider
     * org.apache.ws.security.crypto.merlin.file
     * org.apache.ws.security.crypto.merlin.keystore.type
     * org.apache.ws.security.crypto.merlin.keystore.provider
     * org.apache.ws.security.crypto.merlin.keystore.password
     * org.apache.ws.security.crypto.merlin.keystore.alias
     * org.apache.ws.security.crypto.merlin.cert.provider
     * </pre>
     * The entries are:
     * <ul>
     * <li> <code>org.apache.ws.security.crypto.provider</code> see
     * description above
     * </li>
     * <li><code>org.apache.ws.security.crypto.merlin.file</code>
     * The path to the keystore file. At first the classloader tries to load
     * this file, if this fails the implementations performs a file system
     * lookup.
     * </li>
     * <li><code>org.apache.ws.security.crypto.merlin.keystore.type</code>
     * The keystore type, for example <code>JKS</code> for the Java key store.
     * Other keystore type, such as <code>pkcs12</code> are also possible but depend
     * on the actual <code>Crypto</code> implementation.
     * </li>
     * <li><code>org.apache.ws.security.crypto.merlin.keystore.password</code>
     * The password to read the keystore. If this property is not set, then
     * the <code>pwcallback</code>property must be defined.
     * </li>
     * </ul>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.SIG_PROP_FILE, "myCrypto.properties");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * If a property file is not set and a signature is requested,
     * the handler throws an <code>AxisFault</code>.
     */
    public static final String SIG_PROP_FILE = "signaturePropFile";

    /**
     * The key that hold the refernce of the <code>java.util.Properties</code> 
     * object holding complete info about signature Crypto implementation. 
     * This should contain all information that would contain in an equivalent 
     * .properties file which includes the Crypto implementation class name.
     * 
     * Refer to documentation of {@link #SIG_PROP_FILE}.
     */
    public final static String SIG_PROP_REF_ID = "SignaturePropRefId";
    
    /**
     * The WSDoAllReceiver handler stores a result <code>Vector</code>
     * in this property.
     * <p/>
     * The vector contains <code>WSDoAllReceiverResult</code> objects
     * for each chained WSDoAllReceiver handler.
     */
    public static final String RECV_RESULTS = "RECV_RESULTS";

    /**
     * The name of the crypto propterty file to use for SOAP Decryption.
     * <p/>
     * Refer to documentation of {@link #SIG_PROP_FILE}.
     * <p/>
     * Refer to {@link #SIG_PROP_FILE} for a detail description
     * about the format and how to use this property file.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.DEC_PROP_FILE, "myCrypto.properties");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * If this parameter is not used, but the signature crypto property
     * file is defined (combined Encryption/Signature action), then the
     * encryption function uses that file. Otherwise the handler throws
     * an <code>AxisFault</code>.
     */
    public static final String DEC_PROP_FILE = "decryptionPropFile";
    
    /**
     * The key that hold the refernce of the <code>java.util.Properties</code> 
     * object holding complete info about decryption Crypto implementation. This
     * should contain all information that would contain in an equivalent 
     * .properties file which includes the Crypto implementation class name.
     * 
     * Refer to documentation of {@link #DEC_PROP_FILE}.
     */
    public final static String DEC_PROP_REF_ID = "decryptionPropRefId";

    /**
     * Specific parameter for UsernameToken action to define the encoding
     * of the password.
     * <p/>
     * The parameter can be set to either {@link WSConstants#PW_DIGEST}
     * or to {@link WSConstants#PW_TEXT}.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * The default setting is PW_DIGEST.
     */
    public static final String PASSWORD_TYPE = "passwordType";
    
    /**
     * This variable controls whether types other than PasswordDigest or PasswordText
     * are allowed when processing UsernameTokens. 
     * 
     * By default this is set to false so that the user doesn't have to explicitly
     * reject custom token types in the callback handler.
     */
    public static final String HANDLE_CUSTOM_PASSWORD_TYPES = "handleCustomPasswordTypes";
    
    /**
     * This variable controls whether (wsse) namespace qualified password types are
     * accepted when processing UsernameTokens.
     * 
     * By default this is set to false.
     */
    public static final String ALLOW_NAMESPACE_QUALIFIED_PASSWORD_TYPES 
        = "allowNamespaceQualifiedPasswordTypes";

    /**
     * Parameter to generate additional elements in <code>UsernameToken</code>.
     * <p/>
     * The value of this parameter is a list of element names that are added
     * to the UsernameToken. The names of the list a separated by spaces.
     * <p/>
     * The list may containe the names <code>nonce</code> and
     * <code>created</code> only. Use this option if the password type is
     * <code>passwordText</code> and the handler shall add the <code>Nonce</code>
     * and/or <code>Created</code> elements.
     */
    public static final String ADD_UT_ELEMENTS = "addUTElements";

    /**
     * Defines which key identifier type to use. The WS-Security specifications
     * recommends to use the identifier type <code>IssuerSerial</code>. For
     * possible signature key identifier types refer to
     * {@link #keyIdentifier}. For signature <code>IssuerSerial</code>
     * and <code>DirectReference</code> are valid only.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.SIG_KEY_ID, "DirectReference");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String SIG_KEY_ID = "signatureKeyIdentifier";

    /**
     * Defines which signature algorithm to use.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *     WSHandlerConstants.SIG_ALGO, 
     *     "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
     * );
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String SIG_ALGO = "signatureAlgorithm";
    
    /**
     * Defines which signature digest algorithm to use. 
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(
     *    WSHandlerConstants.SIG_DIGEST_ALGO, "http://www.w3.org/2001/04/xmlenc#sha256"
     * );
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String SIG_DIGEST_ALGO = "signatureDigestAlgorithm";

    /**
     * Parameter to define which parts of the request shall be signed.
     * <p/>
     * Refer to {@link #ENCRYPTION_PARTS} for a detailed description of
     * the format of the value string.
     * <p/>
     * If this parameter is not specified the handler signs the SOAP Body
     * by default.
     * <p/>
     * The WS Security specifications define several formats to transfer the
     * signature tokens (certificates) or  references to these tokens.
     * Thus, the plain element name <code>Token</code>
     * signs the token and takes care of the different format.
     * <p/>
     * To sign the SOAP body <b>and</b> the signature token the value of this
     * parameter must contain:
     * <pre>
     * &lt;parameter name="signatureParts"
     *   value="{}{http://schemas.xmlsoap.org/soap/envelope/}Body; Token" />
     * </pre>
     * To specify an element without a namespace use the string
     * <code>Null</code> as the namespace name (this is a case sensitive
     * string)
     * <p/>
     * If there is no other element in the request with a local name of
     * <code>Body</code> then the SOAP namespace identifier can be empty
     * (<code>{}</code>).
     */
    public static final String SIGNATURE_PARTS = "signatureParts";
    
    /**
     * This parameter sets whether to use a single certificate or a whole certificate
     * chain when constructing a BinarySecurityToken used for direct reference in
     * signature. The default is true, meaning that only a single certificate is used.
     */
    public static final String USE_SINGLE_CERTIFICATE = "useSingleCertificate";
    
    /**
     * This parameter sets the length of the secret (derived) key to use for the
     * WSE UT_SIGN functionality.
     * 
     * The default value is 16 bytes.
     */
    public static final String WSE_SECRET_KEY_LENGTH = "wseSecretKeyLength";
    
    /**
     * This parameter sets whether to use UsernameToken Key Derivation, as defined 
     * in the UsernameTokenProfile 1.1 specification. The default is false for
     * backwards compatibility reasons, and defaults to WSE key derivation.
     */
    public static final String USE_DERIVED_KEY = "useDerivedKey";
    
    /**
     * This parameter sets whether to use the Username Token derived key for a MAC
     * or not. The default is true.
     */
    public static final String USE_DERIVED_KEY_FOR_MAC = "useDerivedKeyForMAC";
    
    /**
     * This parameter sets the number of iterations to use when deriving a key
     * from a Username Token. The default is 1000. 
     */
    public static final String DERIVED_KEY_ITERATIONS = "derivedKeyIterations";

    /**
     * The name of the crypto property file to use for SOAP Encryption.
     * <p/>
     * Refer to documentation of {@link #SIG_PROP_FILE}.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENC_PROP_FILE, "myCrypto.properties");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     * <p/>
     * If this parameter is not used, but the signature crypto property
     * file is defined (combined Encryption/Signature action), then the
     * encryption function uses signature property file. Otherwise the
     * handler throws an <code>AxisFault</code>.
     */
    public static final String ENC_PROP_FILE = "encryptionPropFile";
    
    /**
     * The key that hold the refernce of the 
     * <code>java.util.Properties</code> object holding complete info about 
     * encryption Crypto implementation. This should contain all information 
     * that would contain in an equivalent .properties file which includes the 
     * Crypto implementation class name.
     * 
     * Refer to documentation of {@link #DEC_PROP_FILE}.
     */
    public final static String ENC_PROP_REF_ID = "encryptionPropRefId";


    /**
     * Defines which key identifier type to use. The WS-Security specifications
     * recommends to use the identifier type <code>IssuerSerial</code>. For
     * possible encryption key identifier types refer to
     * {@link #keyIdentifier}. For encryption <code>IssuerSerial</code>,
     * <code>X509KeyIdentifier</code>,  <code>DirectReference</code>, 
     * <code>Thumbprint</code>, <code>SKIKeyIdentifier</code>, and
     * <code>EmbeddedKeyName</code> are valid only.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENC_KEY_ID, "X509KeyIdentifier");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String ENC_KEY_ID = "encryptionKeyIdentifier";

    /**
     * Defines which symmetric encryption algorithm to use. WSS4J supports the
     * following alorithms: {@link WSConstants#TRIPLE_DES},
     * {@link WSConstants#AES_128}, {@link WSConstants#AES_256},
     * and {@link WSConstants#AES_192}. Except for AES 192 all of these
     * algorithms are required by the XML Encryption specification.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENC_SYM_ALGO, WSConstants.AES_256);
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String ENC_SYM_ALGO = "encryptionSymAlgorithm";

    /**
     * Defines which algorithm to use to encrypt the generated symmetric key.
     * Currently WSS4J supports {@link WSConstants#KEYTRANSPORT_RSA15} only.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENC_KEY_TRANSPORT, "RSA15");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String ENC_KEY_TRANSPORT =
            "encryptionKeyTransportAlgorithm";
    
    /**
     * Defines whether to encrypt the symmetric encryption key or not. If true
     * (the default), the symmetric key used for encryption is encrypted in turn,
     * and inserted into the security header in an "EncryptedKey" structure. If
     * set to false, no EncryptedKey structure is constructed.
     * <p/>
     * The application may set this parameter using the following method:
     * <pre>
     * call.setProperty(WSHandlerConstants.ENC_SYM_ENC_KEY, "false");
     * </pre>
     * However, the parameter in the WSDD deployment file overwrites the
     * property setting (deployment setting overwrites application setting).
     */
    public static final String ENC_SYM_ENC_KEY = "encryptSymmetricEncryptionKey";

    /**
     * Parameter to define which parts of the request shall be encrypted.
     * <p/>
     * The value of this parameter is a list of semi-colon separated
     * element names that identify the elements to encrypt. An encryption mode
     * specifier and a namespace identification, each inside a pair of curly
     * brackets, may preceed each element name.
     * <p/>
     * The encryption mode specifier is either <code>{Content}</code> or
     * <code>{Element}</code>. Please refer to the W3C XML Encryption
     * specification about the differences between Element and Content
     * encryption. The encryption mode defaults to <code>Content</code>
     * if it is omitted. Example of a list:
     * <pre>
     * &lt;parameter name="encryptionParts"
     *   value="{Content}{http://example.org/paymentv2}CreditCard;
     *             {Element}{}UserName" />
     * </pre>
     * The the first entry of the list identifies the element
     * <code>CreditCard</code> in the namespace
     * <code>http://example.org/paymentv2</code>, and will encrypt its content.
     * Be aware that the element name, the namespace identifier, and the
     * encryption modifier are case sensitive.
     * <p/>
     * The encryption modifier and the namespace identifier can be ommited.
     * In this case the encryption mode defaults to <code>Content</code> and
     * the namespace is set to the SOAP namespace.
     * <p/>
     * An empty encryption mode defaults to <code>Content</code>, an empty
     * namespace identifier defaults to the SOAP namespace.
     * The second line of the example defines <code>Element</code> as
     * encryption mode for an <code>UserName</code> element in the SOAP
     * namespace.
     * <p/>
     * To specify an element without a namespace use the string
     * <code>Null</code> as the namespace name (this is a case sensitive
     * string)
     * <p/>
     * If no list is specified, the handler encrypts the SOAP Body in
     * <code>Content</code> mode by default.
     */
    public static final String ENCRYPTION_PARTS = "encryptionParts";

    /**
     * The name of the SAML Issuer factory property file.
     * The classloader loads this file. Therefore it must be accessible
     * via the classpath.
     */
    public static final String SAML_PROP_FILE = "samlPropFile";

    /**
     * Time-To-Live is the time difference between creation and expiry time in
     * the WSS Timestamp.
     * The time-to-live in seconds. After this time the SOAP request is
     * invalid (at least the security data shall be treated this way).
     * <p/>
     * If this parameter is not defined, contains a value less or equal
     * zero, or an illegal format the handlers use a default TTL of
     * 300 seconds (5 minutes).
     */
    public static final String TTL_TIMESTAMP = "timeToLive";

    /**
     * Whether to enable signatureConfirmation or not
     * By default signatureConfirmation is enabled
     */
    public static final String ENABLE_SIGNATURE_CONFIRMATION = "enableSignatureConfirmation";
    
    /**
     * Should timestamps have precision in milliseconds
     */
    public static final String TIMESTAMP_PRECISION = "precisionInMilliseconds";
    
    /**
     * Set the value of this parameter to true to enable strict timestamp
     * handling.
     * 
     * Strict Timestamp handling: throw an exception if a Timestamp contains
     * an <code>Expires</code> element and the semantics of the request are
     * expired, i.e. the current time at the receiver is past the expires time. 
     */
    public static final String TIMESTAMP_STRICT = "timestampStrict";
    
    /**
     * Set the value of this parameter to true to treat passwords as binary values
     * for Username Tokens.
     * 
     * This is needed to properly handle password equivalence for UsernameToken
     * passwords.  Binary passwords are Base64 encoded so they can be treated as 
     * strings in most places, but when the password digest is calculated or a key
     * is derived from the password, the password will be Base64 decoded before 
     * being used. This is most useful for hashed passwords as password equivalents.
     */
    public static final String USE_ENCODED_PASSWORDS = "useEncodedPasswords";
    
    /**
     * Define the parameter values to set the key identifier types. These are:
     * <ul>
     * <li><code>DirectReference</code> for {@link WSConstants#BST_DIRECT_REFERENCE}
     * </li>
     * <li><code>IssuerSerial</code> for {@link WSConstants#ISSUER_SERIAL}
     * </li>
     * <li><code>X509KeyIdentifier</code> for {@link WSConstants#X509_KEY_IDENTIFIER}
     * </li>
     * <li><code>SKIKeyIdentifier</code> for {@link WSConstants#SKI_KEY_IDENTIFIER}
     * </li>
     * <li><code>EmbeddedKeyName</code> for {@link WSConstants#EMBEDDED_KEYNAME}
     * </li>
     * <li><code>Thumbprint</code> for {@link WSConstants#THUMBPRINT}
     * </li>
     * <li><code>EncryptedKeySHA1</code> for {@link WSConstants#ENCRYPTED_KEY_SHA1_IDENTIFIER}
     * </li>
     * </ul
     * See {@link #SIG_KEY_ID} {@link #ENC_KEY_ID}.
     */
    public static Map keyIdentifier = new Hashtable();

    static {
        keyIdentifier.put("DirectReference",
                new Integer(WSConstants.BST_DIRECT_REFERENCE));
        keyIdentifier.put("IssuerSerial",
                new Integer(WSConstants.ISSUER_SERIAL));
        keyIdentifier.put("X509KeyIdentifier",
                new Integer(WSConstants.X509_KEY_IDENTIFIER));
        keyIdentifier.put("SKIKeyIdentifier",
                new Integer(WSConstants.SKI_KEY_IDENTIFIER));
        keyIdentifier.put("EmbeddedKeyName",
                new Integer(WSConstants.EMBEDDED_KEYNAME));
        keyIdentifier.put("Thumbprint",
                new Integer(WSConstants.THUMBPRINT_IDENTIFIER));
        keyIdentifier.put("EncryptedKeySHA1",
                new Integer(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER));
    }
    /*
     * internally used property names to store values inside the message context
     * that must have the same livetime as a message (request/response model).
     */
    public static final String SEND_SIGV = "_sendSignatureValues_";
    public static final String SIG_CONF_DONE = "_sigConfDone_";
}

