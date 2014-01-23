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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jsr181.JSR181Helper;
import org.apache.axis2.jsr181.WebServiceAnnotation;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class POJODeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(POJODeployer.class);

    private ConfigurationContext configCtx;
    private String directory;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
    }//Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) {
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        // Get the hierarchical path of the service
        String serviceHierarchy = Utils.getServiceHierarchy(deploymentFileData.getAbsolutePath(), 
                this.directory);

        if (serviceHierarchy == null){
            serviceHierarchy = "";
        }
        
        try {
            String extension = DeploymentFileData.getFileExtension(deploymentFileData.getName());
            if ("class".equals(extension)) {
                File file = deploymentFileData.getFile();
                File parentFile = file.getParentFile();
                ClassLoader classLoader =
                        Utils.getClassLoader(configCtx.getAxisConfiguration().
                                getSystemClassLoader(), parentFile,
                                configCtx.getAxisConfiguration().isChildFirstClassLoading());

                Thread.currentThread().setContextClassLoader(classLoader);
                String className = Utils.getClassNameFromResourceName(file.getName());
                Class<?> clazz = Loader.loadClass(className);
                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_POJO,
                        serviceHierarchy + className,
                        deploymentFileData.getFile().getAbsolutePath()));


                /**
                 * Schema genertaion done in two stage 1. Load all the methods and
                 * create type for methods parameters (if the parameters are Bean
                 * then it will create Complex types for those , and if the
                 * parameters are simple type which decribe in SimpleTypeTable
                 * nothing will happen) 2. In the next stage for all the methods
                 * messages and port types will be creteated
                 */
                WebServiceAnnotation annotation =
                        JSR181Helper.INSTANCE.getWebServiceAnnotation(clazz);
                AxisService axisService;
                if (annotation != null) {
                    // try to see whether JAX-WS jars in the class path , if so use them
                    // to process annotated pojo else use annogen to process the pojo class
                    axisService =
                            createAxisService(classLoader,
                                    className,
                                    deploymentFileData.getFile().toURL());
                } else {
                    axisService =
                            createAxisServiceUsingAnnogen(className,
                                    classLoader,
                                    deploymentFileData.getFile().toURL());
                }
                //add the hierarchical path to the service name
                axisService.setName(serviceHierarchy + axisService.getName());
                configCtx.getAxisConfiguration().addService(axisService);

            } else if ("jar".equals(extension)) {
                List<String> classList = Utils.getListOfClasses(deploymentFileData);
                ArrayList<AxisService> axisServiceList = new ArrayList<AxisService>();
                for (String className : classList) {
                    ArrayList<URL> urls = new ArrayList<URL>();
                    urls.add(deploymentFileData.getFile().toURL());
                    urls.add(configCtx.getAxisConfiguration().getRepository());
                    String webLocation = DeploymentEngine.getWebLocationString();
                    if (webLocation != null) {
                        urls.add(new File(webLocation).toURL());
                    }
                    ClassLoader classLoader = Utils.createClassLoader(
                            urls,
                            configCtx.getAxisConfiguration().getSystemClassLoader(),
                            true,
                            (File)configCtx.getAxisConfiguration().
                                    getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                            configCtx.getAxisConfiguration().isChildFirstClassLoading());
                    Thread.currentThread().setContextClassLoader(classLoader);
                    Class<?> clazz = Loader.loadClass(className);

                    /**
                     * Schema generation done in two stage 1. Load all the methods and
                     * create type for methods parameters (if the parameters are Bean
                     * then it will create Complex types for those , and if the
                     * parameters are simple type which decribe in SimpleTypeTable
                     * nothing will happen) 2. In the next stage for all the methods
                     * messages and port types will be creteated
                     */
                    WebServiceAnnotation annotation =
                            JSR181Helper.INSTANCE.getWebServiceAnnotation(clazz);
                    if (annotation != null) {
                        AxisService axisService;
                        axisService =
                                createAxisService(classLoader,
                                                  className,
                                                  deploymentFileData.getFile().toURL());
                        axisServiceList.add(axisService);
                    }
                }

                if (axisServiceList.size() > 0) {
                    //create the service group considering the hierarchical path also
                    AxisServiceGroup serviceGroup = new AxisServiceGroup();
                    serviceGroup.setServiceGroupName(serviceHierarchy +
                            deploymentFileData.getName());
                    for (Object anAxisServiceList : axisServiceList) {
                        AxisService axisService = (AxisService)anAxisServiceList;
                        axisService.setName(serviceHierarchy + axisService.getName());
                        serviceGroup.addService(axisService);
                    }
                    configCtx.getAxisConfiguration().addServiceGroup(serviceGroup);
                } else {
                    String msg = "Error:\n No annotated classes found in the jar: " +
                            deploymentFileData.getFile().getName() +
                            ". Service deployment failed.";
                    log.error(msg);
                    configCtx.getAxisConfiguration().getFaultyServices().
                            put(deploymentFileData.getFile().getAbsolutePath(), msg);
                }
            }
            super.deploy(deploymentFileData);
        } catch (Exception e) {
            log.debug(Messages.getMessage(DeploymentErrorMsgs.STORING_FAULTY_SERVICE,e.getMessage()),e);
            storeFaultyService(deploymentFileData, e);
        } catch (Throwable t) {
            log.debug(Messages.getMessage(DeploymentErrorMsgs.STORING_FAULTY_SERVICE,t.getMessage()),t);
            storeFaultyService(deploymentFileData, t);
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }
    }

    private void storeFaultyService(DeploymentFileData deploymentFileData, Throwable t) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter ptintWriter = new PrintWriter(errorWriter);
        t.printStackTrace(ptintWriter);
        String error = "Error:\n" + errorWriter.toString();
        configCtx.getAxisConfiguration().getFaultyServices().
                put(deploymentFileData.getFile().getAbsolutePath(), error);
    }

    private AxisService createAxisService(ClassLoader classLoader,
                                          String className,
                                          URL serviceLocation) throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            AxisFault {
        AxisService axisService;
        try {
            Class<?> claxx = Class.forName(
                    "org.apache.axis2.jaxws.description.DescriptionFactory");
            Method mthod = claxx.getMethod("createAxisService", Class.class);
            Class<?> pojoClass = Loader.loadClass(classLoader, className);
            axisService = (AxisService) mthod.invoke(claxx, pojoClass);
            if (axisService != null) {
                Iterator<AxisOperation> operations = axisService.getOperations();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = operations.next();
                    if (axisOperation.getMessageReceiver() == null) {
                        try {
                            Class<?> jaxwsMR = Loader.loadClass(
                                    "org.apache.axis2.jaxws.server.JAXWSMessageReceiver");
                            MessageReceiver jaxwsMRInstance =
                                    (MessageReceiver) jaxwsMR.newInstance();
                            axisOperation.setMessageReceiver(jaxwsMRInstance);
                        } catch (Exception e) {
                            log.debug("Error occurde while loading JAXWSMessageReceiver for "
                                    + className );
                        }
                    }
                }
            }
            axisService.setElementFormDefault(false);
            axisService.setFileName(serviceLocation);
            Utils.fillAxisService(axisService,
                    configCtx.getAxisConfiguration(),
                    new ArrayList<String>(),
                    new ArrayList<String>());
            //Not needed at this case, the message receivers always set to RPC if this executes
            //setMessageReceivers(axisService);
            
        } catch (Exception e) {
            // Seems like the jax-ws jars missing in the class path .
            // lets try with annogen
            log.info(Messages.getMessage(DeploymentErrorMsgs.JAXWS_JARS_MISSING,e.getMessage()),e);
            axisService = createAxisServiceUsingAnnogen(className, classLoader, serviceLocation);
        }
        return axisService;
    }

    private AxisService createAxisServiceUsingAnnogen(String className,
                                                      ClassLoader classLoader,
                                                      URL serviceLocation)
            throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            AxisFault {
        HashMap<String,MessageReceiver> messageReciverMap = new HashMap<String,MessageReceiver>();
        Class<?> inOnlyMessageReceiver = Loader.loadClass(
                "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
        MessageReceiver messageReceiver =
                (MessageReceiver) inOnlyMessageReceiver.newInstance();
        messageReciverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
                messageReceiver);
        Class<?> inoutMessageReceiver = Loader.loadClass(
                "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
        MessageReceiver inOutmessageReceiver =
                (MessageReceiver) inoutMessageReceiver.newInstance();
        messageReciverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
                inOutmessageReceiver);
        messageReciverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
                inOutmessageReceiver);
        AxisService axisService =
                AxisService.createService(className,
                        configCtx.getAxisConfiguration(),
                        messageReciverMap,
                        null, null,
                        classLoader);
        axisService.setFileName(serviceLocation);
        return axisService;
    }

    public void setMessageReceivers(AxisService service) {
        Iterator<AxisOperation> iterator = service.getOperations();
        while (iterator.hasNext()) {
            AxisOperation operation = iterator.next();
            String MEP = operation.getMessageExchangePattern();
            if (MEP != null) {
                try {
                    if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)) {
                        Class<?> inOnlyMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
                        MessageReceiver messageReceiver =
                                (MessageReceiver) inOnlyMessageReceiver.newInstance();
                        operation.setMessageReceiver(messageReceiver);
                    } else {
                        Class<?> inoutMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
                        MessageReceiver inOutmessageReceiver =
                                (MessageReceiver) inoutMessageReceiver.newInstance();
                        operation.setMessageReceiver(inOutmessageReceiver);
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        //find the hierarchical part of the service group name
        String serviceHierarchy = Utils.getServiceHierarchy(fileName, this.directory);
        
        if (serviceHierarchy == null){
            serviceHierarchy = "";
        }
        
        fileName = Utils.getShortFileName(fileName);
        if (fileName.endsWith(".class")) {
            String className = Utils.getClassNameFromResourceName(fileName);
            className = serviceHierarchy + className;
            try {
                AxisServiceGroup serviceGroup =
                        configCtx.getAxisConfiguration().removeServiceGroup(className);
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                        className));
            } catch (AxisFault axisFault) {
                //May be a faulty service
                log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,axisFault.getMessage()),axisFault);
                configCtx.getAxisConfiguration().removeFaultyService(fileName);
            }
        } else if (fileName.endsWith(".jar")) {
            fileName = serviceHierarchy + fileName;
            try {
                AxisServiceGroup serviceGroup =
                        configCtx.getAxisConfiguration().removeServiceGroup(fileName);
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                        fileName));
            } catch (AxisFault axisFault) {
                //May be a faulty service
                log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,axisFault.getMessage()),axisFault);
                configCtx.getAxisConfiguration().removeFaultyService(fileName);
            }
        }
        super.undeploy(fileName);
    }
}

