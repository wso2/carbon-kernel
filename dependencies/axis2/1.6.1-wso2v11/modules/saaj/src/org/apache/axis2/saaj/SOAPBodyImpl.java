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

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody {

    private org.apache.axiom.soap.SOAPBody omSOAPBody;
    private boolean isBodyElementAdded;

    /** @param omSOAPBody  */
    public SOAPBodyImpl(org.apache.axiom.soap.SOAPBody omSOAPBody) {
        super((ElementImpl)omSOAPBody);
        this.omSOAPBody = omSOAPBody;
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
    */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        if (omSOAPBody.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        SOAPBodyElementImpl childEle =
                new SOAPBodyElementImpl((ElementImpl)getOwnerDocument().createElement(localName));
        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        isBodyElementAdded = true;
        return childEle;
    }

    public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
        String namespaceURI = getNamespaceURI(prefix);

        if (namespaceURI == null) {
            throw new SOAPException("Namespace not declared for the give prefix: " + prefix);
        }
        SOAPBodyElementImpl childEle =
                new SOAPBodyElementImpl(
                        (ElementImpl)getOwnerDocument().createElementNS(namespaceURI,
                                                                        localName));
        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        childEle.element.setNamespace(childEle.element.declareNamespace(namespaceURI, prefix));
        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }

    @Override
    protected Element appendElement(ElementImpl child) throws SOAPException {    
        String namespaceURI = child.getNamespaceURI();
        String prefix = child.getPrefix();

        SOAPBodyElementImpl childEle = new SOAPBodyElementImpl(child);

        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.element.setNamespace(childEle.element.declareNamespace(namespaceURI, prefix));
        }
        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }
    
    public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {
        String namespaceURI = soapElement.getNamespaceURI();
        String prefix = soapElement.getPrefix();
        String localName = soapElement.getLocalName();

        SOAPBodyElementImpl childEle;
        if (namespaceURI == null || namespaceURI.trim().length() == 0) {
            childEle =
                new SOAPBodyElementImpl(
                        (ElementImpl)getOwnerDocument().createElement(localName));
        } else {
            element.declareNamespace(namespaceURI, prefix);
            childEle =
                new SOAPBodyElementImpl(
                        (ElementImpl)getOwnerDocument().createElementNS(namespaceURI,
                                                                        localName));            
        }

        for (Iterator iter = soapElement.getAllAttributes(); iter.hasNext();) {
            Name name = (Name)iter.next();
            childEle.addAttribute(name, soapElement.getAttributeValue(name));
        }

        for (Iterator iter = soapElement.getChildElements(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof Text) {
                childEle.addTextNode(((Text)o).getData());
            } else {
                childEle.addChildElement((SOAPElement)o);
            }
        }

        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.element.setNamespace(childEle.element.declareNamespace(namespaceURI, prefix));
        }
        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
    */
    public SOAPElement addChildElement(String localName, String prefix, String uri)
            throws SOAPException {
        if (omSOAPBody.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        SOAPBodyElementImpl childEle;
        if (uri == null || "".equals(uri)) {
            childEle = new SOAPBodyElementImpl(
                    (ElementImpl)getOwnerDocument().createElement(localName));
        } else if (prefix == null || "".equals(prefix)) {
            childEle = new SOAPBodyElementImpl(
                (ElementImpl)getOwnerDocument().createElementNS(uri,
                                                                localName));
        } else {
            childEle = new SOAPBodyElementImpl(
                    (ElementImpl)getOwnerDocument().createElementNS(uri,
                                                                    prefix + ":" + localName));
        }
        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        childEle.element.setNamespace(childEle.element.declareNamespace(uri, prefix));
        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        isBodyElementAdded = true;
        childEle.setParentElement(this);
        return childEle;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code>
     * object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault() throws SOAPException {
        if (isBodyElementAdded) {
            throw new SOAPException("A SOAPBodyElement has been already added to this SOAPBody");
        }
        SOAPFaultImpl saajSOAPFault = null;

        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            SOAP11FaultImpl fault =
                    new SOAP11FaultImpl(omSOAPBody, (SOAPFactory)this.element.getOMFactory());
            saajSOAPFault = new SOAPFaultImpl(fault);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            SOAP12FaultImpl fault =
                    new SOAP12FaultImpl(omSOAPBody, (SOAPFactory)this.element.getOMFactory());
            saajSOAPFault = new SOAPFaultImpl(fault);
        }
        // set default fault code and string
        saajSOAPFault.setDefaults();
        
        ((NodeImpl)omSOAPBody.getFault()).setUserData(SAAJ_NODE, saajSOAPFault, null);
        return saajSOAPFault;
    }

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in this <code>SOAPBody</code>
     * object.
     *
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in this
     *         <code>SOAPBody</code> object; <code>false</code> otherwise
     */
    public boolean hasFault() {
        return omSOAPBody.hasFault();
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code> object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code> object
     */
    public SOAPFault getFault() {
        if (omSOAPBody.hasFault()) {
            return (SOAPFault)toSAAJNode((org.w3c.dom.Node)omSOAPBody.getFault());
        }
        return null;
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the specified name and adds it to this
     * <code>SOAPBody</code> object.
     *
     * @param name a <code>Name</code> object with the name for the new <code>SOAPBodyElement</code>
     *             object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(Name name) throws SOAPException {
        return (SOAPBodyElement)addChildElement(name);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code> and localized to <code>locale</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be set; must be one of
     *                    the fault codes defined in the SOAP 1.1 specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @param locale      a <code>Locale</code> object indicating the native language of the
     *                    <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString, Locale locale)
            throws SOAPException {
        org.apache.axiom.soap.SOAPFault fault;
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            fault = new SOAP11FaultImpl(omSOAPBody, new Exception(
                    faultString), (SOAPFactory)this.element.getOMFactory());
        } else {
            fault = new SOAP12FaultImpl(omSOAPBody, new Exception(
                    faultString), (SOAPFactory)this.element.getOMFactory());
        }
        SOAPFaultImpl faultImpl = new SOAPFaultImpl(fault);
        faultImpl.setFaultCode(faultCode);

        if (locale != null) {
            faultImpl.setFaultString(faultString, locale);
        } else {
            faultImpl.setFaultString(faultString);
        }

        return faultImpl;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be set; must be one of
     *                    the fault codes defined in the SOAP 1.1 specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException {
        return addFault(faultCode, faultString, null);
    }

    /**
     * Adds the root node of the DOM <code>Document</code> to this <code>SOAPBody</code> object.
     * <p/>
     * Calling this method invalidates the <code>document</code> parameter. The client application
     * should discard all references to this <code>Document</code> and its contents upon calling
     * <code>addDocument</code>. The behavior of an application that continues to use such
     * references is undefined.
     *
     * @param document the <code>Document</code> object whose root node will be added to this
     *                 <code>SOAPBody</code>
     * @return the <code>SOAPBodyElement</code> that represents the root node that was added
     * @throws SOAPException if the <code>Document</code> cannot be added
     */
    public SOAPBodyElement addDocument(Document document) throws SOAPException {
        Element docEle = document.getDocumentElement();

        SOAPElement saajSOAPEle = (SOAPElement)toSAAJNode(docEle, this);
        SOAPBodyElementImpl bodyEle =
                new SOAPBodyElementImpl(((SOAPElementImpl)saajSOAPEle).element);
        addChildElement(bodyEle);
        return bodyEle;
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the specified name and adds it to this
     * <code>SOAPBody</code> object.
     *
     * @param qname a <code>QName</code> object with the name for the new <code>SOAPBodyElement</code>
     *              object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(QName qname) throws SOAPException {
        return (SOAPBodyElement)addChildElement(qname);
    }


    public SOAPFault addFault(QName faultcode, String faultString) throws SOAPException {
        return addFault(faultcode, faultString, null);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code> and localized to <code>locale</code>.
     *
     * @param faultCode   a <code>QName</code> object giving the fault code to be
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @param locale      a <code>Locale</code> object indicating the native language of the
     *                    <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(QName faultCode, String faultString, Locale locale)
            throws SOAPException {
        SOAPFaultImpl faultImpl = null;

        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            SOAP11FaultImpl fault = new SOAP11FaultImpl(omSOAPBody, new Exception(
                    faultString), (SOAPFactory)this.element.getOMFactory());
            faultImpl = new SOAPFaultImpl(fault);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            SOAP12FaultImpl fault = new SOAP12FaultImpl(omSOAPBody, new Exception(
                    faultString), (SOAPFactory)this.element.getOMFactory());
            faultImpl = new SOAPFaultImpl(fault);
        }

        if (faultImpl != null) {
            faultImpl.setFaultCode(faultCode);
            if (locale != null) {
                faultImpl.setFaultString(faultString, locale);
            } else {
                faultImpl.setFaultString(faultString);
            }
        }
        return faultImpl;
    }

    /**
     * Creates a new DOM org.w3c.dom.Document and sets the first child of this SOAPBody as its
     * document element. The child SOAPElement is removed as part of the process.
     *
     * @return The org.w3c.dom.Document representation of the SOAPBody content.
     * @throws SOAPException - if there is not exactly one child SOAPElement of the SOAPBody.
     */
    public Document extractContentAsDocument() throws SOAPException {
        Iterator childElements = this.getChildElements();
        org.w3c.dom.Node domNode = null;
        int childCount = 0;
        while (childElements.hasNext()) {
            domNode = (org.w3c.dom.Node)childElements.next();
            childCount++;
            if (childCount > 1) {
                throw new SOAPException("SOAPBody contains more than one child element");
            }
        }
        //The child SOAPElement is removed as part of the process
        this.removeContents();


        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element element =
                    document.createElementNS(domNode.getNamespaceURI(), domNode.getLocalName());
            element.setNodeValue(domNode.getNodeValue());
            document.appendChild(element);
        } catch (ParserConfigurationException e) {
            throw new SOAPException(e);
        }
        return document;
    }

    private javax.xml.soap.Node toSAAJNode(org.w3c.dom.Node node,
                                           SOAPElement parent) throws SOAPException {
        if (node == null) {
            return null;
        }
        if (node instanceof org.w3c.dom.Text) {
            org.w3c.dom.Text domText = (org.w3c.dom.Text)node;
            return new TextImplEx(domText.getData(), parent);
        }
        if (node instanceof org.w3c.dom.Comment) {
            org.w3c.dom.Comment domText = (org.w3c.dom.Comment)node;
            return new CommentImpl(domText.getData(), parent);
        }
        Element domEle = ((Element)node);
        int indexOfColon = domEle.getTagName().indexOf(":");
        NamespaceImpl ns;
        String localName;
        if (indexOfColon != -1) {
            localName = domEle.getTagName().substring(indexOfColon + 1);
            ns = new NamespaceImpl(domEle.getNamespaceURI(),
                                   domEle.getTagName().substring(0, indexOfColon));
        } else {
            localName = domEle.getLocalName();
            if (localName == null) {  //it is possible that localname isn't set but name is set
                localName = domEle.getTagName();
            }     
            
            String prefix = domEle.getPrefix();
            if(prefix == null) {
                prefix = "";
            }
            if (domEle.getNamespaceURI() != null) {
                ns = new NamespaceImpl(domEle.getNamespaceURI(), prefix);
            } else {
                if (prefix != null) {
                    ns = new NamespaceImpl("", prefix);
                } else {
                    ns = new NamespaceImpl("", "");
                    
                }
            }
        }
        ElementImpl eleImpl =
                new ElementImpl((DocumentImpl)this.getOwnerDocument(),
                                localName, ns, this.element.getOMFactory());

        SOAPElementImpl saajEle = new SOAPElementImpl(eleImpl);

        saajEle.setParentElement(parent);
        NamedNodeMap domAttrs = domEle.getAttributes();
        for (int i = 0; i < domAttrs.getLength(); i++) {
            org.w3c.dom.Node attrNode = domAttrs.item(i);
            String attrLocalName = attrNode.getLocalName();
            if (attrLocalName == null) {
                attrLocalName = attrNode.getNodeName();
            }
            if (attrLocalName == null) {
                //local part is required.  "" is allowed to preserve compatibility with QName 1.0
                attrLocalName = "";
            } 
            saajEle.addAttribute(new PrefixedQName(attrNode.getNamespaceURI(),
                                                   attrLocalName,
                                                   attrNode.getPrefix()),
                                                   attrNode.getNodeValue());                
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childSAAJNode = toSAAJNode(childNodes.item(i), saajEle);
            if (childSAAJNode instanceof javax.xml.soap.Text) {
                saajEle.addTextNode(childSAAJNode.getValue());
            } else {
                saajEle.addChildElement((javax.xml.soap.SOAPElement)childSAAJNode);
            }
        }
        return saajEle;
    }

    public void detachNode() {
        this.detach();
    }

    public OMNode detach() {
        this.parentElement = null;
        return this.element.detach();
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(element.getChildrenWithName(qName));
    }

    public SOAPElement addAttribute(QName qname, String value) throws SOAPException {
        OMNamespace omNamespace = null;
        SOAPFactory soapFactory;
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            soapFactory = new SOAP11Factory();
            omNamespace = soapFactory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            soapFactory = new SOAP12Factory();
            omNamespace = soapFactory.createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
        }
        this.element.addAttribute(qname.getLocalPart(), value, omNamespace);
        return this;
    }

    public SOAPElement addChildElement(QName qname) throws SOAPException {
        if (omSOAPBody.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        SOAPBodyElementImpl childEle;
        if (qname.getNamespaceURI() == null || "".equals(qname.getNamespaceURI())) {
            childEle = new SOAPBodyElementImpl(
                    (ElementImpl)getOwnerDocument().createElement(qname.getLocalPart()));
        }else if(null == qname.getPrefix() || "".equals(qname.getPrefix().trim())) {
            childEle = new SOAPBodyElementImpl(
                    (ElementImpl)getOwnerDocument().createElementNS(qname.getNamespaceURI(),
                                                                            qname.getLocalPart()));
        }else {
            childEle = new SOAPBodyElementImpl(
                    (ElementImpl)getOwnerDocument().createElementNS(qname.getNamespaceURI(),
                                                                    qname.getPrefix() + ":" +
                                                                            qname.getLocalPart()));
        }
        childEle.element.setUserData(SAAJ_NODE, childEle, null);
        childEle.element.setNamespace(childEle.element.declareNamespace(
                qname.getNamespaceURI(), qname.getPrefix()));

        element.appendChild(childEle.element);
        ((NodeImpl)childEle.element.getParentNode()).setUserData(SAAJ_NODE, this, null);
        isBodyElementAdded = true;
        childEle.setParentElement(this);
        return childEle;
    }

    public QName createQName(String localName, String prefix) throws SOAPException {
        if (this.element.getOMFactory() instanceof SOAP11Factory) {
            return super.createQName(localName, prefix);
        } else if (this.element.getOMFactory() instanceof SOAP12Factory) {
            if (this.element.findNamespaceURI(prefix) == null) {
                throw new SOAPException("Only Namespace Qualified elements are allowed");
            } else {
                return super.createQName(localName, prefix);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }


    public Iterator getAllAttributesAsQNames() {
        return super.getAllAttributesAsQNames();
    }

    public String getAttributeValue(QName qname) {
        return super.getAttributeValue(qname);
    }

    public Iterator getChildElements(QName qname) {
        return super.getChildElements(qname);
    }

    public QName getElementQName() {
        return super.getElementQName();
    }

    public boolean removeAttribute(QName qname) {
        return super.removeAttribute(qname);
    }

    public SOAPElement setElementQName(QName qname) throws SOAPException {
        return super.setElementQName(qname);
    }

    public Iterator getChildElements() {
        return getChildren(element.getChildren());
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        return super.addTextNode(text);
    }

    private Iterator getChildren(Iterator childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node)childIter.next();
            Node saajNode = toSAAJNode(domNode);
            if (saajNode instanceof javax.xml.soap.Text) {
                childElements.add(saajNode);
            } else if (!(saajNode instanceof SOAPBodyElement)) {
                // silently replace node, as per saaj 1.2 spec
                if (domNode instanceof ElementImpl) {
                    if (omSOAPBody.hasFault()) {

                        SOAPFactory omFactory = (SOAPFactory)this.element.getOMFactory();
                        org.apache.axiom.soap.SOAPFault fault;
                        if (omFactory instanceof SOAP11Factory) {
                            fault = new SOAP11FaultImpl(omSOAPBody, omFactory);
                        } else {
                            fault = new SOAP12FaultImpl(omSOAPBody, omFactory);
                        }
                        SOAPFaultImpl saajSOAPFault = new SOAPFaultImpl(fault);
                        ((NodeImpl)omSOAPBody.getFault())
                                .setUserData(SAAJ_NODE, saajSOAPFault, null);
                        childElements.add(saajSOAPFault);
                    } else {
                        SOAPBodyElement saajBodyEle = new SOAPBodyElementImpl((ElementImpl)domNode);
                        ((NodeImpl)domNode).setUserData(SAAJ_NODE, saajBodyEle, null);
                        childElements.add(saajBodyEle);
                    }
                }
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }
}
