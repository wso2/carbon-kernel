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

import java.io.File;
import java.io.IOException;

/**
 * Sets up the libraries for the war distribution installer to use and to work 
 * on.
 */
public class LibExtractor {

    public static void main(String[] args) {
        System.out.println("Extracting libraries to lib directory...");
        File libDir = new File("lib");
        String temp = "temp-WAR";
        File tempDir = new File(temp);
        if(tempDir.exists()){
            FileManipulator.deleteDir(tempDir);
        }

        ArchiveManipulator am = new ArchiveManipulator();
        try {
            if(!libDir.mkdirs()){
                throw new IOException("Fail to create the directory: " + libDir.getAbsolutePath());
            }
            extract(am, temp, libDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Error occurred while copying libs: " + e);
            System.exit(1);
        }
    }

    private static void extract(ArchiveManipulator am,
                                String temp,
                                File libDir) throws IOException {
        am.extract("wso2wsas-war.zip", temp);
        FileManipulator.copyDir(new File(temp + File.separator + "WEB-INF" + File.separator + "lib"),
                        libDir);
        System.out.println("OK");
    }
}
