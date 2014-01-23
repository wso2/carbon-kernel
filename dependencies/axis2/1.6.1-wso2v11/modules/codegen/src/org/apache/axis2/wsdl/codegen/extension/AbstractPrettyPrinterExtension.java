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

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractPrettyPrinterExtension extends AbstractCodeGenerationExtension {
    /** If the extension for property file changes it might effect this as well !!! */
    protected String fileExtension = "";

    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {

        //recurse through the output files and prettify them
        File outputFolder = configuration.getOutputLocation();
        prettify(outputFolder,configuration);


    }

    /**
     * Recursive procedure to prettify the files
     *
     * @param file
     */
    protected void prettify(File file, CodeGenConfiguration configuration) {

        List fileList = configuration.getOutputFileNamesList();
        String fileName = null;
        for (Iterator iter = fileList.iterator();iter.hasNext();){
            fileName = (String) iter.next();
            if (fileName.toLowerCase().endsWith(fileExtension)) {
                prettifyFile(new File(fileName));
            }
        }

    }

    /**
     * Implement this to call the proper pretty printers
     *
     * @param file
     */
    protected abstract void prettifyFile(File file);


}
