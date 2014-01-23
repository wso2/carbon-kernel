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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.MTOMXMLStreamWriter;
import org.apache.axiom.om.util.XMLStreamWriterRemoveIllegalChars;
import org.apache.axiom.util.stax.XMLStreamReaderUtils;
import org.apache.axiom.util.stax.xop.MimePartProvider;
import org.apache.axiom.util.stax.xop.XOPEncodedStream;
import org.apache.axiom.util.stax.xop.XOPUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.context.utils.ContextUtils;
import org.apache.axis2.jaxws.message.OccurrenceArray;
import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.message.util.XMLStreamWriterWithOS;
import org.apache.axis2.jaxws.spi.Constants;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.axis2.jaxws.utility.XmlEnumUtils;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/*
 * To marshal or unmarshal a JAXB object, the JAXBContext is necessary.
 * In addition, access to the MessageContext and other context objects may be necessary
 * to get classloader information, store attachments etc.
 * 
 * The JAXBDSContext bundles all of this information together.
 */
public class JAXBDSContext {

    private static final Log log = LogFactory.getLog(JAXBDSContext.class);
    public static final boolean DEBUG_ENABLED = log.isDebugEnabled();

    private TreeSet<String> contextPackages;  // List of packages needed by the context
    private String contextPackagesKey;        // Unique key that represents the set of packages
    
    private JAXBContext customerJAXBContext;      // JAXBContext provided by the customer api
    //  JAXBContext loaded by the engine.  It is weakref'd to allow GC
    private WeakReference<JAXBContext> autoJAXBContext = null;   
    private JAXBUtils.CONSTRUCTION_TYPE       // How the JAXBContext is constructed
            constructionType = JAXBUtils.CONSTRUCTION_TYPE.UNKNOWN;
    private MessageContext msgContext;    

    // There are two modes of marshalling and unmarshalling: 
    //   "by java type" and "by schema element".
    // The prefered mode is "by schema element" because it is safe and xml-centric.
    // However there are some circumstances when "by schema element" is not available.
    //    Examples: RPC Lit processing (the wire element is defined by a wsdl:part...not schema)
    //              Doc/Lit Bare "Minimal" Processing (JAXB ObjectFactories are missing...
    //                   and thus we must use "by type" for primitives/String)
    // Please don't use "by java type" processing to get around errors.
    private Class processType = null;
    private boolean isxmlList =false;
    
    private String webServiceNamespace;

    /**
     * Full Constructor JAXBDSContext (most performant)
     *
     * @param packages Set of packages needed by the JAXBContext.
     */
    public JAXBDSContext(TreeSet<String> packages, String packagesKey) {
        this.contextPackages = packages;
        this.contextPackagesKey = packagesKey;
    }

    /**
     * Slightly slower constructor
     *
     * @param packages
     */
    public JAXBDSContext(TreeSet<String> packages) {
        this(packages, packages.toString());
    }

    /**
     * Normal Constructor JAXBBlockContext
     *
     * @param contextPackage
     * @deprecated
     */
    public JAXBDSContext(String contextPackage) {
        this.contextPackages = new TreeSet();
        this.contextPackages.add(contextPackage);
        this.contextPackagesKey = this.contextPackages.toString();
    }

    /**
     * "Dispatch" Constructor 
     * Use this full constructor when the JAXBContent is provided by the
     * customer.
     *
     * @param jaxbContext
     */
    public JAXBDSContext(JAXBContext jaxbContext) {
        this.customerJAXBContext = jaxbContext;
    }

    /** @return Class representing type of the element */
    public TreeSet<String> getContextPackages() {
        return contextPackages;
    }
    
    public JAXBContext getJAXBContext() throws JAXBException {
        
        return getJAXBContext(null);
    }

    /**
     * @return get the JAXBContext
     * @throws JAXBException
     */
    public JAXBContext getJAXBContext(ClassLoader cl) throws JAXBException {
        return getJAXBContext(cl, false);
    }
    
    /**
     * @param ClassLoader
     * @param forceArrays boolean (if true, then JAXBContext will automatically contain arrays)
     * @return get the JAXBContext
     * @throws JAXBException
     */
    public JAXBContext getJAXBContext(ClassLoader cl, boolean forceArrays) throws JAXBException {
        if (customerJAXBContext != null) {
            return customerJAXBContext;
        }
        
        // Get the weakly cached JAXBContext
        JAXBContext jc = null;
        if (autoJAXBContext != null) {
            jc = autoJAXBContext.get();
        }
        
        if (forceArrays && 
                jc != null &&
                constructionType != JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY_PLUS_ARRAYS) {
            if (log.isDebugEnabled()) {
                log.debug("A JAXBContext exists but it was not constructed with array class.  " +
                    "The JAXBContext will be rebuilt.");
            }
            jc = null;
        }
        
        if (jc == null) {
            if (log.isDebugEnabled()) {
                log.debug("Creating a JAXBContext with the context packages.");
            }
            Holder<JAXBUtils.CONSTRUCTION_TYPE> constructType =
                    new Holder<JAXBUtils.CONSTRUCTION_TYPE>();
            Map<String, Object> properties = null;
            
            /*
             * We set the default namespace to the web service namespace to fix an
             * obscure bug.
             * 
             * If the class representing a JAXB data object does not define a namespace
             * (via an annotation like @XmlType or via ObjectFactory or schema gen information)
             * then the namespace information is defaulted.
             * 
             * The xjc tool defaults the namespace information to unqualified.
             * However the wsimport tool defaults the namespace to the namespace of the
             * webservice.
             * 
             * To "workaround" this issue, a default namespace equal to the webservice
             * namespace is set on the JAXB marshaller.  This has the effect of changing the
             * "unqualified namespaces" into the namespace used by the webservice.
             * 
             */
            if (this.webServiceNamespace != null) {
                properties = new HashMap<String, Object>();
                properties.put(JAXBUtils.DEFAULT_NAMESPACE_REMAP, this.webServiceNamespace);
            }
            jc = JAXBUtils.getJAXBContext(contextPackages, constructType, forceArrays,
                                          contextPackagesKey, cl, properties);
            constructionType = constructType.value;
            autoJAXBContext = new WeakReference<JAXBContext>(jc);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using an existing JAXBContext");
            }
        }
        return jc;
    }

    public void setWebServiceNamespace(String namespace) {
        this.webServiceNamespace = namespace;
    }
    
    /** @return RPC Declared Type */
    public Class getProcessType() {
        return processType;
    }

    /**
     * The procesess type to indicate the class of the target of the unmarshaling.
     * This method should only be used in the cases where the element being unmarshaled
     * is not known to the JAXBContext (examples include RPC/Literal processing
     * and Doc/Literal Wrapped processing with a non-element wrapper class)
     *
     * @param type
     */
    public void setProcessType(Class type) {
    	if (log.isDebugEnabled()) {
     		log.debug("Process Type set to: " + type);
     	}
        processType = type;
    }

    public JAXBUtils.CONSTRUCTION_TYPE getConstructionType() {
        return constructionType;
    }

    public boolean isxmlList() {
        return isxmlList;
    }

    public void setIsxmlList(boolean isxmlList) {
    	if (log.isDebugEnabled()) {
     		log.debug("isxmlListSet to " + isxmlList);
     	}
        this.isxmlList = isxmlList;
    }
    
    public MessageContext getMessageContext() {
        return msgContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.msgContext = messageContext;
    }
    
    public ClassLoader getClassLoader() {
        MessageContext context = getMessageContext();
        if (context != null) {
            Parameter param = context.getParameter(Constants.CACHE_CLASSLOADER);
            if (param != null) {
                return (ClassLoader) param.getValue();
            }
        }
        return null;
    }
    
    /**
     * Create an AttachmentMarshaller to marshal MTOM/SWA Attachments
     * @param writer
     * @return
     */
    protected AttachmentMarshaller createAttachmentMarshaller(XMLStreamWriter writer) {
        return new JAXBAttachmentMarshaller(getMessageContext(), writer);
    }
    
    /**
     * Create an Attachment unmarshaller for unmarshalling MTOM/SWA Attachments
     * @return AttachmentUnmarshaller
     */
    protected AttachmentUnmarshaller createAttachmentUnmarshaller(MimePartProvider mimePartProvider) {
        return new JAXBAttachmentUnmarshaller(mimePartProvider, getMessageContext());
    }

    /**
     * Unmarshal the xml into a JAXB object
     * @param inputReader
     * @return
     * @throws JAXBException
     */
    public Object unmarshal(XMLStreamReader inputReader) throws JAXBException {

        if (DEBUG_ENABLED) {
            String clsText = (inputReader !=null) ? inputReader.getClass().toString() : "null";
            log.debug("unmarshal with inputReader=" + clsText);
        } 
        // See the Javadoc of the CustomBuilder interface for a complete explanation of
        // the following two instructions:
        XOPEncodedStream xopEncodedStream = XOPUtils.getXOPEncodedStream(inputReader);
        XMLStreamReader reader = XMLStreamReaderUtils.getOriginalXMLStreamReader(xopEncodedStream.getReader());
        if (DEBUG_ENABLED) {
            String clsText = (reader !=null) ? reader.getClass().toString() : "null";
            log.debug("  originalReader=" + clsText);
        } 
        
        // There may be a preferred classloader that should be used
        ClassLoader cl = getClassLoader();
        
        Unmarshaller u = JAXBUtils.getJAXBUnmarshaller(getJAXBContext(cl));

        
        // Create an attachment unmarshaller
        AttachmentUnmarshaller aum = createAttachmentUnmarshaller(xopEncodedStream.getMimePartProvider());

        if (aum != null) {
            if (DEBUG_ENABLED) {
                log.debug("Adding JAXBAttachmentUnmarshaller to Unmarshaller");
            } 
            u.setAttachmentUnmarshaller(aum);
        }

        Object jaxb = null;

        // Unmarshal into the business object.
        if (getProcessType() == null) {
            jaxb = unmarshalByElement(u, reader);   // preferred and always used for
                                                    // style=document
        } else {
            jaxb = unmarshalByType(u,
                                   reader,
                                   getProcessType(),
                                   isxmlList(),
                                   getConstructionType());
        }

        // Successfully unmarshalled the object
        JAXBUtils.releaseJAXBUnmarshaller(getJAXBContext(cl), u);
        
        // Don't close the reader.  The reader is owned by the caller, and it
        // may contain other xml instance data (other than this JAXB object)
        // reader.close();
        return jaxb;
    }
    
    /**
     * Marshal the jaxb object
     * @param obj
     * @param writer
     * @param am AttachmentMarshaller, optional Attachment
     */
    public void marshal(Object obj, 
            XMLStreamWriter writer) throws JAXBException {
        if (log.isDebugEnabled()) {
            log.debug("enter marshal");
        }
        boolean installedFilter = false;

        try {
            // There may be a preferred classloader that should be used
            ClassLoader cl = getClassLoader();


            // Very easy, use the Context to get the Marshaller.
            // Use the marshaller to write the object.
            JAXBContext jbc = getJAXBContext(cl);
            Marshaller m = JAXBUtils.getJAXBMarshaller(jbc);
            if (writer instanceof MTOMXMLStreamWriter && ((MTOMXMLStreamWriter) writer).getOutputFormat() != null) {
                String encoding = ((MTOMXMLStreamWriter) writer).getOutputFormat().getCharSetEncoding();

                String marshallerEncoding = (String) m.getProperty(Marshaller.JAXB_ENCODING);

                // Make sure that the marshaller respects the encoding of the message.
                // This is accomplished by setting the encoding on the Marshaller's JAXB_ENCODING property.
                if (encoding == null && marshallerEncoding == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("The encoding and the marshaller's JAXB_ENCODING are both set to the default (UTF-8)");
                    }
                } else {
                    // Must set the encoding to an actual String to set it on the Marshaller
                    if (encoding == null) {
                        encoding = "UTF-8";
                    }
                    if (!encoding.equalsIgnoreCase(marshallerEncoding)) {
                        if (log.isDebugEnabled()) {
                            log.debug("The Marshaller.JAXB_ENCODING is " + marshallerEncoding);
                            log.debug("The Marshaller.JAXB_ENCODING is changed to the message encoding " + 
                                    encoding);
                        }
                        m.setProperty(Marshaller.JAXB_ENCODING, encoding);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("The encoding and the marshaller's JAXB_ENCODING are both set to:" + 
                                    marshallerEncoding);
                        }
                    }
                }
            }

            AttachmentMarshaller am = createAttachmentMarshaller(writer);
            if (am != null) {
                if (DEBUG_ENABLED) {
                    log.debug("Adding JAXBAttachmentMarshaller to Marshaller");
                }
                m.setAttachmentMarshaller(am);
            }

            MessageContext mc = getMessageContext();

            // If requested install a filter to remove illegal characters
            installedFilter = installFilter(mc, writer);


            // Marshal the object
            if (getProcessType() == null) {
                marshalByElement(obj, 
                        m, 
                        writer, 
                        true);
                //!am.isXOPPackage());
            } else {
                marshalByType(obj,
                        m,
                        writer,
                        getProcessType(),
                        isxmlList(),
                        getConstructionType(),
                        true); // Attempt to optimize by writing to OutputStream
            }

            JAXBUtils.releaseJAXBMarshaller(jbc, m);

            if (log.isDebugEnabled()) {
                log.debug("exit marshal");
            }
        } finally {
            // Make sure the filter is uninstalled
            if (installedFilter) {
                uninstallFilter(writer);
            }
        }
    }
    
    
    /**
     * Preferred way to marshal objects.
     * 
     * @param b Object that can be rendered as an element and the element name is known by the
     * Marshaller
     * @param m Marshaller
     * @param writer XMLStreamWriter
     */
    private static void marshalByElement(final Object b, final Marshaller m, 
                                         final XMLStreamWriter writer,
                                         final boolean optimize) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                // Marshalling directly to the output stream is faster than marshalling through the
                // XMLStreamWriter. 
                // Take advantage of this optimization if there is an output stream.
                try {
                    OutputStream os = (optimize) ? getOutputStream(writer,m) : null;
                    if (os != null) {
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByElement.  " +
                                        "Marshaling to an OutputStream. " +
                                      "Object is "
                                      + getDebugName(b));
                        }
                        writer.flush();
                        m.marshal(b, os);
                    } else {
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByElement.  " +
                                        "Marshaling to an XMLStreamWriter. " +
                                      "Object is "
                                      + getDebugName(b));
                        }
                        m.marshal(b, writer);
                    }
                } catch (OMException e) {
                    throw e;
                }
                catch (Throwable t) {
                    throw new OMException(t);
                }
                return null;
            }});
    }
    
    /**
     * Print out the name of the class of the specified object
     * @param o Object
     * @return text to use for debugging
     */
    private static String getDebugName(Object o) {
        String name = (o == null) ? "null" : o.getClass().getCanonicalName();
        if (o instanceof JAXBElement) {
            name += " containing " + getDebugName(((JAXBElement) o).getValue());
        }
        return name;
    }

    /**
     * If the writer is backed by an OutputStream, then return the OutputStream
     * @param writer
     * @param Marshaller
     * @return OutputStream or null
     */
    private static OutputStream getOutputStream(XMLStreamWriter writer, 
                Marshaller m) throws XMLStreamException {
        if (log.isDebugEnabled()) {
            log.debug("XMLStreamWriter is " + writer);
        }
        OutputStream os = null;
        if (writer.getClass() == MTOMXMLStreamWriter.class) {
            os = ((MTOMXMLStreamWriter) writer).getOutputStream();
            if (log.isDebugEnabled()) {
                log.debug("OutputStream accessible from MTOMXMLStreamWriter is " + os);
            }
        }
        if (writer.getClass() == XMLStreamWriterWithOS.class) {
            os = ((XMLStreamWriterWithOS) writer).getOutputStream();
            if (log.isDebugEnabled()) {
                log.debug("OutputStream accessible from XMLStreamWriterWithOS is " + os);
            }
        }
        if (os != null) {
            String marshallerEncoding = null;
            try {
                marshallerEncoding = (String) m.getProperty(Marshaller.JAXB_ENCODING);
            } catch (PropertyException e) {
                if (DEBUG_ENABLED) {
                    log.debug("Could not query JAXB_ENCODING..Continuing. " + e);
                }
            }
            if (marshallerEncoding != null && !marshallerEncoding.equalsIgnoreCase("UTF-8")) {
                if (DEBUG_ENABLED) {
                    log.debug("Wrapping output stream to remove BOM");
                }
                os = new BOMOutputStreamFilter(marshallerEncoding, os);
            }
        }

        return os;
    }
    
    /**
     * The root element being read is defined by schema/JAXB; however its contents are known by
     * schema/JAXB. Therefore we use unmarshal by the declared type (This method is used to
     * unmarshal rpc elements)
     * 
     * @param u Unmarshaller
     * @param reader XMLStreamReader
     * @param type Class
     * @return Object
     * @throws WebServiceException
     */
    public static Object unmarshalByType(final Unmarshaller u, final XMLStreamReader reader,
                                          final Class type, final boolean isList,
                                          final JAXBUtils.CONSTRUCTION_TYPE ctype)
        throws WebServiceException {

        if (DEBUG_ENABLED) {
            log.debug("Invoking unmarshalByType.");
            log.debug("  type = " + type);
            log.debug("  isList = " + isList);
            log.debug("  ctype = "+ ctype);
        }

        return AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    // Unfortunately RPC is type based. Thus a
                    // declared type must be used to unmarshal the xml.
                    Object jaxb;
                   
                    if (!isList) {
                        // case: We are not unmarshalling an xsd:list but an Array.

                        if (type.isArray()) {
                            // If the context is created using package
                            // we will not have common arrays or type array in the context
                            // but there is not much we can do about it so seralize it as
                            // usual
                            if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                                if (DEBUG_ENABLED) {
                                    log.debug("Unmarshal Array via BY_CONTEXT_PATH approach");
                                }
                                jaxb = u.unmarshal(reader, type);
                            }
                            // list on client array on server, Can happen only in start from java
                            // case.
                            else if ((ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY)) {
                                // The type could be any Object or primitive
                            	
                            	//process primitives first
                            	//first verify if we have a primitive type associated in the array.
                            	//array could be single dimension or multi dimension.
                            	Class cType = type.getComponentType();
                            	while(cType.isArray()){
                            		cType = cType.getComponentType();
                            	}
                            	if(cType.isPrimitive()){
                            	    if (DEBUG_ENABLED) {
                                        log.debug("Unmarshal Array of primitive via BY_CLASS_ARRAY approach");
                                    }
                            		jaxb = u.unmarshal(reader, type);
                            	}
                            	// process non primitive                       	
                                // I will first unmarshall the xmldata to a String[]
                                // Then use the unmarshalled jaxbElement to create
                                // proper type Object Array.
                            	
                            	else{
                            	    if (DEBUG_ENABLED) {
                                        log.debug("Unmarshal Array of non-primitive via BY_CLASS_ARRAY approach");
                                    }
                            		jaxb = unmarshalArray(reader, u, type);
                            	}
                                
                            } else {
                                if (DEBUG_ENABLED) {
                                    log.debug("Unmarshal Array");
                                }
                                jaxb = u.unmarshal(reader, type);
                                
                            }

                        } else if (type.isEnum()) {
                            // When JAXBContext is created using a context path, it will not 
                            // include Enum classes.
                            // These classes have @XmlEnum annotation but not @XmlType/@XmlElement,
                            // so the user will see MarshallingEx, class not known to ctxt.
                            // 
                            // This is a jax-b defect, for now this fix is in place to pass CTS.
                            // This only fixes the
                            // situation where the enum is the top-level object (e.g., message-part
                            // in rpc-lit scenario)
                            //
                            // Sample of what enum looks like:
                            // @XmlEnum public enum EnumString {
                            // @XmlEnumValue("String1") STRING_1("String1"),
                            // @XmlEnumValue("String2") STRING_2("String2");
                            //
                            // public static getValue(String){} <-- resolves a "value" to an emum
                            // object
                            // ... }
                            if (DEBUG_ENABLED) {
                                log.debug("Unmarshalling " + type.getName()
                                        + " as Enum");
                            }

                            JAXBElement<String> enumValue = u.unmarshal(reader, XmlEnumUtils.getConversionType(type));

                            if (enumValue != null) {
                                jaxb = XmlEnumUtils.fromValue(type, enumValue.getValue());
                            } else {
                                jaxb = null;
                            }
                        }
                        //Normal case: We are not unmarshalling a xsd:list or Array
                        else {
                            if (DEBUG_ENABLED) {
                                log.debug("Unmarshalling normal case (not array, not xsd:list, not enum)");
                            }
                            jaxb = u.unmarshal(reader, type);
                        }

                    } else {
                        // If this is an xsd:list, we need to return the appropriate
                        // list or array (see NOTE above)
                        // First unmarshal as a String
                        //Second convert the String into a list or array
                        if (DEBUG_ENABLED) {
                            log.debug("Unmarshalling xsd:list");
                        }
                        jaxb = unmarshalAsListOrArray(reader, u, type);
                        
                    }
                    if (log.isDebugEnabled()) {
                        Class cls;
                        if (jaxb == null) {
                            if (DEBUG_ENABLED) {
                                log.debug("End unmarshalByType returning null object");
                            }

                        } else if (jaxb instanceof JAXBElement) {
                            JAXBElement jbe = (JAXBElement) jaxb;
                            if (DEBUG_ENABLED) {
                                log.debug("End unmarshalByType returning JAXBElement");
                                log.debug("  Class = " + jbe.getDeclaredType());
                                log.debug("  QName = " + jbe.getName());
                            }
                        } else {
                            if (DEBUG_ENABLED) {
                                log.debug("End unmarshalByType returning " + jaxb.getClass());
                            }
                        }
                    }
                    return jaxb;
                } catch (OMException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new OMException(t);
                }
            }
        });
    }

    private static Object unmarshalArray(final XMLStreamReader reader, 
                                         final Unmarshaller u, 
                                         Class type)
       throws Exception {
        try {
            if (DEBUG_ENABLED) {
                log.debug("Invoking unmarshalArray");
            }
            Object jaxb = AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        return u.unmarshal(reader, String[].class);
                    } catch (OMException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new OMException(t);
                    }
                }
            });

            Object typeObj = getTypeEnabledObject(jaxb);

            // Now convert String Array in to the required Type Array.
            if (typeObj instanceof String[]) {
                String[] strArray = (String[]) typeObj;
                Object obj = XSDListUtils.fromStringArray(strArray, type);
                QName qName =
                    XMLRootElementUtil.getXmlRootElementQNameFromObject(jaxb);
                jaxb = new JAXBElement(qName, type, obj);
            }

            return jaxb;
        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }
   
    /**
     * convert the String into a list or array
     * @param <T>
     * @param jaxb
     * @param type
     * @return
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws DatatypeConfigurationException
     * @throws InvocationTargetException
     */
    public static Object unmarshalAsListOrArray(final XMLStreamReader reader, 
                                                final Unmarshaller u, 
                                                 Class type)
        throws IllegalAccessException, ParseException,NoSuchMethodException,
        InstantiationException,
        DatatypeConfigurationException,InvocationTargetException,JAXBException {
        
        
            if (DEBUG_ENABLED) {
                log.debug("Invoking unmarshalAsListOrArray");
            }
            
            // If this is an xsd:list, we need to return the appropriate
            // list or array (see NOTE above)
            // First unmarshal as a String
            Object jaxb = null;
            try {
                jaxb = AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            return u.unmarshal(reader, String.class);
                        } catch (OMException e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new OMException(t);
                        }
                    }
                });
            } catch (OMException e) {
                throw e;
            } catch (Throwable t) {
                throw new OMException(t);
            }
            //Second convert the String into a list or array
            if (getTypeEnabledObject(jaxb) instanceof String) {
                QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(jaxb);
                Object obj = XSDListUtils.fromXSDListString((String) getTypeEnabledObject(jaxb), type);
                return new JAXBElement(qName, type, obj);
            } else {
                return jaxb;
            }

    }

    /**
     * Return type enabled object
     *
     * @param obj type or element enabled object
     * @return type enabled object
     */
    static Object getTypeEnabledObject(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JAXBElement) {
            return ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    private static boolean isOccurrenceArray(Object obj) {
        return (obj instanceof JAXBElement) &&
            (((JAXBElement)obj).getValue() instanceof OccurrenceArray);
                
    }
    /**
     * Marshal objects by type
     * 
     * @param b Object that can be rendered as an element, but the element name is not known to the
     * schema (i.e. rpc)
     * @param m Marshaller
     * @param writer XMLStreamWriter
     * @param type Class
     * @param isList true if this is an XmlList
     * @param ctype CONSTRUCTION_TYPE
     * @param optimize boolean set to true if optimization directly to 
     * outputstream should be attempted.
     */
    private void marshalByType(final Object b, final Marshaller m,
                                      final XMLStreamWriter writer, final Class type,
                                      final boolean isList, 
                                      final JAXBUtils.CONSTRUCTION_TYPE ctype,
                                      final boolean optimize) 
        throws WebServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Enter marshalByType b=" + getDebugName(b) + 
                        " type=" + type + 
                        " marshaller=" + m +
                        " writer=" + writer +
                        " isList=" + isList +
                        " ctype=" + ctype +
                        " optimize=" + optimize);
                        
        }
        if (isOccurrenceArray(b)) {
            marshalOccurrenceArray((JAXBElement) b, m, writer);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {

                    // NOTE
                    // Example:
                    // <xsd:simpleType name="LongList">
                    // <xsd:list>
                    // <xsd:simpleType>
                    // <xsd:restriction base="xsd:unsignedInt"/>
                    // </xsd:simpleType>
                    // </xsd:list>
                    // </xsd:simpleType>
                    // <element name="myLong" nillable="true" type="impl:LongList"/>
                    //
                    // LongList will be represented as an int[]
                    // On the wire myLong will be represented as a list of integers
                    // with intervening whitespace
                    // <myLong>1 2 3</myLong>
                    //
                    // Unfortunately, we are trying to marshal by type. Therefore
                    // we want to marshal an element (foo) that is unknown to schema.
                    // If we use the normal marshal code, the wire will look like
                    // this (which is incorrect):
                    // <foo><item>1</item><item>2</item><item>3</item></foo>
                    //
                    // The solution is to detect this situation and marshal the
                    // String instead. Then we get the correct wire format:
                    // <foo>1 2 3</foo>
                    Object jbo = b;
                    if(DEBUG_ENABLED){
                    	log.debug("check if marshalling list or array object, type = "+ (( b!=null )? b.getClass().getName():"null"));
                    }
                    if (isList) {                   	
                        if (DEBUG_ENABLED) {
                            log.debug("marshalling type which is a List");
                        }
                        
                        // This code assumes that the JAXBContext does not understand
                        // the array or list. In such cases, the contents are converted
                        // to a String and passed directly.
                        
                        if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CONTEXT_PATH) {
                            QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(b);
                            String text = XSDListUtils.toXSDListString(getTypeEnabledObject(b));
                            if (DEBUG_ENABLED) {
                                log.debug("marshalling [context path approach] " +
                                                "with xmllist text = " + text);
                            }
                            jbo = new JAXBElement(qName, String.class, text);
                        } else if (ctype == JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY) {
                            // Some versions of JAXB have array/list processing built in.
                            // This code is a safeguard because apparently some versions
                            // of JAXB don't.
                            QName qName = XMLRootElementUtil.getXmlRootElementQNameFromObject(b);
                            String text = XSDListUtils.toXSDListString(getTypeEnabledObject(b));
                            if (DEBUG_ENABLED) {
                                log.debug("marshalling [class array approach] " +
                                                "with xmllist text = " + text);
                            }
                            jbo = new JAXBElement(qName, String.class, text); 
                        }
                    }
                    // When JAXBContext is created using a context path, it will not include Enum
                    // classes.
                    // These classes have @XmlEnum annotation but not @XmlType/@XmlElement, so the
                    // user will see MarshallingEx, class not known to ctxt.
                    // 
                    // This is a jax-b defect, for now this fix is in place to pass CTS. This only
                    // fixes the
                    // situation where the enum is the top-level object (e.g., message-part in
                    // rpc-lit scenario)
                    //
                    // Sample of what enum looks like:
                    // @XmlEnum public enum EnumString {
                    // @XmlEnumValue("String1") STRING_1("String1"),
                    // @XmlEnumValue("String2") STRING_2("String2");
                    // ... }
                    if (type.isEnum()) {
                        if (b != null) {
                            if (DEBUG_ENABLED) {
                                log.debug("marshalByType. Marshaling " + type.getName()
                                        + " as Enum");
                            }
                            JAXBElement jbe = (JAXBElement) b;
                            String value = XMLRootElementUtil.getEnumValue((Enum) jbe.getValue());

                            jbo = new JAXBElement(jbe.getName(), String.class, value);
                        }
                    }

                    // If the output stream is available, marshal directly to it
                    OutputStream os = (optimize) ? getOutputStream(writer, m) : null;
                    if (os == null){ 
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByType.  " +
                                    "Marshaling to an XMLStreamWriter. Object is "
                                    + getDebugName(jbo));
                        }   
                        m.marshal(jbo, writer);
                    } else {
                        if (DEBUG_ENABLED) {
                            log.debug("Invoking marshalByType.  " +
                                    "Marshaling to an OutputStream. Object is "
                                    + getDebugName(jbo));
                        }   
                        m.marshal(jbo, os);
                    }

                } catch (OMException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new OMException(t);
                }
                return null;
            }
        });
    }

    /**
     * Marshal array objects by type
     * 
     * Invoke marshalByType for each element in the array
     * 
     * @param jaxb_in JAXBElement containing a value that is a List or array
     * @param m_in Marshaller
     * @param writer_in XMLStreamWriter
     */
    private void marshalOccurrenceArray(
                final JAXBElement jbe_in, 
                final Marshaller m_in,
                final XMLStreamWriter writer_in) {
        
        if (log.isDebugEnabled()) {
            log.debug("Enter marshalOccurrenceArray");
            log.debug("  Marshaller = " + JavaUtils.getObjectIdentity(m_in));
        }
        
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    
                    Marshaller m = m_in;
                    JAXBContext newJBC = null;
                    if (getConstructionType() != JAXBUtils.CONSTRUCTION_TYPE.BY_CLASS_ARRAY_PLUS_ARRAYS) {
                        // Rebuild JAXBContext
                        // There may be a preferred classloader that should be used
                        if (log.isDebugEnabled()) {
                            log.debug("Building a JAXBContext with array capability");
                        }
                        ClassLoader cl = getClassLoader();
                        newJBC = getJAXBContext(cl, true);
                        m = JAXBUtils.getJAXBMarshaller(newJBC);
                        if (log.isDebugEnabled()) {
                            log.debug("The new JAXBContext was constructed with " + getConstructionType());
                        }
                    }
                    

                    OccurrenceArray occurArray = (OccurrenceArray) jbe_in.getValue();

                    // Create a new JAXBElement.
                    // The name is the name of the individual occurence elements
                    // Type type is Object[]
                    // The value is the array of Object[] representing each element
                    JAXBElement jbe = new JAXBElement(jbe_in.getName(), 
                            Object[].class, 
                            occurArray.getAsArray());

                    // The jaxb marshal command cannot write out a list/array
                    // of occurence elements.  So we marshal it as a single
                    // element containing items...and then put a filter on the
                    // writer to transform it into a stream of occurence elements
                    XMLStreamWriterArrayFilter writer = new XMLStreamWriterArrayFilter(writer_in);


                    m.marshal(jbe, writer);
                    
                    if (newJBC != null) {
                        JAXBUtils.releaseJAXBMarshaller(newJBC, m);
                    }

                    return null;
                } catch (OMException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new OMException(t);
                }
            }
            });
            
        
        if (log.isDebugEnabled()) {
            log.debug("Exit marshalOccurrenceArray");
        }
        
    }
    
    /**
     * Preferred way to unmarshal objects
     * 
     * @param u Unmarshaller
     * @param reader XMLStreamReader
     * @return Object that represents an element
     * @throws WebServiceException
     */
    public static Object unmarshalByElement(final Unmarshaller u, final XMLStreamReader reader)
        throws WebServiceException {
        try {
            if (DEBUG_ENABLED) {
                log.debug("Invoking unMarshalByElement");
            }
            return AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        return u.unmarshal(reader);
                    } catch (OMException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new OMException(t);
                    }
                }
            });

        } catch (OMException e) {
            throw e;
        } catch (Throwable t) {
            throw new OMException(t);
        }
    }
    

    /**
     * Install a JAXB filter if requested
     * @param mc
     * @param writer
     * @return true if filter installed
     */
    private boolean installFilter(MessageContext mc, XMLStreamWriter writer) {
        if (!(writer instanceof MTOMXMLStreamWriter)) {
            return false;
        }
        if (!ContextUtils.isJAXBRemoveIllegalChars(mc)) {
            return false;
        }
        
         
        MTOMXMLStreamWriter mtomWriter = (MTOMXMLStreamWriter) writer;
        mtomWriter.setFilter(new XMLStreamWriterRemoveIllegalChars());
        return true;
    }
    
    /**
     * UninstallInstall a JAXB filter if requested
     * @param mc
     * @param writer
     * @return true if filter installed
     */
    private void uninstallFilter(XMLStreamWriter writer) {
        MTOMXMLStreamWriter mtomWriter = (MTOMXMLStreamWriter) writer;
        mtomWriter.removeFilter();
    }

}
