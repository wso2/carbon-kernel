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

package org.apache.axis2.tools.java2wsdl;


public class FileFilter {

    public boolean accept(String fileName ) {

        String   extension = getExtension(fileName);
        if (extension != null && extension.equals("wsdl")) {
            return  true;
        }else if(extension != null && extension.equals("xml")) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return ".wsdl";
    }

    private String getExtension(String  extension) {
        String ext = null;
        int i = extension.lastIndexOf('.');

        if (i > 0 && i < extension.length() - 1) {
            ext = extension.substring(i + 1).toLowerCase();
        }
        return ext;
    }

}
