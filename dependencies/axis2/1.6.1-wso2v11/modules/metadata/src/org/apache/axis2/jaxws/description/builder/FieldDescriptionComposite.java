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


public class FieldDescriptionComposite implements TMFAnnotationComposite {

    //Method reflective information
    private String fieldName;       // field name
    private String modifierType;    // field modifier
    private String fieldClassName;  // fully qualified field class name
	
	// indicates whether the field was annotated with @XmlList or not
	private boolean isListType = false;

    private HandlerChainAnnot handlerChainAnnot;
    private WebServiceRefAnnot webServiceRefAnnot;

    /*
      * Default Constructor
      */
    public FieldDescriptionComposite() {

    }

    public FieldDescriptionComposite(
            String fieldName,
            String modifierType,
            HandlerChainAnnot handlerChainAnnot,
            WebServiceRefAnnot webServiceRefAnnot
    ) {
        this.fieldName = fieldName;
        this.modifierType = modifierType;
        this.handlerChainAnnot = handlerChainAnnot;
        this.webServiceRefAnnot = webServiceRefAnnot;
    }

    /** @return Returns the fieldName. */
    public String getFieldName() {
        return fieldName;
    }

    /** @return Returns the handlerChainAnnot. */
    public HandlerChainAnnot getHandlerChainAnnot() {
        return handlerChainAnnot;
    }

    /** @return Returns the modifierType. */
    public String getModifierType() {
        return modifierType;
    }
    
    /** @return Returns the fieldClassName. */
    public String getFieldClassName() {
        return fieldClassName;
    }

    /** @return Returns the webServiceRefAnnot. */
    public WebServiceRefAnnot getWebServiceRefAnnot() {
        return webServiceRefAnnot;
    }

    /** @param fieldName The fieldName to set. */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /** @param handlerChainAnnot The handlerChainAnnot to set. */
    public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
        this.handlerChainAnnot = handlerChainAnnot;
    }

    /** @param modifierType The modifierType to set. */
    public void setModifierType(String modifierType) {
        this.modifierType = modifierType;
    }

    /** @param webServiceRefAnnot The webServiceRefAnnot to set. */
    public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
        this.webServiceRefAnnot = webServiceRefAnnot;
    }

	public void setIsListType(boolean isListType) {
		this.isListType = isListType;
	}
	
	public boolean isListType() {
		return isListType;
	}
     
    public void setFieldClassName(String fieldClassName) {
        this.fieldClassName = fieldClassName;
    }
	
    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append("***** BEGIN FieldDescriptionComposite *****");
        sb.append("FDC.fieldName=" + fieldName);
        sb.append(newLine);
        if (handlerChainAnnot != null) {
            sb.append("\t @HandlerChain");
            sb.append(newLine);
            sb.append("\t" + handlerChainAnnot.toString());
        }
        sb.append(newLine);
        if (webServiceRefAnnot != null) {
            sb.append("\t @WebServiceRef");
            sb.append(newLine);
            sb.append("\t" + webServiceRefAnnot.toString());
        }
        sb.append("***** END FieldDescriptionComposite");
        return sb.toString();
	}
}
	
