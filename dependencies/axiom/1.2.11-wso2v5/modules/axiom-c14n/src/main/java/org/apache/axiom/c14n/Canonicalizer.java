/*
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

package org.apache.axiom.c14n;


import org.apache.axiom.c14n.exceptions.AlgorithmAlreadyRegisteredException;
import org.apache.axiom.c14n.exceptions.CanonicalizationException;
import org.apache.axiom.c14n.exceptions.InvalidCanonicalizerException;
import org.apache.axiom.c14n.omwrapper.factory.WrapperFactory;
import org.apache.axiom.om.OMElement;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class Canonicalizer {

    /**
     * The URL defined in XML-SEC Rec for inclusive org.apache.axiom.c14n.impl <b>without</b> comments.
     */
    public static final String ALGO_ID_C14N_OMIT_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    /**
     * The URL defined in XML-SEC Rec for inclusive org.apache.axiom.c14n.impl <b>with</b> comments.
     */
    public static final String ALGO_ID_C14N_WITH_COMMENTS = ALGO_ID_C14N_OMIT_COMMENTS + "#WithComments";
    /**
     * The URL defined in XML-SEC Rec for exclusive org.apache.axiom.c14n.impl <b>without</b> comments.
     */
    public static final String ALGO_ID_C14N_EXCL_OMIT_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#";
    /**
     * The URL defined in XML-SEC Rec for exclusive org.apache.axiom.c14n.impl <b>with</b> comments.
     */
    public static final String ALGO_ID_C14N_EXCL_WITH_COMMENTS = ALGO_ID_C14N_EXCL_OMIT_COMMENTS + "WithComments";

    static boolean _alreadyInitialized = false;
    static Map _canonicalizerHash = null;

    protected CanonicalizerSpi canonicalizerSpi = null;

    /**
     * Method init
     */
    public static synchronized void init() {

        if (!Canonicalizer._alreadyInitialized) {
            Canonicalizer._canonicalizerHash = new HashMap(10);
            Canonicalizer._alreadyInitialized = true;
        }
    }

    /**
     * Constructor Canonicalizer
     *
     * @param algorithmURI
     * @throws org.apache.axiom.c14n.exceptions.InvalidCanonicalizerException
     *
     */
    private Canonicalizer(String algorithmURI)
            throws InvalidCanonicalizerException {

        try {
            Class implementingClass = getImplementingClass(algorithmURI);
            this.canonicalizerSpi = (CanonicalizerSpi) implementingClass.newInstance();
            this.canonicalizerSpi.reset = true;
        } catch (Exception e) {
            Object exArgs[] = {algorithmURI};
            throw new InvalidCanonicalizerException(
                    "c14n.Canonicalizer.UnknownCanonicalizer", exArgs);
        }
    }

    /**
     * Method getInstance
     *
     * @param algorithmURI
     * @return an appropriate Canonicalizer instance
     * @throws InvalidCanonicalizerException
     */
    public static synchronized final Canonicalizer getInstance(String algorithmURI)
            throws InvalidCanonicalizerException {

        Canonicalizer canonicalizer = new Canonicalizer(algorithmURI);

        return canonicalizer;
    }

    /**
     * Method register
     *
     * @param algorithmURI
     * @param implementingClass
     * @throws AlgorithmAlreadyRegisteredException
     *
     */
    public static synchronized void register(String algorithmURI, String implementingClass)
            throws AlgorithmAlreadyRegisteredException {

        // check whether URI is already registered
        Class registeredClass = getImplementingClass(algorithmURI);

        if (registeredClass != null) {
            Object exArgs[] = {algorithmURI, registeredClass};
                throw new AlgorithmAlreadyRegisteredException(
                        "algorithm.alreadyRegistered", exArgs);
        }

        try {
            _canonicalizerHash.put(algorithmURI, Class.forName(implementingClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(implementingClass + " not found");
        }
    }

    /**
     * Method engineGetURI()
     *
     * @return the URI of the canonicalization method
     */
    public final String getURI() {
        return this.canonicalizerSpi.engineGetURI();
    }

    /**
     * Method engineGetIncludeComments
     *
     * @return true if comments are included or false otherwise
     */
    public boolean getIncludeComments() {
        return this.canonicalizerSpi.engineGetIncludeComments();
    }

    /**
     * Method canonicalize
     *
     * @param inputBytes
     * @return the canonicalized array of bytes
     * @throws CanonicalizationException
     */
    public byte[] canonicalize(byte[] inputBytes) throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalize(inputBytes);
    }

    /**
     * Canonicalizes the subtree rooted by <CODE>element</CODE>.
     *
     * @param element The element to canicalize
     * @return the result of the org.apache.axiom.c14n.impl.
     * @throws CanonicalizationException
     */
    public byte[] canonicalizeSubtree(OMElement element) throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeSubTree(
                new WrapperFactory().getNode(element));
    }

    /**
     * Canonicalizes the subtree rooted by <CODE>element</CODE>.
     *
     * @param element
     * @param inclusiveNamespaces
     * @return the result of the org.apache.axiom.c14n.impl.
     * @throws CanonicalizationException
     */
    public byte[] canonicalizeSubtree(OMElement element, String inclusiveNamespaces)
            throws CanonicalizationException {
        return this.canonicalizerSpi.engineCanonicalizeSubTree(new WrapperFactory().getNode(element),
                inclusiveNamespaces);
    }

    /**
     * Sets the writter where the cannocalization ends. ByteArrayOutputStream if
     * none is setted.
     *
     * @param os
     */
    public void setWriter(OutputStream os) {
        this.canonicalizerSpi.setWriter(os);
    }

    /**
     * Method getImplementingCanonicalizerClss
     *
     * @return the name of the implementing {@link CanonicalizerSpi} class
     */
    public String getImplementingCanonicalizerClass() {
        return this.canonicalizerSpi.getClass().getName();
    }

    /**
     * Method getImplementingClass
     *
     * @param URI
     * @return the class implementing the given URI
     */
    private static Class getImplementingClass(String URI) {
        return (Class) _canonicalizerHash.get(URI);
    }

    /**
     * Set the canonicalizator behaviour to not reset.
     */
    public void notReset() {
        this.canonicalizerSpi.reset = false;
    }
}
