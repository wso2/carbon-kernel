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

/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodDescriptionComposite implements TMAnnotationComposite, TMFAnnotationComposite {

    //Method reflective information
    private String methodName;    //a public method name in this class
    private String returnType;    //Methods return type
    private String[] exceptions;
    private String declaringClass; //the class/interface that actually declares this method
    private boolean staticModifier= false;//true if method is static
    private boolean finalModifier = false; //true if method is final
    
    boolean oneWayAnnotated;
	// boolean that indicates if an @XmlList annotation was found on the method
	private boolean 				isListType = false;
    private WebMethodAnnot webMethodAnnot;
    private WebResultAnnot webResultAnnot;
    private HandlerChainAnnot handlerChainAnnot;
    private SoapBindingAnnot soapBindingAnnot;
    private WebServiceRefAnnot webServiceRefAnnot;
    private WebEndpointAnnot webEndpointAnnot;
    private RequestWrapperAnnot requestWrapperAnnot; //TODO EDIT CHECK: only on methods of SEI
    private ResponseWrapperAnnot responseWrapperAnnot;//TODO EDIT CHECK: only on methods of SEI
    private List<ParameterDescriptionComposite> parameterDescriptions;//TODO EDIT CHECK: only on methods of SEI

    private DescriptionBuilderComposite parentDBC;
    
    private ActionAnnot actionAnnot;

    /*
      * Default Constructor
      */
    public MethodDescriptionComposite() {
        parameterDescriptions = new ArrayList<ParameterDescriptionComposite>();
    }

    public MethodDescriptionComposite(
            String methodName,
            String returnType,
            WebMethodAnnot webMethodAnnot,
            WebResultAnnot webResultAnnot,
            boolean oneWayAnnotated,
            HandlerChainAnnot handlerChainAnnot,
            SoapBindingAnnot soapBindingAnnot,
            WebServiceRefAnnot webServiceRefAnnot,
            WebEndpointAnnot webEndpointAnnot,
            RequestWrapperAnnot requestWrapperAnnot,
            ResponseWrapperAnnot responseWrapperAnnot
    ) {

        this.methodName = methodName;
        this.returnType = returnType;
        this.webMethodAnnot = webMethodAnnot;
        this.webResultAnnot = webResultAnnot;
        this.oneWayAnnotated = oneWayAnnotated;
        this.handlerChainAnnot = handlerChainAnnot;
        this.soapBindingAnnot = soapBindingAnnot;
        this.webServiceRefAnnot = webServiceRefAnnot;
        this.webEndpointAnnot = webEndpointAnnot;
        this.requestWrapperAnnot = requestWrapperAnnot;
        this.responseWrapperAnnot = responseWrapperAnnot;
    }

    /** @return Returns the methodName */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the String descrbing this method result type.  Note that this string is unparsed.  For
     * example, if it represents a java.util.List<my.package.Foo>, then that excact string will be
     * returned, i.e. "java.util.List<my.package.Foo>".  You can use other methods on this object to
     * retrieve parsed values for Generics and Holders.
     *
     * @return Returns the returnType
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Returns the class associated with the method result type.  Note that if the resturn type a
     * generic (such as java.util.List<my.package.Foo>) then the class associated with the raw type is
     * returned (i.e. java.util.List).
     * <p/>
     * There are other methods that return the class for the actual type for certain JAX-WS specific
     * generics such as Response<T>
     *
     * @return Returns the parameterTypeClass.
     */
    public Class getReturnTypeClass() {
        Class returnTypeClass = null;
        String fullReturnType = getReturnType();
        if (fullReturnType != null) {
            returnTypeClass = DescriptionBuilderUtils.getPrimitiveClass(fullReturnType);
            if (returnTypeClass == null) {
                // If this is a Generic, we need to load the class associated with the Raw Type, 
                // i.e. for List<Foo>, we want to load List. Othwerise, load the type directly. 
                String classToLoad = null;
                if (DescriptionBuilderUtils.getRawType(fullReturnType) != null) {
                    classToLoad = DescriptionBuilderUtils.getRawType(fullReturnType);
                } else {
                    classToLoad = fullReturnType;
                }
                returnTypeClass = loadClassFromMDC(classToLoad);
            }
        }

        return returnTypeClass;
    }

    private Class loadClassFromMDC(String classToLoad) {
        Class returnClass = null;
        ClassLoader classLoader = null;

        if (getDescriptionBuilderCompositeRef() != null) {
            classLoader = getDescriptionBuilderCompositeRef().getClassLoader();
        }
        returnClass = DescriptionBuilderUtils.loadClassFromComposite(classToLoad, classLoader);
        return returnClass;
    }

    /** @return returns whether this is OneWay */
    public boolean isOneWay() {
        return oneWayAnnotated;
    }

    /** @return Returns the webEndpointAnnot. */
    public WebEndpointAnnot getWebEndpointAnnot() {
        return webEndpointAnnot;
    }

    /** @return Returns the requestWrapperAnnot. */
    public RequestWrapperAnnot getRequestWrapperAnnot() {
        return requestWrapperAnnot;
    }

    /** @return Returns the responseWrapperAnnot. */
    public ResponseWrapperAnnot getResponseWrapperAnnot() {
        return responseWrapperAnnot;
    }

    /** @return Returns the handlerChainAnnot. */
    public HandlerChainAnnot getHandlerChainAnnot() {
        return handlerChainAnnot;
    }

    /** @return Returns the soapBindingAnnot. */
    public SoapBindingAnnot getSoapBindingAnnot() {
        return soapBindingAnnot;
    }

    /** @return Returns the webMethodAnnot. */
    public WebMethodAnnot getWebMethodAnnot() {
        return webMethodAnnot;
    }

    /** @return Returns the webResultAnnot. */
    public WebResultAnnot getWebResultAnnot() {
        return webResultAnnot;
    }

    /** @return Returns the webServiceRefAnnot. */
    public WebServiceRefAnnot getWebServiceRefAnnot() {
        return webServiceRefAnnot;
    }

    /** @return Returns the actionAnnot. */
    public ActionAnnot getActionAnnot() {
        return actionAnnot;
    }

    /** @return Returns the exceptions. */
    public String[] getExceptions() {
        return exceptions;
    }

    /** @return Returns the exceptions. */
    public Class[] getExceptionTypes() {
        //TODO: Implement this...
        //for each exception in the array, convert it to a class, and return that
        //If a classloader was not set, then just use the default
        Class[] classes = new Class[0];
        return classes;
    }

    /** @return Returns the fully qualified name of the declaring class. */
    public String getDeclaringClass() {
        if (declaringClass == null && parentDBC != null) {
            return parentDBC.getClassName();
        }
        return declaringClass;
    }

    /** @return Returns the ModuleClassType. */
    public DescriptionBuilderComposite getDescriptionBuilderCompositeRef() {

        return this.parentDBC;
    }

    /** @param methodName The methodName to set. */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /** @param returnType The returnType to set. */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /** @param oneWayAnnotated The oneWay boolean to set */
    public void setOneWayAnnot(boolean oneWayAnnotated) {
        this.oneWayAnnotated = oneWayAnnotated;
    }

    /** @param webEndpointAnnotImpl The webEndpointAnnotImpl to set. */
    public void setWebEndpointAnnot(WebEndpointAnnot webEndpointAnnot) {
        this.webEndpointAnnot = webEndpointAnnot;
    }

    /** @param requestWrapperAnnot The requestWrapperAnnot to set. */
    public void setRequestWrapperAnnot(
            RequestWrapperAnnot requestWrapperAnnot) {
        this.requestWrapperAnnot = requestWrapperAnnot;
    }

    /** @param responseWrapperAnnot The responseWrapperAnnot to set. */
    public void setResponseWrapperAnnot(
            ResponseWrapperAnnot responseWrapperAnnot) {
        this.responseWrapperAnnot = responseWrapperAnnot;
    }

    /** @param handlerChainAnnot The handlerChainAnnot to set. */
    public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
        this.handlerChainAnnot = handlerChainAnnot;
    }

    /** @param soapBindingAnnot The soapBindingAnnot to set. */
    public void setSoapBindingAnnot(SoapBindingAnnot soapBindingAnnot) {
        this.soapBindingAnnot = soapBindingAnnot;
    }

    /** @param webMethodAnnot The webMethodAnnot to set. */
    public void setWebMethodAnnot(WebMethodAnnot webMethodAnnot) {
        this.webMethodAnnot = webMethodAnnot;
    }

    /** @param webResultAnnot The webResultAnnot to set. */
    public void setWebResultAnnot(WebResultAnnot webResultAnnot) {
        this.webResultAnnot = webResultAnnot;
    }

    /** @param webServiceRefAnnot The webServiceRefAnnot to set. */
    public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
        this.webServiceRefAnnot = webServiceRefAnnot;
    }

    /** @param actionAnnot The actionAnnot to set. */
    public void setActionAnnot(ActionAnnot actionAnnot) {
        this.actionAnnot = actionAnnot;
    }

    /** @param parameterDescription The parameterDescription to add to the set. */
    public void addParameterDescriptionComposite(
            ParameterDescriptionComposite parameterDescription) {
        parameterDescriptions.add(parameterDescription);
    }

    /**
     * @param parameterDescription The parameterDescription to add to the set.
     * @param index                The index at which to place this parameterDescription
     */
    public void addParameterDescriptionComposite(ParameterDescriptionComposite parameterDescription,
                                                 int index) {
        parameterDescription.setListOrder(index);
        parameterDescriptions.add(index, parameterDescription);
    }

    /** @param parameterDescription The parameterDescription to add to the set. */
    public void setParameterDescriptionCompositeList(
            List<ParameterDescriptionComposite> parameterDescriptionList) {
        this.parameterDescriptions = parameterDescriptionList;
    }

    /** @param parameterDescription The parameterDescription to add to the set. */
    public ParameterDescriptionComposite getParameterDescriptionComposite(int index) {
        return parameterDescriptions.get(index);
    }

    /**
     */
    public List<ParameterDescriptionComposite> getParameterDescriptionCompositeList() {
        return parameterDescriptions;
    }

    /** @param exceptions The exceptions to set. */
    public void setExceptions(String[] exceptions) {
        this.exceptions = exceptions;
    }

    /** @param declaringClass The wrapper class to set. */
    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    /** @return Returns the ModuleClassType. */
    public void setDescriptionBuilderCompositeRef(DescriptionBuilderComposite dbc) {

        this.parentDBC = dbc;
    }

    public boolean isStatic() {
        return staticModifier;
    }

    public void setStaticModifier(boolean staticModifier) {
        this.staticModifier = staticModifier;
    }

    public boolean isFinal() {
        return finalModifier;
    }

    public void setFinalModifier(boolean finalModifier) {
        this.finalModifier = finalModifier;
    }

    public boolean compare(Object obj) {
        if (obj instanceof MethodDescriptionComposite) {
            MethodDescriptionComposite mdc = (MethodDescriptionComposite)obj;
            if (!(this.methodName.equals(mdc.getMethodName()))) {
                return false;
            }
            List<ParameterDescriptionComposite> thisParamList = this.parameterDescriptions;
            List<ParameterDescriptionComposite> paramList =
                    mdc.getParameterDescriptionCompositeList();
            if (thisParamList.size() != paramList.size()) {
                return false;
            }
            for (int i = 0; i < thisParamList.size(); i++) {
                if (!(thisParamList.get(i).compare(paramList.get(i)))) {
                    return false;
                }
            }
            return true;
        } else {
            return super.equals(obj);
        }
    }

	public void setIsListType(boolean isListType) {
		this.isListType = isListType;
	}
	
	public boolean isListType() {
		return isListType;
	}
	
    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        final String newLine = "\n";
        final String sameLine = "; ";
        sb.append(super.toString());
        sb.append(newLine);
        sb.append("Name: " + methodName);
        sb.append(sameLine);
        sb.append("ReturnType: " + returnType);

        sb.append(newLine);
        sb.append("Exceptions: ");
        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                sb.append(exceptions[i]);
                sb.append(sameLine);
            }
        }

        if (oneWayAnnotated) {
            sb.append(newLine);
            sb.append("OneWay");
        }

        if (webMethodAnnot != null) {
            sb.append(newLine);
            sb.append("WebMethod: ");
            sb.append(webMethodAnnot.toString());
        }

        if (requestWrapperAnnot != null) {
            sb.append(newLine);
            sb.append("RequestWrapper: ");
            sb.append(requestWrapperAnnot.toString());
        }

        if (responseWrapperAnnot != null) {
            sb.append(newLine);
            sb.append("ResponsetWrapper: ");
            sb.append(responseWrapperAnnot.toString());
        }

        if (soapBindingAnnot != null) {
            sb.append(newLine);
            sb.append("SOAPBinding: ");
            sb.append(soapBindingAnnot.toString());
        }

        if (webEndpointAnnot != null) {
            sb.append(newLine);
            sb.append("WebEndpoint: ");
            sb.append(webEndpointAnnot.toString());
        }

        if (webResultAnnot != null) {
            sb.append(newLine);
            sb.append("WebResult: ");
            sb.append(webResultAnnot.toString());
        }

        if (webServiceRefAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceRef: ");
            sb.append(webServiceRefAnnot.toString());
        }

        if (actionAnnot != null) {
            sb.append(newLine);
            sb.append("Action: ");
            sb.append(actionAnnot.toString());
        }

        if (handlerChainAnnot != null) {
            sb.append(newLine);
            sb.append("HandlerChain: ");
            sb.append(handlerChainAnnot.toString());
        }

        sb.append(newLine);
        sb.append("Number of Parameter Descriptions: " + parameterDescriptions.size());
        Iterator<ParameterDescriptionComposite> pdcIter = parameterDescriptions.iterator();
        while (pdcIter.hasNext()) {
            sb.append(newLine);
            ParameterDescriptionComposite pdc = pdcIter.next();
			sb.append(pdc.toString());
		}
		return sb.toString();
	}
}
