/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils.codegen;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.WSDL2Java;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CodegenHelper {

    private static Log log = LogFactory.getLog(CodegenHelper.class);    

    private static final String SET = "set";

    private OMElement loadCodegenXML(String codegenXML) throws Exception {
        InputStream inStream =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(codegenXML);

        if (inStream == null) {
            // File is not available in classpath, thus, let it find from the file system
            File inFile = new File(codegenXML);
            if (!inFile.exists()) {
                throw new Exception("Null input stream. Codegen XML " + codegenXML +
                                    " file not found.");

            }
            inStream = new FileInputStream(inFile);
        }
        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        return builder.getDocumentElement();
    }

    private List parse(OMElement document) {
        List mainList = new ArrayList();
        for (Iterator childrenWithNameSetIte = document.getChildrenWithName(new QName(SET));
             childrenWithNameSetIte.hasNext();) {
            OMElement setElement = (OMElement) childrenWithNameSetIte.next();
            ArrayList arrayList = new ArrayList();

            for (Iterator iter = setElement.getChildElements(); iter.hasNext();) {
                OMElement ele = (OMElement) iter.next();
                arrayList.add("-" + ele.getLocalName());
                String value = ele.getText().trim();
                if (value.length() != 0) {
                    arrayList.add(value);
                }
            }
            mainList.add(arrayList);
        }
        return mainList;
    }

    public void execute(String codegenXML) throws Exception {

        OMElement document = loadCodegenXML(codegenXML);
        List setArrayList = parse(document);

        for (int i = 0; i < setArrayList.size(); i++) {
            ArrayList setList = (ArrayList) setArrayList.get(i);

            String[] wsdl2JavaArgs =
                    (String[]) setList.toArray(new String[setList.size()]);

            generateCode(wsdl2JavaArgs);

            if(log.isDebugEnabled()){
                log.debug(setArrayList.get(i) + " processed successfully.");
            }
        }
    }

    private static synchronized void generateCode(String[] wsdl2JavaArgs) throws Exception {
        WSDL2Java.main(wsdl2JavaArgs);
    }

    public static void main(String[] args) {
        System.out.println("Starting Codegen ...");
        if (args.length < 1) {
            System.out.println("Please provide the codegen XML file path");
        }
        System.out.println("Generating code for " + args[0]);
        try {
            CodegenHelper codegenHelper = new CodegenHelper();
            codegenHelper.execute(args[0]);
            System.out.println("Codegen completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

