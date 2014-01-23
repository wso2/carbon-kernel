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

package org.apache.axis2.jaxrs;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestUtil {

    public static final String ToArchiveTestEPR="http://127.0.0.1:"+ UtilServer.TESTING_PORT+"/axis2/services/AnnotationService.AnnotationServiceHttpEndpoint/";
    public static final String ToPojoTestEPR="http://127.0.0.1:"+UtilServer.TESTING_PORT+"/axis2/services/PojoService.PojoServiceHttpEndpoint/";

    private static ServiceClient archiveTestserviceClient;
    private static ServiceClient pojoTestserviceClient;

    private static Options ArchiveTestOptions;
    private static Options pojoTestOptions;

    private static ConfigurationContext configContext;



    private static final Log log = LogFactory.getLog(TestUtil.class);


    public static ServiceClient getArchiveTestServiceClient(Options options){
        if(TestUtil.archiveTestserviceClient!=null){
            TestUtil.archiveTestserviceClient.setOptions(options);
            return TestUtil.archiveTestserviceClient;
        }else{

            try {
                configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);
            } catch (AxisFault axisFault) {
                log.error("could not create Configuration context : test failed",axisFault);

            }
            try {
                archiveTestserviceClient=new ServiceClient(configContext,null);
            } catch (AxisFault axisFault) {
                log.error("failed to create the service client : test failed",axisFault);
            }
             archiveTestserviceClient.setOptions(options);
            return archiveTestserviceClient;
        }


    }

    public static Options getArchiveTestOptions(){
        
         if(ArchiveTestOptions!=null){
             return ArchiveTestOptions;
         }else{
             ArchiveTestOptions=new Options();
             ArchiveTestOptions.setTo(new EndpointReference(TestUtil.ToArchiveTestEPR));
             ArchiveTestOptions.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
             ArchiveTestOptions.setCallTransportCleanup(true);
             return ArchiveTestOptions;
         }

    }


    public static ServiceClient getPojoTestServiceClient(Options options){
        if(TestUtil.pojoTestserviceClient!=null){
            TestUtil.pojoTestserviceClient.setOptions(options);
            return TestUtil.pojoTestserviceClient;
        }else{

            try {
                configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);
            } catch (AxisFault axisFault) {
                log.error("could not create Configuration context : test failed",axisFault);

            }
            try {
                pojoTestserviceClient=new ServiceClient(configContext,null);
            } catch (AxisFault axisFault) {
                log.error("failed to create the service client : test failed",axisFault);
            }
             pojoTestserviceClient.setOptions(options);
            return pojoTestserviceClient;
        }

    }  
        public static Options getPojoTestOptions(){

         if(pojoTestOptions!=null){
             return pojoTestOptions;
         }else{
             pojoTestOptions=new Options();
             pojoTestOptions.setTo(new EndpointReference(TestUtil.ToPojoTestEPR));
             pojoTestOptions.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
             pojoTestOptions.setCallTransportCleanup(true);
             return pojoTestOptions;
         }

    }

}
