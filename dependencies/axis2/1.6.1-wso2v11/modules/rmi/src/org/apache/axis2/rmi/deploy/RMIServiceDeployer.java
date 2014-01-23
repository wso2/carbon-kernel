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

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.databind.SimpleTypeHandler;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.deploy.config.ClassInfo;
import org.apache.axis2.rmi.deploy.config.Config;
import org.apache.axis2.rmi.deploy.config.CustomClassInfo;
import org.apache.axis2.rmi.deploy.config.FieldInfo;
import org.apache.axis2.rmi.deploy.config.PackageToNamespaceMap;
import org.apache.axis2.rmi.deploy.config.Service;
import org.apache.axis2.rmi.exception.ConfigFileReadingException;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class RMIServiceDeployer implements Deployer {

    private static Log log = LogFactory.getLog(RMIServiceDeployer.class);
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;

    public void init(ConfigurationContext configCtx) {
        this.configurationContext = configCtx;
        this.axisConfiguration = this.configurationContext.getAxisConfiguration();

    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        boolean isDirectory = deploymentFileData.getFile().isDirectory();
        try {
            deploymentFileData.setClassLoader(isDirectory,
                                              this.axisConfiguration.getServiceClassLoader(),
                    (File)this.axisConfiguration.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));

            ClassLoader deploymentClassLoader = deploymentFileData.getClassLoader();
            String absolutePath = deploymentFileData.getFile().getAbsolutePath();

            // gettting the file reader for zipinput stream
            Config configObject = getConfig(absolutePath);

            Configurator configurator = getConfigurator(configObject, deploymentClassLoader);

            Service[] services = configObject.getServices().getService();
            ClassDeployer classDeployer =
                    new ClassDeployer(configurationContext, deploymentClassLoader, configurator);
            Class serviceClass;

            for (int i = 0; i < services.length; i++) {
                serviceClass = Loader.loadClass(deploymentClassLoader, services[i].getServiceClass());
                classDeployer.deployClass(serviceClass);
            }
            log.info("Deployed RMI Services with deployment file " + deploymentFileData.getName());

        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Service class not found", e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException("axis fault", axisFault);
        } catch (IOException e) {
            throw new DeploymentException("zip file not found", e);
        } catch (ConfigFileReadingException e) {
            throw new DeploymentException("config file reading problem", e);
        }
    }

    private Configurator getConfigurator(Config configObject, ClassLoader deploymentClassLoader)
            throws ClassNotFoundException, DeploymentException {
        Configurator configurator = new Configurator();

        if (configObject.getExtensionClasses() != null) {
            String[] extensionClasses = configObject.getExtensionClasses().getExtensionClass();
            if (extensionClasses != null) {
                Class extensionClass;
                for (int i = 0; i < extensionClasses.length; i++) {
                    extensionClass = Loader.loadClass(deploymentClassLoader, extensionClasses[i]);
                    configurator.addExtension(extensionClass);
                }
            }
        }

        if (configObject.getPackageToNamespaceMapings() != null) {
            PackageToNamespaceMap[] packageToNamespaceMapings =
                    configObject.getPackageToNamespaceMapings().getPackageToNamespaceMap();
            if (packageToNamespaceMapings != null) {
                for (int i = 0; i < packageToNamespaceMapings.length; i++) {
                    configurator.addPackageToNamespaceMaping(packageToNamespaceMapings[i].getPackageName(),
                            packageToNamespaceMapings[i].getNamespace());
                }
            }
        }

        // set the simple type data handler if it is set
        if ((configObject.getSimpleDataHandlerClass() != null)
                && (configObject.getSimpleDataHandlerClass().trim().length() > 0)){
            Class simpleTypeHandlerClass =
                    Loader.loadClass(deploymentClassLoader,configObject.getSimpleDataHandlerClass());
            try {
                SimpleTypeHandler simpleTypeHandler = (SimpleTypeHandler) simpleTypeHandlerClass.newInstance();
                configurator.setSimpleTypeHandler(simpleTypeHandler);
            } catch (InstantiationException e) {
                throw new DeploymentException("Can not instantiate simple type handler",e);
            } catch (IllegalAccessException e) {
                throw new DeploymentException("Can not instantiate simple type handler",e);
            }
        }

        // setting the custom class info
        ClassInfo[] classInfos = null;
        FieldInfo[] filsInfos = null;
        org.apache.axis2.rmi.config.ClassInfo classInfo;
        Class customClass = null;
        if (configObject.getCustomClassInfo() != null) {
            CustomClassInfo customClassInfo = configObject.getCustomClassInfo();
            if ((customClassInfo.getClassInfo() != null) && (customClassInfo.getClassInfo().length > 0)) {
                classInfos = customClassInfo.getClassInfo();
                for (int i = 0; i < classInfos.length; i++) {

                    if ((classInfos[i].getFieldInfo() != null) &&
                            (classInfos[i].getFieldInfo().length > 0)){
                        customClass = Loader.loadClass(deploymentClassLoader,classInfos[i].getClassName());
                        classInfo = new org.apache.axis2.rmi.config.ClassInfo(customClass);
                        filsInfos = classInfos[i].getFieldInfo();
                        for (int j = 0; j < filsInfos.length; j++) {
                           classInfo.addFieldInfo(
                                   new org.apache.axis2.rmi.config.FieldInfo(
                                           filsInfos[j].getJavaName(),
                                           filsInfos[j].getXmlName(),
                                           filsInfos[j].isElement()));
                        }
                        configurator.addClassInfo(classInfo);
                    }
                }
            }
        }
        return configurator;
    }

    private Config getConfig(String zipFilePath) throws ConfigFileReadingException {
        try {
            InputStream configFileInputStream = getConfigFileInputStream(zipFilePath);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(configFileInputStream);

            Configurator configurator = new Configurator();
            configurator.addPackageToNamespaceMaping("org.apache.axis2.rmi.deploy.config",
                    "http://ws.apache.org/axis2/rmi");

            // add configurator details to handle custom class info
            org.apache.axis2.rmi.config.ClassInfo classInfo = new org.apache.axis2.rmi.config.ClassInfo(FieldInfo.class);
            classInfo.addFieldInfo(new org.apache.axis2.rmi.config.FieldInfo("javaName", null, false));
            classInfo.addFieldInfo(new org.apache.axis2.rmi.config.FieldInfo("xmlName", null, false));
            classInfo.addFieldInfo(new org.apache.axis2.rmi.config.FieldInfo("element", "isElement", false));
            configurator.addClassInfo(classInfo);

            classInfo = new org.apache.axis2.rmi.config.ClassInfo(ClassInfo.class);
            classInfo.addFieldInfo(new org.apache.axis2.rmi.config.FieldInfo("className", null, false));
            configurator.addClassInfo(classInfo);


            Map processedTypeMap = new HashMap();
            Map processedSchemaMap = new HashMap();
            Parameter parameter = new Parameter(Config.class, "config");
            parameter.setNamespace("http://ws.apache.org/axis2/rmi");
            parameter.populateMetaData(configurator, processedTypeMap);
            parameter.generateSchema(configurator, processedSchemaMap);

            XmlStreamParser xmlStreamParser = new XmlStreamParser(processedTypeMap, configurator, processedSchemaMap);
            return (Config) xmlStreamParser.getObjectForParameter(xmlReader, parameter);
        } catch (IOException e) {
            throw new ConfigFileReadingException("Can not read configuration file", e);
        } catch (XMLStreamException e) {
            throw new ConfigFileReadingException("xml stream exception with configuration file", e);
        } catch (MetaDataPopulateException e) {
            throw new ConfigFileReadingException("metadata population problem with configuration file", e);
        } catch (XmlParsingException e) {
            throw new ConfigFileReadingException("xml stream reading problem with configuration file", e);
        } catch (SchemaGenerationException e) {
            throw new ConfigFileReadingException("problem in generating schema", e);
        }
    }

    private InputStream getConfigFileInputStream(String zipFilePath) throws IOException {

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipEntry zipEntry;
        byte[] buffer = new byte[1024];
        int read;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals("META-INF" + File.separator + "config.xml")) {
                while ((read = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }
            }

        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public void setDirectory(String directory) {

    }

    public void setExtension(String extension) {

    }

    public void unDeploy(String fileName) throws DeploymentException {
       //TODO: implement undeploy
    }
}
