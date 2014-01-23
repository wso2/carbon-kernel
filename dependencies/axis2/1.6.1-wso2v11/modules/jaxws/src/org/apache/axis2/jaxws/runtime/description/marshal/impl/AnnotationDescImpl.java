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

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.security.PrivilegedAction;

/**
 * Common annotation information for a class. This is setup once
 * so that and stored in the AnnotationDesc so that it is easily accessible.
 */
class AnnotationDescImpl implements AnnotationDesc {

    private boolean _hasXmlRootElement = false;
    private String _XmlRootElementName = null;
    private String _XmlRootElementNamespace = null;
    private Class[] _XmlSeeAlsoClasses = null;
    private boolean _hasXmlType = false;
    private String _XmlTypeName = null;
    private String _XmlTypeNamespace = null;

    private AnnotationDescImpl() {
        super();
    }

    public boolean hasXmlRootElement() {
        return _hasXmlRootElement;
    }

    public String getXmlRootElementName() {
        return _XmlRootElementName;
    }

    public String getXmlRootElementNamespace() {
        return _XmlRootElementNamespace;
    }
    
    public boolean hasXmlType() {
        return _hasXmlType;
    }
    
    
    public String getXmlTypeName() {
        return _XmlTypeName;
    }
    
    public String getXmlTypeNamespace() {
        return _XmlTypeNamespace;
    }

    static AnnotationDesc create(Class cls) {
        AnnotationDescImpl aDesc = new AnnotationDescImpl();

        // XMLSeeAlso is part of JAXB 2.1.2.  
        // The assumption is that this is a prereq for JAXWS 2.1; thus
        // we can safely reference this class
        XmlSeeAlso xmlSeeAlso = (XmlSeeAlso)
            getAnnotation(cls, XmlSeeAlso.class);
        
        if (xmlSeeAlso != null) {
            aDesc._XmlSeeAlsoClasses = xmlSeeAlso.value();
        }
        
        QName qName = XMLRootElementUtil.getXmlRootElementQName(cls);
        if (qName != null) {
            aDesc._hasXmlRootElement = true;
            aDesc._XmlRootElementName = qName.getLocalPart();
            aDesc._XmlRootElementNamespace = qName.getNamespaceURI();
        }
        
        qName = XMLRootElementUtil.getXmlTypeQName(cls);
        if (qName != null) {
            aDesc._hasXmlType = true;
            aDesc._XmlTypeName = qName.getLocalPart();
            aDesc._XmlTypeNamespace = qName.getNamespaceURI();
        }
        return aDesc;
    }

    public String toString() {
        final String newline = "\n";
        StringBuffer string = new StringBuffer();

        string.append(newline);
        string.append("      @XMLRootElement exists = " + this.hasXmlRootElement());

        if (this.hasXmlRootElement()) {
            string.append(newline);
            string.append("      @XMLRootElement namespace = " + this.getXmlRootElementNamespace());
            string.append(newline);
            string.append("      @XMLRootElement name      = " + this.getXmlRootElementName());
        }
        
        string.append(newline);
        string.append("      @XmlType exists = " + this.hasXmlType());
        if (this.hasXmlRootElement()) {
            string.append(newline);
            string.append("      @XmlType namespace = " + this.getXmlTypeNamespace());
            string.append(newline);
            string.append("      @XmlType name      = " + this.getXmlTypeName());
        }

        if (this._XmlSeeAlsoClasses != null) {
            for (int i=0; i<_XmlSeeAlsoClasses.length; i++) {
                string.append(newline);
                string.append("      @XMLSeeAlso class = " + this._XmlSeeAlsoClasses[i].getName());
            }
        }

        return string.toString();
    }

    public Class[] getXmlSeeAlsoClasses() {
        return _XmlSeeAlsoClasses;
    }
    
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final AnnotatedElement element, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotation);
            }
        });
    }
}
