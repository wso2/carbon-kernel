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

package org.apache.axis2.schema;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.axis2.schema.i18n.SchemaCompilerMessages.getMessage;

public class XSD2Java {
    private static Options options;
    private static CommandLine line;

    /**
     * for now the arguments this main method accepts is the source schema and the output
     * location
     *
     * @param args
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        options = new Options();
        options.addOption(OptionBuilder.withArgName(getMessage("schema.ns2p.argname"))
                                       .hasArgs(2)
                                       .withValueSeparator()
                                       .withDescription(getMessage("schema.ns2p.description"))
                                       .create("ns2p"));
        options.addOption(OptionBuilder.withArgName(getMessage("schema.mp.argname"))
                                       .hasArg()
                                       .withDescription(getMessage("schema.mp.description"))
                                       .create("mp"));
        options.addOption(OptionBuilder.withArgName(getMessage("schema.dp.argname"))
                                       .hasArg()
                                       .withDescription(getMessage("schema.dp.description"))
                                       .create("dp"));
        options.addOption(OptionBuilder.withDescription(getMessage("schema.h.description"))
                                       .create("h"));
        options.addOption(OptionBuilder.withArgName(getMessage("schema.p.argname"))
                                       .hasArg()
                                       .withDescription(getMessage("schema.p.description"))
                                       .create("p"));
        CommandLineParser parser = new GnuParser();
        try {
            line = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println(ex.getLocalizedMessage());
            System.out.println();
            printUsage();
            System.out.println(ex);
            System.exit(1);
        }
        args = line.getArgs();
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        } else {
            File outputFolder = new File(args[args.length-1]);
            for (int i=0; i<args.length-1; i++) {
                File xsdFile = new File(args[i]);
                // Only output a message if the user has specified more than one schema file
                if (args.length > 2) {
                    System.out.println(getMessage("schema.compiling", xsdFile.getName()));
                }
                compile(xsdFile, outputFolder);
            }
        }
    }
    
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getMessage("schema.usage"), options);
    }

    /**
     * @param xsdName
     * @param outputLocation
     */
    private static void compile(File xsdFile, File outputFolder) throws Exception {
            //load the current Schema through a file
            //first read the file into a DOM
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(xsdFile);

            //now read it to a schema
            XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            XmlSchema currentSchema = schemaCol.read(doc, xsdFile.toURI().toString(), null);

            if (outputFolder.exists()) {
                if (outputFolder.isFile()) {
                    throw new IOException(getMessage("schema.locationNotFolder"));
                }
            } else {
                outputFolder.mkdirs();
            }

            CompilerOptions compilerOptions = new CompilerOptions();
            compilerOptions.setOutputLocation(outputFolder);
            compilerOptions.setGenerateAll(true);

            Map ns2p = new HashMap();
            if (line.hasOption("ns2p")) {
                ns2p.putAll(line.getOptionProperties("ns2p"));
            }
            if (line.hasOption("dp")) {
                ns2p.put("", line.getOptionValue("dp"));
            }
            if (!ns2p.isEmpty()) {
                compilerOptions.setNs2PackageMap(ns2p);
            }
            
            if (line.hasOption("mp")) {
                compilerOptions.setMapperClassPackage(line.getOptionValue("mp"));
            }

            compilerOptions.setHelperMode(line.hasOption("h"));
            
            if (line.hasOption("p")) {
                compilerOptions.setPackageName(line.getOptionValue("p"));
            }
            
            //todo - this should come from the users preferences
             compilerOptions.setWrapClasses(false);

            //there's no point in not writing the classes here.
             compilerOptions.setWriteOutput(true);

//             compilerOptions.setUseWrapperClasses(true);

            SchemaCompiler compiler = new SchemaCompiler(compilerOptions);
            compiler.compile(currentSchema);
    }
}
