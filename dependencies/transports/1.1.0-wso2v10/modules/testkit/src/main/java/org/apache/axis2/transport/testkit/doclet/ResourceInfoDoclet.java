/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

public class ResourceInfoDoclet {
    private static File outFile;
    
    public static boolean start(RootDoc root) throws IOException {
        parseOptions(root.options());
        ResourceInfo resourceInfo = new ResourceInfo();
        for (ClassDoc clazz : root.classes()) {
            Resource resource = null;
            for (MethodDoc method : clazz.methods()) {
                if (getAnnotation(method, "org.apache.axis2.transport.testkit.tests.Setup") != null) {
                    if (resource == null) {
                        resource = new Resource(clazz.qualifiedName());
                    }
                    ParamTag[] paramTags = method.paramTags();
                    for (Parameter parameter : method.parameters()) {
                        Type type = parameter.type();
                        String name = parameter.name();
                        String comment = null;
                        for (ParamTag paramTag : paramTags) {
                            if (paramTag.parameterName().equals(name)) {
                                comment = paramTag.parameterComment();
                                break;
                            }
                        }
                        if (comment == null) {
                            comment = getFirstSentence(root.classNamed(type.qualifiedTypeName()));
                        }
                        resource.addDependency(type.qualifiedTypeName(),
                                               type.dimension().equals("[]") ? "0..*" : "1", 
                                               comment);
                    }
                }
            }
            if (resource != null) {
                resourceInfo.addResource(resource);
            }
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
        out.writeObject(resourceInfo);
        out.close();
        return true;
    }
    
    private static AnnotationDesc getAnnotation(ProgramElementDoc doc, String qualifiedName) {
        for (AnnotationDesc annotation : doc.annotations()) {
            if (annotation.annotationType().qualifiedName().equals(qualifiedName)) {
                return annotation;
            }
        }
        return null;
    }
    
    private static String getFirstSentence(Doc doc) {
        Tag[] tags = doc.firstSentenceTags();
        if (tags.length == 0) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (Tag tag : tags) {
            if (tag instanceof SeeTag) {
                buffer.append("{");
                buffer.append(tag.name());
                buffer.append(" ");
                buffer.append(((SeeTag)tag).referencedClassName());
                buffer.append("}");
            } else {
                buffer.append(tag.text());
            }
        }
        return buffer.toString();
    }

    private static void parseOptions(String[][] options) {
        for (String[] option : options) {
            if (option[0].equals("-out")) {
                outFile = new File(option[1]);
                System.out.println("Output is going to " + outFile);
            }
        }
    }

    public static int optionLength(String option) {
        if (option.equals("-out")) {
            return 2;
        } else {
            return 0;
        }
    }
    
    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        boolean hasOut = false;
        for (String[] option : options) {
            String opt = option[0];
            if (opt.equals("-out")) {
                hasOut = true;
            }
        }
        if (!hasOut) {
            reporter.printError("No output file specified: -out <output file>");
            return false;
        } else {
            return true;
        }
    }
}
