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


package org.apache.axis2.addressing;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.externalize.ExternalizeConstants;
import org.apache.axis2.context.externalize.SafeObjectInputStream;
import org.apache.axis2.context.externalize.SafeObjectOutputStream;
import org.apache.axis2.context.externalize.SafeSerializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class EndpointReference
 * This class models the WS-A EndpointReferenceType. But this can be used without any WS-A handlers as well
 * Since the models for this in Submission and Final versions are different, lets make this to comply with
 * WS-A Final version. So any information found with WS-A submission will be "pumped" in to this model.
 */
public class EndpointReference implements Externalizable, SafeSerializable {

    private static final long serialVersionUID = 5278892171162372439L;

    private static final Log log = LogFactory.getLog(EndpointReference.class);
    
    //  supported revision levels, add a new level to manage compatible changes
    private static final int REVISION_2 = 2;
    // current revision level of this object
    private static final int revisionID = REVISION_2;

    private static final String myClassName = "EndpointReference";

    /**
     * An ID which can be used to correlate operations on an instance of
     * this object in the log files
     */
    private String logCorrelationIDString;
    
    /**
     * The list of URIs that should be considered equivalent to 
     * the WS-Addressing anonymous URI
     */
    private static volatile List<String> anonymousEquivalentURIs = new ArrayList<String>();


    /**
     * <EndpointReference>
     * <Address>xs:anyURI</Address>
     * <ReferenceParameters>xs:any*</ReferenceParameters>
     * <MetaData>xs:any*</MetaData>
     * <!-- In addition to this, EPR can contain any number of OMElements -->
     * </EndpointReference>
     */

    private String name;
    private String address;
    private ArrayList<OMAttribute> addressAttributes;
    private ArrayList<OMNode> metaData;
    private ArrayList<OMAttribute> metaDataAttributes;
    private Map<QName, OMElement> referenceParameters;
    private ArrayList<OMElement> extensibleElements;
    private ArrayList<OMAttribute> attributes;

    /**
     * No-Arg Constructor
     * Required for Externalizable objects
     */
    public EndpointReference() {}
    
    /**
     * Adds a parameter to the list of anonymous equivalent URIs. 
     * @param anonymousEquivalentURI any URI that has an address that
     * begins with this value will be considered to be anonymous
     */
    public static void addAnonymousEquivalentURI(String anonymousEquivalentURI){
        if (log.isTraceEnabled())
        	log.trace("addAnonymousEquivalentURI: " + anonymousEquivalentURI);
        
        // Avoid synchronization in hasAnonymousAddress by using the
        // technique outlined at http://is.gd/gv3
        synchronized (anonymousEquivalentURIs) {
        	ArrayList<String> newList = new ArrayList<String>(anonymousEquivalentURIs);
        	newList.add(anonymousEquivalentURI);
            anonymousEquivalentURIs = newList;
        }
    }
 

    /**
     * @param address
     */
    public EndpointReference(String address) {
        this.address = address;
    }

    /**
     * @param omElement
     */
    public void addReferenceParameter(OMElement omElement) {
        if (omElement == null) {
            return;
        }
        if (referenceParameters == null) {
            referenceParameters = new HashMap<QName, OMElement>();
        }
        referenceParameters.put(omElement.getQName(), omElement);
    }

    /**
     * @param qname
     * @param value - the text of the OMElement. Remember that this is a convenient method for the user,
     *              which has limited capability. If you want more power use @See EndpointReference#addReferenceParameter(OMElement)
     */
    public void addReferenceParameter(QName qname, String value) {
        if (qname == null) {
            return;
        }
        OMElement omElement = OMAbstractFactory.getOMFactory().createOMElement(qname, null);
        omElement.setText(value);
        addReferenceParameter(omElement);
    }

    /**
     * This will return a Map of reference parameters with QName as the key and an OMElement
     * as the value
     *
     * @return - map of the reference parameters, where the key is the QName of the reference parameter
     *         and the value is an OMElement
     */
    public Map<QName, OMElement> getAllReferenceParameters() {
        return referenceParameters;
    }

    public String getAddress() {
        return address;
    }

    /**
     * @param address - xs:anyURI
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<OMAttribute> getAddressAttributes() {
        return addressAttributes;
    }

    public void setAddressAttributes(ArrayList<OMAttribute> al) {
        addressAttributes = al;
    }

    public ArrayList<OMAttribute> getMetadataAttributes() {
        return metaDataAttributes;
    }

    public void setMetadataAttributes(ArrayList<OMAttribute> al) {
        metaDataAttributes = al;
    }
    
    /** 
     * This method identifies whether the address is a WS-Addressing spec defined
     * anonymous URI.
     * 
     * @return true if the address is a WS-Addressing spec defined anonymous URI.
     * 
     * @see #hasAnonymousAddress()
     */ 
    public boolean isWSAddressingAnonymous() {
        return (AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(address) ||
                AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(address));
    }
    
    /**
     * This method is used to identify when response messages should be sent using
     * the back channel of a two-way transport.
     *
     * @return true if the address is a WS-Addressing spec defined anonymous URI,
     * or starts with a string that is specified to be equivalent to an anonymous
     * URI.
     * 
     * @see #addAnonymousEquivalentURI(String)
     */
    public boolean hasAnonymousAddress() {
        boolean result = isWSAddressingAnonymous();
        
        if(!result && address != null) {
        	// If the address is not WS-A anonymous it might still be considered anonymous
        	// Avoid synchronization by using the
            // technique outlined at http://is.gd/gv3
        	List<String> localList = anonymousEquivalentURIs;
        	if(!localList.isEmpty()){
        		Iterator<String> it = localList.iterator();
        		while(it.hasNext()){
        			result = address.startsWith((String)it.next());
        			if(result){
        				break;
        			}
        		}	
        	}    	
        }

        if (log.isTraceEnabled()) {
            log.trace("hasAnonymousAddress: " + address + " is Anonymous: " + result);
        }
        
        return result;
    }

    /**
     * hasNoneAddress
     *
     * @return true if the address is the 'None URI' from the final addressing spec.
     */
    public boolean hasNoneAddress() {
        boolean result = AddressingConstants.Final.WSA_NONE_URI.equals(address);
        if (log.isTraceEnabled()) {
            log.trace("hasNoneAddress: " + address + " is None: " + result);
        }
        return result;
    }

    /**
     * @param localName
     * @param ns
     * @param value
     */
    public void addAttribute(String localName, OMNamespace ns, String value) {
        if (attributes == null) {
            attributes = new ArrayList<OMAttribute>();
        }
        attributes.add(OMAbstractFactory.getOMFactory().createOMAttribute(localName, ns, value));
    }

    public ArrayList<OMAttribute> getAttributes() {
        return attributes;
    }


    /**
     * @param omAttribute
     */
    public void addAttribute(OMAttribute omAttribute) {
        if (attributes == null) {
            attributes = new ArrayList<OMAttribute>();
        }
        attributes.add(omAttribute);
    }

    public ArrayList<OMElement> getExtensibleElements() {
        return extensibleElements;
    }

    /**
     * {any}
     *
     * @param extensibleElements
     */
    public void setExtensibleElements(ArrayList<OMElement> extensibleElements) {
        this.extensibleElements = extensibleElements;
    }

    public void addExtensibleElement(OMElement extensibleElement) {
        if (extensibleElement != null) {
            if (this.extensibleElements == null) {
                this.extensibleElements = new ArrayList<OMElement>();
            }
            this.extensibleElements.add(extensibleElement);
        }
    }

    public ArrayList<OMNode> getMetaData() {
        return metaData;
    }

    public void addMetaData(OMNode metaData) {
        if (metaData != null) {
            if (this.metaData == null) {
                this.metaData = new ArrayList<OMNode>();
            }
            this.metaData.add(metaData);
        }

    }

    /**
     * @deprecated
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * @deprecated
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set a Map with QName as the key and an OMElement
     * as the value
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(Map<QName, OMElement> referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

    /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Address: " + address);

        if (addressAttributes != null) {
            buffer.append(", Address Attributes: ").append(addressAttributes);
        }

        if (metaData != null) {
            buffer.append(", Metadata: ").append(metaData);
        }
        
        if (metaDataAttributes != null) {
            buffer.append(", Metadata Attributes: ").append(metaDataAttributes);
        }

        if (referenceParameters != null) {
            buffer.append(", Reference Parameters: ").append(referenceParameters);
        }

        if (extensibleElements != null) {
            buffer.append(", Extensibility elements: ").append(extensibleElements);
        }

        if (attributes != null) {
            buffer.append(", Attributes: ").append(attributes);
        }

        return buffer.toString();
    }

    /**
     * @param eprOMElement
     * @deprecated use {@link EndpointReferenceHelper#fromOM(OMElement)} instead.
     */
    public void fromOM(OMElement eprOMElement) {
        OMElement addressElement = eprOMElement.getFirstChildWithName(new QName("Address"));
        setAddress(addressElement.getText());
        Iterator allAddrAttributes = addressElement.getAllAttributes();
        if (addressAttributes == null) {
            addressAttributes = new ArrayList<OMAttribute>();
        }

        while (allAddrAttributes.hasNext()) {
            OMAttribute attribute = (OMAttribute) allAddrAttributes.next();
            addressAttributes.add(attribute);
        }


        OMElement refParamElement = eprOMElement
                .getFirstChildWithName(new QName(AddressingConstants.EPR_REFERENCE_PARAMETERS));

        if (refParamElement != null) {
            Iterator refParams = refParamElement.getChildElements();
            while (refParams.hasNext()) {
                OMElement omElement = (OMElement) refParams.next();
                addReferenceParameter(omElement);
            }
        }


        OMElement metaDataElement = eprOMElement
                .getFirstChildWithName(new QName(AddressingConstants.Final.WSA_METADATA));
        if (metaDataElement != null) {
            Iterator children = metaDataElement.getChildren();
            while (children.hasNext()) {
                OMNode omNode = (OMNode) children.next();
                addMetaData(omNode);
            }
        }

        setName(eprOMElement.getLocalName());

        Iterator allAttributes = eprOMElement.getAllAttributes();
        if (attributes == null) {
            attributes = new ArrayList();
        }

        while (allAttributes.hasNext()) {
            OMAttribute attribute = (OMAttribute) allAttributes.next();
            attributes.add(attribute);
        }

        Iterator childElements = eprOMElement.getChildElements();
        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            String localName = eprChildElement.getLocalName();
            if (!localName.equals("Address") &&
                    !localName.equals(AddressingConstants.EPR_REFERENCE_PARAMETERS) &&
                    !localName.equals(AddressingConstants.Final.WSA_METADATA)) {
                addExtensibleElement(eprChildElement);
            }
        }
    }

    /**
     * @param nsurl
     * @param localName
     * @param prefix
     * @throws AxisFault
     * @deprecated use {@link EndpointReferenceHelper#toOM(EndpointReference, QName, String)} instead.
     */
    public OMElement toOM(String nsurl, String localName, String prefix) throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        if (prefix != null) {
            OMNamespace wrapNs = fac.createOMNamespace(nsurl, prefix);
            OMElement epr = fac.createOMElement(localName, wrapNs);
            OMNamespace wsaNS = fac.createOMNamespace(AddressingConstants.Final.WSA_NAMESPACE,
                                                      AddressingConstants.WSA_DEFAULT_PREFIX);
            OMElement addressE = fac.createOMElement(AddressingConstants.EPR_ADDRESS, wsaNS, epr);
            addressE.setText(address);

            if (addressAttributes != null) {
                Iterator attrIter = addressAttributes.iterator();
                while (attrIter.hasNext()) {
                    OMAttribute omAttributes = (OMAttribute) attrIter.next();
                    addressE.addAttribute(omAttributes);
                }
            }

            if (this.metaData != null) {
                OMElement metadataE =
                        fac.createOMElement(AddressingConstants.Final.WSA_METADATA, wsaNS, epr);
                Iterator metadata = this.metaData.iterator();
                while (metadata.hasNext()) {
                    metadataE.addChild((OMNode) metadata.next());
                }
            }

            if (this.referenceParameters != null) {
                OMElement refParameterElement =
                        fac.createOMElement(AddressingConstants.EPR_REFERENCE_PARAMETERS,
                                            wsaNS,
                                            epr);
                Iterator refParms = referenceParameters.values().iterator();
                while (refParms.hasNext()) {
                    refParameterElement.addChild((OMNode) refParms.next());
                }
            }

            if (attributes != null) {
                Iterator attrIter = attributes.iterator();
                while (attrIter.hasNext()) {
                    OMAttribute omAttributes = (OMAttribute) attrIter.next();
                    epr.addAttribute(omAttributes);
                }
            }

            // add xs:any
            ArrayList omElements = extensibleElements;
            if (omElements != null) {
                for (int i = 0; i < omElements.size(); i++) {
                    epr.addChild((OMElement) omElements.get(i));
                }
            }

            return epr;
        } else {
            throw new AxisFault("prefix must be specified");
        }
    }

    /**
     * Compares key parts of the state from the current instance of
     * this class with the specified instance to see if they are
     * equivalent.
     * <p/>
     * This differs from the java.lang.Object.equals() method in
     * that the equals() method generally looks at both the
     * object identity (location in memory) and the object state
     * (data).
     * <p/>
     *
     * @param epr The object to compare with
     * @return TRUE if this object is equivalent with the specified object
     *         that is, key fields match
     *         FALSE, otherwise
     */
    public boolean isEquivalent(EndpointReference epr) {
        // NOTE: the input object is expected to exist (ie, be non-null)

        if ((this.name != null) && (epr.getName() != null)) {
            if (!this.name.equals(epr.getName())) {
                return false;
            }
        } else if ((this.name == null) && (epr.getName() == null)) {
            // continue
        } else {
            // mismatch
            return false;
        }


        if ((this.address != null) && (epr.getAddress() != null)) {
            if (!this.address.equals(epr.getAddress())) {
                return false;
            }
        } else if ((this.address == null) && (epr.getAddress() == null)) {
            // continue
        } else {
            // mismatch
            return false;
        }

        // TODO: is a strict test ok to use?

        ArrayList<OMNode> eprMetaData = epr.getMetaData();

        if ((this.metaData != null) && (eprMetaData != null)) {
            if (!this.metaData.equals(eprMetaData)) {
                // This is a strict test
                // Returns true if and only if the specified object
                // is also a list, both lists have the same size, and
                // all corresponding pairs of elements in the two lists
                // are equal, ie, two lists are defined to be equal if
                // they contain the same elements in the same order.

                return false;
            }
        } else if ((this.metaData == null) && (eprMetaData == null)) {
            // keep going
        } else {
            // one of the lists is null
            return false;
        }


        ArrayList<OMElement> eprExtensibleElements = epr.getExtensibleElements();

        if ((this.extensibleElements != null) && (eprExtensibleElements != null)) {
            if (!this.extensibleElements.equals(eprExtensibleElements)) {
                // This is a strict test
                // Returns true if and only if the specified object
                // is also a list, both lists have the same size, and
                // all corresponding pairs of elements in the two lists
                // are equal, ie, two lists are defined to be equal if
                // they contain the same elements in the same order.

                return false;
            }
        } else if ((this.extensibleElements == null) && (eprExtensibleElements == null)) {
            // keep going
        } else {
            // one of the lists is null
            return false;
        }


        ArrayList<OMAttribute> eprAttributes = epr.getAttributes();

        if ((this.attributes != null) && (eprAttributes != null)) {
            if (!this.attributes.equals(eprAttributes)) {
                // This is a strict test
                // Returns true if and only if the specified object
                // is also a list, both lists have the same size, and
                // all corresponding pairs of elements in the two lists
                // are equal, ie, two lists are defined to be equal if
                // they contain the same elements in the same order.

                return false;
            }
        } else if ((this.attributes == null) && (eprAttributes == null)) {
            // keep going
        } else {
            // one of the lists is null
            return false;
        }

        // TODO: check the Map referenceParameters for equivalency

        return true;
    }

    //REVIEW: The following code is rather heavyweight, because we have to build 
    //   the OM tree -- it would probably be better to have two serialization/deserialization 
    //   paths and therefore, for trivial EPRs, store a smaller amount of info  

    /**
     * Write the EPR to the specified OutputStream.  Because of potential
     * OMElements/Attributes, we need to actually serialize the OM structures
     * (at least in some cases.)
     */
    public synchronized void writeExternal(java.io.ObjectOutput o)
            throws IOException {
        SafeObjectOutputStream out = SafeObjectOutputStream.install(o);
        
        // revision ID
        out.writeInt(revisionID);
        
        // Correlation ID
        String logCorrelationIDString = getLogCorrelationIDString();

        // String object id
        out.writeObject(logCorrelationIDString);

        // Write out the content as xml
        out.writeUTF("start xml"); // write marker
        OMElement om =
                EndpointReferenceHelper.toOM(OMAbstractFactory.getOMFactory(),
                                             this,
                                             new QName("urn:axis2", "omepr", "ser"),
                                             AddressingConstants.Final.WSA_NAMESPACE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            om.serialize(baos);
        } catch (Exception e) {
            IOException ioe = new IOException("Unable to serialize the EndpointReference with logCorrelationID ["
                                              +logCorrelationIDString+"]"); 
            ioe.initCause(e);

            if (log.isDebugEnabled()) {
                log.debug("writeObject(): Unable to serialize the EPR with logCorrelationID ["
                          +logCorrelationIDString+"]   original error ["+e.getClass().getName()
                          +"]  message ["+e.getMessage()+"]",e);  
            }

            throw ioe;
        }

        out.writeInt(baos.size());
        out.write(baos.toByteArray());  
        out.writeUTF("end xml"); // write marker

        if (log.isDebugEnabled()) {
            byte[] buffer = baos.toByteArray();
            String content = new String(buffer);

            log.debug("writeObject(): EPR logCorrelationID ["+logCorrelationIDString+"] "    
                      +"    EPR content size ["+baos.size()+"]"
                      +"    EPR content ["+content+"]"); 
        }

    }

    /**
     * Read the EPR to the specified InputStream.
     */
    public void readExternal(java.io.ObjectInput inObject)
            throws IOException, ClassNotFoundException {
        SafeObjectInputStream in = SafeObjectInputStream.install(inObject);
        
        // revision ID
        int revID = in.readInt();
        
        // make sure the object data is in a revision level we can handle
        if (revID != REVISION_2) {
            throw new ClassNotFoundException(ExternalizeConstants.UNSUPPORTED_REVID);
        }
        
        // String object id
        logCorrelationIDString = (String) in.readObject();

        // Read xml content
        in.readUTF(); // read marker
        int numBytes = in.readInt();

        byte[] serBytes = new byte[numBytes];

        // read the data from the input stream

        int bytesRead = 0;
        int numberOfBytesLastRead;

        while (bytesRead < numBytes) {
            numberOfBytesLastRead = in.read(serBytes, bytesRead, numBytes - bytesRead);

            if (numberOfBytesLastRead == -1) {
                // TODO: What should we do if the reconstitution fails?
                // For now, log the event and throw an exception
                if (log.isDebugEnabled()) {
                    log.debug("readObject(): EPR logCorrelationID ["+logCorrelationIDString+"] "    
                            + " ***WARNING*** unexpected end to data:    data read from input stream ["
                            + bytesRead + "]    expected data size [" + numBytes + "]");
                }

                IOException ioe = new IOException("Unable to deserialize the EndpointReference with logCorrelationID ["
                                                  +logCorrelationIDString+"]"
                                                  +"  Cause: Unexpected end to data from input stream"); 

                throw ioe;
            }

            bytesRead += numberOfBytesLastRead;
        }


        if (bytesRead == 0) {
            IOException ioe = new IOException("Unable to deserialize the EndpointReference with logCorrelationID ["
                                              +logCorrelationIDString+"]"
                                              +"  Cause: No data from input stream"); 

            throw ioe;
        }
        in.readUTF(); // read marker

        ByteArrayInputStream bais = new ByteArrayInputStream(serBytes);

        if (log.isDebugEnabled()) {
            String content = new String(serBytes);

            log.debug("readObject(): EPR logCorrelationID ["+logCorrelationIDString+"] "    
                      +"    expected content size ["+numBytes+"]"
                      +"    content size ["+content.length()+"]"
                      +"    EPR buffered content ["+content+"]"); 
        }

        XMLStreamReader xmlReader = null;

        try {
            OMElement om = OMXMLBuilderFactory.createOMBuilder(bais).getDocumentElement();

            // expand the OM so we can close the stream reader
            om.build();

            // trace point
            if (log.isDebugEnabled()) {
                log.debug(myClassName + ":readObject():  "  
                          + " EPR ["+logCorrelationIDString + "]"
                          + " EPR OM content ["+om.toString()+ "]");
            }

            EndpointReferenceHelper.fromOM(this, om, AddressingConstants.Final.WSA_NAMESPACE);


        } catch (Exception e) {
            IOException ioe = new IOException("Unable to deserialize the EndpointReference with logCorrelationID ["
                                              +logCorrelationIDString+"]"); 
            ioe.initCause(e);

            if (log.isDebugEnabled()) {
                log.debug("readObject(): Unable to deserialize the EPR with logCorrelationID ["
                          +logCorrelationIDString+"]   original error ["+e.getClass().getName()
                          +"]  message ["+e.getMessage()+"]",e);  
            }

            throw ioe;

        } finally {
        	// Make sure that the reader is properly closed
            // Note that closing a ByteArrayInputStream has no effect

            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (Exception e2) {
                    IOException ioe2 = new IOException("Unable to close the XMLStreamReader for the EndpointReference with logCorrelationID ["
                                                      +logCorrelationIDString+"]"); 
                    ioe2.initCause(e2);

                    if (log.isDebugEnabled()) {
                        log.debug("readObject(): Unable to close the XMLStreamReader for the EPR with logCorrelationID ["
                                  +logCorrelationIDString+"]   original error ["+e2.getClass().getName()
                                  +"]  message ["+e2.getMessage()+"]",e2);  
                    }

                    throw ioe2;
                }
            }
        }
    }

    /**
     * Get the ID associated with this object instance.
     *
     * @return A string that can be output to a log file as an identifier
     *         for this object instance.  It is suitable for matching related log
     *         entries.
     */
    public String getLogCorrelationIDString() {
        if (logCorrelationIDString == null) {
            logCorrelationIDString = myClassName + "@" + UIDGenerator.generateUID();
        }
        return logCorrelationIDString;
    }

}
