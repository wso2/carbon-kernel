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

package org.apache.axis2.jaxws.description.builder;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

/**
 * This subclass of the DescriptionBuilderComposite will be used to model
 * information specific to a port.  The DescriptionBuilderComposite, when
 * used in server-side processing, more or less represents a single web 
 * service implementation class. It is possible, and likely, that a single
 * implementation class will serve multiple ports. The PortComposite allows
 * metadata that applies at the port level to be separated from the 
 * DescriptionBuilderComposite. This will allow processing and configuring
 * of multiple ports in the description hierarchy from a single DBC object.
 *
 */
public class PortComposite extends DescriptionBuilderComposite {
    
    private WsdlGenerator wsdlGenerator;
    
    // The WebServiceAnnot should have been created from the WebServiceAnnot
    // that exists on the DBC with which a PortComposite instance was constructed.
    // It exists here so port-specific values, such as 'portName', can be overridden.
    private WebServiceAnnot wsAnnot;
    
    // The WebServiceProviderAnnot should have been created from the WebServiceProviderAnnot
    // that exists on the DBC with which a PortComposite instance was constructed.
    // It exists here so port-specific values, such as 'portName', can be overridden.
    private WebServiceProviderAnnot wspAnnot;
    
    private BindingTypeAnnot btAnnot;
    
    private HandlerChainAnnot hcAnnot;
    
    private HandlerChainsType hcsType;
    
    private Boolean mtomEnabled;
    
    private Map<String, Object> properties;
    
    private DescriptionBuilderComposite baseDBC;
    
    public PortComposite(DescriptionBuilderComposite baseDBC) {
        this.baseDBC = baseDBC;
    }
    
    
    public WsdlGenerator getCustomWsdlGenerator() {
        if(wsdlGenerator == null) {
            return baseDBC.getCustomWsdlGenerator();
        }
        return wsdlGenerator;
    }

    public WebServiceAnnot getWebServiceAnnot() {
        if(wsAnnot == null) {
            return baseDBC.getWebServiceAnnot();
        }
        return wsAnnot;
    }

    public WebServiceProviderAnnot getWebServiceProviderAnnot() {
        if(wspAnnot == null) {
            return baseDBC.getWebServiceProviderAnnot();
        }
        return wspAnnot;
    }

    public void setCustomWsdlGenerator(WsdlGenerator wsdlGenerator) {
        this.wsdlGenerator = wsdlGenerator;
    }

    public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
        this.hcAnnot = handlerChainAnnot;
    }

    public void setHandlerChainsType(HandlerChainsType handlerChainsType) {
        this.hcsType = handlerChainsType;
    }

    public void setIsMTOMEnabled(boolean isMTOMEnabled) {
        this.mtomEnabled = isMTOMEnabled;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setWebServiceAnnot(WebServiceAnnot webServiceAnnot) {
        this.wsAnnot = webServiceAnnot;
    }

    public void setWebServiceProviderAnnot(WebServiceProviderAnnot webServiceProviderAnnot) {
        this.wspAnnot = webServiceProviderAnnot;
    }

    public BindingTypeAnnot getBindingTypeAnnot() {
        if(btAnnot == null) {
            return baseDBC.getBindingTypeAnnot();
        }
        return btAnnot;
    }

    public HandlerChainAnnot getHandlerChainAnnot() {
        if(hcAnnot == null) {
            return baseDBC.getHandlerChainAnnot();
        }
        return hcAnnot;
    }

    public HandlerChainsType getHandlerChainsType() {
        if(hcsType == null) {
            return baseDBC.getHandlerChainsType();
        }
        return hcsType;
    }

    public boolean isMTOMEnabled() {
        if(mtomEnabled == null) {
            return baseDBC.isMTOMEnabled();
        }
        return mtomEnabled;
    }

    public void setBindingTypeAnnot(BindingTypeAnnot bindingTypeAnnot) {
        this.btAnnot = bindingTypeAnnot;
    }

    public Map<String, Object> getProperties() {
        if(properties == null) {
            return baseDBC.getProperties();
        }
        return properties;
    }

    public void addPortComposite(PortComposite portDBC) {
        // nothing to do here
    }

    public List<PortComposite> getPortComposites() {
        return null;
    }
    
    public JAXWSCatalogManager getCatalogManager() {
        return baseDBC.getCatalogManager();
    }

    public ClassLoader getClassLoader() {
        return baseDBC.getClassLoader();
    }

    public String[] getClassModifiers() {
        return baseDBC.getClassModifiers();
    }

    public String getClassName() {
        return baseDBC.getClassName();
    }

    public List<CustomAnnotationInstance> getCustomAnnotationInstances() {
        return baseDBC.getCustomAnnotationInstances();
    }

    public Map<String, CustomAnnotationProcessor> getCustomAnnotationProcessors() {
        return baseDBC.getCustomAnnotationProcessors();
    }
    
    public FieldDescriptionComposite getFieldDescriptionComposite(String fieldName) {
        return baseDBC.getFieldDescriptionComposite(fieldName);
    }

    public List<String> getInterfacesList() {
        return baseDBC.getInterfacesList();
    }

    public MethodDescriptionComposite getMethodDescriptionComposite(String methodName, int occurence) {
        return baseDBC.getMethodDescriptionComposite(methodName, occurence);
    }

    public List<MethodDescriptionComposite> getMethodDescriptionComposite(String methodName) {
        return baseDBC.getMethodDescriptionComposite(methodName);
    }

    public List<MethodDescriptionComposite> getMethodDescriptionsList() {
        return baseDBC.getMethodDescriptionsList();
    }

    public QName getPreferredPort() {
        return baseDBC.getPreferredPort();
    }

    public QName getPreferredPort(Object key) {
        return baseDBC.getPreferredPort(key);
    }

    public ServiceModeAnnot getServiceModeAnnot() {
        return baseDBC.getServiceModeAnnot();
    }

    public SoapBindingAnnot getSoapBindingAnnot() {
        return baseDBC.getSoapBindingAnnot();
    }

    public String getSuperClassName() {
        return baseDBC.getSuperClassName();
    }

    public boolean isInterface() {
        return baseDBC.isInterface();
    }
    
    public Definition getWsdlDefinition() {
        return baseDBC.getWsdlDefinition();
    }
    
    public Set<QName> getServiceQNames() {
        return baseDBC.getServiceQNames();
    }

    public Definition getWsdlDefinition(QName serviceQName) {
        return baseDBC.getWsdlDefinition(serviceQName);
    }

    public void setServiceQNames(Set<QName> serviceQNames) {
        baseDBC.setServiceQNames(serviceQNames);
    }

    public void setWsdlDefinition(QName serviceQName, Definition definition) {
        baseDBC.setWsdlDefinition(serviceQName, definition);
    }
    
    public void setwsdlURL(QName serviceQName, URL url) {
        baseDBC.setwsdlURL(serviceQName, url);
    }
    
    public URL getWsdlURL(QName serviceQName) {
        return baseDBC.getWsdlURL(serviceQName);
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        final String newLine = "\n";
        final String sameLine = "; ";
        sb.append(super.toString());
        sb.append(newLine);
        sb.append("ClassName: " + getClassName());

        sb.append(newLine);

        sb.append(newLine);
        sb.append("has wsdlDefinition?: ");
        if (getWsdlDefinition() !=null) {
            sb.append("true");
        } else {
            sb.append("false");
        }

        if (wsAnnot != null) {
            sb.append(newLine);
            sb.append("WebService: ");
            sb.append(wsAnnot.toString());
        }

        if (wspAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceProvider: ");
            sb.append(wspAnnot.toString());
        }

        if (btAnnot != null) {
            sb.append(newLine);
            sb.append("BindingType: ");
            sb.append(btAnnot.toString());
        }
        
        if (hcAnnot != null) {
            sb.append(newLine);
            sb.append("HandlerChain: ");
            sb.append(hcAnnot.toString());
        }
        
        return sb.toString();
    }

}
