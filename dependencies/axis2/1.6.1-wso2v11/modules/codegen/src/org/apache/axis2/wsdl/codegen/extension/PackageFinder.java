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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;

public class PackageFinder extends AbstractCodeGenerationExtension {


    public void engage(CodeGenConfiguration configuration) {
        String packageName = configuration.getPackageName();
        if (packageName == null || URLProcessor.DEFAULT_PACKAGE.equals(packageName)) {

            //use the target namespace from the axis service to form a package name
            // since all the services are in same name space
            String targetNameSpace = configuration.getTargetNamespace();

            // if this target name space exists in the ns2p then we have to get that package
            if ((configuration.getUri2PackageNameMap() != null) &&
                    configuration.getUri2PackageNameMap().containsKey(targetNameSpace.trim())) {
                packageName = (String)configuration.getUri2PackageNameMap().get(targetNameSpace);
            } else {
                // i.e. user have not given any ns2p information for this name space
                packageName = URLProcessor.makePackageName(configuration.getTargetNamespace());
                if (packageName != null) {
                    packageName = packageName.toLowerCase();
                }
            }

        }

        if ((packageName == null) || "".equals(packageName)) {
            packageName = URLProcessor.DEFAULT_PACKAGE;
        }

        configuration.setPackageName(packageName);
    }


}
