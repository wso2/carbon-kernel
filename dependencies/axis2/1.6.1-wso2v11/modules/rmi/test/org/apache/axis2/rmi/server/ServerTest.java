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

package org.apache.axis2.rmi.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.Service;
import org.apache.axis2.rmi.server.services.Service1;
import org.apache.axis2.rmi.wsdl.WSDL11DefinitionBuilder;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import java.io.FileWriter;
import java.io.IOException;


public class ServerTest {

    public static final String AXIS2_CONFIG_FILE = "modules/rmi/conf/axis2.xml";
    public static final String AXIS2_REPOSITORY_LOCATION = "modules/rmi/repository";

    public void deployAndStartService() {
        try {
            ConfigurationContext confContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            AXIS2_REPOSITORY_LOCATION, AXIS2_CONFIG_FILE);
            // add the service
//            Configurator configurator = new Configurator();
//            ClassDeployer classDeployer = new ClassDeployer(confContext, configurator);
//            classDeployer.deployClass(Service2.class);

            SimpleHTTPServer simpleHttpServer = new SimpleHTTPServer(confContext, 5555);
            simpleHttpServer.start();

            System.out.println("Server started on port 5555 ");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void testWSDL() {
        Configurator configurator = new Configurator();
        Service service = new Service(Service1.class, configurator);
        try {
            service.populateMetaData();
            service.generateSchema();
            WSDL11DefinitionBuilder definitionBuilder = new WSDL11DefinitionBuilder(service);
            Definition definition = definitionBuilder.generateWSDL();

            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            FileWriter fileWriter = new FileWriter("test.wsdl");
            wsdlWriter.writeWSDL(definition, fileWriter);

        } catch (MetaDataPopulateException e) {
            e.printStackTrace();
        } catch (SchemaGenerationException e) {
            e.printStackTrace();
        } catch (WSDLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new ServerTest().deployAndStartService();
//        new ServerTest().testWSDL();
    }
}
