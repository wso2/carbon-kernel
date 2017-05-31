/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 */
public class FileListGenerator {
    public FileListGenerator() {
    }

    public void generate(String sourceDirName, String outputFile) throws ServerException {
        File sourceDir = new File(sourceDirName);
        if (!sourceDir.exists()) {
            throw new ServerException("Source directory " + sourceDirName + " does not exist.");
        } else if (!sourceDir.isDirectory()) {
            throw new ServerException("Source " + sourceDirName + " is not a directory.");
        }

        try {
            File outFile = new File(outputFile);

            if (outFile.exists() && !outFile.delete()) {
                throw new IOException("Fail to delete the file: " + outFile.getAbsolutePath());
            }

            if (!outFile.createNewFile()) {
                throw new IOException("Fail to create the file: " + outFile.getAbsolutePath());
            }
            try (FileWriter fileWriter = new FileWriter(outFile);
                 BufferedWriter buffWriter = new BufferedWriter(fileWriter)) {

                String[] files = sourceDir.list();
                for (int i = 0; i < files.length; i++) {
                    String file = files[i];
                    buffWriter.write(file);
                    if (i != files.length - 1) {
                        buffWriter.newLine();
                    }
                }
                buffWriter.flush();
            } catch (IOException e) {
            }
        } catch (IOException e) {
            throw new ServerException("Could not create new output file", e);
        }
    }

    public static void main(String[] args) throws ServerException {
        if (args.length < 2) {
            System.out.println("Usage: FileListGenerator <source-dir> <output-file>");
        }
        FileListGenerator fileListGenerator = new FileListGenerator();
        fileListGenerator.generate(args[0], args[1]);
    }
}
