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

public class ParameterDescriptionComposite {
    private String parameterType;
    private Class parameterTypeClass;
    private WebParamAnnot webParamAnnot;
    private WebServiceRefAnnot webServiceRefAnnot;
    private int listOrder;
	// Will indicate whether or not we found an @XMLList annotation on the parameter
	private boolean                 isListType;

    private MethodDescriptionComposite parentMDC;

    public ParameterDescriptionComposite() {

    }

    public ParameterDescriptionComposite(
            String parameterType,
            Class parameterTypeClass,
            WebParamAnnot webParamAnnot,
            WebServiceRefAnnot webServiceRefAnnot) {

        this.parameterType = parameterType;
        this.parameterTypeClass = parameterTypeClass;
        this.webParamAnnot = webParamAnnot;
        this.webServiceRefAnnot = webServiceRefAnnot;
    }

    /**
     * Returns the String descrbing this Parameter type.  Note that this string is unparsed.  For
     * example, if it represents a java.util.List<my.package.Foo>, then that excact string will be
     * returned, i.e. "java.util.List<my.package.Foo>".  You can use other methods to retrieve parsed
     * values for Generics and Holders.  For example, getParameterTypeClass(), getRawType(),
     * getHolderActualType(), and getHolderActualTypeClass().
     *
     * @return Returns the parameterType.
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * Returns the class associated with the Parameter.  Note that if this is a generic (including a
     * JAXWS Holder<T>) then the class associated with the raw type is returned (i.e. Holder.class).
     * <p/>
     * For a JAX-WS Holder<T>, use getHolderActualType(...) to get the class associated with T.
     *
     * @return Returns the parameterTypeClass.
     */
    public Class getParameterTypeClass() {

        if (parameterTypeClass == null) {
            if (getParameterType() != null) {
                parameterTypeClass = DescriptionBuilderUtils.getPrimitiveClass(getParameterType());
                if (parameterTypeClass == null) {
                    // If this is a Generic, we need to load the class associated with the Raw Type,
                    // i.e. for JAX-WS Holder<Foo>, we want to load Holder. Othwerise, load the type directly. 
                    String classToLoad = null;
                    if (DescriptionBuilderUtils.getRawType(parameterType) != null) {
                        classToLoad = DescriptionBuilderUtils.getRawType(parameterType);
                    } else {
                        classToLoad = parameterType;
                    }
                    parameterTypeClass = loadClassFromPDC(classToLoad);
                }
            }
        }
        return parameterTypeClass;
    }

    /**
     * For JAX-WS Holder<T>, returns the class associated with T.  For non-JAX-WS Holders returns
     * null.
     */
    public Class getHolderActualTypeClass() {
        Class returnClass = null;

        if (DescriptionBuilderUtils.isHolderType(parameterType)) {
            String classToLoad = DescriptionBuilderUtils.getHolderActualType(parameterType);
            returnClass = loadClassFromPDC(classToLoad);
        }
        return returnClass;
    }

    private Class loadClassFromPDC(String classToLoad) {
        Class returnClass = null;
        ClassLoader classLoader = null;

        if (getMethodDescriptionCompositeRef() != null) {
            if (getMethodDescriptionCompositeRef().getDescriptionBuilderCompositeRef() != null) {
                classLoader = getMethodDescriptionCompositeRef().getDescriptionBuilderCompositeRef()
                        .getClassLoader();
            }
        }
        returnClass = DescriptionBuilderUtils.loadClassFromComposite(classToLoad, classLoader);
        return returnClass;
    }

    /** @return Returns the webParamAnnot. */
    public WebParamAnnot getWebParamAnnot() {
        return webParamAnnot;
    }

    /** @return Returns the webServiceRefAnnot. */
    public WebServiceRefAnnot getWebServiceRefAnnot() {
        return webServiceRefAnnot;
    }

    /** @return Returns the webServiceContextAnnot. */
    public int getListOrder() {
        return listOrder;
    }

    /** @return Returns the parentMDC. */
    public MethodDescriptionComposite getMethodDescriptionCompositeRef() {
        return this.parentMDC;
    }

    /** @param parameterType The parameterType to set. */
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    /** @param parameterTypeClass The parameterTypeClass to set. */
    private void setParameterTypeClass(Class parameterTypeClass) {
        this.parameterTypeClass = parameterTypeClass;
    }

    /** @param webParamAnnot The webParamAnnot to set. */
    public void setWebParamAnnot(WebParamAnnot webParamAnnot) {
        this.webParamAnnot = webParamAnnot;
    }

    /** @param webServiceRefAnnot The webServiceRefAnnot to set. */
    public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
        this.webServiceRefAnnot = webServiceRefAnnot;
    }

    /** @param webServiceContextAnnot The webServiceContextAnnot to set. */
    public void setListOrder(int listOrder) {
        this.listOrder = listOrder;
    }

    /** @param mdc The parent MethodDescriptionComposite to set. */
    public void setMethodDescriptionCompositeRef(MethodDescriptionComposite mdc) {
        this.parentMDC = mdc;
    }

    public boolean compare(Object obj) {
        if (obj instanceof ParameterDescriptionComposite) {
            ParameterDescriptionComposite pdc = (ParameterDescriptionComposite)obj;
            if (!(this.parameterType.equals(pdc.getParameterType()))) {
                return false;
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
    
    /** Convenience method for unit testing. We will print all of the data members here. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        final String newLine = "\n";
        final String sameLine = "; ";
        sb.append(super.toString());
        sb.append(sameLine);
        sb.append("ParameterType: " + parameterType);

        if (webParamAnnot != null) {
            sb.append(newLine);
            sb.append("WebParam: ");
            sb.append(webParamAnnot.toString());
        }

        if (webServiceRefAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceRef: ");
            sb.append(webServiceRefAnnot.toString());
        }
        return sb.toString();
    }

    public String getRawType() {
        return DescriptionBuilderUtils.getRawType(parameterType);
    }

    public String getHolderActualType() {
        return DescriptionBuilderUtils.getHolderActualType(parameterType);
    }

    public boolean isHolderType() {
        return DescriptionBuilderUtils.isHolderType(parameterType);
    }
    
}
