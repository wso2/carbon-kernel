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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.DOM2Writer;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.namespace.QName;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * UsernameToken according to WS Security specifications, UsernameToken profile.
 * 
 * Enhanced to support digest password type for username token signature
 * Enhanced to support passwordless usernametokens as allowed by spec.
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 * @author Werner Dittmann (Werner.Dittmann@t-online.de)
 */
public class UsernameToken {
    public static final String BASE64_ENCODING = WSConstants.SOAPMESSAGE_NS + "#Base64Binary";
    public static final String PASSWORD_TYPE = "passwordType";
    public static final int DEFAULT_ITERATION = 1000;
    public static final QName TOKEN = 
        new QName(WSConstants.WSSE_NS, WSConstants.USERNAME_TOKEN_LN);
    
    private static final Log LOG = LogFactory.getLog(UsernameToken.class.getName());
    private static final boolean DO_DEBUG = LOG.isDebugEnabled();
    private static SecureRandom random;

    protected Element element = null;
    protected Element elementUsername = null;
    protected Element elementPassword = null;
    protected Element elementNonce = null;
    protected Element elementCreated = null;
    protected Element elementSalt = null;
    protected Element elementIteration = null;
    protected String passwordType = null;
    protected boolean hashed = true;
    private String rawPassword;        // enhancement by Alberto Coletti
    private boolean passwordsAreEncoded = false;
    
    static {
        try {
            random = WSSecurityUtil.resolveSecureRandom();
        } catch (NoSuchAlgorithmException e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Constructs a <code>UsernameToken</code> object and parses the
     * <code>wsse:UsernameToken</code> element to initialize it.
     * 
     * @param elem the <code>wsse:UsernameToken</code> element that contains
     *             the UsernameToken data
     * @throws WSSecurityException
     */
    public UsernameToken(Element elem) throws WSSecurityException {
        this (elem, false);
    }

    /**
     * Constructs a <code>UsernameToken</code> object and parses the
     * <code>wsse:UsernameToken</code> element to initialize it.
     * 
     * @param elem the <code>wsse:UsernameToken</code> element that contains
     *             the UsernameToken data
     * @param allowNamespaceQualifiedPasswordTypes whether to allow (wsse)
     *        namespace qualified password types or not (for interop with WCF)
     * @throws WSSecurityException
     */
    public UsernameToken(
        Element elem, 
        boolean allowNamespaceQualifiedPasswordTypes
    ) throws WSSecurityException {
        element = elem;
        QName el = new QName(element.getNamespaceURI(), element.getLocalName());
        if (!el.equals(TOKEN)) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN,
                "badTokenType00", 
                new Object[] {el}
            );
        }
        elementUsername = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.USERNAME_LN, WSConstants.WSSE_NS
            );
        elementPassword = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.PASSWORD_LN, WSConstants.WSSE_NS
            );
        elementNonce = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.NONCE_LN, WSConstants.WSSE_NS
            );
        elementCreated = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.CREATED_LN, WSConstants.WSU_NS
            );
        elementSalt = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.SALT_LN, WSConstants.WSSE11_NS
            );
        elementIteration = 
            (Element) WSSecurityUtil.getDirectChild(
                element, WSConstants.ITERATION_LN, WSConstants.WSSE11_NS
            );
        if (elementUsername == null) {
            throw new WSSecurityException(
                WSSecurityException.INVALID_SECURITY_TOKEN,
                "badTokenType01", 
                new Object[] {el}
            );
        }
        hashed = false;
        if (elementSalt != null) {
            //
            // If the UsernameToken is to be used for key derivation, the (1.1)
            // spec says that it cannot contain a password, and it must contain
            // an Iteration element
            //
            if (elementPassword != null || elementIteration == null) {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "badTokenType01", 
                    new Object[] {el}
                );
            }
            return;
        }
        if (elementPassword != null) {
            if (elementPassword.hasAttribute(WSConstants.PASSWORD_TYPE_ATTR)) {
                passwordType = elementPassword.getAttribute(WSConstants.PASSWORD_TYPE_ATTR);
            } else if (elementPassword.hasAttributeNS(
                WSConstants.WSSE_NS, WSConstants.PASSWORD_TYPE_ATTR)
            ) {
                if (allowNamespaceQualifiedPasswordTypes) {
                    passwordType = 
                        elementPassword.getAttributeNS(
                            WSConstants.WSSE_NS, WSConstants.PASSWORD_TYPE_ATTR
                        );
                } else {
                    throw new WSSecurityException(
                        WSSecurityException.INVALID_SECURITY_TOKEN,
                        "badTokenType01", 
                        new Object[] {el}
                    );
                }
            }
            
        }
        if (passwordType != null
            && passwordType.equals(WSConstants.PASSWORD_DIGEST)) {
            hashed = true;
            if (elementNonce == null || elementCreated == null) {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY_TOKEN,
                    "badTokenType01", 
                    new Object[] {el}
                );
            }
        }
    }

    /**
     * Constructs a <code>UsernameToken</code> object according to the defined
     * parameters. <p/> This constructs set the password encoding to
     * {@link WSConstants#PASSWORD_DIGEST}
     * 
     * @param doc the SOAP envelope as <code>Document</code>
     */
    public UsernameToken(boolean milliseconds, Document doc) {
        this(milliseconds, doc, WSConstants.PASSWORD_DIGEST);
    }

    /**
     * Constructs a <code>UsernameToken</code> object according to the defined
     * parameters.
     * 
     * @param doc the SOAP envelope as <code>Document</code>
     * @param pwType the required password encoding, either
     *               {@link WSConstants#PASSWORD_DIGEST} or
     *               {@link WSConstants#PASSWORD_TEXT} or 
     *               {@link WSConstants#PW_NONE} <code>null</code> if no
     *               password required
     */
    public UsernameToken(boolean milliseconds, Document doc, String pwType) {
        element = 
            doc.createElementNS(WSConstants.WSSE_NS, "wsse:" + WSConstants.USERNAME_TOKEN_LN);
        WSSecurityUtil.setNamespace(element, WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX);

        elementUsername = 
            doc.createElementNS(WSConstants.WSSE_NS, "wsse:" + WSConstants.USERNAME_LN);
        elementUsername.appendChild(doc.createTextNode(""));
        element.appendChild(elementUsername);

        if (pwType != null) {
            elementPassword = 
                doc.createElementNS(WSConstants.WSSE_NS, "wsse:" + WSConstants.PASSWORD_LN);
            elementPassword.appendChild(doc.createTextNode(""));
            element.appendChild(elementPassword);

            hashed = false;
            passwordType = pwType;
            if (passwordType.equals(WSConstants.PASSWORD_DIGEST)) {
                hashed = true;
                addNonce(doc);
                addCreated(milliseconds, doc);
            }
        }
    }

    /**
     * Creates and adds a Nonce element to this UsernameToken
     */
    public void addNonce(Document doc) {
        if (elementNonce != null) {
            return;
        }
        byte[] nonceValue = new byte[16];
        random.nextBytes(nonceValue);
        elementNonce = doc.createElementNS(WSConstants.WSSE_NS, "wsse:" + WSConstants.NONCE_LN);
        elementNonce.appendChild(doc.createTextNode(Base64.encode(nonceValue)));
        elementNonce.setAttributeNS(null, "EncodingType", BASE64_ENCODING);
        element.appendChild(elementNonce);
    }

    /**
     * Creates and adds a Created element to this UsernameToken
     */
    public void addCreated(boolean milliseconds, Document doc) {
        if (elementCreated != null) {
            return;
        }
        DateFormat zulu = null;
        if (milliseconds) {
            zulu = new XmlSchemaDateFormat();
        } else {
            zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            zulu.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        Calendar rightNow = Calendar.getInstance();
        elementCreated = 
            doc.createElementNS(
                WSConstants.WSU_NS,WSConstants.WSU_PREFIX + ":" + WSConstants.CREATED_LN
            );
        WSSecurityUtil.setNamespace(element, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
        elementCreated.appendChild(doc.createTextNode(zulu.format(rightNow.getTime())));
        element.appendChild(elementCreated);
    }

    /**
     * Adds and optionally creates a Salt element to this UsernameToken.
     * 
     * If the <code>saltValue</code> is <code>null</code> the the method
     * generates a new salt. Otherwise it uses the the given value.
     * 
     * @param doc The Document for the UsernameToken
     * @param saltValue The salt to add, if null generate a new salt value
     * @param mac If <code>true</code> then an optionally generated value is
     *            usable for a MAC
     * @return Returns the added salt
     */
    public byte[] addSalt(Document doc, byte[] saltValue, boolean mac) {
        if (saltValue == null) {
            saltValue = generateSalt(mac);
        }
        elementSalt = 
            doc.createElementNS(
                WSConstants.WSSE11_NS, WSConstants.WSSE11_PREFIX + ":" + WSConstants.SALT_LN
            );
        WSSecurityUtil.setNamespace(this.element, WSConstants.WSSE11_NS, WSConstants.WSSE11_PREFIX);
        elementSalt.appendChild(doc.createTextNode(Base64.encode(saltValue)));
        element.appendChild(elementSalt);
        return saltValue;
    }

    /**
     * Creates and adds a Iteration element to this UsernameToken
     */
    public void addIteration(Document doc, int iteration) {
        String text = "" + iteration;
        elementIteration = 
            doc.createElementNS(
                WSConstants.WSSE11_NS, WSConstants.WSSE11_PREFIX + ":" + WSConstants.ITERATION_LN
            );
        WSSecurityUtil.setNamespace(element, WSConstants.WSSE11_NS, WSConstants.WSSE11_PREFIX);
        this.elementIteration.appendChild(doc.createTextNode(text));
        element.appendChild(elementIteration);
    }

    /**
     * Get the user name.
     * 
     * @return the data from the user name element.
     */
    public String getName() {
        return nodeString(elementUsername);
    }

    /**
     * Set the user name.
     * 
     * @param name sets a text node containing the use name into the user name
     *             element.
     */
    public void setName(String name) {
        Text node = getFirstNode(elementUsername);
        node.setData(name);
    }

    /**
     * Get the nonce.
     * 
     * @return the data from the nonce element.
     */
    public String getNonce() {
        return nodeString(elementNonce);
    }

    /**
     * Get the created timestamp.
     * 
     * @return the data from the created time element.
     */
    public String getCreated() {
        return nodeString(elementCreated);
    }

    /**
     * Gets the password string. This is the password as it is in the password
     * element of a username token. Thus it can be either plain text or the
     * password digest value.
     * 
     * @return the password string or <code>null</code> if no such node exists.
     */
    public String getPassword() {
        String password = nodeString(elementPassword);
        // See WSS-219
        if (password == null && elementPassword != null) {
            return "";
        }
        return password;
    }

    /**
     * Get the Salt value of this UsernameToken.
     * 
     * @return Returns the binary Salt value or <code>null</code> if no Salt
     *         value is available in the username token.
     * @throws WSSecurityException
     */
    public byte[] getSalt() throws WSSecurityException {
        String salt = nodeString(elementSalt);
        if (salt != null) {
            return Base64.decode(nodeString(elementSalt));
        }
        return null;
    }

    /**
     * Get the Iteration value of this UsernameToken.
     * 
     * @return Returns the Iteration value. If no Iteration was specified in the
     *         username token the default value according to the specification
     *         is returned.
     */
    public int getIteration() {
        String iter = nodeString(elementIteration);
        if (iter != null) {
            return Integer.parseInt(iter);
        }
        return DEFAULT_ITERATION;
    }

    /**
     * Get the hashed indicator. If the indicator is <code>true> the password of the
     * <code>UsernameToken</code> was encoded using {@link WSConstants#PASSWORD_DIGEST}
     *
     * @return the hashed indicator.
     */
    public boolean isHashed() {
        return hashed;
    }

    /**
     * @return Returns the passwordType.
     */
    public String getPasswordType() {
        return passwordType;
    }

    /**
     * Sets the password string. This function sets the password in the
     * <code>UsernameToken</code> either as plain text or encodes the password
     * according to the WS Security specifications, UsernameToken profile, into
     * a password digest.
     * 
     * @param pwd the password to use
     */
    public void setPassword(String pwd) {
        if (pwd == null) {
            if (passwordType != null) {
                throw new IllegalArgumentException("pwd == null but a password is needed");
            } else {
                // Ignore setting the password.
                return;
            }
        }
        
        rawPassword = pwd;             // enhancement by Alberto coletti
        Text node = getFirstNode(elementPassword);
        try {
            if (!hashed) {
                node.setData(pwd);
                elementPassword.setAttributeNS(null, "Type", WSConstants.PASSWORD_TEXT);
            } else {
                if (passwordsAreEncoded) {
                    node.setData(doPasswordDigest(getNonce(), getCreated(), Base64.decode(pwd)));
                } else {
                    node.setData(doPasswordDigest(getNonce(), getCreated(), pwd));
                }
                elementPassword.setAttributeNS(null, "Type", WSConstants.PASSWORD_DIGEST);
            }
        } catch (Exception e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    /**
     * Set the raw (plain text) password used to compute secret key.
     * 
     * @param newRawPassword the raw password to set
     */
    public void setRawPassword(String newRawPassword) {
        rawPassword = newRawPassword;
    }
    
    /**
     * Get the raw (plain text) password used to compute secret key.
     */
    public String getRawPassword() {
        return rawPassword;
    }
    
    /**
     * @param passwordsAreEncoded whether passwords are encoded
     */
    public void setPasswordsAreEncoded(boolean passwordsAreEncoded) {
        this.passwordsAreEncoded = passwordsAreEncoded;
    }
    
    /**
     * @return whether passwords are encoded
     */
    public boolean getPasswordsAreEncoded() {
        return passwordsAreEncoded;
    }
    
    public static String doPasswordDigest(String nonce, String created, byte[] password) {
        String passwdDigest = null;
        try {
            byte[] b1 = nonce != null ? Base64.decode(nonce) : new byte[0];
            byte[] b2 = created != null ? created.getBytes("UTF-8") : new byte[0];
            byte[] b3 = password;
            byte[] b4 = new byte[b1.length + b2.length + b3.length];
            int offset = 0;
            System.arraycopy(b1, 0, b4, offset, b1.length);
            offset += b1.length;
            
            System.arraycopy(b2, 0, b4, offset, b2.length);
            offset += b2.length;

            System.arraycopy(b3, 0, b4, offset, b3.length);
            
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.reset();
            sha.update(b4);
            passwdDigest = Base64.encode(sha.digest());
        } catch (Exception e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return passwdDigest;
    }

    public static String doPasswordDigest(String nonce, String created, String password) {
        String passwdDigest = null;
        try {
            passwdDigest = doPasswordDigest(nonce, created, password.getBytes("UTF-8"));
        } catch (Exception e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return passwdDigest;
    }

    /**
     * Returns the first text node of an element.
     * 
     * @param e the element to get the node from
     * @return the first text node or <code>null</code> if node is null or is
     *         not a text node
     */
    private Text getFirstNode(Element e) {
        Node node = e.getFirstChild();
        return ((node != null) && node instanceof Text) ? (Text) node : null;
    }

    /**
     * Returns the data of an element as String or null if either the the element
     * does not contain a Text node or the node is empty.
     * 
     * @param e DOM element
     * @return Element text node data as String
     */
    private String nodeString(Element e) {
        if (e != null) {
            Text node = getFirstNode(e);
            if (node != null) {
                return node.getData();
            }
        }
        return null;
    }

    /**
     * Returns the dom element of this <code>UsernameToken</code> object.
     * 
     * @return the <code>wsse:UsernameToken</code> element
     */
    public Element getElement() {
        return element;
    }

    /**
     * Returns the string representation of the token.
     * 
     * @return a XML string representation
     */
    public String toString() {
        return DOM2Writer.nodeToString((Node) this.element);
    }

    /**
     * Gets the id.
     * 
     * @return the value of the <code>wsu:Id</code> attribute of this username
     *         token
     */
    public String getID() {
        return element.getAttributeNS(WSConstants.WSU_NS, "Id");
    }

    /**
     * Set the id of this username token.
     * 
     * @param id
     *            the value for the <code>wsu:Id</code> attribute of this
     *            username token
     */
    public void setID(String id) {
        String prefix = 
            WSSecurityUtil.setNamespace(element, WSConstants.WSU_NS, WSConstants.WSU_PREFIX);
        element.setAttributeNS(WSConstants.WSU_NS, prefix + ":Id", id);
    }

    /**
     * Gets the secret key as per WS-Trust spec. This method uses default setting
     * to generate the secret key. These default values are suitable for .NET
     * WSE.
     * 
     * @return a secret key constructed from information contained in this
     *         username token
     */
    public byte[] getSecretKey() {
        return getSecretKey(WSConstants.WSE_DERIVED_KEY_LEN, WSConstants.LABEL_FOR_DERIVED_KEY);
    }
    
    /**
     * Gets the secret key as per WS-Trust spec. This method uses default setting
     * to generate the secret key. These default values are suitable for .NET
     * WSE.
     * 
     * @return a secret key constructed from information contained in this
     *         username token
     */
    public byte[] getSecretKey(int keylen) {
        return getSecretKey(keylen, WSConstants.LABEL_FOR_DERIVED_KEY);
    }

    /**
     * Gets the secret key as per WS-Trust spec.
     * 
     * @param keylen How many bytes to generate for the key
     * @param labelString the label used to generate the seed
     * @return a secret key constructed from information contained in this
     *         username token
     */
    public byte[] getSecretKey(int keylen, String labelString) {
        byte[] key = null;
        try {
            Mac mac = Mac.getInstance("HMACSHA1");
            byte[] password;
            if (passwordsAreEncoded) {
                password = Base64.decode(rawPassword);
            } else {
                password = rawPassword.getBytes("UTF-8"); // enhancement by Alberto Coletti
            }
            byte[] label = labelString.getBytes("UTF-8");
            byte[] nonce = Base64.decode(getNonce());
            byte[] created = getCreated().getBytes("UTF-8");
            byte[] seed = new byte[label.length + nonce.length + created.length];

            int offset = 0;
            System.arraycopy(label, 0, seed, offset, label.length);
            offset += label.length;
            
            System.arraycopy(nonce, 0, seed, offset, nonce.length);
            offset += nonce.length;

            System.arraycopy(created, 0, seed, offset, created.length);
            
            key = P_hash(password, seed, mac, keylen);

            if (LOG.isDebugEnabled()) {
                LOG.debug("password   :" + Base64.encode(password));
                LOG.debug("label      :" + Base64.encode(label));
                LOG.debug("nonce      :" + Base64.encode(nonce));
                LOG.debug("created    :" + Base64.encode(created));
                LOG.debug("seed       :" + Base64.encode(seed));
                LOG.debug("Key        :" + Base64.encode(key));
            }
        } catch (Exception e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            return null;
        }
        return key;
    }
    
    
    /**
     * This static method generates a derived key as defined in WSS Username
     * Token Profile.
     * 
     * @param password The password to include in the key generation
     * @param salt The Salt value
     * @param iteration The Iteration value. If zero (0) is given the method uses the
     *                  default value
     * @return Returns the derived key a byte array
     * @throws WSSecurityException
     */
    public static byte[] generateDerivedKey(
        byte[] password, 
        byte[] salt, 
        int iteration
    ) throws WSSecurityException {
        if (iteration == 0) {
            iteration = DEFAULT_ITERATION;
        }

        byte[] pwSalt = new byte[salt.length + password.length];
        System.arraycopy(password, 0, pwSalt, 0, password.length);
        System.arraycopy(salt, 0, pwSalt, password.length, salt.length);

        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "noSHA1availabe", null, e
            );
        }
        sha.reset();

        //
        // Make the first hash round with start value
        //
        byte[] K = sha.digest(pwSalt);
        //
        // Perform the 1st up to iteration-1 hash rounds
        //
        for (int i = 1; i < iteration; i++) {
            K = sha.digest(K);
        }
        return K;
    }
    
    /**
     * This static method generates a derived key as defined in WSS Username
     * Token Profile.
     * 
     * @param password The password to include in the key generation
     * @param salt The Salt value
     * @param iteration The Iteration value. If zero (0) is given the method uses the
     *                  default value
     * @return Returns the derived key a byte array
     * @throws WSSecurityException
     */
    public static byte[] generateDerivedKey(
        String password, 
        byte[] salt, 
        int iteration
    ) throws WSSecurityException {
        try {
            return generateDerivedKey(password.getBytes("UTF-8"), salt, iteration);
        } catch (final java.io.UnsupportedEncodingException e) {
            if (DO_DEBUG) {
                LOG.debug(e.getMessage(), e);
            }
            throw new WSSecurityException("Unable to convert password to UTF-8", e);
        }
    }
    
    
    /**
     * This method gets a derived key as defined in WSS Username Token Profile.
     * 
     * @return Returns the derived key as a byte array
     * @throws WSSecurityException
     */
    public byte[] getDerivedKey() throws WSSecurityException {
        int iteration = getIteration();
        byte[] salt = getSalt();
        if (passwordsAreEncoded) {
            return generateDerivedKey(Base64.decode(rawPassword), salt, iteration);
        } else {
            return generateDerivedKey(rawPassword, salt, iteration);
        }
    }
    
    /**
     * Return whether the UsernameToken represented by this class is to be used
     * for key derivation as per the UsernameToken Profile 1.1. It does this by
     * checking that the username token has salt and iteration values.
     * 
     * @throws WSSecurityException
     */
    public boolean isDerivedKey() throws WSSecurityException {
        if (elementSalt != null && elementIteration != null) {
            return true;
        }
        return false;
    }

    
    /**
     * This static method generates a 128 bit salt value as defined in WSS
     * Username Token Profile.
     * 
     * @param useForMac If <code>true</code> define the Salt for use in a MAC
     * @return Returns the 128 bit salt value as byte array
     */
    public static byte[] generateSalt(boolean useForMac) {
        byte[] saltValue = new byte[16];
        random.nextBytes(saltValue);
        if (useForMac) {
            saltValue[15] = 0x01;
        } else {
            saltValue[15] = 0x02;
        }
        return saltValue;
    }

    /**
     * P_hash as defined in RFC 2246 for TLS.
     * 
     * @param secret is the key for the HMAC
     * @param seed the seed value to start the generation - A(0)
     * @param mac the HMAC algorithm
     * @param required number of bytes to generate
     * @return a byte array that contains a secret key
     * @throws Exception
     */
    private static byte[] P_hash(
        byte[] secret, 
        byte[] seed, 
        Mac mac, 
        int required
    ) throws Exception {
        byte[] out = new byte[required];
        int offset = 0, tocpy;
        byte[] A, tmp;
        //
        // A(0) is the seed
        //
        A = seed;
        SecretKeySpec key = new SecretKeySpec(secret, "HMACSHA1");
        mac.init(key);
        while (required > 0) {
            mac.update(A);
            A = mac.doFinal();
            mac.update(A);
            mac.update(seed);
            tmp = mac.doFinal();
            tocpy = min(required, tmp.length);
            System.arraycopy(tmp, 0, out, offset, tocpy);
            offset += tocpy;
            required -= tocpy;
        }
        return out;
    }

    /**
     * helper method.
     *
     * @param a
     * @param b
     * @return
     */
    private static int min(int a, int b) {
        return (a > b) ? b : a;
    }
}
