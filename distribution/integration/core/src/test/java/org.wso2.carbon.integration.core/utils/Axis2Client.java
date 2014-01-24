/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.integration.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Axis2Client {

    static Process process;

    public static String fireClient(String requestString) {

        return fireClient(requestString, -1);
    }

    public static String fireClient(String requestString, int kilTime) {
        String log = "";
        String osName = "";
        try {
            Runtime runTime = Runtime.getRuntime();
            String carbonHome = System.getProperty("CARBON_HOME");

            String filePath = carbonHome + File.separator + "samples" + File.separator + "axis2Client";
            File file = new File(filePath);

            if (kilTime > 0) {
                Thread thread = new Thread(new ClientStopper(kilTime));
                thread.start();
            }

            //    Process process = runTime.exec( "ant stockquote -Daddurl=http://localhost:9000/services/SimpleStockQuoteService -Dtrpurl=http://localhost:8280/ ", null, file );
            try {
                osName = System.getProperty("os.name");
            } catch (Exception e) {
                System.out.println("Exception caught =" + e.getMessage());
            }

            if (osName.startsWith("Windows")) {
                process = runTime.exec("cmd.exe /C" + "" + requestString, null, file);
            } else {
                process = runTime.exec(requestString, null, file);
            }

            System.out.println("after executePath runs");


            InputStream inputstream = process.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);


            String line;
            while ((line = bufferedreader.readLine()) != null) {

                log += line;
                System.out.println(line);
                if (line.contains("org.apache.axis2.AxisFault")) {
                    process.destroy();
                }
            }
            System.out.println(process.waitFor());
            // dispose all the resources after using them.
            inputstream.close();
            inputstreamreader.close();
            bufferedreader.close();

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return log;
    }


    private static class ClientStopper implements Runnable {
        int killTime;

        ClientStopper(int killTime) {
            this.killTime = killTime;
        }

        public void run() {

            try {

                Thread.sleep(killTime);
                process.destroy();

            } catch (InterruptedException e) {
                System.out.println("operation failed..!");
            }
        }
    }


}
