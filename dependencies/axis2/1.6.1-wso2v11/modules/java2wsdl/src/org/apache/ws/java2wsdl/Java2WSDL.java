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

package org.apache.ws.java2wsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOptionParser;
import org.apache.ws.java2wsdl.utils.Java2WSDLOptionsValidator;

public class Java2WSDL {
	private static Log log = LogFactory.getLog(Java2WSDL.class);
	
    public static void main(String[] args) throws Exception {
        Java2WSDLCommandLineOptionParser commandLineOptionParser = new Java2WSDLCommandLineOptionParser(
                args);
        //  validate the arguments
        validateCommandLineOptions(commandLineOptionParser);
        Java2WSDLCodegenEngine engine = new Java2WSDLCodegenEngine(commandLineOptionParser.getAllOptions());
        engine.generate();
        log.info("WSDL created at "+ engine.getOutputFile());
    }

    public static void printUsage() {
        System.out.println("Usage: java2wsdl [options] -cn <fully qualified class name>");
        System.out.println("\nwhere [options] include:");
        System.out.println("  -o <output location>                    output directory");
        System.out.println("  -of <output file name>                  output file name for the WSDL");
        System.out.println("  -sn <service name>                      service name");
        System.out.println("  -l <soap address>                       address of the port for the WSDL");
        System.out.println("  -cp <class path uri>                    list of classpath entries - (urls)");
        System.out.println("  -tn <target namespace>                  target namespace for service");
        System.out.println("  -tp <target namespace prefix>           target namespace prefix for service");
        System.out.println("  -stn <schema target namespace>          target namespace for schema");
        System.out.println("  -stp <schema target namespace prefix>   target namespace prefix for schema");
        System.out.println("  -st <binding style>                     style for the WSDL");
        System.out.println("  -u <binding use>                        use for the WSDL");
        System.out.println("  -nsg <class name>                       fully qualified name of a class that implements NamespaceGenerator");
        System.out.println("  -sg <class name>                        fully qualified name of a class that implements SchemaGenerator");
        System.out.println("  -p2n [<java package>,<namespace] [<java package>,<namespace]... ");
        System.out.println("                                          java package to namespace mapping for argument and return types");
        System.out.println("  -p2n [all, <namespace>]                 to assign all types to a single namespace");
        System.out.println("  -efd <qualified/unqualified>            setting for elementFormDefault (defaults to qualified)");
        System.out.println("  -afd <qualified/unqualified>            setting for attributeFormDefault (defaults to qualified)");
        System.out.println("  -xc class1 -xc class2...                extra class(es) for which schematype must be generated.  ");
        System.out.println("  -wv <1.1/2.0>                           wsdl version - defaults to 1.1 if not specified");
        System.out.println("  -dlb                                    generate schemas conforming to doc/lit/bare style");
        System.out.println("  -dne                                    disallow nillable elements in the generated schema");
        System.out.println("  -disableSOAP11                          disable binding generation for SOAP11");
        System.out.println("  -disableSOAP12                          disable binding generation for SOAP12");
        System.out.println("  -disableREST                            disable binding generation for REST");
        System.out.println("  -mpn <messagePartName>                  change the part name of the generated wsdl messages");
        System.out.println("  -ptn <portTypeName>                     port Type name of the WSDL");
        System.out.println("  -soap11BindingName                      soap11 binding name of the WSDL");
        System.out.println("  -soap12BindingName                      soap 12 binding name of the WSDL");
        System.out.println("  -restBindingName                        rest binding name of the WSDL");
        System.out.println("  -res <requestElementSuffix>             Adds a suffix to the request elemment");
        System.out.println("  -dat <disallowAnonymousTypes>           Creates a named complex type for the annonymous complex type");
        System.exit(0);
    }


    private static void validateCommandLineOptions(
            Java2WSDLCommandLineOptionParser parser) {
        if (parser.getAllOptions().size() == 0) {
            printUsage();
        } else if (parser.getInvalidOptions(new Java2WSDLOptionsValidator()).size() > 0) {
            printUsage();
        }

    }

}

