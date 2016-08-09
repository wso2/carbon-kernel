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
package org.wso2.carbon.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 */
public class FileUtil {

    public static boolean copyDirectory(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            System.out.println("Creating directory \"" + destDir.getAbsolutePath() + "\"");
        } else {
            System.out.println("Copying \"" + srcDir.getAbsolutePath() +
                               "\" to \"" + destDir.getAbsolutePath() + "\"");
        }
        boolean replaceDir = false;
        if (destDir.exists()) {
            String delete;
            do {
                System.out.print(destDir.getAbsolutePath() +
                                 " already exists. Do you want to replace this directory(y/n)? [y]: ");
                delete = InputReader.readInput();
            }
            while (!delete.equalsIgnoreCase("y") && !delete.equalsIgnoreCase("n") &&
                   !(delete.trim().length() == 0));
            if (delete.equalsIgnoreCase("y") || delete.trim().length() == 0) {
                FileManipulator.deleteDir(destDir);
                if (!destDir.mkdirs()) {
                    throw new IOException("Fail to create the directory: " + destDir.getAbsolutePath());
                }
                if (srcDir != null) {
                    try {
                        FileManipulator.copyDir(srcDir, destDir);
                    } catch (IOException e) {
                        System.err.println(" Could not copy " + srcDir.getName() +
                                           " dir: " + e);
                        System.exit(1);
                    }
                }
                replaceDir = true;
            }
        } else {
            if (!destDir.mkdirs()) {
                throw new IOException("Fail to create the file: " + destDir.getAbsolutePath());
            }

            if (srcDir != null) {
                try {
                    FileManipulator.copyDir(srcDir, destDir);
                } catch (IOException e) {
                    System.err.println(" Could not copy " + srcDir.getName() +
                                       " dir: " + e);
                    System.exit(1);
                }
            }
            replaceDir = true;
        }
        System.out.println("OK");
        return replaceDir;
    }

    public static String readFileToString(String path) throws IOException {
        StringBuffer fileContent = new StringBuffer(1000);
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                fileContent.append(buf, 0, numRead);
            }
        }
        return fileContent.toString();
    }

}
