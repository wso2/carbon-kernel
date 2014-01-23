
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

import org.apache.axiom.c14n.exceptions.CanonicalizationException;
import org.apache.axiom.c14n.omwrapper.factory.WrapperFactory;
import org.apache.axiom.c14n.omwrapper.interfaces.Node;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public abstract class CanonicalizerSpi {
    protected boolean reset = false;

    /**
     * Method engineGetURI()
     *
     * @return the URI of the canonicalization method
     */
    public abstract String engineGetURI();

    /**
     * Method engineGetIncludeComments
     *
     * @return true if comments are included or false otherwise
     */
    public abstract boolean engineGetIncludeComments();

    /**
     * Method setWriter
     * @param os
     */
    public abstract void setWriter(OutputStream os);

    public byte[] engineCanonicalize(byte[] inputBytes) throws CanonicalizationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        StAXOMBuilder builder = null;
        try {
            builder = new StAXOMBuilder(bais);
        } catch (XMLStreamException e) {
            throw new CanonicalizationException(e);
        }
        byte [] result = this.engineCanonicalizeSubTree(
                new WrapperFactory().getNode(builder.getDocument()));
        return result;
    }

    public abstract byte[] engineCanonicalizeSubTree(Node node)
            throws CanonicalizationException;

    public abstract byte[] engineCanonicalizeSubTree(Node node, String inclusiveNamespaces)
            throws CanonicalizationException;
}
