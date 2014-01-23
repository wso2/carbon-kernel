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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.action.Action;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SignatureConfirmation;
import org.apache.ws.security.message.token.Timestamp;
import org.apache.ws.security.util.Loader;
import org.apache.ws.security.util.StringUtil;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;


/**
 * Extracted from WSDoAllReceiver and WSDoAllSender
 * Extended to all passwordless UsernameTokens and configurable identities.
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 * @author Werner Dittmann (Werner.Dittmann@t-online.de).
 * @author Marcel Ammerlaan (marcel.ammerlaan@gmail.com).
 */
public abstract class WSHandler {
    public static String DONE = "done";
    private static Log log = LogFactory.getLog(WSHandler.class.getName());
    protected static final WSSecurityEngine secEngine = WSSecurityEngine.getInstance();
    protected static Hashtable cryptos = new Hashtable(5);

    private boolean doDebug = log.isDebugEnabled();

    /**                                                             
     * Performs all defined security actions to set-up the SOAP request.
     * 
     * 
     * @param doAction a set defining the actions to do 
     * @param doc   the request as DOM document 
     * @param reqData a data storage to pass values around between methods
     * @param actions a vector holding the actions to do in the order defined
     *                in the deployment file or property
     * @throws WSSecurityException
     */
    protected void doSenderAction(
            int doAction, 
            Document doc,
            RequestData reqData, 
            Vector actions, 
            boolean isRequest
    ) throws WSSecurityException {

        boolean mu = decodeMustUnderstand(reqData);

        WSSConfig wssConfig = reqData.getWssConfig();
        if (wssConfig == null) {
            wssConfig = secEngine.getWssConfig();
        }

        boolean enableSigConf = decodeEnableSignatureConfirmation(reqData);
        wssConfig.setEnableSignatureConfirmation(
            enableSigConf || ((doAction & WSConstants.SC) != 0)
        );
        wssConfig.setPasswordsAreEncoded(decodeUseEncodedPasswords(reqData));

        wssConfig.setPrecisionInMilliSeconds(
            decodeTimestampPrecision(reqData)
        );
        reqData.setWssConfig(wssConfig);

        Object mc = reqData.getMsgContext();
        String actor = getString(WSHandlerConstants.ACTOR, mc);
        reqData.setActor(actor);

        WSSecHeader secHeader = new WSSecHeader(actor, mu);
        secHeader.insertSecurityHeader(doc);

        reqData.setSecHeader(secHeader);
        reqData.setSoapConstants(
            WSSecurityUtil.getSOAPConstants(doc.getDocumentElement())
        );
        /*
         * Here we have action, username, password, and actor, mustUnderstand.
         * Now get the action specific parameters.
         */
        if ((doAction & WSConstants.UT) == WSConstants.UT) {
            decodeUTParameter(reqData);
        }
        /*
         * Here we have action, username, password, and actor, mustUnderstand.
         * Now get the action specific parameters.
         */
        if ((doAction & WSConstants.UT_SIGN) == WSConstants.UT_SIGN) {
            decodeUTParameter(reqData);
            decodeSignatureParameter(reqData);
        }
        /*
         * Get and check the Signature specific parameters first because they
         * may be used for encryption too.
         */
        if ((doAction & WSConstants.SIGN) == WSConstants.SIGN) {
            reqData.setSigCrypto(loadSignatureCrypto(reqData));
            decodeSignatureParameter(reqData);
        }
        /*
         * If we need to handle signed SAML token then we may need the
         * Signature parameters. The handle procedure loads the signature crypto
         * file on demand, thus don't do it here.
         */
        if ((doAction & WSConstants.ST_SIGNED) == WSConstants.ST_SIGNED) {
            decodeSignatureParameter(reqData);
        }
        /*
         * Set and check the encryption specific parameters, if necessary take
         * over signature parameters username and crypto instance.
         */
        if ((doAction & WSConstants.ENCR) == WSConstants.ENCR) {
            reqData.setEncCrypto(loadEncryptionCrypto(reqData));
            decodeEncryptionParameter(reqData);
        }
        /*
         * If after all the parsing no Signature parts defined, set here a
         * default set. This is necessary because we add SignatureConfirmation
         * and therefore the default (Body) must be set here. The default setting
         * in WSSignEnvelope doesn't work because the vector is not empty anymore.
         */
        if (reqData.getSignatureParts().isEmpty()) {
            WSEncryptionPart encP = new WSEncryptionPart(reqData.getSoapConstants()
                    .getBodyQName().getLocalPart(), reqData.getSoapConstants()
                    .getEnvelopeURI(), "Content");
            reqData.getSignatureParts().add(encP);
        }
        /*
         * If SignatureConfirmation is enabled and this is a response then
         * insert SignatureConfrmation elements, note their wsu:id in the signature
         * parts. They will be signed automatically during a (probably) defined
         * SIGN action.
         */
        if (wssConfig.isEnableSignatureConfirmation() && !isRequest) {
            String done = (String) 
                getProperty(reqData.getMsgContext(), WSHandlerConstants.SIG_CONF_DONE);
            if (!DONE.equals(done)
                && (getProperty(reqData.getMsgContext(), WSHandlerConstants.RECV_RESULTS)) 
                    != null) {
                wssConfig.getAction(WSConstants.SC).execute(this, WSConstants.SC, doc, reqData);
            }
        }
        /*
         * Here we have all necessary information to perform the requested
         * action(s).
         */
        for (int i = 0; i < actions.size(); i++) {

            int actionToDo = ((Integer) actions.get(i)).intValue();
            if (doDebug) {
                log.debug("Performing Action: " + actionToDo);
            }

            switch (actionToDo) {
            case WSConstants.UT:
            case WSConstants.ENCR:
            case WSConstants.SIGN:
            case WSConstants.ST_SIGNED:
            case WSConstants.ST_UNSIGNED:
            case WSConstants.TS:
            case WSConstants.UT_SIGN:
                wssConfig.getAction(actionToDo).execute(this, actionToDo, doc, reqData);
                break;
            case WSConstants.NO_SERIALIZE:
                reqData.setNoSerialization(true);
                break;
                //
                // Handle any "custom" actions, similarly,
                // but to preserve behavior from previous
                // versions, consume (but log) action lookup failures.
                //
            default:
                Action doit = null;
            try {
                doit = wssConfig.getAction(actionToDo);
            } catch (final WSSecurityException e) {
                log.warn(
                        "Error trying to locate a custom action (" + actionToDo + ")", 
                        e
                );
            }
            if (doit != null) {
                doit.execute(this, actionToDo, doc, reqData);
            }
            }
        }
        
        /*
         * If this is a request then store all signature values. Add ours to
         * already gathered values because of chained handlers, e.g. for
         * other actors.
         */
        if (wssConfig.isEnableSignatureConfirmation() 
                && isRequest
                && reqData.getSignatureValues().size() > 0) {
            Vector sigv = (Vector) 
            getProperty(reqData.getMsgContext(), WSHandlerConstants.SEND_SIGV);
            if (sigv == null) {
                sigv = new Vector();
                setProperty(reqData.getMsgContext(),
                        WSHandlerConstants.SEND_SIGV, sigv);
            }
            // sigv.add(reqData.getSignatureValues());
            sigv.addAll(reqData.getSignatureValues());
        }
    }



    protected void doReceiverAction(int doAction, RequestData reqData)
        throws WSSecurityException {

        WSSConfig wssConfig = reqData.getWssConfig();
        if (wssConfig == null) {
            wssConfig = secEngine.getWssConfig();
        }
        boolean enableSigConf = decodeEnableSignatureConfirmation(reqData);
        wssConfig.setEnableSignatureConfirmation(
            enableSigConf || ((doAction & WSConstants.SC) != 0)
        );
        wssConfig.setTimeStampStrict(decodeTimestampStrict(reqData));
        wssConfig.setHandleCustomPasswordTypes(decodeCustomPasswordTypes(reqData));
        wssConfig.setPasswordsAreEncoded(decodeUseEncodedPasswords(reqData));
        wssConfig.setAllowNamespaceQualifiedPasswordTypes(
            decodeNamespaceQualifiedPasswordTypes(reqData)
        );
        wssConfig.setSecretKeyLength(reqData.getSecretKeyLength());
        reqData.setWssConfig(wssConfig);

        if ((doAction & WSConstants.SIGN) == WSConstants.SIGN) {
            decodeSignatureParameter2(reqData);
        }
        
        if ((doAction & WSConstants.ST_SIGNED) == WSConstants.ST_SIGNED) {
            decodeSignatureParameter2(reqData);
        }
        
        if ((doAction & WSConstants.ENCR) == WSConstants.ENCR) {
            decodeDecryptionParameter(reqData);
        }
        if ((doAction & WSConstants.NO_SERIALIZE) == WSConstants.NO_SERIALIZE) {
            reqData.setNoSerialization(true);
        }
    }

    protected boolean checkReceiverResults(Vector wsResult, Vector actions) {
        int resultActions = wsResult.size();
        int size = actions.size();

        int ai = 0;
        for (int i = 0; i < resultActions; i++) {
            final Integer actInt = (Integer) ((WSSecurityEngineResult) wsResult
                    .get(i)).get(WSSecurityEngineResult.TAG_ACTION);
            int act = actInt.intValue();
            if (act == WSConstants.SC || act == WSConstants.BST) {
                continue;
            }
            if (ai >= size || ((Integer) actions.get(ai++)).intValue() != act) {
                return false;
            }
        }

        if (ai != size) {
            return false;
        }

        return true;
    }
    
    protected boolean checkReceiverResultsAnyOrder(Vector wsResult, Vector actions) {

        java.util.List recordedActions = new Vector(actions.size());
        for (int i = 0; i < actions.size(); i++) {
            Integer action = (Integer)actions.get(i);
            recordedActions.add(action);
        }
        
        for (int i = 0; i < wsResult.size(); i++) {
            final Integer actInt = (Integer) ((WSSecurityEngineResult) wsResult
                    .get(i)).get(WSSecurityEngineResult.TAG_ACTION);
            int act = actInt.intValue();
            if (act == WSConstants.SC || act == WSConstants.BST) {
                continue;
            }
            
            if (!recordedActions.remove(actInt)) {
                return false;
            }
        }

        if (!recordedActions.isEmpty()) {
            return false;
        }

        return true;
    }

    protected void checkSignatureConfirmation(RequestData reqData,
            Vector wsResult) throws WSSecurityException{
        if (doDebug) {
            log.debug("Check Signature confirmation");
        }

        /*
         * First get all Signature values stored during sending the request
         */
        Vector sigv = (Vector) getProperty(reqData.getMsgContext(),
                WSHandlerConstants.SEND_SIGV);
        /*
         * Now get all results that hold a SignatureConfirmation element from
         * the current run of receiver (we can have more than one run: if we
         * have several security header blocks with different actors/roles)
         */
        Vector sigConf = new Vector();
        WSSecurityUtil.fetchAllActionResults(wsResult, WSConstants.SC, sigConf);
        /*
         * now loop over all SignatureConfirmation results and check:
         * - if there is a signature value and no Signature value generated in request: error
         * - if there is a signature value and no matching Signature value found: error
         * 
         *  If a matching value found: remove from vector of stored signature values
         */
        for (int i = 0; i < sigConf.size(); i++) {
            WSSecurityEngineResult result = 
                (WSSecurityEngineResult)sigConf.get(i);
            SignatureConfirmation sc = 
                (SignatureConfirmation)result.get(WSSecurityEngineResult.TAG_SIGNATURE_CONFIRMATION);

            byte[] sigVal = sc.getSignatureValue();
            if (sigVal != null) {
                if (sigv == null || sigv.size() == 0) {
                    // If there are no stored signature values
                    if (sigVal.length != 0) {
                        // If there's no value in the case where there are no
                        // stored SV it is valid. Therefore if there IS a value 
                        // in the sig confirmation element
                        throw new WSSecurityException(
                            "WSHandler: Check Signature confirmation: got a SC element, "
                            + "but no stored SV"
                        );
                    }
                } else {
                    //If we have stored signature values
                    boolean found = false;
                    for (int ii = 0; ii < sigv.size(); ii++) {
                        byte[] storedValue = (byte[]) sigv.get(ii);
                        if (Arrays.equals(sigVal, storedValue)) {
                            found = true;
                            sigv.remove(ii);
                            break;
                        }
                    }
                    if (!found) {
                        throw new WSSecurityException(
                            "WSHandler: Check Signature confirmation: got SC element, "
                            + "but no matching SV"
                        );
                    } 
                }
            }
        }

        /*
         * This indicates this is the last handler: the vector holding the
         * stored Signature values must be empty, otherwise we have an error
         */
        if (!reqData.isNoSerialization()) {
            log.debug("Check Signature confirmation - last handler");
            if (sigv != null && !sigv.isEmpty()) {
                throw new WSSecurityException(
                    "WSHandler: Check Signature confirmation: stored SV vector not empty"
                );
            }
        }
    }
    
    /**
     * Hook to allow subclasses to load their Signature Crypto however they see
     * fit.
     */
    public Crypto loadSignatureCrypto(RequestData reqData) 
        throws WSSecurityException {
        Crypto crypto = null;
        /*
         * Get crypto property file for signature. If none specified throw
         * fault, otherwise get a crypto instance.
         */
        String sigPropFile = 
            getString(WSHandlerConstants.SIG_PROP_FILE, reqData.getMsgContext());
        if (sigPropFile != null) {
            crypto = (Crypto) cryptos.get(sigPropFile);
            if (crypto == null) {
                crypto = 
                    CryptoFactory.getInstance(
                        sigPropFile, this.getClassLoader(reqData.getMsgContext())
                    );
                cryptos.put(sigPropFile, crypto);
            }
        } else if (getString(WSHandlerConstants.SIG_PROP_REF_ID, reqData.getMsgContext()) != null) {
            //
            // If the property file is missing then look for the Properties object 
            //
            String refId = 
                getString(WSHandlerConstants.SIG_PROP_REF_ID, reqData.getMsgContext());
            if (refId != null) {
                Object propObj = getProperty(reqData.getMsgContext(), refId);
                if (propObj instanceof Properties) {
                    crypto = (Crypto) cryptos.get(refId);
                    if (crypto == null) {
                        crypto = CryptoFactory.getInstance((Properties)propObj);
                        cryptos.put(refId, crypto);
                    }
                }
            }
        }
        
        return crypto;
    }

    /**
     * Hook to allow subclasses to load their Encryption Crypto however they
     * see fit.
     */
    protected Crypto loadEncryptionCrypto(RequestData reqData) 
        throws WSSecurityException {
        Crypto crypto = null;
        /*
         * Get encryption crypto property file. If non specified take crypto
         * instance from signature, if that fails: throw fault
         */
        String encPropFile = 
            getString(WSHandlerConstants.ENC_PROP_FILE, reqData.getMsgContext());
        if (encPropFile != null) {
            crypto = (Crypto) cryptos.get(encPropFile);
            if (crypto == null) {
                crypto = 
                    CryptoFactory.getInstance(
                        encPropFile, this.getClassLoader(reqData.getMsgContext())
                    );
                cryptos.put(encPropFile, crypto);
            }
        } else if (getString(WSHandlerConstants.ENC_PROP_REF_ID, reqData.getMsgContext()) != null) {
            //
            // If the property file is missing then look for the Properties object 
            //
            String refId = 
                getString(WSHandlerConstants.ENC_PROP_REF_ID, reqData.getMsgContext());
            if (refId != null) {
                Object propObj = getProperty(reqData.getMsgContext(), refId);
                if (propObj instanceof Properties) {
                    crypto = (Crypto) cryptos.get(refId);
                    if (crypto == null) {
                        crypto = CryptoFactory.getInstance((Properties)propObj);
                        cryptos.put(refId, crypto);
                    }
                }
            }
        } else if (reqData.getSigCrypto() != null) {
            //
            // Default to the signature crypto
            //
            crypto = reqData.getSigCrypto();
        }
        
        return crypto;
    }

    protected void decodeUTParameter(RequestData reqData) 
        throws WSSecurityException {
        Object mc = reqData.getMsgContext();

        String type = getString(WSHandlerConstants.PASSWORD_TYPE, mc);
        if (type != null) {
            if (WSConstants.PW_TEXT.equals(type)) {
                reqData.setPwType(WSConstants.PASSWORD_TEXT);
            } else if (WSConstants.PW_DIGEST.equals(type)) {
                reqData.setPwType(WSConstants.PASSWORD_DIGEST);
            } else if (WSConstants.PW_NONE.equals(type)) {
                // No password requested.
                reqData.setPwType(null);
            } else {
                throw new WSSecurityException("Unknown password type encoding: " + type);
            }
        }

        String add = getString(WSHandlerConstants.ADD_UT_ELEMENTS, mc);
        if (add != null) {
            reqData.setUtElements(StringUtil.split(add, ' '));
        }
        
        String derived = getString(WSHandlerConstants.USE_DERIVED_KEY, mc);
        boolean useDerivedKey = Boolean.valueOf(derived).booleanValue();
        if (useDerivedKey) {
            reqData.setUseDerivedKey(useDerivedKey);
        }
        
        String derivedMAC = getString(WSHandlerConstants.USE_DERIVED_KEY, mc);
        boolean useDerivedKeyForMAC = Boolean.valueOf(derivedMAC).booleanValue();
        if (useDerivedKeyForMAC) {
            reqData.setUseDerivedKeyForMAC(useDerivedKeyForMAC);
        }
        
        String iterations = getString(WSHandlerConstants.DERIVED_KEY_ITERATIONS, mc);
        if (iterations != null) {
            int iIterations = Integer.parseInt(iterations);
            reqData.setDerivedKeyIterations(iIterations);
        }
    }

    protected void decodeSignatureParameter(RequestData reqData) 
        throws WSSecurityException {
        Object mc = reqData.getMsgContext();
        String signatureUser = getString(WSHandlerConstants.SIGNATURE_USER, mc);

        if (signatureUser != null) {
            reqData.setSignatureUser(signatureUser);
        } else {
            reqData.setSignatureUser(reqData.getUsername());
        }
        
        String keyId = getString(WSHandlerConstants.SIG_KEY_ID, mc);
        if (keyId != null) {
            Integer id = (Integer) WSHandlerConstants.keyIdentifier.get(keyId);
            if (id == null) {
                throw new WSSecurityException(
                    "WSHandler: Signature: unknown key identification"
                );
            }
            int tmp = id.intValue();
            if (!(tmp == WSConstants.ISSUER_SERIAL
                    || tmp == WSConstants.BST_DIRECT_REFERENCE
                    || tmp == WSConstants.X509_KEY_IDENTIFIER
                    || tmp == WSConstants.SKI_KEY_IDENTIFIER
                    || tmp == WSConstants.THUMBPRINT_IDENTIFIER
                    || tmp == WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER)) {
                throw new WSSecurityException(
                    "WSHandler: Signature: illegal key identification"
                );
            }
            reqData.setSigKeyId(tmp);
        }
        String algo = getString(WSHandlerConstants.SIG_ALGO, mc);
        reqData.setSigAlgorithm(algo);
        
        String digestAlgo = getString(WSHandlerConstants.SIG_DIGEST_ALGO, mc);
        reqData.setSigDigestAlgorithm(digestAlgo);

        String parts = getString(WSHandlerConstants.SIGNATURE_PARTS, mc);
        if (parts != null) {
            splitEncParts(parts, reqData.getSignatureParts(), reqData);
        }
        
        String secretKeyLength = getString(WSHandlerConstants.WSE_SECRET_KEY_LENGTH, mc);
        if (secretKeyLength != null) {
            int iSecretKeyLength = Integer.parseInt(secretKeyLength);
            reqData.setSecretKeyLength(iSecretKeyLength);
        }
        
        boolean useSingleCert = decodeUseSingleCertificate(reqData);
        reqData.setUseSingleCert(useSingleCert);
    }

    protected void decodeEncryptionParameter(RequestData reqData) 
        throws WSSecurityException {
        Object mc = reqData.getMsgContext();
        String encUser = getString(WSHandlerConstants.ENCRYPTION_USER, mc);

        if (encUser != null) {
            reqData.setEncUser(encUser);
        } else {
            reqData.setEncUser(reqData.getUsername());
        }
        if (reqData.getEncUser() == null) {
            throw new WSSecurityException("WSHandler: Encryption: no username");
        }
        /*
         * String msgType = msgContext.getCurrentMessage().getMessageType(); if
         * (msgType != null && msgType.equals(Message.RESPONSE)) {
         * handleSpecialUser(encUser); }
         */
        handleSpecialUser(reqData);

        /*
         * If the following parameters are no used (they return null) then the
         * default values of WSS4J are used.
         */
        String encKeyId = getString(WSHandlerConstants.ENC_KEY_ID, mc);
        if (encKeyId != null) {
            Integer id = (Integer) WSHandlerConstants.keyIdentifier.get(encKeyId);
            if (id == null) {
                throw new WSSecurityException(
                    "WSHandler: Encryption: unknown key identification"
                );
            }
            int tmp = id.intValue();
            reqData.setEncKeyId(tmp);
            if (!(tmp == WSConstants.ISSUER_SERIAL
                    || tmp == WSConstants.X509_KEY_IDENTIFIER
                    || tmp == WSConstants.SKI_KEY_IDENTIFIER
                    || tmp == WSConstants.BST_DIRECT_REFERENCE
                    || tmp == WSConstants.EMBEDDED_KEYNAME
                    || tmp == WSConstants.THUMBPRINT_IDENTIFIER
                    || tmp == WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER)) {
                throw new WSSecurityException(
                    "WSHandler: Encryption: illegal key identification"
                );
            }
        }
        String encSymAlgo = getString(WSHandlerConstants.ENC_SYM_ALGO, mc);
        reqData.setEncSymmAlgo(encSymAlgo);

        String encKeyTransport = 
            getString(WSHandlerConstants.ENC_KEY_TRANSPORT, mc);
        reqData.setEncKeyTransport(encKeyTransport);
        
        String encSymEncKey = getString(WSHandlerConstants.ENC_SYM_ENC_KEY, mc);
        if (encSymEncKey != null) {
            boolean encSymEndKeyBoolean = Boolean.valueOf(encSymEncKey).booleanValue();
            reqData.setEncryptSymmetricEncryptionKey(encSymEndKeyBoolean);
        }

        String encParts = getString(WSHandlerConstants.ENCRYPTION_PARTS, mc);
        if (encParts != null) {
            splitEncParts(encParts, reqData.getEncryptParts(), reqData);
        }
    }

    protected boolean decodeMustUnderstand(RequestData reqData) 
        throws WSSecurityException {
        String mu = 
            getString(WSHandlerConstants.MUST_UNDERSTAND, reqData.getMsgContext());

        if (mu == null) {
            return true;
        }
        if ("0".equals(mu) || "false".equals(mu)) {
            return false;
        } 
        if ("1".equals(mu) || "true".equals(mu)) {
            return true;
        }

        throw new WSSecurityException(
            "WSHandler: illegal mustUnderstand parameter"
        );
    }

    public int decodeTimeToLive(RequestData reqData) {
        String ttl = 
            getString(WSHandlerConstants.TTL_TIMESTAMP, reqData.getMsgContext());
        int ttl_i = 0;
        if (ttl != null) {
            try {
                ttl_i = Integer.parseInt(ttl);
            } catch (NumberFormatException e) {
                ttl_i = reqData.getTimeToLive();
            }
        }
        if (ttl_i <= 0) {
            ttl_i = reqData.getTimeToLive();
        }
        return ttl_i;
    }

    protected boolean decodeEnableSignatureConfirmation(RequestData reqData) throws WSSecurityException {

        String value = getString(WSHandlerConstants.ENABLE_SIGNATURE_CONFIRMATION,
                reqData.getMsgContext());

        if (value == null) {
            return true;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }

        throw new WSSecurityException(
            "WSHandler: illegal enableSignatureConfirmation parameter"
        );
    }

    protected boolean decodeTimestampPrecision(RequestData reqData) 
        throws WSSecurityException {
        String value = getString(WSHandlerConstants.TIMESTAMP_PRECISION,
                reqData.getMsgContext());

        if (value == null) {
            return true;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }

        throw new WSSecurityException(
            "WSHandler: illegal precisionInMilliSeconds parameter"
        );
    }

    protected boolean decodeCustomPasswordTypes(RequestData reqData) 
        throws WSSecurityException {
        String value = getString(
                WSHandlerConstants.HANDLE_CUSTOM_PASSWORD_TYPES,
                reqData.getMsgContext()
        );

        if (value == null) {
            return false;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }

        throw new WSSecurityException(
            "WSHandler: illegal handleCustomPasswordTypes parameter"
        );
    }
    
    protected boolean decodeUseEncodedPasswords(RequestData reqData) 
        throws WSSecurityException {
        String value = getString(
            WSHandlerConstants.USE_ENCODED_PASSWORDS,
            reqData.getMsgContext()
        );
    
        if (value == null) {
            return false;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }
    
        throw new WSSecurityException(
            "WSHandler: illegal useEncodedPasswords parameter"
        );
    }
    
    protected boolean decodeNamespaceQualifiedPasswordTypes(RequestData reqData) 
        throws WSSecurityException {
        String value = getString(
            WSHandlerConstants.ALLOW_NAMESPACE_QUALIFIED_PASSWORD_TYPES,
            reqData.getMsgContext()
        );
    
        if (value == null) {
            return false;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }
    
        throw new WSSecurityException(
            "WSHandler: illegal allowNamespaceQualifiedPasswordTypes parameter"
        );
    }

    protected boolean decodeTimestampStrict(RequestData reqData) 
        throws WSSecurityException {
        String value = getString(WSHandlerConstants.TIMESTAMP_STRICT,
                reqData.getMsgContext());

        if (value == null) {
            return true;
        }
        if ("0".equals(value) || "false".equals(value)) {
            return false;
        } 
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }

        throw new WSSecurityException(
            "WSHandler: illegal timestampStrict parameter"
        );
    }
    
    protected boolean decodeUseSingleCertificate(RequestData reqData) 
        throws WSSecurityException {
        String useSingleCert = 
            getString(WSHandlerConstants.USE_SINGLE_CERTIFICATE, reqData.getMsgContext());
    
        if (useSingleCert == null) {
            return true;
        }
        if ("0".equals(useSingleCert) || "false".equals(useSingleCert)) {
            return false;
        } 
        if ("1".equals(useSingleCert) || "true".equals(useSingleCert)) {
            return true;
        }
    
        throw new WSSecurityException(
            "WSHandler: illegal useSingleCert parameter"
        );
    }

    /**
     * Get a password to construct a UsernameToken or sign a message.
     * <p/>
     * Try all possible sources to get a password.
     */
    public WSPasswordCallback getPassword(String username,
            int doAction,
            String clsProp,
            String refProp,
            RequestData reqData
    ) throws WSSecurityException {
        WSPasswordCallback pwCb = null;
        Object mc = reqData.getMsgContext();
        String callback = getString(clsProp, mc);
        
        if (callback != null) { 
            // we have a password callback class
            pwCb = readPwViaCallbackClass(callback, username, doAction, reqData);
        } else {
            // Try to obtain a password callback class from the message context or handler options
            CallbackHandler cbHandler = (CallbackHandler) getOption(refProp);
            if (cbHandler == null) {
                cbHandler = (CallbackHandler) getProperty(mc, refProp);
            }
            if (cbHandler != null) {
                pwCb = performCallback(cbHandler, username, doAction);
            } else {
                //
                // If a callback isn't configured then try to get the password
                // from the message context
                //
                String password = getPassword(mc);
                if (password == null) {
                    String err = "provided null or empty password";
                    throw new WSSecurityException("WSHandler: application " + err);
                }
                pwCb = constructPasswordCallback(username, doAction);
                pwCb.setPassword(password);
            }
        }
        
        return pwCb;
    }

    private WSPasswordCallback readPwViaCallbackClass(String callback,
            String username,
            int doAction,
            RequestData requestData
    ) throws WSSecurityException {

        Class cbClass = null;
        CallbackHandler cbHandler = null;
        try {
            cbClass = 
                Loader.loadClass(getClassLoader(requestData.getMsgContext()), callback);
        } catch (ClassNotFoundException e) {
            throw new WSSecurityException(
                "WSHandler: cannot load password callback class: " + callback, e
            );
        }
        try {
            cbHandler = (CallbackHandler) cbClass.newInstance();
        } catch (Exception e) {
            throw new WSSecurityException(
                "WSHandler: cannot create instance of password callback: " + callback, e
            );
        }
        return performCallback(cbHandler, username, doAction);
    }

    /**
     * Perform a callback to get a password.
     * <p/>
     * The called back function gets an indication why to provide a password:
     * to produce a UsernameToken, Signature, or a password (key) for a given
     * name.
     */
    private WSPasswordCallback performCallback(CallbackHandler cbHandler,
            String username,
            int doAction
    ) throws WSSecurityException {

        WSPasswordCallback pwCb = constructPasswordCallback(username, doAction);
        Callback[] callbacks = new Callback[1];
        callbacks[0] = pwCb;
        /*
         * Call back the application to get the password
         */
        try {
            cbHandler.handle(callbacks);
        } catch (Exception e) {
            throw new WSSecurityException("WSHandler: password callback failed", e);
        }
        return pwCb;
    }

    private WSPasswordCallback constructPasswordCallback(
            String username,
            int doAction
    ) throws WSSecurityException {

        int reason = WSPasswordCallback.UNKNOWN;

        switch (doAction) {
        case WSConstants.UT:
        case WSConstants.UT_SIGN:
            reason = WSPasswordCallback.USERNAME_TOKEN;
            break;
        case WSConstants.SIGN:
            reason = WSPasswordCallback.SIGNATURE;
            break;
        case WSConstants.ENCR:
            reason = WSPasswordCallback.KEY_NAME;
            break;
        }
        return new WSPasswordCallback(username, reason);
    }

    private void splitEncParts(String tmpS, Vector parts, RequestData reqData)
        throws WSSecurityException {
        WSEncryptionPart encPart = null;
        String[] rawParts = StringUtil.split(tmpS, ';');

        for (int i = 0; i < rawParts.length; i++) {
            String[] partDef = StringUtil.split(rawParts[i], '}');

            if (partDef.length == 1) {
                if (doDebug) {
                    log.debug("single partDef: '" + partDef[0] + "'");
                }
                encPart =
                    new WSEncryptionPart(partDef[0].trim(),
                            reqData.getSoapConstants().getEnvelopeURI(),
                            "Content");
            } else if (partDef.length == 3) {
                String mode = partDef[0].trim();
                if (mode.length() <= 1) {
                    mode = "Content";
                } else {
                    mode = mode.substring(1);
                }
                String nmSpace = partDef[1].trim();
                if (nmSpace.length() <= 1) {
                    nmSpace = reqData.getSoapConstants().getEnvelopeURI();
                } else {
                    nmSpace = nmSpace.substring(1);
                    if (nmSpace.equals(WSConstants.NULL_NS)) {
                        nmSpace = null;
                    }
                }
                String element = partDef[2].trim();
                if (doDebug) {
                    log.debug(
                        "partDefs: '" + mode + "' ,'" + nmSpace + "' ,'" + element + "'"
                    );
                }
                encPart = new WSEncryptionPart(element, nmSpace, mode);
            } else {
                throw new WSSecurityException("WSHandler: wrong part definition: " + tmpS);
            }
            parts.add(encPart);
        }
    }

    private void handleSpecialUser(RequestData reqData) {
        if (!WSHandlerConstants.USE_REQ_SIG_CERT.equals(reqData.getEncUser())) {
            return;
        }
        Vector results = 
            (Vector) getProperty(reqData.getMsgContext(), WSHandlerConstants.RECV_RESULTS);
        if (results == null) {
            return;
        }
        /*
         * Scan the results for a matching actor. Use results only if the
         * receiving Actor and the sending Actor match.
         */
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult =
                (WSHandlerResult) results.get(i);
            String hActor = rResult.getActor();
            if (!WSSecurityUtil.isActorEqual(reqData.getActor(), hActor)) {
                continue;
            }
            Vector wsSecEngineResults = rResult.getResults();
            /*
             * Scan the results for the first Signature action. Use the
             * certificate of this Signature to set the certificate for the
             * encryption action :-).
             */
            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser =
                    (WSSecurityEngineResult) wsSecEngineResults.get(j);
                int wserAction = 
                    ((java.lang.Integer)wser.get(WSSecurityEngineResult.TAG_ACTION)).intValue();
                if (wserAction == WSConstants.SIGN) {
                    X509Certificate cert = 
                        (X509Certificate)wser.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
                    reqData.setEncCert(cert);
                    return;
                }
            }
        }
    }

    /**
     * Hook to allow subclasses to load their Decryption Crypto however they 
     * see fit.
     */
    protected Crypto loadDecryptionCrypto(RequestData reqData) 
        throws WSSecurityException {

        Crypto crypto = null;
        String decPropFile = 
            getString(WSHandlerConstants.DEC_PROP_FILE, reqData.getMsgContext());
        if (decPropFile != null) {
            crypto = (Crypto) cryptos.get(decPropFile);
            if (crypto == null) {
                crypto = 
                    CryptoFactory.getInstance(
                        decPropFile, this.getClassLoader(reqData.getMsgContext())
                    );
                cryptos.put(decPropFile, crypto);
            }
        } else if (getString(WSHandlerConstants.DEC_PROP_REF_ID, reqData.getMsgContext()) != null) {
            //
            // If the property file is missing then look for the Properties object 
            //
            String refId = 
                getString(WSHandlerConstants.DEC_PROP_REF_ID, reqData.getMsgContext());
            if (refId != null) {
                Object propObj = getProperty(reqData.getMsgContext(), refId);
                if (propObj instanceof Properties) {
                    crypto = (Crypto) cryptos.get(refId);
                    if (crypto == null) {
                        crypto = CryptoFactory.getInstance((Properties)propObj);
                        cryptos.put(refId, crypto);
                    }
                }
            }
        } else if (reqData.getSigCrypto() != null) {
            //
            // Default to the signature crypto
            //
            crypto = reqData.getSigCrypto();
        }
        
        return crypto;
    }

    protected void decodeSignatureParameter2(RequestData reqData) 
        throws WSSecurityException {
        reqData.setSigCrypto(loadSignatureCrypto(reqData));
        /* There are currently no other signature parameters that need 
         * to be handled here, but we call the load crypto hook rather 
         * than just changing the visibility
         * of this method to maintain parity with WSDoAllSender.
         */
    }

    /*
     * Set and check the decryption specific parameters, if necessary
     * take over signature crypto instance.
     */
    protected void decodeDecryptionParameter(RequestData reqData) 
        throws WSSecurityException {
        reqData.setDecCrypto(loadDecryptionCrypto(reqData));
        /* There are currently no other decryption parameters that need 
         * to be handled here, but we call the load crypto hook rather 
         * than just changing the visibility
         * of this method to maintain parity with WSDoAllSender.
         */
    }

    /**
     * Get the password callback class and get an instance
     * <p/>
     */
    protected CallbackHandler getPasswordCB(RequestData reqData) 
        throws WSSecurityException {

        Object mc = reqData.getMsgContext();
        CallbackHandler cbHandler = null;
        String callback = getString(WSHandlerConstants.PW_CALLBACK_CLASS, mc);
        if (callback != null) {
            Class cbClass = null;
            try {
                cbClass = 
                    Loader.loadClass(getClassLoader(reqData.getMsgContext()), callback);
            } catch (ClassNotFoundException e) {
                throw new WSSecurityException(
                    "WSHandler: cannot load password callback class: " + callback, e
                );
            }
            try {
                cbHandler = (CallbackHandler) cbClass.newInstance();
            } catch (java.lang.Exception e) {
                throw new WSSecurityException(
                    "WSHandler: cannot create instance of password callback: " + callback, e
                );
            }
        } else {
            cbHandler = 
                (CallbackHandler) getProperty(mc, WSHandlerConstants.PW_CALLBACK_REF);
            if (cbHandler == null) {
                throw new WSSecurityException(
                    "WSHandler: no reference in callback property"
                );
            }
        }
        return cbHandler;
    }

    /**
     * Evaluate whether a given certificate should be trusted.
     * Hook to allow subclasses to implement custom validation methods however they see fit.
     * <p/>
     * Policy used in this implementation:
     * 1. Search the keystore for the transmitted certificate
     * 2. Search the keystore for a connection to the transmitted certificate
     * (that is, search for certificate(s) of the issuer of the transmitted certificate
     * 3. Verify the trust path for those certificates found because the search for the issuer 
     * might be fooled by a phony DN (String!)
     *
     * @param cert the certificate that should be validated against the keystore
     * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions
     * during CertPathValidation)
     * @throws WSSecurityException
     */
    protected boolean verifyTrust(X509Certificate cert, RequestData reqData) 
        throws WSSecurityException {

        // If no certificate was transmitted, do not trust the signature
        if (cert == null) {
            return false;
        }

        String[] aliases = null;
        String alias = null;
        X509Certificate[] certs;

        String subjectString = cert.getSubjectX500Principal().getName();
        String issuerString = cert.getIssuerX500Principal().getName();
        BigInteger issuerSerial = cert.getSerialNumber();

        if (doDebug) {
            log.debug("WSHandler: Transmitted certificate has subject " + subjectString);
            log.debug(
                "WSHandler: Transmitted certificate has issuer " + issuerString 
                + " (serial " + issuerSerial + ")"
            );
        }

        // FIRST step
        // Search the keystore for the transmitted certificate

        // Search the keystore for the alias of the transmitted certificate
        try {
            alias = reqData.getSigCrypto().getAliasForX509Cert(issuerString, issuerSerial);
        } catch (WSSecurityException ex) {
            throw new WSSecurityException(
                "WSHandler: Could not get alias for certificate with " + subjectString, ex
            );
        }

        if (alias != null) {
            // Retrieve the certificate for the alias from the keystore
            try {
                certs = reqData.getSigCrypto().getCertificates(alias);
            } catch (WSSecurityException ex) {
                throw new WSSecurityException(
                    "WSHandler: Could not get certificates for alias " + alias, ex
                );
            }

            // If certificates have been found, the certificates must be compared
            // to ensure against phony DNs (compare encoded form including signature)
            if (certs != null && certs.length > 0 && cert.equals(certs[0])) {
                if (doDebug) {
                    log.debug("Direct trust for certificate with " + subjectString);
                }
                return true;
            }
        } else {
            if (doDebug) {
                log.debug(
                    "No alias found for subject from issuer with " + issuerString 
                    + " (serial " + issuerSerial + ")"
                );
            }
        }

        // SECOND step
        // Search for the issuer of the transmitted certificate in the keystore

        // Search the keystore for the alias of the transmitted certificates issuer
        try {
            aliases = reqData.getSigCrypto().getAliasesForDN(issuerString);
        } catch (WSSecurityException ex) {
            throw new WSSecurityException(
                "WSHandler: Could not get alias for certificate with " + issuerString, ex
            );
        }

        // If the alias has not been found, the issuer is not in the keystore
        // As a direct result, do not trust the transmitted certificate
        if (aliases == null || aliases.length < 1) {
            if (doDebug) {
                log.debug(
                    "No aliases found in keystore for issuer " + issuerString 
                    + " of certificate for " + subjectString
                );
            }
            return false;
        }

        // THIRD step
        // Check the certificate trust path for every alias of the issuer found in the keystore
        for (int i = 0; i < aliases.length; i++) {
            alias = aliases[i];

            if (doDebug) {
                log.debug(
                    "Preparing to validate certificate path with alias " + alias 
                    + " for issuer " + issuerString
                );
            }

            // Retrieve the certificate(s) for the alias from the keystore
            try {
                certs = reqData.getSigCrypto().getCertificates(alias);
            } catch (WSSecurityException ex) {
                throw new WSSecurityException(
                    "WSHandler: Could not get certificates for alias " + alias, ex
                );
            }

            // If no certificates have been found, there has to be an error:
            // The keystore can find an alias but no certificate(s)
            if (certs == null || certs.length < 1) {
                throw new WSSecurityException(
                    "WSHandler: Could not get certificates for alias " + alias
                );
            }

            // Form a certificate chain from the transmitted certificate
            // and the certificate(s) of the issuer from the keystore
            // First, create new array
            X509Certificate[] x509certs = new X509Certificate[certs.length + 1];
            // Then add the first certificate ...
            x509certs[0] = cert;
            // ... and the other certificates
            for (int j = 0; j < certs.length; j++) {
                x509certs[j + 1] = certs[j];
            }
            certs = x509certs;

            // Use the validation method from the crypto to check whether the subjects' 
            // certificate was really signed by the issuer stated in the certificate
            try {
                if (reqData.getSigCrypto().validateCertPath(certs)) {
                    if (doDebug) {
                        log.debug(
                            "WSHandler: Certificate path has been verified for certificate "
                            + "with subject " + subjectString
                        );
                    }
                    return true;
                }
            } catch (WSSecurityException ex) {
                throw new WSSecurityException(
                    "WSHandler: Certificate path verification failed for certificate "
                    + "with subject " + subjectString, ex
                );
            }
        }

        if (doDebug) {
            log.debug(
                "WSHandler: Certificate path could not be verified for "
                + "certificate with subject " + subjectString
            );
        }
        return false;
    }
    
    /**
     * Evaluate whether the given certificate chain should be trusted.
     * 
     * @param certificates the certificate chain that should be validated against the keystore
     * @return true if the certificate chain is trusted, false if not
     * @throws WSSecurityException
     */
    protected boolean verifyTrust(X509Certificate[] certificates, RequestData reqData) 
        throws WSSecurityException {
        
        // If no certificate was transmitted, do not trust the signature
        if (certificates == null) {
            return false;
        }
        
        String subjectString = certificates[0].getSubjectX500Principal().getName();
        //
        // Use the validation method from the crypto to check whether the subjects' 
        // certificate was really signed by the issuer stated in the certificate
        //
        try {
            if (certificates != null && certificates.length > 1
                && reqData.getSigCrypto().validateCertPath(certificates)) {
                if (log.isDebugEnabled()) {
                    log.debug(
                        "Certificate path has been verified for certificate with subject " 
                        + subjectString
                    );
                }
                return true;
            }
        } catch (WSSecurityException ex) {
            throw new WSSecurityException(
                "WSHandler: Certificate path verification failed for certificate "
                + "with subject " + subjectString, ex
            );
        }
        
        if (log.isDebugEnabled()) {
            log.debug(
                "Certificate path could not be verified for certificate with subject " 
                + subjectString
            );
        }
            
        return false;
    }

    /**
     * Evaluate whether a timestamp is considered valid on the receivers' side. Hook to
     * allow subclasses to implement custom validation methods however they see fit.
     * 
     * Policy used in this implementation:
     * 
     * 1. The receiver can set its own time to live (besides from that set on
     * sender side)
     * 
     * 2. If the message was created before (now-ttl) the message is rejected
     * 
     * @param timestamp
     *            the timestamp that is validated
     * @param timeToLive
     *            the limit on the receivers' side, that the timestamp is validated against
     * @return true if the timestamp is before (now-timeToLive), false otherwise
     * @throws WSSecurityException
     */
    protected boolean verifyTimestamp(Timestamp timestamp, int timeToLive) throws WSSecurityException {

        // Calculate the time that is allowed for the message to travel
        Calendar validCreation = Calendar.getInstance();
        Calendar cre = timestamp.getCreated();
        if (cre != null && cre.after(validCreation)) {
            if (doDebug) {
                log.debug("Validation of Timestamp: The message was created in the future!");
            }
            return false;
        }
        long currentTime = validCreation.getTime().getTime();
        currentTime -= timeToLive * 1000;
        validCreation.setTime(new Date(currentTime));

        if (doDebug) {
            log.debug("Preparing to verify the timestamp");
            DateFormat zulu = new XmlSchemaDateFormat();
            log.debug("Validation of Timestamp: Current time is "
                    + zulu.format(Calendar.getInstance().getTime()));
            log.debug("Validation of Timestamp: Valid creation is "
                    + zulu.format(validCreation.getTime()));
            if (timestamp.getCreated() != null) {
                log.debug("Validation of Timestamp: Timestamp created is "
                        + zulu.format(timestamp.getCreated().getTime()));
            }
        }
        // Validate the time it took the message to travel
        // if (timestamp.getCreated().before(validCreation) ||
        // !timestamp.getCreated().equals(validCreation)) {
        if (cre != null && !cre.after(validCreation)) {
            if (doDebug) {
                log.debug("Validation of Timestamp: The message was created too long ago");
            }
            return false;
        }

        if (doDebug) {
            log.debug("Validation of Timestamp: Everything is ok");
        }
        return true;
    }

    /**
     * Looks up key first via {@link #getOption(String)} and if not found
     * there, via {@link #getProperty(Object, String)}
     *
     * @param key the key to search for. May not be null.
     * @param mc the message context to search. 
     * @return the value found.
     * @throws IllegalArgumentException if <code>key</code> is null.
     */
    public String getString(String key, Object mc) { 
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String s = getStringOption(key);
        if (s != null) {
            return s;
        }
        if (mc == null) {
            throw new IllegalArgumentException("Message context cannot be null");
        }
        return (String) getProperty(mc, key);
    }


    /**
     * Returns the option on <code>name</code>.
     *
     * @param key the non-null key of the option.
     * @return the option on <code>key</code> if <code>key</code>
     *  exists and is of type java.lang.String; otherwise null.
     */
    public String getStringOption(String key) {
        Object o = getOption(key);
        if (o instanceof String){
            return (String) o;
        } else {
            return null;
        }
    }

    /**
     * Returns the classloader to be used for loading the callback class
     * @param msgCtx The MessageContext 
     * @return class loader
     */
    public ClassLoader getClassLoader(Object msgCtx) {
        try {
            return Loader.getTCL();
        } catch (Throwable t) {
            return null;
        }
    }

    public abstract Object getOption(String key);
    public abstract Object getProperty(Object msgContext, String key);

    public abstract void setProperty(Object msgContext, String key, 
            Object value);


    public abstract String getPassword(Object msgContext);

    public abstract void setPassword(Object msgContext, String password);
}
