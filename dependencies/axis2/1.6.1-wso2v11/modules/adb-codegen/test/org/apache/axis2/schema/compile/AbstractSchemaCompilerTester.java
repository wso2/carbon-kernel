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

package org.apache.axis2.schema.compile;

import junit.framework.TestCase;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompiler;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/* Maven is a little dumb when it comes to executing test cases. It tries to run everything
  that ends with 'test' even if the class is abstract or non junit!!!!!
  Rather than putting an explicit exclude, this is a cheaper way out
*/
public abstract class AbstractSchemaCompilerTester extends TestCase {

    //this should be an xsd name in the test-resource directory
    protected String fileName = "";
    protected  XmlSchema currentSchema;
    protected File outputFolder = null;

    private static String TEMP_OUT_FOLDER="temp_compile";

    protected void setUp() throws Exception {
        //load the current Schema through a file
        //first read the file into a DOM
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder builder =  documentBuilderFactory.newDocumentBuilder();
        Document doc = builder.parse(new File(System.getProperty("basedir",".")+"/"+fileName));

        //now read it to a schema
        XmlSchemaCollection schemaCol =  getSchemaReader();
        currentSchema = schemaCol.read(doc,null);

        outputFolder = new File(TEMP_OUT_FOLDER);
        if (outputFolder.exists()){
            if (outputFolder.isFile()){
                outputFolder.delete();
                outputFolder.mkdirs();
            }
        }else{
            outputFolder.mkdirs();
        }
    }

    protected  XmlSchemaCollection getSchemaReader(){
        return new XmlSchemaCollection();
    }

    public void testSchema() throws Exception{
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setOutputLocation(outputFolder);
        compilerOptions.setWrapClasses(false);
        compilerOptions.setWriteOutput(true);
        SchemaCompiler compiler = new SchemaCompiler(compilerOptions);
        compiler.compile(currentSchema);
       
    }

    protected void tearDown() throws Exception {
         deleteDir(outputFolder);
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
