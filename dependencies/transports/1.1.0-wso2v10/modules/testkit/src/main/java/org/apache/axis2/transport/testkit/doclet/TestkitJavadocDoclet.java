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
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.standard.Standard;

public class TestkitJavadocDoclet {
    private static File resourceInfoFile;
    
    public static LanguageVersion languageVersion() {
        return Standard.languageVersion();
    }
    
    public static int optionLength(String option) {
        if (option.equals("-resource-info")) {
            return 2;
        } else {
            return Standard.optionLength(option);
        }
    }
    
    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        return Standard.validOptions(options, reporter);
    }
    
    public static boolean start(RootDoc root) throws Exception {
        parseOptions(root.options());
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(resourceInfoFile));
        ResourceInfo resourceInfo = (ResourceInfo)in.readObject();
        in.close();
        for (ClassDoc clazz : root.classes()) {
            String qualifiedName = clazz.qualifiedName();
            List<Resource> usedBy = resourceInfo.getUsedBy(qualifiedName);
            Resource resource = resourceInfo.getResource(qualifiedName);
            List<Dependency> dependencies = resource == null ? null : resource.getDependencies();
            if (dependencies != null || usedBy != null) {
                StringBuilder buffer = new StringBuilder(clazz.getRawCommentText());
                buffer.append("<h2>Resource information</h2>");
                if (usedBy != null) {
                    buffer.append("This resource is used by: ");
                    boolean first = true;
                    for (Resource r : usedBy) {
                        if (first) {
                            first = false;
                        } else {
                            buffer.append(", ");
                        }
                        buffer.append("{@link ");
                        buffer.append(r.getType());
                        buffer.append("}");
                    }
                }
                if (dependencies != null) {
                    buffer.append("<h3>Dependencies</h3>");
                    buffer.append("<dl>");
                    for (Dependency dependency : dependencies) {
                        buffer.append("<dt>{@link ");
                        buffer.append(dependency.getType());
                        buffer.append("} (");
                        buffer.append(dependency.getMultiplicity());
                        buffer.append(")</dt><dd>");
                        String comment = dependency.getComment();
                        if (comment == null) {
                            buffer.append("(no documentation available)");
                        } else {
                            buffer.append(comment);
                        }
                        buffer.append("</dd>");
                    }
                    buffer.append("</dl>");
                }
                clazz.setRawCommentText(buffer.toString());
            }
        }
        return Standard.start(root);
    }

    private static void parseOptions(String[][] options) {
        for (String[] option : options) {
            if (option[0].equals("-resource-info")) {
                resourceInfoFile = new File(option[1]);
                System.out.println("Resource information is read from " + resourceInfoFile);
            }
        }
    }
}
