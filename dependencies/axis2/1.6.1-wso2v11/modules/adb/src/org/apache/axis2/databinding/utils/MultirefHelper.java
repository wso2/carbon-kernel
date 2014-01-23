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

package org.apache.axis2.databinding.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.engine.ObjectSupplier;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultirefHelper {

    public static final String SOAP12_REF_ATTR = "ref";
    public static final String SOAP11_REF_ATTR = "href";

    private boolean filledTable;

    private OMElement parent;

    private HashMap objectmap = new HashMap();
    private HashMap elementMap = new HashMap();
    private HashMap omElementMap = new HashMap();

    public MultirefHelper(OMElement parent) {
        this.parent = parent;
    }

    public Object getObject(String id) {
        return objectmap.get(id);
    }

    public OMElement getOMElement(String id) {
        return (OMElement)omElementMap.get(id);
    }

    public OMElement processOMElementRef(String id) throws AxisFault {
        if (!filledTable) {
            readallChildElements();
        }
        OMElement val = (OMElement)elementMap.get(id);
        if (val == null) {
            throw new AxisFault("Invalid reference :" + id);
        } else {
            OMElement ele = processElementforRefs(val);
            OMElement cloneele = elementClone(ele);
            omElementMap.put(id, cloneele);
            return cloneele;
        }
    }

    public OMElement processElementforRefs(OMElement elemnts) throws AxisFault {
        Iterator itr = elemnts.getChildElements();
        while (itr.hasNext()) {
            OMElement omElement = (OMElement)itr.next();
            OMAttribute attri = processRefAtt(omElement);
            if (attri != null) {
                String ref = getAttvalue(attri);
                OMElement tempele = getOMElement(ref);
                if (tempele == null) {
                    tempele = processOMElementRef(ref);
                }
                OMElement ele2 = elementClone(tempele);
                Iterator itrChild = ele2.getChildren();
                while (itrChild.hasNext()) {
                    Object obj = itrChild.next();
                    if (obj instanceof OMNode) {
                        omElement.addChild((OMNode)obj);
                    }
                }
            }
        }
        return elemnts;
    }

    private OMElement elementClone(OMElement ele) {
        return new StAXOMBuilder(ele.getXMLStreamReader()).getDocumentElement();
    }

    public Object processRef(Class javatype, String id, ObjectSupplier objectSupplier)
            throws AxisFault {
        if (!filledTable) {
            readallChildElements();
        }
        OMElement val = (OMElement)elementMap.get(id);
        if (val == null) {
            throw new AxisFault("Invalid reference :" + id);
        } else {
            if (SimpleTypeMapper.isSimpleType(javatype)) {
                /**
                 * in this case OM element can not contains more child, that is no way to get
                 * the value as an exp ,
                 * <refernce id="12">
                 *   <value>foo</value>
                 * </refernce>
                 * the above one is not valid , that should always be like below
                 * <refernce id="12">foo</refernce>
                 */
                Object valObj = SimpleTypeMapper.getSimpleTypeObject(javatype, val);
                objectmap.put(id, valObj);
                return valObj;
            } else if (SimpleTypeMapper.isCollection(javatype)) {
                Object valobj = SimpleTypeMapper.getArrayList(val);
                objectmap.put(id, valobj);
                return valobj;
            } else {
                Object obj = BeanUtil.deserialize(javatype, val, this, objectSupplier);
                objectmap.put(id, obj);
                return obj;
            }
        }
    }

    private void readallChildElements() {
        Iterator childs = parent.getChildElements();
        while (childs.hasNext()) {
            OMElement omElement = (OMElement)childs.next();
            OMAttribute id = omElement.getAttribute(new QName("id"));
            if (id != null) {
                omElement.build();
                elementMap.put(id.getAttributeValue(), omElement.detach());
            }
        }
        filledTable = true;
    }

    public static String getAttvalue(OMAttribute omatribute) {
        String ref;
        ref = omatribute.getAttributeValue();
        if (ref != null) {
            if (ref.charAt(0) == '#') {
                ref = ref.substring(1);
            }
        }
        return ref;
    }

    public static OMAttribute processRefAtt(OMElement omElement) {
        OMAttribute omatribute = omElement.getAttribute(new QName(SOAP11_REF_ATTR));
        if (omatribute == null) {
            omatribute = omElement.getAttribute(new QName(SOAP12_REF_ATTR));
        }
        return omatribute;
    }

    public void clean() {
        elementMap.clear();
        objectmap.clear();
    }

    /**
     * this method is used to process the href attributes which may comes with the incomming soap mesaage
     * <soap:body>
     * <operation>
     * <arg1 href="#obj1"/>
     * </operation>
     * <multiref id="obj1">
     * <name>the real argument</name>
     * <color>blue</color>
     * </multiref>
     * </soap:body>
     * here we assume first child of the soap body has the main object structure and others contain the
     * multiref parts.
     * Soap spec says that those multiref parts must be top level elements.
     *
     * @param soapEnvelope
     */

    public static void processHrefAttributes(SOAPEnvelope soapEnvelope)
            throws AxisFault {
        // first populate the multiref parts to a hash table.
        SOAPBody soapBody = soapEnvelope.getBody();
        // first build the whole tree
        soapBody.build();
        OMElement omElement = null;
        OMAttribute idAttribute = null;
        Map idAndOMElementMap = new HashMap();
        for (Iterator iter = soapBody.getChildElements(); iter.hasNext();) {
            omElement = (OMElement) iter.next();
            // the attribute id is an unqualified attribute
            idAttribute = omElement.getAttribute(new QName(null, "id"));
            if (idAttribute != null) {
                // for the first element there may not have an id
                idAndOMElementMap.put(idAttribute.getAttributeValue(), omElement);
            }
        }

        // start processing from the first child
        processHrefAttributes(idAndOMElementMap, soapBody.getFirstElement(), OMAbstractFactory.getOMFactory());

    }

    public static void processHrefAttributes(Map idAndOMElementMap,
                                         OMElement elementToProcess,
                                         OMFactory omFactory)
            throws AxisFault {

        // first check whether this element has an href value.
        // href is also an unqualifed attribute
        OMAttribute hrefAttribute = elementToProcess.getAttribute(new QName(null, "href"));
        if (hrefAttribute != null) {
            // i.e this has an href attribute
            String hrefAttributeValue = hrefAttribute.getAttributeValue();
            if (!hrefAttributeValue.startsWith("#")) {
                throw new AxisFault("In valid href ==> " + hrefAttributeValue + " does not starts with #");
            } else {
                OMElement referedOMElement =
                        (OMElement) idAndOMElementMap.get(hrefAttributeValue.substring(1));
                if (referedOMElement == null) {
                    throw new AxisFault("In valid href ==> " + hrefAttributeValue + " can not find" +
                            "the matching element");
                } else {
                    // now we have to remove the hrefAttribute and add all the child elements to the
                    // element being proccesed
                    elementToProcess.removeAttribute(hrefAttribute);
                    OMElement clonedReferenceElement = getClonedOMElement(referedOMElement, omFactory);
                    OMNode omNode = null;
                    for (Iterator iter = clonedReferenceElement.getChildren(); iter.hasNext();) {
                        omNode = (OMNode) iter.next();
                        elementToProcess.addChild(omNode.detach());
                    }

                    // add attributes
                    OMAttribute omAttribute = null;
                    for (Iterator iter = clonedReferenceElement.getAllAttributes(); iter.hasNext();) {
                        omAttribute = (OMAttribute) iter.next();
                        // we do not have to populate the id attribute
                        if (!omAttribute.getLocalName().equals("id")) {
                            elementToProcess.addAttribute(omAttribute);
                        }
                    }
                }
            }
        }

        // call recursively to proces all elements
        OMElement childOMElement = null;
        for (Iterator iter = elementToProcess.getChildElements(); iter.hasNext();) {
            childOMElement = (OMElement) iter.next();
            processHrefAttributes(idAndOMElementMap, childOMElement, omFactory);
        }
    }

    /**
     * returns an cloned om element for this OMElement
     *
     * @param omElement
     * @return cloned omElement
     */
    public static OMElement getClonedOMElement(OMElement omElement, OMFactory omFactory) throws AxisFault {

        OMElement newOMElement = omFactory.createOMElement(omElement.getQName());

        // copying attributes
        OMAttribute omAttribute = null;
        OMAttribute newOMAttribute = null;
        for (Iterator iter = omElement.getAllAttributes(); iter.hasNext();) {
            omAttribute = (OMAttribute) iter.next();
            if (!omAttribute.getAttributeValue().equals("id")) {
                newOMAttribute = omFactory.createOMAttribute(
                        omAttribute.getLocalName(),
                        omAttribute.getNamespace(),
                        omAttribute.getAttributeValue());
                newOMElement.addAttribute(newOMAttribute);
            }
        }
        OMNode omNode = null;
        OMText omText = null;
        for (Iterator iter = omElement.getChildren(); iter.hasNext();) {
            omNode = (OMNode) iter.next();
            if (omNode instanceof OMText) {
                omText = (OMText) omNode;
                newOMElement.addChild(omFactory.createOMText(omText.getText()));
            } else if (omNode instanceof OMElement) {
                newOMElement.addChild(getClonedOMElement((OMElement) omNode, omFactory));
            } else {
                throw new AxisFault("Unknown child element type ==> " + omNode.getClass().getName());
            }
        }
        return newOMElement;
    }

}
