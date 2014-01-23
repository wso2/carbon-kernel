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


package org.apache.axis2.jaxws.description.impl;

import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ParameterDescriptionJava;
import org.apache.axis2.jaxws.description.ParameterDescriptionWSDL;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.converter.ConverterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** @see ../ParameterDescription */
class ParameterDescriptionImpl
        implements ParameterDescription, ParameterDescriptionJava, ParameterDescriptionWSDL {
    private static final Log log = LogFactory.getLog(ParameterDescriptionImpl.class);
    private OperationDescription parentOperationDescription;
    // The Class representing the parameter.  Note that for a Generic, including the JAX-WS Holder<T> Generic, 
    // this represents the raw type of the Generic (e.g. List for List<T> or Holder for Holder<T>).
    private Class parameterType;
    // For the JAX-WS Generic Holder<T> (e.g. Holder<Foo>), this will be the actual type argument (e.g. Foo).  For 
    // any other parameter (including other Generics), this will be null. 
    // Note that since JAX-WS Holder<T> only supports a single actual type T (not multiple types such as <K,V>)
    private Class parameterHolderActualType;

    // 0-based number of the parameter in the argument list
    private int parameterNumber = -1;
    // The Parameter Description Composite used to build the ParameterDescription
    private ParameterDescriptionComposite paramDescComposite;

    // ANNOTATION: @WebMethod
    private WebParam webParamAnnotation;
    private String webParamName;
    private String webParamPartName;
    public static final String WebParam_TargetNamespace_DEFAULT = "";
    private String webParamTargetNamespace;
    private WebParam.Mode webParamMode;
    public static final Boolean WebParam_Header_DEFAULT = new Boolean(false);
    private Boolean webParamHeader;
    
    // Attachment Description information
    private boolean             _setAttachmentDesc = false;
    private AttachmentDescription attachmentDesc = null;
    
    // This boolean indicates whether or not there was an @XMLList on the parameter
    private boolean isListType = false;
    
    ParameterDescriptionImpl(int parameterNumber, Class parameterType, Type parameterGenericType,
                             Annotation[] parameterAnnotations, OperationDescription parent) {
        this.parameterNumber = parameterNumber;
        this.parentOperationDescription = parent;
        this.parameterType = parameterType;

        // The Type argument could be a Type (if the parameter is a Paramaterized Generic) or
        // just a Class (if it is not).  If it JAX-WS Holder<T> parameterized type, then get the 
        // actual parameter type and hang on to that, too.
        if (ParameterizedType.class.isInstance(parameterGenericType)) {
            this.parameterHolderActualType =
                    getGenericParameterActualType((ParameterizedType)parameterGenericType);
        }
        findWebParamAnnotation(parameterAnnotations);
        this.isListType = ConverterUtils.hasXmlListAnnotation(parameterAnnotations);
    }

    ParameterDescriptionImpl(int parameterNumber, ParameterDescriptionComposite pdc,
                             OperationDescription parent) {
        this.paramDescComposite = pdc;
        this.parameterNumber = parameterNumber;
        this.parentOperationDescription = parent;
        webParamAnnotation = pdc.getWebParamAnnot();
        this.isListType = pdc.isListType();

        //TODO: Need to build the schema map. Need to add logic to add this parameter
        //      to the schema map.

    }

    /*
    * This grabs the WebParam annotation from the list of annotations for this parameter
    * This should be DEPRECATED once DBC processing is complete.
    */
    private void findWebParamAnnotation(Annotation[] annotations) {
        for (Annotation checkAnnotation : annotations) {
            // REVIEW: This may not work with the MDQInput.  From the java.lang.annotation.Annotation interface
            //         javadoc: "Note that an interface that manually extends this one does not define an annotation type."
            if (checkAnnotation.annotationType() == WebParam.class) {
                webParamAnnotation = (WebParam)checkAnnotation;
            }
        }
    }
    
    public OperationDescription getOperationDescription() {
        return parentOperationDescription;
    }

    /**
     * Returns the class associated with the parameter.  Note that for the JAX-WS Holder<T> type,
     * you can use getParameterActualType() to get the class associated with T.
     */
    public Class getParameterType() {
        if (parameterType == null && paramDescComposite != null) {
            parameterType = paramDescComposite.getParameterTypeClass();
        }
        return parameterType;
    }

    /**
     * For a non-Holder type, returns the parameter class.  For a Holder<T> type, returns the class
     * of T.
     *
     * @return
     */
    public Class getParameterActualType() {
        if (parameterHolderActualType == null && paramDescComposite != null &&
                paramDescComposite.isHolderType()) {
            parameterHolderActualType = paramDescComposite.getHolderActualTypeClass();
            return parameterHolderActualType;
        } else if (parameterHolderActualType != null) {
            return parameterHolderActualType;
        } else {
            if (paramDescComposite != null && parameterType == null) {
                parameterType = paramDescComposite.getParameterTypeClass();
            }
            return parameterType;
        }
    }

    /**
     * TEMPORARY METHOD!  For a JAX-WS Holder<T> this returns the class associated with <T>. For a
     * Holder<Generic<...>>, it returns the class associated with Generic.  If the type is not a
     * JAX-WS Holder, return a null.
     * <p/>
     * This method SHOULD BE REMOVED when the description layer is refactored to use only DBC and
     * not Java reflection directly.
     *
     * @param parameterGenericType
     * @return
     */
    // TODO: Remove this method when code refactored to only use DBC.
    private Class getGenericParameterActualType(ParameterizedType parameterGenericType) {
        Class returnClass = null;
        // If this is a JAX-WS Holder type, then get the actual type.  Note that we can't use the
        // isHolderType method yet because the class variable it is going to check (parameterHolderActualType)
        // hasn't been initialized yet.
        if (parameterGenericType != null &&
                parameterGenericType.getRawType() == javax.xml.ws.Holder.class) {
            // NOTE
            // If you change this code, please remember to change 
            // OperationDesc.getResultActualType

            Type type = parameterGenericType.getActualTypeArguments()[0];
            if (type != null && ParameterizedType.class.isInstance(type)) {
                // For types of Holder<Generic<K,V>>, return class associated with Generic
                returnClass = (Class)((ParameterizedType)type).getRawType();
            } else if (type != null && GenericArrayType.class.isInstance(type)) {
                Type componentType = ((GenericArrayType)type).getGenericComponentType();
                Class arrayClass = null;
                if (ParameterizedType.class.isInstance(componentType)) {
                    // For types of Holder<Generic<K,V>[]>, return class associated with Generic[]
                    arrayClass = (Class)((ParameterizedType)componentType).getRawType();
                } else {
                    // For types of Holder<Object[]>, return class associated with Object[]
                    arrayClass = (Class)componentType;
                }
                // REVIEW: This only works for a single dimension array!  Note that if this method is removed
                //         when DBC is used, just make sure DBC supports multi-dim arrays
                returnClass = Array.newInstance(arrayClass, 0).getClass();
            } else {
                // For types of Holder<Object>, return the class associated with Object
                returnClass = (Class)type;
            }
        }

        return returnClass;
    }

    /** Answer whether this ParameterDescription represents a JAX-WS Holder<T> type. */
    public boolean isHolderType() {
        // If this is a JAX-WS Holder<T> type, then we set the the class of the actual
        // parameter <T> in the constructor.  Otherwise, that is null.
        // Holder types are defined by JSR-224 JAX-WS 2.0, Sec 2.3.3, pg 16
        if (paramDescComposite != null) {
            return paramDescComposite.isHolderType();
        } else {
            return Holder.class.equals(getParameterType());
        }
    }

    // =====================================
    // ANNOTATION: WebParam
    // =====================================
    public WebParam getAnnoWebParam() {
        return webParamAnnotation;
    }

    public String getParameterName() {
        return getAnnoWebParamName();
    }

    public String getAnnoWebParamName() {
        if (webParamName == null) {
            if (getAnnoWebParam() != null && !DescriptionUtils.isEmpty(getAnnoWebParam().name())) {
                webParamName = getAnnoWebParam().name();
            } else if (getOperationDescription().getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getOperationDescription().getSoapBindingParameterStyle() ==
                    SOAPBinding.ParameterStyle.BARE) {
                // Defaul per JSR-181 MR Sec 4.4.1, pg 19
                // TODO: Validation: For BARE paramaterUse, only a single IN our INOUT paramater and a single output (either return or OUT or INOUT) is allowed
                //       Per JSR-224, Sec 3.6.2.2, pg 37
                webParamName = getOperationDescription().getOperationName();
            } else {
                // Default per JSR-181 MR Sec 4.4.1, pg 20
                // Return "argN" where N is the index of the parameter in the method signature
                webParamName = "arg" + parameterNumber;
            }
        }
        return webParamName;
    }

    public String getPartName() {
        return getAnnoWebParamPartName();
    }

    public String getAnnoWebParamPartName() {
        if (webParamPartName == null) {
            if (getAnnoWebParam() != null &&
                    !DescriptionUtils.isEmpty(getAnnoWebParam().partName())) {
                webParamPartName = getAnnoWebParam().partName();
            } else {
                // Default per JSR-181 MR Sec 4.4.1, pg 20
                webParamPartName = getAnnoWebParamName();
            }
        }
        return webParamPartName;
    }

    public String getTargetNamespace() {
        return getAnnoWebParamTargetNamespace();
    }

    public String getAnnoWebParamTargetNamespace() {
        if (webParamTargetNamespace == null) {
            if (getAnnoWebParam() != null &&
                    !DescriptionUtils.isEmpty(getAnnoWebParam().targetNamespace())) {
                webParamTargetNamespace = getAnnoWebParam().targetNamespace();
            } else if (getOperationDescription().getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT
                    && getOperationDescription().getSoapBindingParameterStyle() ==
                    SOAPBinding.ParameterStyle.WRAPPED
                    && !getAnnoWebParamHeader()) {
                // Defaul per JSR-181 MR Sec 4.4.1, pg 20
                webParamTargetNamespace = WebParam_TargetNamespace_DEFAULT;
            } else {
                // Default per JSR-181 MR Sec 4.4.1, pg 20
                webParamTargetNamespace = ((EndpointDescriptionJava)getOperationDescription()
                        .getEndpointInterfaceDescription().getEndpointDescription())
                        .getAnnoWebServiceTargetNamespace();
            }
        }
        return webParamTargetNamespace;
    }

//    public Mode getMode() {

    public WebParam.Mode getMode() {
        return getAnnoWebParamMode();
    }

    public WebParam.Mode getAnnoWebParamMode() {
        if (webParamMode == null) {
            // Interesting conundrum here:
            // Because WebParam.mode has a default value, it will always return something if the
            // annotation is present.  That value is currently Mode.IN.  However, that default is only
            // correct for a non-Holder Type; the correct default for a Holder Type is Mode.INOUT.  Furthermore,
            // there's no way (I can tell) to differentiate if the setting for mode() was specified or defaulted,
            // so there's no way to tell if the value is defaulted to IN or explicitly specified IN by the annotation.
            // The conundrum is: Do we return the value from the annotation, or do we return the default value based on the
            // type.  For now, for a Holder type that has a value of IN, we reset the value to INOUT.
            // That means even if WebParam.mode=IN was explicitly set, it will be overridden to INOUT.
            // The default values are from JSR-181 MR Sec 4.4.1, pg 20

            // Unlike a String value, if the annotation is present, it will return a usable default value as defined by 
            // the Annotation.  That is currently Mode.IN
            if (getAnnoWebParam() != null) {
                webParamMode = getAnnoWebParam().mode();
            } else {
                webParamMode = WebParam.Mode.IN;
            }

            if (isHolderType() && webParamMode == WebParam.Mode.IN) {
                // Default per JSR-181 MR Sec 4.4.1, pg 20
                webParamMode = WebParam.Mode.INOUT;
            }
        }
        return webParamMode;
    }

    public boolean isHeader() {
        return getAnnoWebParamHeader();
    }

    public boolean getAnnoWebParamHeader() {
        if (webParamHeader == null) {
            // Unlike a String value, if the annotation is present, it will return a usable default value.
            if (getAnnoWebParam() != null) {
                webParamHeader = getAnnoWebParam().header();
            } else {
                webParamHeader = WebParam_Header_DEFAULT;
            }
        }
        return webParamHeader.booleanValue();
    }

    public String toString() {
        final String newline = "\n";
        final String sameline = "; ";
        StringBuffer string = new StringBuffer();
        try {
            string.append(super.toString());
            string.append(newline);
            string.append("Name: " + getParameterName());
            //
            string.append(newline);
            string.append("Namespace: " + getTargetNamespace());
            string.append(newline);
            string.append("PartName: " + getPartName());
            //
            string.append(newline);
            string.append("Is header: " + (isHeader() == true));
            string.append(sameline);
            string.append("Is holder: " + (isHolderType() == true));
            //
            string.append(newline);
            string.append("Mode: " + getMode());
            //
            string.append(newline);
            string.append("Type: " + getParameterType());
            string.append(sameline);
            string.append("Actual type: " + getParameterActualType());
            if (getAttachmentDescription() != null) {
                string.append(newline);
                string.append(getAttachmentDescription().toString());
            }
        }
        catch (Throwable t) {
            string.append(newline);
            string.append("Complete debug information not currently available for " +
                    "ParameterDescription");
            return string.toString();
        }
        return string.toString();
    }

    public boolean isListType() {
    	return isListType;
    }
    
    /**
     * Helper method to get to parent impl object.
     */
    private OperationDescriptionImpl getOperationDescriptionImpl() {
        if(this.getOperationDescription() instanceof OperationDescriptionImpl) {
                return (OperationDescriptionImpl) this.getOperationDescription();
        }
        return null;
    }
    
    /**
     * This method will return an AttachmentDescription based on the part name of the parameter.
     */
    public AttachmentDescription getAttachmentDescription() {
        String partName = this.getPartName();
        if(partName != null && getOperationDescriptionImpl() != null) {
            if(log.isDebugEnabled()) {
                log.debug("Returning parameter AttachmentDescription for partName: " + 
                          partName);
            }
            return getOperationDescriptionImpl().getPartAttachmentDescription(partName);
            
        }
        if(log.isDebugEnabled()) {
            log.debug("Did not find parameter AttachmentDescription for partName: " + partName);
        }
        return null;
    }
}
