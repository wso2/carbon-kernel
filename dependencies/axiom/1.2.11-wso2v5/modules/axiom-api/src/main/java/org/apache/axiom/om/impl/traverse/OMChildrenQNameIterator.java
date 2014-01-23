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

package org.apache.axiom.om.impl.traverse;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;

import javax.xml.namespace.QName;

/** 
 * Class OMChildrenQNameIterator
 * 
 * This iterator returns the elements that have a matching QName.
 * This class can be extended to customize the QName equality.
 *
 */
public class OMChildrenQNameIterator extends OMFilterIterator {
    /** Field givenQName */
    private final QName givenQName;

    /**
     * Constructor OMChildrenQNameIterator.
     *
     * @param currentChild
     * @param givenQName
     */
    public OMChildrenQNameIterator(OMNode currentChild, QName givenQName) {
        super(new OMChildrenIterator(currentChild));
        this.givenQName = givenQName;
    }
    
    /**
     * Returns true if the qnames are equal.
     * The default algorithm is to use the QName equality (which examines the namespace and localPart).
     * You can extend this class to provide your own equality algorithm.
     * @param searchQName
     * @param currentQName
     * @return true if qnames are equal.
     */
    public boolean isEqual(QName searchQName, QName currentQName) {
        return searchQName.equals(currentQName);
    }

    protected boolean matches(OMNode node) {
        if (node.getType() == OMNode.ELEMENT_NODE) {
            QName thisQName = ((OMElement)node).getQName();
            // A null givenName is an indicator to return all elements
            return givenQName == null || isEqual(givenQName, thisQName);
        } else {
            return false;
        }
    }

    /**
     * Prior versions of the OMChildrenQNameIterator used the following
     * logic to check equality.  This algorithm is incorrect; however some customers
     * have dependency on this behavior.  This method is retained (but deprecated) to allow
     * them an opportunity to use the old algorithm.
     * 
     * @param searchQName
     * @param currentQName
     * @return true using legacy equality match
     * @deprecated
     */
    public static boolean isEquals_Legacy(QName searchQName, QName currentQName) {
        
        // if the given localname is null, whatever value this.qname has, its a match. But can one give a QName without a localName ??
        String localPart = searchQName.getLocalPart();
        boolean localNameMatch =(localPart == null) || (localPart.equals("")) ||
            ((currentQName != null) && currentQName.getLocalPart().equals(localPart));
        String namespaceURI = searchQName.getNamespaceURI();
        boolean namespaceURIMatch = (namespaceURI == null) || (namespaceURI.equals(""))||
            ((currentQName != null) && currentQName.getNamespaceURI().equals(namespaceURI));
        return localNameMatch && namespaceURIMatch;
    }
    
    
}
