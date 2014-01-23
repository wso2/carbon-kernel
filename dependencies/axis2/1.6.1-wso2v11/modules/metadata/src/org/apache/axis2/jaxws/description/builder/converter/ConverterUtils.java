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

package org.apache.axis2.jaxws.description.builder.converter;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.FieldDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.HandlerChainAnnot;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.SoapBindingAnnot;
import org.apache.axis2.jaxws.description.builder.TMAnnotationComposite;
import org.apache.axis2.jaxws.description.builder.TMFAnnotationComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceRefAnnot;

import javax.jws.HandlerChain;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlList;
import javax.xml.ws.WebServiceRef;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class ConverterUtils {

    /**
     * Helper method to retrieve the annotation specified by a certain <code>Class</code>
     *
     * @param annotationClass - <code>Class</code> the annotation <code>Class</code>
     * @param element         - <code>AnnotatedElement</code> - the element on which we are looking for
     *                        the annotation (i.e. Class, Method, Field)
     * @return - <code>Annotation</code> annotation represented by the given <code>Class</code>
     */
    public static Annotation getAnnotation(final Class annotationClass, final AnnotatedElement element) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotationClass);
            }
        });
     }
    
    /**
     * Helper method to retrieve a list of all annotations that match the following
     * conditions:
     * 
     * - Annotations that extend the parameterized type T
     * - Annotations that themselves are annotated with type T
     * 
     * @param annotationClass
     * @param element
     * @return
     */
    public static <T extends Annotation> List<Annotation> getAnnotations(final Class<T> annotationClass, final AnnotatedElement element) {
        List<Annotation> matches = new ArrayList<Annotation>();
        Annotation[] annotations = null;
        
        // Get the complete list of annotations from the class that was provided.
        annotations = (Annotation[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotations();
            }
        });
        
        for (Annotation a: annotations) {        
            // If the annotation matches the parameter type we're looking
            // for, add it to the list.
            if (a.annotationType().isAnnotationPresent(annotationClass) || 
                annotationClass.isAssignableFrom(a.annotationType())) {
                matches.add(a);
            }
        }
        
        return matches;
    }

    /**
     * This is a helper method to create a <code>HandlerChainAnnot</code> since the
     *
     * @param handlerChain - <code>HandlerChain</code>
     * @return - <code>HandlerChainAnnot</code>
     * @HandlerChain annotation may be present on a Type, Method, or Field.
     */
    public static HandlerChainAnnot createHandlerChainAnnot(HandlerChain handlerChain) {
        HandlerChainAnnot hcAnnot = HandlerChainAnnot.createHandlerChainAnnotImpl();
        hcAnnot.setFile(handlerChain.file());
        hcAnnot.setName(handlerChain.name());
        return hcAnnot;
    }

    /**
     * This is a helper method to create a <code>SoapBindingAnnot</code> since the
     *
     * @param soapBinding - <code>SOAPBinding</code>
     * @return - <code>SoapBindingAnnot</code>
     * @SOAPBinding annotation may be present on a Type or Method.
     */
    public static SoapBindingAnnot createSoapBindingAnnot(SOAPBinding soapBinding) {
        SoapBindingAnnot sbAnnot = SoapBindingAnnot.createSoapBindingAnnotImpl();
        sbAnnot.setParameterStyle(soapBinding.parameterStyle());
        sbAnnot.setStyle(soapBinding.style());
        sbAnnot.setUse(soapBinding.use());
        return sbAnnot;
    }

    /**
     * This is a helper method to create a <code>WebServiceRefAnnot</code> since the
     *
     * @param webServiceRef - <code>WebServiceRef</code>
     * @return - <code>WebServiceRefAnnot</code>
     * @WebServiceRef annotation may be present on a Type, Method, or Field.
     */
    public static WebServiceRefAnnot createWebServiceRefAnnot(WebServiceRef webServiceRef) {
        WebServiceRefAnnot wsrAnnot = WebServiceRefAnnot.createWebServiceRefAnnotImpl();
        wsrAnnot.setMappedName(webServiceRef.mappedName());
        wsrAnnot.setName(webServiceRef.name());
        wsrAnnot.setType(webServiceRef.type());
        wsrAnnot.setValue(webServiceRef.value());
        wsrAnnot.setWsdlLocation(webServiceRef.wsdlLocation());
        return wsrAnnot;
    }

    /**
     * This method is use to attach @HandlerChain annotation data to a composite object.
     *
     * @param composite        - <code>TMFAnnotationComposite</code>
     * @param annotatedElement - <code>AnnotatedElement</code>
     */
    public static void attachHandlerChainAnnotation(TMFAnnotationComposite composite,
                                                    AnnotatedElement annotatedElement) {
        HandlerChain handlerChain = (HandlerChain)ConverterUtils.getAnnotation(
                HandlerChain.class, annotatedElement);
        if (handlerChain != null) {
            HandlerChainAnnot hcAnnot = ConverterUtils.createHandlerChainAnnot(
                    handlerChain);
            composite.setHandlerChainAnnot(hcAnnot);
        }
    }

    /**
     * This method is use to attach @SOAPBinding annotation data to a composite object.
     *
     * @param composite        - <code>TMAnnotationComposite</code>
     * @param annotatedElement - <code>AnnotatedElement</code>
     */
    public static void attachSoapBindingAnnotation(TMAnnotationComposite composite,
                                                   AnnotatedElement annotatedElement) {
        SOAPBinding soapBinding = (SOAPBinding)ConverterUtils.getAnnotation(
                SOAPBinding.class, annotatedElement);
        if (soapBinding != null) {
            SoapBindingAnnot sbAnnot = ConverterUtils.createSoapBindingAnnot(soapBinding);
            composite.setSoapBindingAnnot(sbAnnot);
        }
    }

    /**
     * This method is use to attach @WebServiceRef annotation data to a composite object.
     *
     * @param composite        - <code>TMFAnnotationComposite</code>
     * @param annotatedElement - <code>AnnotatedElement</code>
     */
    public static void attachWebServiceRefAnnotation(TMFAnnotationComposite composite,
                                                     AnnotatedElement annotatedElement) {
        WebServiceRef webServiceRef = (WebServiceRef)ConverterUtils.getAnnotation(
                WebServiceRef.class, annotatedElement);
        if (webServiceRef != null) {
            WebServiceRefAnnot wsrAnnot = ConverterUtils.createWebServiceRefAnnot(
                    webServiceRef);
            composite.setWebServiceRefAnnot(wsrAnnot);
        }
    }

    /** This method will add FieldDescriptionComposite objects to a DescriptionBuilderComposite */
    public static void attachFieldDescriptionComposites(DescriptionBuilderComposite
            composite, List<FieldDescriptionComposite> fdcList) {
        for (FieldDescriptionComposite fdc : fdcList) {
            composite.addFieldDescriptionComposite(fdc);
        }
    }

    /** This method will add MethodDescriptionComposite objects to a DescriptionBuilderComposite */
    public static void attachMethodDescriptionComposites(DescriptionBuilderComposite
            composite, List<MethodDescriptionComposite> mdcList) {
        for (MethodDescriptionComposite mdc : mdcList) {
            composite.addMethodDescriptionComposite(mdc);
            mdc.setDescriptionBuilderCompositeRef(composite);
        }
    }

    /** This method will add ParameterDescriptionComposite objects to a MethodDescriptionComposite */
    public static void attachParameterDescriptionComposites(List
            <ParameterDescriptionComposite> pdcList, MethodDescriptionComposite mdc) {
        for (ParameterDescriptionComposite pdc : pdcList) {
            mdc.addParameterDescriptionComposite(pdc);
            pdc.setMethodDescriptionCompositeRef(mdc);
        }
    }

    /**
     * This method will check to see if a method's declaring class is the Object class.
     *
     * @param method - <code>Method</code>
     * @return - <code>boolean</code>
     */
    public static boolean isInherited(Method method, String declaringClass) {
        if (method.getDeclaringClass().getName().equals(declaringClass)) {
            return false;
        }
        return true;
    }

    /**
     * This method will construct a <code>String</code> that represents the
     * full type of a parameterized variable.
     * @param pt - <code>ParameterizedType</code>
     * @param paramType - <code>String</code>
     * @return - <code>String</code>
     */
    public static String getFullType(ParameterizedType pt, String paramType) {
        if (pt.getRawType() instanceof Class) {
            Class rawClass = (Class)pt.getRawType();
            paramType = paramType + rawClass.getName();
        }
        Type[] genericTypes = pt.getActualTypeArguments();
        if (genericTypes.length > 0) {
            paramType = paramType + "<";
            for (int i = 0; i < genericTypes.length; i++) {
                Type type = genericTypes[i];
                paramType = getType(type, paramType);

                // Set string for more parameters OR close the generic if this is the last one.
                if (i != genericTypes.length - 1) {
                    paramType = paramType + ", ";
                } else {
                    paramType = paramType + ">";
                }

            }
        }
        return paramType;
	}
    
    public static String getType(Type type, String paramType) {
        if (type instanceof Class) {
            paramType = paramType + ((Class)type).getName();
        } else if (type instanceof ParameterizedType) {
            paramType = getFullType((ParameterizedType)type, paramType);
        } else if (type instanceof WildcardType) {
            paramType = paramType + "?";
        } else if (type instanceof GenericArrayType) {
            paramType = getType(((GenericArrayType)type).getGenericComponentType(), paramType) + "[]";
        }
        return paramType;
    }
    
    /**
     * This method will search array of parameter annotations for the presence of the @XmlList
     * annotation.
     */
    public static boolean hasXmlListAnnotation(Annotation[] annotations) {
        for(Annotation annotation : annotations) {
            if(annotation.annotationType() == XmlList.class) {
                return true;
            }
        }
        return false;
    }
}
