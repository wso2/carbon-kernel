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

package org.apache.axis2.rmi.deploy.config;

public class Config {

   private Services services;
   private ExtensionClasses extensionClasses;
   private PackageToNamespaceMapings packageToNamespaceMapings;
   private String simpleDataHandlerClass;
   private CustomClassInfo customClassInfo;

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public ExtensionClasses getExtensionClasses() {
        return extensionClasses;
    }

    public void setExtensionClasses(ExtensionClasses extensionClasses) {
        this.extensionClasses = extensionClasses;
    }

    public PackageToNamespaceMapings getPackageToNamespaceMapings() {
        return packageToNamespaceMapings;
    }

    public void setPackageToNamespaceMapings(PackageToNamespaceMapings packageToNamespaceMapings) {
        this.packageToNamespaceMapings = packageToNamespaceMapings;
    }

    public String getSimpleDataHandlerClass() {
        return simpleDataHandlerClass;
    }

    public void setSimpleDataHandlerClass(String simpleDataHandlerClass) {
        this.simpleDataHandlerClass = simpleDataHandlerClass;
    }

    public CustomClassInfo getCustomClassInfo() {
        return customClassInfo;
    }

    public void setCustomClassInfo(CustomClassInfo customClassInfo) {
        this.customClassInfo = customClassInfo;
    }

}
