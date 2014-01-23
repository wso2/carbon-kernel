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

import java.util.ArrayList;

public class Page2Bean {
    private boolean manual = false;
    private boolean automatic = false;

    private String manualFileName;
    private String automaticClassName;
    private String providerClassName;

    private ArrayList selectedMethodNames;
    private String serviceName;
    
    
    /**
     * @return Returns the serviceName.
     */
    public String getServiceName() {
        return serviceName;
    }
    /**
     * @param serviceName The serviceName to set.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getProviderClassName() {
        return providerClassName;
    }

    public void setProviderClassName(String providerClassName) {
        this.providerClassName = providerClassName;
    }

    public Page2Bean() {
        selectedMethodNames = new ArrayList();
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public String getManualFileName() {
        return manualFileName;
    }

    public void setManualFileName(String manualFileName) {
        this.manualFileName = manualFileName;
    }

    public String getAutomaticClassName() {
        return automaticClassName;
    }

    public void setAutomaticClassName(String automaticClassName) {
        this.automaticClassName = automaticClassName;
    }

    public int getMethodNameCount() {
        return selectedMethodNames.size();
    }

    public void setSelectedMethodNames(ArrayList list) {
        this.selectedMethodNames = list;
    }

    public String getMethodName(int index) {
        return selectedMethodNames.get(index).toString();
    }

    public void addMethodName(String selectedMethodName) {
        this.selectedMethodNames.add(selectedMethodNames);
    }

    public ArrayList getSelectedMethodNames() {
        return selectedMethodNames;
    }
}
