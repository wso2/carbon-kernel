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

package org.apache.axis2.rmi.deploy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.metadata.Service;
import org.apache.axis2.rmi.receiver.RMIMessageReciever;
import org.apache.axis2.rmi.wsdl.WSDL11DefinitionBuilder;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;


public class ClassDeployer {

    private ConfigurationContext configurationContext;
    private Configurator configurator;
    private ClassLoader classLoader;

    public ClassDeployer(ConfigurationContext configurationContext,
                         ClassLoader classLoader) {
        this(configurationContext, classLoader, new Configurator());
    }

    public ClassDeployer(ConfigurationContext configurationContext,
                         ClassLoader classLoader,
                         Configurator configurator) {
        this.configurationContext = configurationContext;
        this.classLoader = classLoader;
        this.configurator = configurator;
    }

    public ClassDeployer(ConfigurationContext configurationContext) {
        this(configurationContext, new Configurator());
    }

    public ClassDeployer(ConfigurationContext configurationContext,
                         Configurator configurator) {
        this.configurationContext = configurationContext;
        this.configurator = configurator;
    }

    public void deployClass(Class serviceClass) throws AxisFault {
        Service service = new Service(serviceClass, this.configurator);
        try {
            service.populateMetaData();
            service.generateSchema();
            WSDL11DefinitionBuilder definitionBuilder = new WSDL11DefinitionBuilder(service);
            Definition definition = definitionBuilder.generateWSDL();

            WSDL11ToAxisServiceBuilder builder = new WSDL11ToAxisServiceBuilder(definition, null, null);
            AxisService axisService = builder.populateService();
            axisService.setClassLoader(this.classLoader);
            axisService.addParameter(new Parameter("useOriginalwsdl", "true"));
            axisService.addParameter(new Parameter("modifyUserWSDLPortAddress", "true"));
            
            List operations = service.getOperations();
            Operation operation = null;
            QName qName = null;
            AxisOperation axisOperation = null;
            RMIMessageReciever messageReciever = new RMIMessageReciever(service);
            for (Iterator iter = operations.iterator(); iter.hasNext();) {
                operation = (Operation) iter.next();
                qName = new QName(operation.getNamespace(), operation.getName());
                axisOperation = axisService.getOperation(qName);
                axisOperation.setMessageReceiver(messageReciever);
            }

            configurationContext.deployService(axisService);
        } catch (MetaDataPopulateException e) {
            throw AxisFault.makeFault(e);
        } catch (SchemaGenerationException e) {
            throw AxisFault.makeFault(e);
        }

    }

}
