package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.OMTextImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.namespace.NamespaceContext;
import javax.activation.DataHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Iterator;


public class JAXBMTOMAwareOMBuilder implements XMLStreamWriter {
    // this is the om Element we are going to create
    private OMElement rootElement;
    private OMFactory omFactory;

    private OMElement currentOMElement;

    private Stack omElementStack;

    // this map contains the namespace key and OMNamespace
    private Map namespaceOMNamesapceMap;

    private int prefixNum;

    private OMStreamNamespaceContext omStreamNamespaceContext;

    // this list contains content Id's of all OMTexts which contains DataHandlers
    private ArrayList<String> dataHandlerIdList;

    public JAXBMTOMAwareOMBuilder() {
        omFactory = OMAbstractFactory.getOMFactory();
        omElementStack = new Stack();
        currentOMElement = null;
        omStreamNamespaceContext = new OMStreamNamespaceContext();
        namespaceOMNamesapceMap = new HashMap();
        dataHandlerIdList = new ArrayList<String>();
        prefixNum = 0;

    }

    // serailizer must have finish serializing when we call this method.
    public OMElement getOMElement() throws XMLStreamException {
        if (!omElementStack.isEmpty()) {
            throw new XMLStreamException("This is an invalid Xml ");
        }
        return rootElement;
    }

    private OMNamespace getOMNamespace(String namespace, String prefix) throws XMLStreamException {
        OMNamespace omNamespace = null;
        if (namespace != null) {
            if (namespaceOMNamesapceMap.containsKey(namespace)) {
                omNamespace = (OMNamespace) namespaceOMNamesapceMap.get(namespace);
            } else {
                if (prefix == null) {
                    prefix = "ns" + ++prefixNum;
                } else if (this.omStreamNamespaceContext.getNamespaceURI(prefix) != null) {
                    throw new XMLStreamException("the prefix ==> " + prefix +
                            " Already exists for namespace ==> " + namespace);
                }
                omNamespace = omFactory.createOMNamespace(namespace, prefix);
                this.omStreamNamespaceContext.registerNamespace(namespace, prefix);
                namespaceOMNamesapceMap.put(namespace, omNamespace);
            }
        }
        return omNamespace;
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    public void writeStartElement(String namespace, String localName) throws XMLStreamException {
        writeStartElement(null, localName, namespace);
    }

    public void writeStartElement(String prefix, String localName,
                                  String namespace) throws XMLStreamException {
        OMNamespace omNamespace = getOMNamespace(namespace, prefix);
        currentOMElement = omFactory.createOMElement(localName, omNamespace);
        if (!omElementStack.isEmpty()) {
            // we always keep the parent at the top of the stack
            OMElement parent = (OMElement) omElementStack.peek();
            parent.addChild(currentOMElement);
        } else {
            // i.e this must be an start root element
            rootElement = currentOMElement;
        }
        // set this as the top element
        omElementStack.push(currentOMElement);
    }

    public void writeEmptyElement(String namespaceURI,
                                  String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, namespaceURI);
    }

    public void writeEmptyElement(String prefix, String localName,
                                  String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    public void writeEndElement() throws XMLStreamException {
        omElementStack.pop();
    }

    public void writeEndDocument() throws XMLStreamException {
        // nothing to do
    }

    public void close() throws XMLStreamException {
        // nothing to do
    }

    public void flush() throws XMLStreamException {
        // nothing to do


    }

    public void writeAttribute(String attributeName,
                               String attributeValue) throws XMLStreamException {
        writeAttribute(null, null, attributeName, attributeValue);
    }

    public void writeAttribute(
            String prefix, String namespace, String attributeName,
            String attributeValue) throws XMLStreamException {
        currentOMElement.addAttribute(attributeName,
                attributeValue, getOMNamespace(namespace, prefix));
    }

    public void writeAttribute(String namespace, String attributeName, String attributeValue)
            throws XMLStreamException {
        writeAttribute(null, namespace, attributeName, attributeValue);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (namespaceURI != null) {
            OMNamespace omNamespace = getOMNamespace(namespaceURI, prefix);
            currentOMElement.declareNamespace(omNamespace);
        }
    }

    public void writeDefaultNamespace(String namespace) throws XMLStreamException {
        rootElement.declareDefaultNamespace(namespace);
        getOMNamespace(namespace, "");
    }

    public void writeComment(String string) throws XMLStreamException {
        omFactory.createOMComment(currentOMElement, string);
    }

    public void writeProcessingInstruction(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeProcessingInstruction(String string, String string1)
            throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeCData(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeDTD(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeEntityRef(String string) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public void writeStartDocument() throws XMLStreamException {
        // nothing to do
    }

    public void writeStartDocument(String string) throws XMLStreamException {
        // nothing to do
    }

    public void writeStartDocument(String string, String string1) throws XMLStreamException {
        // nothing to do
    }

    public void writeCharacters(String string) throws XMLStreamException {
        /**
         * If the recieved charaters are going to replace an already existing OMTextImpl which
         * contains a data handler inside, don't allow it. This is because, if this is allowed,
         * MTOM functionality will not work and the data will be written in base64.
         */
        Iterator itr = currentOMElement.getChildren();
        while (itr.hasNext()) {
            Object node = itr.next();
            if (node instanceof OMTextImpl) {
                OMTextImpl omText = (OMTextImpl) node;
                if (dataHandlerIdList.contains(omText.getContentID())) {
                    return;
                }
            }
        }
        currentOMElement.setText(string);
    }

    public void writeCharacters(char[] chars, int i, int i1) throws XMLStreamException {
        writeCharacters(new String(chars, i, i1));
    }

    public String getPrefix(String namespace) throws XMLStreamException {
        return this.omStreamNamespaceContext.getPrefix(namespace);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        // this method will add the namespace correctly.
        getOMNamespace(uri, prefix);
    }

    public void setDefaultNamespace(String namespace) throws XMLStreamException {
        rootElement.declareDefaultNamespace(namespace);
        getOMNamespace(namespace, "");
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        throw new UnsupportedOperationException("this method has not yet been implemented");
    }

    public NamespaceContext getNamespaceContext() {
        return this.omStreamNamespaceContext;
    }

    public Object getProperty(String string) throws IllegalArgumentException {
        //TODO - need to fix properly
        return Boolean.FALSE;
    }

    public void writeDataHandler(DataHandler dataHandler) {
        OMText omText = omFactory.createOMText(dataHandler, true);

        // Add the content id into the list
        dataHandlerIdList.add(omText.getContentID());
        currentOMElement.addChild(omText);
    }
}

