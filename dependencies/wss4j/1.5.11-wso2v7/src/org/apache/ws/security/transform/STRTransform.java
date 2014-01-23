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

package org.apache.ws.security.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSDocInfoStore;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.apache.xml.security.utils.XMLUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Class STRTransform
 * 
 * @author Werner Dittmann (Werner.Dittmann@siemens.com)
 * @version 1.0
 */
public class STRTransform extends TransformSpi {

    /**
     * Field implementedTransformURI
     */
    public static final String implementedTransformURI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform";

    private static Log log = LogFactory.getLog(STRTransform.class.getName());

    private static boolean doDebug = false;

    private static String XMLNS = "xmlns=";

    private WSDocInfo wsDocInfo = null;
    
    public boolean wantsOctetStream() {
        return false;
    }

    public boolean wantsNodeSet() {
        return true;
    }

    public boolean returnsOctetStream() {
        return true;
    }

    public boolean returnsNodeSet() {
        return false;
    }

    /**
     * Method engineGetURI
     */
    protected String engineGetURI() {
        return STRTransform.implementedTransformURI;
    }
    
    /**
     * {@inheritDoc}
     */
    protected XMLSignatureInput enginePerformTransform(
            XMLSignatureInput input, OutputStream os, Transform _transformObject)
            throws IOException,
                   CanonicalizationException, InvalidCanonicalizerException,
                   TransformationException, ParserConfigurationException,
                   SAXException {
            return enginePerformTransform(input, _transformObject);
    }

    /**
     * Method enginePerformTransform
     * 
     * @param input
     * @throws CanonicalizationException
     * @throws InvalidCanonicalizerException
     */
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, 
                                                       Transform transformObject)
        throws IOException, CanonicalizationException, InvalidCanonicalizerException {
        doDebug = log.isDebugEnabled();

        if (doDebug) {
            log.debug("Beginning STRTransform..." + input.toString());
        }

        try {
            //
            // Get the main document, that is the complete SOAP request document
            //
            Document thisDoc = transformObject.getDocument();

            //
            // Here we get some information about the document that is being
            // processed, in particular the crypto implementation, and already
            // detected BST that may be used later during dereferencing.
            //
            wsDocInfo = WSDocInfoStore.lookup(thisDoc);
            if (wsDocInfo == null) {
                throw (new CanonicalizationException("no WSDocInfo found"));
            }
            //
            // According to the OASIS WS Specification "Web Services Security:
            // SOAP Message Security 1.0" Monday, 19 January 2004, chapter 8.3
            // describes that the input node set must be processed by the c14n
            // that is specified in the argument element of the STRTransform
            // element.
            // 
            // First step: Get the required c14n argument and get the specified
            // Canonicalizer
            //
            String canonAlgo = null;
            if (transformObject.length(
                WSConstants.WSSE_NS, "TransformationParameters") == 1) {
                Element tmpE = 
                    XMLUtils.selectNode(
                        transformObject.getElement().getFirstChild(), 
                        WSConstants.WSSE_NS,
                        "TransformationParameters", 
                        0
                    );
                Element canonElem = 
                    (Element) WSSecurityUtil.getDirectChild(
                        tmpE, "CanonicalizationMethod", WSConstants.SIG_NS
                    );
                canonAlgo = canonElem.getAttribute("Algorithm");
                if (doDebug) {
                    log.debug("CanonAlgo: " + canonAlgo);
                }
            }
            Canonicalizer canon = Canonicalizer.getInstance(canonAlgo);

            ByteArrayOutputStream bos = null;
            byte[] buf = null;
            if (doDebug) {
                buf = input.getBytes();
                bos = new ByteArrayOutputStream(buf.length);
                bos.write(buf, 0, buf.length);
                log.debug("canon bos: " + bos.toString());
            }

            //
            // Get the input (node) to transform. Currently we support only an
            // Element as input format. If other formats are required we must
            // get it as bytes and probably reparse it into a DOM tree (How to
            // work with nodesets? how to select the right node from a nodeset?)
            //
            Element str = null;
            if (input.isElement()) {
                str = (Element) input.getSubNode();
            } else {
                throw new CanonicalizationException(
                    "Wrong input format - only element input supported"
                );
            }

            if (doDebug) {
                log.debug("STR: " + str.toString());
            }
            //
            // The element to transform MUST be a SecurityTokenReference
            // element.
            //
            SecurityTokenReference secRef = new SecurityTokenReference(str);
            //
            // Third and forth step are performed by derefenceSTR()
            //
            Element dereferencedToken = STRTransformUtil.dereferenceSTR(
                    thisDoc, secRef, wsDocInfo);
            //
            // C14n with specified algorithm. According to WSS Specification.
            //
            buf = canon.canonicalizeSubtree(dereferencedToken, "#default");
            if (doDebug) {
                bos = new ByteArrayOutputStream(buf.length);
                bos.write(buf, 0, buf.length);
                log.debug("after c14n: " + bos.toString());
            }

            //
            // Alert: Hacks ahead According to WSS spec an Apex node must
            // contain a default namespace. If none is availabe in the first
            // node of the c14n output (this is the apex element) then we do
            // some editing to insert an empty default namespace
            // 
            // TODO: Rework theses hacks after c14n was updated and can be
            // instructed to insert empty default namespace if required
            //
            // If the problem with c14n method is solved then just do:
            // return new XMLSignatureInput(buf);
            
            // start of HACK
            StringBuffer bf = new StringBuffer(new String(buf));
            String bf1 = bf.toString();

            //
            // Find start and end of first element <....>, this is the Apex node
            //
            int gt = bf1.indexOf(">");
            //
            // Lookup the default namespace
            //
            int idx = bf1.indexOf(XMLNS);
            //
            // If none found or if it is outside of this (Apex) element look for
            // first blank in, insert default namespace there (this is the
            // correct place according to c14n specification)
            //
            if (idx < 0 || idx > gt) {
                idx = bf1.indexOf(" ");
                bf.insert(idx + 1, "xmlns=\"\" ");
                bf1 = bf.toString();
            }
            if (doDebug) {
                log.debug("last result: ");
                log.debug(bf1);
            }
            return new XMLSignatureInput(bf1.getBytes());
        }
        // End of HACK
        catch (WSSecurityException ex) {
            log.debug(ex.getMessage(), ex);
            throw (new CanonicalizationException("c14n.Canonicalizer.Exception", ex));
        }
    }    
}
