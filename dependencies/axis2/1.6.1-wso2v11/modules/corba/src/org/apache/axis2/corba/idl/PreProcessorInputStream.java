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

package org.apache.axis2.corba.idl;

import org.apache.axis2.corba.exceptions.PreProcessorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PreProcessorInputStream extends InputStream {

    public static int MAX_DEPTH = 99;

    protected String[] userIncludePaths;
    protected String[] systemIncludePaths;
    protected String currentFile;
    protected String parentPath;
    protected StringBuffer idlContent;
    protected int contentLength;
    protected int lastRead = 0;

    public PreProcessorInputStream(String parentPath, String idlFilename)
            throws PreProcessorException {
        this(parentPath, idlFilename, new String[] {}, new String[] {});
    }

    public PreProcessorInputStream(String parentPath, String idlFilename,
            String[] userIncludePaths, String[] systemIncludePaths)
            throws PreProcessorException {
        this.userIncludePaths = userIncludePaths;
        this.systemIncludePaths = systemIncludePaths;
        this.parentPath = parentPath;
        this.currentFile = parentPath + File.separator + idlFilename;
        InputStream idlStream = getInputStream(parentPath, idlFilename);
        if (idlFilename == null)
            throw new PreProcessorException("Cannot find the file "
                    + parentPath + File.separator + idlFilename);

        idlContent = readIdl(idlStream, 0);
        contentLength = idlContent.length();
    }

    protected StringBuffer readIdl(InputStream idlStream, int depth)
            throws PreProcessorException {
        StringBuffer buffer = new StringBuffer();
        LineNumberReader lineNumberReader = new LineNumberReader(
                new InputStreamReader(idlStream));
        lineNumberReader.setLineNumber(1);
        String line;
        Map defs = new HashMap();
        Stack ifdefValue = new Stack();
        boolean validLine = true;
        boolean elseNotValid = false;
        String lastLine = null;
        while (true) {
            try {
                line = lineNumberReader.readLine();
            } catch (IOException e) {
                throw new PreProcessorException("Error while reading next line"
                        + getLineNoString(lineNumberReader), e);
            }
            if (line == null) {
                break;
            } else if (line.startsWith("#")) {
                line = line.trim();
            }

            if (lastLine != null) {
                line = lastLine + " " + line;
                lastLine = null;
            }

            if (!ifdefValue.empty()
                    && !((Boolean) ifdefValue.peek()).booleanValue()
                    && !line.startsWith("#")) {
                continue;
            } else if (line.startsWith("#") && line.endsWith("\\")) {
                lastLine = line.substring(0, line.lastIndexOf("\\"));
                continue;
            } else if (line.startsWith("#include") && validLine) {
                if (depth < MAX_DEPTH) {
                    String inc = line.replaceAll("#include", "").trim();
                    addComment(buffer, line);
                    InputStream incis = resolveInclude(inc,
                            getLineNoString(lineNumberReader));
                    buffer.append(readIdl(incis, depth + 1));
                    addComment(buffer, "end of " + line);
                } else {
                    throw new PreProcessorException("More than " + MAX_DEPTH
                            + " nested #includes are not allowed"
                            + getLineNoString(lineNumberReader));
                }
            } else if (line.startsWith("#define")) {
                String def = line.replaceAll("#define", "").trim();
                def = def.replaceAll("\"", "");
                StringTokenizer tok = new StringTokenizer(def, " ");
                if (tok.countTokens() == 1) {
                    def = tok.nextToken();
                    if (defs.containsKey(def))
                        throw new PreProcessorException("Variable " + def
                                + " is already defined"
                                + getLineNoString(lineNumberReader));
                    defs.put(def, null);
                } else if (tok.countTokens() == 2) {
                    defs.put(tok.nextToken(), tok.nextToken());
                }
                addComment(buffer, line);
            } else if (line.startsWith("#undef")) {
                String def = line.replaceAll("#undef", "").trim();
                def = def.replaceAll("\"", "");
                if (defs.containsKey(def)) {
                    defs.remove(def);
                } else {
                    throw new PreProcessorException("Undifined variable " + def
                            + getLineNoString(lineNumberReader));
                }
                addComment(buffer, line);
            } else if (line.startsWith("#ifdef")) {
                String def = line.replaceAll("#ifdef", "").trim();
                def = def.replaceAll("\"", "");
                if (defs.containsKey(def)) {
                    ifdefValue.push(Boolean.TRUE);
                } else {
                    validLine = false;
                    ifdefValue.push(Boolean.FALSE);
                }
                elseNotValid = false;
                addComment(buffer, line);
            } else if (line.startsWith("#ifndef")) {
                String def = line.replaceAll("#ifndef", "").trim();
                def = def.replaceAll("\"", "");
                if (defs.containsKey(def)) {
                    validLine = false;
                    ifdefValue.push(Boolean.FALSE);
                } else {
                    ifdefValue.push(Boolean.TRUE);
                }
                elseNotValid = false;
                addComment(buffer, line);
            } else if (line.startsWith("#else")) {
                if (elseNotValid)
                    throw new PreProcessorException("Invalid #else preprocessor directive" + getLineNoString(lineNumberReader));
                    
                // invert last element
                boolean lastval = ((Boolean) ifdefValue.peek()).booleanValue();
                ifdefValue.setElementAt(new Boolean(!lastval), ifdefValue.size() - 1);
                validLine = isAllTrue(ifdefValue);
                elseNotValid = true;
                addComment(buffer, line);
            } else if (line.startsWith("#endif")) {
                if (ifdefValue.empty())
                    throw new PreProcessorException("Invalid #endif preprocessor directive" + getLineNoString(lineNumberReader));
                ifdefValue.pop();
                validLine = isAllTrue(ifdefValue);
                elseNotValid = false;
                addComment(buffer, line);
            } else if (line.startsWith("#")) {
                // TODO: log
                System.out
                        .println("Ignoring unsupported preprocessor directive "
                                + line + getLineNoString(lineNumberReader));
            } else if (validLine) {
                buffer.append(line);
                buffer.append('\n');
                // System.out.println(line);
            }
        }

        if (!ifdefValue.empty()) {
            throw new PreProcessorException(
                    "One or more #ifdef/#ifndef preprocessor directives are not properly closed" + getLineNoString(lineNumberReader));
        }

        try {
            lineNumberReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer;
    }

    protected InputStream resolveInclude(String include, String lineNoString)
            throws PreProcessorException {
        try {
            File incFile;
            if (include.startsWith("\"") && include.endsWith("\"")) {
                include = include.replaceAll("\"", "");
                incFile = new File(include);
                if (incFile.exists()) {
                    currentFile = incFile.getAbsolutePath();
                    return new FileInputStream(incFile);
                }

                InputStream stream = getInputStream(parentPath, include);
                if (stream != null)
                    return stream;

                for (int i = 0; i < userIncludePaths.length; i++) {
                    if (userIncludePaths[i].endsWith(File.separator)) {
                        incFile = new File(userIncludePaths[i] + include);
                    } else {
                        incFile = new File(userIncludePaths[i] + File.separator
                                + include);
                    }
                    if (incFile.exists()) {
                        currentFile = incFile.getAbsolutePath();
                        return new FileInputStream(incFile);
                    }
                }

                throw new PreProcessorException("Unable to resolve include "
                        + include + lineNoString);

            } else if (include.startsWith("<") && include.endsWith(">")) {
                include = include.replaceAll("<", "");
                include = include.replaceAll(">", "");

                for (int i = 0; i < systemIncludePaths.length; i++) {
                    if (systemIncludePaths[i].endsWith(File.separator)) {
                        incFile = new File(systemIncludePaths[i] + include);
                    } else {
                        incFile = new File(systemIncludePaths[i]
                                + File.separator + include);
                    }
                    if (incFile.exists()) {
                        currentFile = incFile.getAbsolutePath();
                        return new FileInputStream(incFile);
                    }
                }
                throw new PreProcessorException("Unable to resolve include "
                        + include + lineNoString);
            } else {
                throw new PreProcessorException(
                        "Include name must be enclosed in '< >' or '\" \"'"
                                + lineNoString);
            }
        } catch (FileNotFoundException e) {
            throw new PreProcessorException("Unable to resolve include "
                    + include + lineNoString, e);
        }
    }

    protected InputStream getInputStream(String parent, String filename)
            throws PreProcessorException {
        File parentFile = new File(parent);
        try {
            if (parentFile.isDirectory()) {
                return new FileInputStream(parent + File.separator + filename);
            } else {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(
                        parentFile));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(filename)) {
                        return zin;
                    }
                }
                return null;
            }
        } catch (IOException e) {
            throw new PreProcessorException("Unable to pre-process file " + parent
                    + File.separator + filename, e);
        }
    }

    private boolean isAllTrue(Stack stack) {
        if (stack.empty()) {
            return true;
        } else {
            for (Iterator iterator = stack.iterator(); iterator.hasNext();) {
                Boolean value = (Boolean) iterator.next();
                if (!value.booleanValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    private void addComment(StringBuffer buffer, String line) {
        buffer.append("/* ");
        buffer.append(line);
        buffer.append(" */\n");
    }

    private String getLineNoString(LineNumberReader lineNumberReader) {
        if (lineNumberReader != null) {
            int lineNo = lineNumberReader.getLineNumber();
            if (lineNo > 0) {
                return " (file:" + currentFile + ", line:" + (lineNo - 1) + ")";
            }
        }
        return "";
    }

    public int read() throws IOException {
        return (contentLength > lastRead) ? idlContent.charAt(lastRead++) : -1;
    }
}
