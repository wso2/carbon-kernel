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

package org.apache.axis2.tool.service.bean;

public class WizardBean {
    private ClassFileSelectionBean classFileBean;
    private WSDLFileLocationBean wsdlBean;
    private LibrarySelectionBean libraryBean;
    
    private Page2Bean page2bean;
    private Page3Bean page3bean;
    

    /**
     * @return Returns the libraryBean.
     */
    public LibrarySelectionBean getLibraryBean() {
        return libraryBean;
    }
    /**
     * @param libraryBean The libraryBean to set.
     */
    public void setLibraryBean(LibrarySelectionBean libraryBean) {
        this.libraryBean = libraryBean;
    }
    
    /**
     * @return Returns the wsdlBean.
     */
    public WSDLFileLocationBean getWsdlBean() {
        return wsdlBean;
    }
    /**
     * @param wsdlBean The wsdlBean to set.
     */
    public void setWsdlBean(WSDLFileLocationBean wsdlBean) {
        this.wsdlBean = wsdlBean;
    }
    public ClassFileSelectionBean getPage1bean() {
        return classFileBean;
    }

    public void setPage1bean(ClassFileSelectionBean page1bean) {
        this.classFileBean = page1bean;
    }

    public Page2Bean getPage2bean() {
        return page2bean;
    }

    public void setPage2bean(Page2Bean page2bean) {
        this.page2bean = page2bean;
    }

    public Page3Bean getPage3bean() {
        return page3bean;
    }

    public void setPage3bean(Page3Bean page3bean) {
        this.page3bean = page3bean;
    }

}
