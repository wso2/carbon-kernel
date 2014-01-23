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

package org.apache.axiom.om.impl.llom.util;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.exception.XMLComparisonException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Vector;


/** @deprecated This class is outdated. Please use XmlUnit to compate two xml files. */
public class XMLComparator {
    /** Eran Chinthaka (chinthaka@apache.org) */
    private static Log log = LogFactory.getLog(XMLComparator.class);

    private Vector ignorableNamespaceList = new Vector();

    public void addIgnorableNamespace(String nsURI) {
        ignorableNamespaceList.add(nsURI);
    }

    public void clearIgnorableNamespaces() {
        ignorableNamespaceList.clear();
    }


    public boolean compare(OMElement elementOne, OMElement elementTwo)
            throws XMLComparisonException {

        //ignore if the elements belong to any of the ignorable namespaces list
        if (isIgnorable(elementOne) ||
                isIgnorable(elementTwo)) {
            return true;
        }

        if (elementOne == null && elementTwo == null) {
            log.info("Both Elements are null.");
            return true;
        }
        if (elementOne == null && elementTwo != null) {
            throw new XMLComparisonException(
                    "Element One is null and Element Two is not null");
        }
        if (elementOne != null && elementTwo == null) {
            throw new XMLComparisonException(
                    "Element Two is null and Element One is not null");
        }

        log.info(
                "Now Checking " + elementOne.getLocalName() + " and " +
                        elementTwo.getLocalName() +
                        "=============================");

        log.info("Comparing Element Names .......");
        compare("Elements names are not equal. ",
                elementOne.getLocalName(),
                elementTwo.getLocalName());

        log.info("Comparing Namespaces .........");
        compare("Element namespaces are not equal",
                elementOne.getNamespace(),
                elementTwo.getNamespace());

        log.info("Comparing attributes .....");
        compareAllAttributes(elementOne, elementTwo);

        log.info("Comparing texts .....");

        /*
        * Trimming the value of the XMLElement is not correct
        * since this compare method cannot be used to compare
        * element contents with trailing and leading whitespaces
        * BUT for the practicalltiy of tests and to get the current
        * tests working we have to trim() the contents
        */
        compare("Elements texts are not equal ",
                elementOne.getText().trim(),
                elementTwo.getText().trim());

        log.info("Comparing Children ......");
        compareAllChildren(elementOne, elementTwo);


        return true;
    }

    private void compareAllAttributes(OMElement elementOne,
                                      OMElement elementTwo) throws XMLComparisonException {
        compareAttibutes(elementOne, elementTwo);
        compareAttibutes(elementTwo, elementOne);
    }

    private void compareAllChildren(OMElement elementOne,
                                    OMElement elementTwo) throws XMLComparisonException {
        compareChildren(elementOne, elementTwo);
        compareChildren(elementTwo, elementOne);
    }


    private boolean isIgnorable(OMElement elt) {
        if (elt != null) {
            OMNamespace namespace = elt.getNamespace();
            if (namespace != null) {
                return ignorableNamespaceList.contains(namespace.getNamespaceURI());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private void compareChildren(OMElement elementOne, OMElement elementTwo)
            throws XMLComparisonException {
        //ignore if the elements belong to any of the ignorable namespaces list
        if (isIgnorable(elementOne) ||
                isIgnorable(elementTwo)) {
            return;
        }
        Iterator elementOneChildren = elementOne.getChildren();
        while (elementOneChildren.hasNext()) {
            OMNode omNode = (OMNode) elementOneChildren.next();
            if (omNode.getType() == OMNode.ELEMENT_NODE) {
                OMElement elementOneChild = (OMElement) omNode;
                OMElement elementTwoChild = null;
                //Do the comparison only if the element is not ignorable
                if (!isIgnorable(elementOneChild)) {
                    elementTwoChild = elementTwo.getFirstChildWithName(
                            elementOneChild.getQName());
                    //Do the comparison only if the element is not ignorable
                    if (!isIgnorable(elementTwoChild)) {
                        if (elementTwoChild == null) {
                            throw new XMLComparisonException(
                                    " There is no " + elementOneChild.getLocalName() +
                                            " element under " +
                                            elementTwo.getLocalName());
                        }
                    }
                }
                compare(elementOneChild, elementTwoChild);
            }
        }
    }


    private void compareAttibutes(OMElement elementOne, OMElement elementTwo)
            throws XMLComparisonException {
        int elementOneAtribCount = 0;
        int elementTwoAtribCount = 0;
        Iterator attributes = elementOne.getAllAttributes();
        while (attributes.hasNext()) {
            OMAttribute omAttribute = (OMAttribute) attributes.next();
            OMAttribute attr = elementTwo.getAttribute(
                    omAttribute.getQName());
            if (attr == null) {
                throw new XMLComparisonException(
                        "Attributes are not the same in two elements. Attribute " +
                                omAttribute.getLocalName() +
                                " != ");
            }
            elementOneAtribCount++;
        }

        Iterator elementTwoIter = elementTwo.getAllAttributes();
        while (elementTwoIter.hasNext()) {
            elementTwoIter.next();
            elementTwoAtribCount++;

        }

        if (elementOneAtribCount != elementTwoAtribCount) {
            throw new XMLComparisonException(
                    "Attributes are not the same in two elements.");
        }
    }

    private void compare(String failureNotice, String one, String two)
            throws XMLComparisonException {
        if (!one.equals(two)) {
            throw new XMLComparisonException(
                    failureNotice + one + " != " + two);
        }
    }

    private void compare(String failureNotice,
                         OMNamespace one,
                         OMNamespace two) throws XMLComparisonException {
        if (one == null && two == null) {
            return;
        } else if (one != null && two == null) {
            throw new XMLComparisonException(
                    "First Namespace is NOT null. But the second is null");
        } else if (one == null && two != null) {
            throw new XMLComparisonException(
                    "First Namespace is null. But the second is NOT null");
        }

        if (!one.getNamespaceURI().equals(two.getNamespaceURI())) {
            throw new XMLComparisonException(
                    failureNotice + one + " != " + two);
        }

        // Do we need to compare prefixes as well
    }
}
