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

package org.apache.axis2.wsdl;

import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;

public class WSDL2Code {


    public static void main(String[] args) throws Exception {
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        if (isOptionsValid(commandLineOptionParser)){
            new CodeGenerationEngine(commandLineOptionParser).generate();
        } else {
            printUsage();
        }
    }

    private static void printUsage() {

        System.out.println(CodegenMessages.getMessage("wsdl2code.arg"));
        System.out.println(CodegenMessages.getMessage("wsdl2code.arg1"));
        for (int i = 2; i <= 49; i++) {
            System.out.println("  " + CodegenMessages.getMessage("wsdl2code.arg" + i));
        }
    }


    private static boolean isOptionsValid(CommandLineOptionParser parser) {
        boolean isValid = true;
        if (parser.getInvalidOptions(new WSDL2JavaOptionsValidator()).size() > 0){
            isValid = false;
        }
        if (null == parser.getAllOptions().get(
                        CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION)){
            isValid = false;
        }
        return isValid;
    }

}
