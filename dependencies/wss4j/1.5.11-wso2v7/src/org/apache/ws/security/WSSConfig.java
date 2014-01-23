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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.action.Action;
import org.apache.ws.security.processor.Processor;
import org.apache.ws.security.transform.STRTransform;
import org.apache.ws.security.util.Loader;
import org.apache.ws.security.util.UUIDGenerator;
import org.apache.xml.security.transforms.Transform;

/**
 * WSSConfig <p/> Carries configuration data so the WSS4J spec compliance can be
 * modified in runtime. Configure an instance of this object only if you need
 * WSS4J to emulate certain industry clients or previous OASIS specifications
 * for WS-Security interoperability testing purposes. <p/> The default settings
 * follow the latest OASIS and changing anything might violate the OASIS specs.
 * <p/> <b>WARNING: changing the default settings will break the compliance with
 * the latest specs. Do this only if you know what you are doing.</b> <p/>
 * 
 * @author Rami Jaamour (rjaamour@parasoft.com)
 * @author Werner Dittmann (werner@apache.org)
 */
public class WSSConfig {
    
    private static final Log log = LogFactory.getLog(WSSConfig.class.getName());

    /**
     * The default collection of actions supported by the toolkit.
     */
    private static final java.util.Map DEFAULT_ACTIONS;
    static {
        final java.util.Map tmp = new java.util.HashMap();
        try {
            tmp.put(
                new Integer(WSConstants.UT),
                org.apache.ws.security.action.UsernameTokenAction.class.getName()
            );
            tmp.put(
                new Integer(WSConstants.ENCR),
                org.apache.ws.security.action.EncryptionAction.class.getName()
            );
            tmp.put(
                new Integer(WSConstants.SIGN),
                org.apache.ws.security.action.SignatureAction.class.getName()
            );
            //
            // Note that all actions/processors with dependencies on opensaml are
            // registered as Strings. This is so that applications that do not use
            // saml do not have to have the opensaml jar available.
            //
            tmp.put(
                new Integer(WSConstants.ST_SIGNED),
                "org.apache.ws.security.action.SAMLTokenSignedAction"
            );
            tmp.put(
                new Integer(WSConstants.ST_UNSIGNED),
                "org.apache.ws.security.action.SAMLTokenUnsignedAction"
            );
            tmp.put(
                new Integer(WSConstants.TS),
                org.apache.ws.security.action.TimestampAction.class.getName()
            );
            tmp.put(
                new Integer(WSConstants.UT_SIGN),
                org.apache.ws.security.action.UsernameTokenSignedAction.class.getName()
            );
            tmp.put(
                new Integer(WSConstants.SC),
                org.apache.ws.security.action.SignatureConfirmationAction.class.getName()
            );
        } catch (final Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug(t.getMessage(), t);
            }
        }
        DEFAULT_ACTIONS = java.util.Collections.unmodifiableMap(tmp);
    }

    /**
     * The default collection of processors supported by the toolkit
     */
    private static final java.util.Map DEFAULT_PROCESSORS;
    static {
        final java.util.Map tmp = new java.util.HashMap();
        try {
            tmp.put(
                WSSecurityEngine.SAML_TOKEN,
                "org.apache.ws.security.processor.SAMLTokenProcessor"
            );
            tmp.put(
                WSSecurityEngine.ENCRYPTED_KEY,
                org.apache.ws.security.processor.EncryptedKeyProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.SIGNATURE,
                org.apache.ws.security.processor.SignatureProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.timeStamp,
                org.apache.ws.security.processor.TimestampProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.usernameToken,
                org.apache.ws.security.processor.UsernameTokenProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.REFERENCE_LIST,
                org.apache.ws.security.processor.ReferenceListProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.signatureConfirmation,
                org.apache.ws.security.processor.SignatureConfirmationProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.DERIVED_KEY_TOKEN_05_02,
                org.apache.ws.security.processor.DerivedKeyTokenProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.DERIVED_KEY_TOKEN_05_12,
                tmp.get(WSSecurityEngine.DERIVED_KEY_TOKEN_05_02)
            );
            tmp.put(
                WSSecurityEngine.SECURITY_CONTEXT_TOKEN_05_02,
                org.apache.ws.security.processor.SecurityContextTokenProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.SECURITY_CONTEXT_TOKEN_05_12,
                tmp.get(WSSecurityEngine.SECURITY_CONTEXT_TOKEN_05_02)
            );
            tmp.put(
                WSSecurityEngine.binaryToken,
                org.apache.ws.security.processor.BinarySecurityTokenProcessor.class.getName()
            );
            tmp.put(
                WSSecurityEngine.ENCRYPTED_DATA,
                org.apache.ws.security.processor.EncryptedDataProcessor.class.getName()
            );

            tmp.put(WSSecurityEngine.SAML2_TOKEN, org.apache.ws.security.processor.SAML2TokenProcessor.class.getName());

        } catch (final Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug(t.getMessage(), t);
            }
        }
        DEFAULT_PROCESSORS = java.util.Collections.unmodifiableMap(tmp);
    }

    protected static WSSConfig defaultConfig = null;

    protected boolean wsiBSPCompliant = false;

    /**
     * Set the timestamp precision mode. If set to <code>true</code> then use
     * timestamps with milliseconds, otherwise omit the milliseconds. As per XML
     * Date/Time specification the default is to include the milliseconds.
     */
    protected boolean precisionInMilliSeconds = true;

    protected boolean enableSignatureConfirmation = true;

    /**
     * If set to true then the timestamp handling will throw an exception if the
     * timestamp contains an expires element and the semantics are expired.
     * 
     * If set to false, no exception will be thrown, even if the semantics are
     * expired.
     */
    protected boolean timeStampStrict = true;
    
    /**
     * This variable controls whether types other than PasswordDigest or PasswordText
     * are allowed when processing UsernameTokens. 
     * 
     * By default this is set to false so that the user doesn't have to explicitly
     * reject custom token types in the callback handler.
     */
    protected boolean handleCustomPasswordTypes = false;
    
    /**
     * This variable controls whether (wsse) namespace qualified password types are
     * accepted when processing UsernameTokens.
     * 
     * By default this is set to false.
     */
    protected boolean allowNamespaceQualifiedPasswordTypes = false;
    
    /**
     * The secret key length to be used for UT_SIGN.
     */
    protected int secretKeyLength = WSConstants.WSE_DERIVED_KEY_LEN;
    
    /**
     * Whether the password should be treated as a binary value.  This
     * is needed to properly handle password equivalence for UsernameToken
     * passwords.  Binary passwords are Base64 encoded so they can be
     * treated as strings in most places, but when the password digest
     * is calculated or a key is derived from the password, the password
     * will be Base64 decoded before being used. This is most useful for
     * hashed passwords as password equivalents.
     *
     * See https://issues.apache.org/jira/browse/WSS-239
     */
    protected boolean passwordsAreEncoded = false;
    
    /**
     * The default wsu:Id allocator is a simple "start at 1 and increment up"
     * thing that is very fast.
     */
    public static WsuIdAllocator DEFAULT_ID_ALLOCATOR = new WsuIdAllocator() {
        int i;
        private synchronized String next() {
            return Integer.toString(++i);
        }
        public String createId(String prefix, Object o) {
            if (prefix == null) {
                return next();
            }
            return prefix + next();
        }

        public String createSecureId(String prefix, Object o) {
            if (prefix == null) {
                return UUIDGenerator.getUUID();
            }
            return prefix + UUIDGenerator.getUUID();
        }
    };
    protected WsuIdAllocator idAllocator = DEFAULT_ID_ALLOCATOR;
    
    protected HashMap jceProvider = new HashMap(10);

    /**
     * The known actions. This map is of the form <Integer, String> or <Integer, Action>. 
     * The known actions are initialized from a set of defaults,
     * but the list may be modified via the setAction operations.
     */
    private final java.util.Map actionMap = new java.util.HashMap(DEFAULT_ACTIONS);

    /**
     * The known processors. This map is of the form <String, String> or <String,Processor>.
     * The known processors are initialized from a set of defaults,
     * but the list may be modified via the setProcessor operations.
     */
    private final java.util.Map processorMap = new java.util.HashMap(DEFAULT_PROCESSORS);
    
    /**
     * a static boolean flag that determines whether default JCE providers
     * should be added at the time of construction.
     *
     * These providers, and the order in which they are added, can interfere
     * with some JVMs (such as IBMs).
     */
    private static boolean addJceProviders = true;
    
    /**
     * a boolean flag to record whether we have already been statically
     * initialized.  This flag prevents repeated and unnecessary calls
     * to static initialization code at construction time.
     */
    private static boolean staticallyInitialized = false;
    
    /**
     * Set the value of the internal addJceProviders flag.  This flag
     * turns on (or off) automatic registration of known JCE providers
     * that provide necessary cryptographic algorithms for use with WSS4J.
     * By default, this flag is true, for backwards compatibility.  You may
     * wish (or need) to initialize the JCE manually, e.g., in some JVMs.
     */
    public static void setAddJceProviders(boolean value) {
        addJceProviders = value;
    }
    
    private synchronized void
    staticInit() {
        if (!staticallyInitialized) {
            org.apache.xml.security.Init.init();
            if (addJceProviders) {
                /*
                 * The last provider added has precedence, that is if JuiCE can be added
                 * then WSS4J uses this provider.
                 */
                addJceProvider("BC", "org.bouncycastle.jce.provider.BouncyCastleProvider");
                addJceProvider("JuiCE", "org.apache.security.juice.provider.JuiCEProviderOpenSSL");
            }
            //Transform.init();
            try {
                Transform.register(
                    STRTransform.implementedTransformURI,
                    "org.apache.ws.security.transform.STRTransform"
                );
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug(ex.getMessage(), ex);
                }
            }
            staticallyInitialized = true;
        }
    }
    
    protected WSSConfig() {
        staticInit();
    }
    
    /**
     * @return a new WSSConfig instance configured with the default values
     *         (values identical to
     *         {@link #getDefaultWSConfig getDefaultWSConfig()})
     */
    public static WSSConfig getNewInstance() {
        return new WSSConfig();
    }

    /**
     * returns a static WSConfig instance that is configured with the latest
     * OASIS WS-Security settings.
     */
    public static WSSConfig getDefaultWSConfig() {
        if (defaultConfig == null) {
            defaultConfig = getNewInstance();
        }
        return defaultConfig;
    }
    
    /**
     * Checks if we are in WS-I Basic Security Profile compliance mode
     * 
     * @return TODO
     */
    public boolean isWsiBSPCompliant() {
        return wsiBSPCompliant;
    }

    /**
     * Set the WS-I Basic Security Profile compliance mode. The default is false
     * (dues to .Net interop problems).
     * 
     * @param wsiBSPCompliant
     */
    public void setWsiBSPCompliant(boolean wsiBSPCompliant) {
        this.wsiBSPCompliant = wsiBSPCompliant;
    }

    /**
     * Checks if we need to use milliseconds in timestamps
     * 
     * @return TODO
     */
    public boolean isPrecisionInMilliSeconds() {
        return precisionInMilliSeconds;
    }

    /**
     * Set the precision in milliseconds
     * 
     * @param precisionInMilliSeconds
     *            TODO
     */
    public void setPrecisionInMilliSeconds(boolean precisionInMilliSeconds) {
        this.precisionInMilliSeconds = precisionInMilliSeconds;
    }

    /**
     * @return Returns the enableSignatureConfirmation.
     */
    public boolean isEnableSignatureConfirmation() {
        return enableSignatureConfirmation;
    }

    /**
     * @param enableSignatureConfirmation
     *            The enableSignatureConfirmation to set.
     */
    public void setEnableSignatureConfirmation(boolean enableSignatureConfirmation) {
        this.enableSignatureConfirmation = enableSignatureConfirmation;
    }
    
    /**
     * @param handleCustomTypes 
     * whether to handle custom UsernameToken password types or not
     */
    public void setHandleCustomPasswordTypes(boolean handleCustomTypes) {
        this.handleCustomPasswordTypes = handleCustomTypes;
    }
    
    /**
     * @return whether custom UsernameToken password types are allowed or not
     */
    public boolean getHandleCustomPasswordTypes() {
        return handleCustomPasswordTypes;
    }
    
    /**
     * @param allowNamespaceQualifiedTypes
     * whether (wsse) namespace qualified password types are accepted or not
     */
    public void setAllowNamespaceQualifiedPasswordTypes(boolean allowNamespaceQualifiedTypes) {
        allowNamespaceQualifiedPasswordTypes = allowNamespaceQualifiedTypes;
    }
    
    /**
     * @return whether (wsse) namespace qualified password types are accepted or not
     */
    public boolean getAllowNamespaceQualifiedPasswordTypes() {
        return allowNamespaceQualifiedPasswordTypes;
    }
    
    /**
     * @return Returns if we shall throw an exception on expired request
     *         semantic
     */
    public boolean isTimeStampStrict() {
        return timeStampStrict;
    }

    /**
     * @param timeStampStrict
     *            If true throw an exception on expired request semantic
     */
    public void setTimeStampStrict(boolean timeStampStrict) {
        this.timeStampStrict = timeStampStrict;
    }
    
    /**
     * Set the secret key length to be used for UT_SIGN.
     */
    public void setSecretKeyLength(int length) {
        secretKeyLength = length;
    }
    
    /**
     * Get the secret key length to be used for UT_SIGN.
     */
    public int getSecretKeyLength() {
        return secretKeyLength;
    }
    
    /**
     * @param passwordsAreEncoded
     * whether passwords are encoded
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
    
    /**
     * @return Returns the WsuIdAllocator used to generate wsu:Id attributes
     */
    public WsuIdAllocator getIdAllocator() {
        return idAllocator;
    }

    public void setIdAllocator(WsuIdAllocator idAllocator) {
        this.idAllocator = idAllocator;
    }

    /**
     * Associate an action name with a specific action code.
     *
     * This operation allows applications to supply their own
     * actions for well-known operations.
     */
    public String setAction(int code, String action) {
        Object previousAction = actionMap.put(new Integer(code), action);
        if (previousAction instanceof String) {
            return (String)previousAction;
        } else if (previousAction instanceof Action){
            return previousAction.getClass().getName();
        }
        return null;
    }
    
    /**
     * Associate an action instance with a specific action code.
     *
     * This operation allows applications to supply their own
     * actions for well-known operations.
     */
    public String setAction(int code, Action action) {
        Object previousAction = actionMap.put(new Integer(code), action);
        if (previousAction instanceof String) {
            return (String)previousAction;
        } else if (previousAction instanceof Action){
            return previousAction.getClass().getName();
        }
        return null;
    }

    /**
     * Lookup action
     * 
     * @param action
     * @return An action class to create a security token
     * @throws WSSecurityException
     */
    public Action getAction(int action) throws WSSecurityException {
        Integer key = new Integer(action);
        final Object actionObject = actionMap.get(key);
        
        if (actionObject instanceof String) {
            final String name = (String)actionObject;
            try {
                return (Action) Loader.loadClass(name).newInstance();
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug(t.getMessage(), t);
                }
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "unableToLoadClass", new Object[] { name }, t);
            }
        } else if (actionObject instanceof Action) {
            return (Action)actionObject;
        } 
        return null;
    }
    
    /**
     * Associate a SOAP processor name with a specified SOAP Security header
     * element QName.  Processors registered under this QName will be
     * called when processing header elements with the specified type.
     */
    public String setProcessor(QName el, String name) {
        Object previousProcessor = processorMap.put(el, name);
        if (previousProcessor instanceof String) {
            return (String)previousProcessor;
        } else if (previousProcessor instanceof Processor){
            return previousProcessor.getClass().getName();
        }
        return null;
    }
    
    /**
     * Associate a SOAP processor instance with a specified SOAP Security header
     * element QName.  Processors registered under this QName will be
     * called when processing header elements with the specified type.
     */
    public String setProcessor(QName el, Processor processor) {
        Object previousProcessor = processorMap.put(el, processor);
        if (previousProcessor instanceof String) {
            return (String)previousProcessor;
        } else if (previousProcessor instanceof Processor){
            return previousProcessor.getClass().getName();
        }
        return null;
    }

    /**
     * @return      the SOAP processor associated with the specified
     *              QName.  The QName is intended to refer to an element
     *              in a SOAP security header.  This operation returns
     *              null if there is no processor associated with the 
     *              specified QName.
     */
    public Processor getProcessor(QName el) throws WSSecurityException {
        final Object processorObject = processorMap.get(el);
        if (processorObject instanceof String) {
            final String name = (String)processorObject;
            try {
                return (Processor) Loader.loadClass(name).newInstance();
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug(t.getMessage(), t);
                }
                throw new WSSecurityException(WSSecurityException.FAILURE,
                        "unableToLoadClass", new Object[] { name }, t);
            }
        } else if (processorObject instanceof Processor) {
            return (Processor)processorObject;
        } 
        return null;
    }

    private boolean loadProvider(String id, String className) {
        try {
            if (java.security.Security.getProvider(id) == null) {
                Class c = Loader.loadClass(className, false);
                java.security.Provider[] provs = 
                    java.security.Security.getProviders();
                //
                // Install the provider after the SUN provider (see WSS-99)
                // Otherwise fall back to the old behaviour of inserting
                // the provider in position 2. For AIX, install it after
                // the IBMJCE provider.
                //
                int ret = 0;
                for (int i = 0; i < provs.length; i++) {
                    if ("SUN".equals(provs[i].getName())
                        || "IBMJCE".equals(provs[i].getName())) {
                        ret =
                            java.security.Security.insertProviderAt(
                                (java.security.Provider) c.newInstance(), i + 2
                            );
                        break;
                    }
                }
                if (ret == 0) {
                    ret =
                    java.security.Security.insertProviderAt(
                        (java.security.Provider) c.newInstance(), 2
                    );
                }
                if (log.isDebugEnabled()) {
                    log.debug("The provider " + id + " was added at position: " + ret);
                }                
            }
            return true;
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("The provider " + id + " could not be added: " + t.getMessage());
            }
            return false;
        }

    }

    /**
     * Add a new JCE security provider to use for WSS4J.
     * 
     * If the provider is not already known the method loads a security provider
     * class and adds the provider to the java security service.
     * 
     * 
     * @param id
     *            The id string of the provider
     * @param className
     *            Name of the class the implements the provider. This class must
     *            be a subclass of <code>java.security.Provider</code>
     * 
     * @return Returns <code>true</code> if the provider was successfully
     *         added, <code>false</code> otherwise.
     */
    public boolean addJceProvider(String id, String className) {
        if (jceProvider.get(id) == null && loadProvider(id, className)) {
            jceProvider.put(id, className);
            return true;
        }
        return false;
    }
}
