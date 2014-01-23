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

package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.FileWriter;

import java.io.File;
import java.io.FileOutputStream;

public class WSDL20Writer {
    private File baseFolder = null;


    public WSDL20Writer(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    public void writeWSDL(AxisService axisService) {
        try {
            if (axisService != null) {
                //create a output file
                File outputFile = FileWriter.createClassFile(baseFolder,
                                                             null,
                                                             axisService.getName(),
                                                             ".wsdl");
                FileOutputStream fos = new FileOutputStream(outputFile);
                axisService.printWSDL2(fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }


}
