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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.util.OMSerializerUtil;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.description.java2wsdl.TypeTable;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.lang.reflect.Array;

/**
 * This is the new implementation of the ADBpullaparser. The approach here is simple When the pull
 * parser needs to generate events for a particular name-value(s) pair it always handes over
 * (delegates) the task to another pull parser which knows how to deal with it The common types of
 * name value pairs we'll come across are 1. String name/QName name - String value 2. String
 * name/QName name - String[] value 3. OMElementkey - OMElement value 4. QName name/String name  -
 * ADBBean value 5. QName name/String name  - Java bean 5. QName name/String name  - Datahandler
 * <p/>
 * As for the attributes, these are the possible combinations in the array 1. String name/QName name
 * - String value 2. OMAttributeKey - OMAttribute
 * <p/>
 * Note that certain array methods have  been deliberately removed to avoid complications. The
 * generated code will take the trouble to lay the elements of the array in the correct order
 * <p/>
 * <p/>
 * Hence there will be a parser impl that knows how to handle these types, and this parent parser
 * will always delegate these tasks to the child pullparasers in effect this is one huge state
 * machine that has only a few states and delegates things down to the child parsers whenever
 * possible
 * <p/>
 */
public class ADBXMLStreamReaderImpl implements ADBXMLStreamReader {

    private Object[] properties;
    private Object[] attributes;
    private QName elementQName;

    //This is to store the QName which are in the typeTable after setting the correct prefix
    private HashMap qnameMap = new HashMap();

    //we always create a new namespace context
    private ADBNamespaceContext namespaceContext = new ADBNamespaceContext();

    private Map declaredNamespaceMap = new HashMap();

    //states for this pullparser - it can only have four states
    private static final int START_ELEMENT_STATE = 0;
    private static final int END_ELEMENT_STATE = 1;
    private static final int DELEGATED_STATE = 2;
    private static final int TEXT_STATE = 3;

    //integer field that keeps the state of this
    //parser.
    private int state = START_ELEMENT_STATE;

    //reference to the child reader
    private ADBXMLStreamReader childReader;

    //current property index
    //initialized at zero
    private int currentPropertyIndex = 0;

    //To keep element formdefault qualified or not
    private boolean qualified = false;

    //to keep the current types which are in AxisService
    private TypeTable typeTable = null;


    /*
     * we need to pass in a namespace context since when delegated, we've no
    * idea of the current namespace context. So it needs to be passed on
    * here!
    */
    public ADBXMLStreamReaderImpl(QName adbBeansQName,
                                  Object[] properties,
                                  Object[] attributes) {
        //validate the lengths, since both the arrays are supposed
        //to have
        this.properties = properties;
        this.elementQName = adbBeansQName;
        this.attributes = attributes;
    }

    public ADBXMLStreamReaderImpl(QName adbBeansQName,
                                  Object[] properties,
                                  Object[] attributes,
                                  TypeTable typeTable,
                                  boolean qualified) {
        this(adbBeansQName, properties, attributes);
        this.qualified = qualified;
        this.typeTable = typeTable;
        if(this.typeTable!=null){
            Map complexTypeMap = this.typeTable.getComplexSchemaMap();
            if(complexTypeMap !=null){
                Iterator keys = complexTypeMap.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    QName qname = (QName) complexTypeMap.get(key);
                    if(qname !=null){
                        String prefix =qname.getPrefix();
                        if(prefix == null || "".equals(prefix)){
                            prefix = OMSerializerUtil.getNextNSPrefix();
                        }
                        qname = new QName(qname.getNamespaceURI(),qname.getLocalPart(),prefix);
                        this.typeTable.getComplexSchemaMap().put(key,qname);
                        qnameMap.put(qname.getNamespaceURI(),prefix);
                        addToNsMap(prefix, qname.getNamespaceURI());
                    }
                }
            }
        }
    }

    /** add the namespace context */

    public void addNamespaceContext(NamespaceContext nsContext) {
        // register the namespace context passed in to this
        this.namespaceContext.setParentNsContext(nsContext);


    }

    /**
     * we need to split out the calling to the populate namespaces seperately since this needs to be
     * done *after* setting the parent namespace context. We cannot assume it will happen at
     * construction!
     */
    public void init() {
        // here we have an extra issue to attend to. we need to look at the
        // prefixes and uris (the combination) and populate a hashmap of
        // namespaces. The hashmap of namespaces will be used to serve the
        // namespace context

        populateNamespaceContext();
    }

    /**
     * @param key
     * @throws IllegalArgumentException
     */
    public Object getProperty(String key) throws IllegalArgumentException {
        if (OPTIMIZATION_ENABLED.equals(key)) {
            return Boolean.TRUE;
        } else if (state == TEXT_STATE) {
            if (IS_BINARY.equals(key)) {
                return Boolean.FALSE;
            } else {
                return null;
            }
        } else if (state == DELEGATED_STATE) {
            return childReader.getProperty(key);
        } else {
            return null;
        }

    }

    public void require(int i, String string, String string1)
            throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /**
     * todo implement the right contract for this
     *
     * @throws XMLStreamException
     */
    public String getElementText() throws XMLStreamException {
        if (state == DELEGATED_STATE) {
            return childReader.getElementText();
        } else {
            return null;
        }

    }

    /**
     * todo implement this
     *
     * @throws XMLStreamException
     */
    public int nextTag() throws XMLStreamException {
        return 0;
    }

    /** @throws XMLStreamException  */
    public boolean hasNext() throws XMLStreamException {
        if (state == DELEGATED_STATE) {
            if (childReader.isDone()) {
                //the child reader is done. We shouldn't be getting the
                //hasnext result from the child pullparser then
                return true;
            } else {
                return childReader.hasNext();
            }
        } else {
            return (state == START_ELEMENT_STATE
                    || state == TEXT_STATE);


        }
    }

    public void close() throws XMLStreamException {
        //do nothing here - we have no resources to free
    }

    public String getNamespaceURI(String prefix) {
        return namespaceContext.getNamespaceURI(prefix);
    }

    public boolean isStartElement() {
        if (state == START_ELEMENT_STATE) {
            return true;
        } else if (state == END_ELEMENT_STATE) {
            return false;
        }
        return childReader.isStartElement();
    }

    public boolean isEndElement() {
        if (state == START_ELEMENT_STATE) {
            return false;
        } else if (state == END_ELEMENT_STATE) {
            return true;
        }
        return childReader.isEndElement();
    }

    public boolean isCharacters() {
        if (state == START_ELEMENT_STATE || state == END_ELEMENT_STATE) {
            return false;
        }
        return childReader.isCharacters();
    }

    public boolean isWhiteSpace() {
        if (state == START_ELEMENT_STATE || state == END_ELEMENT_STATE) {
            return false;
        }
        return childReader.isWhiteSpace();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///  attribute handling
    ///////////////////////////////////////////////////////////////////////////

    public String getAttributeValue(String nsUri, String localName) {

        int attribCount = getAttributeCount();
        String returnValue = null;
        QName attribQualifiedName;
        for (int i = 0; i < attribCount; i++) {
            attribQualifiedName = getAttributeName(i);
            if (nsUri == null) {
                if (localName.equals(attribQualifiedName.getLocalPart())) {
                    returnValue = getAttributeValue(i);
                    break;
                }
            } else {
                if (localName.equals(attribQualifiedName.getLocalPart())
                        && nsUri.equals(attribQualifiedName.getNamespaceURI())) {
                    returnValue = getAttributeValue(i);
                    break;
                }
            }

        }


        return returnValue;
    }

    public int getAttributeCount() {
        return (state == DELEGATED_STATE) ?
                childReader.getAttributeCount() :
                ((attributes != null) && (state == START_ELEMENT_STATE) ? attributes.length / 2 :
                        0);
    }

    /** @param i  */
    public QName getAttributeName(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getAttributeName(i);
        } else if (state == START_ELEMENT_STATE) {
            if (attributes == null) {
                return null;
            } else {
                if ((i >= (attributes.length / 2)) || i < 0) { //out of range
                    return null;
                } else {
                    //get the attribute pointer
                    Object attribPointer = attributes[i * 2];
                    //case one - attrib name is null
                    //this should be the pointer to the OMAttribute then
                    if (attribPointer == null) {
                        Object omAttribObj = attributes[(i * 2) + 1];
                        if (omAttribObj == null ||
                                !(omAttribObj instanceof OMAttribute)) {
                            // wrong object set to have in the attrib array -
                            // this should have been detected by now but just be
                            // sure
                            throw new UnsupportedOperationException();
                        }
                        OMAttribute att = (OMAttribute)omAttribObj;
                        return att.getQName();
                    } else if (attribPointer instanceof OMAttribKey) {
                        Object omAttribObj = attributes[(i * 2) + 1];
                        if (omAttribObj == null ||
                                !(omAttribObj instanceof OMAttribute)) {
                            // wrong object set to have in the attrib array -
                            // this should have been detected by now but just be
                            // sure
                            throw new UnsupportedOperationException();
                        }
                        OMAttribute att = (OMAttribute)omAttribObj;
                        return att.getQName();
                        //case two - attrib name is a plain string
                    } else if (attribPointer instanceof String) {
                        return new QName((String)attribPointer);
                    } else if (attribPointer instanceof QName) {
                        return (QName)attribPointer;
                    } else {
                        return null;
                    }
                }
            }
        } else {
            throw new IllegalStateException();//as per the api contract
        }

    }

    public String getAttributeNamespace(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getAttributeNamespace(i);
        } else if (state == START_ELEMENT_STATE) {
            QName name = getAttributeName(i);
            if (name == null) {
                return null;
            } else {
                return name.getNamespaceURI();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributeLocalName(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getAttributeLocalName(i);
        } else if (state == START_ELEMENT_STATE) {
            QName name = getAttributeName(i);
            if (name == null) {
                return null;
            } else {
                return name.getLocalPart();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributePrefix(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getAttributePrefix(i);
        } else if (state == START_ELEMENT_STATE) {
            QName name = getAttributeName(i);
            if (name == null) {
                return null;
            } else {
                return name.getPrefix();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public String getAttributeType(int i) {
        return null;  //not supported
    }

    public String getAttributeValue(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getAttributeValue(i);
        } else if (state == START_ELEMENT_STATE) {
            if (attributes == null) {
                return null;
            } else {
                if ((i >= (attributes.length / 2)) || i < 0) { //out of range
                    return null;
                } else {
                    //get the attribute pointer
                    Object attribPointer = attributes[i * 2];
                    Object omAttribObj = attributes[(i * 2) + 1];
                    //case one - attrib name is null
                    //this should be the pointer to the OMAttribute then
                    if (attribPointer == null) {

                        if (omAttribObj == null ||
                                !(omAttribObj instanceof OMAttribute)) {
                            // wrong object set to have in the attrib array -
                            // this should have been detected by now but just be
                            // sure
                            throw new UnsupportedOperationException();
                        }
                        OMAttribute att = (OMAttribute)omAttribObj;
                        return att.getAttributeValue();
                    } else if (attribPointer instanceof OMAttribKey) {
                        if (omAttribObj == null ||
                                !(omAttribObj instanceof OMAttribute)) {
                            // wrong object set to have in the attrib array -
                            // this should have been detected by now but just be
                            // sure
                            throw new UnsupportedOperationException();
                        }
                        OMAttribute att = (OMAttribute)omAttribObj;
                        return att.getAttributeValue();
                        //case two - attrib name is a plain string
                    } else if (attribPointer instanceof String) {
                        return (String)omAttribObj;
                    } else if (attribPointer instanceof QName) {
                        if (omAttribObj instanceof QName){
                           QName attributeQName = (QName) omAttribObj;
                           // first check it is already there if not add the namespace.
                           String prefix = namespaceContext.getPrefix(attributeQName.getNamespaceURI());
                           if (prefix == null){
                               prefix = OMSerializerUtil.getNextNSPrefix();
                               addToNsMap(prefix,attributeQName.getNamespaceURI());
                           }

                           String attributeValue = null;
                           if (prefix.equals("")){
                               // i.e. this is the default namespace
                               attributeValue = attributeQName.getLocalPart();
                           } else {
                               attributeValue = prefix + ":" + attributeQName.getLocalPart();
                           }
                           return attributeValue;
                        } else {
                            return (String)omAttribObj;
                        }

                    } else {
                        return null;
                    }
                }
            }
        } else {
            throw new IllegalStateException();
        }

    }

    public boolean isAttributeSpecified(int i) {
        return false;  //not supported
    }

///////////////////////////////////////////////////////////////////////////
//////////////  end of attribute handling
///////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////
//////////////   namespace handling
////////////////////////////////////////////////////////////////////////////

    public int getNamespaceCount() {
        if (state == DELEGATED_STATE) {
            return childReader.getNamespaceCount();
        } else {
            return declaredNamespaceMap.size();
        }
    }

    /** @param i  */
    public String getNamespacePrefix(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getNamespacePrefix(i);
        } else if (state != TEXT_STATE) {
            //order the prefixes
            String[] prefixes = makePrefixArray();
            if ((i >= prefixes.length) || (i < 0)) {
                return null;
            } else {
                return prefixes[i];
            }

        } else {
            throw new IllegalStateException();
        }

    }

    /** Get the prefix list from the hastable and take that into an array */
    private String[] makePrefixArray() {
        String[] prefixes =
                (String[])declaredNamespaceMap.keySet().
                        toArray(new String[declaredNamespaceMap.size()]);
        Arrays.sort(prefixes);
        return prefixes;
    }

    public String getNamespaceURI(int i) {
        if (state == DELEGATED_STATE) {
            return childReader.getNamespaceURI(i);
        } else if (state != TEXT_STATE) {
            String namespacePrefix = getNamespacePrefix(i);
            return namespacePrefix == null ? null :
                    (String)declaredNamespaceMap.get(namespacePrefix);
        } else {
            throw new IllegalStateException();
        }

    }

    public NamespaceContext getNamespaceContext() {
        if (state == DELEGATED_STATE) {
            return childReader.getNamespaceContext();
        } else {
            return namespaceContext;
        }


    }

///////////////////////////////////////////////////////////////////////////
/////////  end of namespace handling
///////////////////////////////////////////////////////////////////////////

    public int getEventType() {
        if (state == START_ELEMENT_STATE) {
            return START_ELEMENT;
        } else if (state == END_ELEMENT_STATE) {
            return END_ELEMENT;
        } else { // this is the delegated state
            return childReader.getEventType();
        }

    }

    public String getText() {
        if (state == DELEGATED_STATE) {
            return childReader.getText();
        } else if (state == TEXT_STATE) {
            Object property = properties[currentPropertyIndex - 1];
            if (property instanceof DataHandler){
                return ConverterUtil.getStringFromDatahandler((DataHandler)property);
            } else {
                return (String)properties[currentPropertyIndex - 1];
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public char[] getTextCharacters() {
        if (state == DELEGATED_STATE) {
            return childReader.getTextCharacters();
        } else if (state == TEXT_STATE) {
            return properties[currentPropertyIndex - 1] == null ? new char[0] :
                    ((String)properties[currentPropertyIndex - 1]).toCharArray();
        } else {
            throw new IllegalStateException();
        }
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2)
            throws XMLStreamException {
        if (state == DELEGATED_STATE) {
            return childReader.getTextCharacters(i, chars, i1, i2);
        } else if (state == TEXT_STATE) {
            //todo  - implement this
            return 0;
        } else {
            throw new IllegalStateException();
        }
    }

    public int getTextStart() {
        if (state == DELEGATED_STATE) {
            return childReader.getTextStart();
        } else if (state == TEXT_STATE) {
            return 0;//assume text always starts at 0
        } else {
            throw new IllegalStateException();
        }
    }

    public int getTextLength() {
        if (state == DELEGATED_STATE) {
            return childReader.getTextLength();
        } else if (state == TEXT_STATE) {
            return 0;//assume text always starts at 0
        } else {
            throw new IllegalStateException();
        }
    }

    public String getEncoding() {
        if (state == DELEGATED_STATE) {
            return childReader.getEncoding();
        } else {
            //we've no idea what the encoding is going to be in this case
            //perhaps we ought to return some constant here, which the user might
            //have access to change!
            return null;
        }
    }

    /** check the validity of this implementation */
    public boolean hasText() {
        if (state == DELEGATED_STATE) {
            return childReader.hasText();
        } else return state == TEXT_STATE;

    }

    /**
     */
    public Location getLocation() {
        //return a default location
        return new Location() {
            public int getLineNumber() {
                return 0;
            }

            public int getColumnNumber() {
                return 0;
            }

            public int getCharacterOffset() {
                return 0;
            }

            public String getPublicId() {
                return null;
            }

            public String getSystemId() {
                return null;
            }
        };
    }

    public QName getName() {
        if (state == DELEGATED_STATE) {
            return childReader.getName();
        } else if (state != TEXT_STATE) {
            return elementQName;
        } else {
            throw new IllegalStateException();
        }

    }

    public String getLocalName() {
        if (state == DELEGATED_STATE) {
            return childReader.getLocalName();
        } else if (state != TEXT_STATE) {
            return elementQName.getLocalPart();
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean hasName() {
        //since this parser always has a name, the hasname
        //has to return true if we are still navigating this element
        //if not we should ask the child reader for it.
        if (state == DELEGATED_STATE) {
            return childReader.hasName();
        } else return state != TEXT_STATE;
    }

    public String getNamespaceURI() {
        if (state == DELEGATED_STATE) {
            return childReader.getNamespaceURI();
        } else if (state == TEXT_STATE) {
            return null;
        } else {
            return elementQName.getNamespaceURI();
        }
    }

    public String getPrefix() {
        if (state == DELEGATED_STATE) {
            return childReader.getPrefix();
        } else if (state == TEXT_STATE) {
            return null;
        } else {
            String prefix = elementQName.getPrefix();
            return "".equals(prefix) ? null : prefix;
        }
    }

    public String getVersion() {
        return null;
    }

    public boolean isStandalone() {
        return true;
    }

    public boolean standaloneSet() {
        return true;
    }

    public String getCharacterEncodingScheme() {
        return null;   //todo - should we return something for this ?
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Yet to be implemented !!");
    }

///////////////////////////////////////////////////////////////////////////
/// Other utility methods
//////////////////////////////////////////////////////////////////////////


    /** Populates a namespace context */
    private void populateNamespaceContext() {

        //first add the current element namespace to the namespace context
        //declare it if not found
        addToNsMap(elementQName.getPrefix(), elementQName.getNamespaceURI());

        //traverse through the attributes and populate the namespace context
        //the attrib list can be of many combinations
        // the valid combinations are
        // String - String
        // QName - QName
        // null - OMAttribute

        if (attributes != null) {
            for (int i = 0; i < attributes.length; i = i + 2) { //jump in two
                Object attribName = attributes[i];
                if (attribName == null) {
                    //this should be the OMAttrib case!
                    OMAttribute OMAttrib = (OMAttribute)attributes[i + 1];
                    OMNamespace namespace = OMAttrib.getNamespace();
                    if (namespace != null) {
                        addToNsMap(namespace.getPrefix(),
                                   namespace.getNamespaceURI());
                    }
                } else if (attribName instanceof OMAttribKey) {
                    //this is definitely the OMAttribute case
                    OMAttribute OMAttrib = (OMAttribute)attributes[i + 1];
                    OMNamespace namespace = OMAttrib.getNamespace();
                    if (namespace != null) {
                        addToNsMap(namespace.getPrefix(),
                                   namespace.getNamespaceURI());
                    }
                } else if (attribName instanceof String) {
                    //ignore this case - Nothing to do
                } else if (attribName instanceof QName) {
                    QName attribQName = ((QName)attribName);
                    addToNsMap(attribQName.getPrefix(),
                               attribQName.getNamespaceURI());

                }
            }
        }


    }

    /**
     * @param prefix
     * @param uri
     */
    private void addToNsMap(String prefix, String uri) {
        if (!uri.equals(namespaceContext.getNamespaceURI(prefix))) {
            namespaceContext.pushNamespace(prefix, uri);
            declaredNamespaceMap.put(prefix, uri);
        }
    }

    /**
     * By far this should be the most important method in this class this method changes the state
     * of the parser
     */
    public int next() throws XMLStreamException {
        int returnEvent = -1; //invalid state is the default state
        switch (state) {
            case START_ELEMENT_STATE:
                //current element is start element. We should be looking at the
                //property list and making a pullparser for the property value
                if (properties == null || properties.length == 0) {
                    //no properties - move to the end element state straightaway
                    state = END_ELEMENT_STATE;
                    returnEvent = END_ELEMENT;
                } else {
                    //there are properties. now we should delegate this task to a
                    //child reader depending on the property type
                    returnEvent = processProperties();


                }
                break;
            case END_ELEMENT_STATE:
                //we've reached the end element already. If the user tries to push
                // further ahead then it is an exception
                throw new XMLStreamException(
                        "Trying to go beyond the end of the pullparser");


            case DELEGATED_STATE:
                if (childReader.isDone()) {
                    //we've reached the end!
                    if (currentPropertyIndex > (properties.length - 1)) {
                        state = END_ELEMENT_STATE;
                        returnEvent = END_ELEMENT;
                    } else {
                        returnEvent = processProperties();
                    }
                } else {
                    returnEvent = childReader.next();
                }
                break;

            case TEXT_STATE:
                // if there are any more event we should be delegating to
                // processProperties. if not we just return an end element
                if (currentPropertyIndex > (properties.length - 1)) {
                    state = END_ELEMENT_STATE;
                    returnEvent = END_ELEMENT;
                } else {
                    returnEvent = processProperties();
                }
                break;
        }
        return returnEvent;
    }

    /**
     * A convenient method to reuse the properties
     *
     * @return event to be thrown
     * @throws XMLStreamException
     */
    private int processProperties() throws XMLStreamException {
        //move to the next property depending on the current property
        //index
        Object propPointer = properties[currentPropertyIndex];
        QName propertyQName = null;
        boolean textFound = false;
        if (propPointer == null) {
            throw new XMLStreamException("property key cannot be null!");
        } else if (propPointer instanceof String) {
            // propPointer being a String has a special case
            // that is it can be a the special constant ELEMENT_TEXT that
            // says this text event
            if (ELEMENT_TEXT.equals(propPointer)) {
                textFound = true;
            } else {
                propertyQName = new QName((String)propPointer);
            }
        } else if (propPointer instanceof QName) {
            propertyQName = (QName)propPointer;
        } else if (propPointer instanceof OMElementKey) {
            // ah - in this case there's nothing to be done
            //about the propertyQName in this case - we'll just leave
            //it as it is
        } else {
            //oops - we've no idea what kind of key this is
            throw new XMLStreamException(
                    "unidentified property key!!!" + propPointer);
        }

        if(propertyQName!=null){
            String prefix = (String) qnameMap.get(propertyQName.getNamespaceURI());
            if(prefix!=null){
                propertyQName = new QName(propertyQName.getNamespaceURI(),propertyQName.getLocalPart(),prefix);
            }
        }

        //ok! we got the key. Now look at the value
        Object propertyValue = properties[currentPropertyIndex + 1];
        //cater for the special case now
        if (textFound) {
            //no delegation here - make the parser null and immediately
            //return with the event characters
            childReader = null;
            state = TEXT_STATE;
            currentPropertyIndex = currentPropertyIndex + 2;
            return CHARACTERS;
        } else if (propertyValue == null) {
            //if the value is null we delegate the work to a nullable
            // parser
            childReader = new NullXMLStreamReader(propertyQName);
            childReader.addNamespaceContext(this.namespaceContext);
            childReader.init();

            //we've a special pullparser for a datahandler!
        } else if (propertyValue instanceof DataHandler) {
            childReader = new ADBDataHandlerStreamReader(propertyQName,
                                                         (DataHandler)propertyValue);
            childReader.addNamespaceContext(this.namespaceContext);
            childReader.init();

        } else if (propertyValue instanceof String) {
            //strings are handled by the NameValuePairStreamReader
            childReader =
                    new NameValuePairStreamReader(propertyQName,
                                                  (String)propertyValue);
            childReader.addNamespaceContext(this.namespaceContext);
            childReader.init();
        } else if (propertyValue.getClass().isArray()) {
            // this is an arrary object and we need to get the pull parser for that
            int length = Array.getLength(propertyValue);
            if (length == 0) {
                //advance the index
                currentPropertyIndex = currentPropertyIndex + 2;
                return processProperties();
            } else {
                List objects = new ArrayList();
                Object valueObject = null;
                for (int i = 0; i < length; i++) {
                    //for innter Arrary Complex types we use the special local name array
                    objects.add(new QName(propertyQName.getNamespaceURI(), "array"));
                    valueObject = Array.get(propertyValue, i);
                    if ((valueObject != null) && SimpleTypeMapper.isSimpleType(valueObject)){
                        objects.add(SimpleTypeMapper.getStringValue(valueObject));
                    } else {
                        objects.add(valueObject);
                    }
                }

                ADBXMLStreamReader reader = new ADBXMLStreamReaderImpl(propertyQName,
                        objects.toArray(), new ArrayList().toArray(), typeTable, qualified);
                childReader = new WrappingXMLStreamReader(reader);
            }
        } else if (propertyValue instanceof ADBBean) {
            //ADBbean has it's own method to get a reader
            XMLStreamReader reader = ((ADBBean)propertyValue).
                    getPullParser(propertyQName);
            // we know for sure that this is an ADB XMLStreamreader.
            // However we need to make sure that it is compatible
            if (reader instanceof ADBXMLStreamReader) {
                childReader = (ADBXMLStreamReader)reader;
                childReader.addNamespaceContext(this.namespaceContext);
                childReader.init();
            } else {
                //wrap it to make compatible
                childReader = new WrappingXMLStreamReader(
                        reader);
            }
        } else if (propertyValue instanceof OMElement) {
            //OMElements do not provide the kind of parser we need
            //there is no other option than wrapping
            childReader = new WrappingXMLStreamReader(
                    ((OMElement)propertyValue).getXMLStreamReader());
            //we cannot register the namespace context here!!

        } else {
            //all special possiblilities has been tried! Let's treat
            //the thing as a bean and try generating events from it
            childReader = new WrappingXMLStreamReader
                    (BeanUtil.getPullParser(propertyValue,
                                            propertyQName, typeTable, qualified, false));
            //we cannot register the namespace context here
        }

        //set the state here
        state = DELEGATED_STATE;
        //we are done with the delegation
        //increment the property index
        currentPropertyIndex = currentPropertyIndex + 2;
        // If necessary, discard the START_DOCUMENT element (AXIS2-4271)
        int eventType = childReader.getEventType();
        return eventType == START_DOCUMENT ? childReader.next() : eventType;
    }

    /** are we done ? */
    public boolean isDone() {
        return (state == END_ELEMENT_STATE);
    }

}
